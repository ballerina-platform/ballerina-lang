package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.values.BError;

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
