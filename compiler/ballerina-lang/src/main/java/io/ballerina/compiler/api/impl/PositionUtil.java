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

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

/**
 * A class for holding the common utilities related to positions.
 *
 * @since 2.0.0
 */
class PositionUtil {

    static boolean withinBlock(LinePosition cursorPos, Location symbolPosition) {
        /*
        Temporary null check added
         */
        if (symbolPosition == null) {
            return false;
        }
        int startLine = symbolPosition.lineRange().startLine().line();
        int endLine = symbolPosition.lineRange().endLine().line();
        int startColumn = symbolPosition.lineRange().startLine().offset();
        int endColumn = symbolPosition.lineRange().endLine().offset();
        int cursorLine = cursorPos.line();
        int cursorColumn = cursorPos.offset();

        return (startLine < cursorLine && endLine > cursorLine)
                || (startLine < cursorLine && endLine == cursorLine && endColumn > cursorColumn)
                || (startLine == cursorLine && startColumn < cursorColumn && endLine > cursorLine)
                || (startLine == endLine && startLine == cursorLine
                && startColumn <= cursorColumn && endColumn > cursorColumn);
    }

    static boolean withinRange(LineRange specifiedRange, Location nodePosition) {
        /*
        Temporary null check added
         */
        if (nodePosition == null || !nodePosition.lineRange().filePath().equals(specifiedRange.filePath())) {
            return false;
        }

        int nodeStartLine = nodePosition.lineRange().startLine().line();
        int nodeStartColumn = nodePosition.lineRange().startLine().offset();
        int nodeEndLine = nodePosition.lineRange().endLine().line();
        int nodeEndColumn = nodePosition.lineRange().endLine().offset();

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
