package com.practice.bank;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CHECKING")
public class CheckingAccount extends Account {

    private  static final BigDecimal DAILY_LIMIT = new BigDecimal("500.00");
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("200.00");


    protected CheckingAccount() {

    }
    public CheckingAccount(String holder, String pinHash, BigDecimal openingBalance) {
        super(holder, pinHash, openingBalance);
    }
    
    @Override
    public String getAccountType() {
        return "Checking";
    }

    @Override
    public BigDecimal getDailyLimit() {
        return DAILY_LIMIT;
    }

    @Override
    public BigDecimal getAvailableBalance() {
        return getBalance().add(OVERDRAFT_LIMIT);
    }

    public BigDecimal getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }
}
