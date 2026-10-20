package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import com.practice.bank.exception.NotLoggedInException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountSession {

    private final Bank bank;

    public AccountSession(Bank bank) {
        this.bank = bank;
    }

    public Optional<Account> currentAccount(Authentication authentication) {
        if (!hasRole(authentication, "ROLE_CUSTOMER")) {
            return Optional.empty();
        }
        return bank.findAccount(Long.parseLong(authentication.getName()));
    }

    public Account requireAccount(Authentication authentication) {
        return currentAccount(authentication).orElseThrow(NotLoggedInException::new);
    }

    public static boolean hasRole(Authentication authentication, String role) {
        return authentication !=null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
