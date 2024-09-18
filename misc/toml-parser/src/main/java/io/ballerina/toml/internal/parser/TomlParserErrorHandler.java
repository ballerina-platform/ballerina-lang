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

    private static final ParserRuleContext[] TOP_LEVEL_NODE = { ParserRuleContext.EOF,
            ParserRuleContext.NEWLINE,
            ParserRuleContext.KEY_VALUE_PAIR,
            ParserRuleContext.TOML_TABLE,
            ParserRuleContext.TOML_TABLE_ARRAY
    };

    private static final ParserRuleContext[] ARRAY_VALUE_END = 
            { ParserRuleContext.COMMA, ParserRuleContext.ARRAY_VALUE_LIST_END };

    private static final ParserRuleContext[] INLINE_TABLE_ENTRY_END = 
            { ParserRuleContext.COMMA, ParserRuleContext.INLINE_TABLE_END };

    private static final ParserRuleContext[] ARRAY_VALUE_START_OR_VALUE_LIST_END =
            { ParserRuleContext.ARRAY_VALUE_LIST_END, ParserRuleContext.ARRAY_VALUE_START };

    private static final ParserRuleContext[] INLINE_TABLE_START_OR_TABLE_ENTRY_LIST_END =
            { ParserRuleContext.INLINE_TABLE_END, ParserRuleContext.INLINE_TABLE_ENTRY_START };

    private static final ParserRuleContext[] NUMERICAL_LITERAL = { ParserRuleContext.DECIMAL_INTEGER_LITERAL,
            ParserRuleContext.DECIMAL_FLOATING_POINT_LITERAL, ParserRuleContext.HEX_INTEGER_LITERAL,
            ParserRuleContext.OCTAL_INTEGER_LITERAL, ParserRuleContext.BINARY_INTEGER_LITERAL };

    private static final ParserRuleContext[] VALUE = { ParserRuleContext.STRING_START,
            ParserRuleContext.LITERAL_STRING_START, ParserRuleContext.MULTILINE_STRING_START,
            ParserRuleContext.MULTILINE_LITERAL_STRING_START, ParserRuleContext.SIGN_TOKEN,
            ParserRuleContext.BOOLEAN_LITERAL, ParserRuleContext.NUMERICAL_LITERAL,
            ParserRuleContext.ARRAY_VALUE_LIST_START, ParserRuleContext.INLINE_TABLE_START
    };

    private static final ParserRuleContext[] ARRAY_VALUE_START = VALUE;

    private static final ParserRuleContext[] KEY_START = { ParserRuleContext.IDENTIFIER_LITERAL,
            ParserRuleContext.NUMERICAL_LITERAL, ParserRuleContext.BOOLEAN_LITERAL, ParserRuleContext.STRING_START,
            ParserRuleContext.LITERAL_STRING_START };

    private static final ParserRuleContext[] KEY_END = { ParserRuleContext.DOT, ParserRuleContext.KEY_LIST_END };

    private static final ParserRuleContext[] KEY_LIST_END = {
            ParserRuleContext.ASSIGN_OP, ParserRuleContext.TABLE_END, ParserRuleContext.ARRAY_TABLE_FIRST_END };

    private static final ParserRuleContext[] STRING_CONTENT = { ParserRuleContext.STRING_END,
            ParserRuleContext.STRING_BODY };

    private static final ParserRuleContext[] MULTILINE_STRING_CONTENT = { ParserRuleContext.MULTILINE_STRING_END,
            ParserRuleContext.MULTILINE_STRING_BODY };

    private static final ParserRuleContext[] LITERAL_STRING_CONTENT = { ParserRuleContext.LITERAL_STRING_END,
            ParserRuleContext.LITERAL_STRING_BODY };

    private static final ParserRuleContext[] MULTILINE_LITERAL_STRING_CONTENT =
            { ParserRuleContext.MULTILINE_LITERAL_STRING_END,
            ParserRuleContext.MULTILINE_LITERAL_STRING_BODY };

    public TomlParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
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
                case ARRAY_TABLE_FIRST_START:
                case ARRAY_TABLE_SECOND_START:
                case TABLE_START:
                case ARRAY_VALUE_LIST_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN;
                    break;
                case ARRAY_TABLE_FIRST_END:
                case ARRAY_TABLE_SECOND_END:
                case TABLE_END:
                case ARRAY_VALUE_LIST_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN;
                    break;
                case INLINE_TABLE_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACE_TOKEN;
                    break;
                case STRING_START:
                case STRING_END:
                    hasMatch = nextToken.kind == SyntaxKind.DOUBLE_QUOTE_TOKEN;
                    break;
                case MULTILINE_STRING_START:
                case MULTILINE_STRING_END:
                    hasMatch = nextToken.kind == SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN;
                    break;
                case LITERAL_STRING_START:
                case LITERAL_STRING_END:
                    hasMatch = nextToken.kind == SyntaxKind.SINGLE_QUOTE_TOKEN;
                    break;
                case MULTILINE_LITERAL_STRING_START:
                case MULTILINE_LITERAL_STRING_END:
                    hasMatch = nextToken.kind == SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN;
                    break;
                case INLINE_TABLE_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACE_TOKEN;
                    break;
                case COMMA:
                    hasMatch = nextToken.kind == SyntaxKind.COMMA_TOKEN;
                    break;
                case DOT:
                    hasMatch = nextToken.kind == SyntaxKind.DOT_TOKEN;
                    break;
                case DECIMAL_INTEGER_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_INT_TOKEN;
                    break;
                case DECIMAL_FLOATING_POINT_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_FLOAT_TOKEN;
                    break;
                case HEX_INTEGER_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN;
                    break;
                case OCTAL_INTEGER_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.OCTAL_INTEGER_LITERAL_TOKEN;
                    break;
                case BINARY_INTEGER_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.BINARY_INTEGER_LITERAL_TOKEN;
                    break;
                case BOOLEAN_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.TRUE_KEYWORD || nextToken.kind == SyntaxKind.FALSE_KEYWORD;
                    break;
                case STRING_BODY:
                case LITERAL_STRING_BODY:
                case MULTILINE_STRING_BODY:
                case MULTILINE_LITERAL_STRING_BODY:
                    hasMatch = nextToken.kind == SyntaxKind.STRING_LITERAL_TOKEN ||
                            nextToken.kind == SyntaxKind.ML_STRING_LITERAL ||
                            nextToken.kind == SyntaxKind.IDENTIFIER_LITERAL;
                    break;
                case IDENTIFIER_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.IDENTIFIER_LITERAL;
                    break;
                case NEWLINE:
                    hasMatch = nextToken.kind == SyntaxKind.NEWLINE;
                    break;
                case SIGN_TOKEN:
                    hasMatch = nextToken.kind == SyntaxKind.PLUS_TOKEN || nextToken.kind == SyntaxKind.MINUS_TOKEN;
                    break;
                case STRING_CONTENT:
                    alternativeRules = STRING_CONTENT;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case MULTILINE_STRING_CONTENT:
                    alternativeRules = MULTILINE_STRING_CONTENT;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case LITERAL_STRING_CONTENT:
                    alternativeRules = LITERAL_STRING_CONTENT;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case MULTILINE_LITERAL_STRING_CONTENT:
                    alternativeRules = MULTILINE_LITERAL_STRING_CONTENT;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case NUMERICAL_LITERAL:
                    alternativeRules = NUMERICAL_LITERAL;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case VALUE:
                    alternativeRules = VALUE;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case TOP_LEVEL_NODE:
                    alternativeRules = TOP_LEVEL_NODE;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case ARRAY_VALUE_END:
                    alternativeRules = ARRAY_VALUE_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case ARRAY_VALUE_START:
                    alternativeRules = ARRAY_VALUE_START;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case ARRAY_VALUE_START_OR_VALUE_LIST_END:
                    alternativeRules = ARRAY_VALUE_START_OR_VALUE_LIST_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case INLINE_TABLE_ENTRY_END:
                    alternativeRules = INLINE_TABLE_ENTRY_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case INLINE_TABLE_START_OR_TABLE_ENTRY_LIST_END:
                    alternativeRules = INLINE_TABLE_START_OR_TABLE_ENTRY_LIST_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case KEY_START:
                    alternativeRules = KEY_START;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case KEY_END:
                    alternativeRules = KEY_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                case KEY_LIST_END:
                    alternativeRules = KEY_LIST_END;
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternativeRules,
                            isEntryPoint);
                default:
                    // Stay at the same place
                    skipRule = true;
                    hasMatch = true;
                    break;
            }

            if (!hasMatch) {
                //If terminal node is not matched.
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
            case ARRAY_VALUE_LIST:
            case INLINE_TABLE_LIST:
            case KEY_LIST:
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
        ParserRuleContext parentCtx = getParentContext();
        switch (currentCtx) {
            case EOF:
                return ParserRuleContext.EOF;
            case ARRAY_TABLE_SECOND_END:
            case TABLE_END:
            case ARRAY_VALUE_LIST_END:
            case INLINE_TABLE_END:
                endContext();
                return ParserRuleContext.NEWLINE;
            case ASSIGN_OP:
                endContext();
                return ParserRuleContext.VALUE;
            case NEWLINE:
                return ParserRuleContext.TOP_LEVEL_NODE;
            case INLINE_TABLE_ENTRY_START:
                return ParserRuleContext.KEY_VALUE_PAIR;
            case KEY_VALUE_PAIR:
            case TABLE_START:
            case ARRAY_TABLE_SECOND_START:
                return ParserRuleContext.KEY_LIST;
            case ARRAY_TABLE_FIRST_START:
                return ParserRuleContext.ARRAY_TABLE_SECOND_START;
            case ARRAY_TABLE_FIRST_END:
                return ParserRuleContext.ARRAY_TABLE_SECOND_END;
            case TOML_TABLE:
                return ParserRuleContext.TABLE_START;
            case TOML_TABLE_ARRAY:
                return ParserRuleContext.ARRAY_TABLE_FIRST_START;
            case COMMA:
                return ParserRuleContext.VALUE;
            case SIGN_TOKEN:
                return ParserRuleContext.NUMERICAL_LITERAL;
            case DOT:
            case KEY_LIST:
                return ParserRuleContext.KEY_START;
            case ARRAY_VALUE_LIST_START:
                return ParserRuleContext.ARRAY_VALUE_LIST;
            case ARRAY_VALUE_LIST:
                return ParserRuleContext.ARRAY_VALUE_START_OR_VALUE_LIST_END;
            case INLINE_TABLE_START:
                return ParserRuleContext.INLINE_TABLE_LIST;
            case INLINE_TABLE_LIST:
                return ParserRuleContext.INLINE_TABLE_START_OR_TABLE_ENTRY_LIST_END;
            case STRING_START:
                return ParserRuleContext.STRING_CONTENT;
            case MULTILINE_STRING_START:
                return ParserRuleContext.MULTILINE_STRING_CONTENT;
            case LITERAL_STRING_START:
                return ParserRuleContext.LITERAL_STRING_CONTENT;
            case MULTILINE_LITERAL_STRING_START:
                return ParserRuleContext.MULTILINE_LITERAL_STRING_CONTENT;
            case STRING_BODY:
                return ParserRuleContext.STRING_END;
            case MULTILINE_STRING_BODY:
                return ParserRuleContext.MULTILINE_STRING_END;
            case LITERAL_STRING_BODY:
                return ParserRuleContext.LITERAL_STRING_END;
            case MULTILINE_LITERAL_STRING_BODY:
                return ParserRuleContext.MULTILINE_LITERAL_STRING_END;
            case STRING_END:
            case MULTILINE_STRING_END:
            case LITERAL_STRING_END:
            case MULTILINE_LITERAL_STRING_END:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case OCTAL_INTEGER_LITERAL:
            case BINARY_INTEGER_LITERAL:
            case BOOLEAN_LITERAL:
                if (parentCtx == ParserRuleContext.ARRAY_VALUE_LIST) {
                    return ParserRuleContext.ARRAY_VALUE_END;
                } else if (parentCtx == ParserRuleContext.INLINE_TABLE_LIST) {
                    return ParserRuleContext.INLINE_TABLE_ENTRY_END;
                } else if (parentCtx == ParserRuleContext.KEY_LIST) {
                    return ParserRuleContext.KEY_END;
                } else {
                    return ParserRuleContext.NEWLINE;
                }
            case IDENTIFIER_LITERAL:
                return ParserRuleContext.KEY_END;
            default:
                throw new IllegalStateException("Next rule is not handled " + currentCtx); //use next token and try

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
        return switch (ctx) {
            case TOML_TABLE -> SyntaxKind.TABLE;
            case TOML_TABLE_ARRAY -> SyntaxKind.TABLE_ARRAY;
            case KEY_VALUE_PAIR -> SyntaxKind.KEY_VALUE;
            case ASSIGN_OP -> SyntaxKind.EQUAL_TOKEN;
            case IDENTIFIER_LITERAL -> SyntaxKind.IDENTIFIER_LITERAL;
            case EOF -> SyntaxKind.EOF_TOKEN;
            case COMMA -> SyntaxKind.COMMA_TOKEN;
            case STRING_START,
                 STRING_END -> SyntaxKind.DOUBLE_QUOTE_TOKEN;
            case LITERAL_STRING_START,
                 LITERAL_STRING_END -> SyntaxKind.SINGLE_QUOTE_TOKEN;
            case MULTILINE_STRING_START,
                 MULTILINE_STRING_END -> SyntaxKind.TRIPLE_DOUBLE_QUOTE_TOKEN;
            case MULTILINE_LITERAL_STRING_START,
                 MULTILINE_LITERAL_STRING_END -> SyntaxKind.TRIPLE_SINGLE_QUOTE_TOKEN;
            case STRING_BODY,
                 LITERAL_STRING_BODY,
                 MULTILINE_STRING_BODY,
                 MULTILINE_LITERAL_STRING_BODY -> SyntaxKind.IDENTIFIER_LITERAL;
            case INLINE_TABLE_START -> SyntaxKind.OPEN_BRACE_TOKEN;
            case ARRAY_VALUE_LIST_START,
                 TABLE_START -> SyntaxKind.OPEN_BRACKET_TOKEN;
            case ARRAY_VALUE_LIST_END,
                 TABLE_END -> SyntaxKind.CLOSE_BRACKET_TOKEN;
            case INLINE_TABLE_END -> SyntaxKind.CLOSE_BRACE_TOKEN;
            case DECIMAL_INTEGER_LITERAL -> SyntaxKind.DECIMAL_INT_TOKEN;
            case HEX_INTEGER_LITERAL -> SyntaxKind.HEX_INTEGER_LITERAL_TOKEN;
            case OCTAL_INTEGER_LITERAL -> SyntaxKind.OCTAL_INTEGER_LITERAL_TOKEN;
            case BINARY_INTEGER_LITERAL -> SyntaxKind.BINARY_INTEGER_LITERAL_TOKEN;
            case DECIMAL_FLOATING_POINT_LITERAL -> SyntaxKind.DECIMAL_FLOAT_TOKEN;
            case BOOLEAN_LITERAL -> SyntaxKind.FALSE_KEYWORD;
            case NEWLINE -> SyntaxKind.NEWLINE;
            case DOT -> SyntaxKind.DOT_TOKEN;
            case ARRAY_TABLE_FIRST_END,
                 ARRAY_TABLE_SECOND_END -> SyntaxKind.CLOSE_BRACKET_TOKEN;
            case ARRAY_TABLE_FIRST_START,
                 ARRAY_TABLE_SECOND_START -> SyntaxKind.OPEN_BRACKET_TOKEN;
            case SIGN_TOKEN -> SyntaxKind.PLUS_TOKEN;
            default -> SyntaxKind.NONE;
        };
    }
}
