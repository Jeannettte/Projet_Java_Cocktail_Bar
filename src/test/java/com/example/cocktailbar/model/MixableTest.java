package com.example.cocktailbar.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixableTest {

    private final Mixable mixable = () -> List.of("rum", "mint", "soda water");

    @Test
    void containsAnIngredientOfTheList() {
        assertTrue(mixable.containsIngredient("rum"));
    }

    @Test
    void searchIgnoresCaseAndSurroundingSpaces() {
        assertTrue(mixable.containsIngredient("  RUM "));
        assertTrue(mixable.containsIngredient("Soda Water"));
    }

    @Test
    void ingredientMustMatchEntirelyNotPartially() {
        assertFalse(mixable.containsIngredient("min"));
        assertFalse(mixable.containsIngredient("mint leaves"));
    }

    @Test
    void missingIngredientIsNotFound() {
        assertFalse(mixable.containsIngredient("gin"));
    }

    @Test
    void nullOrBlankIngredientIsNeverFound() {
        assertFalse(mixable.containsIngredient(null));
        assertFalse(mixable.containsIngredient(""));
        assertFalse(mixable.containsIngredient("   "));
    }

    @Test
    void ingredientSearchOnAnEmptyListIsAlwaysFalse() {
        Mixable nothing = List::of;

        assertFalse(nothing.containsIngredient("rum"));
    }

    @Test
    void onlyCocktailsAreMixable() {
        assertTrue(mojito() instanceof Mixable);
        assertFalse(lager() instanceof Mixable);
        assertFalse(cola() instanceof Mixable);
    }

    @Test
    void cocktailUsesTheDefaultSearchOnItsOwnIngredients() {
        Mixable cocktail = (Mixable) mojito();

        assertTrue(cocktail.containsIngredient("mint"));
        assertFalse(cocktail.containsIngredient("tequila"));
    }
}
