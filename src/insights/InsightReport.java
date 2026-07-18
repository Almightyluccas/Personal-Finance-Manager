package insights;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

/**
 * Creates, prints, and exports financial insight reports.
 *
 * @author Waliur Sun
 * @author Adrian Singh
 * @author Felix Santos
 */
public class InsightReport {

    /** Width used for summary labels. */
    private static final int SUMMARY_LABEL_WIDTH = 32;

    /** Width used for summary values. */
    private static final int SUMMARY_VALUE_WIDTH = 18;

    /** Width used for category names. */
    private static final int CATEGORY_WIDTH = 28;

    /** Width used for monetary values. */
    private static final int MONEY_WIDTH = 16;

    /** Width used for percentages. */
    private static final int PERCENT_WIDTH = 12;

    /**
     * Constructs a new InsightReport.
     *
     * @author Waliur Sun
     */
    public InsightReport() {
    }

    /**
     * Generates a formatted text report.
     *
     * <p>Money values are right aligned, displayed without cents,
     * and include thousands separators.</p>
     *
     * @param result completed insight result
     * @return formatted report text
     * @author Waliur Sun
     */
    public String generateSummary(InsightResult result) {

        validateResult(result);

        StringBuilder report = new StringBuilder();

        report.append(
                "================================================================\n");
        report.append(
                "                   FINANCIAL INSIGHTS REPORT\n");
        report.append(
                "================================================================\n\n");

        appendTextRow(
                report,
                "Year",
                Integer.toString(result.year()));

        appendMoneyRow(
                report,
                "Total Income",
                result.totalIncome());

        appendMoneyRow(
                report,
                "Total Expenses",
                result.totalExpenses());

        appendMoneyRow(
                report,
                "Net Balance",
                result.netBalance());

        appendTextRow(
                report,
                "Budget Status",
                result.budgetStatus().toString());

        appendMoneyRow(
                report,
                "Average Monthly Spending",
                Math.round(
                        result.averageMonthlySpending()));

        report.append("\n");
        report.append(
                "MONTHLY TOTALS\n");
        report.append(
                "----------------------------------------------------------------\n");

        report.append(
                String.format(
                        Locale.US,
                        "%-28s %16s%n",
                        "Month",
                        "Net Total"));

        report.append(
                String.format(
                        Locale.US,
                        "%-28s %16s%n",
                        "----------------------------",
                        "----------------"));

        for (Map.Entry<Integer, Integer> entry
                : result.monthlyTotals().entrySet()) {

            String monthName =
                    Month.of(entry.getKey())
                            .getDisplayName(
                                    TextStyle.FULL,
                                    Locale.US);

            report.append(
                    String.format(
                            Locale.US,
                            "%-28s %16s%n",
                            monthName,
                            formatMoney(entry.getValue())));
        }

        report.append("\n");
        report.append(
                "EXPENSE CATEGORIES\n");
        report.append(
                "----------------------------------------------------------------\n");

        report.append(
                String.format(
                        Locale.US,
                        "%-28s %16s %12s%n",
                        "Category",
                        "Amount",
                        "Percent"));

        report.append(
                String.format(
                        Locale.US,
                        "%-28s %16s %12s%n",
                        "----------------------------",
                        "----------------",
                        "------------"));

        if (result.categoryTotals().isEmpty()) {
            report.append(
                    "No expense categories were found.\n");

        } else {
            for (Map.Entry<String, Integer> entry
                    : result.categoryTotals().entrySet()) {

                double percentage =
                        result.categoryPercentages()
                                .getOrDefault(
                                        entry.getKey(),
                                        0.0);

                report.append(
                        String.format(
                                Locale.US,
                                "%-28s %16s %12s%n",
                                limitText(
                                        entry.getKey(),
                                        CATEGORY_WIDTH),
                                formatMoney(entry.getValue()),
                                formatPercentage(percentage)));
            }
        }

        report.append("\n");
        report.append(
                "RECOMMENDATIONS\n");
        report.append(
                "----------------------------------------------------------------\n");

        if (result.recommendations().isEmpty()) {
            report.append(
                    "- No recommendations available.\n");

        } else {
            for (String recommendation
                    : result.recommendations()) {

                report.append("- ")
                        .append(recommendation)
                        .append("\n");
            }
        }

        report.append(
                "================================================================\n");

        return report.toString();
    }

