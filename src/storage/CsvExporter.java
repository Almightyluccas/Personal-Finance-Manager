package storage;
 
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
 
/**
 * Handles exporting transaction data and reports to CSV files.
 *
 * @author Ayub
 */
public class CsvExporter {
 
    /** The header line written to every exported CSV file. */
    private static final String HEADER = "Date,Category,Amount";
 
    /** Date format used when writing transactions back out, per project spec. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
 
    /**
     * Constructs a new {@code CsvExporter} instance.
     *
     * @author Ayub
     */
    public CsvExporter() {
    }
 
    /**
     * Writes a report of the given transactions to a CSV file at the
     * specified path.
     *
     * @param transactions the transactions to include in the report
     * @param filePath     the destination path for the CSV file
     * @author Ayub
     */
    public void writeReportToCsv(List<Transaction> transactions, String filePath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath))) {
            writer.write(HEADER);
            writer.newLine();
            for (Transaction t : transactions) {
                writer.write(t.date().format(DATE_FORMAT) + "," + t.category() + "," + formatAmount(t.amount()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to write CSV report to: " + filePath, e);
        }
    }
 
    /**
     * Formats a transaction amount for CSV output. Whole-dollar values are
     * written without a trailing decimal (e.g. {@code 100} instead of
     * {@code 100.0}), matching the project spec's "no cents" convention
     * even though amounts are stored as {@code double}.
     *
     * @param amount the amount to format
     * @return the formatted amount as a string
     * @author Ayub
     */
    private String formatAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }
}
 