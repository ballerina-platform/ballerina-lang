/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.utils;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility functions required by invokers.
 * Static class.
 *
 * @since 2.0.0
 */
public class StringUtils {
    private static final int MAX_VAR_STRING_LENGTH = 78;
    private static final String QUOTE = "'";
    private static final String SPACE = " ";
    private static final String CARET = "^";
    private static final String DASH = "-";

    /**
     * Creates an quoted identifier to use for variable names.
     *
     * @param identifier Identifier without quote.
     * @return Quoted identifier.
     */
    public static String quoted(String identifier) {
        if (String.valueOf(identifier).startsWith(QUOTE)) {
            return identifier;
        }
        return QUOTE + identifier;
    }

    /**
     * Short a string to a certain length.
     *
     * @param input Input string to shorten.
     * @return Shortened string.
     */
    public static String shortenedString(Object input) {
        String value = String.valueOf(input);
        value = value.replace("\n", "");
        if (value.length() > MAX_VAR_STRING_LENGTH) {
            int subStrLength = MAX_VAR_STRING_LENGTH / 2;
            return value.substring(0, subStrLength)
                    + "..." + value.substring(value.length() - subStrLength);
        }
        return value;
    }

    /**
     * Highlight and show the error position.
     * The highlighted text would follow format,
     * <pre>
     * incompatible types: expected 'string', found 'int'
     *     int i = "Hello";
     *             ^-----^
     * </pre>
     * However if the error is multiline, it would not highlight the error.
     *
     * @param textDocument Text document to extract source code.
     * @param diagnostic   Diagnostic to show.
     * @return The string with position highlighted.
     */
    public static String highlightDiagnostic(TextDocument textDocument,
                                             io.ballerina.tools.diagnostics.Diagnostic diagnostic) {
        LineRange lineRange = diagnostic.location().lineRange();
        LinePosition startLine = lineRange.startLine();
        LinePosition endLine = lineRange.endLine();

        if (startLine.line() != endLine.line()) {
            // Error spans for several lines, will not highlight error
            StringJoiner errorMessage = new StringJoiner("\n\t");
            errorMessage.add(diagnostic.message());
            for (int i = startLine.line(); i <= endLine.line(); i++) {
                errorMessage.add(textDocument.line(i).text().strip());
            }
            return errorMessage.toString();
        }

        // Error is same line, can highlight using ^-----^
        // Error will expand as ^, ^^, ^-^, ^--^
        int position = startLine.offset();
        int length = Math.max(endLine.offset() - position, 1);
        String caretUnderline = length == 1
                ? CARET : CARET + DASH.repeat(length - 2) + CARET;

        // Get the source code
        String sourceLine = textDocument.line(startLine.line()).text();

        // Count leading spaces
        int leadingSpaces = sourceLine.length() - sourceLine.stripLeading().length();
        String strippedSourceLine = sourceLine.substring(leadingSpaces);

        // Result should be padded with a tab
        return String.format("error: %s%n\t%s%n\t%s%s", diagnostic.message(), strippedSourceLine,
                SPACE.repeat(position - leadingSpaces), caretUnderline);
    }

    /**
     * Replace the unicode patterns in identifiers into respective unicode characters.
     *
     * @param identifier identifier string
     * @return modified identifier with unicode character
     */
    public static String unescapeUnicodeCodepoints(String identifier) {
        return Utils.unescapeUnicodeCodepoints(identifier);
    }

    /**
     * Escapes the <code>String</code> with the escaping rules of Java language
     * string literals, so it's safe to insert the value into a string literal.
     * The resulting string will not be quoted.
     *
     * @param string String to encode.
     * @return encoded string.
     */
    public static String encodeIdentifier(String string) {
        return string.replace("\\", "\\\\");
    }

    /**
     * Converts a Ballerina object to its `toBalString` counterpart.
     *
     * @param object Object to convert.
     * @return Converted string.
     */
    public static String getExpressionStringValue(Object object) {
        return io.ballerina.runtime.api.utils.StringUtils.getExpressionStringValue(object);
    }

    /**
     * Converts {@link Throwable} to a more descriptive format.
     *
     * @param error Error object to convert.
     * @return Converted string.
     */
    public static String getErrorStringValue(Throwable error) {
        if (error instanceof BError bError) {
            return bError.getErrorMessage() + " " + bError.getDetails();
        }
        return error.getMessage();
    }

    /**
     * Convert unicode character to ballerina unicode format.
     *
     * @param character Unicode character to convert.
     * @return Reformatted unicode string.
     */
    public static String convertUnicode(char character) {
        return "\\u{" + Integer.toHexString(character) + "}";
    }

    /**
     * Replace ballerina unicode format codes with their unicode character
     * for a given string.
     *
     * @param toConvert String need to convert.
     * @return Reformatted string.
     */
    public static String convertUnicodeToCharacter(String toConvert) {
        Matcher matcher = Pattern.compile("\\\\u\\{([\\da-fA-F]*)}").matcher(toConvert);
        StringBuilder stringBuilder = new StringBuilder();
        int currentPosition = 0;
        while (matcher.find()) {
            stringBuilder.append(toConvert, currentPosition, matcher.start());
            int code = Integer.parseInt(matcher.group(1), 16);
            stringBuilder.append(Character.toChars(code));
            currentPosition = matcher.end();
        }

        stringBuilder.append(toConvert.substring(currentPosition));
        return stringBuilder.toString();
    }
}
