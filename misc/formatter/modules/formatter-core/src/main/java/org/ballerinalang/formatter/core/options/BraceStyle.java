package org.ballerinalang.formatter.core.options;

import org.ballerinalang.formatter.core.FormatterException;

public enum BraceStyle {
    NEWLINE,
    ENDOFLINE;

    public static BraceStyle fromString(String value) throws FormatterException {
        try {
            return BraceStyle.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new FormatterException("Invalid Brace style: " + value);
        }
    }
}
