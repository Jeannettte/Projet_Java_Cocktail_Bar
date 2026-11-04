package com.example.cocktailbar.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.cocktailbar.TestFixtures.adult;
import static com.example.cocktailbar.TestFixtures.at;
import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    private static OrderLine line(Drink drink, int quantity, String unitPrice) {
        return new OrderLine(drink, quantity, new BigDecimal(unitPrice));
    }

    private static Order orderWith(OrderLine... lines) {
        return new Order(1, adult(), at(20, 0), List.of(lines));
    }

    @Test
    void totalIsTheSumOfEveryLine() {
        Order order = orderWith(line(mojito(), 2, "8.50"), line(lager(), 1, "4.50"));

        assertEquals(new BigDecimal("21.50"), order.getTotal());
    }

    @Test
    void lineTotalUsesTheUnitPriceStoredAtOrderTime() {
        // 5.95 is a happy-hour price: the line must keep it even though the menu price is 8.50
        OrderLine discounted = line(mojito(), 3, "5.95");

        assertEquals(new BigDecimal("17.85"), discounted.getLineTotal());
        assertEquals(new BigDecimal("8.50"), mojito().getBasePrice());
    }

    @Test
    void preparationTimeMultipliesEachDrinkByItsQuantity() {
        Order order = orderWith(line(mojito(), 2, "8.50"), line(lager(), 1, "4.50"));

        assertEquals(11, order.getPreparationTimeMinutes()); // 2 x 5 + 1 x 1
    }

    @Test
    void orderWithoutLineIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Order(1, adult(), at(20, 0), List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Order(1, adult(), at(20, 0), null));
    }

    @Test
    void orderKeepsItsOwnCopyOfTheLines() {
        List<OrderLine> lines = new ArrayList<>(List.of(line(cola(), 1, "3.00")));
        Order order = new Order(1, adult(), at(20, 0), lines);

        lines.add(line(lager(), 5, "4.50")); // must not leak into the order

        assertEquals(1, order.getLines().size());
        assertEquals(new BigDecimal("3.00"), order.getTotal());
    }

    @Test
    void cancelMarksTheOrderAsCancelled() {
        Order order = orderWith(line(cola(), 1, "3.00"));
        assertFalse(order.isCancelled());

        order.cancel();

        assertTrue(order.isCancelled());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancellingTwiceIsRefused() {
        Order order = orderWith(line(cola(), 1, "3.00"));
        order.cancel();

        IllegalStateException error = assertThrows(IllegalStateException.class, order::cancel);

        assertTrue(error.getMessage().contains("#1"));
    }

    @Test
    void orderLineRejectsNonPositiveQuantityAndNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> line(cola(), 0, "3.00"));
        assertThrows(IllegalArgumentException.class, () -> line(cola(), -2, "3.00"));
        assertThrows(IllegalArgumentException.class, () -> line(cola(), 1, "-0.01"));
    }
}
