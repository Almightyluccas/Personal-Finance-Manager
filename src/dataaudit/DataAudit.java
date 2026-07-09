package dataaudit;

/**
 * Performs data auditing on a user's annual budget.
 *
 * @author Muhaymen Dhali
 */
public class DataAudit {

	/**
	 * Creates a DataAudit object.
	 *
	 * @author Muhaymen Dhali
	 */
	public DataAudit() {

	}

	/**
	 * Audits one year of financial data.
	 *
	 * @author Muhaymen Dhali
	 */
	public void auditYear() {
		System.out.println("Starting annual data audit...");
		String[] dates = { "01/15/2026", "01/15/2026", "02/01/2026", "03/10/2026" };
        String[] categories = { "Food", "Food", "Compensation", "Education" };
        int[] amounts = { -25, -25, 10000, -3000 };
		
		findDuplicates(dates, categories, amounts);
		findAnomalies();
		printAuditSummary();

	}

	/**
	 * Finds duplicate transactions.
	 *
	 *@param dates transaction dates
	 *@param categories transaction categories
	 *@param amounts transaction amounts
	 * @author Muhaymen Dhali
	 */
	public void findDuplicates(String[] dates, String[] categories, int[] amounts) {
		DuplicateChecker checker = new DuplicateChecker();
        checker.detectDuplicates(dates, categories, amounts);
		
	}
	
	/**
     * Main method used to test the Data Audit alpha build.
     *
     * @param args command line arguments
     * @author Muhaymen Dhali
     */
    public static void main(String[] args) {
        DataAudit audit = new DataAudit();
        audit.auditYear();
    }

	/**
	 * Finds anomalous transactions.
	 *
	 * @author Muhaymen Dhali
	 */
	public void findAnomalies() {
		System.out.println("Checking for anomalous transactions...");

	}

	/**
	 * Prints a summary of the audit results.
	 *
	 * @author Muhaymen Dhali
	 */
	public void printAuditSummary() {
		System.out.println("Audit completed successfully.");

	}

}