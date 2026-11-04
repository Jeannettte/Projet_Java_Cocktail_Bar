package com.example.cocktailbar.exception;

/** Raised when a customer under the legal drinking age orders an alcoholic drink. */
public class UnderageCustomerException extends BarException {

    private static final long serialVersionUID = 1L;

    // Plain values instead of Customer/Drink objects: the exception package must not depend on the model package.
    public UnderageCustomerException(String customerName, int customerAge, String drinkName) {
        super(customerName + " (" + customerAge + " years old) cannot be served the alcoholic drink '" + drinkName + "'");
    }
}
