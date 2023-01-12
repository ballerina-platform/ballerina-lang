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
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.text.CharReader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract lexer to be extended by all ballerina lexer implementations.
 *
 * @since 2.0.0
 */
public abstract class AbstractLexer {

    protected static final int INITIAL_TRIVIA_CAPACITY = 10;
    protected List<STNode> leadingTriviaList;
    private Collection<STNodeDiagnostic> diagnostics;
    protected CharReader reader;
    protected ParserMode mode;
    protected ArrayDeque<ParserMode> modeStack = new ArrayDeque<>();
    protected ArrayDeque<KeywordMode> keywordModes = new ArrayDeque<>();

    public AbstractLexer(CharReader charReader, ParserMode initialParserMode) {
        this(charReader, initialParserMode, new ArrayList<>(INITIAL_TRIVIA_CAPACITY), new ArrayList<>());
    }

    public AbstractLexer(CharReader charReader,
                         ParserMode initialParserMode,
                         List<STNode> leadingTriviaList,
                         Collection<STNodeDiagnostic> diagnostics) {
        this.reader = charReader;
        startMode(initialParserMode);
        startKeywordMode(KeywordMode.DEFAULT);
        this.leadingTriviaList = leadingTriviaList;
        this.diagnostics = diagnostics;
    }

    /**
     * Get the next lexical token.
     * 
     * @return Next lexical token.
     */
    public abstract STToken nextToken();

    /**
     * Reset the lexer to a given offset. The lexer will read the next tokens
     * from the given offset.
     * 
     * @param offset Offset
     */
    public void reset(int offset) {
        reader.reset(offset);
    }

    /**
     * Start the given operation mode of the lexer.
     * 
     * @param mode Mode to switch on to
     */
    public void startMode(ParserMode mode) {
        this.mode = mode;
        this.modeStack.push(mode);
    }

    /**
     * Switch from current operation mode to the given operation mode in the lexer.
     *
     * @param mode Mode to switch on to
     */
    public void switchMode(ParserMode mode) {
        this.modeStack.pop();
        this.mode = mode;
        this.modeStack.push(mode);
    }

    /**
     * End the current mode the mode of the lexer and fall back the previous mode.
     */
    public void endMode() {
        this.modeStack.pop();
        this.mode = this.modeStack.peek();
    }

    /**
     * Start the given keyword operation mode in the lexer.
     *
     * @param keywordMode mode to add
     */
    public void startKeywordMode(KeywordMode keywordMode) {
        this.keywordModes.push(keywordMode);
    }

    /**
     * End the current keyword mode in the lexer.
     *
     */
    public void endKeywordMode() {
        this.keywordModes.pop();
    }

    private void resetDiagnosticList() {
        this.diagnostics = new ArrayList<>();
    }

    private boolean noDiagnostics() {
        return diagnostics.isEmpty();
    }

    private Collection<STNodeDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    protected STToken cloneWithDiagnostics(STToken toClone) {
        if (noDiagnostics()) {
            return toClone;
        }

        STToken cloned = SyntaxErrors.addSyntaxDiagnostics(toClone, getDiagnostics());
        resetDiagnosticList();
        return cloned;
    }

    protected void reportLexerError(DiagnosticCode diagnosticCode, Object... args) {
        diagnostics.add(SyntaxErrors.createDiagnostic(diagnosticCode, args));
    }

    protected STNode getLeadingTrivia() {
        STNode trivia = STNodeFactory.createNodeList(this.leadingTriviaList);
        this.leadingTriviaList = new ArrayList<>(INITIAL_TRIVIA_CAPACITY);
        return trivia;
    }

    /**
     * Check whether a given char is an identifier following char.
     * <p>
     * <code>IdentifierFollowingChar := IdentifierInitialChar | Digit</code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is an identifier following char. <code>false</code> otherwise.
     */
    protected static boolean isIdentifierFollowingChar(int c) {
        return isIdentifierInitialChar(c) || isDigit(c);
    }

