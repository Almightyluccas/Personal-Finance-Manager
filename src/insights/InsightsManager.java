package insights;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main controller for the Insights module.
 *
 * <p>This class coordinates all calculations performed by the
 * Insights module and serves as the entry point used by the
 * Integration Team.</p>
 *
 * @author Adrian Singh
 * @author Felix Santos
 * @author Waliur Sun
 */
public class InsightsManager {

    /** Index of the date column in a transaction row. */
    private static final int DATE_INDEX = 0;

    /** Index of the category column in a transaction row. */
    private static final int CATEGORY_INDEX = 1;

    /** Strict formatter for transaction dates in MM/DD/YYYY format. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /** Performs overall budget calculations. */
    private final BudgetStatistics budgetStatistics;

    /** Performs spending analysis. */
    private final SpendingAnalyzer spendingAnalyzer;

    /** Generates financial recommendations. */
    private final RecommendationEngine recommendationEngine;

    /** Formats and exports reports. */
    private final InsightReport insightReport;

    /**
     * Creates a new InsightsManager and initializes its helper classes.
     *
     * @author Waliur Sun
     */
    public InsightsManager() {
        budgetStatistics = new BudgetStatistics();
        spendingAnalyzer = new SpendingAnalyzer();
        recommendationEngine = new RecommendationEngine();
        insightReport = new InsightReport();
    }

    /**
     * Generates a complete yearly insight result without excluding categories.
     *
     * @param transactions yearly transaction list
     * @return completed insight result
     * @author Waliur Sun
     */
    public InsightResult generateInsights(
            List<String[]> transactions) {

        return generateInsights(
                transactions,
                new ArrayList<>());
    }

    /**
     * Generates a complete yearly insight result while excluding selected
     * categories.
     *
     * <p>All original transactions are validated to ensure they belong
     * to the same calendar year. Excluded categories are then removed
     * before financial calculations are performed.</p>
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories that should not be analyzed
     * @return completed insight result
     * @author Waliur Sun
     */
    public InsightResult generateInsights(
            List<String[]> transactions,
            List<String> excludedCategories) {

        validateTransactions(transactions);

        /*
         * Validate the original list before applying category exclusions.
         * This prevents an excluded transaction from hiding a different year.
         */
        int year = extractYear(transactions);

        List<String[]> filteredTransactions =
                filterExcludedCategories(
                        transactions,
                        excludedCategories);

        if (filteredTransactions.isEmpty()) {
            throw new IllegalArgumentException(
                    "No transactions remain after excluding "
                            + "the selected categories.");
        }

        /*
         * Calculate income and expenses in one traversal.
         */
        BudgetStatistics.BudgetTotals totals =
                budgetStatistics.calculateTotals(
                        filteredTransactions);

        int totalIncome =
                totals.totalIncome();

        int totalExpenses =
                totals.totalExpenses();

        int netBalance =
                budgetStatistics.calculateNetBalance(
                        totalIncome,
                        totalExpenses);

        BudgetStatus budgetStatus =
                budgetStatistics.determineBudgetStatus(
                        netBalance);

        Map<Integer, Integer> monthlyTotals =
                spendingAnalyzer.calculateMonthlyTotals(
                        filteredTransactions);

        Map<String, Integer> categoryTotals =
                spendingAnalyzer.calculateCategoryTotals(
                        filteredTransactions);

        Map<String, Double> categoryPercentages =
                spendingAnalyzer.calculateCategoryPercentages(
                        categoryTotals,
                        totalExpenses);

        double averageMonthlySpending =
                spendingAnalyzer.calculateAverageMonthlySpending(
                        totalExpenses);

        List<String> recommendations =
                recommendationEngine.generateRecommendations(
                        budgetStatus,
                        netBalance,
                        categoryPercentages);

        return new InsightResult(
                year,
                totalIncome,
                totalExpenses,
                netBalance,
                budgetStatus,
                monthlyTotals,
                categoryTotals,
                categoryPercentages,
                averageMonthlySpending,
                recommendations);
    }

    /**
     * Creates a formatted yearly report without excluding categories.
     *
     * @param transactions yearly transaction list
     * @return formatted report text
     * @author Felix Santos
     * @author Waliur Sun
     */
    public String analyzeYear(
            List<String[]> transactions) {

        return analyzeYear(
                transactions,
                new ArrayList<>());
    }

    /**
     * Creates a formatted yearly report while excluding selected categories.
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories that should not be analyzed
     * @return formatted report text
     * @author Felix Santos
     * @author Waliur Sun
     * @author Adrian Singh
     */
    public String analyzeYear(
            List<String[]> transactions,
            List<String> excludedCategories) {

        InsightResult result =
                generateInsights(
                        transactions,
                        excludedCategories);

        return insightReport.generateSummary(result);
    }

    /**
     * Prints the insight report without excluding categories.
     *
     * @param transactions yearly transaction list
     * @author Waliur Sun
     */
    public void displayInsights(
            List<String[]> transactions) {

        displayInsights(
                transactions,
                new ArrayList<>());
    }

    /**
     * Prints the insight report while excluding selected categories.
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories that should not be analyzed
     * @author Waliur Sun
     */
    public void displayInsights(
            List<String[]> transactions,
            List<String> excludedCategories) {

        InsightResult result =
                generateInsights(
                        transactions,
                        excludedCategories);

        insightReport.printReport(result);
    }

    /**
     * Exports the insight report without excluding categories.
     *
     * @param transactions yearly transaction list
     * @param filePath destination CSV file path
     * @throws IOException if the report cannot be written
     * @author Felix Santos
     * @author Waliur Sun
     */
    public void exportInsights(
            List<String[]> transactions,
            String filePath)
            throws IOException {

        exportInsights(
                transactions,
                filePath,
                new ArrayList<>());
    }

