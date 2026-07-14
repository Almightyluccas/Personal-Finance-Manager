package dataaudit;
import java.util.ArrayList;
import integration.AppModule;

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
	 * Audits one year of sample financial data.
	 *
	 * @author Muhaymen Dhali
	 */
	public void auditYear() {
		System.out.println("Starting annual data audit...");
		 String[] dates = {
	                "01/15/2026", "01/15/2026",
	                "01/31/2026", "02/28/2026",
	                "03/31/2026", "04/30/2026",
	                "05/31/2026", "06/30/2026",
	                "07/31/2026", "08/31/2026",
	                "09/30/2026", "10/31/2026",
	                "11/30/2026", "12/31/2026",
	                "08/20/2026"
	        };

	        String[] categories = {
	                "Food", "Food",
	                "Compensation", "Compensation",
	                "Compensation", "Compensation",
	                "Compensation", "Compensation",
	                "Compensation", "Compensation",
	                "Compensation", "Compensation",
	                "Compensation", "Compensation",
	                "Education"
	        };

	        int[] amounts = {
	                -25, -25,
	                10000, 10000,
	                10000, 10000,
	                10000, 10000,
	                10000, 10000,
	                10000, 10000,
	                10000, 100000,
	                -3000
	        };
	        
	        ArrayList<String[]> transactions =
	                createTransactionList(dates, categories, amounts);
		
	        AuditResult result = new AuditResult();
	        findDuplicates(dates, categories, amounts, result);
	        findAnomalies(transactions, result);
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
        auditOptions.addExcludedCategory("Education");
        auditOptions.showExcludedCategories();
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