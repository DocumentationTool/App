package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.db.daos.DatabaseFunctions;
import com.wonkglorg.docapi.db.daos.ResourceFunctions;
import com.wonkglorg.docapi.db.dbs.JdbiDatabase;
import com.wonkglorg.docapi.db.objects.Resource;
import com.wonkglorg.docapi.git.RepoProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RepoDB extends JdbiDatabase<HikariDataSource> {
	private static final Logger log = LoggerFactory.getLogger(RepoDB.class);
	private final RepoProperties repoProperties;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public RepoDB(RepoProperties repoProperties, Path openInPath) {
		super(getDataSource(repoProperties, openInPath));
		this.repoProperties = repoProperties;
		dataSource = new HikariDataSource();
	}

	private static HikariDataSource getDataSource(RepoProperties repoProperties, Path openInPath) {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(SQLITE.driver() + openInPath.toString());
		return new HikariDataSource(hikariConfig);
	}

	public boolean deleteResource(Path path) {
		log.info("Deleting resource '{}' from repo '{}'", path, repoProperties.getName());


		try (var connection = getConnection(); var resourceStatement = connection.prepareStatement(
				"DELETE FROM Resources WHERE resourcePath = ?");
				var fileDataStatement = connection.prepareStatement(
						"DELETE FROM FileData WHERE resourcePath = ?")) {

			connection.setAutoCommit(false); // Start transaction

			resourceStatement.setString(1, path.toString());
			fileDataStatement.setString(1, path.toString());

			boolean resourceDeleted = resourceStatement.executeUpdate() == 1;
			boolean fileDataDeleted = fileDataStatement.executeUpdate() >= 0;

			connection.commit();
			return resourceDeleted;
		} catch (SQLException e) {
			log.error("Error while deleting resource '{}' from repo '{}'", path,
					repoProperties.getName(),
					e);
			return false;
		}
	}

	/**
	 * Retrieve all saved resources from the database
	 *
	 * @return all resources or an empty set
	 */
	public List<Resource> getResources() {
		try (Handle handle = jdbi().open()) {
			ResourceFunctions attach = handle.attach(ResourceFunctions.class);
			return attach.findAll();
		}
	}

	@SuppressWarnings("TextBlockBackwardMigration")
	public void initialize() {
		log.info("Initialising Database for repo '{}'", repoProperties.getName());
		try (Handle handle = jdbi().open()) {
			handle.attach(DatabaseFunctions.class).initialize();
		}
		log.info("Database initialized for repo '{}'", repoProperties.getName());
	}

	public boolean insertResource(Path path) {
		try (Handle handle = jdbi().open()) {
			return handle.attach(ResourceFunctions.class).insert(new Resource(path, "system")) == 1;
		}
	}

	/**
	 * Inserts a new resource into the database
	 *
	 * @param path the path the resource is located at
	 * @return true if it was inserted false otherwise
	 */
	public boolean insertResource(Path path, String data) {
		log.info("Inserting resource '{}' into repo '{}'", path, repoProperties.getName());
		try {
			return attach(ResourceFunctions.class,
					f -> f.insert(new Resource(path, "system"), data) == 2);
		} catch (Exception e) {
			log.error("Error while inserting resource '{}' from repo '{}'", path,
					repoProperties.getName(), e);
			return false;
		}
	}

	/**
	 * Moves a resource to another location (also includes renaming)
	 *
	 * @param oldPath the initial resource location
	 * @param newPath the new resource location
	 * @return
	 */
	public boolean moveResource(Path oldPath, Path newPath) {
		log.info("Updating resource '{}' to '{}' in repo '{}'", oldPath, newPath,
				repoProperties.getName());
		try (var deleteStatement = connection.prepareStatement(
				"DELETE FROM FileData WHERE resourcePath = ?");
				var insertStatement = connection.prepareStatement(
						"INSERT INTO FileData(resourcePath, data) VALUES(?, ?)")) {

			connection.setAutoCommit(false); // Start transaction

			// Delete old entry
			deleteStatement.setString(1, oldPath.toString());
			deleteStatement.executeUpdate();

			// Insert new entry
			insertStatement.setString(1, newPath.toString());
			insertStatement.setString(2, newData);
			boolean inserted = insertStatement.executeUpdate() == 1;

			connection.commit();
			return inserted;
		} catch (SQLException e) {
			log.error("Error while updating resource '{}' in repo '{}'", oldPath,
					repoProperties.getName(), e);
			return false;
		}
	}

	/**
	 * Rebuilds the entire ntfs table to remove any unused records
	 */
	public void rebuildFts() {
		log.info("Rebuilding FTS for repo '{}'", repoProperties.getName());
		try (Handle handle = jdbi.open()) {
			handle.attach(DatabaseFunctions.class).rebuildFts();
			log.info("Finished rebuilding FTS for repo '{}'", repoProperties.getName());
		} catch (Exception e) {
			log.error("Error while rebuilding FTS for repo '{}'", repoProperties.getName(), e);
		}
	}

	public boolean updateResource(Path oldPath, Path newPath, String newData) {
		log.info("Updating resource '{}' to '{}' in repo '{}'", oldPath, newPath,
				repoProperties.getName());


		try (var connection = getConnection(); var deleteStatement = connection.prepareStatement(
				"DELETE FROM FileData WHERE resourcePath = ?");
				var insertStatement = connection.prepareStatement(
						"INSERT INTO FileData(resourcePath, data) VALUES(?, ?)")) {

			connection.setAutoCommit(false); // Start transaction

			// Delete old entry
			deleteStatement.setString(1, oldPath.toString());
			deleteStatement.executeUpdate();

			// Insert new entry
			insertStatement.setString(1, newPath.toString());
			insertStatement.setString(2, newData);
			boolean inserted = insertStatement.executeUpdate() == 1;

			connection.commit();
			return inserted;
		} catch (SQLException e) {
			log.error("Error while updating resource '{}' in repo '{}'", oldPath,
					repoProperties.getName(), e);
			return false;
		}
	}

	public boolean updateResources(Set<Path> files) {
		//todo:jmd compare last changes + other info to determin if changes happened, if so rebuild
		// that particular entry and redo.
		boolean filesChanged = false;

		log.info("Updating resources for '{}'.", repoProperties.getName());
		Set<Path> existingFiles =
				getResources().stream().map(Resource::resourcePath).collect(Collectors.toSet());
		Set<Path> modifiedFiles = new HashSet<>(existingFiles);
		modifiedFiles.forEach(path -> {
		});

		Set<Path> filesToAdd = new HashSet<>(files);
		filesToAdd.removeAll(existingFiles);

		Set<Path> filesToRemove = new HashSet<>(existingFiles);
		filesToRemove.removeAll(files);


		if (!filesToRemove.isEmpty() || !filesToAdd.isEmpty()) {
			filesChanged = true;
		}

		for (Path path : filesToRemove) {
			removeResource(path);
		}

		for (Path path : filesToAdd) {
			insertResource(path);
		}
		log.info("Finished updating resources for '{}'.", repoProperties.getName());
		log.info("Added: {}", filesToAdd.size());
		log.info("Modified: {}", filesChanged);
		log.info("Deleted: {}", filesToRemove.size());
		return filesChanged;
	}


}
