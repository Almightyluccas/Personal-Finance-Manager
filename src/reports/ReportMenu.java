package reports;

import integration.MenuUtil;
import validation.Validation;

/**
 * Displays and manages report-related menu options for the Personal Finance
 * Manager (PFM) application.
 *
 * <p>
 * This class prompts the user for report type, output type, year, and month
 * using the Integration module's shared console utilities.
 * </p>
 *
 * @author Sheikh Tanvir Hossain
 * @author Alyssa Johnson
 * @version 2.0
 * @since 1.0
 */
public class ReportMenu {

    /**
     * Constructs a new ReportMenu object.
     */
    public ReportMenu() {
    }

    /**
     * Displays the report menu.
     *
     * @return the user's menu selection
     */
    public String displayReportMenu() {
        return MenuUtil.promptChoice(
                "Reports Menu",
                "1. Annual Report",
                "2. Monthly Summary",
                "3. Category Totals",
                "4. Budget Summary",
                "0. Return to Main Menu");
    }

    /**
     * Allows the user to choose which report to generate.
     *
     * @return the selected ReportType, or null if returning
     */
    public ReportType selectReportType() {
        while (true) {
            String choice = displayReportMenu();

            switch (choice) {
                case "1":
                    return ReportType.ANNUAL;
                case "2":
                    return ReportType.MONTHLY;
                case "3":
                    return ReportType.CATEGORY_TOTALS;
                case "4":
                    return ReportType.BUDGET_SUMMARY;
                case "0":
                    return null;
                default:
                    System.out.println("Invalid report choice. Please try again.");
            }
        }
    }

    /**
     * Allows the user to choose the report output format.
     *
     * @return the selected OutputType, or null if returning
     */
    public OutputType selectOutputOption() {
        while (true) {
            String choice = MenuUtil.promptChoice(
                    "Output Format",
                    "1. Display on Console",
                    "2. Export to CSV",
                    "0. Return to Reports Menu");

            switch (choice) {
                case "1":
                    return OutputType.CONSOLE;
                case "2":
                    return OutputType.CSV;
                case "0":
                    return null;
                default:
                    System.out.println("Invalid output choice. Please try again.");
            }
        }
    }

    /**
     * Prompts the user for a year.
     *
     * @return the selected year, or 0 when cancelled
     */
    public int promptForYear() {
        String input = MenuUtil.promptString("Enter year (or 0 to cancel)");
        try {
            int year = Integer.parseInt(input);
            if (year == 0) {
                return 0;
            }
            if (year < Validation.MIN_YEAR || year > Validation.MAX_YEAR) {
                System.out.println("Please enter a year between "
                        + Validation.MIN_YEAR + " and " + Validation.MAX_YEAR + ".");
                return 0;
            }
            return year;
        } catch (NumberFormatException e) {
            System.out.println("Invalid year entered.");
            return 0;
        }
    }

    /**
     * Prompts the user for a month.
     *
     * @return the selected month, or 0 when cancelled
     */
    public int promptForMonth() {
        String input = MenuUtil.promptString("Enter month (1-12, or 0 to cancel)");
        try {
            int month = Integer.parseInt(input);
            if (month == 0) {
                return 0;
            }
            if (!ReportAnalytics.isValidMonth(month)) {
                System.out.println("Month must be between 1 and 12.");
                return 0;
            }
            return month;
        } catch (NumberFormatException e) {
            System.out.println("Invalid month entered.");
            return 0;
        }
    }

    /**
     * Returns the user to the application's main menu.
     */
    public void returnToMainMenu() {
        System.out.println();
        System.out.println("Returning to the main menu...");
    }

    /**
     * Asks whether the user wants to generate another report.
     *
     * @return true to stay in the Reports module, false to return
     */
    public boolean selectAfterReportAction() {
        while (true) {
            String choice = MenuUtil.promptChoice(
                    "Reports Module",
                    "1. Generate Another Report",
                    "0. Return to Main Menu");

            switch (choice) {
                case "1":
                    return true;
                case "0":
                    return false;
                default:
                    System.out.println("Invalid selection. Please try again.");
            }
        }
    }
}
