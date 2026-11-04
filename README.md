# Cocktail Bar
Cocktail Bar is a small point-of-sale programme for a bar. It loads the menu and stock levels from a file. It takes
orders in accordance with the bar’s rules, then displays receipts and a sales summary.

## Ce qu'il fait concrètement : 
* Loads the menu and initial stock levels from drinks.csv.
* Takes and checks orders. The drink must be available, alcohol is not served to under-18s, and there
  must be sufficient stock.
* Calculates the price according to the time of day. Cocktails must be 30 per cent off from 5 pm to 7 pm and a 15 per cent surcharge on all drinks
  from 11 pm to 4 am.
* Prints a receipt showing the total and the preparation time.
* Cancels an order and returns the drinks to stock.
* Generates a report showing turnover, average basket value, sales by category, best-sellers, the busiest
* hour, and drinks that are running low.
* Finds cocktails containing a specific ingredient.

## Le dossier cocktail-bar
The cocktail-bar folder is a Maven project as it contains everything needed to compile, run and test the application.
We will use the following files:
* pom.xml: the project’s ‘identity card’, containing the name, Java version, test library and the run command `mvn compile exec:java`
* .gitignore: a list of files that Git should not track
* src/main/java/com/example/cocktailbar/: the application code
* src/main/resources/drinks.csv: the drinks menu and initial stock
* src/test/java/com/example/cocktailbar/: the automated tests, organised in the same way as the code they test
* target/: compiled files, created by Maven

com/example/cocktailbar/ is the code package. Inside, you’ll find the following files:
* model, which answers the question: What are we selling and who’s ordering?
* pricing, which answers the question: How much does it cost at this time of day?
* service, which answers the question: What are the bar’s rules?
* data, which answers the question: Where does the data come from?
* display, which answers the question: How do we display it?
* exception, which answers the question: What errors might we encounter?
* utils, which answers the question: What tools are shared?

The tree structure looks like the diagram below:
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
## Parcours d'une commande, étape par étape 
1. _Application_ read _drinks.csv_ with _CsvDrinkRepository_, fills _Menu_ and  _Inventory_ then calls up the pricing rules _OrderService_.
2. _DemoScenario_ sends a command to _OrderService_ with information : client, drink with quantity and hour.
3. _OrderService_ search drinks in _Menu_, check _Customer_'s age and check stck with _Inventory_.
4. For each line, it queries the pricing rule for the price which takes the time of day into account.
5. If everything is in order, it deducts the stock and creates an _Order_ with its _OrderLine_ items. At the slightest rejection, it throws an exception (_DrinkNotFoundException_, _UnderageCustomerException_ or _OutOfStockException)_ and nothing is updated. 
6. _TextReceiptFormatter_ converts the _Order_ into a receipt. At the end of the evening, _ReportService_ calculates the statistics and _ReportFormatter_ formats them.

Each class performs a single task and only interacts with interfaces. You can change the data source, a pricing rule or the receipt format without altering _OrderService_.