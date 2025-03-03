package com.wonkglorg.doc.core.db.functions;

import com.wonkglorg.doc.core.objects.RepoId;

public class ResponseTemplates {

    /**
     * Templates a default error response
     *
     * @param repoId  the repo this error occurred in
     * @param message the error message
     * @return a formatted error message
     */
    public static String errorTemplate(RepoId repoId, String message) {
        return String.format("Error in '%s' | '%s'", repoId, message);
    }


}
