package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import com.practice.bank.CheckingAccount;
import com.practice.bank.SavingsAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {

    private final AccountSession accountSession;
    private final Bank bank;

    public DashboardController(AccountSession accountSession, Bank bank) {
        this.accountSession = accountSession;
        this.bank = bank;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        long number = accountSession.requireAccountNumber(authentication);
        Account account = bank.getAccountWithTransactions(number);
        model.addAttribute("account", account);
        model.addAttribute("transactions", account.getTransactions().reversed());
        model.addAttribute("extraInfo", describe(account));
        return "dashboard";
    }

    private static String describe(Account account) {
        return switch (account) {
            case CheckingAccount checking -> "Overdraft up to " + checking.getOverdraftLimit() + " EUR";
            case SavingsAccount savings -> "Earns " + savings.getInterestRate().movePointRight(2) + "% interest per year";
            default -> "";
        };
    }
}
