package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

public class ResourceException extends CoreException {

    public ResourceException(RepoId repoId) {
        super(repoId);
    }

    public ResourceException(RepoId repoId, String message) {
        super(repoId, message);
    }

    public ResourceException(RepoId repoId, String message, Throwable cause) {
        super(repoId, message, cause);
    }

    public ResourceException(RepoId repoId, Throwable cause) {
        super(repoId, cause);
    }

    public ResourceException(RepoId repoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(repoId, message, cause, enableSuppression, writableStackTrace);
    }
}
