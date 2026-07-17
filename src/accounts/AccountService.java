package accounts;
import validation.Validation;
import storage.AccountFileManager;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class that provides basic operations for an Account.
 * Allows for creating, loging in/out, and account recovery from forgotten passowrd
 * @author Dwann, Harmony, Sakif
 */

   public class AccountService {
	   
	   private AccountService() {}
	   
	/**
	 * Manages the current user session. Tracks
	 * the logged-in user and authentication status.
	 * @author Sakif
	 */
	 public static class SessionManager{
		 
		private SessionManager() {}
		//current logged in user or null if no one is logged in 
		private static Account currentUser;
		//flag for user if authenticated
		private static boolean isAuthenticated;
		/**
		 * gets current logged in user
		 * @return current user or null if not logged in
		 * @author Sakif
		 */
		public static Account getCurrentUser() {
			return currentUser;
		}
		/**
		 * sets current logged in user and update authentication
		 * @param user set as logged in
		 * @author Sakif
		 */
		public static void setCurrentUser(Account user) {
			currentUser = user;
			isAuthenticated = (user != null);
		}
		/**
		 * checks if user is authenticated
		 * @return true if authenticated, false if not
		 * @author Sakif
		 */
		public static boolean isAuthenticated() {
			return isAuthenticated;
		}
		/**
		 * clears the session and logs out user
		 * resets currentuser and isAuthenticated
		 * @author Sakif
		 */
		public static void clearSession() {
			currentUser = null;
			isAuthenticated = false;
			System.out.println("Session cleared.");
		}
	}
	
	/**
	 * @param username the user's username
	 * @param password the user's password
	 * @param secretQuestion the user's own secret question
	 * @param secretAnswer the user's own secret answer
	 * @return whether the account creation was succesful
	 * @author Dwann
	 */
	public static boolean createAccount(String username, String password, String secretQuestion, String secretAnswer) {
		// requirements: the username, password, and secret question/answer is valid. The username must be unique.
		// 
		// postconditions: an account object is created with a username, hased password
		// and secret question/answer. The account is stored into the appropiate CSV file. The password in the CSV file 
		// will be hashed
		
		if (!Validation.isValidUsername(username)) {
			System.out.println("Username \"" + username + "\" failed:  " + missingUserReq(username));
			return false;
		}
		if (AccountFileManager.accountExists(username)) {
			System.out.println("Username \"" + username + "\" failed: Username alredy taken.");
			return false; 
		}
		if (!Validation.isValidPassword(password)) {
			System.out.println("Password \"" + password + "\" failed: " + missingPasswordReq(password));
			return false;
		}
		if (!Validation.isValidSecretQuestion(secretQuestion)) {
			System.out.println("Secret question \"" + secretQuestion +"\" failed:\n"
					+ "Question must be between 10 and 100 characters in length.");
			return false;
		}
		if (!Validation.isValidSecretAnswer(secretAnswer)) {
			System.out.println("Secret answer \"" + secretAnswer + "\" failed:\n"
					+ "Answer must be between 2 and 100 characters in length.");
			return false;
		}
		
		password = hash(password);
		secretAnswer = hash(secretAnswer);
		
		//Assuming storage uses a try-catch block:
		AccountFileManager.saveAccount(username, password, secretQuestion, secretAnswer);
		return true;
	
	}
	
	/**
	 * Finds why the username failed validation.
	 * @param username the username that failed validation.
	 * @return a meaningful message to why said username failed.
	 * @author Dwann
	 */
	private static String missingUserReq(String username) {
		if (username.length() < 4) {
			return "Username must be at least 4 characters in length.";
		} else if (username.length() > 20) {
			return "Username must be no longer than 20 characters in length.";
		} else if (username.matches(".*[^A-Za-z0-9_].*")){
			if (username.contains(" "))
				return "Username cannot contain spaces.";
			return "Only letters, numbers, and underscores are allowed.";
		}
		
		return null;
	}
	
	/**
	 * Finds why the password failed validation.
	 * @param password the password that failed validation.
	 * @return a meaningful message to why said username failed.
	 * @author Dwann
	 */
	private static String missingPasswordReq(String password) {
		if (password.length() < 8) {
			return "Password must be at least 8 characters in length";
		} else if (password.matches("^[^A-Z]*$")) {
			return "Password must contain at least one uppercase letter";
		} else if (password.matches("^[^a-z]*$")) {
			return "Password must contain at least one lowercase letter";
		} else if (password.matches("^\\D*$")) {
			return "Password must contain at least one number";
		}
		
		return null;
	}
	
	
	/**
	 * logs in a user into an account
	 * @param username the users username
	 * @param password the users password
	 * @return whether or not the login was succesful
	 * @author Harmony
	 */
	public static boolean login(String username, String password) {
		// requirements: The username must be within the CSV file. The user's account info must be read from the file.
		// Upon hashing the password witt the same hasing algorithm, it must match the hashed password read from file.
		//
		// postconditions: The user can go to the next page set by integration to access their audits and related information.
		
		if (!AccountFileManager.accountExists(username))
			return false;
		Account account = (Account) AccountFileManager.loadAccount(username);

		String hashedInput = hash(password);
		if(hashedInput.equals(account.getHashedPassword())){
			SessionManager.setCurrentUser(account);
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * saves any changes to the appropiate file(s), and sends the user
	 * back to the login screen.
	 * @return whether or not the logout was succesful
	 * @author Sakif
	 */
	public static boolean logout() {
		// postconditions: any account changes made will be written to the accounts CSV file.
		// the user will return to the login page.
		try {
			System.out.println("Logging out user..........");
			// save the current user data before logging out
			Account currentUser = SessionManager.getCurrentUser();
			if (currentUser != null){
				try{
					AccountFileManager.saveAccount(currentUser);
					System.out.println("Account saved for "+ currentUser.getUsername());
				}
				catch (Exception e){
					System.err.println("Coudn't save the account: "+ e.getMessage());
				}
			}
			SessionManager.clearSession();
			System.out.println("Returning to login screen...");
			return true;
		}
		catch(Exception e) {
			System.err.println("Error during logout process: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * prompts the user to to answer their secret question. Prompts the user to change
	 * their password if they answered correctly
	 * @author Harmony
	 */
	
	public static void forgotPassword() {
		// requirements: The prompt requesting the user to answer their secret question
		// must be called. Their answer must be correct.
		// postcondition: The user is prompted to change their password.
		
		Scanner scanner = new Scanner(System.in);

		System.out.print("Enter your username: ");
		String username = scanner.nextLine();
		
		if (!AccountFileManager.accountExists(username)) {
			System.err.print("Error: No such user exist with name " + username + ".");
			scanner.close();
			return;
		}

		Account account = (Account) AccountFileManager.loadAccount(username);
		
		if (account == null) {
		    System.out.println("No account found with that username.");
		    scanner.close();
		    return;
		}

		System.out.println(account.getSecretQuestion());
		System.out.print("Enter your answer: ");
		String answer = scanner.nextLine();

		if (checkSecretAnswer(answer, account)) {
		    System.out.print("Correct! Enter your new password: ");
		    String newPassword = scanner.nextLine();
		    changePassword(newPassword, account);
		    System.out.println("Password updated successfully.");
		} else {
			System.out.println("Incorrect answer. Password reset denied.");
		}
		scanner.close();
	}

	/**
	 * verifies the secretAnswer from the user with the one in their account
	 * @param secretAnswer the secret answer entered from the user
	 * @param account the account of said user to compare the secret questions
	 * @return whether or not the answers matched
	 * @author Harmony
	 */
	private static boolean checkSecretAnswer(String secretAnswer, Account account) {
		// postcondition: if the secret answer matches the accounts, the user can change whatever they need to accordingly.
		return account.getSecretAnswer().equals(secretAnswer); 
	}
	
	/**
	 * changes the password of a user's account
	 * @param newPassword the new password the user wants to set
	 * @param account the account of said user in question
	 * @return whether or not the password change was succesful
	 * @author Sakif
	 */
	private static boolean changePassword(String newPassword, Account account) {
		// requirements: the new password must be valid. the password must then be hashed
		//
		// postconditions: the old password is replace with the new password. The password is updated in the CSV file.
		if (account == null) {
			return false;
		}
		
		if(!Validation.isValidPassword(newPassword)) {
			System.err.println("Error: Invalid password format.");
			return false;
		}
		
		
		//hashing the new password
		String hashedPassword = hash(newPassword);
		if(hashedPassword == null) {
			System.err.println("Error: failure to hash password.");
			return false;
		}
		
		// Update the account with the hashed password
		account.setHashedPassword(hashedPassword);
		AccountFileManager.saveAccount(account);
		return true;
	}
	
	/**
	 * changes the username of a user's account
	 * @param newUsername the new username the user wants to set
	 * @param account the account of said user in question
	 * @return whether or not the username change was succesful
	 * @author Dwann
	 */
	public static boolean changeUsername(String newUsername, Account account) {
		// requirements: the new username must be valid. the new username must also be unique
		//
		// postconditions: the old username is replace with the new username. The username is updated in the CSV file.
		

		if (!Validation.isValidUsername(newUsername)) {
			System.out.println("Username \"" + newUsername + "\" failed:  " + missingUserReq(newUsername));
			return false;
		} else if (AccountFileManager.accountExists(newUsername)) {
			System.out.println("Username \"" + newUsername + "\" failed: Username alredy taken.");
			return false; 
		} else {
			account.setUsername(newUsername);
			return true;
		}
	}
	
	
	/**
	 * hashes the password into unique gibberish based on some algorithm
	 * @param password the password to be hashed
	 * @return the hashed password
	 * @author Sakif
	 */
	private static String hash(String password) {
		// postcondition: the password is hashed.
		// hashing the password using SHA-256
		if (password == null || password.isEmpty()) {
			return null;
		}
		try{
			//Use SHA-256 hash algorithm
			MessageDigest hasher = MessageDigest.getInstance("SHA-256");
			byte[] hashByte = hasher.digest(password.getBytes());
			StringBuilder hexString = new StringBuilder();
			for (byte b : hashByte){
				String hex = String.format("%02x", b);
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch (NoSuchAlgorithmException e){
			System.err.println("hashing algorithm not found! "+ e.getMessage());
			return null;
		}
			
	}
}
