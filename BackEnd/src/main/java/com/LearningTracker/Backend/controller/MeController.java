package com.LearningTracker.Backend.controller;

import com.LearningTracker.Backend.dto.request.UpdateAssignmentStatusRequest;
import com.LearningTracker.Backend.dto.response.AssignmentResponse;
import com.LearningTracker.Backend.entity.Learner;
import com.LearningTracker.Backend.repository.LearnerRepository;
import com.LearningTracker.Backend.security.CustomUserDetails;
import com.LearningTracker.Backend.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@PreAuthorize("hasRole('LEARNER')")
public class MeController {

    private final AssignmentService assignmentService;
    private final LearnerRepository learnerRepository;

    public MeController(AssignmentService assignmentService, LearnerRepository learnerRepository) {
        this.assignmentService = assignmentService;
        this.learnerRepository = learnerRepository;
    }

    private Long resolveLearnerId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long accountId = userDetails.getAccount().getId();
        Learner learner = learnerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learner profile not found"));
        return learner.getId();
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResponse>> getMyAssignments(Authentication authentication) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForLearner(resolveLearnerId(authentication)));
    }

    @PatchMapping("/assignments/{id}/status")
    public ResponseEntity<AssignmentResponse> updateMyAssignmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentStatusRequest req,
            Authentication authentication) {
        Long learnerId = resolveLearnerId(authentication);
        return ResponseEntity.ok(assignmentService.updateStatusForLearner(id, learnerId, req));
    }
}
