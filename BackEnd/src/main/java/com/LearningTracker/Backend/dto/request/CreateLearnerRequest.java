package com.LearningTracker.Backend.dto.request;

import jakarta.validation.constraints.*;

public class CreateLearnerRequest {

    @NotBlank
    @Email
    private String email;

    /**
     * Frontend sends "name" (from the candidate form field).
     * Stored in learners.display_name.
     */
    @NotBlank
    @Size(min = 2, max = 150)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String cohort;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /** Alias: the frontend form field is "name", maps to displayName in entity. */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Convenience getter & setter used by LearnerService and tests */
    public String getDisplayName() { return name; }
    public void setDisplayName(String displayName) { this.name = displayName; }

    public String getCohort() { return cohort; }
    public void setCohort(String cohort) { this.cohort = cohort; }
}
