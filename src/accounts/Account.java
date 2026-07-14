package accounts;

/**
 * class that packages all the data for a user Account
 * holds the username, hashed password, secret question and answer
 * @author Sakif
 */
public class Account {
/**
 * username for this account
 */
	private String username;
/**
 * hashed password for security measures.
 * Allows passwords to be stored in secrecy.
 */
	private String hashedPassword;
/**
 * A security question as an extra layer of protection for password.
 */
	private String secretQuestion;
/**
 * The answer for the security question.
 * Should be hashed in storage.
 */
	private String secretAnswer;
/**
 * Pathing to user's financial data.
 */
	//This may no longer be needed, depending on how the storage team handles it
	//private String pathToUserInfo;
	
/**
 * Create new account with the specific details
 * @param username Unique username for the account
 * @param hashedPassword 
 * @param secretQuestion A security question for recovery
 * @param secretAnswer The answer to the security question
 * @author Sakif
 */
	public Account(String username, String hashedPassword, String secretQuestion, String secretAnswer) {
		this.username = username;
		this.hashedPassword = hashedPassword;
		this.secretQuestion = secretQuestion;
		this.secretAnswer = secretAnswer;
	}
/**
 * Get username that belongs to the account.
 * @return accounts.Account username
 * @author Sakif
 */
	public String getUsername() {
		return username;
	}
/**
 * Set the username for the account.
 * @param username New username to set
 * @author Sakif
 */
	public void setUsername(String username) {
		this.username = username;
	}
/**
 * Get the hashed password for the account.
 * @return Hashed password string
 * @author Sakif
 */
	public String getHashedPassword() {
		return hashedPassword;
	}
/**
 * Set hashed password for the account.
 * In this case the password would be hashed prior to calling this method.
 * @param hashedPassword The new hashed password to be set
 * @author Sakif
 */
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
/**
 * Get security question for password recovery.
 * @return Security question
 * @author Sakif
 */
	public String getSecretQuestion() {
		return secretQuestion;
	}
/**
 * Get the answer to the secret question
 * @return Secret answer (should already be  hashed)
 * @author Sakif
 */
	public String getSecretAnswer() {
		return secretAnswer;
	}
}
