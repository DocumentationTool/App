package com.wonkglorg.docapi.common;


import com.wonkglorg.docapi.user.UserProfile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a document found on the file system
 *
 * @param title the title of the file
 * @param author the author
 * @param creationDate the creation date
 * @param editTime last edited time
 * @param contentLines
 */
public record Document(Path path, String title, UserProfile author, LocalDateTime creationDate,
											 LocalDateTime editTime, UserProfile lastEditor, List<String> contentLines) {


	public Document(Path path, String title, UserProfile author, LocalDateTime creationDate,
			LocalDateTime editTime, UserProfile lastEditor, String content) {
		this(path, title, author, creationDate, editTime, lastEditor,
				Arrays.stream(content.split("\n")).toList());
	}

	@Override
	public String toString() {
		return "Document{" + "path=" + path + ", title='" + title + '\'' + ", author=" + author
				+ ", creationDate=" + creationDate + ", editTime=" + editTime + ", lastEditor=" + lastEditor
				+ ", contentLines=" + contentLines + '}';
	}
}
