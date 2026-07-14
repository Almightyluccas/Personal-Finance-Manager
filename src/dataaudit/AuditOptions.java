package dataaudit;

import java.util.ArrayList;

/**
 * Manages audit settings selected by the user,
 * such as categories to exclude from anomaly detection.
 * @author Syed Karim
 */
public class AuditOptions {

	/** Categories to exclude from anomaly analysis */
	private final ArrayList<String> excludedCategories; 
	

	/**
	 * Creates an AuditOptions object with no categories excluded by default 
	 *
	 * @author Syed Karim
	 */
	public AuditOptions() {
		excludedCategories = new ArrayList<>();
	}

	/**
	 * Excludes a category from the anomaly analysis.
	 * Has no effect if the category is already excluded.
	 * 
	 * @param category the caregory name to exlude 
	 * @author Syed Karim
	 */
	public void addExcludedCategory(String category) {
		if (!excludedCategories.contains(category)) {
			excludedCategories.add(category);
			System.out.println("Category added to excluded list: " + category);
		} else {
			System.out.println("Category already excluded: " + category);
		}
	}

	/**
	 * Removes a category from the exclusion list so that 
	 * it will be included in the anomaly analysis 
	 * 
	 * @param category the category name to re-include
	 * @author Syed Karim
	 */
	public void removeExcludedCategory(String category) {
		if (excludedCategories.remove(category)) {
			System.out.println(category + " has been re-included in the anomaly analysis."); 
		} else {
			System.out.println(category + " was not in the exclusion list.");
		}
	}

	/**
	 * Displays all categories currently excluded from anomaly analysis.
	 *
	 * @author Syed Karim
	 */
	public void showExcludedCategories() {
		if (excludedCategories.isEmpty()) {
			System.out.println("No categories are currently excluded.");
		} else {
			System.out.println("Excluded categories:");
			for (String category : excludedCategories) {
				System.out.println("- " + category);
			}
		}
	}

	/**
	 * Returns the list of excluded categories 
	 * Used by AnomalyChecker to skip excluded categories during analysis.
	 * 
	 * @return the list of excluded categories
	 * @author Syed Karim
	 */	
	public ArrayList<String> getExcludedCategories() {
		return excludedCategories;
	}
}