package com.carbontrace.modules.emission.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The request body Spring Boot POSTs to FastAPI {@code /calculate}
 * (COMMANDO.md Section 8.11).
 *
 * <p>Internal to the Spring Boot ↔ FastAPI hop — never returned to a browser.
 * Section 21 fixes the casing boundary: "snake_case JSON in FastAPI contracts,
 * camelCase in Spring Boot contracts — the mapping happens in Spring Boot's DTOs
 * ({@code @JsonProperty} where needed) ... be explicit, never rely on implicit
 * conversion." So every field carries {@code @JsonProperty} even where a naming
 * strategy could have inferred it: the annotation is what makes this class a
 * readable copy of the Section 8.11 example rather than a shape that depends on
 * an ObjectMapper setting configured somewhere else.
 *
 * <p>Section 5 is the reason this DTO is as wide as it is: "FastAPI ↔ RDS:
 * FORBIDDEN. All data FastAPI needs ... is passed in the request payload by
 * Spring Boot." The emission factor and circuity travel in the payload because
 * the compute service is not allowed to look them up.
 *
 * <p>{@code distanceKm} is deliberately serialised even when null — the
 * Section 8.11 example shows {@code "distance_km": null}, and null is the
 * instruction that means "compute it from the coordinates" (Section 15).
 * Suppressing nulls here would silently change that contract.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationRequestDto {

    @JsonProperty("shipment_id")
    private Long shipmentId;

    @JsonProperty("weight_tonnes")
    private BigDecimal weightTonnes;

    @JsonProperty("distance_km")
    private BigDecimal distanceKm;

    @JsonProperty("origin_lat")
    private Double originLat;

    @JsonProperty("origin_lng")
    private Double originLng;

    @JsonProperty("destination_lat")
    private Double destinationLat;

    @JsonProperty("destination_lng")
    private Double destinationLng;

    @JsonProperty("transport_mode")
    private String transportMode;

    @JsonProperty("emission_factor_kgco2e_per_tonne_km")
    private BigDecimal emissionFactorKgco2ePerTonneKm;

    @JsonProperty("circuity_factor")
    private BigDecimal circuityFactor;
}
