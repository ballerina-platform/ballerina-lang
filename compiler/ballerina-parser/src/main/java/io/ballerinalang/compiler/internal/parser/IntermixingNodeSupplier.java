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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.ModulePart;
import io.ballerinalang.compiler.syntax.tree.Span;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.text.TextEdit;
import io.ballerinalang.compiler.text.TextRange;

import java.util.ArrayDeque;

/**
 * This class supplies {@code STNode}s and {@code STToken}s on-demand from
 * the old syntax tree (from previous compilation) as well as from the
 * new (modified) source text.
 * <p>
 * In other words, this class mix together {@code STNode}s from the
 * old syntax tree and new source text.
 * <p>
 * If a particular node in the old syntax tree intersects with a text edit range,
 * then issue a token from the new source text, otherwise return the node
 * from the old tree.
 *
 * @since 1.3.0
 */
public class IntermixingNodeSupplier extends AbstractNodeSupplier {
    private final BallerinaLexer lexer;
    private NodePointer oldTreeNodePointer;

    private int oldTextOffset;
    private int newTextOffset;
    private ArrayDeque<TextEditRange> textEditRanges;

    private static final int BUFFER_SIZE = 20;
    private TokenBuffer tokensAhead = new TokenBuffer(BUFFER_SIZE);
    private STToken currentToken = null;

    // TODO an array of text changes
    public IntermixingNodeSupplier(SyntaxTree oldTree, TextDocument newTextDocument, TextDocumentChange textDocumentChange) {
        this.lexer = new BallerinaLexer(newTextDocument.getCharacterReader());
        this.oldTreeNodePointer = new NodePointer(oldTree.getModulePart());
        textEditRanges = markAffectedRanges(oldTree.getModulePart(), textDocumentChange);
    }

    @Override
    public STToken read() {
        if (this.tokensAhead.size > 0) {
            // cache the head
            this.currentToken = tokensAhead.consume();
            return this.currentToken;
        }

        // cache the head
        this.currentToken = nextToken();
        return this.currentToken;
    }

    @Override
    public STToken peek() {
        if (this.tokensAhead.size > 0) {
            return this.tokensAhead.peek();
        }

        STToken token = nextToken();
        this.tokensAhead.add(token);
        return token;
    }

    @Override
    public STToken peek(int k) {
        STToken nextToken;
        while (this.tokensAhead.size < k) {
            nextToken = nextToken();
            this.tokensAhead.add(nextToken);
        }

        return this.tokensAhead.peek(k);
    }

    @Override
    public STToken head() {
        return this.currentToken;
    }

    private STToken lexNextToken() {
        lexer.reset(newTextOffset);
        STToken syntaxToken = lexer.nextToken();
        newTextOffset += syntaxToken.width();
        return syntaxToken;
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

    private STToken reuseOldToken() {
        oldTreeNodePointer = oldTreeNodePointer.nextToken();
        Token token = oldTreeNodePointer.currentToken();
        int tokenWidth = token.getSpanWithMinutiae().width();
        oldTextOffset += tokenWidth;
        if (isReusable(token)) {
            newTextOffset += tokenWidth;
            return (STToken) token.getInternalNode();
        }

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

    private STToken nextToken() {
        while (true) {
            // We are at the end of the old token stream
            if (oldTreeNodePointer.isAtEOF()) {
                // Lex tokens from the new text stream
                return lexNextToken();
            }

            if (oldTextOffset > newTextOffset) {
                // We need to lex a token from the new text stream and see whether both offsets are aligned
                return lexNextToken();
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

    /**
     * Represents a text range in the old source code that has been
     * modified in the new source code.
     *
     * @since 1.3.0
     */
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

    /**
     * A ring buffer of tokens.
     *
     * @since 1.2.0
     */
    private static class TokenBuffer {

        private final int capacity;
        private final STToken[] tokens;
        private int endIndex = -1;
        private int startIndex = -1;
        private int size = 0;

        TokenBuffer(int size) {
            this.capacity = size;
            this.tokens = new STToken[size];
        }

        /**
         * Add a token to the buffer.
         *
         * @param token Token to add
         */
        public void add(STToken token) {
            if (this.size == this.capacity) {
                throw new IndexOutOfBoundsException("buffer overflow");
            }

            if (this.endIndex == this.capacity - 1) {
                this.endIndex = 0;
            } else {
                this.endIndex++;
            }

            if (this.size == 0) {
                this.startIndex = this.endIndex;
            }

            this.tokens[this.endIndex] = token;
            this.size++;
        }

        /**
         * Consume and return the token at the front of the buffer. This will remove the
         * consumed token from the buffer.
         *
         * @return Token at the front of the buffer.
         */
        public STToken consume() {
            STToken token = this.tokens[this.startIndex];
            this.size--;
            if (this.startIndex == this.capacity - 1) {
                this.startIndex = 0;
            } else {
                this.startIndex++;
            }

            return token;
        }

        /**
         * Get the token at the front of the buffer, without removing it.
         *
         * @return Token at the front of the buffer.
         */
        public STToken peek() {
            return this.tokens[this.startIndex];
        }

        /**
         * Get the token at the k-th position from the front of the buffer, without removing it.
         *
         * @return Token at the k-th position from the front of the buffer.
         */
        public STToken peek(int k) {
            if (k > this.size) {
                throw new IndexOutOfBoundsException("size: " + this.size + ", index: " + k);
            }

            int index = this.startIndex + k - 1;
            if (index >= this.capacity) {
                index = index - this.capacity;
            }

            return this.tokens[index];
        }
    }
}
