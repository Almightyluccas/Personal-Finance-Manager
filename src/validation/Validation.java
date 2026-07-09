package validation;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Provides static validation utilities for the Personal Finance Manager (PFM)
 * application.
 * @author Selina Zhu
 * @author Tasfia Rahman
 * @author David Guanga
 */

public class Validation
{
	/**
     * Validates a user's username.
     *
     * @param username the username entered by the user
     * @return true if the username satisfies all username requirements;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidUsername(String username) {
        return false;
    }

    /**
     * Validates a user's password.
     *
     * @param password the password entered by the user
     * @return true if the password satisfies all password requirements;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidPassword(String password) {
        return false;
    }

    /**
     * Validates the user's secret question.
     *
     * @param secretQuestion the secret question entered by the user
     * @return true if the secret question is valid;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidSecretQuestion(String secretQuestion) {
        return false;
    }

    /**
     * Validates the user's secret answer.
     *
     * @param secretAnswer the secret answer entered by the user
     * @return true if the secret answer is valid;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidSecretAnswer(String secretAnswer) {
        return false;
    }

	/**
     * Checks whether a file name follows the required YYYY.csv pattern.
     *
     * <p>
     * The file may be given as a bare name in the local folder or as a fully
     * qualified path name (FQPN); in either case the base name must be a
     * four-digit year followed by the .csv extension, for example 2025.csv.
     * </p>
     *
     * @param fileName the file name or fully qualified path to validate
     * @return true if the base file name matches YYYY.csv, false otherwise
     * @author Tasfia Rahman
     */
    public static boolean isValidFileName(String fileName) {
        return false; // TODO: implement
    }

    /**
     * Checks whether a CSV file exists, is readable, and begins with the
     * required header line.
     *
     * @param fileName the file name or fully qualified path of the CSV file
     * @return true if the file exists, can be read, and has a valid header,
     *         false otherwise
     * @author Tasfia Rahman
     */
    public static boolean isValidCsvFile(String fileName) {
        return false; // TODO: implement
    }

    /**
     * Checks whether a header line matches the required CSV header.
     *
     * <p>
     * The required header is {@code Date,Category,Amount}. The comparison is
     * case-insensitive and ignores surrounding whitespace and a leading
     * byte-order mark (BOM) if present.
     * </p>
     *
     * @param headerLine the first line of a CSV file
     * @return true if the header is valid, false otherwise
     * @author Tasfia Rahman
     */
    public static boolean isValidHeader(String headerLine) {
        return false; // TODO: implement
    }

    /**
     * Checks whether a CSV data file already exists for the given year in the
     * given directory.
     *
     * <p>
     * Used to warn the user before overwriting a previously loaded year.
     * </p>
     *
     * @param directory the directory to search
     * @param year      the four-digit year to look for
     * @return true if a file named YYYY.csv exists in the directory,
     *         false otherwise
     * @author Tasfia Rahman
     */
    public static boolean fileExistsForYear(String directory, int year) {
        return false; // TODO: implement
    }

	/**
	 * Checks to see if the date is in the proper format and represents a valid month, date, and year
	 *@param date takes in the date as a String object
     *@param recordIndex the line number of the data record being currently read from the .csv file 
	 *@return Returns true if date is in the correct format MM/DD/YYYY and checks to see if the date is valid
 
	 *@author David Guanga
	 * */
	private static boolean isValidDateFormat(String date, int recordIndex)
    {
        String[] dateArr = date.split("/");
        int month = 0, day = 0, year = 0;
        try
        {
            month = Integer.parseInt(dateArr[0]);
            day = Integer.parseInt(dateArr[1]);
            year = Integer.parseInt(dateArr[2]);
            LocalDate tempDate = LocalDate.of(year, month, day);

            return (tempDate.getYear() > 0);
        }
        catch(DateTimeException dateException)
        {
            return false;
        }
        catch(NumberFormatException e)
        {
            System.out.println("Invalid date format at the " + recordIndex + " data entry in the file" );
            return false;
        }
	}
	
	/**
	 * Checks to see if the year of each entry of the .csv file is the same
	 *@param year the year of the given .csv file 
     *@param yearToCheck the year given by a data entry to check to see if both are equal
     *@param recordIndex the line number of the data record being currently read from the .csv file 
	 *@return Returns true if the passed date is of the same year as all the other dates 
	 *@author David Guanga
	 * */
	public static boolean isSameYear(final String year, String yearToCheck, int recordIndex) 
    {
        boolean isSameYear = year.equals(yearToCheck);
        if(!isSameYear)
            System.out.println("Invalid year at the " + recordIndex + " data entry. The year must match the year of the file name.");

        return isSameYear;
	}

	/**
	 * Checks to see if the category matches one of the accepted categories 
	 *@param categ the name of the category to validate
     *@param recordIndex the line number of the data record being currently read from the .csv file 
	 *@return Returns true if the input category is one of the following: Compensation, Allowance, Investments,
	 *Home, Utilities, Food, Appearance, Work, Education, Transportation, Entertainment, Professional Services, Other
	 *@author David Guanga
	 * */
	private static boolean isValidCategory(String categ, int recordIndex)
    {
    //TODO:Try to make categories its own type so that categories that are either expenses or income can be easily distinguished and
    //add to 
        String[] validCategories = {
        "Compensation", "Allowance", "Investments",
        "Home", "Utilities", "Food",
        "Appearance", "Work", "Education",
        "Transportation", "Entertainment",
        "Professional Services", "Other"
        };

        boolean check = false;
        for(int i = 0; (i < validCategories.length) && !check; i++)
        {
            check = categ.equalsIgnoreCase(validCategories[i]);
        }

        if(!check)
        {
            System.out.println("Invalid category at the " + recordIndex + " data entry in the file");
        }

        return check;
    }

	/**
	 * Checks to see if the amounts can be converted to a number
	 *@param amount the amount to process and validate
     *@param recordIndex the line number of the data record being currently read from the .csv file 
	 *@return Returns true if the String am can be converted into a number 
	 *@author David Guanga
	 * */
	private static boolean isValidAmount(String amount, int recordIndex)
    {
        try
        {
            double tempAmount = Double.parseDouble(amount);
            int dollarAmount = (int)tempAmount;
            double cents = tempAmount - dollarAmount;

            if(cents > 0.001)
                System.out.println("Truncating amount in " + recordIndex + " data entry to the nearest dollar");
            return true;
        }
        catch(Exception e)
        {
            System.out.println("Invalid amount at the " + recordIndex + " data entry in the file.\n"
            + "Only dollar amount is accepted");
            return false;
        }
	}

	/**
	 * Checks to see if an entry in the .csv file has a valid date, category, and amount
	 *@param take in a BudgetRecord that must be validated
	 *@return true if the records hava a valid date format, category, and amount
	 *@author David Guanga
	 * */
	public static boolean isValidRecord(){
		return false;
	}
}
