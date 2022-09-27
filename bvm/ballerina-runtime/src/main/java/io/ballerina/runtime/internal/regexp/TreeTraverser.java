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

import io.ballerina.runtime.internal.util.exceptions.BallerinaException;

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
    private final String errorMsgStart = "Invalid character '";
    
    public TreeTraverser(CharReader charReader) {
        this.reader = charReader;
        this.mode = ParserMode.RE_DISJUNCTION;
        this.modeStack.add(this.mode);
    }

    public Token nextToken() {
        switch (this.mode) {
            case RE_DISJUNCTION:
            case RE_CAPTURING_GROUP_RE_DISJUNCTION:
                return readTokenInReDisjunction();
            case RE_ESCAPE:
                return readTokenInReEscape();
            case RE_UNICODE_PROP_ESCAPE:
                return readTokenInReUnicodePropertyEscape();
            case RE_UNICODE_PROP_START:
                return readTokenInReUnicodePropertyStart();
            case RE_UNICODE_PROPERTY_VALUE:
                return readTokenInReUnicodePropertyValue();
            case RE_UNICODE_GENERAL_CATEGORY_NAME:
                return readTokenInReUnicodeGeneralCategoryName();
            case RE_CHAR_SET:
            case RE_NEGATED_CHAR_CLASS_START:
                return readTokenInCharacterClass();
            case RE_CHAR_SET_RANGE_RHS:
            case RE_CHAR_SET_RANGE_RHS_START:
                return readTokenInReCharSetRangeRhs();
            case RE_CHAR_SET_NO_DASH:
                return readTokenInReCharSetNoDash();
            case RE_CHAR_SET_RANGE_NO_DASH_RHS_START:
            case RE_CHAR_SET_RANGE_NO_DASH_RHS:
                return readTokenInReCharSetRangeNoDashRhs();
            case RE_FLAGS_START:
            case RE_FLAGS:
                return readTokenInFlagsExpression();
            case RE_QUANTIFIER:
            case RE_NON_GREEDY_QUANTIFIER:
                return readTokenInQuantifier();
            case RE_BRACED_QUANTIFIER:
            case RE_BRACED_QUANTIFIER_LEAST_DIGITS:
            case RE_BRACED_QUANTIFIER_MOST_DIGITS_START:
            case RE_BRACED_QUANTIFIER_MOST_DIGITS:
                return readTokenInBracedQuantifier();
            default:
                // Should not reach here.
                return null;
        }
    }

    private Token readTokenInReDisjunction() {
        reader.mark();
        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        int nextChar = peek();
        switch (nextChar) {
            case Terminals.DOLLAR:
                // Cannot have insertions.
                if (this.reader.peek(1) == Terminals.OPEN_BRACE) {
                    throw new BallerinaException("Invalid insertion with ${");
                }
                break;
            case Terminals.PIPE:
                reader.advance();
                // Pipe cannot be the end of the regular expression.
                if (this.reader.isEOF()) {
                    throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
                }
                return getRegExpToken(TokenKind.PIPE_TOKEN);
            default:
                break;
        }

        // ReSequence has zero or more ReTerm.
        return readTokenInReTerm();
    }

    private Token readTokenInReTerm() {
        reader.mark();
        if (reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        int nextChar = peek();
        switch (nextChar) {
            case Terminals.BITWISE_XOR:
            case Terminals.DOLLAR:
                this.reader.advance();
                return getRegExpText(TokenKind.RE_ASSERTION_VALUE);
            case Terminals.DOT:
                this.reader.advance();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpText(TokenKind.RE_CHAR);
            case Terminals.BACKSLASH:
                boolean processed = processOnlyNumericEscapeOrControlEscape();
                if (processed) {
                    startMode(ParserMode.RE_QUANTIFIER);
                    return getRegExpText(TokenKind.RE_ESCAPE);
                }
                this.reader.advance();
                return getRegExpToken(TokenKind.BACK_SLASH_TOKEN);
            // Handle "[" ["^"] [ReCharSet] "]".
            case Terminals.OPEN_BRACKET:
                this.reader.advance();
                if (peek() == Terminals.BITWISE_XOR) {
                    startMode(ParserMode.RE_NEGATED_CHAR_CLASS_START);
                } else {
                    startMode(ParserMode.RE_CHAR_SET);
                }
                return getRegExpToken(TokenKind.OPEN_BRACKET_TOKEN);
            // Handle "(" ["?" ReFlagsOnOff ":"] ReDisjunction ")".
            case Terminals.OPEN_PARENTHESIS:
                if (this.reader.peek(1) == Terminals.QUESTION_MARK) {
                    startMode(ParserMode.RE_FLAGS_START);
                } else {
                    startMode(ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION);
                }
                this.reader.advance();
                return getRegExpToken(TokenKind.OPEN_PAREN_TOKEN);
            case Terminals.CLOSE_PARENTHESIS:
                this.reader.advance();
                if (this.mode == ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION) {
                    switchMode(ParserMode.RE_QUANTIFIER);
                    return getRegExpToken(TokenKind.CLOSE_PAREN_TOKEN);
                }
                throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
            default:
                // Handle ReLiteralChar.
                this.reader.advance();
                if (!isReSyntaxChar(nextChar)) {
                    startMode(ParserMode.RE_QUANTIFIER);
                    return getRegExpText(TokenKind.RE_CHAR);
                }
                throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
        }
    }

    /**
     * Read tokens in ReQuoteEscape or ReSimpleCharClassEscape.
     * It can be either p, P, {, or }.
     * <p>
     * <code>
     * ReQuoteEscape := "\" ReSyntaxChar
     * </code>
     * <code>
     * ReSimpleCharClassEscape := "\" ReSimpleCharClassCode
     * </code>
     *
     * @return Next token
     */
    private Token readTokenInReEscape() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        int nextToken = peek();
        boolean isReSyntaxChar = isReSyntaxChar(nextToken);
        boolean isReSimpleCharClassCode = isReSimpleCharClassCode(nextToken);

        if (!isReSyntaxChar && !isReSimpleCharClassCode) {
            throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
        }

        this.reader.advance();
        endMode();
        switchParserModeFollowingReEscape();

        return getRegExpText(isReSyntaxChar ? TokenKind.RE_SYNTAX_CHAR : TokenKind.RE_SIMPLE_CHAR_CLASS_CODE);
    }

    private void switchParserModeFollowingReEscape() {
        switch (this.mode) {
            case RE_CHAR_SET_NO_DASH:
                // An ReCharSetAtomNoDash can be followed by - in a ReCharSetRangeNoDash.
                if (isStartOfCharSetRange()) {
                    // Switch parser mode to handle rhs of the ReCharSetRangeNoDash.
                    switchMode(ParserMode.RE_CHAR_SET_RANGE_NO_DASH_RHS_START);
                    break;
                }
                // An ReCharSetAtomNoDash can be followed by ReCharSetNoDash.
                if (peek() != Terminals.CLOSE_BRACKET) {
                    // Switch parser mode to handle ReCharSetNoDash.
                    switchMode(ParserMode.RE_CHAR_SET_NO_DASH);
                    break;
                }
                switchMode(ParserMode.RE_CHAR_SET);
                break;
            case RE_CHAR_SET:
            case RE_NEGATED_CHAR_CLASS_START:
                // An ReCharSetAtom can be followed by - in a ReCharSetRange.
                if (isStartOfCharSetRange()) {
                    // Switch parser mode to handle rhs of the ReCharSetRange.
                    switchMode(ParserMode.RE_CHAR_SET_RANGE_RHS_START);
                    break;
                }
                // An ReCharSetAtom can be followed by ReCharSetNoDash.
                if (peek() != Terminals.CLOSE_BRACKET) {
                    // Switch parser mode to handle ReCharSetNoDash.
                    switchMode(ParserMode.RE_CHAR_SET_NO_DASH);
                }
                break;
            case RE_CHAR_SET_RANGE_RHS:
            case RE_CHAR_SET_RANGE_RHS_START:
            case RE_CHAR_SET_RANGE_NO_DASH_RHS_START:
            case RE_CHAR_SET_RANGE_NO_DASH_RHS:
                switchMode(ParserMode.RE_CHAR_SET);
                break;
            default:
                startMode(ParserMode.RE_QUANTIFIER);
                break;
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

        if (peek() == Terminals.OPEN_BRACE) {
            this.reader.advance();
            switchMode(ParserMode.RE_UNICODE_PROP_START);
            return getRegExpToken(TokenKind.OPEN_BRACE_TOKEN);
        }

        if (peek() == Terminals.CLOSE_BRACE) {
            this.reader.advance();
            endMode();
            switchParserModeFollowingReEscape();
            return getRegExpToken(TokenKind.CLOSE_BRACE_TOKEN);
        }

        this.reader.advance();
        return getRegExpText(TokenKind.RE_PROPERTY);
    }

    /**
     * Read token at the start of a ReUnicodeProperty.
     * It can be either sc=, gc= or ReUnicodeGeneralCategoryName.
     *
     * @return Next token
     */
    private Token readTokenInReUnicodePropertyStart() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        if (peek() == 's' && this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
            switchMode(ParserMode.RE_UNICODE_PROPERTY_VALUE);
            this.reader.advance(3);
            return getRegExpText(TokenKind.RE_UNICODE_SCRIPT_START);
        }

        if (peek() == 'g' && this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
            switchMode(ParserMode.RE_UNICODE_GENERAL_CATEGORY_NAME);
            this.reader.advance(3);
            return getRegExpText(TokenKind.RE_UNICODE_GENERAL_CATEGORY_START);
        }

        processReUnicodePropertyValue();
        endMode();
        if (peek() == Terminals.CLOSE_BRACE) {
            startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
        }

        return getRegExpText(TokenKind.RE_UNICODE_GENERAL_CATEGORY_NAME);
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
            throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
        }

        while (!isEndOfUnicodePropertyEscape()) {
            if (!isReUnicodePropertyValueChar(peek())) {
                throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
            }
            this.reader.advance();
        }
    }

    /**
     * Read token in ReUnicodeGeneralCategoryName.
     *
     * @return Next token
     */
    private Token readTokenInReUnicodeGeneralCategoryName() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        processReUnicodePropertyValue();
        endMode();
        if (peek() == Terminals.CLOSE_BRACE) {
            startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
        }
        return getRegExpText(TokenKind.RE_UNICODE_GENERAL_CATEGORY_NAME);
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
     * Read token in character class.
     *
     * @return Next token
     */
    private Token readTokenInCharacterClass() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        switch (peek()) {
            case Terminals.BITWISE_XOR:
                // Handle negation token at the start of a character class.
                if (this.mode == ParserMode.RE_NEGATED_CHAR_CLASS_START) {
                    this.reader.advance();
                    switchMode(ParserMode.RE_CHAR_SET);
                    return getRegExpToken(TokenKind.BITWISE_XOR_TOKEN);
                }
                break;
            // Handle end of a character class.
            case Terminals.CLOSE_BRACKET:
                endMode();
                this.reader.advance();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpToken(TokenKind.CLOSE_BRACKET_TOKEN);
            default:
        }

        // Process the first ReCharSetAtom.
        boolean processed = processReCharSetAtom();
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpToken(TokenKind.BACK_SLASH_TOKEN);
        }

        // An ReCharSetAtom can be followed by - in a ReCharSetRange.
        if (isStartOfCharSetRange()) {
            // Switch parser mode to handle rhs of the ReCharSetRange.
            switchMode(ParserMode.RE_CHAR_SET_RANGE_RHS_START);
            return getRegExpText(TokenKind.RE_CHAR_SET_RANGE_LHS_CHAR_SET_ATOM);
        }

        // An ReCharSetAtom can be followed by ReCharSetNoDash.
        if (peek() != Terminals.CLOSE_BRACKET) {
            // Switch parser mode to handle ReCharSetNoDash.
            switchMode(ParserMode.RE_CHAR_SET_NO_DASH);
        }

        return getRegExpText(TokenKind.RE_CHAR_SET_ATOM);
    }

    private boolean isStartOfCharSetRange() {
        return peek() == Terminals.MINUS && this.reader.peek(1) != Terminals.CLOSE_BRACKET;
    }

    /**
     * Read "-" ReCharSetAtom tokens in rhs of a ReCharSetRange.
     *
     * @return Next token
     */
    private Token readTokenInReCharSetRangeRhs() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        // Handle "-" in ReCharSetRange.
        if (this.mode == ParserMode.RE_CHAR_SET_RANGE_RHS_START && peek() == Terminals.MINUS) {
            this.reader.advance();
            // If the close bracket is missing, "-" might be incorrectly identified as a part of ReCharSetRange.
            // Instead it should be identified as a part of ReCharSet.
            if (this.reader.isEOF()) {
                endMode();
                return getRegExpText(TokenKind.RE_CHAR_SET_ATOM);
            }
            switchMode(ParserMode.RE_CHAR_SET_RANGE_RHS);
            return getRegExpToken(TokenKind.MINUS_TOKEN);
        }

        // Process ReCharSetAtom in the rhs of a ReCharSetRange.
        boolean processed = processReCharSetAtom();
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpToken(TokenKind.BACK_SLASH_TOKEN);
        }

        // Switch parser mode to handle the following ReCharSet if available.
        switchMode(ParserMode.RE_CHAR_SET);

        return getRegExpText(TokenKind.RE_CHAR_SET_ATOM);
    }

    /**
     * Read token in ReCharSetNoDash.
     *
     * @return Next token
     */
    private Token readTokenInReCharSetNoDash() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        // Handle "-" in ReCharSetAtom which can be a ReCharSetNoDash.
        // It cannot be followed by any other char. Hence end the current parser mode.
        if (peek() == Terminals.MINUS) {
            this.reader.advance();
            switchMode(ParserMode.RE_CHAR_SET);
            return getRegExpText(TokenKind.RE_CHAR_SET_ATOM);
        }

        // Process the first ReCharSetAtomNoDash.
        boolean processed = processReCharSetAtomNoDash(peek());
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpToken(TokenKind.BACK_SLASH_TOKEN);
        }

        // An ReCharSetAtomNoDash can be followed by - in a ReCharSetRangeNoDash.
        if (isStartOfCharSetRange()) {
            // Switch parser mode to handle rhs of the ReCharSetRangeNoDash.
            switchMode(ParserMode.RE_CHAR_SET_RANGE_NO_DASH_RHS_START);
            return getRegExpText(TokenKind.RE_CHAR_SET_RANGE_NO_DASH_LHS_CHAR_SET_ATOM_NO_DASH);
        }

        // An ReCharSetAtomNoDash can be followed by ReCharSetNoDash.
        if (peek() != Terminals.CLOSE_BRACKET) {
            // Switch parser mode to handle ReCharSetNoDash.
            switchMode(ParserMode.RE_CHAR_SET_NO_DASH);
        } else {
            switchMode(ParserMode.RE_CHAR_SET);
        }

        return getRegExpText(TokenKind.RE_CHAR_SET_ATOM_NO_DASH);
    }

    /**
     * Read "-" ReCharSetAtom tokens in rhs of a ReCharSetRangeNoDash.
     *
     * @return Next token
     */
    private Token readTokenInReCharSetRangeNoDashRhs() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        // Handle "-" in ReCharSetRangeNoDash.
        if (this.mode == ParserMode.RE_CHAR_SET_RANGE_NO_DASH_RHS_START && peek() == Terminals.MINUS) {
            this.reader.advance();
            // If the close bracket is missing, "-" might be incorrectly identified as a part of ReCharSetRangeNoDash.
            // Instead it should be identified as a part of ReCharSetNoDash.
            if (this.reader.isEOF()) {
                endMode();
                return getRegExpText(TokenKind.RE_CHAR_SET_ATOM);
            }
            switchMode(ParserMode.RE_CHAR_SET_RANGE_NO_DASH_RHS);
            return getRegExpToken(TokenKind.MINUS_TOKEN);
        }

        // Process ReCharSetAtom in the rhs of a ReCharSetRangeNoDash.
        boolean processed = processReCharSetAtom();
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpToken(TokenKind.BACK_SLASH_TOKEN);
        }

        // Switch parser mode to handle the following ReCharSet if available.
        switchMode(ParserMode.RE_CHAR_SET);

        return getRegExpText(TokenKind.RE_CHAR_SET_ATOM);
    }

    /**
     * Process ReCharSetAtom.
     * <p>
     * <code>
     * ReCharSetAtom :=
     *   ReCharSetAtomNoDash
     *   | "-"
     * </code>
     */
    private boolean processReCharSetAtom() {
        int nextToken = peek();
        if (nextToken == Terminals.MINUS) {
            this.reader.advance();
            return true;
        }
        return processReCharSetAtomNoDash(nextToken);
    }

    /**
     * Process ReCharSetAtomNoDash.
     * <p>
     * <code>
     * ReCharSetAtomNoDash :=
     *   ReCharSetLiteralChar
     *   | ReEscape
     *   | "\-"
     * </code>
     */
    private boolean processReCharSetAtomNoDash(int nextToken) {
        if (isReCharSetLiteralChar(nextToken)) {
            this.reader.advance();
            return true;
        }

        if (nextToken == Terminals.BACKSLASH) {
            if (this.reader.peek(1) == Terminals.MINUS) {
                this.reader.advance(2);
                return true;
            }
            return processOnlyNumericEscapeOrControlEscape();
        }

        return true;
    }

    /**
     * Read token in ReQuantifier.
     * <p>
     * <code>
     * ReQuantifier := ReBaseQuantifier ["?"]
     * ReBaseQuantifier :=
     *    "*"
     *   | "+"
     *   | "?"
     *   | "{" Digit+ ["," Digit*] "}"
     * </code>
     */
    private Token readTokenInQuantifier() {
        reader.mark();

        if (reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        if (this.mode == ParserMode.RE_NON_GREEDY_QUANTIFIER && peek() == Terminals.QUESTION_MARK) {
            this.reader.advance();
            endMode();
            return getRegExpText(TokenKind.QUESTION_MARK_TOKEN);
        }

        switch (peek()) {
            // Handle ReBaseQuantifier
            case Terminals.ASTERISK:
            case Terminals.PLUS:
            case Terminals.QUESTION_MARK:
                this.reader.advance();
                endMode();
                if (peek() == Terminals.QUESTION_MARK) {
                    startMode(ParserMode.RE_NON_GREEDY_QUANTIFIER);
                }
                return getRegExpText(TokenKind.RE_BASE_QUANTIFIER_VALUE);
            case Terminals.OPEN_BRACE:
                this.reader.advance();
                switchMode(ParserMode.RE_BRACED_QUANTIFIER);
                return getRegExpToken(TokenKind.OPEN_BRACE_TOKEN);
            default:
                break;
        }

        endMode();
        return nextToken();
    }

    /**
     * Read token in braced quantifier.
     * <p>
     * <code>
     * ReBracedQuantifier := "{" Digit+ ["," Digit*] "}"
     * </code>
     */
    private Token readTokenInBracedQuantifier() {
        reader.mark();

        if (reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        if (this.mode == ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS_START && peek() == Terminals.COMMA) {
            this.reader.advance();
            switchMode(ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS);
            return getRegExpText(TokenKind.COMMA_TOKEN);
        }

        if ((this.mode == ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS ||
                this.mode == ParserMode.RE_BRACED_QUANTIFIER_LEAST_DIGITS) && peek() == Terminals.CLOSE_BRACE) {
            this.reader.advance();
            endMode();
            if (peek() == Terminals.QUESTION_MARK) {
                startMode(ParserMode.RE_NON_GREEDY_QUANTIFIER);
            }
            return getRegExpToken(TokenKind.CLOSE_BRACE_TOKEN);
        }

        if (!isDigit(peek())) {
            throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
        }

        this.reader.advance();

        if (this.mode == ParserMode.RE_BRACED_QUANTIFIER) {
            switchMode(ParserMode.RE_BRACED_QUANTIFIER_LEAST_DIGITS);
        }

        if (this.mode == ParserMode.RE_BRACED_QUANTIFIER_LEAST_DIGITS && peek() == Terminals.COMMA) {
            switchMode(ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS_START);
        }

        return getRegExpText(TokenKind.RE_BRACED_QUANTIFIER_DIGIT);
    }

    /**
     * Read token in ReFlagsOnOff.
     * <p>
     * <code>
     * ReFlagsOnOff := ReFlags ["-" ReFlags]
     * </code>
     */
    private Token readTokenInFlagsExpression() {
        this.reader.mark();

        if (reader.isEOF()) {
            return getRegExpToken(TokenKind.EOF_TOKEN);
        }

        if (this.mode == ParserMode.RE_FLAGS_START && peek() == Terminals.QUESTION_MARK) {
            this.reader.advance();
            switchMode(ParserMode.RE_FLAGS);
            return getRegExpToken(TokenKind.QUESTION_MARK_TOKEN);
        }

        if (peek() == Terminals.COLON) {
            this.reader.advance();
            // Colon marks the end of the flags.
            switchMode(ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION);
            return getRegExpToken(TokenKind.COLON_TOKEN);
        }

        if (this.mode == ParserMode.RE_FLAGS_START && peek() == Terminals.MINUS) {
            this.reader.advance();
            switchMode(ParserMode.RE_FLAGS);
            return getRegExpToken(TokenKind.MINUS_TOKEN);
        }

        if (!isReFlag(peek())) {
            throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
        }

        this.reader.advance();

        if (peek() == Terminals.MINUS) {
            switchMode(ParserMode.RE_FLAGS_START);
        }

        return getRegExpText(TokenKind.RE_FLAGS_VALUE);
    }

    /**
     * Process ReEscape.
     * <p>
     * <code>
     * ReEscape :=
     *    NumericEscape
     *    | ControlEscape
     *    | ReQuoteEscape
     *    | ReUnicodePropEscape
     *    | ReSimpleCharClassEscape
     * </code>
     */
    private boolean processOnlyNumericEscapeOrControlEscape() {
        switch (this.reader.peek(1)) {
            // Handle NumericEscape.
            case 'u':
                if (this.reader.peek(2) == Terminals.OPEN_BRACE) {
                    processNumericEscape();
                    return true;
                }
                break;
            // Handle ControlEscape.
            case 'n':
            case 't':
            case 'r':
                this.reader.advance(2);
                return true;
            // Handle ReUnicodePropertyEscape separately.
            case 'p':
            case 'P':
                if (this.reader.peek(2) == Terminals.OPEN_BRACE) {
                    startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
                    return false;
                }
                break;
            default:
                // Handle ReQuoteEscape and ReSimpleCharClassEscape.
                if (isReSyntaxChar(this.reader.peek(1)) || isReSimpleCharClassCode(this.reader.peek(1))) {
                    startMode(ParserMode.RE_ESCAPE);
                    return false;
                }
        }
        // Invalid ReEscape.
        this.reader.advance(2);
        throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
    }

    private void processNumericEscape() {
        // Process '\ u {'
        this.reader.advance(3);

        // Process code-point.
        if (!isHexDigit(this.reader.peek())) {
            throw new BallerinaException();
        }

        reader.advance();
        while (isHexDigit(this.reader.peek())) {
            reader.advance();
        }
        
        if (this.reader.peek() != Terminals.CLOSE_BRACE) {
            throw new BallerinaException(errorMsgStart + getMarkedChars() + "'");
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
     *   "^" | "$" | "\" | "." | "*" | "+" | "?"
     *   | "(" | ")" | "[" | "]" | "{" | "}" | "|"
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
    private static boolean isReCharSetLiteralChar(int c) {
        switch (c) {
            case '\\':
            case ']':
            case '-':
                return false;
            default:
                return true;
        }
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
    private static boolean isReFlag(int c) {
        switch (c) {
            case 'm':
            case 's':
            case 'i':
            case 'x':
                return true;
            default:
                return false;
        }
    }

    private static boolean isHexDigit(int c) {
        return ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || isDigit(c);
    }
}
