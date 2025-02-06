package com.wonkglorg.docapi.controller;

import com.wonkglorg.docapi.common.Document;
import com.wonkglorg.docapi.db.FileDB;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RequestController {


    private final FileDB fileDB;

    public RequestController(FileDB fileDB) {
        this.fileDB = fileDB;
    }

    @GetMapping(value = "/user/{id}")
    public String getUserProfile(@PathVariable String id) {
        return "returning info for user " + id;
    }

    @GetMapping(value = "/document/{path}")
    public Document getDocument(@PathVariable String path) {
        return fileDB.

    }





}
