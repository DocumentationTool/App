package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.objects.RepoId;

/**
 * The response indicating a user
 */
public class ObjectResponse<T> extends Response {

    /**
     * The data returned by this query
     */
    private final T data;

    private ObjectResponse(RepoId causingRepo, String response, Exception exception, T data) {
        super(causingRepo, response, exception);
        this.data = data;
    }


    /**
     * Constructs a new Sucess response with the attached data
     *
     * @param causingRepo the repo this response originated from
     * @param data        the data the query returns
     * @param <T>
     * @return the returned response
     */
    public static <T> ObjectResponse<T> success(RepoId causingRepo, T data) {
        return new ObjectResponse<>(causingRepo, null, null, data);
    }

    /**
     * Constructs a new Success response with the attached data and a message
     *
     * @param causingRepo the repo this response originated from
     * @param response    the message to respond with
     * @param data        the data provided
     * @param <T>
     * @return the constructed response
     */
    public static <T> ObjectResponse<T> success(RepoId causingRepo, String response, T data) {
        return new ObjectResponse<>(causingRepo, response, null, data);
    }

    /**
     * Constructs a new Failed response with the causing exception
     *
     * @param causingRepo the repo this response originated from
     * @param e           the exception that causes the error
     * @param <T>
     * @return the constructed reponse
     */
    public static <T> ObjectResponse<T> fail(RepoId causingRepo, Exception e) {
        return new ObjectResponse<>(causingRepo, null, e, null);
    }

    public T getData() {
        return data;
    }


}
