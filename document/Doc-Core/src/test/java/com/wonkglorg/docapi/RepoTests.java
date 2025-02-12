package com.wonkglorg.docapi;

import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.git.GitRepo;
import com.wonkglorg.docapi.git.RepoProperties;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.wonkglorg.docapi.git.GitRepo.GitStage.*;

class RepoTests {

	private RepoProperties properties;

	@Test
	void canCreateDatabaseInRepo() {
		try {
			GitRepo gitRepo = new GitRepo(properties);
			var file = gitRepo.getSingleFile(s -> s.equalsIgnoreCase(properties.getDbName()), UNTRACKED,
					MODIFIED, ADDED);

			Assertions.assertTrue(file.isEmpty());
			RepoDB repoDB = new RepoDB(properties,
					gitRepo.getRepoPath().resolve(properties.getDbName()));
			repoDB.close();

			var existingFile =
					gitRepo.getSingleFile(s -> s.equalsIgnoreCase(properties.getDbName()), UNTRACKED,
							MODIFIED, ADDED);

			Assertions.assertTrue(existingFile.isPresent());

		} catch (GitAPIException e) {
			Assertions.fail(e);
		}
	}

	@Test
	void canCreateRepo() {
		try {
			new GitRepo(properties);
		} catch (GitAPIException e) {
			Assertions.fail(e);
		}
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

	@Test
	void refuseCreationOfReadonlyRepo() {
		try {
			properties.setReadOnly(true);
			new GitRepo(properties);
		} catch (GitAPIException e) {
			return;
		}
		Assertions.fail("Readonly Repository creation did not fail as expected");
	}

	@BeforeEach
	public void setUp() throws IOException {
		properties = new RepoProperties();
		properties.setPath(Path.of("temp", "test", "repo"));
		properties.setName("Test Repo");
		properties.setReadOnly(false);
		deleteDirecory(properties.getPath());
	}

}
