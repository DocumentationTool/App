package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.git.GitRepo;
import com.wonkglorg.docapi.properties.ApiProperties;
import com.wonkglorg.docapi.user.DefaultProfile;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.wonkglorg.docapi.git.GitRepo.GitStage.ADDED;

//this is just the component reference, the actual db will be in core? might be stupid gotta test
// that
@Component
public class FileRepository {
	private static final Logger log = LogManager.getLogger(FileRepository.class);
	private final ApiProperties properties;
	/**
	 * The backing repo
	 */
	private GitRepo gitRepo;
	/**
	 * Represents the data in the database for quicker access and
	 */
	private DataDB dataDB;

	public FileRepository(ApiProperties properties) {
		this.properties = properties;
	}

	public Document getDocument(Path path) {
		return new Document(path, "Test", DefaultProfile.createDefault(), LocalDateTime.MIN,
				LocalDateTime.MAX, DefaultProfile.createDefault(), "Content");
	}

	@PostConstruct
	public void initialize() throws GitAPIException, IOException {
		gitRepo = new GitRepo(properties.getRepo());
		Optional<Path> file = gitRepo.getSingleFile(s -> s.equalsIgnoreCase(properties.getDbName()),
				GitRepo.GitStage.UNTRACKED, ADDED, ADDED);

		dataDB = new DataDB(gitRepo.getRepoPath().resolve(properties.getDbName()));
		if (file.isPresent()) {
			//commit the file.
			log.info("No Database in Repo");
			gitRepo.getGit().add().addFilepattern(properties.getDbName()).call();
		} else {
			log.info("Found database in repo!");
			//build db and then commit changes?
		}
	}

}
