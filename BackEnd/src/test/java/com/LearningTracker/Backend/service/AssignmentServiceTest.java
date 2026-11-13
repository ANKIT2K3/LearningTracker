package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.CreateAssignmentRequest;
import com.LearningTracker.Backend.dto.request.UpdateAssignmentStatusRequest;
import com.LearningTracker.Backend.dto.response.AssignmentResponse;
import com.LearningTracker.Backend.dto.response.CohortSummaryResponse;
import com.LearningTracker.Backend.entity.Activity;
import com.LearningTracker.Backend.entity.Assignment;
import com.LearningTracker.Backend.entity.Learner;
import com.LearningTracker.Backend.entity.enums.AssignmentStatus;
import com.LearningTracker.Backend.repository.ActivityRepository;
import com.LearningTracker.Backend.repository.AssignmentRepository;
import com.LearningTracker.Backend.repository.LearnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private LearnerRepository learnerRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Learner learner;
    private Activity activity;

    @BeforeEach
    void setUp() {
        learner = new Learner();
        learner.setDisplayName("Jane Doe");
        learner.setCohort("Cohort-2026");

        activity = new Activity();
        activity.setTitle("Spring Boot Deep Dive");
        activity.setTopic("Spring");
        activity.setPlannedHours(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Should create assignment successfully when no duplicate exists")
    void testCreateAssignment_Success() {
        CreateAssignmentRequest req = new CreateAssignmentRequest();
        req.setLearnerId(1L);
        req.setActivityId(2L);

        when(learnerRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(activityRepository.findById(2L)).thenReturn(Optional.of(activity));
        when(assignmentRepository.existsByLearner_IdAndActivity_Id(1L, 2L)).thenReturn(false);

        Assignment saved = new Assignment();
        saved.setLearner(learner);
        saved.setActivity(activity);
        saved.setStatus(AssignmentStatus.NOT_STARTED);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(saved);

        AssignmentResponse res = assignmentService.createAssignment(req);

        assertNotNull(res);
        assertEquals(AssignmentStatus.NOT_STARTED, res.getStatus());
        assertEquals("Spring Boot Deep Dive", res.getActivity().getTitle());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if assignment already exists")
    void testCreateAssignment_DuplicateConflict() {
        CreateAssignmentRequest req = new CreateAssignmentRequest();
        req.setLearnerId(1L);
        req.setActivityId(2L);

        when(learnerRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(activityRepository.findById(2L)).thenReturn(Optional.of(activity));
        when(assignmentRepository.existsByLearner_IdAndActivity_Id(1L, 2L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            assignmentService.createAssignment(req);
        });

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Assignment already exists", ex.getReason());
        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    @DisplayName("Should update status as coordinator")
    void testUpdateStatus_Success() {
        Assignment assignment = new Assignment();
        assignment.setLearner(learner);
        assignment.setActivity(activity);
        assignment.setStatus(AssignmentStatus.NOT_STARTED);

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        UpdateAssignmentStatusRequest req = new UpdateAssignmentStatusRequest();
        req.setStatus(AssignmentStatus.IN_PROGRESS);

        AssignmentResponse res = assignmentService.updateStatus(10L, req);

        assertNotNull(res);
        assertEquals(AssignmentStatus.IN_PROGRESS, res.getStatus());
    }

    @Test
    @DisplayName("Learner should update their own assignment status successfully")
    void testUpdateStatusForLearner_Success() {
        Assignment assignment = new Assignment();
        assignment.setLearner(learner);
        assignment.setActivity(activity);
        assignment.setStatus(AssignmentStatus.NOT_STARTED);

        when(assignmentRepository.findByIdAndLearner_Id(10L, 1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        UpdateAssignmentStatusRequest req = new UpdateAssignmentStatusRequest();
        req.setStatus(AssignmentStatus.COMPLETED);

        AssignmentResponse res = assignmentService.updateStatusForLearner(10L, 1L, req);

        assertNotNull(res);
        assertEquals(AssignmentStatus.COMPLETED, res.getStatus());
    }

    @Test
    @DisplayName("Learner cannot update another learner's assignment (throws 404)")
    void testUpdateStatusForLearner_OwnershipCheck() {
        when(assignmentRepository.findByIdAndLearner_Id(10L, 99L)).thenReturn(Optional.empty());

        UpdateAssignmentStatusRequest req = new UpdateAssignmentStatusRequest();
        req.setStatus(AssignmentStatus.COMPLETED);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            assignmentService.updateStatusForLearner(10L, 99L, req);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Assignment not found or not yours", ex.getReason());
    }

    @Test
    @DisplayName("Should compute cohort summary aggregation properly")
    void testGetCohortSummary() {
        Object[] row1 = new Object[]{"Cohort-Alpha", 10L, 6L};
        Object[] row2 = new Object[]{"Cohort-Beta", 5L, 2L};
        when(assignmentRepository.findCohortSummaryRaw()).thenReturn(List.of(row1, row2));

        List<CohortSummaryResponse> summary = assignmentService.getCohortSummary();

        assertEquals(2, summary.size());
        assertEquals("Cohort-Alpha", summary.get(0).getCohort());
        assertEquals(10L, summary.get(0).getTotalAssignments());
        assertEquals(6L, summary.get(0).getCompletedAssignments());
    }
}
