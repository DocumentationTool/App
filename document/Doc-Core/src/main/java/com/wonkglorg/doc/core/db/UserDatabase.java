package com.wonkglorg.doc.core.db;

import com.wonkglorg.doc.core.db.dbs.SqliteDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;

/**
 * Database containing the users information
 */
public class UserDatabase extends SqliteDatabase<HikariDataSource> {

    //todo:jmd properly implement user database

    public UserDatabase(Path path) {
        super(getDataSource(path));
    }


    /**
     * Retrieves the data source for the current sql connection
     *
     * @param openInPath the path to open the data source in
     * @return the created data source
     */
    private static HikariDataSource getDataSource(Path openInPath) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setLeakDetectionThreshold(1000);
        hikariConfig.setJdbcUrl(SQLITE.driver() + openInPath.toString());
        return new HikariDataSource(hikariConfig);
    }


}
