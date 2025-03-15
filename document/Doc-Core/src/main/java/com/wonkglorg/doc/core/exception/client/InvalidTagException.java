package com.wonkglorg.doc.core.exception.client;

public class InvalidTagException extends ClientException{
	public InvalidTagException() {
	}
	
	public InvalidTagException(String message) {
		super(message);
	}
	
	public InvalidTagException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidTagException(Throwable cause) {
		super(cause);
	}
	
	public InvalidTagException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
