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

package io.ballerina.toml.internal.parser;

import io.ballerina.toml.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.toml.internal.parser.tree.STNode;
import io.ballerina.toml.internal.parser.tree.STNodeFactory;
import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.CharReader;

import java.util.ArrayList;
import java.util.List;

/**
 * A LL(k) lexer for TOML.
 *
 * @since 2.0.0
 */
public class TomlLexer extends AbstractLexer {

    public TomlLexer(CharReader charReader) {
        super(charReader, ParserMode.DEFAULT);
    }

    /**
     * Get the next lexical token.
     *
     * @return Next lexical token.
     */
    public STToken nextToken() {
        STToken token;
        switch (this.mode) {
            case STRING:
                token = readStringToken();
                break;
            case MULTILINE_STRING:
                token = readMultilineStringToken();
                break;
            case LITERAL_STRING:
                token = readLiteralStringToken();
                break;
            case NEW_LINE:
                token = readNewlineToken();
                if (token == null) {
                    token = nextToken();
                }
                break;
            case DEFAULT:
            default:
                processLeadingTrivia();
                token = readToken();
        }

        // Can we improve this logic by creating the token with diagnostics then and there?
        return cloneWithDiagnostics(token);
    }

    /*
     * Private Methods
     */
    private STToken readToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int c = reader.peek();
        reader.advance();
        STToken token;
        switch (c) {
            case LexerTerminals.NEWLINE:
            case LexerTerminals.CARRIAGE_RETURN:
                token = getNewlineToken();
                break;
            case LexerTerminals.OPEN_BRACKET:
                token = getSyntaxToken(SyntaxKind.OPEN_BRACKET_TOKEN);
                break;
            case LexerTerminals.CLOSE_BRACKET:
                token = getSyntaxToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                break;
            case LexerTerminals.SINGLE_QUOTE:
                if (this.reader.peek(1) == LexerTerminals.SINGLE_QUOTE) {
                    this.reader.advance(2);
                    token = getDoubleQuoteToken(SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN);
                } else {
                    token = getDoubleQuoteToken(SyntaxKind.SINGLE_QUOTE_TOKEN);
                }
                startMode(ParserMode.LITERAL_STRING);
                break;
            // Arithmetic operators
            case LexerTerminals.EQUAL:
                token = getSyntaxToken(SyntaxKind.EQUAL_TOKEN);
                break;
            case LexerTerminals.COMMA:
                token = getSyntaxToken(SyntaxKind.COMMA_TOKEN);
                break;
            case LexerTerminals.DOT:
                token = getSyntaxToken(SyntaxKind.DOT_TOKEN);
                break;
            case LexerTerminals.PLUS:
                token = getSyntaxToken(SyntaxKind.PLUS_TOKEN);
                break;
            case LexerTerminals.MINUS:
                token = getSyntaxToken(SyntaxKind.MINUS_TOKEN);
                break;
            case LexerTerminals.DOUBLE_QUOTE:
                if (this.reader.peek(1) == LexerTerminals.DOUBLE_QUOTE) {
                    this.reader.advance(2);
                    token = getDoubleQuoteToken(SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN);
                    startMode(ParserMode.MULTILINE_STRING);
                } else {
                    token = getDoubleQuoteToken(SyntaxKind.DOUBLE_QUOTE_TOKEN);
                    startMode(ParserMode.STRING);
                }
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
                token = processNumericLiteral(c);
                break;

            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case '_':
                token = processKey();
                break;

            // Other
            default:
                // Process invalid token as trivia, and continue to next token
                processInvalidToken();
                // Use the internal method to use the already captured trivia.
                token = nextToken();
                break;
        }

