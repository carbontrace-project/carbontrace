package com.carbontrace.modules.analytics.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.carbontrace.modules.goal.dto.GoalResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The single response body of {@code GET /api/analytics/dashboard}
 * (COMMANDO.md Section 8.9), in that section's field order.
 *
 * <p>Every field is derived live from aggregate queries — Section 8.9: "All
 * values computed with aggregate queries — nothing is stored separately." There
 * is no analytics table and no cached snapshot; each request re-reads the
 * shipments and goals.
 *
 * <p>Two fields carry deliberate STEP-A024 limitations, both traceable to the
 * purchases table not existing until STEP A026:
 * <ul>
 *   <li>{@link #totalOffsetTonnes} is summed from {@code shipments.offset_tonnes}
 *       (currently zero on every shipment — nothing has been offset yet);</li>
 *   <li>{@link #totalOffsetSpendUsd} is {@code BigDecimal.ZERO}, because the
 *       {@code offset_purchases.total_cost_usd} it must sum has no table yet.
 *       STEP A026 replaces the zero with the real sum — see
 *       {@code AnalyticsServiceImpl}.</li>
 * </ul>
 *
 * <p>{@link #goals} reuses {@link GoalResponseDto}, and this is the ONE place its
 * {@code progressPercent} is populated (Section 8.9); every goal-module endpoint
 * leaves it null.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    private long totalShipments;
    private long calculatedShipments;

    private BigDecimal totalEmissionsKgco2e;
    private BigDecimal totalOffsetTonnes;

    /** {@code totalEmissions − totalOffsetTonnes × 1000} — offsets are tonnes, emissions kg (Section 8.9). */
    private BigDecimal netEmissionsKgco2e;

    /** Zero until STEP A026 gives {@code offset_purchases} a table to sum. */
    private BigDecimal totalOffsetSpendUsd;

    /** Transport mode name → summed emissions, over CALCULATED shipments. */
    private Map<String, BigDecimal> emissionsByMode;

    /** Per-vendor totals, highest emitter first. */
    private List<VendorEmissionDto> emissionsByVendor;

    /** Exactly the last six months, oldest first, {@code yyyy-MM} keys, zero-filled (Section 8.9). */
    private Map<String, BigDecimal> monthlyEmissions;

    /** Every goal, each with its {@code progressPercent} computed here (Section 9). */
    private List<GoalResponseDto> goals;
}
