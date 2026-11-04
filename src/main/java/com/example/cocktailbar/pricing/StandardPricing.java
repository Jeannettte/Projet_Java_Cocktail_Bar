package com.example.cocktailbar.pricing;

import com.example.cocktailbar.model.Drink;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** The menu price, whatever the time. It is also the base that other rules build upon. */
public class StandardPricing implements PricingStrategy {

    @Override
    public BigDecimal priceFor(Drink drink, LocalDateTime orderTime) {
        return drink.getBasePrice();
    }
}
