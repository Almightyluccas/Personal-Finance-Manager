package insights;

import accounts.Account;
import accounts.AccountService;
import integration.AppModule;
import integration.MenuUtil;
import storage.Budget;
import storage.BudgetStorage;
import storage.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entry point for the Insights module.
 *
 * <p>This class implements {@link AppModule} so the Integration layer
 * can invoke the Insights module without needing to know how its internal
 * calculations work.</p>
 *
 * <p>The module retrieves real budget data from Storage, allows the user
 * to select a budget year, manages excluded categories, and delegates
 * financial analysis to {@link InsightsManager}.</p>
 *
 * @author Waliur Sun
 * @author Adrian Singh
 * @author Felix Santos
 */
public class InsightsModule implements AppModule {

    /** Module name registered with the Integration layer. */
    private static final String MODULE_NAME = "insights";

    /** Date format used by the Personal Finance Manager CSV files. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /** Performs insight calculations and report generation. */
    private InsightsManager insightsManager;

    /** Retrieves saved budgets and transactions. */
    private BudgetStorage budgetStorage;

    /** Categories the user has chosen to exclude from analysis. */
    private Set<String> excludedCategories;

    /**
     * Constructs a new Insights module.
     *
     * <p>Module dependencies are initialized in {@link #initialize()}.</p>
     *
     * @author Waliur Sun
     */
    public InsightsModule() {
    }

    /**
     * Returns the unique name used by the Integration module registry.
     *
     * @return the lowercase module name
     * @author Adrian Singh
     */
    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    /**
     * Performs one-time setup for the Insights module.
     *
     * @author Adrian Singh
     * @author Waliur Sun
     */
    @Override
    public void initialize() {
        insightsManager = new InsightsManager();
        budgetStorage = new BudgetStorage();
        excludedCategories = new LinkedHashSet<>();
    }

    /**
     * Displays and controls the Insights submenu.
     *
     * <p>This method is called by Integration when the user selects
     * Insights from the main menu.</p>
     *
     * @author Waliur Sun
     */
    @Override
    public void handleSelection() {
        ensureInitialized();

        boolean running = true;

        while (running) {
            String choice = MenuUtil.promptChoice(
                    "Insights Module",
                    "1. Generate insights for a budget year",
                    "2. Add categories to exclude",
                    "3. View excluded categories",
                    "4. Clear excluded categories",
                    "0. Back to main menu"
            );

            switch (choice) {
                case "1" -> handleGenerateInsights();
                case "2" -> handleAddExcludedCategories();
                case "3" -> handleViewExcludedCategories();
                case "4" -> handleClearExcludedCategories();
                case "0" -> running = false;
                default -> System.out.println(
                        "Invalid option. Please select one of the displayed choices.");
            }
        }
    }

    /**
     * Generates insights using the logged-in user's real stored budget data.
     *
     * <p>The user selects one of the budget years available in Storage.
     * The stored transactions are converted into the format expected by
     * {@link InsightsManager}.</p>
     *
     * @author Waliur Sun
     * @author Felix Santos
     */
    private void handleGenerateInsights() {
        Account currentUser =
                AccountService.SessionManager.getCurrentUser();

        if (currentUser == null) {
            System.out.println(
                    "You must be logged in before generating financial insights.");
            return;
        }

        String username = currentUser.getUsername();

        List<Integer> availableYears =
                budgetStorage.listYearsForUser(username);

        if (availableYears == null || availableYears.isEmpty()) {
            System.out.println(
                    "No saved budget years were found for " + username + ".");
            System.out.println(
                    "Please import a budget file before using Insights.");
            return;
        }

        Integer selectedYear = promptForBudgetYear(availableYears);

        if (selectedYear == null) {
            return;
        }

        Budget budget =
                budgetStorage.readBudget(username, selectedYear);

        if (budget == null) {
            System.out.println(
                    "The budget for " + selectedYear
                            + " could not be loaded.");
            return;
        }

        List<Transaction> storedTransactions =
                budget.getTransactions();

        if (storedTransactions == null || storedTransactions.isEmpty()) {
            System.out.println(
                    "The budget for " + selectedYear
                            + " does not contain any transactions.");
            return;
        }

        try {
            List<String[]> insightTransactions =
                    convertTransactions(storedTransactions);

            System.out.println();
            System.out.println(
                    "Generating financial insights for "
                            + selectedYear + "...");

            if (!excludedCategories.isEmpty()) {
                System.out.println(
                        "Excluded categories: " + excludedCategories);
            }

            insightsManager.displayInsights(
                    insightTransactions,
                    new ArrayList<>(excludedCategories));

        } catch (IllegalArgumentException exception) {
            System.out.println(
                    "Could not generate insights: "
                            + exception.getMessage());
        }
    }

