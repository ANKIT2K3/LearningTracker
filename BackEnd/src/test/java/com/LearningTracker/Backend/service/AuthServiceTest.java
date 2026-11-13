package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.ChangePasswordRequest;
import com.LearningTracker.Backend.dto.request.LoginRequest;
import com.LearningTracker.Backend.dto.response.LoginResponse;
import com.LearningTracker.Backend.dto.response.MessageResponse;
import com.LearningTracker.Backend.entity.Account;
import com.LearningTracker.Backend.repository.AccountRepository;
import com.LearningTracker.Backend.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession httpSession;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setEmail("user@test.com");
        account.setPasswordHash("hashed_pw");
        account.setRole("COORDINATOR");
        account.setMustChangePassword(false);
    }

    @Test
    @DisplayName("Should successfully login user and initiate session")
    void testLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("rawPassword");

        CustomUserDetails userDetails = new CustomUserDetails(account);
        when(accountRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(account));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(httpRequest.getSession(true)).thenReturn(httpSession);

        LoginResponse response = authService.login(req, httpRequest);

        assertNotNull(response);
        assertEquals("user@test.com", response.getEmail());
        assertEquals("COORDINATOR", response.getRole());
        assertFalse(response.isMustChangePassword());
        verify(httpSession).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("Should throw 401 UNAUTHORIZED if temporary password expired")
    void testLogin_ExpiredTempPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("tempPassword");

        account.setMustChangePassword(true);
        account.setTempPasswordExpiresAt(LocalDateTime.now().minusHours(1)); // expired 1 hr ago
        when(accountRepository.findByEmailIgnoreCase("user@test.com")).thenReturn(Optional.of(account));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            authService.login(req, httpRequest);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Temporary password has expired"));
    }

    @Test
    @DisplayName("Should successfully change password and clear temporary status")
    void testChangePassword_Success() {
        account.setMustChangePassword(true);
        account.setTempPasswordExpiresAt(LocalDateTime.now().plusHours(10));

        CustomUserDetails userDetails = new CustomUserDetails(account);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("oldPass", "hashed_pw")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("new_hash");

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("oldPass");
        req.setNewPassword("newPass123");

        MessageResponse response = authService.changePassword(req, authentication);

        assertNotNull(response);
        assertEquals("Password changed successfully", response.getMessage());
        assertFalse(account.isMustChangePassword());
        assertNull(account.getTempPasswordExpiresAt());
        assertEquals("new_hash", account.getPasswordHash());
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST if current password does not match")
    void testChangePassword_IncorrectCurrentPassword() {
        CustomUserDetails userDetails = new CustomUserDetails(account);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrongPass", "hashed_pw")).thenReturn(false);

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("wrongPass");
        req.setNewPassword("newPass123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            authService.changePassword(req, authentication);
        });

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Current password is incorrect", ex.getReason());
    }

    @Test
    @DisplayName("Should invalidate session on logout")
    void testLogout() {
        when(httpRequest.getSession(false)).thenReturn(httpSession);

        MessageResponse response = authService.logout(httpRequest);

        assertNotNull(response);
        assertEquals("Logged out successfully", response.getMessage());
        verify(httpSession).invalidate();
    }
}
