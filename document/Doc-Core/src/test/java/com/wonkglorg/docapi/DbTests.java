package com.wonkglorg.docapi;

import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.git.RepoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

class DbTests {
	private RepoProperties properties;

	@Test
	void canCreateDatabase() {
		RepoDB repoDB = new RepoDB(properties, properties.getPath().resolve(properties.getDbName()));
	}

	private void deleteDirecory(Path path) throws IOException {
		if (!Files.isDirectory(path)) {
			return;
		}
		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@BeforeEach
	void setUp() throws IOException {
		properties = new RepoProperties();
		properties.setPath(Path.of("temp", "test", "repo"));
		properties.setName("Test Repo");
		properties.setReadOnly(false);
		deleteDirecory(properties.getPath());
		Files.createDirectory(properties.getPath());
	}

}
