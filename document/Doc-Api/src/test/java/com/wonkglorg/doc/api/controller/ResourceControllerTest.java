package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.core.request.ResourceRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResourceControllerTest{
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@Test
	void testGetResource() {
		ResourceRequest request = new ResourceRequest();
		
		ResponseEntity restResponseResponseEntity = restTemplate.postForEntity("/api/resource/add", request, RestResponse.class);
		System.out.println("Response: " + restResponseResponseEntity);
		assertEquals(HttpStatus.OK, restResponseResponseEntity.getStatusCode());
	}
}
