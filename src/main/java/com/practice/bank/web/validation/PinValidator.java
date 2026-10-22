package com.practice.bank.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PinValidator implements ConstraintValidator<Pin, String> {

    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && FOUR_DIGITS.matcher(value).matches();
    }
}
