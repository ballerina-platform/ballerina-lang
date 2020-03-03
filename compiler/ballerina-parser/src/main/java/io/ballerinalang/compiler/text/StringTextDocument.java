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

import io.ballerinalang.compiler.internal.parser.CharacterReader;
import io.ballerinalang.compiler.text.TextLineMap.TextLine;

public class StringTextDocument extends TextDocument {
    private final String text;

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
    // TODO Can we find a better alternative? This is the first approach that occurred to me ;-)
    protected TextLine[] populateTextLineMap() {
        String[] lines = text.split("\\R", -1);
        TextLine[] textLines = new TextLine[lines.length];
        int startingOffset = 0;
        for (int line = 0; line < lines.length; line++) {
            String text = lines[line];
            int length = text.length();
            textLines[line] = new TextLine(line, new TextRange(startingOffset, length), text);
            startingOffset += length;
        }
        return textLines;
    }

    @Override
    public CharacterReader getCharacterReader() {
        return CharacterReader.fromString(text);
    }

    public String toString() {
        return text;
    }
}
