package com.wonkglorg.doc.core.response;

/**
 * A generic response
 */
public abstract class Response {
    /**
     * Response Text
     */
    private final String response;
    /**
     * Null or an error code if the request failed
     */
    private final Exception exception;

    protected Response(String response, Exception exception) {
        this.response = response;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return exception == null;
    }

    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return exception.getMessage();
    }
}
