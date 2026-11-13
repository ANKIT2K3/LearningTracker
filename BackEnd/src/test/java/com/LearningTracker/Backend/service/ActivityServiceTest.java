package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.CreateActivityRequest;
import com.LearningTracker.Backend.dto.request.UpdateActivityRequest;
import com.LearningTracker.Backend.dto.response.ActivityResponse;
import com.LearningTracker.Backend.entity.Activity;
import com.LearningTracker.Backend.repository.ActivityRepository;
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
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    private CreateActivityRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateActivityRequest();
        createRequest.setTitle("Java 21 Fundamentals");
        createRequest.setTopic("Core Java");
        createRequest.setPlannedHours(new BigDecimal("12.50"));
    }

    @Test
    @DisplayName("Should create activity successfully")
    void testCreateActivity_Success() {
        Activity savedActivity = new Activity();
        savedActivity.setTitle(createRequest.getTitle());
        savedActivity.setTopic(createRequest.getTopic());
        savedActivity.setPlannedHours(createRequest.getPlannedHours());

        when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

        ActivityResponse response = activityService.createActivity(createRequest);

        assertNotNull(response);
        assertEquals("Java 21 Fundamentals", response.getTitle());
        assertEquals("Core Java", response.getTopic());
        assertEquals(new BigDecimal("12.50"), response.getPlannedHours());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("Should update existing activity successfully")
    void testUpdateActivity_Success() {
        Activity existing = new Activity();
        existing.setTitle("Old Title");
        existing.setTopic("Old Topic");
        existing.setPlannedHours(new BigDecimal("5.00"));

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(activityRepository.save(existing)).thenReturn(existing);

        UpdateActivityRequest updateReq = new UpdateActivityRequest();
        updateReq.setTitle("New Title");
        updateReq.setTopic("New Topic");
        updateReq.setPlannedHours(new BigDecimal("8.00"));

        ActivityResponse response = activityService.updateActivity(1L, updateReq);

        assertNotNull(response);
        assertEquals("New Title", response.getTitle());
        assertEquals("New Topic", response.getTopic());
        assertEquals(new BigDecimal("8.00"), response.getPlannedHours());
    }

    @Test
    @DisplayName("Should throw 404 when updating non-existent activity")
    void testUpdateActivity_NotFound() {
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateActivityRequest updateReq = new UpdateActivityRequest();
        updateReq.setTitle("Updated");
        updateReq.setTopic("Topic");
        updateReq.setPlannedHours(new BigDecimal("4.00"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            activityService.updateActivity(999L, updateReq);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Activity not found", ex.getReason());
    }

    @Test
    @DisplayName("Should list all activities")
    void testGetAllActivities() {
        Activity a1 = new Activity();
        a1.setTitle("Act 1");
        a1.setTopic("Topic 1");
        a1.setPlannedHours(new BigDecimal("2.00"));

        when(activityRepository.findAll()).thenReturn(List.of(a1));

        List<ActivityResponse> list = activityService.getAllActivities();
        assertEquals(1, list.size());
        assertEquals("Act 1", list.get(0).getTitle());
    }
}
