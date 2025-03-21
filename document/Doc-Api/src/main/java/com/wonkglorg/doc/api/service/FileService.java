package com.wonkglorg.doc.api.service;

import com.wonkglorg.doc.core.convert.Converter;
import com.wonkglorg.doc.core.exception.client.ClientException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@Component
@Service
public class FileService {

    @Async
    public Future<String> convertWordToMarkdown(Path path) throws ClientException {
        return new FutureTask<>(() -> Converter.convertWordToMarkdown(path));
    }
}
