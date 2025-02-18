package com.wonkglorg.docapi;

import com.github.javafaker.Faker;
import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.git.RepoProperties;
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
    private static RepoProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        properties = new RepoProperties();
        properties.setPath(Path.of("temp", "test", "repo"));
        properties.setName("Test Repo");
        properties.setReadOnly(false);
        deleteDirecory(properties.getPath());
        Files.createDirectories(properties.getPath());
    }

    @Test
    void testReadWriteSpeed() {
        try (RepoDB repoDB = new RepoDB(properties)) {
            repoDB.initialize();

            Faker faker = new Faker();

            for (int i = 0; i < 5000; i++) {
                repoDB.insertResource(Path.of("documents/test/doc%s.xml".formatted(i)), faker.lorem().characters(300, 9000));
            }
        }
    }

}
