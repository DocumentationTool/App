package com.wonkglorg.doc.api.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

class RepoControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void canGetRepos() {
        
        RestResponse restResponse = restTemplate.postForObject("/api/repo/get", null, RestResponse.class);
        Assertions.assertNotNull(restResponse);
        
    }
}
