package com.wonkglorg.doc.core.interfaces;

import com.wonkglorg.doc.core.exception.CoreException;
import com.wonkglorg.doc.core.exception.CoreSqlException;
import com.wonkglorg.doc.core.exception.ResourceException;
import com.wonkglorg.doc.core.exception.client.ClientException;
import com.wonkglorg.doc.core.exception.client.InvalidPathException;
import com.wonkglorg.doc.core.exception.client.InvalidRepoException;
import com.wonkglorg.doc.core.exception.client.InvalidResourceException;
import com.wonkglorg.doc.core.exception.client.InvalidTagException;
import com.wonkglorg.doc.core.exception.client.InvalidUserException;
import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.Tag;
import com.wonkglorg.doc.core.objects.TagId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.request.ResourceRequest;
import com.wonkglorg.doc.core.request.ResourceUpdateRequest;

import java.nio.file.Path;
import java.util.List;

public interface ResourceCalls{
	//------------------- Resources -------------------
	
	/**
	 * Get a list of resources based on the request
	 *
	 * @param request The request object
	 * @return A list of resources
	 * @throws CoreException
	 * @throws ClientException
	 */
	List<Resource> getResources(ResourceRequest request) throws CoreException, ClientException;
	
	/**
	 * Get a resource based on the repoId and path
	 *
	 * @param resource The resource object
	 * @throws ClientException
	 * @throws CoreException
	 */
	void insertResource(Resource resource) throws ClientException, CoreException;
	
	/**
	 * Get a resource based on the repoId and path
	 *
	 * @param repoId The repoId object
	 * @param path The path object
	 * @return The resource object
	 */
	boolean removeResource(RepoId repoId, Path path) throws CoreException, ClientException;
	
	/**
	 * Get a resource based on the repoId and path
	 *
	 * @param request The request object
	 * @return The resource object
	 * @throws ClientException
	 * @throws CoreSqlException
	 */
	Resource updateResource(ResourceUpdateRequest request) throws ClientException, CoreSqlException;
	
	/**
	 * Checks if a resource exists in the database
	 *
	 * @param repoId the repoId of the resource
	 * @param path the path to the resource
	 * @return true if the resource exists false otherwise
	 */
	boolean resourceExists(RepoId repoId, Path path) throws InvalidRepoException;
	
	/**
	 * Move a resource from one path to another
	 * @param repoId The repoId object
	 * @param oldPath The old path object
	 * @param newPath The new path object
	 * @return true if the resource was moved false otherwise
	 * @throws InvalidRepoException if the repoId is invalid
	 */
	boolean moveResource(RepoId repoId, Path oldPath, Path newPath) throws InvalidRepoException, CoreSqlException, InvalidResourceException;
	
	//------------------- Tags -------------------
	
	/**
	 * Get a list of tags based on the repoId
	 *
	 * @param repoId The repoId object
	 * @return A list of tags
	 * @throws InvalidRepoException
	 */
	List<Tag> getTags(RepoId repoId) throws InvalidRepoException;
	
	/**
	 * Create a tag based on the repoId and tag
	 *
	 * @param repoId The repoId object
	 * @param tag The tag object
	 * @throws ClientException
	 * @throws CoreSqlException
	 */
	void createTag(RepoId repoId, Tag tag) throws ClientException, CoreSqlException;
	
	/**
	 * Remove a tag based on the repoId and tagId
	 *
	 * @param repoId The repoId object
	 * @param tagId The tagId object
	 * @throws CoreSqlException
	 * @throws InvalidRepoException
	 * @throws InvalidTagException
	 */
	void removeTag(RepoId repoId, TagId tagId) throws CoreSqlException, InvalidRepoException, InvalidTagException;
	
	/**
	 * Check if a tag exists based on the repoId and tagId
	 *
	 * @param repoId The repoId object
	 * @param tagId The tagId object
	 * @return A boolean
	 */
	boolean tagExists(RepoId repoId, TagId tagId);
	
	//------------------- Editing -------------------
	
	/**
	 * Get the user editing a resource
	 *
	 * @param repoId The repoId object
	 * @param path The path object
	 * @return The userId object or null
	 * @throws InvalidResourceException
	 * @throws InvalidRepoException
	 */
	UserId getEditingUser(RepoId repoId, Path path) throws InvalidResourceException, InvalidRepoException;
	
	/**
	 * Check if a user is editing a resource
	 *
	 * @param id The repoId object
	 * @param userId The userId object
	 * @return A boolean
	 * @throws InvalidRepoException
	 */
	boolean isUserEditing(RepoId id, UserId userId) throws InvalidRepoException, InvalidUserException;
	
	/**
	 * Sets a user as editing a file locking it for others to edit at the same time
	 *
	 * @param userId the user editing
	 * @param path the path to the file
	 */
	void setCurrentlyEdited(RepoId repoId, UserId userId, Path path) throws ClientException;
	
	/**
	 * Get the resource being edited by a user
	 *
	 * @param repoId The repoId object
	 * @param userId The userId object
	 * @throws InvalidRepoException
	 * @throws InvalidUserException
	 */
	void removeCurrentlyEdited(RepoId repoId, UserId userId) throws InvalidRepoException, InvalidUserException;
	
	/**
	 * Get the resource being edited by a user
	 *
	 * @param id The repoId object
	 * @param path The path object
	 * @throws InvalidResourceException
	 * @throws InvalidRepoException
	 */
	void removeCurrentlyEdited(RepoId id, Path path) throws InvalidResourceException, InvalidRepoException;
}
