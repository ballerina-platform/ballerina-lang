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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @Override
    public STToken nextToken() {
        STToken token;
        switch (Objects.requireNonNull(this.mode, "Lexer mode was null, an underflow occurred")) {
            case STRING:
                token = readStringToken();
                break;
            case MULTILINE_STRING:
                token = readMultilineStringToken();
                break;
            case LITERAL_STRING:
                token = readLiteralStringToken();
                break;
            case MULTILINE_LITERAL_STRING:
                token = readMultilineLiteralStringToken();
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
            case LexerTerminals.OPEN_BRACE:
                token = getSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
                break;
            case LexerTerminals.CLOSE_BRACE:
                token = getSyntaxToken(SyntaxKind.CLOSE_BRACE_TOKEN);
                break;
            case LexerTerminals.CLOSE_BRACKET:
                token = getSyntaxToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
                break;
            case LexerTerminals.SINGLE_QUOTE:
                if (this.reader.peek() == LexerTerminals.SINGLE_QUOTE &&
                        this.reader.peek(1) == LexerTerminals.SINGLE_QUOTE) {
                    this.reader.advance(2);
                    token = getQuoteToken(SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN);
                    startMode(ParserMode.MULTILINE_LITERAL_STRING);
                } else {
                    token = getQuoteToken(SyntaxKind.SINGLE_QUOTE_TOKEN);
                    startMode(ParserMode.LITERAL_STRING);
                }
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
                if (this.reader.peek() == LexerTerminals.DOUBLE_QUOTE &&
                        this.reader.peek(1) == LexerTerminals.DOUBLE_QUOTE) {
                    this.reader.advance(2);
                    token = getQuoteToken(SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN);
                    startMode(ParserMode.MULTILINE_STRING);
                } else {
                    token = getQuoteToken(SyntaxKind.DOUBLE_QUOTE_TOKEN);
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
                STToken invalidToken = processInvalidToken();
                // Use the internal method to use the already captured trivia.
                token = nextToken();
                token = SyntaxErrors.addDiagnostic(token, DiagnosticErrorCode.ERROR_INVALID_TOKEN, invalidToken);
                break;
        }

        return token;
    }

    @Nullable
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
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    endMode();
                    return getUnquotedKey();
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

    private STToken readMultilineLiteralStringToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        char nextChar = this.reader.peek();
        char secondNextChar = this.reader.peek(1);
        char thirdNextChar = this.reader.peek(2);
        if (nextChar == LexerTerminals.SINGLE_QUOTE && secondNextChar == LexerTerminals.SINGLE_QUOTE &&
                thirdNextChar == LexerTerminals.SINGLE_QUOTE) {
            endMode();
            reader.advance(3);
            return getSyntaxToken(SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN);
        }
        while (!reader.isEOF()) {
            nextChar = this.reader.peek();
            if (nextChar == LexerTerminals.SINGLE_QUOTE && this.reader.peek(1) == LexerTerminals.SINGLE_QUOTE &&
                    this.reader.peek(2) == LexerTerminals.SINGLE_QUOTE) {
                break;
            }
            reader.advance();
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

    private STToken getQuoteToken(SyntaxKind kind) {
        // Trivia after the single or double quote including whitespace belongs to the content inside the quotes.
        // Therefore do not process trailing trivia for starting quote. We reach here only for
        // starting single or double quote. Ending quote is processed by the LITERAL_STRING mode.
        STNode leadingTrivia = getLeadingTrivia();
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
        return switch (c) {
            case LexerTerminals.NEWLINE -> STNodeFactory.createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE, "\n");
            case LexerTerminals.CARRIAGE_RETURN -> {
                if (reader.peek(1) == LexerTerminals.NEWLINE) {
                    reader.advance();
                }
                yield STNodeFactory.createMinutiae(SyntaxKind.END_OF_LINE_MINUTIAE, "\r\n");
            }
            default -> throw new IllegalStateException();
        };
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
        if (isHexIndicator(startChar, nextChar)) {
            return processHexLiteral();
        }
        if (isOctalIndicator(startChar, nextChar)) {
            return processOctalLiteral();
        }
        if (isBinaryIndicator(startChar, nextChar)) {
            return processBinaryLiteral();
        }

        int len = 1;
        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.DOT:
                case 'e':
                case 'E':
                    // If there's more than one dot, only capture the integer
                    if (reader.peek(1) == LexerTerminals.DOT) {
                        break;
                    }

                    // Integer part of the float cannot have a leading zero
                    if (startChar == '0' && len > 1) {
                        reportLexerError(DiagnosticErrorCode.ERROR_LEADING_ZEROS_IN_NUMERIC_LITERALS);
                    }

                    // Code would not reach here if the floating point starts with a dot
                    return processDecimalFloatLiteral();
                default:
                    if (isAlphabeticChar(nextChar)) {
                        return processKey();
                    }
                    if (isValidNumericalDigit(nextChar)) {
                        reader.advance();
                        len++;
                        nextChar = peek();
                        continue;
                    }
                    break;
            }
            break;
        }

        // Integer cannot have a leading zero
        if (startChar == '0' && len > 1) {
            reportLexerError(DiagnosticErrorCode.ERROR_LEADING_ZEROS_IN_NUMERIC_LITERALS);
        }

        return getLiteral(SyntaxKind.DECIMAL_INT_TOKEN);
    }

    /**
     * Process Decimal Literal.
     *
     * @return Decimal Literal
     */
    private STToken processDecimalFloatLiteral() {
        int nextChar = peek();
        if (nextChar == LexerTerminals.DOT) {
            reader.advance();
            nextChar = peek();
        }

        while (isValidNumericalDigit(nextChar)) {
            reader.advance();
            nextChar = peek();
        }

        return switch (nextChar) {
            case 'e', 'E' -> processExponent();
            default -> getLiteral(SyntaxKind.DECIMAL_FLOAT_TOKEN);
        };

    }

    /**
     * Process the Hex Literal.
     *
     * @return Hex Literal
     */
    private STToken processHexLiteral() {
        reader.advance();
        while (isHexDigit(peek())) {
            reader.advance();
        }
        return getLiteral(SyntaxKind.HEX_INTEGER_LITERAL_TOKEN);
    }

    /**
     * Process the Octal Literal.
     *
     * @return Ocatal Literal
     */
    private STToken processOctalLiteral() {
        reader.advance();
        while (isOctalDigit(peek())) {
            reader.advance();
        }
        return getLiteral(SyntaxKind.OCTAL_INTEGER_LITERAL_TOKEN);
    }

    /**
     * Process the Binary Literal.
     *
     * @return Binary Literal
     */
    private STToken processBinaryLiteral() {
        reader.advance();
        while (isBinaryDigit(peek())) {
            reader.advance();
        }
        return getLiteral(SyntaxKind.BINARY_INTEGER_LITERAL_TOKEN);
    }

    /**
     * Process an exponent of Float.
     *
     * @return The decimal floating point literal.
     */
    private STToken processExponent() {
        // Advance reader as exponent indicator is already validated
        reader.advance();
        int nextChar = peek();

        // Capture if there is a sign
        if (nextChar == LexerTerminals.PLUS || nextChar == LexerTerminals.MINUS) {
            reader.advance();
            nextChar = peek();
        }

        // Make sure at least one digit is present after the indicator
        if (!isValidNumericalDigit(nextChar)) {
            reportLexerError(DiagnosticErrorCode.ERROR_MISSING_DIGIT_AFTER_EXPONENT_INDICATOR);
        }

        while (isValidNumericalDigit(nextChar)) {
            reader.advance();
            nextChar = peek();
        }

        return getLiteral(SyntaxKind.DECIMAL_FLOAT_TOKEN);
    }

    private boolean isHexIndicator(int startChar, int nextChar) {
        return startChar == '0' && (nextChar == 'x' || nextChar == 'X');
    }

    private boolean isOctalIndicator(int startChar, int nextChar) {
        return startChar == '0' && (nextChar == 'o' || nextChar == 'O');
    }

    private boolean isBinaryIndicator(int startChar, int nextChar) {
        return startChar == '0' && (nextChar == 'b' || nextChar == 'B');
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
        return switch (tokenText) {
            case LexerTerminals.TRUE -> getSyntaxToken(SyntaxKind.TRUE_KEYWORD);
            case LexerTerminals.FALSE -> getSyntaxToken(SyntaxKind.FALSE_KEYWORD);
            case LexerTerminals.INF -> getSyntaxToken(SyntaxKind.INF_TOKEN);
            case LexerTerminals.NAN -> getSyntaxToken(SyntaxKind.NAN_TOKEN);
            default -> getUnquotedKey();
        };
    }

    /**
     * Process and returns an invalid token. Consumes the input until {@link #isEndOfInvalidToken()}
     * is reached.
     */
    private STToken processInvalidToken() {
        while (!isEndOfInvalidToken()) {
            reader.advance();
        }

        String tokenText = getLexeme();
        STToken invalidToken = STNodeFactory.createInvalidToken(tokenText);
        STNode invalidNodeMinutiae = STNodeFactory.createInvalidNodeMinutiae(invalidToken);
        this.leadingTriviaList.add(invalidNodeMinutiae);
        return invalidToken;
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
        return switch (currentChar) {
            case LexerTerminals.NEWLINE,
                 LexerTerminals.CARRIAGE_RETURN,
                 LexerTerminals.SPACE,
                 LexerTerminals.TAB,
                 LexerTerminals.SEMICOLON,
                 LexerTerminals.OPEN_BRACE,
                 LexerTerminals.CLOSE_BRACE,
                 LexerTerminals.OPEN_BRACKET,
                 LexerTerminals.CLOSE_BRACKET,
                 LexerTerminals.OPEN_PARANTHESIS,
                 LexerTerminals.CLOSE_PARANTHESIS -> true;
            default -> false;
        };
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
    private boolean isAlphabeticChar(int c) {
        return 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z';
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
        return isAlphabeticChar(c) || isValidNumericalDigit(c) || c == '-';
    }

    private static boolean isValidNumericalDigit(int c) {
        return c == '_' || isDigit(c);
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
        return isValidNumericalDigit(c);
    }

    static boolean isOctalDigit(int c) {
        return c == '_' || ('0' <= c && c <= '7');
    }

    static boolean isBinaryDigit(int c) {
        return c == '0' || c == '1' || c == '_';
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
