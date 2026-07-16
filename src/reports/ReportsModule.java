package reports;

import integration.AppModule;

import integration.MenuUtil;

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
 * @version 1.0
 * @since 1.0
 */

public class ReportsModule implements AppModule {

    /**
     * Registered module name
     */
    private static final String MODULE_NAME = "reports";

    private ReportMenu reportMenu;
    private ReportManager reportManager;

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
        reportMenu = new ReportMenu();
        reportManager = new ReportManager();
    }

    @Override
    public void handleSelection() {
        boolean running = true;

        while (running) {
            String choice = MenuUtil.promptChoice(
                    "Reports Module",
                    "1. Annual Report",
                    "2. Monthly Summary",
                    "3. Category Totals",
                    "4. Budget Summary",
                    "0. Return to Main Menu"
            );
            switch (choice) {

                case "1":
                    reportManager.generateReport(ReportType.ANNUAL);
                    break;

                case "2":
                    reportManager.generateReport(ReportType.MONTHLY);
                    break;

                case "3":
                    reportManager.generateReport(ReportType.CATEGORY_TOTALS);
                    break;

                case "4":
                    reportManager.generateReport(ReportType.BUDGET_SUMMARY);
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }


}