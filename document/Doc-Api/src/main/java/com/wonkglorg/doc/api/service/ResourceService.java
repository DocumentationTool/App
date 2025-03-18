package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.db.DbHelper;
import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.ResourceException;
import com.wonkglorg.doc.core.exception.client.*;
import com.wonkglorg.doc.core.objects.*;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.wonkglorg.doc.core.path.TargetPath.normalizePath;

@Component
@Service
public class ResourceService {

    private final RepoService repoService;
    private final UserService userService;

    public ResourceService(@Lazy RepoService repoService, UserService userService) {
        this.repoService = repoService;
        this.userService = userService;
    }

    /**
     * Gets a repository by its id
     *
     * @param request the request
     * @return the repository
     */
    public List<Resource> getResources(ResourceRequest request) throws CoreException, ClientException {
        RepoId id = repoService.validateRepoId(request.repoId.id(), true);
        if (id.isAllRepos()) { //gets resources from all repos
            List<Resource> allResources = new ArrayList<>();
            for (var repo : repoService.getRepositories().values()) {
                allResources.addAll(repo.getDatabase().getResources(request));
            }
            return allResources;
        }

        return repoService.getRepo(id).getDatabase().getResources(request);
    }

    /**
     * Checks if a resource exists
     *
     * @param repoId the repo id
     * @param path   the path
     * @return true if the resource exists
     */
    public boolean resourceExists(RepoId repoId, Path path) throws InvalidRepoException {
        return repoService.getRepo(repoId).getDatabase().resourceExists(path);
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
            return repoService.getRepo(repoId).getDatabase().tagExists(tagId);
        } catch (InvalidRepoException e) {
            return false;
        }
    }

    public TagId validateTagId(RepoId repoId, String tagId) throws InvalidTagException, InvalidRepoException {
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

    public List<Tag> getTags(RepoId repoId, List<TagId> ids) throws InvalidTagException, InvalidRepoException {
        List<Tag> tags = new ArrayList<>();
        FileRepository repo = repoService.getRepo(repoId);
        for (TagId id : ids) {
            Tag tag = repo.getDatabase().getTagCache().get(id);
            if (tag == null) {
                throw new InvalidTagException("Tag '%s' does not exist".formatted(id));
            }
            tags.add(tag);
        }
        return tags;
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
                tags.addAll(repo.getDatabase().getTags());
            }
            return tags;
        }
        return repoService.getRepo(repoId).getDatabase().getTags();
    }

    /**
     * Inserts a resource into the database
     *
     * @param resource the resource
     * @return the response
     */
    public void insertResource(Resource resource) throws ClientException, CoreException {
        RepoId id = repoService.validateRepoId(resource.repoId().id());
        resource.setResourcePath(normalizePath(resource.resourcePath()));
        Path path = resource.resourcePath();
        DbHelper.validatePath(path);
        DbHelper.validateFileType(path);
        if (resourceExists(id, path)) {
            throw new ClientException("The resource '%s' already exists in repository '%s'".formatted(path, id));
        }
        FileRepository repo = repoService.getRepo(id);
        repo.checkTags(resource.getResourceTags());
        repo.getDatabase().insertResource(resource);
        repo.addResourceAndCommit(resource);
    }

    /**
     * Updates a resource in the database
     *
     * @param repoId the repo id
     * @param path   the path
     * @return the response
     */
    public boolean removeResource(RepoId repoId, Path path) throws InvalidResourceException, CoreException, InvalidRepoException,
            InvalidPathException {
        if (!repoService.isValidRepo(repoId)) {
            throw new InvalidRepoException("Repo '%s' does not exist".formatted(repoId));
        }
        path = normalizePath(path);

        DbHelper.validatePath(path);
        DbHelper.validateFileType(path);


        if (!resourceExists(repoId, path)) {
            throw new ResourceException("Resource '%s' does not exist in repository '%s'".formatted(path, repoId));
        }

        if (isBeingEdited(repoId, path)) {
            throw new CoreException("Resource '%s' in '%s' is currently being edited".formatted(path, repoId));
        }

        FileRepository repo = repoService.getRepo(repoId);
        repo.getDatabase().removeResource(path);
        if (Files.exists(repo.getRepoProperties().getPath().resolve(path))) {
            try {
                Files.delete((repo.getRepoProperties().getPath().resolve(path)));
                return false;
            } catch (IOException e) {
                throw new CoreException("Failed to delete resource '%s'".formatted(path), e);
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
    public Resource move(UserId userId, RepoId repoFrom, Path pathFrom, RepoId repoTo, Path pathTo) throws ClientException, CoreException {

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

        if (resourceExists(repoTo, pathTo)) {
            throw new ResourceException("Resource '%s' already exists in target '%s'".formatted(pathTo, repoTo));
        }

        ResourceRequest request = new ResourceRequest();
        request.setPath(pathFrom.toString());
        request.withData = true;
        request.repoId = repoFrom;

        FileRepository fileRepoFrom = repoService.getRepo(repoFrom);
        FileRepository fileRepoTo = repoService.getRepo(repoTo);

        Resource resource = fileRepoFrom.getDatabase().getResources(request).stream().findFirst().orElseThrow();
        fileRepoTo.getDatabase().insertResource(resource);
        fileRepoTo.addResourceAndCommit(resource);
        fileRepoFrom.getDatabase().removeResource(resource.resourcePath());
        fileRepoFrom.removeResourceAndCommit(userId, pathFrom);

        ResourceRequest returnRequest = new ResourceRequest();
        returnRequest.setPath(pathTo.toString());
        returnRequest.withData = true;
        returnRequest.repoId = repoTo;
        return fileRepoTo.getDatabase().getResources(returnRequest).stream().findFirst().orElseThrow();
    }

    /**
     * Updates a resource in the database
     *
     * @param request the request
     * @return the response
     */
    public Resource updateResource(ResourceUpdateRequest request) throws ClientException, CoreSqlException {
        RepoId id = repoService.validateRepoId(request.repoId);
        request.path = normalizePath(request.path);
        Path path = Path.of(request.path);
        DbHelper.validatePath(path);
        DbHelper.validateFileType(path);
        FileRepository repo = repoService.getRepo(id);
        if (!resourceExists(id, path)) {
            throw new InvalidResourceException("Resource '%s' does not exist in repository '%s'".formatted(path, id));
        }

        repo.checkTags(request.tagsToSet.stream().map(TagId::new).collect(Collectors.toSet()));
        repo.checkTags(request.tagsToAdd.stream().map(TagId::new).collect(Collectors.toSet()));
        repo.checkTags(request.tagsToRemove.stream().map(TagId::new).collect(Collectors.toSet()));

        Resource resource = repo.getDatabase().updateResourceData(request);
        repo.addResourceAndCommit(resource);
        return resource;
    }

    /**
     * Check if a file is currently being edited
     *
     * @param repoId the repo repoId
     * @param path   the path to check
     * @return the user editing the file or null if not being edited
     */
    public UserId getEditingUser(RepoId repoId, Path path) throws InvalidResourceException, InvalidRepoException {
        path = normalizePath(path);
        repoService.validateRepoId(repoId);
        validateResource(repoId, path);
        return repoService.getRepo(repoId).getDatabase().isBeingEdited(path);
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
    public boolean isUserEditing(RepoId id, UserId userId) throws InvalidRepoException {
        return repoService.getRepo(id).getDatabase().isUserEditing(userId);
    }

    /**
     * Sets a user as editing a file locking it for others to edit at the same time
     *
     * @param userId the user editing
     * @param path   the path to the file
     */
    public void setCurrentlyEdited(RepoId repoId, UserId userId, Path path)
            throws InvalidResourceException, CoreException, InvalidRepoException, InvalidUserException {
        path = normalizePath(path);
        repoService.validateRepoId(repoId);
        validateResource(repoId, path);
        userService.validateUserId(repoId, userId);
        if (isBeingEdited(repoId, path)) {
            throw new CoreException("Resource '%s' in '%s' is already being edited by '%s'".formatted(path, repoId, userId));
        }
        repoService.getRepo(repoId).getDatabase().setCurrentlyEdited(userId, path);
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
        path = normalizePath(path);
        if (!resourceExists(repoId, path)) {
            throw new InvalidResourceException("Resource '%s' does not exist in repository '%s'".formatted(path, repoId));
        }
    }

    /**
     * Removes a user from editing a file
     *
     * @param userId the user to remove
     */
    public void removeCurrentlyEdited(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException {
        repoService.validateRepoId(repoId);
        userService.validateUserId(repoId, userId);
        repoService.getRepo(repoId).getDatabase().removeCurrentlyEdited(userId);
    }

    /**
     * Removes a file from being edited
     *
     * @param path the path to the file
     */
    public void removeCurrentlyEdited(RepoId id, Path path) throws InvalidResourceException, InvalidRepoException {
        repoService.validateRepoId(id);
        path = normalizePath(path);
        validateResource(id, path);
        repoService.getRepo(id).getDatabase().removeCurrentlyEdited(path);
    }

    /**
     * Creates a new tag in the database
     *
     * @param repoId the repo id
     * @param tag    the tag to create
     */
    public void createTag(RepoId repoId, Tag tag) throws ClientException, CoreSqlException {
        repoService.validateRepoId(repoId);
        if (tagExists(repoId, tag.tagId())) {
            throw new ClientException("Tag '%s' already exists in repository '%s'".formatted(tag.tagId(), repoId));
        }
        FileRepository repo = repoService.getRepo(repoId);
        repo.getDatabase().createTag(tag);
    }

    public void removeTag(RepoId repoId, TagId tagId) throws CoreSqlException, InvalidRepoException, InvalidTagException {
        repoService.validateRepoId(repoId);
        validateTagId(repoId, tagId);
        FileRepository repo = repoService.getRepo(repoId);
        repo.getDatabase().removeTag(tagId);
    }
}
