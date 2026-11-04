package com.example.cocktailbar.model;

import java.math.BigDecimal;
import java.util.List;

/** A mixed drink. Alcohol percentage may be 0 for "virgin" cocktails. */
public class Cocktail extends Drink implements Mixable {

    private static final int BASE_PREPARATION_MINUTES = 2;

    private final List<String> ingredients;

    public Cocktail(String name, BigDecimal basePrice, double alcoholPercent, List<String> ingredients) {
        super(name, basePrice, alcoholPercent);
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("A cocktail needs at least one ingredient: " + name);
        }
        // Defensive copy: later changes to the caller's list must not alter this cocktail.
        this.ingredients = List.copyOf(ingredients);
    }

    @Override
    public List<String> getIngredients() {
        return ingredients;
    }

    @Override
    public DrinkCategory getCategory() {
        return DrinkCategory.COCKTAIL;
    }

    /** Each ingredient adds one minute of measuring and mixing on top of the base set-up time. */
    @Override
    public int getPreparationTimeMinutes() {
        return BASE_PREPARATION_MINUTES + ingredients.size();
    }

    @Override
    public String describe() {
        return super.describe() + " - " + String.join(", ", ingredients);
    }
}
