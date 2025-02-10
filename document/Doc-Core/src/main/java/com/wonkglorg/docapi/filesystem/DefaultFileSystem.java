package com.wonkglorg.docapi.filesystem;

import com.wonkglorg.docapi.common.Document;

import java.nio.file.Path;

public class DefaultFileSystem implements FileSystem{

    @Override
    public String getDocument(Path path) {
        return "";
    }

    @Override
    public void createDocument(Document document) {

    }

    @Override
    public boolean deleteDocument(Document document) {
        return false;
    }

    @Override
    public boolean deleteDocument(Path path) {
        return false;
    }

    @Override
    public boolean saveDocument(Document document) {
        return false;
    }
}
