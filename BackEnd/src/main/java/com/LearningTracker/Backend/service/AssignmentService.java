package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.CreateAssignmentRequest;
import com.LearningTracker.Backend.dto.request.UpdateAssignmentStatusRequest;
import com.LearningTracker.Backend.dto.response.ActivityResponse;
import com.LearningTracker.Backend.dto.response.AssignmentResponse;
import com.LearningTracker.Backend.dto.response.CohortSummaryResponse;
import com.LearningTracker.Backend.entity.Activity;
import com.LearningTracker.Backend.entity.Assignment;
import com.LearningTracker.Backend.entity.Learner;
import com.LearningTracker.Backend.repository.ActivityRepository;
import com.LearningTracker.Backend.repository.AssignmentRepository;
import com.LearningTracker.Backend.repository.LearnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LearnerRepository learnerRepository;
    private final ActivityRepository activityRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             LearnerRepository learnerRepository,
                             ActivityRepository activityRepository) {
        this.assignmentRepository = assignmentRepository;
        this.learnerRepository = learnerRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForLearner(Long learnerId) {
        learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learner not found"));
        return assignmentRepository.findByLearner_Id(learnerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponse createAssignment(CreateAssignmentRequest req) {
        Learner learner = learnerRepository.findById(req.getLearnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learner not found"));
        Activity activity = activityRepository.findById(req.getActivityId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        if (assignmentRepository.existsByLearner_IdAndActivity_Id(req.getLearnerId(), req.getActivityId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assignment already exists");
        }

        Assignment assignment = new Assignment();
        assignment.setLearner(learner);
        assignment.setActivity(activity);
        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponse updateStatus(Long assignmentId, UpdateAssignmentStatusRequest req) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        assignment.setStatus(req.getStatus());
        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponse updateStatusForLearner(Long assignmentId, Long learnerId, UpdateAssignmentStatusRequest req) {
        Assignment assignment = assignmentRepository.findByIdAndLearner_Id(assignmentId, learnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found or not yours"));
        assignment.setStatus(req.getStatus());
        return toResponse(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<CohortSummaryResponse> getCohortSummary() {
        return assignmentRepository.findCohortSummaryRaw()
                .stream()
                .map(row -> new CohortSummaryResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    private AssignmentResponse toResponse(Assignment a) {
        Activity act = a.getActivity();
        ActivityResponse ar = new ActivityResponse(act.getId(), act.getTitle(), act.getTopic(), act.getPlannedHours());
        return new AssignmentResponse(a.getId(), a.getLearner().getId(), ar, a.getStatus());
    }
}
