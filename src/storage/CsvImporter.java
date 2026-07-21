package storage;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handles reading and parsing of CSV files into {@link Transaction} records,
 * including previewing file contents and filtering out invalid rows.
 *
 * @author Ayub
 */
public class CsvImporter {

    private static final String EXPECTED_HEADER = "Date,Category,Amount";
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final int PREVIEW_LINE_LIMIT = 20;

    /**
     * Windows-reserved device names that cannot be used as a file name
     * (with or without an extension) on that platform.
     */
    private static final Set<String> RESERVED_FILE_NAMES = Set.of(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );

    /** Characters that are not permitted in a file name on common platforms. */
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[<>:\"|?*\\x00-\\x1F]");

    /**
     * Constructs a new {@code CsvImporter} instance.
     *
     * @author Ayub
     */
    public CsvImporter() {
    }

    /**
     * Returns a preview of the contents of the specified CSV file without fully
     * parsing it into transactions. Useful for showing the user a quick look at
     * the file before committing to a full import.
     *
     * @param filePath the path to the CSV file
     * @return a list of raw lines representing a preview of the file
     * @author Ayub
     */
    public List<String> previewCsvFile(String filePath) {
        validateFilePath(filePath);
        List<String> preview = new ArrayList<>();
        try {
            List<String> allLines = Files.readAllLines(Path.of(filePath));
            int limit = Math.min(allLines.size(), PREVIEW_LINE_LIMIT);
            for (int i = 0; i < limit; i++) {
                preview.add(allLines.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(friendlyReadErrorMessage(filePath, e), e);
        }
        return preview;
    }

    /**
     * Parses the specified CSV file into a list of transactions. Rows that fail
     * to parse are included in the returned list as {@code null} entries so the
     * caller can report how many rows were invalid; use
     * {@link #filterInvalidRecords(List)} to strip them out before storing.
     *
     * @param filePath the path to the CSV file
     * @return a list of parsed transactions (may contain {@code null} for
     * malformed rows)
     * @author Ayub
     */
    public List<Transaction> parseCsvFile(String filePath) {
        validateFilePath(filePath);
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
            throw new RuntimeException(friendlyReadErrorMessage(filePath, e), e);
        }
        return transactions;
    }

    /**
     * Validates that a file path is usable before it is opened, catching
     * problems (blank paths, illegal characters, reserved device names) with
     * a clear message instead of letting the file system raise a cryptic
     * low-level error later.
     *
     * @param filePath the file path to validate
     * @throws IllegalArgumentException if the path is null, blank, malformed,
     * contains characters that are not valid in a file name, or uses a
     * reserved file name
     * @author Mohammed
     */
    private void validateFilePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path must not be null or empty.");
        }

        Path path;
        try {
            path = Path.of(filePath);
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException(
                    "\"" + filePath + "\" is not a valid file path on this system.", e);
        }

        Path fileNamePart = path.getFileName();
        if (fileNamePart == null) {
            throw new IllegalArgumentException("\"" + filePath + "\" does not include a file name.");
        }
        String fileName = fileNamePart.toString();

        if (INVALID_FILENAME_CHARS.matcher(fileName).find()) {
            throw new IllegalArgumentException(
                    "\"" + fileName + "\" contains characters that are not allowed in a file name.");
        }

        String baseName = fileName.contains(".") ? fileName.substring(0, fileName.indexOf('.')) : fileName;
        if (RESERVED_FILE_NAMES.contains(baseName.toUpperCase())) {
            throw new IllegalArgumentException(
                    "\"" + fileName + "\" is a reserved system file name and cannot be used.");
        }
    }

    /**
     * Builds a user-friendly message for an {@link IOException} raised while
     * reading a CSV file, tailored to the most common causes (missing file,
     * permissions) rather than surfacing a raw stack trace to the user.
     *
     * @param filePath the file path that failed to read
     * @param e the underlying I/O exception
     * @return a clear, user-facing description of what went wrong
     * @author Mohammed
     */
    private String friendlyReadErrorMessage(String filePath, IOException e) {
        if (e instanceof NoSuchFileException) {
            return "Could not find a file at \"" + filePath + "\". Please check the path and try again.";
        }
        if (e instanceof AccessDeniedException) {
            return "Permission denied while reading \"" + filePath + "\". "
                    + "Check that the file isn't open elsewhere and that you have permission to read it.";
        }
        return "Something went wrong while reading \"" + filePath + "\". "
                + "Please make sure it is a valid, readable CSV file and try again.";
    }

    /**
     * Parses a single line of CSV text into a transaction. Returns {@code null}
     * if the line is malformed (wrong number of fields, an unparseable date, or
     * a non-numeric amount) rather than throwing, so callers can tally invalid
     * rows without a try/catch per line.
     *
     * @bug Previously {@code Double.parseDouble} accepted the literal
     * strings "Infinity", "-Infinity", and "NaN" as valid amounts, letting
     * nonsensical transactions through. Non-finite amounts are now rejected
     * the same way malformed numbers already were.
     *
     * @param line the raw CSV line to parse
     * @return the parsed transaction, or {@code null} if the line is malformed
     * @author Ayub
     */
    public Transaction parseLine(String line) {
        List<String> fields = splitCsvLine(line);
        if (fields.size() != 3) {
            return null;
        }

        String rawDate = fields.get(0).trim();
        String category = fields.get(1).trim();
        String rawAmount = fields.get(2).trim();

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

        if (!Double.isFinite(amount)) {
            return null;
        }

        return new Transaction(date, category, amount);
    }

    /**
     * Splits one line of CSV text into fields, honouring quoted fields so that
     * commas inside quotation marks (e.g. {@code "Food, Dining"}) are kept as
     * part of a single field rather than treated as separators. A doubled quote
     * ({@code ""}) inside a quoted field is read as one literal quote
     * character.
     *
     * @param line the raw CSV line to split
     * @return the parsed fields, with surrounding quotes removed
     * @author Fuad
     */
    private List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentCharacter = line.charAt(i);

            if (currentCharacter == '"') {
                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (currentCharacter == ',' && !insideQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(currentCharacter);
            }
        }

        fields.add(currentField.toString());
        return fields;
    }

    /**
     * Filters out invalid or malformed records from a list of parsed
     * transactions. Currently removes {@code null} entries produced by
     * {@link #parseLine(String)} for rows that failed to parse.
     *
     * @param transactions the transactions to filter
     * @return a list containing only valid transactions
     * @author Ayub
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