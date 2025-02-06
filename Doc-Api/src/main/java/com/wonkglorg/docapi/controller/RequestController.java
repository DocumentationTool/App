package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.db.FileDB;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Path;

@Controller
public class RequestController {

    private final FileDB fileDB;

    public RequestController(FileDB fileDB) {
        this.fileDB = fileDB;
    }

    @GetMapping(value = "/user/{id}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String id) {
        return new ResponseEntity<>(fileDB.getUserProfile(id), HttpStatus.OK);
    }

    @GetMapping(value = "/document/{path}")
    public ResponseEntity<Document> getDocument(@PathVariable String path) {
        return new ResponseEntity<>(fileDB.getDocument(Path.of(path)), HttpStatus.OK);
    }
}
