package com.LearningTracker.Backend.dto.response;

public class CreateLearnerResponse {

    private LearnerResponse learner;
    private String temporaryPassword;

    public CreateLearnerResponse() {}

    public CreateLearnerResponse(LearnerResponse learner, String temporaryPassword) {
        this.learner = learner;
        this.temporaryPassword = temporaryPassword;
    }

    public LearnerResponse getLearner() { return learner; }
    public void setLearner(LearnerResponse learner) { this.learner = learner; }

    public String getTemporaryPassword() { return temporaryPassword; }
    public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }
}
