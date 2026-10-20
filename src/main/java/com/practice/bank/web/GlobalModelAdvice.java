package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final Bank bank;
    private final AccountSession accountSession;

    public GlobalModelAdvice(Bank bank, AccountSession accountSession) {
        this.bank = bank;
        this.accountSession = accountSession;
    }

    @ModelAttribute("bankName")
    public String bankName() {
        return bank.getName();
    }

    @ModelAttribute("currentAccount")
    public Account currentAccount(Authentication authentication) {
        return accountSession.currentAccount(authentication).orElse(null);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return AccountSession.hasRole(authentication, "ROLE_ADMIN");
    }
}
