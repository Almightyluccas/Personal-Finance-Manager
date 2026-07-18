package reports;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Formats report data for display in the Personal Finance Manager (PFM)
 * application.
 *
 * <p>
 * This class centralizes shared report wording and display formatting for
 * console and CSV output.
 * </p>
 *
 * @author Alyssa Johnson
 * @author Tahsin Abid
 * @version 2.0
 * @since 1.0
 */
public class ReportFormatter {

    private static final int LABEL_WIDTH = 16;

    private final NumberFormat currencyFormatter;
    private final DateTimeFormatter dateFormatter;

    /**
     * Constructs a new ReportFormatter object and initializes the US currency formatter.
     */
    public ReportFormatter() {
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    }

    /**
     * Formats monetary values for display.
     *
     * @param amount amount to format
     * @return formatted currency string
     */
    public String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }

    /**
     * Formats a monetary value that may be positive or negative.
     *
     * @param amount amount to format
     * @return formatted signed currency string
     */
    public String formatSignedCurrency(double amount) {
        return amount < 0
                ? "-" + formatCurrency(Math.abs(amount))
                : formatCurrency(amount);
    }

    /**
     * Formats report titles and section headings.
     *
     * @param title report title
     * @return formatted header
     */
    public String formatHeader(String title) {
        String text = title == null ? "Report" : title.trim();
        return text + "\n" + "-".repeat(text.length());
    }

    /**
     * Formats a readable section heading.
     *
     * @param title heading text
     * @return formatted heading
     */
    public String formatSectionHeading(String title) {
        return formatHeader(title);
    }

    /**
     * Formats monthly financial information.
     *
     * @param month month name
     * @param income monthly income
     * @param expenses monthly expenses
     * @return formatted monthly report line
     */
    public String formatMonthlyData(String month, double income, double expenses) {
        double balance = income - expenses;
        return String.format(
                "%-12s  Income: %-12s  Expenses: %-12s  Net: %s",
                month,
                formatCurrency(income),
                formatCurrency(expenses),
                formatSignedCurrency(balance));
    }

    /**
     * Formats yearly totals for each income and expense category.
     *
     * @param category category name
     * @param total yearly total
     * @return formatted category line
     */
    public String formatCategoryData(String category, double total) {
        return String.format("%-20s %14s", normalizeCategory(category), formatSignedCurrency(total));
    }

    /**
     * Formats the overall budget performance summary.
     *
     * @param income total income
     * @param expenses total expenses
     * @return formatted budget summary
     */
    public String formatBudgetSummary(double income, double expenses) {
        double balance = income - expenses;
        return "Income: " + formatCurrency(income)
                + "\nExpenses: " + formatCurrency(expenses)
                + "\nNet Balance: " + formatSignedCurrency(balance)
                + "\nStatus: " + formatStatus(balance);
    }

    /**
     * Formats a label-value pair with aligned labels.
     *
     * @param label label text
     * @param value value text
     * @return aligned label-value output
     */
    public String formatLabelValue(String label, String value) {
        return String.format("%-" + LABEL_WIDTH + "s %s", label + ":", value);
    }

    /**
     * Formats a date consistently for report output.
     *
     * @param date the date to format
     * @return formatted date or a fallback message
     */
    public String formatDate(LocalDate date) {
        return date == null ? "N/A" : date.format(dateFormatter);
    }

    /**
     * Formats a month number as a full month name.
     *
     * @param month the month number
     * @return month display name
     */
    public String formatMonth(int month) {
        if (!ReportAnalytics.isValidMonth(month)) {
            return "Invalid Month";
        }
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.US);
    }

    /**
     * Returns a user-facing status for the given balance.
     *
     * @param balance the net balance
     * @return Surplus, Deficit, or Balanced
     */
    public String formatStatus(double balance) {
        if (balance > 0.000001d) {
            return "Surplus";
        }
        if (balance < -0.000001d) {
            return "Deficit";
        }
        return "Balanced";
    }

    /**
     * Returns a normalized category label.
     *
     * @param category category text from storage
     * @return cleaned category label
     */
    public String normalizeCategory(String category) {
        return (category == null || category.isBlank()) ? "Uncategorized" : category.trim();
    }

    /**
     * Creates a short performance message for summary reports.
     *
     * @param totals calculated totals
     * @return a professional summary message
     */
    public String buildPerformanceMessage(ReportAnalytics.ReportTotals totals) {
        if (totals == null || totals.transactionCount() == 0) {
            return "No transaction activity is available for the selected budget.";
        }

        String status = formatStatus(totals.net());
        if ("Surplus".equals(status)) {
            return "The budget finished with a surplus, meaning income exceeded spending for the selected period.";
        }
        if ("Deficit".equals(status)) {
            return "The budget finished with a deficit, meaning spending exceeded income for the selected period.";
        }
        return "The budget finished balanced, with income matching expenses for the selected period.";
    }
}
