package insights;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
     * Generates insights without excluding any recommendation categories.
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
     * Generates a complete yearly insight result.
     *
     * <p>Excluded categories are removed only from category-specific
     * recommendations. They are not removed from income, expenses,
     * net balance, monthly totals, category totals, or percentages.</p>
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories excluded from recommendations
     * @return completed insight result
     * @author Waliur Sun
     */
    public InsightResult generateInsights(
            List<String[]> transactions,
            List<String> excludedCategories) {

        validateTransactions(transactions);

        int year =
                extractYear(transactions);

        /*
         * Financial totals must use every transaction.
         * Excluded categories still represent real expenses.
         */
        BudgetStatistics.BudgetTotals totals =
                budgetStatistics.calculateTotals(
                        transactions);

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

        /*
         * Monthly and category information also use every transaction.
         */
        Map<Integer, Integer> monthlyTotals =
                spendingAnalyzer.calculateMonthlyTotals(
                        transactions);

        Map<String, Integer> categoryTotals =
                spendingAnalyzer.calculateCategoryTotals(
                        transactions);

        Map<String, Double> categoryPercentages =
                spendingAnalyzer.calculateCategoryPercentages(
                        categoryTotals,
                        totalExpenses);

        double averageMonthlySpending =
                spendingAnalyzer.calculateAverageMonthlySpending(
                        totalExpenses);

        /*
         * Only category-specific recommendations use the filtered map.
         */
        Map<String, Double> recommendationPercentages =
                filterRecommendationPercentages(
                        categoryPercentages,
                        excludedCategories);

        List<String> recommendations =
                recommendationEngine.generateRecommendations(
                        budgetStatus,
                        netBalance,
                        recommendationPercentages);

        /*
         * The report stores the full financial information.
         */
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
     * Creates a formatted yearly report without exclusions.
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
     * Creates a formatted yearly report with recommendation exclusions.
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories excluded from recommendations
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
     * Prints the insight report without exclusions.
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
     * Prints the insight report with recommendation exclusions.
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories excluded from recommendations
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
     * Exports the insight report without exclusions.
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
     * Exports the insight report with recommendation exclusions.
     *
     * @param transactions yearly transaction list
     * @param filePath destination CSV file path
     * @param excludedCategories categories excluded from recommendations
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
     * Creates a percentage map used only for recommendations.
     *
     * <p>The original percentage map remains unchanged so the report
     * continues to show every expense category.</p>
     *
     * @param categoryPercentages complete category percentage map
     * @param excludedCategories categories excluded from recommendations
     * @return filtered recommendation percentage map
     * @author Waliur Sun
     */
    private Map<String, Double> filterRecommendationPercentages(
            Map<String, Double> categoryPercentages,
            List<String> excludedCategories) {

        Map<String, Double> filteredPercentages =
                new LinkedHashMap<>();

        if (excludedCategories == null
                || excludedCategories.isEmpty()) {

            filteredPercentages.putAll(
                    categoryPercentages);

            return filteredPercentages;
        }

        for (Map.Entry<String, Double> entry
                : categoryPercentages.entrySet()) {

            if (!isExcludedCategory(
                    entry.getKey(),
                    excludedCategories)) {

                filteredPercentages.put(
                        entry.getKey(),
                        entry.getValue());
            }
        }

        return filteredPercentages;
    }

    /**
     * Determines whether a category is excluded from recommendations.
     *
     * <p>Comparisons are case-insensitive and ignore surrounding spaces.</p>
     *
     * @param category transaction category
     * @param excludedCategories excluded category list
     * @return true if the category is excluded
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
     * @param transaction transaction row
     * @param rowNumber human-readable row number
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