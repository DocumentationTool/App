package com.wonkglorg.doc.api;


import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class DocApiApplication{
	/**
	 * Bypasses permissions and allows full access to all endpoints
	 */
	public static final boolean DEV_MODE = true;
	public static final UserProfile DEV_USER = new UserProfile("dev_p10209","dev_hash", List.of(), List.of(new Role("ADMIN", "Admin")));
	
	public static void main(String[] args) {
		SpringApplication.run(DocApiApplication.class, args);
	}
	
}
