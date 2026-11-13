package com.LearningTracker.Backend.dto.request;

import com.LearningTracker.Backend.entity.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateAssignmentStatusRequest {

    @NotNull
    private AssignmentStatus status;

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
}
