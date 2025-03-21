package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.values.BError;

/*
    * This class represents the Exception in query execution.
 */
public class QueryException extends RuntimeException {
    private BError error;

    public QueryException(BError error) {
        this.error = error;
    }

    public BError getError() {
        return error;
    }

    public void setError(BError error) {
        this.error = error;
    }
}
