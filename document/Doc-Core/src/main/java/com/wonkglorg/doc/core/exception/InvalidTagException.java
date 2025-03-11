package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

public class InvalidTagException extends CoreException{

	public InvalidTagException(RepoId repoId) {
		super(repoId);
	}

	public InvalidTagException(RepoId repoId, String message) {
		super(repoId, message);
	}

	public InvalidTagException(RepoId repoId, String message, Throwable cause) {
		super(repoId, message, cause);
	}

	public InvalidTagException(RepoId repoId, Throwable cause) {
		super(repoId, cause);
	}

	public InvalidTagException(RepoId repoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(repoId, message, cause, enableSuppression, writableStackTrace);
	}
}
