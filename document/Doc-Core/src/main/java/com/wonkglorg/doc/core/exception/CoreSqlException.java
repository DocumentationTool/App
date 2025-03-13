package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

public class CoreSqlException extends CoreException{
	public CoreSqlException(RepoId repoId) {
		super(repoId);
	}
	
	public CoreSqlException(RepoId repoId, String message) {
		super(repoId, message);
	}
	
	public CoreSqlException(RepoId repoId, String message, Throwable cause) {
		super(repoId, message, cause);
	}
	
	public CoreSqlException(RepoId repoId, Throwable cause) {
		super(repoId, cause);
	}
	
	public CoreSqlException(RepoId repoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(repoId, message, cause, enableSuppression, writableStackTrace);
	}
}
