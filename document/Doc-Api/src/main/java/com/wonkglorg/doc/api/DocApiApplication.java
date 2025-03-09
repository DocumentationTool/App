package com.wonkglorg.doc.api;

import com.wonkglorg.doc.core.objects.RoleId;
import com.wonkglorg.doc.core.objects.UserId;
import com.wonkglorg.doc.core.permissions.Role;
import com.wonkglorg.doc.core.user.UserProfile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Set;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class DocApiApplication{
	/**
	 * Bypasses permissions and allows full access to all endpoints
	 */
	public static final boolean DEV_MODE = true;
	public static final UserProfile DEV_USER = new UserProfile(new UserId("dev_p10209"),
			"dev_hash",
			Set.of(),
			Set.of(new Role(new RoleId("ADMIN"), "Admin")));
	
	public static void main(String[] args) {
		SpringApplication.run(DocApiApplication.class, args);
	}
}
