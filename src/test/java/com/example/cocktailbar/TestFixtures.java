package com.example.cocktailbar;

import com.example.cocktailbar.model.Beer;
import com.example.cocktailbar.model.Cocktail;
import com.example.cocktailbar.model.Customer;
import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.model.SoftDrink;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ready-made objects shared by the tests, so each test file stays focused on what it checks. */
public final class TestFixtures {

    private TestFixtures() {
        // Utility class: no instances.
    }

    /** Alcoholic cocktail, 8.50 EUR, 3 ingredients -> 5 minutes of preparation. */
    public static Drink mojito() {
        return new Cocktail("Mojito", new BigDecimal("8.50"), 10.0, List.of("rum", "mint", "lime"));
    }

    public static Drink virginMojito() {
        return new Cocktail("Virgin Mojito", new BigDecimal("6.50"), 0.0, List.of("mint", "lime"));
    }

    /** Alcoholic beer, 4.50 EUR. */
    public static Drink lager() {
        return new Beer("Lager", new BigDecimal("4.50"), 5.0, 33);
    }

    public static Drink alcoholFreeBeer() {
        return new Beer("Zero Lager", new BigDecimal("4.00"), 0.0, 33);
    }

    /** Soft drink, 3.00 EUR. */
    public static Drink cola() {
        return new SoftDrink("Cola", new BigDecimal("3.00"), false);
    }

    public static Customer adult() {
        return new Customer("Alice", 25);
    }

    public static Customer minor() {
        return new Customer("Bob", 17);
    }

    /** A time on a fixed day, so tests never depend on the real clock. */
    public static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(2026, 11, 6, hour, minute);
    }

    /** Builds an ordered drink -> quantity map from alternating name/quantity arguments. */
    public static Map<String, Integer> orderOf(Object... nameAndQuantityPairs) {
        Map<String, Integer> items = new LinkedHashMap<>();
        for (int i = 0; i < nameAndQuantityPairs.length; i += 2) {
            items.put((String) nameAndQuantityPairs[i], (Integer) nameAndQuantityPairs[i + 1]);
        }
        return items;
    }
}
