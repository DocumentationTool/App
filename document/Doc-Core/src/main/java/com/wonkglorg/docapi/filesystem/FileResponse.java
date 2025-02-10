package com.wonkglorg.docapi.filesystem;

public record FileResponse(StatusCode statusCode, String message, Exception exception) {
    public boolean wasSuccessful() {
        return exception == null && statusCode == StatusCode.SUCCESS;
    }
}
