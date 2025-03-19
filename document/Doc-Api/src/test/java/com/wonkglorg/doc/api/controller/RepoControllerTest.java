package com.wonkglorg.doc.api.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RepoControllerTest extends BaseIntegrationTest{
	
	public RepoControllerTest(boolean deleteOnExit) {
		super(deleteOnExit);
	}
	
	@Test
	void canGetRepos() {
		
		RestResponse restResponse = requestTemplate.getForObject("/api/repo/get", RestResponse.class);
		Assertions.assertNotNull(restResponse);
	}
}
