package insights;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates financial recommendations based on yearly budget analysis.
 *
 * @author Waliur Sun
 * @author Adrian Singh
 * @author Felix Santos
 */
public class RecommendationEngine {

    /**
     * Generates recommendations for a surplus budget.
     *
     * @param netBalance positive net balance
     * @param categoryPercentages spending percentages by category
     * @return list of surplus recommendations
     * @author Waliur Sun
     */
    public List<String> generateSurplusRecommendations(
            int netBalance,
            Map<String, Double> categoryPercentages) {

        validateCategoryPercentages(categoryPercentages);

        List<String> recommendations = new ArrayList<>();

        if (netBalance <= 0) {
            recommendations.add(
                    "No surplus recommendations available.");
            return recommendations;
        }

        recommendations.add(
                "You finished the year with a surplus of $"
                        + netBalance + ".");

        recommendations.add(
                "Consider saving a portion of your surplus.");

        recommendations.add(
                "Maintain your current spending habits.");

        for (Map.Entry<String, Double> entry
                : categoryPercentages.entrySet()) {

            int adjustment =
                    estimateCategoryAdjustments(
                            netBalance,
                            entry.getValue());

            if (adjustment > 0) {
                recommendations.add(
                        "You could safely increase "
                                + entry.getKey()
                                + " spending by approximately $"
                                + adjustment
                                + ".");
            }
        }

        return recommendations;
    }

    /**
     * Generates recommendations for a deficit budget.
     *
     * @param netBalance negative net balance
     * @param categoryPercentages spending percentages by category
     * @return list of deficit recommendations
     * @author Waliur Sun
     */
    public List<String> generateDeficitRecommendations(
            int netBalance,
            Map<String, Double> categoryPercentages) {

        validateCategoryPercentages(categoryPercentages);

        List<String> recommendations = new ArrayList<>();

        if (netBalance >= 0) {
            recommendations.add(
                    "No deficit recommendations available.");
            return recommendations;
        }

        /*
         * Convert to long first to safely handle Integer.MIN_VALUE.
         */
        long deficitAmount = Math.abs((long) netBalance);

        recommendations.add(
                "You exceeded your income by $"
                        + deficitAmount
                        + ".");

        recommendations.add(
                "Reduce unnecessary expenses.");

        for (Map.Entry<String, Double> entry
                : categoryPercentages.entrySet()) {

            int reduction =
                    estimateCategoryAdjustments(
                            deficitAmount,
                            entry.getValue());

            if (reduction > 0) {
                recommendations.add(
                        "Reduce "
                                + entry.getKey()
                                + " spending by approximately $"
                                + reduction
                                + ".");
            }
        }

        return recommendations;
    }

    /**
     * Finds the largest expense category.
     *
     * @param categoryPercentages spending percentages by category
     * @return message describing the largest expense category
     * @author Waliur Sun
     */
    public String compareSpendingPatterns(
            Map<String, Double> categoryPercentages) {

        validateCategoryPercentages(categoryPercentages);

        if (categoryPercentages.isEmpty()) {
            return "No expense categories found.";
        }

        String largestCategory = "";
        double highestPercentage = -1.0;

        for (Map.Entry<String, Double> entry
                : categoryPercentages.entrySet()) {

            if (entry.getValue() > highestPercentage) {
                highestPercentage = entry.getValue();
                largestCategory = entry.getKey();
            }
        }

        return "Largest spending category: "
                + largestCategory
                + " ("
                + String.format("%.2f", highestPercentage)
                + "%)";
    }

    /**
     * Estimates an adjustment amount for one spending category.
     *
     * @param balanceAmount surplus or deficit amount
     * @param categoryPercentage category's percentage of total expenses
     * @return estimated whole-dollar adjustment
     * @author Waliur Sun
     */
    public int estimateCategoryAdjustments(
            long balanceAmount,
            double categoryPercentage) {

        if (balanceAmount < 0) {
            throw new IllegalArgumentException(
                    "Balance amount cannot be negative.");
        }

        validatePercentage(
                "Category",
                categoryPercentage);

        double adjustment =
                balanceAmount
                        * (categoryPercentage / 100.0);

        if (adjustment > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Calculated adjustment exceeds the supported range.");
        }

        return (int) Math.round(adjustment);
    }

    /**
     * Generates recommendations based on the overall budget status.
     *
     * @param status overall budget status
     * @param netBalance yearly net balance
     * @param categoryPercentages expense percentages by category
     * @return list of financial recommendations
     * @author Waliur Sun
     */
    public List<String> generateRecommendations(
            BudgetStatus status,
            int netBalance,
            Map<String, Double> categoryPercentages) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Budget status cannot be null.");
        }

        validateCategoryPercentages(categoryPercentages);

        List<String> recommendations = new ArrayList<>();

        recommendations.add(
                compareSpendingPatterns(
                        categoryPercentages));

        switch (status) {
            case SURPLUS -> recommendations.addAll(
                    generateSurplusRecommendations(
                            netBalance,
                            categoryPercentages));

            case DEFICIT -> recommendations.addAll(
                    generateDeficitRecommendations(
                            netBalance,
                            categoryPercentages));

            case BALANCED -> recommendations.add(
                    "Your yearly budget is balanced.");
        }

        return recommendations;
    }

    /**
     * Validates all category names and percentage values.
     *
     * <p>Each category must have a nonblank name, and each percentage
     * must be a finite number between zero and one hundred.</p>
     *
     * @param categoryPercentages expense percentages by category
     * @author Waliur Sun
     */
    private void validateCategoryPercentages(
            Map<String, Double> categoryPercentages) {

        if (categoryPercentages == null) {
            throw new IllegalArgumentException(
                    "Category percentages cannot be null.");
        }

        double totalPercentage = 0.0;

        for (Map.Entry<String, Double> entry
                : categoryPercentages.entrySet()) {

            String category = entry.getKey();
            Double percentage = entry.getValue();

            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException(
                        "Category name cannot be null or blank.");
            }

            if (percentage == null) {
                throw new IllegalArgumentException(
                        "Percentage cannot be null for category: "
                                + category);
            }

            validatePercentage(
                    category,
                    percentage);

            totalPercentage += percentage;
        }

        /*
         * A small tolerance prevents harmless floating-point
         * rounding from rejecting values such as 100.0000001.
         */
        if (totalPercentage > 100.01) {
            throw new IllegalArgumentException(
                    "Combined category percentages cannot exceed 100%.");
        }
    }

    /**
     * Validates one category percentage.
     *
     * @param category category associated with the percentage
     * @param percentage percentage to validate
     * @author Waliur Sun
     */
    private void validatePercentage(
            String category,
            double percentage) {

        if (!Double.isFinite(percentage)) {
            throw new IllegalArgumentException(
                    "Percentage must be a finite number for category: "
                            + category);
        }

        if (percentage < 0.0 || percentage > 100.0) {
            throw new IllegalArgumentException(
                    "Percentage must be between 0 and 100 for category: "
                            + category);
        }
    }
}