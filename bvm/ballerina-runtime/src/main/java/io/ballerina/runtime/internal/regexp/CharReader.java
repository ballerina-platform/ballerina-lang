/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.regexp;

import java.util.Arrays;

/**
 * A character reader utility used by TreeTraverser when parsing Ballerina string value of a
 * regular expression string.
 *
 * @since 2201.3.0
 */
public class CharReader {

    private char[] charBuffer;
    private int offset = 0;
    private int charBufferLength;

    private int lexemeStartPos;

    private CharReader(char[] buffer) {
        this.charBuffer = buffer;
        this.charBufferLength = buffer.length;
    }

    public static CharReader from(String text) {
        return new CharReader(text.toCharArray());
    }

    public char peek() {
        if (offset < charBufferLength) {
            return charBuffer[offset];
        } else {
            return Character.MAX_VALUE;
        }
    }

    public char peek(int k) {
        int n = offset + k;
        if (n < charBufferLength) {
            return charBuffer[n];
        } else {
            return Character.MAX_VALUE;
        }
    }

    /**
     *
     */
    public void advance() {
        offset++;
    }

    public void advance(int k) {
        offset += k;
    }

    /**
     *
     */
    public void mark() {
        lexemeStartPos = offset;
    }

    /**
     *
     * @return {@link String} consist of the marked chars
     */
    public String getMarkedChars() {
        return new String(Arrays.copyOfRange(charBuffer, lexemeStartPos, offset));
    }

    public boolean isEOF() {
        return offset >= charBufferLength;
    }
}
