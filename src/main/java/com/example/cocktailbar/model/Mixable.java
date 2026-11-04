package com.example.cocktailbar.model;

import java.util.List;

/**
 * Capability "is made by mixing ingredients".
 *
 * <p>Only some drinks have it (cocktails), which is why it is an interface and not a method of
 * Drink: a beer would otherwise be forced to answer a question that makes no sense for it.
 */
public interface Mixable {

    List<String> getIngredients();

    /**
     * Default method: the search rule is written once for every Mixable.
     * The whole ingredient must match (so "min" does not find "mint"); case and spaces are ignored.
     */
    default boolean containsIngredient(String ingredient) {
        if (ingredient == null || ingredient.isBlank()) {
            return false;
        }
        String wanted = ingredient.trim();
        return getIngredients().stream().anyMatch(candidate -> candidate.equalsIgnoreCase(wanted));
    }
}
