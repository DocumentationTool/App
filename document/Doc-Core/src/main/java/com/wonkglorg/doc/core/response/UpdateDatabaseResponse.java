package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.objects.RepoId;

/**
 * A response for a update database operation, indicating its success or failure with an optional message for more information, alongside a count of rows affected
 */
public class UpdateDatabaseResponse extends Response {

    /**
     * How many rows were affected by the operation
     */
    private final int rowsAffected;

    private UpdateDatabaseResponse(RepoId causingRepo, String response, Exception exception, int rowsAffected) {
        super(causingRepo, response, exception);
        this.rowsAffected = rowsAffected;
    }


    /**
     * Constructs a new Success response with the attached data and a message
     *
     * @param causingRepo  the repo this response originated from
     * @param response     the message to respond with
     * @param rowsAffected the amount of rows affected
     * @param <T>
     * @return the constructed response
     */
    public static UpdateDatabaseResponse success(RepoId causingRepo, String response, int rowsAffected) {
        return new UpdateDatabaseResponse(causingRepo, response, null, rowsAffected);
    }

    /**
     * Constructs a new Success response with the attached data and a message
     *
     * @param causingRepo  the repo this response originated from
     * @param rowsAffected the amount of rows affected
     * @param <T>
     * @return the constructed response
     */
    public static UpdateDatabaseResponse success(RepoId causingRepo, int rowsAffected) {
        return new UpdateDatabaseResponse(causingRepo, null, null, rowsAffected);
    }

    /**
     * Constructs a new Failed response with the causing exception
     *
     * @param causingRepo the repo this response originated from
     * @param e           the exception that causes the error
     * @param <T>
     * @return the constructed reponse
     */
    public static UpdateDatabaseResponse fail(RepoId causingRepo, Exception e) {
        return new UpdateDatabaseResponse(causingRepo, null, e, 0);
    }


    public int getRowsAffected() {
        return rowsAffected;
    }
}
