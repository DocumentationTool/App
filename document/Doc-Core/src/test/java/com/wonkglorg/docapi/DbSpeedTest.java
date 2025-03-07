package com.wonkglorg.docapi;

import com.github.javafaker.Faker;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.wonkglorg.docapi.TestUtils.deleteDirecory;

class DbSpeedTest {
    private static final Logger log = LoggerFactory.getLogger(DbSpeedTest.class);
    private static RepoProperty properties;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() throws IOException {
        properties = new RepoProperty();
        properties.setPath(Path.of("temp", "test", "repo"));
        properties.setId(RepoId.of("Test Repo"));
        properties.setReadOnly(false);
        deleteDirecory(properties.getPath());
        Files.createDirectories(properties.getPath());
    }

    @Test
    void testReadWriteSpeed() throws SQLException {
        try (RepositoryDatabase repoDB = new RepositoryDatabase(properties)) {
            repoDB.initialize();
            try (PreparedStatement statement = repoDB.getConnection().prepareStatement("INSERT INTO FileData (resource_path,data) values (?, ?)")) {
                for (int i = 0; i < 5000; i++) {
                    statement.setString(1, "documents/test/doc%s.xml".formatted(i));
                    statement.setString(2, faker.yoda().quote());
                }
            }
        }
    }

    @Test
    void testWriteSpeed() {
        log.info("Starting testWriteSpeed");
        long start = System.currentTimeMillis();
        try (RepositoryDatabase repoDB = new RepositoryDatabase(properties)) {
            repoDB.initialize();
            for (int i = 0; i < 5000; i++) {
                repoDB.insertResource(createResource(i));
            }
        }
        log.info("testWriteSpeed took {}ms", System.currentTimeMillis() - start);
    }


    private Resource createResource(int index) {
        return new Resource(Path.of("documents/test/doc%s.xml".formatted(index)), "System", new RepoId("t"), "testCommit", true, faker.lorem().characters(500, 5000));
    }
}
