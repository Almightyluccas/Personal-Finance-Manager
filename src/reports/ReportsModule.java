package reports;

import accounts.Account;
import accounts.AccountService;
import integration.AppModule;

/**
 * Entry point for the Reports module.
 * <p>
 * Implements {@link AppModule} so the Integration layer can invoke
 * the Reports module without knowing its internal implementation.
 * This class owns the Reports submenu and delegates report generation
 * and display to the other classes in the reports package.
 * </p>
 *
 * @author Tahsin Abid
 * @version 2.0
 * @since 1.0
 */
public class ReportsModule implements AppModule {

    /**
     * Registered module name
     */
    private static final String MODULE_NAME = "reports";

    private static final int CANCEL = 0;

    private ReportManager reportManager;
    private ReportMenu reportMenu;

    /**
     * Constructs a ReportsModule.
     */
    public ReportsModule() {
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public void initialize() {
        reportManager = new ReportManager();
        reportMenu = new ReportMenu();
    }

    @Override
    public void handleSelection() {
        Account currentUser = AccountService.SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("You must be logged in to use the Reports module.");
            return;
        }

        String username = currentUser.getUsername();
        boolean running = true;

        while (running) {
            ReportType reportType = reportMenu.selectReportType();
            if (reportType == null) {
                reportMenu.returnToMainMenu();
                return;
            }

            OutputType outputType = reportMenu.selectOutputOption();
            if (outputType == null) {
                continue;
            }

            int year = reportMenu.promptForYear();
            if (year == CANCEL) {
                System.out.println("Cancelled.");
                continue;
            }

            if (reportType == ReportType.MONTHLY) {
                int month = reportMenu.promptForMonth();
                if (month == CANCEL) {
                    System.out.println("Cancelled.");
                    continue;
                }
                reportManager.generateReport(reportType, username, year, month, outputType);
            } else {
                reportManager.generateReport(reportType, username, year, outputType);
            }

            running = reportMenu.selectAfterReportAction();
        }

        reportMenu.returnToMainMenu();
    }
}
