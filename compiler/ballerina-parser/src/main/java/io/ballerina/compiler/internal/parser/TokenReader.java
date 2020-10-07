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
package io.ballerina.compiler.internal.parser;

import io.ballerina.compiler.internal.parser.tree.STToken;

/**
 * Reader that can read tokens from a given lexer. Supports k-lookahead
 * of tokens, by buffering the peeked tokens in a ring buffer.
 * 
 * @since 1.2.0
 */
public class TokenReader extends AbstractTokenReader {

    private static final int BUFFER_SIZE = 20;

    private AbstractLexer lexer;
    private TokenBuffer tokensAhead = new TokenBuffer(BUFFER_SIZE);
    private STToken currentToken = null;

    TokenReader(AbstractLexer lexer) {
        this.lexer = lexer;
    }

    /**
     * Consumes the input and return the next token.
     * 
     * @return Next token in the input
     */
    public STToken read() {
        if (this.tokensAhead.size > 0) {
            // cache the head
            this.currentToken = tokensAhead.consume();
            return this.currentToken;
        }

        // cache the head
        this.currentToken = this.lexer.nextToken();
        return this.currentToken;
    }

    /**
     * Lookahead in the input and returns the next token. This will not consume the input.
     * That means calling this method multiple times will return the same result.
     * 
     * @return Next token in the input
     */
    public STToken peek() {
        if (this.tokensAhead.size > 0) {
            return this.tokensAhead.peek();
        }

        STToken token = this.lexer.nextToken();
        this.tokensAhead.add(token);
        return token;
    }

    /**
     * Lookahead in the input and returns the token at the k-th position from the current
     * position of the input token stream. This will not consume the input. That means
     * calling this method multiple times will return the same result.
     * 
     * @param k Position of the character to peek
     * @return Token at the k-position from the current position
     */
    public STToken peek(int k) {
        STToken nextToken;
        while (this.tokensAhead.size < k) {
            nextToken = this.lexer.nextToken();
            this.tokensAhead.add(nextToken);
        }

        return this.tokensAhead.peek(k);
    }

    /**
     * Returns the current token. i.e: last consumed token.
     * 
     * @return The current token.
     */
    public STToken head() {
        return this.currentToken;
    }

    /**
     * Start the given mode of the token reader.
     * 
     * @param mode Mode to switch on to
     */
    public void startMode(ParserMode mode) {
        this.lexer.startMode(mode);
    }

    /**
     * End the mode of the token reader.
     */
    public void endMode() {
        this.lexer.endMode();
    }

    /**
     *
     * @return current token index
     */
    public int getCurrentTokenIndex() {
        return tokensAhead.getCurrentTokenIndex();
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

        /**
         *
         * @return current token index
         */
        public int getCurrentTokenIndex() {
            return this.startIndex;
        }
    }
}
