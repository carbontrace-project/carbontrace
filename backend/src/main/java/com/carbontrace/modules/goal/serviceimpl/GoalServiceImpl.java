package com.carbontrace.modules.goal.serviceimpl;

import java.time.Year;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.exception.BadRequestException;
import com.carbontrace.exception.ResourceNotFoundException;
import com.carbontrace.modules.auth.entity.User;
import com.carbontrace.modules.auth.repository.UserRepository;
import com.carbontrace.modules.goal.dto.GoalRequestDto;
import com.carbontrace.modules.goal.dto.GoalResponseDto;
import com.carbontrace.modules.goal.entity.ReductionGoal;
import com.carbontrace.modules.goal.mapper.GoalMapper;
import com.carbontrace.modules.goal.repository.ReductionGoalRepository;
import com.carbontrace.modules.goal.service.GoalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Goal CRUD per COMMANDO.md Section 8.8, with the Section 9 goal rules.
 *
 * <p>{@code UserRepository} is injected directly rather than {@code UserService}
 * for the reason {@code ShipmentServiceImpl} documents: this class needs the
 * {@code User} ENTITY to hang {@code created_by} on, and the service returns a
 * DTO.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private static final String GOAL_NOT_FOUND = "Goal not found with id: ";
    private static final String CREATOR_NOT_FOUND = "User not found with email: ";
    private static final String TARGET_NOT_BELOW_BASELINE =
            "Target emissions must be less than baseline emissions";
    private static final String YEAR_IN_PAST =
            "Target year must be the current year or later";

    /** Newest first, by unique id so the unpaged list is deterministic — as in every other module. */
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final ReductionGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final GoalMapper goalMapper;

    @Override
    @Transactional
    public GoalResponseDto createGoal(GoalRequestDto request, String creatorEmail) {
        validateRules(request);

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException(CREATOR_NOT_FOUND + creatorEmail));

        ReductionGoal goal = goalMapper.toEntity(request);
        goal.setCreatedBy(creator);
        goalRepository.save(goal);

        log.info("Goal created: id={} createdBy={} targetYear={} baseline={} target={}",
                goal.getId(), creator.getId(), goal.getTargetYear(),
                goal.getBaselineEmissionsKgco2e(), goal.getTargetEmissionsKgco2e());
        return goalMapper.toResponseDto(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponseDto> getGoals() {
        List<GoalResponseDto> goals = goalRepository.findAll(NEWEST_FIRST).stream()
                .map(goalMapper::toResponseDto)
                .toList();

        log.info("Goals retrieved: {}", goals.size());
        return goals;
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponseDto getGoalById(Long id) {
        return goalMapper.toResponseDto(findGoal(id));
    }

    @Override
    @Transactional
    public GoalResponseDto updateGoal(Long id, GoalRequestDto request) {
        // The rules are checked before the mapper touches the entity, so a
        // rejected update leaves the goal byte-identical — nothing to roll back.
        validateRules(request);

        ReductionGoal goal = findGoal(id);
        goalMapper.updateGoalFromDto(request, goal);
        goalRepository.save(goal);

        log.info("Goal updated: id={} targetYear={} baseline={} target={}",
                goal.getId(), goal.getTargetYear(),
                goal.getBaselineEmissionsKgco2e(), goal.getTargetEmissionsKgco2e());
        return goalMapper.toResponseDto(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id) {
        // Existence is checked first so an unknown id is a 404, not a silent
        // no-op: deleteById returns void whether or not the row existed.
        ReductionGoal goal = findGoal(id);
        goalRepository.delete(goal);
        log.info("Goal deleted: id={}", id);
    }

    /**
     * The two Section 9 goal rules, enforced on every write. Both throw
     * {@link BadRequestException} (→ 400, Section 23) with the message the
     * frontend shows the user.
     *
     * <p>{@code target < baseline} uses {@link java.math.BigDecimal#compareTo} —
     * never {@code equals} — so that {@code 84000} and {@code 84000.000} compare
     * as equal rather than differing on scale. {@code targetYear} is compared to
     * {@link Year#now()}, evaluated per call so the rule stays correct across a
     * year boundary without a restart.
     */
    private void validateRules(GoalRequestDto request) {
        if (request.getTargetEmissionsKgco2e().compareTo(request.getBaselineEmissionsKgco2e()) >= 0) {
            log.warn("Goal rejected — target {} is not below baseline {}",
                    request.getTargetEmissionsKgco2e(), request.getBaselineEmissionsKgco2e());
            throw new BadRequestException(TARGET_NOT_BELOW_BASELINE);
        }
        if (request.getTargetYear() < Year.now().getValue()) {
            log.warn("Goal rejected — target year {} is before the current year", request.getTargetYear());
            throw new BadRequestException(YEAR_IN_PAST);
        }
    }

    private ReductionGoal findGoal(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Goal lookup failed for id={}", id);
                    return new ResourceNotFoundException(GOAL_NOT_FOUND + id);
                });
    }
}
