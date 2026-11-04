package com.example.cocktailbar.display;

import com.example.cocktailbar.service.ReportService;
import com.example.cocktailbar.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

/** Builds the text of the sales report. Display only: all figures come from ReportService. */
public class ReportFormatter {

    private static final int TOP_SELLERS_SHOWN = 3;

    public String format(ReportService report) {
        List<String> output = new ArrayList<>();
        output.add("===== SALES REPORT =====");
        output.add("Orders counted : " + report.getOrderCount());
        output.add("Total revenue  : " + MoneyUtils.format(report.totalRevenue()));
        output.add("Average order  : " + report.averageOrderValue()
                .map(MoneyUtils::format)
                .orElse("n/a"));
        output.add("Busiest hour   : " + report.busiestHour()
                .map(hour -> String.format("%02d:00", hour))
                .orElse("n/a"));

        output.add("Revenue by category:");
        report.revenueByCategory().forEach((category, amount) ->
                output.add(String.format("  %-12s %10s", category.getLabel(), MoneyUtils.format(amount))));

        output.add("Top " + TOP_SELLERS_SHOWN + " sellers:");
        report.topSellers(TOP_SELLERS_SHOWN).forEach(sale ->
                output.add(String.format("  %-20s %3d sold", sale.drinkName(), sale.quantity())));

        return String.join(System.lineSeparator(), output);
    }
}
