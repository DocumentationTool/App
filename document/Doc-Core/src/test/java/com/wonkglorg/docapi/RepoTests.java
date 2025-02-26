package com.wonkglorg.docapi;

import com.wonkglorg.docapi.db.RepositoryDatabase;
import com.wonkglorg.docapi.git.GitRepo;
import com.wonkglorg.docapi.git.RepoProperties;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static com.wonkglorg.docapi.TestUtils.deleteDirecory;

class RepoTests {

	private static RepoProperties properties;

	@AfterAll
	public static void exit() throws IOException, InterruptedException {
		Thread.sleep(500);
		deleteDirecory(properties.getPath());
	}

	@Test
	void canCreateDatabaseInRepo() {
		try {
			GitRepo gitRepo = new GitRepo(properties);
			var file = gitRepo.getSingleFile(s -> s.equalsIgnoreCase(properties.getDbName()), UNTRACKED,
					MODIFIED, ADDED);

			Assertions.assertTrue(file.isEmpty());
			RepositoryDatabase repoDB = new RepositoryDatabase(properties,
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
