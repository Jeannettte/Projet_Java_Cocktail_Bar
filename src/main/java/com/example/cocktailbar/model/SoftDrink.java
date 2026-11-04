package com.example.cocktailbar.model;

import java.math.BigDecimal;

/** A non-alcoholic drink such as a soda or a juice. */
public class SoftDrink extends Drink {

    private static final int PREPARATION_MINUTES = 1;

    private final boolean sugarFree;

    public SoftDrink(String name, BigDecimal basePrice, boolean sugarFree) {
        // The alcohol level is fixed to 0 here, so a soft drink can never be flagged as alcoholic by mistake.
        super(name, basePrice, 0.0);
        this.sugarFree = sugarFree;
    }

    public boolean isSugarFree() {
        return sugarFree;
    }

    @Override
    public DrinkCategory getCategory() {
        return DrinkCategory.SOFT_DRINK;
    }

    @Override
    public int getPreparationTimeMinutes() {
        return PREPARATION_MINUTES;
    }

    @Override
    public String describe() {
        return super.describe() + (sugarFree ? " - sugar-free" : "");
    }
}
