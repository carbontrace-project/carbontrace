package com.carbontrace.modules.goal.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.carbontrace.modules.auth.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * A carbon-reduction target — maps to {@code reduction_goals}
 * (COMMANDO.md Section 7).
 *
 * <p>A goal states, in plain numbers, "we emitted {@code baseline} and intend to
 * be at {@code target} by {@code targetYear}". Section 9's two rules —
 * {@code target < baseline} and {@code targetYear >= current year} — are the
 * only things that make those numbers a goal rather than a wish, and they are
 * enforced in {@code GoalServiceImpl}, not here: they are cross-field and
 * time-relative, which no column constraint can express.
 *
 * <p>Progress toward the goal is NOT stored. Section 8.9 computes
 * {@code progressPercent} in the analytics dashboard (STEP A024) from live
 * emission aggregates; persisting it would be a second, staler copy of a figure
 * the dashboard already derives on every request.
 *
 * <p>{@link #createdBy} is LAZY and excluded from {@code toString}/{@code equals}
 * for the reason the {@code Shipment} associations document — {@code @Data} would
 * otherwise trigger the proxy on every print and drag a {@code User} (and its
 * BCrypt hash) into scope.
 */
@Entity
@Table(name = "reduction_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReductionGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who created the goal ({@code created_by}, Section 7). An audit
     * field, not an owner: Section 9 makes goals shared company-wide ("single-
     * company tool"), so every authenticated user sees and edits every goal.
     * The row is never reassigned.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    /** {@code INT NOT NULL} — a calendar year, and {@code >= current year} (Section 9). */
    @Column(name = "target_year", nullable = false)
    private Integer targetYear;

    /** {@code NUMERIC(14,3)} — BigDecimal per Section 21; the same scale as a shipment's emissions. */
    @Column(name = "baseline_emissions_kgco2e", nullable = false, precision = 14, scale = 3)
    private BigDecimal baselineEmissionsKgco2e;

    /** {@code NUMERIC(14,3) NOT NULL}, and strictly less than {@link #baselineEmissionsKgco2e} (Section 9). */
    @Column(name = "target_emissions_kgco2e", nullable = false, precision = 14, scale = 3)
    private BigDecimal targetEmissionsKgco2e;

    /** Free text, e.g. how the baseline was derived. {@code TEXT} in Section 7 — unbounded. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
