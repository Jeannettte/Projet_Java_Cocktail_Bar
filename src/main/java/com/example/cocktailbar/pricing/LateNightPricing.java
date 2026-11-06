package com.example.cocktailbar.pricing;

import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.utils.MoneyUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/** the night-time surcharge.
 * 15% surcharge on every drink from 23:00 (included) to 04:00 (excluded).
 * This class also encompasses another rule
 * Application constructs new HappyHourPricing(new
 * LateNightPricing(new StandardPricing())) : the happy hour rule retrieves its price from the night-time rule, which in turn retrieves it from the base rule. The two time periods never overlap, but the chaining would work if they did
 * never, but the chaining would work if they did.*/

public class LateNightPricing implements PricingStrategy {

    private static final LocalTime START = LocalTime.of(23, 0);
    private static final LocalTime END = LocalTime.of(4, 0);
    private static final BigDecimal SURCHARGE_FACTOR = new BigDecimal("1.15");

    private final PricingStrategy delegate;

    // Constructor
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

    // The period crosses midnight (23:00 -> 04:00) so it cannot be written as "start <= t < end":
    // a time is late-night if it is after the start OR before the end.
    private boolean isLateNight(LocalTime time) {
        return !time.isBefore(START) || time.isBefore(END);
    }
}
