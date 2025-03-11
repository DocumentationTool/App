package com.wonkglorg.doc.core.db;

import java.nio.file.Path;

/**
 * Helper class for database operations
 */
public class DbHelper {

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
    public static String isAllowedPath(Path path) {
        if (path == null) {
            return "The path cannot be null";
        }

        String pathStr = path.toString();

        if (pathStr.length() >= 255) {
            return "The path is too long contained " + pathStr.length() + " characters expected less than 255";
        }

        if (pathStr.contains("..")) {
            return "The path cannot contain '..' to escape the current directory";
        }

        if (pathStr.contains("%")) {
            return "The path cannot contain '%' duo to SQL issues";
        }

        boolean matches = pathStr.matches("^[a-zA-Z0-9_\\-./*?{}]+$");
        if (!matches) {
            return "The path contains invalid characters, only a-z, A-Z, 0-9, _, -, ., /, *, ?, and {} are allowed";
        }
        return null;
    }


}
