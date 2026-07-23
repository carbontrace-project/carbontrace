package com.carbontrace.modules.goal.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code POST /api/goals} and {@code PUT /api/goals/{id}}
 * (COMMANDO.md Section 8.8) — the five fields of that section's example.
 *
 * <p>The annotations here are the STATIC guards only: presence, length, and the
 * numeric range each {@code NUMERIC(14,3)} column can hold. Section 9's two
 * actual goal rules are NOT here:
 * <ul>
 *   <li>{@code targetEmissions < baselineEmissions} is cross-field — it compares
 *       two properties of this object, which a per-field constraint cannot do;</li>
 *   <li>{@code targetYear >= current year} is time-relative — "current" is not a
 *       compile-time constant, so no annotation can express it.</li>
 * </ul>
 * Both live in {@code GoalServiceImpl}, throwing {@code BadRequestException} →
 * 400 (Section 23). Keeping them out of here rather than reaching for a custom
 * class-level validator is the Section 24 "no unnecessary abstractions" call:
 * the service already exists and already needs the entity loaded.
 *
 * <p>{@code createdBy} is absent by design: the creator is the authenticated
 * principal, taken from the security context, never from the body — the same
 * rule {@code ShipmentCreateRequest} follows for {@code uploaded_by}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    /**
     * The calendar year the target is set for. Required here; the "current year
     * or later" rule (Section 9) is enforced in the service, where {@code now}
     * is available.
     */
    @NotNull(message = "Target year is required")
    private Integer targetYear;

    /**
     * The starting emissions, in kgCO2e. Must be positive: a baseline of zero
     * makes the goal meaningless (there is nothing to reduce) and would make the
     * Section 9 progress formula divide by zero once {@code target} is also
     * forced below it. The ceiling is the {@code NUMERIC(14,3)} column.
     */
    @NotNull(message = "Baseline emissions are required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Baseline emissions must be greater than zero")
    @DecimalMax(value = "99999999999.999", message = "Baseline emissions are out of range")
    private BigDecimal baselineEmissionsKgco2e;

    /**
     * The emissions being aimed for. Zero is allowed — a net-zero target is
     * legitimate — but it must come out strictly below the baseline, which the
     * service checks. The ceiling matches the baseline's column.
     */
    @NotNull(message = "Target emissions are required")
    @DecimalMin(value = "0.0", message = "Target emissions cannot be negative")
    @DecimalMax(value = "99999999999.999", message = "Target emissions are out of range")
    private BigDecimal targetEmissionsKgco2e;

    /** Optional context, e.g. "Baseline = FY2025 total". {@code TEXT} in Section 7 — no length cap. */
    private String notes;
}
