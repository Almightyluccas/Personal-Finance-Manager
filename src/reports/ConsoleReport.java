package reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import storage.Budget;
import storage.Transaction;

/**
 * Displays financial reports to the console for the Personal Finance Manager.
 *
 * <p>
 * This class is responsible only for displaying report information.
 * Budget data is supplied by the ReportManager after being loaded
 * from the Storage module.
 * </p>
 *
 * @author Alyssa Johnson
 * @version 1.0
 * @since 1.0
 */
public class ConsoleReport {

    /**
     * Creates a new ConsoleReport.
     */
    public ConsoleReport() {
    }

    /**
     * Prints an annual financial report.
     *
     * @param budget the budget to display
     */
    public void printAnnualReport(Budget budget) {

        printReportHeader("ANNUAL FINANCIAL REPORT");

        if (budget == null) {
            System.out.println("No budget data available.");
            return;
        }

        double income = 0;
        double expenses = 0;

        for (Transaction transaction : budget.getTransactions()) {

            if (transaction.amount() >= 0) {
                income += transaction.amount();
            } else {
                expenses += Math.abs(transaction.amount());
            }

        }

        System.out.printf("Year: %d%n", budget.getYear());
        System.out.printf("Total Income: $%.2f%n", income);
        System.out.printf("Total Expenses: $%.2f%n", expenses);
        System.out.printf("Net Balance: $%.2f%n", income - expenses);
    }

    /**
     * Prints a monthly summary.
     *
     * @param budget the budget
     * @param month the month (1-12)
     */
    public void printMonthlySummary(Budget budget, int month) {

        printReportHeader("MONTHLY SUMMARY");

        if (budget == null) {
            System.out.println("No budget data available.");
            return;
        }

        List<Transaction> transactions = budget.getTransactionsByMonth(month);

        double income = 0;
        double expenses = 0;

        for (Transaction transaction : transactions) {

            if (transaction.amount() >= 0) {
                income += transaction.amount();
            } else {
                expenses += Math.abs(transaction.amount());
            }

        }

        System.out.println("Month: " + month);
        System.out.printf("Income: $%.2f%n", income);
        System.out.printf("Expenses: $%.2f%n", expenses);
        System.out.printf("Net Balance: $%.2f%n", income - expenses);
    }

    /**
     * Prints yearly category totals.
     *
     * @param budget the budget
     */
    public void printCategoryTotals(Budget budget) {

        printReportHeader("CATEGORY TOTALS");

        if (budget == null) {
            System.out.println("No budget data available.");
            return;
        }

        Map<String, Double> totals = new HashMap<>();

        for (Transaction transaction : budget.getTransactions()) {

            totals.put(
                transaction.category(),
                totals.getOrDefault(transaction.category(), 0.0)
                        + transaction.amount());

        }

        for (String category : totals.keySet()) {
            System.out.printf("%-20s $%.2f%n",
                    category,
                    totals.get(category));
        }
    }

    /**
     * Prints a simple budget summary.
     *
     * @param budget the budget
     */
    public void printBudgetSummary(Budget budget) {

        printReportHeader("BUDGET SUMMARY");

        if (budget == null) {
            System.out.println("No budget data available.");
            return;
        }

        double balance = 0;

        for (Transaction transaction : budget.getTransactions()) {
            balance += transaction.amount();
        }

        System.out.printf("Overall Balance: $%.2f%n", balance);

        if (balance >= 0) {
            System.out.println("Status: Surplus");
        } else {
            System.out.println("Status: Deficit");
        }
    }

    /**
     * Prints a standard report header.
     *
     * @param title the report title
     */
    public void printReportHeader(String title) {

        System.out.println();
        System.out.println("==========================================");
        System.out.println(" Personal Finance Manager");
        System.out.println(title);
        System.out.println("==========================================");

    }

}