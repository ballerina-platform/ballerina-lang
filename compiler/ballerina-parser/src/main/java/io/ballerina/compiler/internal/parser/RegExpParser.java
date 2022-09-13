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
        return parseReDisjunction();
    }

    /**
     * Parse reDisjunction.
     *
     * @return ReDisjunction node
     */
    private STNode parseReDisjunction() {
        List<STNode> reSequenceList = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfReDisjunction(nextToken.kind)) {
            STNode reSequence = parseReSequence();
            reSequenceList.add(reSequence);
            nextToken = peek();
            if (nextToken.kind == SyntaxKind.PIPE_TOKEN) {
                STNode pipe = consume();
                reSequenceList.add(pipe);
                nextToken = peek();
            }
        }
        return STNodeFactory.createNodeList(reSequenceList);
    }

    /**
     * Parse reSequence.
     *
     * @return ReSequence node
     */
    private STNode parseReSequence() {
        List<STNode> reTerms = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfReSequence(nextToken.kind)) {
            STNode reTerm = parseReTerm();
            reTerms.add(reTerm);
            nextToken = peek();
        }
        return STNodeFactory.createReSequenceNode(STNodeFactory.createNodeList(reTerms));
    }

    /**
     * Parse reTerm.
     *
     * @return ReTerm node
     */
    private STNode parseReTerm() {
        STToken nextToken = peek();
        SyntaxKind tokenKind = nextToken.kind;
        if (tokenKind == SyntaxKind.RE_ASSERTION_VALUE) {
            return parseReAssertion();
        }

        STNode reAtom;
        switch (nextToken.kind) {
            case RE_CHAR_ESCAPE_VALUE:
                reAtom = parseReAtomCharEscape();
                break;
            case OPEN_BRACKET_TOKEN:
                reAtom = parseCharacterClass();
                break;
            case OPEN_PAREN_TOKEN:
                reAtom = parseCapturingGroups();
                break;
            case INTERPOLATION_START_TOKEN:
                reAtom = parseInterpolation();
                break;
            default:
                reAtom = consume();
        }

        nextToken = peek();
        if (nextToken.kind == SyntaxKind.RE_BASE_QUANTIFIER_VALUE ||
                nextToken.kind == SyntaxKind.OPEN_BRACE_TOKEN) {
            STNode quantifier = parseReQuantifier();
            return STNodeFactory.createReAtomQuantifierNode(reAtom, quantifier);
        }

        return STNodeFactory.createReAtomQuantifierNode(reAtom, null);
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
     * Parse ReLiteralChar, . or ReEscape.
     *
     * @return ReAtomCharOrEscape node
     */
    private STNode parseReAtomCharEscape() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.BACK_SLASH_TOKEN) {

        }
        STNode charEscape = consume();
        return STNodeFactory.createReAtomCharOrEscapeNode(charEscape);
    }

    /**
     * Parse character class.
     *
     * @return Character class node
     */
    private STNode parseCharacterClass() {
        STNode characterClassStart = parseCharacterClassStart();
        STNode negation = parseNegation();
        STNode characterSet = parseReCharSet();
        STNode characterClassEnd = parseCharacterClassEnd();
        return STNodeFactory.createReCharacterClassNode(characterClassStart, negation, characterSet, characterClassEnd);
    }

    /**
     * Parse open bracket token in a character class.
     *
     * @return Open bracket token
     */
    private STNode parseCharacterClassStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACKET_TOKEN) {
            return consume();
        }
        throw new IllegalStateException();
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
    private STNode parseReCharSet() {
        STToken nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return null;
        }
        STNode startReCharSetAtom = consume();
        nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return startReCharSetAtom;
        }
        if (nextToken.kind == SyntaxKind.MINUS_TOKEN) {
            STNode minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return STNodeFactory.createReCharSetAtomWithReCharSetNoDashNode(startReCharSetAtom, minus);
            }
            STNode rhsReCharSetAtom = consume();
            STNode reCharSetRange = STNodeFactory.createReCharSetRangeNode(startReCharSetAtom, minus, rhsReCharSetAtom);
            STNode reCharSet = parseReCharSet();
            return STNodeFactory.createReCharSetRangeWithReCharSetNode(reCharSetRange, reCharSet);
        }
        STNode reCharSetNoDash = parseCharSetAtomNoDash();
        return STNodeFactory.createReCharSetAtomWithReCharSetNoDashNode(startReCharSetAtom, reCharSetNoDash);
    }

    /**
     * Parse ReCharSetNoDash in a character class.
     *
     * @return ReCharSetNoDash node
     */
    private STNode parseCharSetAtomNoDash() {
        STNode startReCharSetAtomNoDash = consume();
        STToken nextToken = peek();
        if (isCharacterClassEnd(nextToken.kind)) {
            return startReCharSetAtomNoDash;
        }
        if (nextToken.kind == SyntaxKind.MINUS_TOKEN) {
            STNode minus = consume();
            nextToken = peek();
            if (isCharacterClassEnd(nextToken.kind)) {
                return STNodeFactory.createReCharSetAtomNoDashWithReCharSetNoDashNode(startReCharSetAtomNoDash, minus);
            }
            STNode rhsReCharSetAtom = consume();
            STNode reCharSetRange = STNodeFactory.createReCharSetRangeNoDashNode(startReCharSetAtomNoDash, minus, rhsReCharSetAtom);
            STNode reCharSet = parseReCharSet();
            return STNodeFactory.createReCharSetRangeNoDashWithReCharSetNode(reCharSetRange, reCharSet);
        }
        STNode reCharSetNoDash = parseCharSetAtomNoDash();
        return STNodeFactory.createReCharSetAtomNoDashWithReCharSetNoDashNode(startReCharSetAtomNoDash, reCharSetNoDash);
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
        STNode leastDigits = parseDigits(true);
        STNode comma = null;
        STNode mostDigits = null;
        nextToken = peek();
        if (nextToken.kind == SyntaxKind.COMMA_TOKEN) {
            comma = consume();
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
            digits.add(digit);
            nextToken = peek();
        }
        return STNodeFactory.createNodeList(digits);
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
            return createMissingTokenWithDiagnostics(SyntaxKind.CLOSE_PAREN_TOKEN);
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
    private STNode parseCapturingGroups() {
        STNode openParenthesis = parseOpenParenthesis();
        STToken nextToken = peek();
        STNode flagExpression = null;
        if (nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            flagExpression = parseFlagExpression();
        }
        STNode reDisjunction = parseReDisjunction();
        STNode closeParenthesis = parseCloseParenthesis();
        return STNodeFactory.createReCapturingGroupsNode(openParenthesis, flagExpression, reDisjunction,
                closeParenthesis);
    }

    /**
     * Parse open parenthesis token in capturing group.
     *
     * @return Open parenthesis token
     */
    private STNode parseOpenParenthesis() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            return consume();
        }
        throw new IllegalStateException();
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
            reFlagsOnOff = parseReFlagsOnOff();
        }
        STNode colon = parseColon();
        return STNodeFactory.createReFlagExpressionNode(questionMark, reFlagsOnOff, colon);
    }

    private boolean isEndOfFlagExpression(SyntaxKind kind) {
        return kind == SyntaxKind.COLON_TOKEN || kind == SyntaxKind.EOF_TOKEN;
    }

    /**
     * Parse ReFlagsOnOff in a flag expression.
     *
     * @return ReFlagsOnOff node
     */
    private STNode parseReFlagsOnOff() {
        STNode lhsReFlags = parseReFlags();
        STToken nextToken = peek();
        STNode dash = null;
        STNode rhsReFlags = null;
        if (nextToken.kind == SyntaxKind.MINUS_TOKEN) {
            dash = consume();
            rhsReFlags = parseReFlags();
        }
        return STNodeFactory.createReFlagsOnOffNode(lhsReFlags, dash, rhsReFlags);
    }

    /**
     * Parse ReFlags in ReFlagsOnOff.
     *
     * @return ReFlags node
     */
    private STNode parseReFlags() {
        List<STNode> reFlags = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfReFlags(nextToken.kind)) {
            STNode reFlag = consume();
            reFlags.add(reFlag);
            nextToken = peek();
        }
        return STNodeFactory.createNodeList(reFlags);
    }

    private boolean isEndOfReFlags(SyntaxKind kind) {
        return kind == SyntaxKind.MINUS_TOKEN || kind == SyntaxKind.COLON_TOKEN || kind == SyntaxKind.EOF_TOKEN;
    }

    /**
     * Parse colon token in a flag expression.
     *
     * @return Colon token
     */
    private STNode parseColon() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
            return consume();
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

    private boolean isEndOfReDisjunction(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case CLOSE_PAREN_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isEndOfReSequence(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case PIPE_TOKEN:
            case CLOSE_PAREN_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode createMissingTokenWithDiagnostics(SyntaxKind expectedKind) {
        return SyntaxErrors.createMissingRegExpTokenWithDiagnostics(expectedKind);
    }
}
