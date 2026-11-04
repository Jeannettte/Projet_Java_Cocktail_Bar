package com.example.cocktailbar.display;

import com.example.cocktailbar.model.Order;

/** Turns an order into text to show to the customer. One method only: implementers have nothing useless to write. */
public interface ReceiptFormatter {

    String format(Order order);
}
