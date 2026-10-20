package com.practice.bank.web;



import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (AccountSession.hasRole(authentication, "ROLE_ADMIN")) {
            return "redirect:/admin";
        }
        return "redirect:/dashboard";
    }
}
