package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.db.DbHelper;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.ResourceException;
import com.wonkglorg.doc.core.exception.client.*;
import com.wonkglorg.doc.core.interfaces.ResourceCalls;
import com.wonkglorg.doc.core.objects.*;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.wonkglorg.doc.core.path.TargetPath.normalizePath;

@Component
@Service
public class ResourceService implements ResourceCalls {
    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    private final RepoService repoService;
    private final UserService userService;
    private final PermissionService permissionService;

    public ResourceService(@Lazy RepoService repoService, @Lazy UserService userService, @Lazy PermissionService permissionService) {
        this.repoService = repoService;
        this.userService = userService;
        this.permissionService = permissionService;
    }

    /**
     * Gets a repository by its id
     *
     * @param request the request
     * @return the repository
     */
    public List<Resource> getResources(ResourceRequest request) throws CoreException, ClientException {
        if (request.repoId().isAllRepos()) { //gets resources from all repos
            List<Resource> allResources = new ArrayList<>();
            for (var repo : repoService.getRepositories().values()) {
                request.repoId(repo.getRepoProperties().getId());
                try {
                    List<Resource> resourcesFromRepo = getResourcesFromRepo(repo.getRepoProperties().getId(), request);

                    if (resourcesFromRepo != null && !resourcesFromRepo.isEmpty()) {
                        allResources.addAll(resourcesFromRepo);
                    }

                } catch (InvalidUserException e) {
                    //ignore for cases where a user is not in a specify repo
                }

            }
            return allResources;
        }
        return getResourcesFromRepo(request.repoId(), request);
    }

    /**
     * Method to retrieve resources from a repository  and filter them by user permissions if given
     *
     * @param repoId  the repo id
     * @param request the request
     * @return the resources
     * @throws InvalidRepoException
     * @throws InvalidUserException
     * @throws CoreException
     */
    private List<Resource> getResourcesFromRepo(RepoId repoId, ResourceRequest request)
            throws InvalidRepoException, InvalidUserException, CoreException {
        List<Resource> resources = repoService.getRepo(repoId).getDatabase().resourceFunctions().getResources(request);
        //filter resources by user permissions
        if (!request.userId().isAllUsers()) {
            resources = permissionService.filterResources(request.repoId(), request.userId(), resources);
        }
        return resources;
    }

    /**
     * Checks if a tag exists
     *
     * @param repoId the repo id
     * @param tagId  the tag id
     * @return true if the tag exists
     */
    public boolean tagExists(RepoId repoId, TagId tagId) {
        try {
            return repoService.getRepo(repoId).getDatabase().resourceFunctions().tagExists(repoId, tagId);
        } catch (InvalidRepoException e) {
            return false;
        }
    }

    public TagId validateTagId(RepoId repoId, String tagId) throws InvalidTagException {
        TagId id = new TagId(tagId);
        if (!tagExists(repoId, id)) {
            throw new InvalidTagException("Tag '%s' does not exist".formatted(tagId));
        }
        return id;
    }

    /**
     * Validates a tag id throws an exception if it does not exist
     *
     * @param repoId the repo id
     * @param tagId  the tag id
     * @throws InvalidTagException  if the tag does not exist
     * @throws InvalidRepoException if the repo does not exist
     */
    public void validateTagId(RepoId repoId, TagId tagId) throws InvalidTagException, InvalidRepoException {
        if (!tagExists(repoId, tagId)) {
            throw new InvalidTagException("Tag '%s' does not exist".formatted(tagId));
        }
    }

    /**
     * Gets all tags in a repository or all repositories if specified
     *
     * @param repoId the repo id
     * @return the tags
     */
    public List<Tag> getTags(RepoId repoId) throws InvalidRepoException {
        List<Tag> tags = new ArrayList<>();
        if (repoId.isAllRepos()) {
            for (var repo : repoService.getRepositories().values()) {
                tags.addAll(repo.getDatabase().resourceFunctions().getTags(repoId));
            }
            return tags;
        }
        return repoService.getRepo(repoId).getDatabase().resourceFunctions().getTags(repoId);
    }

