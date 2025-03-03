package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.user.UserProfile;

/**
 * The response indicating a user
 */
public class  ObjectResponse<T> extends Response {

    /**
     * The data returned by this query
     */
    private final T data;

    private ObjectResponse(String response, Exception exception, T data) {
        super(response, exception);
        this.data = data;
    }


    /**
     * Constructs a new Sucess response with the attached data
     * @param data the data the query returns
     * @return the returned response
     * @param <T>
     */
    public static <T> ObjectResponse<T> success(T data) {
        return new ObjectResponse<>(null, null, data);
    }

    /**
     * Constructs a new Success response with the attached data and a message
     * @param response the message to respond with
     * @param data the data provided
     * @return the constructed response
     * @param <T>
     */
    public static <T> ObjectResponse<T> success(String response, T data) {
        return new ObjectResponse<>(response, null, data);
    }

    /**
     * Constructs a new Failed response with the causing exception
     * @param e the exception that causes the error
     * @return the constructed reponse
     * @param <T>
     */
    public static <T> ObjectResponse<T> fail(Exception e) {
        return new ObjectResponse<>(null, e, null);
    }

    public T getData() {
        return data;
    }


}
