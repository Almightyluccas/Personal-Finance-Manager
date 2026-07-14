package reports;

import java.util.Scanner;

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
 * @version 1.0
 * @since 1.0
 */
public class ReportMenu {

    private Scanner scanner;

    /**
     * Constructs a new ReportMenu object.
     *
     * @author Sheikh Tanvir Hossain
     */
    public ReportMenu() {

        // Initialize Scanner for user input.
        scanner = new Scanner(System.in);

    }

    /**
     * Displays the report menu.
     *
     * <p>
     * Shows all available report options to the user.
     * </p>
     *
     * @author Sheikh Tanvir Hossain
     */
    public void displayReportMenu() {

        System.out.println();
        System.out.println("===== REPORT MENU =====");
        System.out.println("1. Annual Report");
        System.out.println("2. Monthly Summary");
        System.out.println("3. Category Totals");
        System.out.println("4. Budget Summary");
        System.out.println("5. Return to Main Menu");
        System.out.println("=======================");

    }

    /**
     * Allows the user to choose which report to generate.
     *
     * <p>
     * The selected report type will be passed to the ReportManager.
     * </p>
     *
     * @author Sheikh Tanvir Hossain
     */
    public void selectReportType() {

        displayReportMenu();

        System.out.print("Select a report type (1-5): ");

        if (scanner.hasNextInt()) {

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    System.out.println("Annual Report selected.");
                    break;

                case 2:
                    System.out.println("Monthly Summary selected.");
                    break;

                case 3:
                    System.out.println("Category Totals selected.");
                    break;

                case 4:
                    System.out.println("Budget Summary selected.");
                    break;

                case 5:
                    returnToMainMenu();
                    break;

                default:
                    System.out.println(
                            "Invalid selection. Please choose 1 through 5.");
                    break;
            }

        } else {

            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();

        }

    }

    /**
     * Allows the user to choose the report output format.
     *
     * <p>
     * Reports may be displayed on the console or exported as a CSV file.
     * </p>
     *
     * @author Sheikh Tanvir Hossain
     */
    public void selectOutputOption() {

        System.out.println();
        System.out.println("===== OUTPUT OPTIONS =====");
        System.out.println("1. Display on Console");
        System.out.println("2. Export to CSV File");
        System.out.println("==========================");

        System.out.print("Select an output option (1-2): ");

        if (scanner.hasNextInt()) {

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    System.out.println("Console output selected.");
                    break;

                case 2:
                    System.out.println("CSV output selected.");
                    break;

                default:
                    System.out.println(
                            "Invalid selection. Please choose 1 or 2.");
                    break;
            }

        } else {

            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();

        }

    }

    /**
     * Returns the user to the application's main menu.
     *
     * <p>
     * Transfers control back to the Integration module.
     * </p>
     *
     * @author Sheikh Tanvir Hossain
     */
    public void returnToMainMenu() {

        System.out.println();
        System.out.println("Returning to the main menu...");

    }

}
