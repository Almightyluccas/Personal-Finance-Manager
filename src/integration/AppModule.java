package integration;

/**
 * Contract implemented by every feature module (Accounts, Insights,
 * Data Audit, Reports, Storage) so it can be discovered and invoked by
 * the Integration layer.
 */
public interface AppModule {

    /**
     * Returns the unique name used to identify this module in the registry.
     *
     * @return the module's name
     */
    String getModuleName();

    /**
     * Performs any one-time setup this module needs before it can handle
     * selections, can be left empty.
     */
    void initialize();

    /**
     * Handles the user's menu selection for this module.
     */
    void handleSelection();
}
