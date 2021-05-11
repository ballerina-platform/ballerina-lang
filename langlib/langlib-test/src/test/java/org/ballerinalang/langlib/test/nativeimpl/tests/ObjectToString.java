package org.ballerinalang.langlib.test.nativeimpl.tests;

import io.ballerina.runtime.api.utils.StringUtils;

public class ObjectToString {

    public static String objectToJString(Object value) {
        if (value == null) {
            return "";
        }
        StringBuilder content = new StringBuilder();
        content.append(StringUtils.getStringValue(value, null));
        return content.toString();
    }
}
