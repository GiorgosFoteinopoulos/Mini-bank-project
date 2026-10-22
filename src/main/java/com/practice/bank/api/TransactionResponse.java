package com.practice.bank.api;

import com.practice.bank.Transaction;

import java.time.LocalDateTime;

public record TransactionResponse(
        LocalDateTime timestamp,
        String type,
        boolean credit,
        String amount,
        String balanceAfter,
        String description) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.timestamp(),
                t.type().name(),
                t.type().isCredit(),
                t.amount().toPlainString(),
                t.balanceAfter().toPlainString(),
                t.description());
    }
}
