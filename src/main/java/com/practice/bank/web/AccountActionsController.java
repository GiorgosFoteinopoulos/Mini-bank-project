package com.practice.bank.web;


import com.practice.bank.Account;
import com.practice.bank.Bank;
import com.practice.bank.exception.BankException;
import com.practice.bank.web.MoneyFormat;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
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
    private final MoneyFormat moneyFormat;

    public AccountActionsController(Bank bank, AccountSession accountSession, MoneyFormat moneyFormat) {
        this.bank = bank;
        this.accountSession = accountSession;
        this.moneyFormat = moneyFormat;
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount,
                          Authentication authentication,
                          RedirectAttributes flash) {
        BigDecimal money = toMoney(amount);
        bank.deposit(accountSession.requireAccountNumber(authentication), money);
        flash.addFlashAttribute("success","Deposited " + moneyFormat.format(money) + " EUR.");
        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,
                           Authentication authentication,
                           RedirectAttributes flash) {

        BigDecimal money = toMoney(amount);
        bank.withdraw(accountSession.requireAccountNumber(authentication), money );
        flash.addFlashAttribute("success", "Withdrew " + moneyFormat.format(money) + " EUR.");
        return "redirect:/dashboard";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam long targetAccount,
                           @RequestParam BigDecimal amount,
                           Authentication authentication,
                           RedirectAttributes flash) {
        BigDecimal money = toMoney(amount);
        Account target = bank.transfer(accountSession.requireAccountNumber(authentication), targetAccount, money);
        flash.addFlashAttribute("success", "Transferred " + moneyFormat.format(money) + " EUR to"
        + target.getHolder() + " (#" + target.getAccountNumber() + ").");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleConcurrentUpdate(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request)
                .put("error", "The account was changed at the same time by another operation. Please try againt.");
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
