package com.wonkglorg.docapi;

import com.wonkglorg.docapi.permissions.Role;
import com.wonkglorg.docapi.user.DefaultProfile;
import com.wonkglorg.docapi.user.UserProfile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;

@SpringBootApplication
@EnableAsync
public class DocApiApplication{
	/**
	 * Bypasses permissions and allows full access to all endpoints
	 */
	public static final boolean DEV_MODE = true;
	public static final UserProfile DEV_USER = new DefaultProfile("dev_p10209", List.of(), List.of(new Role("ADMIN", "Admin")));
	
	public static void main(String[] args) {
		SpringApplication.run(DocApiApplication.class, args);
	}
	
}
