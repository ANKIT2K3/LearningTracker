package com.LearningTracker.Backend.dto.response;

import java.math.BigDecimal;

public class ActivityResponse {

    private Long id;
    private String title;
    private String topic;
    private BigDecimal plannedHours;

    public ActivityResponse() {}

    public ActivityResponse(Long id, String title, String topic, BigDecimal plannedHours) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.plannedHours = plannedHours;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }
}
