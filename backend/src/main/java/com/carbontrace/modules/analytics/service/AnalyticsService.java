package com.carbontrace.modules.analytics.service;

import com.carbontrace.modules.analytics.dto.DashboardDto;

/**
 * The single analytics operation reachable from {@code AnalyticsController}
 * (COMMANDO.md Section 8.9).
 */
public interface AnalyticsService {

    /**
     * Builds the dashboard from live aggregate queries — nothing is stored or
     * cached (Section 8.9). Includes every goal with its {@code progressPercent}
     * computed per Section 9.
     *
     * @return the fully populated dashboard; an empty database yields zeros and
     *         empty collections, never an error
     */
    DashboardDto getDashboard();
}
