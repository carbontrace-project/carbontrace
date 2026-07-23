package com.carbontrace.modules.analytics.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.modules.analytics.dto.DashboardDto;
import com.carbontrace.modules.analytics.dto.VendorEmissionDto;
import com.carbontrace.modules.analytics.service.AnalyticsService;
import com.carbontrace.modules.goal.dto.GoalResponseDto;
import com.carbontrace.modules.goal.service.GoalService;
import com.carbontrace.modules.shipment.entity.TransportMode;
import com.carbontrace.modules.shipment.repository.ShipmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds the Section 8.9 dashboard from live aggregate queries.
 *
 * <p>{@code ShipmentRepository} supplies the shipment aggregates; {@code GoalService}
 * supplies the goals already mapped to DTOs, onto which this class fills the one
 * field the goal module deliberately leaves null — {@code progressPercent}
 * (Section 8.9). Injecting the service rather than the goal repository reuses
 * that mapping and its ordering instead of duplicating them.
 *
 * <p>The whole method is one {@code readOnly} transaction so its several queries
 * see one consistent snapshot: a shipment calculated between two of them must
 * not land in {@code totalEmissions} but miss {@code emissionsByMode}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    /** Section 8.9's trend window: the current month and the five before it. */
    private static final int TREND_MONTHS = 6;

    /** 1 tonne = 1000 kg — offsets are stored in tonnes, emissions in kg (Section 8.9). */
    private static final BigDecimal KG_PER_TONNE = BigDecimal.valueOf(1000);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal PROGRESS_FLOOR = BigDecimal.ZERO;
    private static final BigDecimal PROGRESS_CEILING = HUNDRED;

    private final ShipmentRepository shipmentRepository;
    private final GoalService goalService;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        BigDecimal totalEmissions = zeroIfNull(shipmentRepository.sumCalculatedEmissions());
        BigDecimal totalOffsetTonnes = zeroIfNull(shipmentRepository.sumOffsetTonnes());
        BigDecimal netEmissions = totalEmissions.subtract(totalOffsetTonnes.multiply(KG_PER_TONNE));

        DashboardDto dashboard = DashboardDto.builder()
                .totalShipments(shipmentRepository.count())
                .calculatedShipments(shipmentRepository.countByStatus(
                        com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED))
                .totalEmissionsKgco2e(totalEmissions)
                .totalOffsetTonnes(totalOffsetTonnes)
                .netEmissionsKgco2e(netEmissions)
                // STEP A026: sum offset_purchases.total_cost_usd here once that
                // table exists. It does not yet (Section 6 / STEP A025-A026), so
                // per this step's instruction the spend is a hard zero rather
                // than a query against a table that cannot be referenced.
                .totalOffsetSpendUsd(BigDecimal.ZERO)
                .emissionsByMode(buildEmissionsByMode())
                .emissionsByVendor(shipmentRepository.sumEmissionsByVendor())
                .monthlyEmissions(buildMonthlyEmissions())
                .goals(buildGoalsWithProgress())
                .build();

        log.info("Dashboard built: totalShipments={} calculated={} totalEmissions={} netEmissions={} goals={}",
                dashboard.getTotalShipments(), dashboard.getCalculatedShipments(),
                dashboard.getTotalEmissionsKgco2e(), dashboard.getNetEmissionsKgco2e(),
                dashboard.getGoals().size());
        return dashboard;
    }

    /**
     * The {@code emissionsByMode} map, keyed by the enum NAME (Section 8.9 shows
     * "SEA", "AIR", ...). Insertion order is the query's — highest is not
     * guaranteed, so a {@link LinkedHashMap} preserves whatever order the group
     * returns rather than pretending to sort.
     */
    private Map<String, BigDecimal> buildEmissionsByMode() {
        Map<String, BigDecimal> byMode = new LinkedHashMap<>();
        for (Object[] row : shipmentRepository.sumEmissionsByMode()) {
            byMode.put(((TransportMode) row[0]).name(), (BigDecimal) row[1]);
        }
        return byMode;
    }

    /**
     * The {@code monthlyEmissions} map: EXACTLY the last six months, oldest
     * first, {@code yyyy-MM} keys, zero-filled (Section 8.9).
     *
     * <p>The six keys are built first, in order, so the window is always
     * complete and correctly ordered regardless of which months have data;
     * {@link YearMonth#toString()} already renders {@code yyyy-MM}. The grouped
     * rows are then dropped into their bucket — a row outside the window (a
     * future {@code shipment_date}) simply has no bucket and is ignored.
     */
    private Map<String, BigDecimal> buildMonthlyEmissions() {
        YearMonth current = YearMonth.now();
        YearMonth windowStart = current.minusMonths(TREND_MONTHS - 1L);

        Map<String, BigDecimal> monthly = new LinkedHashMap<>();
        for (int i = 0; i < TREND_MONTHS; i++) {
            monthly.put(windowStart.plusMonths(i).toString(), BigDecimal.ZERO);
        }

        for (Object[] row : shipmentRepository.sumEmissionsByMonth(windowStart.atDay(1))) {
            String key = String.format("%04d-%02d", (Integer) row[0], (Integer) row[1]);
            // Only in-window months have a bucket; anything else is out of range.
            if (monthly.containsKey(key)) {
                monthly.put(key, (BigDecimal) row[2]);
            }
        }
        return monthly;
    }

    /**
     * Every goal, each with its Section 9 {@code progressPercent} filled in.
     *
     * <p>The progress numerator {@code currentNetAnnualEmissions} is a single
     * company-wide figure (this year's calculated emissions minus this year's
     * offset tonnes in kg), so it is computed ONCE and reused for every goal;
     * only {@code baseline} and {@code target} differ between goals.
     */
    private List<GoalResponseDto> buildGoalsWithProgress() {
        int year = Year.now().getValue();
        BigDecimal currentYearEmissions = zeroIfNull(shipmentRepository.sumEmissionsForYear(year));
        BigDecimal currentYearOffsetTonnes = zeroIfNull(shipmentRepository.sumOffsetTonnesForYear(year));
        BigDecimal currentNetAnnualEmissions =
                currentYearEmissions.subtract(currentYearOffsetTonnes.multiply(KG_PER_TONNE));

        List<GoalResponseDto> goals = goalService.getGoals();
        for (GoalResponseDto goal : goals) {
            goal.setProgressPercent(progressPercent(
                    goal.getBaselineEmissionsKgco2e(),
                    goal.getTargetEmissionsKgco2e(),
                    currentNetAnnualEmissions));
        }
        return goals;
    }

    /**
     * Section 9: {@code clamp((baseline − currentNet) / (baseline − target) × 100, 0, 100)}.
     *
     * <p>The denominator {@code baseline − target} is always positive — the goal
     * rules (Section 9, enforced in {@code GoalServiceImpl}) reject any goal whose
     * target is not strictly below its baseline — so this never divides by zero.
     * The numerator is scaled by 100 BEFORE dividing to keep precision, rounded
     * to two decimals, then clamped: a company still at its baseline reads 0%, one
     * that has reached its target reads 100%, and overshooting in either
     * direction is pinned to the ends of that range.
     */
    private BigDecimal progressPercent(BigDecimal baseline, BigDecimal target, BigDecimal currentNet) {
        BigDecimal denominator = baseline.subtract(target);
        BigDecimal raw = baseline.subtract(currentNet)
                .multiply(HUNDRED)
                .divide(denominator, 2, RoundingMode.HALF_UP);
        return raw.max(PROGRESS_FLOOR).min(PROGRESS_CEILING);
    }

    /** SUM over an empty set is null in SQL; the dashboard reports zero, not absent. */
    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
