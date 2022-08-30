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
import java.util.List;

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
            case RE_CHAR_CLASS_CHAR_SET:
                token = readTokenInReCharSet();
                break;
            case RE_FLAGS:
                token = readTokenInFlags();
                break;
            case RE_QUANTIFIER:
                token = readTokenInQuantifier();
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
                return getRegExpText(SyntaxKind.REGEXP_ASSERTION);
            case LexerTerminals.DOT:
                this.reader.advance();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpText(SyntaxKind.REGEXP_CHAR_ESCAPE);
            case LexerTerminals.BACKSLASH:
                processReEscape();
                startMode(ParserMode.RE_QUANTIFIER);
                return getRegExpText(SyntaxKind.REGEXP_CHAR_ESCAPE);
            // Handle "[" ["^"] [ReCharSet] "]".
            case LexerTerminals.OPEN_BRACKET:
                this.reader.advance();
                startMode(ParserMode.RE_CHAR_CLASS_CHAR_SET);
                if (peek() == LexerTerminals.BITWISE_XOR) {
                    this.reader.advance();
                    return getRegExpSyntaxToken(SyntaxKind.NEGATED_CHAR_CLASS_START_TOKEN);
                }
                return getRegExpSyntaxToken(SyntaxKind.OPEN_BRACKET_TOKEN);
            // Handle "(" ["?" ReFlagsOnOff ":"] ReDisjunction ")".
            case LexerTerminals.OPEN_PARANTHESIS:
                if (this.reader.peek(1) == LexerTerminals.QUESTION_MARK) {
                    startMode(ParserMode.RE_FLAGS);
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
                return getRegExpText(SyntaxKind.REGEXP_CHAR_ESCAPE);
            default:
                // Handle ReLiteralChar.
                this.reader.advance();
                if (!isReSyntaxChar(nextChar)) {
                    startMode(ParserMode.RE_QUANTIFIER);
                } else {
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                    return getRegExpText(SyntaxKind.REGEXP_TEXT);
                }
                return getRegExpText(SyntaxKind.REGEXP_CHAR_ESCAPE);
        }
    }

    /**
     * Read token from ReCharSet.
     * <p>
     * <code>
     * ReCharSet :=ReCharSetAtom
     *      | ReCharSetRange [ReCharSet]
     *      | ReCharSetAtom ReCharSetNoDash
     * </code>
     * @return Next token
     */
    private STToken readTokenInReCharSet() {
        this.reader.mark();
        if (this.reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        if (peek() == LexerTerminals.CLOSE_BRACKET) {
            endMode();
            this.reader.advance();
            startMode(ParserMode.RE_QUANTIFIER);
            return getRegExpSyntaxToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        }

        processReCharSet();
        return getRegExpText(SyntaxKind.REGEXP_CHARSET);
    }

    /**
     * Process ReCharSet.
     * <p>
     * <code>
     * ReCharSet :=
     *   ReCharSetAtom
     *   | ReCharSetRange [ReCharSet]
     *   | ReCharSetAtom ReCharSetNoDash
     * </code>
     */
    private void processReCharSet() {
        processReCharSetAtom();
        if (isEndOfCharSet()) {
            return;
        }
        if (peek() == LexerTerminals.MINUS) {
            this.reader.advance();
            processReCharSetAtom();
            if (isEndOfCharSet()) {
                return;
            }
            processReCharSet();
            return;
        }
        processReCharSetNoDash();
    }

    private boolean isEndOfCharSet() {
        return peek() == LexerTerminals.CLOSE_BRACKET || this.reader.isEOF();
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
    private void processReCharSetAtom() {
        int nextToken = peek();
        if (nextToken == LexerTerminals.MINUS) {
            this.reader.advance();
            return;
        }
        processReCharSetAtomNoDash(nextToken);
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
    private void processReCharSetAtomNoDash(int nextToken) {
        if (isReCharSetLiteralChar(nextToken)) {
            this.reader.advance();
            return;
        }
        if (nextToken == LexerTerminals.BACKSLASH) {
            if (this.reader.peek(1) == LexerTerminals.MINUS) {
                this.reader.advance(2);
                return;
            }
            processReEscape();
            return;
        }
        this.reader.advance();
        reportLexerError(DiagnosticErrorCode.ERROR_INVALID_CHARACTER_SET_IN_REG_EXP);
    }

    /**
     * Process ReCharSetNoDash.
     * <p>
     * <code>
     * ReCharSetNoDash :=
     *   ReCharSetAtom
     *   | ReCharSetRangeNoDash [ReCharSet]
     *   | ReCharSetAtomNoDash ReCharSetNoDash
     * </code>
     */
    private void processReCharSetNoDash() {
        int nextToken = peek();
        if (nextToken == LexerTerminals.MINUS) {
            this.reader.advance();
            return;
        }
        processReCharSetAtomNoDash(nextToken);
        nextToken = peek();
        if (isEndOfCharSet()) {
            return;
        }
        if (nextToken == LexerTerminals.MINUS) {
            this.reader.advance();
            processReCharSetAtom();
            if (isEndOfCharSet()) {
                return;
            }
            processReCharSet();
            return;
        }
        processReCharSetNoDash();
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

        boolean hasQuantifier = false;
        switch (peek()) {
            // Handle ReBaseQuantifier
            case LexerTerminals.ASTERISK:
            case LexerTerminals.PLUS:
            case LexerTerminals.QUESTION_MARK:
                if (reader.peek(1) == LexerTerminals.QUESTION_MARK) {
                    this.reader.advance(2);
                } else {
                    this.reader.advance();
                }
                hasQuantifier = true;
                break;
            case LexerTerminals.OPEN_BRACE:
                processReQuantifierWithDigits();
                if (peek() == LexerTerminals.QUESTION_MARK) {
                    this.reader.advance();
                }
                hasQuantifier = true;
                break;
            default:
                break;
        }

        endMode();

        if (hasQuantifier) {
            return getRegExpText(SyntaxKind.REGEXP_QUANTIFIER);
        }
        return nextToken();
    }

    /**
     * Read token in ReFlagsOnOff.
     * <p>
     * <code>
     * ReFlagsOnOff := ReFlags ["-" ReFlags]
     * </code>
     */
    private STToken readTokenInFlags() {
        this.reader.mark();

        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        if (peek() == LexerTerminals.QUESTION_MARK) {
            this.reader.advance();
            return getRegExpSyntaxToken(SyntaxKind.QUESTION_MARK_TOKEN);
        }

        if (peek() == LexerTerminals.COLON) {
            this.reader.advance();
            // Colon marks the end of the flags.
            switchMode(ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION);
            return getRegExpSyntaxToken(SyntaxKind.COLON_TOKEN);
        }

        if (!isReFlag(peek())) {
            this.reader.advance();
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_FLAG_IN_REG_EXP);
            return getRegExpText(SyntaxKind.RE_FLAGS_ON_OFF);
        }

        // Handle ReFlags ["-" ReFlags].
        while (isReFlag(peek())) {
            this.reader.advance();
        }

        if (peek() == LexerTerminals.MINUS) {
            this.reader.advance();

            while (isReFlag(peek())) {
                this.reader.advance();
            }
        }

        if (peek() != LexerTerminals.COLON) {
            // Colon marks the end of the flags.
            // reportLexerError(DiagnosticErrorCode.ERROR_INVALID_FLAG_IN_REG_EXP);
            switchMode(ParserMode.RE_CAPTURING_GROUP_RE_DISJUNCTION);
        }

        return getRegExpText(SyntaxKind.RE_FLAGS_ON_OFF);
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
     * Process ReUnicodeProperty.
     * <p>
     * <code>
     * ReUnicodeProperty :=  ReUnicodeScript | ReUnicodeGeneralCategory
     * </code>
     */
    private void processReUnicodeProperty() {
        this.reader.advance(2);

        if (peek() == 's') {
            this.reader.advance();
            // ReUnicodeScript starts with `sc=`.
            if (peek() != 'c') {
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
                return;
            }
            this.reader.advance();

            if (peek() != LexerTerminals.EQUAL) {
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
                return;
            }
            this.reader.advance();

            processReUnicodePropertyValue();
            return;
        }

        // Handle ReUnicodeGeneralCategory.
        if (peek() == 'g') {
            // ReUnicodeGeneralCategory might start with `gc=`.
            this.reader.advance();
            if (peek() != 'c') {
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
                return;
            }
            this.reader.advance();

            if (peek() != LexerTerminals.EQUAL) {
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
                return;
            }
            this.reader.advance();
        }

        processReUnicodePropertyValue();
    }

    /**
     * Process ReUnicodePropertyValue.
     * <p>
     * <code>
     * ReUnicodePropertyValue := ReUnicodePropertyValueChar+
     * </code>
     */
    private void processReUnicodePropertyValue() {
        int nextChar = peek();
        while (isDigit(nextChar) || 'A' <= nextChar && nextChar <= 'Z' || 'a' <= nextChar && nextChar <= 'z' ||
                nextChar == '_') {
            reader.advance();
            nextChar = peek();
        }

        if (peek() != LexerTerminals.CLOSE_BRACE) {
            // ReUnicodePropertyEscape should end with close brace.
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
            return;
        }

        this.reader.advance();
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
    private void processReEscape() {
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
                break;
            // Handle ControlEscape.
            case 'n':
            case 't':
            case 'r':
                this.reader.advance(2);
                break;
            // Handle ReUnicodePropertyEscape.
            case 'p':
            case 'P':
                if (this.reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                    processReUnicodeProperty();
                } else {
                    // Invalid ReUnicodePropertyEscape.
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_UNICODE_PROP_ESCAPE_IN_REG_EXP);
                    this.reader.advance(2);
                }
                break;
            default:
                // Handle ReQuoteEscape and ReSimpleCharClassEscape.
                if (isReSyntaxChar(this.reader.peek(1)) || isReSimpleCharClassCode(this.reader.peek(1))) {
                    this.reader.advance(2);
                } else {
                    // Invalid ReEscape.
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE);
                    this.reader.advance();
                }
        }
    }

    /**
     * Process ReBaseQuantifier with digits.
     */
    private void processReQuantifierWithDigits() {
        this.reader.advance();
        // Braced base quantifier should have at least one digit.
        if (!isDigit(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_QUANTIFIER_IN_REG_EXP);
            return;
        }

        while (isDigit(peek())) {
            this.reader.advance();
        }

        if (peek() == LexerTerminals.COMMA) {
            this.reader.advance();

            while (isDigit(peek())) {
                reader.advance();
            }
        }

        if (peek() != LexerTerminals.CLOSE_BRACE) {
            // Braced base quantifier should end with a close brace.
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_QUANTIFIER_IN_REG_EXP);
            return;
        }

        this.reader.advance();
    }

    private STToken getRegExpSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = processTrailingRegExpTrivia();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getRegExpText(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingRegExpTrivia();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    /**
     * Process and return trailing trivia.
     *
     * @return Trailing trivia
     */
    private STNode processTrailingRegExpTrivia() {
        List<STNode> triviaList = new ArrayList<>(10);
        processRegExpTrivia(triviaList);
        return STNodeFactory.createNodeList(triviaList);
    }

    private STToken getRegExpCloseBraceTokenWithoutTrailingWS() {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = STNodeFactory.createNodeList(new ArrayList<>(0));
        return STNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN, leadingTrivia, trailingTrivia);
    }

    /**
     * Process regExp trivia and add it to the provided list.
     * <p>
     * <code>
     * regExp-trivia := (WS | invalid-tokens)+
     * <br/><br/>
     * WS := #x20 | #x9 | #xD | #xA
     * </code>
     *
     * @param triviaList List of trivia
     */
    private void processRegExpTrivia(List<STNode> triviaList) {
        while (!reader.isEOF()) {
            reader.mark();
            char c = reader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                    triviaList.add(processWhitespaces());
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    triviaList.add(processEndOfLine());
                    return;
                default:
                    return;
            }
        }
    }

    /**
     * Process whitespace up to an end of line.
     * <p>
     * <code>whitespace := 0x9 | 0xC | 0x20</code>
     *
     * @return Whitespace trivia
     */
    private STNode processWhitespaces() {
        while (!reader.isEOF()) {
            char c = reader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    reader.advance();
                    continue;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                default:
                    break;
            }
            break;
        }

        return STNodeFactory.createMinutiae(SyntaxKind.WHITESPACE_MINUTIAE, getLexeme());
    }

    /**
     * Process end of line.
     * <p>
     * <code>end-of-line := 0xA | 0xD</code>
     *
     * @return End of line trivia
     */
    private STNode processEndOfLine() {
        char c = reader.peek();
        switch (c) {
            case LexerTerminals.NEWLINE:
                reader.advance();
                return STNodeFactory.createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE, getLexeme());
            case LexerTerminals.CARRIAGE_RETURN:
                reader.advance();
                if (reader.peek() == LexerTerminals.NEWLINE) {
                    reader.advance();
                }
                return STNodeFactory.createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE, getLexeme());
            default:
                throw new IllegalStateException();
        }
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
