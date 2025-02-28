package com.wonkglorg.docapi;

import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.db.RepositoryDatabase;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.wonkglorg.docapi.TestUtils.deleteDirecory;

class DbTests {
    private static RepoProperty properties;

    @Test
    void insertResource() {
        RepositoryDatabase repoDB = new RepositoryDatabase(properties, properties.getPath().resolve(properties.getDbName()));

        repoDB.initialize();
        Resource resource1 = new Resource(Path.of("test.xml"), "System", "TestCommit", "insertedData");
        Resource resource2 = new Resource(Path.of("test2.xml"), "System", "TestCommit", "insertedData2");

        repoDB.insertResource(resource1);
        repoDB.insertResource(resource2);
        List<Resource> resources = repoDB.getResources();

        Assertions.assertEquals(2, resources.size());
        System.out.println(resources);
        repoDB.close();
    }

    @Test
    void canCreateDatabase() {
        RepositoryDatabase repoDB = new RepositoryDatabase(properties, properties.getPath().resolve(properties.getDbName()));
        repoDB.close();
    }

    @AfterAll
    public static void exit() throws IOException, InterruptedException {
        Thread.sleep(1500);
        deleteDirecory(properties.getPath());
    }

    @BeforeEach
    void setUp() throws IOException {
        properties = new RepoProperty();
        properties.setPath(Path.of("temp", "test", "repo"));
        properties.setId(new RepoId("Test Repo"));
        properties.setReadOnly(false);
        deleteDirecory(properties.getPath());
        Files.createDirectories(properties.getPath());
    }
}
