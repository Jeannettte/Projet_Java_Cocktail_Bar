package com.example.cocktailbar.service;

import com.example.cocktailbar.exception.DrinkNotFoundException;
import com.example.cocktailbar.exception.OutOfStockException;
import com.example.cocktailbar.exception.UnderageCustomerException;
import com.example.cocktailbar.model.Customer;
import com.example.cocktailbar.model.Order;
import com.example.cocktailbar.model.OrderStatus;
import com.example.cocktailbar.pricing.HappyHourPricing;
import com.example.cocktailbar.pricing.StandardPricing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.example.cocktailbar.TestFixtures.adult;
import static com.example.cocktailbar.TestFixtures.at;
import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.minor;
import static com.example.cocktailbar.TestFixtures.mojito;
import static com.example.cocktailbar.TestFixtures.orderOf;
import static com.example.cocktailbar.TestFixtures.virginMojito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The heart of the bar: total and stock for a standard order, exact age of 18, duplicates merged, order
 * declined with no effect on stock, cancellation, price applied at the time of the order
 * Menu used in every test: Mojito 8.50 (stock 5), Lager 4.50 (stock 10), Cola 3.00 (stock 2),
 * Virgin Mojito 6.50 (stock 5). Prices are the standard ones unless a test says otherwise.
 */
class OrderServiceTest {

    private Menu menu;
    private Inventory inventory;
    private OrderService service;

    @BeforeEach
    void setUp() {
        menu = new Menu();
        menu.add(mojito());
        menu.add(lager());
        menu.add(cola());
        menu.add(virginMojito());

        inventory = new Inventory();
        inventory.setStock("Mojito", 5);
        inventory.setStock("Lager", 10);
        inventory.setStock("Cola", 2);
        inventory.setStock("Virgin Mojito", 5);

        service = new OrderService(menu, inventory, new StandardPricing());
    }

    //  normal cases

    @Test
    void normalOrderComputesTheTotalAndReducesStock() {
        Order order = service.placeOrder(adult(), orderOf("Mojito", 2, "Lager", 1), at(20, 0));

        assertEquals(new BigDecimal("21.50"), order.getTotal()); // 2 x 8.50 + 4.50
        assertEquals(3, inventory.getStock("Mojito"));
        assertEquals(9, inventory.getStock("Lager"));
        assertEquals(OrderStatus.PLACED, order.getStatus());
    }

    @Test
    void orderIdsStartAtOneAndIncrease() {
        Order first = service.placeOrder(adult(), orderOf("Lager", 1), at(20, 0));
        Order second = service.placeOrder(adult(), orderOf("Lager", 1), at(20, 5));

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(2, service.getOrders().size());
    }

    @Test
    void linesAreSortedAlphabeticallyWhateverTheInputOrder() {
        Order order = service.placeOrder(adult(), orderOf("Mojito", 1, "Lager", 1), at(20, 0));

        assertEquals("Lager", order.getLines().get(0).getDrink().getName());
        assertEquals("Mojito", order.getLines().get(1).getDrink().getName());
    }

    @Test
    void drinkNamesAreMatchedIgnoringCase() {
        Order order = service.placeOrder(adult(), orderOf("mojito", 1), at(20, 0));

        assertEquals("Mojito", order.getLines().get(0).getDrink().getName());
    }

    //  edge cases

    @Test
    void sameDrinkWrittenTwiceIsMergedIntoOneLine() {
        Order order = service.placeOrder(adult(), orderOf("Mojito", 2, "mojito", 1), at(20, 0));

        assertEquals(1, order.getLines().size());
        assertEquals(3, order.getLines().get(0).getQuantity());
        assertEquals(2, inventory.getStock("Mojito"));
    }

    @Test
    void mergedQuantitiesAreComparedToStockTogether() {
        // 3 + 3 = 6 > 5: each entry alone would pass, so the check must use the merged quantity
        assertThrows(OutOfStockException.class,
                () -> service.placeOrder(adult(), orderOf("Mojito", 3, "mojito", 3), at(20, 0)));

        assertEquals(5, inventory.getStock("Mojito"));
    }

    @Test
    void orderingExactlyTheRemainingStockSucceeds() {
        service.placeOrder(adult(), orderOf("Mojito", 5), at(20, 0));

        assertEquals(0, inventory.getStock("Mojito"));
    }

    @Test
    void customerWhoIsExactlyEighteenCanOrderAlcohol() {
        Order order = service.placeOrder(new Customer("Eve", 18), orderOf("Lager", 1), at(20, 0));

        assertEquals(new BigDecimal("4.50"), order.getTotal());
    }

