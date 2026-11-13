package com.LearningTracker.Backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class CreateAssignmentRequest {

    @NotNull
    private Long learnerId;

    @NotNull
    private Long activityId;

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
}
