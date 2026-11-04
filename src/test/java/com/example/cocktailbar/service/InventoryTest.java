package com.example.cocktailbar.service;

import com.example.cocktailbar.exception.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setStock("Mojito", 5);
        inventory.setStock("Cola", 2);
    }

    @Test
    void removeDecreasesTheStock() {
        inventory.remove("Mojito", 2);

        assertEquals(3, inventory.getStock("Mojito"));
    }

    @Test
    void removingExactlyTheRemainingStockIsAllowed() {
        inventory.remove("Mojito", 5);

        assertEquals(0, inventory.getStock("Mojito"));
    }

    @Test
    void removingMoreThanAvailableFailsAndKeepsStockUntouched() {
        OutOfStockException error = assertThrows(OutOfStockException.class, () -> inventory.remove("Mojito", 6));

        assertEquals("Mojito", error.getDrinkName());
        assertEquals(6, error.getRequested());
        assertEquals(5, error.getAvailable());
        assertEquals(5, inventory.getStock("Mojito"));
    }

    @Test
    void removingFromAnEmptyStockFails() {
        inventory.remove("Cola", 2);

        assertThrows(OutOfStockException.class, () -> inventory.remove("Cola", 1));
    }

    @Test
    void unknownDrinkHasNoStock() {
        assertEquals(0, inventory.getStock("Whisky Sour"));
        assertEquals(0, inventory.getStock(null));
        assertThrows(OutOfStockException.class, () -> inventory.remove("Whisky Sour", 1));
    }

    @Test
    void drinkNamesIgnoreCase() {
        assertEquals(5, inventory.getStock("mojito"));

        inventory.remove("MOJITO", 1);

        assertEquals(4, inventory.getStock("Mojito"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    void removeAndRestockRejectNonPositiveQuantities(int quantity) {
        assertThrows(IllegalArgumentException.class, () -> inventory.remove("Mojito", quantity));
        assertThrows(IllegalArgumentException.class, () -> inventory.restock("Mojito", quantity));
    }

    @Test
    void restockAddsToTheExistingStock() {
        inventory.restock("Mojito", 10);

        assertEquals(15, inventory.getStock("Mojito"));
    }

    @Test
    void setStockRejectsNegativeQuantityAndBlankName() {
        assertThrows(IllegalArgumentException.class, () -> inventory.setStock("Mojito", -1));
        assertThrows(IllegalArgumentException.class, () -> inventory.setStock(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> inventory.setStock(null, 1));
    }

    @Test
    void setStockAcceptsZero() {
        inventory.setStock("Mojito", 0);

        assertEquals(0, inventory.getStock("Mojito"));
    }

    @Test
    void lowStockThresholdIsInclusiveAndResultIsSorted() {
        inventory.setStock("Lager", 40);

        assertEquals(List.of("Cola"), inventory.findLowStock(2));
        assertEquals(List.of("Cola", "Mojito"), inventory.findLowStock(5));
        assertTrue(inventory.findLowStock(1).isEmpty());
    }
}
