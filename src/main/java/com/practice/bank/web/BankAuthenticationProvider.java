package com.practice.bank.web;

import com.practice.bank.Bank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BankAuthenticationProvider implements AuthenticationProvider {

    private static final String LOGIN_FAILED = "Wrong account number or PIN";

    private final Bank bank;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPasswordHash;

    public BankAuthenticationProvider(Bank bank, PasswordEncoder passwordEncoder,
                                      @Value("${bank.admin.username}") String adminUsername,
                                      @Value("${bank.admin.password}") String adminPassword) {
        this.bank = bank;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPasswordHash = passwordEncoder.encode(adminPassword);
    }

    @Override
    public Authentication authenticate(Authentication attempt) {
        String username = attempt.getName().trim();
        String password = String.valueOf(attempt.getCredentials());

        if (username.equals(adminUsername)) {
            if (passwordEncoder.matches(password, adminPasswordHash)) {
                return authenticated(username, "ROLE_ADMIN");
            }
            throw new BadCredentialsException(LOGIN_FAILED);
        }

        boolean valid = parseAccountNumber(username)
                .map(number -> bank.checkPin(number, password))
                .orElse(false);
        if (valid) {
            return authenticated(username, "ROLE_CUSTOMER");
        }
        throw new BadCredentialsException(LOGIN_FAILED);
    }

    @Override
    public boolean supports(Class<?> attemptType) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(attemptType);
    }

    private static Authentication authenticated(String username, String role) {
        return UsernamePasswordAuthenticationToken.authenticated(
                username, null, List.of(new SimpleGrantedAuthority(role))
        );
    }

    private static Optional<Long> parseAccountNumber(String text) {
        try {
            return Optional.of(Long.parseLong(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
