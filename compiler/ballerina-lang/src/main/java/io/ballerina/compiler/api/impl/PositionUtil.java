/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.util.diagnostic.Diagnostic;

/**
 * A class for holding the common utilities related to positions.
 *
 * @since 2.0.0
 */
class PositionUtil {

    static boolean withinBlock(LinePosition cursorPos, Diagnostic.DiagnosticPosition symbolPosition) {
        int startLine = symbolPosition.getStartLine();
        int endLine = symbolPosition.getEndLine();
        int startColumn = symbolPosition.getStartColumn();
        int endColumn = symbolPosition.getEndColumn();
        int cursorLine = cursorPos.line();
        int cursorColumn = cursorPos.offset();

        return (startLine < cursorLine && endLine > cursorLine)
                || (startLine < cursorLine && endLine == cursorLine && endColumn > cursorColumn)
                || (startLine == cursorLine && startColumn < cursorColumn && endLine > cursorLine)
                || (startLine == endLine && startLine == cursorLine
                && startColumn <= cursorColumn && endColumn > cursorColumn);
    }

    static boolean withinRange(LineRange specifiedRange, Diagnostic.DiagnosticPosition nodePosition) {
        int nodeStartLine = nodePosition.getStartLine();
        int nodeStartColumn = nodePosition.getStartColumn();
        int nodeEndLine = nodePosition.getEndLine();
        int nodeEndColumn = nodePosition.getEndColumn();

        int specifiedStartLine = specifiedRange.startLine().line();
        int specifiedStartColumn = specifiedRange.startLine().offset();
        int specifiedEndLine = specifiedRange.endLine().line();
        int specifiedEndColumn = specifiedRange.endLine().offset();

        if (specifiedStartLine < nodeStartLine || specifiedEndLine > nodeEndLine) {
            return false;
        }

        if (specifiedStartLine > nodeStartLine && specifiedEndLine < nodeEndLine) {
            return true;
        }

        if (specifiedStartLine == nodeStartLine && specifiedEndLine == nodeEndLine) {
            return specifiedStartColumn >= nodeStartColumn && specifiedEndColumn <= nodeEndColumn;
        }

        if (specifiedStartLine == nodeStartLine) {
            return specifiedStartColumn >= nodeStartColumn;
        }

        return specifiedEndColumn <= nodeEndColumn;
    }
}