    /**
     * Inserts a resource into the database
     *
     * @param resource the resource
     * @return the response
     */
    @Override
    public void insertResource(Resource resource) throws ClientException, CoreException {
        RepoId id = repoService.validateRepoId(resource.repoId().id());
        resource.setResourcePath(normalizePath(resource.resourcePath()));
        Path path = resource.resourcePath();
        DbHelper.validatePath(path);
        DbHelper.validateFileType(path);
        if (resourceExists(id, path)) {
            throw new ClientException("The resource '%s' already exists in repository '%s'".formatted(normalizePath(path.toString()), id));
        }
        FileRepository repo = repoService.getRepo(id);
        repo.checkTags(resource.getResourceTags());
        repo.getDatabase().resourceFunctions().insertResource(resource);
        repo.addResourceAndCommit(resource);
    }

    /**
     * Updates a resource in the database
     *
     * @param repoId the repo id
     * @param path   the path
     * @return the response
     */
    public boolean removeResource(RepoId repoId, Path path) throws ClientException, CoreException {
        if (!repoService.isValidRepo(repoId)) {
            throw new InvalidRepoException("Repo '%s' does not exist".formatted(repoId));
        }

        DbHelper.validatePath(path);
        DbHelper.validateFileType(path);

        if (!resourceExists(repoId, path)) {
            throw new InvalidResourceException("Resource '%s' does not exist in repository '%s'".formatted(normalizePath(path.toString()), repoId));
        }

        if (isBeingEdited(repoId, path)) {
            throw new ClientException("Resource '%s' in '%s' is currently being edited".formatted(normalizePath(path.toString()), repoId));
        }

        FileRepository repo = repoService.getRepo(repoId);
        repo.getDatabase().resourceFunctions().removeResource(repoId, path);
        if (Files.exists(repo.getRepoProperties().getPath().resolve(path))) {
            try {
                Files.delete((repo.getRepoProperties().getPath().resolve(path)));
                return false;
            } catch (IOException e) {
                throw new CoreException("Failed to delete resource '%s'".formatted(normalizePath(path.toString())), e);
            }
        }
        return true;
    }

    /**
     * Updates a resource in the database
     *
     * @param repoFrom the repo from
     * @param pathFrom the path from
     * @param repoTo   the repo to
     *                 * @param pathTo the path to
     * @return the response
     */
    public Resource moveToOtherRepo(UserId userId, RepoId repoFrom, Path pathFrom, RepoId repoTo, Path pathTo) throws ClientException, CoreException, IOException {

        repoService.validateRepoId(repoFrom);
        repoService.validateRepoId(repoTo);
        //validate users in both repos
        userService.validateUserId(repoFrom, userId);
        userService.validateUserId(repoTo, userId);

        pathFrom = normalizePath(pathFrom);
        pathTo = normalizePath(pathTo);

        if (!resourceExists(repoFrom, pathFrom)) {
            throw new ResourceException("Can't move a non existing resource '%s' in '%S'".formatted(pathFrom, repoFrom));
        }

        if (isBeingEdited(repoFrom, pathFrom)) {
            throw new CoreSqlException("Resource '%s' in '%s' is currently being edited and cannot be updated!".formatted(pathFrom, repoFrom));
        }

        if (resourceExists(repoTo, pathTo)) {
            throw new ResourceException("Resource '%s' already exists in target '%s'".formatted(pathTo, repoTo));
        }

        log.info("Moving resource '%s' from '%s' to '%s' repo '%s'".formatted(pathFrom, repoFrom, pathTo, repoTo));

        ResourceRequest request = new ResourceRequest();
        request.setPath(pathFrom.toString());
        request.setWithData(true);
        request.repoId(repoFrom);

        FileRepository fileRepoFrom = repoService.getRepo(repoFrom);
        FileRepository fileRepoTo = repoService.getRepo(repoTo);


        Resource oldResource = fileRepoFrom.getDatabase().resourceFunctions().getResources(request).stream().findFirst().orElseThrow();
        Resource resourceToInsert = new Resource(pathTo, oldResource.createdAt(), oldResource.createdBy(), LocalDateTime.now(), userId.id(), repoTo, oldResource.getResourceTags(), oldResource.category(), oldResource.data());

        fileRepoTo.getDatabase().resourceFunctions().insertResource(resourceToInsert);
        fileRepoTo.addResourceAndCommit(resourceToInsert);
        fileRepoFrom.getDatabase().resourceFunctions().removeResource(repoFrom, normalizePath(pathFrom));
        fileRepoFrom.removeResourceAndCommit(userId, pathFrom);

        ResourceRequest returnRequest = new ResourceRequest();
        returnRequest.setPath(pathTo.toString());
        returnRequest.setWithData(true);
        returnRequest.repoId(repoTo);
        log.info("Resource '%s' in '%s' moved to '%s' in '%s'".formatted(pathFrom, repoFrom, pathTo, repoTo));
        return fileRepoTo.getDatabase().resourceFunctions().getResources(returnRequest).stream().findFirst().orElseThrow();
    }

