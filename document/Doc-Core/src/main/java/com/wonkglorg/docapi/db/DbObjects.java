package com.wonkglorg.docapi.db;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class DbObjects {

	public record Resource(Path resourcePath, LocalDateTime createdAt, String createdBy,
												 LocalDateTime modifiedAt, String modifiedBy) {
	}

}
