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

import io.ballerina.compiler.internal.diagnostics.DiagnosticWarningCode;
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

    /**
     * Backtick mode to fall back.
     * <p>
     * For code references, we switch to {@link ParserMode#DOC_CODE_LINE_START_HASH},
     * to capture the initial hash of the code line.
     * Once it is captured, we need to fall back to the previous mode.
     */
    private ParserMode previousBacktickMode = null;

    public DocumentationLexer(CharReader charReader,
                              List<STNode> leadingTriviaList,
                              Collection<STNodeDiagnostic> diagnostics) {
        super(charReader, ParserMode.DOC_LINE_START_HASH, leadingTriviaList, diagnostics);
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
            case DOC_LINE_START_HASH:
                processLeadingTrivia();
                token = readDocLineStartHashToken();
                break;
            case DOC_LINE_DIFFERENTIATOR:
                processLeadingTrivia();
                token = readDocLineDifferentiatorToken();
                break;
            case DOC_INTERNAL:
                token = readDocInternalToken();
                break;
            case DOC_PARAMETER:
                processLeadingTrivia();
                token = readDocParameterToken();
                break;
            case DOC_REFERENCE_TYPE:
                processLeadingTrivia();
                token = readDocReferenceTypeToken();
                break;
            case DOC_SINGLE_BACKTICK_CONTENT:
                token = readSingleBacktickContentToken();
                break;
            case DOC_DOUBLE_BACKTICK_CONTENT:
                token = readCodeContent(2);
                break;
            case DOC_TRIPLE_BACKTICK_CONTENT:
                token = readCodeContent(3);
                break;
            case DOC_CODE_REF_END:
                token = readCodeReferenceEndToken();
                break;
            case DOC_CODE_LINE_START_HASH:
                processLeadingTrivia();
                token = readCodeLineStartHashToken();
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
     */
    private void processIdentifierEnd() {
        while (!reader.isEOF()) {
            int nextChar = reader.peek();
            if (isIdentifierFollowingChar(nextChar)) {
                reader.advance();
                continue;
            }

            if (nextChar != LexerTerminals.BACKSLASH) {
                break;
            }

            // IdentifierSingleEscape | NumericEscape

            nextChar = reader.peek(1);
            switch (nextChar) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.TAB:
                    reader.advance();
                    reportLexerError(DiagnosticWarningCode.WARNING_INVALID_ESCAPE_SEQUENCE, "");
                    break;
                case 'u':
                    // NumericEscape
                    if (reader.peek(2) == LexerTerminals.OPEN_BRACE) {
                        processNumericEscape();
                    } else {
                        reader.advance(2);
                    }
                    continue;
                default:
                    reader.advance(2);
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

    private STToken getLiteral(SyntaxKind tokenKind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        return STNodeFactory.createLiteralValueToken(tokenKind, lexeme, leadingTrivia,
                trailingTrivia);
    }

    private STToken getDocSyntaxToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        STNode trailingTrivia = processTrailingTrivia();
        checkAndTerminateCurrentMode(trailingTrivia);
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getDocLiteralToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        checkAndTerminateCurrentMode(trailingTrivia);
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getDocIdentifierToken() {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();
        STNode trailingTrivia = processTrailingTrivia();
        checkAndTerminateCurrentMode(trailingTrivia);
        return STNodeFactory.createIdentifierToken(lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getDocSyntaxTokenWithoutTrivia(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();

        // We reach here for backtick tokens. Trailing trivia for those tokens can only be a newline.
        // i.e. if there's whitespace trivia they should be a part of the next token.
        STNode trailingTrivia;
        List<STNode> triviaList = new ArrayList<>(1);

        int nextChar = peek();
        if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
            reader.mark();
            triviaList.add(processEndOfLine());
            // Newline reached, hence end the current mode
            endMode();
        }

        trailingTrivia = STNodeFactory.createNodeList(triviaList);
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getDocLiteralWithoutTrivia(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();
        String lexeme = getLexeme();

        // We reach here for deprecation literal. We will not capture whitespace trivia for that token.
        // This done to give better formatting if someone uses more text after the deprecated literal.
        // Allowing more text inline in deprecation line is still in discussion.
        // Refer: https://github.com/ballerina-platform/ballerina-spec/issues/461
        STNode trailingTrivia;
        List<STNode> triviaList = new ArrayList<>(1);

        int nextChar = peek();
        if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
            reader.mark();
            triviaList.add(processEndOfLine());
            // Newline reached, hence end the current mode
            endMode();
        }

        trailingTrivia = STNodeFactory.createNodeList(triviaList);
        return STNodeFactory.createLiteralValueToken(kind, lexeme, leadingTrivia, trailingTrivia);
    }

    private STToken getCodeStartBacktickToken(SyntaxKind kind) {
        STNode leadingTrivia = getLeadingTrivia();

        // We reach here for double and triple backtick tokens. Trailing trivia for those tokens can only be a newline.
        // i.e. if there's whitespace trivia they should be a part of the next token.
        STNode trailingTrivia;
        List<STNode> triviaList = new ArrayList<>(1);

        int nextChar = peek();
        if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
            reader.mark();
            triviaList.add(processEndOfLine());
            previousBacktickMode = this.mode; // store the the current mode to fall back later
            switchMode(ParserMode.DOC_CODE_LINE_START_HASH);
        }

        trailingTrivia = STNodeFactory.createNodeList(triviaList);
        return STNodeFactory.createToken(kind, leadingTrivia, trailingTrivia);
    }

    private STToken getCodeLineStartHashToken() {
        STNode leadingTrivia = getLeadingTrivia();

        // Trivia for # in a code line can only have following 3 cases.
        // single whitespace char, newline or single whitespace char followed by a newline
        // Additional whitespaces should be part of the code description.
        STNode trailingTrivia;
        List<STNode> triviaList = new ArrayList<>(2);

        int nextChar = peek();
        switch (nextChar) {
            case LexerTerminals.SPACE:
            case LexerTerminals.TAB:
            case LexerTerminals.FORM_FEED:
                reader.mark();
                reader.advance();
                STNode singleWhitespace = STNodeFactory.createMinutiae(SyntaxKind.WHITESPACE_MINUTIAE, getLexeme());
                triviaList.add(singleWhitespace);

                nextChar = peek();
                if (nextChar == LexerTerminals.NEWLINE || nextChar == LexerTerminals.CARRIAGE_RETURN) {
                    reader.mark();
                    triviaList.add(processEndOfLine());
                } else {
                    // No newline, switch the mode to capture the code description
                    switchMode(previousBacktickMode);
                }
                break;
            case LexerTerminals.CARRIAGE_RETURN:
            case LexerTerminals.NEWLINE:
                reader.mark();
                triviaList.add(processEndOfLine());
                break;
            default:
                // No newline, switch the mode to capture the code description
                switchMode(previousBacktickMode);
        }

        trailingTrivia = STNodeFactory.createNodeList(triviaList);
        return STNodeFactory.createToken(SyntaxKind.HASH_TOKEN, leadingTrivia, trailingTrivia);
    }

    /**
     * When there is a newline present in the trailing minutiae, the current mode is terminated.
     * Therefore, lines below the erroneous line will be unaffected.
     *
     * @param trailingTrivia Trailing trivia node
     */
    private void checkAndTerminateCurrentMode(STNode trailingTrivia) {
        // Check for newline minutiae and terminate the current mode.
        int bucketCount = trailingTrivia.bucketCount();
        if (bucketCount > 0 && trailingTrivia.childInBucket(bucketCount - 1).kind == SyntaxKind.END_OF_LINE_MINUTIAE) {
            endMode();
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_LINE_START_HASH Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocLineStartHashToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        if (nextChar == LexerTerminals.HASH) {
            reader.advance();
            startMode(ParserMode.DOC_LINE_DIFFERENTIATOR);
            return getDocSyntaxToken(SyntaxKind.HASH_TOKEN);
        }

        assert false : "Documentation line should always start with a hash";
        return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_LINE_DIFFERENTIATOR Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocLineDifferentiatorToken() {
        int c = peek();
        switch (c) {
            case LexerTerminals.PLUS:
                return processPlusToken();
            case LexerTerminals.HASH:
                switchMode(ParserMode.DOC_INTERNAL);
                return processDeprecationLiteralToken();
            case LexerTerminals.BACKTICK:
                if (reader.peek(1) == LexerTerminals.BACKTICK) {
                    return processDoubleOrTripleBacktickToken();
                }
                // Else fall through
            default:
                switchMode(ParserMode.DOC_INTERNAL);
                return readDocInternalToken();
        }
    }

    private STToken processPlusToken() {
        reader.advance(); // Advance for +
        switchMode(ParserMode.DOC_PARAMETER);
        return getDocSyntaxToken(SyntaxKind.PLUS_TOKEN);
    }

    private STToken processDoubleOrTripleBacktickToken() {
        reader.advance(2); // Advance for two backticks
        if (peek() == LexerTerminals.BACKTICK) {
            reader.advance();
            switchMode(ParserMode.DOC_TRIPLE_BACKTICK_CONTENT);
            return getCodeStartBacktickToken(SyntaxKind.TRIPLE_BACKTICK_TOKEN);
        } else {
            switchMode(ParserMode.DOC_DOUBLE_BACKTICK_CONTENT);
            return getCodeStartBacktickToken(SyntaxKind.DOUBLE_BACKTICK_TOKEN);
        }
    }

    private STToken processDeprecationLiteralToken() {
        // Look ahead and see if next non-trivial char belongs to a deprecation literal.
        // There could be spaces and tabs in between.
        int lookAheadCount = 1;
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
                return readDocInternalToken();
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
        return getDocLiteralWithoutTrivia(SyntaxKind.DEPRECATION_LITERAL);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_INTERNAL Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocInternalToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        if (nextChar == LexerTerminals.BACKTICK) {
            reader.advance();
            nextChar = peek();
            if (nextChar != LexerTerminals.BACKTICK) {
                // single backtick
                switchMode(ParserMode.DOC_SINGLE_BACKTICK_CONTENT);
                return getDocSyntaxTokenWithoutTrivia(SyntaxKind.BACKTICK_TOKEN);
            }

            reader.advance();
            nextChar = peek();
            if (nextChar != LexerTerminals.BACKTICK) {
                // double backtick
                switchMode(ParserMode.DOC_DOUBLE_BACKTICK_CONTENT);
                return getCodeStartBacktickToken(SyntaxKind.DOUBLE_BACKTICK_TOKEN);
            }

            reader.advance();
            // triple backtick
            switchMode(ParserMode.DOC_TRIPLE_BACKTICK_CONTENT);
            return getCodeStartBacktickToken(SyntaxKind.TRIPLE_BACKTICK_TOKEN);
        }

        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                    endMode();
                    break;
                case LexerTerminals.BACKTICK:
                    break;
                default:
                    if (isIdentifierInitialChar(nextChar)) {
                        boolean hasDocumentationReference = processDocumentationReference(nextChar);
                        if (hasDocumentationReference) {
                            switchMode(ParserMode.DOC_REFERENCE_TYPE);
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
            return readDocReferenceTypeToken();
        }

        return getLiteral(SyntaxKind.DOCUMENTATION_DESCRIPTION);
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
     * DOC_PARAMETER Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocParameterToken() {
        reader.mark();
        int nextChar = peek();
        if (isPossibleIdentifierStart(nextChar)) {
            if (nextChar != LexerTerminals.BACKSLASH) {
                reader.advance();
            }

            processIdentifierEnd();
            STToken token;
            if (LexerTerminals.RETURN.equals(getLexeme())) {
                token = getDocSyntaxToken(SyntaxKind.RETURN_KEYWORD);
            } else {
                token = getDocLiteralToken(SyntaxKind.PARAMETER_NAME);
            }
            // If the parameter name is not followed by a minus token switch the mode.
            // However, if the parameter name ends with a newline DOC_PARAMETER mode is already ended.
            // Therefore, DOC_LINE_START_HASH is the active mode. In that case do not switch mode.
            if (peek() != LexerTerminals.MINUS && this.mode != ParserMode.DOC_LINE_START_HASH) {
                switchMode(ParserMode.DOC_INTERNAL);
            }
            return token;
        } else if (nextChar == LexerTerminals.MINUS) {
            reader.advance();
            switchMode(ParserMode.DOC_INTERNAL);
            return getDocSyntaxToken(SyntaxKind.MINUS_TOKEN);
        } else {
            switchMode(ParserMode.DOC_INTERNAL);
            return readDocInternalToken();
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_REFERENCE_TYPE Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readDocReferenceTypeToken() {
        int nextChar = peek();
        if (nextChar == LexerTerminals.BACKTICK) {
            reader.advance();
            switchMode(ParserMode.DOC_SINGLE_BACKTICK_CONTENT);
            return getDocSyntaxTokenWithoutTrivia(SyntaxKind.BACKTICK_TOKEN);
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
                return getDocSyntaxToken(SyntaxKind.TYPE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.SERVICE:
                return getDocSyntaxToken(SyntaxKind.SERVICE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.VARIABLE:
                return getDocSyntaxToken(SyntaxKind.VARIABLE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.VAR:
                return getDocSyntaxToken(SyntaxKind.VAR_DOC_REFERENCE_TOKEN);
            case LexerTerminals.ANNOTATION:
                return getDocSyntaxToken(SyntaxKind.ANNOTATION_DOC_REFERENCE_TOKEN);
            case LexerTerminals.MODULE:
                return getDocSyntaxToken(SyntaxKind.MODULE_DOC_REFERENCE_TOKEN);
            case LexerTerminals.FUNCTION:
                return getDocSyntaxToken(SyntaxKind.FUNCTION_DOC_REFERENCE_TOKEN);
            case LexerTerminals.PARAMETER:
                return getDocSyntaxToken(SyntaxKind.PARAMETER_DOC_REFERENCE_TOKEN);
            case LexerTerminals.CONST:
                return getDocSyntaxToken(SyntaxKind.CONST_DOC_REFERENCE_TOKEN);
            default:
                assert false : "Invalid reference type";
                return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
        }
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_SINGLE_BACKTICK_CONTENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readSingleBacktickContentToken() {
        reader.mark();
        int nextChar = peek();
        if (nextChar == LexerTerminals.BACKSLASH) {
            processIdentifierEnd();
            return getDocIdentifierToken();
        }

        reader.advance();
        switch (nextChar) {
            case LexerTerminals.BACKTICK:
                switchMode(ParserMode.DOC_INTERNAL);
                return getDocSyntaxTokenWithoutTrivia(SyntaxKind.BACKTICK_TOKEN);
            case LexerTerminals.DOT:
                return getDocSyntaxToken(SyntaxKind.DOT_TOKEN);
            case LexerTerminals.COLON:
                return getDocSyntaxToken(SyntaxKind.COLON_TOKEN);
            case LexerTerminals.OPEN_PARANTHESIS:
                return getDocSyntaxToken(SyntaxKind.OPEN_PAREN_TOKEN);
            case LexerTerminals.CLOSE_PARANTHESIS:
                return getDocSyntaxToken(SyntaxKind.CLOSE_PAREN_TOKEN);
            default:
                if (isPossibleIdentifierStart(nextChar)) {
                    processIdentifierEnd();
                    return getDocIdentifierToken();
                }

                processInvalidChars();
                return getDocLiteralToken(SyntaxKind.CODE_CONTENT);
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

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_DOUBLE_BACKTICK_CONTENT / DOC_TRIPLE_BACKTICK_CONTENT Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readCodeContent(int backtickCount) {
        reader.mark();
        if (reader.isEOF()) {
            return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
        }

        int nextChar = peek();
        while (!reader.isEOF()) {
            switch (nextChar) {
                case LexerTerminals.BACKTICK:
                    int count = getBackticksCount();
                    if (count == backtickCount) {
                        switchMode(ParserMode.DOC_CODE_REF_END);
                        break;
                    }
                    reader.advance(count);
                    nextChar = peek();
                    continue;
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.NEWLINE:
                    previousBacktickMode = this.mode;
                    switchMode(ParserMode.DOC_CODE_LINE_START_HASH);
                    break;
                default:
                    reader.advance();
                    nextChar = peek();
                    continue;
            }
            break;
        }

        if (getLexeme().isEmpty()) {
            // We only reach here for ``<empty_code>`` and ```<empty_code>```
            return readCodeReferenceEndToken();
        }

        return getLiteral(SyntaxKind.CODE_CONTENT);
    }

    private int getBackticksCount() {
        int count = 1;
        while (reader.peek(count) == LexerTerminals.BACKTICK) {
            count += 1;
        }
        return count;
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_CODE_REF_END Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readCodeReferenceEndToken() {
        switchMode(ParserMode.DOC_INTERNAL);
        if (peek() == LexerTerminals.BACKTICK) {
            reader.advance();
            if (peek() == LexerTerminals.BACKTICK) {
                reader.advance();
                if (peek() == LexerTerminals.BACKTICK) {
                    reader.advance();
                    // triple backtick
                    return getDocSyntaxTokenWithoutTrivia(SyntaxKind.TRIPLE_BACKTICK_TOKEN);
                } else {
                    // double backtick
                    return getDocSyntaxTokenWithoutTrivia(SyntaxKind.DOUBLE_BACKTICK_TOKEN);
                }
            }
        }

        assert false : "Invalid character: Expected a backtick";
        return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
    }

    /*
     * ------------------------------------------------------------------------------------------------------------
     * DOC_CODE_LINE_START_HASH Mode
     * ------------------------------------------------------------------------------------------------------------
     */

    private STToken readCodeLineStartHashToken() {
        reader.mark();
        if (reader.isEOF()) {
            return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
        }
        int nextChar = peek();
        if (nextChar == LexerTerminals.HASH) {
            reader.advance();
            return getCodeLineStartHashToken();
        }

        assert false : "Invalid character: Expected a hash";
        return getDocSyntaxToken(SyntaxKind.EOF_TOKEN);
    }
}
