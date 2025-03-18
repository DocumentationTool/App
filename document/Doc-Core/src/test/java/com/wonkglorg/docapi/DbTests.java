package com.wonkglorg.docapi;

import com.github.javafaker.Faker;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

class DbTests {
    private static RepoProperty properties;
    private final Faker faker = new Faker();

    @Test
    void canCreateDatabase() {
        RepositoryDatabase repoDB = new RepositoryDatabase(properties, properties.getPath().resolve(properties.getDbName()));
        repoDB.close();
    }

    /*
    @AfterAll
    public static void exit() throws IOException, InterruptedException {
        Thread.sleep(1500);
        deleteDirecory(properties.getPath());
    }

     */

    @BeforeAll
    static void setUp() throws IOException {
        properties = new RepoProperty();
        properties.setPath(Path.of("..", "..", "temp", "git", "repo1"));
        properties.setId(RepoId.of("Test Repo"));
        properties.setReadOnly(false);
        if (!Files.exists(properties.getPath())) {
            Files.createDirectories(properties.getPath());
        }
    }
}