    /**
     * Exports the insight report while excluding selected categories.
     *
     * @param transactions yearly transaction list
     * @param filePath destination CSV file path
     * @param excludedCategories categories that should not be analyzed
     * @throws IOException if the report cannot be written
     * @author Felix Santos
     * @author Waliur Sun
     * @author Adrian Singh
     */
    public void exportInsights(
            List<String[]> transactions,
            String filePath,
            List<String> excludedCategories)
            throws IOException {

        InsightResult result =
                generateInsights(
                        transactions,
                        excludedCategories);

        insightReport.saveReportToCSV(
                result,
                filePath);
    }

    /**
     * Removes transactions whose categories appear in the exclusion list.
     *
     * <p>Category comparisons are case-insensitive and ignore surrounding
     * spaces.</p>
     *
     * @param transactions original transaction list
     * @param excludedCategories categories that should not be analyzed
     * @return filtered transaction list
     * @author Adrian Singh
     * @author Waliur Sun
     */
    private List<String[]> filterExcludedCategories(
            List<String[]> transactions,
            List<String> excludedCategories) {

        List<String[]> filteredTransactions =
                new ArrayList<>();

        if (excludedCategories == null
                || excludedCategories.isEmpty()) {

            filteredTransactions.addAll(transactions);
            return filteredTransactions;
        }

        for (String[] transaction : transactions) {

            /*
             * Keep malformed rows so that later validation can produce
             * an appropriate error message.
             */
            if (transaction == null
                    || transaction.length <= CATEGORY_INDEX) {

                filteredTransactions.add(transaction);
                continue;
            }

            String category =
                    transaction[CATEGORY_INDEX];

            if (!isExcludedCategory(
                    category,
                    excludedCategories)) {

                filteredTransactions.add(transaction);
            }
        }

        return filteredTransactions;
    }

    /**
     * Determines whether a transaction category is excluded.
     *
     * @param category transaction category
     * @param excludedCategories categories selected for exclusion
     * @return true when the category should be excluded
     * @author Waliur Sun
     */
    private boolean isExcludedCategory(
            String category,
            List<String> excludedCategories) {

        if (category == null) {
            return false;
        }

        for (String excludedCategory
                : excludedCategories) {

            if (excludedCategory != null
                    && category.trim().equalsIgnoreCase(
                            excludedCategory.trim())) {

                return true;
            }
        }

        return false;
    }

    /**
     * Validates the transaction list.
     *
     * @param transactions transaction list
     * @author Felix Santos
     * @author Waliur Sun
     */
    private void validateTransactions(
            List<String[]> transactions) {

        if (transactions == null) {
            throw new IllegalArgumentException(
                    "Transaction list cannot be null.");
        }

        if (transactions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Transaction list cannot be empty.");
        }
    }

    /**
     * Extracts and validates the year of every transaction.
     *
     * <p>Every transaction included in one insight report must belong
     * to the same calendar year.</p>
     *
     * @param transactions transaction list
     * @return common calendar year
     * @author Felix Santos
     * @author Waliur Sun
     * @author Adrian Singh
     */
    private int extractYear(
            List<String[]> transactions) {

        int expectedYear =
                parseTransactionYear(
                        transactions.get(0),
                        1);

        for (int index = 1;
                index < transactions.size();
                index++) {

            int currentYear =
                    parseTransactionYear(
                            transactions.get(index),
                            index + 1);

            if (currentYear != expectedYear) {
                throw new IllegalArgumentException(
                        "All transactions must belong to the same year. "
                                + "Expected "
                                + expectedYear
                                + ", but transaction row "
                                + (index + 1)
                                + " belongs to "
                                + currentYear
                                + ".");
            }
        }

        return expectedYear;
    }

    /**
     * Parses and validates the date in one transaction row.
     *
     * @param transaction transaction row containing Date, Category, and Amount
     * @param rowNumber human-readable row number used in error messages
     * @return transaction calendar year
     * @author Waliur Sun
     */
    private int parseTransactionYear(
            String[] transaction,
            int rowNumber) {

        if (transaction == null
                || transaction.length <= DATE_INDEX
                || transaction[DATE_INDEX] == null
                || transaction[DATE_INDEX].isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " is missing a valid date.");
        }

        String dateText =
                transaction[DATE_INDEX].trim();

        try {
            LocalDate date =
                    LocalDate.parse(
                            dateText,
                            DATE_FORMATTER);

            return date.getYear();

        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid date in transaction row "
                            + rowNumber
                            + ": "
                            + dateText
                            + ". Expected MM/DD/YYYY.",
                    exception);
        }
    }

    /**
     * Returns the budget statistics calculator.
     *
     * @return budget statistics calculator
     * @author Felix Santos
     * @author Waliur Sun
     */
    public BudgetStatistics getBudgetStatistics() {
        return budgetStatistics;
    }

    /**
     * Returns the spending analyzer.
     *
     * @return spending analyzer
     * @author Waliur Sun
     */
    public SpendingAnalyzer getSpendingAnalyzer() {
        return spendingAnalyzer;
    }

    /**
     * Returns the recommendation engine.
     *
     * @return recommendation engine
     * @author Waliur Sun
     */
    public RecommendationEngine getRecommendationEngine() {
        return recommendationEngine;
    }

    /**
     * Returns the insight report formatter.
     *
     * @return insight report formatter
     * @author Waliur Sun
     */
    public InsightReport getInsightReport() {
        return insightReport;
    }
}