package com.carbontrace.modules.shipment.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.carbontrace.modules.auth.entity.User;
import com.carbontrace.modules.vendor.entity.Vendor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A freight shipment extracted from an uploaded bill — maps to {@code shipments}
 * (COMMANDO.md Section 7).
 *
 * <p>Almost every field is nullable, which is deliberate rather than lax: a
 * shipment exists from the moment its PDF lands in S3, long before extraction or
 * the auditor's review has filled anything in. Section 14 is explicit that the
 * LLM "never invents values" — a field the document does not contain arrives
 * null and stays null until a human types it. Only {@code vendor},
 * {@code uploadedBy} and {@code status} are required, because those three are
 * known at creation time.
 *
 * <p>The two Section 7 indexes are declared here so {@code ddl-auto: update}
 * creates them: {@code vendor_id} and {@code status} are the two
 * {@code GET /api/shipments} filters (Section 8.4).
 *
 * <p>Associations are LAZY and excluded from {@code toString}/{@code equals} for
 * the reason {@code RefreshToken} documents — {@code @Data} would otherwise
 * trigger the proxy on every print.
 */
@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipments_vendor", columnList = "vendor_id"),
        @Index(name = "idx_shipments_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    /** The auditor who uploaded the document; the row is never reassigned. */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    // ------------------------------------------------- extracted document fields

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    /** {@code DATE} in Section 7 — a calendar day, so {@link LocalDate}, not a timestamp. */
    @Column(name = "shipment_date")
    private LocalDate shipmentDate;

    @Column(name = "origin_city", length = 100)
    private String originCity;

    @Column(name = "origin_country", length = 60)
    private String originCountry;

    /**
     * {@code DOUBLE PRECISION} in Section 7, and boxed so it can be null.
     * Coordinates are the one thing Section 14 lets the LLM supply from its own
     * knowledge rather than the document, capped at MEDIUM confidence and always
     * auditor-editable — so double is the right type here, while every value
     * that feeds the emission arithmetic below is BigDecimal.
     */
    @Column(name = "origin_lat")
    private Double originLat;

    @Column(name = "origin_lng")
    private Double originLng;

    @Column(name = "destination_city", length = 100)
    private String destinationCity;

    @Column(name = "destination_country", length = 60)
    private String destinationCountry;

    @Column(name = "destination_lat")
    private Double destinationLat;

    @Column(name = "destination_lng")
    private Double destinationLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", length = 10)
    private TransportMode transportMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 30)
    private FuelType fuelType;

    // ------------------------------------------------------- calculation inputs

    /** {@code NUMERIC(12,3)} — BigDecimal per Section 21 (tonnes). */
    @Column(name = "weight_tonnes", precision = 12, scale = 3)
    private BigDecimal weightTonnes;

    /** {@code NUMERIC(12,2)} — either taken from the document or computed (Section 15). */
    @Column(name = "distance_km", precision = 12, scale = 2)
    private BigDecimal distanceKm;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_source", length = 10)
    private DistanceSource distanceSource;

    /** Overall extraction confidence; per-field levels are not persisted (Section 7). */
    @Enumerated(EnumType.STRING)
    @Column(name = "extraction_confidence", length = 10)
    private ConfidenceLevel extractionConfidence;

    // ------------------------------------------------------ calculation results

    /** {@code NUMERIC(14,3)} — wider than the inputs; this is a product of three of them. */
    @Column(name = "total_emissions_kgco2e", precision = 14, scale = 3)
    private BigDecimal totalEmissionsKgco2e;

    /**
     * Running total of offsets purchased against this shipment
     * ({@code NUMERIC(12,3) DEFAULT 0}).
     *
     * <p>Defaults to zero rather than null because Section 9 computes
     * {@code requiredTonnes = totalKg/1000 − offsetTonnes} — a null here would
     * make that arithmetic fail on every shipment that has never been offset,
     * which is all of them at first.
     */
    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "offset_tonnes", precision = 12, scale = 3)
    private BigDecimal offsetTonnes = BigDecimal.ZERO;

    // -------------------------------------------------------------- lifecycle

    /**
     * {@code VARCHAR(15) NOT NULL DEFAULT 'UPLOADED'} — the only enum column on
     * this table that is NOT NULL, because a shipment always has a state.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'UPLOADED'")
    @Column(name = "status", nullable = false, length = 15)
    private ShipmentStatus status = ShipmentStatus.UPLOADED;

    /**
     * Why extraction failed, for the banner that routes the auditor into manual
     * review (Section 23). {@code TEXT} in Section 7 — unbounded, because it can
     * carry an upstream error message.
     */
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
