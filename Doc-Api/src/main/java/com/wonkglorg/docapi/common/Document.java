package com.wonkglorg.docapi.common;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a document found on the file system
 *
 * @param title        the title of the file
 * @param author       the author
 * @param creationDate the creation date
 * @param editTime     last edited time
 * @param contentLines
 */
public record Document(Path path, String title, String author, LocalDateTime creationDate, LocalDateTime editTime,
                       List<String> contentLines) {


    public Document(Path path, String title, String author, LocalDateTime creationDate, LocalDateTime editTime, String content) {
        this(path,title, author, creationDate, editTime, Arrays.stream(content.split("\n")).toList());
    }
}
