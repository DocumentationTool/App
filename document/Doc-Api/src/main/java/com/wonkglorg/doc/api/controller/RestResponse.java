package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.core.response.ObjectResponse;
import com.wonkglorg.doc.core.response.QueryDatabaseResponse;
import com.wonkglorg.doc.core.response.Response;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

/**
 * A response object returned by the REST endpoints
 */
public record RestResponse<T>(String message, String error, T content) {

    /**
     * Create a RestResponse from an ObjectResponse
     *
     * @param response the response to convert
     * @return the converted response
     */
    public static <T> RestResponse<T> of(ObjectResponse<T> response) {
        if (response.isError()) {
            return RestResponse.error(response.getErrorMessage());
        }

        return RestResponse.success(response.getResponseText(), response.getData());
    }

    /**
     * Create a RestResponse from a QueryDatabaseResponse
     *
     * @param response the response to convert
     * @return the converted response
     */
    public static <T> RestResponse<T> of(QueryDatabaseResponse<T> response) {
        if (response.isError()) {
            return RestResponse.error(response.getErrorMessage());
        }

        return RestResponse.success(response.getResponseText(), response.get());
    }

    /**
     * Create a RestResponse from a Response
     *
     * @param response the response to convert
     * @return the converted response
     */
    public static RestResponse<Void> of(Response response) {
        if (response.isError()) {
            return RestResponse.error(response.getErrorMessage());
        }

        return RestResponse.success(response.getResponseText(), null);
    }

    public static <T> RestResponse<T> success(String message, T content) {
        return new RestResponse<>(message, null, content);
    }

    public static <T> RestResponse<T> success(T content) {
        return new RestResponse<>(null, null, content);
    }

    public static <T> RestResponse<T> error(String error) {
        return new RestResponse<>(null, error, null);
    }

    public ResponseEntity<RestResponse<T>> toResponse() {
        if (error != null) {
            return ResponseEntity.badRequest().body(this);
        }
        return ResponseEntity.ok(this);
    }

    public ResponseEntity<RestResponse<T>> toResponse(HttpStatusCode errorStatus) {
        if (error != null) {
            return ResponseEntity.status(errorStatus).body(this);
        }
        return ResponseEntity.ok(this);
    }

}
