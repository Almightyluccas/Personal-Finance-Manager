package integration;

import java.util.Scanner;

/**
 * Stateless static helper providing common console prompting utilities
 * used across menus.
 */
public final class MenuUtil {

    private static final Scanner scanner = new Scanner(System.in);
    private static final int BORDER_PADDING = 2;

    private MenuUtil() {
    }

    /**
     * Returns the shared {@link Scanner} instance wrapping {@code System.in}.
     * Modules should generally use {@link #promptString(String)},
     * {@link #promptChoice(String, String...)}, or
     * {@link #promptYesNo(String)} instead of reading from this directly,
     * but it's available for cases those don't cover.
     *
     * @return the shared scanner instance
     * @author Luccas Amorim
     */
    public static Scanner getScanner() {
        return scanner;
    }

    /**
     * Takes in a String and prints out the string formatted inside a box
     *
     * @param title the tittle to be printed out
     * @author Luccas Amorim
     */
    public static void printTitle(String title) {
        int width = title.length() + (BORDER_PADDING * 2);
        String top = "┌" + "─".repeat(width) + "┐";
        String bottom = "└" + "─".repeat(width) + "┘";
        String middle = "│" + " ".repeat(BORDER_PADDING) + title + " ".repeat(BORDER_PADDING) + "│";

        System.out.println(top);
        System.out.println(middle);
        System.out.println(bottom);
    }

    /**
     * Prompts the user to choose from a set of numbered options, printing
     * a bordered title first.
     *
     * @param title   menu title shown above the options
     * @param options the option lines to display, e.g. {"1. View", "2. Exit"}
     * @return the raw trimmed input line entered by the user
     * @author Mohsen Kanj
     * @author Luccas Amorim
     */
    public static String promptChoice(String title, String... options) {
        int width = title.length() + (BORDER_PADDING * 2);
        for (String option : options) {
            width = Math.max(width, option.length() + (BORDER_PADDING * 2));
        }

        System.out.println("┌" + "─".repeat(width) + "┐");
        System.out.println("│" + center(title, width) + "│");
        System.out.println("├" + "─".repeat(width) + "┤");
        for (String option : options) {
            System.out.println("│" + " ".repeat(BORDER_PADDING) + pad(option, width - BORDER_PADDING) + "│");
        }
        System.out.println("└" + "─".repeat(width) + "┘");
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    /**
     * Prompts the user for a yes/no response.
     *
     * @param prompt the prompt message
     * @return true for yes, false for no
     * @author Mohsen Kanj
     */
    public static boolean promptYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            }

            if (input.equals("n") || input.equals("no")) {
                return false;
            }

            System.err.println("Please enter y or n.");
        }
    }

    /**
     * Prompts the user for a free-form string input.
     *
     * @param prompt the prompt text to display
     * @return the raw trimmed input line entered by the user
     * @author Luccas Amorim
     */
    public static String promptString(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Centers text within the given width, padding evenly on both sides.
     * @author Luccas Amorim
     */
    private static String center(String text, int width) {
        int totalPadding = width - text.length();
        int left = totalPadding / 2;
        int right = totalPadding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    /**
     * Left-aligns text, padding with spaces on the right to fill the width.
     * @author Luccas Amorim
     *
     */
    private static String pad(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }
}