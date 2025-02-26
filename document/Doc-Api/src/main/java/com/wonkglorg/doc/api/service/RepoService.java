package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.api.properties.RepoProperties;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.docapi.permissions.Permission;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Component
@Service
public class RepoService{

    private static final Logger log = LogManager.getLogger(RepoService.class);
    private final RepoAsyncOperations operations;
    /**
     * A Map of all loaded repositories
     */
    private final Map<RepoId, FileRepository> repositories = new HashMap<>();


    private final RepoProperties properties;

    public RepoService(RepoProperties properties, RepoAsyncOperations operations) {
        this.properties = properties;
        this.operations = operations;
    }

    public Map<RepoId, FileRepository> getRepositories() {
        return repositories;
    }

    public FileRepository getRepo(RepoId repoId){
        return repositories.get(repoId);
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
     * Gets all users from a specified group
     *
     * @param groupId the group to look for
     * @return the users in the group
     */
    @Cacheable(value = "groupUsers", key = "{#repoId, #groupId}")
    public List<UserId> getUsersFromGroup(RepoId repoId, GroupId groupId) {
        return getRepo(repoId).getDatabase().getUsersFromGroup(groupId);
    }
    
    @Cacheable(value = "userGroups", key = "{#repoId, #userId}")
    public List<GroupId> getGroupsFromUser(RepoId repoId, UserId userId) {
        return getRepo(repoId).getDatabase().getGroupsFromUser(userId);
    }
    
    @Cacheable(value = "userPermissions", key = "{#repoId, #userId}")
    public List<Permission<UserId>> getUserPermissions(RepoId repoId, UserId userId) {
        return getRepo(repoId).getDatabase().getUserPermissions(userId);
    }
    
    
    @Cacheable(value = "groupPermissions", key = "{#repoId, #groupId}")
    public List<Permission<GroupId>> getGroupPermissions(RepoId repoId, GroupId groupId) {
        return getRepo(repoId).getDatabase().getGroupPermissions(groupId);
    }
    
    
    @Cacheable(value = "allResources", key = "{#repoId}")
    public List<Resource> getResources(RepoId repoId) {
        return getRepo(repoId).getDatabase().getResources();
    }
    

}
