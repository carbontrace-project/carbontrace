package com.carbontrace.modules.vendor.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrace.common.ApiResponse;
import com.carbontrace.common.AppConstants;
import com.carbontrace.common.PagedResponse;
import com.carbontrace.modules.vendor.dto.VendorRequestDto;
import com.carbontrace.modules.vendor.dto.VendorResponseDto;
import com.carbontrace.modules.vendor.service.VendorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The five vendor endpoints of COMMANDO.md Section 8.3.
 *
 * <p>Authorization needs no annotation here — it is already in the Section 10
 * filter chain that {@code SecurityConfig} implements:
 * <ul>
 *   <li>{@code PUT /api/vendors/{id}} and {@code PUT /api/vendors/{id}/toggle-active}
 *       require {@code ROLE_ADMIN};</li>
 *   <li>the two GETs and the POST fall to {@code anyRequest().authenticated()},
 *       so any authenticated role reaches them.</li>
 * </ul>
 * Adding {@code @PreAuthorize} on top would duplicate that matrix in a second
 * place, which is exactly how the two drift apart.
 *
 * <p>There is no {@code @DeleteMapping}: Section 9 says vendors are never
 * deleted.
 */
@Slf4j
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private static final String CREATE_MESSAGE = "Vendor created successfully";
    private static final String LIST_MESSAGE = "Vendors retrieved";
    private static final String GET_MESSAGE = "Vendor retrieved";
    private static final String UPDATE_MESSAGE = "Vendor updated successfully";
    private static final String ACTIVATED_MESSAGE = "Vendor activated";
    private static final String DEACTIVATED_MESSAGE = "Vendor deactivated";

    private final VendorService vendorService;

    /** 201 — creates a LOGISTICS vendor. Any authenticated role. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VendorResponseDto> createVendor(@Valid @RequestBody VendorRequestDto request) {
        log.info("Vendor creation requested: name={}", request.getName());
        return ApiResponse.success(CREATE_MESSAGE, vendorService.createVendor(request));
    }

    /** 200 — paged list with an optional name filter. Any authenticated role. */
    @GetMapping
    public ApiResponse<PagedResponse<VendorResponseDto>> getVendors(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        log.info("Vendor list requested: search={} page={} size={}", search, page, size);
        return ApiResponse.success(LIST_MESSAGE, vendorService.getVendors(search, page, size));
    }

    /** 200 — a single vendor, or 404. Any authenticated role. */
    @GetMapping("/{id}")
    public ApiResponse<VendorResponseDto> getVendorById(@PathVariable Long id) {
        log.info("Vendor requested: id={}", id);
        return ApiResponse.success(GET_MESSAGE, vendorService.getVendorById(id));
    }

    /** 200 — updates name, contact email and country. ROLE_ADMIN only. */
    @PutMapping("/{id}")
    public ApiResponse<VendorResponseDto> updateVendor(@PathVariable Long id,
                                                       @Valid @RequestBody VendorRequestDto request) {
        log.info("Vendor update requested: id={}", id);
        return ApiResponse.success(UPDATE_MESSAGE, vendorService.updateVendor(id, request));
    }

    /** 200 — flips the active flag (the soft delete and its undo). ROLE_ADMIN only. */
    @PutMapping("/{id}/toggle-active")
    public ApiResponse<VendorResponseDto> toggleActive(@PathVariable Long id) {
        log.info("Vendor toggle-active requested: id={}", id);
        VendorResponseDto vendor = vendorService.toggleActive(id);
        return ApiResponse.success(
                Boolean.TRUE.equals(vendor.getIsActive()) ? ACTIVATED_MESSAGE : DEACTIVATED_MESSAGE,
                vendor);
    }
}
