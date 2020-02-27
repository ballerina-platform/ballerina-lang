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
package org.ballerinalang.compiler.parser;

/**
 * Represents a lexeme.
 * 
 * @since 1.2.0
 */
public class Token {

    public final String text;
    public final TokenKind kind;

    protected int line;
    protected int startCol;
    protected int endCol;

    public Token(String text, TokenKind kind, int startLine, int startCol, int tokenLen) {
        this.text = text;
        this.kind = kind;
        this.line = startLine;
        this.startCol = startCol;
        this.endCol = startCol + tokenLen;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return this.kind.toString() + "(" + this.text + ")";
    }

    public int getLine() {
        return this.line;
    }

    public int getStartCol() {
        return this.startCol;
    }

    public int getEndCol() {
        return this.endCol;
    }
}
