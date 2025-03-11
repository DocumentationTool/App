package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.exception.InvalidPathException;

import java.nio.file.Path;

/**
 * Helper class for database operations
 */
public class DbHelper {


    private DbHelper() {
        // Prevent instantiation
    }

    /**
     * Helper method to limit how many things sql returns before antpath matching the rest, this method converts any possible antpath values to sql like values
     *
     * @param antPath the ant path to convert, if null is given returns % to match everything
     * @return the sql like path
     */
    public static String convertAntPathToSQLLike(String antPath) {
        if (antPath == null || antPath.isEmpty()) {
            return "%"; // Match everything if empty
        }

        StringBuilder sqlPattern = new StringBuilder();

        for (int i = 0; i < antPath.length(); i++) {
            char c = antPath.charAt(i);

            switch (c) {
                case '*':
                    // Handle "**" (match multiple directories)
                    if (i + 1 < antPath.length() && antPath.charAt(i + 1) == '*') {
                        sqlPattern.append("%");
                        i++; // Skip next *
                    } else {
                        sqlPattern.append("%"); // Single * becomes %
                    }
                    break;
                case '?':
                    sqlPattern.append("_"); // Single character match
                    break;
                case '{':
                    // Replace `{variable}` with `%`
                    sqlPattern.append("%");
                    // Skip until `}`
                    while (i < antPath.length() && antPath.charAt(i) != '}') {
                        i++;
                    }
                    break;
                case '\\': // Normalize backslashes to forward slashes
                    sqlPattern.append("/");
                    break;
                case '%', '_':
                    // Escape SQL wildcards (_ and %) to prevent accidental misuse
                    sqlPattern.append("\\").append(c);
                    break;
                default:
                    sqlPattern.append(c);
                    break;
            }
        }

        return sqlPattern.toString();
    }

    /**
     * Checks if a path is allowed
     *
     * @param path the path to check
     * @return null if the path is allowed, otherwise a message explaining why it is not allowed
     */
    public static void validatePath(Path path) throws InvalidPathException {
        if (path == null) {
            throw new InvalidPathException(null, "The path cannot be null");
        }

        String pathStr = path.toString();

        if (pathStr.length() >= 255) {
            throw new InvalidPathException(null, "Path '%s' is too long contained %s characters expected less than 255".formatted(pathStr, pathStr.length()));
        }

        if (pathStr.contains("..")) {
            throw new InvalidPathException(null, "Path '%s' cannot contain '..' to escape the current directory".formatted(pathStr));
        }

        if (pathStr.contains("%")) {
            throw new InvalidPathException(null, "Path '%s' cannot contain '%' duo to SQL issues".formatted(pathStr));
        }

        if (path.isAbsolute()) {
            throw new InvalidPathException(null, "Path '%s' cannot be absolute".formatted(pathStr));
        }

        boolean matches = pathStr.matches("^[a-zA-Z0-9_\\\\\\-./*?{}]+$");
        if (!matches) {
            throw new InvalidPathException(null, "Path '%s' contains invalid characters, only a-z, A-Z, 0-9, _, -, ., /, *, ?, and {} are allowed".formatted(pathStr));
        }
    }

    public static void validateFileType(Path path) throws com.wonkglorg.doc.core.exception.InvalidPathException {
        if (!path.toString().endsWith(".md")) {
            throw new InvalidPathException(null, "Path '%s' file type is not allowed, only .md files are allowed".formatted(path.toString()));
        }
    }
}
