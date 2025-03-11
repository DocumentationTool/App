package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.api.properties.RepoProperties;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.exception.NotaRepoException;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all repositories (ALWAYS LAZY LOAD)
 */
@Component
@Service
public class RepoService {

    private static final Logger log = LogManager.getLogger(RepoService.class);
    /**
     * A Map of all loaded repositories
     */
    private final Map<RepoId, FileRepository> repositories = new HashMap<>();

    private final RepoProperties properties;

    public RepoService(RepoProperties properties) {
        this.properties = properties;
    }

    public Map<RepoId, FileRepository> getRepositories() {
        return repositories;
    }

    /**
     * Gets a repository by its id
     *
     * @param repoId the id of the repository
     * @return the repository
     * @throws NotaRepoException if the repository does not exist or null is passed
     */
    public FileRepository getRepo(RepoId repoId) throws NotaRepoException {
        if (!isValidRepo(repoId)) {
            log.error("Repo '{}' does not exist", repoId);
            throw new NotaRepoException(repoId, "Repo '%s' does not exist".formatted(repoId));
        }

        return repositories.get(repoId);
    }

    public List<RepoProperty> getProperties() {
        return properties.getRepositories();
    }

    /**
     * Checks if a repository is valid
     *
     * @param repoId the repo id to check
     * @return true if the repo is valid
     */
    public boolean isValidRepo(RepoId repoId) {
        if (repoId == null) {
            return false;
        }
        return repositories.containsKey(repoId);
    }

    public RepoId validateRepoId(String repoId) throws NotaRepoException {
        if (repoId == null) {
            return null;
        }

        RepoId id = new RepoId(repoId);
        if (!repositories.containsKey(id)) {
            throw new NotaRepoException(id, "Repo '%s' does not exist".formatted(repoId));
        }

        return id;

    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing RepoManager");

        for (RepoProperty repoProperty : properties.getRepositories()) {
            log.info("Adding Repo '{}'", repoProperty.getId());
            FileRepository repository = new FileRepository(repoProperty);
            repositories.put(repoProperty.getId(), repository);
            try {
                repository.initialize();
            } catch (GitAPIException e) {
                log.error("Failed to initialize repository '{}'", repoProperty.getId(), e);
            }
        }
    }

	/*
	public CompletableFuture<List<Resource>> searchFor(String searchTerm) {
		var searchFutures = repositories.stream().map(repo -> operations.searchInRepositoryAsync(repo, searchTerm)).toList();
		return CompletableFuture.allOf(searchFutures.toArray(new CompletableFuture[0]))
								.thenApply(v -> searchFutures
										.stream().flatMap(future -> future
												.join().stream()).collect(Collectors.toList()));
	}
	 */

    /**
     * Gets all users from a specified group
     *
     * @param groupId the group to look for
     * @return the users in the group
     */
    public QueryDatabaseResponse<List<UserId>> getUsersFromGroup(RepoId repoId, GroupId groupId) {
        try {
            return QueryDatabaseResponse.success(repoId, getRepo(repoId).getDatabase().getUsersFromGroup(groupId));
        } catch (NotaRepoException e) {
            return QueryDatabaseResponse.fail(null, e);
        }
    }

    public QueryDatabaseResponse<List<GroupId>> getGroupsFromUser(RepoId repoId, UserId userId) {
        try {
            return QueryDatabaseResponse.success(repoId, getRepo(repoId).getDatabase().getGroupsFromUser(userId));
        } catch (NotaRepoException e) {
            return QueryDatabaseResponse.fail(null, e);
        }
    }
}
