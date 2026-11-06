package com.example.cocktailbar.exception;

/** Raised when the requested quantity of a drink is higher than what is left in stock. */
public class OutOfStockException extends BarException {

    private static final long serialVersionUID = 1L;

    private final String drinkName;
    private final int requested;
    private final int available;

    //constructor
    public OutOfStockException(String drinkName, int requested, int available) {
        super("Not enough '" + drinkName + "' in stock: requested " + requested + ", available " + available);
        this.drinkName = drinkName;
        this.requested = requested;
        this.available = available;
    }

    public String getDrinkName() {
        return drinkName;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
