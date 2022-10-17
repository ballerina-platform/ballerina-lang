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
                token = readTokenInReDisjunction();
                break;
            case RE_ESCAPE:
                token = readTokenInReEscape();
                break;
            case RE_UNICODE_PROP_ESCAPE:
                token = readTokenInReUnicodePropertyEscape();
                break;
            case RE_UNICODE_PROPERTY_VALUE:
                token = readTokenInReUnicodePropertyValue();
                break;
            case RE_CHAR_SET:
                token = readTokenInCharacterClass();
                break;
            case RE_FLAG_EXPRESSION:
                token = readTokenInFlagExpression();
                break;
            case RE_QUANTIFIER:
                token = readTokenInQuantifier();
                break;
            case RE_BRACED_QUANTIFIER:
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
        if (nextChar == LexerTerminals.PIPE) {
            reader.advance();
            return getRegExpSyntaxToken(SyntaxKind.PIPE_TOKEN);
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
                this.reader.advance();
                return getRegExpText(SyntaxKind.RE_ASSERTION_VALUE);
            case LexerTerminals.DOLLAR:
                this.reader.advance();
                if (peek() == LexerTerminals.OPEN_BRACE) {
                    // Start interpolation mode.
                    startMode(ParserMode.INTERPOLATION);
                    this.reader.advance();
                    return getRegExpSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                return getRegExpText(SyntaxKind.RE_ASSERTION_VALUE);
            case LexerTerminals.DOT:
                this.reader.advance();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpText(SyntaxKind.DOT_TOKEN);
            case LexerTerminals.BACKSLASH:
                return processReEscape();
            // Start parsing character class [[^] [ReCharSet]].
            case LexerTerminals.OPEN_BRACKET:
                this.reader.advance();
                startMode(ParserMode.RE_CHAR_SET);
                return getRegExpSyntaxToken(SyntaxKind.OPEN_BRACKET_TOKEN);
            // Start parsing capturing group ([? ReFlagsOnOff :] ReDisjunction).
            case LexerTerminals.OPEN_PARANTHESIS:
                this.reader.advance();
                if (peek() == LexerTerminals.QUESTION_MARK) {
                    startMode(ParserMode.RE_FLAG_EXPRESSION);
                } else {
                    startMode(ParserMode.RE_DISJUNCTION);
                }
                return getRegExpSyntaxToken(SyntaxKind.OPEN_PAREN_TOKEN);
            case LexerTerminals.CLOSE_PARANTHESIS:
                this.reader.advance();
                // modeStack.size() is > 1 if the close parenthesis is found in a capturing group.
                // If not it's an invalid token.
                if (this.modeStack.size() > 1) {
                    endMode();
                    startMode(ParserMode.RE_QUANTIFIER);
                    return getRegExpSyntaxToken(SyntaxKind.CLOSE_PAREN_TOKEN);
                }
                break;
            case LexerTerminals.QUESTION_MARK:
                this.reader.advance();
                return getRegExpText(SyntaxKind.QUESTION_MARK_TOKEN);
            default:
                this.reader.advance();
                if (!isReSyntaxChar(nextChar)) {
                    startMode(ParserMode.RE_QUANTIFIER);
                    return getRegExpText(SyntaxKind.RE_LITERAL_CHAR);
                }
        }

        reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
        return getRegExpText(SyntaxKind.RE_SYNTAX_CHAR);
    }

    private STToken processReEscape() {
        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }
        int nextNextChar = this.reader.peek(1);
        switch (nextNextChar) {
            // Handle NumericEscape.
            case 'u':
                if (this.reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                    processNumericEscape();
                    return getRegExpText(SyntaxKind.RE_NUMERIC_ESCAPE);
                }
                break;
            // Handle ControlEscape.
            case 'n':
            case 't':
            case 'r':
                this.reader.advance(2);
                return getRegExpText(SyntaxKind.RE_CONTROL_ESCAPE);
            // Handle ReUnicodePropertyEscape.
            case 'p':
            case 'P':
                this.reader.advance();
                // Change the parser mode to handle ReUnicodePropertyEscape.
                startMode(ParserMode.RE_UNICODE_PROP_ESCAPE);
                break;
            default:
                this.reader.advance();
                // Change the parser mode to handle ReQuoteEscape and ReSimpleCharClassEscape.
                startMode(ParserMode.RE_ESCAPE);
                break;
        }

        return getRegExpText(SyntaxKind.BACK_SLASH_TOKEN);
    }

    /**
     * Read tokens in ReQuoteEscape or ReSimpleCharClassEscape.
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

        this.reader.advance();
        endMode();

        if (isReSyntaxChar) {
            startMode(ParserMode.RE_QUANTIFIER);
            return getRegExpText(SyntaxKind.RE_SYNTAX_CHAR);
        }

        if (isReSimpleCharClassCode) {
            startMode(ParserMode.RE_QUANTIFIER);
            return getRegExpText(SyntaxKind.RE_SIMPLE_CHAR_CLASS_CODE);
        }

        return getRegExpText(SyntaxKind.RE_LITERAL_CHAR);
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
                this.reader.advance();
                return getRegExpText(SyntaxKind.RE_PROPERTY);
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
                startMode(ParserMode.RE_QUANTIFIER);
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
                break;
            default:
                break;
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
                break;
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
                break;
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
                break;
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
                break;
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
                break;
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
                break;
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
     * Read token in character class.
     *
     * @return Next token
     */
    private STToken readTokenInCharacterClass() {
        this.reader.mark();

        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextToken = peek();
        switch (nextToken) {
            case LexerTerminals.BITWISE_XOR:
                this.reader.advance();
                // Handle negation token(^) at the start of a character class.
                return getRegExpSyntaxToken(SyntaxKind.BITWISE_XOR_TOKEN);
            case LexerTerminals.BACKSLASH:
                // Handle "\-", which is a ReCharSetAtomNoDash.
                if (this.reader.peek(1) == LexerTerminals.MINUS) {
                    this.reader.advance(2);
                    return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM_NO_DASH);
                }
                // Handle ReEscape, which is a ReCharSetAtomNoDash.
                return processReEscape();
            case LexerTerminals.MINUS:
                // Handle "-", which is a ReCharSetAtomNoDash or dash token, which is a part of either
                // ReCharSetRange or ReCharSetRangeNoDash.
                this.reader.advance();
                return getRegExpSyntaxToken(SyntaxKind.MINUS_TOKEN);
            // Handle end of a character class.
            case LexerTerminals.CLOSE_BRACKET:
                endMode();
                this.reader.advance();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
            default:
                this.reader.advance();
                // Handle ReCharSetLiteralChar, which is a ReCharSetAtomNoDash.
                if (!isReCharSetLiteralChar(nextToken)) {
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                }
                return getRegExpText(SyntaxKind.RE_CHAR_SET_ATOM_NO_DASH);
        }
    }

    /**
     * Read token in flag expression.
     * <p>
     * <code>
     * ? ReFlagsOnOff :
     * </code>
     */
    private STToken readTokenInFlagExpression() {
        this.reader.mark();

        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextToken = peek();
        this.reader.advance();
        switch (nextToken) {
            case LexerTerminals.QUESTION_MARK:
                return getRegExpSyntaxToken(SyntaxKind.QUESTION_MARK_TOKEN);
            case LexerTerminals.COLON:
                // Colon marks the end of the flags. Start RE_DISJUNCTION mode to handle proceeding sequences.
                switchMode(ParserMode.RE_DISJUNCTION);
                return getRegExpSyntaxToken(SyntaxKind.COLON_TOKEN);
            case LexerTerminals.MINUS:
                return getRegExpSyntaxToken(SyntaxKind.MINUS_TOKEN);
            default:
                if (!isReFlag(nextToken)) {
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                }
                return getRegExpText(SyntaxKind.RE_FLAGS_VALUE);
        }
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

        endMode();

        switch (peek()) {
            // Handle ReBaseQuantifier
            case LexerTerminals.ASTERISK:
            case LexerTerminals.PLUS:
                this.reader.advance();
                return getRegExpText(SyntaxKind.RE_BASE_QUANTIFIER_VALUE);
            case LexerTerminals.QUESTION_MARK:
                this.reader.advance();
                return getRegExpText(SyntaxKind.QUESTION_MARK_TOKEN);
            case LexerTerminals.OPEN_BRACE:
                // Parsing a braced quantifier is handled separately { Digit+ [, Digit*] }. Hence start
                // a new parser mode.
                this.reader.advance();
                startMode(ParserMode.RE_BRACED_QUANTIFIER);
                return getRegExpSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
            default:
                break;
        }

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

        int nextToken = peek();
        switch (nextToken) {
            case LexerTerminals.CLOSE_BRACE:
                this.reader.advance();
                endMode();
                return getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACE_TOKEN);
            case LexerTerminals.COMMA:
                this.reader.advance();
                return getRegExpSyntaxToken(SyntaxKind.COMMA_TOKEN);
            default:
                this.reader.advance();
                if (!isDigit(nextToken)) {
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                }
                return getRegExpText(SyntaxKind.RE_BRACED_QUANTIFIER_DIGIT);
        }
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
