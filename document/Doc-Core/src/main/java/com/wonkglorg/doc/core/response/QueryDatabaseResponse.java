package com.wonkglorg.doc.core.response;

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
     * @param data the data the query returns
     * @return the returned response
     * @param <T>
     */
    public static <T> QueryDatabaseResponse<T> success(T data) {
        return new QueryDatabaseResponse<>(null, null, data);
    }

    /**
     * Constructs a new Success response with the attached data and a message
     * @param response the message to respond with
     * @param data the data provided
     * @return the constructed response
     * @param <T>
     */
    public static <T> QueryDatabaseResponse<T> success(String response, T data) {
        return new QueryDatabaseResponse<>(response, null, data);
    }

    /**
     * Constructs a new Failed response with the causing exception
     * @param e the exception that causes the error
     * @return the constructed reponse
     * @param <T>
     */
    public static <T> QueryDatabaseResponse<T> fail(Exception e) {
        return new QueryDatabaseResponse<>(null, e, null);
    }

    public T getData() {
        return data;
    }
}
