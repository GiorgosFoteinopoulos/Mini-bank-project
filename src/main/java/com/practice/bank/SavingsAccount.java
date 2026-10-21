package com.practice.bank;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;


@Entity
@DiscriminatorValue("SAVINGS")
public class SavingsAccount extends Account implements InterestBearing {

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("200.00");
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.03");

    protected SavingsAccount() {

    }
    public SavingsAccount(String holder, String pinHash, BigDecimal openingBalance) {
        super(holder, pinHash, openingBalance);
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    @Override
    public BigDecimal getBalance() {
        return getAvailableBalance();
    }

    @Override
    public BigDecimal getDailyLimit() {
        return DAILY_LIMIT;
    }

    @Override
    public BigDecimal getInterestRate() {
        return INTEREST_RATE;
    }
}
