package com.wonkglorg.docapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocApiApplication {
    /**
     * Bypasses permissions and allows full access to all endpoints
     */
    public static final boolean DEV_MODE = true;


    public static void main(String[] args) {
        SpringApplication.run(DocApiApplication.class, args);
    }

}
