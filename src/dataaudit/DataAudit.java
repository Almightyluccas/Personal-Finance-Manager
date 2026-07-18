package dataaudit;
import java.util.ArrayList;
import java.util.List;
import integration.AppModule;
import integration.MenuUtil;
import accounts.Account;
import accounts.AccountService;
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

	    String yearInput = MenuUtil.promptString(
	            "Enter the budget year to audit");

	    int year;

	    try {
	        year = Integer.parseInt(yearInput);
	    } catch (NumberFormatException e) {
	        System.out.println("Invalid year. Please enter a number.");
	        return;
	    }

	    if (!storage.yearExists(username, year)) {
	        System.out.println("No budget found for " + year + ".");
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
	    int[] amounts =
	            new int[storedTransactions.size()];

	    for (int i = 0; i < storedTransactions.size(); i++) {
	        Transaction transaction = storedTransactions.get(i);

	        dates[i] = transaction.date().toString();
	        categories[i] = transaction.category();
	        amounts[i] = (int) Math.round(transaction.amount());
	    }

	    ArrayList<String[]> auditTransactions =
	            createTransactionList(dates, categories, amounts);

	    AuditResult result = new AuditResult();

	    findDuplicates(dates, categories, amounts, result);
	    findAnomalies(auditTransactions, result);

	    result.printResults();
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
            String[] dates, String[] categories, int[] amounts) {

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
	public void findDuplicates(String[] dates, String[] categories, int[] amounts, AuditResult result) {
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