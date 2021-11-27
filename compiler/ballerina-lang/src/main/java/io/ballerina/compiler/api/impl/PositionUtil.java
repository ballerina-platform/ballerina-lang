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
        if (symbolPosition == null) {
            return false;
        }

        int startLine = symbolPosition.lineRange().startLine().line();
        int endLine = symbolPosition.lineRange().endLine().line();
        int startColumn = symbolPosition.lineRange().startLine().offset();
        int endColumn = symbolPosition.lineRange().endLine().offset();
        int cursorLine = cursorPos.line();
        int cursorColumn = cursorPos.offset();

        // Eliminates the cases where the cursor falls outside of a block
        // 1) The line the cursor is at is outside of either the starting line or the ending line of the block
        // 2) If the cursor is at the same line as the starting line, see if the starting column is ahead of the
        // cursor's column.
        // 3) If the cursor is at the same line as the ending line, see if the ending column is before the cursor's
        // column. If the cursor's column is the same as the ending column, it is still considered as outside of the
        // block.
        if ((cursorLine < startLine || cursorLine > endLine)
                || cursorLine == startLine && cursorColumn < startColumn
                || cursorLine == endLine && cursorColumn >= endColumn) {
            return false;
        }

        // 4) The above scenarios are the only cases where the cursor can be outside the block. Any other location is
        // within the block.
        return true;
    }

    // Checks whether the specified range falls within the node
    static boolean withinRange(LineRange specifiedRange, Location nodePosition) {

        //TODO: Remove this check
        if (nodePosition == null) {
            return false;
        }

        if (!nodePosition.lineRange().filePath().equals(specifiedRange.filePath())) {
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

    //  todo to be removed once https://github.com/ballerina-platform/ballerina-lang/discussions/28983 is sorted
    static boolean withinRightInclusive(LinePosition cursorPos, Location nodePosition) {
        int startLine = nodePosition.lineRange().startLine().line();
        int startColumn = nodePosition.lineRange().startLine().offset();
        int endLine = nodePosition.lineRange().endLine().line();

        int cursorLine = cursorPos.line();
        int cursorColumn = cursorPos.offset();
        return (startLine < cursorLine && endLine > cursorLine)
                || (startLine < cursorLine && endLine == cursorLine)
                || ((startLine == cursorLine && endLine > cursorLine)
                || (startLine == endLine && startLine == cursorLine)
                && startColumn <= cursorColumn);
    }
}
