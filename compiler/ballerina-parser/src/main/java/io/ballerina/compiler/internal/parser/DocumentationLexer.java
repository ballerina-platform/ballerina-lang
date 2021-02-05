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

import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.CharReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A LL(k) lexer for documentation in ballerina.
 *
 * @since 2.0.0
 */
public class DocumentationLexer extends AbstractLexer {

    /**
     * Character array of "Deprecated" keyword.
     */
    private static final char[] deprecatedChars = { 'D', 'e', 'p', 'r', 'e', 'c', 'a', 't', 'e', 'd' };

    public DocumentationLexer(CharReader charReader,
                              List<STNode> leadingTriviaList,
                              Collection<STNodeDiagnostic> diagnostics) {
        super(charReader, ParserMode.DOCUMENTATION_INIT, leadingTriviaList, diagnostics);
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
            case DOCUMENTATION_INIT:
                processLeadingTrivia();
                token = readDocumentationInitToken();
                break;
            case DOCUMENTATION:
                token = readDocumentationToken();
                break;
            case DOCUMENTATION_INTERNAL:
                token = readDocumentationInternalToken();
                break;
            case DOCUMENTATION_PARAMETER:
                processLeadingTrivia();
                token = readDocumentationParameterToken();
                break;
            case DOCUMENTATION_REFERENCE_TYPE:
                processLeadingTrivia();
                token = readDocumentationReferenceTypeToken();
                break;
            case DOCUMENTATION_BACKTICK_CONTENT:
                token = readDocumentationBacktickContentToken();
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
     * Check whether a given char is a possible identifier start.
     */
    private boolean isPossibleIdentifierStart(int startChar) {
        switch (startChar) {
            case LexerTerminals.SINGLE_QUOTE:
            case LexerTerminals.BACKSLASH:
                return true;
            default:
                return isIdentifierInitialChar(startChar);
        }
    }

    /**
     * Process identifier end.
     * <p>
     * <code>
     * IdentifierEnd := IdentifierChar*
     * <br/>
     * IdentifierChar := IdentifierFollowingChar | IdentifierEscape
     * <br/>
     * IdentifierEscape := IdentifierSingleEscape | NumericEscape
     * </code>
     *
     * @param initialEscape Denotes whether <code>\</code> is at the beginning of the identifier
     */
    private void processIdentifierEnd(boolean initialEscape) {
        while (!reader.isEOF()) {
            int k = 1;
            int nextChar = reader.peek();
            if (isIdentifierFollowingChar(nextChar)) {
                reader.advance();
                initialEscape = false;
                continue;
            }

            if (nextChar != LexerTerminals.BACKSLASH && !initialEscape) {
                break;
            }
            if (initialEscape) {
                k = 0;
                initialEscape = false;
            }

            // IdentifierSingleEscape | NumericEscape

            nextChar = reader.peek(k);
            switch (nextChar) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.TAB:
                    break;
                case 'u':
                    // NumericEscape
                    if (reader.peek(k + 1) == LexerTerminals.OPEN_BRACE) {
                        processNumericEscape();
                    } else {
                        reader.advance(k + 1);
                    }
                    continue;
                default:
                    reader.advance(k + 1);
                    continue;
            }
            break;
        }
    }

    /**
     * Process numeric escape.
     * <p>
     * <code>NumericEscape := \ u { CodePoint }</code>
     */
    private void processNumericEscape() {
        // Process '\ u {'
        this.reader.advance(3);

        // Process code-point
        if (!isHexDigit(peek())) {
            return;
        }

        reader.advance();
        while (isHexDigit(peek())) {
            reader.advance();
        }

        // Process close brace
        if (peek() != LexerTerminals.CLOSE_BRACE) {
            return;
        }

        this.reader.advance();
    }

