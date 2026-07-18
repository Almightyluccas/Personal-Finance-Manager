package reports;
import accounts.Account;
import accounts.AccountService;
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

    private static final int CANCEL = 0;
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
        reportManager = new ReportManager();
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
            String choice = MenuUtil.promptChoice(
                    "Reports Module",
                    "1. Annual Report",
                    "2. Monthly Summary",
                    "3. Category Totals",
                    "4. Budget Summary",
                    "0. Return to Main Menu"
            );
switch (choice) {
                case "1" -> handleAnnualReport(username);
                case "2" -> handleMonthlySummary(username);
                case "3" -> handleCategoryTotals(username);
                case "4" -> handleBudgetSummary(username);
                case "0" -> running = false;
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }
//Submenu Actions
private void handleAnnualReport(String username) {
        int year = promptForYear();
        if (year == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }

        System.out.println("\nGenerating Annual Report for " + year + "...");
        reportManager.generateReport(ReportType.ANNUAL, username, year);
    }
    private void handleMonthlySummary(String username) {
        int year = promptForYear();
        if (year == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }
        int month = promptForMonth();
        if (month == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }

        System.out.println("\nGenerating Monthly Summary for " + month + "/" + year + "...");
        reportManager.generateReport(ReportType.MONTHLY, username, year, month);
    }
    private void handleCategoryTotals(String username) {
        int year = promptForYear();
        if (year == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }

        System.out.println("\nGenerating Category Totals Report for " + year + "...");
        reportManager.generateReport(ReportType.CATEGORY_TOTALS, username, year);
    }
    private void handleBudgetSummary(String username) {
        int year = promptForYear();
        if (year == CANCEL) {
            System.out.println("Cancelled.");
            return;
        }

        System.out.println("\nGenerating Budget Summary Report for " + year + "...");
        reportManager.generateReport(ReportType.BUDGET_SUMMARY, username, year);
    }
   
    /**
     * Prompts the user to enter a year, or 0 to cancel.
     *
     * @return the valid year, or CANCEL (0) if backed out or invalid
     */
private int promptForYear() {
        String input = MenuUtil.promptString("Enter year (or 0 to cancel)");
        try {
            int year = Integer.parseInt(input);
            if (year == CANCEL) {
                return CANCEL;
            }
            if (year < 1900 || year > 2100) { // Standard bounds check
                System.out.println("Please enter a realistic year (e.g., 2026).");
                return CANCEL;
            }
            return year;
        } catch (NumberFormatException e) {
            System.out.println("Invalid year entered.");
            return CANCEL;
        }
    }
    /**
     * Prompts the user to enter a month (1-12), or 0 to cancel.
     *
     * @return the valid month, or CANCEL (0) if backed out or invalid
     */
    private int promptForMonth() {
        String input = MenuUtil.promptString("Enter month (1-12, or 0 to cancel)");
        try {
            int month = Integer.parseInt(input);
            if (month == CANCEL) {
                return CANCEL;
            }
            if (month < 1 || month > 12) {
                System.out.println("Month must be between 1 and 12.");
                return CANCEL;
            }
            return month;
        } catch (NumberFormatException e) {
            System.out.println("Invalid month entered.");
            return CANCEL;
        }
    }

}
