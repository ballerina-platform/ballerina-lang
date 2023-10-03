package org.ballerinalang.formatter.core.options;

import org.ballerinalang.formatter.core.FormatterException;

public enum WrappingMethod {
    ChopDown,
    Wrap,
    NoWrap;

    public static WrappingMethod fromString(String value) throws FormatterException {
        try {
            return WrappingMethod.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new FormatterException("Invalid wrapping method: " + value);
        }
    }
}
