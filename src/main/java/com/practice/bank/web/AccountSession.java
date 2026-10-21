package com.practice.bank.web;

import com.practice.bank.Account;
import com.practice.bank.Bank;
import com.practice.bank.exception.NotLoggedInException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountSession {

    private final Bank bank;

    public AccountSession(Bank bank) {
        this.bank = bank;
    }

    public Optional<Long> currentAccountNumber(Authentication authentication) {
        if (!hasRole(authentication, "ROLE_CUSTOMER")) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(authentication.getName()));
    }

    public long requireAccountNumber(Authentication authentication) {
        return currentAccountNumber(authentication).orElseThrow(NotLoggedInException::new);
    }

    public Optional<Account> currentAccount(Authentication authentication) {
        return currentAccountNumber(authentication).flatMap(bank::findAccount);
    }

    public static boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}