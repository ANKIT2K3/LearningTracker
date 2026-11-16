package com.LearningTracker.Backend.dto.response;

/**
 * Returned after a coordinator resets a learner's password.
 * Shape matches the frontend Credentials type: { email, temporaryPassword, expiresAt }
 */
public class ResetPasswordResponse {

    private String email;
    private String temporaryPassword;
    private long expiresAt;

    public ResetPasswordResponse() {}

    public ResetPasswordResponse(String email, String temporaryPassword, long expiresAt) {
        this.email = email;
        this.temporaryPassword = temporaryPassword;
        this.expiresAt = expiresAt;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTemporaryPassword() { return temporaryPassword; }
    public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
}

