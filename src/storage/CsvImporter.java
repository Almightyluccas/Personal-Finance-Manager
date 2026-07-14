package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reading and parsing of CSV files into {@link Transaction} records,
 * including previewing file contents and filtering out invalid rows.
 *
 * @author Mohammed, Ayub, Fuad
 */
public class CsvImporter {

    private static final String EXPECTED_HEADER = "Date,Category,Amount";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final int PREVIEW_LINE_LIMIT = 20;

    /**
     * Constructs a new {@code CsvImporter} instance.
     *
     * @author Mohammed, Ayub, Fuad
     */
    public CsvImporter() {
    }

    /**
     * Returns a preview of the contents of the specified CSV file without
     * fully parsing it into transactions. Useful for showing the user a
     * quick look at the file before committing to a full import.
     *
     * @param filePath the path to the CSV file
     * @return a list of raw lines representing a preview of the file
     * @author Mohammed, Ayub, Fuad
     */
    public List<String> previewCsvFile(String filePath) {
        List<String> preview = new ArrayList<>();
        try {
            List<String> allLines = Files.readAllLines(Path.of(filePath));
            int limit = Math.min(allLines.size(), PREVIEW_LINE_LIMIT);
            for (int i = 0; i < limit; i++) {
                preview.add(allLines.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read file for preview: " + filePath, e);
        }
        return preview;
    }

    /**
     * Parses the specified CSV file into a list of transactions. Rows that
     * fail to parse are included in the returned list as {@code null}
     * entries so the caller can report how many rows were invalid; use
     * {@link #filterInvalidRecords(List)} to strip them out before storing.
     *
     * @param filePath the path to the CSV file
     * @return a list of parsed transactions (may contain {@code null} for
     *         malformed rows)
     * @author Mohammed, Ayub, Fuad
     */
    public List<Transaction> parseCsvFile(String filePath) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of(filePath));
            if (lines.isEmpty()) {
                return transactions;
            }

            String header = lines.get(0).trim();
            if (!header.equalsIgnoreCase(EXPECTED_HEADER)) {
                throw new IllegalArgumentException(
                        "Invalid CSV header. Expected \"" + EXPECTED_HEADER + "\" but found \"" + header + "\"");
            }

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                transactions.add(parseLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read CSV file: " + filePath, e);
        }
        return transactions;
    }

    /**
     * Parses a single line of CSV text into a transaction. Returns
     * {@code null} if the line is malformed (wrong number of fields, an
     * unparseable date, or a non-numeric amount) rather than throwing, so
     * callers can tally invalid rows without a try/catch per line.
     *
     * @param line the raw CSV line to parse
     * @return the parsed transaction, or {@code null} if the line is
     *         malformed
     * @author Mohammed, Ayub, Fuad
     */
    public Transaction parseLine(String line) {
        String[] fields = line.split(",");
        if (fields.length != 3) {
            return null;
        }

        String rawDate = fields[0].trim();
        String category = fields[1].trim();
        String rawAmount = fields[2].trim();

        LocalDate date;
        try {
            date = LocalDate.parse(rawDate, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }

        if (category.isEmpty()) {
            return null;
        }

        double amount;
        try {
            amount = Double.parseDouble(rawAmount);
        } catch (NumberFormatException e) {
            return null;
        }

        return new Transaction(date, category, amount);
    }

    /**
     * Filters out invalid or malformed records from a list of parsed
     * transactions. Currently removes {@code null} entries produced by
     * {@link #parseLine(String)} for rows that failed to parse.
     *
     * @param transactions the transactions to filter
     * @return a list containing only valid transactions
     * @author Mohammed, Ayub, Fuad
     */
    public List<Transaction> filterInvalidRecords(List<Transaction> transactions) {
        List<Transaction> valid = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t != null) {
                valid.add(t);
            }
        }
        return valid;
    }
}