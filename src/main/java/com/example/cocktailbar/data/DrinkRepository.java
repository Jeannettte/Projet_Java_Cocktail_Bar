package com.example.cocktailbar.data;

import com.example.cocktailbar.exception.DataLoadException;

import java.util.List;

/**
 * Source of the bar's menu. The rest of the application only knows this interface,
 * so the CSV file could be replaced by JSON or a database without touching any other class
 * (Dependency Inversion). It has a single method on purpose (Interface Segregation).
 */
public interface DrinkRepository {

    List<MenuEntry> loadAll() throws DataLoadException;
}
