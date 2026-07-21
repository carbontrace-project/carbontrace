package com.carbontrace.modules.emission.service;

import com.carbontrace.common.PagedResponse;
import com.carbontrace.modules.emission.dto.EmissionFactorRequestDto;
import com.carbontrace.modules.emission.dto.EmissionFactorResponseDto;
import com.carbontrace.modules.shipment.entity.TransportMode;

/**
 * The four emission-factor operations of COMMANDO.md Section 8.5.
 *
 * <p>There is no delete, and no lookup method. Deletion would orphan the
 * provenance of every figure already calculated against a row, so Section 8.5
 * offers deactivation instead. The Section 9 fallback LOOKUP lives in the
 * repository and is consumed by {@code CalculationService} at STEP A021 — adding
 * a service method for it now would be an unused abstraction.
 */
public interface EmissionFactorService {

    /**
     * Lists factors, newest first, filtered by whichever parameters are given.
     *
     * @param region        filter by region, or null for any
     * @param transportMode filter by mode, or null for any
     * @param page          zero-based page index
     * @param size          page size
     * @return one page of factors in the {@link PagedResponse} envelope
     */
    PagedResponse<EmissionFactorResponseDto> getFactors(String region, TransportMode transportMode,
                                                        int page, int size);

    /**
     * Creates a factor. Active by default; circuity defaults to 1.20 when omitted.
     *
     * @param request region, mode, fuel, factor, optional circuity and source
     * @return the persisted factor
     * @throws org.springframework.dao.DataIntegrityViolationException if
     *         {@code (region, transportMode, fuelType)} already exists — mapped
     *         to 409 by Section 23
     */
    EmissionFactorResponseDto createFactor(EmissionFactorRequestDto request);

    /**
     * Updates a factor's values. {@code isActive} is not reachable through this path.
     *
     * @param id      factor primary key
     * @param request the editable fields
     * @return the factor as stored after the update
     * @throws com.carbontrace.exception.ResourceNotFoundException if no factor has that id
     */
    EmissionFactorResponseDto updateFactor(Long id, EmissionFactorRequestDto request);

    /**
     * Flips {@code isActive}. Section 9: only active factors participate in the
     * calculation lookup, so this is how a factor is retired without destroying
     * the record of what past calculations used.
     *
     * @param id factor primary key
     * @return the factor with its new state
     * @throws com.carbontrace.exception.ResourceNotFoundException if no factor has that id
     */
    EmissionFactorResponseDto toggleActive(Long id);
}
