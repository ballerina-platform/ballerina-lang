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

import io.ballerinalang.compiler.internal.parser.CharReader;

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
    public CharReader getCharacterReader() {
        return CharReader.fromString(text);
    }

    public String toString() {
        return text;
    }

    private TextLine[] calculateTextLines() {
        // TODO Can we find a better alternative? This is the first approach that occurred to me ;-)
        // TODO Here we are considering both \r and \n characters, but for now lengthOfNewLineChars=1
        int lengthOfNewLineChars;
        int startOffset = 0;
        String[] strLines = text.split("\\r?\\n", -1);
        int noOfLines = strLines.length;
        TextLine[] textLines = new TextLine[noOfLines];
        for (int index = 0; index < noOfLines; index++) {
            lengthOfNewLineChars = isLastLine(index, noOfLines) ? 0 : 1;
            String strLine = strLines[index];
            int endOffset = startOffset + strLine.length();
            textLines[index] = new TextLine(index, strLine, startOffset, endOffset, lengthOfNewLineChars);
            startOffset = endOffset + lengthOfNewLineChars;
        }
        return textLines;
    }

    private boolean isLastLine(int index, int noOfLines) {
        return index == (noOfLines - 1);
    }
}
