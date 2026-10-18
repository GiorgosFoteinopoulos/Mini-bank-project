package com.practice.bank.web;



import com.practice.bank.Account;
import com.practice.bank.Bank;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final Bank bank;
    private final AccountSession accountSession;

    public AuthController(Bank bank, AccountSession accountSession) {
        this.bank = bank;
        this.accountSession = accountSession;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @PostMapping("/login")
    public String login(@RequestParam String accountNumber,
                        @RequestParam String pin,
                        HttpServletRequest request,
                        Model model) {
        Optional<Account> account= parseAccountNumber(accountNumber)
                .flatMap(bank::findAccount)
                .filter(a -> a.checkPin(pin));

        if (account.isPresent()) {
            accountSession.logIn(request,account.get());
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Wrong account number or PIN");
        model.addAttribute("accountNumber", accountNumber);
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        accountSession.logOut(session);
        return "redirect:/login?logout";
    }

    private static Optional<Long> parseAccountNumber(String text) {
        try {
            return Optional.of(Long.parseLong(text.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
