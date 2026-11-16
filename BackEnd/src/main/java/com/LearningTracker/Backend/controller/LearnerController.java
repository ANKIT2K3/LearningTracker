package com.LearningTracker.Backend.controller;

import com.LearningTracker.Backend.dto.request.CreateLearnerRequest;
import com.LearningTracker.Backend.dto.response.AssignmentResponse;
import com.LearningTracker.Backend.dto.response.CreateLearnerResponse;
import com.LearningTracker.Backend.dto.response.LearnerResponse;
import com.LearningTracker.Backend.dto.response.ResetPasswordResponse;
import com.LearningTracker.Backend.service.AssignmentService;
import com.LearningTracker.Backend.service.LearnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learners")
@PreAuthorize("hasRole('COORDINATOR')")
public class LearnerController {

    private final LearnerService learnerService;
    private final AssignmentService assignmentService;

    public LearnerController(LearnerService learnerService, AssignmentService assignmentService) {
        this.learnerService = learnerService;
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ResponseEntity<List<LearnerResponse>> getAll() {
        return ResponseEntity.ok(learnerService.getAllLearners());
    }

    @PostMapping
    public ResponseEntity<CreateLearnerResponse> create(@Valid @RequestBody CreateLearnerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(learnerService.createLearner(req));
    }

    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForLearner(id));
    }

    /**
     * Returns { email, temporaryPassword, expiresAt } — matching the frontend Credentials type.
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(learnerService.resetPassword(id));
    }
}
