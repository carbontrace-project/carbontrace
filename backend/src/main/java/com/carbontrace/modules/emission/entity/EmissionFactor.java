package com.carbontrace.modules.emission.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.carbontrace.modules.shipment.entity.TransportMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A reference emission factor — maps to {@code emission_factors}
 * (COMMANDO.md Section 7).
 *
 * <p>Reference data, not business data: rows are seeded and curated by admins
 * (Section 8.5) and read during calculation. Section 9 makes
 * {@code (region, transport_mode, fuel_type)} unique, which is what allows the
 * four-step fallback chain to be a deterministic lookup rather than a search.
 *
 * <p><strong>The demo-values caveat.</strong> Section 15 is explicit that these
 * are "illustrative numbers for a portfolio project, not certified accounting
 * factors". The {@link #source} column exists to carry that statement per row.
 *
 * <p>There is no {@code updated_at}: Section 7 gives this table {@code created_at}
 * only.
 */
@Entity
@Table(name = "emission_factors",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_emission_factor_region_mode_fuel",
               columnNames = {"region", "transport_mode", "fuel_type"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmissionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A country name, or the literal {@code GLOBAL} that Section 9 requires as
     * the last-resort fallback. A String rather than an enum because the set of
     * countries is open.
     */
    @Column(name = "region", nullable = false, length = 60)
    private String region;

    /**
     * Reuses the shipment module's {@code TransportMode} deliberately. The
     * lookup in Section 9 matches a shipment's mode against this column, so one
     * enum for both sides is what guarantees they can never drift apart.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false, length = 10)
    private TransportMode transportMode;

    /**
     * A {@code FuelType} name OR the literal {@code ANY} wildcard (Section 7).
     *
     * <p>Deliberately a String, not the {@code FuelType} enum: {@code ANY} is not
     * a fuel and has no place in an enum that describes what a vehicle burns.
     * Adding it there would force every consumer of {@code FuelType} — the
     * shipment entity, the review DTO, the extractor's allowed values — to handle
     * a constant that can never legitimately appear on a shipment. The wildcard
     * belongs to the lookup, so it lives here.
     */
    @Column(name = "fuel_type", nullable = false, length = 30)
    private String fuelType;

    /** {@code NUMERIC(10,6)} — BigDecimal per Section 21; six decimals matter at 0.011. */
    @Column(name = "factor_kgco2e_per_tonne_km", nullable = false, precision = 10, scale = 6)
    private BigDecimal factorKgco2ePerTonneKm;

    /**
     * Multiplier correcting great-circle distance toward a realistic route
     * ({@code NUMERIC(4,2) DEFAULT 1.20}). Mode-specific: Section 15's seed uses
     * SEA 1.15, ROAD 1.30, RAIL 1.20, AIR 1.05 — an aircraft flies almost the
     * great circle, a lorry follows roads.
     */
    @Builder.Default
    @ColumnDefault("1.20")
    @Column(name = "circuity_factor", nullable = false, precision = 4, scale = 2)
    private BigDecimal circuityFactor = new BigDecimal("1.20");

    /** Provenance, e.g. "demo values inspired by GLEC-style factors" (Section 15). */
    @Column(name = "source", length = 200)
    private String source;

    /**
     * Section 9: "Only active factors participate in lookup." Factors are
     * deactivated rather than deleted — a shipment calculated last month was
     * calculated against the row as it stood, and deleting it would erase the
     * provenance of a stored figure.
     */
    @Builder.Default
    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive = Boolean.TRUE;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
