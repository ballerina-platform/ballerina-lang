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

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;

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
        value = value.replaceAll("\n", "");
        if (value.length() > MAX_VAR_STRING_LENGTH) {
            int subStrLength = MAX_VAR_STRING_LENGTH / 2;
            return value.substring(0, subStrLength)
                    + "..." + value.substring(value.length() - subStrLength);
        }
        return value;
    }

    /**
     * Highlight and show the error position.
     *
     * @param textDocument Text document to extract source code.
     * @param diagnostic   Diagnostic to show.
     * @return The string with position highlighted.
     */
    public static String highlightDiagnostic(TextDocument textDocument,
                                             io.ballerina.tools.diagnostics.Diagnostic diagnostic) {
        // Get the source code
        String sourceLine = textDocument.line(diagnostic.location().lineRange().startLine().line()).text();


        LinePosition startLine = diagnostic.location().lineRange().startLine();
        LinePosition endLine = diagnostic.location().lineRange().endLine();

        if (startLine.line() != endLine.line()) {
            // Error spans for line, will not highlight error
            return String.format("%s%n%s", diagnostic.message(), sourceLine);
        }

        // Error is same line, can highlight using ^^^
        int position = startLine.offset();
        int length = endLine.offset() - startLine.offset() + 1;
        return String.format("%s%n%s%n%s%s", diagnostic.message(), sourceLine,
                SPACE.repeat(position), CARET.repeat(length));
    }
}
