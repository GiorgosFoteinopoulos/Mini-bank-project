package com.practice.bank;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


@Embeddable
public record Transaction(
        @Column(name= "occured_at", nullable = false)
        LocalDateTime timestamp,

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        TransactionType type,

        @Column(nullable = false, precision = 15, scale = 2)
        BigDecimal amount,

        @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
        BigDecimal balanceAfter,

        @Column(nullable = false)
        String description) {

    public Transaction {
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(type, "type is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be zero or positive");
        }
    }
}
