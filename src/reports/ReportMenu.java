package reports;

import integration.MenuUtil;

/**
 * Displays and manages report-related menu options for the Personal Finance
 * Manager (PFM) application.
 *
 * <p>
 * This class provides menu options that allow users to select report types
 * and output methods after logging into the application.
 * </p>
 *
 * @author Sheikh Tanvir Hossain
 * @author Alyssa Johnson
 * @version 1.1
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
                "5. Return to Main Menu");

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

                case "5":
                    return null;

                default:
                    System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    /**
     * Allows the user to choose the report output format.
     *
     * @return the selected OutputType
     */
    public OutputType selectOutputOption() {

        while (true) {

            String choice = MenuUtil.promptChoice(
                    "Output Format",
                    "1. Display on Console",
                    "2. Export to CSV");

            switch (choice) {

                case "1":
                    return OutputType.CONSOLE;

                case "2":
                    return OutputType.CSV;

                default:
                    System.out.println("Invalid selection. Please try again.");
            }
        }
    }

    /**
     * Returns the user to the application's main menu.
     */
    public void returnToMainMenu() {

        System.out.println();
        System.out.println("Returning to the main menu...");

    }

}