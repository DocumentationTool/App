package com.wonkglorg.doc.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the API
 */
@Configuration
@ConfigurationProperties("doc.web.api")
public class ApiProperties {

    //private Map<String, List<CorsData>> crossOrigin = new HashMap<>();
    /**
     * Allowed cross origin access for specific paths
     */
    private List<CorsData> crossOrigin = new ArrayList<>();
    /**
     * All whitelisted pages that can be accessed without user permissions
     */
    private List<String> whitelist = new ArrayList<>();

    public List<String> getWhitelist() {
        return whitelist;
    }


    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public List<CorsData> getCrossOrigin() {
        return crossOrigin;
    }

    public void setCrossOrigin(List<CorsData> crossOrigin) {
        this.crossOrigin = crossOrigin;
    }

    /**
     * Cross origin entry
     */
    public static class CorsData {
        /**
         * The path this applies to
         */
        private String path;
        /**
         * The origin this applies to
         */
        private String origin;
        /**
         * Allowed headers for this path
         */
        private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
        /**
         * Allowed methods for this path
         */
        private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE"));

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }


}
