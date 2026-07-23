package com.carbontrace.modules.analytics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrace.common.ApiResponse;
import com.carbontrace.modules.analytics.dto.DashboardDto;
import com.carbontrace.modules.analytics.service.AnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The single analytics endpoint of COMMANDO.md Section 8.9.
 *
 * <p>Authorization needs no annotation: {@code /api/analytics/dashboard} matches
 * no Section 10 permitAll or ROLE_ADMIN rule, so {@code anyRequest().authenticated()}
 * covers it — exactly Section 8.9's "any role". Section 9 makes the underlying
 * data company-wide, so the figures are the same whoever asks.
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private static final String DASHBOARD_MESSAGE = "Dashboard retrieved";

    private final AnalyticsService analyticsService;

    /** 200 — the whole dashboard, computed live (Section 8.9). Any authenticated role. */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardDto> getDashboard() {
        log.info("Dashboard requested");
        return ApiResponse.success(DASHBOARD_MESSAGE, analyticsService.getDashboard());
    }
}
