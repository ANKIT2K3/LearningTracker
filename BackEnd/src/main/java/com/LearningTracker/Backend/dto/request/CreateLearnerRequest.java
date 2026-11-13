package com.LearningTracker.Backend.dto.request;

import jakarta.validation.constraints.*;

public class CreateLearnerRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 150)
    private String displayName;

    @NotBlank
    @Size(max = 100)
    private String cohort;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCohort() { return cohort; }
    public void setCohort(String cohort) { this.cohort = cohort; }
}
