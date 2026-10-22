package com.practice.bank.web.validation;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PinValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Pin {

    String message() default "PIN must be exactly 4 digits.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
