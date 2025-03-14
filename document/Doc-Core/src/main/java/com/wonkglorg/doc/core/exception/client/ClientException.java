package com.wonkglorg.doc.core.exception.client;

/**
 * Represent exceptions that do not necessarily indicate an error in the code, but rather an error in the client's request and should be treated in the controllers return separately instead of logging it as an error
 */
public class ClientException extends RuntimeException{
	public ClientException() {
	}
	
	public ClientException(String message) {
		super(message);
	}
	
	public ClientException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ClientException(Throwable cause) {
		super(cause);
	}
	
	public ClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
