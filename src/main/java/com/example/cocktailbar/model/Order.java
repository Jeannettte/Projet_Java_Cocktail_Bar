package com.example.cocktailbar.model;

import com.example.cocktailbar.utils.MoneyUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * A validated order.
 * A number, the customer, the date and time, the lines (at least one) and a status. It calculates the total and the preparation time and can be cancelled (a
 * second cancellation is not permitted). It does not decide whether the order is authorised: that is the job of OrderService.
 * créée par OrderService ; lue par TextReceiptFormatter et ReportService
 */
public class Order {

    private final int id;
    private final Customer customer;
    private final LocalDateTime createdAt;
    private final List<OrderLine> lines;
    private OrderStatus status = OrderStatus.PLACED;

    public Order(int id, Customer customer, LocalDateTime createdAt, List<OrderLine> lines) {
        this.id = id;
        this.customer = Objects.requireNonNull(customer, "customer");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line");
        }
        this.lines = List.copyOf(lines);
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = lines.stream()
                .map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MoneyUtils.round(sum);
    }

    /** The bartender prepares drinks one after the other, so times add up. */
    public int getPreparationTimeMinutes() {
        return lines.stream()
                .mapToInt(line -> line.getDrink().getPreparationTimeMinutes() * line.getQuantity())
                .sum();
    }

    /** Cancelling twice is a programming/usage error, so it is refused rather than silently ignored. */
    public void cancel() {
        if (isCancelled()) {
            throw new IllegalStateException("Order #" + id + " is already cancelled");
        }
        status = OrderStatus.CANCELLED;
    }
}
