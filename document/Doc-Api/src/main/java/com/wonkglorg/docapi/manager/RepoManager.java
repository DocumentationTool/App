package com.wonkglorg.docapi.manager;

import com.wonkglorg.docapi.common.Resource;
import com.wonkglorg.docapi.git.RepoProperties;
import com.wonkglorg.docapi.response.Response;
import com.wonkglorg.docapi.response.UserResponse;
import com.wonkglorg.docapi.user.DefaultProfile;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.wonkglorg.docapi.DocApiApplication.DEV_USER;

@Component
public class RepoManager {

    private static final Logger log = LogManager.getLogger(RepoManager.class);
    private final RepoAsyncOperations operations;
    /**
     * A list of all loaded repositories
     */
    private final Map<String, FileRepository> repositories = new HashMap<>();

    /**
     * Keeps track of all cached resources for quick access in each repo
     */
    private final Map<String, List<Resource>> cachedResources = new HashMap<>();
    //todo:jmd also cache all permissions, roles etc across all repos, how to best handle that? Should I combine them and treat them as 1 space or have seperate perms per repo?

    private final com.wonkglorg.docapi.properties.RepoProperties properties;

    public RepoManager(com.wonkglorg.docapi.properties.RepoProperties properties, RepoAsyncOperations operations) {
        this.properties = properties;
        this.operations = operations;
    }

    public Map<String, FileRepository> getRepositories() {
        return repositories;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing RepoManager");

        for (RepoProperties repoProperty : properties.getRepositories()) {
            log.info("Adding Repo '{}'", repoProperty.getName());
            FileRepository repository = new FileRepository(repoProperty);
            repositories.put(repoProperty.getName(), repository);
            operations.initializeRepositoryAsync(repository);
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
     * Gets all resources from all repositories
     *
     * @return a list of all resources
     */
    public List<Resource> getResources() {
        var resourceFutures = repositories.values().stream().map(operations::getResourcesFromRepositoryAsync).toList();


        return resourceFutures.stream().map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }


    /**
     * adds a new user to an existing repo
     *
     * @param repoName the repo to add the user to
     * @param id       the id of the user
     * @param password the password of the user (unhashed)
     * @return true if the user was added, false otherwise
     */
    public UserResponse addUser(String repoName, String id, String password) {
        //todo:jmd implement
        return new UserResponse(DEV_USER,"Worked yes", null);
    }


}
