package com.LearningTracker.Backend.controller;

import com.LearningTracker.Backend.dto.request.CreateActivityRequest;
import com.LearningTracker.Backend.dto.request.UpdateActivityRequest;
import com.LearningTracker.Backend.dto.response.ActivityResponse;
import com.LearningTracker.Backend.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getAll() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ActivityResponse> create(@Valid @RequestBody CreateActivityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createActivity(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ActivityResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateActivityRequest req) {
        return ResponseEntity.ok(activityService.updateActivity(id, req));
    }
}
