package com.carbontrace.modules.shipment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code PUT /api/shipments/{id}/review} (COMMANDO.md
 * Section 8.4) — every extractable field, all editable by the auditor.
 *
 * <p><strong>Every field is optional.</strong> This endpoint is the auditor's
 * correction pass over an AI extraction that Section 14 guarantees will contain
 * nulls wherever the document was silent, and a shipment whose extraction FAILED
 * arrives here completely empty. Requiring anything would make the very
 * documents that need manual entry the ones that cannot be saved. The fields
 * calculation actually needs are enforced at
 * {@code POST /api/shipments/{id}/calculate} (STEP A021), where Section 9 places
 * that rule — not here.
 *
 * <p>What IS enforced is that a supplied value is sane: coordinates inside the
 * real globe, a positive weight, and enum values inside their allowed sets. This
 * mirrors the deterministic post-validation Section 14 applies to the LLM's own
 * output — the same limits, applied to a human instead of a model.
 *
 * <p>{@code distanceKm} left null means "compute it for me": Section 8.4 defers
 * {@code distance_source} to calculation time, where Section 15's
 * {@code compute_distance} decides between DOCUMENT and COMPUTED. This endpoint
 * therefore never writes {@code distance_source}.
 *
 * <p>The two enums travel as Strings, not as the enum types, for the reason
 * {@code RegisterRequest.role} documents: an unknown value must fail as a 400
 * validation error with a readable message, whereas binding straight to the enum
 * fails earlier inside Jackson with an unhelpful deserialization error.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentReviewRequest {

    @Size(max = 100, message = "Invoice number must not exceed 100 characters")
    private String invoiceNumber;

    @Size(max = 100, message = "Carrier name must not exceed 100 characters")
    private String carrierName;

    /**
     * ISO {@code yyyy-MM-dd}, matching the Section 7 {@code DATE} column and the
     * format Section 14 normalises the LLM's output to. The explicit
     * {@link JsonFormat} makes the accepted format part of the contract rather
     * than a property of the ambient Jackson configuration.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate shipmentDate;

    @Size(max = 100, message = "Origin city must not exceed 100 characters")
    private String originCity;

    @Size(max = 60, message = "Origin country must not exceed 60 characters")
    private String originCountry;

    /**
     * Latitude in degrees. Section 14 applies exactly this range check to the
     * LLM's coordinates; an auditor typing 999 gets the same rejection.
     */
    @DecimalMin(value = "-90.0", message = "Origin latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Origin latitude must be between -90 and 90")
    private Double originLat;

    @DecimalMin(value = "-180.0", message = "Origin longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Origin longitude must be between -180 and 180")
    private Double originLng;

    @Size(max = 100, message = "Destination city must not exceed 100 characters")
    private String destinationCity;

    @Size(max = 60, message = "Destination country must not exceed 60 characters")
    private String destinationCountry;

    @DecimalMin(value = "-90.0", message = "Destination latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Destination latitude must be between -90 and 90")
    private Double destinationLat;

    @DecimalMin(value = "-180.0", message = "Destination longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Destination longitude must be between -180 and 180")
    private Double destinationLng;

    /** The four Section 7 modes; also the {@code transport_mode} axis of the factor table. */
    @Pattern(regexp = "^(ROAD|RAIL|SEA|AIR)$",
             message = "Transport mode must be one of ROAD, RAIL, SEA, AIR")
    private String transportMode;

    /**
     * The seven Section 7 fuels. {@code UNKNOWN} is included deliberately — it is
     * a storable value meaning "stated but unrecognised", distinct from null
     * meaning "not stated" (see {@code FuelType}).
     */
    @Pattern(regexp = "^(DIESEL|PETROL|LNG|JET_FUEL|HEAVY_FUEL_OIL|ELECTRIC|UNKNOWN)$",
             message = "Fuel type must be one of DIESEL, PETROL, LNG, JET_FUEL, HEAVY_FUEL_OIL, ELECTRIC, UNKNOWN")
    private String fuelType;

    /**
     * Tonnes, {@link BigDecimal} per Section 21. Strictly greater than zero:
     * Section 9 requires {@code weightTonnes > 0} for calculation, and a zero or
     * negative shipment weight is not a correction, it is a typo. The upper bound
     * is Section 14's own sanity limit on the extracted value.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
    @DecimalMax(value = "100000.0", inclusive = false, message = "Weight must be less than 100000 tonnes")
    private BigDecimal weightTonnes;

    /**
     * Kilometres. Null means "compute it from the coordinates at calculation
     * time" (Section 8.4); a supplied value must be positive.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Distance must be greater than zero")
    private BigDecimal distanceKm;
}
