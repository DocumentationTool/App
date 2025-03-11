package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

public class CoreException extends RuntimeException {
    private final RepoId repoId;

    public CoreException(RepoId repoId) {
        this.repoId = repoId;
    }

    public CoreException(RepoId repoId, String message) {
        super(message);
        this.repoId = repoId;
    }

    public CoreException(RepoId repoId, String message, Throwable cause) {
        super(message, cause);
        this.repoId = repoId;
    }

    public CoreException(RepoId repoId, Throwable cause) {
        super(cause);
        this.repoId = repoId;
    }

    public CoreException(RepoId repoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.repoId = repoId;
    }
}
