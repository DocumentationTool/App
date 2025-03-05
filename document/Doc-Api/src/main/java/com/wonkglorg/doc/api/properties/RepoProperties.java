package com.wonkglorg.doc.api.properties;

import com.wonkglorg.doc.core.RepoProperty;
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
	private final List<RepoProperty> repositories = new ArrayList<>();
	
	public List<RepoProperty> getRepositories() {
		return repositories;
	}

}
