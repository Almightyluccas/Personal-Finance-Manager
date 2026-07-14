package reports;

import storage.Budget;
import storage.Transaction;

import java.util.List;

/**
 * Displays financial reports to the console for the Personal Finance Manager
 * (PFM) application.
 *
 * <p>
 * This class is responsible only for displaying report information.
 * Calculations should be performed by {@code ReportManager}. Budget data is
 * supplied by the Storage module.
 * </p>
 *
 * @author Alyssa Johnson
 * @version 1.1
 * @since 1.0
 */
public class ConsoleReport {

    /**
     * Constructs a new ConsoleReport object.
     */
    public ConsoleReport() {
        // No initialization currently required.
    }

    /**
     * Prints an annual report using budget data supplied by the Storage module.
     *
     * @param budget the budget to display
     */
    public void printAnnualReport(Budget budget) {

        printReportHeader();

        if (budget == null) {
            System.out.println("No budget data available.");
            return;
        }

        System.out.println("Year: " + budget.getYear());

        List<Transaction> transactions = budget.getTransactions();

        if (transactions == null || transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        System.out.println("Number of Transactions: " + transactions.size());

        // TODO:
        // ReportManager should calculate:
        // - Total Income
        // - Total Expenses
        // - Net Savings
        // - Budget Performance
        //
        // ConsoleReport should ONLY display those values.

        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    /**
     * Prints a monthly summary.
     *
     * @param budget budget to display
     * @param month month (1-12)
     */
    public void printMonthlySummary(Budget budget, int month) {

        printReportHeader();

        if (budget == null) {
            System.out.println("No budget loaded.");
            return;
        }

        List<Transaction> monthlyTransactions =
                budget.getTransactionsByMonth(month);

        if (monthlyTransactions == null || monthlyTransactions.isEmpty()) {
            System.out.println("No transactions found for month " + month + ".");
            return;
        }

        System.out.println("Monthly Summary");
        System.out.println("Month: " + month);
        System.out.println();

        for (Transaction transaction : monthlyTransactions) {
            System.out.println(transaction);
        }

        // TODO:
        // ReportManager will calculate monthly totals before
        // passing them to this class.
    }

    /**
     * Prints yearly totals for a category.
     *
     * @param budget budget to display
     * @param category category to display
     */
    public void printCategoryTotals(Budget budget, String category) {

        printReportHeader();

        if (budget == null) {
            System.out.println("No budget loaded.");
            return;
        }

        List<Transaction> categoryTransactions =
                budget.getTransactionsByCategory(category);

        if (categoryTransactions == null || categoryTransactions.isEmpty()) {
            System.out.println("No transactions found for category: " + category);
            return;
        }

        System.out.println("Category: " + category);
        System.out.println();

        for (Transaction transaction : categoryTransactions) {
            System.out.println(transaction);
        }

        // TODO:
        // ReportManager should calculate the yearly total for
        // this category before displaying it.
    }

    /**
     * Prints the budget summary.
     *
     * @param budget budget to display
     */
    public void printBudgetSummary(Budget budget) {

        printReportHeader();

        if (budget == null) {
            System.out.println("No budget available.");
            return;
        }

        System.out.println("Budget Summary");
        System.out.println("Year: " + budget.getYear());

        // TODO:
        // Storage currently provides Budget and Transactions.
        // ReportManager should calculate:
        // - Total Income
        // - Total Expenses
        // - Surplus / Deficit
        // before calling this method.
    }

    /**
     * Prints a standard report header.
     */
    public void printReportHeader() {

        System.out.println();
        System.out.println("========================================");
        System.out.println("      Personal Finance Manager");
        System.out.println("========================================");
        System.out.println();
    }
}