package com.carbontrace.modules.vendor.service;

import com.carbontrace.common.PagedResponse;
import com.carbontrace.modules.vendor.dto.VendorRequestDto;
import com.carbontrace.modules.vendor.dto.VendorResponseDto;

/**
 * The five vendor operations of COMMANDO.md Section 8.3.
 *
 * <p>There is no delete method, and there never will be: Section 9 states
 * vendors "are soft-deactivated (toggle-active), never deleted".
 */
public interface VendorService {

    /**
     * Creates a vendor, always as {@code LOGISTICS} and active (Section 9).
     *
     * @param request name, optional contact email, country
     * @return the persisted vendor
     */
    VendorResponseDto createVendor(VendorRequestDto request);

    /**
     * Lists vendors, newest first, optionally filtered by name.
     *
     * @param search optional case-insensitive name fragment; null or blank lists all
     * @param page   zero-based page index
     * @param size   page size
     * @return one page of vendors in the {@link PagedResponse} envelope
     */
    PagedResponse<VendorResponseDto> getVendors(String search, int page, int size);

    /**
     * @param id vendor primary key
     * @return the vendor
     * @throws com.carbontrace.exception.ResourceNotFoundException if no vendor has that id
     */
    VendorResponseDto getVendorById(Long id);

    /**
     * Updates name, contact email and country. Neither {@code vendorType} nor
     * {@code isActive} is reachable through this path.
     *
     * @param id      vendor primary key
     * @param request the editable fields
     * @return the vendor as stored after the update
     * @throws com.carbontrace.exception.ResourceNotFoundException if no vendor has that id
     */
    VendorResponseDto updateVendor(Long id, VendorRequestDto request);

    /**
     * Flips {@code isActive} — the soft-delete and the undo, in one endpoint.
     *
     * @param id vendor primary key
     * @return the vendor with its new state
     * @throws com.carbontrace.exception.ResourceNotFoundException if no vendor has that id
     */
    VendorResponseDto toggleActive(Long id);
}
