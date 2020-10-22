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

package io.ballerina.toml.internal.parser;

import io.ballerina.toml.internal.parser.tree.STToken;
import io.ballerina.toml.syntax.tree.SyntaxKind;

import java.util.ArrayDeque;

/**
 * <p>
 * Responsible for recovering from a parser error.
 * <p>
 * When an unexpected token is reached, error handler will try inserting/removing a token from the current head, and see
 * how far the parser can successfully progress. After fixing the current head and trying to progress, if it encounters
 * more errors, then it will try to fix those as well. All possible combinations of insertions and deletions will be
 * tried out for such errors. Once all possible paths are discovered, pick the optimal combination that leads to the
 * best recovery. Finally, apply the best solution and continue the parsing.
 * </p>
 * e.g.:
 * If the best combination of fixes was <code>[insert, insert, remove, remove]</code>, then apply only the first
 * fix and continue.
 * <ul>
 * <li>
 * If the fix was a ‘remove’ - then consume the token stream once, and continue from the same rule again.
 * </li>
 * <li>
 * If the fix was an ‘insert’ - then insert the missing node, and continue from the next rule, without consuming the
 * token stream.
 * </li>
 * </ul>
 *
 * @since 2.0.0
 */
public class TomlParserErrorHandler extends AbstractParserErrorHandler {

    private static final ParserRuleContext[] TOP_LEVEL_NODE = {ParserRuleContext.EOF,
            ParserRuleContext.KEY_VALUE_PAIR, ParserRuleContext.TOML_TABLE, ParserRuleContext.TOML_TABLE_ARRAY};

    private static final ParserRuleContext[] VALUE_END = { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.COMMA };

    private static final ParserRuleContext[] VALUE_START =
            { ParserRuleContext.VALUE, ParserRuleContext.TOML_ARRAY };

    private static final ParserRuleContext[] ARRAY_VALUE_START_OR_VALUE_LIST_END =
            { ParserRuleContext.ARRAY_VALUE_LIST_END, ParserRuleContext.ARRAY_VALUE_START };



