package com.example.cocktailbar.pricing;

import com.example.cocktailbar.model.Drink;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Decides the unit price of a drink at a given moment.
 *
 * This is the extension point for price rules (Open/Closed principle): a new rule is a new
 * implementation and OrderService never has to change. Every implementation must return a
 * strictly positive price rounded to cents so any of them can replace another.
 *
 */
public interface PricingStrategy {

    BigDecimal priceFor(Drink drink, LocalDateTime orderTime);
}
