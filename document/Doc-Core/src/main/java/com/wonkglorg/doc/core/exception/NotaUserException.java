package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

public class NotaUserException extends CoreException{
    public NotaUserException(RepoId repoId) {
        super(repoId);
    }

    public NotaUserException(RepoId repoId, String message) {
        super(repoId, message);
    }

    public NotaUserException(RepoId repoId, String message, Throwable cause) {
        super(repoId, message, cause);
    }

    public NotaUserException(RepoId repoId, Throwable cause) {
        super(repoId, cause);
    }

    public NotaUserException(RepoId repoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(repoId, message, cause, enableSuppression, writableStackTrace);
    }
}
