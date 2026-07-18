package insights;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs spending analysis for yearly transactions.
 *
 * <p>Each transaction row must contain the following values:</p>
 *
 * <ol>
 *     <li>Date in MM/DD/YYYY format</li>
 *     <li>Category</li>
 *     <li>Whole-dollar amount</li>
 * </ol>
 *
 * <p>Positive amounts represent income, while negative amounts
 * represent expenses.</p>
 *
 * @author Waliur Sun
 * @author Adrian Singh
 */
public class SpendingAnalyzer {

    /** Index of the date column. */
    private static final int DATE_INDEX = 0;

    /** Index of the category column. */
    private static final int CATEGORY_INDEX = 1;

    /** Index of the amount column. */
    private static final int AMOUNT_INDEX = 2;

    /** Required number of fields in each transaction row. */
    private static final int REQUIRED_FIELD_COUNT = 3;

    /** Strict formatter for transaction dates. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Constructs a new SpendingAnalyzer.
     *
     * @author Waliur Sun
     */
    public SpendingAnalyzer() {
    }

    /**
     * Calculates the net transaction total for every month.
     *
     * <p>The returned map always contains months 1 through 12.
     * Positive values represent a net gain, while negative values
     * represent a net loss for that month.</p>
     *
     * @param transactions yearly transaction list
     * @return map containing month numbers and net monthly totals
     * @author Waliur Sun
     */
    public Map<Integer, Integer> calculateMonthlyTotals(
            List<String[]> transactions) {

        validateTransactions(transactions);

        Map<Integer, Integer> monthlyTotals =
                new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            monthlyTotals.put(month, 0);
        }

        for (int index = 0;
                index < transactions.size();
                index++) {

            String[] transaction =
                    transactions.get(index);

            int rowNumber = index + 1;

            int month =
                    extractMonth(
                            transaction,
                            rowNumber);

            int amount =
                    parseAmount(
                            transaction,
                            rowNumber);

            int currentTotal =
                    monthlyTotals.get(month);

            long updatedTotal =
                    (long) currentTotal + amount;

            if (updatedTotal > Integer.MAX_VALUE
                    || updatedTotal < Integer.MIN_VALUE) {

                throw new IllegalArgumentException(
                        "Monthly total exceeds the supported range "
                                + "for month "
                                + month
                                + ".");
            }

            monthlyTotals.put(
                    month,
                    (int) updatedTotal);
        }

