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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STIdentifier;
import io.ballerinalang.compiler.internal.parser.tree.STLiteralValueToken;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.STTypeToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxTrivia;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A LL(k) lexer for ballerina.
 * 
 * @since 1.2.0
 */
public class BallerinaLexer {

    private static final byte EOF = -1;
    private final PositionTracer tracer = new PositionTracer();
    private StringBuilder sb = new StringBuilder();
    private InputReader reader;
    private List<STNode> leadingTriviaList;

    public BallerinaLexer(InputStream inputStream) {
        this.reader = new InputReader(inputStream);
    }

    public BallerinaLexer(String source) {
        this.reader = new InputReader(source);
    }

    /**
     * Get the next lexical token.
     * 
     * @return Next lexical token.
     */
    public STToken nextToken() {
        this.leadingTriviaList = new ArrayList<>(10);
        STToken token = readToken();
        this.tracer.markTokenEnd();
        return token;
    }

    private STToken readToken() {
        STToken token;
        int startChar = consume();
        switch (startChar) {
            case EOF:
                token = getSyntaxToken(SyntaxKind.EOF_TOKEN);
                break;

            // Separators
            case LexerTerminals.COLON:
                token = getSyntaxToken(SyntaxKind.COLON_TOKEN);
                break;
            case LexerTerminals.SEMICOLON:
                token = getSyntaxToken(SyntaxKind.SEMICOLON_TOKEN);
                break;
            case LexerTerminals.DOT:
                token = parseDotOrEllipsis();
                break;
            case LexerTerminals.COMMA:
                token = getSyntaxToken(SyntaxKind.COMMA_TOKEN);
                break;
            case LexerTerminals.OPEN_PARANTHESIS:
                token = getSyntaxToken(SyntaxKind.OPEN_PAREN_TOKEN);
                break;
            case LexerTerminals.CLOSE_PARANTHESIS:
                token = getSyntaxToken(SyntaxKind.CLOSE_PAREN_TOKEN);
                break;
            case LexerTerminals.OPEN_BRACE:
                token = getSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
                break;
            case LexerTerminals.CLOSE_BRACE:
                token = getSyntaxToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                break;
            case LexerTerminals.OPEN_BRACKET:
                token = getSyntaxToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                break;
            case LexerTerminals.CLOSE_BRACKET:
                token = getSyntaxToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                break;

            // Arithmetic operators
            case LexerTerminals.EQUAL:
                token = processEqualOperator();
                break;
            case LexerTerminals.ADD:
                token = getSyntaxToken(SyntaxKind.PLUS_TOKEN);
                break;
            case LexerTerminals.SUB:
                token = getSyntaxToken(SyntaxKind.MINUS_TOKEN);
                break;
            case LexerTerminals.MUL:
                token = getSyntaxToken(SyntaxKind.ASTERISK_TOKEN);
                break;
            case LexerTerminals.DIV:
                if (peek() == LexerTerminals.DIV) {
                    // Process comments as trivia, and continue to next token
                    processComment(startChar);
                    token = readToken();
                } else {
                    token = getSyntaxToken(SyntaxKind.SLASH_TOKEN);
                }
                break;
            case LexerTerminals.MOD:
                token = getSyntaxToken(SyntaxKind.PERCENT_TOKEN);
                break;
            case LexerTerminals.LT:
                token = getSyntaxToken(SyntaxKind.LT_TOKEN);
                break;
            // Numbers
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                token = processNumericLiteral(startChar);
                break;

            // Other
            case 0x9:
            case 0xD:
            case 0x20:
                // Process whitespace as trivia, and continue to next token
                processWhiteSpace(startChar);
                token = readToken();
                break;
            case LexerTerminals.NEWLINE:
                // Process newline as trivia, and continue to next token
                processNewLine(startChar);
                token = readToken();
                break;

            // Identifiers and keywords
            default:
                append(startChar);
                if (isIdentifierInitialChar(startChar)) {
                    token = processIdentifierOrKeyword();
                    break;
                }

                // Process invalid token as trivia, and continue to next token
                processInvalidToken();
                token = readToken();
                break;
        }

        return token;
    }

