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
 * A LL(k) lexer for XML in ballerina.
 * 
 * @since 2.0.0
 */
public class XMLLexer extends AbstractLexer {

    public XMLLexer(CharReader charReader) {
        super(charReader, ParserMode.XML_CONTENT);
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
            case XML_CONTENT:
                token = readTokenInXMLContent();
                break;
            case XML_ELEMENT_START_TAG:
                processLeadingXMLTrivia();
                token = readTokenInXMLElement(true);
                break;
            case XML_ELEMENT_END_TAG:
                processLeadingXMLTrivia();
                token = readTokenInXMLElement(false);
                break;
            case XML_TEXT:
                // XML text have no trivia. Whitespace is part of the text.
                token = readTokenInXMLText();
                break;
            case INTERPOLATION:
                token = readTokenInInterpolation();
                break;
            case XML_ATTRIBUTES:
                processLeadingXMLTrivia();
                token = readTokenInXMLAttributes(true);
                break;
            case XML_COMMENT:
                token = readTokenInXMLComment();
                break;
            case XML_PI:
                processLeadingXMLTrivia();
                token = readTokenInXMLPI();
                break;
            case XML_PI_DATA:
                processLeadingXMLTrivia();
                token = readTokenInXMLPIData();
                break;
            case XML_SINGLE_QUOTED_STRING:
                token = processXMLSingleQuotedString();
                break;
            case XML_DOUBLE_QUOTED_STRING:
                token = processXMLDoubleQuotedString();
                break;
            default:
                token = null;
        }

