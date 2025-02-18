package com.wonkglorg.docapi;

import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.common.Resource;
import com.wonkglorg.docapi.git.RepoProperties;
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
	private static RepoProperties properties;

	@Test
	void addResource() {
		RepoDB repoDB = new RepoDB(properties, properties.getPath().resolve(properties.getDbName()));

		repoDB.initialize();
		repoDB.insertResource(Path.of("test.xml"));
		repoDB.insertResource(Path.of("folder/test.xml"));
		List<Resource> resources = repoDB.getResources();

		Assertions.assertEquals(2, resources.size());
		repoDB.close();
	}

	@Test
	void canCreateDatabase() {
		RepoDB repoDB = new RepoDB(properties, properties.getPath().resolve(properties.getDbName()));
		repoDB.close();
	}

	@AfterAll
	public static void exit() throws IOException, InterruptedException {
		Thread.sleep(500);
		deleteDirecory(properties.getPath());
	}

	@BeforeEach
	void setUp() throws IOException {
		properties = new RepoProperties();
		properties.setPath(Path.of("temp", "test", "repo"));
		properties.setName("Test Repo");
		properties.setReadOnly(false);
		deleteDirecory(properties.getPath());
		Files.createDirectories(properties.getPath());
	}
}
