package com.practice.bank.web;

import com.practice.bank.web.validation.Pin;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class OpenAccountForm {

    @NotNull(message= "Choose an account type.")
    @Pattern(regexp = "CHECKING|SAVINGS", message = "Choose an account tyoe.")
    private String type = "CHECKING";

    @NotBlank(message = "Holder name is required.")
    @Size(max = 50, message = "Holder name can be at most 50 characters.")
    private String holder;

    @Pin
    private String pin;

    @NotNull(message = "Opening deposit is required.")
    @PositiveOrZero(message = "Opening deposit cannot be negative.")
    @Digits(integer = 13, fraction = 2, message = "Use at most 2 decimal places.")
    private BigDecimal openingDeposit =  BigDecimal.ZERO;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getHolder() {
        return holder;
    }
    public void setHolder(String holder) {
        this.holder = holder;
    }
    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }
    public BigDecimal getOpeningDeposit() {
        return openingDeposit;
    }
    public void setOpeningDeposit(BigDecimal openingDeposit) {
        this.openingDeposit = openingDeposit;
    }
}
