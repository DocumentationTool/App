package com.wonkglorg.docapi.filesystem;

import com.wonkglorg.docapi.common.Document;

import java.nio.file.Path;

public class GitFileSystem implements FileSystem {

    @Override
    public String getDocument(Path path) {
        return "";
    }

    @Override
    public void createDocument(Document document) {

    }

    @Override
    public void deleteDocument(Document document) {

    }

    @Override
    public void deleteDocument(Path path) {

    }

    @Override
    public void saveDocument(Document document) {

    }
}
