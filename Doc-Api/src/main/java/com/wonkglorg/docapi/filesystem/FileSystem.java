package com.wonkglorg.docapi.filesystem;

import com.wonkglorg.docapi.common.Document;

import java.nio.file.Path;

public interface FileSystem {

    /**
     * @return the documents content
     */
    String getDocument(Path path);

    void createDocument(Document document);

    void deleteDocument(Document document);

    void deleteDocument(Path path);

    void saveDocument(Document document);


}
