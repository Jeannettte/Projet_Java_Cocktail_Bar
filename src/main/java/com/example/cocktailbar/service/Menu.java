package com.example.cocktailbar.service;

import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.model.DrinkCategory;
import com.example.cocktailbar.model.Mixable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/** The catalogue of drinks the bar sells. It knows nothing about stock or prices rules. */
public class Menu {

    // Case-insensitive keys: a customer typing "mojito" must get the same drink as "Mojito".
    // A TreeMap also keeps the menu sorted by name when it is displayed.
    private final Map<String, Drink> drinks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public void add(Drink drink) {
        Objects.requireNonNull(drink, "drink");
        if (drinks.putIfAbsent(drink.getName(), drink) != null) {
            throw new IllegalArgumentException("Drink already on the menu: " + drink.getName());
        }
    }

    /** Optional makes "not found" explicit: the caller has to decide what an unknown name means. */
    public Optional<Drink> find(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(drinks.get(name.trim()));
    }

    public List<Drink> findByCategory(DrinkCategory category) {
        return drinks.values().stream()
                .filter(drink -> drink.getCategory() == category)
                .toList();
    }

    /**
     * Drinks made with the given ingredient. Only Mixable drinks can match: asking a beer for its
     * ingredients makes no sense. The result is sorted by name because the TreeMap already is.
     */
    public List<Drink> findByIngredient(String ingredient) {
        return drinks.values().stream()
                .filter(drink -> drink instanceof Mixable mixable && mixable.containsIngredient(ingredient))
                .toList();
    }

    public List<Drink> getAll() {
        return List.copyOf(drinks.values());
    }

    public int size() {
        return drinks.size();
    }
}