        // Can we improve this logic by creating the token with diagnostics then and there?
        return cloneWithDiagnostics(token);
    }

    /*
     * Private Methods
     */

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

    /*
     * ------------------------------------------------------------------------------------------------------------
     * INTERPOLATION Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInInterpolation() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextToken = peek();
        switch (nextToken) {
            case LexerTerminals.CLOSE_BRACE:
                // Close-brace in the interpolation mode definitely means its
                // then end of the interpolation.
                endMode();
                reader.advance();
                return getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind.CLOSE_BRACE_TOKEN);
            default:
                // We should never reach here. Interpolation must be empty since
                // this is something we are injecting. This is just a fail-safe.
                endMode();
                return nextToken();
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_CONTENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    /**
     * Process leading trivia.
     */
    private void processLeadingXMLTrivia() {
        processXMLTrivia(this.leadingTriviaList, true);
    }

    /**
     * Process and return trailing trivia.
     * 
     * @return Trailing trivia
     */
    private STNode processTrailingXMLTrivia() {
        List<STNode> triviaList = new ArrayList<>(10);
        processXMLTrivia(triviaList, false);
        return STNodeFactory.createNodeList(triviaList);
    }

    /**
     * Process XML trivia and add it to the provided list.
     * <p>
     * <code>
     * xml-trivia := (WS | invalid-tokens)+ 
     * <br/><br/>
     * WS := #x20 | #x9 | #xD | #xA
     * </code>
     * 
     * @param triviaList List of trivia
     * @param isLeading Flag indicating whether the currently processing leading trivia or not
     */
    private void processXMLTrivia(List<STNode> triviaList, boolean isLeading) {
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
                    if (isLeading) {
                        break;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private STToken getXMLSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getXMLSyntaxToken(SyntaxKind kind, boolean allowLeadingWS, boolean allowTrailingWS) {
        STNode leadingTrivia = getLeadingTrivia();
        if (!allowLeadingWS && leadingTrivia.bucketCount() != 0) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_WHITESPACE_BEFORE, kind.stringValue());
        }

        STNode trailingTrivia = processTrailingXMLTrivia();
        if (!allowTrailingWS && trailingTrivia.bucketCount() != 0) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_WHITESPACE_AFTER, kind.stringValue());
        }
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = STNodeFactory.createNodeList(new ArrayList<>(0));
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getLiteral(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    /**
     * Read the next token in the {@link ParserMode#XML_CONTENT} mode.
     * <p>
     * <code>
     * content :=  CharData? ((element | Reference | CDSect | PI | Comment) CharData?)*
     * </code>
     * 
     * @return Next token
     */
    private STToken readTokenInXMLContent() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = reader.peek();
        switch (nextChar) {
            case LexerTerminals.BACKTICK:
                // Back tick means the end of currently processing XML content as
                // well as the end of entire XML literal. Hence end the context,
                // and start unwinding.
                endMode();
                return nextToken();
            case LexerTerminals.LT:
                reader.advance();
                nextChar = reader.peek();
                switch (nextChar) {
                    case LexerTerminals.EXCLAMATION_MARK:
                        // '<!--' is the comment start.
                        if (reader.peek(1) == LexerTerminals.MINUS && reader.peek(2) == LexerTerminals.MINUS) {
                            reader.advance(3);
                            startMode(ParserMode.XML_COMMENT);
                            return getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind.XML_COMMENT_START_TOKEN);
                        }
                        break;
                    case LexerTerminals.QUESTION_MARK:
                        // '<?' is the processing instruction start.
                        reader.advance();
                        startMode(ParserMode.XML_PI);
                        return getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind.XML_PI_START_TOKEN);
                    case LexerTerminals.SLASH:
                        endMode();
                        startMode(ParserMode.XML_ELEMENT_END_TAG);
                        return getXMLSyntaxToken(SyntaxKind.LT_TOKEN, false, false);
                    default:
                        break;
                }
                // Treat everything else as element start tag
                startMode(ParserMode.XML_ELEMENT_START_TAG);
                return getXMLSyntaxToken(SyntaxKind.LT_TOKEN, false, false);
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    // Switch to interpolation mode. Then the next token will be read in that mode.
                    startMode(ParserMode.INTERPOLATION);
                    reader.advance(2);
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            default:
                break;
        }

        // Everything else treat as charData
        startMode(ParserMode.XML_TEXT);
        return readTokenInXMLText();
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_ELEMENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    /**
     * Read the next token in an XML element. Precisely this will operate on {@link ParserMode#XML_ELEMENT_START_TAG} or
     * {@link ParserMode#XML_ELEMENT_END_TAG} mode.
     * <p>
     * <code>element := EmptyElemTag | STag content ETag</code>
     * 
     * @param isStartTag Flag indicating whether the next token should be read in start-tag or the end-tag
     * @return Next token
     */
    private STToken readTokenInXMLElement(boolean isStartTag) {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int c = reader.peek();
        switch (c) {
            case LexerTerminals.LT:
                // '<' token means the start of a new tag, where the closing '>'
                // is missing for the current element.
                if (isStartTag) {
                    startMode(ParserMode.XML_CONTENT);
                }
                return nextToken();
            case LexerTerminals.GT:
                endMode();
                if (isStartTag) {
                    startMode(ParserMode.XML_CONTENT);
                }

                reader.advance();
                return getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind.GT_TOKEN);
            case LexerTerminals.SLASH:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.SLASH_TOKEN, isStartTag, false);
            case LexerTerminals.COLON:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.COLON_TOKEN, false, false);
            case LexerTerminals.DOLLAR:
                // This is possible only in quoted-literals
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    reader.advance(2);
                    startMode(ParserMode.INTERPOLATION);

                    // Trailing trivia should be captured similar to DEFAULT mode.
                    // Hence using the 'getSyntaxToken()' method.
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            case LexerTerminals.BACKTICK:
                // Back tick means the end of currently processing element as well as
                // the end of entire XML literal. Hence end the context, and start
                // unwinding.
                endMode();
                return nextToken();
            default:
                break;
        }

        reader.advance();
        STToken tagName = processXMLName(c, false);
        startMode(ParserMode.XML_ATTRIBUTES);
        return tagName;
    }

    /**
     * Process XML name token in the non-canonicalized form.
     * <p>
     * <code>
     * NCName := Name - (Char* ':' Char*)
     * <br/><br/>
     * Name := NameStartChar (NameChar)*
     * <br/><br/>
     * NameStartChar := ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D]
     *                | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF]
     *                | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     * <br/><br/>
     * NameChar := NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     * <br/><br/>
     * Char := #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * 
     * </code>
     * 
     * 
     * @param startChar Starting character of the XML name
     * @param allowLeadingWS Flag indicating whether to allow whitespace before the name
     * @return XML name token
     */
    private STToken processXMLName(int startChar, boolean allowLeadingWS) {
        boolean isValid = true;
        if (!XMLValidator.isNCNameStart(startChar)) {
            isValid = false;
        }

        while (!reader.isEOF() && XMLValidator.isNCName(peek())) {
            reader.advance();
        }

        String text = getLexeme();
        if (!isValid) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_XML_NAME, text);
        }

        return getXMLNameToken(text, allowLeadingWS);
    }

    private STToken getXMLNameToken(String tokenText, boolean allowLeadingWS) {
        STNode leadingTrivia = getLeadingTrivia();
        if (!allowLeadingWS && leadingTrivia.bucketCount() != 0) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_WHITESPACE_BEFORE, tokenText);
        }

        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createIdentifierToken(lexeme, leadingTrivia, trailingTrivia);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_ATTRIBUTES Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInXMLAttributes(boolean isStartTag) {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = reader.peek();
        switch (nextChar) {
            case LexerTerminals.LT:
            case LexerTerminals.GT:
            case LexerTerminals.SLASH:
            case LexerTerminals.BACKTICK:
                endMode(); // end attributes
                // Delegate the next token reading task to parent element
                return readTokenInXMLElement(isStartTag);
            case LexerTerminals.COLON:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.COLON_TOKEN, false, false);
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    reader.advance(2);
                    startMode(ParserMode.INTERPOLATION);
                    // Trailing trivia should be captured similar to DEFAULT mode.
                    // Hence using the 'getSyntaxToken()' method.
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            case LexerTerminals.EQUAL:
                reader.advance();
                return getXMLSyntaxToken(SyntaxKind.EQUAL_TOKEN, true, true);
            case LexerTerminals.DOUBLE_QUOTE:
                reader.advance();
                startMode(ParserMode.XML_DOUBLE_QUOTED_STRING);
                return getXMLSyntaxToken(SyntaxKind.DOUBLE_QUOTE_TOKEN, false, false);
            case LexerTerminals.SINGLE_QUOTE:
                reader.advance();
                startMode(ParserMode.XML_SINGLE_QUOTED_STRING);
                return getXMLSyntaxToken(SyntaxKind.SINGLE_QUOTE_TOKEN, false, false);
            default:
                break;
        }

        reader.advance();
        STToken attributeName = processXMLName(nextChar, true);
        return attributeName;
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_SINGLE_QUOTED_STRING mode and XML_DOUBLE_QUOTED_STRING mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken processXMLDoubleQuotedString() {
        return processXMLQuotedString(LexerTerminals.DOUBLE_QUOTE, SyntaxKind.DOUBLE_QUOTE_TOKEN);
    }

    private STToken processXMLSingleQuotedString() {
        return processXMLQuotedString(LexerTerminals.SINGLE_QUOTE, SyntaxKind.SINGLE_QUOTE_TOKEN);
    }

    private STToken processXMLQuotedString(int startingQuote, SyntaxKind startQuoteKind) {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        /*
         * Handle starting char
         */
        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.DOUBLE_QUOTE:
            case LexerTerminals.SINGLE_QUOTE:
                if (nextChar == startingQuote) {
                    this.reader.advance();
                    endMode();
                    return getXMLSyntaxToken(startQuoteKind, false, true);
                }
                break;
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    reader.advance(2);
                    startMode(ParserMode.INTERPOLATION);
                    // Trailing trivia should be captured similar to DEFAULT mode.
                    // Hence using the 'getSyntaxToken()' method.
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            default:
                break;
        }

        /*
         * Handle remaining chars
         */
        while (!reader.isEOF()) {
            nextChar = peek();
            switch (nextChar) {
                case LexerTerminals.DOUBLE_QUOTE:
                case LexerTerminals.SINGLE_QUOTE:
                    if (nextChar == startingQuote) {
                        break;
                    }
                    this.reader.advance();
                    continue;
                case LexerTerminals.BITWISE_AND:
                    processXMLReferenceInQuotedString(startingQuote);
                    continue;
                case LexerTerminals.LT:
                    reader.advance();
                    this.reportLexerError(DiagnosticErrorCode.ERROR_INVALID_CHARACTER_IN_XML_ATTRIBUTE_VALUE,
                            LexerTerminals.LT);
                    continue;
                case LexerTerminals.DOLLAR:
                    if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                        break;
                    }
                    // fall through
                default:
                    this.reader.advance();
                    continue;
            }
            break;
        }

        return getLiteral(SyntaxKind.XML_TEXT_CONTENT);
    }

    private void processXMLReferenceInQuotedString(int startingQuote) {
        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.DOUBLE_QUOTE:
            case LexerTerminals.SINGLE_QUOTE:
                if (nextChar == startingQuote) {
                    break;
                }
                // fall through
            default:
                // Process the name component
                processXMLReference();
                break;
        }
    }

    /**
     * Process XML references.
     * <p>
     * <code>
     * Reference := EntityRef | CharRef
     * </code>
     */
    private void processXMLReference() {
        this.reader.advance();
        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.SEMICOLON:
                reportLexerError(DiagnosticErrorCode.ERROR_MISSING_ENTITY_REFERENCE_NAME);
                reader.advance();
                return;
            case LexerTerminals.HASH:
                processXMLCharRef();
                break;
            default:
                // Process the name component
                processXMLEntityRef();
                break;
        }

        // Process ending semicolon
        if (peek() == LexerTerminals.SEMICOLON) {
            reader.advance();
        } else {
            reportLexerError(DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_IN_XML_REFERENCE);
        }
    }

    /**
     * Process XML char references.
     * <p>
     * <code>
     * CharRef := ('&#' [0-9]+ ';') | ('&#x' [0-9a-fA-F]+ ';')
     * </code>
     */
    private void processXMLCharRef() {
        // We reach here after consuming '&', and verifying the following '#'.
        // Hence consume the hash token.
        reader.advance();
        int nextChar = peek();
        if (nextChar == 'x') {
            reader.advance();
            processHexDigits();
        } else {
            processDigits();
        }
    }

    private void processHexDigits() {
        while (isHexDigit(peek())) {
            reader.advance();
        }
    }

    private void processDigits() {
        while (isDigit(peek())) {
            reader.advance();
        }
    }

    /**
     * Process the name component of an XML entity reference. This method will
     * advance the token reader until the end of entity reference.
     * 
     * <p>
     * <code>EntityRef := '&' Name ';'</code>
     */
    private void processXMLEntityRef() {
        // We reach here after consuming '&'

        // Process the name component
        if (!XMLValidator.isNCNameStart(peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_ENTITY_REFERENCE_NAME_START);
        } else {
            reader.advance();
        }

        while (!reader.isEOF() && XMLValidator.isNCName(peek())) {
            reader.advance();
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_TEXT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    /**
     * Read token form the XML text (i.e.: charData).
     * <p>
     * <code>
     * text := CharData?
     * <br/>
     * CharData :=  [^<&]* - ([^<&]* ']]>' [^<&]*)
     * </code>
     * 
     * @return Next token
     */
    private STToken readTokenInXMLText() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar;
        while (!reader.isEOF()) {
            nextChar = this.reader.peek();
            switch (nextChar) {
                case LexerTerminals.LT:
                    break;
                case LexerTerminals.DOLLAR:
                    if (this.reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                        break;
                    }
                    reader.advance();
                    continue;
                case LexerTerminals.BITWISE_AND:
                    // The ampersand character denotes an XML reference. Validate it as reference,
                    // but treat that as part of the text.
                    processXMLReference();
                    continue;
                case LexerTerminals.BACKTICK:
                    break;
                default:
                    // ']]>' should also terminate charData, since we treat CData as text
                    reader.advance();
                    continue;
            }

            break;
        }

        // End of charData also means the end of XML_TEXT mode
        endMode();
        return getXMLText(SyntaxKind.XML_TEXT_CONTENT);
    }

    private STToken getXMLText(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingXMLTrivia();
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_COMMENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInXMLComment() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        /*
         * This block handles the case where very next token is the end of comment.
         * This is possible in two cases:
         * 1) No comment body is available.
         * 2) Comment body was processed during previous readToken(). And the next readToken()
         * will bring us here as we are still inside the xml-comment-mode.
         */
        switch (reader.peek()) {
            case LexerTerminals.MINUS:
                if (reader.peek(1) == LexerTerminals.MINUS) {
                    // '-->' marks the end of comment
                    if (reader.peek(2) == LexerTerminals.GT) {
                        reader.advance(3);
                        endMode();
                        return getXMLSyntaxTokenWithoutTrailingWS(SyntaxKind.XML_COMMENT_END_TOKEN);
                    }

                    // Double-hyphen is not allowed. So log an error, but continue.
                    reader.advance(1);
                    reportLexerError(DiagnosticErrorCode.ERROR_DOUBLE_HYPHEN_NOT_ALLOWED_WITHIN_XML_COMMENT);
                }
                break;
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    reader.advance(2);
                    startMode(ParserMode.INTERPOLATION);
                    // Trailing trivia should be captured similar to DEFAULT mode.
                    // Hence using the 'getSyntaxToken()' method.
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            default:
                break;
        }

        /*
         * This block handles the comment body.
         */
        while (!reader.isEOF()) {
            switch (reader.peek()) {
                case LexerTerminals.MINUS:
                    if (reader.peek(1) == LexerTerminals.MINUS) {
                        // '-->' marks the end of comment
                        if (reader.peek(2) == LexerTerminals.GT) {
                            break;
                        }

                        // otherwise, double-hyphen is not allowed
                        reader.advance(2);
                        reportLexerError(DiagnosticErrorCode.ERROR_DOUBLE_HYPHEN_NOT_ALLOWED_WITHIN_XML_COMMENT);
                    } else {
                        reader.advance();
                    }
                    continue;
                case LexerTerminals.DOLLAR:
                    if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                        break;
                    }
                    reader.advance();
                    continue;
                case LexerTerminals.BACKTICK:
                    endMode();
                    break;
                default:
                    reader.advance();
                    continue;
            }
            break;
        }

        return getXMLText(SyntaxKind.XML_TEXT_CONTENT);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_PI Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInXMLPI() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = reader.peek();
        switch (nextChar) {
            case LexerTerminals.QUESTION_MARK:
                if (this.reader.peek(1) == LexerTerminals.GT) {
                    reader.advance(2);
                    endMode();
                    return getXMLSyntaxToken(SyntaxKind.XML_PI_END_TOKEN);
                }
                break;
            case LexerTerminals.BACKTICK:
                endMode();
                return nextToken();
            default:
                break;
        }

        reader.advance();
        STToken tagName = processXMLName(nextChar, false);
        startMode(ParserMode.XML_PI_DATA);
        return tagName;
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * XML_PI_DATA Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readTokenInXMLPIData() {
        reader.mark();
        if (reader.isEOF()) {
            return getXMLSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        switch (reader.peek()) {
            case LexerTerminals.DOLLAR:
                if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                    reader.advance(2);
                    startMode(ParserMode.INTERPOLATION);
                    // Trailing trivia should be captured similar to DEFAULT mode.
                    // Hence using the 'getSyntaxToken()' method.
                    return getXMLSyntaxToken(SyntaxKind.INTERPOLATION_START_TOKEN);
                }
                break;
            case LexerTerminals.QUESTION_MARK:
                if (this.reader.peek(1) == LexerTerminals.GT) {
                    reader.advance(2);
                    endMode();
                    endMode();
                    return getXMLSyntaxToken(SyntaxKind.XML_PI_END_TOKEN);
                }
                break;
            default:
                break;
        }

        while (!reader.isEOF()) {
            switch (reader.peek()) {
                case LexerTerminals.QUESTION_MARK:
                    // '?>' marks the end of comment
                    if (reader.peek(1) == LexerTerminals.GT) {
                        break;
                    }
                    reader.advance();
                    continue;
                case LexerTerminals.DOLLAR:
                    if (reader.peek(1) == LexerTerminals.OPEN_BRACE) {
                        break;
                    }
                    // fall through
                case LexerTerminals.BACKTICK:
                    endMode();
                    break;
                default:
                    reader.advance();
                    continue;
            }
            break;
        }

        return getXMLText(SyntaxKind.XML_TEXT_CONTENT);
    }

}
