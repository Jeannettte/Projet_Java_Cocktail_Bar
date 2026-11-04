package com.example.cocktailbar;

import com.example.cocktailbar.data.CsvDrinkRepository;
import com.example.cocktailbar.data.DrinkRepository;
import com.example.cocktailbar.data.MenuEntry;
import com.example.cocktailbar.display.ReportFormatter;
import com.example.cocktailbar.display.TextReceiptFormatter;
import com.example.cocktailbar.exception.DataLoadException;
import com.example.cocktailbar.pricing.HappyHourPricing;
import com.example.cocktailbar.pricing.LateNightPricing;
import com.example.cocktailbar.pricing.PricingStrategy;
import com.example.cocktailbar.pricing.StandardPricing;
import com.example.cocktailbar.service.Inventory;
import com.example.cocktailbar.service.Menu;
import com.example.cocktailbar.service.OrderService;

import java.nio.file.Path;
import java.util.List;

/**
 * Entry point and "composition root": the only place that chooses concrete classes
 * and wires them together. Everything else depends on interfaces.
 *
 * <p>Flow: load the data file -> create domain objects -> run business operations -> display results,
 * with a data error stopping the program cleanly.
 */
public final class Application {

    // Relative to the project root, which is where Maven and most IDEs run the program from.
    private static final String DEFAULT_DATA_FILE = "src/main/resources/drinks.csv";

    private Application() {
        // Not meant to be instantiated.
    }

    public static void main(String[] args) {
        Path dataFile = Path.of(args.length > 0 ? args[0] : DEFAULT_DATA_FILE);
        DrinkRepository repository = new CsvDrinkRepository(dataFile);

        try {
            List<MenuEntry> entries = repository.loadAll();

            Menu menu = new Menu();
            Inventory inventory = new Inventory();
            for (MenuEntry entry : entries) {
                menu.add(entry.drink());
                inventory.setStock(entry.drink().getName(), entry.initialStock());
            }

            // Rules are stacked like layers: base price, then late-night surcharge, then happy-hour discount.
            PricingStrategy pricing = new HappyHourPricing(new LateNightPricing(new StandardPricing()));
            OrderService orderService = new OrderService(menu, inventory, pricing);

            new DemoScenario(menu, inventory, orderService, new TextReceiptFormatter(), new ReportFormatter()).run();
        } catch (DataLoadException e) {
            System.err.println("Cannot open the bar: " + e.getMessage());
            System.exit(1);
        }
    }
}
