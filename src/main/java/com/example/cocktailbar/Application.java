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
 *Application is the project's main file. It reads the path to the data file (provided as an argument, or defaulting to src/main/resources/drinks.csv), loads the drinks, sets up the pricing rules, creates the Menu and OrderService, and then launches Inventory and DemoScenario.
 * If the file is invalid, it displays "Cannot open the bar" and terminates gracefully.
 */
public final class Application { // first method that is executed when reading from

    private static final String DEFAULT_DATA_FILE = "src/main/resources/drinks.csv";


    public static void main(String[] args) {
        //Reading the drink.csv file
        Path dataFile = Path.of(args.length > 0 ? args[0] : DEFAULT_DATA_FILE);
        //Creating a repository object that retrieves the file's information
        DrinkRepository repository = new CsvDrinkRepository(dataFile);

        //Using a try/catch block to verify that the information from the drink.csv file is correctly retrieved
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
