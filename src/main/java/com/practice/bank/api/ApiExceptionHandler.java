package com.practice.bank.api;


import com.practice.bank.exception.AccountNotEmptyException;
import com.practice.bank.exception.AccountNotFoundException;
import com.practice.bank.exception.BankException;
import com.practice.bank.exception.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.practice.bank.api")
public class ApiExceptionHandler {

    private static final HttpStatusCode UNPROCESSABLE = HttpStatusCode.valueOf(422);

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail accountNotFound(AccountNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Account not found", e.getMessage());
    }

    @ExceptionHandler(AccountNotEmptyException.class)
    public ProblemDetail accountNotEmpty(AccountNotEmptyException e) {
        return problem(HttpStatus.CONFLICT, "Account not empty", e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail concurrentUpdate() {
        return problem(HttpStatus.CONFLICT, "Concurrent update", "The account was changed at the same time by another operation. Please try again.");
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail insufficientFunds(InsufficientFundsException e) {
        ProblemDetail problem = problem(UNPROCESSABLE, "Insufficient funds", e.getMessage());
        problem.setProperty("shortfall", e.getShortfall().toPlainString());
        return problem;
    }

    @ExceptionHandler(BankException.class)
    public ProblemDetail businessRule(BankException e) {
        return problem(UNPROCESSABLE, "Operation not allowed", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidFields(MethodArgumentNotValidException e) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid request",
                "One or more fields are invalid.");
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadableBody() {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request body",
                "The request body is not valid JSON or contains a value of the wrong type.");
    }

    private static ProblemDetail problem(HttpStatusCode status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
