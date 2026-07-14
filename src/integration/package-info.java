/**
 * Integration layer for the Personal Finance Manager (PFM) application.
 * <p>
 * This package serves as the central orchestration point that ties together
 * all feature modules into a single running application. I
 * <p>
 * Key components:
 * <ul>
 *   <li>{@link integration.MainOrchestrator} - sole entry point of the application</li>
 *   <li>{@link integration.AppModule} - interface each feature module implements
 *       to plug into the application</li>
 *   <li>{@link integration.ModuleRegistry} - string-based registration and
 *       lookup of available modules</li>
 *   <li>{@link integration.MenuUtil} - shared console prompting utilities
 *       ({@code promptChoice}, {@code promptYesNo}, {@code promptString})</li>
 *   <li>{@link integration.MenuOptions} - menu options scoped internally to
 *       this package</li>
 * </ul>
 */
package integration;

