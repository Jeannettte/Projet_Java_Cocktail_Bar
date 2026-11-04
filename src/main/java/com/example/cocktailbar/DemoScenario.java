package com.example.cocktailbar;

import com.example.cocktailbar.display.ReceiptFormatter;
import com.example.cocktailbar.display.ReportFormatter;
import com.example.cocktailbar.exception.DrinkNotFoundException;
import com.example.cocktailbar.exception.OutOfStockException;
import com.example.cocktailbar.exception.UnderageCustomerException;
import com.example.cocktailbar.model.Customer;
import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.model.Order;
import com.example.cocktailbar.service.Inventory;
import com.example.cocktailbar.service.Menu;
import com.example.cocktailbar.service.OrderService;
import com.example.cocktailbar.service.ReportService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DemoScenario makes the program visible when it is launched
 */

public class DemoScenario {
    // Day of the event
    private static final LocalDate EVENING = LocalDate.of(2026, 11, 6);
    private static final int LOW_STOCK_THRESHOLD = 3;

    private final Menu menu;
    private final Inventory inventory;
    private final OrderService orderService;
    private final ReceiptFormatter receiptFormatter;
    private final ReportFormatter reportFormatter;

    // Constructor
    public DemoScenario(Menu menu, Inventory inventory, OrderService orderService,
                        ReceiptFormatter receiptFormatter, ReportFormatter reportFormatter) {
        this.menu = menu;
        this.inventory = inventory;
        this.orderService = orderService;
        this.receiptFormatter = receiptFormatter;
        this.reportFormatter = reportFormatter;
    }

    public void run() {
        printMenu();
        printIngredientSearch();

        // creation of client objects with their age
        Customer alice = new Customer("Alice", 25);
        Customer bob = new Customer("Bob", 17);
        Customer chloe = new Customer("Chloe", 30);
        Customer dan = new Customer("Dan", 45);

        section("Evening orders");
        //
        attemptOrder("Alice, 17:30 (happy hour), 2 Mojito + 1 Lager",
                alice, items("Mojito", 2, "Lager", 1), at(17, 30));
        attemptOrder("Bob (17), 18:15, soft drinks only",
                bob, items("Cola", 1, "Orange Juice", 1), at(18, 15));
        attemptOrder("Dan, 20:30, 2 Old Fashioned + 1 IPA",
                dan, items("Old Fashioned", 2, "IPA", 1), at(20, 30));
        attemptOrder("Chloe, 23:30 (late night), 2 Margarita + 1 IPA",
                chloe, items("Margarita", 2, "IPA", 1), at(23, 30));

        section("Refused orders");
        attemptOrder("Bob (17) asks for a Lager", bob, items("Lager", 1), at(19, 0));
        attemptOrder("Chloe asks for 3 Old Fashioned (none left)", chloe, items("Old Fashioned", 3), at(20, 0));
        attemptOrder("Chloe asks for a Whisky Sour (not on the menu)", chloe, items("Whisky Sour", 1), at(20, 5));
        attemptOrder("Dan asks for 0 Diet Cola", dan, items("Diet Cola", 0), at(20, 10));
        attemptOrder("Dan sends an empty order", dan, new LinkedHashMap<>(), at(20, 15));

        section("Cancellation");
        cancel(3); // Dan's order: its drinks go back to the shelf
        cancel(3); // second cancellation must be refused

        section("Report");
        System.out.println(reportFormatter.format(new ReportService(orderService.getOrders())));
        System.out.println("Low stock (<= " + LOW_STOCK_THRESHOLD + "): "
                + inventory.findLowStock(LOW_STOCK_THRESHOLD));
    }

    // display Menu
    private void printMenu() {
        section("Menu");
        for (Drink drink : menu.getAll()) {
            String quickTag = drink.isQuickToPrepare() ? " (quick)" : "";
            System.out.println("  " + drink.describe() + " | prep " + drink.getPreparationTimeMinutes() + " min" + quickTag);
        }
    }

    // Diplay Ingredient
    private void printIngredientSearch() {
        section("Search by ingredient");
        for (String ingredient : List.of("rum", "mint", "gin")) {
            String names = menu.findByIngredient(ingredient).stream()
                    .map(Drink::getName)
                    .collect(Collectors.joining(", "));
            System.out.println("  " + ingredient + ": " + (names.isEmpty() ? "none" : names));
        }
    }

    private void attemptOrder(String description, Customer customer,
                              Map<String, Integer> requestedItems, LocalDateTime time) {
        System.out.println("> " + description);
        try {
            Order order = orderService.placeOrder(customer, requestedItems, time);
            System.out.println(receiptFormatter.format(order));
        } catch (OutOfStockException e) {
            System.out.println("  [stock] " + e.getMessage());
        } catch (UnderageCustomerException | DrinkNotFoundException e) {
            System.out.println("  [refused] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("  [invalid] " + e.getMessage());
        }
    }

    private void cancel(int orderId) {
        System.out.println("> Cancel order #" + orderId);
        try {
            Order order = orderService.cancelOrder(orderId);
            System.out.println("  Order #" + order.getId() + " cancelled, stock restored");
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("  [invalid] " + e.getMessage());
        }
    }

    private void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    private static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(EVENING, LocalTime.of(hour, minute));
    }

    /** Builds an ordered drink -> quantity map from alternating name/quantity arguments. */
    private static Map<String, Integer> items(Object... nameAndQuantityPairs) {
        Map<String, Integer> items = new LinkedHashMap<>();
        for (int i = 0; i < nameAndQuantityPairs.length; i += 2) {
            items.put((String) nameAndQuantityPairs[i], (Integer) nameAndQuantityPairs[i + 1]);
        }
        return items;
    }
}
