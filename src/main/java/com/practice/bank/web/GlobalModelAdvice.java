package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import jakarta.servlet.http.HttpSession;
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
    public Account currentAccount(HttpSession session) {
        return accountSession.currentAccount(session).orElse(null);
    }
}
