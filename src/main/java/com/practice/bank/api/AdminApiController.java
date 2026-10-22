package com.practice.bank.api;

import com.practice.bank.Account;
import com.practice.bank.Bank;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AdminApiController {

    private final Bank bank;

    public AdminApiController(Bank bank) {
        this.bank = bank;
    }

    @GetMapping
    public List<AccountResponse> list(@RequestParam(required = false) String q) {
        Collection<Account> accounts = (q == null || q.isBlank())
                ? bank.getAllAccounts()
                : bank.findByHolder(q);
        return accounts.stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/{number}")
    public AccountResponse get(@PathVariable long number) {
        return AccountResponse.from(bank.getAccount(number));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        BigDecimal deposit = request.openingDeposit().setScale(2, RoundingMode.HALF_EVEN);
        String holder = request.holder().strip();

        Account account = switch (request.type()) {
            case "CHECKING" -> bank.openCheckingAccount(holder, request.pin(),  deposit);
            case "SAVINGS" -> bank.openSavingsAccount(holder, request.pin(), deposit);
            default -> throw new IllegalArgumentException("Unknown account type: " + request.type());
        };
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{number}")
                .buildAndExpand(account.getAccountNumber())
                .toUri();

        return ResponseEntity.created(location).body(AccountResponse.from(account));
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<Void> close(@PathVariable long number) {
        bank.closeAccount(number);
        return ResponseEntity.noContent().build();
    }
}
