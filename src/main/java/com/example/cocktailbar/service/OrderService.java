package com.example.cocktailbar.service;

import com.example.cocktailbar.exception.DrinkNotFoundException;
import com.example.cocktailbar.exception.OutOfStockException;
import com.example.cocktailbar.exception.UnderageCustomerException;
import com.example.cocktailbar.model.Customer;
import com.example.cocktailbar.model.Drink;
import com.example.cocktailbar.model.Order;
import com.example.cocktailbar.model.OrderLine;
import com.example.cocktailbar.pricing.PricingStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * the core of the programme.
 * Applies the business rules of an order: the drink must exist, alcohol is only for adults,
 * stock must be sufficient, and the price depends on the pricing strategy.
 *
 * All collaborators are received through the constructor (Dependency Inversion) and the
 * pricing rule is an interface, so changing prices never requires editing this class.
 * To place an order, OrderService checks, in turn, that the order is not empty, that each quantity is positive,
 * that each drink is available, that the customer is of legal drinking age, and that there is sufficient stock.
 * Duplicate items are merged (‘Mojito’ + ‘mojito’ = one line). It then retrieves the price from the pricing rule,
 * deducts the stock, and creates the order: a rejected order therefore never leaves the stock partially updated.
 * It can also cancel an order by returning the drinks to stock
 */
public class OrderService {

    private final Menu menu;
    private final Inventory inventory;
    private final PricingStrategy pricingStrategy;
    private final List<Order> orders = new ArrayList<>();
    private int nextOrderId = 1;

    public OrderService(Menu menu, Inventory inventory, PricingStrategy pricingStrategy) {
        this.menu = Objects.requireNonNull(menu, "menu");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy, "pricingStrategy");
    }

    /**
     * Places an order for the given drinks.
     *
     * @param requestedItems drink name -> quantity
     * @throws IllegalArgumentException   if the order is empty or a quantity is not positive
     * @throws DrinkNotFoundException     if a drink is not on the menu
     * @throws UnderageCustomerException  if a minor asks for an alcoholic drink
     * @throws OutOfStockException        if stock is insufficient for a drink
     */
    public Order placeOrder(Customer customer, Map<String, Integer> requestedItems, LocalDateTime orderTime) {
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(orderTime, "orderTime");
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one drink");
        }

        Map<Drink, Integer> quantities = resolveDrinks(customer, requestedItems);
        List<OrderLine> lines = buildLines(quantities, orderTime);

        // Stock is only touched once every check has passed: a refused order never leaves the inventory half-updated.
        lines.forEach(line -> inventory.remove(line.getDrink().getName(), line.getQuantity()));

        Order order = new Order(nextOrderId++, customer, orderTime, lines);
        orders.add(order);
        return order;
    }

    /** Cancels an order and puts its drinks back in stock. */
    public Order cancelOrder(int orderId) {
        Order order = orders.stream()
                .filter(candidate -> candidate.getId() == orderId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order #" + orderId));

        // cancel() first: if the order was already cancelled it throws, and stock is not restored twice.
        order.cancel();
        order.getLines().forEach(line -> inventory.restock(line.getDrink().getName(), line.getQuantity()));
        return order;
    }

    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    /** Looks up every drink, checks the age rule, and merges duplicates ("Mojito" + "mojito" = one line). */
    private Map<Drink, Integer> resolveDrinks(Customer customer, Map<String, Integer> requestedItems) {
        Map<Drink, Integer> quantities = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> item : requestedItems.entrySet()) {
            String name = item.getKey();
            int quantity = item.getValue() == null ? 0 : item.getValue();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for '" + name + "': " + quantity);
            }

            Drink drink = menu.find(name).orElseThrow(() -> new DrinkNotFoundException(name));
            if (drink.isAlcoholic() && !customer.isAdult()) {
                throw new UnderageCustomerException(customer.getName(), customer.getAge(), drink.getName());
            }
            // Merging matters for the stock check: two entries for the same drink must be compared to stock together.
            quantities.merge(drink, quantity, Integer::sum);
        }
        return quantities;
    }

    /** Checks stock and fixes the unit price of each line at the time of the order. */
    private List<OrderLine> buildLines(Map<Drink, Integer> quantities, LocalDateTime orderTime) {
        List<OrderLine> lines = new ArrayList<>();
        for (Map.Entry<Drink, Integer> entry : quantities.entrySet()) {
            Drink drink = entry.getKey();
            int quantity = entry.getValue();

            int available = inventory.getStock(drink.getName());
            if (available < quantity) {
                throw new OutOfStockException(drink.getName(), quantity, available);
            }
            lines.add(new OrderLine(drink, quantity, pricingStrategy.priceFor(drink, orderTime)));
        }
        // Drink's natural order (alphabetical, ignoring case) gives stable receipts,
        // whatever the iteration order of the input map.
        lines.sort(Comparator.comparing(OrderLine::getDrink));
        return lines;
    }
}
