package com.practice.bank.api;


import com.practice.bank.Account;

public record AccountResponse(
        Long accountNumber,
        String holder,
        String type,
        String balance,
        String availableBalance,
        String remainingDailyLimit) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getHolder(),
                account.getAccountType(),
                account.getBalance().toPlainString(),
                account.getAvailableBalance().toPlainString(),
                account.getRemainingDailyLimit().toPlainString()
        );
    }
}
