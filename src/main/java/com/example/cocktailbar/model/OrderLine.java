package com.example.cocktailbar.model;

import com.example.cocktailbar.utils.MoneyUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One line of an order: a drink, how many, and the unit price that was applied.
 * The unit price is stored on purpose instead of being recomputed from the drink:
 * the price depends on the time of the order (happy hour...), and a receipt must never
 * change after the fact.
 */
public class OrderLine {

    private final Drink drink;
    private final int quantity;
    private final BigDecimal unitPrice;
    // Constructor
    public OrderLine(Drink drink, int quantity, BigDecimal unitPrice) {
        this.drink = Objects.requireNonNull(drink, "drink");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be negative");
        }
        this.quantity = quantity;
        this.unitPrice = MoneyUtils.round(unitPrice);
    }

    // getter : Boisson, quantité, prix unitaire
    public Drink getDrink() {
        return drink;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return MoneyUtils.round(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
