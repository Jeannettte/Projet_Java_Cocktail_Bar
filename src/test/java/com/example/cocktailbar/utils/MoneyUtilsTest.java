package com.example.cocktailbar.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyUtilsTest {
// check the rouding is half up to two decimals
    @Test
    void roundingIsHalfUpToTwoDecimals() {
        assertEquals(new BigDecimal("2.35"), MoneyUtils.round(new BigDecimal("2.345")));
        assertEquals(new BigDecimal("2.34"), MoneyUtils.round(new BigDecimal("2.344")));
    }

    // check if two decimals is reading
    @Test
    void wholeAmountsAlwaysShowTwoDecimals() {
        assertEquals("3.00 EUR", MoneyUtils.format(new BigDecimal("3")));
    }

    //check that the rounding is correct
    @Test
    void formatRoundsBeforeDisplaying() {
        assertEquals("9.78 EUR", MoneyUtils.format(new BigDecimal("9.775")));
    }
}
