package com.LearningTracker.Backend.dto.request;

import jakarta.validation.constraints.*;

/**
 * Frontend sends: { currentPassword, newPassword }
 */
public class ChangePasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
