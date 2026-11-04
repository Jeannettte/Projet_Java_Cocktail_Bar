package com.example.cocktailbar.display;

import com.example.cocktailbar.model.Order;
import com.example.cocktailbar.model.OrderLine;
import com.example.cocktailbar.utils.MoneyUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Plain-text receipt for the console. */
public class TextReceiptFormatter implements ReceiptFormatter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String format(Order order) {
        List<String> output = new ArrayList<>();
        output.add(String.format("  Order #%d - %s - %s",
                order.getId(), order.getCustomer().getName(), order.getCreatedAt().format(DATE_FORMAT)));

        for (OrderLine line : order.getLines()) {
            output.add(String.format("    %dx %-20s %10s",
                    line.getQuantity(), line.getDrink().getName(), MoneyUtils.format(line.getLineTotal())));
        }

        output.add(String.format("    %-23s %10s", "TOTAL", MoneyUtils.format(order.getTotal())));
        output.add(String.format("    Preparation time: %d min", order.getPreparationTimeMinutes()));
        return String.join(System.lineSeparator(), output);
    }
}
