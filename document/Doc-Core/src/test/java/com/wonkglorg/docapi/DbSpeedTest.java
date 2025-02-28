package com.wonkglorg.docapi;

import com.github.javafaker.Faker;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.objects.RepoId;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.wonkglorg.docapi.TestUtils.deleteDirecory;

class DbSpeedTest {
    private static final Logger log = LoggerFactory.getLogger(DbSpeedTest.class);
    private static RepoProperty properties;

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
    void testReadWriteSpeed() {
        try (RepositoryDatabase repoDB = new RepositoryDatabase(properties)) {
            repoDB.initialize();

            Faker faker = new Faker();
            try (Handle handle = repoDB.jdbi().open(); PreparedBatch preparedBatch = handle.prepareBatch(
                    "insert into FileData (resource_path,data) values (:path, :data)")) {
                for (int i = 0; i < 5000; i++) {
                    preparedBatch.bind("path", "documents/test/doc%s.xml".formatted(i))//
                            .bind("data", faker.lorem().characters(300, 9000)).add();
                }
                preparedBatch.execute();

            }
        }
    }

}
