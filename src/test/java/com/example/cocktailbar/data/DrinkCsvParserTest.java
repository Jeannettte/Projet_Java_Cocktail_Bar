package com.example.cocktailbar.data;

import com.example.cocktailbar.model.Beer;
import com.example.cocktailbar.model.Cocktail;
import com.example.cocktailbar.model.SoftDrink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrinkCsvParserTest {

    private final DrinkCsvParser parser = new DrinkCsvParser();

    // ---- valid lines: each type is turned into the right subclass ----

    @Test
    void cocktailLineBecomesACocktailWithItsIngredients() {
        MenuEntry entry = parser.parse("COCKTAIL,Mojito,8.50,20,10.0,rum|mint|lime");

        assertTrue(entry.drink() instanceof Cocktail);
        assertEquals(List.of("rum", "mint", "lime"), ((Cocktail) entry.drink()).getIngredients());
        assertEquals(new BigDecimal("8.50"), entry.drink().getBasePrice());
        assertTrue(entry.drink().isAlcoholic());
        assertEquals(20, entry.initialStock());
    }

    @Test
    void beerLineBecomesABeerWithItsVolume() {
        MenuEntry entry = parser.parse("BEER,Lager,4.50,40,5.0,33");

        assertTrue(entry.drink() instanceof Beer);
        assertEquals(33, ((Beer) entry.drink()).getVolumeCl());
        assertEquals(40, entry.initialStock());
    }

    @Test
    void softDrinkLineBecomesASoftDrinkWithItsSugarFlag() {
        MenuEntry sugarFree = parser.parse("SOFT_DRINK,Diet Cola,3.00,3,0.0,sugarfree");
        MenuEntry regular = parser.parse("SOFT_DRINK,Cola,3.00,50,0.0,regular");

        assertTrue(((SoftDrink) sugarFree.drink()).isSugarFree());
        assertTrue(!((SoftDrink) regular.drink()).isSugarFree());
    }

    @Test
    void extraSpacesAndLowerCaseTypeAreTolerated() {
        MenuEntry entry = parser.parse("  cocktail , Mojito , 8.50 , 20 , 10.0 , rum | mint ");

        assertEquals("Mojito", entry.drink().getName());
        assertEquals(List.of("rum", "mint"), ((Cocktail) entry.drink()).getIngredients());
    }

    @Test
    void zeroStockIsAValidStartingPoint() {
        assertEquals(0, parser.parse("BEER,Lager,4.50,0,5.0,33").initialStock());
    }

    // ---- invalid lines ----

    @ParameterizedTest
    @ValueSource(strings = {
            "COCKTAIL,Mojito,8.50,20,10.0",                  // too few columns
            "COCKTAIL,Mojito,8.50,20,10.0,rum,extra",        // too many columns
            "WINE,Chardonnay,7.00,10,12.0,75",               // unknown type
            "COCKTAIL,Mojito,abc,20,10.0,rum",               // price is not a number
            "COCKTAIL,Mojito,-8.50,20,10.0,rum",             // negative price
            "COCKTAIL,Mojito,8.50,many,10.0,rum",            // stock is not an integer
            "COCKTAIL,Mojito,8.50,-1,10.0,rum",              // negative stock
            "COCKTAIL,Mojito,8.50,20,150.0,rum",             // alcohol above 100%
            "COCKTAIL,Mojito,8.50,20,10.0,",                 // no ingredient
            "COCKTAIL,,8.50,20,10.0,rum",                    // blank name
            "BEER,Lager,4.50,40,5.0,big",                    // volume is not a number
            "BEER,Lager,4.50,40,5.0,0",                      // volume is zero
            "SOFT_DRINK,Cola,3.00,50,4.0,regular",           // a soft drink with alcohol
            "SOFT_DRINK,Cola,3.00,50,0.0,fizzy",             // unknown soft drink details
            ""                                               // empty line: 1 column only
    })
    void invalidLineIsRejected(String line) {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(line));
    }

    @Test
    void errorMessageNamesTheUnknownType() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> parser.parse("WINE,Chardonnay,7.00,10,12.0,75"));

        assertTrue(error.getMessage().contains("WINE"));
    }

    @Test
    void invalidNumberKeepsTheOriginalExceptionAsCause() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> parser.parse("BEER,Lager,abc,40,5.0,33"));

        assertTrue(error.getCause() instanceof NumberFormatException);
    }
}
