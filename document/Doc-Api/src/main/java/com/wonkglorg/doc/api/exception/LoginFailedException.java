package com.wonkglorg.doc.api.exception;

import org.springframework.http.HttpStatusCode;

/**
 * Thrown when login fails
 */
public class LoginFailedException extends RuntimeException{
	private HttpStatusCode statusCode;
	
	public LoginFailedException(String message, HttpStatusCode statusCode) {
		super(message);
		this.statusCode = statusCode;
	}
	
	public HttpStatusCode getStatusCode() {
		return statusCode;
	}
}
