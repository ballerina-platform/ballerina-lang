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
 * A generator class to produce tokens/lexemes on demand.
 * 
 * @author 1.2.0
 */
public class TokenGenerator {

    public static final Token EOF = new Token("EOF", TokenKind.EOF, -1, -1, -1);
    public static final Token SOF = new Token("SOF", TokenKind.SOF, -1, -1, -1);
    private final PositionTracer tracer;

    public TokenGenerator(PositionTracer tracer) {
        this.tracer = tracer;
    }

    private Token newToken(String text, TokenKind kind) {
        return new Token(text, kind, this.tracer.line, this.tracer.startCol, this.tracer.length);
    }

    public Token getPublic() {
        return newToken(LexerTerminals.PUBLIC, TokenKind.PUBLIC);
    }

    public Token getFunction() {
        return newToken(LexerTerminals.FUNCTION, TokenKind.FUNCTION);
    }

    public Token getReturn() {
        return newToken(LexerTerminals.RETURN, TokenKind.RETURN);
    }

    public Token getReturns() {
        return newToken(LexerTerminals.RETURNS, TokenKind.RETURNS);
    }

    public Token getExternal() {
        return newToken(LexerTerminals.EXTERNAL, TokenKind.EXTERNAL);
    }

    public Token getColon() {
        return newToken(":", TokenKind.COLON);
    }

    public Token getSemicolon() {
        return newToken(";", TokenKind.SEMICOLON);
    }

    public Token getDot() {
        return newToken(".", TokenKind.DOT);
    }

    public Token getComma() {
        return newToken(",", TokenKind.COMMA);
    }

    public Token getLeftParanthesis() {
        return newToken("(", TokenKind.OPEN_PARENTHESIS);
    }

    public Token getRigthtParanthesis() {
        return newToken(")", TokenKind.CLOSE_PARENTHESIS);
    }

    public Token getLeftBrace() {
        return newToken("{", TokenKind.OPEN_BRACE);
    }

    public Token getRightBrace() {
        return newToken("}", TokenKind.CLOSE_BRACE);
    }

    public Token getLeftBracket() {
        return newToken("[", TokenKind.OPEN_BRACKET);
    }

    public Token getRightBracket() {
        return newToken("]", TokenKind.CLOSE_BRACKET);
    }

    public Token getAssignOp() {
        return newToken("=", TokenKind.ASSIGN);
    }

    public Token getPlus() {
        return newToken("+", TokenKind.ADD);
    }

    public Token getMinus() {
        return newToken("-", TokenKind.SUB);
    }

    public Token getMutiplication() {
        return newToken("*", TokenKind.MUL);
    }

    public Token getDivision() {
        return newToken("/", TokenKind.DIV);
    }

    public Token getModulus() {
        return newToken("%", TokenKind.MOD);
    }

    public Token getLessThanOp() {
        return newToken("<", TokenKind.LT);
    }

    public Token getEqualOp() {
        return newToken("==", TokenKind.EQUAL);
    }

    public Token getRefEqualOp() {
        return newToken("===", TokenKind.REF_EQUAL);
    }

    public Token getEqualGreaterOp() {
        return newToken("=>", TokenKind.EQUAL_GT);
    }

    public Token getNewline() {
        return newToken("\\n", TokenKind.NEWLINE);
    }

    public Token getIdentifier(String text) {
        return newToken(text, TokenKind.IDENTIFIER);
    }

    public Token getWhiteSpaces(String text) {
        return newToken(text, TokenKind.WHITE_SPACE);
    }

    public Token getIntLiteral(String text) {
        return newToken(text, TokenKind.INT_LITERAL);
    }

    public Token getLiteral(String text, TokenKind kind) {
        return newToken(text, kind);
    }

    public Token getFloatLiteral(String text) {
        return newToken(text, TokenKind.FLOAT_LITERAL);
    }

    public Token getInvalidToken(String text) {
        return newToken(text, TokenKind.INVALID);
    }

    public Token getType(String typeName) {
        return newToken(typeName, TokenKind.TYPE);
    }

    public Token getComment(String text) {
        return newToken(text, TokenKind.COMMENT);
    }

    public Token getEllipsis() {
        return newToken("...", TokenKind.ELLIPSIS);
    }
}
