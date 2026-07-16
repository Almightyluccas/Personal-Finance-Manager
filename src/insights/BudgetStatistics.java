package insights;

import java.util.List;

/**
 * Calculates high-level budget statistics such as total income,
 * total expenses, net balance, and overall budget status.
 *
 * <p>Each transaction row must contain the following values:</p>
 *
 * <ol>
 *     <li>Date</li>
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
public class BudgetStatistics {

    /** Index of the amount column in a transaction row. */
    private static final int AMOUNT_INDEX = 2;

    /** Required number of fields in each transaction row. */
    private static final int REQUIRED_FIELD_COUNT = 3;

    /**
     * Stores income and expense totals calculated in one traversal.
     *
     * @param totalIncome total positive transaction amounts
     * @param totalExpenses absolute total of negative transaction amounts
     * @author Waliur Sun
     */
    public record BudgetTotals(
            int totalIncome,
            int totalExpenses) {

        /**
         * Validates the calculated totals.
         *
         * @author Waliur Sun
         */
        public BudgetTotals {
            if (totalIncome < 0) {
                throw new IllegalArgumentException(
                        "Total income cannot be negative.");
            }

            if (totalExpenses < 0) {
                throw new IllegalArgumentException(
                        "Total expenses cannot be negative.");
            }
        }
    }

    /**
     * Constructs a new BudgetStatistics calculator.
     *
     * @author Waliur Sun
     */
    public BudgetStatistics() {
    }

    /**
     * Calculates total income and total expenses in one traversal.
     *
     * <p>Positive amounts contribute to income. Negative amounts
     * contribute to expenses as positive values.</p>
     *
     * @param transactions transaction rows to analyze
     * @return income and expense totals
     * @author Waliur Sun
     */
    public BudgetTotals calculateTotals(
            List<String[]> transactions) {

        validateTransactions(transactions);

        long totalIncome = 0;
        long totalExpenses = 0;

        for (int index = 0;
                index < transactions.size();
                index++) {

            int amount = parseAmount(
                    transactions.get(index),
                    index + 1);

            if (amount > 0) {
                totalIncome += amount;
            } else if (amount < 0) {
                /*
                 * Convert to long before Math.abs so that
                 * Integer.MIN_VALUE is handled safely.
                 */
                totalExpenses += Math.abs((long) amount);
            }

            if (totalIncome > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Total income exceeds the supported integer range.");
            }

            if (totalExpenses > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Total expenses exceed the supported integer range.");
            }
        }

        return new BudgetTotals(
                (int) totalIncome,
                (int) totalExpenses);
    }

    /**
     * Calculates total yearly income.
     *
     * <p>This method is retained for compatibility with existing code.
     * Code requiring both totals should use {@link #calculateTotals(List)}
     * to avoid processing the list twice.</p>
     *
     * @param transactions transaction rows to analyze
     * @return total yearly income
     * @author Waliur Sun
     */
    public int calculateTotalIncome(
            List<String[]> transactions) {

        return calculateTotals(transactions).totalIncome();
    }

    /**
     * Calculates total yearly expenses.
     *
     * <p>Expenses are returned as positive whole-dollar values.</p>
     *
     * @param transactions transaction rows to analyze
     * @return total yearly expenses
     * @author Waliur Sun
     */
    public int calculateTotalExpenses(
            List<String[]> transactions) {

        return calculateTotals(transactions).totalExpenses();
    }

    /**
     * Calculates the yearly net balance.
     *
     * @param totalIncome total yearly income
     * @param totalExpenses total yearly expenses
     * @return total income minus total expenses
     * @author Waliur Sun
     */
    public int calculateNetBalance(
            int totalIncome,
            int totalExpenses) {

        if (totalIncome < 0) {
            throw new IllegalArgumentException(
                    "Total income cannot be negative.");
        }

        if (totalExpenses < 0) {
            throw new IllegalArgumentException(
                    "Total expenses cannot be negative.");
        }

        long netBalance =
                (long) totalIncome - totalExpenses;

        if (netBalance > Integer.MAX_VALUE
                || netBalance < Integer.MIN_VALUE) {

            throw new IllegalArgumentException(
                    "Net balance exceeds the supported integer range.");
        }

        return (int) netBalance;
    }

    /**
     * Determines the budget status from the net balance.
     *
     * @param netBalance yearly net balance
     * @return surplus, deficit, or balanced status
     * @author Waliur Sun
     */
    public BudgetStatus determineBudgetStatus(
            int netBalance) {

        if (netBalance > 0) {
            return BudgetStatus.SURPLUS;
        }

        if (netBalance < 0) {
            return BudgetStatus.DEFICIT;
        }

        return BudgetStatus.BALANCED;
    }

    /**
     * Validates the transaction list.
     *
     * <p>The list must not be null or empty, matching the validation
     * rules used by {@link SpendingAnalyzer}.</p>
     *
     * @param transactions transaction rows to validate
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
     * Parses and validates the amount from one transaction row.
     *
     * @param transaction transaction row
     * @param rowNumber human-readable row number
     * @return parsed whole-dollar amount
     * @author Waliur Sun
     */
    private int parseAmount(
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

        if (transaction[AMOUNT_INDEX] == null) {
            throw new IllegalArgumentException(
                    "Transaction row "
                            + rowNumber
                            + " has a null amount.");
        }

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
}