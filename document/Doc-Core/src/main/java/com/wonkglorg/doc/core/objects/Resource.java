package com.wonkglorg.doc.core.objects;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a resource in the database
 *
 * @param resourcePath the path to the resource
 * @param createdAt the time the resource was created
 * @param createdBy the user who created the resource
 * @param modifiedAt the time the resource was last modified
 * @param modifiedBy the user who last modified the resource
 * @param commitId the commit id of the resource
 * @param data the data of the resource (null unless explicitly requested)
 */
public record Resource(Path resourcePath, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, RepoId repoId,
					   String commitId, boolean isEditable, String data){
	
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public Resource {
		Objects.requireNonNull(resourcePath, "A Resources, path cannot be null");
	}
	
	public Resource(Path resourcePath, String creator, RepoId repoId, String commitId, boolean isEditable, String data) {
		this(resourcePath, LocalDateTime.now(), creator, LocalDateTime.now(), creator, repoId, commitId, isEditable, data);
	}
	
	public static LocalDateTime parseDateTime(String dateTime) {
		return LocalDateTime.parse(dateTime, formatter);
	}
	
	public static String fromDateTime(LocalDateTime dateTime) {
		return dateTime.format(formatter);
	}
	
}