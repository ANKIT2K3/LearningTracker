package com.LearningTracker.Backend.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateActivityRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 150)
    private String topic;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 4, fraction = 2)
    private BigDecimal plannedHours;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }
}