    @Test
    void minorCanOrderSoftDrinksAndAlcoholFreeCocktails() {
        Order order = service.placeOrder(minor(), orderOf("Cola", 1, "Virgin Mojito", 1), at(20, 0));

        assertEquals(new BigDecimal("9.50"), order.getTotal());
    }

    //  invalid situations and exceptions

    @Test
    void orderingMoreThanTheStockFailsAndReportsTheAmounts() {
        OutOfStockException error = assertThrows(OutOfStockException.class,
                () -> service.placeOrder(adult(), orderOf("Mojito", 6), at(20, 0)));

        assertEquals(6, error.getRequested());
        assertEquals(5, error.getAvailable());
        assertEquals(5, inventory.getStock("Mojito"));
    }

    @Test
    void minorCannotOrderAlcohol() {
        UnderageCustomerException error = assertThrows(UnderageCustomerException.class,
                () -> service.placeOrder(minor(), orderOf("Lager", 1), at(20, 0)));

        assertTrue(error.getMessage().contains("Bob"));
        assertEquals(10, inventory.getStock("Lager"));
    }

    @Test
    void unknownDrinkIsRefused() {
        DrinkNotFoundException error = assertThrows(DrinkNotFoundException.class,
                () -> service.placeOrder(adult(), orderOf("Whisky Sour", 1), at(20, 0)));

        assertEquals("Whisky Sour", error.getDrinkName());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void nonPositiveQuantityIsRefused(int quantity) {
        assertThrows(IllegalArgumentException.class,
                () -> service.placeOrder(adult(), orderOf("Cola", quantity), at(20, 0)));
    }

    @Test
    void emptyOrNullOrderIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder(adult(), Map.of(), at(20, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder(adult(), null, at(20, 0)));
    }

    @Test
    void missingCustomerOrTimeIsRefused() {
        assertThrows(NullPointerException.class, () -> service.placeOrder(null, orderOf("Cola", 1), at(20, 0)));
        assertThrows(NullPointerException.class, () -> service.placeOrder(adult(), orderOf("Cola", 1), null));
    }

    @Test
    void refusedOrderLeavesStockAndHistoryUntouched() {
        // The valid Lager comes first: it must NOT be removed from stock when Cola (stock 2) fails afterwards.
        assertThrows(OutOfStockException.class,
                () -> service.placeOrder(adult(), orderOf("Lager", 1, "Cola", 3), at(20, 0)));

        assertEquals(10, inventory.getStock("Lager"));
        assertEquals(2, inventory.getStock("Cola"));
        assertTrue(service.getOrders().isEmpty());
    }

    @Test
    void ageRefusalAlsoLeavesStockUntouched() {
        assertThrows(UnderageCustomerException.class,
                () -> service.placeOrder(minor(), orderOf("Cola", 1, "Lager", 1), at(20, 0)));

        assertEquals(2, inventory.getStock("Cola"));
        assertEquals(10, inventory.getStock("Lager"));
    }

    //  pricing is delegated to the strategy

    @Test
    void pricingStrategyIsAppliedAtTheTimeOfTheOrder() {
        OrderService happyHourService = new OrderService(menu, inventory, new HappyHourPricing(new StandardPricing()));

        Order duringHappyHour = happyHourService.placeOrder(adult(), orderOf("Mojito", 2), at(17, 30));
        Order afterHappyHour = happyHourService.placeOrder(adult(), orderOf("Mojito", 1), at(20, 0));

        assertEquals(new BigDecimal("11.90"), duringHappyHour.getTotal()); // 2 x 5.95
        assertEquals(new BigDecimal("8.50"), afterHappyHour.getTotal());
    }

    //  cancellation

    @Test
    void cancellingAnOrderPutsItsDrinksBackInStock() {
        Order order = service.placeOrder(adult(), orderOf("Mojito", 2, "Cola", 1), at(20, 0));

        Order cancelled = service.cancelOrder(order.getId());

        assertTrue(cancelled.isCancelled());
        assertEquals(5, inventory.getStock("Mojito"));
        assertEquals(2, inventory.getStock("Cola"));
    }

    @Test
    void cancellingTwiceIsRefusedAndStockIsNotRestoredTwice() {
        Order order = service.placeOrder(adult(), orderOf("Mojito", 2), at(20, 0));
        service.cancelOrder(order.getId());

        assertThrows(IllegalStateException.class, () -> service.cancelOrder(order.getId()));

        assertEquals(5, inventory.getStock("Mojito"));
    }

    @Test
    void cancellingAnUnknownOrderIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> service.cancelOrder(42));
    }

    @Test
    void getOrdersReturnsACopyThatCannotBeModified() {
        service.placeOrder(adult(), orderOf("Cola", 1), at(20, 0));
        List<Order> orders = service.getOrders();

        assertThrows(UnsupportedOperationException.class, orders::clear);
    }
}
