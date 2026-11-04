package com.example.cocktailbar.model;

/** The families of drinks sold by the bar. Used for pricing rules and sales reports. */
public enum DrinkCategory {
    COCKTAIL("Cocktail"),
    BEER("Beer"),
    SOFT_DRINK("Soft drink");

    private final String label;

    DrinkCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
