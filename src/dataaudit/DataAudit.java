package dataaudit;
import accounts.Account;
import accounts.AccountService;
import integration.AppModule;
import integration.MenuUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import storage.Budget;
import storage.BudgetStorage;
import storage.Transaction;

/**
 * Performs data auditing on a user's annual budget.
 *
 * @author Muhaymen Dhali
 */
public class DataAudit implements AppModule {
	
	/** Stores the user's category exclusion choices. */
    private final AuditOptions auditOptions;

	/**
	 * Creates a DataAudit object.
	 *
	 * @author Muhaymen Dhali
	 */
	public DataAudit() {
		auditOptions = new AuditOptions();
	}

	/**
	 * Audits one year of the current user's stored financial data.
	 *
	 * @author Muhaymen Dhali
	 */
	public void auditYear() {
	    Account currentUser = AccountService.SessionManager.getCurrentUser();

	    if (currentUser == null) {
	        System.out.println("You must be logged in to run Data Audit.");
	        return;
	    }

	    String username = currentUser.getUsername();
	    BudgetStorage storage = new BudgetStorage();

	    List<Integer> availableYears = storage.listYearsForUser(username);

	    if (availableYears.isEmpty()) {
	        System.out.println("No budgets found for " + username + ".");
	        return;
	    }

	    Integer year = promptForBudgetYear(availableYears);

	    if (year == null) {
	        return;
	    }

	    Budget budget = storage.readBudget(username, year);

	    if (budget == null) {
	        System.out.println("The budget for " + year
	                + " could not be loaded.");
	        return;
	    }

	    List<Transaction> storedTransactions =
	            budget.getTransactions();

	    if (storedTransactions.isEmpty()) {
	        System.out.println("The budget for " + year
	                + " contains no transactions.");
	        return;
	    }

	    System.out.println("Starting data audit for "
	            + username + "'s " + year + " budget...");

	    String[] dates =
	            new String[storedTransactions.size()];
	    String[] categories =
	            new String[storedTransactions.size()];
	    double[] amounts =
	            new double[storedTransactions.size()];

	    for (int i = 0; i < storedTransactions.size(); i++) {
	        Transaction transaction = storedTransactions.get(i);

	        dates[i] = transaction.date().toString();
	        categories[i] = transaction.category();
	        amounts[i] = transaction.amount();
	    }

	    ArrayList<String[]> auditTransactions =
	            createTransactionList(dates, categories, amounts);

	    List<String> availableCategories = extractCategories(auditTransactions);
	    promptForExcludedCategories(availableCategories);

	    AuditResult result = new AuditResult();

	    findDuplicates(dates, categories, amounts, result);
	    findAnomalies(auditTransactions, result);

	    result.printResults();

	    removeAnomalies(result, budget, username, storage);
	}

	/**
	 * Prompts the user to select a budget year from the years available
	 * for the current user.
	 *
	 * @param availableYears the years that have a stored budget
	 * @return the selected year, or {@code null} if the user chose to
	 * return to the main menu
	 * @author Syed Karim
	 */
	private Integer promptForBudgetYear(List<Integer> availableYears) {
	    String[] options = new String[availableYears.size() + 1];

	    for (int i = 0; i < availableYears.size(); i++) {
	        options[i] = (i + 1) + ". " + availableYears.get(i);
	    }
	    options[availableYears.size()] = "0. Back to main menu";

	    while (true) {
	        String choice = MenuUtil.promptChoice("Select Budget Year", options);

	        if (choice.equals("0")) {
	            return null;
	        }

	        try {
	            int index = Integer.parseInt(choice);
	            if (index >= 1 && index <= availableYears.size()) {
	                return availableYears.get(index - 1);
	            }
	        } catch (NumberFormatException e) {
	            // fall through to the error message below
	        }

	        System.out.println("Invalid selection. Please try again.");
	    }
	}

	/**
	 * Extracts the unique set of categories present in the given
	 * transactions, sorted alphabetically.
	 *
	 * @param transactions the transactions to inspect
	 * @return the sorted list of unique categories
	 * @author Syed Karim
	 */
	private List<String> extractCategories(List<String[]> transactions) {
	    Set<String> categories = new TreeSet<>();

	    for (String[] transaction : transactions) {
	        categories.add(transaction[1]);
	    }

	    return new ArrayList<>(categories);
	}

	/**
	 * Presents an interactive menu allowing the user to toggle which
	 * categories are excluded from anomaly detection.
	 *
	 * @param availableCategories the categories present in the audited year
	 * @author Syed Karim
	 */
	private void promptForExcludedCategories(List<String> availableCategories) {
	    if (availableCategories.isEmpty()) {
	        return;
	    }

	    while (true) {
	        String[] options = new String[availableCategories.size() + 1];

	        for (int i = 0; i < availableCategories.size(); i++) {
	            String category = availableCategories.get(i);
	            String marker = auditOptions.getExcludedCategories()
	                    .contains(category) ? "[X]" : "[ ]";
	            options[i] = (i + 1) + ". " + marker + " " + category;
	        }
	        options[availableCategories.size()] = "0. Done";

	        String choice = MenuUtil.promptChoice(
	                "Toggle Excluded Categories", options);

	        if (choice.equals("0")) {
	            return;
	        }

	        try {
	            int index = Integer.parseInt(choice);
	            if (index >= 1 && index <= availableCategories.size()) {
	                String category = availableCategories.get(index - 1);

	                if (auditOptions.getExcludedCategories().contains(category)) {
	                    auditOptions.removeExcludedCategory(category);
	                } else {
	                    auditOptions.addExcludedCategory(category);
	                }
	                continue;
	            }
	        } catch (NumberFormatException e) {
	            // fall through to the error message below
	        }

	        System.out.println("Invalid selection. Please try again.");
	    }
	}

