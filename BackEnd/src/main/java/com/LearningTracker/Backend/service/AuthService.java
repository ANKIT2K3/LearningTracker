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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest req, HttpServletRequest httpRequest) {
        // Check temp password expiry before authenticating
        accountRepository.findByEmailIgnoreCase(req.getEmail()).ifPresent(acc -> {
            if (acc.isMustChangePassword() && acc.getTempPasswordExpiresAt() != null
                    && acc.getTempPasswordExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Temporary password has expired. Contact coordinator.");
            }
        });

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Account account = userDetails.getAccount();

        return new LoginResponse(account.getId(), account.getEmail(), account.getRole(), account.isMustChangePassword());
    }

    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest req, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Account account = accountRepository.findById(userDetails.getAccount().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        account.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        account.setMustChangePassword(false);
        account.setTempPasswordExpiresAt(null);
        accountRepository.save(account);

        return new MessageResponse("Password changed successfully");
    }

    public MessageResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return new MessageResponse("Logged out successfully");
    }
}
