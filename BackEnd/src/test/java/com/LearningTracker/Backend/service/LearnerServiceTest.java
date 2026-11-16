package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.CreateLearnerRequest;
import com.LearningTracker.Backend.dto.response.CreateLearnerResponse;
import com.LearningTracker.Backend.dto.response.LearnerResponse;
import com.LearningTracker.Backend.dto.response.ResetPasswordResponse;
import com.LearningTracker.Backend.entity.Account;
import com.LearningTracker.Backend.entity.Learner;
import com.LearningTracker.Backend.repository.AccountRepository;
import com.LearningTracker.Backend.repository.LearnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearnerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LearnerRepository learnerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LearnerService learnerService;

    private CreateLearnerRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateLearnerRequest();
        createRequest.setEmail("learner@example.com");
        createRequest.setDisplayName("John Doe");
        createRequest.setCohort("Cohort-2026");
    }

    @Test
    @DisplayName("Should successfully create a learner with a temporary password and account")
    void testCreateLearner_Success() {
        when(accountRepository.existsByEmailIgnoreCase(createRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_temp_pw");

        Account savedAccount = new Account();
        savedAccount.setEmail(createRequest.getEmail().toLowerCase());
        savedAccount.setRole("LEARNER");
        savedAccount.setPasswordHash("hashed_temp_pw");
        savedAccount.setMustChangePassword(true);
        savedAccount.setTempPasswordExpiresAt(LocalDateTime.now().plusHours(24));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Learner savedLearner = new Learner();
        savedLearner.setAccount(savedAccount);
        savedLearner.setDisplayName(createRequest.getDisplayName());
        savedLearner.setCohort(createRequest.getCohort());
        when(learnerRepository.save(any(Learner.class))).thenReturn(savedLearner);

        CreateLearnerResponse response = learnerService.createLearner(createRequest);

        assertNotNull(response);
        assertNotNull(response.getTemporaryPassword());
        assertEquals(12, response.getTemporaryPassword().length());
        assertEquals("learner@example.com", response.getLearner().getEmail());
        assertEquals("John Doe", response.getLearner().getDisplayName());
        assertEquals("Cohort-2026", response.getLearner().getCohort());
        assertTrue(response.getLearner().isMustChangePassword());

        verify(accountRepository).save(any(Account.class));
        verify(learnerRepository).save(any(Learner.class));
    }

    @Test
    @DisplayName("Should throw 409 CONFLICT if email is already in use")
    void testCreateLearner_DuplicateEmail() {
        when(accountRepository.existsByEmailIgnoreCase(createRequest.getEmail())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            learnerService.createLearner(createRequest);
        });

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Email already in use", ex.getReason());
        verify(accountRepository, never()).save(any(Account.class));
        verify(learnerRepository, never()).save(any(Learner.class));
    }

    @Test
    @DisplayName("Should retrieve learner by ID successfully")
    void testGetLearnerById_Success() {
        Account account = new Account();
        account.setEmail("test@test.com");
        account.setRole("LEARNER");
        account.setMustChangePassword(true);

        Learner learner = new Learner();
        learner.setAccount(account);
        learner.setDisplayName("Test Learner");
        learner.setCohort("Cohort-A");

        when(learnerRepository.findById(1L)).thenReturn(Optional.of(learner));

        LearnerResponse response = learnerService.getLearnerById(1L);
        assertNotNull(response);
        assertEquals("test@test.com", response.getEmail());
        assertEquals("Test Learner", response.getDisplayName());
    }

    @Test
    @DisplayName("Should throw 404 NOT_FOUND when learner ID does not exist")
    void testGetLearnerById_NotFound() {
        when(learnerRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            learnerService.getLearnerById(99L);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Learner not found", ex.getReason());
    }

    @Test
    @DisplayName("Should reset learner password and return temporary password")
    void testResetPassword_Success() {
        Account account = new Account();
        account.setEmail("reset@test.com");
        account.setMustChangePassword(false);

        Learner learner = new Learner();
        learner.setAccount(account);

        when(learnerRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(passwordEncoder.encode(anyString())).thenReturn("new_hash");

        ResetPasswordResponse response = learnerService.resetPassword(1L);

        assertNotNull(response);
        assertEquals("reset@test.com", response.getEmail());
        assertNotNull(response.getTemporaryPassword());
        assertEquals(12, response.getTemporaryPassword().length());
        assertTrue(response.getExpiresAt() > System.currentTimeMillis());
        assertTrue(account.isMustChangePassword());
        assertNotNull(account.getTempPasswordExpiresAt());
        verify(accountRepository).save(account);
    }
}
