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
            case RE_ATOM_RE_DISJUNCTION:
                token = readTokenInReDisjunction();
                break;
            case RE_ATOM_CHAR_SET:
            case RE_CHAR_SET_RANGE:
                token = readTokenInReCharSet();
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
        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = reader.peek();
        switch (nextChar) {
            case LexerTerminals.BACKTICK:
                // End of regular expression.
                endMode();
                return nextToken();
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    // Start interpolation mode.
                    startMode(ParserMode.INTERPOLATION);
                    reader.advance(2);
                    return getRegExpSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            case LexerTerminals.PIPE:
                reader.advance();
                return getRegExpText();
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
                startMode(ParserMode.RE_ASSERTION);
                reader.advance();
                break;
            case LexerTerminals.DOT:
                startMode(ParserMode.RE_ATOM);
                reader.advance();
                processReBaseQuantifier();
                break;
            case LexerTerminals.PIPE:
                reader.advance();
                return getRegExpText();
            case LexerTerminals.BACKSLASH:
                processReEscape(false);
                break;
            // Handle "[" ["^"] [ReCharSet] "]".
            case LexerTerminals.OPEN_BRACKET:
                startMode(ParserMode.RE_ATOM_CHAR_SET);
                return readTokenInReCharSet();
            // Handle "(" ["?" ReFlagsOnOff ":"] ReDisjunction ")".
            case LexerTerminals.OPEN_PARANTHESIS:
                startMode(ParserMode.RE_ATOM_RE_DISJUNCTION);
                if (this.reader.peek(1) == LexerTerminals.QUESTION_MARK) {
                    reader.advance(2);
                    processReFlags();
                    return getRegExpText();
                }
                this.reader.advance();
                return getRegExpText();
            case LexerTerminals.CLOSE_PARANTHESIS:
                this.reader.advance();
                if (this.mode == ParserMode.RE_ATOM_RE_DISJUNCTION) {
                    endMode();
                    processReBaseQuantifier();
                    return getRegExpText();
                }
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN, nextChar);
                break;
            case LexerTerminals.DOLLAR:
                // Handle Interpolation.
                if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    startMode(ParserMode.INTERPOLATION);
                    reader.advance(2);
                    return getRegExpSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                startMode(ParserMode.RE_ASSERTION);
                reader.advance();
                break;
            default:
                // Handle ReLiteralChar.
                if (!isReSyntaxChar(nextChar)) {
                    startMode(ParserMode.RE_ATOM);
                    this.reader.advance();
                    processReBaseQuantifier();
                    break;
                }
                this.reader.advance();
                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
                return getRegExpText();
        }

        endMode();
        return getRegExpText();
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
        reader.mark();
        if (reader.isEOF()) {
            return getRegExpSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.BITWISE_XOR:
                this.reader.advance();
                break;
            case LexerTerminals.DOLLAR:
                if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    // Start interpolation mode.
                    startMode(ParserMode.INTERPOLATION);
                    this.reader.advance(2);
                    return getRegExpSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
        }

        while (!this.reader.isEOF()) {
            switch (peek()) {
                case LexerTerminals.DOLLAR:
                    if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                        return getRegExpText();
                    }
                    this.reader.advance();
                    continue;
                case LexerTerminals.CLOSE_BRACKET:
                    this.reader.advance();
                    break;
                case LexerTerminals.BACKSLASH:
                    if (this.reader.peek(1) == LexerTerminals.MINUS) {
                        this.reader.advance(2);
                        continue;
                    }
                    processReEscape(true);
                    continue;
                case LexerTerminals.MINUS:
                    this.reader.advance();
                    switchMode(ParserMode.RE_CHAR_SET_RANGE);
                    continue;
                case LexerTerminals.OPEN_BRACKET:
                    this.reader.advance();
                    continue;
                default:
                    if (isReCharSetLiteralChar(peek())) {
                        this.reader.advance();
                        continue;
                    }
            }

            break;
        }

        endMode();
        processReBaseQuantifier();
        return getRegExpText();
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
            return getRegExpSyntaxTokenWithoutTrailingWS();
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
        this.reader.advance(3);

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
    private void processReEscape(boolean inCharSet) {
        switch (this.reader.peek(1)) {
            // Handle NumericEscape.
            case 'u':
                if (this.reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                    startReAtomMode(inCharSet);
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
                startReAtomMode(inCharSet);
                this.reader.advance(2);
                break;
            // Handle ReUnicodePropertyEscape.
            case 'p':
            case 'P':
                if (this.reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                    startReAtomMode(inCharSet);
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
                    startReAtomMode(inCharSet);
                    this.reader.advance(2);
                } else {
                    // Invalid ReEscape.
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE);
                    this.reader.advance();
                }
        }
    }

    /**
     * Process ReFlagsOnOff.
     * <p>
     * <code>
     * ReFlagsOnOff := ReFlags ["-" ReFlags]
     * </code>
     */
    private void processReFlags() {
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
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_FLAG_IN_REG_EXP);
        }

        reader.advance();
    }

    /**
     * Process numeric escape.
     * <p>
     * <code>NumericEscape := \ u { CodePoint }</code>
     */
    private void processNumericEscape() {
        // Process '\ u {'
        this.reader.advance(3);

        // Process code-point.
        if (!isHexDigit(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE, peek());
            return;
        }

        reader.advance();
        while (isHexDigit(peek())) {
            reader.advance();
        }

        // Process close brace.
        if (peek() != LexerTerminals.CLOSE_BRACE) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE, peek());
            return;
        }

        this.reader.advance();
    }

    /**
     * Process ReBaseQuantifier.
     * <p>
     * <code>
     * ReBaseQuantifier :=
     *    "*"
     *   | "+"
     *   | "?"
     *   | "{" Digit+ ["," Digit*] "}"
     * </code>
     */
    private void processReBaseQuantifier() {
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
                break;
            case LexerTerminals.OPEN_BRACE:
                processReQuantifierWithDigits();
                if (peek() == LexerTerminals.QUESTION_MARK) {
                    this.reader.advance();
                }
                break;
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

        reader.advance();
        while (isDigit(peek())) {
            reader.advance();
        }

        if (peek() == LexerTerminals.COMMA) {
            reader.advance();

            while (isDigit(peek())) {
                reader.advance();
            }
        }

        if (peek() != LexerTerminals.CLOSE_BRACE) {
            // Braced base quantifier should end with a close brace.
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_QUANTIFIER_IN_REG_EXP, peek());
            return;
        }

        reader.advance();
    }


    private void startReAtomMode(boolean inCharSet) {
        if (!inCharSet) {
            startMode(ParserMode.RE_ATOM);
        }
    }

    private STToken getRegExpText() {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingRegExpTrivia();
        return STNodeFactory.createLiteralValueToken(SyntaxKind.TEMPLATE_STRING, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getRegExpSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = processTrailingRegExpTrivia();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
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

    private STToken getRegExpSyntaxTokenWithoutTrailingWS() {
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
}
