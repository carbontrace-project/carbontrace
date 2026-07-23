package com.carbontrace.modules.goal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.carbontrace.modules.goal.dto.GoalRequestDto;
import com.carbontrace.modules.goal.dto.GoalResponseDto;
import com.carbontrace.modules.goal.entity.ReductionGoal;

/**
 * MapStruct mapping for the goal module (COMMANDO.md Section 21: MapStruct for
 * ALL mapping — no manual field copying).
 */
@Mapper(componentModel = "spring")
public interface GoalMapper {

    /**
     * Entity to response. {@code createdBy} is flattened by path; every other
     * field maps by name.
     *
     * <p>{@code progressPercent} is ignored, not mapped: the entity has no such
     * field (progress is not stored, Section 8.9), so it stays null until
     * analytics computes it at STEP A024.
     */
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByEmail", source = "createdBy.email")
    @Mapping(target = "progressPercent", ignore = true)
    GoalResponseDto toResponseDto(ReductionGoal goal);

    /**
     * Request to a NEW entity for {@code POST /api/goals}.
     *
     * <p>{@code createdBy} is ignored: the service assigns the authenticated
     * principal, never the request. {@code id} and the timestamps belong to
     * Hibernate.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ReductionGoal toEntity(GoalRequestDto request);

    /**
     * Applies an edit onto an existing goal for {@code PUT /api/goals/{id}}.
     *
     * <p>Same ignore set as {@link #toEntity}, and for the same reason a review
     * cannot reassign a shipment: {@code createdBy} records who created the goal
     * and an edit must not rewrite that, nor {@code createdAt}. The four editable
     * fields — title, targetYear, and the two emission figures — map by name.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateGoalFromDto(GoalRequestDto request, @MappingTarget ReductionGoal goal);
}
