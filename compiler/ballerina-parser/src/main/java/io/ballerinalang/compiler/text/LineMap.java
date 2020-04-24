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
package io.ballerinalang.compiler.text;

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

    LinePosition lineFrom(int position) {
        positionRangeCheck(position);
        TextLine textLine = findLineFrom(position);
        return new LinePosition(textLine.lineNo(), position - textLine.startOffset());
    }

    int positionFrom(LinePosition linePosition) {
        lineRangeCheck(linePosition.line());
        TextLine textLine = textLines[linePosition.line()];
        if (textLine.length() < linePosition.offset()) {
            throw new IllegalArgumentException("Cannot find a line with the character offset '" +
                    linePosition.offset() + "'");
        }

        return textLine.startOffset + linePosition.offset();
    }

    private void positionRangeCheck(int position) {
        if (position < 0 || position > textLines[length - 1].endOffset) {
            throw new IndexOutOfBoundsException("Index: '" + position + "', Size: '" +
                    textLines[length - 1].endOffset + "'");
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
        TextLine foundTextLine = null;
        int left = 0;
        int right = length - 1;
        while (left <= right) {
            int middle = (left + right) / 2;
            if (textLines[middle].startOffset <= position && position <= textLines[middle].endOffset) {
                foundTextLine = textLines[middle];
                break;
            } else if (textLines[middle].endOffset < position) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }
        return foundTextLine;
    }

    /**
     * A representation of a single line in the {@code TextDocument}.
     *
     * @since 2.0.0
     */
    static class TextLine {
        /**
         * The span of the line within the {@code TextDocument}
         */
        private final int startOffset;
        private final int endOffset;
        private final int lengthOfNewLineChars;

        private final int lineNo;

        TextLine(int lineNo, int startOffset, int endOffset, int lengthOfNewLineChars) {
            this.lineNo = lineNo;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.lengthOfNewLineChars = lengthOfNewLineChars;
        }

        int lineNo() {
            return lineNo;
        }

        int startOffset() {
            return startOffset;
        }

        int getEndOffset() {
            return endOffset;
        }

        int length() {
            return endOffset - startOffset;
        }

        int lengthWithNewLineChars() {
            return endOffset - startOffset + lengthOfNewLineChars;
        }
    }
}