        return monthlyTotals;
    }

    /**
     * Calculates total expenses for every category.
     *
     * <p>Only negative transaction amounts are treated as expenses.
     * Expense totals are returned as positive whole-dollar values.</p>
     *
     * @param transactions yearly transaction list
     * @return map containing category names and expense totals
     * @author Waliur Sun
     */
    public Map<String, Integer> calculateCategoryTotals(
            List<String[]> transactions) {

        validateTransactions(transactions);

        Map<String, Integer> categoryTotals =
                new LinkedHashMap<>();

        for (int index = 0;
                index < transactions.size();
                index++) {

            String[] transaction =
                    transactions.get(index);

            int rowNumber = index + 1;

            String category =
                    parseCategory(
                            transaction,
                            rowNumber);

            int amount =
                    parseAmount(
                            transaction,
                            rowNumber);

            if (amount < 0) {
                /*
                 * Convert to long before taking the absolute value
                 * so Integer.MIN_VALUE can be handled safely.
                 */
                long expenseAmount =
                        Math.abs((long) amount);

                long updatedTotal =
                        categoryTotals.getOrDefault(
                                category,
                                0)
                                + expenseAmount;

                if (updatedTotal > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException(
                            "Expense total exceeds the supported range "
                                    + "for category: "
                                    + category
                                    + ".");
                }

                categoryTotals.put(
                        category,
                        (int) updatedTotal);
            }
        }

        return categoryTotals;
    }

    /**
     * Calculates each category's percentage of total yearly expenses.
     *
     * @param categoryTotals expense totals by category
     * @param totalExpenses total yearly expenses
     * @return map containing category names and expense percentages
     * @author Waliur Sun
     */
    public Map<String, Double> calculateCategoryPercentages(
            Map<String, Integer> categoryTotals,
            int totalExpenses) {

        validateCategoryTotals(
                categoryTotals);

        if (totalExpenses < 0) {
            throw new IllegalArgumentException(
                    "Total expenses cannot be negative.");
        }

        Map<String, Double> percentages =
                new LinkedHashMap<>();

        if (totalExpenses == 0) {
            return percentages;
        }

        for (Map.Entry<String, Integer> entry
                : categoryTotals.entrySet()) {

            double percentage =
                    entry.getValue()
                            * 100.0
                            / totalExpenses;

            percentages.put(
                    entry.getKey(),
                    percentage);
        }

        return percentages;
    }

    /**
     * Calculates average monthly spending.
     *
     * @param totalExpenses total yearly expenses
     * @return average monthly spending
     * @author Waliur Sun
     */
    public double calculateAverageMonthlySpending(
            int totalExpenses) {

        if (totalExpenses < 0) {
            throw new IllegalArgumentException(
                    "Total expenses cannot be negative.");
        }

        return totalExpenses / 12.0;
    }

    /**
     * Extracts the month from one transaction row.
     *
     * @param transaction transaction row
     * @param rowNumber human-readable row number
     * @return month number from 1 through 12
     * @author Waliur Sun
     */
    private int extractMonth(
            String[] transaction,
            int rowNumber) {

        validateTransaction(
                transaction,
                rowNumber);

        String dateText =
                transaction[DATE_INDEX].trim();

        if (dateText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " has a blank date.");
        }

        try {
            LocalDate date =
                    LocalDate.parse(
                            dateText,
                            DATE_FORMATTER);

            return date.getMonthValue();

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
     * Reads and validates the category from one transaction row.
     *
     * @param transaction transaction row
     * @param rowNumber human-readable row number
     * @return trimmed category name
     * @author Waliur Sun
     */
    private String parseCategory(
            String[] transaction,
            int rowNumber) {

        validateTransaction(
                transaction,
                rowNumber);

        String category =
                transaction[CATEGORY_INDEX].trim();

        if (category.isEmpty()) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " has a blank category.");
        }

        return category;
    }

    /**
     * Reads and validates the whole-dollar amount from one transaction row.
     *
     * @param transaction transaction row
     * @param rowNumber human-readable row number
     * @return parsed whole-dollar amount
     * @author Waliur Sun
     */
    private int parseAmount(
            String[] transaction,
            int rowNumber) {

        validateTransaction(
                transaction,
                rowNumber);

        String amountText =
                transaction[AMOUNT_INDEX].trim();

        if (amountText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " has a blank amount.");
        }

        try {
            return Integer.parseInt(amountText);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid amount in transaction row "
                            + rowNumber
                            + ": "
                            + amountText
                            + ". Expected a whole-dollar amount.",
                    exception);
        }
    }

    /**
     * Validates the complete transaction list.
     *
     * @param transactions transaction list
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

        for (int index = 0;
                index < transactions.size();
                index++) {

            validateTransaction(
                    transactions.get(index),
                    index + 1);
        }
    }

    /**
     * Validates one transaction row and its required fields.
     *
     * @param transaction transaction row
     * @param rowNumber human-readable row number
     * @author Waliur Sun
     */
    private void validateTransaction(
            String[] transaction,
            int rowNumber) {

        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " cannot be null.");
        }

        if (transaction.length < REQUIRED_FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " must contain Date, Category, and Amount.");
        }

        if (transaction[DATE_INDEX] == null) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " has a null date.");
        }

        if (transaction[CATEGORY_INDEX] == null) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " has a null category.");
        }

        if (transaction[AMOUNT_INDEX] == null) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " has a null amount.");
        }
    }

    /**
     * Validates category totals before percentage calculations.
     *
     * @param categoryTotals expense totals by category
     * @author Waliur Sun
     */
    private void validateCategoryTotals(
            Map<String, Integer> categoryTotals) {

        if (categoryTotals == null) {
            throw new IllegalArgumentException(
                    "Category totals cannot be null.");
        }

        for (Map.Entry<String, Integer> entry
                : categoryTotals.entrySet()) {

            String category = entry.getKey();
            Integer total = entry.getValue();

            if (category == null || category.isBlank()) {
                throw new IllegalArgumentException(
                        "Category name cannot be null or blank.");
            }

            if (total == null) {
                throw new IllegalArgumentException(
                        "Category total cannot be null for "
                                + category
                                + ".");
            }

            if (total < 0) {
                throw new IllegalArgumentException(
                        "Category total cannot be negative for "
                                + category
                                + ".");
            }
        }
    }
}