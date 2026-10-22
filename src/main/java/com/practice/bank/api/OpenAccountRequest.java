package com.practice.bank.api;

import com.practice.bank.web.validation.Pin;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record OpenAccountRequest(
        @NotNull @Pattern(regexp = "CHECKING|SAVINGS", message = "must be CHECKING or SAVINGS") String type,
        @NotBlank @Size(max = 50) String holder,
        @Pin String pin,
        @NotNull @PositiveOrZero @Digits(integer = 13, fraction = 2) BigDecimal openingDeposit) {

}
