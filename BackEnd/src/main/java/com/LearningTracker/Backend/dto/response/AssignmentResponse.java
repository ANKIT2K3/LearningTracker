package com.LearningTracker.Backend.dto.response;

import com.LearningTracker.Backend.entity.enums.AssignmentStatus;

public class AssignmentResponse {

    private Long id;
    private Long learnerId;
    private ActivityResponse activity;
    private AssignmentStatus status;

    public AssignmentResponse() {}

    public AssignmentResponse(Long id, Long learnerId, ActivityResponse activity, AssignmentStatus status) {
        this.id = id;
        this.learnerId = learnerId;
        this.activity = activity;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public ActivityResponse getActivity() { return activity; }
    public void setActivity(ActivityResponse activity) { this.activity = activity; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
}
