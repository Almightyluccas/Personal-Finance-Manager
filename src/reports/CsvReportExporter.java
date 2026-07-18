package reports;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import storage.Budget;
import storage.FileUtil;

/**
 * Exports financial reports to CSV files for the Personal Finance Manager
 * (PFM) application.
 *
 * <p>
 * This class creates report files containing real budget data that can be
 * opened cleanly in spreadsheet applications.
 * </p>
 *
 * @author Alyssa Johnson
 * @version 2.0
 * @since 1.0
 */
public class CsvReportExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final FileUtil fileUtil;
    private final ReportFormatter formatter;

    /**
     * Constructs a new CsvReportExporter object.
     */
    public CsvReportExporter() {
        this(new FileUtil(), new ReportFormatter());
    }

    /**
     * Constructs an exporter with shared helpers.
     *
     * @param fileUtil file-system helper for report destinations
     * @param formatter shared formatter for consistent labels
     */
    CsvReportExporter(FileUtil fileUtil, ReportFormatter formatter) {
        this.fileUtil = fileUtil == null ? new FileUtil() : fileUtil;
        this.formatter = formatter == null ? new ReportFormatter() : formatter;
    }

    /**
     * Exports the annual financial report to a CSV file.
     *
     * @param budget the budget to export
     */
    public void exportAnnualReport(Budget budget) {
        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        ReportAnalytics.ReportTotals annual = ReportAnalytics.forBudget(budget);
        Path file = buildReportPath("annual_report_" + annual.year() + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writeSummaryRows(writer, annual, "Annual Financial Report");
            writer.newLine();
            writeRow(writer, "Category", "Income", "Expenses", "Net");
            for (ReportAnalytics.CategoryTotals category : annual.categories()) {
                writeRow(
                        writer,
                        category.category(),
                        decimal(category.income()),
                        decimal(category.expenses()),
                        decimal(category.net()));
            }

            writer.newLine();
            writeRow(writer, "Transaction Count", String.valueOf(annual.transactionCount()), "", "");
            System.out.println("Annual report exported to " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Unable to export the annual report: " + e.getMessage());
        }
    }

    /**
     * Exports a monthly summary.
     *
     * @param budget the budget
     * @param month the month (1-12)
     */
    public void exportMonthlySummary(Budget budget, int month) {
        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        if (!ReportAnalytics.isValidMonth(month)) {
            System.out.println("Please choose a month between 1 and 12.");
            return;
        }

        ReportAnalytics.ReportTotals monthly = ReportAnalytics.forMonth(budget, month);
        String monthName = formatter.formatMonth(month).toLowerCase();
        Path file = buildReportPath("monthly_summary_" + monthly.year() + "_" + monthName + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writeSummaryRows(writer, monthly, "Monthly Financial Summary");
            writeRow(writer, "Month", formatter.formatMonth(month), "", "");
            writer.newLine();
            writeRow(writer, "Date", "Category", "Type", "Amount");
            for (ReportAnalytics.ReportRow row : monthly.transactions()) {
                writeRow(
                        writer,
                        row.date().format(DATE_FORMAT),
                        formatter.normalizeCategory(row.category()),
                        row.amount() >= 0 ? "Income" : "Expense",
                        decimal(Math.abs(row.amount())));
            }

            writer.newLine();
            writeRow(writer, "Transaction Count", String.valueOf(monthly.transactionCount()), "", "");
            System.out.println("Monthly summary exported to " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Unable to export the monthly summary: " + e.getMessage());
        }
    }

    /**
     * Exports totals grouped by category.
     *
     * @param budget the budget
     */
    public void exportCategoryTotals(Budget budget) {
        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        ReportAnalytics.ReportTotals totals = ReportAnalytics.forBudget(budget);
        Path file = buildReportPath("category_totals_" + totals.year() + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writeRow(writer, "Category Totals Report", "", "", "");
            writeRow(writer, "Year", String.valueOf(totals.year()), "", "");
            writer.newLine();
            writeRow(writer, "Category", "Income", "Expenses", "Net");
            for (ReportAnalytics.CategoryTotals category : totals.categories()) {
                writeRow(
                        writer,
                        category.category(),
                        decimal(category.income()),
                        decimal(category.expenses()),
                        decimal(category.net()));
            }

            writer.newLine();
            writeRow(writer, "Overall Income", decimal(totals.income()), "", "");
            writeRow(writer, "Overall Expenses", decimal(totals.expenses()), "", "");
            writeRow(writer, "Overall Net", decimal(totals.net()), "", "");
            writeRow(writer, "Transaction Count", String.valueOf(totals.transactionCount()), "", "");
            System.out.println("Category totals exported to " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Unable to export category totals: " + e.getMessage());
        }
    }

    /**
     * Exports the budget summary.
     *
     * @param budget the budget
     */
    public void exportBudgetSummary(Budget budget) {
        if (budget == null) {
            System.out.println("No budget data is available for the selected year.");
            return;
        }

        ReportAnalytics.ReportTotals summary = ReportAnalytics.forBudget(budget);
        Path file = buildReportPath("budget_summary_" + summary.year() + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writeSummaryRows(writer, summary, "Budget Performance Summary");
            writer.newLine();
            writeRow(writer, "Performance Message", formatter.buildPerformanceMessage(summary), "", "");
            System.out.println("Budget summary exported to " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Unable to export the budget summary: " + e.getMessage());
        }
    }

    /**
     * Creates an empty CSV file.
     *
     * @param fileName the file name
     */
    public void createCsvFile(String fileName) {
        Path file = buildReportPath(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("");
        } catch (IOException e) {
            System.out.println("Unable to create file: " + e.getMessage());
        }
    }

    /**
     * Writes report data to a CSV file.
     *
     * @param fileName the output file
     * @param rows the rows to write
     */
    public void writeReportData(String fileName, String[] rows) {
        Path file = buildReportPath(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            if (rows != null) {
                for (String row : rows) {
                    writer.write(row == null ? "" : row);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to write report: " + e.getMessage());
        }
    }

    private Path buildReportPath(String fileName) {
        String cleanName = (fileName == null || fileName.isBlank()) ? "report.csv" : fileName.trim();
        Path reportsDirectory = fileUtil.resolvePath("reports");
        try {
            fileUtil.ensureDataDirectoryExists();
            Files.createDirectories(reportsDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare the reports directory.", e);
        }
        return reportsDirectory.resolve(cleanName).toAbsolutePath().normalize();
    }

    private void writeSummaryRows(
            BufferedWriter writer,
            ReportAnalytics.ReportTotals totals,
            String title) throws IOException {
        writeRow(writer, title, "", "", "");
        writeRow(writer, "Year", String.valueOf(totals.year()), "", "");
        writeRow(writer, "Income", decimal(totals.income()), "", "");
        writeRow(writer, "Expenses", decimal(totals.expenses()), "", "");
        writeRow(writer, "Net Balance", decimal(totals.net()), "", "");
        writeRow(writer, "Status", formatter.formatStatus(totals.net()), "", "");
        writeRow(writer, "Transaction Count", String.valueOf(totals.transactionCount()), "", "");
    }

    private void writeRow(BufferedWriter writer, String... values) throws IOException {
        if (values == null || values.length == 0) {
            writer.newLine();
            return;
        }

        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                writer.write(',');
            }
            writer.write(escape(values[index]));
        }
        writer.newLine();
    }

    private String escape(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    private String decimal(double amount) {
        return String.format("%.2f", amount);
    }
}