    private STToken getSyntaxToken(SyntaxKind kind) {
        STNodeList leadingTrivia = new STNodeList(this.leadingTriviaList);
        STNodeList trailingTrivia = new STNodeList(new ArrayList<>(0));
        return new STToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getIdentifierToken(String tokenText) {
        STNodeList leadingTrivia = new STNodeList(this.leadingTriviaList);
        STNodeList trailingTrivia = new STNodeList(new ArrayList<>(0));
        return new STIdentifier(tokenText, leadingTrivia, trailingTrivia);
    }

    private void addTrivia(SyntaxKind kind) {
        SyntaxTrivia trivia = new SyntaxTrivia(kind, getCurrentTokenText());
        this.leadingTriviaList.add(trivia);
    }

    private STLiteralValueToken getLiteral(SyntaxKind kind) {
        STNodeList leadingTrivia = new STNodeList(this.leadingTriviaList);
        STNodeList trailingTrivia = new STNodeList(new ArrayList<>(0));
        return new STLiteralValueToken(kind, getCurrentTokenText(), -1, leadingTrivia, trailingTrivia);
    }

    private STTypeToken getTypeToken(String tokenText) {
        STNodeList leadingTrivia = new STNodeList(this.leadingTriviaList);
        STNodeList trailingTrivia = new STNodeList(new ArrayList<>(0));
        return new STTypeToken(SyntaxKind.TYPE_TOKEN, tokenText, leadingTrivia, trailingTrivia);
    }

    /**
     * Process dot or ellipsis token.
     * 
     * @return Dot or ellipsis token
     */
    private STToken parseDotOrEllipsis() {
        if (peek(1) == LexerTerminals.DOT && peek(2) == LexerTerminals.DOT) {
            consume();
            consume();
            return getSyntaxToken(SyntaxKind.ELLIPSIS_TOKEN);
        }

        return getSyntaxToken(SyntaxKind.DOT_TOKEN);
    }

    /**
     * <p>
     * Process a comment, and add it to trivia list.
     * </p>
     * <code>Comment := // AnyCharButNewline*
     * <br/><br/>AnyCharButNewline := ^ 0xA</code>
     * 
     * @param commentStartChar Starting slash of the comment
     */
    private void processComment(int commentStartChar) {
        append(commentStartChar);
        consumeAndAppend(); // consume and append the second '/' of the comment. This is already verified.
        while (peek() != LexerTerminals.NEWLINE) {
            consumeAndAppend();
        }

        addTrivia(SyntaxKind.COMMENT);
    }

    /**
     * Process any token that starts with '='.
     * 
     * @return One of the tokens: <code>'=', '==', '=>', '==='</code>
     */
    private STToken processEqualOperator() {
        switch (peek()) { // check for the second char
            case LexerTerminals.EQUAL:
                consume();
                if (peek() == LexerTerminals.EQUAL) {
                    // this is '==='
                    consume();
                    return getSyntaxToken(SyntaxKind.TRIPPLE_EQUAL_TOKEN);
                } else {
                    // this is '=='
                    return getSyntaxToken(SyntaxKind.DOUBLE_EQUAL_TOKEN);
                }
            case LexerTerminals.GT:
                // this is '=>'
                consume();
                return getSyntaxToken(SyntaxKind.EQUAL_GT_TOKEN);
            default:
                // this is '='
                return getSyntaxToken(SyntaxKind.EQUAL_TOKEN);
        }
    }

    /**
     * <p>
     * Process and returns a numeric literal.
     * </p>
     * <code>
     * numeric-literal := int-literal | floating-point-literal
     * <br/>
     * int-literal := DecimalNumber | HexIntLiteral
     * <br/>
     * DecimalNumber := 0 | NonZeroDigit Digit*
     * <br/>
     * Digit := 0 .. 9
     * <br/>
     * NonZeroDigit := 1 .. 9
     * </code>
     * 
     * @return The numeric literal.
     */
    private STToken processNumericLiteral(int startChar) {
        append(startChar);

        int nextChar = peek();
        if (isHexIndicator(startChar, nextChar)) {
            return processHexIntLiteral();
        }

        int len = 1;
        while (true) {
            switch (nextChar) {
                case '.':
                case 'e':
                case 'E':
                    // TODO: handle float
                    processInvalidToken();
                    return readToken();
                default:
                    if (isDigit(nextChar)) {
                        consumeAndAppend();
                        len++;
                        nextChar = peek();
                        continue;
                    }
                    break;
            }
            break;
        }

        // Integer cannot have a leading zero
        // TODO: validate whether this is an integer, once float support is added.
        if (startChar == '0' && len > 1) {
            processInvalidToken();
            return readToken();
        }

        return getLiteral(SyntaxKind.NUMERIC_LITERAL_TOKEN);
    }

    /**
     * <p>
     * Process and returns a hex integer literal.
     * </p>
     * <code>
     * HexIntLiteral := HexIndicator HexNumber
     * <br/>
     * HexNumber := HexDigit+
     * <br/>
     * HexIndicator := 0x | 0X
     * <br/>
     * HexDigit := Digit | a .. f | A .. F
     * <br/>
     * </code>
     * 
     * @return
     */
    private STToken processHexIntLiteral() {
        consumeAndAppend();
        while (isHexDigit(peek())) {
            consumeAndAppend();
        }

        // TODO: can send syntax kind as HEX_LITERAL ??
        return getLiteral(SyntaxKind.NUMERIC_LITERAL_TOKEN);
    }

    /**
     * Process and returns an identifier or a keyword.
     * 
     * @return An identifier or a keyword.
     */
    private STToken processIdentifierOrKeyword() {
        while (isIdentifierFollowingChar(peek())) {
            consumeAndAppend();
        }

        String tokenText = getCurrentTokenText();
        switch (tokenText) {
            case LexerTerminals.PUBLIC:
                return getSyntaxToken(SyntaxKind.PUBLIC_KEYWORD);
            case LexerTerminals.FUNCTION:
                return getSyntaxToken(SyntaxKind.FUNCTION_KEYWORD);
            case LexerTerminals.INT:
            case LexerTerminals.FLOAT:
            case LexerTerminals.STRING:
            case LexerTerminals.BOOLEAN:
                return getTypeToken(tokenText);
            case LexerTerminals.RETURN:
                return getSyntaxToken(SyntaxKind.RETURNS_KEYWORD);
            case LexerTerminals.RETURNS:
                return getSyntaxToken(SyntaxKind.RETURNS_KEYWORD);
            case LexerTerminals.EXTERNAL:
                return getSyntaxToken(SyntaxKind.EXTERNAL_KEYWORD);
            default:
                return getIdentifierToken(tokenText);
        }
    }

    /**
     * Process and returns an invalid token. Consumes the input until {@link #isEndOfInvalidToken()}
     * is reached.
     * 
     * @return The invalid token.
     */
    private void processInvalidToken() {
        while (!isEndOfInvalidToken()) {
            consumeAndAppend();
        }

        addTrivia(SyntaxKind.INVALID);
    }

    /**
     * Process whitespace.
     * <code>WhiteSpaceChar := 0x9 | 0xA | 0xD | 0x20</code>
     * 
     * @param startChar Starting character of the whitespace.
     */
    private void processWhiteSpace(int startChar) {
        append(startChar);
        while (isWhiteSpace(peek())) {
            consumeAndAppend();
        }

        addTrivia(SyntaxKind.WHITESPACE_TRIVIA);
    }

    /**
     * Process new line.
     * 
     * @param newLineChar new line character.
     */
    private void processNewLine(int newLineChar) {
        append(newLineChar);
        addTrivia(SyntaxKind.END_OF_LINE_TRIVIA);
        this.tracer.markNewLine();
    }

    /**
     * Check whether the current index is pointing to an end of an invalid lexer-token.
     * An invalid token is considered to end if one of the below is reached:
     * <ul>
     * <li>a whitespace</li>
     * <li>semicolon</li>
     * <li>newline</li>
     * </ul>
     * 
     * @return <code>true</code>, if the end of an invalid token is reached, <code>false</code> otherwise
     */
    private boolean isEndOfInvalidToken() {
        int currentChar = peek();
        switch (currentChar) {
            case LexerTerminals.NEWLINE:
            case LexerTerminals.SEMICOLON:
                // TODO: add all separators (braces, parentheses, etc)
                // TODO: add all operators (arithmetic, binary, etc)
                return true;
            default:
                return isWhiteSpace(currentChar);
        }
    }

    /**
     * <p>
     * Check whether a given char is an identifier start char.
     * </p>
     * <code>IdentifierInitialChar := A .. Z | a .. z | _ | UnicodeIdentifierChar</code>
     * 
     * @param c character to check
     * @return <code>true</code>, if the character is an identifier start char. <code>false</code> otherwise.
     */
    private boolean isIdentifierInitialChar(int c) {
        // TODO: pre-mark all possible characters, using a mask. And use that mask here to check
        if ('A' <= c && c <= 'Z') {
            return true;
        }

        if ('a' <= c && c <= 'z') {
            return true;
        }

        if (c == '_') {
            return true;
        }

        // TODO: if (UnicodeIdentifierChar) return false;
        return false;
    }

    /**
     * <p>
     * Check whether a given char is an identifier following char.
     * </p>
     * <code>IdentifierFollowingChar := IdentifierInitialChar | Digit</code>
     * 
     * @param c character to check
     * @return <code>true</code>, if the character is an identifier following char. <code>false</code> otherwise.
     */
    private boolean isIdentifierFollowingChar(int c) {
        return isIdentifierInitialChar(c) || isDigit(c);
    }

    /**
     * <p>
     * Check whether a given char is a digit.
     * </p>
     * <code>Digit := 0..9</code>
     * 
     * @param c character to check
     * @return <code>true</code>, if the character represents a digit. <code>false</code> otherwise.
     */
    private boolean isDigit(int c) {
        return ('0' <= c && c <= '9');
    }

    /**
     * <p>
     * Check whether a given char is a hexa digit.
     * </p>
     * <code>HexDigit := Digit | a .. f | A .. F</code>
     * 
     * @param c character to check
     * @return <code>true</code>, if the character represents a hex digit. <code>false</code> otherwise.
     */
    private boolean isHexDigit(int c) {
        if ('a' <= c && c <= 'f') {
            return true;
        }
        if ('A' <= c && c <= 'F') {
            return true;
        }
        return isDigit(c);
    }

    /**
     * <p>
     * Check whether a given char is a whitespace.
     * </p>
     * <code>WhiteSpaceChar := 0x9 | 0xD | 0x20</code>
     * 
     * @param c character to check
     * @return <code>true</code>, if the character represents a whitespace. <code>false</code> otherwise.
     */
    private boolean isWhiteSpace(int c) {
        return c == 0x9 || c == 0xD || c == 0x20;
    }

    /**
     * <p>
     * Check whether current input index points to a start of a hex-numeric literal.
     * </p>
     * <code>HexIndicator := 0x | 0X</code>
     * 
     * @param startChar Starting character of the literal
     * @param nextChar Second character of the literal
     * @return <code>true</code>, if the current input points to a start of a hex-numeric literal.
     *         <code>false</code> otherwise.
     */
    private boolean isHexIndicator(int startChar, int nextChar) {
        return startChar == '0' && (nextChar == 'x' || nextChar == 'X');
    }

    /**
     * Consumes and returns the next character from the reader.
     * 
     * @return Next character
     */
    private int consume() {
        try {
            this.tracer.length++;
            return reader.read();
        } catch (IOException e) {
            // TODO: Fix this properly
            this.tracer.length--;
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the next character from the reader, without consuming the stream.
     * 
     * @return Next character
     */
    private int peek() {
        try {
            return this.reader.peek();
        } catch (IOException e) {
            // TODO: Fix this properly
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the next k-th character from the reader, without consuming the stream.
     * 
     * @return Next k-th character
     */
    private int peek(int k) {
        try {
            return this.reader.peek(k);
        } catch (IOException e) {
            // TODO: Fix this properly
            throw new IllegalStateException(e);
        }
    }

    /**
     * Append a given character to the currently processing token.
     * 
     * @param c Character to append
     */
    private void append(int c) {
        this.sb.append((char) c);
    }

    /**
     * Consume the next character from the input and append it to the currently processing token.
     */
    private void consumeAndAppend() {
        this.sb.append((char) consume());
    }

    /**
     * Get the text associated with the current token.
     * 
     * @return Text associated with the current token.
     */
    private String getCurrentTokenText() {
        String text = this.sb.toString();
        // reset the string builder
        this.sb = new StringBuilder();
        return text;
    }
}
