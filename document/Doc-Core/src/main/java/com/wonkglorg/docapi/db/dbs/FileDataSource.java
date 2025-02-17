package com.wonkglorg.docapi.db.dbs;


import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileDataSource implements DataSource {
	private static final Logger log = Logger.getLogger(FileDataSource.class.getName());
	protected final Path sourceDbFile;
	protected final Path dbFile;
	private final String connectionString;
	private final Database.DatabaseType databaseType;
	protected Connection connection;

	/**
	 * IInstantiates a new Datasource
	 *
	 * @param type the type of database to connect to (has to be a file based one)
	 * @param file the file to connect to (or create if absent)
	 */
	public FileDataSource(Database.DatabaseType type, Path file) {
		this(type, file, file, type.driver() + file.toString());
	}

	/**
	 * Instantiates a new Datasource
	 *
	 * @param type the type of database to connect to (has to be a file based one)
	 * @param file the file to connect to (or create if absent)
	 * @param connectionString the url to connect to the driver with if a custom one should be used
	 */
	public FileDataSource(Database.DatabaseType type, Path file, String connectionString) {
		this(type, file, file, connectionString);
	}

	/**
	 * Instantiates a new Datasource
	 *
	 * @param type the type of database to connect to (has to be a file based one)
	 * @param sourceFile the location to copy it from if it exists
	 * @param file the file to connect to (or create if absent)
	 */
	public FileDataSource(Database.DatabaseType type, Path sourceFile, Path file) {
		this(type, sourceFile, file, type.driver() + file.toString());
	}

	/**
	 * Instantiates a new Datasource
	 *
	 * @param type the type of database to connect to (has to be a file based one)
	 * @param file the file to connect to (or create if absent)
	 * @param connectionString the url to connect to the driver with if a custom one should be used
	 */
	public FileDataSource(Database.DatabaseType type, Path sourceFile, Path file,
			String connectionString) {
		this.databaseType = type;
		this.sourceDbFile = sourceFile;
		this.dbFile = file;
		this.connectionString = connectionString;
	}

	/**
	 * Opens a new Connection to the database if non exists currently
	 */
	private void connect() {
		try {
			if (connection != null && !connection.isClosed()) {
				return;
			}
		} catch (SQLException e) {
			log.warning("Connection error " + e.getMessage());
		}

		try {
			Class.forName(databaseType.classLoader());

			Path databaseFile = dbFile;
			if (!Files.exists(databaseFile)) {
				copyDatabaseFile(databaseFile, sourceDbFile);
			}
			connection = DriverManager.getConnection(connectionString);

		} catch (ClassNotFoundException | IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copies the database file from the sourcePath to the destinationPath or creates a new file
	 * if it
	 * does not exist.
	 *
	 * @param databaseFile the file to copy to
	 */
	private void copyDatabaseFile(Path databaseFile, Path sourceFile) throws IOException {
		try (InputStream resourceStream = getResource(sourceFile.toString())) {
			if (resourceStream != null) {
				Files.createDirectories(databaseFile.getParent());
				Files.copy(resourceStream, databaseFile);
			} else {
				Files.createFile(databaseFile);
			}
		}

	}

	@Override
	public Connection getConnection() {
		connect();
		return connection;
	}

	@Override
	public Connection getConnection(String username, String password) {
		return getConnection();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	private InputStream getResource(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Filename cannot be null");
		}

		try {
			URL url = this.getClass().getClassLoader().getResource(filename.replace("\\\\", "/"));

			if (url == null) {
				return null;
			}

			URLConnection urlConnection = url.openConnection();
			urlConnection.setUseCaches(false);
			return urlConnection.getInputStream();
		} catch (IOException ex) {
			return null;
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {

	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}
}
