package com.carbontrace.modules.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for every {@code /api/goals} endpoint (COMMANDO.md Section 8.8),
 * and the element type the analytics dashboard reuses (Section 8.9).
 *
 * <p>The {@code createdBy} association is flattened to an id plus the creator's
 * email, exactly as {@code ShipmentResponseDto} flattens {@code uploadedBy}: the
 * nested {@code User} is never serialised, which would otherwise carry a BCrypt
 * hash into the payload.
 *
 * <p><strong>{@link #progressPercent} is part of this contract but is NOT
 * populated by the goal module.</strong> Section 8.9 names {@code GoalResponseDto}
 * as the dashboard's goal type "with computed progressPercent", so the field
 * belongs on this class — freezing the shape now (Section 25.2) means STEP A024
 * fills it in rather than reshaping a DTO other tracks already mock. From the
 * goal CRUD endpoints it is null: progress is a function of live emission
 * aggregates the goal module does not have and must not guess. The clamped
 * {@code 0..100} formula is Section 9's, computed only in analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalResponseDto {

    private Long id;

    private Long createdById;
    private String createdByEmail;

    private String title;
    private Integer targetYear;
    private BigDecimal baselineEmissionsKgco2e;
    private BigDecimal targetEmissionsKgco2e;
    private String notes;

    /** Filled by analytics (STEP A024); null from the goal endpoints. See the class note. */
    private BigDecimal progressPercent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
