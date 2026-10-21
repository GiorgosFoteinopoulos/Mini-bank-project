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
      long existing = bank.getNumberOfAccounts();
      if (existing > 0) {
          log.info("Database already contains {} accounts. No demo data added", existing);
          return;
      }

      Account george = bank.openCheckingAccount("George", "1234", new BigDecimal("150.00"));
      bank.openSavingsAccount("Anna", "5678", new BigDecimal("80.00"));
      Account maria = bank.openSavingsAccount("Maria", "4321", new BigDecimal("300.00"));

      bank.deposit(george.getAccountNumber(), new BigDecimal("50.00"));
      bank.withdraw(george.getAccountNumber(), new BigDecimal("20.00"));
      bank.transfer(george.getAccountNumber(), maria.getAccountNumber(), new BigDecimal("30.00"));
      bank.applyYearlyInterest();

      log.info("Demo data loaded: {} accounts", bank.getNumberOfAccounts());
    }
}
