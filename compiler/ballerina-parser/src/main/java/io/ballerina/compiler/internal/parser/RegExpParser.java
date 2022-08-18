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
 * An regular expression parser for ballerina.
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

    private STNode parseReSequence() {
        List<STNode> reTerms = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfReSequence(nextToken.kind)) {
            STNode reTerm = parseRegExpTerm();
            reTerms.add(reTerm);
            nextToken = peek();
        }
        return STNodeFactory.createReSequenceNode(STNodeFactory.createNodeList(reTerms));
    }

    /**
     * Parse a single item in regular expression.
     *
     * @return Regular expression content item node
     */
    private STNode parseRegExpTerm() {
        STToken nextToken = peek();
        SyntaxKind tokenKind = nextToken.kind;
        if (tokenKind == SyntaxKind.REGEXP_ASSERTION) {
            return parseReAssertion();
        }

        STNode reAtom;
        switch (nextToken.kind) {
            case REGEXP_ATOM_CHAR_ESCAPE:
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

        return STNodeFactory.createReAtomQuantifierNode(reAtom, null);
    }

    private STNode parseReAssertion() {
        STNode assertion = consume();
        return STNodeFactory.createReAssertionNode(assertion);
    }

    private STNode parseReAtomCharEscape() {
        STNode atoms = consume();
        return STNodeFactory.createReAtomCharOrEscapeNode(atoms);
    }

    private STNode parseCharacterClass() {
        STNode characterClassStart = parseCharacterClassStart();
        STNode characterSet = parseReCharSet();
        STNode characterClassEnd = parseCharacterClassEnd();

        return STNodeFactory.createReCharacterClassNode(characterClassStart, characterSet, characterClassEnd);
    }

    private STNode parseCharacterClassStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACKET_TOKEN ||
                token.kind == SyntaxKind.NEGATED_CHAR_CLASS_START_TOKEN) {
            return consume();
        } else {
            //recover(token, ParserRuleContext.XML_COMMENT_START);
            return parseCharacterClassStart();
        }
    }

    private STNode parseReCharSet() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.REGEXP_CHARSET) {
            STNode charSet = consume();
            return STNodeFactory.createReCharSetNode(charSet);
        }
        throw new IllegalStateException();
    }

    private STNode parseCharacterClassEnd() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN) {
            return consume();
        } else {
            //recover(token, ParserRuleContext.XML_COMMENT_START);
            return parseCharacterClassStart();
        }
    }

    private STNode parseReQuantifier() {
        STNode quantifier = consume();
        return STNodeFactory.createReQuantifierNode(quantifier);
    }

    private STNode parseCapturingGroups() {
        STNode openParenthesis = consume();
        STNode flagExpression = parseFlagExpression();
        STNode reDisjunction = parseReDisjunction();
        STNode closeParenthesis = consume();

        return STNodeFactory.createReCapturingGroupsNode(openParenthesis, flagExpression, reDisjunction,
                closeParenthesis);
    }

    private STNode parseFlagExpression() {
        STToken nextToken = peek();
        STNode questionMark = null;
        STNode reFlagsOnOff = null;
        STNode colon = null;
        if (nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            questionMark = consume();
        }

        nextToken = peek();
        if (nextToken.kind == SyntaxKind.RE_FLAGS_ON_OFF) {
            reFlagsOnOff = consume();
        }
        STNode reFlags = STNodeFactory.createReFlagsOnOffNode(reFlagsOnOff);

        nextToken = peek();
        if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
            colon = consume();
        }

        return STNodeFactory.createReFlagExpressionNode(questionMark, reFlags, colon);
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
        // consume the injected interpolation start and end. i.e: &{}
        consume();
        consume();
        return this.interpolationExprs.remove();
    }

    /**
     * Check whether the parser has reached the end of the back-tick literal.
     *
     * @param kind           Next token kind
     * @return <code>true</code> if this is the end of the back-tick literal. <code>false</code> otherwise
     */
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

    private boolean isEndOfFlagExpr(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
            case COLON_TOKEN:
                return true;
            default:
                return false;
        }
    }
}
