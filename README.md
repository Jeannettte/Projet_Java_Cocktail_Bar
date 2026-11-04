# Cocktail Bar

A small console application that simulates the orders of a cocktail bar: it loads a menu from a CSV file, takes orders, applies business rules (age check, stock, time-based prices) and prints receipts and a sales report.

The goal of the project is **clean object-oriented design**, meaningful business logic, clean Java code and tests. There is no GUI and no database on purpose.

## Table of contents

1. [How to run it](#how-to-run-it)
2. [Main features](#main-features)
3. [Project structure and main classes](#project-structure-and-main-classes)
4. [Technical decisions](#technical-decisions)
5. [Where inheritance is used](#where-inheritance-is-used)
6. [Where polymorphism is used](#where-polymorphism-is-used)
7. [Where the abstract class and interfaces are used](#where-the-abstract-class-and-interfaces-are-used)
8. [Custom exceptions](#custom-exceptions)
9. [How the application loads its data](#how-the-application-loads-its-data)
10. [How SOLID principles are applied](#how-solid-principles-are-applied)
11. [Java features used](#java-features-used)
12. [How to run the tests](#how-to-run-the-tests)

## How to run it

**Requirements:** JDK 17 or newer, and Maven 3.8+ (Maven is only needed for the tests and the shortcut below).

From the project root:

```bash
mvn compile exec:java
```

Use another data file by passing its path:

```bash
mvn compile exec:java -Dexec.args="path/to/my-drinks.csv"
```

Without Maven:

```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.example.cocktailbar.Application
```

In an IDE, run the `main` method of `Application` with the **project root as working directory**: the default data file path (`src/main/resources/drinks.csv`) is relative to it.

### Execution flow

```
Application starts
      ↓
Load data from src/main/resources/drinks.csv        (CsvDrinkRepository)
      ↓
Create domain objects: Menu, Inventory, Drinks      (model + service)
      ↓
Execute business operations: orders, refusals, cancellation   (OrderService)
      ↓
Display results: receipts, sales report, low stock  (display package)
      ↓
Handle possible errors: refused orders are reported, a bad data file stops the program cleanly
```

The program runs a scripted evening (`DemoScenario`) so that one run shows normal orders **and** every kind of refused order.

Excerpt of the output:

```
> Alice, 17:30 (happy hour), 2 Mojito + 1 Lager
  Order #1 - Alice - 2026-11-06 17:30
    1x Lager                  4.50 EUR
    2x Mojito                11.90 EUR
    TOTAL                    16.40 EUR
    Preparation time: 15 min

> Bob (17) asks for a Lager
  [refused] Bob (17 years old) cannot be served the alcoholic drink 'Lager'
> Chloe asks for 3 Old Fashioned (none left)
  [stock] Not enough 'Old Fashioned' in stock: requested 3, available 0

===== SALES REPORT =====
Orders counted : 3
Total revenue  : 49.93 EUR
Busiest hour   : 17:00
```

## Main features

- Menu loaded from a CSV file (cocktails, beers, soft drinks), with initial stock.
- Orders with several drinks; the same drink typed twice (`Mojito` + `mojito`) is merged into one line.
- **Business rules** applied on every order:
  - the drink must exist on the menu;
  - alcoholic drinks are refused to customers under 18 (18 exactly is allowed);
  - quantities must be positive and available in stock;
  - an order is **all or nothing**: if one drink is refused, no stock is removed.
- **Time-based pricing**: 30% off cocktails during happy hour (17:00 to 19:00), 15% surcharge on everything late at night (23:00 to 04:00). Rules can be stacked.
- Receipts with total and total preparation time.
- Search of the cocktails that contain a given ingredient (case-insensitive, whole word).
- Order cancellation: drinks go back in stock; a cancelled order cannot be cancelled twice.
- Sales report: total revenue, average order, revenue by category, best sellers, busiest hour, low stock. Cancelled orders are ignored.

## Project structure and main classes

```
src/
├── main/
│   ├── java/com/example/cocktailbar/
│   │   ├── Application.java        entry point and wiring (composition root)
│   │   ├── DemoScenario.java       the scripted evening shown when running the program
│   │   ├── model/                  entities
│   │   ├── pricing/                price rules
│   │   ├── service/                business logic
│   │   ├── data/                   file loading
│   │   ├── display/                text output
│   │   ├── exception/              custom exceptions
│   │   └── utils/                  small helpers
│   └── resources/drinks.csv        data file used to initialise the application
└── test/java/com/example/cocktailbar/   JUnit 5 tests (same packages as the code they test)
```

| Package | Class | Role |
|---|---|---|
| `model` | `Drink` (abstract), `Cocktail`, `Beer`, `SoftDrink` | What the bar sells. |
| `model` | `Preparable`, `Mixable` (interfaces) | Capabilities: "can be prepared" (all drinks) and "is made of ingredients" (cocktails only). Each has a `default` method. |
| `model` | `Customer`, `Order`, `OrderLine` | Who orders and what was ordered. `OrderLine` stores the unit price applied at order time. |
| `model` | `DrinkCategory`, `OrderStatus` | Enums. |
| `pricing` | `PricingStrategy` (interface), `StandardPricing`, `HappyHourPricing`, `LateNightPricing` | How the price of a drink depends on the time. |
| `service` | `Menu` | Catalogue of drinks (search, by category). |
| `service` | `Inventory` | Stock of each drink (remove, restock, low stock). |
| `service` | `OrderService` | **The core business logic**: validates and places orders, cancels them. |
| `service` | `ReportService`, `DrinkSales` | Statistics on orders (streams and collectors). |
| `data` | `DrinkRepository` (interface), `CsvDrinkRepository` | Reads the file. |
| `data` | `DrinkCsvParser`, `MenuEntry` | Turns one CSV line into a drink and its stock. |
| `display` | `ReceiptFormatter` (interface), `TextReceiptFormatter`, `ReportFormatter` | Builds the text shown to the user. |
| `utils` | `MoneyUtils`, `CollectionUtils` | Rounding/format of money; generic `topN` helper. |

No class does everything: the biggest one, `OrderService`, only applies order rules; stock, catalogue, prices, statistics, file reading and display each have their own class.

## Technical decisions

1. **`BigDecimal` for money, and the price is frozen in `OrderLine`.** `double` gives results like `5.949999...`, which is not acceptable for prices. All amounts are rounded half-up to cents in `MoneyUtils`. The unit price is stored in the order line instead of being recomputed from the drink, because the price depends on the time of the order: a receipt must never change afterwards.

2. **Pricing rules are small interchangeable objects (strategy + decorator).** `HappyHourPricing` and `LateNightPricing` wrap another `PricingStrategy`, so they can be stacked in `Application` (`new HappyHourPricing(new LateNightPricing(new StandardPricing()))`). Adding a new rule (for example a student discount) means adding a class, not editing `OrderService`. The late-night period crosses midnight, which is handled with an "after start **or** before end" test and is covered by tests at 22:59, 23:00, 00:00, 03:59 and 04:00.

3. **An order is validated completely before any stock is touched.** `OrderService.placeOrder` first resolves drinks, checks age and stock, builds the lines, and only then removes stock. Otherwise an order with a valid Lager and an unavailable Cola would leave the Lager removed from stock. Duplicate entries for the same drink are merged first, so that `Mojito: 3` and `mojito: 3` are compared to a stock of 5 as a total of 6 and not as two valid orders of 3.

4. **Two kinds of exceptions on purpose.** Problems with the *request* (unknown drink, no stock, minor) extend the **unchecked** `BarException`: the caller decides whether to handle them. A problem with the *data file* is a **checked** `DataLoadException`: it is an expected failure at start-up, so the compiler forces `Application` to handle it. Bad data stops the program with a clear message including the line number, instead of running with half a menu.

5. **Parsing is separated from file reading.** `DrinkCsvParser` converts one line, `CsvDrinkRepository` reads the file and adds line numbers. This way almost all parsing rules are tested with plain strings, and only a few tests need temporary files.

## Where inheritance is used

`Drink` is the parent of `Cocktail`, `Beer` and `SoftDrink`. The shared state and validation (name, price, alcohol percentage, `isAlcoholic()`, `equals`/`hashCode`) are written once in `Drink`. Each child adds its own data: ingredients for `Cocktail`, volume for `Beer`, a sugar flag for `SoftDrink`. The custom exceptions also form a hierarchy: `OutOfStockException`, `DrinkNotFoundException` and `UnderageCustomerException` all extend `BarException`.

## Where polymorphism is used

- `Drink.getPreparationTimeMinutes()`, `getCategory()` and `describe()` behave differently in each subclass. `Order.getPreparationTimeMinutes()` and `DemoScenario.printMenu()` call them on a `Drink` without knowing which kind it is. A cocktail takes 2 minutes plus 1 per ingredient, a beer or soft drink takes 1.
- `OrderService` uses a `PricingStrategy` variable: the actual price rule is chosen at runtime in `Application`.
- `Application` and `DemoScenario` use `DrinkRepository` and `ReceiptFormatter` through their interfaces.

## Where the abstract class and interfaces are used

| | Where | Why |
|---|---|---|
| Abstract class | `Drink` | A generic drink does not exist on its own; subclasses must define their category and preparation time, but share the common data. |
| Interface | `PricingStrategy` | Extension point for price rules. |
| Interface | `DrinkRepository` | The source of the menu can change (JSON, database) without touching the rest. |
| Interface | `ReceiptFormatter` | The output format can change (HTML, PDF) without touching the rest. |
| Interface | `Preparable` | Capability "can be prepared". Its `default` method `isQuickToPrepare()` is written once for every implementation. |
| Interface | `Mixable` | Capability "is made of ingredients", only for cocktails: a beer must not be forced to answer a question that makes no sense for it. Its `default` method `containsIngredient(...)` is the search rule. |
| Standard interface | `Comparable<Drink>` | Natural order of drinks (by name, ignoring case), used to sort the lines of a receipt. |

**A class implementing several interfaces:** `Drink` implements `Preparable` **and** `Comparable<Drink>`; `Cocktail` extends `Drink` and adds `Mixable`, so a cocktail has three capabilities. This is what a class cannot do with inheritance, because Java allows only one parent class.

## Custom exceptions

| Exception | Type | Raised when |
|---|---|---|
| `BarException` | unchecked, base class | Parent of all request errors below. |
| `DrinkNotFoundException` | unchecked | An ordered drink is not on the menu. |
| `OutOfStockException` | unchecked | Requested quantity is higher than the stock (carries the requested and available amounts). |
| `UnderageCustomerException` | unchecked | A customer under 18 orders an alcoholic drink. |
| `DataLoadException` | **checked** | The data file is missing, unreadable, empty, has a duplicate drink or an invalid line. |

Standard exceptions are used too: `IllegalArgumentException` for invalid values (negative price, quantity 0, blank name...), `IllegalStateException` for cancelling an order twice, `NullPointerException` (via `Objects.requireNonNull`) for missing collaborators, and `IOException`, which is caught and translated into `DataLoadException`. `try/catch` is used in `DrinkCsvParser` (`NumberFormatException`), `CsvDrinkRepository` (`IOException`, plus a `finally` that closes the reader), `DemoScenario` (every refusal, with multi-catch) and `Application` (`DataLoadException`).

## How the application loads its data

`Application` creates a `CsvDrinkRepository` for `src/main/resources/drinks.csv` (or the path given as first argument) and calls `loadAll()`:

1. The file is opened with `Files.newBufferedReader` (UTF-8) and read line by line. An `IOException` (missing file, invalid encoding, a directory instead of a file...) becomes a `DataLoadException`, and a `finally` block always closes the reader, whether the reading succeeded or failed.
2. Blank lines and lines starting with `#` are ignored; the first remaining line is the header.
3. Each other line goes to `DrinkCsvParser`, which creates a `Cocktail`, `Beer` or `SoftDrink` according to the `type` column.
4. A bad line stops the loading with an error such as `Invalid data at line 3: ...`. Duplicate names (ignoring case) and files without any drink are also refused.
5. `Application` then fills `Menu` with the drinks and `Inventory` with the initial stocks.

CSV format:

```
type,name,price,stock,alcohol_percent,details
COCKTAIL,Mojito,8.50,20,10.0,rum|mint|lime|sugar|soda water
BEER,Lager,4.50,40,5.0,33
SOFT_DRINK,Diet Cola,3.00,3,0.0,sugarfree
```

`details` depends on the type: ingredients separated by `|` for a cocktail, volume in cl for a beer, `sugarfree` or `regular` for a soft drink.

## How SOLID principles are applied

- **Single Responsibility.** Each class has one reason to change: `Menu` (catalogue), `Inventory` (stock), `OrderService` (order rules), `ReportService` (statistics), `DrinkCsvParser` (line format), `CsvDrinkRepository` (file access), `TextReceiptFormatter` (display). There is no "God class".
- **Open/Closed.** New price rules, receipt formats or data sources are added as new classes implementing `PricingStrategy`, `ReceiptFormatter` or `DrinkRepository`; `OrderService` and `Application` logic do not change. The one place to edit for a new kind of drink is the `switch` in `DrinkCsvParser`, which is the only code that maps CSV text to classes.
- **Liskov Substitution.** Any `Drink` subclass can be used where a `Drink` is expected: `OrderService` only uses the `Drink` API. Any `PricingStrategy` must return a positive price in cents; a test runs every strategy and combination for every hour of the day to check this contract.
- **Interface Segregation.** Interfaces are tiny (one abstract method each, plus a `default` one for `Preparable` and `Mixable`), so implementers are never forced to write methods they do not need.
- **Dependency Inversion.** `OrderService` depends on the `PricingStrategy` abstraction and receives `Menu`, `Inventory` and the strategy through its constructor. Only `Application` (the composition root) knows the concrete classes and wires them together.

## Java features used

Collections: `List`, `Map` (`TreeMap` with case-insensitive keys, `LinkedHashMap`, `EnumMap`), `Set` (duplicate detection). `Optional` (`Menu.find`, `averageOrderValue`, `busiestHour`). Streams and collectors (`groupingBy`, `summingInt`, `reducing`, `counting`). Lambdas and method references. Enums. `LocalDate`, `LocalTime`, `LocalDateTime`. Generics (`CollectionUtils.topN`). Records for pure data carriers (`MenuEntry`, `DrinkSales`). Switch expressions. `default` methods in interfaces, a class implementing several interfaces, `try/catch/finally` with `BufferedReader`.

## How to run the tests

```bash
mvn test
```

The tests use JUnit 5 (including parameterized tests) and cover the **business logic**, not only getters and setters. They are grouped in four kinds of situations:

- **Normal cases:** an order computes the right total and reduces stock; the right subclass is created from each CSV line.
- **Edge cases:** exactly 18 years old; ordering exactly the remaining stock; prices at 16:59, 17:00, 18:59 and 19:00; late-night period across midnight; rounding of 9.775 to 9.78; the same drink typed twice; a tie between best sellers or busiest hours; reports with no order.
- **Invalid situations:** quantity 0 or negative, empty order, unknown drink, negative price, alcohol above 100%, malformed CSV lines, duplicate drinks in the file.
- **Exceptions:** each custom exception is checked with the data it carries; a refused order leaves stock and history unchanged; cancelling twice does not restore stock twice; a bad CSV line is reported with the right line number.

| Test class | What it checks |
|---|---|
| `OrderServiceTest` | Age, stock, merging, atomicity, cancellation, pricing at order time. |
| `PricingStrategyTest` | Time boundaries, rounding, stacking of rules, contract of every strategy. |
| `ReportServiceTest` | Revenue, average, categories, best sellers, busiest hour, cancelled orders. |
| `InventoryTest`, `MenuTest` | Stock and catalogue rules. |
| `DrinkTest`, `CustomerTest`, `OrderTest` | Polymorphism, natural order and validation in the model. |
| `PreparableTest`, `MixableTest` | The `default` methods, tested alone through lambdas (limits: 1 vs 2 minutes, partial ingredient names, null and blank). |
| `DrinkCsvParserTest`, `CsvDrinkRepositoryTest` | Parsing rules and file loading (with temporary files), including the real `drinks.csv`. |
| `CollectionUtilsTest`, `MoneyUtilsTest`, `TextReceiptFormatterTest` | Helpers and receipt. |
