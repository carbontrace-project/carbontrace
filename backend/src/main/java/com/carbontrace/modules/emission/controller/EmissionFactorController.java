package com.carbontrace.modules.emission.controller;

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
import com.carbontrace.modules.emission.dto.EmissionFactorRequestDto;
import com.carbontrace.modules.emission.dto.EmissionFactorResponseDto;
import com.carbontrace.modules.emission.service.EmissionFactorService;
import com.carbontrace.modules.shipment.entity.TransportMode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The four emission-factor endpoints of COMMANDO.md Section 8.5.
 *
 * <p>Authorization needs no annotation — it is already in the Section 10 filter
 * chain that {@code SecurityConfig} implements: {@code POST} and {@code PUT} on
 * {@code /api/emission-factors/**} require {@code ROLE_ADMIN}, while the
 * {@code GET} falls to {@code anyRequest().authenticated()} and is open to any
 * role. Adding {@code @PreAuthorize} would duplicate that matrix in a second
 * place, which is how the two drift apart.
 *
 * <p>There is no {@code @DeleteMapping}: deleting a factor would orphan the
 * provenance of every emissions figure already calculated from it. Section 8.5
 * offers {@code toggle-active} instead.
 */
@Slf4j
@RestController
@RequestMapping("/api/emission-factors")
@RequiredArgsConstructor
public class EmissionFactorController {

    // Section 8.5 quotes no messages; these follow the tone of the other modules.
    private static final String LIST_MESSAGE = "Emission factors retrieved";
    private static final String CREATE_MESSAGE = "Emission factor created successfully";
    private static final String UPDATE_MESSAGE = "Emission factor updated successfully";
    private static final String ACTIVATED_MESSAGE = "Emission factor activated";
    private static final String DEACTIVATED_MESSAGE = "Emission factor deactivated";

    private final EmissionFactorService emissionFactorService;

    /**
     * 200 — paged list with optional {@code region} and {@code transportMode}
     * filters. Any authenticated role.
     *
     * <p>{@code transportMode} binds straight to the enum, so an unknown value is
     * a 400 from the {@code MethodArgumentTypeMismatchException} handler rather
     * than a silently empty page.
     */
    @GetMapping
    public ApiResponse<PagedResponse<EmissionFactorResponseDto>> getFactors(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) TransportMode transportMode,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        log.info("Emission factor list requested: region={} transportMode={} page={} size={}",
                region, transportMode, page, size);
        return ApiResponse.success(LIST_MESSAGE,
                emissionFactorService.getFactors(region, transportMode, page, size));
    }

    /** 201 — creates a factor. ROLE_ADMIN only. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EmissionFactorResponseDto> createFactor(
            @Valid @RequestBody EmissionFactorRequestDto request) {
        log.info("Emission factor creation requested: {}/{}/{}",
                request.getRegion(), request.getTransportMode(), request.getFuelType());
        return ApiResponse.success(CREATE_MESSAGE, emissionFactorService.createFactor(request));
    }

    /** 200 — updates a factor's values. ROLE_ADMIN only. */
    @PutMapping("/{id}")
    public ApiResponse<EmissionFactorResponseDto> updateFactor(
            @PathVariable Long id, @Valid @RequestBody EmissionFactorRequestDto request) {
        log.info("Emission factor update requested: id={}", id);
        return ApiResponse.success(UPDATE_MESSAGE, emissionFactorService.updateFactor(id, request));
    }

    /** 200 — flips the active flag, which changes what future lookups resolve to. ROLE_ADMIN only. */
    @PutMapping("/{id}/toggle-active")
    public ApiResponse<EmissionFactorResponseDto> toggleActive(@PathVariable Long id) {
        log.info("Emission factor toggle-active requested: id={}", id);
        EmissionFactorResponseDto factor = emissionFactorService.toggleActive(id);
        return ApiResponse.success(
                Boolean.TRUE.equals(factor.getIsActive()) ? ACTIVATED_MESSAGE : DEACTIVATED_MESSAGE,
                factor);
    }
}
