package com.wonkglorg.docapi;

import com.wonkglorg.docapi.db.RepositoryDatabase;
import com.wonkglorg.docapi.git.RepoProperties;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class DbDataTests {

    @Test
    void insertResource(){
        RepoProperties repoProperties = new RepoProperties();
        repoProperties.setName("testDB");
        repoProperties.setPath(Path.of("test","temp"));
        repoProperties.setReadOnly(false);
        RepositoryDatabase repoDB = new RepositoryDatabase(repoProperties);

        repoDB.insertResource(Path.of("test","temp"),"TestData");
    }


}
