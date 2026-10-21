package com.practice.bank;
import com.practice.bank.exception.AccountNotEmptyException;
import com.practice.bank.exception.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;


@Service
@Transactional(readOnly = true)
public class Bank {

    private final AccountRepository accounts;
    private final String name;
    private final PasswordEncoder passwordEncoder;


    public Bank(AccountRepository accounts, PasswordEncoder passwordEncoder, @Value("${bank.name}") String name) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
    }
    public String getName() {
        return name;
    }

    @Transactional
    public CheckingAccount openCheckingAccount(String holder, String pin, BigDecimal openingBalance) {
        return accounts.save(new CheckingAccount(holder, hashPin(pin), openingBalance));
    }

    @Transactional
    public SavingsAccount openSavingsAccount( String holder, String pin, BigDecimal openingBalance) {
        return accounts.save(new SavingsAccount(holder, hashPin(pin), openingBalance));
    }

    @Transactional
    public void closeAccount(long accountNumber) {
        Account account = getAccount(accountNumber);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountNotEmptyException(accountNumber, account.getBalance());
        }
        accounts.delete(account);
    }

    private String hashPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits");
        }
        return passwordEncoder.encode(pin);
    }

    @Transactional
    public void withdraw(long accountNumber, BigDecimal amount) {
        getAccount(accountNumber).withdraw(amount);
    }

    @Transactional
    public Account transfer(long fromNumber, long toNumber, BigDecimal amount) {
        Account source = getAccount(fromNumber);
        Account target = getAccount(toNumber);
        source.transferTo(target, amount);
        return target;
    }

    @Transactional
    public void deposit(long accountNumber, BigDecimal amount) {
        getAccount(accountNumber).deposit(amount);
    }

    public Optional<Account> findAccount(long accountNumber) {
        return accounts.findById(accountNumber);
    }

    public Account getAccount(long accountNumber) {
        return findAccount(accountNumber).orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public Account getAccountWithTransactions(long accountNumber) {
        return accounts.findWithTransactionsByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public boolean checkPin(long accountNumber, String pin) {
        return findAccount(accountNumber)
                .map(account -> account.checkPin(pin, passwordEncoder))
                .orElse(false);
    }

    public List<Account> getAllAccounts() {
        return accounts.findAll(Sort.by("accountNumber"));
    }

    public Map<Account, BigDecimal> applyYearlyInterest() {
        Map<Account, BigDecimal> payments = new LinkedHashMap<>();

        for (Account account : accounts.findAll(Sort.by("accountNumber"))) {
            if (account instanceof InterestBearing saver) {
                BigDecimal interest = saver.calculateYearlyInterest();
                if (interest.compareTo(BigDecimal.ZERO) > 0) {
                    account.addInterest(interest);
                    payments.put(account, interest);
                }
            }
        }
        return payments;
    }

    public List<Account> findByHolder(String query) {
        return accounts.findByHolderContainingIgnoreCaseOrderByAccountNumber(query);

    }

    public long getNumberOfAccounts() {
        return accounts.count();
    }

    public BigDecimal getTotalDeposits() {
        return accounts.totalBalance();
    }

    public void startNewDay() {
        accounts.findAll().forEach(Account::resetDailyLimit);
    }

    public Set<String> getHolderNames() {
        return new TreeSet<>(accounts.findDistinctHolders());
    }

    public Optional<Account> findRichestAccount() {
        return accounts.findFirstByOrderByBalanceDesc();
    }

}
