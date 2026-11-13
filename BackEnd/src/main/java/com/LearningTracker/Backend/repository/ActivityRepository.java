package com.LearningTracker.Backend.repository;

import com.LearningTracker.Backend.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByTopicIgnoreCase(String topic);
    boolean existsByTitleIgnoreCase(String title);
}
