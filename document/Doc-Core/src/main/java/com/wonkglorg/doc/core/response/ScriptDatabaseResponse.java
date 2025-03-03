package com.wonkglorg.doc.core.response;

/**
 * A response for a script database operation, indicating its success or failure with an optional message for more information
 */
public class ScriptDatabaseResponse extends Response {

    protected ScriptDatabaseResponse(String response, Exception exception) {
        super(response, exception);
    }

    /**
     * Constructs a new Success response
     *
     * @param <T>
     * @return the constructed response
     */
    public static ScriptDatabaseResponse success() {
        return new ScriptDatabaseResponse(null, null);
    }

    /**
     * Constructs a new Success response
     *
     * @param <T>
     * @return the constructed response
     */
    public static ScriptDatabaseResponse success(String message) {
        return new ScriptDatabaseResponse(message, null);
    }

    /**
     * Constructs a new Failed response with the causing exception
     *
     * @param e   the exception that causes the error
     * @param <T>
     * @return the constructed reponse
     */
    public static ScriptDatabaseResponse fail(Exception e) {
        return new ScriptDatabaseResponse(null, e);
    }


}
