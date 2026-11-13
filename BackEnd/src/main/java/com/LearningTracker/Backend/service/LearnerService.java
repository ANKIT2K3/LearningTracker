package com.LearningTracker.Backend.service;

import com.LearningTracker.Backend.dto.request.CreateLearnerRequest;
import com.LearningTracker.Backend.dto.response.CreateLearnerResponse;
import com.LearningTracker.Backend.dto.response.LearnerResponse;
import com.LearningTracker.Backend.dto.response.MessageResponse;
import com.LearningTracker.Backend.entity.Account;
import com.LearningTracker.Backend.entity.Learner;
import com.LearningTracker.Backend.repository.AccountRepository;
import com.LearningTracker.Backend.repository.LearnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearnerService {

    private final AccountRepository accountRepository;
    private final LearnerRepository learnerRepository;
    private final PasswordEncoder passwordEncoder;

    public LearnerService(AccountRepository accountRepository,
                          LearnerRepository learnerRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.learnerRepository = learnerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateLearnerResponse createLearner(CreateLearnerRequest req) {
        if (accountRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        String tempPassword = generateTempPassword();

        Account account = new Account();
        account.setEmail(req.getEmail().toLowerCase());
        account.setPasswordHash(passwordEncoder.encode(tempPassword));
        account.setRole("LEARNER");
        account.setMustChangePassword(true);
        account.setTempPasswordExpiresAt(LocalDateTime.now().plusHours(24));
        account = accountRepository.save(account);

        Learner learner = new Learner();
        learner.setAccount(account);
        learner.setDisplayName(req.getDisplayName());
        learner.setCohort(req.getCohort());
        learner = learnerRepository.save(learner);

        return new CreateLearnerResponse(toLearnerResponse(learner), tempPassword);
    }

    @Transactional(readOnly = true)
    public List<LearnerResponse> getAllLearners() {
        return learnerRepository.findAll()
                .stream()
                .map(this::toLearnerResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LearnerResponse getLearnerById(Long id) {
        Learner learner = learnerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learner not found"));
        return toLearnerResponse(learner);
    }

    @Transactional
    public MessageResponse resetPassword(Long learnerId) {
        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learner not found"));

        String tempPassword = generateTempPassword();
        Account account = learner.getAccount();
        account.setPasswordHash(passwordEncoder.encode(tempPassword));
        account.setMustChangePassword(true);
        account.setTempPasswordExpiresAt(LocalDateTime.now().plusHours(24));
        accountRepository.save(account);

        return new MessageResponse("Temporary password: " + tempPassword);
    }

    public LearnerResponse toLearnerResponse(Learner learner) {
        return new LearnerResponse(
                learner.getId(),
                learner.getAccount().getEmail(),
                learner.getDisplayName(),
                learner.getCohort(),
                learner.getAccount().isMustChangePassword()
        );
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
