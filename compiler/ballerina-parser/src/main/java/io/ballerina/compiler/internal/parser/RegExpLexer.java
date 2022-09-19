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
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
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
            case RE_CAPTURING_GROUP_RE_DISJUNCTION:
                token = readTokenInReDisjunction();
                break;
            case RE_ESCAPE:
                token = readTokenInReEscape();
                break;
            case RE_UNICODE_PROP_ESCAPE:
                token = readTokenInReUnicodePropertyEscape();
                break;
            case RE_UNICODE_PROP_START:
                token = readTokenInReUnicodePropertyStart();
                break;
            case RE_UNICODE_PROPERTY_VALUE:
                token = readTokenInReUnicodePropertyValue();
                break;
            case RE_UNICODE_GENERAL_CATEGORY_NAME:
                token = readTokenInReUnicodeGeneralCategoryName();
                break;
            case RE_CHAR_SET:
            case RE_NEGATED_CHAR_CLASS_START:
                token = readTokenInCharacterClass();
                break;
            case RE_CHAR_SET_RANGE_RHS:
            case RE_CHAR_SET_RANGE_RHS_START:
                token = readTokenInReCharSetRangeRhs();
                break;
            case RE_CHAR_SET_NO_DASH:
                token = readTokenInReCharSetNoDash();
                break;
            case RE_CHAR_SET_RANGE_NO_DASH_RHS_START:
            case RE_CHAR_SET_RANGE_NO_DASH_RHS:
                token = readTokenInReCharSetRangeNoDashRhs();
                break;
            case RE_FLAGS_START:
            case RE_FLAGS:
                token = readTokenInFlagsExpression();
                break;
            case RE_QUANTIFIER:
            case RE_NON_GREEDY_QUANTIFIER:
                token = readTokenInQuantifier();
                break;
            case RE_BRACED_QUANTIFIER:
            case RE_BRACED_QUANTIFIER_LEAST_DIGITS:
            case RE_BRACED_QUANTIFIER_MOST_DIGITS_START:
            case RE_BRACED_QUANTIFIER_MOST_DIGITS:
                token = readTokenInBracedQuantifier();
                break;
            case INTERPOLATION:
                token = readTokenInInterpolation();
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
     *
     * @return Next token
     */
    private STToken readTokenInReDisjunction() {
        reader.mark();
        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.DOLLAR:
                if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    // Start interpolation mode.
                    startMode(ParserMode.INTERPOLATION);
                    this.reader.advance(2);
                    return getRegExpSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            case LexerTerminals.PIPE:
                reader.advance();
                // Pipe cannot be the end of the regular expression.
                if (this.reader.isEOF()) {
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                }
                return getRegExpSyntaxToken(SyntaxKind.PIPE_TOKEN);
            default:
                break;
        }

        // ReSequence has zero or more ReTerm.
        return readTokenInReTerm();
    }

    /**
     * Read token from ReTerm.
     * <p>
     * <code>
     * ReTerm :=
     *    ReAtom [ReQuantifier]
     *    | ReAssertion
     * </code>
     *
     * @return Next token
     */
    private STToken readTokenInReTerm() {
        reader.mark();
        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.BITWISE_XOR:
            case LexerTerminals.DOLLAR:
                this.reader.advance();
                return getRegExpText(SyntaxKind.RE_ASSERTION_VALUE);
            case LexerTerminals.DOT:
                this.reader.advance();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpText(SyntaxKind.RE_CHAR);
            case LexerTerminals.BACKSLASH:
                boolean processed = processOnlyNumericEscapeOrControlEscape();
                if (processed) {
                    startMode(ParserMode.RE_QUANTIFIER);
                    return getRegExpText(SyntaxKind.RE_ESCAPE);
                }
                this.reader.advance();
                return getRegExpSyntaxToken(SyntaxKind.BACK_SLASH_TOKEN);
            // Handle "[" ["^"] [ReCharSet] "]".
            case LexerTerminals.OPEN_BRACKET:
                this.reader.advance();
                if (peek() == LexerTerminals.BITWISE_XOR) {
                    startMode(ParserMode.RE_NEGATED_CHAR_CLASS_START);
                } else {
                    startMode(ParserMode.RE_CHAR_SET);
                }
                return getRegExpSyntaxToken(SyntaxKind.OPEN_BRACKET_TOKEN);
            // Handle "(" ["?" ReFlagsOnOff ":"] ReDisjunction ")".
            case LexerTerminals.OPEN_PARANTHESIS:
                if (this.reader.peek(1) == LexerTerminals.QUESTION_MARK) {
                    startMode(ParserMode.RE_FLAGS_START);
                } else {
                    startMode(ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION);
                }
                this.reader.advance();
                return getRegExpSyntaxToken(SyntaxKind.OPEN_PAREN_TOKEN);
            case LexerTerminals.CLOSE_PARANTHESIS:
                this.reader.advance();
                if (this.mode == ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION) {
                    switchMode(ParserMode.RE_QUANTIFIER);
                    return getRegExpSyntaxToken(SyntaxKind.CLOSE_PAREN_TOKEN);
                }
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                return getRegExpText(SyntaxKind.RE_CHAR);
            default:
                // Handle ReLiteralChar.
                this.reader.advance();
                if (!isReSyntaxChar(nextChar)) {
                    startMode(ParserMode.RE_QUANTIFIER);
                } else {
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                }
                return getRegExpText(SyntaxKind.RE_CHAR);
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
    private STToken readTokenInReEscape() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextToken = peek();
        boolean isReSyntaxChar = isReSyntaxChar(nextToken);
        boolean isReSimpleCharClassCode = isReSimpleCharClassCode(nextToken);

        if (!isReSyntaxChar && !isReSimpleCharClassCode) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE);
        }

        this.reader.advance();
        endMode();
        switchParserModeFollowingReEscape();

        if (isReSyntaxChar) {
            return getRegExpText(SyntaxKind.RE_SYNTAX_CHAR);
        }

        if (isReSimpleCharClassCode) {
            return getRegExpText(SyntaxKind.RE_SIMPLE_CHAR_CLASS_CODE);
        }

        return getRegExpText(SyntaxKind.RE_CHAR);
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
                if (peek() != LexerTerminals.CLOSE_BRACKET) {
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
                if (peek() != LexerTerminals.CLOSE_BRACKET) {
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
    private STToken readTokenInReUnicodePropertyEscape() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        if (peek() == LexerTerminals.OPEN_BRACE) {
            this.reader.advance();
            switchMode(ParserMode.RE_UNICODE_PROP_START);
            return getRegExpSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
        }

        if (peek() == LexerTerminals.CLOSE_BRACE) {
            this.reader.advance();
            endMode();
            switchParserModeFollowingReEscape();
            return getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        }

        this.reader.advance();
        return getRegExpText(SyntaxKind.RE_PROPERTY);
    }

    /**
     * Read token at the start of a ReUnicodeProperty.
     * It can be either sc=, gc= or ReUnicodeGeneralCategoryName.
     *
     * @return Next token
     */
    private STToken readTokenInReUnicodePropertyStart() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        if (peek() == 's' && this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
            switchMode(ParserMode.RE_UNICODE_PROPERTY_VALUE);
            this.reader.advance(3);
            return getRegExpText(SyntaxKind.RE_UNICODE_SCRIPT_START);
        }

        if (peek() == 'g' && this.reader.peek(1) == 'c' && this.reader.peek(2) == '=') {
            switchMode(ParserMode.RE_UNICODE_GENERAL_CATEGORY_NAME);
            this.reader.advance(3);
            return getRegExpText(SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_START);
        }

        processReUnicodePropertyValue();
        endMode();
        if (peek() == LexerTerminals.CLOSE_BRACE) {
            startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
        }

        return getRegExpText(SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_NAME);
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
    private STToken readTokenInReUnicodePropertyValue() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        processReUnicodePropertyValue();
        endMode();
        if (peek() == LexerTerminals.CLOSE_BRACE) {
            startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
        }
        return getRegExpText(SyntaxKind.RE_UNICODE_PROPERTY_VALUE);
    }

    private void processReUnicodePropertyValue() {
        if (!isReUnicodePropertyValueChar(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
            this.reader.advance();
        }

        while (!isEndOfUnicodePropertyEscape()) {
            if (!isReUnicodePropertyValueChar(peek())) {
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
            }
            this.reader.advance();
        }
    }

    /**
     * Read token in ReUnicodeGeneralCategoryName.
     *
     * @return Next token
     */
    private STToken readTokenInReUnicodeGeneralCategoryName() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        processReUnicodePropertyValue();
        endMode();
        if (peek() == LexerTerminals.CLOSE_BRACE) {
            startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
        }
        return getRegExpText(SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_NAME);
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
     * Read token in character class.
     *
     * @return Next token
     */
    private STToken readTokenInCharacterClass() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        switch (peek()) {
            case LexerTerminals.BITWISE_XOR:
                // Handle negation token at the start of a character class.
                if (this.mode == ParserMode.RE_NEGATED_CHAR_CLASS_START) {
                    this.reader.advance();
                    switchMode(ParserMode.RE_CHAR_SET);
                    return getRegExpSyntaxToken(SyntaxKind.BITWISE_XOR_TOKEN);
                }
                break;
            // Handle end of a character class.
            case LexerTerminals.CLOSE_BRACKET:
                endMode();
                this.reader.advance();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        }

        // Process the first ReCharSetAtom.
        boolean processed = processReCharSetAtom();
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpSyntaxToken(SyntaxKind.BACK_SLASH_TOKEN);
        }

        // An ReCharSetAtom can be followed by - in a ReCharSetRange.
        if (isStartOfCharSetRange()) {
            // Switch parser mode to handle rhs of the ReCharSetRange.
            switchMode(ParserMode.RE_CHAR_SET_RANGE_RHS_START);
            return getRegExpText(SyntaxKind.RE_CHAR_SET_RANGE_LHS_CHAR_SET_ATOM);
        }

        // An ReCharSetAtom can be followed by ReCharSetNoDash.
        if (peek() != LexerTerminals.CLOSE_BRACKET) {
            // Switch parser mode to handle ReCharSetNoDash.
            switchMode(ParserMode.RE_CHAR_SET_NO_DASH);
        }

        return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM);
    }

    private boolean isStartOfCharSetRange() {
        return peek() == LexerTerminals.MINUS && this.reader.peek(1) != LexerTerminals.CLOSE_BRACKET;
    }

    /**
     * Read "-" ReCharSetAtom tokens in rhs of a ReCharSetRange.
     *
     * @return Next token
     */
    private STToken readTokenInReCharSetRangeRhs() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        // Handle "-" in ReCharSetRange.
        if (this.mode == ParserMode.RE_CHAR_SET_RANGE_RHS_START && peek() == LexerTerminals.MINUS) {
            this.reader.advance();
            // If the close bracket is missing, "-" might be incorrectly identified as a part of ReCharSetRange.
            // Instead it should be identified as a part of ReCharSet.
            if (this.reader.isEOF()) {
                endMode();
                return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM);
            }
            switchMode(ParserMode.RE_CHAR_SET_RANGE_RHS);
            return getRegExpSyntaxToken(SyntaxKind.MINUS_TOKEN);
        }

        // Process ReCharSetAtom in the rhs of a ReCharSetRange.
        boolean processed = processReCharSetAtom();
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpSyntaxToken(SyntaxKind.BACK_SLASH_TOKEN);
        }

        // Switch parser mode to handle the following ReCharSet if available.
        switchMode(ParserMode.RE_CHAR_SET);

        return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM);
    }

    /**
     * Read token in ReCharSetNoDash.
     *
     * @return Next token
     */
    private STToken readTokenInReCharSetNoDash() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        // Handle "-" in ReCharSetAtom which can be a ReCharSetNoDash.
        // It cannot be followed by any other char. Hence end the current parser mode.
        if (peek() == LexerTerminals.MINUS) {
            this.reader.advance();
            switchMode(ParserMode.RE_CHAR_SET);
            return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM);
        }

        // Process the first ReCharSetAtomNoDash.
        boolean processed = processReCharSetAtomNoDash(peek());
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpSyntaxToken(SyntaxKind.BACK_SLASH_TOKEN);
        }

        // An ReCharSetAtomNoDash can be followed by - in a ReCharSetRangeNoDash.
        if (isStartOfCharSetRange()) {
            // Switch parser mode to handle rhs of the ReCharSetRangeNoDash.
            switchMode(ParserMode.RE_CHAR_SET_RANGE_NO_DASH_RHS_START);
            return getRegExpText(SyntaxKind.RE_CHAR_SET_RANGE_NO_DASH_LHS_CHAR_SET_ATOM_NO_DASH);
        }

        // An ReCharSetAtomNoDash can be followed by ReCharSetNoDash.
        if (peek() != LexerTerminals.CLOSE_BRACKET) {
            // Switch parser mode to handle ReCharSetNoDash.
            switchMode(ParserMode.RE_CHAR_SET_NO_DASH);
        } else {
            switchMode(ParserMode.RE_CHAR_SET);
        }

        return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM_NO_DASH);
    }

    /**
     * Read "-" ReCharSetAtom tokens in rhs of a ReCharSetRangeNoDash.
     *
     * @return Next token
     */
    private STToken readTokenInReCharSetRangeNoDashRhs() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        // Handle "-" in ReCharSetRangeNoDash.
        if (this.mode == ParserMode.RE_CHAR_SET_RANGE_NO_DASH_RHS_START && peek() == LexerTerminals.MINUS) {
            this.reader.advance();
            // If the close bracket is missing, "-" might be incorrectly identified as a part of ReCharSetRangeNoDash.
            // Instead it should be identified as a part of ReCharSetNoDash.
            if (this.reader.isEOF()) {
                endMode();
                return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM);
            }
            switchMode(ParserMode.RE_CHAR_SET_RANGE_NO_DASH_RHS);
            return getRegExpSyntaxToken(SyntaxKind.MINUS_TOKEN);
        }

        // Process ReCharSetAtom in the rhs of a ReCharSetRangeNoDash.
        boolean processed = processReCharSetAtom();
        if (!processed) {
            // Handle ReEscapes separately.
            this.reader.advance();
            return getRegExpSyntaxToken(SyntaxKind.BACK_SLASH_TOKEN);
        }

        // Switch parser mode to handle the following ReCharSet if available.
        switchMode(ParserMode.RE_CHAR_SET);

        return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM);
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
        if (peek() == LexerTerminals.MINUS) {
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

        if (nextToken == LexerTerminals.BACKSLASH) {
            if (this.reader.peek(1) == LexerTerminals.MINUS) {
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
    private STToken readTokenInQuantifier() {
        reader.mark();

        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        if (this.mode == ParserMode.RE_NON_GREEDY_QUANTIFIER && peek() == LexerTerminals.QUESTION_MARK) {
            this.reader.advance();
            endMode();
            return getRegExpText(SyntaxKind.QUESTION_MARK_TOKEN);
        }

        switch (peek()) {
            // Handle ReBaseQuantifier
            case LexerTerminals.ASTERISK:
            case LexerTerminals.PLUS:
            case LexerTerminals.QUESTION_MARK:
                this.reader.advance();
                endMode();
                if (peek() == LexerTerminals.QUESTION_MARK) {
                    startMode(ParserMode.RE_NON_GREEDY_QUANTIFIER);
                }
                return getRegExpText(SyntaxKind.RE_BASE_QUANTIFIER_VALUE);
            case LexerTerminals.OPEN_BRACE:
                this.reader.advance();
                switchMode(ParserMode.RE_BRACED_QUANTIFIER);
                return getRegExpSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
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
    private STToken readTokenInBracedQuantifier() {
        reader.mark();

        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        if (this.mode == ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS_START && peek() == LexerTerminals.COMMA) {
            this.reader.advance();
            switchMode(ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS);
            return getRegExpText(SyntaxKind.COMMA_TOKEN);
        }

        if ((this.mode == ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS ||
                this.mode == ParserMode.RE_BRACED_QUANTIFIER_LEAST_DIGITS) && peek() == LexerTerminals.CLOSE_BRACE) {
            this.reader.advance();
            endMode();
            if (peek() == LexerTerminals.QUESTION_MARK) {
                startMode(ParserMode.RE_NON_GREEDY_QUANTIFIER);
            }
            return getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        }

        if (!isDigit(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_QUANTIFIER_IN_REG_EXP);
        }

        this.reader.advance();

        if (this.mode == ParserMode.RE_BRACED_QUANTIFIER) {
            switchMode(ParserMode.RE_BRACED_QUANTIFIER_LEAST_DIGITS);
        }

        if (this.mode == ParserMode.RE_BRACED_QUANTIFIER_LEAST_DIGITS && peek() == LexerTerminals.COMMA) {
            switchMode(ParserMode.RE_BRACED_QUANTIFIER_MOST_DIGITS_START);
        }

        return getRegExpText(SyntaxKind.RE_BRACED_QUANTIFIER_DIGIT);
    }

    /**
     * Read token in ReFlagsOnOff.
     * <p>
     * <code>
     * ReFlagsOnOff := ReFlags ["-" ReFlags]
     * </code>
     */
    private STToken readTokenInFlagsExpression() {
        this.reader.mark();

        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        if (this.mode == ParserMode.RE_FLAGS_START && peek() == LexerTerminals.QUESTION_MARK) {
            this.reader.advance();
            switchMode(ParserMode.RE_FLAGS);
            return getRegExpSyntaxToken(SyntaxKind.QUESTION_MARK_TOKEN);
        }

        if (peek() == LexerTerminals.COLON) {
            this.reader.advance();
            // Colon marks the end of the flags.
            switchMode(ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION);
            return getRegExpSyntaxToken(SyntaxKind.COLON_TOKEN);
        }

        if (this.mode == ParserMode.RE_FLAGS_START && peek() == LexerTerminals.MINUS) {
            this.reader.advance();
            switchMode(ParserMode.RE_FLAGS);
            return getRegExpSyntaxToken(SyntaxKind.MINUS_TOKEN);
        }

        if (!isReFlag(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_FLAG_IN_REG_EXP);
        }

        this.reader.advance();

        if (peek() == LexerTerminals.MINUS) {
            switchMode(ParserMode.RE_FLAGS_START);
        }

        return getRegExpText(SyntaxKind.RE_FLAGS_VALUE);
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
            startMode(ParserMode.RE_QUANTIFIER);
            reader.advance();
            return getRegExpCloseBraceTokenWithoutTrailingWS();
        }

        // Fail-safe.
        endMode();
        return nextToken();
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
                if (this.reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                    processNumericEscape();
                } else {
                    // Invalid numeric escape.
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE);
                    this.reader.advance(2);
                }
                return true;
            // Handle ControlEscape.
            case 'n':
            case 't':
            case 'r':
                this.reader.advance(2);
                return true;
            // Handle ReUnicodePropertyEscape separately.
            case 'p':
            case 'P':
                if (this.reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                    startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
                    return false;
                }
                // Invalid ReUnicodePropertyEscape.
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
                this.reader.advance(2);
                return true;
            default:
                // Handle ReQuoteEscape and ReSimpleCharClassEscape.
                if (isReSyntaxChar(this.reader.peek(1)) || isReSimpleCharClassCode(this.reader.peek(1))) {
                    startMode(ParserMode.RE_ESCAPE);
                    return false;
                }
                // Invalid ReEscape.
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE);
                this.reader.advance();
                return true;
        }
    }

    private STToken getRegExpSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createEmptyNodeList();
        STNode trailingTrivia = STNodeFactory.createEmptyNodeList();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getRegExpText(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createEmptyNodeList();
        String lexeme = getLexeme();
        STNode trailingTrivia = STNodeFactory.createEmptyNodeList();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getRegExpCloseBraceTokenWithoutTrailingWS() {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = STNodeFactory.createNodeList(new ArrayList<>(0));
        return STNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN, leadingTrivia, trailingTrivia);
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
    protected static boolean isReSyntaxChar(int c) {
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
    protected static boolean isReSimpleCharClassCode(int c) {
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
    protected static boolean isReCharSetLiteralChar(int c) {
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
    protected static boolean isReFlag(int c) {
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
}
