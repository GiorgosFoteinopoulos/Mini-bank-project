package com.practice.bank.api;


import com.practice.bank.Bank;
import com.practice.bank.web.AccountSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class AccountApiController {

    private final Bank bank;
    private final AccountSession accountSession;

    public AccountApiController(Bank bank, AccountSession accountSession) {
        this.bank = bank;
        this.accountSession = accountSession;
    }

    @GetMapping("/account")
    public AccountResponse account(Authentication authentication) {
        long number = accountSession.requireAccountNumber(authentication);
        return AccountResponse.from(bank.getAccount(number));
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> transactions(Authentication authentication) {
        long number = accountSession.requireAccountNumber(authentication);
        return bank.getAccountWithTransactions(number).getTransactions().reversed().stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
