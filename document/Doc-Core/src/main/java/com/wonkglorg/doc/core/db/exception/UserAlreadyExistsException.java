package com.wonkglorg.doc.core.db.exception;

import com.wonkglorg.doc.core.objects.UserId;

public class UserAlreadyExistsException extends RuntimeException {
    private UserId userId;

    public UserAlreadyExistsException(String message, UserId userId) {
        super(message);
        this.userId = userId;
    }

    public UserAlreadyExistsException(String message, Throwable cause, UserId userId) {
        super(message, cause);
        this.userId = userId;
    }
}
