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

import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayDeque;

/**
 * Error handler to recover from XML parsing errors.
 * 
 * @since 2.0.0
 */
public class XMLParserErrorHandler extends AbstractParserErrorHandler {

    private static final ParserRuleContext[] XML_CONTENT = { ParserRuleContext.XML_START_OR_EMPTY_TAG,
            ParserRuleContext.XML_TEXT, ParserRuleContext.XML_END_TAG /* ParserRuleContext.XML_PI, */ };

    private static final ParserRuleContext[] XML_ATTRIBUTES =
            { ParserRuleContext.XML_ATTRIBUTE, ParserRuleContext.XML_START_OR_EMPTY_TAG_END };

    private static final ParserRuleContext[] XML_START_OR_EMPTY_TAG_END =
            { ParserRuleContext.GT_TOKEN, ParserRuleContext.SLASH };

    public XMLParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
    }

    @Override
    protected boolean isProductionWithAlternatives(ParserRuleContext currentCtx) {
        return false;
    }

    @Override
    protected Result seekMatch(ParserRuleContext currentCtx, int lookahead, int currentDepth, boolean isEntryPoint) {
        boolean hasMatch;
        boolean skipRule;
        int matchingRulesCount = 0;

        while (currentDepth < lookaheadLimit) {
            hasMatch = true;
            skipRule = false;
            STToken nextToken = this.tokenReader.peek(lookahead);

            if (nextToken.kind == SyntaxKind.INTERPOLATION_START_TOKEN) {
                lookahead += 2;
                nextToken = this.tokenReader.peek(lookahead);
            }

            switch (currentCtx) {
                case EOF:
                    hasMatch = nextToken.kind == SyntaxKind.EOF_TOKEN;
                    break;
                case LT_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.LT_TOKEN;
                    break;
                case GT_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.GT_TOKEN;
                    break;
                case XML_NAME:
                    hasMatch = nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case XML_TEXT:
                    hasMatch = nextToken.kind == SyntaxKind.XML_TEXT;
                    break;
                case XML_QUOTED_STRING:
                    hasMatch = nextToken.kind == SyntaxKind.STRING_LITERAL;
                    break;
                case SLASH:
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_TOKEN;
                    break;
                case ASSIGN_OP:
                    hasMatch = nextToken.kind == SyntaxKind.EQUAL_TOKEN;
                    break;
                case XML_CONTENT:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, XML_CONTENT,
                            isEntryPoint);
                case XML_ATTRIBUTES:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, XML_ATTRIBUTES,
                            isEntryPoint);
                case XML_START_OR_EMPTY_TAG_END:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            XML_START_OR_EMPTY_TAG_END, isEntryPoint);
                default:
                    // Stay at the same place
                    skipRule = true;
                    hasMatch = true;
                    break;
            }

            if (!hasMatch) {
                return fixAndContinue(currentCtx, lookahead, currentDepth, matchingRulesCount, isEntryPoint);
            }

            currentCtx = getNextRule(currentCtx, lookahead + 1);
            if (!skipRule) {
                // Try the next token with the next rule
                currentDepth++;
                matchingRulesCount++;
                lookahead++;
                isEntryPoint = false;
            }
        }

        Result result = new Result(new ArrayDeque<>(), matchingRulesCount, currentCtx);
        result.solution =
                new Solution(Action.KEEP, currentCtx, getExpectedTokenKind(currentCtx), currentCtx.toString());
        return result;
    }

    @Override
    protected ParserRuleContext getNextRule(ParserRuleContext currentCtx, int nextLookahead) {
        switch (currentCtx) {
            case XML_START_OR_EMPTY_TAG:
            case XML_END_TAG:
            case XML_ATTRIBUTES:
                startContext(currentCtx);
            default:
                break;
        }

        switch (currentCtx) {
            case XML_START_OR_EMPTY_TAG:
            case XML_END_TAG:
                return ParserRuleContext.LT_TOKEN;
            case LT_TOKEN:
                ParserRuleContext parentCtx = getParentContext();
                switch (parentCtx) {
                    case XML_START_OR_EMPTY_TAG:
                        return ParserRuleContext.XML_NAME;
                    case XML_END_TAG:
                        return ParserRuleContext.SLASH;
                    default:
                        throw new IllegalStateException(" < cannot exist in: " + parentCtx);
                }
            case GT_TOKEN:
                endContext();
                return ParserRuleContext.XML_CONTENT;
            case XML_NAME:
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case XML_START_OR_EMPTY_TAG:
                        return ParserRuleContext.XML_ATTRIBUTES;
                    case XML_END_TAG:
                        return ParserRuleContext.GT_TOKEN;
                    case XML_ATTRIBUTES:
                        return ParserRuleContext.ASSIGN_OP;
                    default:
                        throw new IllegalStateException("XML name cannot exist in: " + parentCtx);
                }
            case SLASH:
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case XML_ATTRIBUTES:
                        endContext();
                        return ParserRuleContext.GT_TOKEN;
                    case XML_START_OR_EMPTY_TAG:
                        return ParserRuleContext.GT_TOKEN;
                    case XML_END_TAG:
                        return ParserRuleContext.XML_NAME;
                    default:
                        throw new IllegalStateException("slash cannot exist in: " + parentCtx);
                }
            case ASSIGN_OP:
                return ParserRuleContext.XML_QUOTED_STRING;
            case XML_ATTRIBUTE:
                return ParserRuleContext.XML_NAME;
            case XML_QUOTED_STRING:
                return ParserRuleContext.XML_ATTRIBUTES;
            case XML_TEXT:
            case XML_PI:
                return ParserRuleContext.XML_CONTENT;
            default:
                throw new IllegalStateException("cannot find the next rule for: " + currentCtx);
        }
    }

    @Override
    protected SyntaxKind getExpectedTokenKind(ParserRuleContext context) {
        switch (context) {
            case LT_TOKEN:
                return SyntaxKind.LT_TOKEN;
            case GT_TOKEN:
                return SyntaxKind.GT_TOKEN;
            case SLASH:
                return SyntaxKind.SLASH_TOKEN;
            case XML_KEYWORD:
                return SyntaxKind.XML_KEYWORD;
            case XML_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case ASSIGN_OP:
                return SyntaxKind.EQUAL_TOKEN;
            case XML_QUOTED_STRING:
                return SyntaxKind.STRING_LITERAL;
            case XML_START_OR_EMPTY_TAG_END:
            case XML_ATTRIBUTES:
                return SyntaxKind.GT_TOKEN;
            case XML_CONTENT:
            case XML_TEXT:
                return SyntaxKind.BACKTICK_TOKEN;
            default:
                return SyntaxKind.NONE;
        }
    }

    @Override
    public ParserRuleContext findBestPath(ParserRuleContext context) {
        throw new UnsupportedOperationException();
    }
}
