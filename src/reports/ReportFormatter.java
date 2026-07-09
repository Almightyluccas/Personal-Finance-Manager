package reports;

import java.text.NumberFormat;
import java.util.Locale;
/**
 * Formats report data for display in the Personal Finance Manager (PFM)
 * application.
 *
 * <p>
 * This class is responsible for formatting report data before it is displayed
 * on the console or exported to a CSV file. Formatting methods improve
 * readability and maintain a consistent report layout.
 * </p>
 *
 * @author Alyssa Johnson
 * @author Tahsin Abid
 * @version 1.1
 * @since 1.0
 */
public class ReportFormatter {

    /**
     * Constructs a new ReportFormatter object and initializes the US currency formatter.
     */
    private final NumberFormat currencyFormatter;

    public ReportFormatter() {
    currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    /**
     * Formats monetary values for display.
     *
     * @param amount amount to format
     * @return formatted currency string
     *
     */
    public String formatCurrency(double amount) {
        return currencyFormatter.format(amount);

    }

    /**
     * Formats report titles and section headings.
     *
     * <p>
     * Creates consistent headers for all reports.
     * </p>
     *
     * @param title report title
     * @return formatted header
     */
    public String formatHeader(String title) {
        String line = "=".repeat(40);
        return line + "\n"
                +title.toUpperCase()
                +"\n"
                +line;



    }

    /**
     * Formats monthly financial information.
     *
     * <p>
     * Organizes monthly income, expenses, and balances.
     *</p>
     *
     * @param month month name
     * @param income monthly income
     * @param expenses monthly expenses
     * @return formatted monthly report line
     *
     */
    public String formatMonthlyData(String month, double income, double expenses) {
        double balance = income + expenses;
        return String.format(
                "%-12s Income: %-12s Expenses: %-12s Balance: %s",
                month, formatCurrency(income), formatCurrency(expenses), formatCurrency(balance)
        );

    }

    /**
     * Formats yearly totals for each income and expense category.
     *
     * <p>
     * Organizes category totals into a readable layout.
     * </p>
     *
     * @param category category name
     * @param total yearly total
     * @return formatted category line
     * 
     */
    public String formatCategoryData(String category, double total) {

        return String.format("%-20s %s", category, formatCurrency(total));

    }

    /**
     * Formats the overall budget performance summary.
     *
     * <p>
     * Formats the final report section displaying surplus or deficit.
     * </p>
     *
     * @param income total income
     * @param expenses total expenses
     * @return formatted budget summary
     */
    public String formatBudgetSummary(double income, double expenses) {
        double balance = income + expenses;
        String status;
        if(balance > 0){
            status = "Surplus";}
        else if (balance < 0){
            status = "Defecit";}
        else{
            status = "Balanced";
        }
    return "income: " + formatCurrency(income) + "\nexpenses: " + formatCurrency(expenses)
            + "\nBalance: " + formatCurrency(balance) + "\nStatus: " + status;

    }

}
