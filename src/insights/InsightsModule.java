package insights;

import accounts.Account;
import accounts.AccountService;
import integration.AppModule;
import integration.MenuUtil;
import storage.Budget;
import storage.BudgetStorage;
import storage.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Entry point for the Insights module.
 *
 * <p>This class retrieves real budget data from Storage, allows users
 * to select a budget year, and lets users choose which expense categories
 * should receive category-specific financial recommendations.</p>
 *
 * <p>Category selections affect recommendations only. They do not change
 * total income, total expenses, net balance, monthly totals, or the
 * categories displayed in the financial report.</p>
 *
 * @author Waliur Sun
 * @author Adrian Singh
 * @author Felix Santos
 */
public class InsightsModule implements AppModule {

    /** Module name registered with the Integration layer. */
    private static final String MODULE_NAME = "insights";

    /** Index of the category field in an Insights transaction row. */
    private static final int CATEGORY_INDEX = 1;

    /** Index of the amount field in an Insights transaction row. */
    private static final int AMOUNT_INDEX = 2;

    /** Date format used by the Personal Finance Manager CSV files. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /** Performs insight calculations and report generation. */
    private InsightsManager insightsManager;

    /** Retrieves saved budgets and transactions. */
    private BudgetStorage budgetStorage;

    /**
     * Stores the information selected by the user for one analysis.
     *
     * @param year selected budget year
     * @param transactions converted transaction rows
     * @param excludedCategories categories excluded from recommendations
     * @param availableCategories all expense categories
     * @param selectedCategories categories selected for recommendations
     * @author Waliur Sun
     */
    private record AnalysisContext(
            int year,
            List<String[]> transactions,
            List<String> excludedCategories,
            List<String> availableCategories,
            Set<String> selectedCategories) {
    }

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
     * @return lowercase module name
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
    }

    /**
     * Displays and controls the Insights submenu.
     *
     * @author Waliur Sun
     */
    @Override
    public void handleSelection() {

        ensureInitialized();

        boolean running = true;

        while (running) {

            String choice =
                    MenuUtil.promptChoice(
                            "Insights Module",
                            "1. Generate insights for a budget year",
                            "2. Export insights report to CSV",
                            "0. Back to main menu"
                    );

            switch (choice) {

                case "1" -> handleGenerateInsights();

                case "2" -> handleExportInsights();

                case "0" -> running = false;

                default -> System.out.println(
                        "Invalid option. Please select one of "
                                + "the displayed choices.");
            }
        }
    }

    /**
     * Generates and displays an Insights report.
     *
     * @author Waliur Sun
     * @author Felix Santos
     */
    private void handleGenerateInsights() {

        try {

            AnalysisContext context =
                    prepareAnalysisContext();

            if (context == null) {
                return;
            }

            System.out.println();
            System.out.println(
                    "Generating financial insights for "
                            + context.year()
                            + "...");

            displayRecommendationSelection(
                    context.availableCategories(),
                    context.selectedCategories());

            insightsManager.displayInsights(
                    context.transactions(),
                    context.excludedCategories());

        } catch (IllegalArgumentException exception) {

            System.out.println(
                    "Could not generate insights: "
                            + exception.getMessage());
        }
    }

    /**
     * Exports an Insights report to a CSV file.
     *
     * <p>The user selects the budget year and recommendation categories.
     * The method asks for a destination filename and warns before replacing
     * an existing file.</p>
     *
     * @author Waliur Sun
     */
    private void handleExportInsights() {

        try {

            AnalysisContext context =
                    prepareAnalysisContext();

            if (context == null) {
                return;
            }

            String defaultFileName =
                    "insights-report-"
                            + context.year()
                            + ".csv";

            System.out.println();
            System.out.println(
                    "Enter a CSV filename or file path.");

            System.out.println(
                    "Press Enter to use: "
                            + defaultFileName);

            String enteredPath =
                    MenuUtil.promptString(
                            "Export file");

            String filePath;

            if (enteredPath == null
                    || enteredPath.isBlank()) {

                filePath =
                        defaultFileName;

            } else {

                filePath =
                        enteredPath.trim();

                if (!filePath.toLowerCase(Locale.ROOT)
                        .endsWith(".csv")) {

                    filePath += ".csv";
                }
            }

            Path outputPath =
                    Path.of(filePath)
                            .toAbsolutePath()
                            .normalize();

            if (Files.exists(outputPath)) {

                boolean overwrite =
                        MenuUtil.promptYesNo(
                                "The file already exists. Overwrite it?");

                if (!overwrite) {
                    System.out.println(
                            "Report export canceled.");

                    return;
                }
            }

            insightsManager.exportInsights(
                    context.transactions(),
                    outputPath.toString(),
                    context.excludedCategories());

            System.out.println();
            System.out.println(
                    "Insights report exported successfully.");

            System.out.println(
                    "Saved to: "
                            + outputPath);

        } catch (IOException exception) {

            System.out.println(
                    "Could not export the Insights report: "
                            + exception.getMessage());

        } catch (IllegalArgumentException
                | SecurityException exception) {

            System.out.println(
                    "Could not export the Insights report: "
                            + exception.getMessage());
        }
    }

    /**
     * Loads a selected budget and gathers recommendation category choices.
     *
     * @return selected analysis information, or {@code null} when canceled
     * @author Waliur Sun
     */
    private AnalysisContext prepareAnalysisContext() {

        Account currentUser =
                AccountService.SessionManager.getCurrentUser();

        if (currentUser == null) {

            System.out.println(
                    "You must be logged in before using "
                            + "financial insights.");

            return null;
        }

        String username =
                currentUser.getUsername();

        List<Integer> availableYears =
                budgetStorage.listYearsForUser(
                        username);

        if (availableYears == null
                || availableYears.isEmpty()) {

            System.out.println(
                    "No saved budget years were found for "
                            + username
                            + ".");

            System.out.println(
                    "Please import a budget file before "
                            + "using Insights.");

            return null;
        }

        Integer selectedYear =
                promptForBudgetYear(
                        availableYears);

        if (selectedYear == null) {
            return null;
        }

        Budget budget =
                budgetStorage.readBudget(
                        username,
                        selectedYear);

        if (budget == null) {

            System.out.println(
                    "The budget for "
                            + selectedYear
                            + " could not be loaded.");

            return null;
        }

        List<Transaction> storedTransactions =
                budget.getTransactions();

        if (storedTransactions == null
                || storedTransactions.isEmpty()) {

            System.out.println(
                    "The budget for "
                            + selectedYear
                            + " does not contain any transactions.");

            return null;
        }

        List<String[]> insightTransactions =
                convertTransactions(
                        storedTransactions);

        List<String> availableCategories =
                extractExpenseCategories(
                        insightTransactions);

        Set<String> selectedCategories =
                promptForRecommendationCategories(
                        availableCategories);

        if (selectedCategories == null) {
            return null;
        }

        List<String> excludedCategories =
                determineExcludedCategories(
                        availableCategories,
                        selectedCategories);

        return new AnalysisContext(
                selectedYear,
                insightTransactions,
                excludedCategories,
                availableCategories,
                selectedCategories);
    }

    /**
     * Prompts the user to select one available budget year.
     *
     * @param availableYears years found by the Storage module
     * @return selected year, or {@code null} when the user goes back
     * @author Waliur Sun
     */
    private Integer promptForBudgetYear(
            List<Integer> availableYears) {

        List<String> options =
                new ArrayList<>();

        for (int index = 0;
                index < availableYears.size();
                index++) {

            options.add(
                    (index + 1)
                            + ". "
                            + availableYears.get(index));
        }

        options.add(
                "0. Back to Insights menu");

        while (true) {

            String choice =
                    MenuUtil.promptChoice(
                            "Select Budget Year",
                            options.toArray(
                                    new String[0]));

            if ("0".equals(choice)) {
                return null;
            }

            try {

                int selectedIndex =
                        Integer.parseInt(choice) - 1;

                if (selectedIndex >= 0
                        && selectedIndex
                        < availableYears.size()) {

                    return availableYears.get(
                            selectedIndex);
                }

            } catch (NumberFormatException exception) {
                // The message below handles invalid input.
            }

            System.out.println(
                    "Invalid selection. Please choose one "
                            + "of the displayed years.");
        }
    }

    /**
     * Prompts the user to choose which expense categories should receive
     * category-specific recommendations.
     *
     * @param availableCategories expense categories in the selected year
     * @return selected categories, an empty set for no category-specific
     *         recommendations, or {@code null} when the user goes back
     * @author Waliur Sun
     */
    private Set<String> promptForRecommendationCategories(
            List<String> availableCategories) {

        if (availableCategories.isEmpty()) {

            System.out.println();
            System.out.println(
                    "This budget does not contain any "
                            + "expense categories.");

            System.out.println(
                    "The report will contain general "
                            + "recommendations only.");

            return new LinkedHashSet<>();
        }

        while (true) {

            String choice =
                    MenuUtil.promptChoice(
                            "Recommendation Categories",
                            "1. Use all expense categories",
                            "2. Choose specific categories",
                            "3. No category-specific recommendations",
                            "0. Back to Insights menu"
                    );

            switch (choice) {

                case "1":
                    return new LinkedHashSet<>(
                            availableCategories);

                case "2":
                    return promptForSpecificCategories(
                            availableCategories);

                case "3":
                    return new LinkedHashSet<>();

                case "0":
                    return null;

                default:
                    System.out.println(
                            "Invalid option. Please select one "
                                    + "of the displayed choices.");
            }
        }
    }

    /**
     * Displays a numbered category-selection menu.
     *
     * <p>Selecting a category toggles it on or off. The user may continue
     * selecting categories until choosing Done.</p>
     *
     * @param availableCategories expense categories in the selected budget
     * @return selected categories, or {@code null} when the user goes back
     * @author Waliur Sun
     */
    private Set<String> promptForSpecificCategories(
            List<String> availableCategories) {

        Set<String> selectedCategories =
                new LinkedHashSet<>();

        while (true) {

            List<String> options =
                    new ArrayList<>();

            for (int index = 0;
                    index < availableCategories.size();
                    index++) {

                String category =
                        availableCategories.get(index);

                String marker =
                        containsIgnoreCase(
                                selectedCategories,
                                category)
                                ? "[X] "
                                : "[ ] ";

                options.add(
                        (index + 1)
                                + ". "
                                + marker
                                + category);
            }

            options.add(
                    "0. Done");

            int backChoiceNumber =
                    availableCategories.size() + 1;

            options.add(
                    backChoiceNumber
                            + ". Back without saving selection");

            String choice =
                    MenuUtil.promptChoice(
                            "Choose Recommendation Categories",
                            options.toArray(
                                    new String[0]));

            if ("0".equals(choice)) {

                if (selectedCategories.isEmpty()) {

                    System.out.println(
                            "Select at least one category, "
                                    + "or choose Back.");

                    continue;
                }

                return selectedCategories;
            }

            try {

                int selectedNumber =
                        Integer.parseInt(choice);

                if (selectedNumber
                        == backChoiceNumber) {

                    return null;
                }

                int selectedIndex =
                        selectedNumber - 1;

                if (selectedIndex >= 0
                        && selectedIndex
                        < availableCategories.size()) {

                    String category =
                            availableCategories.get(
                                    selectedIndex);

                    toggleCategory(
                            selectedCategories,
                            category);

                    continue;
                }

            } catch (NumberFormatException exception) {
                // The message below handles invalid input.
            }

            System.out.println(
                    "Invalid selection. Please choose one "
                            + "of the displayed categories.");
        }
    }

    /**
     * Adds or removes one category from the current selection.
     *
     * @param selectedCategories current category selection
     * @param category category to toggle
     * @author Waliur Sun
     */
    private void toggleCategory(
            Set<String> selectedCategories,
            String category) {

        String existingCategory =
                findIgnoreCase(
                        selectedCategories,
                        category);

        if (existingCategory == null) {

            selectedCategories.add(
                    category);

            System.out.println(
                    "Selected: "
                            + category);

        } else {

            selectedCategories.remove(
                    existingCategory);

            System.out.println(
                    "Removed: "
                            + category);
        }
    }

    /**
     * Extracts unique expense categories from the transaction rows.
     *
     * @param transactions converted Insights transaction rows
     * @return alphabetically ordered expense categories
     * @author Waliur Sun
     */
    private List<String> extractExpenseCategories(
            List<String[]> transactions) {

        Set<String> categories =
                new TreeSet<>(
                        String.CASE_INSENSITIVE_ORDER);

        for (String[] transaction
                : transactions) {

            int amount =
                    Integer.parseInt(
                            transaction[AMOUNT_INDEX]);

            if (amount < 0) {

                categories.add(
                        transaction[CATEGORY_INDEX]);
            }
        }

        return new ArrayList<>(
                categories);
    }

    /**
     * Determines which categories should be excluded from recommendations.
     *
     * @param availableCategories all expense categories
     * @param selectedCategories categories selected for recommendations
     * @return categories that should not receive recommendations
     * @author Waliur Sun
     */
    private List<String> determineExcludedCategories(
            List<String> availableCategories,
            Set<String> selectedCategories) {

        List<String> excludedCategories =
                new ArrayList<>();

        for (String category
                : availableCategories) {

            if (!containsIgnoreCase(
                    selectedCategories,
                    category)) {

                excludedCategories.add(
                        category);
            }
        }

        return excludedCategories;
    }

    /**
     * Displays the categories selected for recommendations.
     *
     * @param availableCategories all expense categories
     * @param selectedCategories selected recommendation categories
     * @author Waliur Sun
     */
    private void displayRecommendationSelection(
            List<String> availableCategories,
            Set<String> selectedCategories) {

        if (availableCategories.isEmpty()
                || selectedCategories.isEmpty()) {

            System.out.println(
                    "Category-specific recommendations: None");

            return;
        }

        if (selectedCategories.size()
                == availableCategories.size()) {

            System.out.println(
                    "Category-specific recommendations: "
                            + "All categories");

            return;
        }

        System.out.println(
                "Recommendation categories: "
                        + selectedCategories);
    }

    /**
     * Converts Storage transactions into the format required by Insights.
     *
     * @param transactions Storage transaction records
     * @return rows containing Date, Category, and Amount
     * @author Waliur Sun
     */
    private List<String[]> convertTransactions(
            List<Transaction> transactions) {

        List<String[]> convertedTransactions =
                new ArrayList<>();

        for (Transaction transaction
                : transactions) {

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

            if (!Double.isFinite(
                    transaction.amount())) {

                throw new IllegalArgumentException(
                        "A stored transaction contains "
                                + "an invalid amount.");
            }

            long roundedAmount =
                    Math.round(
                            transaction.amount());

            if (roundedAmount > Integer.MAX_VALUE
                    || roundedAmount < Integer.MIN_VALUE) {

                throw new IllegalArgumentException(
                        "A transaction amount is outside "
                                + "the supported range.");
            }

            convertedTransactions.add(
                    new String[]{
                            transaction.date()
                                    .format(DATE_FORMATTER),

                            transaction.category()
                                    .trim(),

                            Long.toString(
                                    roundedAmount)
                    });
        }

        return convertedTransactions;
    }

    /**
     * Determines whether a set contains a category without regard to case.
     *
     * @param categories category set
     * @param category category to find
     * @return true when a matching category exists
     * @author Waliur Sun
     */
    private boolean containsIgnoreCase(
            Set<String> categories,
            String category) {

        return findIgnoreCase(
                categories,
                category) != null;
    }

    /**
     * Finds the stored spelling of a category without regard to case.
     *
     * @param categories category set
     * @param category category to find
     * @return matching stored category, or {@code null}
     * @author Waliur Sun
     */
    private String findIgnoreCase(
            Set<String> categories,
            String category) {

        for (String existingCategory
                : categories) {

            if (existingCategory.equalsIgnoreCase(
                    category)) {

                return existingCategory;
            }
        }

        return null;
    }

    /**
     * Ensures the module is initialized before use.
     *
     * @author Waliur Sun
     */
    private void ensureInitialized() {

        if (insightsManager == null
                || budgetStorage == null) {

            initialize();
        }
    }
}