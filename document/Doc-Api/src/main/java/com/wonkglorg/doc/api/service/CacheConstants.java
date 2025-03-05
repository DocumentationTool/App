package com.wonkglorg.doc.api.service;

import java.util.List;

public class CacheConstants {

    public class CacheResourceConstant {
        /**
         * Cache pool to obtain all resources repo unspecific
         */
        public static final String ALL_RESOURCES = "allResources";
        /**
         * Cache pool to obtain all resources repo unspecific
         */
        public static final String ALL_REPOS = "allRepos";
        /**
         * Cache pool to obtain all resources repo unspecific
         */
        public static final String ALL_GROUPS = "allGroups";
        /**
         * Cache pool to obtain all resources repo unspecific
         */
        public static final String ALL_USERS = "allUsers";
        /**
         * Cache pool to obtain all resources from a specified repo
         */
        public static final String REPO_RESOURCES = "repoResources";
        /**
         * Cache pool to obtain all groups from a specified repo
         */
        public static final String REPO_GROUPS = "repoGroups";
        /**
         * Cache pool to obtain all users from a specified repo
         */
        public static final String REPO_USERS = "repoUsers";
        /**
         * Cache pool to obtain all permissions from a specified repo
         */
        public static final String REPO_PERMISSIONS = "repoPermissions";

        private static List<String> cacheResourceConstants = List.of(ALL_RESOURCES, ALL_REPOS, ALL_GROUPS, ALL_USERS, REPO_RESOURCES, REPO_GROUPS, REPO_USERS, REPO_PERMISSIONS);

        public static List<String> getCacheResourceConstants() {
            return cacheResourceConstants;
        }
    }


}
