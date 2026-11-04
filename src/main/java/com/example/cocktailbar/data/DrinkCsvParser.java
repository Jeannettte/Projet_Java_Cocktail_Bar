package com.example.cocktailbar.data;

import com.example.cocktailbar.model.Beer;
import com.example.cocktailbar.model.Cocktail;
import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.model.DrinkCategory;
import com.example.cocktailbar.model.SoftDrink;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Turns ONE csv line into a {@link MenuEntry}.
 *
 * <p>Kept separate from the class that reads the file (Single Responsibility): parsing rules can be
 * tested with plain strings, without creating any file.
 *
 * <p>Expected columns: {@code type,name,price,stock,alcohol_percent,details}.
 * All problems are reported as {@link IllegalArgumentException}; the caller adds the line number.
 */
public class DrinkCsvParser {

    private static final String COLUMN_SEPARATOR = ",";
    private static final String INGREDIENT_SEPARATOR = "\\|";
    private static final int EXPECTED_COLUMNS = 6;

    public MenuEntry parse(String line) {
        // The -1 limit keeps trailing empty columns, so "a,b," is seen as 3 columns and not 2.
        String[] columns = line.split(COLUMN_SEPARATOR, -1);
        if (columns.length != EXPECTED_COLUMNS) {
            throw new IllegalArgumentException(
                    "Expected " + EXPECTED_COLUMNS + " columns but found " + columns.length);
        }

        try {
            DrinkCategory category = parseCategory(columns[0].trim());
            String name = columns[1].trim();
            BigDecimal price = new BigDecimal(columns[2].trim());
            int stock = Integer.parseInt(columns[3].trim());
            double alcoholPercent = Double.parseDouble(columns[4].trim());
            String details = columns[5].trim();

            Drink drink = createDrink(category, name, price, alcoholPercent, details);
            return new MenuEntry(drink, stock);
        } catch (NumberFormatException e) {
            // NumberFormatException messages are cryptic ("For input string: ..."), so we add context.
            throw new IllegalArgumentException("Invalid number in line: " + e.getMessage(), e);
        }
    }

    private DrinkCategory parseCategory(String rawType) {
        try {
            return DrinkCategory.valueOf(rawType.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown drink type: '" + rawType + "'", e);
        }
    }

    // Adding a new drink kind means adding one case here: this switch is the only place that maps csv text to classes.
    private Drink createDrink(DrinkCategory category, String name, BigDecimal price,
                              double alcoholPercent, String details) {
        return switch (category) {
            case COCKTAIL -> new Cocktail(name, price, alcoholPercent, parseIngredients(details));
            case BEER -> new Beer(name, price, alcoholPercent, Integer.parseInt(details));
            case SOFT_DRINK -> createSoftDrink(name, price, alcoholPercent, details);
        };
    }

    private Drink createSoftDrink(String name, BigDecimal price, double alcoholPercent, String details) {
        if (alcoholPercent != 0) {
            throw new IllegalArgumentException("A soft drink cannot contain alcohol: '" + name + "'");
        }
        return switch (details.toLowerCase(Locale.ROOT)) {
            case "sugarfree" -> new SoftDrink(name, price, true);
            case "regular" -> new SoftDrink(name, price, false);
            default -> throw new IllegalArgumentException(
                    "Soft drink details must be 'sugarfree' or 'regular' but was '" + details + "'");
        };
    }

    private List<String> parseIngredients(String details) {
        return Arrays.stream(details.split(INGREDIENT_SEPARATOR))
                .map(String::trim)
                .filter(ingredient -> !ingredient.isEmpty())
                .toList();
    }
}
