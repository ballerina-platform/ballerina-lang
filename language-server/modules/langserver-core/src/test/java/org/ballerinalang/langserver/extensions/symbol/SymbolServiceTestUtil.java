/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions.symbol;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

/**
 * Provides util methods for testing symbol service apis.
 */
public class SymbolServiceTestUtil {

    public static final String INTEGER = "int";
    public static final String STRING = "string";
    public static final String FLOAT = "float";
    public static final String RECORD = "record";
    public static final String ARRAY = "array";
    public static final String UNION = "union";
    public static final String ERROR = "error";
    public static final String NULL = "()";
    public static final String INTERSECTION = "intersection";
    public static final String READ_ONLY = "readonly";

    public static boolean isPositionsEquals(LinePosition expectedPosition, LinePosition actualPosition) {
        return expectedPosition.line() == actualPosition.line()
                && expectedPosition.offset() == actualPosition.offset();
    }

    public static LineRange getExpressionRange(int startLine, int startColumn, int endLine, int endColumn) {
        LinePosition start = LinePosition.from(startLine, startColumn);
        LinePosition end = LinePosition.from(endLine, endColumn);
        return LineRange.from(null, start, end);
    }

    public static boolean isRangesEquals(LineRange expectedRange, LineRange actualRange) {
        return expectedRange.startLine().line() == actualRange.startLine().line()
                && expectedRange.startLine().offset() == actualRange.startLine().offset()
                && expectedRange.endLine().line() == actualRange.endLine().line()
                && expectedRange.endLine().line() == actualRange.endLine().line();
    }
}
