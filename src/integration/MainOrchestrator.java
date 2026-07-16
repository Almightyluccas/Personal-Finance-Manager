package integration;

import accounts.AccountService;
import dataaudit.DataAudit;
import insights.InsightsModule;
import reports.ReportsModule;
import storage.StorageModule;


/**
 * Application entry point. Holds the module registry,
 * main menu loop, and dispatches user selections
 * to the appropriate module.
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
        for (AppModule module : registry.getAllModules()) {
            module.initialize();
        }

        boolean running = true;
        while (running) {
            if (AccountService.SessionManager.getCurrentUser() == null) {
                running = runPreAuthMenu();
            } else {
                runMainMenuLoop();
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
            case "3" -> System.out.println("Password recovery feature coming soon.");
            case "0" -> {
                return false;
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
     * Prompts for new account details and attempts to register via the
     * Accounts module.
     *
     * @author Luccas Amorim
     */
    private void handleRegister() {
        String username = MenuUtil.promptString(
                "Choose a username (4-20 characters; letters, digits, or underscores only)");
        String password = MenuUtil.promptString(
                "Choose a password (at least 8 characters, with 1 uppercase letter, "
                        + "1 lowercase letter, and 1 digit)");
        String secretQuestion = MenuUtil.promptString(
                "Enter a secret question for account recovery (10-100 characters)");
        String secretAnswer = MenuUtil.promptString(
                "Enter the answer to your secret question (2-100 characters)");

        try {
            boolean success = AccountService.createAccount(username, password, secretQuestion, secretAnswer);

            if (success) {
                System.out.println("Account created! You can now log in.");
                //TODO: Should make it so user gets automatically logged in, would improve UX

            } else {
                // TODO: Registration failure is currently a single generic message.
                // AccountService.createAccount() only returns a boolean, so we can't tell which
                // rule failed (bad username, bad password, bad secret Q/A, or username taken).
                // we could possible call a method from Validation for each field so we would know which
                // input was actually wrong. This way we are able to improve UX."
                System.out.println("Registration failed. Username may already be taken or input was invalid.");
            }
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
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
     * dispatching it until the user selects EXIT.
     *
     * @author Luccas Amorim
     */
    public void runMainMenuLoop() {
        boolean loggedIn = true;
        while (loggedIn) {
            String choice = MenuUtil.promptChoice("Main Menu",
                    "1. Storage",
                    "2. Reports",
                    "3. Insights",
                    "4. Data Audit",
                    "5. Logout",
                    "0. Exit");

            switch (choice) {
                case "1" -> dispatchSelection(MenuOptions.STORAGE);
                case "2" -> dispatchSelection(MenuOptions.REPORTS);
                case "3" -> dispatchSelection(MenuOptions.INSIGHTS);
                case "4" -> dispatchSelection(MenuOptions.DATA_AUDIT);
                case "5" -> {
                    handleLogout();
                    loggedIn = false;
                }
                case "0" -> {
                    dispatchSelection(MenuOptions.EXIT);
                    loggedIn = false;
                }
                default -> System.out.println("Invalid option, please try again.");
            }
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
        // TODO: module.handleSelection() runs code owned by other teams.
        //  An uncaught RuntimeException from any of them will
        // currently propagate all the way up and crash the app. we need to wrap this
        // in a try-catch (log it, print a friendly message, and return to the
        // main menu) so one module's bug can't take down the whole program.
        

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
      }   catch (RuntimeException e) {
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
