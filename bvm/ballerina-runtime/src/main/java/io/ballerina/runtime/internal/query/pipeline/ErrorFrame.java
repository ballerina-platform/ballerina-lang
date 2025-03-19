package io.ballerina.runtime.internal.query.pipeline;

import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

public class ErrorFrame extends Frame {

    private final BError error;

    private ErrorFrame(BError error) {
        this.error = error;
    }

    public static ErrorFrame from(BError error) {
        return new ErrorFrame(error);
    }

    public BError getError() {
        return error;
    }

    @Override
    public BMap<BString, Object> getRecord() {
        return null;
    }
}
