package com.practice.bank.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component("money")
public class MoneyFormat {

    private static final Locale GREEK = Locale.forLanguageTag("el-GR");

    public String format(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        DecimalFormat format = new DecimalFormat("#, ##0.00", DecimalFormatSymbols.getInstance(GREEK));
        return format.format(amount);
    }
}
