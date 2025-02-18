package com.wonkglorg.docapi.db.objects;

import java.nio.file.Path;
import java.time.LocalDateTime;

//todo:jmd add repo name to the resource to link it to the correct repo?
public record Resource(Path resourcePath, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, String commitId,
					   String data){
	
	public Resource(Path resourcePath, String creator, String commitId, String data) {
		this(resourcePath, LocalDateTime.now(), creator, LocalDateTime.now(), creator, commitId, data);
	}
	
	public Resource(Path resourcePath, String creator, String commitId) {
		this(resourcePath, LocalDateTime.now(), creator, LocalDateTime.now(), creator, commitId, null);
	}
}