    /**
     * Updates a resource in the database
     *
     * @param request the request
     * @return the response
     */
    @Override
    public Resource updateResource(ResourceUpdateRequest request) throws ClientException, CoreSqlException {
        RepoId id = request.repoId();
        repoService.validateRepoId(id);
        Path path = request.path();
        DbHelper.validatePath(path);
        DbHelper.validateFileType(path);
        FileRepository repo = repoService.getRepo(id);
        if (!resourceExists(id, path)) {
            throw new InvalidResourceException("Resource '%s' does not exist in repository '%s'".formatted(path, id));
        }

        if (isBeingEdited(id, path)) {
            throw new CoreSqlException("Resource '%s' in '%s' is currently being edited and cannot be updated!".formatted(normalizePath(path.toString()), id));
        }

        repo.checkTags(request.tagsToSet());
        repo.checkTags(request.tagsToAdd());
        repo.checkTags(request.tagsToRemove());

        Resource resource = repo.getDatabase().resourceFunctions().updateResource(request);
        repo.addResourceAndCommit(resource);
        return resource;
    }

    @Override
    public boolean resourceExists(RepoId repoId, Path path) throws InvalidRepoException {
        if (!repoService.isValidRepo(repoId)) {
            throw new InvalidRepoException("Repo '%s' does not exist".formatted(repoId));
        }

        return repoService.getRepo(repoId).getDatabase().resourceFunctions().resourceExists(repoId, path);
    }

    @Override
    public boolean moveResource(RepoId repoId, Path oldPath, Path newPath) throws InvalidRepoException, CoreSqlException, InvalidResourceException {
        if (!resourceExists(repoId, oldPath)) {
            throw new InvalidResourceException("Resource '%s' does not exist in repository '%s'".formatted(oldPath, repoId));
        }

        if (resourceExists(repoId, newPath)) {
            throw new InvalidResourceException("Resource '%s' already exists in repository '%s'".formatted(newPath, repoId));
        }

        if (isBeingEdited(repoId, oldPath)) {
            throw new CoreSqlException("Resource '%s' in '%s' is currently being edited and cannot be moved!".formatted(oldPath, repoId));
        }

        return repoService.getRepo(repoId).getDatabase().resourceFunctions().moveResource(repoId, oldPath, newPath);

    }

    @Override
    public UserId getEditingUser(RepoId repoId, Path path) throws InvalidResourceException, InvalidRepoException {
        repoService.validateRepoId(repoId);
        validateResource(repoId, path);
        return repoService.getRepo(repoId).getDatabase().resourceFunctions().getEditingUser(repoId, path);
    }