    /**
     * Prompts the user to select one of their available budget years.
     *
     * @param availableYears years found by the Storage module
     * @return selected year, or {@code null} if the user goes back
     * @author Waliur Sun
     */
    private Integer promptForBudgetYear(
            List<Integer> availableYears) {

        List<String> options = new ArrayList<>();

        for (int index = 0;
                index < availableYears.size();
                index++) {

            options.add(
                    (index + 1)
                            + ". "
                            + availableYears.get(index));
        }

        options.add("0. Back to Insights menu");

        while (true) {
            String choice = MenuUtil.promptChoice(
                    "Select Budget Year",
                    options.toArray(new String[0]));

            if ("0".equals(choice)) {
                return null;
            }

            try {
                int selectedIndex =
                        Integer.parseInt(choice) - 1;

                if (selectedIndex >= 0
                        && selectedIndex < availableYears.size()) {

                    return availableYears.get(selectedIndex);
                }

            } catch (NumberFormatException exception) {
                // The error message below handles nonnumeric input.
            }

            System.out.println(
                    "Invalid selection. Please choose one of the displayed years.");
        }
    }

    /**
     * Converts Storage transaction objects into the transaction-row format
     * used by the Insights calculation classes.
     *
     * <p>Storage currently represents amounts as doubles, while the project
     * contract specifies whole-dollar amounts. Therefore, amounts are rounded
     * to the nearest whole dollar before analysis.</p>
     *
     * @param transactions Storage transaction records
     * @return transaction rows containing Date, Category, and Amount
     * @author Waliur Sun
     */
    private List<String[]> convertTransactions(
            List<Transaction> transactions) {

        List<String[]> convertedTransactions =
                new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (transaction == null) {
                throw new IllegalArgumentException(
                        "A stored transaction is null.");
            }

            if (transaction.date() == null) {
                throw new IllegalArgumentException(
                        "A stored transaction is missing its date.");
            }

            if (transaction.category() == null
                    || transaction.category().isBlank()) {

                throw new IllegalArgumentException(
                        "A stored transaction is missing its category.");
            }

            long roundedAmount =
                    Math.round(transaction.amount());

            if (roundedAmount > Integer.MAX_VALUE
                    || roundedAmount < Integer.MIN_VALUE) {

                throw new IllegalArgumentException(
                        "A transaction amount is outside the supported range.");
            }

            convertedTransactions.add(
                    new String[]{
                            transaction.date().format(DATE_FORMATTER),
                            transaction.category().trim(),
                            Long.toString(roundedAmount)
                    });
        }

        return convertedTransactions;
    }

    /**
     * Prompts the user to enter one or more categories to exclude.
     *
     * @author Felix Santos
     * @author Waliur Sun
     */
    private void handleAddExcludedCategories() {
        System.out.println();
        System.out.println(
                "Enter one category or several categories separated by commas.");
        System.out.println(
                "Example: Education, Entertainment, Food");

        String input =
                MenuUtil.promptString("Categories to exclude");

        if (input.isBlank()) {
            System.out.println("No categories were entered.");
            return;
        }

        String[] categories = input.split(",");

        int addedCount = 0;

        for (String category : categories) {
            String cleanedCategory = category.trim();

            if (!cleanedCategory.isEmpty()
                    && addExcludedCategory(cleanedCategory)) {

                addedCount++;
                System.out.println(
                        "Excluded category added: "
                                + cleanedCategory);
            }
        }

        if (addedCount == 0) {
            System.out.println(
                    "No new categories were added.");
        }
    }

    /**
     * Adds a category without creating case-insensitive duplicates.
     *
     * @param category category to exclude
     * @return true if the category was added
     * @author Waliur Sun
     */
    private boolean addExcludedCategory(String category) {
        for (String existingCategory : excludedCategories) {
            if (existingCategory.equalsIgnoreCase(category)) {
                return false;
            }
        }

        return excludedCategories.add(category);
    }

    /**
     * Displays the categories currently excluded from analysis.
     *
     * @author Waliur Sun
     * @author Adrian Singh
     */
    private void handleViewExcludedCategories() {
        if (excludedCategories.isEmpty()) {
            System.out.println(
                    "No categories are currently excluded.");
            return;
        }

        MenuUtil.printTitle("Excluded Categories");

        for (String category : excludedCategories) {
            System.out.println("- " + category);
        }
    }

    /**
     * Clears every category from the exclusion list after confirmation.
     *
     * @author Waliur Sun
     */
    private void handleClearExcludedCategories() {
        if (excludedCategories.isEmpty()) {
            System.out.println(
                    "The excluded category list is already empty.");
            return;
        }

        boolean confirmed =
                MenuUtil.promptYesNo(
                        "Clear all excluded categories?");

        if (confirmed) {
            excludedCategories.clear();
            System.out.println(
                    "Excluded category list cleared.");
        } else {
            System.out.println(
                    "The excluded category list was not changed.");
        }
    }

    /**
     * Ensures that the module has been initialized before use.
     *
     * @author Waliur Sun
     */
    private void ensureInitialized() {
        if (insightsManager == null
                || budgetStorage == null
                || excludedCategories == null) {

            initialize();
        }
    }
}