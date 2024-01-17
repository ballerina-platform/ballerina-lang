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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;

import java.util.ArrayDeque;

/**
 * A TreeTraverser for regular expression.
 *
 * @since 2201.3.0
 */
public class TreeTraverser {

    private CharReader reader;
    private ParserMode mode;
    private ArrayDeque<ParserMode> modeStack = new ArrayDeque<>();

    public TreeTraverser(CharReader charReader) {
        this.reader = charReader;
        this.mode = ParserMode.RE_DISJUNCTION;
        this.modeStack.add(this.mode);
    }

    public Token nextToken() {
        switch (this.mode) {
            case RE_DISJUNCTION:
                return readTokenInReDisjunction();
            case RE_UNICODE_PROP_ESCAPE:
            case RE_UNICODE_GENERAL_CATEGORY_NAME:     
                return readTokenInReUnicodePropertyEscape();
            case RE_UNICODE_PROPERTY_VALUE:
                return readTokenInReUnicodePropertyValue();
            default:
                // Should not reach here.
                return null;
        }
    }

    /**
     * Read a token in {@link ParserMode#RE_DISJUNCTION} mode.
     * <p>
     * <code>
     * ReDisjunction := ReSequence ("|" ReSequence)*
     * </code>
     * <code>
     * ReSequence := ReTerm*
     * </code>
     * <code>
     * ReTerm :=
     * ReAtom [ReQuantifier]
     * | ReAssertion
     * </code>
     *
     * @return Next token
     */
    private Token readTokenInReDisjunction() {
        reader.mark();
        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        int nextChar = peek();

        this.reader.advance();
        switch (nextChar) {
            case Terminals.BITWISE_XOR:
                return getRegExpToken(TokenKind.BITWISE_XOR_TOKEN);
            case Terminals.DOLLAR:
                return getRegExpToken(TokenKind.DOLLAR_TOKEN);
            case Terminals.DOT:
                return getRegExpToken(TokenKind.DOT_TOKEN);
            case Terminals.ASTERISK:
                return getRegExpToken(TokenKind.ASTERISK_TOKEN);
            case Terminals.PLUS:
                return getRegExpToken(TokenKind.PLUS_TOKEN);
            case Terminals.QUESTION_MARK:
                return getRegExpToken(TokenKind.QUESTION_MARK_TOKEN);
            case Terminals.BACKSLASH:
                return processEscape();
            case Terminals.OPEN_BRACKET:
                return getRegExpToken(TokenKind.OPEN_BRACKET_TOKEN);
            case Terminals.CLOSE_BRACKET:
                return getRegExpToken(TokenKind.CLOSE_BRACKET_TOKEN);
            case Terminals.OPEN_BRACE:
                return getRegExpToken(TokenKind.OPEN_BRACE_TOKEN);
            case Terminals.CLOSE_BRACE:
                return getRegExpToken(TokenKind.CLOSE_BRACE_TOKEN);
            case Terminals.OPEN_PARENTHESIS:
                return getRegExpToken(TokenKind.OPEN_PAREN_TOKEN);
            case Terminals.CLOSE_PARENTHESIS:
                return getRegExpToken(TokenKind.CLOSE_PAREN_TOKEN);
            case Terminals.COMMA:
                return getRegExpToken(TokenKind.COMMA_TOKEN);
            case Terminals.MINUS:
                return getRegExpToken(TokenKind.MINUS_TOKEN);
            case Terminals.COLON:
                return getRegExpToken(TokenKind.COLON_TOKEN);
            case Terminals.PIPE:
                return getRegExpToken(TokenKind.PIPE_TOKEN);
            default:
                if (isDigit(nextChar)) {
                    return getRegExpText(TokenKind.DIGIT);
                }
                return getRegExpText(TokenKind.RE_LITERAL_CHAR);
        }
    }

    /**
     * Read special tokens in ReUnicodePropertyEscape.
     * It can be either p, P, {, or }.
     *
     * @return Next token
     */
    private Token readTokenInReUnicodePropertyEscape() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        int nextChar = peek();

