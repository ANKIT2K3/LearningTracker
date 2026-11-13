package com.LearningTracker.Backend.controller;

import com.LearningTracker.Backend.dto.response.CohortSummaryResponse;
import com.LearningTracker.Backend.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CohortController {

    private final AssignmentService assignmentService;

    public CohortController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/cohort-summary")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<List<CohortSummaryResponse>> getCohortSummary() {
        return ResponseEntity.ok(assignmentService.getCohortSummary());
    }
}
