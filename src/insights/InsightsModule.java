package insights;

import integration.AppModule;
import integration.MenuUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entry point for the Insights module.
 *
 * <p>This class implements {@link AppModule} so the Integration layer
 * can call the Insights module from the main application menu without
 * needing to know how the module works internally.</p>
 *
 * <p>The Insights module owns its own submenu and delegates financial
 * analysis work to {@link InsightsManager}.</p>
 *
 * @author Waliur Sun
 */
public class InsightsModule implements AppModule {

    /** Registered module name used by the Integration registry. */
    private static final String MODULE_NAME = "insights";

    /** Handles insight calculations and report generation. */
    private InsightsManager insightsManager;

    /** Categories that the user wants to exclude from insights. */
    private Set<String> excludedCategories;

    /**
     * Constructs a new InsightsModule.
     *
     * <p>No heavy setup is done in the constructor. Setup belongs in
     * {@link #initialize()}.</p>
     *
     * @author Waliur Sun
     */
    public InsightsModule() {
    }

    /**
     * Returns the unique module name used by Integration.
     *
     * @return the module name
     * @author Waliur Sun
     */
    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    /**
     * Performs one-time setup for the Insights module.
     *
     * <p>This method creates the helper objects needed by the module.</p>
     *
     * @author Waliur Sun
     */
    @Override
    public void initialize() {
        insightsManager = new InsightsManager();
        excludedCategories = new LinkedHashSet<>();
    }

    /**
     * Runs the Insights submenu.
     *
     * <p>This method is called by Integration when the user selects
     * the Insights module from the main menu.</p>
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
                    "1. Generate yearly insights",
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
                        "Invalid option, please try again.");
            }
        }
    }

    /**
     * Generates insights using the current exclusion list.
     *
     * <p>For the Alpha build, this uses demo transactions. When Storage
     * provides real yearly transactions, this method can pass those real
     * transactions into {@link InsightsManager} instead.</p>
     *
     * @author Waliur Sun
     */
    private void handleGenerateInsights() {
        List<String[]> transactions = createAlphaDemoTransactions();

        if (!excludedCategories.isEmpty()) {
            System.out.println();
            System.out.println("Excluded categories applied: "
                    + excludedCategories);
        }

        try {
            insightsManager.displayInsights(
                    transactions,
                    new ArrayList<>(excludedCategories));
        }
        catch (IllegalArgumentException exception) {
            System.out.println("Could not generate insights: "
                    + exception.getMessage());
        }
    }

    /**
     * Lets the user enter categories to exclude.
     *
     * <p>Users may enter one category or several categories separated
     * by commas.</p>
     *
     * @author Waliur Sun
     */
    private void handleAddExcludedCategories() {
        System.out.println();
        System.out.println("Enter categories to exclude.");
        System.out.println("Use commas for multiple categories.");
        System.out.println("Example: Education, Entertainment, Food");

        String input = MenuUtil.promptString("Categories");

        if (input.isEmpty()) {
            System.out.println("No categories entered.");
            return;
        }

        String[] categories = input.split(",");

        for (String category : categories) {
            String cleanedCategory = category.trim();

            if (!cleanedCategory.isEmpty()) {
                excludedCategories.add(cleanedCategory);
                System.out.println(
                        "Category added to excluded list: "
                                + cleanedCategory);
            }
        }
    }

    /**
     * Displays all currently excluded categories.
     *
     * @author Waliur Sun
     */
    private void handleViewExcludedCategories() {
        System.out.println();

        if (excludedCategories.isEmpty()) {
            System.out.println("No categories are currently excluded.");
            return;
        }

        System.out.println("Excluded categories:");

        for (String category : excludedCategories) {
            System.out.println("- " + category);
        }
    }

    /**
     * Clears the excluded category list.
     *
     * @author Waliur Sun
     */
    private void handleClearExcludedCategories() {
        excludedCategories.clear();
        System.out.println("Excluded category list cleared.");
    }

    /**
     * Creates temporary transactions for Alpha build testing.
     *
     * <p>This should be replaced with real Storage module data once
     * the final integration contract is ready.</p>
     *
     * @return demo transaction list
     * @author Waliur Sun
     */
    private List<String[]> createAlphaDemoTransactions() {
        List<String[]> transactions = new ArrayList<>();

        transactions.add(new String[]{"01/15/2025", "Compensation", "3000"});
        transactions.add(new String[]{"01/20/2025", "Food", "-250"});
        transactions.add(new String[]{"02/01/2025", "Home", "-1200"});
        transactions.add(new String[]{"02/15/2025", "Allowance", "500"});
        transactions.add(new String[]{"03/10/2025", "Transportation", "-150"});
        transactions.add(new String[]{"04/05/2025", "Entertainment", "-100"});
        transactions.add(new String[]{"05/01/2025", "Utilities", "-200"});
        transactions.add(new String[]{"06/01/2025", "Education", "-900"});

        return transactions;
    }

    /**
     * Makes sure initialize was called before the module runs.
     *
     * @author Waliur Sun
     */
    private void ensureInitialized() {
        if (insightsManager == null || excludedCategories == null) {
            initialize();
        }
    }
}