    /**
     * Prints the formatted insight report to the console.
     *
     * @param result completed insight result
     * @author Waliur Sun
     */
    public void printReport(InsightResult result) {

        System.out.println(
                generateSummary(result));
    }

    /**
     * Saves the insight report to a CSV file.
     *
     * <p>All text fields are escaped so commas, quotation marks,
     * and line breaks do not corrupt the CSV structure. Monetary
     * values are exported as whole-dollar numeric values.</p>
     *
     * @param result completed insight result
     * @param filePath destination CSV file path
     * @throws IOException if the file cannot be written
     * @author Waliur Sun
     * @author Felix Santos
     */
    public void saveReportToCSV(
            InsightResult result,
            String filePath)
            throws IOException {

        validateResult(result);
        validateFilePath(filePath);

        try (PrintWriter writer =
                     new PrintWriter(
                             new FileWriter(filePath))) {

            writeCsvRow(
                    writer,
                    "Section",
                    "Name",
                    "Value",
                    "Percentage");

            writeCsvRow(
                    writer,
                    "Summary",
                    "Year",
                    Integer.toString(result.year()),
                    "");

            writeCsvRow(
                    writer,
                    "Summary",
                    "Total Income",
                    Integer.toString(result.totalIncome()),
                    "");

            writeCsvRow(
                    writer,
                    "Summary",
                    "Total Expenses",
                    Integer.toString(result.totalExpenses()),
                    "");

            writeCsvRow(
                    writer,
                    "Summary",
                    "Net Balance",
                    Integer.toString(result.netBalance()),
                    "");

            writeCsvRow(
                    writer,
                    "Summary",
                    "Budget Status",
                    result.budgetStatus().toString(),
                    "");

            writeCsvRow(
                    writer,
                    "Summary",
                    "Average Monthly Spending",
                    Long.toString(
                            Math.round(
                                    result.averageMonthlySpending())),
                    "");

            for (Map.Entry<Integer, Integer> entry
                    : result.monthlyTotals().entrySet()) {

                String monthName =
                        Month.of(entry.getKey())
                                .getDisplayName(
                                        TextStyle.FULL,
                                        Locale.US);

                writeCsvRow(
                        writer,
                        "Month",
                        monthName,
                        Integer.toString(entry.getValue()),
                        "");
            }

            for (Map.Entry<String, Integer> entry
                    : result.categoryTotals().entrySet()) {

                double percentage =
                        result.categoryPercentages()
                                .getOrDefault(
                                        entry.getKey(),
                                        0.0);

                writeCsvRow(
                        writer,
                        "Category",
                        entry.getKey(),
                        Integer.toString(entry.getValue()),
                        formatDecimal(percentage));
            }

            for (String recommendation
                    : result.recommendations()) {

                writeCsvRow(
                        writer,
                        "Recommendation",
                        recommendation,
                        "",
                        "");
            }

            if (writer.checkError()) {
                throw new IOException(
                        "An error occurred while writing the CSV report.");
            }
        }
    }

    /**
     * Appends one aligned text row to the report.
     *
     * @param report report builder
     * @param label row label
     * @param value row value
     * @author Waliur Sun
     */
    private void appendTextRow(
            StringBuilder report,
            String label,
            String value) {

        report.append(
                String.format(
                        Locale.US,
                        "%-" + SUMMARY_LABEL_WIDTH
                                + "s %"
                                + SUMMARY_VALUE_WIDTH
                                + "s%n",
                        label + ":",
                        value));
    }

