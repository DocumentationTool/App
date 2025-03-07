package com.wonkglorg.doc.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("doc.web.api")
public class ApiProperties {

    //private Map<String, List<CorsData>> crossOrigin = new HashMap<>();

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

    public static class CorsData {
        private String path;
        private String origin;
        private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
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
