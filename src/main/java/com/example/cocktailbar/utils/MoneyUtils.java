package com.example.cocktailbar.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Helpers to keep every amount of money rounded and displayed the same way.
 * Rounds to the cent (0.5 cent and up rounds up) and displays an amount in the form 8.50 EUR.
 * Amounts use BigDecimal instead of double, which gives rounding errors on prices.*/
public final class MoneyUtils {

    private static final int SCALE = 2;

    private MoneyUtils() {
        // Utility class: no instances.
    }

    /** Rounds to cents. BigDecimal is used instead of double to avoid binary rounding errors on prices. */
    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal amount) {
        // toPlainString does not depend on the machine's locale so output is the same everywhere.
        return round(amount).toPlainString() + " EUR";
    }
}