        return token;
    }

    private STToken readNewlineToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getSyntaxToken(SyntaxKind.EOF_TOKEN);
        }
        int c = reader.peek();
        endMode();
        if (c == LexerTerminals.NEWLINE || c == LexerTerminals.CARRIAGE_RETURN) {
            reader.advance();
            return getNewlineToken();
        }
        return null;
    }

    private STToken readStringToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        char nextChar = this.reader.peek();
        if (nextChar == LexerTerminals.DOUBLE_QUOTE) {
            endMode();
            reader.advance();
            return getSyntaxToken(SyntaxKind.DOUBLE_QUOTE_TOKEN);
        }
        while (!reader.isEOF()) {
            nextChar = this.reader.peek();
            switch (nextChar) {
                case LexerTerminals.DOUBLE_QUOTE:
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    endMode();
                    return getUnquotedKey();
                case LexerTerminals.BACKSLASH:
                    switch (this.reader.peek(1)) {
                        case 'n':
                        case 'r':
                            endMode();
                            continue;
                        case 't':
                        case LexerTerminals.BACKSLASH:
                        case LexerTerminals.DOUBLE_QUOTE:
                            this.reader.advance(2);
                            continue;
                        case 'u':
                        case 'U':
                            processStringNumericEscape();
                            continue;
                        default:
                            String escapeSequence = String.valueOf(this.reader.peek(2));
                            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE, escapeSequence);
                            this.reader.advance();
                            continue;
                    }
                default:
                    reader.advance();
                    continue;
            }
            break;
        }
        return getUnquotedKey();
    }

    private STToken readLiteralStringToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        char nextChar = this.reader.peek();
        if (nextChar == LexerTerminals.SINGLE_QUOTE) {
            endMode();
            reader.advance();
            return getSyntaxToken(SyntaxKind.SINGLE_QUOTE_TOKEN);
        }
        while (!reader.isEOF()) {
            nextChar = this.reader.peek();
            switch (nextChar) {
                case LexerTerminals.SINGLE_QUOTE:
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    break;
                default:
                    reader.advance();
                    continue;
            }
            break;
        }
        return getUnquotedKey();
    }

    private STToken readMultilineStringToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        char nextChar = this.reader.peek();
        char secondNextChar = this.reader.peek(1);
        char thirdNextChar = this.reader.peek(2);
        if (nextChar == LexerTerminals.DOUBLE_QUOTE && secondNextChar == LexerTerminals.DOUBLE_QUOTE && thirdNextChar
                == LexerTerminals.DOUBLE_QUOTE) {
            endMode();
            reader.advance(3);
            return getSyntaxToken(SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN);
        }
        while (!reader.isEOF()) {
            nextChar = this.reader.peek();
            if (nextChar == LexerTerminals.DOUBLE_QUOTE && this.reader.peek(1) == LexerTerminals.DOUBLE_QUOTE &&
                    this.reader.peek(2) == LexerTerminals.DOUBLE_QUOTE) {
                break;
            }
            if (nextChar != LexerTerminals.BACKSLASH) {
                reader.advance();
                continue;
            }
            switch (this.reader.peek(1)) {
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    reader.advance();
                    continue;
                case 'n':
                case 't':
                case 'r':
                case LexerTerminals.BACKSLASH:
                case LexerTerminals.DOUBLE_QUOTE:
                    this.reader.advance(2);
                    continue;
                case 'u':
                case 'U':
                    processStringNumericEscape();
                    continue;
                default:
                    String escapeSequence = String.valueOf(this.reader.peek(2));
                    reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE, escapeSequence);
                    this.reader.advance();
            }
        }
        return getUnquotedKey();
    }

    private STToken getNewlineToken() {
        STNode leadingTrivia = STNodeFactory.createEmptyNodeList();
        STNode trailingTrivia = STNodeFactory.createEmptyNodeList();
        return STNodeFactory.createToken(SyntaxKind.NEWLINE, leadingTrivia, trailingTrivia);
    }

    private STToken getSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getUnquotedKey() {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createIdentifierToken(lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getLiteral(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getDoubleQuoteToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        // Trivia after the back-tick including whitespace belongs to the content of the back-tick.
        // Therefore do not process trailing trivia for starting back-tick. We reach here only for
        // starting back-tick. Ending back-tick is processed by the template mode.
        STNode trailingTrivia = STNodeFactory.createEmptyNodeList();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    /**
     * Process leading trivia.
     */
    private void processLeadingTrivia() {
        processSyntaxTrivia(this.leadingTriviaList, true);
    }

    /**
     * Process and return trailing trivia.
     *
     * @return Trailing trivia
     */
    private STNode processTrailingTrivia() {
        List<STNode> triviaList = new ArrayList<>(INITIAL_TRIVIA_CAPACITY);
        processSyntaxTrivia(triviaList, false);
        return STNodeFactory.createNodeList(triviaList);
    }

    /**
     * Process syntax trivia and add it to the provided list.
     * <p>
     * <code>syntax-trivia := whitespace | end-of-line | comments</code>
     *
     * @param triviaList List of trivia
     * @param isLeading  Flag indicating whether the currently processing leading trivia or not
     */
    private void processSyntaxTrivia(List<STNode> triviaList, boolean isLeading) {
        while (!reader.isEOF()) {
            reader.mark();
            char c = reader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    triviaList.add(processWhitespaces());
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    if (!isLeading) {
                        startMode(ParserMode.NEW_LINE);
                    }
                    triviaList.add(processEndOfLine());
                    return;
                case LexerTerminals.HASH:
                    triviaList.add(processComment());
                    break;
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
                    break;
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
                return STNodeFactory.createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE, "\n");
            case LexerTerminals.CARRIAGE_RETURN:
                if (reader.peek(1) == LexerTerminals.NEWLINE) {
                    reader.advance();
                }
                return STNodeFactory.createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE, "\r\n");
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * <p>
     * Process a comment, and add it to trivia list.
     * </p>
     * <code>
     * Comment := #AnyCharButNewline*
     * <br/><br/>
     * AnyCharButNewline := ^ 0xA
     * </code>
     */
    private STNode processComment() {
        reader.advance();
        int nextToken = peek();
        while (!reader.isEOF()) {
            switch (nextToken) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                    break;
                default:
                    reader.advance();
                    nextToken = peek();
                    continue;
            }
            break;
        }

        return STNodeFactory.createMinutiae(SyntaxKind.COMMENT_MINUTIAE, getLexeme());
    }

    /**
     * <p>
     * Process and returns a numeric literal.
     * </p>
     * <code>
     * numeric-literal := int-literal | floating-point-literal
     * <br/>
     * floating-point-literal := DecimalFloatingPointNumber | HexFloatingPointLiteral
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
        int nextChar = peek();
        if (nextChar == '+' || nextChar == '-') {
            reader.advance();
            nextChar = peek();
        }

        int len = 1;
        SyntaxKind type = SyntaxKind.DECIMAL_INT_TOKEN;
        boolean isString = false;
        while (!reader.isEOF()) {
            if (isDigit(nextChar) || nextChar == '.' || nextChar == '_') {
                if (nextChar == '.') {
                    type = SyntaxKind.DECIMAL_FLOAT_TOKEN;
                }
                reader.advance();
                len++;
                nextChar = peek();
                continue;
            } else if (Character.isLetter(nextChar)) {
                isString = true;
            }
            break;
        }
        if (isString) {
            type = SyntaxKind.IDENTIFIER_LITERAL;
        }
        // Integer cannot have a leading zero
        if (startChar == '0' && len > 1) {
            reportLexerError(DiagnosticErrorCode.ERROR_LEADING_ZEROS_IN_NUMERIC_LITERALS);
        }

        return getLiteral(type);
    }

    /**
     * Process and returns an identifier or a keyword.
     *
     * @return An identifier or a keyword.
     */
    private STToken processKey() {
        while (isIdentifierFollowingChar(peek())) {
            reader.advance();
        }

        String tokenText = getLexeme();
        switch (tokenText) {
            case LexerTerminals.TRUE:
                return getSyntaxToken(SyntaxKind.TRUE_KEYWORD);
            case LexerTerminals.FALSE:
                return getSyntaxToken(SyntaxKind.FALSE_KEYWORD);
            case LexerTerminals.INF:
                return getSyntaxToken(SyntaxKind.INF_TOKEN);
            case LexerTerminals.NAN:
                return getSyntaxToken(SyntaxKind.NAN_TOKEN);
            default:
                return getUnquotedKey();
        }
    }

    /**
     * Process and returns an invalid token. Consumes the input until {@link #isEndOfInvalidToken()}
     * is reached.
     */
    private void processInvalidToken() {
        while (!isEndOfInvalidToken()) {
            reader.advance();
        }

        String tokenText = getLexeme();
        STNode invalidToken = STNodeFactory.createInvalidToken(tokenText);
        STNode invalidNodeMinutiae = STNodeFactory.createInvalidNodeMinutiae(invalidToken);
        this.leadingTriviaList.add(invalidNodeMinutiae);
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
        if (reader.isEOF()) {
            return true;
        }

        int currentChar = peek();
        switch (currentChar) {
            case LexerTerminals.NEWLINE:
            case LexerTerminals.CARRIAGE_RETURN:
            case LexerTerminals.SPACE:
            case LexerTerminals.TAB:
            case LexerTerminals.SEMICOLON:
            case LexerTerminals.OPEN_BRACE:
            case LexerTerminals.CLOSE_BRACE:
            case LexerTerminals.OPEN_BRACKET:
            case LexerTerminals.CLOSE_BRACKET:
            case LexerTerminals.OPEN_PARANTHESIS:
            case LexerTerminals.CLOSE_PARANTHESIS:
                // TODO: add all separators (braces, parentheses, etc)
                // TODO: add all operators (arithmetic, binary, etc)
                return true;
            default:
                return false;
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

        if (c == '_' || c == '-') {
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
    static boolean isDigit(int c) {
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
    static boolean isHexDigit(int c) {
        if ('a' <= c && c <= 'f') {
            return true;
        }
        if ('A' <= c && c <= 'F') {
            return true;
        }
        return isDigit(c);
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
     * Process string numeric escape.
     * <p>
     * <code>StringNumericEscape := \u00E9 </code>
     */
    private void processStringNumericEscape() {
        this.reader.advance(2);

        // Process code-point
        if (!isHexDigit(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE);
            return;
        }

        reader.advance();
        while (isHexDigit(peek())) {
            reader.advance();
        }
        this.reader.advance();
    }

}
