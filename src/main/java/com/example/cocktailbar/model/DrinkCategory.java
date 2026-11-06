package com.example.cocktailbar.model;

/** The families of drinks sold by the bar. Used for pricing rules and sales reports.
 * the fixed list of drink categories: COCKTAIL, BEER, SOFT_DRINK, each with its own label
 *Reserve ‘happy hour’ for cocktails, group turnover
 * by category in the report, and choose which category to create
 * based on the ‘type’ column in the CSV file */
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
