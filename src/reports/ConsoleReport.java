package reports;

import integration.MenuUtil;
import java.util.List;
import storage.Budget;

/**
 * Displays financial reports to the console for the Personal Finance Manager.
 *
 * <p>
 * This class presents annual, monthly, category, and summary reports using
 * stored budget data loaded by the Reports module.
 * </p>
 *
 * @author Alyssa Johnson
 * @version 2.0
 * @since 1.0
 */
public class ConsoleReport {

    private static final String NO_DATA_MESSAGE = "No transactions are available for this report.";

    private final ReportFormatter formatter;

    /**
     * Creates a new ConsoleReport.
     */
    public ConsoleReport() {
        this(new ReportFormatter());
    }

    /**
     * Creates a new ConsoleReport using the provided formatter.
     *
     * @param formatter shared formatter for consistent report output
     */
    ConsoleReport(ReportFormatter formatter) {
        this.formatter = formatter == null ? new ReportFormatter() : formatter;
    }

    /**
     * Prints an annual financial report.
     *
     * @param budget the budget to display
     */
    public void printAnnualReport(Budget budget) {
        MenuUtil.printTitle("Annual Financial Report");

        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        ReportAnalytics.ReportTotals annual = ReportAnalytics.forBudget(budget);
        printSummaryBlock(annual, "Year", String.valueOf(annual.year()));

        if (annual.transactionCount() == 0) {
            System.out.println(NO_DATA_MESSAGE);
            return;
        }

        System.out.println();
        System.out.println(formatter.formatSectionHeading("Category Breakdown"));
        printCategoryTable(annual.categories());
    }

    /**
     * Prints a monthly summary.
     *
     * @param budget the budget
     * @param month the month (1-12)
     */
    public void printMonthlySummary(Budget budget, int month) {
        MenuUtil.printTitle("Monthly Financial Summary");

        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        if (!ReportAnalytics.isValidMonth(month)) {
            System.out.println("Please choose a month between 1 and 12.");
            return;
        }

        ReportAnalytics.ReportTotals monthly = ReportAnalytics.forMonth(budget, month);
        printSummaryBlock(
                monthly,
                "Month",
                formatter.formatMonth(monthly.month()),
                "Year",
                String.valueOf(monthly.year()));

        if (monthly.transactionCount() == 0) {
            System.out.println(NO_DATA_MESSAGE);
            return;
        }

        System.out.println();
        System.out.println(formatter.formatSectionHeading("Transactions"));
        printTransactionTable(monthly.transactions());
    }

    /**
     * Prints yearly category totals.
     *
     * @param budget the budget
     */
    public void printCategoryTotals(Budget budget) {
        MenuUtil.printTitle("Category Totals");

        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        ReportAnalytics.ReportTotals totals = ReportAnalytics.forBudget(budget);
        System.out.println(formatter.formatLabelValue("Year", String.valueOf(totals.year())));
        System.out.println(formatter.formatLabelValue("Transactions", String.valueOf(totals.transactionCount())));
        System.out.println();
        printCategoryTable(totals.categories());
        System.out.println();
        System.out.println(formatter.formatLabelValue("Overall Income", formatter.formatCurrency(totals.income())));
        System.out.println(formatter.formatLabelValue("Overall Expenses", formatter.formatCurrency(totals.expenses())));
        System.out.println(formatter.formatLabelValue("Overall Net", formatter.formatSignedCurrency(totals.net())));

        if (totals.transactionCount() == 0) {
            System.out.println();
            System.out.println(NO_DATA_MESSAGE);
        }
    }

    /**
     * Prints a budget summary.
     *
     * @param budget the budget
     */
    public void printBudgetSummary(Budget budget) {
        MenuUtil.printTitle("Budget Performance Summary");

        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        ReportAnalytics.ReportTotals summary = ReportAnalytics.forBudget(budget);
        printSummaryBlock(summary, "Year", String.valueOf(summary.year()));
        System.out.println();
        System.out.println(formatter.buildPerformanceMessage(summary));
    }

    private void printSummaryBlock(
            ReportAnalytics.ReportTotals totals,
            String firstLabel,
            String firstValue) {
        printSummaryBlock(totals, firstLabel, firstValue, null, null);
    }

    private void printSummaryBlock(
            ReportAnalytics.ReportTotals totals,
            String firstLabel,
            String firstValue,
            String secondLabel,
            String secondValue) {
        System.out.println(formatter.formatLabelValue(firstLabel, firstValue));
        if (secondLabel != null && secondValue != null) {
            System.out.println(formatter.formatLabelValue(secondLabel, secondValue));
        }
        System.out.println(formatter.formatLabelValue("Income", formatter.formatCurrency(totals.income())));
        System.out.println(formatter.formatLabelValue("Expenses", formatter.formatCurrency(totals.expenses())));
        System.out.println(formatter.formatLabelValue("Net Balance", formatter.formatSignedCurrency(totals.net())));
        System.out.println(formatter.formatLabelValue("Status", formatter.formatStatus(totals.net())));
        System.out.println(formatter.formatLabelValue("Transactions", String.valueOf(totals.transactionCount())));
    }

    private void printTransactionTable(List<ReportAnalytics.ReportRow> rows) {
        String format = "%-12s  %-18s  %-8s  %14s%n";
        System.out.printf(format, "Date", "Category", "Type", "Amount");
        System.out.printf(format, "------------", "------------------", "--------", "--------------");

        for (ReportAnalytics.ReportRow row : rows) {
            System.out.printf(
                    format,
                    formatter.formatDate(row.date()),
                    formatter.normalizeCategory(row.category()),
                    row.amount() >= 0 ? "Income" : "Expense",
                    formatter.formatCurrency(Math.abs(row.amount())));
        }
    }

    private void printCategoryTable(List<ReportAnalytics.CategoryTotals> categories) {
        String format = "%-18s  %14s  %14s  %14s%n";
        System.out.printf(format, "Category", "Income", "Expenses", "Net");
        System.out.printf(format, "------------------", "--------------", "--------------", "--------------");

        for (ReportAnalytics.CategoryTotals category : categories) {
            System.out.printf(
                    format,
                    category.category(),
                    formatter.formatCurrency(category.income()),
                    formatter.formatCurrency(category.expenses()),
                    formatter.formatSignedCurrency(category.net()));
        }
    }
}
