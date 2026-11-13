package com.LearningTracker.Backend.security;

import com.LearningTracker.Backend.repository.AccountRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return accountRepository.findByEmailIgnoreCase(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account for email: " + email));
    }
}
