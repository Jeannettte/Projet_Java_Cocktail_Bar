package com.example.cocktailbar.display;

import com.example.cocktailbar.model.Order;
import com.example.cocktailbar.model.OrderLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.example.cocktailbar.TestFixtures.adult;
import static com.example.cocktailbar.TestFixtures.at;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextReceiptFormatterTest {

    @Test
    void receiptShowsCustomerDateLinesTotalAndPreparationTime() {
        Order order = new Order(7, adult(), at(20, 0), List.of(
                new OrderLine(mojito(), 2, new BigDecimal("8.50")),
                new OrderLine(lager(), 1, new BigDecimal("4.50"))));

        String receipt = new TextReceiptFormatter().format(order);

        assertTrue(receipt.contains("Order #7"), receipt);
        assertTrue(receipt.contains("Alice"), receipt);
        assertTrue(receipt.contains("2026-11-06 20:00"), receipt);
        assertTrue(receipt.contains("2x Mojito"), receipt);
        assertTrue(receipt.contains("17.00 EUR"), receipt);   // line total
        assertTrue(receipt.contains("21.50 EUR"), receipt);   // order total
        assertTrue(receipt.contains("Preparation time: 11 min"), receipt);
    }
}
