package com.practice.bank.exception;

public class NotLoggedInException extends RuntimeException {
    public NotLoggedInException() {
        super("Not logged in");
    }
}
