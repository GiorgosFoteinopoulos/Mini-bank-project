package com.practice.bank;

import com.practice.bank.exception.BankException;
import com.practice.bank.exception.DailyLimitExceededException;
import com.practice.bank.exception.InsufficientFundsException;
import com.practice.bank.exception.InvalidAmountException;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name= "account")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type")
public abstract class Account {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_number_gen")
    @SequenceGenerator(name = "account_number_gen", sequenceName = "account_number_seq",
    initialValue = 1001, allocationSize = 1)
    private Long accountNumber;

    @Version
    private Long version;

    @Column(nullable = false)
    private String holder;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "withdrawn_today", nullable = false, precision = 15, scale = 2)
    private BigDecimal withdrawnToday = new BigDecimal("0.00");

    @ElementCollection
    @CollectionTable(name = "account_transaction", joinColumns = @JoinColumn(name = "account_number"))
    @OrderColumn(name = "position")
    private List<Transaction> transactions = new ArrayList<>();

    protected Account() {

    }


    protected Account(String holder, String pinHash, BigDecimal openingBalance) {
        if (holder == null || holder.isBlank()) {
            throw new IllegalArgumentException("Holder name is required");
        }
        if (pinHash == null || pinHash.isBlank()) {
            throw new IllegalArgumentException("PIN is required.");
        }
        if (openingBalance == null || openingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative");
        }

        this.holder = holder;
        this.pinHash = pinHash;
        this.balance = openingBalance;
        transactions.add(new Transaction(LocalDateTime.now(), TransactionType.OPENING, openingBalance, openingBalance, "Account opened"));
    }

    public abstract String getAccountType();

    public abstract BigDecimal getDailyLimit();

    public BigDecimal getAvailableBalance() {
        return balance;
    }

    public final void deposit(BigDecimal amount) {
        requirePositive(amount);
        credit(amount, TransactionType.DEPOSIT, "Cash Deposit");
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

    public boolean checkPin(String attempt, PasswordEncoder encoder) {
        return attempt != null && encoder.matches(attempt, pinHash);
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

    public String getHolder() {
        return holder;
    }
    public BigDecimal getBalance() {
        return balance;
    };

    public BigDecimal getRemainingDailyLimit() {
        return getDailyLimit().subtract(withdrawnToday);
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
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
        return accountNumber != null && accountNumber.equals(other.accountNumber);
    }

    @Override
    public int hashCode() {
        return Account.class.hashCode();
    }
}
