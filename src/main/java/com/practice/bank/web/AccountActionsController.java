package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import com.practice.bank.exception.BankException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequestMapping("/account")
public class AccountActionsController {

    private final Bank bank;
    private final AccountSession accountSession;
    private final MoneyFormat money;

    public AccountActionsController(Bank bank, AccountSession accountSession, MoneyFormat money) {
        this.bank = bank;
        this.accountSession = accountSession;
        this.money = money;
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount,
                          HttpSession session,
                          RedirectAttributes flash) {
        Account account = accountSession.requireAccount(session);
        BigDecimal amountValue = toMoney(amount);

        account.deposit(amountValue);
        flash.addFlashAttribute("success", "Deposited " + money.format(amountValue) + " EUR.");
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,
                           HttpSession session,
                           RedirectAttributes flash) {
        Account account = accountSession.requireAccount(session);
        BigDecimal amountValue = toMoney(amount);

        account.withdraw(amountValue);
        flash.addFlashAttribute("success", "Withdrew " + money.format(amountValue) + " EUR.");
        return "redirect:/dashboard";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam long targetAccount,
                           @RequestParam BigDecimal amount,
                           HttpSession session,
                           RedirectAttributes flash) {
        Account account = accountSession.requireAccount(session);
        Account target = bank.getAccount(targetAccount);
        BigDecimal amountValue = toMoney(amount);
        account.transferTo(target, amountValue);

        flash.addFlashAttribute("success",
                "Transferred " + money.format(amountValue) + " EUR to " + target.getHolder() + " (#" + target.getAccountNumber() + ").");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(BankException.class)
    public String handleBankException(BankException e, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", e.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public String handleBadInput(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", "Please enter a valid number.");
        return "redirect:/dashboard";
    }

    private static BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }
}
