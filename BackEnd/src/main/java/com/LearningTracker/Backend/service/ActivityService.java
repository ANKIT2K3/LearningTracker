
package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.CreateActivityRequest;
import com.LearningTracker.Backend.dto.request.UpdateActivityRequest;
import com.LearningTracker.Backend.dto.response.ActivityResponse;
import com.LearningTracker.Backend.entity.Activity;
import com.LearningTracker.Backend.repository.ActivityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getAllActivities() {
        return activityRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        return toResponse(activity);
    }

    @Transactional
    public ActivityResponse createActivity(CreateActivityRequest req) {
        Activity activity = new Activity();
        activity.setTitle(req.getTitle());
        activity.setTopic(req.getTopic());
        activity.setPlannedHours(req.getPlannedHours());
        return toResponse(activityRepository.save(activity));
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, UpdateActivityRequest req) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        activity.setTitle(req.getTitle());
        activity.setTopic(req.getTopic());
        activity.setPlannedHours(req.getPlannedHours());
        return toResponse(activityRepository.save(activity));
    }

    public ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getTitle(),
                activity.getTopic(),
                activity.getPlannedHours()
        );
    }
}
