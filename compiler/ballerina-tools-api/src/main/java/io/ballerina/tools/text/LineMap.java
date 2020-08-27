/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.tools.text;

/**
 * The {@code LineMap} represents a collection text lines in the {@code TextDocument}.
 *
 * @since 2.0.0
 */
class LineMap {
    private final TextLine[] textLines;
    private final int length;

    LineMap(TextLine[] textLines) {
        this.textLines = textLines;
        this.length = textLines.length;
    }

    TextLine textLine(int line) {
        lineRangeCheck(line);
        return textLines[line];
    }

    LinePosition linePositionFrom(int position) {
        positionRangeCheck(position);
        TextLine textLine = findLineFrom(position);
        return LinePosition.from(textLine.lineNo(), position - textLine.startOffset());
    }

    int textPositionFrom(LinePosition linePosition) {
        lineRangeCheck(linePosition.line());
        TextLine textLine = textLines[linePosition.line()];
        if (textLine.length() < linePosition.offset()) {
            throw new IllegalArgumentException("Cannot find a line with the character offset '" +
                    linePosition.offset() + "'");
        }

        return textLine.startOffset() + linePosition.offset();
    }

    private void positionRangeCheck(int position) {
        if (position < 0 || position > textLines[length - 1].endOffset()) {
            throw new IndexOutOfBoundsException("Index: '" + position + "', Size: '" +
                    textLines[length - 1].endOffset() + "'");
        }
    }

    private void lineRangeCheck(int lineNo) {
        if (lineNo < 0 || lineNo > length) {
            throw new IndexOutOfBoundsException("Line number: '" + lineNo + "', Size: '" + length + "'");
        }
    }

    /**
     * Return the {@code TextLine} to which the given position belongs to.
     * <p>
     * Perform a binary search to find the matching text line.
     *
     * @param position of the source text
     * @return the {@code TextLine} to which the given position belongs to
     */
    private TextLine findLineFrom(int position) {
        // Check boundary conditions
        if (position == 0) {
            return textLines[0];
        } else if (position == textLines[length - 1].endOffset()) {
            return textLines[length - 1];
        }

        TextLine foundTextLine = null;
        int left = 0;
        int right = length - 1;
        while (left <= right) {
            // Using >>> handle the case when the sum of left and right is greater than
            // the maximum positive int value (2^31 - 1)
            // FYI: https://ai.googleblog.com/2006/06/extra-extra-read-all-about-it-nearly.html
            int middle = (left + right) >>> 1;
            int startOffset = textLines[middle].startOffset();
            int endOffset = textLines[middle].endOffsetWithNewLines();
            if (startOffset <= position && position < endOffset) {
                foundTextLine = textLines[middle];
                break;
            } else if (endOffset <= position) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }
        return foundTextLine;
    }
}
