package com.carbontrace.modules.emission.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The response body FastAPI {@code /calculate} returns (COMMANDO.md
 * Section 8.11), mirrored here so Spring Boot can persist it.
 *
 * <p>Named for the {@code ExtractionResultDto} of Section 6, which plays the
 * same role for {@code /extract}: "mirrors FastAPI response".
 *
 * <p>{@code ignoreUnknown} is not laziness. The {@link org.springframework.web.client.RestTemplate}
 * bean is built with {@code new RestTemplate(...)}, so it carries a plain Jackson
 * {@code ObjectMapper} with {@code FAIL_ON_UNKNOWN_PROPERTIES} still at its
 * library default of true — NOT Spring Boot's auto-configured mapper, which
 * disables it. Without this annotation, the day Track C adds a field to its
 * response every calculation in the platform would fail with a 502. An additive
 * field is not a contract break; a fatal one would be.
 *
 * <p>{@code formula} is read but not stored: Section 7 defines no column for it
 * and Section 24 forbids extra columns. It is logged instead, which is where the
 * derivation of a stored figure belongs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalculationResultDto {

    /** Echoed back whether it was supplied or computed from the coordinates. */
    @JsonProperty("distance_km")
    private BigDecimal distanceKm;

    /** {@code DOCUMENT} when the distance came from the request, {@code COMPUTED} when haversine produced it. */
    @JsonProperty("distance_source")
    private String distanceSource;

    @JsonProperty("total_emissions_kgco2e")
    private BigDecimal totalEmissionsKgco2e;

    /** Human-readable derivation, e.g. {@code "18.5 t × 20430.00 km × 0.011 kgCO2e/t·km"}. */
    @JsonProperty("formula")
    private String formula;
}
