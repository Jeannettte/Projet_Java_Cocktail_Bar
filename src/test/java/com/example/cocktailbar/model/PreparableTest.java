package com.example.cocktailbar.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
// Interface methods: time limit of 1 or 2 minutes; partial, empty or missing ingredient names

class PreparableTest {

    // The limit is "1 minute or less": the interesting values are right on it and right above it.
    @ParameterizedTest
    @CsvSource({"0,true", "1,true", "2,false", "10,false"})
    void preparationIsQuickFromOneMinuteDown(int minutes, boolean expectedQuick) {
        // The interface has a single abstract method, so a lambda is enough to test the default method alone.
        Preparable item = () -> minutes;

        assertEquals(expectedQuick, item.isQuickToPrepare());
    }

    @Test
    void beersAndSoftDrinksAreQuickButCocktailsAreNot() {
        assertTrue(lager().isQuickToPrepare());
        assertTrue(cola().isQuickToPrepare());
        assertFalse(mojito().isQuickToPrepare());
    }

    @Test
    void everyDrinkIsPreparable() {
        assertTrue(mojito() instanceof Preparable);
        assertTrue(lager() instanceof Preparable);
        assertTrue(cola() instanceof Preparable);
    }
}
