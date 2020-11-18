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
 * A LL(k) lexer for ballerina.
 *
 * @since 1.2.0
 */
public class BallerinaLexer extends AbstractLexer {

    public BallerinaLexer(CharReader charReader) {
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
            case TEMPLATE:
                token = readTemplateToken();
                break;
            case INTERPOLATION:
                processLeadingTrivia();
                token = readTokenInInterpolation();
                break;
            case INTERPOLATION_BRACED_CONTENT:
                processLeadingTrivia();
                token = readTokenInBracedContentInInterpolation();
                break;
            case DEFAULT:
            case IMPORT:
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
            // Separators
            case LexerTerminals.COLON:
                token = getSyntaxToken(SyntaxKind.COLON_TOKEN);
                break;
            case LexerTerminals.SEMICOLON:
                token = getSyntaxToken(SyntaxKind.SEMICOLON_TOKEN);
                break;
            case LexerTerminals.DOT:
                token = processDot();
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
                if (peek() == LexerTerminals.PIPE) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.OPEN_BRACE_PIPE_TOKEN);
                } else {
                    token = getSyntaxToken(SyntaxKind.OPEN_BRACE_TOKEN);
                }
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
            case LexerTerminals.PIPE:
                token = processPipeOperator();
                break;
            case LexerTerminals.QUESTION_MARK:
                if (peek() == LexerTerminals.DOT && reader.peek(1) != LexerTerminals.DOT) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.OPTIONAL_CHAINING_TOKEN);
                } else if (peek() == LexerTerminals.COLON) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.ELVIS_TOKEN);
                } else {
                    token = getSyntaxToken(SyntaxKind.QUESTION_MARK_TOKEN);
                }
                break;
            case LexerTerminals.DOUBLE_QUOTE:
                token = processStringLiteral();
                break;
            case LexerTerminals.HASH:
                token = processDocumentationString();
                break;
            case LexerTerminals.AT:
                token = getSyntaxToken(SyntaxKind.AT_TOKEN);
                break;

            // Arithmetic operators
            case LexerTerminals.EQUAL:
                token = processEqualOperator();
                break;
            case LexerTerminals.PLUS:
                token = getSyntaxToken(SyntaxKind.PLUS_TOKEN);
                break;
            case LexerTerminals.MINUS:
                if (reader.peek() == LexerTerminals.GT) {
                    reader.advance();
                    if (peek() == LexerTerminals.GT) {
                        reader.advance();
                        token = getSyntaxToken(SyntaxKind.SYNC_SEND_TOKEN);
                    } else {
                        token = getSyntaxToken(SyntaxKind.RIGHT_ARROW_TOKEN);
                    }
                } else {
                    token = getSyntaxToken(SyntaxKind.MINUS_TOKEN);
                }
                break;
            case LexerTerminals.ASTERISK:
                token = getSyntaxToken(SyntaxKind.ASTERISK_TOKEN);
                break;
            case LexerTerminals.SLASH:
                token = processSlashToken();
                break;
            case LexerTerminals.PERCENT:
                token = getSyntaxToken(SyntaxKind.PERCENT_TOKEN);
                break;
            case LexerTerminals.LT:
                int nextChar = peek();
                if (nextChar == LexerTerminals.EQUAL) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.LT_EQUAL_TOKEN);
                } else if (nextChar == LexerTerminals.MINUS) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.LEFT_ARROW_TOKEN);
                } else if (nextChar == LexerTerminals.LT) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.DOUBLE_LT_TOKEN);
                } else {
                    token = getSyntaxToken(SyntaxKind.LT_TOKEN);
                }
                break;
            case LexerTerminals.GT:
                token = processTokenStartWithGt();
                break;
            case LexerTerminals.EXCLAMATION_MARK:
                token = processExclamationMarkOperator();
                break;
            case LexerTerminals.BITWISE_AND:
                if (peek() == LexerTerminals.BITWISE_AND) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.LOGICAL_AND_TOKEN);
                } else {
                    token = getSyntaxToken(SyntaxKind.BITWISE_AND_TOKEN);
                }
                break;
            case LexerTerminals.BITWISE_XOR:
                token = getSyntaxToken(SyntaxKind.BITWISE_XOR_TOKEN);
                break;
            case LexerTerminals.NEGATION:
                token = getSyntaxToken(SyntaxKind.NEGATION_TOKEN);
                break;
            case LexerTerminals.BACKTICK:
                startMode(ParserMode.TEMPLATE);
                token = getBacktickToken();
                break;
            case LexerTerminals.SINGLE_QUOTE:
                token = processQuotedIdentifier();
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
                token = processIdentifierOrKeyword();
                break;

            // Other
            default:
                // Process invalid token as trivia, and continue to next token
                STToken invalidToken = processInvalidToken();
                token = nextToken();
                token = SyntaxErrors.addDiagnostic(token, DiagnosticErrorCode.ERROR_INVALID_TOKEN, invalidToken);
                break;
        }

        return token;
    }

    private STToken getSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getIdentifierToken() {
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
     * @param isLeading Flag indicating whether the currently processing leading trivia or not
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
                    triviaList.add(processEndOfLine());
                    if (isLeading) {
                        break;
                    }
                    return;
                case LexerTerminals.SLASH:
                    if (reader.peek(1) == LexerTerminals.SLASH) {
                        triviaList.add(processComment());
                        break;
                    }
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
     * Process dot, ellipsis or decimal floating point token.
     *
     * @return Dot, ellipsis or decimal floating point token
     */
    private STToken processDot() {
        int nextChar = reader.peek();
        if (nextChar == LexerTerminals.DOT) {
            int nextNextChar = reader.peek(1);
            if (nextNextChar == LexerTerminals.DOT) {
                reader.advance(2);
                return getSyntaxToken(SyntaxKind.ELLIPSIS_TOKEN);
            } else if (nextNextChar == LexerTerminals.LT) {
                reader.advance(2);
                return getSyntaxToken(SyntaxKind.DOUBLE_DOT_LT_TOKEN);
            }
        } else if (nextChar == LexerTerminals.AT) {
            reader.advance();
            return getSyntaxToken(SyntaxKind.ANNOT_CHAINING_TOKEN);
        } else if (nextChar == LexerTerminals.LT) {
            reader.advance();
            return getSyntaxToken(SyntaxKind.DOT_LT_TOKEN);
        }

        if (this.mode != ParserMode.IMPORT && isDigit(nextChar)) {
            return processDecimalFloatLiteral();
        }
        return getSyntaxToken(SyntaxKind.DOT_TOKEN);
    }

    /**
     * <p>
     * Process a comment, and add it to trivia list.
     * </p>
     * <code>
     * Comment := // AnyCharButNewline*
     * <br/><br/>
     * AnyCharButNewline := ^ 0xA
     * </code>
     */
    private STNode processComment() {
        // We reach here after verifying up to 2 code-points ahead. Hence advance(2).
        reader.advance(2);
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
     * Process any token that starts with '='.
     *
     * @return One of the tokens: <code>'=', '==', '=>', '==='</code>
     */
    private STToken processEqualOperator() {
        switch (peek()) { // check for the second char
            case LexerTerminals.EQUAL:
                reader.advance();
                if (peek() == LexerTerminals.EQUAL) {
                    // this is '==='
                    reader.advance();
                    return getSyntaxToken(SyntaxKind.TRIPPLE_EQUAL_TOKEN);
                } else {
                    // this is '=='
                    return getSyntaxToken(SyntaxKind.DOUBLE_EQUAL_TOKEN);
                }
            case LexerTerminals.GT:
                // this is '=>'
                reader.advance();
                return getSyntaxToken(SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN);
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

        int len = 1;
        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.DOT:
                case 'e':
                case 'E':
                case 'f':
                case 'F':
                case 'd':
                case 'D':
                    // If there's more than one dot, only capture the integer
                    if (reader.peek(1) == LexerTerminals.DOT) {
                        break;
                    }

                    // In sem-var mode, only decimal integer literals are supported
                    if (this.mode == ParserMode.IMPORT) {
                        break;
                    }

                    // Integer part of the float cannot have a leading zero
                    if (startChar == '0' && len > 1) {
                        reportLexerError(DiagnosticErrorCode.ERROR_LEADING_ZEROS_IN_NUMERIC_LITERALS);
                    }

                    // Code would not reach here if the floating point starts with a dot
                    return processDecimalFloatLiteral();
                default:
                    if (isDigit(nextChar)) {
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

        return getLiteral(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN);
    }

    /**
     * <p>
     * Process and returns a decimal floating point literal.
     * </p>
     * <code>
     * DecimalFloatingPointNumber :=
     *    DecimalNumber Exponent [FloatingPointTypeSuffix]
     *    | DottedDecimalNumber [Exponent] [FloatingPointTypeSuffix]
     *    | DecimalNumber FloatingPointTypeSuffix
     * <br/>
     * DottedDecimalNumber := DecimalNumber . Digit* | . Digit+
     * <br/>
     * FloatingPointTypeSuffix := DecimalTypeSuffix | FloatTypeSuffix
     * <br/>
     * DecimalTypeSuffix := d | D
     * <br/>
     * FloatTypeSuffix :=  f | F
     * </code>
     *
     * @return The decimal floating point literal.
     */
    private STToken processDecimalFloatLiteral() {
        int nextChar = peek();

        // For float literals start with a DOT, this condition will always be false,
        // as the reader is already advanced for the DOT before coming here.
        if (nextChar == LexerTerminals.DOT) {
            reader.advance();
            nextChar = peek();
        }

        while (isDigit(nextChar)) {
            reader.advance();
            nextChar = peek();
        }

        switch (nextChar) {
            case 'e':
            case 'E':
                return processExponent(false);
            case 'f':
            case 'F':
            case 'd':
            case 'D':
                return parseFloatingPointTypeSuffix();
        }

        return getLiteral(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
    }

    /**
     * <p>
     * Process an exponent or hex-exponent.
     * </p>
     * <code>
     * exponent := Exponent | HexExponent
     * <br/>
     * Exponent := ExponentIndicator [Sign] Digit+
     * <br/>
     * HexExponent := HexExponentIndicator [Sign] Digit+
     * <br/>
     * ExponentIndicator := e | E
     * <br/>
     * HexExponentIndicator := p | P
     * <br/>
     * Sign := + | -
     * <br/>
     * Digit := 0 .. 9
     * </code>
     *
     * @param isHex HexExponent or not
     * @return The decimal floating point literal.
     */
    private STToken processExponent(boolean isHex) {
        // Advance reader as exponent indicator is already validated
        reader.advance();
        int nextChar = peek();

        // Capture if there is a sign
        if (nextChar == LexerTerminals.PLUS || nextChar == LexerTerminals.MINUS) {
            reader.advance();
            nextChar = peek();
        }

        // Make sure at least one digit is present after the indicator
        if (!isDigit(nextChar)) {
            reportLexerError(DiagnosticErrorCode.ERROR_MISSING_DIGIT_AFTER_EXPONENT_INDICATOR);
        }

        while (isDigit(nextChar)) {
            reader.advance();
            nextChar = peek();
        }

        if (isHex) {
            return getLiteral(SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN);
        }

        switch (nextChar) {
            case 'f':
            case 'F':
            case 'd':
            case 'D':
                return parseFloatingPointTypeSuffix();
        }

        return getLiteral(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
    }

    /**
     * <p>
     * Parse floating point type suffix.
     * </p>
     * <code>
     * FloatingPointTypeSuffix := DecimalTypeSuffix | FloatTypeSuffix
     * <br/>
     * DecimalTypeSuffix := d | D
     * <br/>
     * FloatTypeSuffix :=  f | F
     * </code>
     *
     * @return The decimal floating point literal.
     */
    private STToken parseFloatingPointTypeSuffix() {
        reader.advance();
        return getLiteral(SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN);
    }

    /**
     * <p>
     * Process and returns a hex literal.
     * </p>
     * <code>
     * hex-literal := HexIntLiteral | HexFloatingPointLiteral
     * <br/>
     * HexIntLiteral := HexIndicator HexNumber
     * <br/>
     * HexNumber := HexDigit+
     * <br/>
     * HexIndicator := 0x | 0X
     * <br/>
     * HexDigit := Digit | a .. f | A .. F
     * <br/>
     * HexFloatingPointLiteral := HexIndicator HexFloatingPointNumber
     * <br/>
     * HexFloatingPointNumber := HexNumber HexExponent | DottedHexNumber [HexExponent]
     * <br/>
     * DottedHexNumber := HexDigit+ . HexDigit* | . HexDigit+
     * </code>
     *
     * @return The hex literal.
     */
    private STToken processHexLiteral() {
        reader.advance();

        // Make sure at least one hex-digit present if processing started from a dot
        if (peek() == LexerTerminals.DOT && !isHexDigit(reader.peek(1))) {
            reportLexerError(DiagnosticErrorCode.ERROR_MISSING_HEX_DIGIT_AFTER_DOT);
        }

        int nextChar;
        while (isHexDigit(peek())) {
            reader.advance();
        }
        nextChar = peek();

        switch (nextChar) {
            case LexerTerminals.DOT:
                reader.advance();
                nextChar = peek();
                while (isHexDigit(nextChar)) {
                    reader.advance();
                    nextChar = peek();
                }
                switch (nextChar) {
                    case 'p':
                    case 'P':
                        return processExponent(true);
                }
                break;
            case 'p':
            case 'P':
                return processExponent(true);
            default:
                return getLiteral(SyntaxKind.HEX_INTEGER_LITERAL_TOKEN);
        }

        return getLiteral(SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN);
    }

    /**
     * Process and returns an identifier or a keyword.
     *
     * @return An identifier or a keyword.
     */
    private STToken processIdentifierOrKeyword() {
        while (isIdentifierFollowingChar(peek())) {
            reader.advance();
        }

        String tokenText = getLexeme();
        switch (tokenText) {
            // built-in named-types
            case LexerTerminals.INT:
                return getSyntaxToken(SyntaxKind.INT_KEYWORD);
            case LexerTerminals.FLOAT:
                return getSyntaxToken(SyntaxKind.FLOAT_KEYWORD);
            case LexerTerminals.STRING:
                return getSyntaxToken(SyntaxKind.STRING_KEYWORD);
            case LexerTerminals.BOOLEAN:
                return getSyntaxToken(SyntaxKind.BOOLEAN_KEYWORD);
            case LexerTerminals.DECIMAL:
                return getSyntaxToken(SyntaxKind.DECIMAL_KEYWORD);
            case LexerTerminals.XML:
                return getSyntaxToken(SyntaxKind.XML_KEYWORD);
            case LexerTerminals.JSON:
                return getSyntaxToken(SyntaxKind.JSON_KEYWORD);
            case LexerTerminals.HANDLE:
                return getSyntaxToken(SyntaxKind.HANDLE_KEYWORD);
            case LexerTerminals.ANY:
                return getSyntaxToken(SyntaxKind.ANY_KEYWORD);
            case LexerTerminals.ANYDATA:
                return getSyntaxToken(SyntaxKind.ANYDATA_KEYWORD);
            case LexerTerminals.NEVER:
                return getSyntaxToken(SyntaxKind.NEVER_KEYWORD);
            case LexerTerminals.BYTE:
                return getSyntaxToken(SyntaxKind.BYTE_KEYWORD);

            // Keywords
            case LexerTerminals.PUBLIC:
                return getSyntaxToken(SyntaxKind.PUBLIC_KEYWORD);
            case LexerTerminals.PRIVATE:
                return getSyntaxToken(SyntaxKind.PRIVATE_KEYWORD);
            case LexerTerminals.FUNCTION:
                return getSyntaxToken(SyntaxKind.FUNCTION_KEYWORD);
            case LexerTerminals.RETURN:
                return getSyntaxToken(SyntaxKind.RETURN_KEYWORD);
            case LexerTerminals.RETURNS:
                return getSyntaxToken(SyntaxKind.RETURNS_KEYWORD);
            case LexerTerminals.EXTERNAL:
                return getSyntaxToken(SyntaxKind.EXTERNAL_KEYWORD);
            case LexerTerminals.TYPE:
                return getSyntaxToken(SyntaxKind.TYPE_KEYWORD);
            case LexerTerminals.RECORD:
                return getSyntaxToken(SyntaxKind.RECORD_KEYWORD);
            case LexerTerminals.OBJECT:
                return getSyntaxToken(SyntaxKind.OBJECT_KEYWORD);
            case LexerTerminals.REMOTE:
                return getSyntaxToken(SyntaxKind.REMOTE_KEYWORD);
            case LexerTerminals.ABSTRACT:
                return getSyntaxToken(SyntaxKind.ABSTRACT_KEYWORD);
            case LexerTerminals.CLIENT:
                return getSyntaxToken(SyntaxKind.CLIENT_KEYWORD);
            case LexerTerminals.IF:
                return getSyntaxToken(SyntaxKind.IF_KEYWORD);
            case LexerTerminals.ELSE:
                return getSyntaxToken(SyntaxKind.ELSE_KEYWORD);
            case LexerTerminals.WHILE:
                return getSyntaxToken(SyntaxKind.WHILE_KEYWORD);
            case LexerTerminals.TRUE:
                return getSyntaxToken(SyntaxKind.TRUE_KEYWORD);
            case LexerTerminals.FALSE:
                return getSyntaxToken(SyntaxKind.FALSE_KEYWORD);
            case LexerTerminals.CHECK:
                return getSyntaxToken(SyntaxKind.CHECK_KEYWORD);
            case LexerTerminals.FAIL:
                return getSyntaxToken(SyntaxKind.FAIL_KEYWORD);
            case LexerTerminals.CHECKPANIC:
                return getSyntaxToken(SyntaxKind.CHECKPANIC_KEYWORD);
            case LexerTerminals.CONTINUE:
                return getSyntaxToken(SyntaxKind.CONTINUE_KEYWORD);
            case LexerTerminals.BREAK:
                return getSyntaxToken(SyntaxKind.BREAK_KEYWORD);
            case LexerTerminals.PANIC:
                return getSyntaxToken(SyntaxKind.PANIC_KEYWORD);
            case LexerTerminals.IMPORT:
                return getSyntaxToken(SyntaxKind.IMPORT_KEYWORD);
            case LexerTerminals.VERSION:
                return getSyntaxToken(SyntaxKind.VERSION_KEYWORD);
            case LexerTerminals.AS:
                return getSyntaxToken(SyntaxKind.AS_KEYWORD);
            case LexerTerminals.SERVICE:
                return getSyntaxToken(SyntaxKind.SERVICE_KEYWORD);
            case LexerTerminals.ON:
                return getSyntaxToken(SyntaxKind.ON_KEYWORD);
            case LexerTerminals.RESOURCE:
                return getSyntaxToken(SyntaxKind.RESOURCE_KEYWORD);
            case LexerTerminals.LISTENER:
                return getSyntaxToken(SyntaxKind.LISTENER_KEYWORD);
            case LexerTerminals.CONST:
                return getSyntaxToken(SyntaxKind.CONST_KEYWORD);
            case LexerTerminals.FINAL:
                return getSyntaxToken(SyntaxKind.FINAL_KEYWORD);
            case LexerTerminals.TYPEOF:
                return getSyntaxToken(SyntaxKind.TYPEOF_KEYWORD);
            case LexerTerminals.IS:
                return getSyntaxToken(SyntaxKind.IS_KEYWORD);
            case LexerTerminals.NULL:
                return getSyntaxToken(SyntaxKind.NULL_KEYWORD);
            case LexerTerminals.LOCK:
                return getSyntaxToken(SyntaxKind.LOCK_KEYWORD);
            case LexerTerminals.ANNOTATION:
                return getSyntaxToken(SyntaxKind.ANNOTATION_KEYWORD);
            case LexerTerminals.SOURCE:
                return getSyntaxToken(SyntaxKind.SOURCE_KEYWORD);
            case LexerTerminals.VAR:
                return getSyntaxToken(SyntaxKind.VAR_KEYWORD);
            case LexerTerminals.WORKER:
                return getSyntaxToken(SyntaxKind.WORKER_KEYWORD);
            case LexerTerminals.PARAMETER:
                return getSyntaxToken(SyntaxKind.PARAMETER_KEYWORD);
            case LexerTerminals.FIELD:
                return getSyntaxToken(SyntaxKind.FIELD_KEYWORD);
            case LexerTerminals.ISOLATED:
                return getSyntaxToken(SyntaxKind.ISOLATED_KEYWORD);
            case LexerTerminals.XMLNS:
                return getSyntaxToken(SyntaxKind.XMLNS_KEYWORD);
            case LexerTerminals.FORK:
                return getSyntaxToken(SyntaxKind.FORK_KEYWORD);
            case LexerTerminals.MAP:
                return getSyntaxToken(SyntaxKind.MAP_KEYWORD);
            case LexerTerminals.FUTURE:
                return getSyntaxToken(SyntaxKind.FUTURE_KEYWORD);
            case LexerTerminals.TYPEDESC:
                return getSyntaxToken(SyntaxKind.TYPEDESC_KEYWORD);
            case LexerTerminals.TRAP:
                return getSyntaxToken(SyntaxKind.TRAP_KEYWORD);
            case LexerTerminals.IN:
                return getSyntaxToken(SyntaxKind.IN_KEYWORD);
            case LexerTerminals.FOREACH:
                return getSyntaxToken(SyntaxKind.FOREACH_KEYWORD);
            case LexerTerminals.TABLE:
                return getSyntaxToken(SyntaxKind.TABLE_KEYWORD);
            case LexerTerminals.ERROR:
                return getSyntaxToken(SyntaxKind.ERROR_KEYWORD);
            case LexerTerminals.LET:
                return getSyntaxToken(SyntaxKind.LET_KEYWORD);
            case LexerTerminals.STREAM:
                return getSyntaxToken(SyntaxKind.STREAM_KEYWORD);
            case LexerTerminals.NEW:
                return getSyntaxToken(SyntaxKind.NEW_KEYWORD);
            case LexerTerminals.READONLY:
                return getSyntaxToken(SyntaxKind.READONLY_KEYWORD);
            case LexerTerminals.DISTINCT:
                return getSyntaxToken(SyntaxKind.DISTINCT_KEYWORD);
            case LexerTerminals.FROM:
                return getSyntaxToken(SyntaxKind.FROM_KEYWORD);
            case LexerTerminals.WHERE:
                return getSyntaxToken(SyntaxKind.WHERE_KEYWORD);
            case LexerTerminals.SELECT:
                return getSyntaxToken(SyntaxKind.SELECT_KEYWORD);
            case LexerTerminals.START:
                return getSyntaxToken(SyntaxKind.START_KEYWORD);
            case LexerTerminals.FLUSH:
                return getSyntaxToken(SyntaxKind.FLUSH_KEYWORD);
            case LexerTerminals.DEFAULT:
                return getSyntaxToken(SyntaxKind.DEFAULT_KEYWORD);
            case LexerTerminals.WAIT:
                return getSyntaxToken(SyntaxKind.WAIT_KEYWORD);
            case LexerTerminals.DO:
                return getSyntaxToken(SyntaxKind.DO_KEYWORD);
            case LexerTerminals.TRANSACTION:
                return getSyntaxToken(SyntaxKind.TRANSACTION_KEYWORD);
            case LexerTerminals.COMMIT:
                return getSyntaxToken(SyntaxKind.COMMIT_KEYWORD);
            case LexerTerminals.RETRY:
                return getSyntaxToken(SyntaxKind.RETRY_KEYWORD);
            case LexerTerminals.ROLLBACK:
                return getSyntaxToken(SyntaxKind.ROLLBACK_KEYWORD);
            case LexerTerminals.TRANSACTIONAL:
                return getSyntaxToken(SyntaxKind.TRANSACTIONAL_KEYWORD);
            case LexerTerminals.ENUM:
                return getSyntaxToken(SyntaxKind.ENUM_KEYWORD);
            case LexerTerminals.BASE16:
                return getSyntaxToken(SyntaxKind.BASE16_KEYWORD);
            case LexerTerminals.BASE64:
                return getSyntaxToken(SyntaxKind.BASE64_KEYWORD);
            case LexerTerminals.MATCH:
                return getSyntaxToken(SyntaxKind.MATCH_KEYWORD);
            case LexerTerminals.CONFLICT:
                return getSyntaxToken(SyntaxKind.CONFLICT_KEYWORD);
            case LexerTerminals.LIMIT:
                return getSyntaxToken(SyntaxKind.LIMIT_KEYWORD);
            case LexerTerminals.JOIN:
                return getSyntaxToken(SyntaxKind.JOIN_KEYWORD);
            case LexerTerminals.OUTER:
                return getSyntaxToken(SyntaxKind.OUTER_KEYWORD);
            case LexerTerminals.EQUALS:
                return getSyntaxToken(SyntaxKind.EQUALS_KEYWORD);
            case LexerTerminals.ORDER:
                return getSyntaxToken(SyntaxKind.ORDER_KEYWORD);
            case LexerTerminals.BY:
                return getSyntaxToken(SyntaxKind.BY_KEYWORD);
            case LexerTerminals.ASCENDING:
                return getSyntaxToken(SyntaxKind.ASCENDING_KEYWORD);
            case LexerTerminals.DESCENDING:
                return getSyntaxToken(SyntaxKind.DESCENDING_KEYWORD);
            case LexerTerminals.CLASS:
                return getSyntaxToken(SyntaxKind.CLASS_KEYWORD);
            default:
                return getIdentifierToken();
        }
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
        if ('A' <= c && c <= 'Z') {
            return true;
        }

        if ('a' <= c && c <= 'z') {
            return true;
        }

        if (c == '_') {
            return true;
        }

        return isUnicodeIdentifierChar(c);
    }

    /**
     * <p>
     * Check whether a given char is a unicode identifier char.
     * </p>
     * <code> UnicodeIdentifierChar := ^ ( AsciiChar | UnicodeNonIdentifierChar ) </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is a unicode identifier char. <code>false</code> otherwise.
     */
    private boolean isUnicodeIdentifierChar(int c) {
        // check ASCII char range
        if (0x0000 <= c && c <= 0x007F) {
            return false;
        }

        // check UNICODE private use char
        if (isUnicodePrivateUseChar(c) || isUnicodePatternWhiteSpaceChar(c)) {
            return false;
        }

        // TODO: if (UnicodePatternSyntaxChar) return false
        return (c != Character.MAX_VALUE);
    }

    /**
     * <p>
     * Check whether a given char is a unicode pattern white space char.
     * </p>
     * <code> UnicodePatternWhiteSpaceChar := 0x200E | 0x200F | 0x2028 | 0x2029 </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is a unicode pattern white space char. <code>false</code> otherwise.
     */
    private boolean isUnicodePatternWhiteSpaceChar(int c) {
        return (0x200E == c || 0x200F == c || 0x2028 == c || 0x2029 == c);
    }

    /**
     * <p>
     * Check whether a given char is a unicode private use char.
     * </p>
     * <code> UnicodePrivateUseChar := 0xE000 .. 0xF8FF | 0xF0000 .. 0xFFFFD | 0x100000 .. 0x10FFFD </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is a unicode private use char. <code>false</code> otherwise.
     */
    private boolean isUnicodePrivateUseChar(int c) {
        return (0xE000 <= c && c <= 0xF8FF || 0xF0000 <= c && c <= 0xFFFFD || 0x100000 <= c && c <= 0x10FFFD);
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
     * Process and return double-quoted string literal.
     * <p>
     * <code>string-literal := DoubleQuotedStringLiteral
     * <br/>
     * DoubleQuotedStringLiteral := " (StringChar | StringEscape)* "
     * <br/>
     * StringChar := ^ ( 0xA | 0xD | \ | " )
     * <br/>
     * StringEscape := StringSingleEscape | StringNumericEscape
     * <br/>
     * StringSingleEscape := \t | \n | \r | \\ | \"
     * <br/>
     * StringNumericEscape := \ u{ CodePoint }
     * <br/>
     * CodePoint := HexDigit+
     * </code>
     *
     * @return String literal token
     */
    private STToken processStringLiteral() {
        int nextChar;
        while (!reader.isEOF()) {
            nextChar = peek();
            switch (nextChar) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                    reportLexerError(DiagnosticErrorCode.ERROR_MISSING_DOUBLE_QUOTE);
                    break;
                case LexerTerminals.DOUBLE_QUOTE:
                    this.reader.advance();
                    break;
                case LexerTerminals.BACKSLASH:
                    switch (this.reader.peek(1)) {
                        case 'n':
                        case 't':
                        case 'r':
                        case LexerTerminals.BACKSLASH:
                        case LexerTerminals.DOUBLE_QUOTE:
                            this.reader.advance(2);
                            continue;
                        case 'u':
                            if (this.reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                                processStringNumericEscape();
                            } else {
                                reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE);
                                this.reader.advance(2);
                            }
                            continue;
                        default:
                            String escapeSequence = String.valueOf((char) this.reader.peek(2));
                            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE, escapeSequence);
                            this.reader.advance();
                            continue;
                    }
                default:
                    this.reader.advance();
                    continue;
            }
            break;
        }

        return getLiteral(SyntaxKind.STRING_LITERAL_TOKEN);
    }

    /**
     * Process string numeric escape.
     * <p>
     * <code>StringNumericEscape := \ u { CodePoint }</code>
     */
    private void processStringNumericEscape() {
        // Process '\ u {'
        this.reader.advance(3);

        // Process code-point
        if (!isHexDigit(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE);
            return;
        }

        reader.advance();
        while (isHexDigit(peek())) {
            reader.advance();
        }

        // Process close brace
        if (peek() != LexerTerminals.CLOSE_BRACE) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE);
            return;
        }

        this.reader.advance();
    }

    /**
     * Process any token that starts with '!'.
     *
     * @return One of the tokens: <code>'!', '!=', '!=='</code>
     */
    private STToken processExclamationMarkOperator() {
        switch (peek()) { // check for the second char
            case LexerTerminals.EQUAL:
                reader.advance();
                if (peek() == LexerTerminals.EQUAL) {
                    // this is '!=='
                    reader.advance();
                    return getSyntaxToken(SyntaxKind.NOT_DOUBLE_EQUAL_TOKEN);
                } else {
                    // this is '!='
                    return getSyntaxToken(SyntaxKind.NOT_EQUAL_TOKEN);
                }
            default:
                // this is '!'
                return getSyntaxToken(SyntaxKind.EXCLAMATION_MARK_TOKEN);
        }
    }

    /**
     * Process any token that starts with '|'.
     *
     * @return One of the tokens: <code>'|', '|}', '||'</code>
     */
    private STToken processPipeOperator() {
        switch (peek()) { // check for the second char
            case LexerTerminals.CLOSE_BRACE:
                reader.advance();
                return getSyntaxToken(SyntaxKind.CLOSE_BRACE_PIPE_TOKEN);
            case LexerTerminals.PIPE:
                reader.advance();
                return getSyntaxToken(SyntaxKind.LOGICAL_OR_TOKEN);
            default:
                return getSyntaxToken(SyntaxKind.PIPE_TOKEN);
        }
    }

    /**
     * Process any token that starts with '/'.
     *
     * @return One of the tokens: <code>'/', '/*', '/**\/<' </code>
     */
    private STToken processSlashToken() {
        // check for the second char
        if (peek() != LexerTerminals.ASTERISK) {
            return getSyntaxToken(SyntaxKind.SLASH_TOKEN);
        }

        reader.advance();
        if (peek() != LexerTerminals.ASTERISK) {
            return getSyntaxToken(SyntaxKind.SLASH_ASTERISK_TOKEN);
        } else if (reader.peek(1) == LexerTerminals.SLASH && reader.peek(2) == LexerTerminals.LT) {
            reader.advance(3);
            return getSyntaxToken(SyntaxKind.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN);
        } else {
            return getSyntaxToken(SyntaxKind.SLASH_ASTERISK_TOKEN);
        }
    }

    /**
     * Process and return documentation string.
     * <p>
     * <code>
     * DocumentationContentString := ( BlankSpace* # [DocumentationContent] )+
     * <br/>
     * DocumentationContent := (^ 0xA)* 0xA
     * <br/>
     * BlankSpace := Tab | Space
     * <br/>
     * Space := 0x20
     * <br/>
     * Tab := 0x9
     * </code>
     *
     * @return Documentation string token
     */
    private STToken processDocumentationString() {
        int nextChar = peek();
        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:

                    // Advance reader for the new line
                    if (peek() == LexerTerminals.CARRIAGE_RETURN && reader.peek(1) == LexerTerminals.NEWLINE) {
                        reader.advance();
                    }
                    reader.advance();

                    // Look ahead and see if next line also belongs to the documentation.
                    // i.e. look for a `WS #` match
                    // If there's a match, advance reader for the next line as well.
                    // Otherwise terminate documentation content after the new line.
                    int lookAheadCount = 0;
                    int lookAheadChar = reader.peek(lookAheadCount);
                    while (lookAheadChar == LexerTerminals.SPACE || lookAheadChar == LexerTerminals.TAB) {
                        lookAheadCount++;
                        lookAheadChar = reader.peek(lookAheadCount);
                    }

                    if (lookAheadChar != LexerTerminals.HASH) {
                        // Next line does not belong to documentation, hence break
                        break;
                    }

                    reader.advance(lookAheadCount);
                    nextChar = peek();
                    continue;
                default:
                    reader.advance();
                    nextChar = peek();
                    continue;
            }
            break;
        }

        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = STNodeFactory.createNodeList(new ArrayList<>(0)); // No trailing trivia
        return STNodeFactory.createLiteralValueToken(SyntaxKind.DOCUMENTATION_STRING, lexeme, leadingTrivia,
                trailingTrivia);
    }

    private STToken getBacktickToken() {
        STNode leadingTrivia = getLeadingTrivia();
        // Trivia after the back-tick including whitespace belongs to the content of the back-tick.
        // Therefore do not process trailing trivia for starting back-tick. We reach here only for
        // starting back-tick. Ending back-tick is processed by the template mode.
        STNode trailingTrivia = STNodeFactory.createEmptyNodeList();
        return STNodeFactory.createToken(SyntaxKind.BACKTICK_TOKEN, leadingTrivia, trailingTrivia);
    }

    private STToken readTemplateToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        char nextChar = this.reader.peek();
        switch (nextChar) {
            case LexerTerminals.BACKTICK:
                reader.advance();
                endMode();
                return getSyntaxToken(SyntaxKind.BACKTICK_TOKEN);
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    // Switch to interpolation mode. Then the next token will be read in that mode.
                    startMode(ParserMode.INTERPOLATION);
                    reader.advance(2);

                    return getSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                // fall through
            default:
                while (!reader.isEOF()) {
                    nextChar = this.reader.peek();
                    switch (nextChar) {
                        case LexerTerminals.DOLLAR:
                            if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                                break;
                            }
                            reader.advance();
                            continue;
                        case LexerTerminals.BACKTICK:
                            break;
                        default:
                            reader.advance();
                            continue;
                    }

                    break;
                }
        }

        return getLiteral(SyntaxKind.TEMPLATE_STRING);
    }

    /**
     * Process quoted Identifier token.
     * 
     * <code>
     * QuotedIdentifierChar := IdentifierFollowingChar | QuotedIdentifierEscape | StringNumericEscape
     * </code>
     * 
     * @return Quoted identifier token
     */
    private STToken processQuotedIdentifier() {
        while (!reader.isEOF()) {
            int nextChar = reader.peek();
            if (isIdentifierFollowingChar(nextChar)) {
                reader.advance();
                continue;
            }

            if (nextChar != '\\') {
                break;
            }

            // QuotedIdentifierEscape | StringNumericEscape

            nextChar = reader.peek(1);
            switch (nextChar) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.TAB:
                    break;
                case 'u':
                    // StringNumericEscape
                    if (reader.peek(2) == '{') {
                        processStringNumericEscape();
                    } else {
                        reader.advance(2);
                    }
                    continue;
                default:
                    if (!isValidQuotedIdentifierEscapeChar(nextChar)) {
                        String escapeSequence = String.valueOf((char) nextChar);
                        reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ESCAPE_SEQUENCE, escapeSequence);
                    }

                    reader.advance(2);
                    continue;
            }
            break;
        }

        return getIdentifierToken();
    }

    private boolean isValidQuotedIdentifierEscapeChar(int nextChar) {
        // ASCII letters are not allowed
        if ('A' <= nextChar && nextChar <= 'Z') {
            return false;
        }

        if ('a' <= nextChar && nextChar <= 'z') {
            return false;
        }

        // Unicode pattern white space characters are not allowed
        return !isUnicodePatternWhiteSpaceChar(nextChar);
    }

    private STToken processTokenStartWithGt() {
        if (peek() == LexerTerminals.EQUAL) {
            reader.advance();
            return getSyntaxToken(SyntaxKind.GT_EQUAL_TOKEN);
        }

        if (reader.peek() != LexerTerminals.GT) {
            return getSyntaxToken(SyntaxKind.GT_TOKEN);
        }

        char nextChar = reader.peek(1);
        switch (nextChar) {
            case LexerTerminals.GT:
                if (reader.peek(2) == LexerTerminals.EQUAL) {
                    // ">>>="
                    reader.advance(2);
                    return getSyntaxToken(SyntaxKind.TRIPPLE_GT_TOKEN);
                }
                return getSyntaxToken(SyntaxKind.GT_TOKEN);
            case LexerTerminals.EQUAL:
                // ">>="
                reader.advance(1);
                return getSyntaxToken(SyntaxKind.DOUBLE_GT_TOKEN);
            default:
                return getSyntaxToken(SyntaxKind.GT_TOKEN);
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * INTERPOLATION Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInInterpolation() {
        reader.mark();
        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.OPEN_BRACE:
                // Start braced-content mode. This is to keep track of the
                // open-brace and the corresponding close-brace. This way,
                // those will not be mistaken as the close-brace of the
                // interpolation end.
                startMode(ParserMode.INTERPOLATION_BRACED_CONTENT);
                return readToken();
            case LexerTerminals.CLOSE_BRACE:
                // Close-brace in the interpolation mode definitely means its
                // then end of the interpolation.
                endMode();
                reader.advance();
                return getSyntaxTokenWithoutTrailingTrivia(SyntaxKind.CLOSE_BRACE_TOKEN);
            case LexerTerminals.BACKTICK:
                // If we are inside the interpolation, that means its no longer XML
                // mode, but in the default mode. Hence treat the back-tick in the
                // same way as in the default mode.
            default:
                // Otherwise read the token from default mode.
                return readToken();
        }
    }

    private STToken getSyntaxTokenWithoutTrailingTrivia(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = STNodeFactory.createNodeList(new ArrayList<>(0));
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * INTERPOLATION_BRACED_CONTENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInBracedContentInInterpolation() {
        reader.mark();
        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.OPEN_BRACE:
                startMode(ParserMode.INTERPOLATION_BRACED_CONTENT);
                break;
            case LexerTerminals.CLOSE_BRACE:
            case LexerTerminals.BACKTICK:
                endMode();
                break;
            default:
                // Otherwise read the token from default mode.
                break;
        }

        return readToken();
    }
}
