package storage;

import accounts.Account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Manages persistence of user account data to and from the file system,
 * including basic password obfuscation.
 *
 * @author Mohammed, Ayub, Fuad
 */
public final class AccountFileManager {

    private static final FileUtil FILE_UTIL = new FileUtil();
    private static final Path ACCOUNT_FILE = FILE_UTIL.resolvePath("accounts.csv");
    private static final String HEADER = "Username,HashedPassword,SecretQuestion,SecretAnswer";
    private static final int EXPECTED_FIELD_COUNT = 4;

    /**
     * Private constructor prevents this utility class from being instantiated.
     *
     * @author Mohammed, Ayub, Fuad
     */
    private AccountFileManager() {
    }

    /**
     * Saves the given account's data to the file system.
     *
     * <p>For the alpha build, this method stores the username and hashed
     * password. Secret question and secret answer are stored as blank values
     * until Accounts wires those fields into the save call.</p>
     *
     * @param username the username identifying the account
     * @param password the already-hashed password to persist, which will be obfuscated
     * @author Mohammed, Ayub, Fuad
     */
    public static void saveAccount(String username, String password) {
        saveAccount(username, password, "", "");
    }

    /**
     * Saves the given full account data to the file system.
     *
     * @param username the username identifying the account
     * @param hashedPassword the already-hashed password to persist
     * @param secretQuestion the secret question for account recovery
     * @param secretAnswer the secret answer for account recovery
     * @author Mohammed, Ayub, Fuad
     */
    public static void saveAccount(
            String username,
            String hashedPassword,
            String secretQuestion,
            String secretAnswer
    ) {
        Account account = new Account(username, hashedPassword, secretQuestion, secretAnswer);
        saveAccount(account);
    }

