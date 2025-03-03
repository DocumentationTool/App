package com.wonkglorg.doc.core.response;

import java.util.Optional;

/**
 * A response for a database query operation, indicating its success or failure with an optional message for more information, alongside the data returned by the query
 * @param <T>
 */
public class QueryDatabaseResponse<T> extends Response {
    /**
     * The data returned by this query
     */
    private final T data;

    private QueryDatabaseResponse(String response, Exception exception, T data) {
        super(response, exception);
        this.data = data;
    }


    /**
     * Constructs a new Sucess response with the attached data
     *
     * @param data the data the query returns
     * @param <T>
     * @return the returned response
     */
    public static <T> QueryDatabaseResponse<T> success(T data) {
        return new QueryDatabaseResponse<>(null, null, data);
    }

    /**
     * Constructs a new Success response with the attached data and a message
     *
     * @param response the message to respond with
     * @param data     the data provided
     * @param <T>
     * @return the constructed response
     */
    public static <T> QueryDatabaseResponse<T> success(String response, T data) {
        return new QueryDatabaseResponse<>(response, null, data);
    }

    /**
     * Constructs a new Failed response with the causing exception
     *
     * @param e   the exception that causes the error
     * @param <T>
     * @return the constructed reponse
     */
    public static <T> QueryDatabaseResponse<T> fail(Exception e) {
        return new QueryDatabaseResponse<>(null, e, null);
    }

    public Optional<T> get() {
        return Optional.of(data);
    }
}
