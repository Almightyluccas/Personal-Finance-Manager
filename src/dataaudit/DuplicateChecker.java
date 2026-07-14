package dataaudit;

/**
 * Detects duplicate financial transactions.
 *
 * @author Muhaymen Dhali
 */
public class DuplicateChecker {

    /**
     * Creates a DuplicateChecker object.
     *
     * @author Muhaymen Dhali
     */
    public DuplicateChecker() {
    }

    /**
     * Detects duplicate transactions.
     * 
     * @param dates transaction dates
     * @param categories transaction categories
     * @param amounts transaction amounts
     * @param result object that stores duplicate findings
     * @author Muhaymen Dhali
     */
    public void detectDuplicates(String[] dates, String[] categories, int[] amounts, AuditResult result) {
    	 boolean duplicateFound = false;

         System.out.println("Checking for duplicate transactions...");

         for (int i = 0; i < dates.length; i++) {
             for (int j = i + 1; j < dates.length; j++) {
                 if (isDuplicate(dates[i], categories[i], amounts[i],
                         dates[j], categories[j], amounts[j])) {
                	 String[] duplicate = {
                	            dates[i],
                	            categories[i],
                	            String.valueOf(amounts[i])
                	    };
                	 result.addDuplicate(duplicate);
                     duplicateFound = true;
                 }
             }
         }

         if (!duplicateFound) {
             System.out.println("No duplicate transactions found.");
         }
    }

    /**
     * Determines whether two transactions are duplicates.
     *
     * @param date1 first transaction date
     * @param category1 first transaction category
     * @param amount1 first transaction amount
     * @param date2 second transaction date
     * @param category2 second transaction category
     * @param amount2 second transaction amount
     * @return true if the transactions are duplicates, false otherwise
     * @author Muhaymen Dhali
     */
    public boolean isDuplicate(String date1, String category1, int amount1,
            String date2, String category2, int amount2) {
    	 return date1.equals(date2)
                 && category1.equals(category2)
                 && amount1 == amount2;
    }
}