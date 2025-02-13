package com.wonkglorg.docapi.db.dbs;


import javax.sql.DataSource;
import java.util.logging.Logger;


/**
 * @author Wonkglorg
 * <p>
 * Base class for databases
 */
@SuppressWarnings("unused")
public abstract class Database implements AutoCloseable {
	public record DatabaseType(String name, String driver, String classLoader) {
	}

	public static final DatabaseType MYSQL =
			new DatabaseType("Mysql", "jdbc:mysql:", "com.mysql.cj.jdbc.Driver");
	public static final DatabaseType SQLITE =
			new DatabaseType("Sqlite", "jdbc:sqlite:", "org.sqlite.JDBC");
	public static final DatabaseType POSTGRESQL =
			new DatabaseType("PostgreSQL", "jdbc:postgresql:", "org.postgresql.Driver");
	public static final DatabaseType SQLSERVER =
			new DatabaseType("SQLServer", "jdbc:sqlserver:", "org.sqlserver.jdbc.SQLServerDriver");
	public static final DatabaseType MARIA_DB =
			new DatabaseType("MariaDB", "jdbc:mariadb:", "org.mariadb.jdbc.Driver");
	protected final String driver;
	protected final String classloader;
	protected final Logger logger = Logger.getLogger(Database.class.getName());
	protected DataSource dataSource;

	protected Database(DatabaseType databaseType) {
		this.driver = databaseType.driver();
		this.classloader = databaseType.classLoader();
	}

	protected Database(final String driver, final String classLoader) {
		this.driver = driver;
		this.classloader = classLoader;
	}

	/**
	 * @return the classloader path
	 */
	public String getClassLoader() {
		return classloader;
	}

	/**
	 * @return The database driver
	 */
	public String getDriver() {
		return driver;
	}


	/**
	 * Small helper method to sanitize input for sql only does not other sanitizations like xss or
	 * html based
	 *
	 * @param input The input to sanitize
	 * @return The sanitized output
	 */
	public String sanitize(String input) {
		return input.replaceAll("[^a-zA-Z0-9]", "");
	}

}
