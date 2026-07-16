package storage;

import accounts.Account;
import accounts.AccountService;
import integration.AppModule;
import integration.MenuUtil;
import java.nio.file.Path;
import java.util.List;
import validation.Validation;

/**
 * Entry point for the Storage module. Implements {@link AppModule} so the
 * Integration layer can select and run this module without knowing about
 * its internals. Owns its own submenu and delegates all real work to the
 * existing storage classes ({@link BudgetStorage}, {@link CsvImporter},
 * {@link CsvExporter}, {@link FileUtil}).
 *
 * <p>Account creation/login is owned by the {@code accounts} package —
 * this module only reads the already-logged-in user from
 * {@link AccountService.SessionManager}.</p>
 *
 * @author Mohammed
 */
public class StorageModule implements AppModule {

    /**
     * Registered module name, kept lowercase per the Integration team's
     * naming convention. Integration's lookup (in {@code registerModule}
     * / {@code dispatchSelection}) needs to match on this lowercase form
     * rather than the raw {@code MenuOptions} constant name.
     */
    private static final String MODULE_NAME = "storage";

    /**
     * Sentinel returned by {@link #promptForYear()} and
     * {@link #promptForMonth()} to signal the user cancelled instead of
     * entering a value. {@code 0} is a safe choice since it's never a
     * valid year ({@link Validation#MIN_YEAR} is 1900) or a valid month
     * (months run 1-12).
     */
    private static final int CANCEL = 0;

    private BudgetStorage budgetStorage;
    private CsvImporter csvImporter;
    private CsvExporter csvExporter;

    /**
     * Constructs a new {@code StorageModule}. No heavy setup here —
     * that belongs in {@link #initialize()}.
     *
     * @author Mohammed
     */
    public StorageModule() {
    }

    /**
     * {@inheritDoc}
     *
     * @return the module name used by Integration's registry
     * @author Mohammed
     */
    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    /**
     * Performs one-time setup: instantiates the helper classes this module
     * depends on and makes sure the data directory exists. Console input is
     * read through {@link MenuUtil}'s shared scanner rather than a
     * module-local one, so there is no {@code Scanner} to set up here.
     *
     * @author Mohammed
     */
    @Override
    public void initialize() {
        budgetStorage = new BudgetStorage();
        csvImporter = new CsvImporter();
        csvExporter = new CsvExporter();
        FileUtil fileUtil = new FileUtil();

        fileUtil.ensureDataDirectoryExists();
    }

    /**
     * Runs the storage module's submenu loop. Called by Integration when
     * the user selects this module from the main menu. Requires that a
     * user is already logged in via the Accounts module.
     *
     * @author Mohammed
     */
    @Override
    public void handleSelection() {
        Account currentUser = AccountService.SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("You must be logged in to use the Storage module.");
            return;
        }
        String username = currentUser.getUsername();

