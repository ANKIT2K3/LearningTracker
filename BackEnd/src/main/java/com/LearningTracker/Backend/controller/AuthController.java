package com.LearningTracker.Backend.controller;

import com.LearningTracker.Backend.dto.request.ChangePasswordRequest;
import com.LearningTracker.Backend.dto.request.LoginRequest;
import com.LearningTracker.Backend.dto.response.LoginResponse;
import com.LearningTracker.Backend.dto.response.MessageResponse;
import com.LearningTracker.Backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletRequest request) {
        return ResponseEntity.ok(authService.login(req, request));
    }

    /**
     * Returns a fresh LoginResponse (not MessageResponse) so the frontend can update
     * its in-memory session object (mustChangePassword = false) in one round-trip.
     * Frontend call: await request('/auth/change-password', 'POST', {...})
     * and then: saveSession({ ...getSession()!, mustChangePassword: false })
     */
    @PostMapping("/change-password")
    public ResponseEntity<LoginResponse> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                                        Authentication authentication) {
        return ResponseEntity.ok(authService.changePassword(req, authentication));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }
}
