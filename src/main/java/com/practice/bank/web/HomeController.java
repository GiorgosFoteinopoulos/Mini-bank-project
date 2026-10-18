package com.practice.bank.web;



import com.practice.bank.Bank;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final Bank bank;
    private final AccountSession accountSession;

    public HomeController(Bank bank, AccountSession accountSession) {
        this.bank = bank;
        this.accountSession = accountSession;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        return accountSession.currentAccount(session).isPresent()
                ? "redirect:/dashboard"
                : "redirect:/login";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", bank.getAllAccounts());
        model.addAttribute("total", bank.getTotalDeposits());
        model.addAttribute("customers", bank.getHolderNames());
        return "accounts";
    }
}
