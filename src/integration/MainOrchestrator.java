package integration;

import accounts.Account;
import accounts.AccountService;
import dataaudit.DataAudit;
import insights.InsightsModule;
import reports.ReportsModule;
import storage.AccountFileManager;
import storage.StorageModule;
import validation.Validation;

/**
 * Application entry point. Holds the module registry,
 * main menu loop, and dispatches user selections
 * to the appropriate module. Also owns the console flows for
 * account operations (login, registration, recovery, settings).
 */
public class MainOrchestrator {

    private final ModuleRegistry registry;

    /**
     * Constructs the orchestrator with a fresh registry.
     *
     * @author Luccas Amorim
     */
    public MainOrchestrator() {
        registry = new ModuleRegistry();

        registry.registerModule(new StorageModule());
        registry.registerModule(new ReportsModule());
        registry.registerModule(new InsightsModule());
        //TODO:  Ask for file name to be changed for consistency?
        registry.registerModule(new DataAudit());
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     * @author Luccas Amorim
     */
    public static void main(String[] args) {
        MainOrchestrator orchestrator = new MainOrchestrator();
        orchestrator.startApplication();
    }

    /**
     * Validates the registry, initializes every registered module, and
     * starts the main menu loop.
     *
     * @author Luccas Amorim
     */
    public void startApplication() {
        registry.validateRegistrations();

        for (AppModule module : registry.getAllModules()) {
            module.initialize();
        }

        boolean running = true;
        while (running) {
            if (AccountService.SessionManager.getCurrentUser() == null) {
                running = runPreAuthMenu();
            } else {
                running = runMainMenuLoop();
            }
        }
        shutdownApplication();
    }

    /**
     * Runs the pre-authentication menu, handling login, registration, &
     * password recovery options before the user logs into the system.
     *
     * @return false if the user chose to exit the application, true otherwise
     * @author Luccas Amorim
     * @author Yazmyrat Aydogdiyev
     */
    private boolean runPreAuthMenu() {
        String choice = MenuUtil.promptChoice("Personal Finance Manager",
                "1. Login",
                "2. Register",
                "3. Forgot Password",
                "0. Exit");

        switch (choice) {
            case "1" -> handleLogin();
            case "2" -> handleRegister();
            case "3" -> handleForgotPassword();
            case "0" -> {
                return !MenuUtil.promptYesNo("Are you sure you want to exit?");
            }
            default -> System.out.println("Invalid option, please try again.");
        }
        return true;
    }

    /**
     * Prompts for credentials and attempts to log in via the Accounts module.
     *
     * @author Luccas Amorim
     */
    private void handleLogin() {
        String username = MenuUtil.promptString("Username");
        String password = MenuUtil.promptString("Password");

        boolean success = AccountService.login(username, password);

        if (success) {
            System.out.println("Welcome, " + username + "!");
        } else {
            System.out.println("Login failed. Please check your credentials.");
        }
    }


    /**
     * Returns why a proposed username is unusable, or null if it's fine.
     *
     * @author Luccas Amorim
     */
    private String usernameError(String username) {
        if (username.length() < 4) {
            return "Username too short: must be at least 4 characters.";
        }
        if (username.length() > 20) {
            return "Username too long: must be 20 characters or fewer.";
        }
        if (username.contains(" ")) {
            return "Username cannot contain spaces.";
        }
        if (!Validation.isValidUsername(username)) {
            return "Username can only contain letters, digits, and underscores.";
        }
        if (AccountFileManager.accountExists(username)) {
            return "That username is already taken.";
        }
        return null;
    }

    /**
     * Returns why a proposed password is invalid, or null if it's fine.
     *
     * @author Luccas Amorim
     */
    private String passwordError(String password) {
        if (password.length() < 8) {
            return "Password too short: must be at least 8 characters.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit.";
        }
        return null;
    }

    /**
     * Returns why a secret question is invalid, or null if it's fine.
     *
     * @author Luccas Amorim
     */
    private String secretQuestionError(String question) {
        if (question.trim().length() < 10) {
            return "Question too short: must be at least 10 characters.";
        }
        if (question.trim().length() > 100) {
            return "Question too long: must be 100 characters or fewer.";
        }
        return null;
    }

    /**
     * Returns why a secret answer is invalid, or null if it's fine.
     *
     * @author Luccas Amorim
     */
    private String secretAnswerError(String answer) {
        if (answer.trim().length() < 2) {
            return "Answer too short: must be at least 2 characters.";
        }
        if (answer.trim().length() > 100) {
            return "Answer too long: must be 100 characters or fewer.";
        }
        return null;
    }

    /**
     * Prompts for new account details, re-prompting each field until valid,
     * registers via the Accounts module, and logs the user in on success.
     *
     * @author Luccas Amorim
     */
    private void handleRegister() {
        String username = MenuUtil.promptUntilValid(
                "Choose a username (4-20 characters; letters, digits, or underscores only)",
                this::usernameError);
        if (username == null) return;

        String password = MenuUtil.promptUntilValid(
                "Choose a password (at least 8 characters, with 1 uppercase letter, "
                        + "1 lowercase letter, and 1 digit)",
                this::passwordError);
        if (password == null) return;

        String secretQuestion = MenuUtil.promptUntilValid(
                "Enter a secret question for account recovery (10-100 characters)",
                this::secretQuestionError);
        if (secretQuestion == null) return;

        String secretAnswer = MenuUtil.promptUntilValid(
                "Enter the answer to your secret question (2-100 characters)",
                this::secretAnswerError);
        if (secretAnswer == null) return;

        try {
            boolean success = AccountService.createAccount(username, password, secretQuestion, secretAnswer);

            if (success) {
                AccountService.login(username, password);
                System.out.println("Account created! Welcome, " + username + "!");
            } else {
                System.out.println("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Runs the password recovery flow: looks up the account, shows the
     * secret question, verifies the answer, then resets the password.
     *
     * @author Luccas Amorim
     */
    private void handleForgotPassword() {
        String username = MenuUtil.promptString("Username");

        Account account = AccountService.getAccountFromUsername(username);
        if (account == null) {
            // getAccountFromUsername already prints the error.
            return;
        }

        System.out.println("Secret question: " + account.getSecretQuestion());
        String answer = MenuUtil.promptString("Answer");

        if (!AccountService.checkSecretAnswer(answer, account)) {
            System.out.println("Incorrect answer. Password reset cancelled.");
            return;
        }

        String newPassword = MenuUtil.promptUntilValid(
                "Enter your new password (at least 8 characters, with 1 uppercase letter, "
                        + "1 lowercase letter, and 1 digit)",
                this::passwordError);
        if (newPassword == null) return;

        String confirmed = MenuUtil.promptString("Confirm new password");

        if (AccountService.changePassword(newPassword, confirmed, account)) {
            System.out.println("Password reset! You can now log in.");
        } else {
            System.out.println("Password reset failed.");
        }
    }

    /**
     * Logs the current user out via the Accounts module.
     *
     * @author Luccas Amorim
     */
    private void handleLogout() {
        AccountService.logout();
        System.out.println("You have been logged out.");
    }

    /**
     * Runs the main menu loop, prompting for a menu option and
     * dispatching it until the user logs out or exits.
     *
     * @return false if the user chose to exit the application, true otherwise
     * @author Luccas Amorim
     */
    public boolean runMainMenuLoop() {
        while (true) {
            String choice = MenuUtil.promptChoice("Main Menu",
                    "1. Storage - Import, export, and manage budget CSV files",
                    "2. Reports - View spending and budget summaries",
                    "3. Insights - Analyze trends in your finances",
                    "4. Data Audit - Check your data files for errors",
                    "5. Account Settings - Username, password, recovery question",
                    "6. Logout",
                    "0. Exit");

            switch (choice) {
                case "1" -> dispatchSelection(MenuOptions.STORAGE);
                case "2" -> dispatchSelection(MenuOptions.REPORTS);
                case "3" -> dispatchSelection(MenuOptions.INSIGHTS);
                case "4" -> dispatchSelection(MenuOptions.DATA_AUDIT);
                case "5" -> handleAccountSettings();
                case "6" -> {
                    if (MenuUtil.promptYesNo("Are you sure you want to log out?")) {
                        handleLogout();
                        return true;
                    }
                }
                case "0" -> {
                    if (MenuUtil.promptYesNo("Are you sure you want to exit?")) {
                        handleLogout();
                        dispatchSelection(MenuOptions.EXIT);
                        return false;
                    }
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    /**
     * Runs the account settings submenu for the logged-in user.
     *
     * @author Luccas Amorim
     */
    private void handleAccountSettings() {
        boolean inSettings = true;
        while (inSettings) {
            String choice = MenuUtil.promptChoice("Account Settings",
                    "1. Change Username",
                    "2. Change Password",
                    "3. Update Secret Question & Answer",
                    "0. Back to Main Menu");

            switch (choice) {
                case "1" -> handleChangeUsername();
                case "2" -> handleChangePassword();
                case "3" -> handleUpdateSecretQA();
                case "0" -> inSettings = false;
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    /**
     * Prompts for a new username and updates it via the Accounts module.
     *
     * @author Luccas Amorim
     */
    private void handleChangeUsername() {
        String newUsername = MenuUtil.promptUntilValid(
                "Enter your new username (4-20 characters; letters, digits, or underscores only)",
                this::usernameError);
        if (newUsername == null) return;

        if (AccountService.changeUsername(newUsername,
                AccountService.SessionManager.getCurrentUser())) {
            System.out.println("Username updated to " + newUsername + ".");
        } else {
            System.out.println("Username change failed.");
        }
    }

    /**
     * Changes the logged-in user's password after verifying the current one.
     *
     * @author Luccas Amorim
     */
    private void handleChangePassword() {
        String current = MenuUtil.promptString("Current password");

        String newPassword = MenuUtil.promptUntilValid(
                "Enter your new password (at least 8 characters, with 1 uppercase letter, "
                        + "1 lowercase letter, and 1 digit)",
                this::passwordError);
        if (newPassword == null) return;

        String confirmed = MenuUtil.promptString("Confirm new password");

        if (AccountService.changePassword(current, newPassword, confirmed,
                AccountService.SessionManager.getCurrentUser())) {
            System.out.println("Password updated.");
        } else {
            System.out.println("Password change failed.");
        }
    }

    /**
     * Updates the logged-in user's secret question and answer after
     * verifying the current password.
     *
     * @author Luccas Amorim
     */
    private void handleUpdateSecretQA() {
        String current = MenuUtil.promptString("Current password");

        String question = MenuUtil.promptUntilValid(
                "Enter your new secret question (10-100 characters)",
                this::secretQuestionError);
        if (question == null) return;

        String answer = MenuUtil.promptUntilValid(
                "Enter the answer to your secret question (2-100 characters)",
                this::secretAnswerError);
        if (answer == null) return;

        if (AccountService.changeSecretQA(current, question, answer,
                AccountService.SessionManager.getCurrentUser())) {
            System.out.println("Secret question and answer updated.");
        } else {
            System.out.println("Update failed.");
        }
    }

    /**
     * Dispatches a single main menu selection to the corresponding module,
     * assuming the module's registered name matches the option's constant
     * name.
     *
     * @param option the selected menu option
     * @author Luccas Amorim
     * @author Mohsen Kanj
     */
    void dispatchSelection(MenuOptions option) {
        if (option == MenuOptions.EXIT) {
            return;
        }

        String moduleName = option.name().toLowerCase().replace("_", "");
        AppModule module = registry.getModule(moduleName);

        if (module == null) {
            System.out.println("Module '" + moduleName + "' is not available.");
            return;
        }

        try {
            module.handleSelection();
        } catch (RuntimeException e) {
            System.err.println("Error in module '" + moduleName + "': " + e.getMessage());
            System.err.println("Returning to main menu.");
        }
    }

    /**
     * Performs any cleanup needed before the application exits.
     *
     * @author Luccas Amorim
     */
    public void shutdownApplication() {
        System.out.println("Goodbye!");
    }
}