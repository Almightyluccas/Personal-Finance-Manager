package integration;

/**
 * Enum for main menu options presented to the user.
 * Internal to the Integration module. Feature modules are looked up by
 * name from ModuleRegistry rather than referencing this enum directly.
 */
enum MenuOptions {

    /** Manage user accounts. */
    ACCOUNTS,

    /** View financial insights. */
    INSIGHTS,

    /** Run a data audit. */
    DATA_AUDIT,

    /** Generate reports. */
    REPORTS,

    /** Manage data storage. */
    STORAGE,

    /** Exit the application. */
    EXIT
}
