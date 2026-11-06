package com.example.cocktailbar.service;

import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.model.DrinkCategory;
import com.example.cocktailbar.model.SoftDrink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static com.example.cocktailbar.TestFixtures.virginMojito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/*
Case-insensitive search, no duplicates allowed, search by category and ingredient
 */

class MenuTest {

    private Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        menu.add(mojito());
        menu.add(lager());
        menu.add(cola());
    }

    @Test
    void findIgnoresCaseAndSurroundingSpaces() {
        assertTrue(menu.find("mojito").isPresent());
        assertTrue(menu.find("  MOJITO ").isPresent());
        assertEquals("Mojito", menu.find("mOjItO").get().getName());
    }

    @Test
    void findReturnsEmptyForUnknownDrink() {
        assertTrue(menu.find("Whisky Sour").isEmpty());
    }

    @Test
    void findReturnsEmptyForNullOrBlankName() {
        assertTrue(menu.find(null).isEmpty());
        assertTrue(menu.find("").isEmpty());
        assertTrue(menu.find("   ").isEmpty());
    }

    @Test
    void addingTheSameNameTwiceIsRefusedEvenWithAnotherCase() {
        Drink duplicate = new SoftDrink("MOJITO", new BigDecimal("2.00"), false);

        assertThrows(IllegalArgumentException.class, () -> menu.add(duplicate));
        assertEquals(3, menu.size());
    }

    @Test
    void addingNullIsRefused() {
        assertThrows(NullPointerException.class, () -> menu.add(null));
    }

    @Test
    void findByCategoryReturnsOnlyThatCategory() {
        List<Drink> cocktails = menu.findByCategory(DrinkCategory.COCKTAIL);

        assertEquals(1, cocktails.size());
        assertEquals("Mojito", cocktails.get(0).getName());
    }

    @Test
    void findByCategoryReturnsEmptyListWhenNothingMatches() {
        assertTrue(new Menu().findByCategory(DrinkCategory.BEER).isEmpty());
    }

    @Test
    void getAllIsSortedByNameAndCannotBeModified() {
        List<Drink> all = menu.getAll();

        assertEquals(List.of("Cola", "Lager", "Mojito"), all.stream().map(Drink::getName).toList());
        assertThrows(UnsupportedOperationException.class, () -> all.add(mojito()));
        assertFalse(menu.getAll().isEmpty());
    }

    //  search by ingredient

    @Test
    void findByIngredientReturnsEveryCocktailThatContainsItSortedByName() {
        menu.add(virginMojito());

        List<Drink> withMint = menu.findByIngredient("mint");

        assertEquals(List.of("Mojito", "Virgin Mojito"), withMint.stream().map(Drink::getName).toList());
    }

    @Test
    void findByIngredientKeepsOnlyTheCocktailsThatReallyContainIt() {
        menu.add(virginMojito()); // no rum in the virgin version

        List<Drink> withRum = menu.findByIngredient("RUM");

        assertEquals(List.of("Mojito"), withRum.stream().map(Drink::getName).toList());
    }

    @Test
    void findByIngredientIgnoresDrinksThatAreNotMixable() {
        // "Lager" and "Cola" are on the menu but have no ingredient list: they can never match.
        assertTrue(menu.findByIngredient("Lager").isEmpty());
        assertTrue(menu.findByIngredient("Cola").isEmpty());
    }

    @Test
    void findByIngredientReturnsEmptyForUnknownNullOrBlankIngredient() {
        assertTrue(menu.findByIngredient("gin").isEmpty());
        assertTrue(menu.findByIngredient(null).isEmpty());
        assertTrue(menu.findByIngredient("  ").isEmpty());
        assertTrue(new Menu().findByIngredient("rum").isEmpty());
    }
}
