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

public class TextLineMap {

    protected TextLine[] textLines;


    public int lineCount() {
        return textLines.length;
    }

    public int lineStartOffset(int line) {
        return textLines[line].lineSpan.startOffset();
    }

    public int lineEndOffset(int line) {
        return textLines[line].lineSpan.endOffset();
    }

    public int lineLength(int line) {
        return textLines[line].lineSpan.length();
    }

    public TextPosition positionFromOffset(int offset) {
        throw new UnsupportedOperationException();
    }

    public int offsetFromPosition(TextPosition po) {
        return 0;
    }




    /**
     * A representation of a single line in the {@code TextDocument}.
     */
    static class TextLine {
        /**
         * The zeo-based line number.
         */
        private final int line;

        /**
         * The span of the line within the {@code TextDocument}
         */
        private final TextRange lineSpan;

        private final String text;

        public TextLine(int line, TextRange lineSpan, String text) {
            this.line = line;
            this.lineSpan = lineSpan;
            this.text = text;
        }

        public int getLine() {
            return line;
        }

        public TextRange getLineSpan() {
            return lineSpan;
        }

        public String getText() {
            return text;
        }
    }
}
