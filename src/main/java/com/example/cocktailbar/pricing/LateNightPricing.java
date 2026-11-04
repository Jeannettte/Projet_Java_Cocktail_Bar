package com.example.cocktailbar.pricing;

import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.utils.MoneyUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/** 15% surcharge on every drink from 23:00 (included) to 04:00 (excluded). */
public class LateNightPricing implements PricingStrategy {

    private static final LocalTime START = LocalTime.of(23, 0);
    private static final LocalTime END = LocalTime.of(4, 0);
    private static final BigDecimal SURCHARGE_FACTOR = new BigDecimal("1.15");

    private final PricingStrategy delegate;

    public LateNightPricing(PricingStrategy delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public BigDecimal priceFor(Drink drink, LocalDateTime orderTime) {
        BigDecimal price = delegate.priceFor(drink, orderTime);
        if (isLateNight(orderTime.toLocalTime())) {
            return MoneyUtils.round(price.multiply(SURCHARGE_FACTOR));
        }
        return price;
    }

    // The period crosses midnight (23:00 -> 04:00), so it cannot be written as "start <= t < end":
    // a time is late-night if it is after the start OR before the end.
    private boolean isLateNight(LocalTime time) {
        return !time.isBefore(START) || time.isBefore(END);
    }
}
