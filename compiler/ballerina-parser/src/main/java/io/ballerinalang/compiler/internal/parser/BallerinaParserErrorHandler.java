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
 * <p>
 * Responsible for recovering from a parser error.
 *
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
public class BallerinaParserErrorHandler extends AbstractParserErrorHandler {

    /**
     * FUNC_DEF_OR_FUNC_TYPE --> When a func-def and func-type-desc are possible.
     * e.g: start of a module level construct that starts with 'function' keyword.
     */
    private static final ParserRuleContext[] FUNC_TYPE_OR_DEF_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.FUNC_TYPE_OR_DEF_SIGNATURE_RHS };

    private static final ParserRuleContext[] FUNC_TYPE_OR_DEF_SIGNATURE_RHS =
            { ParserRuleContext.FUNC_BODY, ParserRuleContext.TYPEDESC_RHS };

    /**
     * FUNC_DEF --> When only function definitions are possible. eg: resource function.
     */
    private static final ParserRuleContext[] FUNC_DEF_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.FUNC_BODY };

    private static final ParserRuleContext[] FUNC_BODY =
            { ParserRuleContext.FUNC_BODY_BLOCK, ParserRuleContext.EXTERNAL_FUNC_BODY };

    /**
     * ANNON_FUNC--> When a anonymous function is possible.
     */
    private static final ParserRuleContext[] ANNON_FUNC_OPTIONAL_RETURNS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.ANNON_FUNC_BODY };

    private static final ParserRuleContext[] ANNON_FUNC_BODY = { ParserRuleContext.FUNC_BODY_BLOCK };

    private static final ParserRuleContext[] WORKER_NAME_RHS =
            { ParserRuleContext.RETURNS_KEYWORD, ParserRuleContext.BLOCK_STMT };

    // We add named-worker-decl also as a statement. This is because we let having a named-worker
    // in all places a statement can be added during parsing, but then validates it based on the
    // context after the parsing the node is complete. This is to provide better error messages.
    private static final ParserRuleContext[] STATEMENTS = { ParserRuleContext.CLOSE_BRACE,
            ParserRuleContext.ASSIGNMENT_STMT, ParserRuleContext.VAR_DECL_STMT, ParserRuleContext.IF_BLOCK,
            ParserRuleContext.WHILE_BLOCK, ParserRuleContext.CALL_STMT, ParserRuleContext.PANIC_STMT,
            ParserRuleContext.CONTINUE_STATEMENT, ParserRuleContext.BREAK_STATEMENT, ParserRuleContext.RETURN_STMT,
            ParserRuleContext.COMPOUND_ASSIGNMENT_STMT, ParserRuleContext.LOCAL_TYPE_DEFINITION_STMT,
            ParserRuleContext.EXPRESSION_STATEMENT, ParserRuleContext.LOCK_STMT, ParserRuleContext.BLOCK_STMT,
            ParserRuleContext.NAMED_WORKER_DECL, ParserRuleContext.FORK_STMT, ParserRuleContext.FOREACH_STMT };

    private static final ParserRuleContext[] VAR_DECL_RHS =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] PARAMETER_RHS = { ParserRuleContext.COMMA, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] TOP_LEVEL_NODE = { ParserRuleContext.DOC_STRING,
            ParserRuleContext.ANNOTATIONS, ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE,
            ParserRuleContext.MODULE_TYPE_DEFINITION, ParserRuleContext.IMPORT_DECL, ParserRuleContext.LISTENER_DECL,
            ParserRuleContext.CONSTANT_DECL, ParserRuleContext.VAR_DECL_STMT, ParserRuleContext.SERVICE_DECL,
            ParserRuleContext.ANNOTATION_DECL, ParserRuleContext.XML_NAMESPACE_DECLARATION, ParserRuleContext.EOF };

    private static final ParserRuleContext[] TOP_LEVEL_NODE_WITHOUT_METADATA = new ParserRuleContext[] {
            ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE,
            ParserRuleContext.MODULE_TYPE_DEFINITION, ParserRuleContext.IMPORT_DECL, ParserRuleContext.SERVICE_DECL,
            ParserRuleContext.LISTENER_DECL, ParserRuleContext.CONSTANT_DECL, ParserRuleContext.VAR_DECL_STMT,
            ParserRuleContext.ANNOTATION_DECL, ParserRuleContext.XML_NAMESPACE_DECLARATION, ParserRuleContext.EOF };

    private static final ParserRuleContext[] TOP_LEVEL_NODE_WITHOUT_MODIFIER =
            { ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE, ParserRuleContext.MODULE_TYPE_DEFINITION,
                    ParserRuleContext.IMPORT_DECL, ParserRuleContext.SERVICE_DECL, ParserRuleContext.LISTENER_DECL,
                    ParserRuleContext.CONSTANT_DECL, ParserRuleContext.ANNOTATION_DECL, ParserRuleContext.VAR_DECL_STMT,
                    ParserRuleContext.XML_NAMESPACE_DECLARATION, ParserRuleContext.EOF };

    private static final ParserRuleContext[] TYPE_OR_VAR_NAME =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN };

    private static final ParserRuleContext[] ASSIGNMENT_OR_VAR_DECL_SECOND_TOKEN =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] FIELD_DESCRIPTOR_RHS =
            { ParserRuleContext.SEMICOLON, ParserRuleContext.QUESTION_MARK, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] FIELD_OR_REST_DESCIPTOR_RHS =
            { ParserRuleContext.ELLIPSIS, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] RECORD_BODY_START =
            { ParserRuleContext.CLOSED_RECORD_BODY_START, ParserRuleContext.OPEN_BRACE };

    private static final ParserRuleContext[] RECORD_BODY_END =
            { ParserRuleContext.CLOSED_RECORD_BODY_END, ParserRuleContext.CLOSE_BRACE };

    // Give object the higher priority over records, since record body is a subset of object body.
    // Array, optional and union type descriptors are not added to the list since they are left recursive.
    private static final ParserRuleContext[] TYPE_DESCRIPTORS = { ParserRuleContext.SIMPLE_TYPE_DESCRIPTOR,
            ParserRuleContext.OBJECT_TYPE_DESCRIPTOR, ParserRuleContext.RECORD_TYPE_DESCRIPTOR,
            ParserRuleContext.NIL_TYPE_DESCRIPTOR, ParserRuleContext.PARAMETERIZED_TYPE,
            ParserRuleContext.ERROR_KEYWORD, ParserRuleContext.STREAM_KEYWORD, ParserRuleContext.FUNC_TYPE_DESC };

    private static final ParserRuleContext[] RECORD_FIELD_OR_RECORD_END =
            { ParserRuleContext.RECORD_BODY_END, ParserRuleContext.RECORD_FIELD };

    private static final ParserRuleContext[] RECORD_FIELD_START =
            { ParserRuleContext.ANNOTATIONS, ParserRuleContext.ASTERISK, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD };

    private static final ParserRuleContext[] RECORD_FIELD_WITHOUT_METADATA =
            { ParserRuleContext.ASTERISK, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD };

    private static final ParserRuleContext[] ARG_START_OR_ARG_LIST_END =
            { ParserRuleContext.ARG_LIST_END, ParserRuleContext.ARG_START };

    private static final ParserRuleContext[] ARG_START =
            { ParserRuleContext.VARIABLE_NAME, ParserRuleContext.ELLIPSIS, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] NAMED_OR_POSITIONAL_ARG_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] PARAM_LIST =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.REQUIRED_PARAM };

    private static final ParserRuleContext[] OBJECT_FIELD_RHS =
            { ParserRuleContext.SEMICOLON, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] OBJECT_MEMBER_START =
            { ParserRuleContext.DOC_STRING, ParserRuleContext.ANNOTATIONS, ParserRuleContext.ASTERISK,
                    ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] OBJECT_MEMBER_WITHOUT_METADATA =
            { ParserRuleContext.ASTERISK, ParserRuleContext.OBJECT_FUNC_OR_FIELD, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] OBJECT_FUNC_OR_FIELD = { ParserRuleContext.PUBLIC_KEYWORD,
            ParserRuleContext.PRIVATE_KEYWORD, ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY };

    private static final ParserRuleContext[] OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.OBJECT_METHOD_START };

    private static final ParserRuleContext[] OBJECT_METHOD_START =
            { ParserRuleContext.REMOTE_KEYWORD, ParserRuleContext.FUNCTION_KEYWORD };

    private static final ParserRuleContext[] OBJECT_TYPE_DESCRIPTOR_START =
            { ParserRuleContext.OBJECT_TYPE_FIRST_QUALIFIER, ParserRuleContext.OBJECT_KEYWORD };

    private static final ParserRuleContext[] ELSE_BODY = { ParserRuleContext.IF_BLOCK, ParserRuleContext.OPEN_BRACE };

    private static final ParserRuleContext[] ELSE_BLOCK =
            { ParserRuleContext.ELSE_KEYWORD, ParserRuleContext.STATEMENT };

    private static final ParserRuleContext[] CALL_STATEMENT =
            { ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] IMPORT_PREFIX_DECL =
            { ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] IMPORT_VERSION =
            { ParserRuleContext.VERSION_KEYWORD, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] IMPORT_DECL_RHS = { ParserRuleContext.SLASH, ParserRuleContext.DOT,
            ParserRuleContext.VERSION_KEYWORD, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] AFTER_IMPORT_MODULE_NAME = { ParserRuleContext.DOT,
            ParserRuleContext.VERSION_KEYWORD, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] MAJOR_MINOR_VERSION_END =
            { ParserRuleContext.DOT, ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] RETURN_RHS = { ParserRuleContext.SEMICOLON, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] EXPRESSION_START = { ParserRuleContext.BASIC_LITERAL,
            ParserRuleContext.NIL_LITERAL, ParserRuleContext.VARIABLE_REF, ParserRuleContext.ACCESS_EXPRESSION,
            ParserRuleContext.TYPEOF_EXPRESSION, ParserRuleContext.TRAP_EXPRESSION, ParserRuleContext.UNARY_EXPRESSION,
            ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.LIST_CONSTRUCTOR,
            ParserRuleContext.TYPE_CAST_EXPRESSION, ParserRuleContext.OPEN_PARENTHESIS,
            ParserRuleContext.TABLE_CONSTRUCTOR, ParserRuleContext.LET_EXPRESSION, ParserRuleContext.TEMPLATE_START,
            ParserRuleContext.XML_KEYWORD, ParserRuleContext.STRING_KEYWORD, ParserRuleContext.ANON_FUNC_EXPRESSION,
            ParserRuleContext.ERROR_CONSTRUCTOR_EXPRESSION };

    private static final ParserRuleContext[] MAPPING_FIELD_START = { ParserRuleContext.MAPPING_FIELD_NAME,
            ParserRuleContext.STRING_LITERAL, ParserRuleContext.COMPUTED_FIELD_NAME, ParserRuleContext.ELLIPSIS };

    private static final ParserRuleContext[] SPECIFIC_FIELD_RHS =
            { ParserRuleContext.COLON, ParserRuleContext.COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] OPTIONAL_SERVICE_NAME =
            { ParserRuleContext.SERVICE_NAME, ParserRuleContext.ON_KEYWORD };

    private static final ParserRuleContext[] RESOURCE_DEF_START =
            { ParserRuleContext.RESOURCE_KEYWORD, ParserRuleContext.FUNC_DEF, ParserRuleContext.CLOSE_BRACE };

    private static final ParserRuleContext[] CONST_DECL_RHS =
            { ParserRuleContext.STATEMENT_START_IDENTIFIER, ParserRuleContext.ASSIGN_OP };

    private static final ParserRuleContext[] ARRAY_LENGTH =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.DECIMAL_INTEGER_LITERAL,
                    ParserRuleContext.HEX_INTEGER_LITERAL, ParserRuleContext.ASTERISK, ParserRuleContext.VARIABLE_REF };

    private static final ParserRuleContext[] PARAMETER_START =
            { ParserRuleContext.ANNOTATIONS, ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.TYPE_DESC_IN_PARAM };

    private static final ParserRuleContext[] PARAMETER_WITHOUT_ANNOTS =
            { ParserRuleContext.PUBLIC_KEYWORD, ParserRuleContext.TYPE_DESC_IN_PARAM };

    private static final ParserRuleContext[] STMT_START_WITH_EXPR_RHS = { ParserRuleContext.ASSIGN_OP,
            ParserRuleContext.RIGHT_ARROW, ParserRuleContext.COMPOUND_BINARY_OPERATOR, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] STMT_START_WITH_IDENTIFIER =
            { ParserRuleContext.ASSIGN_OP, ParserRuleContext.VARIABLE_NAME, ParserRuleContext.EXPRESSION_RHS,
                    ParserRuleContext.VAR_DECL_STARTED_WITH_DENTIFIER };

    private static final ParserRuleContext[] EXPRESSION_STATEMENT_START =
            { ParserRuleContext.VARIABLE_REF, ParserRuleContext.CHECKING_KEYWORD, ParserRuleContext.OPEN_PARENTHESIS };

    private static final ParserRuleContext[] ANNOT_DECL_OPTIONAL_TYPE =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.ANNOTATION_TAG };

    private static final ParserRuleContext[] CONST_DECL_TYPE =
            { ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] ANNOT_DECL_RHS =
            { ParserRuleContext.ANNOTATION_TAG, ParserRuleContext.ON_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] ANNOT_OPTIONAL_ATTACH_POINTS =
            { ParserRuleContext.ON_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] ATTACH_POINT =
            { ParserRuleContext.SOURCE_KEYWORD, ParserRuleContext.ATTACH_POINT_IDENT };

    private static final ParserRuleContext[] ATTACH_POINT_IDENT = { ParserRuleContext.SINGLE_KEYWORD_ATTACH_POINT_IDENT,
            ParserRuleContext.OBJECT_IDENT, ParserRuleContext.RESOURCE_IDENT, ParserRuleContext.RECORD_IDENT };

    private static final ParserRuleContext[] ATTACH_POINT_END =
            { ParserRuleContext.COMMA, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] XML_NAMESPACE_PREFIX_DECL =
            { ParserRuleContext.AS_KEYWORD, ParserRuleContext.SEMICOLON };

    private static final ParserRuleContext[] CONSTANT_EXPRESSION =
            { ParserRuleContext.BASIC_LITERAL, ParserRuleContext.VARIABLE_REF };

    private static final ParserRuleContext[] LIST_CONSTRUCTOR_RHS =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.EXPRESSION };

    private static final ParserRuleContext[] TYPE_CAST_PARAM =
            { ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] TYPE_CAST_PARAM_RHS =
            { ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS, ParserRuleContext.GT };

    private static final ParserRuleContext[] TABLE_KEYWORD_RHS =
            { ParserRuleContext.KEY_SPECIFIER, ParserRuleContext.OPEN_BRACKET };

    private static final ParserRuleContext[] ROW_LIST_RHS =
            { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.MAPPING_CONSTRUCTOR };

    private static final ParserRuleContext[] TABLE_ROW_END =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACKET };

    private static final ParserRuleContext[] KEY_SPECIFIER_RHS =
            { ParserRuleContext.CLOSE_PARENTHESIS, ParserRuleContext.VARIABLE_NAME };

    private static final ParserRuleContext[] TABLE_KEY_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.CLOSE_PARENTHESIS };

    private static final ParserRuleContext[] ERROR_TYPE_PARAMS =
            { ParserRuleContext.INFERRED_TYPE_DESC, ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS };

    private static final ParserRuleContext[] LET_VAR_DECL_START =
            { ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, ParserRuleContext.ANNOTATIONS };

    private static final ParserRuleContext[] STREAM_TYPE_FIRST_PARAM_RHS =
            { ParserRuleContext.COMMA, ParserRuleContext.GT };

    private static final ParserRuleContext[] TEMPLATE_MEMBER = { ParserRuleContext.TEMPLATE_STRING,
            ParserRuleContext.INTERPOLATION_START_TOKEN, ParserRuleContext.TEMPLATE_END };

    private static final ParserRuleContext[] TEMPLATE_STRING_RHS =
            { ParserRuleContext.INTERPOLATION_START_TOKEN, ParserRuleContext.TEMPLATE_END };

    private static final ParserRuleContext[] FUNCTION_KEYWORD_RHS =
            { ParserRuleContext.FUNC_NAME, ParserRuleContext.OPEN_PARENTHESIS };

    private static final ParserRuleContext[] TYPEDESC_RHS =
            { ParserRuleContext.END_OF_TYPE_DESC, ParserRuleContext.ARRAY_TYPE_DESCRIPTOR,
                    ParserRuleContext.OPTIONAL_TYPE_DESCRIPTOR, ParserRuleContext.PIPE };

    public BallerinaParserErrorHandler(AbstractTokenReader tokenReader) {
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
            case FUNC_TYPE_OR_DEF_SIGNATURE_RHS:
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
            case OBJECT_TYPE_FIRST_QUALIFIER:
            case OBJECT_TYPE_SECOND_QUALIFIER:
            case ELSE_BODY:
            case IMPORT_DECL_RHS:
            case IMPORT_SUB_VERSION:
            case VERSION_NUMBER:
            case IMPORT_VERSION_DECL:
            case IMPORT_PREFIX_DECL:
            case MAPPING_FIELD:
            case SPECIFIC_FIELD_RHS:
            case RESOURCE_DEF:
            case PARAMETER_WITHOUT_ANNOTS:
            case PARAMETER_START:
            case STMT_START_WITH_IDENTIFIER:
            case STMT_START_WITH_EXPR_RHS:
            case RECORD_FIELD_OR_RECORD_END:
            case CONST_DECL_TYPE:
            case CONST_DECL_RHS:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
            case XML_NAMESPACE_PREFIX_DECL:
            case ANNOT_DECL_OPTIONAL_TYPE:
            case ANNOT_DECL_RHS:
            case LIST_CONSTRUCTOR_RHS:
            case TYPE_CAST_PARAM:
            case TYPE_CAST_PARAM_RHS:
            case TABLE_KEYWORD_RHS:
            case ROW_LIST_RHS:
            case TABLE_ROW_END:
            case KEY_SPECIFIER_RHS:
            case TABLE_KEY_RHS:
            case ARRAY_LENGTH:
            case TYPEDESC_RHS:
            case ERROR_TYPE_PARAMS:
            case LET_VAR_DECL_START:
            case STREAM_TYPE_FIRST_PARAM_RHS:
            case FUNC_BODY:
            case FUNC_OPTIONAL_RETURNS:
            case TERMINAL_EXPRESSION:
                return true;
            default:
                return false;
        }
    }

    /**
     * TODO: This is a duplicate method. Same as {@link BallerinaParser#isEndOfBlock}.
     *
     * @param token
     * @return
     */
    private boolean isEndOfBlock(STToken token) {
        ParserRuleContext enclosingContext = getParentContext();
        switch (enclosingContext) {
            case OBJECT_TYPE_DESCRIPTOR:
            case SERVICE_DECL:
                switch (token.kind) {
                    case CLOSE_BRACE_TOKEN:
                    case EOF_TOKEN:
                    case CLOSE_BRACE_PIPE_TOKEN:
                    case TYPE_KEYWORD:
                        return true;
                    default:
                        return false;
                }
            case BLOCK_STMT:
                switch (token.kind) {
                    case CLOSE_BRACE_TOKEN:
                    case EOF_TOKEN:
                    case CLOSE_BRACE_PIPE_TOKEN:
                    case ELSE_KEYWORD:
                        return true;
                    default:
                        return false;
                }
            default:
                switch (token.kind) {
                    case CLOSE_BRACE_TOKEN:
                    case EOF_TOKEN:
                    case CLOSE_BRACE_PIPE_TOKEN:
                    case TYPE_KEYWORD:
                    case RESOURCE_KEYWORD:
                        return true;
                    default:
                        return false;
                }
        }
    }

    private boolean isEndOfObjectTypeNode(int nextLookahead) {
        STToken nextToken = this.tokenReader.peek(nextLookahead);
        switch (nextToken.kind) {
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
            case TYPE_KEYWORD:
            case SERVICE_KEYWORD:
                return true;
            default:
                STToken nextNextToken = this.tokenReader.peek(nextLookahead + 1);
                switch (nextNextToken.kind) {
                    case CLOSE_BRACE_TOKEN:
                    case EOF_TOKEN:
                    case CLOSE_BRACE_PIPE_TOKEN:
                    case TYPE_KEYWORD:
                    case SERVICE_KEYWORD:
                        return true;
                    default:
                        return false;
                }
        }
    }

    private boolean isEndOfParametersList(STToken token) {
        switch (token.kind) {
            case OPEN_BRACE_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case PUBLIC_KEYWORD:
            case EOF_TOKEN:
            case RETURNS_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    private boolean isEndOfParameter(STToken token) {
        switch (token.kind) {
            case OPEN_BRACE_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case COMMA_TOKEN:
            case PUBLIC_KEYWORD:
            case EOF_TOKEN:
            case RETURNS_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Search for a solution.
     * Terminals are directly matched and Non-terminals which have alternative productions are seekInAlternativesPaths()
     *
     * @param currentCtx Current context
     * @param lookahead Position of the next token to consider, relative to the position of the original error.
     * @param currentDepth Amount of distance traveled so far.
     * @return Recovery result
     */
    @Override
    protected Result seekMatch(ParserRuleContext currentCtx, int lookahead, int currentDepth, boolean isEntryPoint) {
        boolean hasMatch;
        boolean skipRule;
        int matchingRulesCount = 0;

        while (currentDepth < lookaheadLimit) {
            hasMatch = true;
            skipRule = false;

            STToken nextToken = this.tokenReader.peek(lookahead);
            switch (currentCtx) {
                case EOF:
                    hasMatch = nextToken.kind == SyntaxKind.EOF_TOKEN;
                    break;
                case PUBLIC_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.PUBLIC_KEYWORD;
                    break;
                case PRIVATE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.PRIVATE_KEYWORD;
                    break;
                case REMOTE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.REMOTE_KEYWORD;
                    break;
                case TOP_LEVEL_NODE:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TOP_LEVEL_NODE,
                            isEntryPoint);
                case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            TOP_LEVEL_NODE_WITHOUT_MODIFIER, isEntryPoint);
                case TOP_LEVEL_NODE_WITHOUT_METADATA:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            TOP_LEVEL_NODE_WITHOUT_METADATA, isEntryPoint);
                case FUNCTION_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FUNCTION_KEYWORD;
                    break;
                case FUNC_NAME:
                case VARIABLE_NAME:
                case TYPE_NAME:
                case FIELD_OR_FUNC_NAME:
                case IMPORT_ORG_OR_MODULE_NAME:
                case IMPORT_MODULE_NAME:
                case IMPORT_PREFIX:
                case MAPPING_FIELD_NAME:
                case SERVICE_NAME:
                case QUALIFIED_IDENTIFIER:
                case IDENTIFIER:
                case ANNOTATION_TAG:
                case NAMESPACE_PREFIX:
                case WORKER_NAME:
                    hasMatch = nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case OPEN_PARENTHESIS:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN;
                    break;
                case CLOSE_PARENTHESIS:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN;
                    break;
                case FUNC_OPTIONAL_RETURNS:
                    ParserRuleContext parentCtx = getParentContext();
                    ParserRuleContext[] alternatives;
                    if (parentCtx == ParserRuleContext.FUNC_DEF) {
                        alternatives = FUNC_DEF_OPTIONAL_RETURNS;
                    } else if (parentCtx == ParserRuleContext.ANON_FUNC_EXPRESSION) {
                        alternatives = ANNON_FUNC_OPTIONAL_RETURNS;
                    } else {
                        alternatives = FUNC_TYPE_OR_DEF_OPTIONAL_RETURNS;
                    }
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, alternatives,
                            isEntryPoint);
                case FUNC_TYPE_OR_DEF_SIGNATURE_RHS:
                    endContext(); // end signature
                    parentCtx = getParentContext();
                    if (parentCtx == ParserRuleContext.COMP_UNIT || parentCtx == ParserRuleContext.OBJECT_MEMBER) {
                        // We come here trying to recover statement started with identifier,
                        // and trying to match it against a var-decl. Since this wasn't a var-decl
                        // originally, a context for type hasn't started yet. Therefore start a
                        // a context manually here.
                        startContext(ParserRuleContext.VAR_DECL_STMT);
                        startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                    }
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            FUNC_TYPE_OR_DEF_SIGNATURE_RHS, isEntryPoint);
                case ANNON_FUNC_BODY:
                    endContext(); // end signature
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ANNON_FUNC_BODY,
                            isEntryPoint);
                case RETURNS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RETURNS_KEYWORD;
                    break;
                case SIMPLE_TYPE_DESCRIPTOR:
                    hasMatch = BallerinaParser.isSimpleType(nextToken.kind) ||
                            nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN;
                    break;
                case FUNC_BODY:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, FUNC_BODY,
                            isEntryPoint);
                case OPEN_BRACE:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACE_TOKEN;
                    break;
                case CLOSE_BRACE:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACE_TOKEN;
                    break;
                case ASSIGN_OP:
                    hasMatch = nextToken.kind == SyntaxKind.EQUAL_TOKEN;
                    break;
                case EXTERNAL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.EXTERNAL_KEYWORD;
                    break;
                case SEMICOLON:
                    hasMatch = nextToken.kind == SyntaxKind.SEMICOLON_TOKEN;
                    break;
                case STATEMENT:
                case STATEMENT_WITHOUT_ANNOTS:
                    if (isEndOfBlock(nextToken)) {
                        // If we reach end of statements, then skip processing statements anymore,
                        // and move on to the next rule. This is done to avoid getting stuck on
                        // processing statements forever.
                        skipRule = true;
                        break;
                    }
                    return seekInStatements(currentCtx, nextToken, lookahead, currentDepth, matchingRulesCount,
                            isEntryPoint);
                case BINARY_OPERATOR:
                    hasMatch = isBinaryOperator(nextToken);
                    break;
                case EXPRESSION:
                case TERMINAL_EXPRESSION:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, EXPRESSION_START,
                            isEntryPoint);
                case VAR_DECL_STMT_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, VAR_DECL_RHS,
                            isEntryPoint);
                case EXPRESSION_RHS:
                    return seekMatchInExpressionRhs(nextToken, lookahead, currentDepth, matchingRulesCount,
                            isEntryPoint);
                case COMMA:
                    hasMatch = nextToken.kind == SyntaxKind.COMMA_TOKEN;
                    break;
                case PARAM_LIST:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, PARAM_LIST,
                            isEntryPoint);
                case PARAMETER_NAME_RHS:
                    parentCtx = getParentContext();
                    switch (parentCtx) {
                        case REQUIRED_PARAM:
                            return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, PARAMETER_RHS,
                                    isEntryPoint);
                        case DEFAULTABLE_PARAM:
                        case REST_PARAM:
                            skipRule = true;
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    break;
                case STATEMENT_START_IDENTIFIER:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TYPE_OR_VAR_NAME,
                            isEntryPoint);
                case ASSIGNMENT_OR_VAR_DECL_STMT_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            ASSIGNMENT_OR_VAR_DECL_SECOND_TOKEN, isEntryPoint);

                case CLOSED_RECORD_BODY_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
                    break;
                case CLOSED_RECORD_BODY_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
                    break;
                case ELLIPSIS:
                    hasMatch = nextToken.kind == SyntaxKind.ELLIPSIS_TOKEN;
                    break;
                case QUESTION_MARK:
                    hasMatch = nextToken.kind == SyntaxKind.QUESTION_MARK_TOKEN;
                    break;
                case RECORD_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RECORD_KEYWORD;
                    break;
                case TYPE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TYPE_KEYWORD;
                    break;
                case FIELD_DESCRIPTOR_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, FIELD_DESCRIPTOR_RHS,
                            isEntryPoint);
                case FIELD_OR_REST_DESCIPTOR_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            FIELD_OR_REST_DESCIPTOR_RHS, isEntryPoint);
                case RECORD_BODY_END:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, RECORD_BODY_END,
                            isEntryPoint);
                case RECORD_BODY_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, RECORD_BODY_START,
                            isEntryPoint);
                case TYPE_DESCRIPTOR:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TYPE_DESCRIPTORS,
                            isEntryPoint);
                case RECORD_FIELD_OR_RECORD_END:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            RECORD_FIELD_OR_RECORD_END, isEntryPoint);
                case RECORD_FIELD_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, RECORD_FIELD_START,
                            isEntryPoint);
                case RECORD_FIELD_WITHOUT_METADATA:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            RECORD_FIELD_WITHOUT_METADATA, isEntryPoint);
                case ARG_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ARG_START,
                            isEntryPoint);
                case ARG_LIST_START:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN;
                    break;
                case ARG_LIST_END:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN;
                    break;
                case ARG_START_OR_ARG_LIST_END:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            ARG_START_OR_ARG_LIST_END, isEntryPoint);
                case NAMED_OR_POSITIONAL_ARG_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            NAMED_OR_POSITIONAL_ARG_RHS, isEntryPoint);
                case OBJECT_MEMBER:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, OBJECT_MEMBER_START,
                            isEntryPoint);
                case OBJECT_MEMBER_WITHOUT_METADATA:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            OBJECT_MEMBER_WITHOUT_METADATA, isEntryPoint);
                case OBJECT_FIELD_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, OBJECT_FIELD_RHS,
                            isEntryPoint);
                case OBJECT_METHOD_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, OBJECT_METHOD_START,
                            isEntryPoint);
                case OBJECT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.OBJECT_KEYWORD;
                    break;
                case OBJECT_FUNC_OR_FIELD:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, OBJECT_FUNC_OR_FIELD,
                            isEntryPoint);
                case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY, isEntryPoint);
                case OBJECT_TYPE_DESCRIPTOR_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            OBJECT_TYPE_DESCRIPTOR_START, isEntryPoint);
                case OBJECT_TYPE_FIRST_QUALIFIER:
                case OBJECT_TYPE_SECOND_QUALIFIER:
                    // If currentDepth == 0 means its the very next token after the error. If that erroneous
                    // token is a correct match, then that means we have reached here because of a duplicate
                    // modifier. Therefore treat it as a mismatch.
                    if (currentDepth == 0) {
                        hasMatch = false;
                        break;
                    }

                    hasMatch = nextToken.kind == SyntaxKind.ABSTRACT_KEYWORD ||
                            nextToken.kind == SyntaxKind.CLIENT_KEYWORD;
                    break;
                case ABSTRACT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ABSTRACT_KEYWORD;
                    break;
                case CLIENT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CLIENT_KEYWORD;
                    break;
                case OPEN_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN;
                    break;
                case CLOSE_BRACKET:
                    hasMatch = nextToken.kind == SyntaxKind.CLOSE_BRACKET_TOKEN;
                    break;
                case DOT:
                    hasMatch = nextToken.kind == SyntaxKind.DOT_TOKEN;
                    break;
                case IF_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IF_KEYWORD;
                    break;
                case ELSE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ELSE_KEYWORD;
                    break;
                case ELSE_BLOCK:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ELSE_BLOCK,
                            isEntryPoint);
                case ELSE_BODY:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ELSE_BODY,
                            isEntryPoint);
                case WHILE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.WHILE_KEYWORD;
                    break;
                case CHECKING_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CHECK_KEYWORD ||
                            nextToken.kind == SyntaxKind.CHECKPANIC_KEYWORD;
                    break;
                case CALL_STMT_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, CALL_STATEMENT,
                            isEntryPoint);
                case PANIC_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.PANIC_KEYWORD;
                    break;
                case AS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.AS_KEYWORD;
                    break;
                case LOCK_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.LOCK_KEYWORD;
                    break;
                case BOOLEAN_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.TRUE_KEYWORD || nextToken.kind == SyntaxKind.FALSE_KEYWORD;
                    break;
                case DECIMAL_INTEGER_LITERAL:
                case MAJOR_VERSION:
                case MINOR_VERSION:
                case PATCH_VERSION:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_INTEGER_LITERAL;
                    break;
                case IMPORT_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IMPORT_KEYWORD;
                    break;
                case SLASH:
                    hasMatch = nextToken.kind == SyntaxKind.SLASH_TOKEN;
                    break;
                case VERSION_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.VERSION_KEYWORD;
                    break;
                case CONTINUE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CONTINUE_KEYWORD;
                    break;
                case BREAK_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.BREAK_KEYWORD;
                    break;
                case IMPORT_PREFIX_DECL:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, IMPORT_PREFIX_DECL,
                            isEntryPoint);
                case IMPORT_VERSION_DECL:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, IMPORT_VERSION,
                            isEntryPoint);
                case IMPORT_DECL_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, IMPORT_DECL_RHS,
                            isEntryPoint);
                case AFTER_IMPORT_MODULE_NAME:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            AFTER_IMPORT_MODULE_NAME, isEntryPoint);
                case MAJOR_MINOR_VERSION_END:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, MAJOR_MINOR_VERSION_END,
                            isEntryPoint);
                case RETURN_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RETURN_KEYWORD;
                    break;
                case RETURN_STMT_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, RETURN_RHS,
                            isEntryPoint);
                case ACCESS_EXPRESSION:
                    return seekInAccessExpression(currentCtx, lookahead, currentDepth, matchingRulesCount,
                            isEntryPoint);
                case BASIC_LITERAL:
                    hasMatch = isBasicLiteral(nextToken.kind);
                    break;
                case COLON:
                    hasMatch = nextToken.kind == SyntaxKind.COLON_TOKEN;
                    break;
                case STRING_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.STRING_LITERAL;
                    break;
                case MAPPING_FIELD:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, MAPPING_FIELD_START,
                            isEntryPoint);
                case SPECIFIC_FIELD_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, SPECIFIC_FIELD_RHS,
                            isEntryPoint);
                case SERVICE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.SERVICE_KEYWORD;
                    break;
                case ON_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ON_KEYWORD;
                    break;
                case OPTIONAL_SERVICE_NAME:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, OPTIONAL_SERVICE_NAME,
                            isEntryPoint);
                case RESOURCE_DEF:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, RESOURCE_DEF_START,
                            isEntryPoint);
                case RESOURCE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.RESOURCE_KEYWORD;
                    break;
                case LISTENER_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.LISTENER_KEYWORD;
                    break;
                case CONST_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.CONST_KEYWORD;
                    break;
                case FINAL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FINAL_KEYWORD;
                    break;
                case CONST_DECL_TYPE:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, CONST_DECL_TYPE,
                            isEntryPoint);
                case CONST_DECL_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, CONST_DECL_RHS,
                            isEntryPoint);
                case TYPEOF_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TYPEOF_KEYWORD;
                    break;
                case UNARY_OPERATOR:
                    hasMatch = isUnaryOperator(nextToken);
                    break;
                case ARRAY_LENGTH:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ARRAY_LENGTH,
                            isEntryPoint);
                case HEX_INTEGER_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.HEX_INTEGER_LITERAL;
                    break;
                case AT:
                    hasMatch = nextToken.kind == SyntaxKind.AT_TOKEN;
                    break;
                case PARAMETER_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, PARAMETER_START,
                            isEntryPoint);
                case PARAMETER_WITHOUT_ANNOTS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            PARAMETER_WITHOUT_ANNOTS, isEntryPoint);
                case IS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IS_KEYWORD;
                    break;
                case STMT_START_WITH_EXPR_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            STMT_START_WITH_EXPR_RHS, isEntryPoint);
                case RIGHT_ARROW:
                    hasMatch = nextToken.kind == SyntaxKind.RIGHT_ARROW_TOKEN;
                    break;
                case STMT_START_WITH_IDENTIFIER:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            STMT_START_WITH_IDENTIFIER, isEntryPoint);
                case EXPRESSION_STATEMENT_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            EXPRESSION_STATEMENT_START, isEntryPoint);
                case PARAMETERIZED_TYPE:
                    hasMatch = isParameterizedTypeToken(nextToken.kind);
                    break;
                case LT:
                    hasMatch = nextToken.kind == SyntaxKind.LT_TOKEN;
                    break;
                case GT:
                    hasMatch = nextToken.kind == SyntaxKind.GT_TOKEN;
                    break;
                case NULL_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.NULL_KEYWORD;
                    break;
                case ANNOTATION_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ANNOTATION_KEYWORD;
                    break;
                case FIELD_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.FIELD_KEYWORD;
                    break;
                case FUNCTION_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.FUNCTION_KEYWORD;
                    break;
                case IDENT_AFTER_OBJECT_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.TYPE_KEYWORD ||
                            nextToken.kind == SyntaxKind.FUNCTION_KEYWORD || nextToken.kind == SyntaxKind.FIELD_KEYWORD;
                    break;
                case SOURCE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.SOURCE_KEYWORD;
                    break;
                case ANNOT_DECL_OPTIONAL_TYPE:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            ANNOT_DECL_OPTIONAL_TYPE, isEntryPoint);
                case ANNOT_DECL_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ANNOT_DECL_RHS,
                            isEntryPoint);
                case ANNOT_OPTIONAL_ATTACH_POINTS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            ANNOT_OPTIONAL_ATTACH_POINTS, isEntryPoint);
                case ATTACH_POINT:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ATTACH_POINT,
                            isEntryPoint);
                case ATTACH_POINT_IDENT:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ATTACH_POINT_IDENT,
                            isEntryPoint);
                case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
                    hasMatch = isSingleKeywordAttachPointIdent(nextToken.kind);
                    break;
                case OBJECT_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.OBJECT_KEYWORD;
                    break;
                case RECORD_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.RECORD_KEYWORD;
                    break;
                case RESOURCE_IDENT:
                    hasMatch = nextToken.kind == SyntaxKind.RESOURCE_KEYWORD;
                    break;
                case ATTACH_POINT_END:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ATTACH_POINT_END,
                            isEntryPoint);
                case XML_NAMESPACE_PREFIX_DECL:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            XML_NAMESPACE_PREFIX_DECL, isEntryPoint);
                case CONSTANT_EXPRESSION_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, CONSTANT_EXPRESSION,
                            isEntryPoint);
                case XMLNS_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.XMLNS_KEYWORD;
                    break;
                case WORKER_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.WORKER_KEYWORD;
                    break;
                case FORK_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FORK_KEYWORD;
                    break;
                case DECIMAL_FLOATING_POINT_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL;
                    break;
                case HEX_FLOATING_POINT_LITERAL:
                    hasMatch = nextToken.kind == SyntaxKind.HEX_FLOATING_POINT_LITERAL;
                    break;
                case TYPEDESC_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TYPEDESC_RHS,
                            isEntryPoint);
                case TRAP_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TRAP_KEYWORD;
                    break;
                case LIST_CONSTRUCTOR_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, LIST_CONSTRUCTOR_RHS,
                            isEntryPoint);
                case FOREACH_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.FOREACH_KEYWORD;
                    break;
                case IN_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.IN_KEYWORD;
                    break;
                case TYPE_CAST_PARAM:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TYPE_CAST_PARAM,
                            isEntryPoint);
                case TYPE_CAST_PARAM_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TYPE_CAST_PARAM_RHS,
                            isEntryPoint);
                case PIPE:
                    hasMatch = nextToken.kind == SyntaxKind.PIPE_TOKEN;
                    break;
                case TABLE_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.TABLE_KEYWORD;
                    break;
                case KEY_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.KEY_KEYWORD;
                    break;
                case TABLE_KEYWORD_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TABLE_KEYWORD_RHS,
                            isEntryPoint);
                case ROW_LIST_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ROW_LIST_RHS,
                            isEntryPoint);
                case TABLE_ROW_END:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TABLE_ROW_END,
                            isEntryPoint);
                case KEY_SPECIFIER_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, KEY_SPECIFIER_RHS,
                            isEntryPoint);
                case TABLE_KEY_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TABLE_KEY_RHS,
                            isEntryPoint);
                case ERROR_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.ERROR_KEYWORD;
                    break;
                case ERROR_TYPE_PARAMS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, ERROR_TYPE_PARAMS,
                            isEntryPoint);
                case LET_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.LET_KEYWORD;
                    break;
                case LET_VAR_DECL_START:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, LET_VAR_DECL_START,
                            isEntryPoint);
                case STREAM_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.STREAM_KEYWORD;
                    break;
                case STREAM_TYPE_FIRST_PARAM_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount,
                            STREAM_TYPE_FIRST_PARAM_RHS, isEntryPoint);
                case TEMPLATE_START:
                case TEMPLATE_END:
                    hasMatch = nextToken.kind == SyntaxKind.BACKTICK_TOKEN;
                    break;
                case TEMPLATE_MEMBER:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TEMPLATE_MEMBER,
                            isEntryPoint);
                case TEMPLATE_STRING_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, TEMPLATE_STRING_RHS,
                            isEntryPoint);
                case XML_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.XML_KEYWORD;
                    break;
                case STRING_KEYWORD:
                    hasMatch = nextToken.kind == SyntaxKind.XML_KEYWORD;
                    break;
                case FUNCTION_KEYWORD_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, FUNCTION_KEYWORD_RHS,
                            isEntryPoint);
                case WORKER_NAME_RHS:
                    return seekInAlternativesPaths(lookahead, currentDepth, matchingRulesCount, WORKER_NAME_RHS,
                            isEntryPoint);
                case ASTERISK:
                case INFERRED_TYPE_DESC:
                    hasMatch = nextToken.kind == SyntaxKind.ASTERISK_TOKEN;
                    break;

                case COMP_UNIT:
                case FUNC_DEF_OR_FUNC_TYPE:
                case FUNC_DEF:
                case ANON_FUNC_EXPRESSION:
                case EXTERNAL_FUNC_BODY:
                case FUNC_BODY_BLOCK:
                case ASSIGNMENT_STMT:
                case VAR_DECL_STMT:
                case REQUIRED_PARAM:
                case AFTER_PARAMETER_TYPE:
                case DEFAULTABLE_PARAM:
                case REST_PARAM:
                case MODULE_TYPE_DEFINITION:
                case FUNC_CALL:
                case RECORD_TYPE_DESCRIPTOR:
                case OBJECT_TYPE_DESCRIPTOR:
                case ASSIGNMENT_OR_VAR_DECL_STMT:
                case CALL_STMT:
                case IF_BLOCK:
                case BLOCK_STMT:
                case WHILE_BLOCK:
                case VERSION_NUMBER:
                case IMPORT_DECL:
                case IMPORT_SUB_VERSION:
                case MAPPING_CONSTRUCTOR:
                case PANIC_STMT:
                case COMPUTED_FIELD_NAME:
                case RETURN_STMT:
                case LISTENERS_LIST:
                case SERVICE_DECL:
                case BREAK_STATEMENT:
                case CONTINUE_STATEMENT:
                case LISTENER_DECL:
                case CONSTANT_DECL:
                case NIL_TYPE_DESCRIPTOR:
                case OPTIONAL_TYPE_DESCRIPTOR:
                case ARRAY_TYPE_DESCRIPTOR:
                case LOCAL_TYPE_DEFINITION_STMT:
                case ANNOTATIONS:
                case DOC_STRING:
                case ANNOTATION_DECL:
                case ANNOT_ATTACH_POINTS_LIST:
                case COMPOUND_ASSIGNMENT_STMT:
                case COMPOUND_BINARY_OPERATOR:
                case EXPRESSION_STATEMENT:
                case RECORD_FIELD:
                case TYPEOF_EXPRESSION:
                case UNARY_EXPRESSION:
                case CONSTANT_EXPRESSION:
                case XML_NAMESPACE_DECLARATION:
                case DEFAULT_WORKER_INIT:
                case DEFAULT_WORKER:
                case NAMED_WORKERS:
                case NAMED_WORKER_DECL:
                case TEMPLATE_BODY:
                case NIL_LITERAL:
                case LOCK_STMT:
                case FORK_STMT:
                case TRAP_EXPRESSION:
                case LIST_CONSTRUCTOR:
                case FOREACH_STMT:
                case TYPE_CAST_EXPRESSION:
                case TABLE_CONSTRUCTOR:
                case KEY_SPECIFIER:
                case LET_VAR_DECL:
                case LET_EXPRESSION:
                case END_OF_TYPE_DESC:
                case VAR_DECL_STARTED_WITH_DENTIFIER:
                case ERROR_CONSTRUCTOR_EXPRESSION:

                    // start a context, so that we know where to fall back, and continue
                    // having the qualified-identifier as the next rule.
                case VARIABLE_REF:
                case TYPE_REFERENCE:
                case ANNOT_REFERENCE:

                    // Contexts that expect a type
                case TYPE_DESC_IN_ANNOTATION_DECL:
                case TYPE_DESC_BEFORE_IDENTIFIER:
                case TYPE_DESC_IN_RECORD_FIELD:
                case TYPE_DESC_IN_PARAM:
                case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
                case TYPE_DESC_IN_TYPE_DEF:
                case TYPE_DESC_IN_ANGLE_BRACKETS:
                case TYPE_DESC_IN_RETURN_TYPE_DESC:
                case TYPE_DESC_IN_EXPRESSION:
                case TYPE_DESC_IN_STREAM_TYPE_DESC:
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
        result.solution = new Solution(Action.KEEP, currentCtx, SyntaxKind.NONE, currentCtx.toString());
        return result;
    }

    /**
     * Search for matching token sequences within different kinds of statements and returns the most optimal solution.
     * 
     * @param currentCtx Current context
     * @param nextToken Next token in the token stream
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param fixes Fixes made so far
     * @return Recovery result
     */
    private Result seekInStatements(ParserRuleContext currentCtx, STToken nextToken, int lookahead, int currentDepth,
                                    int currentMatches, boolean isEntryPoint) {
        if (nextToken.kind == SyntaxKind.SEMICOLON_TOKEN) {
            // Semicolon at the start of a statement is a special case. This is equivalent to an empty
            // statement. So assume the fix for this is a REMOVE operation and continue from the next token.
            Result result = seekMatchInSubTree(ParserRuleContext.STATEMENT, lookahead + 1, currentDepth, isEntryPoint);
            result.fixes.push(new Solution(Action.REMOVE, currentCtx, nextToken.kind, nextToken.toString()));
            return getFinalResult(currentMatches, result);
        }

        return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, STATEMENTS, isEntryPoint);
    }

    /**
     * Search for matching token sequences within access expressions and returns the most optimal solution.
     * Access expression can be one of: method-call, field-access, member-access.
     * 
     * @param currentCtx Current context
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param fixes Fixes made so far
     * @param isEntryPoint
     * @return Recovery result
     */
    private Result seekInAccessExpression(ParserRuleContext currentCtx, int lookahead, int currentDepth,
                                          int currentMatches, boolean isEntryPoint) {
        STToken nextToken = this.tokenReader.peek(lookahead);
        currentDepth++;
        if (nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
            Result fixedPathResult = fixAndContinue(currentCtx, lookahead, currentDepth);
            return getFinalResult(currentMatches, fixedPathResult);
        }

        ParserRuleContext nextContext;
        STToken nextNextToken = this.tokenReader.peek(lookahead + 1);
        switch (nextNextToken.kind) {
            case OPEN_PAREN_TOKEN:
                nextContext = ParserRuleContext.OPEN_PARENTHESIS;
                break;
            case DOT_TOKEN:
                nextContext = ParserRuleContext.DOT;
                break;
            case OPEN_BRACKET_TOKEN:
                nextContext = ParserRuleContext.OPEN_BRACKET;
                break;
            default:
                nextContext = getNextRuleForExpr();
                break;
        }

        currentMatches++;
        lookahead++;
        Result result = seekMatch(nextContext, lookahead, currentDepth, isEntryPoint);
        result.ctx = currentCtx;
        return getFinalResult(currentMatches, result);
    }

    /**
     * Search for a match in rhs of an expression. RHS of an expression can be the end
     * of the expression or the rhs of a binary expression.
     * 
     * @param nextToken
     * @param lookahead Position of the next token to consider, relative to the position of the original error
     * @param currentDepth Amount of distance traveled so far
     * @param currentMatches Matching tokens found so far
     * @param isEntryPoint
     * @return Recovery result
     */
    private Result seekMatchInExpressionRhs(STToken nextToken, int lookahead, int currentDepth, int currentMatches,
                                            boolean isEntryPoint) {
        ParserRuleContext parentCtx = getParentContext();
        ParserRuleContext[] next;
        switch (parentCtx) {
            case ARG_LIST:
                next = new ParserRuleContext[] { ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.OPEN_BRACKET, ParserRuleContext.COMMA, ParserRuleContext.ARG_LIST_START,
                        ParserRuleContext.ARG_LIST_END };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case MAPPING_CONSTRUCTOR:
                next = new ParserRuleContext[] { ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT,
                        ParserRuleContext.OPEN_BRACKET, ParserRuleContext.COMMA, ParserRuleContext.CLOSE_BRACE,
                        ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case COMPUTED_FIELD_NAME:
                // Here we give high priority to the comma. Therefore order of the below array matters.
                next = new ParserRuleContext[] { ParserRuleContext.CLOSE_BRACKET, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.OPEN_BRACKET, ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case LISTENERS_LIST:
                next = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.OPEN_BRACKET, ParserRuleContext.OPEN_BRACE,
                        ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case LIST_CONSTRUCTOR:
                next = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.OPEN_BRACKET, ParserRuleContext.CLOSE_BRACKET,
                        ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            case LET_VAR_DECL:
                next = new ParserRuleContext[] { ParserRuleContext.COMMA, ParserRuleContext.BINARY_OPERATOR,
                        ParserRuleContext.DOT, ParserRuleContext.OPEN_BRACKET, ParserRuleContext.IN_KEYWORD,
                        ParserRuleContext.ARG_LIST_START };
                return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
            default:
                if (isParameter(parentCtx)) {
                    next = new ParserRuleContext[] { ParserRuleContext.CLOSE_PARENTHESIS,
                            ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.DOT, ParserRuleContext.OPEN_BRACKET,
                            ParserRuleContext.COMMA, ParserRuleContext.ARG_LIST_START };
                    return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, next, isEntryPoint);
                }
                break;
        }

        ParserRuleContext nextContext;
        if (parentCtx == ParserRuleContext.IF_BLOCK || parentCtx == ParserRuleContext.WHILE_BLOCK ||
                parentCtx == ParserRuleContext.FOREACH_STMT) {
            nextContext = ParserRuleContext.BLOCK_STMT;
        } else if (isStatement(parentCtx) || parentCtx == ParserRuleContext.RECORD_FIELD ||
                parentCtx == ParserRuleContext.OBJECT_MEMBER || parentCtx == ParserRuleContext.LISTENER_DECL ||
                parentCtx == ParserRuleContext.CONSTANT_DECL) {
            nextContext = ParserRuleContext.SEMICOLON;
        } else if (parentCtx == ParserRuleContext.ANNOTATIONS) {
            nextContext = ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
            nextContext = ParserRuleContext.CLOSE_BRACKET;
        } else if (parentCtx == ParserRuleContext.INTERPOLATION) {
            nextContext = ParserRuleContext.CLOSE_BRACE;
        } else {
            throw new IllegalStateException(parentCtx.toString());
        }

        ParserRuleContext[] alternatives = { ParserRuleContext.BINARY_OPERATOR, ParserRuleContext.IS_KEYWORD,
                ParserRuleContext.DOT, ParserRuleContext.OPEN_BRACKET, ParserRuleContext.ARG_LIST_START, nextContext };
        return seekInAlternativesPaths(lookahead, currentDepth, currentMatches, alternatives, isEntryPoint);
    }

    /**
     * Get the next parser rule/context given the current parser context.
     * 
     * @param currentCtx Current parser context
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    @Override
    protected ParserRuleContext getNextRule(ParserRuleContext currentCtx, int nextLookahead) {
        // If this is a production, then push the context to the stack.
        // We can do this within the same switch-case that follows after this one.
        // But doing it separately for the sake of readability/maintainability.
        switch (currentCtx) {
            case COMP_UNIT:
            case FUNC_DEF_OR_FUNC_TYPE:
            case ANON_FUNC_EXPRESSION:
            case FUNC_DEF:
            case FUNC_TYPE_DESC:
            case EXTERNAL_FUNC_BODY:
            case FUNC_BODY_BLOCK:
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case VAR_DECL_STMT:
            case ASSIGNMENT_STMT:
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
            case MODULE_TYPE_DEFINITION:
            case RECORD_FIELD:
            case RECORD_TYPE_DESCRIPTOR:
            case OBJECT_TYPE_DESCRIPTOR:
            case ARG_LIST:
            case OBJECT_FUNC_OR_FIELD:
            case IF_BLOCK:
            case BLOCK_STMT:
            case WHILE_BLOCK:
            case PANIC_STMT:
            case CALL_STMT:
            case IMPORT_DECL:
            case CONTINUE_STATEMENT:
            case BREAK_STATEMENT:
            case RETURN_STMT:
            case COMPUTED_FIELD_NAME:
            case LISTENERS_LIST:
            case SERVICE_DECL:
            case LISTENER_DECL:
            case CONSTANT_DECL:
            case NIL_TYPE_DESCRIPTOR:
            case COMPOUND_ASSIGNMENT_STMT:
            case OPTIONAL_TYPE_DESCRIPTOR:
            case ARRAY_TYPE_DESCRIPTOR:
            case ANNOTATIONS:
            case VARIABLE_REF:
            case TYPE_REFERENCE:
            case ANNOT_REFERENCE:
            case MAPPING_CONSTRUCTOR:
            case LOCAL_TYPE_DEFINITION_STMT:
            case EXPRESSION_STATEMENT:
            case NIL_LITERAL:
            case LOCK_STMT:
            case ANNOTATION_DECL:
            case ANNOT_ATTACH_POINTS_LIST:
            case XML_NAMESPACE_DECLARATION:
            case CONSTANT_EXPRESSION:
            case NAMED_WORKER_DECL:
            case FORK_STMT:
            case FOREACH_STMT:
            case LIST_CONSTRUCTOR:
            case TYPE_CAST_EXPRESSION:
            case TABLE_CONSTRUCTOR:
            case KEY_SPECIFIER:
            case LET_VAR_DECL:
            case ERROR_CONSTRUCTOR_EXPRESSION:

                // Contexts that expect a type
            case TYPE_DESC_IN_ANNOTATION_DECL:
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_PARAM:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
            case TYPE_DESC_IN_TYPE_DEF:
            case TYPE_DESC_IN_ANGLE_BRACKETS:
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
            case TYPE_DESC_IN_EXPRESSION:
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                startContext(currentCtx);
                break;
            default:
                break;
        }

        ParserRuleContext parentCtx;
        STToken nextToken;
        switch (currentCtx) {
            case EOF:
                return ParserRuleContext.EOF;
            case COMP_UNIT:
                return ParserRuleContext.TOP_LEVEL_NODE;
            case PUBLIC_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.OBJECT_TYPE_DESCRIPTOR) {
                    return ParserRuleContext.OBJECT_FUNC_OR_FIELD;
                } else if (isParameter(parentCtx)) {
                    return ParserRuleContext.TYPE_DESC_IN_PARAM;
                }
                return ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_MODIFIER;
            case PRIVATE_KEYWORD:
                return ParserRuleContext.OBJECT_FUNC_OR_FIELD;
            case FUNC_DEF:
            case FUNC_DEF_OR_FUNC_TYPE:
            case FUNC_TYPE_DESC:
            case ANON_FUNC_EXPRESSION:
                return ParserRuleContext.FUNCTION_KEYWORD;
            case EXTERNAL_FUNC_BODY:
                return ParserRuleContext.ASSIGN_OP;
            case FUNC_BODY_BLOCK:
                return ParserRuleContext.OPEN_BRACE;
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
                // We reach here only if an end of a block is reached.
                endContext(); // end statement
                return ParserRuleContext.CLOSE_BRACE;
            case ASSIGN_OP:
                return getNextRuleForEqualOp();
            case COMPOUND_BINARY_OPERATOR:
                return ParserRuleContext.ASSIGN_OP;
            case CLOSE_BRACE:
                return getNextRuleForCloseBrace(nextLookahead);
            case CLOSE_PARENTHESIS:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.PARAM_LIST) {
                    endContext(); // end parameters
                    return ParserRuleContext.FUNC_OPTIONAL_RETURNS;
                } else if (isParameter(parentCtx)) {
                    endContext(); // end parameters
                    endContext(); // end parameter
                    return ParserRuleContext.FUNC_OPTIONAL_RETURNS;
                } else if (parentCtx == ParserRuleContext.NIL_TYPE_DESCRIPTOR) {
                    endContext();
                    // After parsing nil type descriptor all the other parsing is same as next rule of simple type
                    return ParserRuleContext.TYPEDESC_RHS;
                } else if (parentCtx == ParserRuleContext.NIL_LITERAL) {
                    endContext();
                    return ParserRuleContext.EXPRESSION_RHS;
                } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
                    endContext(); // end key-specifier
                    // For now key-specifier ctx is only referred inside table-constructor ctx
                    return ParserRuleContext.OPEN_BRACKET;
                }
                return ParserRuleContext.EXPRESSION_RHS;
            case EXPRESSION:
            case BASIC_LITERAL:
            case TERMINAL_EXPRESSION:
                return getNextRuleForExpr();
            case EXTERNAL_KEYWORD:
                return ParserRuleContext.SEMICOLON;
            case FUNCTION_KEYWORD:
                return ParserRuleContext.FUNCTION_KEYWORD_RHS;
            case FUNC_NAME:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case OPEN_BRACE:
                return getNextRuleForOpenBrace(nextLookahead);
            case OPEN_PARENTHESIS:
                return getNextRuleForOpenParenthesis();
            case RETURNS_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC;
            case SEMICOLON:
                return getNextRuleForSemicolon(nextLookahead);
            case SIMPLE_TYPE_DESCRIPTOR:
                return ParserRuleContext.TYPEDESC_RHS;
            case VARIABLE_NAME:
            case PARAMETER_NAME_RHS:
                return getNextRuleForVarName(nextLookahead);
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
                return ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE;
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                return ParserRuleContext.TYPE_DESC_IN_PARAM;
            case ASSIGNMENT_STMT:
                return ParserRuleContext.VARIABLE_NAME;
            case COMPOUND_ASSIGNMENT_STMT:
                return ParserRuleContext.VARIABLE_NAME;
            case VAR_DECL_STMT:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case EXPRESSION_RHS:
                return ParserRuleContext.BINARY_OPERATOR;
            case BINARY_OPERATOR:
                return ParserRuleContext.EXPRESSION;
            case COMMA:
                return getNextRuleForComma();
            case AFTER_PARAMETER_TYPE:
                return getNextRuleForParamType();
            case MODULE_TYPE_DEFINITION:
                return ParserRuleContext.TYPE_KEYWORD;
            case CLOSED_RECORD_BODY_END:
                endContext();
                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                    return ParserRuleContext.EOF;
                }
                return ParserRuleContext.TYPEDESC_RHS;
            case CLOSED_RECORD_BODY_START:
                return ParserRuleContext.RECORD_FIELD_OR_RECORD_END;
            case ELLIPSIS:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.MAPPING_CONSTRUCTOR || parentCtx == ParserRuleContext.ARG_LIST) {
                    return ParserRuleContext.EXPRESSION;
                }
                return ParserRuleContext.VARIABLE_NAME;
            case QUESTION_MARK:
                return getNextRuleForQuestionMark();
            case RECORD_KEYWORD:
                return ParserRuleContext.RECORD_BODY_START;
            case TYPE_KEYWORD:
                return ParserRuleContext.TYPE_NAME;
            case RECORD_TYPE_DESCRIPTOR:
                return ParserRuleContext.RECORD_KEYWORD;
            case ASTERISK:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
                    return ParserRuleContext.CLOSE_BRACKET;
                }
                return ParserRuleContext.TYPE_REFERENCE;
            case TYPE_NAME:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_DEF;
            case OBJECT_KEYWORD:
                return ParserRuleContext.OPEN_BRACE;
            case REMOTE_KEYWORD:
                return ParserRuleContext.FUNCTION_KEYWORD;
            case OBJECT_TYPE_DESCRIPTOR:
                return ParserRuleContext.OBJECT_TYPE_DESCRIPTOR_START;
            case OBJECT_TYPE_FIRST_QUALIFIER:
            case OBJECT_TYPE_SECOND_QUALIFIER:
                return ParserRuleContext.OBJECT_KEYWORD;
            case ABSTRACT_KEYWORD:
            case CLIENT_KEYWORD:
                return ParserRuleContext.OBJECT_KEYWORD;
            case OPEN_BRACKET:
                return getNextRuleForOpenBracket();
            case CLOSE_BRACKET:
                return getNextRuleForCloseBracket();
            case FIELD_OR_FUNC_NAME:
                return ParserRuleContext.EXPRESSION_RHS;
            case DOT:
                return getNextRuleForDot();
            case IF_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case ELSE_KEYWORD:
                return ParserRuleContext.ELSE_BODY;
            case BLOCK_STMT:
                return ParserRuleContext.OPEN_BRACE;
            case IF_BLOCK:
                return ParserRuleContext.IF_KEYWORD;
            case WHILE_BLOCK:
                return ParserRuleContext.WHILE_KEYWORD;
            case WHILE_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case CHECKING_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case CALL_STMT:
                return ParserRuleContext.CALL_STMT_START;
            case PANIC_STMT:
                return ParserRuleContext.PANIC_KEYWORD;
            case PANIC_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case FUNC_CALL:
                // TODO: check this again
                return ParserRuleContext.IMPORT_PREFIX;
            case IMPORT_KEYWORD:
                return ParserRuleContext.IMPORT_ORG_OR_MODULE_NAME;
            case IMPORT_PREFIX:
            case NAMESPACE_PREFIX:
                return ParserRuleContext.SEMICOLON;
            case VERSION_NUMBER:
            case VERSION_KEYWORD:
                return ParserRuleContext.MAJOR_VERSION;
            case SLASH:
                return ParserRuleContext.IMPORT_MODULE_NAME;
            case IMPORT_ORG_OR_MODULE_NAME:
                return ParserRuleContext.IMPORT_DECL_RHS;
            case IMPORT_MODULE_NAME:
                return ParserRuleContext.AFTER_IMPORT_MODULE_NAME;
            case AS_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.IMPORT_DECL) {
                    return ParserRuleContext.IMPORT_PREFIX;
                } else if (parentCtx == ParserRuleContext.XML_NAMESPACE_DECLARATION) {
                    return ParserRuleContext.NAMESPACE_PREFIX;
                }
                throw new IllegalStateException();
            case MAJOR_VERSION:
            case MINOR_VERSION:
            case IMPORT_SUB_VERSION:
                return ParserRuleContext.MAJOR_MINOR_VERSION_END;
            case PATCH_VERSION:
                return ParserRuleContext.IMPORT_PREFIX_DECL;
            case IMPORT_DECL:
                return ParserRuleContext.IMPORT_KEYWORD;
            case CONTINUE_STATEMENT:
                return ParserRuleContext.CONTINUE_KEYWORD;
            case BREAK_STATEMENT:
                return ParserRuleContext.BREAK_KEYWORD;
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
                return ParserRuleContext.SEMICOLON;
            case RETURN_STMT:
                return ParserRuleContext.RETURN_KEYWORD;
            case RETURN_KEYWORD:
                return ParserRuleContext.RETURN_STMT_RHS;
            case ACCESS_EXPRESSION:
                return ParserRuleContext.VARIABLE_REF;
            case MAPPING_FIELD_NAME:
                return ParserRuleContext.SPECIFIC_FIELD_RHS;
            case COLON:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.MAPPING_CONSTRUCTOR) {
                    return ParserRuleContext.EXPRESSION;
                }

                return ParserRuleContext.IDENTIFIER;
            case STRING_LITERAL:
                // We assume string literal is specifically used only in the mapping constructor key.
                return ParserRuleContext.COLON;
            case COMPUTED_FIELD_NAME:
                return ParserRuleContext.OPEN_BRACKET;
            case LISTENERS_LIST:
                return ParserRuleContext.EXPRESSION;
            case ON_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.ANNOTATION_DECL) {
                    return ParserRuleContext.ANNOT_ATTACH_POINTS_LIST;
                }
                return ParserRuleContext.LISTENERS_LIST;
            case RESOURCE_KEYWORD:
                return ParserRuleContext.FUNC_DEF;
            case SERVICE_DECL:
                return ParserRuleContext.SERVICE_KEYWORD;
            case SERVICE_KEYWORD:
                return ParserRuleContext.OPTIONAL_SERVICE_NAME;
            case SERVICE_NAME:
                return ParserRuleContext.ON_KEYWORD;
            case LISTENER_KEYWORD:
                return ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER;
            case LISTENER_DECL:
                return ParserRuleContext.LISTENER_KEYWORD;
            case FINAL_KEYWORD:
                // Assume the final keyword is only used in var-decl.
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case CONSTANT_DECL:
                return ParserRuleContext.CONST_KEYWORD;
            case CONST_KEYWORD:
                return ParserRuleContext.CONST_DECL_TYPE;
            case CONST_DECL_TYPE:
                return ParserRuleContext.CONST_DECL_RHS;
            case NIL_TYPE_DESCRIPTOR:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case TYPEOF_EXPRESSION:
                return ParserRuleContext.TYPEOF_KEYWORD;
            case TYPEOF_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case OPTIONAL_TYPE_DESCRIPTOR:
                return ParserRuleContext.QUESTION_MARK;
            case UNARY_EXPRESSION:
                return ParserRuleContext.UNARY_OPERATOR;
            case UNARY_OPERATOR:
                return ParserRuleContext.EXPRESSION;
            case ARRAY_TYPE_DESCRIPTOR:
                return ParserRuleContext.OPEN_BRACKET;
            case ARRAY_LENGTH:
                return ParserRuleContext.CLOSE_BRACKET;
            case AT:
                return ParserRuleContext.ANNOT_REFERENCE;
            case DOC_STRING:
                return ParserRuleContext.ANNOTATIONS;
            case ANNOTATIONS:
                return ParserRuleContext.AT;
            case MAPPING_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACE;
            case VARIABLE_REF:
            case TYPE_REFERENCE:
            case ANNOT_REFERENCE:
                return ParserRuleContext.QUALIFIED_IDENTIFIER;
            case QUALIFIED_IDENTIFIER:
                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.COLON_TOKEN) {
                    return ParserRuleContext.COLON;
                }
                // Else this is a simple identifier. Hence fall through.
            case IDENTIFIER:
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case VARIABLE_REF:
                        endContext();
                        return getNextRuleForExpr();
                    case TYPE_REFERENCE:
                        endContext();
                        return ParserRuleContext.SEMICOLON;
                    case ANNOT_REFERENCE:
                        endContext();
                        return ParserRuleContext.MAPPING_CONSTRUCTOR;
                    case ANNOTATION_DECL:
                        return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
                    default:
                        throw new IllegalStateException();
                }
            case IS_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_EXPRESSION;
            case NULL_KEYWORD:
                return ParserRuleContext.EXPRESSION_RHS;
            case NIL_LITERAL:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case LOCAL_TYPE_DEFINITION_STMT:
                return ParserRuleContext.TYPE_KEYWORD;
            case RIGHT_ARROW:
                return ParserRuleContext.EXPRESSION;
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STATEMENT_START_IDENTIFIER:
                return getNextRuleForDecimalIntegerLiteral();
            case EXPRESSION_STATEMENT:
                return ParserRuleContext.EXPRESSION_STATEMENT_START;
            case MAP_KEYWORD:
            case FUTURE_KEYWORD:
            case LOCK_STMT:
                return ParserRuleContext.LOCK_KEYWORD;
            case LOCK_KEYWORD:
                return ParserRuleContext.BLOCK_STMT;
            case RECORD_FIELD:
                return ParserRuleContext.RECORD_FIELD_START;
            case ANNOTATION_TAG:
                return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
            case ANNOTATION_KEYWORD:
                return ParserRuleContext.ANNOT_DECL_OPTIONAL_TYPE;
            case ANNOT_ATTACH_POINTS_LIST:
                return ParserRuleContext.ATTACH_POINT;
            case FIELD_IDENT:
            case FUNCTION_IDENT:
            case IDENT_AFTER_OBJECT_IDENT:
            case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
            case ATTACH_POINT:
                return ParserRuleContext.ATTACH_POINT_END;
            case RECORD_FIELD_OR_RECORD_END:
                return ParserRuleContext.RECORD_BODY_END;
            case SOURCE_KEYWORD:
                return ParserRuleContext.ATTACH_POINT_IDENT;
            case OBJECT_IDENT:
                return ParserRuleContext.IDENT_AFTER_OBJECT_IDENT;
            case RECORD_IDENT:
                return ParserRuleContext.FIELD_IDENT;
            case RESOURCE_IDENT:
                return ParserRuleContext.FUNCTION_IDENT;
            case ANNOTATION_DECL:
                return ParserRuleContext.ANNOTATION_KEYWORD;
            case XML_NAMESPACE_DECLARATION:
                return ParserRuleContext.XMLNS_KEYWORD;
            case XMLNS_KEYWORD:
                return ParserRuleContext.CONSTANT_EXPRESSION;
            case CONSTANT_EXPRESSION:
                return ParserRuleContext.CONSTANT_EXPRESSION_START;
            case XML_NAMESPACE_PREFIX_DECL:
                return ParserRuleContext.SEMICOLON;
            case NAMED_WORKER_DECL:
                return ParserRuleContext.WORKER_KEYWORD;
            case WORKER_KEYWORD:
                return ParserRuleContext.WORKER_NAME;
            case WORKER_NAME:
                return ParserRuleContext.WORKER_NAME_RHS;
            case FORK_STMT:
                return ParserRuleContext.FORK_KEYWORD;
            case FORK_KEYWORD:
                return ParserRuleContext.OPEN_BRACE;
            case TRAP_EXPRESSION:
                return ParserRuleContext.TRAP_KEYWORD;
            case TRAP_KEYWORD:
                return ParserRuleContext.EXPRESSION;
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.OPEN_BRACKET;
            case FOREACH_STMT:
                return ParserRuleContext.FOREACH_KEYWORD;
            case FOREACH_KEYWORD:
                return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
            case IN_KEYWORD:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.LET_VAR_DECL) {
                    endContext(); // end let-var-decl
                }
                return ParserRuleContext.EXPRESSION;
            case TYPE_CAST_EXPRESSION:
                return ParserRuleContext.LT;
            case PIPE:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case TABLE_CONSTRUCTOR:
                return ParserRuleContext.TABLE_KEYWORD;
            case TABLE_KEYWORD:
                return ParserRuleContext.TABLE_KEYWORD_RHS;
            case KEY_SPECIFIER:
                return ParserRuleContext.KEY_KEYWORD;
            case KEY_KEYWORD:
                return ParserRuleContext.OPEN_PARENTHESIS;
            case ERROR_KEYWORD:
                if (isInTypeDescContext()) {
                    return ParserRuleContext.ERROR_TYPE_PARAM_START;
                }
                throw new IllegalStateException();
            case ERROR_TYPE_PARAM_START:
                return ParserRuleContext.ERROR_TYPE_PARAMS;
            case LET_EXPRESSION:
                return ParserRuleContext.LET_KEYWORD;
            case LET_KEYWORD:
                return ParserRuleContext.LET_VAR_DECL;
            case LET_VAR_DECL:
                return ParserRuleContext.LET_VAR_DECL_START;
            case STREAM_KEYWORD:
                return ParserRuleContext.LT;
            case END_OF_TYPE_DESC:
                return getNextRuleForTypeDescriptor();
            case PARAMETERIZED_TYPE:
                return ParserRuleContext.LT;
            case LT:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.TYPE_CAST_EXPRESSION) {
                    return ParserRuleContext.TYPE_CAST_PARAM;
                }
                return ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS;
            case GT:
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC) {
                    // Since type-desc in a stream-type can have alternate endings,
                    // we haven't end the context. So if its '>', then end the ctx here.
                    endContext();
                    return ParserRuleContext.TYPEDESC_RHS;
                }

                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }

                // Type cast expression:
                endContext();
                return ParserRuleContext.EXPRESSION;
            case TEMPLATE_END:
                return ParserRuleContext.EXPRESSION_RHS;
            case TEMPLATE_START:
                return ParserRuleContext.TEMPLATE_BODY;
            case TEMPLATE_BODY:
                return ParserRuleContext.TEMPLATE_MEMBER;
            case TEMPLATE_STRING:
                return ParserRuleContext.TEMPLATE_STRING_RHS;
            case INTERPOLATION_START_TOKEN:
                return ParserRuleContext.EXPRESSION;
            case XML_KEYWORD:
            case STRING_KEYWORD:
                return ParserRuleContext.TEMPLATE_START;
            case ARG_LIST_START:
                return ParserRuleContext.ARG_LIST;
            case ARG_LIST_END:
                endContext();
                return ParserRuleContext.EXPRESSION_RHS;
            case ARG_LIST:
                return ParserRuleContext.ARG_START_OR_ARG_LIST_END;
            case TYPE_DESC_IN_ANNOTATION_DECL:
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_PARAM:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
            case TYPE_DESC_IN_TYPE_DEF:
            case TYPE_DESC_IN_ANGLE_BRACKETS:
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
            case TYPE_DESC_IN_EXPRESSION:
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            case VAR_DECL_STARTED_WITH_DENTIFIER:
                // We come here trying to recover statement started with identifier,
                // and trying to match it against a var-decl. Since this wasn't a var-decl
                // originally, a context for type hasn't started yet. Therefore start a
                // a context manually here.
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                return ParserRuleContext.TYPEDESC_RHS;
            case INFERRED_TYPE_DESC:
                return ParserRuleContext.GT;

            case FUNC_TYPE_OR_DEF_SIGNATURE_RHS:
            case OBJECT_FUNC_OR_FIELD:
            case OBJECT_METHOD_START:
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
            case OBJECT_FIELD_RHS:
            case PARAM_LIST:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case ASSIGNMENT_OR_VAR_DECL_STMT_RHS:
            case BOOLEAN_LITERAL:
            case CALL_STMT_START:
            case ELSE_BLOCK:
            case ELSE_BODY:
            case FIELD_DESCRIPTOR_RHS:
            case FIELD_OR_REST_DESCIPTOR_RHS:
            case IMPORT_PREFIX_DECL:
            case NAMED_OR_POSITIONAL_ARG_RHS:
            case OBJECT_MEMBER:
            case OBJECT_TYPE_DESCRIPTOR_START:
            case RECORD_BODY_END:
            case RECORD_BODY_START:
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
            case TYPE_DESCRIPTOR:
            case VAR_DECL_STMT_RHS:
            case AFTER_IMPORT_MODULE_NAME:
            case IMPORT_DECL_RHS:
            case IMPORT_VERSION_DECL:
            case MAJOR_MINOR_VERSION_END:
            case MAPPING_FIELD:
            case SPECIFIC_FIELD_RHS:
            case RETURN_STMT_RHS:
            case OPTIONAL_SERVICE_NAME:
            case RESOURCE_DEF:
            case CONST_DECL_RHS:
            case OBJECT_MEMBER_WITHOUT_METADATA:
            case TOP_LEVEL_NODE:
            case PARAMETER_START:
            case PARAMETER_WITHOUT_ANNOTS:
            case RECORD_FIELD_WITHOUT_METADATA:
            case STMT_START_WITH_IDENTIFIER:
            case STMT_START_WITH_EXPR_RHS:
            case EXPRESSION_STATEMENT_START:
            case RECORD_FIELD_START:
            case ANNOT_DECL_OPTIONAL_TYPE:
            case ANNOT_DECL_RHS:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
            case ATTACH_POINT_IDENT:
            case ATTACH_POINT_END:
            case CONSTANT_EXPRESSION_START:
            case DEFAULT_WORKER:
            case DEFAULT_WORKER_INIT:
            case NAMED_WORKERS:
            case LIST_CONSTRUCTOR_RHS:
            case TYPE_CAST_PARAM:
            case TYPE_CAST_PARAM_RHS:
            case TABLE_KEYWORD_RHS:
            case ROW_LIST_RHS:
            case TABLE_ROW_END:
            case KEY_SPECIFIER_RHS:
            case TABLE_KEY_RHS:
            case LET_VAR_DECL_START:
            default:
                throw new IllegalStateException("cannot find the next rule for: " + currentCtx);
        }
    }

    private ParserRuleContext getNextRuleForOpenParenthesis() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.EXPRESSION_STATEMENT) {
            return ParserRuleContext.EXPRESSION_STATEMENT_START;
        } else if (isExpressionContext(parentCtx) || parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
            return ParserRuleContext.EXPRESSION;
        } else if (parentCtx == ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE ||
                parentCtx == ParserRuleContext.FUNC_TYPE_DESC || parentCtx == ParserRuleContext.FUNC_DEF ||
                parentCtx == ParserRuleContext.ANON_FUNC_EXPRESSION) {
            // TODO: find a better way
            startContext(ParserRuleContext.PARAM_LIST);
            return ParserRuleContext.PARAM_LIST;
        } else if (parentCtx == ParserRuleContext.NIL_TYPE_DESCRIPTOR || parentCtx == ParserRuleContext.NIL_LITERAL) {
            return ParserRuleContext.CLOSE_PARENTHESIS;
        } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
            return ParserRuleContext.KEY_SPECIFIER_RHS;
        } else if (isParameter(parentCtx)) {
            return ParserRuleContext.EXPRESSION;
        }
        return ParserRuleContext.EXPRESSION;
    }

    private ParserRuleContext getNextRuleForOpenBrace(int nextLookahead) {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.LISTENERS_LIST) {
            endContext();
        }

        if (isEndOfBlock(this.tokenReader.peek(nextLookahead))) {
            return ParserRuleContext.CLOSE_BRACE;
        }

        if (parentCtx == ParserRuleContext.MAPPING_CONSTRUCTOR) {
            return ParserRuleContext.MAPPING_FIELD;
        }

        if (parentCtx == ParserRuleContext.FORK_STMT) {
            return ParserRuleContext.NAMED_WORKER_DECL;
        }

        return ParserRuleContext.STATEMENT;
    }

    private boolean isExpressionContext(ParserRuleContext ctx) {
        switch (ctx) {
            case LISTENERS_LIST:
            case MAPPING_CONSTRUCTOR:
            case COMPUTED_FIELD_NAME:
            case LIST_CONSTRUCTOR:
            case INTERPOLATION:
            case ARG_LIST:
            case LET_VAR_DECL:
            case TABLE_CONSTRUCTOR:
                return true;
            default:
                return isStatement(ctx);
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#AFTER_PARAMETER_TYPE}.
     * 
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForParamType() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.REQUIRED_PARAM || parentCtx == ParserRuleContext.DEFAULTABLE_PARAM) {
            return ParserRuleContext.VARIABLE_NAME;
        } else if (parentCtx == ParserRuleContext.REST_PARAM) {
            return ParserRuleContext.ELLIPSIS;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#COMMA}.
     * 
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForComma() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case PARAM_LIST:
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                endContext();
                return parentCtx;
            case ARG_LIST:
                return ParserRuleContext.ARG_START;
            case MAPPING_CONSTRUCTOR:
                return ParserRuleContext.MAPPING_FIELD;
            case LISTENERS_LIST:
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.EXPRESSION;
            case ANNOT_ATTACH_POINTS_LIST:
                return ParserRuleContext.ATTACH_POINT;
            case TABLE_CONSTRUCTOR:
                return ParserRuleContext.MAPPING_CONSTRUCTOR;
            case KEY_SPECIFIER:
                return ParserRuleContext.VARIABLE_NAME;
            case LET_VAR_DECL:
                return ParserRuleContext.LET_VAR_DECL_START;
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                return ParserRuleContext.TYPE_DESCRIPTOR;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Get the next parser context to visit after a type descriptor.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForTypeDescriptor() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            // Contexts that expect a type
            case TYPE_DESC_IN_ANNOTATION_DECL:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.ANNOTATION_TAG;
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN: // TODO: Update this once the typed-binding-patterns added.
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.VARIABLE_NAME;
            case TYPE_DESC_IN_PARAM:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.AFTER_PARAMETER_TYPE;
            case TYPE_DESC_IN_TYPE_DEF:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.SEMICOLON;
            case TYPE_DESC_IN_ANGLE_BRACKETS:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }
                return ParserRuleContext.GT;
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
                endContext();
                if (isInTypeDescContext()) {
                    return ParserRuleContext.TYPEDESC_RHS;
                }

                parentCtx = getParentContext();
                switch (parentCtx) {
                    case FUNC_TYPE_DESC:
                    case FUNC_DEF_OR_FUNC_TYPE:
                        return ParserRuleContext.FUNC_TYPE_OR_DEF_SIGNATURE_RHS;
                    case FUNC_DEF:
                        endContext(); // End function-signature
                        return ParserRuleContext.FUNC_BODY;
                    case ANON_FUNC_EXPRESSION:
                        return ParserRuleContext.ANNON_FUNC_BODY;
                    case NAMED_WORKER_DECL:
                        return ParserRuleContext.BLOCK_STMT;
                    default:
                        throw new IllegalStateException(parentCtx.toString());
                }
            case TYPE_DESC_IN_EXPRESSION:
                endContext();
                return ParserRuleContext.EXPRESSION_RHS;
            case COMP_UNIT:
                /*
                 * Fact 1:
                 * ------
                 * FUNC_DEF_OR_FUNC_TYPE is only possible for module level construct or object member
                 * that starts with 'function' keyword. However, until the end of func-signature,
                 * we don't know whether this is a func-def or a function type.
                 * Hence a var-decl-stmt context is not started until this point.
                 *
                 * Fact 2:
                 * ------
                 * We reach here for END_OF_TYPE_DESC context. That means we are going to end the
                 * func-type-desc.
                 */
                startContext(ParserRuleContext.VAR_DECL_STMT);
                return ParserRuleContext.VARIABLE_NAME; // TODO add typed-binding-patters
            case OBJECT_MEMBER:
                return ParserRuleContext.VARIABLE_NAME;
            case ANNOTATION_DECL:
                return ParserRuleContext.IDENTIFIER;
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                return ParserRuleContext.STREAM_TYPE_FIRST_PARAM_RHS;
            default:
                // If none of the above that means we reach here via, anonymous-func-or-func-type context.
                // Then the rhs of this is definitely an expression-rhs
                return ParserRuleContext.EXPRESSION_RHS;
        }
    }

    private boolean isInTypeDescContext() {
        switch (getParentContext()) {
            case TYPE_DESC_IN_ANNOTATION_DECL:
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_PARAM:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
            case TYPE_DESC_IN_TYPE_DEF:
            case TYPE_DESC_IN_ANGLE_BRACKETS:
            case TYPE_DESC_IN_RETURN_TYPE_DESC:
            case TYPE_DESC_IN_EXPRESSION:
            case TYPE_DESC_IN_STREAM_TYPE_DESC:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#ASSIGN_OP}.
     * 
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForEqualOp() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case EXTERNAL_FUNC_BODY:
                return ParserRuleContext.EXTERNAL_KEYWORD;
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case RECORD_FIELD:
            case ARG_LIST:
            case OBJECT_MEMBER:
            case LISTENER_DECL:
            case CONSTANT_DECL:
            case LET_VAR_DECL:
                return ParserRuleContext.EXPRESSION;
            default:
                if (parentCtx == ParserRuleContext.STMT_START_WITH_IDENTIFIER) {
                    switchContext(ParserRuleContext.ASSIGNMENT_OR_VAR_DECL_STMT);
                    return ParserRuleContext.EXPRESSION;
                }

                if (isStatement(parentCtx)) {
                    return ParserRuleContext.EXPRESSION;
                }
                throw new IllegalStateException("equal op cannot exist in a " + parentCtx);
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#CLOSE_BRACE}.
     *
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForCloseBrace(int nextLookahead) {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case FUNC_BODY_BLOCK:
                endContext(); // end body block
                STToken nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                    return ParserRuleContext.EOF;
                }

                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.SERVICE_DECL) {
                    return ParserRuleContext.RESOURCE_DEF;
                } else if (parentCtx == ParserRuleContext.OBJECT_TYPE_DESCRIPTOR) {
                    return ParserRuleContext.OBJECT_MEMBER;
                } else if (isExpressionContext(parentCtx)) {
                    // Annonynous func
                    return ParserRuleContext.EXPRESSION_RHS;
                }

                return ParserRuleContext.TOP_LEVEL_NODE;
            case SERVICE_DECL:
                endContext();
                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                    return ParserRuleContext.EOF;
                }
                return ParserRuleContext.TOP_LEVEL_NODE;
            case OBJECT_MEMBER:
                endContext(); // end object member
                // fall through
            case RECORD_TYPE_DESCRIPTOR:
            case OBJECT_TYPE_DESCRIPTOR:
                endContext(); // end record/object type def
                return ParserRuleContext.TYPEDESC_RHS;
            case BLOCK_STMT:
                endContext(); // end block stmt
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.IF_BLOCK) {
                    endContext(); // end if-block
                    return ParserRuleContext.ELSE_BLOCK;
                } else if (parentCtx == ParserRuleContext.WHILE_BLOCK) {
                    endContext(); // end while-block
                    return ParserRuleContext.STATEMENT;
                } else if (parentCtx == ParserRuleContext.NAMED_WORKER_DECL) {
                    endContext(); // end named-worker
                    parentCtx = getParentContext();
                    if (parentCtx == ParserRuleContext.FORK_STMT) {
                        nextToken = this.tokenReader.peek(nextLookahead);
                        switch (nextToken.kind) {
                            case CLOSE_BRACE_TOKEN:
                                return ParserRuleContext.CLOSE_BRACE;
                            default:
                                return ParserRuleContext.STATEMENT;
                        }
                    } else {
                        return ParserRuleContext.STATEMENT;
                    }
                } else if (parentCtx == ParserRuleContext.LOCK_STMT) {
                    endContext();
                    return ParserRuleContext.STATEMENT;
                } else if (parentCtx == ParserRuleContext.FOREACH_STMT) {
                    endContext();
                    return ParserRuleContext.STATEMENT;
                }
                return ParserRuleContext.STATEMENT;
            case MAPPING_CONSTRUCTOR:
                endContext(); // end mapping constructor
                parentCtx = getParentContext();
                if (parentCtx == ParserRuleContext.TABLE_CONSTRUCTOR) {
                    return ParserRuleContext.TABLE_ROW_END;
                }

                if (parentCtx != ParserRuleContext.ANNOTATIONS) {
                    return getNextRuleForExpr();
                }

                nextToken = this.tokenReader.peek(nextLookahead);
                if (nextToken.kind == SyntaxKind.AT_TOKEN) {
                    return ParserRuleContext.AT;
                }

                endContext(); // end annotations
                parentCtx = getParentContext();
                switch (parentCtx) {
                    case COMP_UNIT:
                        return ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_METADATA;
                    case FUNC_DEF:
                    case FUNC_TYPE_DESC:
                    case FUNC_DEF_OR_FUNC_TYPE:
                    case ANON_FUNC_EXPRESSION:
                        return ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC;
                    case LET_VAR_DECL:
                        return ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN;
                    case RECORD_FIELD:
                        return ParserRuleContext.RECORD_FIELD_WITHOUT_METADATA;
                    case OBJECT_MEMBER:
                        return ParserRuleContext.OBJECT_MEMBER_WITHOUT_METADATA;
                    case SERVICE_DECL:
                        return ParserRuleContext.RESOURCE_DEF;
                    case FUNC_BODY_BLOCK:
                        return ParserRuleContext.STATEMENT_WITHOUT_ANNOTS;
                    case EXTERNAL_FUNC_BODY:
                        return ParserRuleContext.EXTERNAL_KEYWORD;
                    case TYPE_CAST_EXPRESSION:
                        return ParserRuleContext.TYPE_CAST_PARAM_RHS;
                    default:
                        if (isParameter(parentCtx)) {
                            return ParserRuleContext.REQUIRED_PARAM;
                        }
                        throw new IllegalStateException("annotation is ending inside a " + parentCtx);
                }
            case FORK_STMT:
                endContext(); // end fork-statement
                return ParserRuleContext.STATEMENT;
            case INTERPOLATION:
                endContext();
                return ParserRuleContext.TEMPLATE_MEMBER;
            default:
                throw new IllegalStateException("found close-brace in: " + parentCtx);
        }
    }

    /**
     * Get the next parser context to visit after a variable/parameter name.
     * 
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForVarName(int nextLookahead) {
        STToken nextToken = this.tokenReader.peek(nextLookahead);
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.REQUIRED_PARAM) {
            if (isEndOfParametersList(nextToken)) {
                return ParserRuleContext.CLOSE_PARENTHESIS;
            } else if (isEndOfParameter(nextToken)) {
                return ParserRuleContext.COMMA;
            } else {
                // Currently processing a required param, but now switch
                // to a defaultable param
                switchContext(ParserRuleContext.DEFAULTABLE_PARAM);
                if (isCompoundBinaryOperator(nextToken.kind)) {
                    return ParserRuleContext.COMPOUND_BINARY_OPERATOR;
                } else {
                    return ParserRuleContext.ASSIGN_OP;
                }
            }
        } else if (parentCtx == ParserRuleContext.DEFAULTABLE_PARAM) {
            if (isEndOfParametersList(nextToken)) {
                return ParserRuleContext.CLOSE_PARENTHESIS;
            } else {
                return ParserRuleContext.ASSIGN_OP;
            }
        } else if (parentCtx == ParserRuleContext.FOREACH_STMT) {
            return ParserRuleContext.IN_KEYWORD;
        } else if (isStatement(parentCtx) || parentCtx == ParserRuleContext.LISTENER_DECL ||
                parentCtx == ParserRuleContext.CONSTANT_DECL) {
            return ParserRuleContext.VAR_DECL_STMT_RHS;
        } else if (parentCtx == ParserRuleContext.RECORD_FIELD) {
            return ParserRuleContext.FIELD_DESCRIPTOR_RHS;
        } else if (parentCtx == ParserRuleContext.ARG_LIST) {
            return ParserRuleContext.NAMED_OR_POSITIONAL_ARG_RHS;
        } else if (parentCtx == ParserRuleContext.OBJECT_MEMBER) {
            return ParserRuleContext.OBJECT_FIELD_RHS;
        } else if (parentCtx == ParserRuleContext.ARRAY_TYPE_DESCRIPTOR) {
            return ParserRuleContext.CLOSE_BRACKET;
        } else if (parentCtx == ParserRuleContext.KEY_SPECIFIER) {
            return ParserRuleContext.TABLE_KEY_RHS;
        } else if (parentCtx == ParserRuleContext.LET_VAR_DECL) {
            return ParserRuleContext.ASSIGN_OP;
        } else if (parentCtx == ParserRuleContext.ANNOTATION_DECL) {
            return ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS;
        } else if (isParameter(parentCtx)) {
            return ParserRuleContext.PARAMETER_NAME_RHS;
        } else {
            throw new IllegalStateException(parentCtx.toString());
        }
    }

    /**
     * Check whether the given token kind is a compound binary operator.
     * 
     * @param kind STToken kind
     * @return <code>true</code> if the token kind refers to a binary operator. <code>false</code> otherwise
     */
    private boolean isCompoundBinaryOperator(SyntaxKind kind) {
        switch (kind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case SLASH_TOKEN:
            case ASTERISK_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#SEMICOLON}.
     *
     * @param nextLookahead Position of the next token to consider, relative to the position of the original error
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForSemicolon(int nextLookahead) {
        STToken nextToken;
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.EXTERNAL_FUNC_BODY) {
            endContext(); // end external func-body
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (isExpression(parentCtx)) {
            // A semicolon after an expression also means its an end of a statement/field, Hence pop the ctx.
            endContext(); // end statement
            if (isEndOfBlock(this.tokenReader.peek(nextLookahead))) {
                return ParserRuleContext.CLOSE_BRACE;
            }
            return ParserRuleContext.STATEMENT;
        } else if (parentCtx == ParserRuleContext.VAR_DECL_STMT) {
            endContext(); // end var-decl
            parentCtx = getParentContext();
            if (parentCtx == ParserRuleContext.COMP_UNIT) {
                return ParserRuleContext.TOP_LEVEL_NODE;
            }
            return ParserRuleContext.STATEMENT;
        } else if (isStatement(parentCtx)) {
            endContext(); // end statement
            if (isEndOfBlock(this.tokenReader.peek(nextLookahead))) {
                return ParserRuleContext.CLOSE_BRACE;
            }
            return ParserRuleContext.STATEMENT;
        } else if (parentCtx == ParserRuleContext.RECORD_FIELD) {
            endContext(); // end record field
            if (isEndOfBlock(this.tokenReader.peek(nextLookahead))) {
                return ParserRuleContext.RECORD_BODY_END;
            }
            return ParserRuleContext.RECORD_FIELD_OR_RECORD_END;
        } else if (parentCtx == ParserRuleContext.MODULE_TYPE_DEFINITION ||
                parentCtx == ParserRuleContext.LISTENER_DECL || parentCtx == ParserRuleContext.CONSTANT_DECL ||
                parentCtx == ParserRuleContext.ANNOTATION_DECL ||
                parentCtx == ParserRuleContext.XML_NAMESPACE_DECLARATION) {
            endContext(); // end declaration
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.OBJECT_MEMBER) {
            if (isEndOfObjectTypeNode(nextLookahead)) {
                endContext(); // end object member
                return ParserRuleContext.CLOSE_BRACE;
            }
            return ParserRuleContext.OBJECT_MEMBER;
        } else if (parentCtx == ParserRuleContext.IMPORT_DECL) {
            endContext(); // end object member
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else if (parentCtx == ParserRuleContext.ANNOT_ATTACH_POINTS_LIST) {
            endContext(); // end annot attach points list
            endContext(); // end annot declaration
            nextToken = this.tokenReader.peek(nextLookahead);
            if (nextToken.kind == SyntaxKind.EOF_TOKEN) {
                return ParserRuleContext.EOF;
            }
            return ParserRuleContext.TOP_LEVEL_NODE;
        } else {
            throw new IllegalStateException();
        }
    }

    private ParserRuleContext getNextRuleForDot() {
        ParserRuleContext parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.IMPORT_DECL) {
            return ParserRuleContext.IMPORT_MODULE_NAME;
        }
        return ParserRuleContext.FIELD_OR_FUNC_NAME;
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#QUESTION_MARK}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForQuestionMark() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case OPTIONAL_TYPE_DESCRIPTOR:
                endContext();
                return ParserRuleContext.TYPEDESC_RHS;
            default:
                return ParserRuleContext.SEMICOLON;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#OPEN_BRACKET}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForOpenBracket() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case ARRAY_TYPE_DESCRIPTOR:
                return ParserRuleContext.ARRAY_LENGTH;
            case LIST_CONSTRUCTOR:
                return ParserRuleContext.LIST_CONSTRUCTOR_RHS;
            case TABLE_CONSTRUCTOR:
                return ParserRuleContext.ROW_LIST_RHS;
            default:
                return ParserRuleContext.EXPRESSION;
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#CLOSE_BRACKET}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForCloseBracket() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case ARRAY_TYPE_DESCRIPTOR:
                endContext(); // End array type descriptor context
                return ParserRuleContext.TYPEDESC_RHS;
            case COMPUTED_FIELD_NAME:
                endContext(); // end computed-field-name
                return ParserRuleContext.COLON;
            case LIST_CONSTRUCTOR:
            case TABLE_CONSTRUCTOR:
                endContext();
                return getNextRuleForExpr();
            default:
                return getNextRuleForExpr();
        }
    }

    /**
     * Get the next parser context to visit after a {@link ParserRuleContext#DECIMAL_INTEGER_LITERAL}.
     *
     * @return Next parser context
     */
    private ParserRuleContext getNextRuleForDecimalIntegerLiteral() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case ARRAY_TYPE_DESCRIPTOR:
            default:
                return ParserRuleContext.CLOSE_BRACKET;
        }
    }

    private ParserRuleContext getNextRuleForExpr() {
        ParserRuleContext parentCtx;
        parentCtx = getParentContext();
        if (parentCtx == ParserRuleContext.CONSTANT_EXPRESSION) {
            endContext();
            return getNextRuleForConstExpr();
        }
        return ParserRuleContext.EXPRESSION_RHS;
    }

    private ParserRuleContext getNextRuleForConstExpr() {
        ParserRuleContext parentCtx = getParentContext();
        switch (parentCtx) {
            case XML_NAMESPACE_DECLARATION:
                return ParserRuleContext.XML_NAMESPACE_PREFIX_DECL;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Check whether the given context is a statement.
     * 
     * @param ctx Parser context to check
     * @return <code>true</code> if the given context is a statement. <code>false</code> otherwise
     */
    private boolean isStatement(ParserRuleContext parentCtx) {
        switch (parentCtx) {
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case VAR_DECL_STMT:
            case ASSIGNMENT_STMT:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case IF_BLOCK:
            case BLOCK_STMT:
            case WHILE_BLOCK:
            case CALL_STMT:
            case PANIC_STMT:
            case CONTINUE_STATEMENT:
            case BREAK_STATEMENT:
            case RETURN_STMT:
            case COMPOUND_ASSIGNMENT_STMT:
            case LOCAL_TYPE_DEFINITION_STMT:
            case STMT_START_WITH_IDENTIFIER:
            case EXPRESSION_STATEMENT:
            case LOCK_STMT:
            case FORK_STMT:
            case FOREACH_STMT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given context is an expression.
     * 
     * @param ctx Parser context to check
     * @return <code>true</code> if the given context is an expression. <code>false</code> otherwise
     */
    private boolean isExpression(ParserRuleContext ctx) {
        return ctx == ParserRuleContext.EXPRESSION || ctx == ParserRuleContext.TERMINAL_EXPRESSION;
    }

    /**
     * Check whether the given token refers to a binary operator.
     * 
     * @param token Token to check
     * @return <code>true</code> if the given token refers to a binary operator. <code>false</code> otherwise
     */
    private boolean isBinaryOperator(STToken token) {
        switch (token.kind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case SLASH_TOKEN:
            case ASTERISK_TOKEN:
            case GT_TOKEN:
            case LT_TOKEN:
            case DOUBLE_EQUAL_TOKEN:
            case TRIPPLE_EQUAL_TOKEN:
            case LT_EQUAL_TOKEN:
            case GT_EQUAL_TOKEN:
            case NOT_EQUAL_TOKEN:
            case NOT_DOUBLE_EQUAL_TOKEN:
            case BITWISE_AND_TOKEN:
            case BITWISE_XOR_TOKEN:
            case PIPE_TOKEN:
            case LOGICAL_AND_TOKEN:
            case LOGICAL_OR_TOKEN:
                return true;

            // Treat these also as binary operators.
            case RIGHT_ARROW_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isParameter(ParserRuleContext ctx) {
        switch (ctx) {
            case REQUIRED_PARAM:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
            case PARAM_LIST:
                return true;
            default:
                return false;
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
            case ASSIGN_OP:
                return SyntaxKind.EQUAL_TOKEN;
            case BINARY_OPERATOR:
                return SyntaxKind.PLUS_TOKEN;
            case CLOSE_BRACE:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case CLOSE_PARENTHESIS:
                return SyntaxKind.CLOSE_PAREN_TOKEN;
            case COMMA:
                return SyntaxKind.COMMA_TOKEN;
            case EXTERNAL_KEYWORD:
                return SyntaxKind.EXTERNAL_KEYWORD;
            case FUNCTION_KEYWORD:
                return SyntaxKind.FUNCTION_KEYWORD;
            case FUNC_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case OPEN_BRACE:
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case OPEN_PARENTHESIS:
            case ARG_LIST_START:
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case RETURNS_KEYWORD:
                return SyntaxKind.RETURNS_KEYWORD;
            case SEMICOLON:
                return SyntaxKind.SEMICOLON_TOKEN;
            case VARIABLE_NAME:
            case STATEMENT_START_IDENTIFIER:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case PUBLIC_KEYWORD:
                return SyntaxKind.PUBLIC_KEYWORD;
            case ASSIGNMENT_STMT:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case EXPRESSION_RHS:
                return SyntaxKind.PLUS_TOKEN;
            case EXPRESSION:
            case TERMINAL_EXPRESSION:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case EXTERNAL_FUNC_BODY:
                return SyntaxKind.EQUAL_TOKEN;
            case FUNC_TYPE_OR_DEF_SIGNATURE_RHS:
            case FUNC_BODY_BLOCK:
                return SyntaxKind.OPEN_BRACE_TOKEN;
            case FUNC_DEF:
            case FUNC_DEF_OR_FUNC_TYPE:
            case FUNC_TYPE_DESC:
                return SyntaxKind.FUNCTION_KEYWORD;
            case VAR_DECL_STMT_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case SIMPLE_TYPE_DESCRIPTOR:
            case REQUIRED_PARAM:
            case VAR_DECL_STMT:
            case ASSIGNMENT_OR_VAR_DECL_STMT:
            case DEFAULTABLE_PARAM:
            case REST_PARAM:
                return SyntaxKind.TYPE_DESC;
            case ASTERISK:
            case INFERRED_TYPE_DESC:
                return SyntaxKind.ASTERISK_TOKEN;
            case CLOSED_RECORD_BODY_END:
                return SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
            case CLOSED_RECORD_BODY_START:
                return SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
            case ELLIPSIS:
                return SyntaxKind.ELLIPSIS_TOKEN;
            case QUESTION_MARK:
                return SyntaxKind.QUESTION_MARK_TOKEN;
            case RECORD_BODY_START:
                return SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
            case RECORD_FIELD:
            case RECORD_KEYWORD:
                return SyntaxKind.RECORD_KEYWORD;
            case TYPE_KEYWORD:
                return SyntaxKind.TYPE_KEYWORD;
            case TYPE_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case TYPE_REFERENCE:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case RECORD_BODY_END:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case OBJECT_KEYWORD:
                return SyntaxKind.OBJECT_KEYWORD;
            case PRIVATE_KEYWORD:
                return SyntaxKind.PRIVATE_KEYWORD;
            case REMOTE_KEYWORD:
                return SyntaxKind.REMOTE_KEYWORD;
            case OBJECT_FIELD_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case ABSTRACT_KEYWORD:
                return SyntaxKind.ABSTRACT_KEYWORD;
            case CLIENT_KEYWORD:
                return SyntaxKind.CLIENT_KEYWORD;
            case OBJECT_TYPE_FIRST_QUALIFIER:
            case OBJECT_TYPE_SECOND_QUALIFIER:
                return SyntaxKind.OBJECT_KEYWORD;
            case CLOSE_BRACKET:
                return SyntaxKind.CLOSE_BRACKET_TOKEN;
            case DOT:
                return SyntaxKind.DOT_TOKEN;
            case FIELD_OR_FUNC_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case OPEN_BRACKET:
                return SyntaxKind.OPEN_BRACKET_TOKEN;
            case IF_KEYWORD:
                return SyntaxKind.IF_KEYWORD;
            case ELSE_KEYWORD:
                return SyntaxKind.ELSE_KEYWORD;
            case WHILE_KEYWORD:
                return SyntaxKind.WHILE_KEYWORD;
            case CHECKING_KEYWORD:
                return SyntaxKind.CHECK_KEYWORD;
            case AS_KEYWORD:
                return SyntaxKind.AS_KEYWORD;
            case BOOLEAN_LITERAL:
                return SyntaxKind.TRUE_KEYWORD;
            case IMPORT_KEYWORD:
                return SyntaxKind.IMPORT_KEYWORD;
            case IMPORT_MODULE_NAME:
            case IMPORT_ORG_OR_MODULE_NAME:
            case IMPORT_PREFIX:
            case VARIABLE_REF:
            case BASIC_LITERAL: // return var-ref for any kind of terminal expression
            case SERVICE_NAME:
            case IDENTIFIER:
            case QUALIFIED_IDENTIFIER:
            case NAMESPACE_PREFIX:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case VERSION_NUMBER:
            case MAJOR_VERSION:
            case MINOR_VERSION:
            case PATCH_VERSION:
                return SyntaxKind.DECIMAL_INTEGER_LITERAL;
            case SLASH:
                return SyntaxKind.SLASH_TOKEN;
            case VERSION_KEYWORD:
                return SyntaxKind.VERSION_KEYWORD;
            case IMPORT_DECL_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case IMPORT_SUB_VERSION:
                return SyntaxKind.SEMICOLON_TOKEN;
            case COLON:
                return SyntaxKind.COLON_TOKEN;
            case MAPPING_FIELD_NAME:
            case MAPPING_FIELD:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case PANIC_KEYWORD:
                return SyntaxKind.PANIC_KEYWORD;
            case STRING_LITERAL:
                return SyntaxKind.STRING_LITERAL;
            case ON_KEYWORD:
                return SyntaxKind.ON_KEYWORD;
            case RESOURCE_KEYWORD:
                return SyntaxKind.RESOURCE_KEYWORD;
            case RETURN_KEYWORD:
                return SyntaxKind.RETURN_KEYWORD;
            case SERVICE_KEYWORD:
                return SyntaxKind.SERVICE_KEYWORD;
            case BREAK_KEYWORD:
                return SyntaxKind.BREAK_KEYWORD;
            case LISTENER_KEYWORD:
                return SyntaxKind.CONST_KEYWORD;
            case CONTINUE_KEYWORD:
                return SyntaxKind.CONTINUE_KEYWORD;
            case CONST_KEYWORD:
                return SyntaxKind.CONST_KEYWORD;
            case FINAL_KEYWORD:
                return SyntaxKind.FINAL_KEYWORD;
            case CONST_DECL_TYPE:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case NIL_TYPE_DESCRIPTOR:
                return SyntaxKind.NIL_TYPE_DESC;
            case TYPEOF_KEYWORD:
                return SyntaxKind.TYPEOF_KEYWORD;
            case OPTIONAL_TYPE_DESCRIPTOR:
                return SyntaxKind.OPTIONAL_TYPE_DESC;
            case UNARY_OPERATOR:
                return SyntaxKind.PLUS_TOKEN;
            case ARRAY_TYPE_DESCRIPTOR:
                return SyntaxKind.ARRAY_TYPE_DESC;
            case AT:
                return SyntaxKind.AT_TOKEN;
            case FIELD_DESCRIPTOR_RHS:
                return SyntaxKind.SEMICOLON_TOKEN;
            case AFTER_PARAMETER_TYPE:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case CONST_DECL_RHS:
                return SyntaxKind.EQUAL_TOKEN;
            case IS_KEYWORD:
                return SyntaxKind.IS_KEYWORD;
            case OBJECT_MEMBER_WITHOUT_METADATA:
            case RECORD_FIELD_WITHOUT_METADATA:
            case PARAMETER_WITHOUT_ANNOTS:
            case TYPE_DESCRIPTOR:
                return SyntaxKind.TYPE_DESC;
            case TYPEOF_EXPRESSION:
                return SyntaxKind.TYPEOF_KEYWORD;
            case RIGHT_ARROW:
                return SyntaxKind.RIGHT_ARROW_TOKEN;
            case STMT_START_WITH_EXPR_RHS:
                return SyntaxKind.EQUAL_TOKEN;
            case COMPOUND_BINARY_OPERATOR:
                return SyntaxKind.PLUS_TOKEN;
            case UNARY_EXPRESSION:
                return SyntaxKind.PLUS_TOKEN;
            case MAP_KEYWORD:
                return SyntaxKind.MAP_KEYWORD;
            case FUTURE_KEYWORD:
                return SyntaxKind.FUTURE_KEYWORD;
            case TYPEDESC_KEYWORD:
                return SyntaxKind.TYPEDESC_KEYWORD;
            case GT:
                return SyntaxKind.GT_TOKEN;
            case LT:
                return SyntaxKind.LT_TOKEN;
            case NULL_KEYWORD:
                return SyntaxKind.NULL_KEYWORD;
            case LOCK_KEYWORD:
                return SyntaxKind.LOCK_KEYWORD;
            case ANNOTATION_KEYWORD:
                return SyntaxKind.ANNOTATION_KEYWORD;
            case ANNOT_DECL_OPTIONAL_TYPE:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case ANNOT_DECL_RHS:
                return SyntaxKind.ON_KEYWORD;
            case ARRAY_LENGTH:
                return SyntaxKind.DECIMAL_INTEGER_LITERAL;
            case ATTACH_POINT_IDENT:
            case IDENT_AFTER_OBJECT_IDENT:
            case SINGLE_KEYWORD_ATTACH_POINT_IDENT:
                return SyntaxKind.TYPE_KEYWORD;
            case FIELD_IDENT:
                return SyntaxKind.FIELD_KEYWORD;
            case FUNCTION_IDENT:
                return SyntaxKind.FUNCTION_KEYWORD;
            case HEX_INTEGER_LITERAL:
                return SyntaxKind.HEX_INTEGER_LITERAL;
            case RECORD_FIELD_OR_RECORD_END:
                return SyntaxKind.CLOSE_BRACE_TOKEN;
            case SOURCE_KEYWORD:
                return SyntaxKind.SOURCE_KEYWORD;
            case ATTACH_POINT_END:
                return SyntaxKind.SEMICOLON_TOKEN;
            case CONSTANT_EXPRESSION:
                return SyntaxKind.STRING_LITERAL;
            case CONSTANT_EXPRESSION_START:
            case OBJECT_IDENT:
                return SyntaxKind.OBJECT_KEYWORD;
            case RECORD_IDENT:
                return SyntaxKind.RECORD_KEYWORD;
            case RESOURCE_IDENT:
                return SyntaxKind.RESOURCE_KEYWORD;
            case XMLNS_KEYWORD:
            case XML_NAMESPACE_DECLARATION:
                return SyntaxKind.XMLNS_KEYWORD;
            case XML_NAMESPACE_PREFIX_DECL:
                return SyntaxKind.SEMICOLON_TOKEN;
            case NAMED_WORKER_DECL:
            case WORKER_KEYWORD:
                return SyntaxKind.WORKER_KEYWORD;
            case WORKER_NAME:
            case NAMED_WORKERS:
            case ANNOTATION_TAG:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case NIL_LITERAL:
                return SyntaxKind.OPEN_PAREN_TOKEN;
            case FORK_KEYWORD:
                return SyntaxKind.FORK_KEYWORD;
            case DECIMAL_FLOATING_POINT_LITERAL:
                return SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL;
            case HEX_FLOATING_POINT_LITERAL:
                return SyntaxKind.HEX_FLOATING_POINT_LITERAL;
            case PARAMETERIZED_TYPE:
                return SyntaxKind.MAP_KEYWORD;
            case TRAP_KEYWORD:
                return SyntaxKind.TRAP_KEYWORD;
            case FOREACH_KEYWORD:
                return SyntaxKind.FOREACH_KEYWORD;
            case IN_KEYWORD:
                return SyntaxKind.IN_KEYWORD;
            case PIPE:
                return SyntaxKind.PIPE_TOKEN;
            case TABLE_KEYWORD:
                return SyntaxKind.TABLE_KEYWORD;
            case KEY_KEYWORD:
                return SyntaxKind.KEY_KEYWORD;
            case ERROR_KEYWORD:
                return SyntaxKind.ERROR_KEYWORD;
            case STREAM_KEYWORD:
                return SyntaxKind.STREAM_KEYWORD;
            case LET_KEYWORD:
                return SyntaxKind.LET_KEYWORD;
            case TEMPLATE_END:
            case TEMPLATE_START:
                return SyntaxKind.BACKTICK_TOKEN;
            case LT_TOKEN:
                return SyntaxKind.LT_TOKEN;
            case GT_TOKEN:
                return SyntaxKind.GT_TOKEN;
            case INTERPOLATION_START_TOKEN:
                return SyntaxKind.INTERPOLATION_START_TOKEN;
            case XML_KEYWORD:
                return SyntaxKind.XML_KEYWORD;
            case XML_NAME:
                return SyntaxKind.IDENTIFIER_TOKEN;
            case STRING_KEYWORD:
                return SyntaxKind.STRING_KEYWORD;

            // TODO:
            case COMP_UNIT:
            case TOP_LEVEL_NODE:
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
            case ANNOTATIONS:
            case PARAM_LIST:
            case PARAMETER_NAME_RHS:
            case STATEMENT:
            case STATEMENT_WITHOUT_ANNOTS:
            case FIELD_OR_REST_DESCIPTOR_RHS:
            case MODULE_TYPE_DEFINITION:
            case RECORD_TYPE_DESCRIPTOR:
            case EOF:
            case FUNC_CALL:
            case NAMED_OR_POSITIONAL_ARG_RHS:
            case OBJECT_FUNC_OR_FIELD:
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
            case OBJECT_MEMBER:
            case OBJECT_METHOD_START:
            case OBJECT_TYPE_DESCRIPTOR:
            case OBJECT_TYPE_DESCRIPTOR_START:
            case AFTER_IMPORT_MODULE_NAME:
            case ASSIGNMENT_OR_VAR_DECL_STMT_RHS:
            case BLOCK_STMT:
            case CALL_STMT:
            case CALL_STMT_START:
            case DECIMAL_INTEGER_LITERAL:
            case ELSE_BLOCK:
            case ELSE_BODY:
            case IF_BLOCK:
            case IMPORT_DECL:
            case IMPORT_PREFIX_DECL:
            case MAJOR_MINOR_VERSION_END:
            case WHILE_BLOCK:
            case ACCESS_EXPRESSION:
            case IMPORT_VERSION_DECL:
            case MAPPING_CONSTRUCTOR:
            case PANIC_STMT:
            case SPECIFIC_FIELD_RHS:
            case COMPUTED_FIELD_NAME:
            case LISTENERS_LIST:
            case RESOURCE_DEF:
            case RETURN_STMT:
            case RETURN_STMT_RHS:
            case SERVICE_DECL:
            case OPTIONAL_SERVICE_NAME:
            case BREAK_STATEMENT:
            case CONTINUE_STATEMENT:
            case LISTENER_DECL:
            case CONSTANT_DECL:
            case ANNOT_REFERENCE:
            case DOC_STRING:
            case COMPOUND_ASSIGNMENT_STMT:
            case PARAMETER_START:
            case STMT_START_WITH_IDENTIFIER:
            case LOCAL_TYPE_DEFINITION_STMT:
            case LOCK_STMT:
            case FORK_STMT:
            case ANNOTATION_DECL:
            case ANNOT_ATTACH_POINTS_LIST:
            case ANNOT_OPTIONAL_ATTACH_POINTS:
            case EXPRESSION_STATEMENT:
            case EXPRESSION_STATEMENT_START:
            case RECORD_FIELD_START:
            case ATTACH_POINT:
            case DEFAULT_WORKER:
            case DEFAULT_WORKER_INIT:
            case TRAP_EXPRESSION:
            case LIST_CONSTRUCTOR:
            case FOREACH_STMT:
            case TYPE_CAST_EXPRESSION:
            case TABLE_CONSTRUCTOR:
            case KEY_SPECIFIER:
            case LET_VAR_DECL:
            case LET_EXPRESSION:
            default:
                break;
        }

        return SyntaxKind.NONE;
    }

    /**
     * Check whether a token kind is a basic literal.
     * 
     * @param kind Token kind to check
     * @return <code>true</code> if the given token kind belongs to a basic literal.<code>false</code> otherwise
     */
    private boolean isBasicLiteral(SyntaxKind kind) {
        switch (kind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
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
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isSingleKeywordAttachPointIdent(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case ANNOTATION_KEYWORD:
            case EXTERNAL_KEYWORD:
            case VAR_KEYWORD:
            case CONST_KEYWORD:
            case LISTENER_KEYWORD:
            case WORKER_KEYWORD:
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token is a parameterized type keyword.
     *
     * @param tokenKind Token to check
     * @return <code>true</code> if the given token is a parameterized type keyword. <code>false</code> otherwise
     */
    public boolean isParameterizedTypeToken(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case MAP_KEYWORD:
            case FUTURE_KEYWORD:
            case TYPEDESC_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    public ParserRuleContext findBestPath(ParserRuleContext context) {
        // We reach here to break ambiguity. Hence increase the lookahead limit
        // to get better results. Since this is an erroneous scenario, the overhead
        // of increasing the lookahead is acceptable.
        int prevLookahead = lookaheadLimit;
        lookaheadLimit = (int) (lookaheadLimit * 1.5);
        ParserRuleContext[] alternatives;
        switch (context) {
            case STATEMENT:
                alternatives = STATEMENTS;
                break;
            case TOP_LEVEL_NODE:
                alternatives = TOP_LEVEL_NODE;
                break;
            case OBJECT_MEMBER:
                alternatives = OBJECT_MEMBER_START;
                break;
            default:
                throw new IllegalStateException();
        }

        Result result = seekInAlternativesPaths(1, 0, 0, alternatives, true);
        lookaheadLimit = prevLookahead;
        return result.ctx;
    }
}