    public TomlParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
    }

    @Override
    protected boolean isProductionWithAlternatives(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case TOP_LEVEL_NODE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Search for a solution.
     * Terminals are directly matched and Non-terminals which have alternative productions are seekInAlternativesPaths()
     *
     * @param currentCtx   Current context
     * @param lookahead    Position of the next token to consider, relative to the position of the original error.
     * @param currentDepth Amount of distance traveled so far.
     * @return Recovery result
     */
    @Override
    protected Result seekMatch(ParserRuleContext currentCtx, int lookahead, int currentDepth, boolean isEntryPoint) {
        boolean hasMatch;
        boolean skipRule;
        int matchingRulesCount = 0;
        ParserRuleContext[] alternativeRules;

        while (currentDepth < LOOKAHEAD_LIMIT) {
            hasMatch = true;
            skipRule = false;
            STToken nextToken = this.tokenReader.peek(lookahead);

            switch (currentCtx) {
                case EOF:
                    hasMatch = nextToken.kind == SyntaxKind.EOF_TOKEN;
                    break;
                case ASSIGN_OP:
                    hasMatch = nextToken.kind == SyntaxKind.EQUAL_TOKEN;
                    break;
                case OPEN_BRACKET:
                case ARRAY_TABLE_FIRST_START:
                case ARRAY_TABLE_SECOND_START:
                case TABLE_START:
                case ARRAY_VALUE_LIST_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN;
                    break;
                case CLOSE_BRACKET:
                case ARRAY_TABLE_FIRST_END:
                case ARRAY_TABLE_SECOND_END:
                case TABLE_END:
                case ARRAY_VALUE_LIST_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN;
                    break;
                case STRING_START:
                case STRING_END:
                    hasMatch = nextToken.kind == SyntaxKind.DOUBLE_QUOTE_TOKEN;
                    break;
                case COMMA:
                    hasMatch = nextToken.kind == SyntaxKind.COMMA_TOKEN;
                    break;
                case KEY:
                    hasMatch = TomlParser.isKey(nextToken);
                    break;
                case VALUE:
                    hasMatch = isBasicLiteral(nextToken.kind);
                    break;
                case TOP_LEVEL_NODE:
                    alternativeRules = TOP_LEVEL_NODE;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case ARRAY_VALUE_END:
                    alternativeRules = VALUE_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case ARRAY_VALUE_START:
                    alternativeRules = VALUE_START;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case ARRAY_VALUE_START_OR_VALUE_LIST_END:
                    alternativeRules = ARRAY_VALUE_START_OR_VALUE_LIST_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
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

        Result result = new Result(new ArrayDeque<>(), matchingRulesCount);
        result.solution = new Solution(Action.KEEP, currentCtx, SyntaxKind.NONE, currentCtx.toString());
        return result;
    }


    private void startContextIfRequired(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case TOML_TABLE_ARRAY:
            case TOML_TABLE:
            case KEY_VALUE_PAIR:
            case TOML_ARRAY:
            case ARRAY_VALUE_LIST:
                startContext(currentCtx);
                break;
            default:
                break;
        }
    }

    /**
     * Get the next parser rule/context given the current parser context.
     *
     * @param currentCtx    Current parser context
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    @Override
    protected ParserRuleContext getNextRule(ParserRuleContext currentCtx, int nextLookahead) {
        // If this is a production, then push the context to the stack.
        // We can do this within the same switch-case that follows after this one.
        // But doing it separately for the sake of readability/maintainability.
        startContextIfRequired(currentCtx);
        switch (currentCtx) {
            case EOF:
                return ParserRuleContext.EOF;
            case TOML_ARRAY:
            case VALUE:
            case CLOSE_BRACKET:
            case DOUBLE_CLOSE_BRACKET:
                return ParserRuleContext.TOP_LEVEL_NODE;
            case ASSIGN_OP:
                return ParserRuleContext.VALUE;
            case KEY_VALUE_PAIR:
            case OPEN_BRACKET:
            case DOUBLE_OPEN_BRACKET:
                return ParserRuleContext.KEY;
            case TOML_TABLE:
                return ParserRuleContext.OPEN_BRACKET;
            case TOML_TABLE_ARRAY:
                return ParserRuleContext.DOUBLE_OPEN_BRACKET;
            case KEY:
                return getNextRuleForKey();
            case COMMA:
                return ParserRuleContext.CLOSE_BRACKET;
            case ARRAY_VALUE_LIST_START:
                return ParserRuleContext.ARRAY_VALUE_LIST;
            case ARRAY_VALUE_LIST:
                return ParserRuleContext.ARRAY_VALUE_START_OR_VALUE_LIST_END;
            case STRING_START:
                return ParserRuleContext.STRING_LITERAL;
            case STRING_LITERAL:
                return ParserRuleContext.STRING_END;
            default:
                throw new IllegalStateException("Next rule is not handled " + currentCtx); //use next token and try

        }
    }

    private ParserRuleContext getNextRuleForKey() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case TOML_TABLE:
                return ParserRuleContext.CLOSE_BRACKET;
            case KEY_VALUE_PAIR:
            case TOP_LEVEL_NODE: //check
                return ParserRuleContext.ASSIGN_OP;
            case TOML_TABLE_ARRAY:
                return ParserRuleContext.DOUBLE_CLOSE_BRACKET;
            default:
                throw new IllegalStateException("Parent rule for key cannot be " + parentCtx);
        }
    }

    /**
     * Get the expected token kind at the given parser rule context. If the parser rule is a terminal,
     * then the corresponding terminal token kind is returned. If the parser rule is a production,
     * then {@link SyntaxKind#NONE} is returned.
     *
     * @param ctx Parser rule context
     * @return Token kind expected at the given parser rule
     */
    @Override
    protected SyntaxKind getExpectedTokenKind(ParserRuleContext ctx) {
        switch (ctx) {
            case TOML_TABLE:
                return SyntaxKind.TABLE;
            case TOML_TABLE_ARRAY:
                return SyntaxKind.TABLE_ARRAY;
            case KEY_VALUE_PAIR:
                return SyntaxKind.KEY_VALUE;
            case ASSIGN_OP:
                return SyntaxKind.EQUAL_TOKEN;
            case KEY:
                return SyntaxKind.IDENTIFIER_LITERAL;
            case VALUE:
                return SyntaxKind.FALSE_KEYWORD; //Better handling
            case EOF:
                return SyntaxKind.EOF_TOKEN;
            case OPEN_BRACKET:
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case CLOSE_BRACKET:
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case COMMA:
                return SyntaxKind.COMMA_TOKEN;
            case STRING_START:
            case STRING_END:
                return SyntaxKind.DOUBLE_QUOTE_TOKEN;
            case STRING_LITERAL:
                return SyntaxKind.STRING_LITERAL;
            case ARRAY_VALUE_LIST_START:
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case ARRAY_VALUE_LIST_END:
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            default:
                return SyntaxKind.NONE;
        }
    }

    /**
     * Check whether a token kind is a basic literal.
     *
     * @param kind Token kind to check
     * @return <code>true</code> if the given token kind belongs to a basic literal.<code>false</code> otherwise
     */
    private boolean isBasicLiteral(SyntaxKind kind) {
        switch (kind) {
            case DEC_INT:
            case FLOAT:
            case STRING_LITERAL:
            case ML_STRING_LITERAL:
            case BOOLEAN:
            case BASIC_LITERAL:
            case ARRAY:
                return true;
            default:
                return false;
        }
    }
}