        boolean running = true;
        while (running) {
            String choice = MenuUtil.promptChoice("Storage Module",
                    "1. List budget years",
                    "2. View a budget",
                    "3. Import transactions from CSV",
                    "4. Export transactions to CSV",
                    "5. Delete a budget year",
                    "0. Back to main menu");

            switch (choice) {
                case "1" -> handleListBudgetYears(username);
                case "2" -> handleViewBudget(username);
                case "3" -> handleImportCsv(username);
                case "4" -> handleExportCsv(username);
                case "5" -> handleDeleteBudget(username);
                case "0" -> running = false;
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    // ---- submenu actions ------------------------------------------------

    /**
     * Looks up and prints all budget years on file for the given user.
     *
     * @param username the logged-in user's username
     * @author Mohammed
     */
    private void handleListBudgetYears(String username) {
        List<Integer> years;
        try {
            years = budgetStorage.listYearsForUser(username);
        } catch (RuntimeException e) {
            System.out.println("Could not list budgets: " + e.getMessage());
            return;
        }
        if (years == null || years.isEmpty()) {
            System.out.println("No budgets found for " + username + ".");
            return;
        }
        System.out.println("Years with budgets: " + years);
    }

    /**
     * Prompts for a year and prints every transaction in that year's
     * budget, if one exists, as an aligned table.
     *
     * @bug Previously printed each transaction as a raw
     * {@code date | category | amount} line with no column alignment,
     * no currency symbol, and amounts shown with whatever decimal
     * precision {@code double} happened to produce (e.g. "45.5"). Now
     * prints an aligned table with whole-dollar amounts.
     *
     * @param username the logged-in user's username
     * @author Mohammed
     */
    private void handleViewBudget(String username) {
        int year = promptForYear();
        if (year == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }

        if (!budgetStorage.yearExists(username, year)) {
            System.out.println("No budget found for " + year + ".");
            return;
        }

        Budget budget;
        try {
            budget = budgetStorage.readBudget(username, year);
        } catch (RuntimeException e) {
            System.out.println("Could not read budget for " + year + ": " + e.getMessage());
            return;
        }
        List<Transaction> transactions = budget.getTransactions();
        System.out.println("Budget for " + year + " (" + transactions.size() + " transactions):");

        if (transactions.isEmpty()) {
            return;
        }

        printTransactionTable(transactions);
    }

    /**
     * Prints the given transactions as an aligned table with the date and
     * category columns sized to fit their contents, and whole-dollar,
     * right-aligned amounts.
     *
     * @param transactions the transactions to print
     * @author Mohammed
     */
    private void printTransactionTable(List<Transaction> transactions) {
        int categoryWidth = "Category".length();
        for (Transaction t : transactions) {
            categoryWidth = Math.max(categoryWidth, t.category().length());
        }

        String rowFormat = "  %-10s  %-" + categoryWidth + "s  %10s%n";

        System.out.printf(rowFormat, "Date", "Category", "Amount");
        System.out.println("  " + "-".repeat(10) + "  " + "-".repeat(categoryWidth) + "  " + "-".repeat(10));

        for (Transaction t : transactions) {
            System.out.printf(rowFormat, t.date(), t.category(), formatCurrency(t.amount()));
        }
    }

    /**
     * Formats a monetary amount as a whole-dollar string with a currency
     * symbol and thousands separators, e.g. {@code $1,235} or
     * {@code -$45}.
     *
     * @param amount the amount to format
     * @return the formatted amount
     * @author Mohammed
     */
    private String formatCurrency(double amount) {
        long rounded = Math.round(amount);
        String sign = rounded < 0 ? "-" : "";
        return sign + "$" + String.format("%,d", Math.abs(rounded));
    }

    /**
     * Prompts for a CSV file path, parses and filters its transactions,
     * then merges them into the budget for the year encoded in the file
     * name (creating the budget if it doesn't already exist).
     *
     * @bug Previously prompted for the year separately from the file
     * path, even though the app already requires budget CSV files to be
     * named {@code YYYY.csv} (see {@link Validation#isValidFileName}).
     * That redundant prompt was also the entry point for the "0 means
     * cancel" data-corruption bug documented on {@link #promptForYear()}
     * — the year is now read directly from the file name instead.
     *
     * @param username the logged-in user's username
     * @author Mohammed
     */
    private void handleImportCsv(String username) {
        String filePath = MenuUtil.promptString("Path to CSV file");

        if (!Validation.isValidCsvFile(filePath)) {
            System.out.println("'" + filePath + "' is not a valid budget CSV file. "
                    + "It must be named YYYY.csv, be readable, and start with the header \""
                    + Validation.VALID_HEADER + "\".");
            return;
        }

        List<Transaction> parsed = csvImporter.parseCsvFile(filePath);
        if (parsed == null) {
            System.out.println("Could not parse " + filePath + ".");
            return;
        }

        List<Transaction> valid = csvImporter.filterInvalidRecords(parsed);
        if (valid == null) {
            System.out.println("Could not validate the parsed transactions.");
            return;
        }

        String baseName = Path.of(filePath).getFileName().toString();
        int year = Integer.parseInt(baseName.substring(0, 4));

        try {
            Budget budget = budgetStorage.yearExists(username, year)
                    ? budgetStorage.readBudget(username, year)
                    : new Budget(year);

            for (Transaction t : valid) {
                budget.addTransaction(t);
            }

            if (budgetStorage.yearExists(username, year)) {
                budgetStorage.updateBudget(username, budget);
            } else {
                budgetStorage.createBudget(username, budget);
            }
        } catch (RuntimeException e) {
            System.out.println("Could not import transactions into " + year + ": " + e.getMessage());
            return;
        }

        System.out.println("Imported " + valid.size() + " transactions into " + year + ".");
    }

    /**
     * Prompts for a year, then lets the user export either the entire
     * year's budget or a filtered subset (by month or category) to a
     * CSV file.
     *
     * @bug Previously always exported the entire year's budget verbatim
     * with no way to filter it, which was indistinguishable from just
     * copying the file the user originally imported — {@link Budget}
     * already had working {@link Budget#getTransactionsByMonth(int)}
     * and {@link Budget#getTransactionsByCategory(String)} methods that
     * were never called anywhere. Export now uses them to produce a
     * filtered subset the user's original file couldn't give them.
     *
     * @param username the logged-in user's username
     * @author Mohammed
     */
    private void handleExportCsv(String username) {
        int year = promptForYear();
        if (year == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }

        if (!budgetStorage.yearExists(username, year)) {
            System.out.println("No budget found for " + year + ".");
            return;
        }

        Budget budget;
        try {
            budget = budgetStorage.readBudget(username, year);
        } catch (RuntimeException e) {
            System.out.println("Could not read budget for " + year + ": " + e.getMessage());
            return;
        }

        List<Transaction> toExport = chooseExportSubset(budget);
        if (toExport == null) {
            System.out.println("Cancelled.");
            return;
        }

        if (toExport.isEmpty()) {
            System.out.println("No matching transactions to export.");
            return;
        }

        String filePath = MenuUtil.promptString("Destination file path");
        csvExporter.writeReportToCsv(toExport, filePath);
        System.out.println("Exported " + toExport.size() + " transaction(s) to " + filePath + ".");
    }

    /**
     * Prompts the user to choose whether to export an entire budget or a
     * subset of it filtered by month or category.
     *
     * @param budget the budget to export from
     * @return the selected transactions, or {@code null} if the user
     *         cancelled
     * @author Mohammed
     */
    private List<Transaction> chooseExportSubset(Budget budget) {
        String choice = MenuUtil.promptChoice("Export " + budget.getYear() + " Transactions",
                "1. Entire year",
                "2. A single month",
                "3. A single category",
                "0. Cancel");

        return switch (choice) {
            case "1" -> budget.getTransactions();
            case "2" -> {
                int month = promptForMonth();
                yield month == CANCEL ? null : budget.getTransactionsByMonth(month);
            }
            case "3" -> {
                String category = MenuUtil.promptString("Category to export");
                yield budget.getTransactionsByCategory(category);
            }
            default -> null;
        };
    }

    /**
     * Prompts for a year and, after confirmation, permanently deletes
     * that year's budget.
     *
     * @param username the logged-in user's username
     * @author Mohammed
     */
    private void handleDeleteBudget(String username) {
        int year = promptForYear();
        if (year == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }

        if (!budgetStorage.yearExists(username, year)) {
            System.out.println("No budget found for " + year + ".");
            return;
        }

        boolean confirmed = MenuUtil.promptYesNo(
                "Delete budget for " + year + "? This cannot be undone");
        if (confirmed) {
            try {
                budgetStorage.deleteBudget(username, year);
            } catch (RuntimeException e) {
                System.out.println("Could not delete budget for " + year + ": " + e.getMessage());
                return;
            }
            System.out.println("Deleted budget for " + year + ".");
        } else {
            System.out.println("Cancelled.");
        }
    }

    /**
     * Prompts the user to enter a year, or {@code 0} to cancel.
     *
     * @bug Previously parsed whatever the user typed as a literal year
     * with no way to back out. A user backing out of the (since removed)
     * year prompt in {@link #handleImportCsv(String)} by entering
     * {@code 0} — mirroring the "0 = back" convention used everywhere
     * else in the app's menus — had it silently create and persist a
     * budget for year 0, which then showed up in
     * {@link #handleListBudgetYears(String)}. {@code 0} is now treated
     * as an explicit cancel signal instead of a literal year.
     *
     * @return the year entered by the user, or {@link #CANCEL} if
     *         the user cancelled or entered something unparseable
     * @author Mohammed
     */
    private int promptForYear() {
        String input = MenuUtil.promptString("Enter year (or 0 to cancel)");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid year entered.");
            return CANCEL;
        }
    }

    /**
     * Prompts the user to enter a month (1-12), or {@code 0} to cancel.
     *
     * @return the month entered by the user, or {@link #CANCEL} if the
     *         user cancelled or entered something invalid
     * @author Mohammed
     */
    private int promptForMonth() {
        String input = MenuUtil.promptString("Enter month (1-12, or 0 to cancel)");
        int month;
        try {
            month = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid month entered.");
            return CANCEL;
        }

        if (month != CANCEL && (month < 1 || month > 12)) {
            System.out.println("Month must be between 1 and 12.");
            return CANCEL;
        }

        return month;
    }
}