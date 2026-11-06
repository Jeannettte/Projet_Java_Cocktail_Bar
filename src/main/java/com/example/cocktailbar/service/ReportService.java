package com.example.cocktailbar.service;

import com.example.cocktailbar.model.DrinkCategory;
import com.example.cocktailbar.model.Order;
import com.example.cocktailbar.model.OrderLine;
import com.example.cocktailbar.utils.CollectionUtils;
import com.example.cocktailbar.utils.MoneyUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Computes statistics on a list of orders. It only reads data, never changes it.
 * Cancelled orders are dropped once, in the constructor, so no method has to remember to filter them.
 *  The class calculates the  number of orders, turnover, average basket value,
 * turnover by category, best-sellers and the busiest time of day. The average basket value and
 * the busiest time of day are left blank if there are no orders, as there is no such thing as an average
 * of nothing. Cancelled orders are removed once and for all as soon as they are created
 */
public class ReportService {

    private final List<Order> activeOrders;

    public ReportService(List<Order> orders) {
        this.activeOrders = orders.stream()
                .filter(order -> !order.isCancelled())
                .toList();
    }

    public int getOrderCount() {
        return activeOrders.size();
    }

    public BigDecimal totalRevenue() {
        BigDecimal sum = activeOrders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MoneyUtils.round(sum);
    }

    /** Empty when there is no order: an average of nothing is undefined, not zero. */
    public Optional<BigDecimal> averageOrderValue() {
        if (activeOrders.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal average = totalRevenue().divide(BigDecimal.valueOf(activeOrders.size()), 2, RoundingMode.HALF_UP);
        return Optional.of(average);
    }

    /** Revenue per category. Categories that sold nothing are absent from the map. */
    public Map<DrinkCategory, BigDecimal> revenueByCategory() {
        return activeOrders.stream()
                .flatMap(order -> order.getLines().stream())
                .collect(Collectors.groupingBy(
                        line -> line.getDrink().getCategory(),
                        () -> new EnumMap<>(DrinkCategory.class), // EnumMap keeps categories in enum order
                        Collectors.reducing(BigDecimal.ZERO, OrderLine::getLineTotal, BigDecimal::add)));
    }

    public List<DrinkSales> topSellers(int limit) {
        Map<String, Integer> quantityByDrink = activeOrders.stream()
                .flatMap(order -> order.getLines().stream())
                .collect(Collectors.groupingBy(
                        line -> line.getDrink().getName(),
                        Collectors.summingInt(OrderLine::getQuantity)));

        return CollectionUtils.topN(quantityByDrink, limit).stream()
                .map(entry -> new DrinkSales(entry.getKey(), entry.getValue()))
                .toList();
    }

    /** Hour of the day (0-23) with the most orders; on a tie the earliest hour wins. */
    public Optional<Integer> busiestHour() {
        Map<Integer, Long> ordersByHour = activeOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().getHour(), Collectors.counting()));

        Comparator<Map.Entry<Integer, Long>> mostOrdersThenEarliestHour =
                Map.Entry.<Integer, Long>comparingByValue()
                        .thenComparing(Map.Entry.<Integer, Long>comparingByKey().reversed());

        return ordersByHour.entrySet().stream()
                .max(mostOrdersThenEarliestHour)
                .map(Map.Entry::getKey);
    }
}
