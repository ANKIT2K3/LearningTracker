package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.ChangePasswordRequest;
import com.LearningTracker.Backend.dto.request.LoginRequest;
import com.LearningTracker.Backend.dto.response.LoginResponse;
import com.LearningTracker.Backend.dto.response.MessageResponse;
import com.LearningTracker.Backend.entity.Account;
import com.LearningTracker.Backend.repository.AccountRepository;
import com.LearningTracker.Backend.repository.LearnerRepository;
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

    // 8-hour session, matching the frontend default expiresAt: Date.now() + 28_800_000
    private static final long SESSION_MS = 28_800_000L;

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final LearnerRepository learnerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       AccountRepository accountRepository,
                       LearnerRepository learnerRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.learnerRepository = learnerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest req, HttpServletRequest httpRequest) {
        // Check temp password expiry before authenticating
        accountRepository.findByEmailIgnoreCase(req.getEmail()).ifPresent(acc -> {
            if (acc.isMustChangePassword() && acc.getTempPasswordExpiresAt() != null
                    && acc.getTempPasswordExpiresAt().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Temporary password has expired. Contact coordinator.");
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

        return buildLoginResponse(account);
    }

    @Transactional
    public LoginResponse changePassword(ChangePasswordRequest req, Authentication authentication) {
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

        // Return fresh login response so the frontend can update its session
        return buildLoginResponse(account);
    }

    public MessageResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return new MessageResponse("Logged out successfully");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private LoginResponse buildLoginResponse(Account account) {
        // Determine display name: coordinators use email prefix; learners use displayName
        String name = resolveDisplayName(account);

        // Map role to frontend convention: "COORDINATOR" -> "Coordinator", "LEARNER" -> "Candidate"
        String frontendRole = "LEARNER".equalsIgnoreCase(account.getRole()) ? "Candidate" : "Coordinator";

        long expiresAt = System.currentTimeMillis() + SESSION_MS;

        return new LoginResponse(
                account.getId(),
                name,
                account.getEmail(),
                frontendRole,
                account.isMustChangePassword(),
                expiresAt,
                1  // version — incremented on password reset; kept at 1 for coordinator accounts
        );
    }

    private String resolveDisplayName(Account account) {
        if ("LEARNER".equalsIgnoreCase(account.getRole())) {
            return learnerRepository.findByAccount_Id(account.getId())
                    .map(l -> l.getDisplayName())
                    .orElse(account.getEmail());
        }
        // Coordinator: use email prefix as name (e.g. "alex.taylor@..." -> "alex.taylor")
        String email = account.getEmail();
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
