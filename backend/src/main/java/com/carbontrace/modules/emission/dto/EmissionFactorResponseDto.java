package com.carbontrace.modules.emission.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for every {@code /api/emission-factors} endpoint (COMMANDO.md
 * Section 8.5), and the element type inside {@code PagedResponse} for the list.
 *
 * <p>Every Section 7 column, with {@code transportMode} as a String — matching
 * how {@code UserResponseDto.role} and {@code VendorResponseDto.vendorType}
 * already serialise. {@code fuelType} is already a String on the entity, because
 * it can hold the {@code ANY} wildcard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmissionFactorResponseDto {

    private Long id;
    private String region;
    private String transportMode;
    private String fuelType;
    private BigDecimal factorKgco2ePerTonneKm;
    private BigDecimal circuityFactor;
    private String source;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
