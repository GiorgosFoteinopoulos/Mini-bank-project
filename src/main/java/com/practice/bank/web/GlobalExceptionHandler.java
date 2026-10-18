package com.practice.bank.web;


import com.practice.bank.exception.NotLoggedInException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoggedInException.class)
    public String notLoggedIn() {
        return "redirect:/login";
    }
}