        switch (nextChar) {
            case 'p':
            case 'P':
                if (this.mode != ParserMode.RE_UNICODE_GENERAL_CATEGORY_NAME) {
                    this.reader.advance();
                    return getRegExpText(TokenKind.RE_PROPERTY);
                }
                break;
            case Terminals.OPEN_BRACE:
                this.reader.advance();
                this.switchMode(ParserMode.RE_UNICODE_GENERAL_CATEGORY_NAME);
                return getRegExpToken(TokenKind.OPEN_BRACE_TOKEN);
            case 's':
                if (this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
                    this.reader.advance(3);
                    this.switchMode(ParserMode.RE_UNICODE_PROPERTY_VALUE);
                    return getRegExpText(TokenKind.RE_UNICODE_SCRIPT_START);
                }
                break;
            case 'g':
                if (this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
                    this.reader.advance(3);
                    return getRegExpText(TokenKind.RE_UNICODE_GENERAL_CATEGORY_START);
                }
                break;
            case Terminals.CLOSE_BRACE:
                this.reader.advance();
                this.endMode();
                return getRegExpToken(TokenKind.CLOSE_BRACE_TOKEN);
            default:
                break;
        }

        return processReUnicodeGeneralCategoryAbbr();
    }

    private Token processReUnicodeGeneralCategoryAbbr() {
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
                while (!isEndOfUnicodePropertyEscape()) {
                    this.reader.advance();
                }
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
        }

        return getRegExpText(TokenKind.RE_UNICODE_GENERAL_CATEGORY_NAME);
    }

    private void processAbbrWithLetter() {
        switch (peek()) {
            case 'u':
            case 'l':
            case 't':
            case 'm':
            case 'o':
                this.reader.advance();
                break;
            default:
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
        }
    }

    private void processAbbrWithMark() {
        switch (peek()) {
            case 'n':
            case 'c':
            case 'e':
                this.reader.advance();
                break;
            default:
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
        }
    }

    private void processAbbrWithNumber() {
        switch (peek()) {
            case 'd':
            case 'l':
            case 'o':
                this.reader.advance();
                break;
            default:
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
        }
    }

    private void processAbbrWithSymbol() {
        switch (peek()) {
            case 'm':
            case 'c':
            case 'k':
            case 'o':
                this.reader.advance();
                break;
            default:
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
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
                break;
            default:
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
        }
    }

    private void processAbbrWithSeparator() {
        switch (peek()) {
            case 's':
            case 'l':
            case 'p':
                this.reader.advance();
                break;
            default:
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
        }
    }

    private void processAbbrWithOther() {
        switch (peek()) {
            case 'c':
            case 'f':
            case 'o':
            case 'n':
                this.reader.advance();
                break;
            default:
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_GENERAL_CATEGORY_VALUE.messageKey(), getMarkedChars()));
        }
    }

    /**
     * Read token in ReUnicodePropertyValue.
     * <p>
     * <code>
     * ReUnicodePropertyValue := ReUnicodePropertyValueChar+
     * </code>
     *
     * @return Next token
     */
    private Token readTokenInReUnicodePropertyValue() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        processReUnicodePropertyValue();
        endMode();
        if (peek() == Terminals.CLOSE_BRACE) {
            startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
        }
        return getRegExpText(TokenKind.RE_UNICODE_PROPERTY_VALUE);
    }

    private void processReUnicodePropertyValue() {
        if (!isReUnicodePropertyValueChar(peek())) {
            this.reader.advance();
            throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                    ErrorCodes.REGEXP_INVALID_UNICODE_PROPERTY_VALUE.messageKey(), getMarkedChars()));
        }

        while (!isEndOfUnicodePropertyEscape()) {
            if (!isReUnicodePropertyValueChar(peek())) {
                this.reader.advance();
                throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                        ErrorCodes.REGEXP_INVALID_UNICODE_PROPERTY_VALUE.messageKey(), getMarkedChars()));
            }
            this.reader.advance();
        }
    }

    private boolean isEndOfUnicodePropertyEscape() {
        return peek() == Terminals.CLOSE_BRACE || this.reader.isEOF();
    }

    private boolean isReUnicodePropertyValueChar(int nextChar) {
        return isDigit(nextChar) || isAsciiLetter(nextChar) || nextChar == '_';
    }

    private static boolean isDigit(int c) {
        return ('0' <= c && c <= '9');
    }

    private boolean isAsciiLetter(int nextChar) {
        return 'A' <= nextChar && nextChar <= 'Z' || 'a' <= nextChar && nextChar <= 'z';
    }

    /**
     * Process ReEscape.
     * <p>
     * <code>
     * ReEscape :=
     * NumericEscape
     * | ControlEscape
     * | ReQuoteEscape
     * | ReUnicodePropEscape
     * | ReSimpleCharClassEscape
     * </code>
     */
    private Token processEscape() {
        switch (peek()) {
            // Handle NumericEscape.
            case 'u':
                if (this.reader.peek(1) == Terminals.OPEN_BRACE) {
                    processNumericEscape();
                    return getRegExpText(TokenKind.RE_NUMERIC_ESCAPE);
                }
                break;
            // Handle ControlEscape.
            case 'n':
            case 't':
            case 'r':
                this.reader.advance();
                return getRegExpText(TokenKind.RE_CONTROL_ESCAPE);
            // Handle ReUnicodePropertyEscape separately.
            case 'p':
            case 'P':
                startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
                break;
            default:
                break;
        }
        return getRegExpToken(TokenKind.BACK_SLASH_TOKEN);
    }

    private void processNumericEscape() {
        // Process '\ u {'
        this.reader.advance(2);

        // Process code-point.
        if (!isHexDigit(this.reader.peek())) {
            throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                    ErrorCodes.REGEXP_INVALID_HEX_DIGIT.messageKey()));
        }

        reader.advance();
        while (isHexDigit(this.reader.peek())) {
            reader.advance();
        }

        if (this.reader.peek() != Terminals.CLOSE_BRACE) {
            throw ErrorCreator.createError(ErrorHelper.getErrorMessage(
                    ErrorCodes.REGEXP_MISSING_CLOSE_BRACE.messageKey()));
        }

        this.reader.advance();
    }

    private String getMarkedChars() {
        return reader.getMarkedChars();
    }

    private Token getRegExpToken(TokenKind kind) {
        return new Token(kind);
    }

    private Token getRegExpText(TokenKind kind) {
        return new Token(kind, getMarkedChars());
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
     * Start the given operation mode of the TreeTraverser.
     *
     * @param mode Mode to switch on to
     */
    public void startMode(ParserMode mode) {
        this.mode = mode;
        this.modeStack.push(mode);
    }

    /**
     * Switch from current operation mode to the given operation mode in the TreeTraverser.
     *
     * @param mode Mode to switch on to
     */
    public void switchMode(ParserMode mode) {
        this.modeStack.pop();
        this.mode = mode;
        this.modeStack.push(mode);
    }

    /**
     * End the current mode the mode of the TreeTraverser and fall back the previous mode.
     */
    public void endMode() {
        this.modeStack.pop();
        this.mode = this.modeStack.peek();
    }

    /**
     * Check whether a given char is ReSyntaxChar.
     * <p>
     * <code>
     * ReSyntaxChar :=
     * "^" | "$" | "\" | "." | "*" | "+" | "?"
     * | "(" | ")" | "[" | "]" | "{" | "}" | "|"
     * </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is ReSyntaxChar. <code>false</code> otherwise.
     */
    private static boolean isReSyntaxChar(int c) {
        switch (c) {
            case '^':
            case '$':
            case '\\':
            case '.':
            case '*':
            case '+':
            case '?':
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case '|':
                return true;
            default:
                return false;
        }
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
    private static boolean isReSimpleCharClassCode(int c) {
        switch (c) {
            case 'd':
            case 'D':
            case 's':
            case 'S':
            case 'w':
            case 'W':
                return true;
            default:
                return false;
        }
    }

    private static boolean isHexDigit(int c) {
        return ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || isDigit(c);
    }
}
