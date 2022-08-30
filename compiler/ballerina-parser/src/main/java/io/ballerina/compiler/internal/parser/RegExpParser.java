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
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * An regular expression parser for ballerina.
 *
 * @since 2201.3.0
 */
public class RegExpParser extends AbstractParser {
    private final Queue<STNode> interpolationExprs;

    protected RegExpParser(AbstractTokenReader tokenReader, Queue<STNode> interpolationExprs) {
        super(tokenReader, new RegExpParserErrorHandler(tokenReader));
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
        startContext(ParserRuleContext.REG_EXP_TERM);
        STToken nextToken = peek();
        SyntaxKind tokenKind = nextToken.kind;
        if (tokenKind == SyntaxKind.REGEXP_ASSERTION) {
            return parseReAssertion();
        }

        STNode reAtom;
        switch (nextToken.kind) {
            case REGEXP_CHAR_ESCAPE:
                reAtom = parseReAtomCharEscape();
                break;
            case OPEN_BRACKET_TOKEN:
            case NEGATED_CHAR_CLASS_START_TOKEN:
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
        if (nextToken.kind == SyntaxKind.REGEXP_QUANTIFIER) {
            STNode quantifier = parseReQuantifier();
            return STNodeFactory.createReAtomQuantifierNode(reAtom, quantifier);
        }

        endContext();

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
        STNode charEscape = consume();
        return STNodeFactory.createReAtomCharOrEscapeNode(charEscape);
    }

    /**
     * Parse character class.
     *
     * @return Character class node
     */
    private STNode parseCharacterClass() {
        startContext(ParserRuleContext.REG_EXP_CHAR_CLASS);
        STNode characterClassStart = parseCharacterClassStart();
        STToken nextToken = peek();
        STNode characterSet = null;
        if (!isEndOfCharacterClass(nextToken)) {
            characterSet = parseReCharSet(nextToken);
        }
        STNode characterClassEnd = parseCharacterClassEnd();
        endContext();
        return STNodeFactory.createReCharacterClassNode(characterClassStart, characterSet, characterClassEnd);
    }

    private boolean isEndOfCharacterClass(STToken nextToken) {
        return nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN || nextToken.kind == SyntaxKind.EOF_TOKEN;
    }

    /**
     * Parse open bracket token or open bracket token with ^ token in a character class.
     *
     * @return Open bracket token or open bracket token with ^ token node
     */
    private STNode parseCharacterClassStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACKET_TOKEN ||
                token.kind == SyntaxKind.NEGATED_CHAR_CLASS_START_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.REG_EXP_CHAR_CLASS_START);
            return parseCharacterClassStart();
        }
    }

    /**
     * Parse ReCharSet in a character class.
     *
     * @return ReCharSet node
     */
    private STNode parseReCharSet(STToken nextToken) {
        if (nextToken.kind == SyntaxKind.REGEXP_CHARSET) {
            STNode charSet = consume();
            return STNodeFactory.createReCharSetNode(charSet);
        }
        throw new IllegalStateException();
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
            recover(token, ParserRuleContext.REG_EXP_CHAR_CLASS_END);
            return parseCharacterClassEnd();
        }
    }

    /**
     * Parse quantifier.
     *
     * @return Quantifier node
     */
    private STNode parseReQuantifier() {
        STNode quantifier = consume();
        return STNodeFactory.createReQuantifierNode(quantifier);
    }

    /**
     * Parse capturing group.
     *
     * @return Capturing group node
     */
    private STNode parseCapturingGroups() {
        startContext(ParserRuleContext.REG_EXP_CAPTURING_GROUP);
        STNode openParenthesis = parseOpenParenthesis();
        STToken nextToken = peek();
        STNode flagExpression = null;
        if (nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            flagExpression = parseFlagExpression(nextToken);
        }
        STNode reDisjunction = parseReDisjunction();
        STNode closeParenthesis = parseCloseParenthesis();
        endContext();
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
        } else {
            recover(nextToken, ParserRuleContext.REG_EXP_CAPTURING_GROUP_START);
            return parseOpenParenthesis();
        }
    }

    /**
     * Parse flag expression in capturing group.
     *
     * @return Flag expression node
     */
    private STNode parseFlagExpression(STToken nextToken) {
        startContext(ParserRuleContext.REG_EXP_FLAG_EXPR);
        STNode questionMark = parseQuestionMark(nextToken);
        nextToken = peek();
        STNode reFlagsOnOff = null;
        if (!isEndOfFlagExpression(nextToken)) {
            reFlagsOnOff = STNodeFactory.createReFlagsOnOffNode(parseReFlagsOnOff(nextToken));
            nextToken = peek();
        }
        STNode colon = parseColon(nextToken);
        if (colon.isMissing()) {
            colon = SyntaxErrors.addDiagnostic(colon, DiagnosticErrorCode.ERROR_MISSING_REG_EXP_FLAG_EXPR_END_TOKEN);
        }
        endContext();
        return STNodeFactory.createReFlagExpressionNode(questionMark, reFlagsOnOff, colon);
    }

    private boolean isEndOfFlagExpression(STToken nextToken) {
        return nextToken.kind == SyntaxKind.COLON_TOKEN || nextToken.kind == SyntaxKind.EOF_TOKEN;
    }

    /**
     * Parse question mark token in a flag expression.
     *
     * @return Question mark token
     */
    private STNode parseQuestionMark(STToken nextToken) {
        if (nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            return consume();
        }
        throw new IllegalStateException();
    }

    /**
     * Parse ReFlagsOnOff in a flag expression..
     *
     * @return ReFlagsOnOff node
     */
    private STNode parseReFlagsOnOff(STToken nextToken) {
        if (nextToken.kind == SyntaxKind.RE_FLAGS_ON_OFF) {
            return consume();
        }
        throw new IllegalStateException();
    }

    /**
     * Parse colon token in a flag expression.
     *
     * @return Colon token
     */
    private STNode parseColon(STToken nextToken) {
        if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
            return consume();
        } else {
            return STNodeFactory.createMissingToken(SyntaxKind.COLON_TOKEN);
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
            recover(nextToken, ParserRuleContext.REG_EXP_CAPTURING_GROUP_END);
            return parseCloseParenthesis();
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

    private AbstractParserErrorHandler.Solution recover(STToken token, ParserRuleContext currentCtx) {
        return this.recover(token, currentCtx, false);
    }
}
