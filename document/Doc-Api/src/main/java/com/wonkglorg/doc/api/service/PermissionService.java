package com.wonkglorg.doc.api.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
public class PermissionService{
	
	private final RepoService repoService;
	
	public PermissionService(@Lazy RepoService repoService) {this.repoService = repoService;}
}
