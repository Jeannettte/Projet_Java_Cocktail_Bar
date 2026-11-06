package com.example.cocktailbar.model;

/** Life cycle of an order. A cancelled order is kept for history but ignored by sales reports.
 * Les deux états possibles d'une commande*/
public enum OrderStatus {
    PLACED,
    CANCELLED
}