    /**
     * Check if a file is currently being edited
     *
     * @param id   the repo id
     * @param path the path to check
     * @return true if it is being edited, false otherwise
     */
    public boolean isBeingEdited(RepoId id, Path path) throws InvalidResourceException, InvalidRepoException {
        return getEditingUser(id, normalizePath(path)) != null;
    }

    /**
     * Check if a user is currently editing a file
     *
     * @param userId the user to check
     * @return true if they are editing, false otherwise
     */
    @Override
    public boolean isUserEditing(RepoId id, UserId userId) throws InvalidRepoException, InvalidUserException {
        if (userId.isAllUsers()) {
            throw new InvalidUserException("User must be a specific user".formatted(userId));
        }
        if (userService.userExists(id, userId)) {
            throw new InvalidUserException("User '%s' does not exist in repository '%s'".formatted(userId, id));
        }

        return repoService.getRepo(id).getDatabase().resourceFunctions().isUserEditing(id, userId);
    }

    @Override
    public void setCurrentlyEdited(RepoId repoId, UserId userId, Path path) throws ClientException {
        repoService.validateRepoId(repoId);
        validateResource(repoId, path);
        userService.validateUserId(repoId, userId);
        if (isBeingEdited(repoId, path)) {
            throw new ClientException("Resource '%s' in '%s' is already being edited by '%s'".formatted(path, repoId, userId));
        }
        repoService.getRepo(repoId).getDatabase().resourceFunctions().setCurrentlyEdited(repoId, userId, path);
    }

    /**
     * Validates a resource
     *
     * @param repoId the repo id
     * @param path   the path
     * @throws InvalidRepoException     if the repo does not exist
     * @throws InvalidResourceException if the resource does not exist
     */
    private void validateResource(RepoId repoId, Path path) throws InvalidRepoException, InvalidResourceException {
        repoService.validateRepoId(repoId);
        if (!resourceExists(repoId, path)) {
            throw new InvalidResourceException("Resource '%s' does not exist in repository '%s'".formatted(path, repoId));
        }
    }

    @Override
    public void removeCurrentlyEdited(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException {
        repoService.validateRepoId(repoId);
        userService.validateUserId(repoId, userId);

        if (!isUserEditing(repoId, userId)) {
            throw new InvalidUserException("User '%s' is not currently editing anything".formatted(userId));
        }
        repoService.getRepo(repoId).getDatabase().resourceFunctions().removeCurrentlyEdited(repoId, userId);
    }

    @Override
    public void removeCurrentlyEdited(RepoId id, Path path) throws InvalidResourceException, InvalidRepoException {
        repoService.validateRepoId(id);
        path = normalizePath(path);
        validateResource(id, path);
        if (!isBeingEdited(id, path)) {
            throw new InvalidResourceException("Resource '%s' in '%s' is not currently being edited".formatted(path, id));
        }
        repoService.getRepo(id).getDatabase().resourceFunctions().removeCurrentlyEdited(id, path);
    }

    @Override
    public void createTag(RepoId repoId, Tag tag) throws ClientException, CoreSqlException {
        repoService.validateRepoId(repoId);
        if (tagExists(repoId, tag.tagId())) {
            throw new ClientException("Tag '%s' already exists in repository '%s'".formatted(tag.tagId(), repoId));
        }
        FileRepository repo = repoService.getRepo(repoId);
        repo.getDatabase().resourceFunctions().createTag(repoId, tag);
    }

    @Override
    public void removeTag(RepoId repoId, TagId tagId) throws CoreSqlException, InvalidRepoException, InvalidTagException {
        repoService.validateRepoId(repoId);
        validateTagId(repoId, tagId);
        FileRepository repo = repoService.getRepo(repoId);
        repo.getDatabase().resourceFunctions().removeTag(repoId, tagId);
    }
}
