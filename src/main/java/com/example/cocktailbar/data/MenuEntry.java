package com.example.cocktailbar.data;

import com.example.cocktailbar.model.Drink;

import java.util.Objects;

/**
 * One row of the data file: a drink and how many of it are in stock at start-up.
 *
 * A record is used because this is a pure data carrier with no behaviour of its own.
 * Stock is not part of {@link Drink}: what the bar sells and what is left on the shelf are different concerns.
 * The stock isn't part of the drink (what's being sold and what's left on the shelf are two different things),
 * but the file gives both at once
 */
public record MenuEntry(Drink drink, int initialStock) {

    public MenuEntry {
        Objects.requireNonNull(drink, "drink");
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock must not be negative for '" + drink.getName() + "'");
        }
    }
}
