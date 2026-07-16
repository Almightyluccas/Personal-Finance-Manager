package insights;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
     * @param result completed insight result
     * @return formatted report text
     * @author Waliur Sun
     */
    public String generateSummary(InsightResult result) {

        validateResult(result);

        StringBuilder report = new StringBuilder();

        report.append("=====================================\n");
        report.append(" Financial Insights Report\n");
        report.append("=====================================\n\n");

        report.append("Year: ")
                .append(result.year())
                .append("\n");

        report.append("Total Income: $")
                .append(result.totalIncome())
                .append("\n");

        report.append("Total Expenses: $")
                .append(result.totalExpenses())
                .append("\n");

        report.append("Net Balance: $")
                .append(result.netBalance())
                .append("\n");

        report.append("Budget Status: ")
                .append(result.budgetStatus())
                .append("\n");

        report.append("Average Monthly Spending: $")
                .append(formatDecimal(
                        result.averageMonthlySpending()))
                .append("\n\n");

        report.append("Monthly Totals\n");
        report.append("-----------------------------\n");

        for (Map.Entry<Integer, Integer> entry
                : result.monthlyTotals().entrySet()) {

            report.append("Month ")
                    .append(entry.getKey())
                    .append(": $")
                    .append(entry.getValue())
                    .append("\n");
        }

        report.append("\nExpense Categories\n");
        report.append("-----------------------------\n");

        for (Map.Entry<String, Integer> entry
                : result.categoryTotals().entrySet()) {

            double percentage =
                    result.categoryPercentages()
                            .getOrDefault(
                                    entry.getKey(),
                                    0.0);

            report.append(entry.getKey())
                    .append(": $")
                    .append(entry.getValue())
                    .append(" (")
                    .append(formatDecimal(percentage))
                    .append("%)\n");
        }

        report.append("\nRecommendations\n");
        report.append("-----------------------------\n");

        if (result.recommendations().isEmpty()) {
            report.append("- No recommendations available.\n");
        } else {
            for (String recommendation
                    : result.recommendations()) {

                report.append("- ")
                        .append(recommendation)
                        .append("\n");
            }
        }

        return report.toString();
    }

    /**
     * Prints the formatted insight report to the console.
     *
     * @param result completed insight result
     * @author Waliur Sun
     */
    public void printReport(InsightResult result) {
        System.out.println(generateSummary(result));
    }

    /**
     * Saves the insight report to a CSV file.
     *
     * <p>All text fields are escaped so commas, quotation marks,
     * and line breaks do not corrupt the CSV structure.</p>
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
                    formatDecimal(
                            result.averageMonthlySpending()),
                    "");

            for (Map.Entry<Integer, Integer> entry
                    : result.monthlyTotals().entrySet()) {

                writeCsvRow(
                        writer,
                        "Month",
                        Integer.toString(entry.getKey()),
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
     * Writes one properly escaped row to a CSV file.
     *
     * @param writer writer connected to the CSV file
     * @param values values to write as one row
     * @author Waliur Sun
     */
    private void writeCsvRow(
            PrintWriter writer,
            String... values) {

        StringBuilder row = new StringBuilder();

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
     * Escapes one value so it can be safely stored in a CSV field.
     *
     * <p>Quotation marks are doubled. Values containing commas,
     * quotation marks, carriage returns, or line breaks are surrounded
     * by quotation marks.</p>
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
                value.replace("\"", "\"\"");

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
     * Formats a decimal value using two decimal places.
     *
     * <p>{@link Locale#US} ensures that CSV numbers use a period
     * instead of a locale-dependent comma as the decimal separator.</p>
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

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "File path cannot be null or blank.");
        }
    }
}