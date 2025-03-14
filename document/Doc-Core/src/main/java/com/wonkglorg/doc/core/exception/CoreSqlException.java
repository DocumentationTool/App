package com.wonkglorg.doc.core.exception;

import com.wonkglorg.doc.core.objects.RepoId;

public class CoreSqlException extends CoreException{
	public CoreSqlException() {
	}

	public CoreSqlException(String message) {
		super(message);
	}

	public CoreSqlException(String message, Throwable cause) {
		super(message, cause);
	}

	public CoreSqlException(Throwable cause) {
		super(cause);
	}

	public CoreSqlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
