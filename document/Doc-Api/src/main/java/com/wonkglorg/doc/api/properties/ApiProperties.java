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
    private Map<String, List<String>> crossOrigin = new HashMap<>();

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

    public Map<String, List<String>> getCrossOrigin() {
        return crossOrigin;
    }

    public void setCrossOrigin(Map<String, List<String>> crossOrigin) {
        this.crossOrigin = crossOrigin;
    }

}
