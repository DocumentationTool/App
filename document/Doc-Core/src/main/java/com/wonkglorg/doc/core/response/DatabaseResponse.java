package com.wonkglorg.doc.core.response;

public class DatabaseResponse<T> extends Response {

    /**
     * Row Change indicating a failed response
     */
    public static final int FAIL = -1;
    /**
     * The amount of rows changed, 0 if no changes -1 if an error occurred or a positive number indicating how many rows changed
     */
    private final int rowChanges;

    /**
     * The data returned
     */
    private final T data;

    public DatabaseResponse(String response, Exception error, int rowChanges, T data) {
        super(response, error);
        this.rowChanges = rowChanges;
        this.data = null;
    }

    public DatabaseResponse(String response, Exception error, int rowChanges) {
        super(response, error);
        this.rowChanges = rowChanges;
        this.data = null;
    }

    public DatabaseResponse(String response, Exception error) {
        super(response, error);
        this.rowChanges = FAIL;
        this.data = null;
    }

    public DatabaseResponse(String response, int rowChanges) {
        super(response, rowChanges < 0 ? new RuntimeException("An error occurred during execution.") : null);
        this.rowChanges = rowChanges;
        this.data = null;
    }

    public DatabaseResponse(Exception e) {
        super(null, e);
        this.data = null;
        this.rowChanges = FAIL;
    }

    public DatabaseResponse(int rowChanges) {
        super("", rowChanges < 0 ? new RuntimeException("An error occurred during execution.") : null);
        this.rowChanges = rowChanges;
        this.data = null;
    }

    public DatabaseResponse(T data) {
        super("", null);
        this.data = data;
        this.rowChanges = 0;
    }

    public boolean hasRowsChanged() {
        return rowChanges > 0;
    }

    public boolean wasSuccessful() {
        return rowChanges > FAIL;
    }
}
