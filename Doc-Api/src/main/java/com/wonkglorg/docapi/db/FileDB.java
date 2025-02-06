package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.user.DefaultProfile;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class FileDB {

    public Document getDocument(Path path) {
        return new Document(path, "Test", "TestAuthor", LocalDateTime.MIN, LocalDateTime.MAX, "Content");
    }

    public UserProfile getUserProfile(String id) {
        return new DefaultProfile(id, List.of());
    }

}
