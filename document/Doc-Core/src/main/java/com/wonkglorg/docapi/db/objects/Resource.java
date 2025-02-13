
package com.wonkglorg.docapi.db.objects;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record Resource(Path resourcePath, LocalDateTime createdAt, String createdBy,
											 LocalDateTime modifiedAt, String modifiedBy) {
	}