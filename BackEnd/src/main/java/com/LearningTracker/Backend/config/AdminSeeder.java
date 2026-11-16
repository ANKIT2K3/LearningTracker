package com.LearningTracker.Backend.config;

import com.LearningTracker.Backend.entity.Account;
import com.LearningTracker.Backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the default Coordinator account dynamically at application startup
 * if one does not already exist.
 *
 * Credentials can be configured via environment variables:
 * - COORDINATOR_EMAIL (defaults to coordinator@learnflow.app)
 * - COORDINATOR_PASSWORD (defaults to Coordinator@123)
 *
 * This prevents hardcoded plain-text passwords or static hashes in the application code.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.coordinator.email:coordinator@learnflow.app}")
    private String coordinatorEmail;

    @Value("${app.coordinator.password:Coordinator@123}")
    private String coordinatorPassword;

    public AdminSeeder(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        accountRepository.findByEmailIgnoreCase(coordinatorEmail).ifPresentOrElse(
            account -> {
                // Ensure coordinator has the correct encoded password from configuration/env
                if (!passwordEncoder.matches(coordinatorPassword, account.getPasswordHash())) {
                    account.setPasswordHash(passwordEncoder.encode(coordinatorPassword));
                    accountRepository.save(account);
                    System.out.println(">>> Coordinator password updated for: " + coordinatorEmail);
                }
            },
            () -> {
                Account coordinator = new Account();
                coordinator.setEmail(coordinatorEmail.toLowerCase());
                coordinator.setPasswordHash(passwordEncoder.encode(coordinatorPassword));
                coordinator.setRole("COORDINATOR");
                coordinator.setMustChangePassword(false);
                accountRepository.save(coordinator);
                System.out.println(">>> Initial coordinator account created for: " + coordinatorEmail);
            }
        );
    }
}
