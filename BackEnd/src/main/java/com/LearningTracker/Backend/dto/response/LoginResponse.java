package com.LearningTracker.Backend.dto.response;

public class LoginResponse {

    private Long accountId;
    private String email;
    private String role;
    private boolean mustChangePassword;

    public LoginResponse() {}

    public LoginResponse(Long accountId, String email, String role, boolean mustChangePassword) {
        this.accountId = accountId;
        this.email = email;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
}
