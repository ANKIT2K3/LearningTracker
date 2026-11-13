package com.LearningTracker.Backend.repository;

import com.LearningTracker.Backend.entity.Assignment;
import com.LearningTracker.Backend.entity.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByLearner_Id(Long learnerId);
    boolean existsByLearner_IdAndActivity_Id(Long learnerId, Long activityId);
    Optional<Assignment> findByIdAndLearner_Id(Long id, Long learnerId);

    @Query("SELECT a.learner.cohort AS cohort, " +
           "COUNT(a) AS total, " +
           "SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed " +
           "FROM Assignment a GROUP BY a.learner.cohort")
    List<Object[]> findCohortSummaryRaw();
}
