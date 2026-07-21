package com.carbontrace.modules.emission.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code POST /api/emission-factors} and
 * {@code PUT /api/emission-factors/{id}} (COMMANDO.md Section 8.5) — the six
 * fields of that section's example, in its order.
 *
 * <p>{@code isActive} is absent: Section 8.5 gives it a dedicated
 * {@code toggle-active} endpoint, and accepting it here would let that endpoint
 * be bypassed — the same reasoning as {@code VendorRequestDto}.
 *
 * <p>Uniqueness of {@code (region, transportMode, fuelType)} is not expressible
 * here; it is a database constraint (Section 9) surfacing as 409 through the
 * Section 23 mapping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmissionFactorRequestDto {

    /** A country name, or the literal {@code GLOBAL} fallback region (Section 9). */
    @NotBlank(message = "Region is required")
    @Size(max = 60, message = "Region must not exceed 60 characters")
    private String region;

    @NotBlank(message = "Transport mode is required")
    @Pattern(regexp = "^(ROAD|RAIL|SEA|AIR)$",
             message = "Transport mode must be one of ROAD, RAIL, SEA, AIR")
    private String transportMode;

    /**
     * A fuel name or the {@code ANY} wildcard. {@code ANY} is accepted here and
     * nowhere else in the API — it is what makes a fuel-agnostic factor row
     * possible, and Section 9 requires one per mode at {@code GLOBAL}.
     */
    @NotBlank(message = "Fuel type is required")
    @Pattern(regexp = "^(DIESEL|PETROL|LNG|JET_FUEL|HEAVY_FUEL_OIL|ELECTRIC|UNKNOWN|ANY)$",
             message = "Fuel type must be a fuel name or the ANY wildcard")
    private String fuelType;

    /**
     * kgCO2e per tonne-kilometre. Must be greater than zero — Section 15's
     * calculator treats a factor of zero or less as unprocessable, and a
     * zero-factor row would silently report every shipment as emission-free.
     * The ceiling is the {@code NUMERIC(10,6)} column's capacity.
     */
    @NotNull(message = "Emission factor is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Emission factor must be greater than zero")
    @DecimalMax(value = "9999.999999", message = "Emission factor is out of range")
    private BigDecimal factorKgco2ePerTonneKm;

    /**
     * Optional; the entity defaults to 1.20 (Section 7) when omitted.
     *
     * <p>The lower bound is 1.0 by definition: this multiplier corrects
     * great-circle distance UPWARD toward a real route, and no route is shorter
     * than the great circle. The ceiling is the {@code NUMERIC(4,2)} column's
     * capacity.
     */
    @DecimalMin(value = "1.0", message = "Circuity factor must be at least 1.0")
    @DecimalMax(value = "99.99", message = "Circuity factor is out of range")
    private BigDecimal circuityFactor;

    /** Provenance. Section 15 requires the demo-values caveat to be recorded. */
    @Size(max = 200, message = "Source must not exceed 200 characters")
    private String source;
}
