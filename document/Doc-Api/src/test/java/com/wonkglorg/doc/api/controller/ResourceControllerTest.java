package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.core.request.ResourceRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
class ResourceControllerTest {
    //todo:jmd how to properly test those and not create real repos?
    private static final RestResponse<Map<?, ?>> NonResponse = new RestResponse<>(null, null, new HashMap<>());
    private static final Logger log = LoggerFactory.getLogger(ResourceControllerTest.class);
    public String token;

    //todo:jmd reset all tests beforehand otherwise there will be conflicts, should also be independent of each other

    @TestConfiguration
    public class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetResources() {
        //empty request should not be valid
        ResourceRequest invalidRequest = new ResourceRequest(null, null, null, null, null, null, false, 0);
        //should fail duo to missing required parameters
        Assertions.assertEquals(NonResponse, restTemplate.postForObject("/api/resource/get", invalidRequest, RestResponse.class));

        ResourceRequest validRequestWithWrongRepo = new ResourceRequest(null, null, "test", "test", null, null, false, 0);
        Assertions.assertEquals("Repo 'test' does not exist",
                restTemplate.postForObject("/api/resource/get", validRequestWithWrongRepo, RestResponse.class).error());

        ResourceRequest validRequestWithWrongUser = new ResourceRequest(null, null, "repo1", "test", null, null, false, 0);
        Assertions.assertEquals("User 'test' does not exist",
                restTemplate.postForObject("/api/resource/get", validRequestWithWrongUser, RestResponse.class).error());
    }

    private void delete(String repoId, Path path) {
        restTemplate.postForObject("/api/resource/remove?repoId=" + repoId + "&path=" + path.toString(), null, RestResponse.class);
    }

    @Test
    void addResources() {
        //should fail duo to missing required parameters
        Assertions.assertEquals("Repo 'test' does not exist",
                restTemplate.postForObject("/api/resource/add?repoId=test&path=test&createdBy=test", "File Content", RestResponse.class).error());

        Assertions.assertEquals("Path 'test' file type is not allowed, only .md files are allowed",
                restTemplate.postForObject("/api/resource/add?repoId=repo1&path=test&createdBy=test", "File Content", RestResponse.class).error());

        Assertions.assertEquals("Path '..\\test.md' cannot contain '..' to escape the current directory",
                restTemplate.postForObject("/api/resource/add?repoId=repo1&path=../test.md&createdBy=test", "File Content", RestResponse.class)
                        .error());

        Assertions.assertEquals("Path '\\test.md' cannot start with a '/'",
                restTemplate.postForObject("/api/resource/add?repoId=repo1&path=/test.md&createdBy=test", "File Content", RestResponse.class)
                        .error());

        //remove resource first before adding it again.
        Assertions.assertNotNull(restTemplate.postForObject("/api/resource/remove?repoId=repo1&path=test.md", "File Content", RestResponse.class));

        Assertions.assertEquals("Successfully inserted 'test.md' Resource!",
                restTemplate.postForObject("/api/resource/add?repoId=repo1&path=test.md&createdBy=test", "File Content", RestResponse.class)
                        .message());
    }

    @Test
    void removeResources() {
        //should fail duo to missing required parameters
        Assertions.assertEquals("Repo 'test' does not exist",
                restTemplate.postForObject("/api/resource/remove?repoId=test&path=test", null, RestResponse.class).error());

        Assertions.assertEquals("Path 'test' file type is not allowed, only .md files are allowed",
                restTemplate.postForObject("/api/resource/remove?repoId=repo1&path=test", null, RestResponse.class).error());

        Assertions.assertEquals("Path '..\\test.md' cannot contain '..' to escape the current directory",
                restTemplate.postForObject("/api/resource/remove?repoId=repo1&path=../test.md", null, RestResponse.class).error());

        Assertions.assertEquals("Path '\\test.md' cannot start with a '/'",
                restTemplate.postForObject("/api/resource/remove?repoId=repo1&path=/test.md", null, RestResponse.class).error());

        Assertions.assertEquals("Successfully inserted 'test/remove/file.md' Resource!",
                restTemplate.postForObject("/api/resource/add?repoId=repo1&path=test/remove/file.md&createdBy=test", "File Content", RestResponse.class)
                        .message());

        Assertions.assertEquals("Successfully removed 'test/remove/file.md' Resource!",
                restTemplate.postForObject("/api/resource/remove?repoId=repo1&path=test/remove/file.md", null, RestResponse.class).message());
    }

}
