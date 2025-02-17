package com.wonkglorg.docapi.exception;

import org.springframework.http.HttpStatusCode;

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
