package com.LearningTracker.Backend.repository;

import com.LearningTracker.Backend.entity.Learner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearnerRepository extends JpaRepository<Learner, Long> {

    Optional<Learner> findByAccount_Id(Long accountId);

    List<Learner> findByCohortIgnoreCase(String cohort);
}
