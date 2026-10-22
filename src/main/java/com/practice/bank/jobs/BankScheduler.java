package com.practice.bank.jobs;



import com.practice.bank.Account;
import com.practice.bank.Bank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class BankScheduler {

    private static final Logger log = LoggerFactory.getLogger(BankScheduler.class);

    private final Bank bank;

    public BankScheduler(Bank bank) {
        this.bank = bank;
    }

    @Scheduled(cron = "${bank.schedule.new-day}", zone = "Europe/Athens")
    public void startNewDay() {
        bank.startNewDay();
        log.info("New banking day started: all daily limits reset.");
    }

    @Scheduled(cron = "${bank.schedule.interest}", zone = "Europe/Athens")
    public void payYearlyInterest() {
        Map<Account, BigDecimal> payments = bank.applyYearlyInterest();
        BigDecimal total = payments.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Yearly interest paid: {} EUR to {} savings account(s).", total.toPlainString(), payments.size());
    }
}
