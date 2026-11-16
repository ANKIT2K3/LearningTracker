package com.LearningTracker.Backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean mustChangePassword;
    // expiresAt: 8 hours from now in epoch-millis, so the frontend session timer works
    private long expiresAt;
    private int version;

    public LoginResponse() {}

    public LoginResponse(Long id, String name, String email, String role,
                         boolean mustChangePassword, long expiresAt, int version) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
        this.expiresAt = expiresAt;
        this.version = version;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
