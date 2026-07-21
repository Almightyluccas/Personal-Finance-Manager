package dataaudit;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores and displays audit results,
 * including duplicate and anomalous entries.
 * 
 * @author Sazedur Khan
 */
public class AuditResult {

	/** Duplicate transactions found during the audit. */
	private ArrayList<String[]> duplicates; 

	/** Anomalous transactions found during the audit. */
	private ArrayList<String[]> anomalies;


	/**
	 * Creates an AuditResult object. 
	 *
	 * @author Sazedur Khan
	 */
	public AuditResult() {
		duplicates = new ArrayList<>();
		anomalies = new ArrayList<>();
	}

	/**
	 * Adds a duplicate entry to the results.
	 *
	 * @param transaction the duplicate transaction represented as a
	 * String array of date, category, and amount.
	 * @author Sazedur Khan
	 */
	public void addDuplicate(String[] transaction) {
		duplicates.add(transaction);
	}

	/**
	 * Adds an anomalous entry to the results.
	 *
	 * @param transaction the anomalous transaction represented as a
	 * String array of date, category, and amount.
	 * @author Sazedur Khan
	 */
	public void addAnomaly(String[] transaction) {
		anomalies.add(transaction);
	}

	/**
	 * Prints the audit results, including duplicates and anomalies.
	 * If no duplicates or anomalies are found, it prints a message
	 * indicating that the audit passed successfully.
	 *
	 * @author Sazedur Khan
	 */
	public void printResults() {
		System.out.println("=== Audit Results ===");
		printSection("Duplicate", duplicates);
		printSection("Anomalous", anomalies);
		System.out.println("Total findings: " + (duplicates.size() + anomalies.size()));
	}

	/** 
	 * Prints one section of the audit results, listing each transaction
	 * or indicating that none were found.
	 * 
	 * @param label the label for the section
	 * @param transactions the list of transactions to print
	 * @author Sazedur Khan
	 */
	 private void printSection(String label, List<String[]> transactions) {
		if (transactions.isEmpty()) {
			System.out.printf("No %s transactions found.%n", label.toLowerCase());
            return;
		}
		System.out.printf("%s transactions (%d):%n", label, transactions.size());
        for (String[] transaction : transactions) {
            System.out.printf("  %s | %s | $%s%n",
                    transaction[0], transaction[1], formatAmount(transaction[2]));
        }
    }

	/**
	 * Formats a transaction amount string for display, dropping
	 * unnecessary decimal places from whole-dollar amounts while
	 * preserving cents (e.g. "301.0" becomes "301", "300.98" stays
	 * "300.98").
	 *
	 * @param amount the amount as a decimal string
	 * @return the amount formatted for clean display
	 * @author Syed Karim
	 */
	public static String formatAmount(String amount) {
		double value = Double.parseDouble(amount);
		if (value == Math.rint(value)) {
			return String.valueOf((long) value);
		}
		return String.format("%.2f", value);
	}

	/** 
	 * Returns the list of duplicate transactions found during the audit.
	 * 
	 * @return the duplicate count 
	 * @author Sazedur Khan
	 */
	public int getDuplicateCount() {
        return duplicates.size();
    }

	/**
	 * Returns the number of anomalous transactions recorded.
	 *
	 * @return the anomaly count
	 * @author Sazedur Khan
	 */
	public int getAnomalyCount() {
		return anomalies.size();
	}

	/**
	 * Returns the list of anomalous transactions found during the audit.
	 *
	 * @return the anomalous transactions, each represented as a
	 * String array of date, category, and amount.
	 * @author Sazedur Khan
	 */
	public List<String[]> getAnomalies() {
		return anomalies;
	}
}
