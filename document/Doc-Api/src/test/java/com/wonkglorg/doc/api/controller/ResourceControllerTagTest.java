package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.api.service.RepoService;
import com.wonkglorg.doc.core.objects.RepoId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
class ResourceControllerTagTest {
    //todo:jmd how to properly cleanup after tests done?

    @TestConfiguration
    public class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RepoService repoService;

    @Test
    void testInsertResource() {

        //should fail not all required parameters given
        Assertions.assertNull(restTemplate.getForObject("/api/resource/tag/add", RestResponse.class));
        //should fail not all required parameters given
        Assertions.assertNull(restTemplate.getForObject("/api/resource/tag/add?repoId='repo1'", RestResponse.class));
        //should fail not all required parameters given
        Assertions.assertNull(restTemplate.getForObject("/api/resource/tag/add?repoId='repo1'&tagId='tag1'", RestResponse.class));
        RepoId repoId = repoService.getRepositories().keySet().iterator().next();


        removeTag(repoId.id(), "tag1");
        addTag(repoId.id(), "tag1", "Tag Name", false);

        //RestResponse sucessObject = restTemplate.getForObject("/api/resource/tag/add?repoId='repo1'&tagId='tag1'&tagId='resource1'&tagName='Tag Name'", RestResponse.class);


        //Assertions.assertEquals("", sucessObject);

    }


    /**
     * Utility method to add tags and assertions for either result if it existed and got added or already existed and nothing happened
     *
     * @param repoId
     * @param tagId
     * @param tagName
     * @param failOnExists
     */
    private void addTag(String repoId, String tagId, String tagName, boolean failOnExists) {
        RestResponse restResponse = restTemplate.postForObject("/api/resource/tag/add?repoId=%s&tagId=%s&tagName=%s".formatted(repoId, tagId, tagName), null, RestResponse.class);
        if (restResponse.error() != null) {
            if (failOnExists) {
                Assertions.fail("Resource insertion marked as no fail, failed with error: '%s'".formatted(restResponse.error()));
            }
            Assertions.assertEquals("Tag '%s' already exists".formatted(tagId), restResponse.error());
        } else {
            Assertions.assertEquals("Created tag '%s' in repo '%s'".formatted(tagId, repoId), restResponse.message());
        }
    }


    /**
     * Utility method to remove tags and assertions for either result if it existed and got removed or didn't exist and nothing happened
     *
     * @param repoId
     * @param tagId
     */
    private void removeTag(String repoId, String tagId) {
        RestResponse restResponse = restTemplate.postForObject("/api/resource/tag/remove?repoId=%s&tagId=%s".formatted(repoId, tagId), null, RestResponse.class);
        if (restResponse.error() != null) {
            Assertions.assertEquals("Tag '%s' does not exist".formatted(tagId), restResponse.error());
        } else {
            Assertions.assertEquals("Removed tag '%s' from repo '%s'".formatted(tagId, repoId), restResponse.message());
        }
    }

}
