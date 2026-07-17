package validation;

import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Pattern;
/**
 * Provides static methods for validating user input and CSV file data
 * used by the Personal Finance Manager (PFM) application.
 *
 * <p>
 * This class validates account information such as usernames,
 * passwords, secret questions, and secret answers. It also validates
 * CSV file names, headers, records, dates, categories, amounts, and
 * ensures that imported financial data conforms to the application's
 * required format.
 * </p>
 *
 * @author Selina Zhu
 * @author Tasfia Rahman
 * @author David Guanga
 */

public class Validation
{
    private static String[] validCategories;

    /** Required CSV header line, per the PFM functional specification. */
    public static final String VALID_HEADER = "Date,Category,Amount";

    /** Earliest year accepted in a PFM file name. */
    public static final int MIN_YEAR = 1900;

    /** Latest year accepted in a PFM file name. */
    public static final int MAX_YEAR = 2100;

    /** Pattern for a base file name of the form YYYY.csv. */
    private static final Pattern FILE_NAME_PATTERN =
            Pattern.compile("^\\d{4}\\.csv$", Pattern.CASE_INSENSITIVE);

    /** Unicode byte-order mark sometimes present at the start of CSV files. */
    private static final String BOM = "﻿";


	/**
     * Validates a user's username.
	 * Requirements:
	 * - Cannot be null
	 * - Must be between 4 and 20 characters
	 * - May contain only letters, digits, and underscores
     *
     * @param username the username entered by the user
     * @return true if the username satisfies all username requirements;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidUsername(String username)
	{
    	if (username == null)
        	return false;

    	username = username.trim();

    	return username.matches("^[A-Za-z0-9_]{4,20}$");
	}

    /**
     * Validates a user's password.
	 * Requirements:
	 * - Cannot be null
  	 * - At least 8 characters
 	 * - Contains at least one uppercase letter
 	 * - Contains at least one lowercase letter
 	 * - Contains at least one digit
     *
     * @param password the password entered by the user
     * @return true if the password satisfies all password requirements;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidPassword(String password)
	{
    	if (password == null)
        	return false;

    	return password.matches(
        	"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
	}

    /**
     * Validates the user's secret question.
	 * Requirements:
 	 * - Cannot be null
 	 * - Between 10 and 100 characters
 	 * - Cannot be blank
     *
     * @param secretQuestion the secret question entered by the user
     * @return true if the secret question is valid;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidSecretQuestion(String secretQuestion)
	{
    	if (secretQuestion == null)
        	return false;

    	secretQuestion = secretQuestion.trim();

    	return secretQuestion.length() >= 10
        	&& secretQuestion.length() <= 100;
	}

    /**
     * Validates the user's secret answer.
	 * Requirements:
 	 * - Cannot be null
 	 * - Between 2 and 100 characters
 	 * - Cannot be blank
     *
     * @param secretAnswer the secret answer entered by the user
     * @return true if the secret answer is valid;
     *         otherwise false
     * @author Selina Zhu
     */
    public static boolean isValidSecretAnswer(String secretAnswer)
	{
    	if (secretAnswer == null)
        	return false;

    	secretAnswer = secretAnswer.trim();

    	return secretAnswer.length() >= 2
        	&& secretAnswer.length() <= 100;
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
        return toValidatedPath(fileName) != null;
    }

