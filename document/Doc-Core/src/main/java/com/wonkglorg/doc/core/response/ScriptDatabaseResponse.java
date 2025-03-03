package com.wonkglorg.doc.core.response;

import com.wonkglorg.doc.core.objects.RepoId;

/**
 * A response for a script database operation, indicating its success or failure with an optional message for more information
 */
public class ScriptDatabaseResponse extends Response {

    protected ScriptDatabaseResponse(RepoId causingRepo, String response, Exception exception) {
        super(causingRepo, response, exception);
    }

    /**
     * Constructs a new Success response
     *
     * @param <T>
     * @return the constructed response
     */
    public static ScriptDatabaseResponse success(RepoId causingRepo) {
        return new ScriptDatabaseResponse(causingRepo, null, null);
    }

    /**
     * Constructs a new Success response
     *
     * @param <T>
     * @return the constructed response
     */
    public static ScriptDatabaseResponse success(RepoId causingRepo, String message) {
        return new ScriptDatabaseResponse(causingRepo, message, null);
    }

    /**
     * Constructs a new Failed response with the causing exception
     *
     * @param e   the exception that causes the error
     * @param <T>
     * @return the constructed reponse
     */
    public static ScriptDatabaseResponse fail(RepoId causingRepo, Exception e) {
        return new ScriptDatabaseResponse(causingRepo, null, e);
    }


}
