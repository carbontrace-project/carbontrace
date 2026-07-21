package com.carbontrace.modules.vendor.serviceimpl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.common.PagedResponse;
import com.carbontrace.exception.ResourceNotFoundException;
import com.carbontrace.modules.vendor.dto.VendorRequestDto;
import com.carbontrace.modules.vendor.dto.VendorResponseDto;
import com.carbontrace.modules.vendor.entity.Vendor;
import com.carbontrace.modules.vendor.mapper.VendorMapper;
import com.carbontrace.modules.vendor.repository.VendorRepository;
import com.carbontrace.modules.vendor.service.VendorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Vendor CRUD per COMMANDO.md Section 8.3, with the Section 9 vendor rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private static final String VENDOR_NOT_FOUND = "Vendor not found with id: ";

    /**
     * Newest first. Section 8.3 does not specify an order, but an unordered
     * paged query is a bug: without a deterministic sort PostgreSQL may return
     * the same row on two different pages. {@code id} is used rather than
     * {@code createdAt} because it is unique, so it can never tie.
     */
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final VendorRepository vendorRepository;
    private final VendorMapper vendorMapper;

    @Override
    @Transactional
    public VendorResponseDto createVendor(VendorRequestDto request) {
        // vendorType and isActive are not mapped from the request — the entity's
        // own defaults (LOGISTICS, active) stand, per Section 9.
        Vendor vendor = vendorRepository.save(vendorMapper.toEntity(request));
        log.info("Vendor created: id={} name={} country={}", vendor.getId(), vendor.getName(), vendor.getCountry());
        return vendorMapper.toResponseDto(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VendorResponseDto> getVendors(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);

        // A missing search param and an empty one mean the same thing — list
        // everything — so "?search=" behaves like no filter rather than matching
        // only vendors whose name contains the empty string by accident.
        Page<Vendor> result = (search == null || search.isBlank())
                ? vendorRepository.findAll(pageable)
                : vendorRepository.findByNameContainingIgnoreCase(search.trim(), pageable);

        List<VendorResponseDto> content = result.getContent().stream()
                .map(vendorMapper::toResponseDto)
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
    @Transactional(readOnly = true)
    public VendorResponseDto getVendorById(Long id) {
        return vendorMapper.toResponseDto(findVendor(id));
    }

    @Override
    @Transactional
    public VendorResponseDto updateVendor(Long id, VendorRequestDto request) {
        Vendor vendor = findVendor(id);

        // Only name, contactEmail and country can arrive in the DTO, and the
        // mapper ignores every other target — vendorType stays LOGISTICS and
        // isActive stays with toggleActive (Section 8.3).
        vendorMapper.updateVendorFromDto(request, vendor);
        vendorRepository.save(vendor);

        log.info("Vendor updated: id={} name={}", vendor.getId(), vendor.getName());
        return vendorMapper.toResponseDto(vendor);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Deactivation is the closest thing to a delete this module has
     * (Section 9), so both directions are logged — an admin needs to be able to
     * see when a vendor stopped accepting shipments and when it started again.
     */
    @Override
    @Transactional
    public VendorResponseDto toggleActive(Long id) {
        Vendor vendor = findVendor(id);

        boolean active = !Boolean.TRUE.equals(vendor.getIsActive());
        vendor.setIsActive(active);
        vendorRepository.save(vendor);

        log.info("Vendor {}: id={} name={}", active ? "activated" : "deactivated", vendor.getId(), vendor.getName());
        return vendorMapper.toResponseDto(vendor);
    }

    private Vendor findVendor(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vendor lookup failed for id={}", id);
                    return new ResourceNotFoundException(VENDOR_NOT_FOUND + id);
                });
    }
}
