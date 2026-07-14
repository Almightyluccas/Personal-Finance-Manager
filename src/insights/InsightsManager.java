package insights;

import java.io.IOException;
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
 * @author Waliur Sun
 */
public class InsightsManager {

    /** Index of the date column in a transaction row. */
    private static final int DATE_INDEX = 0;

    /** Index of the category column in a transaction row. */
    private static final int CATEGORY_INDEX = 1;

    /** Performs overall budget calculations. */
    private final BudgetStatistics budgetStatistics;

    /** Performs spending analysis. */
    private final SpendingAnalyzer spendingAnalyzer;

    /** Generates financial recommendations. */
    private final RecommendationEngine recommendationEngine;

    /** Formats and exports reports. */
    private final InsightReport insightReport;

    /**
     * Creates a new InsightsManager.
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
     * Generates a complete yearly insight report without excluding categories.
     *
     * @param transactions yearly transaction list
     * @return completed InsightResult
     * @author Waliur Sun
     */
    public InsightResult generateInsights(List<String[]> transactions) {
        return generateInsights(transactions, new ArrayList<>());
    }

    /**
     * Generates a complete yearly insight report while excluding selected categories.
     *
     * <p>This method fixes the Alpha build issue where users could not choose
     * categories to exclude from Insights analysis.</p>
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories that should be ignored
     * @return completed InsightResult
     * @author Waliur Sun
     */
    public InsightResult generateInsights(
            List<String[]> transactions,
            List<String> excludedCategories) {

        validateTransactions(transactions);

        List<String[]> filteredTransactions =
                filterExcludedCategories(transactions, excludedCategories);

        if (filteredTransactions.isEmpty()) {
            throw new IllegalArgumentException(
                    "No transactions remain after excluding selected categories.");
        }

        int year = extractYear(filteredTransactions);

        int totalIncome =
                budgetStatistics.calculateTotalIncome(filteredTransactions);

        int totalExpenses =
                budgetStatistics.calculateTotalExpenses(filteredTransactions);

        int netBalance =
                budgetStatistics.calculateNetBalance(
                        totalIncome,
                        totalExpenses);

        BudgetStatus budgetStatus =
                budgetStatistics.determineBudgetStatus(netBalance);

        Map<Integer, Integer> monthlyTotals =
                spendingAnalyzer.calculateMonthlyTotals(filteredTransactions);

        Map<String, Integer> categoryTotals =
                spendingAnalyzer.calculateCategoryTotals(filteredTransactions);

        Map<String, Double> categoryPercentages =
                spendingAnalyzer.calculateCategoryPercentages(
                        categoryTotals,
                        totalExpenses);

        double averageMonthlySpending =
                spendingAnalyzer.calculateAverageMonthlySpending(totalExpenses);

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
     * Returns the formatted yearly report without excluding categories.
     *
     * @param transactions yearly transaction list
     * @return report text
     * @author Waliur Sun
     */
    public String analyzeYear(List<String[]> transactions) {
        return analyzeYear(transactions, new ArrayList<>());
    }

    /**
     * Returns the formatted yearly report while excluding selected categories.
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories that should be ignored
     * @return report text
     * @author Waliur Sun
     */
    public String analyzeYear(
            List<String[]> transactions,
            List<String> excludedCategories) {

        InsightResult result =
                generateInsights(transactions, excludedCategories);

        return insightReport.generateSummary(result);
    }

    /**
     * Prints the report to the console without excluding categories.
     *
     * @param transactions yearly transaction list
     * @author Waliur Sun
     */
    public void displayInsights(List<String[]> transactions) {
        displayInsights(transactions, new ArrayList<>());
    }

    /**
     * Prints the report to the console while excluding selected categories.
     *
     * @param transactions yearly transaction list
     * @param excludedCategories categories that should be ignored
     * @author Waliur Sun
     */
    public void displayInsights(
            List<String[]> transactions,
            List<String> excludedCategories) {

        InsightResult result =
                generateInsights(transactions, excludedCategories);

        insightReport.printReport(result);
    }

    /**
     * Exports the report to a CSV file without excluding categories.
     *
     * @param transactions yearly transaction list
     * @param filePath destination file
     * @throws IOException if export fails
     * @author Waliur Sun
     */
    public void exportInsights(
            List<String[]> transactions,
            String filePath)
            throws IOException {

        exportInsights(transactions, filePath, new ArrayList<>());
    }

    /**
     * Exports the report to a CSV file while excluding selected categories.
     *
     * @param transactions yearly transaction list
     * @param filePath destination file
     * @param excludedCategories categories that should be ignored
     * @throws IOException if export fails
     * @author Waliur Sun
     */
    public void exportInsights(
            List<String[]> transactions,
            String filePath,
            List<String> excludedCategories)
            throws IOException {

        InsightResult result =
                generateInsights(transactions, excludedCategories);

        insightReport.saveReportToCSV(result, filePath);
    }

    /**
     * Removes transactions whose categories are in the excluded list.
     *
     * @param transactions original transaction list
     * @param excludedCategories categories that should be ignored
     * @return filtered transaction list
     * @author Waliur Sun
     */
    private List<String[]> filterExcludedCategories(
            List<String[]> transactions,
            List<String> excludedCategories) {

        List<String[]> filteredTransactions = new ArrayList<>();

        if (excludedCategories == null || excludedCategories.isEmpty()) {
            filteredTransactions.addAll(transactions);
            return filteredTransactions;
        }

        for (String[] transaction : transactions) {
            if (transaction == null || transaction.length <= CATEGORY_INDEX) {
                filteredTransactions.add(transaction);
                continue;
            }

            String category = transaction[CATEGORY_INDEX];

            if (!isExcludedCategory(category, excludedCategories)) {
                filteredTransactions.add(transaction);
            }
        }

        return filteredTransactions;
    }

    /**
     * Checks whether a category should be excluded.
     *
     * @param category transaction category
     * @param excludedCategories categories selected for exclusion
     * @return true if category should be excluded
     * @author Waliur Sun
     */
    private boolean isExcludedCategory(
            String category,
            List<String> excludedCategories) {

        if (category == null) {
            return false;
        }

        for (String excludedCategory : excludedCategories) {
            if (excludedCategory != null
                    && category.trim().equalsIgnoreCase(
                    excludedCategory.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Ensures the transaction list is valid.
     *
     * @param transactions transaction list
     * @author Waliur Sun
     */
    private void validateTransactions(List<String[]> transactions) {

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
     * Extracts the year from the first transaction.
     *
     * <p>Expected date format: MM/DD/YYYY</p>
     *
     * @param transactions transaction list
     * @return budget year
     * @author Waliur Sun
     */
    private int extractYear(List<String[]> transactions) {

        String[] firstTransaction = transactions.get(0);

        if (firstTransaction == null
                || firstTransaction.length <= DATE_INDEX
                || firstTransaction[DATE_INDEX] == null) {

            throw new IllegalArgumentException(
                    "First transaction must contain a valid date.");
        }

        String date = firstTransaction[DATE_INDEX].trim();

        String[] parts = date.split("/");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid date format. Expected MM/DD/YYYY.");
        }

        try {
            return Integer.parseInt(parts[2]);
        }
        catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid year in date: " + date,
                    exception);
        }
    }

    /**
     * Returns the BudgetStatistics object.
     *
     * @return BudgetStatistics
     * @author Waliur Sun
     */
    public BudgetStatistics getBudgetStatistics() {
        return budgetStatistics;
    }

    /**
     * Returns the SpendingAnalyzer object.
     *
     * @return SpendingAnalyzer
     * @author Waliur Sun
     */
    public SpendingAnalyzer getSpendingAnalyzer() {
        return spendingAnalyzer;
    }

    /**
     * Returns the RecommendationEngine object.
     *
     * @return RecommendationEngine
     * @author Waliur Sun
     */
    public RecommendationEngine getRecommendationEngine() {
        return recommendationEngine;
    }

    /**
     * Returns the InsightReport object.
     *
     * @return InsightReport
     * @author Waliur Sun
     */
    public InsightReport getInsightReport() {
        return insightReport;
    }
}