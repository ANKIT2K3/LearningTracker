package com.LearningTracker.Backend.dto.response;

public class CreateLearnerResponse {

    private LearnerResponse learner;
    // The raw temp password — shown once to coordinator
    private String temporaryPassword;
    // Email duplicated at top level so frontend gets {email, temporaryPassword, expiresAt}
    private String email;
    private long expiresAt;

    public CreateLearnerResponse() {}

    public CreateLearnerResponse(LearnerResponse learner, String temporaryPassword, long expiresAt) {
        this.learner = learner;
        this.temporaryPassword = temporaryPassword;
        this.email = learner.getEmail();
        this.expiresAt = expiresAt;
    }

    public LearnerResponse getLearner() { return learner; }
    public void setLearner(LearnerResponse learner) { this.learner = learner; }

    public String getTemporaryPassword() { return temporaryPassword; }
    public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
}
