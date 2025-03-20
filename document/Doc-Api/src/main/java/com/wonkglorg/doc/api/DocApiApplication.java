package com.wonkglorg.doc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class DocApiApplication {
    /**
     * Bypasses permissions and allows full access to all endpoints
     */
    public static final boolean DEV_MODE = true;

    public static void main(String[] args) {
        SpringApplication.run(DocApiApplication.class, args);
    }
}
