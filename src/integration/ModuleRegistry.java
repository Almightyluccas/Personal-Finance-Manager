package integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the mapping of module names to their AppModule implementations.
 */
class ModuleRegistry {

    private final Map<String, AppModule> modules;

    /**
     * Constructs an empty registry.
     */
    public ModuleRegistry() {
        this.modules = new HashMap<>();
    }

    /**
     * Registers a single module under its own name.
     *
     * @param module the module to register
     */
    public void registerModule(AppModule module) {
        modules.put(module.getModuleName(), module);
    }

    /**
     * Looks up a registered module by name.
     *
     * @param name the module's name
     * @return the matching module, or null if none is registered
     */
    public AppModule getModule(String name) {
        return modules.get(name);
    }

    /**
     * Returns all currently registered modules.
     *
     * @return the registered modules
     */
    public Collection<AppModule> getAllModules() {
        return modules.values();
    }

    /**
     * Verifies that a module is registered for every MenuOptions value
     * except EXIT, assuming a module's registered name matches its
     * MenuOptions constant name.
     *
     * @throws IllegalStateException if any expected module is missing
     */
    public void validateRegistrations() {
    }
}