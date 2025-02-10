package com.wonkglorg.docapi.filesystem;

import com.wonkglorg.docapi.common.Document;

import java.nio.file.Path;

public interface FileSystem {

    /**
     * @return the documents content
     */
    String getDocument(Path path);

    /**
     * Creates a new Document
     * @param document the document to create
     */
    void createDocument(Document document);

    /**
     * Deletes a provided document
     * @param document the document to delete
     */
    boolean deleteDocument(Document document);

    /**
     * Deletes a document by the given qualified path
     * @param path the path to delete it by
     */
    boolean deleteDocument(Path path);


    /**
     *
     * @param document
     * @return
     */
    boolean saveDocument(Document document);


}
