package com.LearningTracker.Backend.controller;

import com.LearningTracker.Backend.dto.request.CreateAssignmentRequest;
import com.LearningTracker.Backend.dto.request.UpdateAssignmentStatusRequest;
import com.LearningTracker.Backend.dto.response.AssignmentResponse;
import com.LearningTracker.Backend.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assignments")
@PreAuthorize("hasRole('COORDINATOR')")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody CreateAssignmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createAssignment(req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AssignmentResponse> updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentStatusRequest req) {
        return ResponseEntity.ok(assignmentService.updateStatus(id, req));
    }
}
