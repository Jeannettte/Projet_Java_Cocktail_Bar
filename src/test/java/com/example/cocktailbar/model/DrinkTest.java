package com.example.cocktailbar.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.cocktailbar.TestFixtures.alcoholFreeBeer;
import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static com.example.cocktailbar.TestFixtures.virginMojito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
     Verification of the name, price and alcohol content;
     characteristics specific to each type of drink; consistency and sorting
 */
class DrinkTest {

    private static final BigDecimal PRICE = new BigDecimal("3.00");

    //  inheritance and polymorphism

    @Test
    void cocktailPreparationTimeAddsOneMinutePerIngredient() {
        assertEquals(5, mojito().getPreparationTimeMinutes()); // 2 minutes base + 3 ingredients
    }

    @Test
    void sameMethodCallBehavesDifferentlyForEachSubclass() {
        List<Drink> drinks = List.of(mojito(), lager(), cola());

        List<Integer> preparationTimes = drinks.stream().map(Drink::getPreparationTimeMinutes).toList();
        List<DrinkCategory> categories = drinks.stream().map(Drink::getCategory).toList();

        assertEquals(List.of(5, 1, 1), preparationTimes);
        assertEquals(List.of(DrinkCategory.COCKTAIL, DrinkCategory.BEER, DrinkCategory.SOFT_DRINK), categories);
    }

    @Test
    void describeAddsSubclassSpecificDetails() {
        assertTrue(mojito().describe().contains("rum, mint, lime"));
        assertTrue(lager().describe().contains("33 cl"));
        assertTrue(new SoftDrink("Diet Cola", PRICE, true).describe().contains("sugar-free"));
    }

    //  alcohol rule (used by the age check)

    @Test
    void drinkIsAlcoholicOnlyWhenItContainsAlcohol() {
        assertTrue(mojito().isAlcoholic());
        assertTrue(lager().isAlcoholic());
        assertFalse(virginMojito().isAlcoholic());
        assertFalse(alcoholFreeBeer().isAlcoholic());
        assertFalse(cola().isAlcoholic());
    }

    //  validation

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankNameIsRejected(String name) {
        assertThrows(IllegalArgumentException.class, () -> new SoftDrink(name, PRICE, false));
    }

    @Test
    void nullNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SoftDrink(null, PRICE, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1.00", "0.004"}) // 0.004 rounds to 0.00: it must not become a free drink
    void priceThatIsNotStrictlyPositiveIsRejected(String price) {
        assertThrows(IllegalArgumentException.class, () -> new SoftDrink("Water", new BigDecimal(price), false));
    }

    @Test
    void nullPriceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SoftDrink("Water", null, false));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 100.1})
    void alcoholPercentageOutsideZeroToHundredIsRejected(double percent) {
        assertThrows(IllegalArgumentException.class, () -> new Beer("Strange", PRICE, percent, 33));
    }

    @Test
    void alcoholPercentageBoundariesAreAccepted() {
        assertFalse(new Beer("Zero", PRICE, 0.0, 33).isAlcoholic());
        assertTrue(new Beer("Pure", PRICE, 100.0, 33).isAlcoholic());
    }

    @Test
    void priceIsRoundedToCents() {
        assertEquals(new BigDecimal("2.00"), new SoftDrink("Water", new BigDecimal("1.999"), false).getBasePrice());
    }

    @Test
    void cocktailWithoutIngredientsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Cocktail("Empty", PRICE, 5.0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Cocktail("Null", PRICE, 5.0, null));
    }

    @Test
    void cocktailKeepsItsOwnCopyOfTheIngredients() {
        List<String> ingredients = new ArrayList<>(List.of("rum", "mint"));
        Cocktail cocktail = new Cocktail("Mojito", PRICE, 10.0, ingredients);

        ingredients.add("sugar"); // changing the caller's list must not change the cocktail

        assertEquals(2, cocktail.getIngredients().size());
        assertEquals(4, cocktail.getPreparationTimeMinutes());
        assertThrows(UnsupportedOperationException.class, () -> cocktail.getIngredients().add("ice"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -33})
    void beerWithNonPositiveVolumeIsRejected(int volume) {
        assertThrows(IllegalArgumentException.class, () -> new Beer("Lager", PRICE, 5.0, volume));
    }

    //  equality

    @Test
    void drinksWithTheSameNameIgnoringCaseAreEqual() {
        Drink lower = new SoftDrink("cola", PRICE, false);

        assertEquals(cola(), lower);
        assertEquals(cola().hashCode(), lower.hashCode());
        assertFalse(cola().equals(lager()));
    }

    //  natural order (Comparable)

    @Test
    void drinksAreSortedAlphabeticallyIgnoringCase() {
        List<Drink> drinks = List.of(mojito(), new SoftDrink("apple juice", PRICE, false), lager(), cola());

        List<String> names = drinks.stream().sorted().map(Drink::getName).toList();

        assertEquals(List.of("apple juice", "Cola", "Lager", "Mojito"), names);
    }

    @Test
    void compareToIsZeroExactlyWhenDrinksAreEqual() {
        Drink upperCase = new SoftDrink("COLA", PRICE, false);

        assertEquals(0, cola().compareTo(upperCase));
        assertEquals(cola(), upperCase);
        assertTrue(cola().compareTo(lager()) < 0);
        assertTrue(lager().compareTo(cola()) > 0);
    }
}
