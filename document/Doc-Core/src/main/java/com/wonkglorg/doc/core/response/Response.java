package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.objects.RepoId;

/**
 * A generic response
 */
public abstract class Response{
	/**
	 * Response Text
	 */
	private final String responseText;
	/**
	 * Null or an error code if the request failed
	 */
	private Exception exception;
	
	protected final RepoId causingRepo;
	
	protected Response(RepoId causingRepo, String response, Exception exception) {
		this.responseText = response;
		this.exception = exception;
		this.causingRepo = causingRepo;
	}
	
	public boolean isSuccess() {
		return exception == null;
	}
	
	public boolean isError() {
		return exception != null;
	}
	
	public String getResponseText() {
		return responseText;
	}
	
	public String getErrorMessage() {
		return exception.getMessage();
	}
	
	public Exception getException() {
		return exception;
	}
	
	public RepoId getCausingRepo() {
		return causingRepo;
	}
	
	public void setError(Exception e) {
		this.exception = e;
	}
}
