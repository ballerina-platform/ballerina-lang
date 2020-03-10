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
package io.ballerinalang.compiler.internal.parser.incremental;

import io.ballerinalang.compiler.internal.parser.NodePointer;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.text.TextEdit;
import io.ballerinalang.compiler.text.TextRange;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.syntax.tree.Span;

import java.util.ArrayDeque;

public class IncrementalTokenProvider extends NodeAndTokenProvider {

    private final BallerinaLexer lexer;
    private NodePointer oldTreeNodePointer;

    private int oldTextOffset;
    private int newTextOffset;
    private ArrayDeque<TextEditRange> textEditRanges;

    /**
     * The current token that the parser is looking at.
     */
    private STToken currentToken;

    // TODO an array of text changes
    public IncrementalTokenProvider(SyntaxTree oldTree, TextDocument newTextDocument, TextDocumentChange textDocumentChange) {
        this.lexer = new BallerinaLexer(newTextDocument.getCharacterReader());
        this.oldTreeNodePointer = new NodePointer(oldTree.getModulePart());
        textEditRanges = markAffectedRanges(oldTree.getModulePart(), textDocumentChange);
    }

    private ArrayDeque<TextEditRange> markAffectedRanges(ModulePart oldTree, TextDocumentChange textDocumentChange) {
        int textEditCount = textDocumentChange.getTextEditCount();
        ArrayDeque<TextEditRange> markedTextEdits = new ArrayDeque<>(textEditCount);
        for (int index = textEditCount - 1; index >= 0; index--) {
            TextEdit textEdit = textDocumentChange.getTextEdit(index);
            markedTextEdits.push(markAffectedRange(oldTree, textEdit));
        }
        return markedTextEdits;
    }

    private TextEditRange markAffectedRange(ModulePart oldTree, TextEdit textEdit) {
        TextRange textRange = textEdit.range();
        // Find the affected token in the oldTree
        Token affectedToken = oldTree.findToken(textRange.startOffset());
        // If this is the first toke, then return
        if (affectedToken.getSpanWithMinutiae().startOffset() == 0) {
            return new TextEditRange(textRange.startOffset(), textRange.endOffset(), textEdit.text().length());
        }

        STToken internalToken = (STToken) affectedToken.getInternalNode();
        for (int lbIndex = 0; lbIndex < internalToken.lookback; lbIndex++) {
            // Since the common lookback = 1, this loop runs only once
            // TODO Find a better way to get the previous tokens
            // TODO how about a previousToken() method in NodePointer?
            affectedToken = oldTree.findToken(affectedToken.getSpanWithMinutiae().startOffset() - 1);
        }

        int affectedTokenStartOffset = affectedToken.getSpanWithMinutiae().startOffset();
        int newTextLength = textEdit.text().length() +
                (textRange.startOffset() - affectedTokenStartOffset);
        return new TextEditRange(affectedTokenStartOffset, textRange.endOffset(), newTextLength);
    }

    @Override
    public STToken getCurrentToken() {
        if (currentToken != null) {
            return currentToken;
        }

        nextToken();
        return currentToken;
    }

    @Override
    public STToken consumeToken(SyntaxKind expectedKind) {
        if (currentToken == null) {
            nextToken();
        }

        STToken foundToken = currentToken;
        if (currentToken.kind == expectedKind) {
            currentToken = null;
            return foundToken;
        }

        // Create a missing token and report an error
        return createMissingToken(expectedKind, currentToken.kind, true);
    }

    @Override
    public STToken peekToken(int tokenIndex) {
        return null;
    }

    @Override
    public STNode getCurrentNode() {
        return null;
    }

    @Override
    public STNode consumeNode() {
        return null;
    }

    private void nextToken() {
        currentToken = nextTokenInternal();

        //        oldTreeNodePointer = oldTreeNodePointer.nextToken();
//        currentToken = (STToken) oldTreeNodePointer.currentToken().getInternalNode();
//        tokens.add(currentToken);
//        tokenCount++;
    }

    // I
    // type f111() {}
    // type bawerw() { }
    //      I = 6 + 3
    private STToken nextTokenInternal() {

        while (true) {
            // We are at the end of the old token stream
            if (oldTreeNodePointer.isAtEOF()) {
                // Lex tokens from the new text stream
                return lexNewToken();
            }

            if (oldTextOffset > newTextOffset) {
                // We need to lex a token from the new text stream and see whether both offsets are aligned
                return lexNewToken();
            } else if (oldTextOffset < newTextOffset) {
                // We need to skip a token from the old tree and see weather both offsets are aligned
                throw new UnsupportedOperationException("oldTextOffset < newTextOffset is not supported yet");
            } else {

                // Both oldTextOffset and the newTextOffset are both at the same startOffset.
                // Let's try to reuse a token
                STToken oldToken = reuseOldToken();
                if (oldToken != null) {
                    return oldToken;
                }
            }
        }
    }

    private STToken lexNewToken() {
        lexer.reset(newTextOffset);
        STToken syntaxToken = lexer.lexSyntaxToken();
        newTextOffset += syntaxToken.width();
//        System.out.println("Relexed: " + syntaxToken);
        return syntaxToken;
    }

    private STToken reuseOldToken() {
        oldTreeNodePointer = oldTreeNodePointer.nextToken();
        Token token = oldTreeNodePointer.currentToken();
        int tokenWidth = token.getSpanWithMinutiae().width();
        oldTextOffset += tokenWidth;
        if (isReusable(token)) {
//            System.out.println("Reused: " + token);
            newTextOffset += tokenWidth;
            return (STToken) token.getInternalNode();
        }
//        System.out.println("Not Reused: " + token);


        // TODO Check whether oldNode pointer is after the current text edit.
        //  if so update the oldTextOffset accordingly.
        removeInvalidTextEdits(token);
        return null;
    }

    private void removeInvalidTextEdits(Token oldToken) {
        if (textEditRanges.isEmpty()) {
            return;
        }
        int nextOldTokenStartOffset = oldToken.getSpanWithMinutiae().endOffset();
        TextEditRange textEditRange = textEditRanges.peek();
        if (nextOldTokenStartOffset < textEditRange.oldEndOffset) {
            return;
        }

        textEditRange = textEditRanges.pop();
        oldTextOffset += textEditRange.newTextLength - textEditRange.oldLength;

    }

    private boolean isReusable(Token token) {
        if (token.getInternalNode().width() == 0) {
            return false;
        }

        return noOverlapWithCurrentTextEdit(token);
    }

    private boolean noOverlapWithCurrentTextEdit(Token oldToken) {
        if (textEditRanges.isEmpty()) {
            return true;
        }

        TextEditRange textEditRange = textEditRanges.peek();
        Span oldTokenSpan = oldToken.getSpanWithMinutiae();

        // Does not overlap if => editStart < editEnd < oldTokenStart || oldTokenEnd < editStart < editEnd
        // Can be simplified to => editEnd < oldTokenStart || oldTokenEnd < editStart
        return textEditRange.oldEndOffset < oldTokenSpan.startOffset() ||
                oldTokenSpan.endOffset() < textEditRange.oldStartOffset;
    }

    private static class TextEditRange {
        final int oldStartOffset;
        final int oldEndOffset;
        final int oldLength;
        final int newTextLength;

        TextEditRange(int oldStartOffset, int oldEndOffset, int newTextLength) {
            this.oldStartOffset = oldStartOffset;
            this.oldEndOffset = oldEndOffset;
            this.oldLength = oldEndOffset - oldStartOffset;
            this.newTextLength = newTextLength;
        }
    }
}
