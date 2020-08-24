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

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code StringTextDocument} represents a {@code TextDocument} created with a string.
 *
 * @since 2.0.0
 */
class StringTextDocument extends TextDocument {
    private final String text;
    private LineMap textLineMap;

    StringTextDocument(String text) {
        this.text = text;
    }

    @Override
    public TextDocument apply(TextDocumentChange textDocumentChange) {
        int startOffset = 0;
        StringBuilder sb = new StringBuilder();
        int textEditCount = textDocumentChange.getTextEditCount();
        for (int i = 0; i < textEditCount; i++) {
            TextEdit textEdit = textDocumentChange.getTextEdit(i);
            TextRange textRange = textEdit.range();
            sb.append(text, startOffset, textRange.startOffset());
            sb.append(textEdit.text());
            startOffset = textRange.endOffset();
        }
        sb.append(text, startOffset, text.length());
        return new StringTextDocument(sb.toString());
    }

    @Override
    protected LineMap populateTextLineMap() {
        if (textLineMap != null) {
            return textLineMap;
        }
        textLineMap = new LineMap(calculateTextLines());
        return textLineMap;
    }

    @Override
    public char[] toCharArray() {
        return this.text.toCharArray();
    }

    public String toString() {
        return text;
    }

    private TextLine[] calculateTextLines() {
        int startOffset = 0;
        List<TextLine> textLines = new ArrayList<>();
        StringBuilder lineBuilder = new StringBuilder();
        int index = 0;
        int line = 0;
        int textLength = text.length();
        int lengthOfNewLineChars;
        while (index < textLength) {
            char c = text.charAt(index);
            if (c == '\r' || c == '\n') {
                int nextCharIndex = index + 1;
                if (c == '\r' && textLength != nextCharIndex && text.charAt(nextCharIndex) == '\n') {
                    lengthOfNewLineChars = 2;
                } else {
                    lengthOfNewLineChars = 1;
                }
                String strLine = lineBuilder.toString();
                int endOffset = startOffset + strLine.length();
                textLines.add(new TextLine(line++, strLine, startOffset, endOffset, lengthOfNewLineChars));
                startOffset = endOffset + lengthOfNewLineChars;
                lineBuilder = new StringBuilder();
                index += lengthOfNewLineChars;
            } else {
                lineBuilder.append(c);
                index++;
            }
        }

        String strLine = lineBuilder.toString();
        textLines.add(new TextLine(line, strLine, startOffset, startOffset + strLine.length(), 0));
        return textLines.toArray(new TextLine[0]);
    }
}
