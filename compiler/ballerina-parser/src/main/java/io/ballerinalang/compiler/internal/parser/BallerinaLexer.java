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
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;

/**
 * A LL(k) lexer for ballerina.
 * 
 * @since 1.2.0
 */
public class BallerinaLexer {

    private CharReader reader;
    private List<STNode> leadingTriviaList;
    private ParserMode mode = ParserMode.DEFAULT;
    private final BallerinaParserErrorListener errorListener = new BallerinaParserErrorListener();

    public BallerinaLexer(CharReader charReader) {
        this.reader = charReader;
    }

    /**
     * Get the next lexical token.
     * 
     * @return Next lexical token.
     */
    public STToken nextToken() {
        processLeadingTrivia();
        return readToken();
    }

    public void reset(int offset) {
        reader.reset(offset);
    }

    /**
     * Switch the mode of the lexer to the given mode.
     * 
     * @param mode Mode to switch on to
     */
    public void switchMode(ParserMode mode) {
        this.mode = mode;
    }

    /**
     * Switch the mode of the lexer to {@link ParserMode#DEFAULT}.
     */
    public void resetMode() {
        this.mode = ParserMode.DEFAULT;
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
                token = parseDotOrEllipsis();
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
                token = getSyntaxToken(SyntaxKind.QUESTION_MARK_TOKEN);
                break;
            case LexerTerminals.DOUBLE_QUOTE:
                token = processStringLiteral();
                break;
            case LexerTerminals.HASH:
                token = processDocumentationLine();
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
                if (peek() == LexerTerminals.GT) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.RIGHT_ARROW_TOKEN);
                } else {
                    token = getSyntaxToken(SyntaxKind.MINUS_TOKEN);
                }
                break;
            case LexerTerminals.ASTERISK:
                token = getSyntaxToken(SyntaxKind.ASTERISK_TOKEN);
                break;
            case LexerTerminals.SLASH:
                token = getSyntaxToken(SyntaxKind.SLASH_TOKEN);
                break;
            case LexerTerminals.PERCENT:
                token = getSyntaxToken(SyntaxKind.PERCENT_TOKEN);
                break;
            case LexerTerminals.LT:
                if (peek() == LexerTerminals.EQUAL) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.LT_EQUAL_TOKEN);
                } else {
                    token = getSyntaxToken(SyntaxKind.LT_TOKEN);
                }
                break;
            case LexerTerminals.GT:
                if (peek() == LexerTerminals.EQUAL) {
                    reader.advance();
                    token = getSyntaxToken(SyntaxKind.GT_EQUAL_TOKEN);
                } else {
                    token = getSyntaxToken(SyntaxKind.GT_TOKEN);
                }
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
                processInvalidToken();
                token = readToken();
                break;
        }

        return token;
    }

    private STToken getSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getIdentifierToken(String tokenText) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createIdentifierToken(lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getLiteral(SyntaxKind kind) {
        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, -1, leadingTrivia, trailingTrivia);
    }

    /**
     * Process leading trivia.
     */
    private void processLeadingTrivia() {
        this.leadingTriviaList = new ArrayList<>(10);
        processSyntaxTrivia(this.leadingTriviaList, true);
    }

    /**
     * Process and return trailing trivia.
     * 
     * @return Trailing trivia
     */
    private STNode processTrailingTrivia() {
        List<STNode> triviaList = new ArrayList<>(10);
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
        while (true) {
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
        while (true) {
            boolean done = false;
            char c = reader.peek();
            switch (c) {
                case LexerTerminals.SPACE:
                case LexerTerminals.TAB:
                case LexerTerminals.FORM_FEED:
                    reader.advance();
                    break;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    done = true;
                    break;
                default:
                    done = true;
            }
            if (done) {
                return STNodeFactory.createSyntaxTrivia(SyntaxKind.WHITESPACE_TRIVIA, getLexeme());
            }
        }
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
                return STNodeFactory.createSyntaxTrivia(SyntaxKind.END_OF_LINE_TRIVIA, getLexeme());
            case LexerTerminals.CARRIAGE_RETURN:
                reader.advance();
                if (reader.peek() == LexerTerminals.NEWLINE) {
                    reader.advance();
                }
                return STNodeFactory.createSyntaxTrivia(SyntaxKind.END_OF_LINE_TRIVIA, getLexeme());
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Process dot or ellipsis token.
     * 
     * @return Dot or ellipsis token
     */
    private STToken parseDotOrEllipsis() {
        if (reader.peek() == LexerTerminals.DOT && reader.peek(1) == LexerTerminals.DOT) {
            reader.advance(2);
            return getSyntaxToken(SyntaxKind.ELLIPSIS_TOKEN);
        }
        return getSyntaxToken(SyntaxKind.DOT_TOKEN);
    }

    /**
     * <p>
     * Process a comment, and add it to trivia list.
     * </p>
     * <code>Comment := // AnyCharButNewline*
     * <br/><br/>AnyCharButNewline := ^ 0xA</code>
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

        return STNodeFactory.createSyntaxTrivia(SyntaxKind.COMMENT, getLexeme());
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
                return getSyntaxToken(SyntaxKind.EQUAL_GT_TOKEN);
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
            return processHexIntLiteral();
        }

        int len = 1;
        while (true) {
            switch (nextChar) {
                case '.':
                case 'e':
                case 'E':
                    // In sem-var mode, only decimal integer literals are supported
                    if (this.mode == ParserMode.IMPORT) {
                        break;
                    }

                    // TODO: handle float
                    reader.advance();
                    processInvalidToken();
                    return readToken();
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
        // TODO: validate whether this is an integer, once float support is added.
        if (startChar == '0' && len > 1) {
            processInvalidToken();
            return readToken();
        }

        return getLiteral(SyntaxKind.DECIMAL_INTEGER_LITERAL);
    }

    /**
     * <p>
     * Process and returns a hex integer literal.
     * </p>
     * <code>
     * HexIntLiteral := HexIndicator HexNumber
     * <br/>
     * HexNumber := HexDigit+
     * <br/>
     * HexIndicator := 0x | 0X
     * <br/>
     * HexDigit := Digit | a .. f | A .. F
     * <br/>
     * </code>
     * 
     * @return
     */
    private STToken processHexIntLiteral() {
        reader.advance();
        while (isHexDigit(peek())) {
            reader.advance();
        }

        return getLiteral(SyntaxKind.HEX_INTEGER_LITERAL);
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
            case LexerTerminals.XMLNS:
                return getSyntaxToken(SyntaxKind.XMLNS_KEYWORD);
            default:
                return getIdentifierToken(tokenText);
        }
    }

    /**
     * Process and returns an invalid token. Consumes the input until {@link #isEndOfInvalidToken()}
     * is reached.
     * 
     * @return The invalid token.
     */
    private void processInvalidToken() {
        while (!isEndOfInvalidToken()) {
            reader.advance();
        }

        STNode trivia = STNodeFactory.createSyntaxTrivia(SyntaxKind.INVALID, getLexeme());
        this.leadingTriviaList.add(trivia);
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

        if (c == '_') {
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
    private boolean isDigit(int c) {
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
    private boolean isHexDigit(int c) {
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
        while (true) {
            nextChar = peek();
            switch (nextChar) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                    reportLexerError("missing double-quote");
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
                                reportLexerError("invalid string numeric escape sequence");
                                this.reader.advance(2);
                            }
                            continue;
                        default:
                            reportLexerError("invalid escape sequence");
                            this.reader.advance();
                            continue;
                    }
                default:
                    this.reader.advance();
                    continue;
            }
            break;
        }

        return getLiteral(SyntaxKind.STRING_LITERAL);
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
            reportLexerError("invalid string numeric escape sequence");
            return;
        }

        reader.advance();
        while (isHexDigit(peek())) {
            reader.advance();
        }

        // Process close brace
        if (peek() != LexerTerminals.CLOSE_BRACE) {
            reportLexerError("invalid string numeric escape sequence");
            return;
        }

        this.reader.advance();
    }

    private void reportLexerError(String message) {
        this.errorListener.reportInvalidNodeError(null, message);
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
     * Process and return documentation line.
     * <p>
     * <code>
     * DocumentationLine := BlankSpace* # [Space] DocumentationContent
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
     * @return Documentation line token
     */
    private STToken processDocumentationLine() {
        // TODO: validate the markdown syntax.
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

        STNode leadingTrivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createDocumentationLineToken(lexeme, leadingTrivia, trailingTrivia);
    }
}
