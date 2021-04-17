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

package io.ballerina.syntaxapicallsgen.segment;

/**
 * Segment which encompasses a String.
 * So, the string needs to be escaped and surrounded with quotations.
 * Eg: "\"Hello World\\n\""
 *
 * @since 2.0.0
 */
public class StringSegment extends CodeSegment {
    private static final String BACKSLASH_CHAR = "\\";
    private static final String TAB_CHAR = "\t";
    private static final String BACKSPACE_CHAR = "\b";
    private static final String NEWLINE_CHAR = "\n";
    private static final String CAR_RETURN_CHAR = "\r";
    private static final String FORM_FEED_CHAR = "\f";
    private static final String SINGLE_QUOTE_CHAR = "'";
    private static final String DOUBLE_QUOTE_CHAR = "\"";

    private static final String ESCAPED_BACKSLASH_CHAR = "\\\\";
    private static final String ESCAPED_TAB_CHAR = "\\t";
    private static final String ESCAPED_BACKSPACE_CHAR = "\\b";
    private static final String ESCAPED_NEWLINE_CHAR = "\\n";
    private static final String ESCAPED_CAR_RETURN_CHAR = "\\r";
    private static final String ESCAPED_FORM_FEED_CHAR = "\\f";
    private static final String ESCAPED_SINGLE_QUOTE_CHAR = "\\'";
    private static final String ESCAPED_DOUBLE_QUOTE_CHAR = "\\\"";

    public StringSegment(String content) {
        super(content);
    }

    @Override
    public StringBuilder stringBuilder() {
        String escapedString = super.content
                .replace(BACKSLASH_CHAR, ESCAPED_BACKSLASH_CHAR)
                .replace(TAB_CHAR, ESCAPED_TAB_CHAR)
                .replace(BACKSPACE_CHAR, ESCAPED_BACKSPACE_CHAR)
                .replace(NEWLINE_CHAR, ESCAPED_NEWLINE_CHAR)
                .replace(CAR_RETURN_CHAR, ESCAPED_CAR_RETURN_CHAR)
                .replace(FORM_FEED_CHAR, ESCAPED_FORM_FEED_CHAR)
                .replace(SINGLE_QUOTE_CHAR, ESCAPED_SINGLE_QUOTE_CHAR)
                .replace(DOUBLE_QUOTE_CHAR, ESCAPED_DOUBLE_QUOTE_CHAR);
        return new StringBuilder()
                .append(DOUBLE_QUOTE_CHAR)
                .append(escapedString)
                .append(DOUBLE_QUOTE_CHAR);
    }
}
