package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.user.DefaultProfile;
import com.wonkglorg.docapi.user.UserProfile;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class FileDB {

	private Connection connection;

	public void createDB() {


	}

	public Document getDocument(Path path) {
		return new Document(path, "Test",
				DefaultProfile.createDefault(), LocalDateTime.MIN,
				LocalDateTime.MAX, DefaultProfile.createDefault(),
				"Content");
	}

	public UserProfile getUserProfile(String id) {
		return new DefaultProfile(id, List.of());
	}


}
