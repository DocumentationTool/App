package com.wonkglorg.docapi;

import com.github.javafaker.Faker;
import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.git.RepoProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	void testReadWriteSpeed() throws SQLException {
		RepoDB repoDB = new RepoDB(properties, properties.getPath().resolve(properties.getDbName()));

		//tokenize splits up text by tokens to search for

		try (var statement = repoDB.getConnection().prepareStatement(
				"CREATE VIRTUAL TABLE FileData USING fts5(resourcePath, data, tokenize='trigram');")) { //store the actual data in it too (used for quick lookups, otherwise it takes alot for full text searches)
			statement.execute();
		} catch (Exception e) {
			log.error("Error creating virtual table", e);
		}

		Faker faker = new Faker();


		try (var statement = repoDB.getConnection()
				.prepareStatement("INSERT INTO renderedPages(resourcePath, data) VALUES(?,?)")) {
			repoDB.getConnection().setAutoCommit(false);

			System.out.println("Started adding");
			for (int i = 0; i < 5000; i++) {
				statement.setString(1, "documents/test/doc%s.xml".formatted(i));
				statement.setString(2, faker.lorem().characters(300, 9000));
				statement.addBatch();
			}
			System.out.println("Finished adding");
			statement.executeBatch();
			System.out.println("Executed batch");
			repoDB.getConnection().commit();
			System.out.println("Commited");
		} catch (SQLException e) {
			repoDB.getConnection().rollback();
			System.out.println("Rollback" + e);
			Assertions.fail(e);
		} finally {
			System.out.println("Auto committing");
			repoDB.getConnection().setAutoCommit(true);
		}


		try (var statement = repoDB.getConnection()
				.prepareStatement("SELECT COUNT(*) FROM renderedPages")) {
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				System.out.println("Found %s page".formatted(resultSet.getInt(1)));
			} else {
				System.out.println("No Data");
			}
		} catch (Exception e) {
			log.error("Error creating virtual table", e);
		}

		repoDB.close();
	}

}
