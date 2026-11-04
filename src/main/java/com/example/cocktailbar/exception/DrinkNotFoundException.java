package com.example.cocktailbar.exception;

/** Raised when a customer asks for a drink that is not on the menu. */
public class DrinkNotFoundException extends BarException {

    private static final long serialVersionUID = 1L;

    private final String drinkName;

    public DrinkNotFoundException(String drinkName) {
        super("Drink not found on the menu: '" + drinkName + "'");
        this.drinkName = drinkName;
    }

    public String getDrinkName() {
        return drinkName;
    }
}
