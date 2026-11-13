package com.LearningTracker.Backend.controller;

import com.LearningTracker.Backend.dto.request.UpdateAssignmentStatusRequest;
import com.LearningTracker.Backend.dto.response.ActivityResponse;
import com.LearningTracker.Backend.dto.response.AssignmentResponse;
import com.LearningTracker.Backend.entity.Account;
import com.LearningTracker.Backend.entity.Learner;
import com.LearningTracker.Backend.entity.enums.AssignmentStatus;
import com.LearningTracker.Backend.repository.LearnerRepository;
import com.LearningTracker.Backend.security.CustomUserDetails;
import com.LearningTracker.Backend.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private LearnerRepository learnerRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MeController meController;

    private Learner learner;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        Account account = new Account();
        account.setEmail("learner@test.com");
        account.setRole("LEARNER");

        learner = new Learner();
        learner.setAccount(account);
        learner.setDisplayName("Learner Test");

        userDetails = new CustomUserDetails(account);
    }

    @Test
    @DisplayName("Should fetch assignments for currently authenticated learner")
    void testGetMyAssignments() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(learnerRepository.findByAccount_Id(userDetails.getAccount().getId())).thenReturn(Optional.of(learner));

        ActivityResponse act = new ActivityResponse(1L, "React", "Frontend", new BigDecimal("5.00"));
        AssignmentResponse assign = new AssignmentResponse(10L, learner.getId(), act, AssignmentStatus.IN_PROGRESS);
        when(assignmentService.getAssignmentsForLearner(learner.getId())).thenReturn(List.of(assign));

        ResponseEntity<List<AssignmentResponse>> response = meController.getMyAssignments(authentication);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(AssignmentStatus.IN_PROGRESS, response.getBody().get(0).getStatus());
    }

    @Test
    @DisplayName("Should update status for learner's own assignment")
    void testUpdateMyAssignmentStatus() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(learnerRepository.findByAccount_Id(userDetails.getAccount().getId())).thenReturn(Optional.of(learner));

        UpdateAssignmentStatusRequest req = new UpdateAssignmentStatusRequest();
        req.setStatus(AssignmentStatus.COMPLETED);

        ActivityResponse act = new ActivityResponse(1L, "React", "Frontend", new BigDecimal("5.00"));
        AssignmentResponse updated = new AssignmentResponse(10L, learner.getId(), act, AssignmentStatus.COMPLETED);

        when(assignmentService.updateStatusForLearner(10L, learner.getId(), req)).thenReturn(updated);

        ResponseEntity<AssignmentResponse> response = meController.updateMyAssignmentStatus(10L, req, authentication);

        assertNotNull(response.getBody());
        assertEquals(AssignmentStatus.COMPLETED, response.getBody().getStatus());
    }
}
