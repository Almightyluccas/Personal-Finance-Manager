package reports;

/**
 * Coordinates the generation of financial reports for the Personal Finance
 * Manager (PFM) application. This class acts as the main controller for
 * creating annual, monthly, category, and budget summary reports.
 *
 * <p>
 * The ReportManager communicates with other modules, such as Storage and
 * Integration, to retrieve financial data and generate the requested reports.
 * Actual report generation logic will be implemented in future development.
 * </p>
 *
 * @author Alyssa Johnson
 * @author Tahsin Abid
 * @version 1.1
 * @since 1.0
 */
public class ReportManager {
    private final ConsoleReport consoleReport;
    private final CsvReportExporter csvExporter;
    private final ReportFormatter formatter;

    /**
     * Constructs a new ReportManager object.
     */
    public ReportManager() {
        consoleReport = new ConsoleReport();
        csvExporter = new CsvReportExporter();
        formatter = new ReportFormatter();

    }

    /**
     * Generates the selected report.
     *
     * @param type report type
     */
    public void generateReport(ReportType type) {
        switch (type) {
            case ANNUAL:
                consoleReport.printAnnualReport();
                break;

            case MONTHLY:
                consoleReport.printMonthlySummary();
                break;

            case CATEGORY_TOTALS:
                consoleReport.printCategoryTotals();
                break;

            case BUDGET_SUMMARY:
                consoleReport.printBudgetSummary();
                break;
            default:
                throw new IllegalArgumentException("Unknown report type.");
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
        // TODO: Use username and year once Storage integration is complete.
        generateReport(type);
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
        // TODO: Use username, year, and month once Storage integration is complete.
        generateReport(type);
    }

    /**
     * Returns the formatter used by this manager.
     *
     * @return ReportFormatter
     */
    public ReportFormatter getFormatter() {
        return formatter;
    }

}