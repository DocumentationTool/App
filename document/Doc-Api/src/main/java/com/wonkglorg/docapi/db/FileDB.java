package com.wonkglorg.docapi.db;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.user.DefaultProfile;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//this is just the component reference, the actual db will be in core? might be stupid gotta test that
@Component
public class FileDB {

    private Connection connection;

    public void createDB(){

    }

    public Document getDocument(Path path) {
        return new Document(path, "Test", DefaultProfile.createDefault(), LocalDateTime.MIN, LocalDateTime.MAX, DefaultProfile.createDefault(), "Content");
    }

    public Optional<UserProfile> getUserProfile(String id) {
        return Optional.ofNullable(new DefaultProfile(id, List.of()));
    }



}
