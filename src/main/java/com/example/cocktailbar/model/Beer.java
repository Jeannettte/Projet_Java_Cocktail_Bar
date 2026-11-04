package com.example.cocktailbar.model;

import java.math.BigDecimal;

/** A bottled or draught beer. Served straight from the tap or fridge, so it is quick to prepare. */
public class Beer extends Drink {

    private static final int PREPARATION_MINUTES = 1;

    private final int volumeCl;

    public Beer(String name, BigDecimal basePrice, double alcoholPercent, int volumeCl) {
        super(name, basePrice, alcoholPercent);
        if (volumeCl <= 0) {
            throw new IllegalArgumentException("Beer volume must be positive: " + name);
        }
        this.volumeCl = volumeCl;
    }

    public int getVolumeCl() {
        return volumeCl;
    }

    @Override
    public DrinkCategory getCategory() {
        return DrinkCategory.BEER;
    }

    @Override
    public int getPreparationTimeMinutes() {
        return PREPARATION_MINUTES;
    }

    @Override
    public String describe() {
        return super.describe() + " - " + volumeCl + " cl";
    }
}
