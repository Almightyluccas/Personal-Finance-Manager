package storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides CRUD (Create, Read, Update, Delete) operations for persisting
 * {@link Budget} objects to and from data files.
 *
 * @author Mohammed, Ayub, Fuad
 */
public class BudgetStorage {

    /**
     * Shared file-system utility used to resolve paths relative to the
     * application's data directory.
     */
    private static final FileUtil FILE_UTIL = new FileUtil();

    /**
     * The base directory where budget files are stored, resolved under
     * the application's shared data directory.
     */
    private static final Path BASE_DIRECTORY = FILE_UTIL.resolvePath("budgets");

    /**
     * The required CSV header for budget files.
     */
    private static final String CSV_HEADER = "Date,Category,Amount";

    /**
     * Formatter for dates stored in CSV files.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Constructs a new {@code BudgetStorage} instance.
     *
     * @author Mohammed, Ayub, Fuad
     */
    public BudgetStorage() {
        ensureBaseDirectoryExists();
    }

    /**
     * Creates and persists a new budget for the given user and year.
     *
     * @param username the username who owns the budget
     * @param budget   the budget to create
     * @author Mohammed, Ayub, Fuad
     */
    public void createBudget(String username, Budget budget) {
        writeBudget(username, budget);
    }

    /**
     * Reads and returns the budget for the given user and year.
     *
     * @param username the username who owns the budget
     * @param year     the year of the budget to read
     * @return the requested budget, or {@code null} if it could not be read
     * @author Mohammed, Ayub, Fuad
     */
    public Budget readBudget(String username, int year) {
        Path budgetPath = getBudgetPath(username, year);

        if (!Files.exists(budgetPath)) {
            return null;
        }

        Budget budget = new Budget(year);

        try (BufferedReader reader = Files.newBufferedReader(budgetPath)) {
            String header = reader.readLine();

            if (!CSV_HEADER.equals(header)) {
                System.err.println("Invalid budget file header for year " + year + ".");
                return null;
            }

            String line;

            while ((line = reader.readLine()) != null) {
                Transaction transaction = parseTransaction(line);

                if (transaction != null) {
                    budget.addTransaction(transaction);
                }
            }

            return budget;
        } catch (IOException e) {
            System.err.println("Error reading budget: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates an existing budget with new data.
     *
     * @param username the username who owns the budget
     * @param budget   the budget containing updated data
     * @author Mohammed, Ayub, Fuad
     */
    public void updateBudget(String username, Budget budget) {
        writeBudget(username, budget);
    }

    /**
     * Deletes the budget for the given user and year.
     *
     * @param username the username who owns the budget
     * @param year     the year of the budget to delete
     * @author Mohammed, Ayub, Fuad
     */
    public void deleteBudget(String username, int year) {
        Path budgetPath = getBudgetPath(username, year);

        try {
            Files.deleteIfExists(budgetPath);
        } catch (IOException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
        }
    }

    /**
     * Determines whether a budget exists for the given user and year.
     *
     * @param username the username who owns the budget
     * @param year     the year to check
     * @return {@code true} if a budget exists for the given year, {@code false} otherwise
     * @author Mohammed, Ayub, Fuad
     */
    public boolean yearExists(String username, int year) {
        return Files.exists(getBudgetPath(username, year));
    }

    /**
     * Lists all years for which a budget exists for the given user.
     *
     * @param username the username to look up
     * @return a list of years that have an associated budget
     * @author Mohammed, Ayub, Fuad
     */
    public List<Integer> listYearsForUser(String username) {
        List<Integer> years = new ArrayList<>();
        Path userDirectory = getUserDirectory(username);

        if (!Files.exists(userDirectory)) {
            return years;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(userDirectory, "*.csv")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();

                if (fileName.endsWith(".csv")) {
                    String yearText = fileName.substring(0, fileName.length() - 4);

                    try {
                        years.add(Integer.parseInt(yearText));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid budget file: " + fileName);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error listing budget years: " + e.getMessage());
        }

        Collections.sort(years);
        return years;
    }

    /**
     * Writes a budget to its CSV file.
     *
     * @param username the username who owns the budget
     * @param budget   the budget to write
     * @author Mohammed, Ayub, Fuad
     */
    private void writeBudget(String username, Budget budget) {
        if (username == null || username.isBlank() || budget == null) {
            System.err.println("Cannot save budget. Username or budget is missing.");
            return;
        }

        Path userDirectory = getUserDirectory(username);
        Path budgetPath = getBudgetPath(username, budget.getYear());

        try {
            Files.createDirectories(userDirectory);

            try (BufferedWriter writer = Files.newBufferedWriter(budgetPath)) {
                writer.write(CSV_HEADER);
                writer.newLine();

                for (Transaction transaction : budget.getTransactions()) {
                    writer.write(formatTransaction(transaction));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing budget: " + e.getMessage());
        }
    }

    /**
     * Converts one transaction into a CSV row.
     *
     * @param transaction the transaction to format
     * @return a CSV row
     * @author Mohammed, Ayub, Fuad
     */
    private String formatTransaction(Transaction transaction) {
        return transaction.date().format(DATE_FORMATTER)
                + ","
                + transaction.category()
                + ","
                + transaction.amount();
    }

    /**
     * Converts one CSV row into a {@link Transaction}.
     *
     * @param line the CSV row to parse
     * @return a transaction, or {@code null} if the row is invalid
     * @author Mohammed, Ayub, Fuad
     */
    private Transaction parseTransaction(String line) {
        String[] parts = line.split(",", -1);

        if (parts.length != 3) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
            String category = parts[1].trim();
            double amount = Double.parseDouble(parts[2].trim());

            return new Transaction(date, category, amount);
        } catch (RuntimeException e) {
            System.err.println("Skipping invalid transaction row: " + line);
            return null;
        }
    }

    /**
     * Builds the file path for a user's budget year.
     *
     * @param username the username who owns the budget
     * @param year     the budget year
     * @return the budget file path
     * @author Mohammed, Ayub, Fuad
     */
    private Path getBudgetPath(String username, int year) {
        return getUserDirectory(username).resolve(year + ".csv");
    }

    /**
     * Builds the directory path for a user's budget files.
     *
     * @param username the username
     * @return the user budget directory path
     * @author Mohammed, Ayub, Fuad
     */
    private Path getUserDirectory(String username) {
        return BASE_DIRECTORY.resolve(cleanUsername(username));
    }

    /**
     * Creates the base budget directory if it does not already exist.
     *
     * @author Mohammed, Ayub, Fuad
     */
    private void ensureBaseDirectoryExists() {
        try {
            FILE_UTIL.ensureDataDirectoryExists();
            Files.createDirectories(BASE_DIRECTORY);
        } catch (IOException | IllegalStateException e) {
            System.err.println("Error creating budget directory: " + e.getMessage());
        }
    }

    /**
     * Cleans a username so it can be safely used as part of a file path.
     *
     * @param username the username to clean
     * @return a safe username for file paths
     * @author Mohammed, Ayub, Fuad
     */
    private String cleanUsername(String username) {
        if (username == null || username.isBlank()) {
            return "unknown_user";
        }
        // Replaces any characters that are unsafe for file names with underscores.
        return username.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}