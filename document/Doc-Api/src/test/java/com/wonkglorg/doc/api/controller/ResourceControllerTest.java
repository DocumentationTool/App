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
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "server.port=8080")
class ResourceControllerTest{
	private static final Logger log = LoggerFactory.getLogger(ResourceControllerTest.class);
	public String token;
	
	@TestConfiguration
	public class TestSecurityConfig{
		@Bean
		public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
			return http.csrf().disable().authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
		}
	}
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	//todo:jmd change to post?
	@Test
	void testGetResource() {
		//empty request should not be valid
		ResourceRequest request = new ResourceRequest();
		
		Assertions.assertNull(restTemplate.postForObject("/api/resource/add", request, RestResponse.class));
		
		//adds required repo param
		request.setRepoId("1");
		Assertions.assertNull(restTemplate.postForObject("/api/resource/add", request, RestResponse.class));
		
		
		
	}
}
