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

import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

import java.util.ArrayDeque;

/**
 * Error handler to recover from regular expression parsing errors.
 * 
 * @since 2201.3.0
 */
public class RegExpParserErrorHandler extends AbstractParserErrorHandler {

    private static final ParserRuleContext[] REG_EXP_TERM =
            { ParserRuleContext.REG_EXP_ASSERTION, ParserRuleContext.REG_EXP_CHAR_ESCAPE,
                    ParserRuleContext.REG_EXP_CHAR_CLASS, ParserRuleContext.REG_EXP_CAPTURING_GROUP };

    private static final ParserRuleContext[] REG_EXP_ASSERTION_RHS = { ParserRuleContext.REG_EXP_TERM,
            ParserRuleContext.EOF, ParserRuleContext.PIPE};

    private static final ParserRuleContext[] REG_EXP_CHAR_ESCAPE_RHS = { ParserRuleContext.REG_EXP_TERM,
            ParserRuleContext.REG_EXP_QUANTIFIER, ParserRuleContext.EOF, ParserRuleContext.PIPE};

    private static final ParserRuleContext[] REG_EXP_CHAR_CLASS_START_RHS =
            { ParserRuleContext.REG_EXP_CHAR_CLASS_END, ParserRuleContext.REG_EXP_CHAR_SET };

    private static final ParserRuleContext[] REG_EXP_CHAR_CLASS_END_RHS = { ParserRuleContext.REG_EXP_TERM,
            ParserRuleContext.REG_EXP_QUANTIFIER, ParserRuleContext.EOF, ParserRuleContext.PIPE };

    private static final ParserRuleContext[] REG_EXP_CAPTURING_GROUP_START_RHS =
            { ParserRuleContext.REG_EXP_CAPTURING_GROUP_END, ParserRuleContext.REG_EXP_FLAG_EXPR };

    private static final ParserRuleContext[] REG_EXP_CAPTURING_GROUP_END_RHS = { ParserRuleContext.REG_EXP_TERM,
            ParserRuleContext.REG_EXP_QUANTIFIER, ParserRuleContext.EOF, ParserRuleContext.PIPE };

    private static final ParserRuleContext[] REG_EXP_FLAG_EXPR_START_RHS = { ParserRuleContext.REG_EXP_FLAG_EXPR_END,
            ParserRuleContext.REG_EXP_FLAG_ON_OFF };

    private static final ParserRuleContext[] REG_EXP_FLAG_EXPR_END_RHS = { ParserRuleContext.REG_EXP_TERM,
            ParserRuleContext.EOF };

