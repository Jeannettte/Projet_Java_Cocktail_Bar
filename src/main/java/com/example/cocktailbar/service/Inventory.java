package com.example.cocktailbar.service;

import com.example.cocktailbar.exception.OutOfStockException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Tracks how many units of each drink are left.
 * Its only job is stock; it knows nothing about orders or prices.
 * The Inventory class sets a stock level, initialises it (0 for an unknown drink), removes units
 * (throwing an OutOfStockException if there are not enough), restocks it (either by replenishment or cancellation)
 * and lists the drinks whose stock is less than or equal to a threshold.*/
public class Inventory {

    // Same case-insensitive convention as Menu, so both agree on what "the same drink" means.
    private final Map<String, Integer> stock = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    //Constructor
    public void setStock(String drinkName, int quantity) {
        requireName(drinkName);
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock must not be negative: " + quantity);
        }
        stock.put(drinkName.trim(), quantity);
    }

    /** A drink that was never registered simply has no stock. */
    public int getStock(String drinkName) {
        if (drinkName == null) {
            return 0;
        }
        return stock.getOrDefault(drinkName.trim(), 0);
    }

    public void remove(String drinkName, int quantity) {
        requireName(drinkName);
        requirePositive(quantity);
        int available = getStock(drinkName);
        if (available < quantity) {
            throw new OutOfStockException(drinkName.trim(), quantity, available);
        }
        stock.put(drinkName.trim(), available - quantity);
    }

    public void restock(String drinkName, int quantity) {
        requireName(drinkName);
        requirePositive(quantity);
        stock.merge(drinkName.trim(), quantity, Integer::sum);
    }

    /** Names of drinks whose stock is at or below the threshold, sorted alphabetically. */
    public List<String> findLowStock(int threshold) {
        return stock.entrySet().stream()
                .filter(entry -> entry.getValue() <= threshold)
                .map(Map.Entry::getKey)
                .toList();
    }

    private void requireName(String drinkName) {
        if (drinkName == null || drinkName.isBlank()) {
            throw new IllegalArgumentException("Drink name must not be blank");
        }
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }
}
