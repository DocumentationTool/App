package com.wonkglorg.doc.api;


import com.wonkglorg.doc.core.objects.RepoId;
import com.wonkglorg.doc.core.objects.Resource;
import com.wonkglorg.doc.core.objects.RoleId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class DocApiApplication {
    /**
     * Bypasses permissions and allows full access to all endpoints
     */
    public static final boolean DEV_MODE = true;
    public static final UserProfile DEV_USER = new UserProfile(new UserId("dev_p10209"), "dev_hash", Set.of(), Set.of(new Role(new RoleId("ADMIN"), "Admin")));
    public static final Map<RepoId, Map<UserId, UserProfile>> DEV_USERS = new HashMap<>();

    /**
     * Test resources for development
     */
    public static final List<Resource> DEV_RESOURCES = List.of(
            new Resource(Path.of("path/to/infos.xml"), "system", new RepoId("repo1"), "98z4h1kljh", true, null),
            new Resource(Path.of("path/infos.xml"), "system", new RepoId("repo1"), "98z4h1kljh", true, null),
            new Resource(Path.of("path/infos.xml"), "system", new RepoId("repo2"), "98z1231kljh", false, null),
            new Resource(Path.of("path/to/userLogs.xml"), "system", new RepoId("repo1"), "98z4h1kljh", true, null),
            new Resource(Path.of("websites.xml"), "system", new RepoId("repo2"), "98z4h1kljh", true, null)
    );


    public static void main(String[] args) {
        SpringApplication.run(DocApiApplication.class, args);
    }

}