    public RegExpParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
    }

    @Override
    protected boolean hasAlternativePaths(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case REG_EXP_TERM:
            case REG_EXP_ASSERTION_RHS:
            case REG_EXP_CHAR_ESCAPE_RHS:
            case REG_EXP_CHAR_CLASS_START_RHS:
            case REG_EXP_CHAR_CLASS_END_RHS:
            case REG_EXP_CAPTURING_GROUP_START_RHS:
            case REG_EXP_CAPTURING_GROUP_END_RHS:
            case REG_EXP_FLAG_EXPR_START_RHS:
            case REG_EXP_FLAG_EXPR_END_RHS:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Result seekMatch(ParserRuleContext currentCtx, int lookahead, int currentDepth, boolean isEntryPoint) {
        boolean hasMatch;
        boolean skipRule;
        int matchingRulesCount = 0;

        while (currentDepth < LOOKAHEAD_LIMIT) {
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
                case REG_EXP_CHAR_CLASS_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN ||
                            nextToken.kind == SyntaxKind.NEGATED_CHAR_CLASS_START_TOKEN;
                    break;
                case REG_EXP_CHAR_SET:
                    hasMatch = nextToken.kind == SyntaxKind.RE_CHAR_SET;
                    break;
                case REG_EXP_CHAR_CLASS_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN;
                    break;
                case REG_EXP_CAPTURING_GROUP_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN;
                    break;
                case REG_EXP_CAPTURING_GROUP_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN;
                    break;
                case REG_EXP_FLAG_EXPR_START:
                    hasMatch = nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN;
                    break;
                case REG_EXP_FLAG_ON_OFF:
                    hasMatch = nextToken.kind == SyntaxKind.RE_FLAGS_ON_OFF;
                    break;
                case REG_EXP_FLAG_EXPR_END:
                    hasMatch = nextToken.kind == SyntaxKind.COLON_TOKEN;
                    break;
                case REG_EXP_QUANTIFIER:
                    hasMatch = nextToken.kind == SyntaxKind.RE_QUANTIFIER;
                    break;
                default:
                    if (hasAlternativePaths(currentCtx)) {
                        return seekMatchInAlternativePaths(currentCtx, lookahead, currentDepth, matchingRulesCount,
                                isEntryPoint);
                    }
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

        Result result = new Result(new ArrayDeque<>(), matchingRulesCount);
        result.solution =
                new Solution(Action.KEEP, currentCtx, getExpectedTokenKind(currentCtx), currentCtx.toString());
        return result;
    }

    private Result seekMatchInAlternativePaths(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                               int matchingRulesCount, boolean isEntryPoint) {
        ParserRuleContext[] alternativeRules;
        switch (currentCtx) {
            case REG_EXP_TERM:
                alternativeRules = REG_EXP_TERM;
                break;
            case REG_EXP_ASSERTION_RHS:
                alternativeRules = REG_EXP_ASSERTION_RHS;
                break;
            case REG_EXP_CHAR_ESCAPE_RHS:
                alternativeRules = REG_EXP_CHAR_ESCAPE_RHS;
                break;
            case REG_EXP_CHAR_CLASS_START_RHS:
                alternativeRules = REG_EXP_CHAR_CLASS_START_RHS;
                break;
            case REG_EXP_CHAR_CLASS_END_RHS:
                alternativeRules = REG_EXP_CHAR_CLASS_END_RHS;
                break;
            case REG_EXP_CAPTURING_GROUP_START_RHS:
                alternativeRules = REG_EXP_CAPTURING_GROUP_START_RHS;
                break;
            case REG_EXP_CAPTURING_GROUP_END_RHS:
                alternativeRules = REG_EXP_CAPTURING_GROUP_END_RHS;
                break;
            case REG_EXP_FLAG_EXPR_START_RHS:
                alternativeRules = REG_EXP_FLAG_EXPR_START_RHS;
                break;
            case REG_EXP_FLAG_EXPR_END_RHS:
                alternativeRules = REG_EXP_FLAG_EXPR_END_RHS;
                break;
            default:
                throw new IllegalStateException("seekMatchInExprRelatedAlternativePaths found: " + currentCtx);
        }

        return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules, isEntryPoint);
    }

    @Override
    protected ParserRuleContext getNextRule(ParserRuleContext currentCtx, int nextLookahead) {
        switch (currentCtx) {
            case REG_EXP_CHAR_CLASS:
            case REG_EXP_CAPTURING_GROUP:
            case REG_EXP_FLAG_EXPR:
            case REG_EXP_ASSERTION:
            case REG_EXP_CHAR_ESCAPE:
                startContext(currentCtx);
                break;
            default:
                break;
        }

        switch (currentCtx) {
            case REG_EXP_CHAR_CLASS_END:
                endContext();
                return ParserRuleContext.REG_EXP_CHAR_CLASS_END_RHS;
            case REG_EXP_CAPTURING_GROUP_END:
                endContext();
                return ParserRuleContext.REG_EXP_CAPTURING_GROUP_END_RHS;
            case REG_EXP_ASSERTION:
                endContext();
                return ParserRuleContext.REG_EXP_ASSERTION_RHS;
            case REG_EXP_CHAR_ESCAPE:
                endContext();
                return ParserRuleContext.REG_EXP_CHAR_ESCAPE_RHS;
            case REG_EXP_FLAG_EXPR_END:
                endContext();
                return ParserRuleContext.REG_EXP_FLAG_EXPR_END_RHS;
            case REG_EXP_CHAR_CLASS:
                return ParserRuleContext.REG_EXP_CHAR_CLASS_START;
            case REG_EXP_CHAR_CLASS_START:
                return ParserRuleContext.REG_EXP_CHAR_CLASS_START_RHS;
            case REG_EXP_CHAR_SET:
                return ParserRuleContext.REG_EXP_CHAR_CLASS_END;
            case REG_EXP_CAPTURING_GROUP:
                return ParserRuleContext.REG_EXP_CAPTURING_GROUP_START;
            case REG_EXP_CAPTURING_GROUP_START:
                return ParserRuleContext.REG_EXP_CAPTURING_GROUP_START_RHS;
            case REG_EXP_FLAG_EXPR:
                return ParserRuleContext.REG_EXP_FLAG_EXPR_START;
            case REG_EXP_FLAG_EXPR_START:
                return ParserRuleContext.REG_EXP_FLAG_EXPR_START_RHS;
            case REG_EXP_FLAG_ON_OFF:
                return ParserRuleContext.REG_EXP_FLAG_EXPR_END;
            case REG_EXP_QUANTIFIER:
                return ParserRuleContext.REG_EXP_QUANTIFIER;
            case REG_EXP_TEXT:
                return ParserRuleContext.REG_EXP_TERM;
            case EOF:
                return ParserRuleContext.EOF;
            default:
                throw new IllegalStateException("cannot find the next rule for: " + currentCtx);
        }
    }

    @Override
    protected Solution getInsertSolution(ParserRuleContext ctx) {
        SyntaxKind expectedTokenKind = getExpectedTokenKind(ctx);
        return new Solution(Action.INSERT, ctx, expectedTokenKind, ctx.toString());
    }

    @Override
    protected SyntaxKind getExpectedTokenKind(ParserRuleContext context) {
        switch (context) {
            case REG_EXP_ASSERTION:
                return SyntaxKind.RE_ASSERTION;
            case REG_EXP_CHAR_ESCAPE:
                return SyntaxKind.RE_CHAR_ESCAPE;
            case REG_EXP_CHAR_CLASS:
            case REG_EXP_CHAR_CLASS_START:
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case REG_EXP_CHAR_CLASS_END:
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case REG_EXP_CHAR_SET:
                return SyntaxKind.RE_CHAR_SET;
            case REG_EXP_CAPTURING_GROUP:
            case REG_EXP_CAPTURING_GROUP_START:
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case REG_EXP_CAPTURING_GROUP_END:
                return SyntaxKind.CLOSE_PAREN_TOKEN;
            case REG_EXP_FLAG_EXPR:
            case REG_EXP_FLAG_EXPR_START:
                return SyntaxKind.QUESTION_MARK_TOKEN;
            case REG_EXP_FLAG_ON_OFF:
                return SyntaxKind.RE_FLAGS_ON_OFF;
            case REG_EXP_FLAG_EXPR_END:
                return SyntaxKind.COLON_TOKEN;
            case REG_EXP_QUANTIFIER:
                return SyntaxKind.RE_QUANTIFIER;
            case REG_EXP_TEXT:
                return SyntaxKind.BACKTICK_TOKEN;
            case EOF:
                return SyntaxKind.EOF_TOKEN;
            default:
                return SyntaxKind.NONE;
        }
    }
}
