package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

public class InvalidPathException extends CoreException {
    public InvalidPathException(RepoId repoId) {
        super(repoId);
    }

    public InvalidPathException(RepoId repoId, String message) {
        super(repoId, message);
    }

    public InvalidPathException(RepoId repoId, String message, Throwable cause) {
        super(repoId, message, cause);
    }

    public InvalidPathException(RepoId repoId, Throwable cause) {
        super(repoId, cause);
    }

    public InvalidPathException(RepoId repoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(repoId, message, cause, enableSuppression, writableStackTrace);
    }
}
