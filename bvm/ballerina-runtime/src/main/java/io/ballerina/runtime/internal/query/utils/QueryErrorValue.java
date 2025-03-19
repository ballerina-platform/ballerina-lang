package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.ErrorValue;

public class QueryErrorValue extends ErrorValue {
    public QueryErrorValue(BString message) {
        super(message);
    }
}
