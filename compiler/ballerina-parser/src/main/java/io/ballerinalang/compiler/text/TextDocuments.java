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
 * Contains a set of helper methods.
 */
public class TextDocuments {

    private TextDocuments() {
    }

    public static TextDocument from(String text) {
        return new StringTextDocument(text);
    }
//
//    public static TextDocument from(TextDocument oldTextDocument, TextEdit[] textEdits) {
//        // This algorithm assumes that all the textRanges in edits are ordered and there are no overlaps.
//        // TODO improve this algorithm to handle overlapping edits as well as unordered edits.
//        // TODO This is a very simple implementation. Improve this if necessary
//
//        // TODO this algorithm does not check bounds. FIXME
//        // TODO `prevTextDocument.toString()` is a hack. Figure out a way to work with TextDocument abstraction
//        String oldText = oldTextDocument.toString();
//        StringBuilder newTextBuilder = new StringBuilder();
//        int beginIndex = 0;
//        for (TextEdit textEdit : textEdits) {
//            newTextBuilder.append(oldText, beginIndex, textEdit.span().startOffset());
//            newTextBuilder.append(textEdit.text());
//            beginIndex = textEdit.span().endOffset();
//        }
//
//        return new StringTextDocument(newTextBuilder.toString());
//    }
//
//    public static TextEdit createTextEdit(TextDocument textDocument, TextRange textRange, String newText) {
//        TextLine[] textLines = textDocument.textLines;
//        TextLine startLine = textLines[textRange.start().line()];
//        int startOffset = startLine.getLineSpan().startOffset() + textRange.start().offset();
//
//        TextLine endLine = textLines[textRange.end().line()];
//        int endOffset = endLine.getLineSpan().startOffset() + textRange.end().offset();
//
//        return new TextEdit(textRange, new SourcePartSpan(startOffset, endOffset - startOffset), newText);
//    }
}
