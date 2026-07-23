package com.carbontrace.modules.goal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.goal.entity.ReductionGoal;

/**
 * Data access for {@code reduction_goals}.
 *
 * <p>Plain {@link JpaRepository}: Section 8.8 defines only by-id CRUD and an
 * unfiltered, unpaged list, both of which {@code findById} and {@code findAll}
 * already provide. No custom query is added — the list's ordering is supplied by
 * the service through a {@code Sort}, not by a derived method name.
 */
@Repository
public interface ReductionGoalRepository extends JpaRepository<ReductionGoal, Long> {
}