    /**
     * Process leading trivia.
     */
    private void processLeadingTrivia() {
        // new leading trivia will be added to the current leading trivia list
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
     * <code>syntax-trivia := whitespace | end-of-line </code>
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

    private STToken getDocumentationSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = processTrailingTrivia();
        // check for end of line minutiae and terminate current documentation mode.
        int bucketCount = trailingTrivia.bucketCount();
        if (bucketCount > 0 && trailingTrivia.childInBucket(bucketCount - 1).kind == SyntaxKind.END_OF_LINE_MINUTIAE) {
            endMode();
        }
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getDocumentationSyntaxTokenWithNoTrivia(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();

        // We reach here only for the hash token, minus token and backtick token in the documentation mode.
        // Trivia for those tokens can only be an end of line.
        // Rest of the trivia after that belongs to the documentation description or the backtick content.
        STNode trailingTrivia;
        List<STNode> triviaList = new ArrayList<>(1);

        int nextChar = peek();
        if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
            reader.mark();
            triviaList.add(processEndOfLine());
            // end of line reached, hence end documentation mode
            endMode();
        }

        trailingTrivia = STNodeFactory.createNodeList(triviaList);
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getDeprecationLiteral() {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();

        // Trivia for "Deprecated" token can only be an end of line.
        // Rest of the trivia after that belongs to the documentation description.
        STNode trailingTrivia;
        List<STNode> triviaList = new ArrayList<>(1);

        int nextChar = peek();
        if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
            reader.mark();
            triviaList.add(processEndOfLine());
            // end of line reached, hence end documentation mode
            endMode();
        }

        trailingTrivia = STNodeFactory.createNodeList(triviaList);
        return STNodeFactory
                .createLiteralValueToken(SyntaxKind.DEPRECATION_LITERAL, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getDocumentationLiteral(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();

        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();

        // Check for end of line minutiae and terminate the current documentation mode.
        int bucketCount = trailingTrivia.bucketCount();
        if (bucketCount > 0 && trailingTrivia.childInBucket(bucketCount - 1).kind == SyntaxKind.END_OF_LINE_MINUTIAE) {
            endMode();
        }
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getDescriptionToken(SyntaxKind tokenKind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createLiteralValueToken(tokenKind, lexeme, leadingTrivia,
                trailingTrivia);
    }

    private STToken getDocumentationIdentifierToken() {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();

        // Check for end of line minutiae and terminate the current documentation mode.
        int bucketCount = trailingTrivia.bucketCount();
        if (bucketCount > 0 && trailingTrivia.childInBucket(bucketCount - 1).kind == SyntaxKind.END_OF_LINE_MINUTIAE) {
            endMode();
        }

        return STNodeFactory.createIdentifierToken(lexeme, leadingTrivia, trailingTrivia);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOCUMENTATION_INIT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocumentationInitToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getDocumentationSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        if (nextChar == LexerTerminals.HASH) {
            reader.advance();
            startMode(ParserMode.DOCUMENTATION);
            return getDocumentationSyntaxTokenWithNoTrivia(SyntaxKind.HASH_TOKEN);
        } else {
            throw new IllegalStateException("documentation line should always start with a hash");
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOCUMENTATION Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocumentationToken() {
        // Look ahead and see if next non-trivial char is a plus char or a hash char.
        // If it is a plus char, process trivial chars as leading trivia of the plus token.
        // If it is a hash char, look ahead and see if it is followed by a deprecation literal.
        // Else, let trivial chars be a part of the documentation description.
        int lookAheadCount = 0;
        int lookAheadChar = reader.peek(lookAheadCount);
        while (lookAheadChar == LexerTerminals.SPACE || lookAheadChar == LexerTerminals.TAB) {
            lookAheadCount++;
            lookAheadChar = reader.peek(lookAheadCount);
        }

        if (lookAheadChar == LexerTerminals.PLUS) {
            return processPlusToken();
        } else if (lookAheadChar == LexerTerminals.HASH) {
            return processDeprecationLiteralToken(lookAheadCount);
        } else {
            return readDocumentationInternalToken();
        }
    }

    private STToken processPlusToken() {
        processLeadingTrivia();
        reader.advance();
        switchMode(ParserMode.DOCUMENTATION_PARAMETER);
        return getDocumentationSyntaxToken(SyntaxKind.PLUS_TOKEN);
    }

    private STToken processDeprecationLiteralToken(int lookAheadCount) {
        // Look ahead and see if next non-trivial char belongs to a deprecation literal.
        // There could be spaces and tabs in between.
        lookAheadCount++;
        int lookAheadChar = reader.peek(lookAheadCount);

        int whitespaceCount = 0;
        while (lookAheadChar == LexerTerminals.SPACE || lookAheadChar == LexerTerminals.TAB) {
            lookAheadCount++;
            whitespaceCount++;
            lookAheadChar = reader.peek(lookAheadCount);
        }

        // Look ahead for a "Deprecated" word match.
        for (int i = 0; i < 10; i++) {
            if ((char) lookAheadChar != deprecatedChars[i]) {
                // No match. Hence return a documentation internal token.
                return readDocumentationInternalToken();
            }
            lookAheadCount++;
            lookAheadChar = reader.peek(lookAheadCount);
        }

        // There is a match. Hence return a deprecation literal.
        processLeadingTrivia();
        reader.mark();
        reader.advance(); // Advance reader for #
        reader.advance(whitespaceCount); // Advance reader for WS
        reader.advance(10); // Advance reader for "Deprecated" word
        return getDeprecationLiteral();
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOCUMENTATION_INTERNAL Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocumentationInternalToken() {
        reader.mark();
        int nextChar = peek();
        if (nextChar == LexerTerminals.BACKTICK && reader.peek(1) != LexerTerminals.BACKTICK) {
            reader.advance();
            switchMode(ParserMode.DOCUMENTATION_BACKTICK_CONTENT);
            return getDocumentationSyntaxToken(SyntaxKind.BACKTICK_TOKEN);
        }

        boolean hasCodeContent = false;
        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                    endMode();
                    break;
                case LexerTerminals.BACKTICK:
                    if (reader.peek(1) != LexerTerminals.BACKTICK) {
                        break;
                    }

                    if (reader.peek(2) != LexerTerminals.BACKTICK) {
                        // Double backtick detected
                        reader.advance(2);
                        processDocumentationCodeContent(false);
                    } else {
                        // Triple backtick detected
                        reader.advance(3);
                        processDocumentationCodeContent(true);
                    }
                    hasCodeContent = true;
                    nextChar = peek();
                    continue;
                default:
                    if (isIdentifierInitialChar(nextChar)) {
                        boolean hasDocumentationReference = processDocumentationReference(nextChar);
                        if (hasDocumentationReference) {
                            switchMode(ParserMode.DOCUMENTATION_REFERENCE_TYPE);
                            break;
                        }
                    } else {
                        reader.advance();
                    }
                    nextChar = peek();
                    continue;
            }
            break;
        }

        if (getLexeme().isEmpty()) {
            // Reaching here means, first immediate character itself belong to a documentation reference
            return readDocumentationReferenceTypeToken();
        }

        SyntaxKind tokenKind = hasCodeContent ?
                SyntaxKind.CODE_DESCRIPTION : SyntaxKind.DOCUMENTATION_DESCRIPTION;

        return getDescriptionToken(tokenKind);
    }

    private void processDocumentationCodeContent(boolean isTripleBacktick) {
        int nextChar = peek();
        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.BACKTICK:
                    // Look for a double backtick or a triple backtick
                    // depend on the `isTripleBacktick` boolean value.
                    if (isTripleBacktick) {
                        reader.advance();
                        if (peek() != LexerTerminals.BACKTICK) {
                            nextChar = peek();
                            continue;
                        }
                    }
                    reader.advance();
                    if (peek() != LexerTerminals.BACKTICK) {
                        nextChar = peek();
                        continue;
                    }
                    reader.advance();
                    if (peek() != LexerTerminals.BACKTICK) {
                        return;
                    }
                    nextChar = peek();
                    continue;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    // Reaching here means ending backticks were not found within the same line.
                    // Therefore, look ahead and see if next line is a documentation line and if so,
                    // look for a ending in that line. Otherwise terminate backtick content at the new line.
                    int lookAheadCount = 1;
                    if (peek() == LexerTerminals.CARRIAGE_RETURN && reader.peek(1) == LexerTerminals.NEWLINE) {
                        lookAheadCount++;
                    }
                    int lookAheadChar = reader.peek(lookAheadCount);
                    while (lookAheadChar == LexerTerminals.SPACE || lookAheadChar == LexerTerminals.TAB) {
                        lookAheadCount++;
                        lookAheadChar = reader.peek(lookAheadCount);
                    }
                    if (lookAheadChar != LexerTerminals.HASH) {
                        return;
                    }
                    reader.advance(lookAheadCount);
                    nextChar = peek();
                    continue;
                default:
                    reader.advance();
                    nextChar = peek();
            }
        }
    }

    private boolean processDocumentationReference(int nextChar) {
        // Look ahead and see if next characters belong to a documentation reference.
        // If they do, do not advance the reader and return.
        // Otherwise advance the reader for checked characters and return

        int lookAheadChar = nextChar;
        int lookAheadCount = 0;
        String identifier = "";

        while (isIdentifierInitialChar(lookAheadChar)) {
            identifier = identifier.concat(String.valueOf((char) lookAheadChar));
            lookAheadCount++;
            lookAheadChar = reader.peek(lookAheadCount);
        }

        switch (identifier) {
            case LexerTerminals.TYPE:
            case LexerTerminals.SERVICE:
            case LexerTerminals.VARIABLE:
            case LexerTerminals.VAR:
            case LexerTerminals.ANNOTATION:
            case LexerTerminals.MODULE:
            case LexerTerminals.FUNCTION:
            case LexerTerminals.PARAMETER:
            case LexerTerminals.CONST:
                // Look ahead for a single backtick.
                // There could be spaces or tabs in between.
                while (true) {
                    switch (lookAheadChar) {
                        case LexerTerminals.SPACE:
                        case LexerTerminals.TAB:
                            lookAheadCount++;
                            lookAheadChar = reader.peek(lookAheadCount);
                            continue;
                        case LexerTerminals.BACKTICK:
                            // Make sure backtick is a single backtick
                            if (reader.peek(lookAheadCount + 1) != LexerTerminals.BACKTICK) {
                                // Reaching here means checked characters belong to a documentation reference.
                                // Hence return.
                                return true;
                            }
                            // Fall through
                        default:
                            break;
                    }
                    break;
                }
                // Fall through
            default:
                reader.advance(lookAheadCount);
                return false;
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOCUMENTATION_PARAMETER Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocumentationParameterToken() {
        reader.mark();
        int nextChar = peek();
        if (isPossibleIdentifierStart(nextChar)) {
            reader.advance();
            processIdentifierEnd(nextChar == LexerTerminals.BACKSLASH);
            STToken token;
            if (LexerTerminals.RETURN.equals(getLexeme())) {
                token = getDocumentationSyntaxToken(SyntaxKind.RETURN_KEYWORD);
            } else {
                token = getDocumentationLiteral(SyntaxKind.PARAMETER_NAME);
            }
            // If the parameter name is not followed by a minus token or a newline, switch the mode.
            if (peek() != LexerTerminals.MINUS && ParserMode.DOCUMENTATION_INIT != this.mode) {
                switchMode(ParserMode.DOCUMENTATION_INTERNAL);
            }
            return token;
        } else if (nextChar == LexerTerminals.MINUS) {
            reader.advance();
            switchMode(ParserMode.DOCUMENTATION_INTERNAL);
            return getDocumentationSyntaxTokenWithNoTrivia(SyntaxKind.MINUS_TOKEN);
        } else {
            switchMode(ParserMode.DOCUMENTATION_INTERNAL);
            return readDocumentationInternalToken();
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOCUMENTATION_REFERENCE_TYPE Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocumentationReferenceTypeToken() {
        int nextChar = peek();
        if (nextChar == LexerTerminals.BACKTICK) {
            reader.advance();
            switchMode(ParserMode.DOCUMENTATION_BACKTICK_CONTENT);
            return getDocumentationSyntaxToken(SyntaxKind.BACKTICK_TOKEN);
        }

        while (isIdentifierInitialChar(peek())) {
            reader.advance();
        }

        return processReferenceType();
    }

    private STToken processReferenceType() {
        String tokenText = getLexeme();
        switch (tokenText) {
            case LexerTerminals.TYPE:
                return getDocumentationSyntaxToken(SyntaxKind.TYPE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.SERVICE:
                return getDocumentationSyntaxToken(SyntaxKind.SERVICE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.VARIABLE:
                return getDocumentationSyntaxToken(SyntaxKind.VARIABLE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.VAR:
                return getDocumentationSyntaxToken(SyntaxKind.VAR_DOC_REFERENCE_TOKEN);
            case LexerTerminals.ANNOTATION:
                return getDocumentationSyntaxToken(SyntaxKind.ANNOTATION_DOC_REFERENCE_TOKEN);
            case LexerTerminals.MODULE:
                return getDocumentationSyntaxToken(SyntaxKind.MODULE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.FUNCTION:
                return getDocumentationSyntaxToken(SyntaxKind.FUNCTION_DOC_REFERENCE_TOKEN);
            case LexerTerminals.PARAMETER:
                return getDocumentationSyntaxToken(SyntaxKind.PARAMETER_DOC_REFERENCE_TOKEN);
            case LexerTerminals.CONST:
                return getDocumentationSyntaxToken(SyntaxKind.CONST_DOC_REFERENCE_TOKEN);
            default:
                throw new IllegalStateException();
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOCUMENTATION_BACKTICK_CONTENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocumentationBacktickContentToken() {
        reader.mark();
        int nextChar = peek();
        reader.advance();
        switch (nextChar) {
            case LexerTerminals.BACKTICK:
                switchMode(ParserMode.DOCUMENTATION_INTERNAL);
                return getDocumentationSyntaxTokenWithNoTrivia(SyntaxKind.BACKTICK_TOKEN);
            case LexerTerminals.DOT:
                return getDocumentationSyntaxToken(SyntaxKind.DOT_TOKEN);
            case LexerTerminals.COLON:
                return getDocumentationSyntaxToken(SyntaxKind.COLON_TOKEN);
            case LexerTerminals.OPEN_PARANTHESIS:
                return getDocumentationSyntaxToken(SyntaxKind.OPEN_PAREN_TOKEN);
            case LexerTerminals.CLOSE_PARANTHESIS:
                return getDocumentationSyntaxToken(SyntaxKind.CLOSE_PAREN_TOKEN);
            default:
                if (isPossibleIdentifierStart(nextChar)) {
                    processIdentifierEnd(nextChar == LexerTerminals.BACKSLASH);
                    return getDocumentationIdentifierToken();
                }

                processInvalidChars();
                return getDocumentationLiteral(SyntaxKind.BACKTICK_CONTENT);
        }
    }

    private void processInvalidChars() {
        int nextChar = peek();
        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.BACKTICK:
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                    break;
                default:
                    reader.advance();
                    nextChar = peek();
                    continue;
            }
            break;
        }
    }
}
