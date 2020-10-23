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
package io.ballerina.toml.internal.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Reader that read characters from a given input source.
 * 
 * @since 1.2.0
 */
public class InputReader {

    private static final int BUFFER_SIZE = 10;
    private CharacterBuffer charsAhead = new CharacterBuffer(BUFFER_SIZE);
    private Reader reader;
    private int currentChar;

    InputReader(InputStream inputStream) {
        // Wrapping with a buffered reader for efficiency.
        this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    InputReader(String source) {
        this.reader = new StringReader(source);
    }

    InputReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * Consumes the input and return the next character.
     * 
     * @return Next character in the input
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException {
        if (this.charsAhead.size > 0) {
            // cache the head
            this.currentChar = charsAhead.consume();
            return this.currentChar;
        }

        // cache the head
        this.currentChar = this.reader.read();
        return this.currentChar;
    }

    /**
     * Lookahead in the input and returns the next character. This will not consume the input.
     * That means calling this method multiple times will return the same result.
     * 
     * @return Next character in the input
     * @throws IOException If an I/O error occurs
     */
    public int peek() throws IOException {
        if (this.charsAhead.size == 0) {
            this.charsAhead.add(this.reader.read());
        }
        return this.charsAhead.peek();
    }

    /**
     * Lookahead in the input and returns the character at the k-th position from the current
     * position of the input stream. This will not consume the input. That means calling this
     * method multiple times will return the same result.
     * 
     * @param k Position of the character to peek
     * @return Character at the k-position from the current position
     * @throws IOException If an I/O error occurs
     */
    public int peek(int k) throws IOException {
        while (this.charsAhead.size < k) {
            this.charsAhead.add(this.reader.read());
        }

        return this.charsAhead.peek(k);
    }

    /**
     * A ring buffer of characters.
     * 
     * @since 1.2.0
     */
    private static class CharacterBuffer {

        private final int capacity;
        private final int[] chars;
        private int endIndex = -1;
        private int startIndex = -1;
        private int size = 0;

        CharacterBuffer(int size) {
            this.capacity = size;
            this.chars = new int[size];
        }

        /**
         * Add a character to the buffer.
         * 
         * @param c Character to add
         */
        public void add(int c) {
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

            this.chars[this.endIndex] = c;
            this.size++;
        }

        /**
         * Consume and return the character at the front of the buffer. This will remove the
         * consumed character from the buffer.
         * 
         * @return Character at the front of the buffer.
         */
        public int consume() {
            int token = this.chars[this.startIndex];
            this.size--;
            if (this.startIndex == this.capacity - 1) {
                this.startIndex = 0;
            } else {
                this.startIndex++;
            }

            return token;
        }

        /**
         * Get the character at the front of the buffer, without removing it.
         * 
         * @return Character at the front of the buffer.
         */
        public int peek() {
            return this.chars[this.startIndex];
        }

        /**
         * Get the character at the k-th position from the front of the buffer, without removing it.
         * 
         * @param k position of the character to return.
         * @return Character at the k-th position from the front of the buffer.
         */
        public int peek(int k) {
            if (k > this.size) {
                throw new IndexOutOfBoundsException("size: " + this.size + ", index: " + k);
            }

            int index = this.startIndex + k - 1;
            if (index >= this.capacity) {
                index = index - this.capacity;
            }

            return this.chars[index];
        }
    }
}
