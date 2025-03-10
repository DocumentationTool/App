package com.wonkglorg.doc.core.db;

import java.nio.file.Path;

/**
 * Helper class for database operations
 */
public class DbHelper{
	
	/**
	 * Helper method to limit how many things sql returns before antpath matching the rest, this method converts any possible antpath values to sql like values
	 *
	 * @param antPath the ant path to convert, if null is given returns % to match everything
	 * @return the sql like path
	 */
	public static String convertAntPathToSQLLike(String antPath) {
		if(antPath == null || antPath.isEmpty()){
			return "%"; // Match everything if empty
		}
		
		StringBuilder sqlPattern = new StringBuilder();
		
		for(int i = 0; i < antPath.length(); i++){
			char c = antPath.charAt(i);
			
			switch(c) {
				case '*':
					// Handle "**" (match multiple directories)
					if(i + 1 < antPath.length() && antPath.charAt(i + 1) == '*'){
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
					while(i < antPath.length() && antPath.charAt(i) != '}'){
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
	
	public static boolean isAllowedPath(Path path) {
		if (path == null) {
			return false;
		}
		
		String pathStr = path.toString();
		
		if (pathStr.length() >= 255) {
			return false;
		}
		
		if (pathStr.contains("..")) {
			return false;
		}
		
		if (pathStr.contains("%")) {
			return false;
		}
		
		return pathStr.matches("^[a-zA-Z0-9_\\-./*?{}]+$");
	}
	
	
	
}
