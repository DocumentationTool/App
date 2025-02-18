package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.db.daos.DatabaseFunctions;
import com.wonkglorg.docapi.db.daos.ResourceFunctions;
import com.wonkglorg.docapi.db.dbs.JdbiDatabase;
import com.wonkglorg.docapi.db.objects.Resource;
import com.wonkglorg.docapi.git.RepoProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RepoDB extends JdbiDatabase<HikariDataSource> {
    private static final Logger log = LoggerFactory.getLogger(RepoDB.class);
    private final RepoProperties repoProperties;

    public RepoDB(RepoProperties repoProperties, Path openInPath) {
        super(getDataSource(repoProperties, openInPath));
        this.repoProperties = repoProperties;
    }

    public RepoDB(RepoProperties repoProperties) {
        this(repoProperties, repoProperties.getPath().resolve(repoProperties.getDbName()));
    }


    /**
     * Retrieves the data source for the current sql connection
     *
     * @param repoProperties
     * @param openInPath
     * @return
     */
    private static HikariDataSource getDataSource(RepoProperties repoProperties, Path openInPath) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(SQLITE.driver() + openInPath.toString());
        return new HikariDataSource(hikariConfig);
    }
    
    /**
     * Deletes a resource from the database
     * @param path the path of the resource to delete
     * @return true if the resource was deleted, false otherwise
     */
    public boolean deleteResource(Path path) {
        log.info("Deleting resource '{}' from repo '{}'", path, repoProperties.getName());
        try {
            return attach(ResourceFunctions.class, db -> db.delete(path)) > 0;
        } catch (Exception e) {
            log.error("Error while deleting resource '{}' from repo '{}'", path,
                    repoProperties.getName(),
                    e);
        }
        return false;
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
    
    /**
     * Initializes the database for the current repo (creating tables, triggers, etc.)
     */
    @SuppressWarnings("TextBlockBackwardMigration")
    public void initialize() {
        log.info("Initialising Database for repo '{}'", repoProperties.getName());
        try {
            voidAttach(DatabaseFunctions.class, DatabaseFunctions::initialize);
            //todo:jmd fix this issue
            voidAttach(DatabaseFunctions.class,DatabaseFunctions::setupTriggers);
        } catch (Exception e) {
            log.error("Error while initializing Database for repo '{}'", repoProperties.getName(), e);
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
    public void insertResource(Path path, String data) {
        log.info("Inserting resource '{}' into repo '{}'", path, repoProperties.getName());
        try {
            voidAttach(ResourceFunctions.class,
                    f -> f.insert(new Resource(path, "system"), data));
        } catch (Exception e) {
            log.error("Error while inserting resource '{}' from repo '{}'", path,
                    repoProperties.getName(), e);
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
        log.info("Moving resource '{}' to '{}' in repo '{}'", oldPath, newPath,
                repoProperties.getName());
        try {
            Integer i = attach(ResourceFunctions.class, db -> db.updatePath(oldPath, newPath));
            log.info("Updated {} entries when moving resource '{}' in repo '{}'", i, newPath,
                    repoProperties.getName());
            return i > 0;
        } catch (Exception e) {
            log.error("Error while moving resource '{}' in repo '{}'", oldPath, repoProperties.getName(),
                    e);
        }
        return false;
    }

    /**
     * Rebuilds the entire ntfs table to remove any unused records
     */
    public void rebuildFts() {
        log.info("Rebuilding FTS for repo '{}'", repoProperties.getName());

        try {
            voidAttach(DatabaseFunctions.class, DatabaseFunctions::rebuildFts);
            log.info("Finished rebuilding FTS for repo '{}'", repoProperties.getName());
        } catch (Exception e) {
            log.error("Error while rebuilding FTS for repo '{}'", repoProperties.getName(), e);
        }
    }

    public boolean updateResource(Path resourcePath, String newData) {
        log.info("Updating resource '{}' in repo '{}'", resourcePath, repoProperties.getName());
        try {
            return attach(ResourceFunctions.class, db -> db.updateResource(resourcePath, newData)) > 0;
        } catch (Exception e) {
            log.error("Error while updating resource '{}' in repo '{}'", resourcePath,
                    repoProperties.getName(), e);
        }
        return false;
    }


    public void batchInsert(List<Map.Entry<Resource, String>> resources) {
        try (Handle handle = jdbi().open(); Batch batch = handle.createBatch()) {
            for (var resourceEntry : resources) {
                var resource = resourceEntry.getKey();
                var data = resourceEntry.getValue();
                String sql = "INSERT INTO FileData(resourcePath, data) VALUES(%s,%s)".formatted(resource.resourcePath().toString(), data);
                batch.add(sql);
            }
            batch.execute();
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
        try (Handle handle = jdbi().open()) {
            ResourceFunctions resources = handle.attach(ResourceFunctions.class);
            for (Path path : filesToRemove) {
                resources.delete(path);
            }
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
