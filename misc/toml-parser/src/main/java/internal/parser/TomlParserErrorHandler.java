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

package internal.parser;

import internal.parser.tree.STToken;
import syntax.tree.SyntaxKind;

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
 * @since 1.2.0
 */
public class TomlParserErrorHandler extends AbstractParserErrorHandler {

//    private static final ParserRuleContext[] TOP_LEVEL_NODE = { ParserRuleContext.EOF, ParserRuleContext.DOC_STRING,
//            ParserRuleContext.ANNOTATIONS, ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_METADATA };

    private static final ParserRuleContext[] TOP_LEVEL_NODE = {ParserRuleContext.EOF,
            ParserRuleContext.KEY_VALUE_PAIR, ParserRuleContext.TOML_TABLE, ParserRuleContext.TOML_TABLE_ARRAY};
            //TODO add more

    public TomlParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
    }

    @Override
    protected boolean isProductionWithAlternatives(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            case TOP_LEVEL_NODE:
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case FUNC_BODY_OR_TYPE_DESC_RHS:
            case VAR_DECL_STMT_RHS:
            case EXPRESSION_RHS:
            case PARAMETER_NAME_RHS:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case AFTER_PARAMETER_TYPE:
            case FIELD_DESCRIPTOR_RHS:
            case RECORD_BODY_START:
            case RECORD_BODY_END:
            case TYPE_DESCRIPTOR:
            case NAMED_OR_POSITIONAL_ARG_RHS:
            case OBJECT_FIELD_RHS:
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
            case OBJECT_MEMBER:
            case OBJECT_TYPE_QUALIFIER:
            case ELSE_BODY:
            case IMPORT_DECL_RHS:
            case IMPORT_SUB_VERSION:
            case VERSION_NUMBER:
            case IMPORT_VERSION_DECL:
            case IMPORT_PREFIX_DECL:
            case MAPPING_FIELD:
            case FIRST_MAPPING_FIELD:
            case SPECIFIC_FIELD_RHS:
            case RESOURCE_DEF:
            case PARAMETER_WITHOUT_ANNOTS:
            case PARAMETER_START:
            case STMT_START_WITH_EXPR_RHS:
            case EXPR_STMT_RHS:
            case RECORD_FIELD_OR_RECORD_END:
            case CONST_DECL_TYPE:
            case CONST_DECL_RHS:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
            case XML_NAMESPACE_PREFIX_DECL:
            case ANNOT_DECL_OPTIONAL_TYPE:
            case ANNOT_DECL_RHS:
            case TABLE_KEYWORD_RHS:
            case ARRAY_LENGTH:
            case TYPEDESC_RHS:
            case ERROR_TYPE_PARAMS:
            case STREAM_TYPE_FIRST_PARAM_RHS:
            case KEY_CONSTRAINTS_RHS:
            case TABLE_TYPE_DESC_RHS:
            case FUNC_BODY:
            case FUNC_OPTIONAL_RETURNS:
            case TERMINAL_EXPRESSION:
            case TABLE_CONSTRUCTOR_OR_QUERY_START:
            case TABLE_CONSTRUCTOR_OR_QUERY_RHS:
            case QUERY_PIPELINE_RHS:
            case ANON_FUNC_BODY:
            case BINDING_PATTERN:
            case LIST_BINDING_PATTERN_MEMBER:
            case LIST_BINDING_PATTERN_MEMBER_END:
            case MAPPING_BINDING_PATTERN_MEMBER:
            case MAPPING_BINDING_PATTERN_END:
            case FIELD_BINDING_PATTERN_END:
            case ARG_BINDING_PATTERN:
            case ARG_BINDING_PATTERN_END:
            case ARG_BINDING_PATTERN_START_IDENT:
            case REMOTE_CALL_OR_ASYNC_SEND_RHS:
            case REMOTE_CALL_OR_ASYNC_SEND_END:
            case RECEIVE_FIELD_END:
            case RECEIVE_WORKERS:
            case WAIT_FIELD_NAME:
            case WAIT_FIELD_NAME_RHS:
            case WAIT_FIELD_END:
            case WAIT_FUTURE_EXPR_END:
            case MAPPING_FIELD_END:
            case ENUM_MEMBER_START:
            case ENUM_MEMBER_RHS:
            case STMT_START_BRACKETED_LIST_MEMBER:
            case STMT_START_BRACKETED_LIST_RHS:
            case ENUM_MEMBER_END:
            case BINDING_PATTERN_OR_EXPR_RHS:
            case BRACKETED_LIST_RHS:
            case BRACKETED_LIST_MEMBER:
            case BRACKETED_LIST_MEMBER_END:
            case AMBIGUOUS_STMT:
            case TYPED_BINDING_PATTERN_TYPE_RHS:
            case TYPE_DESC_IN_TUPLE_RHS:
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
            case FUNC_TYPE_DESC_RHS_OR_ANON_FUNC_BODY:
            case OPTIONAL_MATCH_GUARD:
            case MATCH_PATTERN_RHS:
            case MATCH_PATTERN_START:
            case LIST_MATCH_PATTERNS_START:
            case LIST_MATCH_PATTERN_MEMBER:
            case LIST_MATCH_PATTERN_MEMBER_RHS:
            case FIELD_MATCH_PATTERNS_START:
            case FIELD_MATCH_PATTERN_MEMBER:
            case FIELD_MATCH_PATTERN_MEMBER_RHS:
            case FUNC_MATCH_PATTERN_OR_CONST_PATTERN:
            case FUNCTIONAL_MATCH_PATTERN_START:
            case ARG_LIST_MATCH_PATTERN_START:
            case ARG_MATCH_PATTERN:
            case ARG_MATCH_PATTERN_RHS:
            case NAMED_ARG_MATCH_PATTERN_RHS:
            case EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS:
            case LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER:
            case TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER:
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
                case VALUE:
                    hasMatch = isBasicLiteral(nextToken.kind);
                    break;
                case OPEN_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN;
                    break;
                case DOUBLE_OPEN_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.DOUBLE_OPEN_BRACKET_TOKEN;
                    break;
                case CLOSE_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN;
                    break;
                case DOUBLE_CLOSE_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.DOUBLE_CLOSE_BRACKET_TOKEN;
                    break;
                case KEY:
                    hasMatch = TomlParser.isKey(nextToken);
                    break;
                case TOP_LEVEL_NODE:
                    ParserRuleContext[] alternativeRules;
                    alternativeRules = TOP_LEVEL_NODE;
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

        switch (currentCtx) {
            case EOF:
                return ParserRuleContext.EOF;
            case COMP_UNIT:
                return ParserRuleContext.TOP_LEVEL_NODE;
            case ASSIGN_OP:
                return ParserRuleContext.VALUE;
            case VALUE:
            case CLOSE_BRACKET:
            case DOUBLE_CLOSE_BRACKET:
                return ParserRuleContext.TOP_LEVEL_NODE;
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

//    private ParserRuleContext getNextRuleForTopLevelNode () {
//
//    }

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
                return SyntaxKind.UNQUOTED_KEY_TOKEN;
            case VALUE:
                return SyntaxKind.BASIC_LITERAL;
            case EOF:
                return SyntaxKind.EOF_TOKEN;
            case OPEN_BRACKET:
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case DOUBLE_OPEN_BRACKET:
                return SyntaxKind.DOUBLE_OPEN_BRACKET_TOKEN;
            case CLOSE_BRACKET:
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case DOUBLE_CLOSE_BRACKET:
                return SyntaxKind.DOUBLE_CLOSE_BRACKET_TOKEN;
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
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case INF:
            case NAN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token refers to a unary operator.
     *
     * @param token Token to check
     * @return <code>true</code> if the given token refers to a unary operator. <code>false</code> otherwise
     */
    private boolean isUnaryOperator(STToken token) {
        switch (token.kind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return true;
            default:
                return false;
        }
    }
}
