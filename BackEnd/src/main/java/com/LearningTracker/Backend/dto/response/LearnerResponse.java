package com.LearningTracker.Backend.dto.response;

public class LearnerResponse {

    private Long id;
    private String email;
    /** Exposed as "name" so the frontend Learner type ({ id, name, email, cohort }) matches. */
    private String name;
    private String cohort;
    private boolean mustChangePassword;

    public LearnerResponse() {}

    public LearnerResponse(Long id, String email, String name, String cohort, boolean mustChangePassword) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.cohort = cohort;
        this.mustChangePassword = mustChangePassword;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return name; }
    public void setDisplayName(String displayName) { this.name = displayName; }

    public String getCohort() { return cohort; }
    public void setCohort(String cohort) { this.cohort = cohort; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
}
