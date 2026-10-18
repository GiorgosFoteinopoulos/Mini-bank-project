package com.practice.bank;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.boot.CommandLineRunner;

import java.math.BigDecimal;


@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final Bank bank;

    public DemoDataSeeder(Bank bank) {
        this.bank = bank;
    }

    @Override
    public void run(String... args) {
       Account george = bank.openCheckingAccount("George", "1234", new BigDecimal("150.00"));
       bank.openSavingsAccount("Anna", "5678", new BigDecimal("80.00"));
       Account maria = bank.openSavingsAccount("Maria", "4321", new BigDecimal("300.00"));

       george.deposit(new BigDecimal("50.00"));
       george.withdraw(new BigDecimal("20.00"));
       george.transferTo(maria, new BigDecimal("30.00"));
       bank.applyYearlyInterest();

       log.info("Demo data loaded: {} accounts", bank.getNumberOfAccounts());
    }
}
