package com.example.cocktailbar.service;

import com.example.cocktailbar.model.DrinkCategory;
import com.example.cocktailbar.model.Order;
import com.example.cocktailbar.pricing.StandardPricing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.cocktailbar.TestFixtures.adult;
import static com.example.cocktailbar.TestFixtures.at;
import static com.example.cocktailbar.TestFixtures.cola;
import static com.example.cocktailbar.TestFixtures.lager;
import static com.example.cocktailbar.TestFixtures.mojito;
import static com.example.cocktailbar.TestFixtures.orderOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
/*
Test for turnover, rounded average basket value, categories, best-sellers
 (including ties), busiest time, cancelled orders ignored, and reports with no orders
 */

class ReportServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        Menu menu = new Menu();
        menu.add(mojito());
        menu.add(lager());
        menu.add(cola());

        Inventory inventory = new Inventory();
        inventory.setStock("Mojito", 100);
        inventory.setStock("Lager", 100);
        inventory.setStock("Cola", 100);

        orderService = new OrderService(menu, inventory, new StandardPricing());
    }

    private ReportService report() {
        return new ReportService(orderService.getOrders());
    }

    // ---- no data ----

    @Test
    void reportWithoutOrdersIsEmptyButSafe() {
        ReportService empty = new ReportService(List.of());

        assertEquals(0, empty.getOrderCount());
        assertEquals(new BigDecimal("0.00"), empty.totalRevenue());
        assertTrue(empty.averageOrderValue().isEmpty());
        assertTrue(empty.busiestHour().isEmpty());
        assertTrue(empty.topSellers(3).isEmpty());
        assertTrue(empty.revenueByCategory().isEmpty());
    }

    // ---- revenue ----

    @Test
    void totalRevenueSumsAllOrders() {
        orderService.placeOrder(adult(), orderOf("Mojito", 2), at(18, 0));   // 17.00
        orderService.placeOrder(adult(), orderOf("Lager", 1), at(19, 0));    // 4.50

        assertEquals(new BigDecimal("21.50"), report().totalRevenue());
        assertEquals(2, report().getOrderCount());
    }

    @Test
    void cancelledOrdersAreIgnoredByEveryStatistic() {
        orderService.placeOrder(adult(), orderOf("Mojito", 2), at(18, 0));
        Order toCancel = orderService.placeOrder(adult(), orderOf("Lager", 4), at(22, 0));
        orderService.cancelOrder(toCancel.getId());

        ReportService report = report();

        assertEquals(1, report.getOrderCount());
        assertEquals(new BigDecimal("17.00"), report.totalRevenue());
        assertEquals(List.of(new DrinkSales("Mojito", 2)), report.topSellers(5));
        assertEquals(Integer.valueOf(18), report.busiestHour().get());
    }

    @Test
    void averageOrderValueIsRoundedToCents() {
        orderService.placeOrder(adult(), orderOf("Mojito", 1), at(18, 0)); // 8.50
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(18, 5));   // 3.00
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(18, 10));  // 3.00

        // 14.50 / 3 = 4.8333...
        assertEquals(new BigDecimal("4.83"), report().averageOrderValue().get());
    }

    @Test
    void revenueByCategoryGroupsLinesAndKeepsEnumOrder() {
        orderService.placeOrder(adult(), orderOf("Cola", 3, "Mojito", 2, "Lager", 1), at(18, 0));

        Map<DrinkCategory, BigDecimal> revenue = report().revenueByCategory();

        assertEquals(new BigDecimal("17.00"), revenue.get(DrinkCategory.COCKTAIL));
        assertEquals(new BigDecimal("4.50"), revenue.get(DrinkCategory.BEER));
        assertEquals(new BigDecimal("9.00"), revenue.get(DrinkCategory.SOFT_DRINK));
        assertEquals(List.of(DrinkCategory.COCKTAIL, DrinkCategory.BEER, DrinkCategory.SOFT_DRINK),
                new ArrayList<>(revenue.keySet()));
    }

    @Test
    void categoryWithoutSalesIsAbsentFromTheRevenueMap() {
        orderService.placeOrder(adult(), orderOf("Mojito", 1), at(18, 0));

        Map<DrinkCategory, BigDecimal> revenue = report().revenueByCategory();

        assertEquals(1, revenue.size());
        assertTrue(!revenue.containsKey(DrinkCategory.BEER));
    }

    // ---- best sellers ----

    @Test
    void topSellersAreSortedByQuantityAndTiesByName() {
        orderService.placeOrder(adult(), orderOf("Mojito", 2), at(18, 0));
        orderService.placeOrder(adult(), orderOf("Mojito", 1, "Lager", 3), at(19, 0));
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(20, 0));

        // Mojito 3, Lager 3, Cola 1: Lager comes first because of alphabetical order on the tie
        assertEquals(List.of(new DrinkSales("Lager", 3), new DrinkSales("Mojito", 3)), report().topSellers(2));
    }

    @Test
    void topSellersLimitCanBeLargerThanTheNumberOfDrinksSold() {
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(18, 0));

        assertEquals(1, report().topSellers(10).size());
    }

    @Test
    void topSellersWithZeroLimitIsEmptyAndNegativeLimitIsRefused() {
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(18, 0));

        assertTrue(report().topSellers(0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> report().topSellers(-1));
    }

    // ---- busiest hour ----

    @Test
    void busiestHourIsTheHourWithTheMostOrders() {
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(20, 10));
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(18, 0));
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(18, 59));

        assertEquals(Integer.valueOf(18), report().busiestHour().get());
    }

    @Test
    void busiestHourTieIsWonByTheEarliestHour() {
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(22, 0));
        orderService.placeOrder(adult(), orderOf("Cola", 1), at(9, 0));

        assertEquals(Integer.valueOf(9), report().busiestHour().get());
    }
}
