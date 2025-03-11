package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

/**
 * When the repo specified is not found
 */
public class NotaRepoException extends CoreException {
    public NotaRepoException(RepoId repoId) {
        super(repoId);
    }

    public NotaRepoException(RepoId repoId, String message) {
        super(repoId, message);
    }

    public NotaRepoException(RepoId repoId, String message, Throwable cause) {
        super(repoId, message, cause);
    }

    public NotaRepoException(RepoId repoId, Throwable cause) {
        super(repoId, cause);
    }

    public NotaRepoException(RepoId repoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(repoId, message, cause, enableSuppression, writableStackTrace);
    }
}
