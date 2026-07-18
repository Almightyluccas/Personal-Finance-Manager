package reports;

import storage.Budget;
import storage.BudgetStorage;

/**
 * Coordinates the generation of financial reports for the Personal Finance
 * Manager (PFM) application.
 *
 * <p>
 * This class loads the requested budget from Storage and routes the request
 * to the correct console or CSV report generator.
 * </p>
 *
 * @author Alyssa Johnson
 * @author Tahsin Abid
 * @version 2.0
 * @since 1.0
 */
public class ReportManager {
    private final ConsoleReport consoleReport;
    private final CsvReportExporter csvExporter;
    private final ReportFormatter formatter;
    private final BudgetStorage budgetStorage;

    /**
     * Constructs a new ReportManager object.
     */
    public ReportManager() {
        formatter = new ReportFormatter();
        budgetStorage = new BudgetStorage();
        consoleReport = new ConsoleReport(formatter);
        csvExporter = new CsvReportExporter(null, formatter);
    }

    /**
     * Generates the selected report.
     *
     * @param type report type
     * @param budget selected budget data
     * @param output selected output type
     * @param month selected month for monthly reports
     */
    public void generateReport(
            ReportType type,
            Budget budget,
            OutputType output,
            int month) {
        if (type == null) {
            System.out.println("Please choose a valid report type.");
            return;
        }
        if (output == null) {
            System.out.println("Please choose a valid output option.");
            return;
        }
        if (budget == null) {
            System.out.println("No stored budget was found for the selected year.");
            return;
        }

        switch (output) {
            case CONSOLE -> renderConsole(type, budget, month);
            case CSV -> exportCsv(type, budget, month);
            default -> System.out.println("Please choose a valid output option.");
        }
    }

    /**
     * Generates a report that requires a username and year.
     *
     * @param type     the type of report
     * @param username the current user
     * @param year     the selected year
     */
    public void generateReport(ReportType type, String username, int year) {
        generateReport(type, username, year, OutputType.CONSOLE);
    }

    /**
     * Generates a report that requires a username, year, and output type.
     *
     * @param type the report type
     * @param username the current user
     * @param year the selected year
     * @param output the selected output type
     */
    public void generateReport(ReportType type, String username, int year, OutputType output) {
        Budget budget = loadBudget(username, year);
        if (budget != null) {
            generateReport(type, budget, output, 0);
        }
    }

    /**
     * Generates a monthly report.
     *
     * @param type     the report type
     * @param username the current user
     * @param year     selected year
     * @param month    selected month
     */
    public void generateReport(ReportType type, String username, int year, int month) {
        generateReport(type, username, year, month, OutputType.CONSOLE);
    }

    /**
     * Generates a report that requires a username, year, month, and output type.
     *
     * @param type the report type
     * @param username the current user
     * @param year selected year
     * @param month selected month
     * @param output selected output type
     */
    public void generateReport(ReportType type, String username, int year, int month, OutputType output) {
        Budget budget = loadBudget(username, year);
        if (budget != null) {
            generateReport(type, budget, output, month);
        }
    }

    /**
     * Returns the formatter used by this manager.
     *
     * @return ReportFormatter
     */
    public ReportFormatter getFormatter() {
        return formatter;
    }

    private Budget loadBudget(String username, int year) {
        if (username == null || username.isBlank()) {
            System.out.println("A logged-in user is required before reports can be generated.");
            return null;
        }

        try {
            if (!budgetStorage.yearExists(username, year)) {
                System.out.println("No stored budget was found for " + year + ".");
                return null;
            }
            return budgetStorage.readBudget(username, year);
        } catch (RuntimeException e) {
            System.out.println("Unable to load budget data for " + year + ": " + e.getMessage());
            return null;
        }
    }

    private void renderConsole(ReportType type, Budget budget, int month) {
        switch (type) {
            case ANNUAL -> consoleReport.printAnnualReport(budget);
            case MONTHLY -> consoleReport.printMonthlySummary(budget, month);
            case CATEGORY_TOTALS -> consoleReport.printCategoryTotals(budget);
            case BUDGET_SUMMARY -> consoleReport.printBudgetSummary(budget);
            default -> System.out.println("Please choose a valid report type.");
        }
    }

    private void exportCsv(ReportType type, Budget budget, int month) {
        switch (type) {
            case ANNUAL -> csvExporter.exportAnnualReport(budget);
            case MONTHLY -> csvExporter.exportMonthlySummary(budget, month);
            case CATEGORY_TOTALS -> csvExporter.exportCategoryTotals(budget);
            case BUDGET_SUMMARY -> csvExporter.exportBudgetSummary(budget);
            default -> System.out.println("Please choose a valid report type.");
        }
    }
}
