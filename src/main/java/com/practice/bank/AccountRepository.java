package com.practice.bank;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @EntityGraph(attributePaths = "transactions")
    Optional<Account> findWithTransactionsByAccountNumber(Long accountNumber);

    List<Account> findByHolderContainingIgnoreCaseOrderByAccountNumber(String holder);

    Optional<Account> findFirstByOrderByBalanceDesc();

    @Query("select coalesce(sum(a.balance), 0) from Account a")
    BigDecimal totalBalance();

    @Query("select distinct a.holder from Account a order by a.holder")
    List<String> findDistinctHolders();
}
