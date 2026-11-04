package com.example.cocktailbar.pricing;

import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.model.DrinkCategory;
import com.example.cocktailbar.utils.MoneyUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 30% off cocktails between 17:00 (included) and 19:00 (excluded).
 *
 * <p>It wraps another strategy (decorator pattern) instead of extending StandardPricing, so it can be
 * stacked with other rules, for example with {@link LateNightPricing}.
 */
public class HappyHourPricing implements PricingStrategy {

    private static final LocalTime START = LocalTime.of(17, 0);
    private static final LocalTime END = LocalTime.of(19, 0);
    private static final BigDecimal DISCOUNT_FACTOR = new BigDecimal("0.70");

    private final PricingStrategy delegate;

    public HappyHourPricing(PricingStrategy delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public BigDecimal priceFor(Drink drink, LocalDateTime orderTime) {
        BigDecimal price = delegate.priceFor(drink, orderTime);
        if (drink.getCategory() == DrinkCategory.COCKTAIL && isHappyHour(orderTime.toLocalTime())) {
            return MoneyUtils.round(price.multiply(DISCOUNT_FACTOR));
        }
        return price;
    }

    private boolean isHappyHour(LocalTime time) {
        return !time.isBefore(START) && time.isBefore(END);
    }
}
