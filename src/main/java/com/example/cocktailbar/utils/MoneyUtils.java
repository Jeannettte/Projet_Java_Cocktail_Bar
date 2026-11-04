package com.example.cocktailbar.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Helpers to keep every amount of money rounded and displayed the same way. */
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
        // toPlainString does not depend on the machine's locale, so output is the same everywhere.
        return round(amount).toPlainString() + " EUR";
    }
}
