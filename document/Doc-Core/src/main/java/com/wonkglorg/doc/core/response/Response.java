package com.wonkglorg.doc.core.response;

/**
 * A generic response
 */
public abstract class Response {
    /**
     * Response Text
     */
    private final String responseText;
    /**
     * Null or an error code if the request failed
     */
    private final Exception exception;

    protected Response(String response, Exception exception) {
        this.responseText = response;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return exception == null;
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
}
