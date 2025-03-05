package com.wonkglorg.doc.core.db.exception;

public class RuntimeSQLException extends RuntimeException {
    public RuntimeSQLException(String message) {
        super(message);
    }

    public RuntimeSQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeSQLException(Throwable cause) {
        super(cause);
    }

    public RuntimeSQLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RuntimeSQLException() {
    }
}
