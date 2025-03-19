package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.FileRepository;
import com.wonkglorg.doc.core.RepoProperty;
import com.wonkglorg.doc.core.objects.RepoId;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
public class BaseIntegrationTest {

    private static boolean deleteOnExit = false;

    private static Map<RepoId, FileRepository> repositories = new HashMap<>();

    @Autowired
    private TestRestTemplate requestTemplate;

    @Autowired
    private RepoService repoService;

    public BaseIntegrationTest(boolean deleteOnExit) {
        BaseIntegrationTest.deleteOnExit = deleteOnExit;
    }

    @PostConstruct
    public void initialize() {
        if(!deleteOnExit) return;
        for (RepoId repoId : repoService.getRepositories().keySet()) {
            repositories.put(repoId, repoService.getRepositories().get(repoId));
        }
    }

    @AfterAll
    static void cleanUp() throws IOException {
        for (var property : repositories.entrySet()) {
            RepoProperty repoProperties = property.getValue().getRepoProperties();
            Path pathToRemove = repoProperties.getPath();
            if (repoProperties.isReadOnly()) {
                pathToRemove = repoProperties.getDbStorage();
            }
            deleteDirectoryRecursively(pathToRemove);
        }
    }


    private static void deleteDirectoryRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Files.delete(file);
                } catch (Exception e) {
                    file.toFile().deleteOnExit();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    Files.delete(dir);
                } catch (Exception e) {
                    dir.toFile().deleteOnExit();
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