    /**
     * Check whether a given char is a digit.
     * <p>
     * <code>Digit := 0..9</code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character represents a digit. <code>false</code> otherwise.
     */
    protected static boolean isDigit(int c) {
        return ('0' <= c && c <= '9');
    }

    /**
     * Check whether a given char is a hexa digit.
     * <p>
     * <code>HexDigit := Digit | a .. f | A .. F</code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character represents a hex digit. <code>false</code> otherwise.
     */
    protected static boolean isHexDigit(int c) {
        if ('a' <= c && c <= 'f') {
            return true;
        }

        if ('A' <= c && c <= 'F') {
            return true;
        }

        return isDigit(c);
    }

    /**
     * Check whether a given char is an identifier start char.
     * <p>
     * <code>IdentifierInitialChar := A .. Z | a .. z | _ | UnicodeIdentifierChar</code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is an identifier start char. <code>false</code> otherwise.
     */
    protected static boolean isIdentifierInitialChar(int c) {
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
     * Check whether a given char is a unicode identifier char.
     * <p>
     * <code>
     * UnicodeIdentifierChar := ^ ( AsciiChar | UnicodeNonIdentifierChar )
     * <br><br/>
     * AsciiChar := 0x0 .. 0x7F
     * <br><br/>
     * UnicodeNonIdentifierChar := UnicodePrivateUseChar | UnicodePatternWhiteSpaceChar | UnicodePatternSyntaxChar
     * <br><br/>
     * UnicodePatternSyntaxChar := character with Unicode property Pattern_Syntax=True
     * </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is a unicode identifier char. <code>false</code> otherwise.
     */
    protected static boolean isUnicodeIdentifierChar(int c) {
        // check ASCII char range
        if (0x0000 <= c && c <= 0x007F) {
            return false;
        }

        // check UNICODE private use char
        if (isUnicodePrivateUseChar(c) || isUnicodePatternWhiteSpaceChar(c)) {
            return false;
        }

        // return false for UnicodePatternSyntaxChar
        return Character.isUnicodeIdentifierPart(c);
    }

    /**
     * Check whether a given char is a unicode private use char.
     * <p>
     * <code> UnicodePrivateUseChar := 0xE000 .. 0xF8FF | 0xF0000 .. 0xFFFFD | 0x100000 .. 0x10FFFD </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is a unicode private use char. <code>false</code> otherwise.
     */
    protected static boolean isUnicodePrivateUseChar(int c) {
        return (0xE000 <= c && c <= 0xF8FF || 0xF0000 <= c && c <= 0xFFFFD || 0x100000 <= c && c <= 0x10FFFD);
    }

    /**
     * Check whether a given char is a unicode pattern white space char.
     * <p>
     * <code> UnicodePatternWhiteSpaceChar := 0x200E | 0x200F | 0x2028 | 0x2029 </code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character is a unicode pattern white space char. <code>false</code> otherwise.
     */
    protected static boolean isUnicodePatternWhiteSpaceChar(int c) {
        return (0x200E == c || 0x200F == c || 0x2028 == c || 0x2029 == c);
    }

    /**
     * Process numeric escape.
     * <p>
     * <code>NumericEscape := \ u { CodePoint }</code>
     */
    protected void processNumericEscape() {
        // Process for '\'
        reader.advance();
        processNumericEscapeWithoutBackslash();
    }

    protected void processNumericEscapeWithoutBackslash() {
        // Process 'u {'
        this.reader.advance(2);

        // Process code-point
        if (!isHexDigit(this.reader.peek())) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE);
            return;
        }

        reader.advance();
        while (isHexDigit(this.reader.peek())) {
            reader.advance();
        }

        // Process close brace
        if (this.reader.peek() != LexerTerminals.CLOSE_BRACE) {
            reportLexerError(DiagnosticErrorCode.ERROR_INVALID_STRING_NUMERIC_ESCAPE_SEQUENCE);
            return;
        }

        this.reader.advance();
    }
}
