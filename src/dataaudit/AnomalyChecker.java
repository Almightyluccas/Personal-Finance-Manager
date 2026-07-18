package dataaudit;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
 
/**
 * Detects unusual financial transactions by comparing each entry
 * against its category's average amount.
 *
 * @author Syed Karim
 */
public class AnomalyChecker {
 
    /**
     * The factor by which an entry must deviate from its category
     * average to be flagged as anomalous.
     */
    private static final double ANOMALY_FACTOR = 3.0;
 
    /**
     * The minimum number of entries a category must have before
     * a baseline average can be established. Categories with fewer
     * entries are skipped and reported as insufficient data.
     */
    private static final int MIN_ENTRIES_FOR_BASELINE = 3;
 
    /** The audit options containing categories to exclude from analysis. */
    private final AuditOptions options;
 
    /**
     * Creates an AnomalyChecker with the specified audit options.
     *
     * @param options the audit options specifying excluded categories
     * @author Syed Karim
     */
    public AnomalyChecker(AuditOptions options) {
        this.options = options;
    }
 
    /**
     * Detects anomalous transactions from the given list.
     * Skips categories that are excluded in AuditOptions or have
     * fewer entries than MIN_ENTRIES_FOR_BASELINE.
     *
     * @param transactions the list of transactions to analyze,
     * where each entry is a String array of date, category, and amount. 
     * @param result object that stores anomaly findings
     * @author Syed Karim
     */
    public void detectAnomalies(ArrayList<String[]> transactions, AuditResult result) {
        ArrayList<String> excluded = options.getExcludedCategories();
 
        // Group transactions by category
        HashMap<String, ArrayList<Double>> categoryAmounts = new HashMap<>();
        for (String[] transaction : transactions) {
            String category = transaction[1];
            double amount = Math.abs(Double.parseDouble(transaction[2]));
 
            if (!excluded.contains(category)) {
                categoryAmounts.putIfAbsent(category, new ArrayList<>());
                categoryAmounts.get(category).add(amount);
            }
        }
 
        // Check each transaction against its category average
        System.out.println("=== Anomaly Detection Results ===");
        boolean anomalyFound = false;
        HashSet<String> skippedCategories = new HashSet<>();
 
        for (String[] transaction : transactions) {
            String category = transaction[1];
            if (excluded.contains(category)) continue;
 
            ArrayList<Double> amounts = categoryAmounts.get(category);
            if (amounts == null || amounts.size() < MIN_ENTRIES_FOR_BASELINE) {
                if (!skippedCategories.contains(category)) {
                    System.out.println("Skipping " + category
                            + ": insufficient data for baseline.");
                    skippedCategories.add(category);
                }
                continue;
            }
 
            double amount = Math.abs(Double.parseDouble(transaction[2]));
            double average = calculateAverageAmount(amounts);
 
            if (isAnomalous(amount, average)) {
            	if (isAnomalous(amount, average)) {
            	    result.addAnomaly(transaction);
            	    anomalyFound = true;
            	}
            }
        }
 
        if (!anomalyFound) {
            System.out.println("No anomalies detected.");
        }
    }
 
    /**
     * Calculates the average from a list of transaction amounts 
     *
     * @param amounts the list of transaction amounts for a single category
     * @return the average amount
     * @author Syed Karim
     */
    public double calculateAverageAmount(ArrayList<Double> amounts) {
        double total = 0;
        for (double amount : amounts) {
            total += amount;
        }
        return total / amounts.size();
    }
 
    /**
     * Determines whether a single transaction amount is anomalous
     * based on its category average, using ANOMALY_FACTOR as the threshold.
     *
     * @param amount  the absolute value of the transaction amount to check
     * @param average the category's average absolute amount
     * @return true if the amount is anomalous, false otherwise
     * @author Syed Karim
     */
    public boolean isAnomalous(double amount, double average) {
        if (average == 0) return false;
        return amount >= average * ANOMALY_FACTOR || amount <= average / ANOMALY_FACTOR;
    }
}