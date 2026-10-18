package com.practice.bank;

import com.practice.bank.exception.BankException;
import com.practice.bank.exception.DailyLimitExceededException;
import com.practice.bank.exception.InsufficientFundsException;
import com.practice.bank.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public abstract class Account {

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("500.00");
    private static long nextAccountNumber = 1001;
    private final List<Transaction> transactions = new ArrayList<>();

    private final long accountNumber;
    private final String holder;
    private final String pin;
    private BigDecimal balance;
    private BigDecimal withdrawnToday;


    protected Account(String holder, String pin, BigDecimal openingBalance) {
        if (holder == null || holder.isBlank()) {
            throw new IllegalArgumentException("Holder name is required");
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("PIN must be exactly 4 digits.");
        }
        if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative");
        }

        this.accountNumber = nextAccountNumber++;
        this.holder = holder;
        this.pin = pin;
        this.balance = openingBalance;
        transactions.add(new Transaction(LocalDateTime.now(), TransactionType.OPENING, openingBalance, openingBalance, "Account opened"));
        this.withdrawnToday = new BigDecimal("0.00");
    }

    public final void deposit(BigDecimal amount) {
        requirePositive(amount);
        credit(amount, TransactionType.DEPOSIT, "Cash withdrawal");
    }

    public boolean checkPin(String attempt) {
        return pin.equals(attempt);
    }

    public final void withdraw(BigDecimal amount) {
        checkWithdrawal(amount);
        debit(amount, TransactionType.WITHDRAWAL, "Cash withdrawal");
    }

    public final void transferTo(Account target, BigDecimal amount) {
        if (target == null || target == this) {
            throw new IllegalArgumentException("Target account is required.");
        }
        if (target == this) {
            throw new BankException("You can't transfer to your own account.");
        }
        checkWithdrawal(amount);
        debit(amount, TransactionType.TRANSFER_OUT, "To #" + target.accountNumber + " " + target.holder);
        target.credit(amount, TransactionType.TRANSFER_IN, "From #" + accountNumber + " " + holder);
    }

    public final void addInterest(BigDecimal amount) {
        requirePositive(amount);
        credit(amount, TransactionType.INTEREST, "Yearly interest");
    }

    private void checkWithdrawal(BigDecimal amount) {
        requirePositive(amount);

        BigDecimal available = getAvailableBalance();
        if (amount.compareTo(available) > 0) {
            throw new InsufficientFundsException(available, amount);
        }

        BigDecimal remaining = getRemainingDailyLimit();
        if (amount.compareTo(remaining) > 0) {
            throw new DailyLimitExceededException(remaining, amount);
        }
    }

    private void credit(BigDecimal amount, TransactionType type, String description) {
        balance = balance.add(amount);
        transactions.add(new Transaction(LocalDateTime.now(), type, amount, balance, description));
    }

    private void debit(BigDecimal amount, TransactionType type, String description) {
        balance = balance.subtract(amount);
        withdrawnToday = withdrawnToday.add(amount);
        transactions.add(new Transaction(LocalDateTime.now(), type, amount, balance, description));
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(amount);
        }
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

    public BigDecimal getTotalIn() {
        return transactions.stream()
                .filter(t -> t.type().isCredit())
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    public BigDecimal getTotalOut() {
        return transactions.stream()
                .filter(t -> !t.type().isCredit())
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getAccountNumber() {
        return accountNumber;
    }
    public abstract String getAccountType();
    public String getHolder() {
        return holder;
    }
    public BigDecimal getBalance() {
        return balance;
    };
    public BigDecimal getAvailableBalance() {
        return balance;
    }
    public abstract BigDecimal getDailyLimit();
    public BigDecimal getRemainingDailyLimit() {
        return getDailyLimit().subtract(withdrawnToday);
    }

    public void resetDailyLimit() {
        withdrawnToday = new BigDecimal("0.00");
    }

    @Override
    public String toString() {
        return "Account #" + accountNumber + " | " + getAccountType() + " | " + holder + " | balance " + balance + " EUR";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account other)) return false;
        return this.accountNumber == other.accountNumber;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(accountNumber);
    }
}
