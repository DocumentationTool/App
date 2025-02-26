package com.wonkglorg.docapi.manager;

import com.wonkglorg.docapi.common.Resource;
import com.wonkglorg.docapi.common.UserId;
import com.wonkglorg.docapi.db.RepoDB;
import com.wonkglorg.docapi.git.GitRepo;
import com.wonkglorg.docapi.git.RepoProperties;
import com.wonkglorg.docapi.manager.caches.CachedResources;
import com.wonkglorg.docapi.manager.caches.CachedUsers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;
import java.util.*;

import static com.wonkglorg.docapi.git.GitRepo.GitStage.*;

/**
 * Represents a managed repository
 */
public class FileRepository {
    private static final Logger log = LogManager.getLogger(FileRepository.class);
    /**
     * THe properties of the repository
     */
    private final RepoProperties repoProperties;
    /**
     * The backing repo
     */
    private GitRepo gitRepo;
    /**
     * Represents the backing database of a repo
     */
    private RepoDB dataDB;
    private final CachedUsers cachedUsers = new CachedUsers(this);
    private final CachedResources cachedResources = new CachedResources(this);
    public FileRepository(RepoProperties repoProperty) {
        this.repoProperties = repoProperty;
    }

    public RepoDB getDataDB() {
        return dataDB;
    }

    public GitRepo getGitRepo() {
        return gitRepo;
    }

    /**
     * Initializes the repository by checking for the database file and updating the database
     *
     * @throws GitAPIException if there is an error with the git repo
     */
    public void initialize() throws GitAPIException {
        log.info("Looking for repo in: '{}'", repoProperties.getPath());
        gitRepo = new GitRepo(repoProperties);
        Optional<Path> file = gitRepo.getSingleFile(s -> s.equalsIgnoreCase(repoProperties.getDbName()), UNTRACKED, MODIFIED, ADDED);

        if (file.isEmpty()) {
            log.info("No Database in '{}'. Creating new Database.", repoProperties.getDbName());
        }

        dataDB = new RepoDB(repoProperties, gitRepo.getDatabaseRepoPath().resolve(repoProperties.getDbName()));
        dataDB.initialize();

        Set<Path> foundFiles = gitRepo.getFiles(s -> s.toLowerCase().endsWith(".xml"), UNTRACKED, MODIFIED, ADDED);

        boolean hasChanged = dataDB.updateResources(foundFiles);

        if (hasChanged && !repoProperties.isReadOnly()) {
            gitRepo.addFile(repoProperties.getDbName());
            gitRepo.commit("Updated File DB");
        }
    }


    //todo:jmd how to properly build up the permission and read from them?
    /**
     * Gets all Resources the user has access to and their permission
     * @param userId
     * @return
     */
    public List<Resource> getResourcesForUser(String userId){


    }

    public List<PermissionNode> getUserPermissions(UserId userId) {
        userPermissions.getPermissions(userId);

        groupPermissions.getGroupPermissions(userId);
    }

    public RepoProperties getRepoProperties() {
        return repoProperties;
    }
}
