package integration;

import accounts.AccountService;
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
     * Runs the pre-authentication menu, handling login and registration
     * until the user either logs in successfully or chooses to exit.
     *
     * @return false if the user chose to exit the application, true otherwise
     * @author Luccas Amorim
     */
    private boolean runPreAuthMenu() {
        String choice = MenuUtil.promptChoice("Personal Finance Manager",
                "1. Login",
                "2. Register",
                "0. Exit");

        switch (choice) {
            case "1" -> handleLogin();
            case "2" -> handleRegister();
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
        String username = MenuUtil.promptString("Choose a username");
        String password = MenuUtil.promptString("Choose a password");
        String secretQuestion = MenuUtil.promptString("Enter a secret question (for account recovery)");
        String secretAnswer = MenuUtil.promptString("Enter the answer to your secret question");

        boolean success = AccountService.createAccount(username, password, secretQuestion, secretAnswer);

        if (success) {
            System.out.println("Account created! You can now log in.");
        } else {
            System.out.println("Registration failed. Username may already be taken or input was invalid.");
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
                    "2. Logout",
                    "0. Exit");

            switch (choice) {
                case "1" -> dispatchSelection(MenuOptions.STORAGE);
                case "2" -> {
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
     */
    public void dispatchSelection(MenuOptions option) {
        if (option == MenuOptions.EXIT) {
            return;
        }

        String moduleName = option.name().toLowerCase();
        AppModule module = registry.getModule(moduleName);

        if (module == null) {
            System.out.println("Module '" + moduleName + "' is not available.");
            return;
        }

        module.handleSelection();
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