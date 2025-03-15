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
        properties.setId(new RepoId("Test Repo"));
        properties.setReadOnly(false);
        if (!Files.exists(properties.getPath())) {
            Files.createDirectories(properties.getPath());
        }
    }


    @Test
    void createTestData() throws SQLException {

        /*
        final String creator = "Test-System";
        final int dataCount = 5000;
        final String fileNameTemplate = "documents/test/doc%s.xml";

        List<Tag> testTags = List.of(
                new Tag(TagId.of("it"), "IT"),
                new Tag(TagId.of("controlling"), "Controlling"),
                new Tag(TagId.of("old"), "OLD"));
        try (RepositoryDatabase repoDB = new RepositoryDatabase(properties)) {
            repoDB.initialize();
            try (PreparedStatement statement = repoDB.getConnection().prepareStatement("""
                    INSERT OR IGNORE INTO Resources(resource_path, created_by, last_modified_by, category, commit_id) VALUES(?,?,?,?,?)
                    """)) {
                for (int i = 0; i < dataCount; i++) {
                    statement.setString(1, fileNameTemplate.formatted(i));
                    statement.setString(2, creator);
                    statement.setString(3, creator);
                    if (i > dataCount / 2) {
                        statement.setString(4, "Test Category 1");
                    } else {
                        statement.setString(4, "Test Category 2");
                    }
                    statement.setString(5, "TestCommit");
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            try (PreparedStatement statement = repoDB.getConnection().prepareStatement("""
                    INSERT OR IGNORE INTO Tags (tag_id, tag_name, created_by) VALUES(?,?,?)
                    """)) {
                for (Tag tag : testTags) {
                    statement.setString(1, tag.tagId().id());
                    statement.setString(2, tag.tagName());
                    statement.setString(3, creator);
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            try (PreparedStatement statement = repoDB.getConnection().prepareStatement("""
                    INSERT OR IGNORE INTO ResourceTags(tag_id, resource_path, created_by) VALUES (?,?,?)
                    """)) {
                for (int i = 0; i < dataCount; i++) {
                    statement.setString(1, testTags.get(i % 3).tagId().id());
                    statement.setString(2, fileNameTemplate.formatted(i));
                    statement.setString(3, creator);
                    statement.addBatch();
                }
                statement.executeBatch();

            }


            try (PreparedStatement statement = repoDB.getConnection().prepareStatement("INSERT OR IGNORE INTO FileData (resource_path,data) values (?, ?)")) {
                for (int i = 0; i < dataCount; i++) {
                    statement.setString(1, fileNameTemplate.formatted(i));
                    statement.setString(2, faker.howIMetYourMother().catchPhrase());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }
        
         */
    }
}
