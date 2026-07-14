package reports;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import storage.Budget;
import storage.Transaction;

/**
 * Exports financial reports to CSV files for the Personal Finance Manager
 * (PFM) application.
 *
 * <p>
 * This class is responsible for creating CSV files containing report data.
 * The exported files can be opened using spreadsheet applications for further
 * analysis or record keeping.
 * </p>
 *
 * @author Alyssa Johnson
 * @version 1.2
 * @since 1.0
 */
public class CsvReportExporter {

    /**
     * Constructs a new CsvReportExporter object.
     */
    public CsvReportExporter() {

        // No setup is needed yet because each method writes a small placeholder file.
        // TODO: Add file path configuration later if the Integration module requires it.

    }

    /**
     * Exports the annual financial report to a CSV file.
     *
     * @param budget the budget to export
     */
    public void exportAnnualReport(Budget budget) {

        if (budget == null) {
            System.out.println("No budget data available.");
            return;
        }

        String fileName = "annual_report_" + budget.getYear() + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            writer.write("Date,Category,Amount");
            writer.newLine();

            for (Transaction transaction : budget.getTransactions()) {

                writer.write(transaction.date() + ","
                        + transaction.category() + ","
                        + transaction.amount());

                writer.newLine();
            }

            System.out.println("Annual report exported to " + fileName);

        } catch (IOException e) {
            System.out.println("Error exporting annual report: " + e.getMessage());
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
            System.out.println("No budget data available.");
            return;
        }

        String fileName = "monthly_summary_" + month + ".csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            writer.write("Date,Category,Amount");
            writer.newLine();

            for (Transaction transaction : budget.getTransactionsByMonth(month)) {

                writer.write(transaction.date() + ","
                        + transaction.category() + ","
                        + transaction.amount());

                writer.newLine();
            }

            System.out.println("Monthly report exported to " + fileName);

        } catch (IOException e) {
            System.out.println("Error exporting monthly report: " + e.getMessage());
        }

    }

    /**
     * Exports totals grouped by category.
     *
     * @param budget the budget
     */
    public void exportCategoryTotals(Budget budget) {

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

        String fileName = "category_totals.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            writer.write("Category,Total");
            writer.newLine();

            for (String category : totals.keySet()) {

                writer.write(category + "," + totals.get(category));
                writer.newLine();

            }

            System.out.println("Category totals exported.");

        } catch (IOException e) {

            System.out.println("Error exporting category totals: " + e.getMessage());

        }

    }

    /**
     * Exports the budget summary.
     *
     * @param budget the budget
     */
    public void exportBudgetSummary(Budget budget) {

        if (budget == null) {
            System.out.println("No budget data available.");
            return;
        }

        double balance = 0;

        for (Transaction transaction : budget.getTransactions()) {

            balance += transaction.amount();

        }

        String fileName = "budget_summary.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            writer.write("Budget Year," + budget.getYear());
            writer.newLine();
            writer.write("Overall Balance," + balance);
            writer.newLine();

            if (balance >= 0) {
                writer.write("Status,Surplus");
            } else {
                writer.write("Status,Deficit");
            }

            writer.newLine();

            System.out.println("Budget summary exported.");

        } catch (IOException e) {

            System.out.println("Error exporting budget summary: " + e.getMessage());

        }

    }

    /**
     * Creates an empty CSV file.
     *
     * @param fileName the file name
     */
    public void createCsvFile(String fileName) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

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

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            for (String row : rows) {

                writer.write(row);
                writer.newLine();

            }

        } catch (IOException e) {

            System.out.println("Unable to write report: " + e.getMessage());

        }

    }

    /**
     * Writes the provided lines to a CSV file.
     *
     * @param fileName the name of the CSV file to create
     * @param csvLines the CSV rows to write to the file
     */
    private void writeLinesToCsv(String fileName, String[] csvLines) {

        // try-with-resources closes the writer automatically, even if writing fails.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            // Each string is already formatted as a simple CSV row for this placeholder.
            for (String csvLine : csvLines) {

                // Write one row at a time so the file is easy to understand and debug.
                writer.write(csvLine);
                writer.newLine();

            }

            // This message confirms successful export without requiring another class.
            System.out.println("Exported CSV file: " + fileName);

        } catch (IOException exception) {

            // Keep error handling beginner-friendly until the project has shared utilities.
            // TODO: Replace this with Validation or Integration error handling later.
            System.out.println("Unable to export CSV file: " + exception.getMessage());

        }

    }

}
