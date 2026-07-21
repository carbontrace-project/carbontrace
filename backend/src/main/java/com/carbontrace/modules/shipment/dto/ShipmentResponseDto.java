package com.carbontrace.modules.shipment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for every {@code /api/shipments} endpoint (COMMANDO.md
 * Section 8.4), and the element type inside {@code PagedResponse} for the list.
 *
 * <p>Carries every Section 7 {@code shipments} column, including the per-field
 * extraction values and {@code extractionConfidence}, because the Section 12
 * review screen renders all of them and highlights the low-confidence ones. In
 * STEP A017 every extracted field comes back null — extraction is not wired
 * until A-SWAP-1 — and the shape is identical either way, so the frontend needs
 * no change when the values start arriving.
 *
 * <p>Associations are flattened to ids plus one human-readable label each:
 * {@code vendorName} for the Section 12 shipment table, {@code uploadedByEmail}
 * to identify the auditor. The nested entities are never serialised — that would
 * drag a BCrypt hash into the payload through {@code uploadedBy}.
 *
 * <p>Enums are Strings, matching how {@code UserResponseDto.role} and
 * {@code VendorResponseDto.vendorType} already serialise. Money and tonnage stay
 * {@link BigDecimal} per Section 21.
 *
 * <p>The document's own metadata is not here: Section 12 reaches the PDF through
 * {@code GET /api/shipments/{id}/document-url}, which mints a fresh presigned
 * URL. A key or URL embedded in this payload would be either useless or an
 * expiring credential in a cacheable response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponseDto {

    private Long id;

    private Long vendorId;
    private String vendorName;
    private Long uploadedById;
    private String uploadedByEmail;

    // ------------------------------------------------- extracted document fields
    // All null until A-SWAP-1 wires extraction, or until the auditor fills them
    // in during review (STEP A018).

    private String invoiceNumber;
    private String carrierName;
    private LocalDate shipmentDate;
    private String originCity;
    private String originCountry;
    private Double originLat;
    private Double originLng;
    private String destinationCity;
    private String destinationCountry;
    private Double destinationLat;
    private Double destinationLng;
    private String transportMode;
    private String fuelType;

    // ------------------------------------------------------- calculation fields

    private BigDecimal weightTonnes;
    private BigDecimal distanceKm;
    private String distanceSource;
    private String extractionConfidence;
    private BigDecimal totalEmissionsKgco2e;
    private BigDecimal offsetTonnes;

    // -------------------------------------------------------------- lifecycle

    private String status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
