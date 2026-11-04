package com.example.cocktailbar.service;

/** How many units of a drink were sold. Simple data carrier used by the best-sellers ranking. */
public record DrinkSales(String drinkName, int quantity) {
}
