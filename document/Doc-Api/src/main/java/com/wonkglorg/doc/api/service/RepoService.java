package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.api.exception.NotaRepoException;
import com.wonkglorg.doc.api.properties.RepoProperties;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.objects.GroupId;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Permission;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.UpdateDatabaseResponse;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wonkglorg.doc.api.service.CacheConstants.CacheResourceConstant.*;

@Component
@Service
public class RepoService {

    private static final Logger log = LogManager.getLogger(RepoService.class);
    private final RepoAsyncOperations operations;
    /**
     * A Map of all loaded repositories
     */
    private final Map<RepoId, FileRepository> repositories = new HashMap<>();

    private final RepoProperties properties;
    private final RepoAsyncOperations repoAsyncOperations;

    public RepoService(RepoProperties properties, RepoAsyncOperations operations, RepoAsyncOperations repoAsyncOperations) {
        this.properties = properties;
        this.operations = operations;
        this.repoAsyncOperations = repoAsyncOperations;
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
        if (repoId == null) {
            log.error("RepoId cannot be null");
            throw new NotaRepoException("RepoId cannot be null");
        }

        if (!repositories.containsKey(repoId)) {
            log.error("Repo '{}' does not exist", repoId);
            throw new NotaRepoException("Repo '%s' does not exist".formatted(repoId));
        }

        return repositories.get(repoId);
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing RepoManager");

        for (RepoProperty repoProperty : properties.getRepositories()) {
            log.info("Adding Repo '{}'", repoProperty.getId());
            FileRepository repository = new FileRepository(repoProperty);
            repositories.put(repoProperty.getId(), repository);
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
     * Gets all users from a specified group
     *
     * @param groupId the group to look for
     * @return the users in the group
     */
    @Cacheable(value = "groupUsers", key = "{#repoId, #groupId}")
    public QueryDatabaseResponse<List<UserId>> getUsersFromGroup(RepoId repoId, GroupId groupId) {
        try {
            return getRepo(repoId).getDatabase().getUsersFromGroup(groupId);
        } catch (NotaRepoException e) {
            return QueryDatabaseResponse.error(null, e);
        }
    }

    @Cacheable(value = "userGroups", key = "{#repoId, #userId}")
    public QueryDatabaseResponse<List<GroupId>> getGroupsFromUser(RepoId repoId, UserId userId) {
        try {
            return getRepo(repoId).getDatabase().getGroupsFromUser(userId);
        } catch (NotaRepoException e) {
            return QueryDatabaseResponse.error(null, e);
        }
    }

    @Cacheable(value = "userPermissions", key = "{#repoId, #userId}")
    public QueryDatabaseResponse<List<Permission<UserId>>> getUserPermissions(RepoId repoId, UserId userId) {
        //return getRepo(repoId).getDatabase().getUserPermissions(userId);
        return null;
    }

    @Cacheable(value = "groupPermissions", key = "{#repoId, #groupId}")
    public QueryDatabaseResponse<List<Permission<GroupId>>> getGroupPermissions(RepoId repoId, GroupId groupId) {
        //return getRepo(repoId).getDatabase().getGroupPermissions(groupId);
        return null;
    }

    /**
     * Gets all resources from all repositories
     *
     * @return a list of all resources
     */
    @Cacheable(value = ALL_RESOURCES, key = "{#repoId, #userId}")
    public QueryDatabaseResponse<List<Resource>> getResources(RepoId repoId, UserId userId) {
        QueryDatabaseResponse<List<Resource>> resouces;
        if(repoId == null) {
            resouces = getResources(repoId);
        }else{
            resouces = null;//;
        }

        if(resouces.isError()){
            return resouces;
        }

        //todo:jmd add user permissions
        //return resourceFutures.stream().map(CompletableFuture::join).flatMap(List::stream).collect(Collectors.toList());
        return resouces;
    }


    /**
     * Gets all roles from a specified repository
     * @param repoId the repository to get roles from
     * @param userId the user to get roles for
     * @return a list of all roles in the repository
     */
    @Cacheable(value = "roles", key = "{#repoId, #userId}")
    public QueryDatabaseResponse<List<Role>> getRoles(RepoId repoId, UserId userId) {
        //return getRepo(repoId).getDatabase().getRoles(userId);
        return null;
    }

    /**
     * Gets all resources from a specified repository
     *
     * @param repoId the repository to get resources from
     * @return a list of all resources in the repository
     */
    @Cacheable(value = REPO_RESOURCES)
    public QueryDatabaseResponse<List<Resource>> getResources(RepoId repoId) {
        try {
            return getRepo(repoId).getDatabase().getResources();
        } catch (NotaRepoException e) {
            return QueryDatabaseResponse.error(null, e);
        }

    }

    /**
     * Inserts a new resource into a repository
     *
     * @param repoId
     * @return
     */
    public UpdateDatabaseResponse insertResource(RepoId repoId) {
        FileRepository repo = null;
        try {
            repo = getRepo(repoId);
        } catch (NotaRepoException e) {
            throw new RuntimeException(e);
        }

        return null;
        // return repo.getDatabase().insertResource();
    }
}
