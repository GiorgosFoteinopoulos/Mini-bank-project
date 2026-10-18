package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import com.practice.bank.exception.NotLoggedInException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountSession {

    private static final String ACCOUNT_NUMBER = "accountNumber";
    private final Bank bank;

    public AccountSession(Bank bank) {
        this.bank = bank;
    }

    public void logIn(HttpServletRequest request, Account account) {
        request.getSession();
        request.changeSessionId();
        request.getSession().setAttribute(ACCOUNT_NUMBER, account.getAccountNumber());
    }

    public void logOut(HttpSession session) {
        session.invalidate();
    }

    public Optional<Account> currentAccount(HttpSession session) {
        if (session.getAttribute(ACCOUNT_NUMBER) instanceof Long number) {
            return bank.findAccount(number);
        }
        return Optional.empty();
    }
    public Account requireAccount(HttpSession session) {
        return currentAccount(session).orElseThrow(NotLoggedInException::new);
    }
}
