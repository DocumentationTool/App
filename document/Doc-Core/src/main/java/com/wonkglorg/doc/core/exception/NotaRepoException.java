package com.wonkglorg.doc.core.exception;

/**
 * When the repo specified is not found
 */
public class NotaRepoException extends Exception {
    public NotaRepoException(String message) {
        super(message);
    }
}
