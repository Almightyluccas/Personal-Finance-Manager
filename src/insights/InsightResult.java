package insights;

import java.util.List;
import java.util.Map;

/**
 * Stores the final immutable results produced by the Insights module.
 *
 * <p>This record contains all calculated values for one yearly budget
 * analysis. Its compact constructor validates the values and makes
 * defensive copies of all collections.</p>
 *
 * @param year year being analyzed
 * @param totalIncome total yearly income
 * @param totalExpenses total yearly expenses
 * @param netBalance total income minus total expenses
 * @param budgetStatus overall budget outcome
 * @param monthlyTotals net transaction totals by month
 * @param categoryTotals expense totals by category
 * @param categoryPercentages expense percentages by category
 * @param averageMonthlySpending average monthly expense amount
 * @param recommendations generated financial recommendations
 *
 * @author Adrian Singh
 * @author Waliur Sun
 * @author Felix Santos
 */
public record InsightResult(

        int year,

        int totalIncome,

        int totalExpenses,

        int netBalance,

        BudgetStatus budgetStatus,

        Map<Integer, Integer> monthlyTotals,

        Map<String, Integer> categoryTotals,

        Map<String, Double> categoryPercentages,

        double averageMonthlySpending,

        List<String> recommendations

) {

    /**
     * Validates all result values and creates defensive collection copies.
     *
     * @author Waliur Sun
     */
    public InsightResult {

        validateYear(year);
        validateFinancialValues(
                totalIncome,
                totalExpenses,
                netBalance,
                averageMonthlySpending);

        validateBudgetStatus(
                budgetStatus,
                netBalance);

        validateMonthlyTotals(monthlyTotals);
        validateCategoryTotals(categoryTotals);
        validateCategoryPercentages(categoryPercentages);
        validateRecommendations(recommendations);

        monthlyTotals = Map.copyOf(monthlyTotals);
        categoryTotals = Map.copyOf(categoryTotals);
        categoryPercentages = Map.copyOf(categoryPercentages);
        recommendations = List.copyOf(recommendations);
    }

    /**
     * Validates the budget year.
     *
     * @param year year being analyzed
     * @author Waliur Sun
     */
    private static void validateYear(int year) {

        if (year < 1900 || year > 2099) {
            throw new IllegalArgumentException(
                    "Year must be between 1900 and 2099.");
        }
    }

    /**
     * Validates income, expenses, balance, and average monthly spending.
     *
     * @param totalIncome total yearly income
     * @param totalExpenses total yearly expenses
     * @param netBalance yearly net balance
     * @param averageMonthlySpending average monthly spending
     * @author Waliur Sun
     */
    private static void validateFinancialValues(
            int totalIncome,
            int totalExpenses,
            int netBalance,
            double averageMonthlySpending) {

        if (totalIncome < 0) {
            throw new IllegalArgumentException(
                    "Total income cannot be negative.");
        }

        if (totalExpenses < 0) {
            throw new IllegalArgumentException(
                    "Total expenses cannot be negative.");
        }

        long expectedNetBalance =
                (long) totalIncome - totalExpenses;

        if (expectedNetBalance < Integer.MIN_VALUE
                || expectedNetBalance > Integer.MAX_VALUE) {

            throw new IllegalArgumentException(
                    "Net balance exceeds the supported integer range.");
        }

        if (netBalance != (int) expectedNetBalance) {
            throw new IllegalArgumentException(
                    "Net balance must equal total income minus total expenses.");
        }

        if (!Double.isFinite(averageMonthlySpending)) {
            throw new IllegalArgumentException(
                    "Average monthly spending must be a finite number.");
        }

        if (averageMonthlySpending < 0.0) {
            throw new IllegalArgumentException(
                    "Average monthly spending cannot be negative.");
        }

        double expectedAverage =
                totalExpenses / 12.0;

        if (Math.abs(
                averageMonthlySpending
                        - expectedAverage) > 0.01) {

            throw new IllegalArgumentException(
                    "Average monthly spending must equal "
                            + "total expenses divided by 12.");
        }
    }

    /**
     * Validates that the budget status agrees with the net balance.
     *
     * @param budgetStatus overall budget outcome
     * @param netBalance yearly net balance
     * @author Waliur Sun
     */
    private static void validateBudgetStatus(
            BudgetStatus budgetStatus,
            int netBalance) {

        if (budgetStatus == null) {
            throw new IllegalArgumentException(
                    "Budget status cannot be null.");
        }

        BudgetStatus expectedStatus;

        if (netBalance > 0) {
            expectedStatus = BudgetStatus.SURPLUS;
        } else if (netBalance < 0) {
            expectedStatus = BudgetStatus.DEFICIT;
        } else {
            expectedStatus = BudgetStatus.BALANCED;
        }

        if (budgetStatus != expectedStatus) {
            throw new IllegalArgumentException(
                    "Budget status does not match the net balance.");
        }
    }

    /**
     * Validates the monthly totals map.
     *
     * @param monthlyTotals totals by month
     * @author Waliur Sun
     */
    private static void validateMonthlyTotals(
            Map<Integer, Integer> monthlyTotals) {

        if (monthlyTotals == null) {
            throw new IllegalArgumentException(
                    "Monthly totals cannot be null.");
        }

        for (Map.Entry<Integer, Integer> entry
                : monthlyTotals.entrySet()) {

            Integer month = entry.getKey();
            Integer amount = entry.getValue();

            if (month == null
                    || month < 1
                    || month > 12) {

                throw new IllegalArgumentException(
                        "Monthly total keys must be between 1 and 12.");
            }

            if (amount == null) {
                throw new IllegalArgumentException(
                        "Monthly total cannot be null for month "
                                + month
                                + ".");
            }
        }
    }

    /**
     * Validates expense totals by category.
     *
     * @param categoryTotals expense totals by category
     * @author Waliur Sun
     */
    private static void validateCategoryTotals(
            Map<String, Integer> categoryTotals) {

        if (categoryTotals == null) {
            throw new IllegalArgumentException(
                    "Category totals cannot be null.");
        }

        for (Map.Entry<String, Integer> entry
                : categoryTotals.entrySet()) {

            String category = entry.getKey();
            Integer amount = entry.getValue();

            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException(
                        "Category name cannot be null or blank.");
            }

            if (amount == null) {
                throw new IllegalArgumentException(
                        "Category total cannot be null for "
                                + category
                                + ".");
            }

            if (amount < 0) {
                throw new IllegalArgumentException(
                        "Category total cannot be negative for "
                                + category
                                + ".");
            }
        }
    }

    /**
     * Validates expense percentages by category.
     *
     * @param categoryPercentages percentages by category
     * @author Waliur Sun
     */
    private static void validateCategoryPercentages(
            Map<String, Double> categoryPercentages) {

        if (categoryPercentages == null) {
            throw new IllegalArgumentException(
                    "Category percentages cannot be null.");
        }

        double combinedPercentage = 0.0;

        for (Map.Entry<String, Double> entry
                : categoryPercentages.entrySet()) {

            String category = entry.getKey();
            Double percentage = entry.getValue();

            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException(
                        "Percentage category cannot be null or blank.");
            }

            if (percentage == null) {
                throw new IllegalArgumentException(
                        "Percentage cannot be null for "
                                + category
                                + ".");
            }

            if (!Double.isFinite(percentage)) {
                throw new IllegalArgumentException(
                        "Percentage must be finite for "
                                + category
                                + ".");
            }

            if (percentage < 0.0 || percentage > 100.0) {
                throw new IllegalArgumentException(
                        "Percentage must be between 0 and 100 for "
                                + category
                                + ".");
            }

            combinedPercentage += percentage;
        }

        if (combinedPercentage > 100.01) {
            throw new IllegalArgumentException(
                    "Combined category percentages cannot exceed 100%.");
        }
    }

    /**
     * Validates the recommendation list.
     *
     * @param recommendations generated recommendations
     * @author Waliur Sun
     */
    private static void validateRecommendations(
            List<String> recommendations) {

        if (recommendations == null) {
            throw new IllegalArgumentException(
                    "Recommendations cannot be null.");
        }

        for (String recommendation : recommendations) {
            if (recommendation == null
                    || recommendation.isBlank()) {

                throw new IllegalArgumentException(
                        "Recommendations cannot contain null or blank values.");
            }
        }
    }
}