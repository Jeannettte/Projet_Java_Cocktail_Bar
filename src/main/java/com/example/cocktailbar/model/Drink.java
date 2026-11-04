package com.example.cocktailbar.model;

import com.example.cocktailbar.utils.MoneyUtils;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * This is an abstract class: you never create "a drink" on its own—only a cocktail, a beer, or a soft drink.
 **/

public abstract class Drink implements Preparable, Comparable<Drink> {

    private final String name;
    private final BigDecimal basePrice;
    private final double alcoholPercent;

    // constructor
    protected Drink(String name, BigDecimal basePrice, double alcoholPercent) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Drink name must not be blank");
        }
        // Checked after rounding: 0.001 would otherwise pass and silently become a free drink.
        if (basePrice == null || MoneyUtils.round(basePrice).signum() <= 0) {
            throw new IllegalArgumentException("Base price must be strictly positive for '" + name + "'");
        }
        if (alcoholPercent < 0 || alcoholPercent > 100) {
            throw new IllegalArgumentException("Alcohol percentage must be between 0 and 100 for '" + name + "'");
        }
        this.name = name.trim();
        this.basePrice = MoneyUtils.round(basePrice);
        this.alcoholPercent = alcoholPercent;
    }

    // getter
    public String getName() {
        return name;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public double getAlcoholPercent() {
        return alcoholPercent;
    }

    // A drink is alcoholic as soon as it contains any alcohol: this is what the age check relies on.
    public boolean isAlcoholic() {
        return alcoholPercent > 0;
    }

    public abstract DrinkCategory getCategory();

    // getPreparationTimeMinutes() comes from Preparable and is left unimplemented here.
    // each concrete drink gives its own value.


    @Override
    public int compareTo(Drink other) {
        return name.compareToIgnoreCase(other.name);
    }

    /** One-line description for menus; subclasses extend it with their own details. */
    public String describe() {
        String alcohol = isAlcoholic() ? alcoholPercent + "% vol" : "alcohol-free";
        return String.format("%s [%s, %s] %s", name, getCategory().getLabel(), alcohol, MoneyUtils.format(basePrice));
    }

    // Two drinks are the same product when their names match, ignoring case ("mojito" == "Mojito").
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Drink otherDrink)) {
            return false;
        }
        return name.equalsIgnoreCase(otherDrink.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return describe();
    }
}
