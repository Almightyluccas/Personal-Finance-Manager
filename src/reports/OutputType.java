package reports;

/**
 * Represents the available output formats for generated reports in the
 * Personal Finance Manager (PFM) application.
 *
 * <p>
 * Reports may either be displayed on the console or exported to a
 * Comma-Separated Values (CSV) file.
 * </p>
 *
 * @author Alyssa Johnson
 * @version 1.1
 * @since 1.0
 */
public enum OutputType {

    /**
     * Displays the report on the console.
     */
    CONSOLE("Console"),

    /**
     * Exports the report to a CSV file.
     */
    CSV("CSV");

    private final String displayName;

    /**
     * Constructs an OutputType with a readable display name.
     *
     * @param displayName the user-friendly name for the output type
     */
    OutputType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the user-friendly name for this output type.
     *
     * @return the display name for this output type
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the user-friendly name for this output type.
     *
     * @return the display name for this output type
     */
    @Override
    public String toString() {
        return displayName;
    }
}
