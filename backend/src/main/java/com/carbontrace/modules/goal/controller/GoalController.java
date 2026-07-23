package com.carbontrace.modules.goal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrace.common.ApiResponse;
import com.carbontrace.modules.goal.dto.GoalRequestDto;
import com.carbontrace.modules.goal.dto.GoalResponseDto;
import com.carbontrace.modules.goal.service.GoalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The five goal endpoints of COMMANDO.md Section 8.8.
 *
 * <p>Authorization needs no annotation: none of these routes matches a Section 10
 * permitAll or ROLE_ADMIN rule, so {@code anyRequest().authenticated()} covers
 * them, which is exactly Section 8.8's "ROLE_AUDITOR, ROLE_ADMIN" for a two-role
 * system. Section 9 confirms goals are shared company-wide, so the list is not
 * scoped to the caller.
 *
 * <p>This is the ONE module with a {@code @DeleteMapping}. Vendors, factors and
 * marketplace rows are soft-deactivated and purchases are immutable, but
 * Section 8.8 gives goals a real {@code DELETE} — a goal is a plan a company can
 * simply abandon, with no downstream row referencing it to orphan.
 */
@Slf4j
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private static final String CREATE_MESSAGE = "Goal created successfully";
    private static final String LIST_MESSAGE = "Goals retrieved";
    private static final String GET_MESSAGE = "Goal retrieved";
    private static final String UPDATE_MESSAGE = "Goal updated successfully";
    private static final String DELETE_MESSAGE = "Goal deleted successfully";

    private final GoalService goalService;

    /**
     * 201 — creates a goal owned (for audit) by the caller.
     *
     * <p>The creator is the authenticated principal, never the body: a client
     * that could name its own {@code created_by} could attribute a goal to
     * someone else.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoalResponseDto> createGoal(@Valid @RequestBody GoalRequestDto request,
                                                   @AuthenticationPrincipal String email) {
        log.info("Goal creation requested by {} targetYear={}", email, request.getTargetYear());
        return ApiResponse.success(CREATE_MESSAGE, goalService.createGoal(request, email));
    }

    /** 200 — every goal, newest first. Unpaged (Section 8.8 defines no page params). */
    @GetMapping
    public ApiResponse<List<GoalResponseDto>> getGoals() {
        log.info("Goal list requested");
        return ApiResponse.success(LIST_MESSAGE, goalService.getGoals());
    }

    /** 200 — a single goal, or 404. */
    @GetMapping("/{id}")
    public ApiResponse<GoalResponseDto> getGoalById(@PathVariable Long id) {
        log.info("Goal requested: id={}", id);
        return ApiResponse.success(GET_MESSAGE, goalService.getGoalById(id));
    }

    /** 200 — replaces the editable fields; 400 on a Section 9 rule violation, 404 if unknown. */
    @PutMapping("/{id}")
    public ApiResponse<GoalResponseDto> updateGoal(@PathVariable Long id,
                                                   @Valid @RequestBody GoalRequestDto request) {
        log.info("Goal update requested: id={}", id);
        return ApiResponse.success(UPDATE_MESSAGE, goalService.updateGoal(id, request));
    }

    /** 200 — permanently deletes the goal, or 404 if it does not exist. */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGoal(@PathVariable Long id) {
        log.info("Goal deletion requested: id={}", id);
        goalService.deleteGoal(id);
        return ApiResponse.success(DELETE_MESSAGE, null);
    }
}
