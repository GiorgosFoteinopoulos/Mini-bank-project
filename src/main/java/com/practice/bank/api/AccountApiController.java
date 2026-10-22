package com.practice.bank.api;

import com.practice.bank.Bank;
import com.practice.bank.web.AccountSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @PostMapping("/deposits")
    public AccountResponse deposit(@Valid @RequestBody AmountRequest request, Authentication authentication) {
        long number = accountSession.requireAccountNumber(authentication);
        bank.deposit(number, toMoney(request.amount()));
        return AccountResponse.from(bank.getAccount(number));
    }

    @PostMapping("/withdrawals")
    public AccountResponse withdraw(@Valid @RequestBody AmountRequest request, Authentication authentication) {
        long number = accountSession.requireAccountNumber(authentication);
        bank.withdraw(number, toMoney(request.amount()));
        return AccountResponse.from(bank.getAccount(number));
    }

    @PostMapping("/transfers")
    public AccountResponse transfer(@Valid @ RequestBody TransferRequest request, Authentication authentication) {
        long number = accountSession.requireAccountNumber(authentication);
        bank.transfer(number, request.targetAccount(), toMoney(request.amount()));
        return AccountResponse.from(bank.getAccount(number));
    }

    private static BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
