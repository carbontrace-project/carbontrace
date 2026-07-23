package com.carbontrace.modules.goal.service;

import java.util.List;

import com.carbontrace.modules.goal.dto.GoalRequestDto;
import com.carbontrace.modules.goal.dto.GoalResponseDto;

/**
 * The goal operations reachable from {@code GoalController} (COMMANDO.md
 * Section 8.8).
 *
 * <p>Every method enforces the two Section 9 goal rules on write
 * ({@code target < baseline}, {@code targetYear >= current year}) and none of
 * them computes {@code progressPercent} — that is analytics' job (Section 8.9,
 * STEP A024).
 */
public interface GoalService {

    /**
     * Creates a goal owned (for audit) by the authenticated caller.
     *
     * @param request      title, target year, baseline and target emissions, notes
     * @param creatorEmail the authenticated caller, who becomes {@code created_by}
     * @return the created goal
     * @throws com.carbontrace.exception.BadRequestException if {@code target >= baseline}
     *         or {@code targetYear} is before the current year (Section 9)
     * @throws com.carbontrace.exception.ResourceNotFoundException if the creating user
     *         cannot be found
     */
    GoalResponseDto createGoal(GoalRequestDto request, String creatorEmail);

    /**
     * Every goal, newest first. Unpaged and unscoped: Section 8.8 defines no
     * page parameters and Section 9 makes goals shared company-wide.
     *
     * @return all goals; empty when none exist, which is a normal state
     */
    List<GoalResponseDto> getGoals();

    /**
     * @param id goal primary key
     * @return the goal
     * @throws com.carbontrace.exception.ResourceNotFoundException if no goal has that id
     */
    GoalResponseDto getGoalById(Long id);

    /**
     * Replaces a goal's editable fields (title, target year, baseline, target,
     * notes). {@code created_by} and {@code created_at} are preserved.
     *
     * @param id      goal primary key
     * @param request the new values
     * @return the goal as stored after the edit
     * @throws com.carbontrace.exception.ResourceNotFoundException if no goal has that id
     * @throws com.carbontrace.exception.BadRequestException if the new values break a
     *         Section 9 rule
     */
    GoalResponseDto updateGoal(Long id, GoalRequestDto request);

    /**
     * Permanently removes a goal (Section 8.8 defines a hard {@code DELETE};
     * goals carry no history to preserve, unlike vendors or factors).
     *
     * @param id goal primary key
     * @throws com.carbontrace.exception.ResourceNotFoundException if no goal has that id
     */
    void deleteGoal(Long id);
}