    /**
     * Saves the given account object to the file system.
     *
     * @param account the account to save
     * @author Mohammed, Ayub, Fuad
     */
    public static void saveAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null.");
        }

        validateUsername(account.getUsername());

        try {
            ensureAccountFileExists();

            List<String> lines = Files.readAllLines(ACCOUNT_FILE, StandardCharsets.UTF_8);
            List<String> updatedLines = new ArrayList<>();

            updatedLines.add(HEADER);

            boolean wasUpdated = false;

            for (int i = 1; i < lines.size(); i++) {
                Account existingAccount = parseAccount(lines.get(i));

                if (existingAccount != null
                        && existingAccount.getUsername().equals(account.getUsername())) {
                    updatedLines.add(toCsvLine(account));
                    wasUpdated = true;
                } else {
                    updatedLines.add(lines.get(i));
                }
            }

            if (!wasUpdated) {
                updatedLines.add(toCsvLine(account));
            }

            Files.write(ACCOUNT_FILE, updatedLines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save account: "
                    + account.getUsername(), exception);
        }
    }

    /**
     * Loads account data for the specified username from the file system.
     *
     * @param username the username identifying the account
     * @return the loaded {@link Account}, or {@code null} if not found
     * @author Mohammed, Ayub, Fuad
     */
    public static Object loadAccount(String username) {
        validateUsername(username);

        if (!Files.exists(ACCOUNT_FILE)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(ACCOUNT_FILE, StandardCharsets.UTF_8);

            for (int i = 1; i < lines.size(); i++) {
                Account account = parseAccount(lines.get(i));

                if (account != null && account.getUsername().equals(username)) {
                    return account;
                }
            }

            return null;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load account: " + username, exception);
        }
    }

    /**
     * Deletes the account data associated with the specified username.
     *
     * @param username the username identifying the account
     * @author Mohammed, Ayub, Fuad
     */
    public static void deleteAccount(String username) {
        validateUsername(username);

        if (!Files.exists(ACCOUNT_FILE)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(ACCOUNT_FILE, StandardCharsets.UTF_8);
            List<String> updatedLines = new ArrayList<>();

            updatedLines.add(HEADER);

            for (int i = 1; i < lines.size(); i++) {
                Account account = parseAccount(lines.get(i));

                if (account == null || !account.getUsername().equals(username)) {
                    updatedLines.add(lines.get(i));
                }
            }

            Files.write(ACCOUNT_FILE, updatedLines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete account: " + username, exception);
        }
    }

    /**
     * Determines whether an account exists for the specified username.
     *
     * @param username the username to check
     * @return {@code true} if an account exists, {@code false} otherwise
     * @author Mohammed, Ayub, Fuad
     */
    public static boolean accountExists(String username) {
        if (username == null || username.isBlank() || !Files.exists(ACCOUNT_FILE)) {
            return false;
        }

        return loadAccount(username) != null;
    }

    /**
     * Obfuscates the given plaintext password prior to storage.
     *
     * @param plainTextPassword the plaintext password to obfuscate
     * @return the obfuscated password
     * @author Mohammed, Ayub, Fuad
     */
    public static String obfuscatePassword(String plainTextPassword) {
        if (plainTextPassword == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }

        return Base64.getEncoder()
                .encodeToString(plainTextPassword.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reverses obfuscation on a stored password, returning its plaintext form.
     *
     * @param obfuscatedPassword the obfuscated password to decode
     * @return the plaintext password
     * @author Mohammed, Ayub, Fuad
     */
    public static String deobfuscatePassword(String obfuscatedPassword) {
        if (obfuscatedPassword == null) {
            throw new IllegalArgumentException("Obfuscated password cannot be null.");
        }

        byte[] decodedBytes = Base64.getDecoder().decode(obfuscatedPassword);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Creates the data directory and account file if they do not exist.
     *
     * @throws IOException if the directory or file cannot be created
     * @author Mohammed, Ayub, Fuad
     */
    private static void ensureAccountFileExists() throws IOException {
        try {
            FILE_UTIL.ensureDataDirectoryExists();
        } catch (IllegalStateException e) {
            throw new IOException("Unable to create data directory.", e);
        }

        if (!Files.exists(ACCOUNT_FILE)) {
            List<String> lines = new ArrayList<>();
            lines.add(HEADER);
            Files.write(ACCOUNT_FILE, lines, StandardCharsets.UTF_8);
        }
    }

    /**
     * Converts an account object into a CSV line.
     *
     * @param account the account to convert
     * @return the CSV-formatted account line
     * @author Mohammed, Ayub, Fuad
     */
    private static String toCsvLine(Account account) {
        return escapeCsv(account.getUsername()) + ","
                + escapeCsv(obfuscatePassword(account.getHashedPassword())) + ","
                + escapeCsv(account.getSecretQuestion()) + ","
                + escapeCsv(account.getSecretAnswer());
    }

    /**
     * Converts a CSV line into an account object.
     *
     * @param line the CSV line to parse
     * @return the parsed account, or {@code null} if invalid
     * @author Mohammed, Ayub, Fuad
     */
    private static Account parseAccount(String line) {
        List<String> fields = parseCsvLine(line);

        if (fields.size() != EXPECTED_FIELD_COUNT) {
            return null;
        }

        String username = fields.get(0);
        String hashedPassword = deobfuscatePassword(fields.get(1));
        String secretQuestion = fields.get(2);
        String secretAnswer = fields.get(3);

        return new Account(username, hashedPassword, secretQuestion, secretAnswer);
    }

    /**
     * Escapes a value for safe CSV storage.
     *
     * @param value the value to escape
     * @return the escaped CSV value
     * @author Mohammed, Ayub, Fuad
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        String escapedValue = value.replace("\"", "\"\"");

        if (escapedValue.contains(",")
                || escapedValue.contains("\"")
                || escapedValue.contains("\n")
                || escapedValue.contains("\r")) {
            return "\"" + escapedValue + "\"";
        }

        return escapedValue;
    }

    /**
     * Parses one CSV line into fields.
     *
     * @param line the CSV line to parse
     * @return the parsed fields
     * @author Mohammed, Ayub, Fuad
     */
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentCharacter = line.charAt(i);

            if (currentCharacter == '"') {
                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (currentCharacter == ',' && !insideQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(currentCharacter);
            }
        }

        fields.add(currentField.toString());
        return fields;
    }

    /**
     * Validates that a username is not null or blank.
     *
     * @param username the username to validate
     * @author Mohammed, Ayub, Fuad
     */
    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank.");
        }
    }
}