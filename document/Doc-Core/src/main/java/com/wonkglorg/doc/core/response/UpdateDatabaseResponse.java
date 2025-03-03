package com.wonkglorg.doc.core.response;

public class UpdateDatabaseResponse extends Response {

    private final int rowsAffected;

    private UpdateDatabaseResponse(String response, Exception exception, int rowsAffected) {
        super(response, exception);
        this.rowsAffected = rowsAffected;
    }


    /**
     * Constructs a new Success response with the attached data and a message
     *
     * @param response     the message to respond with
     * @param rowsAffected the amount of rows affected
     * @param <T>
     * @return the constructed response
     */
    public static UpdateDatabaseResponse success(String response, int rowsAffected) {
        return new UpdateDatabaseResponse(response, null, rowsAffected);
    }

    /**
     * Constructs a new Success response with the attached data and a message
     *
     * @param rowsAffected the amount of rows affected
     * @param <T>
     * @return the constructed response
     */
    public static UpdateDatabaseResponse success(int rowsAffected) {
        return new UpdateDatabaseResponse(null, null, rowsAffected);
    }

    /**
     * Constructs a new Failed response with the causing exception
     *
     * @param e   the exception that causes the error
     * @param <T>
     * @return the constructed reponse
     */
    public static UpdateDatabaseResponse fail(Exception e) {
        return new UpdateDatabaseResponse(null, e, 0);
    }


    public int getRowsAffected() {
        return rowsAffected;
    }
}