    /**
     * Resolves a file name to a path, but only if its base name is a valid
     * PFM file name of the form YYYY.csv.
     *
     * <p>
     * Shared by {@link #isValidFileName(String)} and
     * {@link #isValidCsvFile(String)} so the path is parsed only once.
     * </p>
     *
     * @param fileName the file name or fully qualified path to resolve
     * @return the resolved path, or null if the name is not a valid PFM
     *         file name
     * @author Tasfia Rahman
     */
    private static Path toValidatedPath(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        Path path;
        try {
            path = Paths.get(fileName.trim());
        } catch (InvalidPathException e) {
            return null; // malformed path (e.g., illegal characters)
        }
        Path base = path.getFileName();
        if (base == null) {
            return null;
        }
        String baseName = base.toString();
        if (!FILE_NAME_PATTERN.matcher(baseName).matches()) {
            return null;
        }
        int year = Integer.parseInt(baseName.substring(0, 4));
        return (year >= MIN_YEAR && year <= MAX_YEAR) ? path : null;
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
        Path path = toValidatedPath(fileName);
        if (path == null) {
            return false;
        }
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            return false;
        }
        try (BufferedReader reader =
                Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            return headerLine != null && isValidHeader(headerLine);
        } catch (IOException e) {
            return false;
        }
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
        if (headerLine == null) {
            return false;
        }
        String cleaned = headerLine.trim();
        if (cleaned.startsWith(BOM)) {
            cleaned = cleaned.substring(1).trim();
        }
        return cleaned.equalsIgnoreCase(VALID_HEADER);
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
        if (directory == null || year < MIN_YEAR || year > MAX_YEAR) {
            return false;
        }
        Path candidate;
        try {
            candidate = Paths.get(directory, year + ".csv");
        } catch (InvalidPathException e) {
            return false;
        }
        return Files.isRegularFile(candidate);
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
        final int dateLength = dateArr.length;
        int month = 0, day = 0, year = 0;
        try
        {
            if(dateLength < 3)
                throw new DateTimeException("Invalid date format at the + " + recordIndex + " data entry in the file: " + 
                "Must be in this date format: MM/DD/YYYY");

            month = Integer.parseInt(dateArr[0].trim());
            day = Integer.parseInt(dateArr[1].trim());
            year = Integer.parseInt(dateArr[2].trim());
            LocalDate tempDate = LocalDate.of(year, month, day);

            if(!(tempDate.getYear() >= Validation.MIN_YEAR) || !(tempDate.getYear() <= Validation.MAX_YEAR))
                throw new DateTimeException("Invalid date format at the + " + recordIndex + " data entry in the file: " +
                "The year must be within the range " + Validation.MIN_YEAR + " - " + Validation.MAX_YEAR);

            return true;
        }
        catch(DateTimeException dateException)
        {
            System.out.println(dateException.getMessage());
            return false;
        }
        catch(NumberFormatException e)
        {
            System.out.println("Invalid date format at the " + recordIndex + " data entry in the file:" +
                "Must be in this date format: MM/DD/YYYY. The month, day, and year must be whole numbers" );
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
        /*String[] validCategories = {
        "Compensation", "Allowance", "Investments",
        "Home", "Utilities", "Food",
        "Appearance", "Work", "Education",
        "Transportation", "Entertainment",
        "Professional Services", "Other"
        };
        */
        if(validCategories == null)
        {

            System.out.println("Error in Validation.isValidCategory(): Unset income and expense valid categories to check for");
            return false;
        }

        final int len = validCategories.length;
        boolean check = false;
        categ = categ.trim();

        for(int i = 0; (i < len) && !check; i++)
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
            double cents = Math.abs(tempAmount - dollarAmount);

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
	 *Sets the column titles to check for in the header of the .csv file
	 *@param categories sets the array of valid column titles for the header of the .csv file
     *@author David Guanga
     * */

    public static void setValidCategories(String[] categories)
    {
        for(int i = 0; i < categories.length; i++)
        {
            categories[i] = categories[i].trim();
        }
        Validation.validCategories = categories;
    }

	/**
	 * Checks to see if an entry in the .csv file has a valid date, category, and amount
	 *@param movieRecordStr takes in a read line from the .csv file.
	 *@param recordIndex the line number of where you are at in the .csv file
     *@return true if the records hava a valid date format, category, and amount
     *@author David Guanga
     * */
	public static boolean isValidRecord(String movieRecordStr, int recordIndex)
    {
        String[] recordArr = movieRecordStr.split(",");
        if(recordArr.length < 3)
            return false;

        String date = recordArr[0];
        String category = recordArr[1];
        String amount = recordArr[2];

        return Validation.isValidDateFormat(date, recordIndex)
        && Validation.isValidAmount(amount, recordIndex)
        && Validation.isValidCategory(category, recordIndex);
	}
}
