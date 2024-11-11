/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.compiler.internal.parser.tree.STAbstractNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.CharReader;

import java.util.ArrayList;

/**
 * A LL(k) lexer for RegExp in ballerina.
 *
 * @since 2201.3.0
 */
public class RegExpLexer extends AbstractLexer {

    public RegExpLexer(CharReader charReader) {
        super(charReader, ParserMode.RE_DISJUNCTION);
    }

    /**
     * Get the next lexical token.
     *
     * @return Next lexical token.
     */
    @Override
    public STToken nextToken() {
        STToken token;
        switch (this.mode) {
            case RE_DISJUNCTION:
            case RE_CHAR_CLASS:
                token = readTokenInReDisjunction();
                break;
            case RE_UNICODE_PROP_ESCAPE:
                token = readTokenInReUnicodePropertyEscape();
                break;
            case RE_UNICODE_PROPERTY_VALUE:
                token = readTokenInReUnicodePropertyValue();
                break;
            case INTERPOLATION:
                token = readTokenInInterpolation();
                break;
            case RE_QUOTE_ESCAPE:
                token = readTokenInReDisjunction();
                endMode();
                break;
            default:
                token = null;
        }

        return cloneWithDiagnostics(token);
    }

    /**
     * Read the next token in the {@link ParserMode#RE_DISJUNCTION} mode.
     * <p>
     * <code>
     * ReDisjunction := ReSequence ("|" ReSequence)*
     * </code>
     * <code>
     * ReSequence := ReTerm*
     * </code>
     * <code>
     * ReTerm :=
     *    ReAtom [ReQuantifier]
     *    | ReAssertion
     * </code>
     *
     * @return Next token
     */
    private STToken readTokenInReDisjunction() {
        reader.mark();
        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        if (nextChar == LexerTerminals.PIPE) {
            reader.advance();
            return getRegExpSyntaxToken(SyntaxKind.PIPE_TOKEN);
        }

        // ReSequence has zero or more ReTerm.
        this.reader.advance();
        return switch (nextChar) {
            case LexerTerminals.BITWISE_XOR -> getRegExpSyntaxToken(SyntaxKind.BITWISE_XOR_TOKEN);
            case LexerTerminals.DOLLAR -> {
                if (this.mode != ParserMode.RE_CHAR_CLASS && peek() == LexerTerminals.OPEN_BRACE) {
                    // Start interpolation mode.
                    startMode(ParserMode.INTERPOLATION);
                    this.reader.advance();
                    yield getRegExpSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                yield getRegExpSyntaxToken(SyntaxKind.DOLLAR_TOKEN);
                // Start interpolation mode.
            }
            case LexerTerminals.DOT -> getRegExpSyntaxToken(SyntaxKind.DOT_TOKEN);
            case LexerTerminals.ASTERISK -> getRegExpSyntaxToken(SyntaxKind.ASTERISK_TOKEN);
            case LexerTerminals.PLUS -> getRegExpSyntaxToken(SyntaxKind.PLUS_TOKEN);
            case LexerTerminals.QUESTION_MARK -> getRegExpSyntaxToken(SyntaxKind.QUESTION_MARK_TOKEN);
            case LexerTerminals.BACKSLASH -> processReEscape();
            // Start parsing ReSyntaxChar character class [[^] [ReCharSet]].
            case LexerTerminals.OPEN_BRACKET -> {
                if (this.mode != ParserMode.RE_QUOTE_ESCAPE) {
                    startMode(ParserMode.RE_CHAR_CLASS);
                }
                yield getRegExpSyntaxToken(SyntaxKind.OPEN_BRACKET_TOKEN);
            }
            case LexerTerminals.CLOSE_BRACKET -> {
                if (this.mode == ParserMode.RE_CHAR_CLASS) {
                    endMode();
                }
                yield getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
            }
            case LexerTerminals.OPEN_BRACE -> getRegExpSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
            case LexerTerminals.CLOSE_BRACE -> getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACE_TOKEN);
            // Start parsing capturing group ([? ReFlagsOnOff :] ReDisjunction).
            case LexerTerminals.OPEN_PARANTHESIS -> getRegExpSyntaxToken(SyntaxKind.OPEN_PAREN_TOKEN);
            case LexerTerminals.CLOSE_PARANTHESIS -> getRegExpSyntaxToken(SyntaxKind.CLOSE_PAREN_TOKEN);
            case LexerTerminals.COMMA -> getRegExpSyntaxToken(SyntaxKind.COMMA_TOKEN);
            default -> {
                if (isDigit(nextChar)) {
                    yield getRegExpText(SyntaxKind.DIGIT);
                }
                yield getRegExpText(SyntaxKind.RE_LITERAL_CHAR);
            }
        };
    }

