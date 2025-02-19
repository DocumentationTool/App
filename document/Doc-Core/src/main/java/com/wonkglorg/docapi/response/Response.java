package com.wonkglorg.docapi.response;

public abstract class Response {
    /**
     * Response Text
     */
    private final String response;
    /**
     * Null or an error message if the request failed
     */
    private final String errorMessage;

    protected Response(String response, String errorMessage) {
        this.response = response;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return errorMessage == null;
    }

    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
