package com.wonkglorg.docapi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("doc.git")
public class RepoProperties{
	/**
	 * List of repositories that are being managed by the application
	 */
	private final List<com.wonkglorg.docapi.git.RepoProperties> repositories = new ArrayList<>();
	
	public List<com.wonkglorg.docapi.git.RepoProperties> getRepositories() {
		return repositories;
	}

}
