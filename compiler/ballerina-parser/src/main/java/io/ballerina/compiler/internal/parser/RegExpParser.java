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
import io.ballerina.compiler.internal.parser.tree.STAbstractNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * A regular expression parser for ballerina.
 *
 * @since 2201.3.0
 */
public class RegExpParser extends AbstractParser {
    private final Queue<STNode> interpolationExprs;

    protected RegExpParser(AbstractTokenReader tokenReader, Queue<STNode> interpolationExprs) {
        super(tokenReader);
        this.interpolationExprs = interpolationExprs;
    }

    @Override
    public STNode parse() {
        return parseReDisjunction(false);
    }

    /**
     * Parse reDisjunction.
     *
     * @return ReDisjunction node
     */
    private STNode parseReDisjunction(boolean inCapturingGroup) {
        List<STNode> reSequenceList = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfReDisjunction(nextToken.kind, inCapturingGroup)) {
            STNode reSequence = parseReSequence(inCapturingGroup);
            reSequenceList.add(reSequence);
            nextToken = peek();
            if (nextToken.kind == SyntaxKind.PIPE_TOKEN) {
                STNode pipe = consume();
                reSequenceList.add(pipe);
                nextToken = peek();
            }
        }
        return STAbstractNodeFactory.createNodeList(reSequenceList);
    }

    /**
     * Parse reSequence.
     *
     * @return ReSequence node
     */
    private STNode parseReSequence(boolean inCapturingGroup) {
        List<STNode> reTerms = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfReSequence(nextToken.kind, inCapturingGroup)) {
            STNode reTerm = parseReTerm();
            reTerms.add(reTerm);
            nextToken = peek();
        }
        return STNodeFactory.createReSequenceNode(STAbstractNodeFactory.createNodeList(reTerms));
    }

    /**
     * Parse reTerm.
     *
     * @return ReTerm node
     */
    private STNode parseReTerm() {
        STToken nextToken = peek();
        SyntaxKind tokenKind = nextToken.kind;
        if (tokenKind == SyntaxKind.BITWISE_XOR_TOKEN ||
                tokenKind == SyntaxKind.DOLLAR_TOKEN) {
            return parseReAssertion();
        }

        STNode reAtom;
        switch (nextToken.kind) {
            case RE_LITERAL_CHAR:
            case RE_NUMERIC_ESCAPE:
            case RE_CONTROL_ESCAPE:
            case COMMA_TOKEN:
            case DOT_TOKEN:
            case DIGIT:
                reAtom = parseChars();
                break;
            case BACK_SLASH_TOKEN:
                reAtom = parseReEscape();
                break;
            case OPEN_BRACKET_TOKEN:
                reAtom = parseCharacterClass();
                break;
            case OPEN_PAREN_TOKEN:
                reAtom = parseCapturingGroup();
                break;
            case INTERPOLATION_START_TOKEN:
                reAtom = parseInterpolation();
                break;
            default:
                // Here the token is a syntax char, which is invalid. A syntax char should have a backslash prior
                // to the token.
                STNode missingBackSlash =
                        SyntaxErrors.createMissingRegExpTokenWithDiagnostics(SyntaxKind.BACK_SLASH_TOKEN);
                reAtom = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(missingBackSlash, consume());
                break;
        }

        nextToken = peek();
        STNode quantifier = parseOptionalQuantifier(nextToken.kind);

        if (quantifier != null) {
            return STNodeFactory.createReAtomQuantifierNode(reAtom, quantifier);
        }

        return STNodeFactory.createReAtomQuantifierNode(reAtom, null);
    }

    private STNode parseOptionalQuantifier(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case PLUS_TOKEN:
            case ASTERISK_TOKEN:
            case QUESTION_MARK_TOKEN:
            case OPEN_BRACE_TOKEN:
                return parseReQuantifier();
            default:
                return null;
        }
    }

    /**
     * Parse ReAssertion.
     *
     * @return ReAssertion node
     */
    private STNode parseReAssertion() {
        STNode assertion = consume();
        return STNodeFactory.createReAssertionNode(assertion);
    }

    /**
     * Parse ReQuoteEscape, ReUnicodePropertyEscape or ReSimpleCharClassEscape.
     *
     * @return ReQuoteEscapeNode, ReUnicodePropertyEscapeNode or ReSimpleCharClassEscapeNode node
     */
    private STNode parseReEscape() {
        STNode backSlash = consume();
        return parseEscapeChar(backSlash);
    }

    private STNode parseEscapeChar(STNode backSlash) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case RE_PROPERTY:
                return parseReUnicodePropertyEscape(backSlash);
            case BITWISE_XOR_TOKEN:
            case DOLLAR_TOKEN:
            case BACK_SLASH_TOKEN:
            case DOT_TOKEN:
            case ASTERISK_TOKEN:
            case PLUS_TOKEN:
            case QUESTION_MARK_TOKEN:
            case OPEN_PAREN_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case PIPE_TOKEN:
                return parseReQuoteEscape(backSlash);
            default:
                if (isReSimpleCharClassCode(nextToken)) {
                    return parseReSimpleCharClassEscape(backSlash);
                }
                // "BITWISE_XOR_TOKEN" syntax kind is used here to mock the flow for a ReSyntaxChar and give the
                // diagnostic, instead of having a separate syntax kind to represent ReSyntaxChar.
                STNode syntaxChar = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.BITWISE_XOR_TOKEN,
                        DiagnosticErrorCode.ERROR_INVALID_RE_SYNTAX_CHAR);
                syntaxChar = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(syntaxChar, consume());
                return STNodeFactory.createReQuoteEscapeNode(backSlash, syntaxChar);
        }
    }

    /**
     * Parse ReLiteralChar, ., NumericEscape or ControlEscape.
     *
     * @return ReAtomCharOrEscape node
     */
    private STNode parseChars() {
        STNode chars = consume();
        return STNodeFactory.createReAtomCharOrEscapeNode(chars);
    }

    /**
     * Parse ReUnicodePropertyEscape.
     *
     * @return ReUnicodePropertyEscapeNode node
     */
    private STNode parseReUnicodePropertyEscape(STNode backSlash) {
        STNode property = consume();
        STNode openBrace = parseOpenBrace();
        STNode unicodeProperty = parseUnicodeProperty();
        STNode closeBrace = parseCloseBrace();
        return STNodeFactory.createReUnicodePropertyEscapeNode(backSlash, property, openBrace, unicodeProperty,
                closeBrace);
    }

    /**
     * Parse { token in ReUnicodePropertyEscape.
     *
     * @return { token
     */
    private STNode parseOpenBrace() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACE_TOKEN) {
            return consume();
        }
        return createMissingTokenWithDiagnostics(SyntaxKind.OPEN_BRACE_TOKEN);
    }

    /**
     * Parse ReUnicodeScript or ReUnicodeGeneralCategory.
     *
     * @return ReUnicodeScriptNode or ReUnicodeGeneralCategoryNode node
     */
    private STNode parseUnicodeProperty() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RE_UNICODE_SCRIPT_START) {
            return parseReUnicodeScript();
        }
        return parseReUnicodeGeneralCategory();
    }

    /**
     * Parse ReUnicodeScript.
     *
     * @return ReUnicodeScriptNode node
     */
    private STNode parseReUnicodeScript() {
        STNode scriptStart = consume();
        STToken nextToken = peek();
        STNode unicodePropertyValue;
        if (nextToken.kind == SyntaxKind.RE_UNICODE_PROPERTY_VALUE) {
            unicodePropertyValue = consume();
        } else {
            unicodePropertyValue = createMissingTokenWithDiagnostics(SyntaxKind.RE_UNICODE_PROPERTY_VALUE);
        }
        return STNodeFactory.createReUnicodeScriptNode(scriptStart, unicodePropertyValue);
    }

    /**
     * Parse ReUnicodeGeneralCategory.
     *
     * @return ReUnicodeGeneralCategoryNode node
     */
    private STNode parseReUnicodeGeneralCategory() {
        STToken nextToken = peek();
        STNode scriptStart = null;
        if (nextToken.kind == SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_START) {
            scriptStart = consume();
        }
        nextToken = peek();
        STNode generalCategory;
        if (nextToken.kind == SyntaxKind.RE_UNICODE_GENERAL_CATEGORY_NAME) {
            generalCategory = consume();
        } else {
            generalCategory = createMissingTokenWithDiagnostics(SyntaxKind.RE_UNICODE_PROPERTY_VALUE);
        }
        return STNodeFactory.createReUnicodeGeneralCategoryNode(scriptStart, generalCategory);
    }

    /**
     * Parse ReQuoteEscape.
     *
     * @return ReQuoteEscapeNode node
     */
    private STNode parseReQuoteEscape(STNode backSlash) {
        STNode syntaxChar = consume();
        return STNodeFactory.createReQuoteEscapeNode(backSlash, syntaxChar);
    }

    static boolean isReSimpleCharClassCode(STToken token) {
        if (token.kind != SyntaxKind.RE_LITERAL_CHAR) {
            return false;
        }
        switch (token.text()) {
            case "d":
            case "D":
            case "s":
            case "S":
            case "w":
            case "W":
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse ReSimpleCharClassEscape.
     *
     * @return ReSimpleCharClassEscapeNode node
     */
    private STNode parseReSimpleCharClassEscape(STNode backSlash) {
        STNode simpleCharClassCode = getLiteralValueToken(consume(), SyntaxKind.RE_SIMPLE_CHAR_CLASS_CODE);
        return STNodeFactory.createReSimpleCharClassEscapeNode(backSlash, simpleCharClassCode);
    }

    /**
     * Parse character class.
     *
     * @return Character class node
     */
    private STNode parseCharacterClass() {
        STNode characterClassStart = consume();
        STNode negation = parseNegation();
        STNode characterSet = parseReCharSet(negation);
        STNode characterClassEnd = parseCharacterClassEnd();
        return STNodeFactory.createReCharacterClassNode(characterClassStart, negation, characterSet, characterClassEnd);
    }

    /**
     * Parse ^ token in a character class.
     *
     * @return ^ token
     */
    private STNode parseNegation() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.BITWISE_XOR_TOKEN) {
            return consume();
        }
        return null;
    }

    /**
     * Parse ReCharSet in a character class.
     *
     * @return ReCharSet node
     */
    private STNode parseReCharSet(STNode prevNode) {
        STToken nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return null;
        }
        STNode startReCharSetAtom = parseCharSetAtom(nextToken, prevNode);
        nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return startReCharSetAtom;
        }
        if ("-".equals(nextToken.text())) {
            STNode minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return STNodeFactory.createReCharSetAtomWithReCharSetNoDashNode(startReCharSetAtom, minus);
            }
            STNode rhsReCharSetAtom = parseCharSetAtom(nextToken, minus);
            STNode reCharSetRange = STNodeFactory.createReCharSetRangeNode(startReCharSetAtom, minus,
                    rhsReCharSetAtom);
            STNode reCharSet = parseReCharSet(reCharSetRange);
            return STNodeFactory.createReCharSetRangeWithReCharSetNode(reCharSetRange, reCharSet);
        }
        STNode reCharSetNoDash = parseCharSetNoDash(nextToken, startReCharSetAtom);
        return STNodeFactory.createReCharSetAtomWithReCharSetNoDashNode(startReCharSetAtom, reCharSetNoDash);
    }

    /**
     * Parse ReCharSetNoDash in a character class.
     *
     * @return ReCharSetNoDash node
     */
    private STNode parseCharSetNoDash(STToken nextToken, STNode prevNode) {
        STNode startReCharSetAtomNoDash = parseCharSetAtom(nextToken, prevNode);
        nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return startReCharSetAtomNoDash;
        }
        if ("-".equals(nextToken.text())) {
            STNode minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return STNodeFactory.createReCharSetAtomNoDashWithReCharSetNoDashNode(startReCharSetAtomNoDash,
                        minus);
            }
            STNode rhsReCharSetAtom = parseCharSetAtom(nextToken, minus);
            STNode reCharSetRange = STNodeFactory.createReCharSetRangeNoDashNode(startReCharSetAtomNoDash, minus,
                    rhsReCharSetAtom);
            STNode reCharSet = parseReCharSet(reCharSetRange);
            return STNodeFactory.createReCharSetRangeNoDashWithReCharSetNode(reCharSetRange, reCharSet);
        }
        STNode reCharSetNoDash = parseCharSetNoDash(nextToken, startReCharSetAtomNoDash);
        return STNodeFactory.createReCharSetAtomNoDashWithReCharSetNoDashNode(startReCharSetAtomNoDash,
                reCharSetNoDash);
    }

    private STNode parseCharSetAtom(STToken nextToken, STNode prevNode) {
        switch (nextToken.kind) {
            case RE_NUMERIC_ESCAPE:
            case RE_CONTROL_ESCAPE:
                return parseChars();
            case BACK_SLASH_TOKEN:
                STToken token = peek(2);
                if (token.kind == SyntaxKind.RE_LITERAL_CHAR &&
                        token.text().equals(Character.toString(LexerTerminals.MINUS))) {
                    consume();
                    STNode minusToken = consume();
                    return STNodeFactory.createToken(SyntaxKind.ESCAPED_MINUS_TOKEN, minusToken.leadingMinutiae(),
                            minusToken.trailingMinutiae());
                }
                return parseReEscape();
            default:
                STNode consumedToken = consume();
                if ("-".equals(nextToken.text())) {
                    return consumedToken;
                }
                if (isReCharSetLiteralChar(nextToken.text())) {
                    return consumedToken;
                }
                return SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(prevNode, consumedToken,
                        DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
        }
    }

    private boolean isReCharSetLiteralChar(String tokenText) {
        switch (tokenText) {
            case "\\":
            case "-":
            case "]":
                return false;
            default:
                return true;
        }
    }

    private boolean isCharacterClassEnd(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse close bracket token in a character class.
     *
     * @return Close bracket token
     */
    private STNode parseCharacterClassEnd() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN) {
            return consume();
        } else {
            return createMissingTokenWithDiagnostics(SyntaxKind.CLOSE_BRACKET_TOKEN);
        }
    }

    /**
     * Parse quantifier.
     *
     * @return Quantifier node
     */
    private STNode parseReQuantifier() {
        STNode quantifier = parseBaseQuantifier();
        STNode nonGreedyChar = parseNonGreedyChar();
        return STNodeFactory.createReQuantifierNode(quantifier, nonGreedyChar);
    }

    /**
     * Parse ReBaseQuantifier.
     *
     * @return ReBaseQuantifier node
     */
    private STNode parseBaseQuantifier() {
        STToken nextToken = peek();
        if (nextToken.kind != SyntaxKind.OPEN_BRACE_TOKEN) {
            return consume();
        }
        // Parse the braced quantifier.
        STNode openBrace = consume();
        nextToken = peek();
        if (isInvalidDigit(nextToken, true)) {
            openBrace = invalidateNonDigitNodesAndAddToTrailingMinutiae(openBrace, true);
        }
        STNode leastDigits = parseDigits(true);
        STNode comma = null;
        STNode mostDigits = null;
        nextToken = peek();
        if (nextToken.kind == SyntaxKind.COMMA_TOKEN) {
            comma = consume();
            if (isInvalidDigit(nextToken, false)) {
                comma = invalidateNonDigitNodesAndAddToTrailingMinutiae(comma, false);
            }
            mostDigits = parseDigits(false);
        }
        STNode closeBrace = parseCloseBrace();
        return STNodeFactory.createReBracedQuantifierNode(openBrace, leastDigits, comma, mostDigits, closeBrace);
    }

    /**
     * Parse digits in braced quantifier.
     *
     * @return Node list of digits
     */
    private STNode parseDigits(boolean isLeastDigits) {
        List<STNode> digits = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfDigits(nextToken.kind, isLeastDigits)) {
            STNode digit = consume();
            if (nextToken.kind != SyntaxKind.DIGIT) {
                updateLastNodeInListWithInvalidNode(digits, digit, DiagnosticErrorCode.ERROR_INVALID_TOKEN_IN_REG_EXP);
            } else {
                digits.add(digit);
            }
            nextToken = peek();
        }
        // There should be at least one least digit.
        if (isLeastDigits && digits.isEmpty()) {
            digits.add(createMissingTokenWithDiagnostics(SyntaxKind.DIGIT));
        }
        return STAbstractNodeFactory.createNodeList(digits);
    }

    private boolean isInvalidDigit(STToken nextToken, boolean isLeastDigits) {
        return !isEndOfDigits(nextToken.kind, isLeastDigits) && nextToken.kind != SyntaxKind.DIGIT;
    }

    private boolean isEndOfDigits(SyntaxKind kind, boolean isLeastDigits) {
        switch (kind) {
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
                return true;
            case COMMA_TOKEN:
                return isLeastDigits;
            default:
                return false;
        }
    }

    /**
     * Parse close brace in braced quantifier.
     *
     * @return Close brace token
     */
    private STNode parseCloseBrace() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.CLOSE_BRACE_TOKEN) {
            return consume();
        } else {
            return createMissingTokenWithDiagnostics(SyntaxKind.CLOSE_BRACE_TOKEN);
        }
    }

    /**
     * Parse ? token that comes after the ReBaseQuantifier.
     *
     * @return ? token
     */
    private STNode parseNonGreedyChar() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            return consume();
        }
        return null;
    }

    /**
     * Parse capturing group.
     *
     * @return Capturing group node
     */
    private STNode parseCapturingGroup() {
        STNode openParenthesis = consume();
        STToken nextToken = peek();
        STNode flagExpression = null;
        if (nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            flagExpression = parseFlagExpression();
        }
        STNode reDisjunction = parseReDisjunction(true);
        STNode closeParenthesis = parseCloseParenthesis();
        return STNodeFactory.createReCapturingGroupsNode(openParenthesis, flagExpression, reDisjunction,
                closeParenthesis);
    }

    /**
     * Parse flag expression in capturing group.
     *
     * @return Flag expression node
     */
    private STNode parseFlagExpression() {
        STNode questionMark = consume();
        STToken nextToken = peek();
        STNode reFlagsOnOff = null;
        if (!isEndOfFlagExpression(nextToken.kind)) {
            if (isInvalidFlag(nextToken, true)) {
                questionMark = invalidateNonFlagNodesAndAddToTrailingMinutiae(questionMark, true);
            }
            reFlagsOnOff = parseReFlagsOnOff();
        }
        STNode colon = parseColon();
        return STNodeFactory.createReFlagExpressionNode(questionMark, reFlagsOnOff, colon);
    }

    private boolean isEndOfFlagExpression(SyntaxKind kind) {
        return kind == SyntaxKind.COLON_TOKEN || kind == SyntaxKind.EOF_TOKEN;
    }

    private boolean isInvalidFlag(STToken nextToken, boolean isLhsFlag) {
        return !isEndOfReFlags(nextToken, isLhsFlag) && !isReFlag(nextToken);
    }

    static boolean isReFlag(STToken nextToken) {
        if (nextToken.kind != SyntaxKind.RE_LITERAL_CHAR) {
            return false;
        }
        switch (nextToken.text()) {
            case "m":
            case "s":
            case "i":
            case "x":
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse ReFlagsOnOff in a flag expression.
     *
     * @return ReFlagsOnOff node
     */
    private STNode parseReFlagsOnOff() {
        STNode lhsReFlags = parseReFlags(true);
        STToken nextToken = peek();
        STNode dash = null;
        STNode rhsReFlags = null;
        if (nextToken.kind == SyntaxKind.RE_LITERAL_CHAR &&
                nextToken.text().equals(Character.toString(LexerTerminals.MINUS))) {
            dash = getToken(consume(), SyntaxKind.MINUS_TOKEN);
            if (isInvalidFlag(nextToken, false)) {
                dash = invalidateNonFlagNodesAndAddToTrailingMinutiae(dash, false);
            }
            rhsReFlags = parseReFlags(false);
        }
        return STNodeFactory.createReFlagsOnOffNode(lhsReFlags, dash, rhsReFlags);
    }

    /**
     * Parse ReFlags in ReFlagsOnOff.
     *
     * @return ReFlags node
     */
    private STNode parseReFlags(boolean isLhsFlag) {
        List<STNode> reFlags = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfReFlags(nextToken, isLhsFlag)) {
            STNode reFlag = getLiteralValueToken(consume(), SyntaxKind.RE_FLAGS_VALUE);
            if (!isReFlag(nextToken)) {
                updateLastNodeInListWithInvalidNode(reFlags, reFlag,
                        DiagnosticErrorCode.ERROR_INVALID_FLAG_IN_REG_EXP);
            } else {
                reFlags.add(reFlag);
            }
            nextToken = peek();
        }
        return STNodeFactory.createReFlagsNode(STAbstractNodeFactory.createNodeList(reFlags));
    }

    private boolean isEndOfReFlags(STToken nextToken, boolean isLhsFlag) {
        SyntaxKind kind = nextToken.kind;
        if (kind == SyntaxKind.EOF_TOKEN) {
            return true;
        }
        if (kind != SyntaxKind.RE_LITERAL_CHAR) {
            return false;
        }
        String tokenText = nextToken.text();
        return (isLhsFlag && tokenText.equals(Character.toString(LexerTerminals.MINUS))) ||
                tokenText.equals(Character.toString(LexerTerminals.COLON));
    }

    /**
     * Parse colon token in a flag expression.
     *
     * @return Colon token
     */
    private STNode parseColon() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.RE_LITERAL_CHAR &&
                nextToken.text().equals(Character.toString(LexerTerminals.COLON))) {
            return getToken(consume(), SyntaxKind.COLON_TOKEN);
        } else {
            return createMissingTokenWithDiagnostics(SyntaxKind.COLON_TOKEN);
        }
    }

    /**
     * Parse close parenthesis token of a capturing group.
     *
     * @return Close parenthesis token
     */
    private STNode parseCloseParenthesis() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            return consume();
        } else {
            return createMissingTokenWithDiagnostics(SyntaxKind.CLOSE_PAREN_TOKEN);
        }
    }

    /**
     * Parse interpolation of a back-tick string.
     * <p>
     * <code>
     * interpolation := ${ expression }
     * </code>
     *
     * @return Interpolation node
     */
    private STNode parseInterpolation() {
        // Consume the injected interpolation start and end. i.e: &{}.
        consume();
        consume();
        return this.interpolationExprs.remove();
    }

    private boolean isEndOfReDisjunction(SyntaxKind kind, boolean inCapturingGroup) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
                return true;
            default:
                return kind == SyntaxKind.CLOSE_PAREN_TOKEN && inCapturingGroup;
        }
    }

    private boolean isEndOfReSequence(SyntaxKind kind, boolean inCapturingGroup) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case PIPE_TOKEN:
                return true;
            default:
                return kind == SyntaxKind.CLOSE_PAREN_TOKEN && inCapturingGroup;
        }
    }

    private STNode getToken(STToken token, SyntaxKind syntaxKind) {
        return STAbstractNodeFactory.createToken(syntaxKind, token.leadingMinutiae(),
                token.trailingMinutiae(), token.diagnostics());
    }

    private STNode getLiteralValueToken(STToken token, SyntaxKind syntaxKind) {
        return STAbstractNodeFactory.createLiteralValueToken(syntaxKind, token.text(),
                token.leadingMinutiae(), token.trailingMinutiae(), token.diagnostics());
    }

    /**
     * Marks the next non-digits in a braced quantifier in regular expression as invalid and attach them as trailing
     * minutiae of the given node.
     *
     * @param node the node to attach the invalid tokens as trailing minutiae.
     * @return Parsed node
     */
    private STNode invalidateNonDigitNodesAndAddToTrailingMinutiae(STNode node, boolean isLeastDigits) {
        node = addInvalidNodeStackToTrailingMinutiae(node);

        while (isInvalidDigit(peek(), isLeastDigits)) {
            node = addTrailingInvalidNode(node, DiagnosticErrorCode.ERROR_INVALID_QUANTIFIER_IN_REG_EXP);
        }

        return node;
    }

    private STNode addTrailingInvalidNode(STNode node, DiagnosticErrorCode errorCode) {
        STToken invalidToken = consume();
        return SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(node, invalidToken, errorCode);
    }

    /**
     * Marks the next non-flags in a flag expression in capturing group as invalid and attach them as trailing
     * minutiae of the given node.
     *
     * @param node the node to attach the invalid tokens as trailing minutiae.
     * @return Parsed node
     */
    private STNode invalidateNonFlagNodesAndAddToTrailingMinutiae(STNode node, boolean isLhsFlag) {
        node = addInvalidNodeStackToTrailingMinutiae(node);

        while (isInvalidFlag(peek(), isLhsFlag)) {
            node = addTrailingInvalidNode(node, DiagnosticErrorCode.ERROR_INVALID_FLAG_IN_REG_EXP);
        }

        return node;
    }

    private STNode createMissingTokenWithDiagnostics(SyntaxKind expectedKind) {
        return SyntaxErrors.createMissingRegExpTokenWithDiagnostics(expectedKind);
    }
}
