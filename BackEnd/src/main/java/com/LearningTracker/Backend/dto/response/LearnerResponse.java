package com.LearningTracker.Backend.dto.response;

public class LearnerResponse {

    private Long id;
    private String email;
    private String displayName;
    private String cohort;
    private boolean mustChangePassword;

    public LearnerResponse() {}

    public LearnerResponse(Long id, String email, String displayName, String cohort, boolean mustChangePassword) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.cohort = cohort;
        this.mustChangePassword = mustChangePassword;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCohort() { return cohort; }
    public void setCohort(String cohort) { this.cohort = cohort; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
}
