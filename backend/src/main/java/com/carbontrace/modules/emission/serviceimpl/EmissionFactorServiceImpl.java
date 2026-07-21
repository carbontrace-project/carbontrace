package com.carbontrace.modules.emission.serviceimpl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.common.PagedResponse;
import com.carbontrace.exception.ResourceNotFoundException;
import com.carbontrace.modules.emission.dto.EmissionFactorRequestDto;
import com.carbontrace.modules.emission.dto.EmissionFactorResponseDto;
import com.carbontrace.modules.emission.entity.EmissionFactor;
import com.carbontrace.modules.emission.mapper.EmissionFactorMapper;
import com.carbontrace.modules.emission.repository.EmissionFactorRepository;
import com.carbontrace.modules.emission.service.EmissionFactorService;
import com.carbontrace.modules.shipment.entity.TransportMode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Emission-factor administration per COMMANDO.md Section 8.5.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmissionFactorServiceImpl implements EmissionFactorService {

    private static final String FACTOR_NOT_FOUND = "Emission factor not found with id: ";

    /** Newest first, by unique id so pages can never overlap — as in the other modules. */
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final EmissionFactorRepository emissionFactorRepository;
    private final EmissionFactorMapper emissionFactorMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmissionFactorResponseDto> getFactors(String region, TransportMode transportMode,
                                                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);

        // A blank region param means "no filter", not "match the empty string" —
        // the same treatment VendorServiceImpl gives its search parameter.
        String regionFilter = (region == null || region.isBlank()) ? null : region.trim();

        Page<EmissionFactor> result =
                emissionFactorRepository.findByOptionalFilters(regionFilter, transportMode, pageable);

        List<EmissionFactorResponseDto> content = result.getContent().stream()
                .map(emissionFactorMapper::toResponseDto)
                .toList();

        return new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast());
    }

    @Override
    @Transactional
    public EmissionFactorResponseDto createFactor(EmissionFactorRequestDto request) {
        // The (region, mode, fuel) uniqueness of Section 9 is enforced by the
        // database constraint, which surfaces as 409. No pre-check here: a
        // pre-check cannot win a race, and the constraint can.
        EmissionFactor factor = emissionFactorRepository.save(emissionFactorMapper.toEntity(request));

        log.info("Emission factor created: id={} {}/{}/{} factor={} circuity={}",
                factor.getId(), factor.getRegion(), factor.getTransportMode(), factor.getFuelType(),
                factor.getFactorKgco2ePerTonneKm(), factor.getCircuityFactor());
        return emissionFactorMapper.toResponseDto(factor);
    }

    @Override
    @Transactional
    public EmissionFactorResponseDto updateFactor(Long id, EmissionFactorRequestDto request) {
        EmissionFactor factor = findFactor(id);

        emissionFactorMapper.updateFactorFromDto(request, factor);
        emissionFactorRepository.save(factor);

        log.info("Emission factor updated: id={} {}/{}/{} factor={} circuity={}",
                factor.getId(), factor.getRegion(), factor.getTransportMode(), factor.getFuelType(),
                factor.getFactorKgco2ePerTonneKm(), factor.getCircuityFactor());
        return emissionFactorMapper.toResponseDto(factor);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Both directions are logged: deactivating a factor silently changes which
     * row every future calculation resolves to, so the change needs to be
     * visible in the log when a figure later looks wrong.
     */
    @Override
    @Transactional
    public EmissionFactorResponseDto toggleActive(Long id) {
        EmissionFactor factor = findFactor(id);

        boolean active = !Boolean.TRUE.equals(factor.getIsActive());
        factor.setIsActive(active);
        emissionFactorRepository.save(factor);

        log.info("Emission factor {}: id={} {}/{}/{}",
                active ? "activated" : "deactivated",
                factor.getId(), factor.getRegion(), factor.getTransportMode(), factor.getFuelType());
        return emissionFactorMapper.toResponseDto(factor);
    }

    private EmissionFactor findFactor(Long id) {
        return emissionFactorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Emission factor lookup failed for id={}", id);
                    return new ResourceNotFoundException(FACTOR_NOT_FOUND + id);
                });
    }
}