    /**
     * Appends one aligned monetary row to the report.
     *
     * @param report report builder
     * @param label row label
     * @param amount whole-dollar amount
     * @author Waliur Sun
     */
    private void appendMoneyRow(
            StringBuilder report,
            String label,
            long amount) {

        appendTextRow(
                report,
                label,
                formatMoney(amount));
    }

    /**
     * Formats money without cents and with thousands separators.
     *
     * <p>Negative values are displayed in the form
     * {@code -$1,250}.</p>
     *
     * @param amount whole-dollar amount
     * @return formatted monetary value
     * @author Waliur Sun
     */
    private String formatMoney(long amount) {

        String formattedNumber =
                String.format(
                        Locale.US,
                        "%,d",
                        amount);

        if (amount < 0) {
            return "-$"
                    + formattedNumber.substring(1);
        }

        return "$" + formattedNumber;
    }

    /**
     * Formats a percentage for display.
     *
     * @param percentage percentage value
     * @return percentage formatted with one decimal place
     * @author Waliur Sun
     */
    private String formatPercentage(
            double percentage) {

        if (!Double.isFinite(percentage)) {
            throw new IllegalArgumentException(
                    "Percentage must be a finite number.");
        }

        return String.format(
                Locale.US,
                "%.1f%%",
                percentage);
    }

    /**
     * Shortens text that exceeds the available report-column width.
     *
     * @param value text to display
     * @param maximumLength maximum permitted length
     * @return original or shortened text
     * @author Waliur Sun
     */
    private String limitText(
            String value,
            int maximumLength) {

        if (value == null) {
            return "";
        }

        if (value.length() <= maximumLength) {
            return value;
        }

        if (maximumLength <= 3) {
            return value.substring(
                    0,
                    maximumLength);
        }

        return value.substring(
                0,
                maximumLength - 3)
                + "...";
    }

    /**
     * Writes one properly escaped row to a CSV file.
     *
     * @param writer writer connected to the CSV file
     * @param values values to write as one row
     * @author Waliur Sun
     */
    private void writeCsvRow(
            PrintWriter writer,
            String... values) {

        StringBuilder row =
                new StringBuilder();

        for (int index = 0;
                index < values.length;
                index++) {

            if (index > 0) {
                row.append(",");
            }

            row.append(
                    escapeCsv(values[index]));
        }

        writer.println(row);
    }

    /**
     * Escapes one value so it can safely be stored in a CSV field.
     *
     * @param value value to escape
     * @return safely escaped CSV value
     * @author Waliur Sun
     */
    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        String escapedValue =
                value.replace(
                        "\"",
                        "\"\"");

        boolean requiresQuotationMarks =
                escapedValue.contains(",")
                        || escapedValue.contains("\"")
                        || escapedValue.contains("\n")
                        || escapedValue.contains("\r");

        if (requiresQuotationMarks) {
            return "\""
                    + escapedValue
                    + "\"";
        }

        return escapedValue;
    }

    /**
     * Formats a decimal value for CSV export.
     *
     * @param value decimal value
     * @return value formatted with two decimal places
     * @author Waliur Sun
     */
    private String formatDecimal(double value) {

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    "Report values must be finite numbers.");
        }

        return String.format(
                Locale.US,
                "%.2f",
                value);
    }

    /**
     * Validates the insight result.
     *
     * @param result insight result to validate
     * @author Waliur Sun
     */
    private void validateResult(InsightResult result) {

        if (result == null) {
            throw new IllegalArgumentException(
                    "InsightResult cannot be null.");
        }
    }

    /**
     * Validates the destination file path.
     *
     * @param filePath destination path
     * @author Waliur Sun
     */
    private void validateFilePath(String filePath) {

        if (filePath == null
                || filePath.isBlank()) {

            throw new IllegalArgumentException(
                    "File path cannot be null or blank.");
        }
    }
}