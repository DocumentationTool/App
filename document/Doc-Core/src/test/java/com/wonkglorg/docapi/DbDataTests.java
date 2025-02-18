package com.wonkglorg.docapi;

import com.wonkglorg.docapi.db.RepoDB;
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
        RepoDB repoDB = new RepoDB(repoProperties);

        repoDB.insertResource(Path.of("test","temp"),"TestData");
    }


}