    private STToken processReEscape() {
        int nextChar = peek();
        switch (nextChar) {
            // Handle NumericEscape.
            case 'u':
                if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    processNumericEscapeWithoutBackslash();
                    return getRegExpText(SyntaxKind.RE_NUMERIC_ESCAPE);
                }
                break;
            // Handle ControlEscape.
            case 'n':
            case 't':
            case 'r':
                this.reader.advance();
                return getRegExpText(SyntaxKind.RE_CONTROL_ESCAPE);
            // Handle ReUnicodePropertyEscape.
            case 'p':
            case 'P':
                if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    // Change the parser mode to handle ReUnicodePropertyEscape.
                    startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
                }
                break;
            case LexerTerminals.OPEN_BRACKET:
                startMode(ParserMode.RE_QUOTE_ESCAPE);
                break;
            default:
                break;
        }

        return getRegExpText(SyntaxKind.BACK_SLASH_TOKEN);
    }

    /**
     * Read tokens in ReUnicodePropertyEscape.
     * It can be either p, P, {, ReUnicodeProperty or }.
     *
     * @return Next token
     */
    private STToken readTokenInReUnicodePropertyEscape() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();

        switch (nextChar) {
            case 'p':
            case 'P':
                if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    this.reader.advance();
                    return getRegExpText(SyntaxKind.RE_PROPERTY);
                }
                break;
            case LexerTerminals.OPEN_BRACE:
                this.reader.advance();
                return getRegExpSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
            case 's':
                if (this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
                    this.reader.advance(3);
                    // Change the parser mode to handle ReUnicodePropertyValue.
                    startMode(ParserMode.RE_UNICODE_PROPERTY_VALUE);
                    return getRegExpText(SyntaxKind.RE_UNICODE_SCRIPT_START);
                }
                break;
            case 'g':
                if (this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
                    this.reader.advance(3);
                    return getRegExpText(SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_START);
                }
                break;
            case LexerTerminals.CLOSE_BRACE:
                this.reader.advance();
                endMode();
                return getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACE_TOKEN);
            default:
                break;
        }

        return processReUnicodeGeneralCategoryAbbr();
    }

    private STToken processReUnicodeGeneralCategoryAbbr() {
        switch (peek()) {
            case 'L':
                this.reader.advance();
                processAbbrWithLetter();
                break;
            case 'M':
                this.reader.advance();
                processAbbrWithMark();
                break;
            case 'N':
                this.reader.advance();
                processAbbrWithNumber();
                break;
            case 'S':
                this.reader.advance();
                processAbbrWithSymbol();
                break;
            case 'P':
                this.reader.advance();
                processAbbrWithPunctuation();
                break;
            case 'Z':
                this.reader.advance();
                processAbbrWithSeparator();
                break;
            case 'C':
                this.reader.advance();
                processAbbrWithOther();
                break;
            default:
                break;
        }

        if (!isEndOfUnicodePropertyEscape()) {
            while (!isEndOfUnicodePropertyEscape()) {
                this.reader.advance();
            }
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
        }

        return getRegExpText(SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_NAME);
    }

    private void processAbbrWithLetter() {
        switch (peek()) {
            case 'u':
            case 'l':
            case 't':
            case 'm':
            case 'o':
                this.reader.advance();
                return;
        }
    }

    private void processAbbrWithMark() {
        switch (peek()) {
            case 'n':
            case 'c':
            case 'e':
                this.reader.advance();
                return;
        }
    }

    private void processAbbrWithNumber() {
        switch (peek()) {
            case 'd':
            case 'l':
            case 'o':
                this.reader.advance();
                return;
        }
    }

    private void processAbbrWithSymbol() {
        switch (peek()) {
            case 'm':
            case 'c':
            case 'k':
            case 'o':
                this.reader.advance();
                return;
        }
    }

    private void processAbbrWithPunctuation() {
        switch (peek()) {
            case 'c':
            case 'd':
            case 's':
            case 'e':
            case 'i':
            case 'f':
            case 'o':
                this.reader.advance();
                return;
        }
    }

    private void processAbbrWithSeparator() {
        switch (peek()) {
            case 's':
            case 'l':
            case 'p':
                this.reader.advance();
                return;
        }
    }

    private void processAbbrWithOther() {
        switch (peek()) {
            case 'c':
            case 'f':
            case 'o':
            case 'n':
                this.reader.advance();
                return;
        }
    }

    /**
     * Read token in ReUnicodePropertyValue.
     * <p>
     * <code>
     * ReUnicodePropertyValue := ReUnicodePropertyValueChar+
     * ReUnicodePropertyValueChar := AsciiLetter | Digit | _
     * </code>
     *
     * @return Next token
     */
    private STToken readTokenInReUnicodePropertyValue() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        boolean invalidTokenFound = false;
        if (!isReUnicodePropertyValueChar(peek())) {
            this.reader.advance();
            invalidTokenFound = true;
        }

        while (!isEndOfUnicodePropertyEscape()) {
            if (!invalidTokenFound && !isReUnicodePropertyValueChar(peek())) {
                invalidTokenFound = true;
            }
            this.reader.advance();
        }

        endMode();
        if (invalidTokenFound) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
        }

        return getRegExpText(SyntaxKind.RE_UNICODE_PROPERTY_VALUE);
    }

    private boolean isEndOfUnicodePropertyEscape() {
        return peek() == LexerTerminals.CLOSE_BRACE || this.reader.isEOF();
    }

    private boolean isReUnicodePropertyValueChar(int nextChar) {
        return isDigit(nextChar) || isAsciiLetter(nextChar) || nextChar == '_';
    }

    private boolean isAsciiLetter(int nextChar) {
        return 'A' <= nextChar && nextChar <= 'Z' || 'a' <= nextChar && nextChar <= 'z';
    }

    /**
     * Read token in interpolation.
     *
     * @return Next token
     */
    private STToken readTokenInInterpolation() {
        reader.mark();
        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextToken = peek();
        if (nextToken == LexerTerminals.CLOSE_BRACE) {
            // End of the interpolation.
            endMode();
            reader.advance();
            return getRegExpCloseBraceTokenWithoutTrailingWS();
        }

        // Fail-safe.
        endMode();
        return nextToken();
    }

    private STToken getRegExpSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = STAbstractNodeFactory.createEmptyNodeList();
        return STAbstractNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getRegExpText(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = STAbstractNodeFactory.createEmptyNodeList();
        return STAbstractNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getRegExpCloseBraceTokenWithoutTrailingWS() {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = STAbstractNodeFactory.createNodeList(new ArrayList<>(0));
        return STAbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN, leadingTrivia, trailingTrivia);
    }

    /**
     * Returns the next character from the reader, without consuming the stream.
     *
     * @return Next character
     */
    private int peek() {
        return this.reader.peek();
    }

    /**
     * Get the text associated with the current token.
     *
     * @return Text associated with the current token.
     */
    private String getLexeme() {
        return reader.getMarkedChars();
    }

    /**
     * Check whether a given char is ReSimpleCharClassCode.
     * <p>
     * <code>
     * ReSimpleCharClassCode := "d" | "D" | "s" | "S" | "w" | "W"
     * </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is ReSimpleCharClassCode. <code>false</code> otherwise.
     */
    protected static boolean isReSimpleCharClassCode(int c) {
        return switch (c) {
            case 'd', 'D', 's', 'S', 'w', 'W' -> true;
            default -> false;
        };
    }

    /**
     * Check whether a given char is ReCharSetLiteralChar.
     * <p>
     * <code>
     * ReCharSetLiteralChar := ^ ("\" | "]" | "-")
     * </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is ReCharSetLiteralChar. <code>false</code> otherwise.
     */
    protected static boolean isReCharSetLiteralChar(int c) {
        return switch (c) {
            case '\\', ']', '-' -> false;
            default -> true;
        };
    }

    /**
     * Check whether a given char is ReFlag.
     * <p>
     * <code>
     * ReFlag :=
     *   ReMultilineFlag
     *   | ReDotAllFlag
     *   | ReIgnoreCaseFlag
     *   | ReCommentFlag
     * </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is ReFlag. <code>false</code> otherwise.
     */
    protected static boolean isReFlag(int c) {
        return switch (c) {
            case 'm', 's', 'i', 'x' -> true;
            default -> false;
        };
    }
}
