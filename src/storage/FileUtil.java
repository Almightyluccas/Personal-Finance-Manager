package storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides low-level file system utility operations used throughout the
 * Storage module, such as path resolution and data directory management.
 *
 * @author Mohammed
 */
public class FileUtil {

    /**
     * The application's data directory, relative to the working
     * directory the application is launched from. Kept consistent with
     * the root {@link AccountFileManager} and {@link BudgetStorage}
     * already use for their own file storage.
     */
    private static final Path DATA_DIRECTORY = Paths.get("data");

    /**
     * Constructs a new {@code FileUtil} instance.
     *
     * @author Mohammed
     */
    public FileUtil() {
    }

    /**
     * Resolves the given relative file name to an absolute path within the
     * application's data directory.
     *
     * @param fileName the relative file name to resolve
     * @return the resolved absolute path
     * @throws IllegalArgumentException if {@code fileName} is null or blank
     * @author Mohammed
     */
    public Path resolvePath(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be blank.");
        }

        return DATA_DIRECTORY.resolve(fileName.trim()).toAbsolutePath().normalize();
    }

    /**
     * Ensures that the application's data directory exists, creating it if
     * necessary.
     *
     * @throws IllegalStateException if the data directory could not be created
     * @author Mohammed
     */
    public void ensureDataDirectoryExists() {
        try {
            Files.createDirectories(DATA_DIRECTORY);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to create data directory: " + DATA_DIRECTORY, e);
        }
    }

    /**
     * Determines whether the file at the specified path exists.
     *
     * @param path the path to check
     * @return {@code true} if the file exists, {@code false} otherwise
     * @author Mohammed
     */
    public boolean fileExists(Path path) {
        return path != null && Files.exists(path);
    }
}