	/**
	 * Offers to remove each flagged anomaly from the budget, persisting
	 * the updated budget to storage if any removals are made.
	 *
	 * @param result   the audit results containing flagged anomalies
	 * @param budget   the budget to remove matched transactions from
	 * @param username the username who owns the budget
	 * @param storage  the storage used to persist the updated budget
	 * @author Syed Karim
	 */
	private void removeAnomalies(AuditResult result, Budget budget,
	        String username, BudgetStorage storage) {
	    if (result.getAnomalies().isEmpty()) {
	        return;
	    }

	    boolean changed = false;

	    for (String[] anomaly : result.getAnomalies()) {
	        boolean remove = MenuUtil.promptYesNo("Remove anomaly "
	                + anomaly[0] + " | " + anomaly[1] + " | $"
	                + AuditResult.formatAmount(anomaly[2]));

	        if (remove && removeMatchingTransaction(budget, anomaly)) {
	            changed = true;
	        }
	    }

	    if (changed) {
	        storage.updateBudget(username, budget);
	        System.out.println("Budget updated.");
	    }
	}

	/**
	 * Removes the first transaction in the budget matching the given
	 * anomaly's date, category, and exact amount.
	 *
	 * @param budget  the budget to remove the transaction from
	 * @param anomaly the anomaly to match, as [date, category, amount]
	 * @return {@code true} if a matching transaction was removed
	 * @author Syed Karim
	 */
	private boolean removeMatchingTransaction(Budget budget, String[] anomaly) {
	    LocalDate date = LocalDate.parse(anomaly[0]);
	    String category = anomaly[1];
	    double amount = Double.parseDouble(anomaly[2]);

	    Iterator<Transaction> iterator = budget.getTransactions().iterator();

	    while (iterator.hasNext()) {
	        Transaction transaction = iterator.next();

	        if (transaction.date().equals(date)
	                && transaction.category().equals(category)
	                && transaction.amount() == amount) {
	            iterator.remove();
	            return true;
	        }
	    }

	    return false;
	}
	
	 /**
     * Converts the transaction arrays into a list used by AnomalyChecker.
     *
     * @param dates transaction dates
     * @param categories transaction categories
     * @param amounts transaction amounts
     * @return the transactions as a list of String arrays
     * @author Syed Karim
     */
    public ArrayList<String[]> createTransactionList(
            String[] dates, String[] categories, double[] amounts) {

        ArrayList<String[]> transactions = new ArrayList<>();

        for (int i = 0; i < dates.length; i++) {
            String[] transaction = {
                    dates[i],
                    categories[i],
                    String.valueOf(amounts[i])
            };

            transactions.add(transaction);
        }

        return transactions;
    }

	/**
	 * Finds duplicate transactions.
	 *
	 *@param dates transaction dates
	 *@param categories transaction categories
	 *@param amounts transaction amounts
	 *@param result object that stores audit findings
	 * @author Muhaymen Dhali
	 */
	public void findDuplicates(String[] dates, String[] categories, double[] amounts, AuditResult result) {
		DuplicateChecker checker = new DuplicateChecker();
        checker.detectDuplicates(dates, categories, amounts, result);
		
	}

	/**
	 * Finds anomalous transactions.
	 *
	 *@param transactions transactions to analyze
	 *@param result object that stores audit findings
	 * @author Muhaymen Dhali
	 */
	public void findAnomalies(ArrayList<String[]> transactions,
	        AuditResult result) {
	    AnomalyChecker checker = new AnomalyChecker(auditOptions);
	    checker.detectAnomalies(transactions, result);
	}
	
	/**
     * Returns the unique name of this module.
     *
     * @return the Data Audit module name
     * @author Muhaymen Dhali
     */
    @Override
    public String getModuleName() {
        return "dataaudit";
    }

    /**
     * Performs setup before the Data Audit module runs.
     *
     * @author Muhaymen Dhali
     */
    @Override
    public void initialize() {

    }

    /**
     * Runs the Data Audit module when selected from the main menu.
     *
     * @author Muhaymen Dhali
     */
    @Override
    public void handleSelection() {
        auditYear();
    }
    
    /**
     * Tests the Data Audit Alpha build.
     *
     * @param args command-line arguments
     * @author Muhaymen Dhali
     */
    
    public static void main(String[] args) {
        DataAudit audit = new DataAudit();
        audit.initialize();
        audit.handleSelection();
    }

}