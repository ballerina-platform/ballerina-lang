package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

public class QueryConstants {
    public static final BString VALUE_FIELD = StringUtils.fromString("value");
    public static final BString $ERROR$_FIELD = StringUtils.fromString("$error$");
    public static final BString $VALUE$_FIELD = StringUtils.fromString("$value$");
}
