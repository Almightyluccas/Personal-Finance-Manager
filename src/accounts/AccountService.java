package accounts;
import validation.Validation;
import storage.AccountFileManager;
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
			return false;
		}
		if (AccountFileManager.accountExists(username)) {
			return false; 
		}
		if (!Validation.isValidPassword(password)) {
			return false;
		}
		if (!Validation.isValidSecretQuestion(secretQuestion)) {
			return false;
		}
		if (!Validation.isValidSecretAnswer(secretAnswer)) {
			return false;
		}
		
		password = hash(password);
		secretAnswer = hash(secretAnswer);
		
		//Assuming storage uses a try-catch block:
		AccountFileManager.saveAccount(username, password, secretQuestion, secretAnswer);
		return true;
	
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
					AccountFileManager.saveAccount(currentUser, currentUser.getUsername());
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
	 * gets the secret question for a given account, for Integration to display
	 * before prompting the user for their answer
	 * @param username the username to look up
	 * @return the account's secret question, or null if no such account exists
	 * @author Harmony
	 */
	public static String getSecretQuestion(String username) {
		//Removed and replaced with method getAccountFromUsername()
		/*if (!AccountFileManager.accountExists(username)) {
			return null;
		}
		Account account = (Account) AccountFileManager.loadAccount(username);*/
		Account account = getAccountFromUsername(username);
		if (account == null) {
			return null;
		}
		return account.getSecretQuestion();
	}
	
	/**
	 * Changes the secret question and answer upon user request.
	 * @param currentPassword the password to authenticate the user.
	 * @param question The new secret question the user want's to set.
	 * @param answer The answer to the new secret question.
	 * @param account the account that will be effected by the changes.
	 * @return Whether or not the change was successful.
	 * @author Dwann
	 */
	public static boolean changeSecretQA(String currentPassword,
										 String question,
										 String answer,
										 Account account) 
	{
		if (!hash(currentPassword).equals(account.getHashedPassword())) {
			System.out.println("Error: The password entered is incorrect.");
			return false;
		}
		
		if(!Validation.isValidSecretQuestion(question)) {
			System.out.println("Error: The secret question must be between 10 and 100 characters.");
			return false;
		}
		
		if(!Validation.isValidSecretAnswer(answer)) {
			System.out.println("Error: The secret answer must be between 2 and 100 characters.");
			return false;
		}
		
		account.setSecretQuestion(question);
		account.setSecretAnswer(hash(answer));
		AccountFileManager.saveAccount(account, account.getUsername());
		return true;
	}
	
	/**
	 * Gets an account from the username passed
	 * @param username The user's username
	 * @return an account object with the given username, or null if doesn't exist
	 * @author Dwann
	 */
	public static Account getAccountFromUsername(String username) {
		if (!AccountFileManager.accountExists(username)) {
			System.out.println("Error: No account with username " + username + ".");
			return null;
		}
		
		Account account = (Account) AccountFileManager.loadAccount(username);
		return account;
	}
	
	/**
	 * verifies the secret answer for a given account and, if correct, resets
	 * the account's password to newPassword
	 * @param username the username whose password is being reset
	 * @param secretAnswer the answer supplied by the user
	 * @param newPassword the new password to set if the answer is correct
	 * @return whether or not the password reset was successful
	 * @author Harmony
	 */
	/*public static boolean resetPassword(String username, String secretAnswer, String newPassword, String confirmedPassword) {
		if (!AccountFileManager.accountExists(username)) {
			return false;
		}
		Account account = (Account) AccountFileManager.loadAccount(username);
		if (account == null) {
			return false;
		}
		if (!checkSecretAnswer(secretAnswer, account)) {
			return false;
		}
		
		return changePassword(newPassword, confirmedPassword, account);
	}*/

	/**
	 * verifies the secretAnswer from the user with the one in their account
	 * @param secretAnswer the secret answer entered from the user
	 * @param account the account of said user to compare the secret questions
	 * @return whether or not the answers matched
	 * @author Harmony
	 */
	public static boolean checkSecretAnswer(String secretAnswer, Account account) {
		// postcondition: if the secret answer matches the accounts, the user can change whatever they need to accordingly.
		if (secretAnswer == null || account.getSecretAnswer() == null) {
			return false;
		}
		// normalize (trim + lowercase) before hashing so the check stays forgiving of
		// case/whitespace, while still comparing hashed values like the password check does
		String hashedInput = hash(secretAnswer); //removed trim() and toLowerCase() so the hash matches the one in the account when created.
		return hashedInput != null && hashedInput.equals(account.getSecretAnswer());
	}
	/**
	 * Changes the password for a user in their account settings.
	 * @param current The users current password.
	 * @param newPass The password the user wants to change to.
	 * @param confirmedPass Confirmation of the users new password.
	 * @param account The user's account, to set the new password.
	 * @return Whether or not the password change was successful.
	 * @author Dwann
	 */
	public static boolean changePassword(String current,
										 String newPassword,
										 String confirmedPassword,
										 Account account) 
	{
		//verify the current password matches
		if (!hash(current).equals(account.getHashedPassword())) {
			System.out.println("Error: The current password entered is invalid.");
			return false;
		}
		
		if (!newPassword.equals(confirmedPassword)) {
			System.out.println("Error: The passwords don\'t match.");
			return false;
		}
		
		account.setHashedPassword(hash(newPassword));
		AccountFileManager.saveAccount(account, account.getUsername());
		return true;
	}
	
	/**
	 * changes the password of a user's account from the previously forgotten one
	 * @param newPassword the new password the user wants to set
	 * @param confirmedPassword the double check to make sure the user entered the new password wanted
	 * @param account the account of said user in question
	 * @return whether or not the password change was succesful
	 * @author Sakif
	 */
	
	public static boolean changePassword(String newPassword, String confirmedPassword, Account account) {
		// requirements: the new password must be valid. the password must then be hashed
		//
		// postconditions: the old password is replace with the new password. The password is updated in the CSV file.
		if (account == null) {
			return false;
		}
		
		if(!Validation.isValidPassword(newPassword)) {
			System.out.println("Error: Invalid password format.");
			return false;
		}
		
		if (!newPassword.equals(confirmedPassword)) {
			System.out.println("Error: The passwords don\'t match.");
			return false;
		}
		
		//hashing the new password
		String hashedPassword = hash(newPassword);
		if(hashedPassword == null) {
			System.out.println("Error: failure to hash password.");
			return false;
		}
		
		// Update the account with the hashed password
		account.setHashedPassword(hashedPassword);
		AccountFileManager.saveAccount(account, account.getUsername());
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
			return false;
		} else if (AccountFileManager.accountExists(newUsername)) {
			return false; 
		} else {
			String oldUsername = account.getUsername();
			account.setUsername(newUsername);
			AccountFileManager.saveAccount(account, oldUsername);
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
