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

import java.util.Arrays;

public class CharacterReader {

    private char[] cbuffer;
    private int offset = 0;
    private int cbufferLen;

    private int lexemeStartPos;

    private CharacterReader(String text) {
        this.cbuffer = text.toCharArray();
        this.cbufferLen = cbuffer.length;
    }

    public static CharacterReader fromString(String text) {
        return new CharacterReader(text);
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public char peekChar() {
        if (offset < cbufferLen) {
            return cbuffer[offset];
        } else {
            return Character.MAX_VALUE;
        }
    }

    public void moveToNextChar() {
        offset++;
    }

    public void resetLexemeStart() {
        lexemeStartPos = offset;
    }

    public String getText() {
        return new String(Arrays.copyOfRange(cbuffer, lexemeStartPos, offset));
    }

    public boolean isEOF() {
        return offset == cbufferLen;
    }
}
