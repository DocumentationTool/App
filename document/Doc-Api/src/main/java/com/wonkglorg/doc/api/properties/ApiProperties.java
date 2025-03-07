package com.wonkglorg.doc.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties("doc.web.api")
public class ApiProperties {
    private Map<String, List<CorsData>> crossOrigin = new HashMap<>();

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

    public Map<String, List<CorsData>> getCrossOrigin() {
        return crossOrigin;
    }

    public void setCrossOrigin(Map<String, List<CorsData>> crossOrigin) {
        this.crossOrigin = crossOrigin;
    }

    public static class CorsData{
        private String origin;
        private List<String> allowedHeaders = List.of("*");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE");

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
    }


}
