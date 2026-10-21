package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import com.practice.bank.exception.BankException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final Bank bank;
    private final MoneyFormat money;

    public AdminController(Bank bank, MoneyFormat money) {
        this.bank = bank;
        this.money = money;
    }

    @GetMapping
    public String overview(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("openForm", new OpenAccountForm());
        addOverview(model, q);
        return "admin";
    }

    @PostMapping("/accounts")
    public String openAccount(@Valid @ModelAttribute("openForm") OpenAccountForm form,
                              BindingResult result,
                              Model model,
                              RedirectAttributes flash) {
        if (result.hasErrors()) {
            addOverview(model, null);
            return "admin";
        }

        BigDecimal deposit = form.getOpeningDeposit().setScale(2, RoundingMode.HALF_EVEN);
        String holder = form.getHolder().strip();

        Account account = switch (form.getType()) {
            case "CHECKING" -> bank.openCheckingAccount(holder, form.getPin(), deposit);
            case "SAVINGS" -> bank.openSavingsAccount(holder, form.getPin(), deposit);
            default -> throw new IllegalArgumentException("Unknown account type: " + form.getType());
        };
        flash.addFlashAttribute("success", "Opened account #" + account.getAccountNumber()
        + " (" + account.getAccountType() + ") for " + account.getHolder() + ".");
        return "redirect:/admin";
    }

    @PostMapping("/accounts/{number}/close")
    public String closeAccount(@PathVariable long number, RedirectAttributes flash) {
        bank.closeAccount(number);
        flash.addFlashAttribute("success", "Account #" + number + " has been closed.");
        return "redirect:/admin";
    }

    @PostMapping("/new-day")
    public String newDay(RedirectAttributes flash) {
        bank.startNewDay();
        flash.addFlashAttribute("success", "A new day has started. All daily limits are reset.");
        return "redirect:/admin";
    }

    @PostMapping("/interest")
    public String payInterest(RedirectAttributes flash) {
        Map<Account, BigDecimal> payments = bank.applyYearlyInterest();
        BigDecimal total = payments.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        flash.addFlashAttribute("success", "Paid " + money.format(total) + " EUR interest to "
        + payments.size() + " savings account(s).");
        return "redirect:/admin";
    }

    private void addOverview(Model model, String q) {
        Collection<Account> accounts = (q == null || q.isBlank())
                ? bank.getAllAccounts()
                : bank.findByHolder(q);

        model.addAttribute("accounts", accounts);
        model.addAttribute("q", q);
        model.addAttribute("accountCount", bank.getNumberOfAccounts());
        model.addAttribute("total", bank.getTotalDeposits());
        model.addAttribute("customers", bank.getHolderNames());
        model.addAttribute("richest", bank.findRichestAccount().orElse(null));
    }

    @ExceptionHandler({BankException.class, IllegalArgumentException.class})
    public String handleError(RuntimeException e, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", e.getMessage());
                return "redirect:/admin";
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String handleBadInput(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", "Invalid request.");
        return "redirect:/admin";
    }
}
