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

import io.ballerinalang.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Action;
import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerinalang.compiler.internal.parser.tree.STAmbiguousCollectionNode;
import io.ballerinalang.compiler.internal.parser.tree.STAnnotAccessExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STArrayTypeDescriptorNode;
import io.ballerinalang.compiler.internal.parser.tree.STAsyncSendActionNode;
import io.ballerinalang.compiler.internal.parser.tree.STBinaryExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STBracedExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STCheckExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STConditionalExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STDefaultableParameterNode;
import io.ballerinalang.compiler.internal.parser.tree.STFieldAccessExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STFunctionSignatureNode;
import io.ballerinalang.compiler.internal.parser.tree.STIndexedExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STIntersectionTypeDescriptorNode;
import io.ballerinalang.compiler.internal.parser.tree.STMissingToken;
import io.ballerinalang.compiler.internal.parser.tree.STNilLiteralNode;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.STOptionalFieldAccessExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STOptionalTypeDescriptorNode;
import io.ballerinalang.compiler.internal.parser.tree.STQualifiedNameReferenceNode;
import io.ballerinalang.compiler.internal.parser.tree.STRemoteMethodCallActionNode;
import io.ballerinalang.compiler.internal.parser.tree.STRequiredParameterNode;
import io.ballerinalang.compiler.internal.parser.tree.STRestBindingPatternNode;
import io.ballerinalang.compiler.internal.parser.tree.STRestParameterNode;
import io.ballerinalang.compiler.internal.parser.tree.STSimpleNameReferenceNode;
import io.ballerinalang.compiler.internal.parser.tree.STSpecificFieldNode;
import io.ballerinalang.compiler.internal.parser.tree.STSyncSendActionNode;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.STTypeReferenceTypeDescNode;
import io.ballerinalang.compiler.internal.parser.tree.STTypeTestExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STTypedBindingPatternNode;
import io.ballerinalang.compiler.internal.parser.tree.STUnionTypeDescriptorNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * A LL(k) recursive-descent parser for ballerina.
 *
 * @since 1.2.0
 */
public class BallerinaParser extends AbstractParser {

    private static final OperatorPrecedence DEFAULT_OP_PRECEDENCE = OperatorPrecedence.ACTION;

    protected BallerinaParser(AbstractTokenReader tokenReader) {
        super(tokenReader, new BallerinaParserErrorHandler(tokenReader));
    }

    /**
     * Start parsing the given input.
     *
     * @return Parsed node
     */
    @Override
    public STNode parse() {
        return parseCompUnit();
    }

    /**
     * Start parsing the input from a given context. Supported starting points are:
     * <ul>
     * <li>Module part (a file)</li>
     * <li>Top level node</li>
     * <li>Statement</li>
     * <li>Expression</li>
     * </ul>
     *
     * @param context Context to start parsing
     * @return Parsed node
     */
    public STNode parse(ParserRuleContext context) {
        switch (context) {
            case COMP_UNIT:
                return parseCompUnit();
            case TOP_LEVEL_NODE:
                startContext(ParserRuleContext.COMP_UNIT);
                return parseTopLevelNode();
            case STATEMENT:
                startContext(ParserRuleContext.COMP_UNIT);
                // startContext(ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE);
                startContext(ParserRuleContext.FUNC_BODY_BLOCK);
                return parseStatement();
            case EXPRESSION:
                startContext(ParserRuleContext.COMP_UNIT);
                // startContext(ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE);
                startContext(ParserRuleContext.FUNC_BODY_BLOCK);
                startContext(ParserRuleContext.STATEMENT);
                return parseExpression();
            default:
                throw new UnsupportedOperationException("Cannot start parsing from: " + context);
        }
    }

    /**
     * Resume the parsing from the given context.
     *
     * @param context Context to resume parsing
     * @param args Arguments that requires to continue parsing from the given parser context
     * @return Parsed node
     */
    @Override
    public STNode resumeParsing(ParserRuleContext context, Object... args) {
        switch (context) {
            case COMP_UNIT:
                return parseCompUnit();
            case EXTERNAL_FUNC_BODY:
                return parseExternalFunctionBody();
            case FUNC_BODY:
                return parseFunctionBody((boolean) args[0]);
            case OPEN_BRACE:
                return parseOpenBrace();
            case CLOSE_BRACE:
                return parseCloseBrace();
            case FUNC_NAME:
                return parseFunctionName();
            case OPEN_PARENTHESIS:
            case ARG_LIST_START:
                return parseOpenParenthesis((ParserRuleContext) args[0]);
            case SIMPLE_TYPE_DESCRIPTOR:
                return parseSimpleTypeDescriptor();
            case ASSIGN_OP:
                return parseAssignOp();
            case EXTERNAL_KEYWORD:
                return parseExternalKeyword();
            case SEMICOLON:
                return parseSemicolon();
            case CLOSE_PARENTHESIS:
                return parseCloseParenthesis();
            case VARIABLE_NAME:
                return parseVariableName();
            case TERMINAL_EXPRESSION:
                return parseTerminalExpression((STNode) args[0], (boolean) args[1], (boolean) args[2]);
            case STATEMENT:
                return parseStatement();
            case STATEMENT_WITHOUT_ANNOTS:
                return parseStatement((STNode) args[0]);
            case EXPRESSION_RHS:
                return parseExpressionRhs((OperatorPrecedence) args[0], (STNode) args[1], (boolean) args[2],
                        (boolean) args[3]);
            case PARAMETER_START:
                return parseParameter((SyntaxKind) args[0], (STNode) args[1], (int) args[2], (boolean) args[3]);
            case PARAMETER_WITHOUT_ANNOTS:
                return parseParamGivenAnnots((SyntaxKind) args[0], (STNode) args[1], (STNode) args[2], (int) args[3],
                        (boolean) args[4]);
            case AFTER_PARAMETER_TYPE:
                return parseAfterParamType((SyntaxKind) args[0], (STNode) args[1], (STNode) args[2], (STNode) args[3],
                        (STNode) args[4], (boolean) args[5]);
            case PARAMETER_NAME_RHS:
                return parseParameterRhs((SyntaxKind) args[0], (STNode) args[1], (STNode) args[2], (STNode) args[3],
                        (STNode) args[4], (STNode) args[5]);
            case TOP_LEVEL_NODE:
                return parseTopLevelNode();
            case TOP_LEVEL_NODE_WITHOUT_METADATA:
                return parseTopLevelNode((STNode) args[0]);
            case TOP_LEVEL_NODE_WITHOUT_MODIFIER:
                return parseTopLevelNode((STNode) args[0], (STNode) args[1]);
            case TYPE_NAME_OR_VAR_NAME:
                return parseStatementStartIdentifier();
            case VAR_DECL_STMT_RHS:
                return parseVarDeclRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (boolean) args[3]);
            case TYPE_REFERENCE:
                return parseTypeReference();
            case FIELD_DESCRIPTOR_RHS:
                return parseFieldDescriptorRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (STNode) args[2]);
            case RECORD_BODY_START:
                return parseRecordBodyStartDelimiter();
            case TYPE_DESCRIPTOR:
                return parseTypeDescriptorInternal((ParserRuleContext) args[0]);
            case OBJECT_MEMBER_START:
                return parseObjectMember();
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
                return parseObjectMethodOrField((STNode) args[0], (STNode) args[1]);
            case OBJECT_FIELD_RHS:
                return parseObjectFieldRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (STNode) args[3],
                        (STNode) args[4]);
            case OBJECT_TYPE_FIRST_QUALIFIER:
                return parseObjectTypeQualifiers();
            case OBJECT_TYPE_SECOND_QUALIFIER:
                return parseObjectTypeSecondQualifier((STNode) args[0]);
            case OBJECT_KEYWORD:
                return parseObjectKeyword();
            case TYPE_NAME:
                return parseTypeName();
            case IF_KEYWORD:
                return parseIfKeyword();
            case ELSE_KEYWORD:
                return parseElseKeyword();
            case ELSE_BODY:
                return parseElseBody();
            case WHILE_KEYWORD:
                return parseWhileKeyword();
            case PANIC_KEYWORD:
                return parsePanicKeyword();
            case MAJOR_VERSION:
                return parseMajorVersion();
            case IMPORT_DECL_RHS:
                return parseImportDecl((STNode) args[0], (STNode) args[1]);
            case IMPORT_PREFIX:
                return parseImportPrefix();
            case IMPORT_MODULE_NAME:
            case IMPORT_ORG_OR_MODULE_NAME:
            case VARIABLE_REF:
            case SERVICE_NAME:
            case IMPLICIT_ANON_FUNC_PARAM:
                return parseIdentifier(context);
            case IMPORT_KEYWORD:
                return parseImportKeyword();
            case SLASH:
                return parseSlashToken();
            case DOT:
                return parseDotToken();
            case IMPORT_VERSION_DECL:
                return parseVersion();
            case VERSION_KEYWORD:
                return parseVersionKeywrod();
            case VERSION_NUMBER:
                return parseVersionNumber();
            case DECIMAL_INTEGER_LITERAL:
                return parseDecimalIntLiteral(context);
            case IMPORT_SUB_VERSION:
                return parseSubVersion(context);
            case IMPORT_PREFIX_DECL:
                return parseImportPrefixDecl();
            case AS_KEYWORD:
                return parseAsKeyword();
            case CONTINUE_KEYWORD:
                return parseContinueKeyword();
            case BREAK_KEYWORD:
                return parseBreakKeyword();
            case RETURN_KEYWORD:
                return parseReturnKeyword();
            case MAPPING_FIELD:
            case FIRST_MAPPING_FIELD:
                return parseMappingField((ParserRuleContext) args[0]);
            case SPECIFIC_FIELD_RHS:
                return parseSpecificFieldRhs((STNode) args[0], (STNode) args[1]);
            case STRING_LITERAL:
                return parseStringLiteral();
            case COLON:
                return parseColon();
            case OPEN_BRACKET:
                return parseOpenBracket();
            case RESOURCE_DEF:
                return parseResource();
            case OPTIONAL_SERVICE_NAME:
                return parseServiceName();
            case SERVICE_KEYWORD:
                return parseServiceKeyword();
            case ON_KEYWORD:
                return parseOnKeyword();
            case RESOURCE_KEYWORD:
                return parseResourceKeyword();
            case LISTENER_KEYWORD:
                return parseListenerKeyword();
            case NIL_TYPE_DESCRIPTOR:
                return parseNilTypeDescriptor();
            case COMPOUND_ASSIGNMENT_STMT:
                return parseCompoundAssignmentStmt();
            case TYPEOF_KEYWORD:
                return parseTypeofKeyword();
            case ARRAY_TYPE_DESCRIPTOR:
                return parseArrayTypeDescriptor((STNode) args[0]);
            case ARRAY_LENGTH:
                return parseArrayLength();
            case FUNC_DEF_OR_FUNC_TYPE:
            case REQUIRED_PARAM:
            case ANNOT_REFERENCE:
                return parseIdentifier(context);
            case IS_KEYWORD:
                return parseIsKeyword();
            case STMT_START_WITH_EXPR_RHS:
                return parseStatementStartWithExprRhs((STNode) args[0]);
            case COMMA:
                return parseComma();
            case CONST_DECL_TYPE:
                return parseConstDecl((STNode) args[0], (STNode) args[1], (STNode) args[2]);
            case BINDING_PATTERN_OR_EXPR_RHS:
                return parseTypedBindingPatternOrExprRhs((STNode) args[0], (boolean) args[1]);
            case LT:
                return parseLTToken();
            case GT:
                return parseGTToken();
            case NIL_LITERAL:
                return parseNilLiteral();
            case RECORD_FIELD_OR_RECORD_END:
                return parseFieldOrRestDescriptor((boolean) args[0]);
            case ANNOTATION_KEYWORD:
                return parseAnnotationKeyword();
            case ANNOT_DECL_OPTIONAL_TYPE:
                return parseAnnotationDeclFromType((STNode) args[0], (STNode) args[1], (STNode) args[2],
                        (STNode) args[3]);
            case ANNOT_DECL_RHS:
                return parseAnnotationDeclRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (STNode) args[3],
                        (STNode) args[4]);
            case ANNOT_OPTIONAL_ATTACH_POINTS:
                return parseAnnotationDeclAttachPoints((STNode) args[0], (STNode) args[1], (STNode) args[2],
                        (STNode) args[3], (STNode) args[4], (STNode) args[5]);
            case SOURCE_KEYWORD:
                return parseSourceKeyword();
            case ATTACH_POINT_IDENT:
                return parseAttachPointIdent((STNode) args[0]);
            case IDENT_AFTER_OBJECT_IDENT:
                return parseIdentAfterObjectIdent();
            case FUNCTION_IDENT:
                return parseFunctionIdent();
            case FIELD_IDENT:
                return parseFieldIdent();
            case ATTACH_POINT_END:
                return parseAttachPointEnd();
            case XMLNS_KEYWORD:
                return parseXMLNSKeyword();
            case XML_NAMESPACE_PREFIX_DECL:
                return parseXMLDeclRhs((STNode) args[0], (STNode) args[1], (boolean) args[2]);
            case NAMESPACE_PREFIX:
                return parseNamespacePrefix();
            case WORKER_KEYWORD:
                return parseWorkerKeyword();
            case WORKER_NAME:
                return parseWorkerName();
            case FORK_KEYWORD:
                return parseForkKeyword();
            case DECIMAL_FLOATING_POINT_LITERAL:
                return parseDecimalFloatingPointLiteral();
            case HEX_FLOATING_POINT_LITERAL:
                return parseHexFloatingPointLiteral();
            case TRAP_KEYWORD:
                return parseTrapKeyword();
            case IN_KEYWORD:
                return parseInKeyword();
            case FOREACH_KEYWORD:
                return parseForEachKeyword();
            case TABLE_KEYWORD:
                return parseTableKeyword();
            case KEY_KEYWORD:
                return parseKeyKeyword();
            case TABLE_KEYWORD_RHS:
                return parseTableConstructorOrQuery((STNode) args[0], (boolean) args[1]);
            case ERROR_KEYWORD:
                return parseErrorKeyWord();
            case LET_KEYWORD:
                return parseLetKeyword();
            case STREAM_KEYWORD:
                return parseStreamKeyword();
            case STREAM_TYPE_FIRST_PARAM_RHS:
                return parseStreamTypeParamsNode((STNode) args[0], (STNode) args[1]);
            case TEMPLATE_START:
            case TEMPLATE_END:
                return parseBacktickToken(context);
            case KEY_CONSTRAINTS_RHS:
                return parseKeyConstraint((STNode) args[0]);
            case FUNCTION_KEYWORD_RHS:
                return parseFunctionKeywordRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (boolean) args[3],
                        (boolean) args[3]);
            case FUNC_OPTIONAL_RETURNS:
                return parseFuncReturnTypeDescriptor();
            case RETURNS_KEYWORD:
                return parseReturnsKeyword();
            case NEW_KEYWORD_RHS:
                return parseNewKeywordRhs((STNode) args[0]);
            case NEW_KEYWORD:
                return parseNewKeyword();
            case IMPLICIT_NEW:
                return parseImplicitNewRhs((STNode) args[0]);
            case FROM_KEYWORD:
                return parseFromKeyword();
            case WHERE_KEYWORD:
                return parseWhereKeyword();
            case SELECT_KEYWORD:
                return parseSelectKeyword();
            case TABLE_CONSTRUCTOR_OR_QUERY_START:
                return parseTableConstructorOrQuery((boolean) args[0]);
            case TABLE_CONSTRUCTOR_OR_QUERY_RHS:
                return parseTableConstructorOrQueryRhs((STNode) args[0], (STNode) args[1], (boolean) args[2]);
            case QUERY_PIPELINE_RHS:
                return parseIntermediateClause((boolean) args[0]);
            case ANON_FUNC_BODY:
                return parseAnonFuncBody((boolean) args[0]);
            case CLOSE_BRACKET:
                return parseCloseBracket();
            case ARG_START_OR_ARG_LIST_END:
                return parseArg((STNode) args[0]);
            case ARG_END:
                return parseArgEnd();
            case MAPPING_FIELD_END:
                return parseMappingFieldEnd();
            case FUNCTION_KEYWORD:
                return parseFunctionKeyword();
            case FIELD_OR_REST_DESCIPTOR_RHS:
                return parseFieldOrRestDescriptorRhs((STNode) args[0], (STNode) args[1]);
            case TYPE_DESC_IN_TUPLE_RHS:
                return parseTupleMemberRhs();
            case LIST_BINDING_PATTERN_END_OR_CONTINUE:
                return parseListBindingpatternRhs();
            case MAPPING_BINDING_PATTERN_END:
                return parseMappingBindingpatternEnd();
            case FIELD_BINDING_PATTERN_NAME:
            case FIELD_BINDING_PATTERN:
                return parseFieldBindingPattern();
            case CONSTANT_EXPRESSION_START:
                return parseConstExprInternal();
            case LIST_CONSTRUCTOR_MEMBER_END:
                return parseListConstructorMemberEnd();
            case NIL_OR_PARENTHESISED_TYPE_DESC_RHS:
                return parseNilOrParenthesisedTypeDescRhs((STNode) args[0]);
            case ANON_FUNC_PARAM_RHS:
                return parseImplicitAnonFuncParamEnd();
            case CAPTURE_BINDING_PATTERN:
                return parseCaptureOrWildcardBindingPattern();
            case LIST_BINDING_PATTERN:
                return parseListBindingPattern();
            case BINDING_PATTERN:
                return parseBindingPattern();
            case PEER_WORKER_NAME:
                return parsePeerWorkerName();
            case SYNC_SEND_TOKEN:
                return parseSyncSendToken();
            case LEFT_ARROW_TOKEN:
                return parseLeftArrowToken();
            case RECEIVE_WORKERS:
                return parseReceiveWorkers();
            case WAIT_KEYWORD:
                return parseWaitKeyword();
            case WAIT_FUTURE_EXPR_END:
                return parseWaitFutureExprEnd((int) args[0]);
            case WAIT_FIELD_NAME:
                return parseWaitField();
            case WAIT_FIELD_END:
                return parseWaitFieldEnd();
            case ANNOT_CHAINING_TOKEN:
                return parseAnnotChainingToken();
            case FIELD_ACCESS_IDENTIFIER:
                return parseFieldAccessIdentifier();
            case DO_KEYWORD:
                return parseDoKeyword();
            case MEMBER_ACCESS_KEY_EXPR_END:
                return parseMemberAccessKeyExprEnd();
            case OPTIONAL_CHAINING_TOKEN:
                return parseOptionalChainingToken();
            case RETRY_KEYWORD_RHS:
                return parseRetryKeywordRhs((STNode) args[0]);
            case RETRY_TYPE_PARAM_RHS:
                return parseRetryTypeParamRhs((STNode) args[0], (STNode) args[1]);
            case TRANSACTION_KEYWORD:
                return parseTransactionKeyword();
            case COMMIT_KEYWORD:
                return parseCommitKeyword();
            case RETRY_KEYWORD:
                return parseRetryKeyword();
            case ROLLBACK_KEYWORD:
                return parseRollbackKeyword();
            case RETRY_BODY:
                return parseRetryBody();
            case ENUM_MEMBER_END:
                return parseEnumMemberEnd();
            case ENUM_MEMBER_NAME:
                return parseEnumMember();
            case BRACKETED_LIST_MEMBER_END:
                return parseBracketedListMemberEnd();
            case STMT_START_BRACKETED_LIST_MEMBER:
                return parseStatementStartBracketedListMember();
            case TYPED_BINDING_PATTERN_TYPE_RHS:
                return parseTypedBindingPatternTypeRhs((STNode) args[0], (ParserRuleContext) args[1]);
            case BRACKETED_LIST_RHS:
                return parseTypedBindingPatternOrMemberAccessRhs((STNode) args[0], (STNode) args[1], (STNode) args[2],
                        (STNode) args[3], (boolean) args[4], (boolean) args[5], (ParserRuleContext) args[6]);
            case UNION_OR_INTERSECTION_TOKEN:
                return parseUnionOrIntersectionToken();
            case BRACKETED_LIST_MEMBER:
            case LIST_BINDING_MEMBER_OR_ARRAY_LENGTH:
                return parseBracketedListMember((boolean) args[0]);
            case BASE16_KEYWORD:
                return parseBase16Keyword();
            case BASE64_KEYWORD:
                return parseBase64Keyword();
            case DOT_LT_TOKEN:
                return parseDotLTToken();
            case SLASH_LT_TOKEN:
                return parseSlashLTToken();
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
                return parseDoubleSlashDoubleAsteriskLTToken();
            case XML_ATOMIC_NAME_PATTERN_START:
                return parseXMLAtomicNamePatternBody();
            case BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS:
                return parseBracedExprOrAnonFuncParamRhs((STNode) args[0], (STNode) args[1], (boolean) args[2]);
            case READONLY_KEYWORD:
                return parseReadonlyKeyword();
            case SPECIFIC_FIELD:
                return parseSpecificField((STNode) args[0]);
            default:
                throw new IllegalStateException("cannot resume parsing the rule: " + context);
        }
    }

    /*
     * Private methods.
     */

    /**
     * Parse a given input and returns the AST. Starts parsing from the top of a compilation unit.
     *
     * @return Parsed node
     */
    private STNode parseCompUnit() {
        startContext(ParserRuleContext.COMP_UNIT);
        STToken token = peek();
        List<STNode> otherDecls = new ArrayList<>();
        List<STNode> importDecls = new ArrayList<>();

        boolean processImports = true;
        while (token.kind != SyntaxKind.EOF_TOKEN) {
            STNode decl = parseTopLevelNode(token.kind);
            if (decl == null) {
                break;
            }
            if (decl.kind == SyntaxKind.IMPORT_DECLARATION) {
                if (processImports) {
                    importDecls.add(decl);
                } else {
                    // If an import occurs after any other module level declaration,
                    // we add it to the other-decl list to preserve the order. But
                    // log an error and mark it as invalid.
                    otherDecls.add(decl);
                    this.errorHandler.reportInvalidNode(token, "imports must be declared before other declarations");
                }
            } else {
                if (processImports) {
                    // While processing imports, if we reach any other declaration,
                    // then mark this as the end of processing imports.
                    processImports = false;
                }
                otherDecls.add(decl);
            }
            token = peek();
        }

        STToken eof = consume();
        endContext();

        return STNodeFactory.createModulePartNode(STNodeFactory.createNodeList(importDecls),
                STNodeFactory.createNodeList(otherDecls), eof);
    }

    /**
     * Parse top level node having an optional modifier preceding it.
     *
     * @return Parsed node
     */
    private STNode parseTopLevelNode() {
        STToken token = peek();
        return parseTopLevelNode(token.kind);
    }

    protected STNode parseTopLevelNode(SyntaxKind tokenKind) {
        STNode metadata;
        switch (tokenKind) {
            case EOF_TOKEN:
                return consume();
            case DOCUMENTATION_LINE:
            case AT_TOKEN:
                metadata = parseMetaData(tokenKind);
                return parseTopLevelNode(metadata);
            case IMPORT_KEYWORD:
            case FINAL_KEYWORD:
            case PUBLIC_KEYWORD:
            case FUNCTION_KEYWORD:
            case TYPE_KEYWORD:
            case LISTENER_KEYWORD:
            case CONST_KEYWORD:
            case ANNOTATION_KEYWORD:
            case XMLNS_KEYWORD:
            case SERVICE_KEYWORD:
            case ENUM_KEYWORD:
                metadata = createEmptyMetadata();
                break;
            case IDENTIFIER_TOKEN:
                // Here we assume that after recovering, we'll never reach here.
                // Otherwise the tokenOffset will not be 1.
                if (isModuleVarDeclStart(1)) {
                    // This is an early exit, so that we don't have to do the same check again.
                    return parseModuleVarDecl(createEmptyMetadata(), null);
                }
                // Else fall through
            default:
                if (isTypeStartingToken(tokenKind) && tokenKind != SyntaxKind.IDENTIFIER_TOKEN) {
                    metadata = createEmptyMetadata();
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    metadata = STNodeFactory.createEmptyNodeList();
                    break;
                }

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTopLevelNode(solution.tokenKind);
        }

        return parseTopLevelNode(tokenKind, metadata);
    }

    /**
     * Parse top level node having an optional modifier preceding it, given the next token kind.
     *
     * @param metadata Next token kind
     * @return Parsed node
     */
    private STNode parseTopLevelNode(STNode metadata) {
        STToken nextToken = peek();
        return parseTopLevelNode(nextToken.kind, metadata);
    }

    private STNode parseTopLevelNode(SyntaxKind tokenKind, STNode metadata) {
        STNode qualifier = null;
        switch (tokenKind) {
            case EOF_TOKEN:
                if (metadata != null) {
                    this.errorHandler.reportInvalidNode(null, "invalid metadata");
                }
                return null;
            case PUBLIC_KEYWORD:
                qualifier = parseQualifier();
                tokenKind = peek().kind;
                break;
            case FUNCTION_KEYWORD:
            case TYPE_KEYWORD:
            case LISTENER_KEYWORD:
            case CONST_KEYWORD:
            case FINAL_KEYWORD:
            case IMPORT_KEYWORD:
            case ANNOTATION_KEYWORD:
            case XMLNS_KEYWORD:
            case ENUM_KEYWORD:
                break;
            case IDENTIFIER_TOKEN:
                // Here we assume that after recovering, we'll never reach here.
                // Otherwise the tokenOffset will not be 1.
                if (isModuleVarDeclStart(1)) {
                    // This is an early exit, so that we don't have to do the same check again.
                    return parseModuleVarDecl(metadata, null);
                }
                // Else fall through
            default:
                if (isTypeStartingToken(tokenKind) && tokenKind != SyntaxKind.IDENTIFIER_TOKEN) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_METADATA, metadata);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    qualifier = STNodeFactory.createEmptyNode();
                    break;
                }

                return parseTopLevelNode(solution.tokenKind, metadata);
        }

        return parseTopLevelNode(tokenKind, metadata, qualifier);
    }

    /**
     * Check whether the cursor is at the start of a module level var-decl.
     *
     * @param lookahead Offset of the token to to check
     * @return <code>true</code> if the cursor is at the start of a module level var-decl.
     *         <code>false</code> otherwise.
     */
    private boolean isModuleVarDeclStart(int lookahead) {
        // Assumes that we reach here after a peek()
        STToken nextToken = peek(lookahead + 1);
        switch (nextToken.kind) {
            case EQUAL_TOKEN:
                // Scenario: foo =
                // Even though this is not valid, consider this as a var-decl and continue;
            case OPEN_BRACKET_TOKEN:
                // Scenario foo[] (Array type descriptor with custom type)
            case QUESTION_MARK_TOKEN:
                // Scenario foo? (Optional type descriptor with custom type)
            case PIPE_TOKEN:
                // Scenario foo | (Union type descriptor with custom type)
            case BITWISE_AND_TOKEN:
                // Scenario foo & (Intersection type descriptor with custom type)
                return true;
            case IDENTIFIER_TOKEN:
                switch (peek(lookahead + 2).kind) {
                    case EQUAL_TOKEN: // Scenario: foo bar =
                    case SEMICOLON_TOKEN: // Scenario: foo bar;
                        return true;
                    default:
                        return false;
                }
            case COLON_TOKEN:
                if (lookahead > 1) {
                    // This means there's a colon somewhere after the type name.
                    // This is not a valid var-decl.
                    return false;
                }

                // Scenario: foo:bar baz ...
                if (peek(lookahead + 2).kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    return false;
                }
                return isModuleVarDeclStart(lookahead + 2);
            default:
                return false;
        }
    }

    /**
     * Parse import declaration.
     * <p>
     * <code>import-decl :=  import [org-name /] module-name [version sem-ver] [as import-prefix] ;</code>
     *
     * @return Parsed node
     */
    private STNode parseImportDecl() {
        startContext(ParserRuleContext.IMPORT_DECL);
        this.tokenReader.startMode(ParserMode.IMPORT);
        STNode importKeyword = parseImportKeyword();
        STNode identifier = parseIdentifier(ParserRuleContext.IMPORT_ORG_OR_MODULE_NAME);

        STToken token = peek();
        STNode importDecl = parseImportDecl(token.kind, importKeyword, identifier);
        this.tokenReader.endMode();
        endContext();
        return importDecl;
    }

    /**
     * Parse import keyword.
     *
     * @return Parsed node
     */
    private STNode parseImportKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IMPORT_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.IMPORT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse identifier.
     *
     * @return Parsed node
     */
    private STNode parseIdentifier(ParserRuleContext currentCtx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, currentCtx);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse RHS of the import declaration. This includes the components after the
     * starting identifier (org-name/module-name) of the import decl.
     *
     * @param importKeyword Import keyword
     * @param identifier Org-name or the module name
     * @return Parsed node
     */
    private STNode parseImportDecl(STNode importKeyword, STNode identifier) {
        STToken nextToken = peek();
        return parseImportDecl(nextToken.kind, importKeyword, identifier);
    }

    private STNode parseImportDecl(SyntaxKind tokenKind, STNode importKeyword, STNode identifier) {
        STNode orgName;
        STNode moduleName;
        STNode version;
        STNode alias;

        switch (tokenKind) {
            case SLASH_TOKEN:
                STNode slash = parseSlashToken();
                orgName = STNodeFactory.createImportOrgNameNode(identifier, slash);
                moduleName = parseModuleName();
                version = parseVersion();
                alias = parseImportPrefixDecl();
                break;
            case DOT_TOKEN:
            case VERSION_KEYWORD:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(tokenKind, identifier);
                version = parseVersion();
                alias = parseImportPrefixDecl();
                break;
            case AS_KEYWORD:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(tokenKind, identifier);
                version = STNodeFactory.createEmptyNode();
                alias = parseImportPrefixDecl();
                break;
            case SEMICOLON_TOKEN:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(tokenKind, identifier);
                version = STNodeFactory.createEmptyNode();
                alias = STNodeFactory.createEmptyNode();
                break;
            default:
                Solution solution = recover(peek(), ParserRuleContext.IMPORT_DECL_RHS, importKeyword, identifier);

                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseImportDecl(solution.tokenKind, importKeyword, identifier);
        }

        STNode semicolon = parseSemicolon();
        return STNodeFactory.createImportDeclarationNode(importKeyword, orgName, moduleName, version, alias, semicolon);
    }

    /**
     * parse slash token.
     *
     * @return Parsed node
     */
    private STNode parseSlashToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SLASH_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.SLASH);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse dot token.
     *
     * @return Parsed node
     */
    private STNode parseDotToken() {
        STToken nextToken = peek();
        return parseDotToken(nextToken.kind);
    }

    private STNode parseDotToken(SyntaxKind tokenKind) {
        if (tokenKind == SyntaxKind.DOT_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.DOT);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse module name of a import declaration.
     *
     * @return Parsed node
     */
    private STNode parseModuleName() {
        STNode moduleNameStart = parseIdentifier(ParserRuleContext.IMPORT_MODULE_NAME);
        return parseModuleName(peek().kind, moduleNameStart);
    }

    /**
     * Parse import module name of a import declaration, given the module name start identifier.
     *
     * @param moduleNameStart Starting identifier of the module name
     * @return Parsed node
     */
    private STNode parseModuleName(SyntaxKind nextTokenKind, STNode moduleNameStart) {
        List<STNode> moduleNameParts = new ArrayList<>();
        moduleNameParts.add(moduleNameStart);

        while (!isEndOfImportModuleName(nextTokenKind)) {
            moduleNameParts.add(parseDotToken());
            moduleNameParts.add(parseIdentifier(ParserRuleContext.IMPORT_MODULE_NAME));
            nextTokenKind = peek().kind;
        }

        return STNodeFactory.createNodeList(moduleNameParts);
    }

    private boolean isEndOfImportModuleName(SyntaxKind nextTokenKind) {
        return nextTokenKind != SyntaxKind.DOT_TOKEN && nextTokenKind != SyntaxKind.IDENTIFIER_TOKEN;
    }

    private boolean isEndOfImportDecl(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case SEMICOLON_TOKEN:
            case PUBLIC_KEYWORD:
            case FUNCTION_KEYWORD:
            case TYPE_KEYWORD:
            case ABSTRACT_KEYWORD:
            case CONST_KEYWORD:
            case EOF_TOKEN:
            case SERVICE_KEYWORD:
            case IMPORT_KEYWORD:
            case FINAL_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse version component of a import declaration.
     * <p>
     * <code>version-decl := version sem-ver</code>
     *
     * @return Parsed node
     */
    private STNode parseVersion() {
        STToken nextToken = peek();
        return parseVersion(nextToken.kind);
    }

    private STNode parseVersion(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case VERSION_KEYWORD:
                STNode versionKeyword = parseVersionKeywrod();
                STNode versionNumber = parseVersionNumber();
                return STNodeFactory.createImportVersionNode(versionKeyword, versionNumber);
            case AS_KEYWORD:
            case SEMICOLON_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                if (isEndOfImportDecl(nextTokenKind)) {
                    return STNodeFactory.createEmptyNode();
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.IMPORT_VERSION_DECL);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseVersion(solution.tokenKind);
        }

    }

    /**
     * Parse version keywrod.
     *
     * @return Parsed node
     */
    private STNode parseVersionKeywrod() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.VERSION_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.VERSION_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse version number.
     * <p>
     * <code>sem-ver := major-num [. minor-num [. patch-num]]
     * <br/>
     * major-num := DecimalNumber
     * <br/>
     * minor-num := DecimalNumber
     * <br/>
     * patch-num := DecimalNumber
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseVersionNumber() {
        STToken nextToken = peek();
        return parseVersionNumber(nextToken.kind);
    }

    private STNode parseVersionNumber(SyntaxKind nextTokenKind) {
        STNode majorVersion;
        switch (nextTokenKind) {
            case DECIMAL_INTEGER_LITERAL:
                majorVersion = parseMajorVersion();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.VERSION_NUMBER);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseVersionNumber(solution.tokenKind);
        }

        List<STNode> versionParts = new ArrayList<>();
        versionParts.add(majorVersion);

        STNode minorVersion = parseMinorVersion();
        if (minorVersion != null) {
            versionParts.add(minorVersion);

            STNode patchVersion = parsePatchVersion();
            if (patchVersion != null) {
                versionParts.add(patchVersion);
            }
        }

        return STNodeFactory.createNodeList(versionParts);

    }

    private STNode parseMajorVersion() {
        return parseDecimalIntLiteral(ParserRuleContext.MAJOR_VERSION);
    }

    private STNode parseMinorVersion() {
        return parseSubVersion(ParserRuleContext.MINOR_VERSION);
    }

    private STNode parsePatchVersion() {
        return parseSubVersion(ParserRuleContext.PATCH_VERSION);
    }

    /**
     * Parse decimal literal.
     *
     * @param context Context in which the decimal literal is used.
     * @return Parsed node
     */
    private STNode parseDecimalIntLiteral(ParserRuleContext context) {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.DECIMAL_INTEGER_LITERAL) {
            return consume();
        } else {
            Solution sol = recover(peek(), context);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse sub version. i.e: minor-version/patch-version.
     *
     * @param context Context indicating what kind of sub-version is being parsed.
     * @return Parsed node
     */
    private STNode parseSubVersion(ParserRuleContext context) {
        STToken nextToken = peek();
        return parseSubVersion(nextToken.kind, context);
    }

    private STNode parseSubVersion(SyntaxKind nextTokenKind, ParserRuleContext context) {
        switch (nextTokenKind) {
            case AS_KEYWORD:
            case SEMICOLON_TOKEN:
                return null;
            case DOT_TOKEN:
                STNode leadingDot = parseDotToken();
                STNode versionNumber = parseDecimalIntLiteral(context);
                return STNodeFactory.createImportSubVersionNode(leadingDot, versionNumber);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.IMPORT_SUB_VERSION);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseSubVersion(solution.tokenKind, context);
        }
    }

    /**
     * Parse import prefix declaration.
     * <p>
     * <code>import-prefix-decl := as import-prefix
     * <br/>
     * import-prefix := a identifier | _
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseImportPrefixDecl() {
        STToken token = peek();
        return parseImportPrefixDecl(token.kind);
    }

    private STNode parseImportPrefixDecl(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case AS_KEYWORD:
                STNode asKeyword = parseAsKeyword();
                STNode prefix = parseImportPrefix();
                return STNodeFactory.createImportPrefixNode(asKeyword, prefix);
            case SEMICOLON_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                if (isEndOfImportDecl(nextTokenKind)) {
                    return STNodeFactory.createEmptyNode();
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.IMPORT_PREFIX_DECL);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseImportPrefixDecl(solution.tokenKind);
        }
    }

    /**
     * Parse <code>as</code> keyword.
     *
     * @return Parsed node
     */
    private STNode parseAsKeyword() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.AS_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.AS_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse import prefix.
     *
     * @return Parsed node
     */
    private STNode parseImportPrefix() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.IMPORT_PREFIX);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse top level node, given the modifier that precedes it.
     *
     * @param qualifier Qualifier that precedes the top level node
     * @return Parsed node
     */
    private STNode parseTopLevelNode(STNode metadata, STNode qualifier) {
        STToken token = peek();
        return parseTopLevelNode(token.kind, metadata, qualifier);
    }

    /**
     * Parse top level node given the next token kind and the modifier that precedes it.
     *
     * @param tokenKind Next token kind
     * @param qualifier Qualifier that precedes the top level node
     * @return Parsed top-level node
     */
    private STNode parseTopLevelNode(SyntaxKind tokenKind, STNode metadata, STNode qualifier) {
        switch (tokenKind) {
            case FUNCTION_KEYWORD:
                // ANything starts with a function keyword could be a function definition
                // or a module-var-decl with function type desc.
                return parseFuncDefOrFuncTypeDesc(metadata, getQualifier(qualifier), false);
            case TYPE_KEYWORD:
                return parseModuleTypeDefinition(metadata, getQualifier(qualifier));
            case LISTENER_KEYWORD:
                return parseListenerDeclaration(metadata, getQualifier(qualifier));
            case CONST_KEYWORD:
                return parseConstantDeclaration(metadata, getQualifier(qualifier));
            case ANNOTATION_KEYWORD:
                STNode constKeyword = STNodeFactory.createEmptyNode();
                return parseAnnotationDeclaration(metadata, getQualifier(qualifier), constKeyword);
            case IMPORT_KEYWORD:
                reportInvalidQualifier(qualifier);
                // TODO log error for metadata
                return parseImportDecl();
            case XMLNS_KEYWORD:
                reportInvalidQualifier(qualifier);
                // TODO log error for metadata
                return parseXMLNamespaceDeclaration(true);
            case FINAL_KEYWORD:
                reportInvalidQualifier(qualifier);
                STNode finalKeyword = parseFinalKeyword();
                return parseVariableDecl(metadata, finalKeyword, true);
            case SERVICE_KEYWORD:
                if (isServiceDeclStart(ParserRuleContext.TOP_LEVEL_NODE, 1)) {
                    reportInvalidQualifier(qualifier);
                    return parseServiceDecl(metadata);
                }
                return parseModuleVarDecl(metadata, qualifier);
            case ENUM_KEYWORD:
                return parseEnumDeclaration(metadata, getQualifier(qualifier));
            case IDENTIFIER_TOKEN:
                // Here we assume that after recovering, we'll never reach here.
                // Otherwise the tokenOffset will not be 1.
                if (isModuleVarDeclStart(1)) {
                    return parseModuleVarDecl(metadata, qualifier);
                }
                // fall through
            default:
                if (isTypeStartingToken(tokenKind) && tokenKind != SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseModuleVarDecl(metadata, qualifier);
                }

                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_MODIFIER, metadata, qualifier);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    return parseModuleVarDecl(metadata, qualifier);
                }

                return parseTopLevelNode(solution.tokenKind, metadata, qualifier);
        }

    }

    private STNode parseModuleVarDecl(STNode metadata, STNode qualifier) {
        reportInvalidQualifier(qualifier);
        STNode finalKeyword = STNodeFactory.createEmptyNode();
        return parseVariableDecl(metadata, finalKeyword, true);
    }

    private STNode getQualifier(STNode qualifier) {
        return qualifier == null ? STNodeFactory.createEmptyNode() : qualifier;
    }

    private void reportInvalidQualifier(STNode qualifier) {
        if (qualifier != null && qualifier.kind != SyntaxKind.NONE) {
            this.errorHandler.reportInvalidNode((STToken) qualifier,
                    "invalid qualifier '" + qualifier.toString().trim() + "'");
        }
    }

    /**
     * Parse access modifiers.
     *
     * @return Parsed node
     */
    private STNode parseQualifier() {
        STToken token = peek();
        if (token.kind == SyntaxKind.PUBLIC_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.PUBLIC_KEYWORD);
            return sol.recoveredNode;
        }
    }

    private STNode parseFuncDefinition(STNode metadata, STNode visibilityQualifier, boolean isObjectMethod) {
        startContext(ParserRuleContext.FUNC_DEF);
        STNode functionKeyword = parseFunctionKeyword();
        STNode funcDef = parseFunctionKeywordRhs(metadata, visibilityQualifier, functionKeyword, true, isObjectMethod);
        return funcDef;
    }

    /**
     * Parse function definition for the function type descriptor.
     * <p>
     * <code>
     * function-defn := FUNCTION identifier function-signature function-body
     * <br/>
     * function-type-descriptor := function function-signature
     * </code>
     *
     * @param metadata Metadata
     * @param visibilityQualifier Visibility qualifier
     * @return Parsed node
     */
    private STNode parseFuncDefOrFuncTypeDesc(STNode metadata, STNode visibilityQualifier, boolean isObjectMethod) {
        startContext(ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE);
        STNode functionKeyword = parseFunctionKeyword();
        STNode funcDefOrType =
                parseFunctionKeywordRhs(metadata, visibilityQualifier, functionKeyword, false, isObjectMethod);
        return funcDefOrType;
    }

    private STNode parseFunctionKeywordRhs(STNode metadata, STNode visibilityQualifier, STNode functionKeyword,
                                           boolean isFuncDef, boolean isObjectMethod) {
        return parseFunctionKeywordRhs(peek().kind, metadata, visibilityQualifier, functionKeyword, isFuncDef,
                isObjectMethod);
    }

    private STNode parseFunctionKeywordRhs(SyntaxKind nextTokenKind, STNode metadata, STNode visibilityQualifier,
                                           STNode functionKeyword, boolean isFuncDef, boolean isObjectMethod) {
        STNode name;
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                name = parseFunctionName();
                isFuncDef = true;
                break;
            case OPEN_PAREN_TOKEN:
                name = STNodeFactory.createEmptyNode();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FUNCTION_KEYWORD_RHS, metadata,
                        visibilityQualifier, functionKeyword, isFuncDef, isObjectMethod);

                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }
                return parseFunctionKeywordRhs(solution.tokenKind, metadata, visibilityQualifier, functionKeyword,
                        isFuncDef, isObjectMethod);
        }

        // If the function name is present, treat this as a function def
        if (isFuncDef) {
            switchContext(ParserRuleContext.FUNC_DEF);
            STNode funcSignature = parseFuncSignature(false);
            STNode funcDef = createFuncDefOrMethodDecl(metadata, visibilityQualifier, functionKeyword, isObjectMethod,
                    name, funcSignature);
            endContext();
            return funcDef;
        }

        // Otherwise it could be a func-def or a func-type
        STNode funcSignature = parseFuncSignature(true);
        return parseReturnTypeDescRhs(metadata, visibilityQualifier, functionKeyword, funcSignature, isObjectMethod);
    }

    private STNode createFuncDefOrMethodDecl(STNode metadata, STNode visibilityQualifier, STNode functionKeyword,
                                             boolean isObjectMethod, STNode name, STNode funcSignature) {
        STNode body = parseFunctionBody(isObjectMethod);
        if (body.kind == SyntaxKind.SEMICOLON_TOKEN) {
            return STNodeFactory.createMethodDeclarationNode(metadata, visibilityQualifier, functionKeyword, name,
                    funcSignature, body);
        }
        return STNodeFactory.createFunctionDefinitionNode(metadata, visibilityQualifier, functionKeyword, name,
                funcSignature, body);
    }

    /**
     * Parse function signature.
     * <p>
     * <code>
     * function-signature := ( param-list ) return-type-descriptor
     * <br/>
     * return-type-descriptor := [ returns [annots] type-descriptor ]
     * </code>
     *
     * @param isParamNameOptional Whether the parameter names are optional
     * @param isInExprContext Whether this function signature is occurred within an expression context
     * @return Function signature node
     */
    private STNode parseFuncSignature(boolean isParamNameOptional) {
        STNode openParenthesis = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        STNode parameters = parseParamList(isParamNameOptional);
        STNode closeParenthesis = parseCloseParenthesis();
        endContext(); // end param-list
        STNode returnTypeDesc = parseFuncReturnTypeDescriptor();
        return STNodeFactory.createFunctionSignatureNode(openParenthesis, parameters, closeParenthesis, returnTypeDesc);
    }

    private STNode parseReturnTypeDescRhs(STNode metadata, STNode visibilityQualifier, STNode functionKeyword,
                                          STNode funcSignature, boolean isObjectMethod) {
        switch (peek().kind) {
            // var-decl with function type
            case SEMICOLON_TOKEN:
            case IDENTIFIER_TOKEN:
            case OPEN_BRACKET_TOKEN:
                // Parse the remaining as var-decl, because its the only module-level construct
                // that can start with a func-type-desc. Constants cannot have func-type-desc.
                endContext(); // end the func-type
                STNode typeDesc = STNodeFactory.createFunctionTypeDescriptorNode(functionKeyword, funcSignature);

                if (isObjectMethod) {
                    STNode readonlyQualifier = STNodeFactory.createEmptyNode();
                    STNode fieldName = parseVariableName();
                    return parseObjectFieldRhs(metadata, visibilityQualifier, readonlyQualifier, typeDesc, fieldName);
                }

                startContext(ParserRuleContext.VAR_DECL_STMT);
                STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
                return parseVarDeclRhs(metadata, visibilityQualifier, typedBindingPattern, true);
            case OPEN_BRACE_TOKEN: // function body block
            case EQUAL_TOKEN: // external function
                break;
            default:
                break;
        }

        // Treat as function definition.

        // We reach this method only if the func-name is not present.
        STNode name = errorHandler.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_FUNCTION_NAME);

        // Function definition cannot have missing param-names. So validate it.
        funcSignature = validateAndGetFuncParams((STFunctionSignatureNode) funcSignature);

        STNode funcDef = createFuncDefOrMethodDecl(metadata, visibilityQualifier, functionKeyword, isObjectMethod, name,
                funcSignature);
        endContext();
        return funcDef;
    }

    /**
     * Validate the param list and return. If there are params without param-name,
     * then this method will create a new set of params with missing param-name
     * and return.
     *
     * @param signature Function signature
     * @return
     */
    private STNode validateAndGetFuncParams(STFunctionSignatureNode signature) {
        STNode parameters = signature.parameters;
        int paramCount = parameters.bucketCount();
        int index = 0;
        for (; index < paramCount; index++) {
            STNode param = parameters.childInBucket(index);
            switch (param.kind) {
                case REQUIRED_PARAM:
                    STRequiredParameterNode requiredParam = (STRequiredParameterNode) param;
                    if (isEmpty(requiredParam.paramName)) {
                        break;
                    }
                    continue;
                case DEFAULTABLE_PARAM:
                    STDefaultableParameterNode defaultableParam = (STDefaultableParameterNode) param;
                    if (isEmpty(defaultableParam.paramName)) {
                        break;
                    }
                    continue;
                case REST_PARAM:
                    STRestParameterNode restParam = (STRestParameterNode) param;
                    if (isEmpty(restParam.paramName)) {
                        break;
                    }
                    continue;
                default:
                    continue;
            }

            // Stop processing any further.
            break;
        }

        // This is an optimization. If none of the parameters have errors,
        // then we can return the same parameter as is. Here we have optimized
        // the happy path.
        if (index == paramCount) {
            return signature;
        }

        // Otherwise, we create a new param list. This overhead is acceptable, since
        // we reach here only for a erroneous edge-case where a function-definition
        // has a missing name, along with some parameter with a missing name.

        // Add the parameters up to the erroneous param, to the new list.
        STNode updatedParams = getUpdatedParamList(parameters, index);
        return STNodeFactory.createFunctionSignatureNode(signature.openParenToken, updatedParams,
                signature.closeParenToken, signature.returnTypeDesc);
    }

    private STNode getUpdatedParamList(STNode parameters, int index) {
        int paramCount = parameters.bucketCount();
        int newIndex = 0;
        ArrayList<STNode> newParams = new ArrayList<>();
        for (; newIndex < index; newIndex++) {
            newParams.add(parameters.childInBucket(index));
        }

        // From there onwards, create a new param with missing param-name, if the
        // param name is empty. Otherwise, add the same param as is, to the new list.
        for (; newIndex < paramCount; newIndex++) {
            STNode param = parameters.childInBucket(newIndex);
            STNode paramName = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            switch (param.kind) {
                case REQUIRED_PARAM:
                    STRequiredParameterNode requiredParam = (STRequiredParameterNode) param;
                    if (isEmpty(requiredParam.paramName)) {
                        param = STNodeFactory.createRequiredParameterNode(requiredParam.leadingComma,
                                requiredParam.annotations, requiredParam.visibilityQualifier, requiredParam.typeName,
                                paramName);
                    }
                    break;
                case DEFAULTABLE_PARAM:
                    STDefaultableParameterNode defaultableParam = (STDefaultableParameterNode) param;
                    if (isEmpty(defaultableParam.paramName)) {
                        param = STNodeFactory.createDefaultableParameterNode(defaultableParam.leadingComma,
                                defaultableParam.annotations, defaultableParam.visibilityQualifier,
                                defaultableParam.typeName, paramName, defaultableParam.equalsToken,
                                defaultableParam.expression);
                    }
                    break;
                case REST_PARAM:
                    STRestParameterNode restParam = (STRestParameterNode) param;
                    if (isEmpty(restParam.paramName)) {
                        param = STNodeFactory.createRestParameterNode(restParam.leadingComma, restParam.annotations,
                                restParam.typeName, restParam.ellipsisToken, paramName);
                    }
                    break;
                default:
                    break;
            }
            newParams.add(param);
        }

        return STNodeFactory.createNodeList(newParams);
    }

    private boolean isEmpty(STNode node) {
        return node == null;
    }

    /**
     * Parse function keyword. Need to validate the token before consuming,
     * since we can reach here while recovering.
     *
     * @return Parsed node
     */
    private STNode parseFunctionKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FUNCTION_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FUNCTION_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse function name.
     *
     * @return Parsed node
     */
    private STNode parseFunctionName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FUNC_NAME);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse open parenthesis.
     *
     * @param ctx Context of the parenthesis
     * @return Parsed node
     */
    private STNode parseOpenParenthesis(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ctx, ctx);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse close parenthesis.
     *
     * @return Parsed node
     */
    private STNode parseCloseParenthesis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CLOSE_PARENTHESIS);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse parameter list.
     * </p>
     * <code>
     * param-list := required-params [, defaultable-params] [, rest-param]
     *     <br/>&nbsp;| defaultable-params [, rest-param]
     *     <br/>&nbsp;| [rest-param]
     * <br/><br/>
     * required-params := required-param (, required-param)*
     * <br/><br/>
     * required-param := [annots] [public] type-descriptor [param-name]
     * <br/><br/>
     * defaultable-params := defaultable-param (, defaultable-param)*
     * <br/><br/>
     * defaultable-param := [annots] [public] type-descriptor [param-name] default-value
     * <br/><br/>
     * rest-param := [annots] type-descriptor ... [param-name]
     * <br/><br/>
     * param-name := identifier
     * </code>
     *
     * @param isParamNameOptional Whether the param names in the signature is optional or not.
     * @return Parsed node
     */
    private STNode parseParamList(boolean isParamNameOptional) {
        startContext(ParserRuleContext.PARAM_LIST);
        STToken token = peek();
        if (isEndOfParametersList(token.kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse the first parameter. Comma precedes the first parameter doesn't exist.
        ArrayList<STNode> paramsList = new ArrayList<>();
        STNode startingComma = STNodeFactory.createEmptyNode();
        startContext(ParserRuleContext.REQUIRED_PARAM);
        STNode firstParam = parseParameter(startingComma, SyntaxKind.REQUIRED_PARAM, isParamNameOptional);
        SyntaxKind prevParamKind = firstParam.kind;
        paramsList.add(firstParam);

        // Parse follow-up parameters.
        token = peek();
        while (!isEndOfParametersList(token.kind)) {
            switch (prevParamKind) {
                case REST_PARAM:
                    // This is an erroneous scenario, where there are more parameters after
                    // the rest parameter. Log an error, and continue the remainder of the
                    // parameters by removing the order restriction.

                    // TODO: mark the node as erroneous
                    this.errorHandler.reportInvalidNode(token, "cannot have more parameters after the rest-parameter");
                    startContext(ParserRuleContext.REQUIRED_PARAM);
                    break;
                case DEFAULTABLE_PARAM:
                    startContext(ParserRuleContext.DEFAULTABLE_PARAM);
                    break;
                case REQUIRED_PARAM:
                default:
                    startContext(ParserRuleContext.REQUIRED_PARAM);
                    break;

            }

            STNode paramEnd = parseParameterRhs(token.kind);
            if (paramEnd == null) {
                endContext();
                break;
            }

            // context is ended inside parseParameter() method
            STNode param = parseParameter(paramEnd, prevParamKind, isParamNameOptional);
            prevParamKind = param.kind;
            paramsList.add(param);
            token = peek();
        }

        return STNodeFactory.createNodeList(paramsList);
    }

    private STNode parseParameterRhs(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_PAREN_TOKEN:
                return null;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.PARAM_END);

                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseParameterRhs(solution.tokenKind);
        }

    }

    /**
     * Parse a single parameter. Parameter can be a required parameter, a defaultable
     * parameter, or a rest parameter.
     *
     * @param prevParamKind Kind of the parameter that precedes current parameter
     * @param leadingComma Comma that occurs before the param
     * @param isParamNameOptional Whether the param names in the signature is optional or not.
     * @return Parsed node
     */
    private STNode parseParameter(STNode leadingComma, SyntaxKind prevParamKind, boolean isParamNameOptional) {
        STToken token = peek();
        return parseParameter(token.kind, prevParamKind, leadingComma, 1, isParamNameOptional);
    }

    private STNode parseParameter(SyntaxKind prevParamKind, STNode leadingComma, int nextTokenOffset,
                                  boolean isParamNameOptional) {
        return parseParameter(peek().kind, prevParamKind, leadingComma, nextTokenOffset, isParamNameOptional);
    }

    private STNode parseParameter(SyntaxKind nextTokenKind, SyntaxKind prevParamKind, STNode leadingComma,
                                  int nextTokenOffset, boolean isParamNameOptional) {
        STNode annots;
        switch (nextTokenKind) {
            case AT_TOKEN:
                annots = parseAnnotations(nextTokenKind);
                nextTokenKind = peek().kind;
                break;
            case PUBLIC_KEYWORD:
            case IDENTIFIER_TOKEN:
                annots = STNodeFactory.createEmptyNodeList();
                break;
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    annots = STNodeFactory.createNodeList(new ArrayList<>());
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.PARAMETER_START, prevParamKind, leadingComma,
                        nextTokenOffset, isParamNameOptional);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    annots = STNodeFactory.createEmptyNodeList();
                    break;
                }

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                // Since we come here after recovering by insertion, then the current token becomes the next token.
                // So the nextNextToken offset becomes 1.
                return parseParameter(solution.tokenKind, prevParamKind, leadingComma, 0, isParamNameOptional);
        }

        return parseParamGivenAnnots(nextTokenKind, prevParamKind, leadingComma, annots, 1, isParamNameOptional);
    }

    private STNode parseParamGivenAnnots(SyntaxKind prevParamKind, STNode leadingComma, STNode annots,
                                         int nextNextTokenOffset, boolean isFuncDef) {
        return parseParamGivenAnnots(peek().kind, prevParamKind, leadingComma, annots, nextNextTokenOffset, isFuncDef);
    }

    private STNode parseParamGivenAnnots(SyntaxKind nextTokenKind, SyntaxKind prevParamKind, STNode leadingComma,
                                         STNode annots, int nextTokenOffset, boolean isParamNameOptional) {
        STNode qualifier;
        switch (nextTokenKind) {
            case PUBLIC_KEYWORD:
                qualifier = parseQualifier();
                break;
            case IDENTIFIER_TOKEN:
                qualifier = STNodeFactory.createEmptyNode();
                break;
            case AT_TOKEN: // Annotations can't reach here
            default:
                if (isTypeStartingToken(nextTokenKind) && nextTokenKind != SyntaxKind.IDENTIFIER_TOKEN) {
                    qualifier = STNodeFactory.createEmptyNode();
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.PARAMETER_WITHOUT_ANNOTS, prevParamKind,
                        leadingComma, annots, nextTokenOffset, isParamNameOptional);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    qualifier = STNodeFactory.createEmptyNode();
                    break;
                }

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                // Since we come here after recovering by insertion, then the current token becomes the next token.
                // So the nextNextToken offset becomes 1.
                return parseParamGivenAnnots(solution.tokenKind, prevParamKind, leadingComma, annots, 0,
                        isParamNameOptional);
        }

        return parseParamGivenAnnotsAndQualifier(prevParamKind, leadingComma, annots, qualifier, isParamNameOptional);
    }

    private STNode parseParamGivenAnnotsAndQualifier(SyntaxKind prevParamKind, STNode leadingComma, STNode annots,
                                                     STNode qualifier, boolean isParamNameOptional) {
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode param = parseAfterParamType(prevParamKind, leadingComma, annots, qualifier, type, isParamNameOptional);
        endContext();
        return param;
    }

    private STNode parseAfterParamType(SyntaxKind prevParamKind, STNode leadingComma, STNode annots, STNode qualifier,
                                       STNode type, boolean isParamNameOptional) {
        STToken token = peek();
        return parseAfterParamType(token.kind, prevParamKind, leadingComma, annots, qualifier, type,
                isParamNameOptional);
    }

    private STNode parseAfterParamType(SyntaxKind tokenKind, SyntaxKind prevParamKind, STNode leadingComma,
                                       STNode annots, STNode qualifier, STNode type, boolean isParamNameOptional) {
        STNode paramName;
        switch (tokenKind) {
            case ELLIPSIS_TOKEN:
                switchContext(ParserRuleContext.REST_PARAM);
                reportInvalidQualifier(qualifier);
                STNode ellipsis = parseEllipsis();
                if (isParamNameOptional && peek().kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    paramName = STNodeFactory.createEmptyNode();
                } else {
                    paramName = parseVariableName();
                }
                return STNodeFactory.createRestParameterNode(leadingComma, annots, type, ellipsis, paramName);
            case IDENTIFIER_TOKEN:
                paramName = parseVariableName();
                return parseParameterRhs(prevParamKind, leadingComma, annots, qualifier, type, paramName);
            case EQUAL_TOKEN:
                if (!isParamNameOptional) {
                    break;
                }
                // If this is a function-type-desc, then param name is optional, and may not exist
                paramName = STNodeFactory.createEmptyNode();
                return parseParameterRhs(prevParamKind, leadingComma, annots, qualifier, type, paramName);
            default:
                if (!isParamNameOptional) {
                    break;
                }
                // If this is a function-type-desc, then param name is optional, and may not exist
                paramName = STNodeFactory.createEmptyNode();
                return parseParameterRhs(prevParamKind, leadingComma, annots, qualifier, type, paramName);
        }
        STToken token = peek();
        Solution solution = recover(token, ParserRuleContext.AFTER_PARAMETER_TYPE, prevParamKind, leadingComma, annots,
                qualifier, type, isParamNameOptional);

        // If the parser recovered by inserting a token, then try to re-parse the same
        // rule with the inserted token. This is done to pick the correct branch
        // to continue the parsing.
        if (solution.action == Action.REMOVE) {
            return solution.recoveredNode;
        }

        return parseAfterParamType(solution.tokenKind, prevParamKind, leadingComma, annots, qualifier, type,
                isParamNameOptional);

    }

    /**
     * Parse ellipsis.
     *
     * @return Parsed node
     */
    private STNode parseEllipsis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ELLIPSIS_TOKEN) {
            return consume(); // parse '...'
        } else {
            Solution sol = recover(token, ParserRuleContext.ELLIPSIS);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse the right hand side of a required/defaultable parameter.
     * </p>
     * <code>parameter-rhs := [= expression]</code>
     *
     * @param leadingComma Comma that precedes this parameter
     * @param prevParamKind Kind of the parameter that precedes current parameter
     * @param annots Annotations attached to the parameter
     * @param qualifier Visibility qualifier
     * @param type Type descriptor
     * @param paramName Name of the parameter
     * @return Parsed parameter node
     */
    private STNode parseParameterRhs(SyntaxKind prevParamKind, STNode leadingComma, STNode annots, STNode qualifier,
                                     STNode type, STNode paramName) {
        STToken token = peek();
        return parseParameterRhs(token.kind, prevParamKind, leadingComma, annots, qualifier, type, paramName);
    }

    private STNode parseParameterRhs(SyntaxKind tokenKind, SyntaxKind prevParamKind, STNode leadingComma, STNode annots,
                                     STNode qualifier, STNode type, STNode paramName) {
        // Required parameters
        if (isEndOfParameter(tokenKind)) {
            if (prevParamKind == SyntaxKind.DEFAULTABLE_PARAM) {
                // This is an erroneous scenario, where a required parameters comes after
                // a defaulatble parameter. Log an error, and continue.

                // TODO: mark the node as erroneous
                this.errorHandler.reportInvalidNode(peek(),
                        "cannot have a required parameter after a defaultable parameter");
            }

            return STNodeFactory.createRequiredParameterNode(leadingComma, annots, qualifier, type, paramName);
        } else if (tokenKind == SyntaxKind.EQUAL_TOKEN) {
            // If we were processing required params so far and found a defualtable
            // parameter, then switch the context to defaultable params.
            if (prevParamKind == SyntaxKind.REQUIRED_PARAM) {
                switchContext(ParserRuleContext.DEFAULTABLE_PARAM);
            }

            // Defaultable parameters
            STNode equal = parseAssignOp();
            STNode expr = parseExpression();
            return STNodeFactory.createDefaultableParameterNode(leadingComma, annots, qualifier, type, paramName, equal,
                    expr);
        } else {
            STToken token = peek();
            Solution solution = recover(token, ParserRuleContext.PARAMETER_NAME_RHS, prevParamKind, leadingComma,
                    annots, qualifier, type, paramName);

            // If the parser recovered by inserting a token, then try to re-parse the same
            // rule with the inserted token. This is done to pick the correct branch
            // to continue the parsing.
            if (solution.action == Action.REMOVE) {
                return solution.recoveredNode;
            }

            return parseParameterRhs(solution.tokenKind, prevParamKind, leadingComma, annots, qualifier, type,
                    paramName);
        }
    }

    /**
     * Parse comma.
     *
     * @return Parsed node
     */
    private STNode parseComma() {
        STToken token = peek();
        if (token.kind == SyntaxKind.COMMA_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.COMMA);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse return type descriptor of a function. A return type descriptor has the following structure.
     *
     * <code>return-type-descriptor := [ returns annots type-descriptor ]</code>
     *
     * @return Parsed node
     */
    private STNode parseFuncReturnTypeDescriptor() {
        return parseFuncReturnTypeDescriptor(peek().kind);
    }

    private STNode parseFuncReturnTypeDescriptor(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case OPEN_BRACE_TOKEN: // func-body block
            case EQUAL_TOKEN: // external func
                return STNodeFactory.createEmptyNode();
            case RETURNS_KEYWORD:
                break;
            default:
                STToken nextNextToken = getNextNextToken(nextTokenKind);
                if (nextNextToken.kind == SyntaxKind.RETURNS_KEYWORD) {
                    break;
                }

                return STNodeFactory.createEmptyNode();
        }

        STNode returnsKeyword = parseReturnsKeyword();
        STNode annot = parseAnnotations();
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC);
        return STNodeFactory.createReturnTypeDescriptorNode(returnsKeyword, annot, type);
    }

    /**
     * Parse 'returns' keyword.
     *
     * @return Return-keyword node
     */
    private STNode parseReturnsKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RETURNS_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.RETURNS_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse a type descriptor. A type descriptor has the following structure.
     * </p>
     * <code>type-descriptor :=
     *      &nbsp;simple-type-descriptor<br/>
     *      &nbsp;| structured-type-descriptor<br/>
     *      &nbsp;| behavioral-type-descriptor<br/>
     *      &nbsp;| singleton-type-descriptor<br/>
     *      &nbsp;| union-type-descriptor<br/>
     *      &nbsp;| optional-type-descriptor<br/>
     *      &nbsp;| any-type-descriptor<br/>
     *      &nbsp;| anydata-type-descriptor<br/>
     *      &nbsp;| byte-type-descriptor<br/>
     *      &nbsp;| json-type-descriptor<br/>
     *      &nbsp;| type-descriptor-reference<br/>
     *      &nbsp;| ( type-descriptor )
     * <br/>
     * type-descriptor-reference := qualified-identifier</code>
     *
     * @return Parsed node
     */
    private STNode parseTypeDescriptor(ParserRuleContext context) {
        return parseTypeDescriptor(context, false);
    }

    private STNode parseTypeDescriptor(ParserRuleContext context, boolean isTypedBindingPattern) {
        startContext(context);
        STNode typeDesc = parseTypeDescriptorInternal(context, isTypedBindingPattern);
        endContext();
        return typeDesc;
    }

    private STNode parseTypeDescriptorInternal(ParserRuleContext context) {
        return parseTypeDescriptorInternal(context, false);
    }

    private STNode parseTypeDescriptorInternal(ParserRuleContext context, boolean isTypedBindingPattern) {
        STToken token = peek();
        STNode typeDesc = parseTypeDescriptorInternal(token.kind, context);
        return parseComplexTypeDescriptor(typeDesc, context, isTypedBindingPattern);
    }

    /**
     * This will handle the parsing of optional,array,union type desc to infinite length.
     *
     * @param typeDesc
     *
     * @return Parsed type descriptor node
     */
    private STNode parseComplexTypeDescriptor(STNode typeDesc, ParserRuleContext context,
                                              boolean isTypedBindingPattern) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            // If next token after a type descriptor is <code>?</code> then it is an optional type descriptor
            case QUESTION_MARK_TOKEN:
                return parseComplexTypeDescriptor(parseOptionalTypeDescriptor(typeDesc), context,
                        isTypedBindingPattern);
            // If next token after a type descriptor is <code>[</code> then it is an array type descriptor
            case OPEN_BRACKET_TOKEN:
                if (isTypedBindingPattern) { // checking for typedesc parsing originating at typed-binding-pattern
                    return typeDesc;
                }
                return parseComplexTypeDescriptor(parseArrayTypeDescriptor(typeDesc), context, isTypedBindingPattern);
            // If next token after a type descriptor is <code>[</code> then it is an array type descriptor
            case PIPE_TOKEN:
                return parseUnionTypeDescriptor(typeDesc, context);
            // If next token after a type descriptor is <code> & </code> then it is an array type descriptor
            case BITWISE_AND_TOKEN:
                return parseIntersectionTypeDescriptor(typeDesc, context);
            default:
                return typeDesc;
        }
    }

    /**
     * <p>
     * Parse a type descriptor, given the next token kind.
     * </p>
     * If the preceding token is <code>?</code> then it is an optional type descriptor
     *
     * @param tokenKind Next token kind
     * @param context Current context
     * @return Parsed node
     */
    private STNode parseTypeDescriptorInternal(SyntaxKind tokenKind, ParserRuleContext context) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
                return parseTypeReference();
            case RECORD_KEYWORD:
                // Record type descriptor
                return parseRecordTypeDescriptor();
            case OBJECT_KEYWORD:
            case ABSTRACT_KEYWORD:
            case CLIENT_KEYWORD:
                // Object type descriptor
                return parseObjectTypeDescriptor();
            case OPEN_PAREN_TOKEN:
                return parseNilOrParenthesisedTypeDesc();
            case MAP_KEYWORD: // map type desc
            case FUTURE_KEYWORD: // future type desc
            case TYPEDESC_KEYWORD: // typedesc type desc
                return parseParameterizedTypeDescriptor();
            case ERROR_KEYWORD: // error type descriptor
                return parseErrorTypeDescriptor();
            case STREAM_KEYWORD: // stream type desc
                return parseStreamTypeDescriptor();
            case TABLE_KEYWORD: // table type desc
                return parseTableTypeDescriptor();
            case FUNCTION_KEYWORD:
                return parseFunctionTypeDesc();
            case OPEN_BRACKET_TOKEN:
                return parseTupleTypeDesc();
            default:
                if (isSingletonTypeDescStart(tokenKind, true)) {
                    return parseSingletonTypeDesc();
                }
                if (isSimpleType(tokenKind)) {
                    return parseSimpleTypeDescriptor();
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TYPE_DESCRIPTOR, context);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTypeDescriptorInternal(solution.tokenKind, context);
        }
    }

    private STNode parseNilOrParenthesisedTypeDesc() {
        STNode openParen = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        return parseNilOrParenthesisedTypeDescRhs(openParen);
    }

    private STNode parseNilOrParenthesisedTypeDescRhs(STNode openParen) {
        return parseNilOrParenthesisedTypeDescRhs(peek().kind, openParen);
    }

    private STNode parseNilOrParenthesisedTypeDescRhs(SyntaxKind nextTokenKind, STNode openParen) {
        STNode closeParen;
        switch (nextTokenKind) {
            case CLOSE_PAREN_TOKEN:
                closeParen = parseCloseParenthesis();
                return STNodeFactory.createNilTypeDescriptorNode(openParen, closeParen);
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    STNode typedesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_PARENTHESIS);
                    closeParen = parseCloseParenthesis();
                    return STNodeFactory.createParenthesisedTypeDescriptorNode(openParen, typedesc, closeParen);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.NIL_OR_PARENTHESISED_TYPE_DESC_RHS, openParen);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseNilOrParenthesisedTypeDescRhs(solution.tokenKind, openParen);
        }
    }

    /**
     * Parse simple type descriptor.
     *
     * @return Parsed node
     */
    private STNode parseSimpleTypeDescriptor() {
        STToken node = peek();
        if (isSimpleType(node.kind)) {
            STToken token = consume();
            SyntaxKind typeKind = getTypeSyntaxKind(token.kind);
            return STNodeFactory.createBuiltinSimpleNameReferenceNode(typeKind, token);
        } else {
            Solution sol = recover(peek(), ParserRuleContext.SIMPLE_TYPE_DESCRIPTOR);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse function body. A function body has the following structure.
     * </p>
     * <code>
     * function-body := function-body-block | external-function-body
     * external-function-body := = annots external ;
     * function-body-block := { [default-worker-init, named-worker-decl+] default-worker }
     * </code>
     *
     * @param isObjectMethod Flag indicating whether this is an object-method
     * @return Parsed node
     */
    private STNode parseFunctionBody(boolean isObjectMethod) {
        STToken token = peek();
        return parseFunctionBody(token.kind, isObjectMethod);
    }

    /**
     * Parse function body, given the next token kind.
     *
     * @param tokenKind Next token kind
     * @param isObjectMethod Flag indicating whether this is an object-method
     * @return Parsed node
     */
    protected STNode parseFunctionBody(SyntaxKind tokenKind, boolean isObjectMethod) {
        switch (tokenKind) {
            case EQUAL_TOKEN:
                return parseExternalFunctionBody();
            case OPEN_BRACE_TOKEN:
                return parseFunctionBodyBlock(false);
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return parseExpressionFuncBody(false, false);
            case SEMICOLON_TOKEN:
                if (isObjectMethod) {
                    return parseSemicolon();
                }

                // else fall through
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FUNC_BODY, isObjectMethod);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.

                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                // If the recovered token is not something that can be re-parsed,
                // then don't try to re-parse the same rule.
                if (solution.tokenKind == SyntaxKind.NONE) {
                    return STNodeFactory.createMissingToken(solution.tokenKind);
                }

                return parseFunctionBody(solution.tokenKind, isObjectMethod);
        }
    }

    /**
     * <p>
     * Parse function body block. A function body block has the following structure.
     * </p>
     *
     * <code>
     * function-body-block := { [default-worker-init, named-worker-decl+] default-worker }<br/>
     * default-worker-init := sequence-stmt<br/>
     * default-worker := sequence-stmt<br/>
     * named-worker-decl := worker worker-name return-type-descriptor { sequence-stmt }<br/>
     * worker-name := identifier<br/>
     * </code>
     *
     * @param isAnonFunc Flag indicating whether the func body belongs to an anonymous function
     * @return Parsed node
     */
    private STNode parseFunctionBodyBlock(boolean isAnonFunc) {
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        STNode openBrace = parseOpenBrace();
        STToken token = peek();

        ArrayList<STNode> firstStmtList = new ArrayList<>();
        ArrayList<STNode> workers = new ArrayList<>();
        ArrayList<STNode> secondStmtList = new ArrayList<>();

        ParserRuleContext currentCtx = ParserRuleContext.DEFAULT_WORKER_INIT;
        boolean hasNamedWorkers = false;
        while (!isEndOfFuncBodyBlock(token.kind, isAnonFunc)) {
            STNode stmt = parseStatement();
            if (stmt == null) {
                break;
            }

            switch (currentCtx) {
                case DEFAULT_WORKER_INIT:
                    if (stmt.kind != SyntaxKind.NAMED_WORKER_DECLARATION) {
                        firstStmtList.add(stmt);
                        break;
                    }
                    // We come here when we find the first named-worker-decl.
                    // Switch to parsing named-workers.
                    currentCtx = ParserRuleContext.NAMED_WORKERS;
                    hasNamedWorkers = true;
                    // fall through
                case NAMED_WORKERS:
                    if (stmt.kind == SyntaxKind.NAMED_WORKER_DECLARATION) {
                        workers.add(stmt);
                        break;
                    }
                    // Otherwise switch to parsing default-worker
                    currentCtx = ParserRuleContext.DEFAULT_WORKER;
                    // fall through
                case DEFAULT_WORKER:
                default:
                    if (stmt.kind == SyntaxKind.NAMED_WORKER_DECLARATION) {
                        this.errorHandler.reportInvalidNode(null, "named-workers are not allowed here");
                        break;
                    }
                    secondStmtList.add(stmt);
                    break;
            }
            token = peek();
        }

        STNode namedWorkersList;
        STNode statements;
        if (hasNamedWorkers) {
            STNode workerInitStatements = STNodeFactory.createNodeList(firstStmtList);
            STNode namedWorkers = STNodeFactory.createNodeList(workers);
            namedWorkersList = STNodeFactory.createNamedWorkerDeclarator(workerInitStatements, namedWorkers);
            statements = STNodeFactory.createNodeList(secondStmtList);
        } else {
            namedWorkersList = STNodeFactory.createEmptyNode();
            statements = STNodeFactory.createNodeList(firstStmtList);
        }

        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createFunctionBodyBlockNode(openBrace, namedWorkersList, statements, closeBrace);
    }

    private boolean isEndOfFuncBodyBlock(SyntaxKind nextTokenKind, boolean isAnonFunc) {
        if (isAnonFunc) {
            switch (nextTokenKind) {
                case CLOSE_BRACE_TOKEN:
                case CLOSE_PAREN_TOKEN:
                case CLOSE_BRACKET_TOKEN:
                case OPEN_BRACE_TOKEN:
                case SEMICOLON_TOKEN:
                case COMMA_TOKEN:
                case PUBLIC_KEYWORD:
                case EOF_TOKEN:
                case EQUAL_TOKEN:
                case BACKTICK_TOKEN:
                    return true;
                default:
                    break;
            }
        }

        return isEndOfStatements();
    }

    private boolean isEndOfRecordTypeNode(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case TYPE_KEYWORD:
            case PUBLIC_KEYWORD:
            default:
                return endOfModuleLevelNode(1);
        }
    }

    private boolean isEndOfObjectTypeNode() {
        return endOfModuleLevelNode(1, true);
    }

    private boolean isEndOfStatements() {
        switch (peek().kind) {
            case RESOURCE_KEYWORD:
                return true;
            default:
                return endOfModuleLevelNode(1);
        }
    }

    private boolean endOfModuleLevelNode(int peekIndex) {
        return endOfModuleLevelNode(peekIndex, false);
    }

    private boolean endOfModuleLevelNode(int peekIndex, boolean isObject) {
        switch (peek(peekIndex).kind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
            case IMPORT_KEYWORD:
            case CONST_KEYWORD:
            case ANNOTATION_KEYWORD:
            case LISTENER_KEYWORD:
                return true;
            case SERVICE_KEYWORD:
                return isServiceDeclStart(ParserRuleContext.OBJECT_MEMBER, 1);
            case PUBLIC_KEYWORD:
                return endOfModuleLevelNode(peekIndex + 1, isObject);
            case FUNCTION_KEYWORD:
                if (isObject) {
                    return false;
                }

                // if function keyword follows by a identifier treat is as
                // the function name. Only function def can have func-name
                return peek(peekIndex + 1).kind == SyntaxKind.IDENTIFIER_TOKEN;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token is an end of a parameter.
     *
     * @param tokenKind Next token kind
     * @return <code>true</code> if the token represents an end of a parameter. <code>false</code> otherwise
     */
    private boolean isEndOfParameter(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case COMMA_TOKEN:
            case RETURNS_KEYWORD:
            case TYPE_KEYWORD:
            case IF_KEYWORD:
            case WHILE_KEYWORD:
            case AT_TOKEN:
                return true;
            default:
                return endOfModuleLevelNode(1);
        }
    }

    /**
     * Check whether the given token is an end of a parameter-list.
     *
     * @param tokenKind Next token kind
     * @return <code>true</code> if the token represents an end of a parameter-list. <code>false</code> otherwise
     */
    private boolean isEndOfParametersList(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case CLOSE_PAREN_TOKEN:
            case SEMICOLON_TOKEN:
            case RETURNS_KEYWORD:
            case TYPE_KEYWORD:
            case IF_KEYWORD:
            case WHILE_KEYWORD:
                return true;
            default:
                return endOfModuleLevelNode(1);
        }
    }

    /**
     * Parse type reference or variable reference.
     *
     * @return Parsed node
     */
    private STNode parseStatementStartIdentifier() {
        return parseQualifiedIdentifier(ParserRuleContext.TYPE_NAME_OR_VAR_NAME);
    }

    /**
     * Parse variable name.
     *
     * @return Parsed node
     */
    private STNode parseVariableName() {
        STToken token = peek();
        return parseVariableName(token.kind);
    }

    /**
     * Parse variable name.
     *
     * @return Parsed node
     */
    private STNode parseVariableName(SyntaxKind tokenKind) {
        if (tokenKind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.VARIABLE_NAME);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse open brace.
     *
     * @return Parsed node
     */
    private STNode parseOpenBrace() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACE_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.OPEN_BRACE);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse close brace.
     *
     * @return Parsed node
     */
    private STNode parseCloseBrace() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACE_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CLOSE_BRACE);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse external function body. An external function body has the following structure.
     * </p>
     * <code>
     * external-function-body := = annots external ;
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseExternalFunctionBody() {
        startContext(ParserRuleContext.EXTERNAL_FUNC_BODY);
        STNode assign = parseAssignOp();
        STNode annotation = parseAnnotations();
        STNode externalKeyword = parseExternalKeyword();
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createExternalFunctionBodyNode(assign, annotation, externalKeyword, semicolon);
    }

    /**
     * Parse semicolon.
     *
     * @return Parsed node
     */
    private STNode parseSemicolon() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SEMICOLON_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.SEMICOLON);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse <code>external</code> keyword.
     *
     * @return Parsed node
     */
    private STNode parseExternalKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.EXTERNAL_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.EXTERNAL_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /*
     * Operators
     */

    /**
     * Parse assign operator.
     *
     * @return Parsed node
     */
    private STNode parseAssignOp() {
        STToken token = peek();
        if (token.kind == SyntaxKind.EQUAL_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ASSIGN_OP);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse binary operator.
     *
     * @return Parsed node
     */
    private STNode parseBinaryOperator() {
        STToken token = peek();
        if (isBinaryOperator(token.kind)) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.BINARY_OPERATOR);
            return sol.recoveredNode;
        }
    }

    /**
     * Check whether the given token kind is a binary operator.
     *
     * @param kind STToken kind
     * @return <code>true</code> if the token kind refers to a binary operator. <code>false</code> otherwise
     */
    private boolean isBinaryOperator(SyntaxKind kind) {
        switch (kind) {
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
            case PERCENT_TOKEN:
            case DOUBLE_LT_TOKEN:
            case DOUBLE_GT_TOKEN:
            case TRIPPLE_GT_TOKEN:
            case ELLIPSIS_TOKEN:
            case DOUBLE_DOT_LT_TOKEN:
            case ELVIS_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the precedence of a given operator.
     *
     * @param binaryOpKind Operator kind
     * @return Precedence of the given operator
     */
    private OperatorPrecedence getOpPrecedence(SyntaxKind binaryOpKind) {
        switch (binaryOpKind) {
            case ASTERISK_TOKEN: // multiplication
            case SLASH_TOKEN: // division
            case PERCENT_TOKEN: // remainder
                return OperatorPrecedence.MULTIPLICATIVE;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return OperatorPrecedence.ADDITIVE;
            case GT_TOKEN:
            case LT_TOKEN:
            case GT_EQUAL_TOKEN:
            case LT_EQUAL_TOKEN:
            case IS_KEYWORD:
                return OperatorPrecedence.BINARY_COMPARE;
            case DOT_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case OPEN_PAREN_TOKEN:
            case ANNOT_CHAINING_TOKEN:
            case OPTIONAL_CHAINING_TOKEN:
            case DOT_LT_TOKEN:
            case SLASH_LT_TOKEN:
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
            case SLASH_ASTERISK_TOKEN:
                return OperatorPrecedence.MEMBER_ACCESS;
            case DOUBLE_EQUAL_TOKEN:
            case TRIPPLE_EQUAL_TOKEN:
            case NOT_EQUAL_TOKEN:
            case NOT_DOUBLE_EQUAL_TOKEN:
                return OperatorPrecedence.EQUALITY;
            case BITWISE_AND_TOKEN:
                return OperatorPrecedence.BITWISE_AND;
            case BITWISE_XOR_TOKEN:
                return OperatorPrecedence.BITWISE_XOR;
            case PIPE_TOKEN:
                return OperatorPrecedence.BITWISE_OR;
            case LOGICAL_AND_TOKEN:
                return OperatorPrecedence.LOGICAL_AND;
            case LOGICAL_OR_TOKEN:
                return OperatorPrecedence.LOGICAL_OR;
            case RIGHT_ARROW_TOKEN:
                return OperatorPrecedence.REMOTE_CALL_ACTION;
            case RIGHT_DOUBLE_ARROW_TOKEN:
            case SYNC_SEND_TOKEN:
                return OperatorPrecedence.ACTION;
            case DOUBLE_LT_TOKEN:
            case DOUBLE_GT_TOKEN:
            case TRIPPLE_GT_TOKEN:
                return OperatorPrecedence.SHIFT;
            case ELLIPSIS_TOKEN:
            case DOUBLE_DOT_LT_TOKEN:
                return OperatorPrecedence.RANGE;
            case ELVIS_TOKEN:
                return OperatorPrecedence.ELVIS_CONDITIONAL;
            case QUESTION_MARK_TOKEN:
            case COLON_TOKEN:
                return OperatorPrecedence.CONDITIONAL;
            default:
                throw new UnsupportedOperationException("Unsupported binary operator '" + binaryOpKind + "'");
        }
    }

    /**
     * <p>
     * Get the operator kind to insert during recovery, given the precedence level.
     * </p>
     *
     * @param opPrecedenceLevel Precedence of the given operator
     * @return Kind of the operator to insert
     */
    private SyntaxKind getBinaryOperatorKindToInsert(OperatorPrecedence opPrecedenceLevel) {
        switch (opPrecedenceLevel) {
            case UNARY:
            case ACTION:
            case EXPRESSION_ACTION:
            case REMOTE_CALL_ACTION:
            case ANON_FUNC_OR_LET:
            case QUERY:
                // If the current precedence level is unary/action, then we return
                // the binary operator with closest precedence level to it.
                // Therefore fall through
            case MULTIPLICATIVE:
                return SyntaxKind.ASTERISK_TOKEN;
            case ADDITIVE:
                return SyntaxKind.PLUS_TOKEN;
            case SHIFT:
                return SyntaxKind.DOUBLE_LT_TOKEN;
            case RANGE:
                return SyntaxKind.ELLIPSIS_TOKEN;
            case BINARY_COMPARE:
                return SyntaxKind.LT_TOKEN;
            case EQUALITY:
                return SyntaxKind.DOUBLE_EQUAL_TOKEN;
            case BITWISE_AND:
                return SyntaxKind.BITWISE_AND_TOKEN;
            case BITWISE_XOR:
                return SyntaxKind.BITWISE_XOR_TOKEN;
            case BITWISE_OR:
                return SyntaxKind.PIPE_TOKEN;
            case LOGICAL_AND:
                return SyntaxKind.LOGICAL_AND_TOKEN;
            case LOGICAL_OR:
                return SyntaxKind.LOGICAL_OR_TOKEN;
            case ELVIS_CONDITIONAL:
                return SyntaxKind.ELVIS_TOKEN;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported operator precedence level'" + opPrecedenceLevel + "'");
        }
    }

    /**
     * <p>
     * Parse a module type definition.
     * </p>
     * <code>module-type-defn := metadata [public] type identifier type-descriptor ;</code>
     *
     * @param metadata Metadata
     * @param qualifier Visibility qualifier
     * @return Parsed node
     */
    private STNode parseModuleTypeDefinition(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.MODULE_TYPE_DEFINITION);
        STNode typeKeyword = parseTypeKeyword();
        STNode typeName = parseTypeName();
        STNode typeDescriptor = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_DEF);
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createTypeDefinitionNode(metadata, qualifier, typeKeyword, typeName, typeDescriptor,
                semicolon);
    }

    /**
     * Parse type keyword.
     *
     * @return Parsed node
     */
    private STNode parseTypeKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TYPE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TYPE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse type name.
     *
     * @return Parsed node
     */
    private STNode parseTypeName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TYPE_NAME);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse record type descriptor. A record type descriptor body has the following structure.
     * </p>
     *
     * <code>record-type-descriptor := inclusive-record-type-descriptor | exclusive-record-type-descriptor
     * <br/><br/>inclusive-record-type-descriptor := record { field-descriptor* }
     * <br/><br/>exclusive-record-type-descriptor := record {| field-descriptor* [record-rest-descriptor] |}
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseRecordTypeDescriptor() {
        startContext(ParserRuleContext.RECORD_TYPE_DESCRIPTOR);
        STNode recordKeyword = parseRecordKeyword();
        STNode bodyStartDelimiter = parseRecordBodyStartDelimiter();

        boolean isInclusive = bodyStartDelimiter.kind == SyntaxKind.OPEN_BRACE_TOKEN;
        STNode fields = parseFieldDescriptors(isInclusive);

        STNode bodyEndDelimiter = parseRecordBodyCloseDelimiter(bodyStartDelimiter.kind);
        endContext();

        return STNodeFactory.createRecordTypeDescriptorNode(recordKeyword, bodyStartDelimiter, fields,
                bodyEndDelimiter);
    }

    /**
     * Parse record body start delimiter.
     *
     * @return Parsed node
     */
    private STNode parseRecordBodyStartDelimiter() {
        STToken token = peek();
        return parseRecordBodyStartDelimiter(token.kind);
    }

    private STNode parseRecordBodyStartDelimiter(SyntaxKind kind) {
        switch (kind) {
            case OPEN_BRACE_PIPE_TOKEN:
                return parseClosedRecordBodyStart();
            case OPEN_BRACE_TOKEN:
                return parseOpenBrace();
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RECORD_BODY_START);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseRecordBodyStartDelimiter(solution.tokenKind);
        }
    }

    /**
     * Parse closed-record body start delimiter.
     *
     * @return Parsed node
     */
    private STNode parseClosedRecordBodyStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACE_PIPE_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CLOSED_RECORD_BODY_START);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse record body close delimiter.
     *
     * @return Parsed node
     */
    private STNode parseRecordBodyCloseDelimiter(SyntaxKind startingDelimeter) {
        switch (startingDelimeter) {
            case OPEN_BRACE_PIPE_TOKEN:
                return parseClosedRecordBodyEnd();
            case OPEN_BRACE_TOKEN:
                return parseCloseBrace();
            default:
                // Ideally should never reach here.

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RECORD_BODY_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseRecordBodyCloseDelimiter(solution.tokenKind);
        }
    }

    /**
     * Parse closed-record body end delimiter.
     *
     * @return Parsed node
     */
    private STNode parseClosedRecordBodyEnd() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACE_PIPE_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CLOSED_RECORD_BODY_END);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse record keyword.
     *
     * @return Parsed node
     */
    private STNode parseRecordKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RECORD_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.RECORD_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse field descriptors.
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseFieldDescriptors(boolean isInclusive) {
        ArrayList<STNode> recordFields = new ArrayList<>();
        STToken token = peek();
        boolean endOfFields = false;
        while (!isEndOfRecordTypeNode(token.kind)) {
            STNode field = parseFieldOrRestDescriptor(isInclusive);
            if (field == null) {
                endOfFields = true;
                break;
            }
            recordFields.add(field);
            token = peek();

            if (field.kind == SyntaxKind.RECORD_REST_TYPE) {
                break;
            }
        }

        // Following loop will only run if there are more fields after the rest type descriptor.
        // Try to parse them and mark as invalid.
        while (!endOfFields && !isEndOfRecordTypeNode(token.kind)) {
            parseFieldOrRestDescriptor(isInclusive);
            // TODO: Mark these nodes as error/invalid nodes.
            this.errorHandler.reportInvalidNode(token, "cannot have more fields after the rest type descriptor");
            token = peek();
        }

        return STNodeFactory.createNodeList(recordFields);
    }

    /**
     * <p>
     * Parse field descriptor or rest descriptor.
     * </p>
     *
     * <code>
     * <br/><br/>field-descriptor := individual-field-descriptor | record-type-reference
     * <br/><br/><br/>individual-field-descriptor := metadata type-descriptor field-name [? | default-value] ;
     * <br/><br/>field-name := identifier
     * <br/><br/>default-value := = expression
     * <br/><br/>record-type-reference := * type-reference ;
     * <br/><br/>record-rest-descriptor := type-descriptor ... ;
     * </code>
     *
     * @return Parsed node
     */

    private STNode parseFieldOrRestDescriptor(boolean isInclusive) {
        return parseFieldOrRestDescriptor(peek().kind, isInclusive);
    }

    private STNode parseFieldOrRestDescriptor(SyntaxKind nextTokenKind, boolean isInclusive) {
        switch (nextTokenKind) {
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
                return null;
            case ASTERISK_TOKEN:
                // record-type-reference
                startContext(ParserRuleContext.RECORD_FIELD);
                STNode asterisk = consume();
                STNode type = parseTypeReference();
                STNode semicolonToken = parseSemicolon();
                endContext();
                return STNodeFactory.createTypeReferenceNode(asterisk, type, semicolonToken);
            case AT_TOKEN:
            case DOCUMENTATION_LINE:
                startContext(ParserRuleContext.RECORD_FIELD);
                STNode metadata = parseMetaData(nextTokenKind);
                nextTokenKind = peek().kind;
                return parseRecordField(nextTokenKind, isInclusive, metadata);
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    // individual-field-descriptor
                    startContext(ParserRuleContext.RECORD_FIELD);
                    metadata = createEmptyMetadata();
                    return parseRecordField(nextTokenKind, isInclusive, metadata);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RECORD_FIELD_OR_RECORD_END, isInclusive);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseFieldOrRestDescriptor(solution.tokenKind, isInclusive);
        }
    }

    private STNode parseRecordField(SyntaxKind nextTokenKind, boolean isInclusive, STNode metadata) {
        if (nextTokenKind != SyntaxKind.READONLY_KEYWORD) {
            STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
            STNode fieldOrRestDesc = parseFieldDescriptor(isInclusive, metadata, type);
            endContext();
            return fieldOrRestDesc;
        }

        // If the readonly-keyword is present, check whether its qualifier
        // or the readonly-type-desc.
        STNode type;
        STNode fieldOrRestDesc;
        STNode readOnlyQualifier;
        readOnlyQualifier = parseReadonlyKeyword();
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STNode fieldNameOrTypeDesc = parseQualifiedIdentifier(ParserRuleContext.RECORD_FIELD_NAME_OR_TYPE_NAME);
            if (fieldNameOrTypeDesc.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                // readonly a:b
                // Then treat "a:b" as the type-desc
                type = fieldNameOrTypeDesc;
            } else {
                // readonly a
                nextToken = peek();
                switch (nextToken.kind) {
                    case SEMICOLON_TOKEN: // readonly a;
                    case EQUAL_TOKEN: // readonly a =
                        // Then treat "readonly" as type-desc, and "a" as the field-name
                        type = readOnlyQualifier;
                        readOnlyQualifier = STNodeFactory.createEmptyNode();
                        return parseFieldDescriptorRhs(metadata, readOnlyQualifier, type, fieldNameOrTypeDesc);
                    default:
                        // else,
                        type = fieldNameOrTypeDesc;
                        break;
                }
            }
        } else if (isTypeStartingToken(nextToken.kind)) {
            type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
        } else {
            type = parseComplexTypeDescriptor(readOnlyQualifier, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD, false);
            readOnlyQualifier = STNodeFactory.createEmptyNode();
        }

        fieldOrRestDesc = parseIndividualRecordField(metadata, readOnlyQualifier, type);
        endContext();
        return fieldOrRestDesc;
    }

    private STNode parseFieldDescriptor(boolean isInclusive, STNode metadata, STNode type) {
        if (isInclusive) {
            STNode readOnlyQualifier = STNodeFactory.createEmptyNode();
            return parseIndividualRecordField(metadata, readOnlyQualifier, type);
        } else {
            return parseFieldOrRestDescriptorRhs(metadata, type);
        }
    }

    private STNode parseIndividualRecordField(STNode metadata, STNode readOnlyQualifier, STNode type) {
        STNode fieldName = parseVariableName();
        return parseFieldDescriptorRhs(metadata, readOnlyQualifier, type, fieldName);
    }

    /**
     * Parse type reference.
     * <code>type-reference := identifier | qualified-identifier</code>
     *
     * @return Type reference node
     */
    private STNode parseTypeReference() {
        return parseQualifiedIdentifier(ParserRuleContext.TYPE_REFERENCE);
    }

    /**
     * Parse identifier or qualified identifier.
     *
     * @return Identifier node
     */
    private STNode parseQualifiedIdentifier(ParserRuleContext currentCtx) {
        STToken token = peek();
        STNode typeRefOrPkgRef;
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            typeRefOrPkgRef = consume();
        } else {
            Solution sol = recover(token, currentCtx);
            if (sol.action == Action.REMOVE) {
                return sol.recoveredNode;
            }

            typeRefOrPkgRef = sol.recoveredNode;
        }

        return parseQualifiedIdentifier(typeRefOrPkgRef);
    }

    /**
     * Parse identifier or qualified identifier, given the starting identifier.
     *
     * @param identifier Starting identifier
     * @return Parse node
     */
    private STNode parseQualifiedIdentifier(STNode identifier) {
        STToken nextToken = peek(1);
        if (nextToken.kind != SyntaxKind.COLON_TOKEN) {
            return STNodeFactory.createSimpleNameReferenceNode(identifier);
        }

        STToken nextNextToken = peek(2);
        if (nextNextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STToken colon = consume();
            STToken varOrFuncName = consume();
            return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
        } else {
            this.errorHandler.removeInvalidToken();
            return parseQualifiedIdentifier(identifier);
        }
    }

    /**
     * Parse RHS of a field or rest type descriptor.
     *
     * @param metadata Metadata
     * @param type Type descriptor
     * @return Parsed node
     */
    private STNode parseFieldOrRestDescriptorRhs(STNode metadata, STNode type) {
        STToken token = peek();
        return parseFieldOrRestDescriptorRhs(token.kind, metadata, type);
    }

    private STNode parseFieldOrRestDescriptorRhs(SyntaxKind kind, STNode metadata, STNode type) {
        switch (kind) {
            case ELLIPSIS_TOKEN:
                // TODO: report error for invalid metadata
                STNode ellipsis = parseEllipsis();
                STNode semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordRestDescriptorNode(type, ellipsis, semicolonToken);
            case IDENTIFIER_TOKEN:
                STNode readonlyQualifier = STNodeFactory.createEmptyNode();
                return parseIndividualRecordField(metadata, readonlyQualifier, type);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FIELD_OR_REST_DESCIPTOR_RHS, metadata, type);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseFieldOrRestDescriptorRhs(solution.tokenKind, metadata, type);
        }
    }

    /**
     * <p>
     * Parse field descriptor rhs.
     * </p>
     *
     * @param metadata Metadata
     * @param readonlyQualifier Readonly qualifier
     * @param type Type descriptor
     * @param fieldName Field name
     * @return Parsed node
     */
    private STNode parseFieldDescriptorRhs(STNode metadata, STNode readonlyQualifier, STNode type, STNode fieldName) {
        STToken token = peek();
        return parseFieldDescriptorRhs(token.kind, metadata, readonlyQualifier, type, fieldName);
    }

    /**
     * <p>
     * Parse field descriptor rhs.
     * </p>
     *
     * <code>
     * field-descriptor := [? | default-value] ;
     * <br/>default-value := = expression
     * </code>
     *
     * @param kind Kind of the next token
     * @param metadata Metadata
     * @param type Type descriptor
     * @param fieldName Field name
     * @return Parsed node
     */
    private STNode parseFieldDescriptorRhs(SyntaxKind kind, STNode metadata, STNode readonlyQualifier, STNode type,
                                           STNode fieldName) {
        switch (kind) {
            case SEMICOLON_TOKEN:
                STNode questionMarkToken = STNodeFactory.createEmptyNode();
                STNode semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordFieldNode(metadata, readonlyQualifier, type, fieldName,
                        questionMarkToken, semicolonToken);
            case QUESTION_MARK_TOKEN:
                questionMarkToken = parseQuestionMark();
                semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordFieldNode(metadata, readonlyQualifier, type, fieldName,
                        questionMarkToken, semicolonToken);
            case EQUAL_TOKEN:
                // parseRecordDefaultValue();
                STNode equalsToken = parseAssignOp();
                STNode expression = parseExpression();
                semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordFieldWithDefaultValueNode(metadata, readonlyQualifier, type, fieldName,
                        equalsToken, expression, semicolonToken);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FIELD_DESCRIPTOR_RHS, metadata, readonlyQualifier,
                        type, fieldName);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseFieldDescriptorRhs(solution.tokenKind, metadata, readonlyQualifier, type, fieldName);
        }
    }

    /**
     * Parse question mark.
     *
     * @return Parsed node
     */
    private STNode parseQuestionMark() {
        STToken token = peek();
        if (token.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            return consume(); // '?' token
        } else {
            Solution sol = recover(token, ParserRuleContext.QUESTION_MARK);
            return sol.recoveredNode;
        }
    }

    /*
     * Statements
     */

    /**
     * Parse statements, until an end of a block is reached.
     *
     * @return Parsed node
     */
    private STNode parseStatements() {
        ArrayList<STNode> stmts = new ArrayList<>();
        return parseStatements(stmts);
    }

    private STNode parseStatements(ArrayList<STNode> stmts) {
        while (!isEndOfStatements()) {
            STNode stmt = parseStatement();
            if (stmt == null) {
                break;
            }

            if (stmt.kind == SyntaxKind.NAMED_WORKER_DECLARATION) {
                this.errorHandler.reportInvalidNode(null, "named-workers are not allowed here");
                break;
            }
            stmts.add(stmt);
        }
        return STNodeFactory.createNodeList(stmts);
    }

    /**
     * Parse a single statement.
     *
     * @return Parsed node
     */
    protected STNode parseStatement() {
        STToken token = peek();
        return parseStatement(token.kind, 1);
    }

    private STNode parseStatement(SyntaxKind tokenKind, int nextTokenIndex) {
        STNode annots = null;
        switch (tokenKind) {
            case CLOSE_BRACE_TOKEN:
                // Returning null marks the end of statements
                return null;
            case SEMICOLON_TOKEN:
                this.errorHandler.removeInvalidToken();
                return parseStatement();
            case AT_TOKEN:
                annots = parseAnnotations(tokenKind);
                tokenKind = peek().kind;
                break;
            case FINAL_KEYWORD:

                // Statements starts other than var-decl
            case IF_KEYWORD:
            case WHILE_KEYWORD:
            case PANIC_KEYWORD:
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
            case RETURN_KEYWORD:
            case TYPE_KEYWORD:
            case LOCK_KEYWORD:
            case OPEN_BRACE_TOKEN:
            case FORK_KEYWORD:
            case FOREACH_KEYWORD:
            case XMLNS_KEYWORD:
            case TRANSACTION_KEYWORD:
            case RETRY_KEYWORD:
            case ROLLBACK_KEYWORD:

                // action-statements
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
            case TRAP_KEYWORD:
            case START_KEYWORD:
            case FLUSH_KEYWORD:
            case LEFT_ARROW_TOKEN:
            case WAIT_KEYWORD:
            case COMMIT_KEYWORD:

                // Even-though worker is not a statement, we parse it as statements.
                // then validates it based on the context. This is done to provide
                // better error messages
            case WORKER_KEYWORD:
                break;
            default:
                // Var-decl-stmt start
                if (isTypeStartingToken(tokenKind)) {
                    break;
                }

                // Expression-stmt start
                if (isValidExpressionStart(tokenKind, nextTokenIndex)) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT, nextTokenIndex);

                if (solution.action == Action.KEEP) {
                    // singleton type starting tokens can be correct one's hence keep them.
                    break;
                }

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseStatement(solution.tokenKind, nextTokenIndex);
        }

        return parseStatement(tokenKind, annots, nextTokenIndex);
    }

    private STNode getAnnotations(STNode nullbaleAnnot) {
        if (nullbaleAnnot != null) {
            return nullbaleAnnot;
        }

        return STNodeFactory.createEmptyNodeList();
    }

    private STNode parseStatement(STNode annots) {
        return parseStatement(peek().kind, annots, 1);
    }

    /**
     * Parse a single statement, given the next token kind.
     *
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseStatement(SyntaxKind tokenKind, STNode annots, int nextTokenIndex) {
        // TODO: validate annotations: not every statement supports annots
        switch (tokenKind) {
            case CLOSE_BRACE_TOKEN:
                this.errorHandler.reportInvalidNode(null, "invalid annotations");
                // Returning null marks the end of statements
                return null;
            case SEMICOLON_TOKEN:
                this.errorHandler.removeInvalidToken();
                return parseStatement(annots);
            case FINAL_KEYWORD:
                STNode finalKeyword = parseFinalKeyword();
                return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
            case IF_KEYWORD:
                return parseIfElseBlock();
            case WHILE_KEYWORD:
                return parseWhileStatement();
            case PANIC_KEYWORD:
                return parsePanicStatement();
            case CONTINUE_KEYWORD:
                return parseContinueStatement();
            case BREAK_KEYWORD:
                return parseBreakStatement();
            case RETURN_KEYWORD:
                return parseReturnStatement();
            case TYPE_KEYWORD:
                return parseLocalTypeDefinitionStatement(getAnnotations(annots));
            case LOCK_KEYWORD:
                return parseLockStatement();
            case OPEN_BRACE_TOKEN:
                return parseStatementStartsWithOpenBrace();
            case WORKER_KEYWORD:
                // Even-though worker is not a statement, we parse it as statements.
                // then validates it based on the context. This is done to provide
                // better error messages
                return parseNamedWorkerDeclaration(getAnnotations(annots));
            case FORK_KEYWORD:
                return parseForkStatement();
            case FOREACH_KEYWORD:
                return parseForEachStatement();
            case START_KEYWORD:
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
            case TRAP_KEYWORD:
            case FLUSH_KEYWORD:
            case LEFT_ARROW_TOKEN:
            case WAIT_KEYWORD:
            case FROM_KEYWORD:
            case COMMIT_KEYWORD:
                return parseExpressionStament(tokenKind, getAnnotations(annots));
            case XMLNS_KEYWORD:
                return parseXMLNamespaceDeclaration(false);
            case TRANSACTION_KEYWORD:
                return parseTransactionStatement();
            case RETRY_KEYWORD:
                return parseRetryStatement();
            case ROLLBACK_KEYWORD:
                return parseRollbackStatement();
            case OPEN_BRACKET_TOKEN:
                // any statement starts with `[` can be either a var-decl with tuple type
                // or a destructuring assignment with list-binding-pattern.
                return parseStatementStartsWithOpenBracket(getAnnotations(annots), false);
            case FUNCTION_KEYWORD:
            case OPEN_PAREN_TOKEN:
            case IDENTIFIER_TOKEN:
                // Can be a singleton type or expression.
            case NIL_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                // These are statement starting tokens that has ambiguity between
                // being a type-desc or an expressions.
                return parseStmtStartsWithTypeOrExpr(tokenKind, getAnnotations(annots));
            default:
                if (isValidExpressionStart(tokenKind, nextTokenIndex)) {
                    // These are expressions that are definitely not types.
                    return parseStatementStartWithExpr(getAnnotations(annots));
                }

                if (isTypeStartingToken(tokenKind)) {
                    // If the statement starts with a type, then its a var declaration.
                    // This is an optimization since if we know the next token is a type, then
                    // we can parse the var-def faster.
                    finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT_WITHOUT_ANNOTS, annots, nextTokenIndex);

                if (solution.action == Action.KEEP) {
                    // singleton type starting tokens can be correct one's hence keep them.
                    finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
                }
                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                // we come here if a token was inserted. Then the current token become the
                // next token. So 'nextTokenIndex = nextTokenIndex - 1'
                return parseStatement(solution.tokenKind, annots, nextTokenIndex - 1);
        }
    }

    /**
     * <p>
     * Parse variable declaration. Variable declaration can be a local or module level.
     * </p>
     *
     * <code>
     * local-var-decl-stmt := local-init-var-decl-stmt | local-no-init-var-decl-stmt
     * <br/><br/>
     * local-init-var-decl-stmt := [annots] [final] typed-binding-pattern = action-or-expr ;
     * <br/><br/>
     * local-no-init-var-decl-stmt := [annots] [final] type-descriptor variable-name ;
     * </code>
     *
     * @param annots Annotations or metadata
     * @param finalKeyword Final keyword
     * @return Parsed node
     */
    private STNode parseVariableDecl(STNode annots, STNode finalKeyword, boolean isModuleVar) {
        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode typeBindingPattern = parseTypedBindingPattern(ParserRuleContext.VAR_DECL_STMT);
        return parseVarDeclRhs(annots, finalKeyword, typeBindingPattern, isModuleVar);
    }

    /**
     * Parse final keyword.
     *
     * @return Parsed node
     */
    private STNode parseFinalKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FINAL_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FINAL_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse the right hand side of a variable declaration statement.
     * </p>
     * <code>
     * var-decl-rhs := ; | = action-or-expr ;
     * </code>
     *
     * @param metadata metadata
     * @param finalKeyword Final keyword
     * @param typedBindingPattern Typed binding pattern
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(STNode metadata, STNode finalKeyword, STNode typedBindingPattern,
                                   boolean isModuleVar) {
        STToken token = peek();
        return parseVarDeclRhs(token.kind, metadata, finalKeyword, typedBindingPattern, isModuleVar);
    }

    /**
     * Parse the right hand side of a variable declaration statement, given the
     * next token kind.
     *
     * @param tokenKind Next token kind
     * @param metadata Metadata
     * @param finalKeyword Final keyword
     * @param typedBindingPattern Typed binding pattern
     * @param isModuleVar flag indicating whether the var is module level
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(SyntaxKind tokenKind, STNode metadata, STNode finalKeyword,
                                   STNode typedBindingPattern, boolean isModuleVar) {
        STNode assign;
        STNode expr;
        STNode semicolon;
        switch (tokenKind) {
            case EQUAL_TOKEN:
                assign = parseAssignOp();
                if (isModuleVar) {
                    expr = parseExpression();
                } else {
                    expr = parseActionOrExpression();
                }
                semicolon = parseSemicolon();
                break;
            case SEMICOLON_TOKEN:
                assign = STNodeFactory.createEmptyNode();
                expr = STNodeFactory.createEmptyNode();
                semicolon = parseSemicolon();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.VAR_DECL_STMT_RHS, metadata, finalKeyword,
                        typedBindingPattern, isModuleVar);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseVarDeclRhs(solution.tokenKind, metadata, finalKeyword, typedBindingPattern, isModuleVar);
        }

        endContext();
        if (isModuleVar) {
            return STNodeFactory.createModuleVariableDeclarationNode(metadata, finalKeyword, typedBindingPattern,
                    assign, expr, semicolon);
        }
        return STNodeFactory.createVariableDeclarationNode(metadata, finalKeyword, typedBindingPattern, assign, expr,
                semicolon);
    }

    /**
     * <p>
     * Parse the RHS portion of the assignment.
     * </p>
     * <code>assignment-stmt-rhs := = action-or-expr ;</code>
     *
     * @param lvExpr LHS expression
     * @return Parsed node
     */
    private STNode parseAssignmentStmtRhs(STNode lvExpr) {
        validateLVExpr(lvExpr);
        STNode assign = parseAssignOp();
        STNode expr = parseActionOrExpression();
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createAssignmentStatementNode(lvExpr, assign, expr, semicolon);
    }

    /*
     * Expressions
     */

    /**
     * Parse expression. This will start parsing expressions from the lowest level of precedence.
     *
     * @return Parsed node
     */
    protected STNode parseExpression() {
        return parseExpression(DEFAULT_OP_PRECEDENCE, true, false);
    }

    /**
     * Parse action or expression. This will start parsing actions or expressions from the lowest level of precedence.
     *
     * @return Parsed node
     */
    private STNode parseActionOrExpression() {
        return parseExpression(DEFAULT_OP_PRECEDENCE, true, true);
    }

    private STNode parseActionOrExpressionInLhs(SyntaxKind tokenKind, STNode annots) {
        return parseExpression(tokenKind, DEFAULT_OP_PRECEDENCE, annots, false, true);
    }

    /**
     * Parse expression.
     *
     * @param isRhsExpr Flag indicating whether this is a rhs expression
     * @return Parsed node
     */
    private STNode parseExpression(boolean isRhsExpr) {
        return parseExpression(DEFAULT_OP_PRECEDENCE, isRhsExpr, false);
    }

    private void validateLVExpr(STNode expression) {
        if (isValidLVExpr(expression)) {
            return;
        }
        this.errorHandler.reportInvalidNode(null, "invalid expression for assignment lhs");
    }

    private boolean isValidLVExpr(STNode expression) {
        switch (expression.kind) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
                return true;
            case FIELD_ACCESS:
                return isValidLVMemberExpr(((STFieldAccessExpressionNode) expression).expression);
            case INDEXED_EXPRESSION:
                return isValidLVMemberExpr(((STIndexedExpressionNode) expression).containerExpression);
            default:
                return (expression instanceof STMissingToken);
        }
    }

    private boolean isValidLVMemberExpr(STNode expression) {
        switch (expression.kind) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
                return true;
            case FIELD_ACCESS:
                return isValidLVMemberExpr(((STFieldAccessExpressionNode) expression).expression);
            case INDEXED_EXPRESSION:
                return isValidLVMemberExpr(((STIndexedExpressionNode) expression).containerExpression);
            case BRACED_EXPRESSION:
                return isValidLVMemberExpr(((STBracedExpressionNode) expression).expression);
            default:
                return (expression instanceof STMissingToken);
        }
    }

    /**
     * Parse an expression that has an equal or higher precedence than a given level.
     *
     * @param precedenceLevel Precedence level of expression to be parsed
     * @param isRhsExpr Flag indicating whether this is a rhs expression
     * @param allowActions Flag indicating whether the current context support actions
     * @return Parsed node
     */
    private STNode parseExpression(OperatorPrecedence precedenceLevel, boolean isRhsExpr, boolean allowActions) {
        STToken token = peek();
        return parseExpression(token.kind, precedenceLevel, isRhsExpr, allowActions);
    }

    private STNode parseExpression(SyntaxKind kind, OperatorPrecedence precedenceLevel, boolean isRhsExpr,
                                   boolean allowActions) {
        STNode expr = parseTerminalExpression(kind, isRhsExpr, allowActions);
        return parseExpressionRhs(precedenceLevel, expr, isRhsExpr, allowActions);
    }

    private STNode parseExpression(SyntaxKind kind, OperatorPrecedence precedenceLevel, STNode annots,
                                   boolean isRhsExpr, boolean allowActions) {
        STNode expr = parseTerminalExpression(kind, annots, isRhsExpr, allowActions);
        return parseExpressionRhs(precedenceLevel, expr, isRhsExpr, allowActions);
    }

    /**
     * Parse terminal expressions. A terminal expression has the highest precedence level
     * out of all expressions, and will be at the leaves of an expression tree.
     *
     * @param annots Annotations
     * @param isRhsExpr Is a rhs expression
     * @param allowActions Allow actions
     * @return Parsed node
     */
    private STNode parseTerminalExpression(STNode annots, boolean isRhsExpr, boolean allowActions) {
        return parseTerminalExpression(peek().kind, annots, isRhsExpr, allowActions);
    }

    private STNode parseTerminalExpression(SyntaxKind kind, boolean isRhsExpr, boolean allowActions) {
        STNode annots;
        if (kind == SyntaxKind.AT_TOKEN) {
            annots = parseAnnotations();
            kind = peek().kind;
        } else {
            annots = STNodeFactory.createEmptyNode();
        }

        STNode expr = parseTerminalExpression(kind, annots, isRhsExpr, allowActions);
        if (!isEmpty(annots) && expr.kind != SyntaxKind.START_ACTION) {
            this.errorHandler.reportInvalidNode(null, "annotations are not supported for expressions");
        }

        return expr;
    }

    private STNode parseTerminalExpression(SyntaxKind kind, STNode annots, boolean isRhsExpr, boolean allowActions) {
        // Whenever a new expression start is added, make sure to
        // add it to all the other places as well.
        switch (kind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return parseBasicLiteral();
            case IDENTIFIER_TOKEN:
                return parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
            case OPEN_PAREN_TOKEN:
                return parseBracedExpression(isRhsExpr, allowActions);
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
                // In the checking action, nested actions are allowed. And that's the only
                // place where actions are allowed within an action or an expression.
                return parseCheckExpression(isRhsExpr, allowActions);
            case OPEN_BRACE_TOKEN:
                return parseMappingConstructorExpr();
            case TYPEOF_KEYWORD:
                return parseTypeofExpression(isRhsExpr);
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
                return parseUnaryExpression(isRhsExpr);
            case TRAP_KEYWORD:
                return parseTrapExpression(isRhsExpr, allowActions);
            case OPEN_BRACKET_TOKEN:
                return parseListConstructorExpr();
            case LT_TOKEN:
                return parseTypeCastExpr(isRhsExpr);
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
            case FROM_KEYWORD:
                return parseTableConstructorOrQuery(isRhsExpr);
            case ERROR_KEYWORD:
                return parseErrorConstructorExpr();
            case LET_KEYWORD:
                return parseLetExpression(isRhsExpr);
            case BACKTICK_TOKEN:
                return parseTemplateExpression();
            case XML_KEYWORD:
                STToken nextNextToken = getNextNextToken(kind);
                if (nextNextToken.kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseXMLTemplateExpression();
                }
                break;
            case STRING_KEYWORD:
                nextNextToken = getNextNextToken(kind);
                if (nextNextToken.kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseStringTemplateExpression();
                }
                break;
            case FUNCTION_KEYWORD:
                return parseExplicitFunctionExpression(annots, isRhsExpr);
            case AT_TOKEN:
                // Annon-func can have annotations. Check for other expressions
                // that can start with annots.
                break;
            case NEW_KEYWORD:
                return parseNewExpression();
            case START_KEYWORD:
                return parseStartAction(annots);
            case FLUSH_KEYWORD:
                return parseFlushAction();
            case LEFT_ARROW_TOKEN:
                return parseReceiveAction();
            case WAIT_KEYWORD:
                return parseWaitAction();
            case COMMIT_KEYWORD:
                return parseCommitAction();
            case TRANSACTIONAL_KEYWORD:
                return parseTransactionalExpression();
            case SERVICE_KEYWORD:
                return parseServiceConstructorExpression(annots);
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
                return parseByteArrayLiteral(kind);
            default:
                break;
        }

        Solution solution = recover(peek(), ParserRuleContext.TERMINAL_EXPRESSION, annots, isRhsExpr, allowActions);
        if (solution.action == Action.REMOVE) {
            return solution.recoveredNode;
        }

        if (solution.action == Action.KEEP) {
            if (kind == SyntaxKind.XML_KEYWORD) {
                return parseXMLTemplateExpression();
            }

            return parseStringTemplateExpression();
        }

        switch (solution.tokenKind) {
            case IDENTIFIER_TOKEN:
                return parseQualifiedIdentifier(solution.recoveredNode);
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return solution.recoveredNode;
            default:
                return parseTerminalExpression(solution.tokenKind, annots, isRhsExpr, allowActions);
        }
    }

    private boolean isValidExprStart(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            case IDENTIFIER_TOKEN:
            case OPEN_PAREN_TOKEN:
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
            case OPEN_BRACE_TOKEN:
            case TYPEOF_KEYWORD:
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
            case TRAP_KEYWORD:
            case OPEN_BRACKET_TOKEN:
            case LT_TOKEN:
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
            case FROM_KEYWORD:
            case ERROR_KEYWORD:
            case LET_KEYWORD:
            case BACKTICK_TOKEN:
            case XML_KEYWORD:
            case STRING_KEYWORD:
            case FUNCTION_KEYWORD:
            case AT_TOKEN:
            case NEW_KEYWORD:
            case START_KEYWORD:
            case FLUSH_KEYWORD:
            case LEFT_ARROW_TOKEN:
            case WAIT_KEYWORD:
            case SERVICE_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * <p>
     * Parse a new expression.
     * </p>
     * <code>
     *  new-expr := explicit-new-expr | implicit-new-expr
     *  <br/>
     *  explicit-new-expr := new type-descriptor ( arg-list )
     *  <br/>
     *  implicit-new-expr := new [( arg-list )]
     * </code>
     *
     * @return Parsed NewExpression node.
     */
    private STNode parseNewExpression() {
        STNode newKeyword = parseNewKeyword();
        return parseNewKeywordRhs(newKeyword);
    }

    /**
     * <p>
     * Parse `new` keyword.
     * </p>
     *
     * @return Parsed NEW_KEYWORD Token.
     */
    private STNode parseNewKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.NEW_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.NEW_KEYWORD);
            return sol.recoveredNode;
        }
    }

    private STNode parseNewKeywordRhs(STNode newKeyword) {
        STNode token = peek();
        return parseNewKeywordRhs(token.kind, newKeyword);
    }

    /**
     * <p>
     * Parse an implicit or explicit new expression.
     * </p>
     *
     * @param kind next token kind.
     * @param newKeyword parsed node for `new` keyword.
     * @return Parsed new-expression node.
     */
    private STNode parseNewKeywordRhs(SyntaxKind kind, STNode newKeyword) {
        switch (kind) {
            case OPEN_PAREN_TOKEN:
                return parseImplicitNewRhs(newKeyword);
            case SEMICOLON_TOKEN:
                break;
            case IDENTIFIER_TOKEN:
            case OBJECT_KEYWORD:
                // TODO: Support `stream` keyword once introduced
                return parseTypeDescriptorInNewExpr(newKeyword);
            default:
                break;
        }

        return STNodeFactory.createImplicitNewExpressionNode(newKeyword, STNodeFactory.createEmptyNode());
    }

    /**
     * <p>
     * Parse an Explicit New expression.
     * </p>
     * <code>
     *  explicit-new-expr := new type-descriptor ( arg-list )
     * </code>
     *
     * @param newKeyword Parsed `new` keyword.
     * @return the Parsed Explicit New Expression.
     */
    private STNode parseTypeDescriptorInNewExpr(STNode newKeyword) {
        STNode typeDescriptor = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_NEW_EXPR);
        STNode parenthesizedArgsList = parseParenthesizedArgList();

        return STNodeFactory.createExplicitNewExpressionNode(newKeyword, typeDescriptor, parenthesizedArgsList);
    }

    /**
     * <p>
     * Parse an <code>implicit-new-expr</code> with arguments.
     * </p>
     *
     * @param newKeyword Parsed `new` keyword.
     * @return Parsed implicit-new-expr.
     */
    private STNode parseImplicitNewRhs(STNode newKeyword) {
        STNode implicitNewArgList = parseParenthesizedArgList();
        return STNodeFactory.createImplicitNewExpressionNode(newKeyword, implicitNewArgList);
    }

    /**
     * <p>
     * Parse the parenthesized argument list for a <code>new-expr</code>.
     * </p>
     *
     * @return Parsed parenthesized rhs of <code>new-expr</code>.
     */
    private STNode parseParenthesizedArgList() {
        STNode openParan = parseOpenParenthesis(ParserRuleContext.ARG_LIST_START);
        STNode arguments = parseArgsList();
        STNode closeParan = parseCloseParenthesis();

        return STNodeFactory.createParenthesizedArgList(openParan, arguments, closeParan);
    }

    /**
     * <p>
     * Parse the right-hand-side of an expression.
     * </p>
     * <code>expr-rhs := (binary-op expression
     *                              | dot identifier
     *                              | open-bracket expression close-bracket
     *                          )*</code>
     *
     * @param precedenceLevel Precedence level of the expression that is being parsed currently
     * @param lhsExpr LHS expression of the expression
     * @param isRhsExpr Flag indicating whether this is on a rhsExpr of a statement
     * @param allowActions Flag indicating whether the current context support actions
     * @return Parsed node
     */
    private STNode parseExpressionRhs(OperatorPrecedence precedenceLevel, STNode lhsExpr, boolean isRhsExpr,
                                      boolean allowActions) {
        STToken token = peek();
        return parseExpressionRhs(token.kind, precedenceLevel, lhsExpr, isRhsExpr, allowActions);
    }

    /**
     * Parse the right hand side of an expression given the next token kind.
     *
     * @param tokenKind Next token kind
     * @param currentPrecedenceLevel Precedence level of the expression that is being parsed currently
     * @param lhsExpr LHS expression
     * @param isRhsExpr Flag indicating whether this is a rhs expr or not
     * @param allowActions Flag indicating whether to allow actions or not
     * @return Parsed node
     */
    private STNode parseExpressionRhs(SyntaxKind tokenKind, OperatorPrecedence currentPrecedenceLevel, STNode lhsExpr,
                                      boolean isRhsExpr, boolean allowActions) {
        if (isEndOfExpression(tokenKind, isRhsExpr)) {
            return lhsExpr;
        }

        if (lhsExpr.kind == SyntaxKind.ASYNC_SEND_ACTION) {
            // Async-send action can only exists in an action-statement.
            // It also has to be the right-most action. i.e: Should be
            // followed by a semicolon
            return lhsExpr;
        }

        if (!isValidExprRhsStart(tokenKind)) {
            STToken token = peek();
            Solution solution = recover(token, ParserRuleContext.EXPRESSION_RHS, currentPrecedenceLevel, lhsExpr,
                    isRhsExpr, allowActions);

            // If the current rule was recovered by removing a token,
            // then this entire rule is already parsed while recovering.
            // so we done need to parse the remaining of this rule again.
            // Proceed only if the recovery action was an insertion.
            if (solution.action == Action.REMOVE) {
                return solution.recoveredNode;
            }

            // If the parser recovered by inserting a token, then try to re-parse the same
            // rule with the inserted token. This is done to pick the correct branch to
            // continue the parsing.
            if (solution.ctx == ParserRuleContext.BINARY_OPERATOR) {
                // We come here if the operator is missing. Treat this as injecting an operator
                // that matches to the current operator precedence level, and continue.
                SyntaxKind binaryOpKind = getBinaryOperatorKindToInsert(currentPrecedenceLevel);
                return parseExpressionRhs(binaryOpKind, currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions);
            } else {
                return parseExpressionRhs(solution.tokenKind, currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions);
            }
        }

        // Look for >> and >>> tokens as they are not sent from lexer due to ambiguity. e.g. <map<int>> a
        if (tokenKind == SyntaxKind.GT_TOKEN && peek(2).kind == SyntaxKind.GT_TOKEN) {
            if (peek(3).kind == SyntaxKind.GT_TOKEN) {
                tokenKind = SyntaxKind.TRIPPLE_GT_TOKEN;
            } else {
                tokenKind = SyntaxKind.DOUBLE_GT_TOKEN;
            }
        }

        // If the precedence level of the operator that was being parsed is higher than
        // the newly found (next) operator, then return and finish the previous expr,
        // because it has a higher precedence.
        OperatorPrecedence nextOperatorPrecedence = getOpPrecedence(tokenKind);
        if (currentPrecedenceLevel.isHigherThan(nextOperatorPrecedence, allowActions)) {
            return lhsExpr;
        }

        STNode newLhsExpr;
        STNode operator;
        switch (tokenKind) {
            case OPEN_PAREN_TOKEN:
                newLhsExpr = parseFuncCall(lhsExpr);
                break;
            case OPEN_BRACKET_TOKEN:
                newLhsExpr = parseMemberAccessExpr(lhsExpr, isRhsExpr);
                break;
            case DOT_TOKEN:
                newLhsExpr = parseFieldAccessOrMethodCall(lhsExpr);
                break;
            case IS_KEYWORD:
                newLhsExpr = parseTypeTestExpression(lhsExpr);
                break;
            case RIGHT_ARROW_TOKEN:
                newLhsExpr = parseRemoteMethodCallOrAsyncSendAction(lhsExpr, isRhsExpr);
                if (!allowActions) {
                    this.errorHandler.reportInvalidNode(null, "actions are not allowed here");
                }
                break;
            case SYNC_SEND_TOKEN:
                newLhsExpr = parseSyncSendAction(lhsExpr);
                if (!allowActions) {
                    this.errorHandler.reportInvalidNode(null, "actions are not allowed here");
                }
                break;
            case RIGHT_DOUBLE_ARROW_TOKEN:
                newLhsExpr = parseImplicitAnonFunc(lhsExpr, isRhsExpr);
                break;
            case ANNOT_CHAINING_TOKEN:
                newLhsExpr = parseAnnotAccessExpression(lhsExpr);
                break;
            case OPTIONAL_CHAINING_TOKEN:
                newLhsExpr = parseOptionalFieldAccessExpression(lhsExpr);
                break;
            case QUESTION_MARK_TOKEN:
                newLhsExpr = parseConditionalExpression(lhsExpr);
                break;
            case DOT_LT_TOKEN:
                newLhsExpr = parseXMLFilterExpression(lhsExpr);
                break;
            case SLASH_LT_TOKEN:
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
            case SLASH_ASTERISK_TOKEN:
                newLhsExpr = parseXMLStepExpression(lhsExpr);
                break;
            default:
                if (tokenKind == SyntaxKind.DOUBLE_GT_TOKEN) {
                    operator = parseSignedRightShiftToken();
                } else if (tokenKind == SyntaxKind.TRIPPLE_GT_TOKEN) {
                    operator = parseUnsignedRightShiftToken();
                } else {
                    operator = parseBinaryOperator();
                }

                // Parse the expression that follows the binary operator, until a operator
                // with different precedence is encountered. If an operator with a lower
                // precedence is reached, then come back here and finish the current
                // binary expr. If a an operator with higher precedence level is reached,
                // then complete that binary-expr, come back here and finish the current expr.

                // Actions within binary-expressions are not allowed.
                STNode rhsExpr = parseExpression(nextOperatorPrecedence, isRhsExpr, false);
                newLhsExpr = STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, lhsExpr, operator,
                        rhsExpr);
                break;
        }

        // Then continue the operators with the same precedence level.
        return parseExpressionRhs(currentPrecedenceLevel, newLhsExpr, isRhsExpr, allowActions);
    }

    private boolean isValidExprRhsStart(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case OPEN_PAREN_TOKEN:
            case DOT_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case IS_KEYWORD:
            case RIGHT_ARROW_TOKEN:
            case RIGHT_DOUBLE_ARROW_TOKEN:
            case SYNC_SEND_TOKEN:
            case ANNOT_CHAINING_TOKEN:
            case OPTIONAL_CHAINING_TOKEN:
            case QUESTION_MARK_TOKEN:
            case COLON_TOKEN:
            case DOT_LT_TOKEN:
            case SLASH_LT_TOKEN:
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
            case SLASH_ASTERISK_TOKEN:
                return true;
            default:
                return isBinaryOperator(tokenKind);
        }
    }

    /**
     * Parse member access expression.
     *
     * @param lhsExpr Container expression
     * @param isRhsExpr Is this is a rhs expression
     * @return Member access expression
     */
    private STNode parseMemberAccessExpr(STNode lhsExpr, boolean isRhsExpr) {
        startContext(ParserRuleContext.MEMBER_ACCESS_KEY_EXPR);
        STNode openBracket = parseOpenBracket();

        STNode keyExpr = parseMemberAccessKeyExprs(isRhsExpr);
        STNode closeBracket = parseCloseBracket();
        endContext();
        return STNodeFactory.createIndexedExpressionNode(lhsExpr, openBracket, keyExpr, closeBracket);
    }

    /**
     * Parse key expression of a member access expression. A type descriptor
     * that starts with a type-ref (e.g: T[a][b]) also goes through this
     * method.
     * <p>
     * <code>key-expression := single-key-expression | multi-key-expression</code>
     *
     * @param isRhsExpr Is this is a rhs expression
     * @return Key expression
     */
    private STNode parseMemberAccessKeyExprs(boolean isRhsExpr) {
        List<STNode> exprList = new ArrayList<>();

        // Parse the remaining exprs
        STNode keyExpr;
        STNode keyExprEnd;
        while (!isEndOfTypeList(peek().kind)) {
            keyExpr = parseKeyExpr(isRhsExpr);
            exprList.add(keyExpr);
            keyExprEnd = parseMemberAccessKeyExprEnd();
            if (keyExprEnd == null) {
                break;
            }
            exprList.add(keyExprEnd);
        }

        // If this is in RHS, then its definitely a member-access.
        if (isRhsExpr && exprList.isEmpty()) {
            exprList.add(STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN));
            this.errorHandler.reportInvalidNode(null, "missing key expression");
        }

        // member-access on LHS / array-type-desc can have only a single key expr.
        if (!isRhsExpr && exprList.size() > 1) {
            this.errorHandler.reportInvalidNode(null, "cannot have multiple keys");
        }

        return STNodeFactory.createNodeList(exprList);
    }

    private STNode parseKeyExpr(boolean isRhsExpr) {
        if (!isRhsExpr && peek().kind == SyntaxKind.ASTERISK_TOKEN) {
            return STNodeFactory.createBasicLiteralNode(SyntaxKind.ASTERISK_TOKEN, consume());
        }

        return parseExpression(isRhsExpr);
    }

    private STNode parseMemberAccessKeyExprEnd() {
        return parseMemberAccessKeyExprEnd(peek().kind);
    }

    private STNode parseMemberAccessKeyExprEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.MEMBER_ACCESS_KEY_EXPR_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseMemberAccessKeyExprEnd(solution.tokenKind);
        }
    }

    /**
     * Parse close bracket.
     *
     * @return Parsed node
     */
    private STNode parseCloseBracket() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACKET_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CLOSE_BRACKET);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse field access, xml required attribute access expressions or method call expression.
     * <p>
     * <code>
     * field-access-expr := expression . field-name
     * <br/>
     * xml-required-attribute-access-expr := expression . xml-attribute-name
     * <br/>
     * xml-attribute-name := xml-qualified-name | qualified-identifier | identifier
     * <br/>
     * method-call-expr := expression . method-name ( arg-list )
     * </code>
     *
     * @param lhsExpr Preceding expression of the field access or method call
     * @return One of <code>field-access-expression</code> or <code>method-call-expression</code>.
     */
    private STNode parseFieldAccessOrMethodCall(STNode lhsExpr) {
        STNode dotToken = parseDotToken();
        STNode fieldOrMethodName = parseFieldAccessIdentifier();

        if (fieldOrMethodName.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return STNodeFactory.createFieldAccessExpressionNode(lhsExpr, dotToken, fieldOrMethodName);
        }

        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            // function invocation
            STNode openParen = parseOpenParenthesis(ParserRuleContext.ARG_LIST_START);
            STNode args = parseArgsList();
            STNode closeParen = parseCloseParenthesis();
            return STNodeFactory.createMethodCallExpressionNode(lhsExpr, dotToken, fieldOrMethodName, openParen, args,
                    closeParen);
        }

        // Everything else is field-access
        return STNodeFactory.createFieldAccessExpressionNode(lhsExpr, dotToken, fieldOrMethodName);
    }

    /**
     * <p>
     * Parse braced expression.
     * </p>
     * <code>braced-expr := ( expression )</code>
     *
     * @param isRhsExpr Flag indicating whether this is on a rhsExpr of a statement
     * @param allowActions Allow actions
     * @return Parsed node
     */
    private STNode parseBracedExpression(boolean isRhsExpr, boolean allowActions) {
        STNode openParen = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);

        if (peek().kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            // Could be nill literal or empty param-list of an implicit-anon-func-expr'
            return parseNilLiteralOrEmptyAnonFuncParamRhs(openParen);
        }

        startContext(ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAMS);
        STNode expr;
        if (allowActions) {
            expr = parseExpression(DEFAULT_OP_PRECEDENCE, isRhsExpr, true);
        } else {
            expr = parseExpression(isRhsExpr);
        }

        return parseBracedExprOrAnonFuncParamRhs(peek().kind, openParen, expr, isRhsExpr);
    }

    private STNode parseNilLiteralOrEmptyAnonFuncParamRhs(STNode openParen) {
        STNode closeParen = parseCloseParenthesis();
        STToken nextToken = peek();
        if (nextToken.kind != SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN) {
            return STNodeFactory.createNilLiteralNode(openParen, closeParen);
        } else {
            STNode params = STNodeFactory.createNodeList();
            STNode anonFuncParam =
                    STNodeFactory.createImplicitAnonymousFunctionParameters(openParen, params, closeParen);
            return anonFuncParam;
        }
    }

    private STNode parseBracedExprOrAnonFuncParamRhs(STNode openParen, STNode expr, boolean isRhsExpr) {
        STToken nextToken = peek();
        return parseBracedExprOrAnonFuncParamRhs(nextToken.kind, openParen, expr, isRhsExpr);
    }

    private STNode parseBracedExprOrAnonFuncParamRhs(SyntaxKind nextTokenKind, STNode openParen, STNode expr,
                                                     boolean isRhsExpr) {
        if (expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            switch (nextTokenKind) {
                case CLOSE_PAREN_TOKEN:
                    break;
                case COMMA_TOKEN:
                    // Here the context is ended inside the method.
                    return parseImplicitAnonFunc(openParen, expr, isRhsExpr);
                default:
                    STToken token = peek();
                    Solution solution = recover(token, ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS, openParen,
                            expr, isRhsExpr);

                    // If the parser recovered by inserting a token, then try to re-parse the same
                    // rule with the inserted token. This is done to pick the correct branch
                    // to continue the parsing.
                    if (solution.action == Action.REMOVE) {
                        endContext();
                        return solution.recoveredNode;
                    }

                    return parseBracedExprOrAnonFuncParamRhs(solution.tokenKind, openParen, expr, isRhsExpr);
            }
        }

        STNode closeParen = parseCloseParenthesis();
        endContext();
        if (isAction(expr)) {
            return STNodeFactory.createBracedExpressionNode(SyntaxKind.BRACED_ACTION, openParen, expr, closeParen);
        }
        return STNodeFactory.createBracedExpressionNode(SyntaxKind.BRACED_EXPRESSION, openParen, expr, closeParen);
    }

    /**
     * Check whether a given node is an action node.
     *
     * @param node Node to check
     * @return <code>true</code> if the node is an action node. <code>false</code> otherwise
     */
    private boolean isAction(STNode node) {
        switch (node.kind) {
            case REMOTE_METHOD_CALL_ACTION:
            case BRACED_ACTION:
            case CHECK_ACTION:
            case START_ACTION:
            case TRAP_ACTION:
            case FLUSH_ACTION:
            case ASYNC_SEND_ACTION:
            case SYNC_SEND_ACTION:
            case RECEIVE_ACTION:
            case WAIT_ACTION:
            case QUERY_ACTION:
            case COMMIT_ACTION:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token is an end of a expression.
     *
     * @param tokenKind Token to check
     * @param isRhsExpr Flag indicating whether this is on a rhsExpr of a statement
     * @return <code>true</code> if the token represents an end of a block. <code>false</code> otherwise
     */
    private boolean isEndOfExpression(SyntaxKind tokenKind, boolean isRhsExpr) {
        if (!isRhsExpr) {
            if (isCompoundBinaryOperator(tokenKind)) {
                return true;
            }
            return !isValidExprRhsStart(tokenKind);
        }

        switch (tokenKind) {
            case CLOSE_BRACE_TOKEN:
            case OPEN_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case COMMA_TOKEN:
            case PUBLIC_KEYWORD:
            case EOF_TOKEN:
            case CONST_KEYWORD:
            case LISTENER_KEYWORD:
            case EQUAL_TOKEN:
            case AT_TOKEN:
            case DOCUMENTATION_LINE:
            case AS_KEYWORD:
            case IN_KEYWORD:
            case BACKTICK_TOKEN:
            case FROM_KEYWORD:
            case WHERE_KEYWORD:
            case LET_KEYWORD:
            case SELECT_KEYWORD:
            case DO_KEYWORD:
                return true;
            default:
                return isSimpleType(tokenKind);
        }
    }

    /**
     * Parse basic literals. It is assumed that we come here after validation.
     *
     * @return Parsed node
     */
    private STNode parseBasicLiteral() {
        STToken literalToken = consume();
        return STNodeFactory.createBasicLiteralNode(literalToken.kind, literalToken);
    }

    /**
     * Parse function call expression.
     * <code>function-call-expr := function-reference ( arg-list )
     * function-reference := variable-reference</code>
     *
     * @param identifier Function name
     * @return Function call expression
     */
    private STNode parseFuncCall(STNode identifier) {
        STNode openParen = parseOpenParenthesis(ParserRuleContext.ARG_LIST_START);
        STNode args = parseArgsList();
        STNode closeParen = parseCloseParenthesis();
        return STNodeFactory.createFunctionCallExpressionNode(identifier, openParen, args, closeParen);
    }

    /**
     * <p>
     * Parse error constructor expression.
     * </p>
     * <code>
     * error-constructor-expr := error ( arg-list )
     * </code>
     *
     * @return Error constructor expression
     */
    private STNode parseErrorConstructorExpr() {
        return parseFuncCall(parseErrorKeyWord());
    }

    /**
     * Parse function call argument list.
     *
     * @return Parsed args list
     */
    private STNode parseArgsList() {
        startContext(ParserRuleContext.ARG_LIST);

        STToken token = peek();
        if (isEndOfParametersList(token.kind)) {
            STNode args = STNodeFactory.createEmptyNodeList();
            endContext();
            return args;
        }

        STNode leadingComma = STNodeFactory.createEmptyNode();
        STNode arg = parseArg(leadingComma);
        if (arg == null) {
            STNode args = STNodeFactory.createEmptyNodeList();
            endContext();
            return args;
        }

        // TODO: Is this check required?
        ArrayList<STNode> argsList = new ArrayList<>();
        SyntaxKind lastProcessedArgKind;
        if (SyntaxKind.POSITIONAL_ARG.ordinal() <= arg.kind.ordinal()) {
            argsList.add(arg);
            lastProcessedArgKind = arg.kind;
        } else {
            reportInvalidOrderOfArgs(peek(), SyntaxKind.POSITIONAL_ARG, arg.kind);
            lastProcessedArgKind = SyntaxKind.POSITIONAL_ARG;
        }

        parseFollowUpArgs(argsList, lastProcessedArgKind);
        STNode args = STNodeFactory.createNodeList(argsList);
        endContext();
        return args;
    }

    /**
     * Parse follow up arguments.
     *
     * @param argsList Arguments list to which the parsed argument must be added
     * @param lastProcessedArgKind Kind of the argument processed prior to this
     */
    private void parseFollowUpArgs(ArrayList<STNode> argsList, SyntaxKind lastProcessedArgKind) {
        STToken nextToken = peek();
        while (!isEndOfParametersList(nextToken.kind)) {
            STNode argEnd = parseArgEnd(nextToken.kind);
            if (argEnd == null) {
                // null marks the end of args
                break;
            }

            // If there's an extra comma at the end of arguments list, remove it.
            // Then stop the argument parsing.
            nextToken = peek();
            if (isEndOfParametersList(nextToken.kind)) {
                this.errorHandler.reportInvalidNode((STToken) argEnd, "invalid token " + argEnd);
                break;
            }

            STNode arg = parseArg(nextToken.kind, argEnd);
            if (lastProcessedArgKind.ordinal() <= arg.kind.ordinal()) {
                if (lastProcessedArgKind == SyntaxKind.REST_ARG && arg.kind == SyntaxKind.REST_ARG) {
                    this.errorHandler.reportInvalidNode(nextToken, "cannot more than one rest arg");
                } else {
                    argsList.add(arg);
                    lastProcessedArgKind = arg.kind;
                }
            } else {
                reportInvalidOrderOfArgs(nextToken, lastProcessedArgKind, arg.kind);
            }

            nextToken = peek();
        }
    }

    private STNode parseArgEnd() {
        return parseArgEnd(peek().kind);
    }

    private STNode parseArgEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_PAREN_TOKEN:
                // null marks the end of args
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.ARG_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseArgEnd(solution.tokenKind);
        }
    }

    /**
     * Report invalid order of args.
     *
     * @param token Staring token of the arg.
     * @param lastArgKind Kind of the previously processed arg
     * @param argKind Current arg
     */
    private void reportInvalidOrderOfArgs(STToken token, SyntaxKind lastArgKind, SyntaxKind argKind) {
        this.errorHandler.reportInvalidNode(token, "cannot have a " + argKind + " after the " + lastArgKind);
    }

    /**
     * Parse function call argument.
     *
     * @param leadingComma Comma that occurs before the param
     * @return Parsed argument node
     */
    private STNode parseArg(STNode leadingComma) {
        STToken token = peek();
        return parseArg(token.kind, leadingComma);
    }

    private STNode parseArg(SyntaxKind kind, STNode leadingComma) {
        STNode arg;
        switch (kind) {
            case ELLIPSIS_TOKEN:
                STToken ellipsis = consume();
                STNode expr = parseExpression();
                arg = STNodeFactory.createRestArgumentNode(leadingComma, ellipsis, expr);
                break;

            // Identifier can means two things: either its a named-arg, or just an expression.
            case IDENTIFIER_TOKEN:
                // TODO: Handle package-qualified var-refs (i.e: qualified-identifier).
                arg = parseNamedOrPositionalArg(leadingComma, kind);
                break;
            case CLOSE_PAREN_TOKEN:
                return null;
            default:
                if (isValidExprStart(kind)) {
                    expr = parseExpression();
                    arg = STNodeFactory.createPositionalArgumentNode(leadingComma, expr);
                    break;
                }

                Solution solution = recover(peek(), ParserRuleContext.ARG_START_OR_ARG_LIST_END, leadingComma);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseArg(solution.tokenKind, leadingComma);
        }

        return arg;
    }

    /**
     * Parse positional or named arg. This method assumed peek()/peek(1)
     * is always an identifier.
     *
     * @param leadingComma Comma that occurs before the param
     * @return Parsed argument node
     */
    private STNode parseNamedOrPositionalArg(STNode leadingComma, SyntaxKind nextTokenKind) {
        STNode argNameOrExpr = parseTerminalExpression(peek().kind, true, false);
        STToken secondToken = peek();
        switch (secondToken.kind) {
            case EQUAL_TOKEN:
                STNode equal = parseAssignOp();
                STNode valExpr = parseExpression();
                return STNodeFactory.createNamedArgumentNode(leadingComma, argNameOrExpr, equal, valExpr);
            case COMMA_TOKEN:
            case CLOSE_PAREN_TOKEN:
                return STNodeFactory.createPositionalArgumentNode(leadingComma, argNameOrExpr);

            // Treat everything else as a single expression. If something is missing,
            // expression-parsing will recover it.
            default:
                argNameOrExpr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, argNameOrExpr, false, false);
                return STNodeFactory.createPositionalArgumentNode(leadingComma, argNameOrExpr);
        }
    }

    /**
     * Parse object type descriptor.
     *
     * @return Parsed node
     */
    private STNode parseObjectTypeDescriptor() {
        startContext(ParserRuleContext.OBJECT_TYPE_DESCRIPTOR);
        STNode objectTypeQualifiers = parseObjectTypeQualifiers();
        STNode objectKeyword = parseObjectKeyword();
        STNode openBrace = parseOpenBrace();
        STNode objectMembers = parseObjectMembers();
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createObjectTypeDescriptorNode(objectTypeQualifiers, objectKeyword, openBrace,
                objectMembers, closeBrace);
    }

    /**
     * Parse object type qualifiers.
     *
     * @return Parsed node
     */
    private STNode parseObjectTypeQualifiers() {
        STToken nextToken = peek();
        return parseObjectTypeQualifiers(nextToken.kind);
    }

    private STNode parseObjectTypeQualifiers(SyntaxKind kind) {
        STNode firstQualifier;
        switch (kind) {
            case CLIENT_KEYWORD:
                STNode clientKeyword = parseClientKeyword();
                firstQualifier = clientKeyword;
                break;
            case ABSTRACT_KEYWORD:
                STNode abstractKeyword = parseAbstractKeyword();
                firstQualifier = abstractKeyword;
                break;
            case OBJECT_KEYWORD:
                return STNodeFactory.createEmptyNodeList();
            default:
                Solution solution = recover(peek(), ParserRuleContext.OBJECT_TYPE_FIRST_QUALIFIER);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseObjectTypeQualifiers(solution.tokenKind);
        }

        // Parse the second qualifier if available.
        STNode secondQualifier = parseObjectTypeSecondQualifier(firstQualifier);

        List<STNode> qualifiers = new ArrayList<>();
        qualifiers.add(firstQualifier);
        if (secondQualifier != null) {
            qualifiers.add(secondQualifier);
        }
        return STNodeFactory.createNodeList(qualifiers);
    }

    private STNode parseObjectTypeSecondQualifier(STNode firstQualifier) {
        STToken nextToken = peek();
        return parseObjectTypeSecondQualifier(nextToken.kind, firstQualifier);
    }

    private STNode parseObjectTypeSecondQualifier(SyntaxKind kind, STNode firstQualifier) {
        if (firstQualifier.kind != kind) {
            switch (kind) {
                case CLIENT_KEYWORD:
                    return parseClientKeyword();
                case ABSTRACT_KEYWORD:
                    return parseAbstractKeyword();
                case OBJECT_KEYWORD:
                    return null;
                default:
                    break;
            }
        }

        Solution solution = recover(peek(), ParserRuleContext.OBJECT_TYPE_SECOND_QUALIFIER, firstQualifier);

        // If the parser recovered by inserting a token, then try to re-parse the same
        // rule with the inserted token. This is done to pick the correct branch
        // to continue the parsing.
        if (solution.action == Action.REMOVE) {
            return solution.recoveredNode;
        }

        return parseObjectTypeSecondQualifier(solution.tokenKind, firstQualifier);
    }

    /**
     * Parse client keyword.
     *
     * @return Parsed node
     */
    private STNode parseClientKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLIENT_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CLIENT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse abstract keyword.
     *
     * @return Parsed node
     */
    private STNode parseAbstractKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ABSTRACT_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ABSTRACT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse object keyword.
     *
     * @return Parsed node
     */
    private STNode parseObjectKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OBJECT_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.OBJECT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse object members.
     *
     * @return Parsed node
     */
    private STNode parseObjectMembers() {
        ArrayList<STNode> objectMembers = new ArrayList<>();
        while (!isEndOfObjectTypeNode()) {
            startContext(ParserRuleContext.OBJECT_MEMBER);
            STNode member = parseObjectMember(peek().kind);
            endContext();

            // Null member indicates the end of object members
            if (member == null) {
                break;
            }
            objectMembers.add(member);
        }

        return STNodeFactory.createNodeList(objectMembers);
    }

    private STNode parseObjectMember() {
        STToken nextToken = peek();
        return parseObjectMember(nextToken.kind);
    }

    private STNode parseObjectMember(SyntaxKind nextTokenKind) {
        STNode metadata;
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                // Null return indicates the end of object members
                return null;
            case ASTERISK_TOKEN:
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
            case REMOTE_KEYWORD:
            case FUNCTION_KEYWORD:
                metadata = createEmptyMetadata();
                break;
            case DOCUMENTATION_LINE:
            case AT_TOKEN:
                metadata = parseMetaData(nextTokenKind);
                nextTokenKind = peek().kind;
                break;
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    metadata = createEmptyMetadata();
                    break;
                }

                Solution solution = recover(peek(), ParserRuleContext.OBJECT_MEMBER_START);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseObjectMember(solution.tokenKind);
        }

        return parseObjectMember(nextTokenKind, metadata);
    }

    private STNode parseObjectMember(SyntaxKind nextTokenKind, STNode metadata) {
        STNode member;
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                // TODO report metadata
                return null;
            case ASTERISK_TOKEN:
                STNode asterisk = consume();
                STNode type = parseTypeReference();
                STNode semicolonToken = parseSemicolon();
                member = STNodeFactory.createTypeReferenceNode(asterisk, type, semicolonToken);
                break;
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
                STNode visibilityQualifier = parseObjectMemberVisibility();
                member = parseObjectMethodOrField(metadata, visibilityQualifier);
                break;
            case REMOTE_KEYWORD:
                member = parseObjectMethodOrField(metadata, STNodeFactory.createEmptyNode());
                break;
            case FUNCTION_KEYWORD:
                member = parseObjectMethod(metadata, STNodeFactory.createEmptyNode());
                break;
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    member = parseObjectField(metadata, STNodeFactory.createEmptyNode());
                    break;
                }

                Solution solution = recover(peek(), ParserRuleContext.OBJECT_MEMBER_WITHOUT_METADATA);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseObjectMember(solution.tokenKind);
        }

        return member;
    }

    private STNode parseObjectMethodOrField(STNode metadata, STNode methodQualifiers) {
        STToken nextToken = peek(1);
        STToken nextNextToken = peek(2);
        return parseObjectMethodOrField(nextToken.kind, nextNextToken.kind, metadata, methodQualifiers);
    }

    /**
     * Parse an object member, given the visibility modifier. Object member can have
     * only one visibility qualifier. This mean the methodQualifiers list can have
     * one qualifier at-most.
     *
     * @param visibilityQualifiers Visibility qualifiers. A modifier can be
     *            a syntax node with either 'PUBLIC' or 'PRIVATE'.
     * @param nextTokenKind Next token kind
     * @param nextNextTokenKind Kind of the token after the
     * @param metadata Metadata
     * @param visibilityQualifiers Visibility qualifiers
     * @return Parse object member node
     */
    private STNode parseObjectMethodOrField(SyntaxKind nextTokenKind, SyntaxKind nextNextTokenKind, STNode metadata,
                                            STNode visibilityQualifiers) {
        switch (nextTokenKind) {
            case REMOTE_KEYWORD:
                STNode remoteKeyword = parseRemoteKeyword();
                ArrayList<STNode> methodQualifiers = new ArrayList<>();
                if (!isEmpty(visibilityQualifiers)) {
                    methodQualifiers.add(visibilityQualifiers);
                }
                methodQualifiers.add(remoteKeyword);
                return parseObjectMethod(metadata, STNodeFactory.createNodeList(methodQualifiers));
            case FUNCTION_KEYWORD:
                return parseObjectMethod(metadata, visibilityQualifiers);

            // All 'type starting tokens' here. should be same as 'parseTypeDescriptor(...)'
            case IDENTIFIER_TOKEN:
                if (nextNextTokenKind != SyntaxKind.OPEN_PAREN_TOKEN) {
                    // Here we try to catch the common user error of missing the function keyword.
                    // In such cases, lookahead for the open-parenthesis and figure out whether
                    // this is an object-method with missing name. If yes, then try to recover.
                    return parseObjectField(metadata, visibilityQualifiers);
                }
                break;
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    return parseObjectField(metadata, visibilityQualifiers);
                }
                break;
        }

        Solution solution = recover(peek(), ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY, metadata,
                visibilityQualifiers);

        // If the parser recovered by inserting a token, then try to re-parse the same
        // rule with the inserted token. This is done to pick the correct branch
        // to continue the parsing.
        if (solution.action == Action.REMOVE) {
            return solution.recoveredNode;
        }

        return parseObjectMethodOrField(solution.tokenKind, nextTokenKind, metadata, visibilityQualifiers);
    }

    /**
     * Parse object visibility. Visibility can be <code>public</code> or <code>private</code>.
     *
     * @return Parsed node
     */
    private STNode parseObjectMemberVisibility() {
        STToken token = peek();
        if (token.kind == SyntaxKind.PUBLIC_KEYWORD || token.kind == SyntaxKind.PRIVATE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.PUBLIC_KEYWORD);
            return sol.recoveredNode;
        }
    }

    private STNode parseRemoteKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.REMOTE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.REMOTE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    private STNode parseObjectField(STNode metadata, STNode methodQualifiers) {
        STToken nextToken = peek();
        if (nextToken.kind != SyntaxKind.READONLY_KEYWORD) {
            STNode readonlyQualifier = STNodeFactory.createEmptyNode();
            STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
            STNode fieldName = parseVariableName();
            return parseObjectFieldRhs(metadata, methodQualifiers, readonlyQualifier, type, fieldName);
        }

        // If the readonly-keyword is present, check whether its qualifier
        // or the readonly-type-desc.
        STNode type;
        STNode readonlyQualifier = parseReadonlyKeyword();
        nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STNode fieldNameOrTypeDesc = parseQualifiedIdentifier(ParserRuleContext.RECORD_FIELD_NAME_OR_TYPE_NAME);
            if (fieldNameOrTypeDesc.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                // readonly a:b
                // Then treat "a:b" as the type-desc
                type = fieldNameOrTypeDesc;
            } else {
                // readonly a
                nextToken = peek();
                switch (nextToken.kind) {
                    case SEMICOLON_TOKEN: // readonly a;
                    case EQUAL_TOKEN: // readonly a =
                        // Then treat "readonly" as type-desc, and "a" as the field-name
                        type = readonlyQualifier;
                        readonlyQualifier = STNodeFactory.createEmptyNode();
                        return parseObjectFieldRhs(metadata, methodQualifiers, readonlyQualifier, type,
                                fieldNameOrTypeDesc);
                    default:
                        // else,
                        type = fieldNameOrTypeDesc;
                        break;
                }
            }
        } else if (isTypeStartingToken(nextToken.kind)) {
            type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
        } else {
            type = parseComplexTypeDescriptor(readonlyQualifier, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD, false);
            readonlyQualifier = STNodeFactory.createEmptyNode();
        }

        STNode fieldName = parseVariableName();
        return parseObjectFieldRhs(metadata, methodQualifiers, readonlyQualifier, type, fieldName);
    }

    /**
     * Parse object field rhs, and complete the object field parsing. Returns the parsed object field.
     *
     * @param metadata Metadata
     * @param visibilityQualifier Visibility qualifier
     * @param readonlyQualifier Readonly qualifier
     * @param type Type descriptor
     * @param fieldName Field name
     * @return Parsed object field
     */
    private STNode parseObjectFieldRhs(STNode metadata, STNode visibilityQualifier, STNode readonlyQualifier,
                                       STNode type, STNode fieldName) {
        STToken nextToken = peek();
        return parseObjectFieldRhs(nextToken.kind, metadata, visibilityQualifier, readonlyQualifier, type, fieldName);
    }

    /**
     * Parse object field rhs, and complete the object field parsing. Returns the parsed object field.
     *
     * @param nextTokenKind Kind of the next token
     * @param metadata Metadata
     * @param visibilityQualifier Visibility qualifier
     * @param readonlyQualifier Readonly qualifier
     * @param type Type descriptor
     * @param fieldName Field name
     * @return Parsed object field
     */
    private STNode parseObjectFieldRhs(SyntaxKind nextTokenKind, STNode metadata, STNode visibilityQualifier,
                                       STNode readonlyQualifier, STNode type, STNode fieldName) {
        STNode equalsToken;
        STNode expression;
        STNode semicolonToken;
        switch (nextTokenKind) {
            case SEMICOLON_TOKEN:
                equalsToken = STNodeFactory.createEmptyNode();
                expression = STNodeFactory.createEmptyNode();
                semicolonToken = parseSemicolon();
                break;
            case EQUAL_TOKEN:
                equalsToken = parseAssignOp();
                expression = parseExpression();
                semicolonToken = parseSemicolon();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.OBJECT_FIELD_RHS, metadata, visibilityQualifier,
                        type, fieldName);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseObjectFieldRhs(solution.tokenKind, metadata, visibilityQualifier, readonlyQualifier, type,
                        fieldName);
        }

        return STNodeFactory.createObjectFieldNode(metadata, visibilityQualifier, readonlyQualifier, type, fieldName,
                equalsToken, expression, semicolonToken);
    }

    private STNode parseObjectMethod(STNode metadata, STNode methodQualifiers) {
        return parseFuncDefOrFuncTypeDesc(metadata, methodQualifiers, true);
    }

    /**
     * Parse if-else statement.
     * <code>
     * if-else-stmt := if expression block-stmt [else-block]
     * </code>
     *
     * @return If-else block
     */
    private STNode parseIfElseBlock() {
        startContext(ParserRuleContext.IF_BLOCK);
        STNode ifKeyword = parseIfKeyword();
        STNode condition = parseExpression();
        STNode ifBody = parseBlockNode();
        endContext();

        STNode elseBody = parseElseBlock();
        return STNodeFactory.createIfElseStatementNode(ifKeyword, condition, ifBody, elseBody);
    }

    /**
     * Parse if-keyword.
     *
     * @return Parsed if-keyword node
     */
    private STNode parseIfKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IF_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.IF_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse else-keyword.
     *
     * @return Parsed else keyword node
     */
    private STNode parseElseKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ELSE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ELSE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse block node.
     * <code>
     * block-stmt := { sequence-stmt }
     * sequence-stmt := statement*
     * </code>
     *
     * @return Parse block node
     */
    private STNode parseBlockNode() {
        startContext(ParserRuleContext.BLOCK_STMT);
        STNode openBrace = parseOpenBrace();
        STNode stmts = parseStatements();
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createBlockStatementNode(openBrace, stmts, closeBrace);
    }

    /**
     * Parse else block.
     * <code>else-block := else (if-else-stmt | block-stmt)</code>
     *
     * @return Else block
     */
    private STNode parseElseBlock() {
        STToken nextToken = peek();
        if (nextToken.kind != SyntaxKind.ELSE_KEYWORD) {
            return STNodeFactory.createEmptyNode();
        }

        STNode elseKeyword = parseElseKeyword();
        STNode elseBody = parseElseBody();
        return STNodeFactory.createElseBlockNode(elseKeyword, elseBody);
    }

    /**
     * Parse else node body.
     * <code>else-body := if-else-stmt | block-stmt</code>
     *
     * @return Else node body
     */
    private STNode parseElseBody() {
        STToken nextToken = peek();
        return parseElseBody(nextToken.kind);
    }

    private STNode parseElseBody(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case IF_KEYWORD:
                return parseIfElseBlock();
            case OPEN_BRACE_TOKEN:
                return parseBlockNode();
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.ELSE_BODY);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseElseBody(solution.tokenKind);
        }
    }

    /**
     * Parse while statement.
     * <code>while-stmt := while expression block-stmt</code>
     *
     * @return While statement
     */
    private STNode parseWhileStatement() {
        startContext(ParserRuleContext.WHILE_BLOCK);
        STNode whileKeyword = parseWhileKeyword();
        STNode condition = parseExpression();
        STNode whileBody = parseBlockNode();
        endContext();
        return STNodeFactory.createWhileStatementNode(whileKeyword, condition, whileBody);
    }

    /**
     * Parse while-keyword.
     *
     * @return While-keyword node
     */
    private STNode parseWhileKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.WHILE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.WHILE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse panic statement.
     * <code>panic-stmt := panic expression ;</code>
     *
     * @return Panic statement
     */
    private STNode parsePanicStatement() {
        startContext(ParserRuleContext.PANIC_STMT);
        STNode panicKeyword = parsePanicKeyword();
        STNode expression = parseExpression();
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createPanicStatementNode(panicKeyword, expression, semicolon);
    }

    /**
     * Parse panic-keyword.
     *
     * @return Panic-keyword node
     */
    private STNode parsePanicKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.PANIC_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.PANIC_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse check expression. This method is used to parse both check expression
     * as well as check action.
     *
     * <p>
     * <code>
     * checking-expr := checking-keyword expression
     * checking-action := checking-keyword action
     * </code>
     *
     * @param allowActions Allow actions
     * @param isRhsExpr Is rhs expression
     * @return Check expression node
     */
    private STNode parseCheckExpression(boolean isRhsExpr, boolean allowActions) {
        STNode checkingKeyword = parseCheckingKeyword();
        STNode expr = parseExpression(OperatorPrecedence.EXPRESSION_ACTION, isRhsExpr, allowActions);
        if (isAction(expr)) {
            return STNodeFactory.createCheckExpressionNode(SyntaxKind.CHECK_ACTION, checkingKeyword, expr);
        } else {
            return STNodeFactory.createCheckExpressionNode(SyntaxKind.CHECK_EXPRESSION, checkingKeyword, expr);
        }
    }

    /**
     * Parse checking keyword.
     * <p>
     * <code>
     * checking-keyword := check | checkpanic
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseCheckingKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CHECK_KEYWORD || token.kind == SyntaxKind.CHECKPANIC_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CHECKING_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     *
     * Parse continue statement.
     * <code>continue-stmt := continue ; </code>
     *
     * @return continue statement
     */
    private STNode parseContinueStatement() {
        startContext(ParserRuleContext.CONTINUE_STATEMENT);
        STNode continueKeyword = parseContinueKeyword();
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createContinueStatementNode(continueKeyword, semicolon);
    }

    /**
     * Parse continue-keyword.
     *
     * @return continue-keyword node
     */
    private STNode parseContinueKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CONTINUE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CONTINUE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse return statement.
     * <code>return-stmt := return [ action-or-expr ] ;</code>
     *
     * @return Return statement
     */
    private STNode parseReturnStatement() {
        startContext(ParserRuleContext.RETURN_STMT);
        STNode returnKeyword = parseReturnKeyword();
        STNode returnRhs = parseReturnStatementRhs(returnKeyword);
        endContext();
        return returnRhs;
    }

    /**
     * Parse return-keyword.
     *
     * @return Return-keyword node
     */
    private STNode parseReturnKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RETURN_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.RETURN_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse break statement.
     * <code>break-stmt := break ; </code>
     *
     * @return break statement
     */
    private STNode parseBreakStatement() {
        startContext(ParserRuleContext.BREAK_STATEMENT);
        STNode breakKeyword = parseBreakKeyword();
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createBreakStatementNode(breakKeyword, semicolon);
    }

    /**
     * Parse break-keyword.
     *
     * @return break-keyword node
     */
    private STNode parseBreakKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BREAK_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.BREAK_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse the right hand side of a return statement.
     * </p>
     * <code>
     * return-stmt-rhs := ; |  action-or-expr ;
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseReturnStatementRhs(STNode returnKeyword) {
        STNode expr;
        STToken token = peek();

        switch (token.kind) {
            case SEMICOLON_TOKEN:
                expr = STNodeFactory.createEmptyNode();
                break;
            default:
                expr = parseActionOrExpression();
                break;
        }

        STNode semicolon = parseSemicolon();
        return STNodeFactory.createReturnStatementNode(returnKeyword, expr, semicolon);
    }

    /**
     * Parse mapping constructor expression.
     * <p>
     * <code>mapping-constructor-expr := { [field (, field)*] }</code>
     *
     * @return Parsed node
     */
    private STNode parseMappingConstructorExpr() {
        startContext(ParserRuleContext.MAPPING_CONSTRUCTOR);
        STNode openBrace = parseOpenBrace();
        STNode fields = parseMappingConstructorFields();
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createMappingConstructorExpressionNode(openBrace, fields, closeBrace);
    }

    /**
     * Parse mapping constructor fields.
     *
     * @return Parsed node
     */
    private STNode parseMappingConstructorFields() {
        STToken nextToken = peek();
        if (isEndOfMappingConstructor(nextToken.kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first field mapping, that has no leading comma
        List<STNode> fields = new ArrayList<>();
        STNode field = parseMappingField(ParserRuleContext.FIRST_MAPPING_FIELD);
        fields.add(field);

        return parseMappingConstructorFields(fields);
    }

    private STNode parseMappingConstructorFields(List<STNode> fields) {
        STToken nextToken;
        // Parse the remaining field mappings
        STNode mappingFieldEnd;
        nextToken = peek();
        while (!isEndOfMappingConstructor(nextToken.kind)) {
            mappingFieldEnd = parseMappingFieldEnd(nextToken.kind);
            if (mappingFieldEnd == null) {
                break;
            }
            fields.add(mappingFieldEnd);

            // Parse field
            STNode field = parseMappingField(ParserRuleContext.MAPPING_FIELD);
            fields.add(field);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(fields);
    }

    private STNode parseMappingFieldEnd() {
        return parseMappingFieldEnd(peek().kind);
    }

    private STNode parseMappingFieldEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.MAPPING_FIELD_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseMappingFieldEnd(solution.tokenKind);

        }
    }

    private boolean isEndOfMappingConstructor(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
            case READONLY_KEYWORD:
                return false;
            case EOF_TOKEN:
            case AT_TOKEN:
            case DOCUMENTATION_LINE:
            case CLOSE_BRACE_TOKEN:
            case SEMICOLON_TOKEN:
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
            case FUNCTION_KEYWORD:
            case RETURNS_KEYWORD:
            case SERVICE_KEYWORD:
            case TYPE_KEYWORD:
            case LISTENER_KEYWORD:
            case CONST_KEYWORD:
            case FINAL_KEYWORD:
            case RESOURCE_KEYWORD:
                return true;
            default:
                return isSimpleType(tokenKind);
        }
    }

    /**
     * Parse mapping constructor field.
     * <p>
     * <code>field := specific-field | computed-name-field | spread-field</code>
     *
     * @param fieldContext Context of the mapping field
     * @param leadingComma Leading comma
     * @return Parsed node
     */
    private STNode parseMappingField(ParserRuleContext fieldContext) {
        STToken nextToken = peek();
        return parseMappingField(nextToken.kind, fieldContext);
    }

    private STNode parseMappingField(SyntaxKind tokenKind, ParserRuleContext fieldContext) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
                STNode readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseSpecificFieldWithOptionalValue(readonlyKeyword);
            case STRING_LITERAL:
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseQualifiedSpecificField(readonlyKeyword);
            // case FINAL_KEYWORD:
            case READONLY_KEYWORD:
                readonlyKeyword = parseReadonlyKeyword();
                return parseSpecificField(readonlyKeyword);
            case OPEN_BRACKET_TOKEN:
                return parseComputedField();
            case ELLIPSIS_TOKEN:
                STNode ellipsis = parseEllipsis();
                STNode expr = parseExpression();
                return STNodeFactory.createSpreadFieldNode(ellipsis, expr);
            case CLOSE_BRACE_TOKEN:
                if (fieldContext == ParserRuleContext.FIRST_MAPPING_FIELD) {
                    return null;
                }
                // else fall through
            default:
                STToken token = peek();
                Solution solution = recover(token, fieldContext, fieldContext);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseMappingField(solution.tokenKind, fieldContext);
        }
    }

    private STNode parseSpecificField(STNode readonlyKeyword) {
        STToken nextToken = peek();
        return parseSpecificField(nextToken.kind, readonlyKeyword);
    }

    private STNode parseSpecificField(SyntaxKind nextTokenKind, STNode readonlyKeyword) {
        switch (nextTokenKind) {
            case STRING_LITERAL:
                return parseQualifiedSpecificField(readonlyKeyword);
            case IDENTIFIER_TOKEN:
                return parseSpecificFieldWithOptionalValue(readonlyKeyword);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.SPECIFIC_FIELD, readonlyKeyword);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseSpecificField(solution.tokenKind, readonlyKeyword);

        }
    }

    private STNode parseQualifiedSpecificField(STNode readonlyKeyword) {
        STNode key = parseStringLiteral();
        STNode colon = parseColon();
        STNode valueExpr = parseExpression();
        return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, valueExpr);
    }

    /**
     * Parse mapping constructor specific-field with an optional value.
     *
     * @return Parsed node
     */
    private STNode parseSpecificFieldWithOptionalValue(STNode readonlyKeyword) {
        STNode key = parseIdentifier(ParserRuleContext.MAPPING_FIELD_NAME);
        return parseSpecificFieldRhs(readonlyKeyword, key);
    }

    private STNode parseSpecificFieldRhs(STNode readonlyKeyword, STNode key) {
        STToken nextToken = peek();
        return parseSpecificFieldRhs(nextToken.kind, readonlyKeyword, key);
    }

    private STNode parseSpecificFieldRhs(SyntaxKind tokenKind, STNode readonlyKeyword, STNode key) {
        STNode colon;
        STNode valueExpr;

        switch (tokenKind) {
            case COLON_TOKEN:
                colon = parseColon();
                valueExpr = parseExpression();
                break;
            case COMMA_TOKEN:
                colon = STNodeFactory.createEmptyNode();
                valueExpr = STNodeFactory.createEmptyNode();
                break;
            default:
                if (isEndOfMappingConstructor(tokenKind)) {
                    colon = STNodeFactory.createEmptyNode();
                    valueExpr = STNodeFactory.createEmptyNode();
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.SPECIFIC_FIELD_RHS, readonlyKeyword, key);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseSpecificFieldRhs(solution.tokenKind, readonlyKeyword, key);

        }
        return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, valueExpr);
    }

    /**
     * Parse string literal.
     *
     * @return Parsed node
     */
    private STNode parseStringLiteral() {
        STToken token = peek();
        if (token.kind == SyntaxKind.STRING_LITERAL) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.STRING_LITERAL);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse colon token.
     *
     * @return Parsed node
     */
    private STNode parseColon() {
        STToken token = peek();
        if (token.kind == SyntaxKind.COLON_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.COLON);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse readonly keyword.
     *
     * @return Parsed node
     */
    private STNode parseReadonlyKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.READONLY_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.READONLY_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse computed-name-field of a mapping constructor expression.
     * <p>
     * <code>computed-name-field := [ field-name-expr ] : value-expr</code>
     *
     * @param leadingComma Leading comma
     * @return Parsed node
     */
    private STNode parseComputedField() {
        // Parse computed field name
        startContext(ParserRuleContext.COMPUTED_FIELD_NAME);
        STNode openBracket = parseOpenBracket();
        STNode fieldNameExpr = parseExpression();
        STNode closeBracket = parseCloseBracket();
        endContext();

        // Parse rhs
        STNode colon = parseColon();
        STNode valueExpr = parseExpression();
        return STNodeFactory.createComputedNameFieldNode(openBracket, fieldNameExpr, closeBracket, colon, valueExpr);
    }

    /**
     * Parse open bracket.
     *
     * @return Parsed node
     */
    private STNode parseOpenBracket() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACKET_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.OPEN_BRACKET);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse compound assignment statement, which takes the following format.
     * </p>
     * <code>assignment-stmt := lvexpr CompoundAssignmentOperator action-or-expr ;</code>
     *
     * @return Parsed node
     */
    private STNode parseCompoundAssignmentStmt() {
        startContext(ParserRuleContext.COMPOUND_ASSIGNMENT_STMT);
        STNode varName = parseVariableName();
        STNode compoundAssignmentStmt = parseCompoundAssignmentStmtRhs(varName);
        endContext();
        return compoundAssignmentStmt;
    }

    /**
     * <p>
     * Parse the RHS portion of the compound assignment.
     * </p>
     * <code>compound-assignment-stmt-rhs := CompoundAssignmentOperator action-or-expr ;</code>
     *
     * @param lvExpr LHS expression
     * @return Parsed node
     */
    private STNode parseCompoundAssignmentStmtRhs(STNode lvExpr) {
        validateLVExpr(lvExpr);
        STNode binaryOperator = parseCompoundBinaryOperator();
        STNode equalsToken = parseAssignOp();
        STNode expr = parseActionOrExpression();
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createCompoundAssignmentStatementNode(lvExpr, binaryOperator, equalsToken, expr,
                semicolon);
    }

    /**
     * Parse compound binary operator.
     * <code>BinaryOperator := + | - | * | / | & | | | ^ | << | >> | >>></code>
     *
     * @return Parsed node
     */
    private STNode parseCompoundBinaryOperator() {
        STToken token = peek();
        if (isCompoundBinaryOperator(token.kind)) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.COMPOUND_BINARY_OPERATOR);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse service declaration.
     * <p>
     * <code>
     * service-decl := metadata service [variable-name] on expression-list service-body-block
     * <br/>
     * expression-list := expression (, expression)*
     * </code>
     *
     * @param metadata Metadata
     * @return Parsed node
     */
    private STNode parseServiceDecl(STNode metadata) {
        startContext(ParserRuleContext.SERVICE_DECL);
        STNode serviceKeyword = parseServiceKeyword();
        STNode serviceDecl = parseServiceRhs(metadata, serviceKeyword);
        endContext();
        return serviceDecl;
    }

    /**
     * Parse rhs of the service declaration.
     * <p>
     * <code>
     * service-rhs := [variable-name] on expression-list service-body-block
     * </code>
     *
     * @param metadata Metadata
     * @param serviceKeyword Service keyword
     * @return Parsed node
     */
    private STNode parseServiceRhs(STNode metadata, STNode serviceKeyword) {
        STNode serviceName = parseServiceName();
        STNode onKeyword = parseOnKeyword();
        STNode expressionList = parseListeners();
        STNode serviceBody = parseServiceBody();

        onKeyword = cloneWithDiagnosticIfListEmpty(expressionList,
                onKeyword, DiagnosticErrorCode.ERROR_MISSING_EXPRESSION);
        return STNodeFactory.createServiceDeclarationNode(metadata, serviceKeyword, serviceName, onKeyword,
                expressionList, serviceBody);
    }

    private STNode parseServiceName() {
        STToken nextToken = peek();
        return parseServiceName(nextToken.kind);
    }

    private STNode parseServiceName(SyntaxKind kind) {
        switch (kind) {
            case IDENTIFIER_TOKEN:
                return parseIdentifier(ParserRuleContext.SERVICE_NAME);
            case ON_KEYWORD:
                return STNodeFactory.createEmptyNode();
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.OPTIONAL_SERVICE_NAME);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseServiceName(solution.tokenKind);
        }
    }

    /**
     * Parse service keyword.
     *
     * @return Parsed node
     */
    private STNode parseServiceKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SERVICE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.SERVICE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Check whether the given token kind is a compound binary operator.
     * <p>
     * <code>compound-binary-operator := + | - | * | / | & | | | ^ | << | >> | >>></code>
     *
     * @param tokenKind STToken kind
     * @return <code>true</code> if the token kind refers to a binary operator. <code>false</code> otherwise
     */
    private boolean isCompoundBinaryOperator(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case SLASH_TOKEN:
            case ASTERISK_TOKEN:
            case BITWISE_AND_TOKEN:
            case BITWISE_XOR_TOKEN:
            case PIPE_TOKEN:
                return getNextNextToken(tokenKind).kind == SyntaxKind.EQUAL_TOKEN;
            default:
                return false;
        }
    }

    /**
     * Parse on keyword.
     *
     * @return Parsed node
     */
    private STNode parseOnKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ON_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ON_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse listener references.
     * <p>
     * <code>expression-list := expression (, expression)*</code>
     *
     * @return Parsed node
     */
    private STNode parseListeners() {
        // TODO: Change body to align with parseOptionalExpressionsList()
        startContext(ParserRuleContext.LISTENERS_LIST);
        List<STNode> listeners = new ArrayList<>();

        STToken nextToken = peek();
        if (isEndOfExpressionsList(nextToken.kind)) {
            endContext();
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first expression, that has no leading comma
        STNode leadingComma = STNodeFactory.createEmptyNode();
        STNode exprListItem = parseExpressionListItem(leadingComma);
        listeners.add(exprListItem);

        // Parse the remaining expressions
        nextToken = peek();
        while (!isEndOfExpressionsList(nextToken.kind)) {
            leadingComma = parseComma();
            exprListItem = parseExpressionListItem(leadingComma);
            listeners.add(exprListItem);
            nextToken = peek();
        }

        endContext();
        return STNodeFactory.createNodeList(listeners);
    }

    private boolean isEndOfExpressionsList(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case COMMA_TOKEN:
                return false;
            case EOF_TOKEN:
            case SEMICOLON_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case OPEN_BRACE_TOKEN:
                return true;
            default:
                return !isValidExprStart(tokenKind);
        }
    }

    /**
     * Parse expression list item.
     *
     * @param leadingComma Leading comma
     * @return Parsed node
     */
    private STNode parseExpressionListItem(STNode leadingComma) {
        STNode expr = parseExpression();
        return STNodeFactory.createExpressionListItemNode(leadingComma, expr);
    }

    /**
     * Parse service body.
     * <p>
     * <code>
     * service-body-block := { service-method-defn* }
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseServiceBody() {
        STNode openBrace = parseOpenBrace();
        STNode resources = parseResources();
        STNode closeBrace = parseCloseBrace();
        return STNodeFactory.createServiceBodyNode(openBrace, resources, closeBrace);
    }

    /**
     * Parse service resource definitions.
     *
     * @return Parsed node
     */
    private STNode parseResources() {
        List<STNode> resources = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfServiceDecl(nextToken.kind)) {
            STNode serviceMethod = parseResource();
            if (serviceMethod == null) {
                break;
            }
            resources.add(serviceMethod);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(resources);
    }

    private boolean isEndOfServiceDecl(SyntaxKind tokenKind) {
        switch (tokenKind) {
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

    /**
     * Parse resource definition (i.e. service-method-defn).
     * <p>
     * <code>
     * service-body-block := { service-method-defn* }
     * <br/>
     * service-method-defn := metadata [resource] function identifier function-signature method-defn-body
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseResource() {
        STToken nextToken = peek();
        return parseResource(nextToken.kind);
    }

    private STNode parseResource(SyntaxKind nextTokenKind) {
        STNode metadata;
        switch (nextTokenKind) {
            case RESOURCE_KEYWORD:
            case FUNCTION_KEYWORD:
                metadata = createEmptyMetadata();
                break;
            case DOCUMENTATION_LINE:
            case AT_TOKEN:
                metadata = parseMetaData(nextTokenKind);
                nextTokenKind = peek().kind;
                break;
            default:
                if (isEndOfServiceDecl(nextTokenKind)) {
                    return null;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RESOURCE_DEF);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseResource(solution.tokenKind);
        }

        return parseResource(nextTokenKind, metadata);
    }

    private STNode parseResource(SyntaxKind nextTokenKind, STNode metadata) {
        switch (nextTokenKind) {
            case RESOURCE_KEYWORD:
                STNode resourceKeyword = parseResourceKeyword();
                return parseFuncDefinition(metadata, resourceKeyword, false);
            case FUNCTION_KEYWORD:
                return parseFuncDefinition(metadata, STNodeFactory.createEmptyNode(), false);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RESOURCE_DEF, metadata);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseResource(solution.tokenKind, metadata);
        }
    }

    /**
     * Parse resource keyword.
     *
     * @return Parsed node
     */
    private STNode parseResourceKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RESOURCE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.RESOURCE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Check whether next construct is a service declaration or not. This method is
     * used to determine whether an end-of-block is reached, if the next token is
     * a service-keyword. Because service-keyword can be used in statements as well
     * as in top-level node (service-decl). We have reached a service-decl, then
     * it could be due to missing close-brace at the end of the current block.
     *
     * @return <code>true</code> if the next construct is a service declaration.
     *         <code>false</code> otherwise
     */
    private boolean isServiceDeclStart(ParserRuleContext currentContext, int lookahead) {
        // Assume we always reach here after a peek()
        switch (peek(lookahead + 1).kind) {
            case IDENTIFIER_TOKEN:
                SyntaxKind tokenAfterIdentifier = peek(lookahead + 2).kind;
                switch (tokenAfterIdentifier) {
                    case ON_KEYWORD: // service foo on ...
                    case OPEN_BRACE_TOKEN: // missing listeners--> service foo {
                        return true;
                    case EQUAL_TOKEN: // service foo = ...
                    case SEMICOLON_TOKEN: // service foo;
                    case QUESTION_MARK_TOKEN: // service foo?;
                        return false;
                    default:
                        // If not any of above, this is not a valid syntax.
                        return false;
                }
            case ON_KEYWORD:
                // Next token sequence is similar to: `service on ...`.
                // Then this is a service decl.
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse listener declaration, given the qualifier.
     *
     * @param metadata Metadata
     * @param qualifier Qualifier that precedes the listener declaration
     * @return Parsed node
     */
    private STNode parseListenerDeclaration(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.LISTENER_DECL);
        STNode listenerKeyword = parseListenerKeyword();
        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode variableName = parseVariableName();
        STNode equalsToken = parseAssignOp();
        STNode initializer = parseExpression();
        STNode semicolonToken = parseSemicolon();
        endContext();
        return STNodeFactory.createListenerDeclarationNode(metadata, qualifier, listenerKeyword, typeDesc, variableName,
                equalsToken, initializer, semicolonToken);
    }

    /**
     * Parse listener keyword.
     *
     * @return Parsed node
     */
    private STNode parseListenerKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.LISTENER_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.LISTENER_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse constant declaration, given the qualifier.
     * <p>
     * <code>module-const-decl := metadata [public] const [type-descriptor] identifier = const-expr ;</code>
     *
     * @param metadata Metadata
     * @param qualifier Qualifier that precedes the listener declaration
     * @return Parsed node
     */
    private STNode parseConstantDeclaration(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.CONSTANT_DECL);
        STNode constKeyword = parseConstantKeyword();
        STNode constDecl = parseConstDecl(metadata, qualifier, constKeyword);
        endContext();
        return constDecl;
    }

    /**
     * Parse the components that follows after the const keyword of a constant declaration.
     *
     * @param metadata Metadata
     * @param qualifier Qualifier that precedes the constant decl
     * @param constKeyword Const keyword
     * @return Parsed node
     */
    private STNode parseConstDecl(STNode metadata, STNode qualifier, STNode constKeyword) {
        STToken nextToken = peek();
        return parseConstDeclFromType(nextToken.kind, metadata, qualifier, constKeyword);
    }

    private STNode parseConstDeclFromType(SyntaxKind nextTokenKind, STNode metadata, STNode qualifier,
                                          STNode constKeyword) {
        switch (nextTokenKind) {
            case ANNOTATION_KEYWORD:
                switchContext(ParserRuleContext.ANNOTATION_DECL);
                return parseAnnotationDeclaration(metadata, qualifier, constKeyword);
            case IDENTIFIER_TOKEN:
                return parseConstantDeclWithOptionalType(metadata, qualifier, constKeyword);
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    break;
                }
                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.CONST_DECL_TYPE, metadata, qualifier, constKeyword);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseConstDeclFromType(solution.tokenKind, metadata, qualifier, constKeyword);
        }

        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode variableName = parseVariableName();
        STNode equalsToken = parseAssignOp();
        STNode initializer = parseExpression();
        STNode semicolonToken = parseSemicolon();
        return STNodeFactory.createConstantDeclarationNode(metadata, qualifier, constKeyword, typeDesc, variableName,
                equalsToken, initializer, semicolonToken);
    }

    private STNode parseConstantDeclWithOptionalType(STNode metadata, STNode qualifier, STNode constKeyword) {
        STNode varNameOrTypeName = parseStatementStartIdentifier();
        STNode constDecl = parseConstantDeclRhs(metadata, qualifier, constKeyword, varNameOrTypeName);
        return constDecl;
    }

    /**
     * Parse the component that follows the first identifier in a const decl. The identifier
     * can be either the type-name (a user defined type) or the var-name there the type-name
     * is not present.
     *
     * @param qualifier Qualifier that precedes the constant decl
     * @param constKeyword Const keyword
     * @param typeOrVarName Identifier that follows the const-keywoord
     * @return Parsed node
     */
    private STNode parseConstantDeclRhs(STNode metadata, STNode qualifier, STNode constKeyword, STNode typeOrVarName) {
        STToken token = peek();
        return parseConstantDeclRhs(token.kind, metadata, qualifier, constKeyword, typeOrVarName);
    }

    private STNode parseConstantDeclRhs(SyntaxKind nextTokenKind, STNode metadata, STNode qualifier,
                                        STNode constKeyword, STNode typeOrVarName) {
        STNode type;
        STNode variableName;
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                type = typeOrVarName;
                variableName = parseVariableName();
                break;
            case EQUAL_TOKEN:
                variableName = ((STSimpleNameReferenceNode) typeOrVarName).name; // variableName is a token
                type = STNodeFactory.createEmptyNode();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.CONST_DECL_RHS, metadata, qualifier, constKeyword,
                        typeOrVarName);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseConstantDeclRhs(solution.tokenKind, metadata, qualifier, constKeyword, typeOrVarName);
        }

        STNode equalsToken = parseAssignOp();
        STNode initializer = parseExpression();
        STNode semicolonToken = parseSemicolon();
        return STNodeFactory.createConstantDeclarationNode(metadata, qualifier, constKeyword, type, variableName,
                equalsToken, initializer, semicolonToken);
    }

    /**
     * Parse const keyword.
     *
     * @return Parsed node
     */
    private STNode parseConstantKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CONST_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.CONST_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse nil type descriptor.
     * <p>
     * <code>nil-type-descriptor :=  ( ) </code>
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseNilTypeDescriptor() {
        startContext(ParserRuleContext.NIL_TYPE_DESCRIPTOR);
        STNode openParenthesisToken = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        STNode closeParenthesisToken = parseCloseParenthesis();
        endContext();

        return STNodeFactory.createNilTypeDescriptorNode(openParenthesisToken, closeParenthesisToken);
    }

    /**
     * Parse typeof expression.
     * <p>
     * <code>
     * typeof-expr := typeof expression
     * </code>
     *
     * @param isRhsExpr
     * @return Typeof expression node
     */
    private STNode parseTypeofExpression(boolean isRhsExpr) {
        STNode typeofKeyword = parseTypeofKeyword();

        // allow-actions flag is always false, since there will not be any actions
        // within the typeof-expression, due to the precedence.
        STNode expr = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, false);
        return STNodeFactory.createTypeofExpressionNode(typeofKeyword, expr);
    }

    /**
     * Parse typeof-keyword.
     *
     * @return Typeof-keyword node
     */
    private STNode parseTypeofKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TYPEOF_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TYPEOF_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse optional type descriptor.
     * <p>
     * <code>optional-type-descriptor := type-descriptor ? </code>
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseOptionalTypeDescriptor(STNode typeDescriptorNode) {
        startContext(ParserRuleContext.OPTIONAL_TYPE_DESCRIPTOR);
        STNode questionMarkToken = parseQuestionMark();
        endContext();

        return STNodeFactory.createOptionalTypeDescriptorNode(typeDescriptorNode, questionMarkToken);
    }

    /**
     * Parse unary expression.
     * <p>
     * <code>
     * unary-expr := + expression | - expression | ~ expression | ! expression
     * </code>
     *
     * @param isRhsExpr
     * @return Unary expression node
     */
    private STNode parseUnaryExpression(boolean isRhsExpr) {
        STNode unaryOperator = parseUnaryOperator();

        // allow-actions flag is always false, since there will not be any actions
        // within the unary expression, due to the precedence.
        STNode expr = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, false);
        return STNodeFactory.createUnaryExpressionNode(unaryOperator, expr);
    }

    /**
     * Parse unary operator.
     * <code>UnaryOperator := + | - | ~ | !</code>
     *
     * @return Parsed node
     */
    private STNode parseUnaryOperator() {
        STToken token = peek();
        if (isUnaryOperator(token.kind)) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.UNARY_OPERATOR);
            return sol.recoveredNode;
        }
    }

    /**
     * Check whether the given token kind is a unary operator.
     *
     * @param kind STToken kind
     * @return <code>true</code> if the token kind refers to a unary operator. <code>false</code> otherwise
     */
    private boolean isUnaryOperator(SyntaxKind kind) {
        switch (kind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse array type descriptor.
     * <p>
     * <code>
     * array-type-descriptor := member-type-descriptor [ [ array-length ] ]
     * member-type-descriptor := type-descriptor
     * array-length :=
     *    int-literal
     *    | constant-reference-expr
     *    | inferred-array-length
     * inferred-array-length := *
     * </code>
     * </p>
     *
     * @param memberTypeDesc
     *
     * @return Parsed Node
     */
    private STNode parseArrayTypeDescriptor(STNode memberTypeDesc) {
        startContext(ParserRuleContext.ARRAY_TYPE_DESCRIPTOR);
        STNode openBracketToken = parseOpenBracket();
        STNode arrayLengthNode = parseArrayLength();
        STNode closeBracketToken = parseCloseBracket();
        endContext();
        return STNodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openBracketToken, arrayLengthNode,
                closeBracketToken);
    }

    /**
     * Parse array length.
     * <p>
     * <code>
     *     array-length :=
     *    int-literal
     *    | constant-reference-expr
     *    | inferred-array-length
     * constant-reference-expr := variable-reference-expr
     * </code>
     * </p>
     *
     * @return Parsed array length
     */
    private STNode parseArrayLength() {
        STToken token = peek();
        switch (token.kind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case ASTERISK_TOKEN:
                return parseBasicLiteral();
            case CLOSE_BRACKET_TOKEN:
                return STNodeFactory.createEmptyNode();
            // Parsing variable-reference-expr is same as parsing qualified identifier
            case IDENTIFIER_TOKEN:
                return parseQualifiedIdentifier(ParserRuleContext.ARRAY_LENGTH);
            default:
                Solution sol = recover(token, ParserRuleContext.ARRAY_LENGTH);
                return sol.recoveredNode;
        }
    }

    /**
     * Parse annotations.
     * <p>
     * <i>Note: In the ballerina spec ({@link https://ballerina.io/spec/lang/2020R1/#annots})
     * annotations-list is specified as one-or-more annotations. And the usage is marked as
     * optional annotations-list. However, for the consistency of the tree, here we make the
     * annotation-list as zero-or-more annotations, and the usage is not-optional.</i>
     * <p>
     * <code>annots := annotation*</code>
     *
     * @return Parsed node
     */
    private STNode parseAnnotations() {
        STToken nextToken = peek();
        return parseAnnotations(nextToken.kind);
    }

    private STNode parseAnnotations(SyntaxKind nextTokenKind) {
        startContext(ParserRuleContext.ANNOTATIONS);
        List<STNode> annotList = new ArrayList<>();
        while (nextTokenKind == SyntaxKind.AT_TOKEN) {
            annotList.add(parseAnnotation());
            nextTokenKind = peek().kind;
        }

        endContext();
        return STNodeFactory.createNodeList(annotList);
    }

    /**
     * Parse annotation attachment.
     * <p>
     * <code>annotation := @ annot-tag-reference annot-value</code>
     *
     * @return Parsed node
     */
    private STNode parseAnnotation() {
        STNode atToken = parseAtToken();
        STNode annotReference;
        if (peek().kind != SyntaxKind.IDENTIFIER_TOKEN) {
            annotReference = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
        } else {
            annotReference = parseQualifiedIdentifier(ParserRuleContext.ANNOT_REFERENCE);
        }

        STNode annotValue;
        if (peek().kind == SyntaxKind.OPEN_BRACE_TOKEN) {
            annotValue = parseMappingConstructorExpr();
        } else {
            annotValue = STNodeFactory.createEmptyNode();
        }
        return STNodeFactory.createAnnotationNode(atToken, annotReference, annotValue);
    }

    /**
     * Parse '@' token.
     *
     * @return Parsed node
     */
    private STNode parseAtToken() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.AT_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(nextToken, ParserRuleContext.AT);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse metadata. Meta data consist of optional doc string and
     * an annotations list.
     * <p>
     * <code>metadata := [DocumentationString] annots</code>
     *
     * @return Parse node
     */
    private STNode parseMetaData(SyntaxKind nextTokenKind) {
        STNode docString;
        STNode annotations;
        switch (nextTokenKind) {
            case DOCUMENTATION_LINE:
                docString = parseDocumentationString();
                annotations = parseAnnotations();
                break;
            case AT_TOKEN:
                docString = STNodeFactory.createEmptyNode();
                annotations = parseAnnotations(nextTokenKind);
                break;
            default:
                return createEmptyMetadata();
        }

        return STNodeFactory.createMetadataNode(docString, annotations);
    }

    /**
     * Create empty metadata node.
     *
     * @return A metadata node with no doc string and no annotations
     */
    private STNode createEmptyMetadata() {
        return STNodeFactory.createMetadataNode(STNodeFactory.createEmptyNode(),
                STNodeFactory.createEmptyNodeList());
    }

    /**
     * Parse is expression.
     * <code>
     * is-expr := expression is type-descriptor
     * </code>
     *
     * @param lhsExpr Preceding expression of the is expression
     * @return Is expression node
     */
    private STNode parseTypeTestExpression(STNode lhsExpr) {
        STNode isKeyword = parseIsKeyword();
        STNode typeDescriptor = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_EXPRESSION);
        return STNodeFactory.createTypeTestExpressionNode(lhsExpr, isKeyword, typeDescriptor);
    }

    /**
     * Parse is-keyword.
     *
     * @return Is-keyword node
     */
    private STNode parseIsKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IS_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.IS_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse local type definition statement statement.
     * <code>ocal-type-defn-stmt := [annots] type identifier type-descriptor ;</code>
     *
     * @return local type definition statement statement
     */
    private STNode parseLocalTypeDefinitionStatement(STNode annots) {
        startContext(ParserRuleContext.LOCAL_TYPE_DEFINITION_STMT);
        STNode typeKeyword = parseTypeKeyword();
        STNode typeName = parseTypeName();
        STNode typeDescriptor = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_DEF);
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createLocalTypeDefinitionStatementNode(annots, typeKeyword, typeName, typeDescriptor,
                semicolon);
    }

    /**
     * Parse statement which is only consists of an action or expression.
     *
     * @param annots Annotations
     * @param nextTokenKind Next token kind
     * @return Statement node
     */
    private STNode parseExpressionStament(SyntaxKind nextTokenKind, STNode annots) {
        startContext(ParserRuleContext.EXPRESSION_STATEMENT);
        STNode expression = parseActionOrExpressionInLhs(nextTokenKind, annots);
        return getExpressionAsStatement(expression);
    }

    /**
     * Parse statements that starts with an expression.
     *
     * @return Statement node
     */
    private STNode parseStatementStartWithExpr(STNode annots) {
        startContext(ParserRuleContext.AMBIGUOUS_STMT);
        STNode expr = parseActionOrExpressionInLhs(peek().kind, annots);
        return parseStatementStartWithExprRhs(expr);
    }

    /**
     * Parse rhs of statements that starts with an expression.
     *
     * @return Statement node
     */
    private STNode parseStatementStartWithExprRhs(STNode expression) {
        STToken nextToken = peek();
        return parseStatementStartWithExprRhs(nextToken.kind, expression);
    }

    /**
     * Parse the component followed by the expression, at the beginning of a statement.
     *
     * @param nextTokenKind Kind of the next token
     * @return Statement node
     */
    private STNode parseStatementStartWithExprRhs(SyntaxKind nextTokenKind, STNode expression) {
        switch (nextTokenKind) {
            case EQUAL_TOKEN:
                switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                return parseAssignmentStmtRhs(expression);
            case SEMICOLON_TOKEN:
                return getExpressionAsStatement(expression);
            case IDENTIFIER_TOKEN:
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundBinaryOperator(nextTokenKind)) {
                    return parseCompoundAssignmentStmtRhs(expression);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STMT_START_WITH_EXPR_RHS, expression);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseStatementStartWithExprRhs(solution.tokenKind, expression);
        }
    }

    private STNode getArrayLength(STNodeList exprs) {
        if (exprs.isEmpty()) {
            return STNodeFactory.createEmptyNode();
        }

        STNode lengthExpr = exprs.get(0);
        switch (lengthExpr.kind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case ASTERISK_TOKEN:
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
                break;
            default:
                this.errorHandler.reportInvalidNode(null, "invalid array length");
                break;
        }
        return lengthExpr;
    }

    private STNode getExpressionAsStatement(STNode expression) {
        switch (expression.kind) {
            case METHOD_CALL:
            case FUNCTION_CALL:
            case CHECK_EXPRESSION:
                return parseCallStatement(expression);
            case REMOTE_METHOD_CALL_ACTION:
            case CHECK_ACTION:
            case BRACED_ACTION:
            case START_ACTION:
            case TRAP_ACTION:
            case FLUSH_ACTION:
            case ASYNC_SEND_ACTION:
            case SYNC_SEND_ACTION:
            case RECEIVE_ACTION:
            case WAIT_ACTION:
            case QUERY_ACTION:
            case COMMIT_ACTION:
                return parseActionStatement(expression);
            default:
                // Everything else can not be written as a statement.
                // TODO: Add proper error reporting
                this.errorHandler.reportInvalidNode(null,
                        "left hand side of an assignment must be a variable reference");

                STNode semicolon = parseSemicolon();
                endContext();
                return STNodeFactory.createExpressionStatementNode(SyntaxKind.INVALID, expression, semicolon);
        }
    }

    /**
     * <p>
     * Parse call statement, given the call expression.
     * <p>
     * <code>
     * call-stmt := call-expr ;
     * <br/>
     * call-expr := function-call-expr | method-call-expr | checking-keyword call-expr
     * </code>
     *
     * @param expression Call expression associated with the call statement
     * @return Call statement node
     */
    private STNode parseCallStatement(STNode expression) {
        validateExprInCallStmt(expression);
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createExpressionStatementNode(SyntaxKind.CALL_STATEMENT, expression, semicolon);
    }

    private void validateExprInCallStmt(STNode expression) {
        switch (expression.kind) {
            case FUNCTION_CALL:
            case METHOD_CALL:
                break;
            case CHECK_EXPRESSION:
                validateExprInCallStmt(((STCheckExpressionNode) expression).expression);
                break;
            case REMOTE_METHOD_CALL_ACTION:
                break;
            case BRACED_EXPRESSION:
                validateExprInCallStmt(((STBracedExpressionNode) expression).expression);
                break;
            default:
                if (isMissingNode(expression)) {
                    break;
                }

                this.errorHandler.reportInvalidNode(null, "expression followed by the checking keyword must be a " +
                        "func-call, a method-call or a check-expr");
                break;
        }
    }

    /**
     * Check whether a node is a missing node.
     *
     * @param node Node to check
     * @return <code>true</code> if the node is a missing node. <code>false</code> otherwise
     */
    private boolean isMissingNode(STNode node) {
        if (node.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return isMissingNode(((STSimpleNameReferenceNode) node).name);
        }
        return node instanceof STMissingToken;
    }

    private STNode parseActionStatement(STNode action) {
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createExpressionStatementNode(SyntaxKind.ACTION_STATEMENT, action, semicolon);
    }

    /**
     * Parse remote method call action, given the starting expression.
     * <p>
     * <code>
     * remote-method-call-action := expression -> method-name ( arg-list )
     * <br/>
     * async-send-action := expression -> peer-worker ;
     * </code>
     *
     * @param isRhsExpr Is this an RHS action
     * @param expression LHS expression
     * @return
     */
    private STNode parseRemoteMethodCallOrAsyncSendAction(STNode expression, boolean isRhsExpr) {
        STNode rightArrow = parseRightArrow();
        return parseRemoteCallOrAsyncSendActionRhs(peek().kind, expression, isRhsExpr, rightArrow);
    }

    private STNode parseRemoteCallOrAsyncSendActionRhs(SyntaxKind nextTokenKind, STNode expression, boolean isRhsExpr,
                                                       STNode rightArrow) {
        STNode name;
        switch (nextTokenKind) {
            case DEFAULT_KEYWORD:
                name = parseDefaultKeyword();
                return parseAsyncSendAction(expression, rightArrow, name);
            case IDENTIFIER_TOKEN:
                name = STNodeFactory.createSimpleNameReferenceNode(parseFunctionName());
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.REMOTE_CALL_OR_ASYNC_SEND_RHS, expression,
                        isRhsExpr, rightArrow);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    name = solution.recoveredNode;
                    break;
                }

                return parseRemoteCallOrAsyncSendActionRhs(solution.tokenKind, expression, isRhsExpr, rightArrow);
        }

        return parseRemoteCallOrAsyncSendEnd(peek().kind, expression, rightArrow, name);
    }

    private STNode parseRemoteCallOrAsyncSendEnd(SyntaxKind nextTokenKind, STNode expression, STNode rightArrow,
                                                 STNode name) {
        switch (nextTokenKind) {
            case OPEN_PAREN_TOKEN:
                return parseRemoteMethodCallAction(expression, rightArrow, name);
            case SEMICOLON_TOKEN:
                return parseAsyncSendAction(expression, rightArrow, name);
            default:
                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.REMOTE_CALL_OR_ASYNC_SEND_END, expression, rightArrow, name);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseRemoteCallOrAsyncSendEnd(solution.tokenKind, expression, rightArrow, name);
        }
    }

    /**
     * Parse default keyword.
     *
     * @return default keyword node
     */
    private STNode parseDefaultKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DEFAULT_KEYWORD) {
            return STNodeFactory.createSimpleNameReferenceNode(consume());
        } else {
            Solution sol = recover(token, ParserRuleContext.DEFAULT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    private STNode parseAsyncSendAction(STNode expression, STNode rightArrow, STNode peerWorker) {
        return STNodeFactory.createAsyncSendActionNode(expression, rightArrow, peerWorker);
    }

    private STNode parseRemoteMethodCallAction(STNode expression, STNode rightArrow, STNode name) {
        STNode openParenToken = parseOpenParenthesis(ParserRuleContext.ARG_LIST_START);
        STNode arguments = parseArgsList();
        STNode closeParenToken = parseCloseParenthesis();
        return STNodeFactory.createRemoteMethodCallActionNode(expression, rightArrow, name, openParenToken, arguments,
                closeParenToken);
    }

    /**
     * Parse right arrow (<code>-></code>) token.
     *
     * @return Parsed node
     */
    private STNode parseRightArrow() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.RIGHT_ARROW_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(nextToken, ParserRuleContext.RIGHT_ARROW);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse parameterized type descriptor.
     * parameterized-type-descriptor := map type-parameter | future type-parameter | typedesc type-parameter
     *
     * @return Parsed node
     */
    private STNode parseParameterizedTypeDescriptor() {
        STNode parameterizedTypeKeyword = parseParameterizedTypeKeyword();
        STNode ltToken = parseLTToken();
        STNode typeNode = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS);
        STNode gtToken = parseGTToken();
        return STNodeFactory.createParameterizedTypeDescriptorNode(parameterizedTypeKeyword, ltToken, typeNode,
                gtToken);
    }

    /**
     * Parse <code>map</code> or <code>future</code> or <code>typedesc</code> keyword token.
     *
     * @return Parsed node
     */
    private STNode parseParameterizedTypeKeyword() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case MAP_KEYWORD: // map type desc
            case FUTURE_KEYWORD: // future type desc
            case TYPEDESC_KEYWORD: // typedesc type desc
                return consume();
            default:
                Solution sol = recover(nextToken, ParserRuleContext.PARAMETERIZED_TYPE);
                return sol.recoveredNode;
        }
    }

    /**
     * Parse <code> < </code> token.
     *
     * @return Parsed node
     */
    private STNode parseGTToken() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.GT_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(nextToken, ParserRuleContext.GT);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse <code> > </code> token.
     *
     * @return Parsed node
     */
    private STNode parseLTToken() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.LT_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(nextToken, ParserRuleContext.LT);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse nil literal. Here nil literal is only referred to ( ).
     *
     * @return Parsed node
     */
    private STNode parseNilLiteral() {
        startContext(ParserRuleContext.NIL_LITERAL);
        STNode openParenthesisToken = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        STNode closeParenthesisToken = parseCloseParenthesis();
        endContext();
        return STNodeFactory.createNilLiteralNode(openParenthesisToken, closeParenthesisToken);
    }

    /**
     * Parse annotation declaration, given the qualifier.
     *
     * @param metadata Metadata
     * @param qualifier Qualifier that precedes the listener declaration
     * @param constKeyword Const keyword
     * @return Parsed node
     */
    private STNode parseAnnotationDeclaration(STNode metadata, STNode qualifier, STNode constKeyword) {
        startContext(ParserRuleContext.ANNOTATION_DECL);
        STNode annotationKeyword = parseAnnotationKeyword();
        STNode annotDecl = parseAnnotationDeclFromType(metadata, qualifier, constKeyword, annotationKeyword);
        endContext();
        return annotDecl;
    }

    /**
     * Parse annotation keyword.
     *
     * @return Parsed node
     */
    private STNode parseAnnotationKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ANNOTATION_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ANNOTATION_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse the components that follows after the annotation keyword of a annotation declaration.
     *
     * @param metadata Metadata
     * @param qualifier Qualifier that precedes the constant decl
     * @param constKeyword Const keyword
     * @param annotationKeyword
     * @return Parsed node
     */
    private STNode parseAnnotationDeclFromType(STNode metadata, STNode qualifier, STNode constKeyword,
                                               STNode annotationKeyword) {
        STToken nextToken = peek();
        return parseAnnotationDeclFromType(nextToken.kind, metadata, qualifier, constKeyword, annotationKeyword);
    }

    private STNode parseAnnotationDeclFromType(SyntaxKind nextTokenKind, STNode metadata, STNode qualifier,
                                               STNode constKeyword, STNode annotationKeyword) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                return parseAnnotationDeclWithOptionalType(metadata, qualifier, constKeyword, annotationKeyword);
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.ANNOT_DECL_OPTIONAL_TYPE, metadata, qualifier,
                        constKeyword, annotationKeyword);
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseAnnotationDeclFromType(solution.tokenKind, metadata, qualifier, constKeyword,
                        annotationKeyword);
        }

        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_ANNOTATION_DECL);
        STNode annotTag = parseAnnotationTag();
        return parseAnnotationDeclAttachPoints(metadata, qualifier, constKeyword, annotationKeyword, typeDesc,
                annotTag);
    }

    /**
     * Parse annotation tag.
     * <p>
     * <code>annot-tag := identifier</code>
     *
     * @return
     */
    private STNode parseAnnotationTag() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.ANNOTATION_TAG);
            return sol.recoveredNode;
        }
    }

    private STNode parseAnnotationDeclWithOptionalType(STNode metadata, STNode qualifier, STNode constKeyword,
                                                       STNode annotationKeyword) {
        // We come here if the type name also and identifier.
        // However, if it is a qualified identifier, then it has to be the type-desc.
        STNode typeDescOrAnnotTag = parseAnnotationTag();
        if (typeDescOrAnnotTag.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            STNode annotTag = parseAnnotationTag();
            return parseAnnotationDeclAttachPoints(metadata, qualifier, constKeyword, annotationKeyword,
                    typeDescOrAnnotTag, annotTag);
        }

        return parseAnnotationDeclRhs(metadata, qualifier, constKeyword, annotationKeyword, typeDescOrAnnotTag);
    }

    /**
     * Parse the component that follows the first identifier in an annotation decl. The identifier
     * can be either the type-name (a user defined type) or the annot-tag, where the type-name
     * is not present.
     *
     * @param metadata Metadata
     * @param qualifier Qualifier that precedes the annotation decl
     * @param constKeyword Const keyword
     * @param annotationKeyword Annotation keyword
     * @param typeDescOrAnnotTag Identifier that follows the annotation-keyword
     * @return Parsed node
     */
    private STNode parseAnnotationDeclRhs(STNode metadata, STNode qualifier, STNode constKeyword,
                                          STNode annotationKeyword, STNode typeDescOrAnnotTag) {
        STToken token = peek();
        return parseAnnotationDeclRhs(token.kind, metadata, qualifier, constKeyword, annotationKeyword,
                typeDescOrAnnotTag);
    }

    private STNode parseAnnotationDeclRhs(SyntaxKind nextTokenKind, STNode metadata, STNode qualifier,
                                          STNode constKeyword, STNode annotationKeyword, STNode typeDescOrAnnotTag) {
        STNode typeDesc;
        STNode annotTag;
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                typeDesc = typeDescOrAnnotTag;
                annotTag = parseAnnotationTag();
                break;
            case SEMICOLON_TOKEN:
            case ON_KEYWORD:
                typeDesc = STNodeFactory.createEmptyNode();
                annotTag = typeDescOrAnnotTag;
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.ANNOT_DECL_RHS, metadata, qualifier, constKeyword,
                        annotationKeyword, typeDescOrAnnotTag);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseAnnotationDeclRhs(solution.tokenKind, metadata, qualifier, constKeyword, annotationKeyword,
                        typeDescOrAnnotTag);
        }

        return parseAnnotationDeclAttachPoints(metadata, qualifier, constKeyword, annotationKeyword, typeDesc,
                annotTag);
    }

    private STNode parseAnnotationDeclAttachPoints(STNode metadata, STNode qualifier, STNode constKeyword,
                                                   STNode annotationKeyword, STNode typeDesc, STNode annotTag) {
        STToken nextToken = peek();
        return parseAnnotationDeclAttachPoints(nextToken.kind, metadata, qualifier, constKeyword, annotationKeyword,
                typeDesc, annotTag);

    }

    private STNode parseAnnotationDeclAttachPoints(SyntaxKind nextTokenKind, STNode metadata, STNode qualifier,
                                                   STNode constKeyword, STNode annotationKeyword, STNode typeDesc,
                                                   STNode annotTag) {
        STNode onKeyword;
        STNode attachPoints;
        switch (nextTokenKind) {
            case SEMICOLON_TOKEN:
                onKeyword = STNodeFactory.createEmptyNode();
                attachPoints = STNodeFactory.createEmptyNodeList();
                break;
            case ON_KEYWORD:
                onKeyword = parseOnKeyword();
                attachPoints = parseAnnotationAttachPoints();
                onKeyword = cloneWithDiagnosticIfListEmpty(attachPoints,
                        onKeyword, DiagnosticErrorCode.ERROR_MISSING_ANNOTATION_ATTACH_POINT);
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS, metadata, qualifier,
                        constKeyword, annotationKeyword, typeDesc, annotTag);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseAnnotationDeclAttachPoints(solution.tokenKind, metadata, qualifier, constKeyword,
                        annotationKeyword, typeDesc, annotTag);
        }

        STNode semicolonToken = parseSemicolon();
        return STNodeFactory.createAnnotationDeclarationNode(metadata, qualifier, constKeyword, annotationKeyword,
                typeDesc, annotTag, onKeyword, attachPoints, semicolonToken);
    }

    /**
     * Parse annotation attach points.
     * <p>
     * <code>
     * annot-attach-points := annot-attach-point (, annot-attach-point)*
     * <br/><br/>
     * annot-attach-point := dual-attach-point | source-only-attach-point
     * <br/><br/>
     * dual-attach-point := [source] dual-attach-point-ident
     * <br/><br/>
     * dual-attach-point-ident :=
     *     [object] type
     *     | [object|resource] function
     *     | parameter
     *     | return
     *     | service
     *     | [object|record] field
     * <br/><br/>
     * source-only-attach-point := source source-only-attach-point-ident
     * <br/><br/>
     * source-only-attach-point-ident :=
     *     annotation
     *     | external
     *     | var
     *     | const
     *     | listener
     *     | worker
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseAnnotationAttachPoints() {
        startContext(ParserRuleContext.ANNOT_ATTACH_POINTS_LIST);
        List<STNode> attachPoints = new ArrayList<>();

        STToken nextToken = peek();
        if (isEndAnnotAttachPointList(nextToken.kind)) {
            endContext();
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first attach-point, that has no leading comma
        STNode attachPoint = parseAnnotationAttachPoint();
        attachPoints.add(attachPoint);

        // Parse the remaining attach-points
        nextToken = peek();
        STNode leadingComma;
        while (!isEndAnnotAttachPointList(nextToken.kind)) {
            leadingComma = parseAttachPointEnd();
            if (leadingComma == null) {
                break;
            }
            attachPoints.add(leadingComma);

            // Parse attach point. Null represents the end of attach-points.
            attachPoint = parseAnnotationAttachPoint();
            if (attachPoint == null) {
                attachPoint = errorHandler.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_ANNOTATION_ATTACH_POINT);
                attachPoints.add(attachPoint);
                break;
            }

            attachPoints.add(attachPoint);
            nextToken = peek();
        }

        endContext();
        return STNodeFactory.createNodeList(attachPoints);
    }

    /**
     * Parse annotation attach point end.
     *
     * @return Parsed node
     */
    private STNode parseAttachPointEnd() {
        STToken nextToken = peek();
        return parseAttachPointEnd(nextToken.kind);
    }

    private STNode parseAttachPointEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case SEMICOLON_TOKEN:
                // null represents the end of attach points.
                return null;
            case COMMA_TOKEN:
                return consume();
            default:
                Solution sol = recover(peek(), ParserRuleContext.ATTACH_POINT_END);
                if (sol.action == Action.REMOVE) {
                    return sol.recoveredNode;
                }

                return sol.tokenKind == SyntaxKind.COMMA_TOKEN ? sol.recoveredNode : null;
        }
    }

    private boolean isEndAnnotAttachPointList(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case SEMICOLON_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse annotation attach point.
     *
     * @return Parsed node
     */
    private STNode parseAnnotationAttachPoint() {
        return parseAnnotationAttachPoint(peek().kind);
    }

    private STNode parseAnnotationAttachPoint(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
                return null;

            // These are source only annotations, but without the source keyword.
            case ANNOTATION_KEYWORD:
            case EXTERNAL_KEYWORD:
            case VAR_KEYWORD:
            case CONST_KEYWORD:
            case LISTENER_KEYWORD:
            case WORKER_KEYWORD:
                // fall through

            case SOURCE_KEYWORD:
                STNode sourceKeyword = parseSourceKeyword();
                return parseAttachPointIdent(sourceKeyword);

            // Dual attach points
            case OBJECT_KEYWORD:
            case TYPE_KEYWORD:
            case RESOURCE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
            case RECORD_KEYWORD:
                sourceKeyword = STNodeFactory.createEmptyNode();
                STNode firstIdent = consume();
                return parseDualAttachPointIdent(sourceKeyword, firstIdent);
            default:
                Solution solution = recover(peek(), ParserRuleContext.ATTACH_POINT);
                return solution.recoveredNode;
        }
    }

    /**
     * Parse source keyword.
     *
     * @return Parsed node
     */
    private STNode parseSourceKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SOURCE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.SOURCE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse attach point ident gievn.
     * <p>
     * <code>
     * source-only-attach-point-ident := annotation | external | var | const | listener | worker
     * <br/><br/>
     * dual-attach-point-ident := [object] type | [object|resource] function | parameter
     *                            | return | service | [object|record] field
     * </code>
     *
     * @param sourceKeyword Source keyword
     * @return Parsed node
     */
    private STNode parseAttachPointIdent(STNode sourceKeyword) {
        return parseAttachPointIdent(peek().kind, sourceKeyword);
    }

    private STNode parseAttachPointIdent(SyntaxKind nextTokenKind, STNode sourceKeyword) {
        switch (nextTokenKind) {
            case ANNOTATION_KEYWORD:
            case EXTERNAL_KEYWORD:
            case VAR_KEYWORD:
            case CONST_KEYWORD:
            case LISTENER_KEYWORD:
            case WORKER_KEYWORD:
                STNode firstIdent = consume();
                STNode secondIdent = STNodeFactory.createEmptyNode();
                return STNodeFactory.createAnnotationAttachPointNode(sourceKeyword, firstIdent, secondIdent);
            case OBJECT_KEYWORD:
            case RESOURCE_KEYWORD:
            case RECORD_KEYWORD:
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
                firstIdent = consume();
                return parseDualAttachPointIdent(sourceKeyword, firstIdent);
            default:
                Solution solution = recover(peek(), ParserRuleContext.ATTACH_POINT_IDENT, sourceKeyword);
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                firstIdent = solution.recoveredNode;
                return parseDualAttachPointIdent(sourceKeyword, firstIdent);
        }
    }

    /**
     * Parse dual-attach-point ident.
     *
     * @param sourceKeyword Source keyword
     * @param firstIdent first part of the dual attach-point
     * @return Parsed node
     */
    private STNode parseDualAttachPointIdent(STNode sourceKeyword, STNode firstIdent) {
        STNode secondIdent;
        switch (firstIdent.kind) {
            case OBJECT_KEYWORD:
                secondIdent = parseIdentAfterObjectIdent();
                break;
            case RESOURCE_KEYWORD:
                secondIdent = parseFunctionIdent();
                break;
            case RECORD_KEYWORD:
                secondIdent = parseFieldIdent();
                break;
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
            default: // default case should never be reached.
                secondIdent = STNodeFactory.createEmptyNode();
                break;
        }

        return STNodeFactory.createAnnotationAttachPointNode(sourceKeyword, firstIdent, secondIdent);
    }

    /**
     * Parse the idents that are supported after object-ident.
     *
     * @return Parsed node
     */
    private STNode parseIdentAfterObjectIdent() {
        STToken token = peek();
        switch (token.kind) {
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case FIELD_KEYWORD:
                return consume();
            default:
                Solution sol = recover(token, ParserRuleContext.IDENT_AFTER_OBJECT_IDENT);
                return sol.recoveredNode;
        }
    }

    /**
     * Parse function ident.
     *
     * @return Parsed node
     */
    private STNode parseFunctionIdent() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FUNCTION_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FUNCTION_IDENT);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse field ident.
     *
     * @return Parsed node
     */
    private STNode parseFieldIdent() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FIELD_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FIELD_IDENT);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse XML namespace declaration.
     * <p>
     * <code>xmlns-decl := xmlns xml-namespace-uri [ as xml-namespace-prefix ] ;
     * <br/>
     * xml-namespace-uri := simple-const-expr
     * <br/>
     * xml-namespace-prefix := identifier
     * </code>
     *
     * @return
     */
    private STNode parseXMLNamespaceDeclaration(boolean isModuleVar) {
        startContext(ParserRuleContext.XML_NAMESPACE_DECLARATION);
        STNode xmlnsKeyword = parseXMLNSKeyword();
        STNode namespaceUri = parseXMLNamespaceUri();
        STNode xmlnsDecl = parseXMLDeclRhs(xmlnsKeyword, namespaceUri, isModuleVar);
        endContext();
        return xmlnsDecl;
    }

    /**
     * Parse xmlns keyword.
     *
     * @return Parsed node
     */
    private STNode parseXMLNSKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.XMLNS_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.XMLNS_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse namespace uri.
     *
     * @return Parsed node
     */
    private STNode parseXMLNamespaceUri() {
        STNode expr = parseConstExpr();
        switch (expr.kind) {
            case STRING_LITERAL:
            case IDENTIFIER_TOKEN:
            case QUALIFIED_NAME_REFERENCE:
                break;
            default:
                this.errorHandler.reportInvalidNode(null, "namespace uri must be a subtype of string");
        }

        return expr;
    }

    private STNode parseConstExpr() {
        startContext(ParserRuleContext.CONSTANT_EXPRESSION);
        STNode expr = parseConstExprInternal();
        endContext();
        return expr;
    }

    private STNode parseConstExprInternal() {
        STToken nextToken = peek();
        return parseConstExprInternal(nextToken.kind);
    }

    /**
     * Parse constants expr.
     *
     * @return Parsed node
     */
    private STNode parseConstExprInternal(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case STRING_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
                return parseBasicLiteral();
            case IDENTIFIER_TOKEN:
                return parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return parseSignedIntOrFloat();
            case OPEN_BRACE_TOKEN:
                return parseNilLiteral();
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.CONSTANT_EXPRESSION_START);
                return solution.recoveredNode;
        }
    }

    /**
     * Parse the portion after the namsepsace-uri of an XML declaration.
     *
     * @param xmlnsKeyword XMLNS keyword
     * @param namespaceUri Namespace URI
     * @return Parsed node
     */
    private STNode parseXMLDeclRhs(STNode xmlnsKeyword, STNode namespaceUri, boolean isModuleVar) {
        return parseXMLDeclRhs(peek().kind, xmlnsKeyword, namespaceUri, isModuleVar);
    }

    private STNode parseXMLDeclRhs(SyntaxKind nextTokenKind, STNode xmlnsKeyword, STNode namespaceUri,
                                   boolean isModuleVar) {
        STNode asKeyword = STNodeFactory.createEmptyNode();
        STNode namespacePrefix = STNodeFactory.createEmptyNode();

        switch (nextTokenKind) {
            case AS_KEYWORD:
                asKeyword = parseAsKeyword();
                namespacePrefix = parseNamespacePrefix();
                break;
            case SEMICOLON_TOKEN:
                break;
            default:
                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.XML_NAMESPACE_PREFIX_DECL, xmlnsKeyword, namespaceUri,
                                isModuleVar);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseXMLDeclRhs(solution.tokenKind, xmlnsKeyword, namespaceUri, isModuleVar);
        }
        STNode semicolon = parseSemicolon();
        if (isModuleVar) {
            return STNodeFactory.createModuleXMLNamespaceDeclarationNode(xmlnsKeyword, namespaceUri, asKeyword,
                    namespacePrefix, semicolon);
        }
        return STNodeFactory.createXMLNamespaceDeclarationNode(xmlnsKeyword, namespaceUri, asKeyword, namespacePrefix,
                semicolon);
    }

    /**
     * Parse import prefix.
     *
     * @return Parsed node
     */
    private STNode parseNamespacePrefix() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.NAMESPACE_PREFIX);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse named worker declaration.
     * <p>
     * <code>named-worker-decl := [annots] worker worker-name return-type-descriptor { sequence-stmt }</code>
     *
     * @param annots Annotations attached to the worker decl
     * @return Parsed node
     */
    private STNode parseNamedWorkerDeclaration(STNode annots) {
        startContext(ParserRuleContext.NAMED_WORKER_DECL);
        STNode workerKeyword = parseWorkerKeyword();
        STNode workerName = parseWorkerName();
        STNode returnTypeDesc = parseReturnTypeDescriptor();
        STNode workerBody = parseBlockNode();
        endContext();
        return STNodeFactory.createNamedWorkerDeclarationNode(annots, workerKeyword, workerName, returnTypeDesc,
                workerBody);
    }

    private STNode parseReturnTypeDescriptor() {
        // If the return type is not present, simply return
        STToken token = peek();
        if (token.kind != SyntaxKind.RETURNS_KEYWORD) {
            return STNodeFactory.createEmptyNode();
        }

        STNode returnsKeyword = consume();
        STNode annot = parseAnnotations();
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC);
        return STNodeFactory.createReturnTypeDescriptorNode(returnsKeyword, annot, type);
    }

    /**
     * Parse worker keyword.
     *
     * @return Parsed node
     */
    private STNode parseWorkerKeyword() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.WORKER_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.WORKER_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse worker name.
     * <p>
     * <code>worker-name := identifier</code>
     *
     * @return Parsed node
     */
    private STNode parseWorkerName() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(peek(), ParserRuleContext.WORKER_NAME);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse documentation string.
     * <p>
     * <code>DocumentationString := DocumentationLine +</code>
     * <p>
     * Refer {@link BallerinaLexer#processDocumentationLine}
     *
     * @return Parsed node
     */
    private STNode parseDocumentationString() {
        List<STNode> docLines = new ArrayList<>();
        STToken nextToken = peek();
        while (nextToken.kind == SyntaxKind.DOCUMENTATION_LINE) {
            docLines.add(consume());
            nextToken = peek();
        }

        STNode documentationLines = STNodeFactory.createNodeList(docLines);
        return STNodeFactory.createDocumentationStringNode(documentationLines);
    }

    /**
     * Parse lock statement.
     * <code>lock-stmt := lock block-stmt ;</code>
     *
     * @return Lock statement
     */
    private STNode parseLockStatement() {
        startContext(ParserRuleContext.LOCK_STMT);
        STNode lockKeyword = parseLockKeyword();
        STNode blockStatement = parseBlockNode();
        endContext();
        return STNodeFactory.createLockStatementNode(lockKeyword, blockStatement);
    }

    /**
     * Parse lock-keyword.
     *
     * @return lock-keyword node
     */
    private STNode parseLockKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.LOCK_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.LOCK_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse union type descriptor.
     * union-type-descriptor := type-descriptor | type-descriptor
     *
     * @param leftTypeDesc Type desc in the LHS os the union type desc.
     * @param context Current context.
     * @return parsed union type desc node
     */
    private STNode parseUnionTypeDescriptor(STNode leftTypeDesc, ParserRuleContext context) {
        STNode pipeToken = parsePipeToken();
        STNode rightTypeDesc = parseTypeDescriptor(context);

        return STNodeFactory.createUnionTypeDescriptorNode(leftTypeDesc, pipeToken, rightTypeDesc);
    }

    /**
     * Parse pipe token.
     *
     * @return parsed pipe token node
     */
    private STNode parsePipeToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.PIPE_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.PIPE);
            return sol.recoveredNode;
        }
    }

    private boolean isTypeStartingToken(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case IDENTIFIER_TOKEN:
            case SERVICE_KEYWORD:
            case RECORD_KEYWORD:
            case OBJECT_KEYWORD:
            case ABSTRACT_KEYWORD:
            case CLIENT_KEYWORD:
            case OPEN_PAREN_TOKEN: // nil type descriptor '()'
            case MAP_KEYWORD: // map type desc
            case FUTURE_KEYWORD: // future type desc
            case TYPEDESC_KEYWORD: // typedesc type desc
            case ERROR_KEYWORD: // error type desc
            case STREAM_KEYWORD: // stream type desc
            case TABLE_KEYWORD: // table type
            case FUNCTION_KEYWORD:
            case OPEN_BRACKET_TOKEN:
                return true;
            default:
                if (isSingletonTypeDescStart(nodeKind, true)) {
                    return true;
                }
                return isSimpleType(nodeKind);
        }
    }

    static boolean isSimpleType(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case INT_KEYWORD:
            case FLOAT_KEYWORD:
            case DECIMAL_KEYWORD:
            case BOOLEAN_KEYWORD:
            case STRING_KEYWORD:
            case BYTE_KEYWORD:
            case XML_KEYWORD:
            case JSON_KEYWORD:
            case HANDLE_KEYWORD:
            case ANY_KEYWORD:
            case ANYDATA_KEYWORD:
            case NEVER_KEYWORD:
            case SERVICE_KEYWORD:
            case VAR_KEYWORD:
            case ERROR_KEYWORD: // This is for the recovery. <code>error a;</code> scenario recovered here.
            case STREAM_KEYWORD: // This is for recovery logic. <code>stream a;</code> scenario recovered here.
            case READONLY_KEYWORD:
            case DISTINCT_KEYWORD:
                return true;
            case TYPE_DESC:
                // This is a special case. TYPE_DESC is only return from
                // error recovery. when a type is missing. Hence we treat it as
                // a simple type
                return true;
            default:
                return false;
        }
    }

    private SyntaxKind getTypeSyntaxKind(SyntaxKind typeKeyword) {
        switch (typeKeyword) {
            case INT_KEYWORD:
                return SyntaxKind.INT_TYPE_DESC;
            case FLOAT_KEYWORD:
                return SyntaxKind.FLOAT_TYPE_DESC;
            case DECIMAL_KEYWORD:
                return SyntaxKind.DECIMAL_TYPE_DESC;
            case BOOLEAN_KEYWORD:
                return SyntaxKind.BOOLEAN_TYPE_DESC;
            case STRING_KEYWORD:
                return SyntaxKind.STRING_TYPE_DESC;
            case BYTE_KEYWORD:
                return SyntaxKind.BYTE_TYPE_DESC;
            case XML_KEYWORD:
                return SyntaxKind.XML_TYPE_DESC;
            case JSON_KEYWORD:
                return SyntaxKind.JSON_TYPE_DESC;
            case HANDLE_KEYWORD:
                return SyntaxKind.HANDLE_TYPE_DESC;
            case ANY_KEYWORD:
                return SyntaxKind.ANY_TYPE_DESC;
            case ANYDATA_KEYWORD:
                return SyntaxKind.ANYDATA_TYPE_DESC;
            case NEVER_KEYWORD:
                return SyntaxKind.NEVER_TYPE_DESC;
            case SERVICE_KEYWORD:
                return SyntaxKind.SERVICE_TYPE_DESC;
            case VAR_KEYWORD:
                return SyntaxKind.VAR_TYPE_DESC;
            default:
                return SyntaxKind.TYPE_DESC;
        }
    }

    /**
     * Parse fork-keyword.
     *
     * @return Fork-keyword node
     */
    private STNode parseForkKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FORK_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FORK_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse multiple named worker declarations.
     *
     * @return named-worker-declarations node array
     */
    private STNode parseMultipleNamedWorkerDeclarations() {
        ArrayList<STNode> workers = new ArrayList<>();
        while (!isEndOfStatements()) {
            STNode stmt = parseStatement();
            if (stmt == null) {
                break;
            }

            switch (stmt.kind) {
                case NAMED_WORKER_DECLARATION:
                    workers.add(stmt);
                    break;
                default:
                    this.errorHandler.reportInvalidNode(null, "Only named-workers are allowed here");
                    break;
            }
        }

        if (workers.isEmpty()) {
            this.errorHandler.reportInvalidNode(null, "Fork Statement must contain atleast one named-worker");
        }
        return STNodeFactory.createNodeList(workers);
    }

    /**
     * Parse fork statement.
     * <code>fork-stmt := fork { named-worker-decl+ }</code>
     *
     * @return Fork statement
     */
    private STNode parseForkStatement() {
        startContext(ParserRuleContext.FORK_STMT);
        STNode forkKeyword = parseForkKeyword();
        STNode openBrace = parseOpenBrace();
        STNode namedWorkerDeclarations = parseMultipleNamedWorkerDeclarations();
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createForkStatementNode(forkKeyword, openBrace, namedWorkerDeclarations, closeBrace);
    }

    /**
     * Parse decimal floating point literal.
     *
     * @return Parsed node
     */
    private STNode parseDecimalFloatingPointLiteral() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.DECIMAL_FLOATING_POINT_LITERAL);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse hex floating point literal.
     *
     * @return Parsed node
     */
    private STNode parseHexFloatingPointLiteral() {
        STToken token = peek();
        if (token.kind == SyntaxKind.HEX_FLOATING_POINT_LITERAL) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.HEX_FLOATING_POINT_LITERAL);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse trap expression.
     * <p>
     * <code>
     * trap-expr := trap expression
     * </code>
     *
     * @param allowActions Allow actions
     * @param isRhsExpr Whether this is a RHS expression or not
     * @return Trap expression node
     */
    private STNode parseTrapExpression(boolean isRhsExpr, boolean allowActions) {
        STNode trapKeyword = parseTrapKeyword();
        STNode expr = parseExpression(OperatorPrecedence.EXPRESSION_ACTION, isRhsExpr, allowActions);
        if (isAction(expr)) {
            return STNodeFactory.createTrapExpressionNode(SyntaxKind.TRAP_ACTION, trapKeyword, expr);
        }

        return STNodeFactory.createTrapExpressionNode(SyntaxKind.TRAP_EXPRESSION, trapKeyword, expr);
    }

    /**
     * Parse trap-keyword.
     *
     * @return Trap-keyword node
     */
    private STNode parseTrapKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TRAP_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TRAP_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse list constructor expression.
     * <p>
     * <code>
     * list-constructor-expr := [ [ expr-list ] ]
     * <br/>
     * expr-list := expression (, expression)*
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseListConstructorExpr() {
        startContext(ParserRuleContext.LIST_CONSTRUCTOR);
        STNode openBracket = parseOpenBracket();
        STNode expressions = parseOptionalExpressionsList();
        STNode closeBracket = parseCloseBracket();
        endContext();
        return STNodeFactory.createListConstructorExpressionNode(openBracket, expressions, closeBracket);
    }

    /**
     * Parse optional expression list.
     *
     * @return Parsed node
     */
    private STNode parseOptionalExpressionsList() {
        List<STNode> expressions = new ArrayList<>();
        if (isEndOfListConstructor(peek().kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        STNode expr = parseExpression();
        expressions.add(expr);
        return parseOptionalExpressionsList(expressions);
    }

    private STNode parseOptionalExpressionsList(List<STNode> expressions) {
        // Parse the remaining expressions
        STNode listConstructorMemberEnd;
        while (!isEndOfListConstructor(peek().kind)) {

            listConstructorMemberEnd = parseListConstructorMemberEnd();
            if (listConstructorMemberEnd == null) {
                break;
            }
            expressions.add(listConstructorMemberEnd);

            STNode expr = parseExpression();
            expressions.add(expr);
        }

        return STNodeFactory.createNodeList(expressions);
    }

    private boolean isEndOfListConstructor(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseListConstructorMemberEnd() {
        return parseListConstructorMemberEnd(peek().kind);
    }

    private STNode parseListConstructorMemberEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.LIST_CONSTRUCTOR_MEMBER_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }
                return parseListConstructorMemberEnd(solution.tokenKind);
        }
    }

    /**
     * Parse foreach statement.
     * <code>foreach-stmt := foreach typed-binding-pattern in action-or-expr block-stmt</code>
     *
     * @return foreach statement
     */
    private STNode parseForEachStatement() {
        startContext(ParserRuleContext.FOREACH_STMT);
        STNode forEachKeyword = parseForEachKeyword();
        STNode typedBindingPattern = parseTypedBindingPattern(ParserRuleContext.FOREACH_STMT);
        STNode inKeyword = parseInKeyword();
        STNode actionOrExpr = parseActionOrExpression();
        STNode blockStatement = parseBlockNode();
        endContext();
        return STNodeFactory.createForEachStatementNode(forEachKeyword, typedBindingPattern, inKeyword, actionOrExpr,
                blockStatement);
    }

    /**
     * Parse foreach-keyword.
     *
     * @return ForEach-keyword node
     */
    private STNode parseForEachKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FOREACH_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FOREACH_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse in-keyword.
     *
     * @return In-keyword node
     */
    private STNode parseInKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IN_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.IN_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse type cast expression.
     * <p>
     * <code>
     * type-cast-expr := < type-cast-param > expression
     * <br/>
     * type-cast-param := [annots] type-descriptor | annots
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseTypeCastExpr(boolean isRhsExpr) {
        startContext(ParserRuleContext.TYPE_CAST);
        STNode ltToken = parseLTToken();
        STNode typeCastParam = parseTypeCastParam();
        STNode gtToken = parseGTToken();
        endContext();

        // allow-actions flag is always false, since there will not be any actions
        // within the type-cast-expr, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, false);
        return STNodeFactory.createTypeCastExpressionNode(ltToken, typeCastParam, gtToken, expression);
    }

    private STNode parseTypeCastParam() {
        STNode annot;
        STNode type;
        STToken token = peek();

        switch (token.kind) {
            case AT_TOKEN:
                annot = parseAnnotations();
                token = peek();
                if (isTypeStartingToken(token.kind)) {
                    type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS);
                } else {
                    type = STNodeFactory.createEmptyNode();
                }
                break;
            default:
                annot = STNodeFactory.createEmptyNode();
                type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS);
                break;
        }

        return STNodeFactory.createTypeCastParamNode(getAnnotations(annot), type);
    }

    /**
     * Parse table constructor expression.
     * <p>
     * <code>
     * table-constructor-expr-rhs := [ [row-list] ]
     * </code>
     *
     * @param tableKeyword tableKeyword that precedes this rhs
     * @param keySpecifier keySpecifier that precedes this rhs
     * @return Parsed node
     */
    private STNode parseTableConstructorExprRhs(STNode tableKeyword, STNode keySpecifier) {
        switchContext(ParserRuleContext.TABLE_CONSTRUCTOR);
        STNode openBracket = parseOpenBracket();
        STNode rowList = parseRowList();
        STNode closeBracket = parseCloseBracket();
        return STNodeFactory.createTableConstructorExpressionNode(tableKeyword, keySpecifier, openBracket, rowList,
                closeBracket);
    }

    /**
     * Parse table-keyword.
     *
     * @return Table-keyword node
     */
    private STNode parseTableKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TABLE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TABLE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse table rows.
     * <p>
     * <code>row-list := [ mapping-constructor-expr (, mapping-constructor-expr)* ]</code>
     *
     * @return Parsed node
     */
    private STNode parseRowList() {
        STToken nextToken = peek();
        // Return an empty list if list is empty
        if (isEndOfTableRowList(nextToken.kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first mapping constructor, that has no leading comma
        List<STNode> mappings = new ArrayList<>();
        STNode mapExpr = parseMappingConstructorExpr();
        mappings.add(mapExpr);

        // Parse the remaining mapping constructors
        nextToken = peek();
        STNode leadingComma;
        while (!isEndOfTableRowList(nextToken.kind)) {
            leadingComma = parseComma();
            mappings.add(leadingComma);
            mapExpr = parseMappingConstructorExpr();
            mappings.add(mapExpr);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(mappings);
    }

    private boolean isEndOfTableRowList(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return true;
            case COMMA_TOKEN:
            case OPEN_BRACE_TOKEN:
                return false;
            default:
                return isEndOfMappingConstructor(tokenKind);
        }
    }

    /**
     * Parse key specifier.
     * <p>
     * <code>key-specifier := key ( [ field-name (, field-name)* ] )</code>
     *
     * @return Parsed node
     */
    private STNode parseKeySpecifier() {
        startContext(ParserRuleContext.KEY_SPECIFIER);
        STNode keyKeyword = parseKeyKeyword();
        STNode openParen = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        STNode fieldNames = parseFieldNames();
        STNode closeParen = parseCloseParenthesis();
        endContext();
        return STNodeFactory.createKeySpecifierNode(keyKeyword, openParen, fieldNames, closeParen);
    }

    /**
     * Parse key-keyword.
     *
     * @return Key-keyword node
     */
    private STNode parseKeyKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.KEY_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.KEY_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse field names.
     * <p>
     * <code>field-name-list := [ field-name (, field-name)* ]</code>
     *
     * @return Parsed node
     */
    private STNode parseFieldNames() {
        STToken nextToken = peek();
        // Return an empty list if list is empty
        if (isEndOfFieldNamesList(nextToken.kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first field name, that has no leading comma
        List<STNode> fieldNames = new ArrayList<>();
        STNode fieldName = parseVariableName();
        fieldNames.add(fieldName);

        // Parse the remaining field names
        nextToken = peek();
        STNode leadingComma;
        while (!isEndOfFieldNamesList(nextToken.kind)) {
            leadingComma = parseComma();
            fieldNames.add(leadingComma);
            fieldName = parseVariableName();
            fieldNames.add(fieldName);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(fieldNames);
    }

    private boolean isEndOfFieldNamesList(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case COMMA_TOKEN:
            case IDENTIFIER_TOKEN:
                return false;
            default:
                return true;
        }
    }

    /**
     * Parse error type descriptor.
     * <p>
     * error-type-descriptor := error [error-type-param]
     * error-type-param := < (detail-type-descriptor | inferred-type-descriptor) >
     * detail-type-descriptor := type-descriptor
     * inferred-type-descriptor := *
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseErrorTypeDescriptor() {
        STNode errorKeywordToken = parseErrorKeyWord();
        STNode errorTypeParamsNode;
        STToken nextToken = peek();
        STToken nextNextToken = peek(2);
        if (nextToken.kind == SyntaxKind.LT_TOKEN || nextNextToken.kind == SyntaxKind.GT_TOKEN) {
            errorTypeParamsNode = parseErrorTypeParamsNode();
        } else {
            errorTypeParamsNode = STNodeFactory.createEmptyNode();
        }
        return STNodeFactory.createErrorTypeDescriptorNode(errorKeywordToken, errorTypeParamsNode);
    }

    /**
     * Parse error type param node.
     * <p>
     * error-type-param := < (detail-type-descriptor | inferred-type-descriptor) >
     * detail-type-descriptor := type-descriptor
     * inferred-type-descriptor := *
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseErrorTypeParamsNode() {
        STNode ltToken = parseLTToken();
        STNode parameter;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.ASTERISK_TOKEN) {
            parameter = consume();
        } else {
            parameter = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS);
        }
        STNode gtToken = parseGTToken();
        return STNodeFactory.createErrorTypeParamsNode(ltToken, parameter, gtToken);
    }

    /**
     * Parse error-keyword.
     *
     * @return Parsed error-keyword node
     */
    private STNode parseErrorKeyWord() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ERROR_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ERROR_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse stream type descriptor.
     * <p>
     * stream-type-descriptor := stream [stream-type-parameters]
     * stream-type-parameters := < type-descriptor [, type-descriptor]>
     * </p>
     *
     * @return Parsed stream type descriptor node
     */
    private STNode parseStreamTypeDescriptor() {
        STNode streamKeywordToken = parseStreamKeyword();
        STNode streamTypeParamsNode;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.LT_TOKEN) {
            streamTypeParamsNode = parseStreamTypeParamsNode();
        } else {
            streamTypeParamsNode = STNodeFactory.createEmptyNode();
        }
        return STNodeFactory.createStreamTypeDescriptorNode(streamKeywordToken, streamTypeParamsNode);
    }

    /**
     * Parse stream type params node.
     * <p>
     * stream-type-parameters := < type-descriptor [, type-descriptor]>
     * </p>
     *
     * @return Parsed stream type params node
     */
    private STNode parseStreamTypeParamsNode() {
        STNode ltToken = parseLTToken();
        startContext(ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC);
        STNode leftTypeDescNode = parseTypeDescriptorInternal(ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC);
        STNode streamTypedesc = parseStreamTypeParamsNode(ltToken, leftTypeDescNode);
        endContext();
        return streamTypedesc;
    }

    private STNode parseStreamTypeParamsNode(STNode ltToken, STNode leftTypeDescNode) {
        return parseStreamTypeParamsNode(peek().kind, ltToken, leftTypeDescNode);
    }

    private STNode parseStreamTypeParamsNode(SyntaxKind nextTokenKind, STNode ltToken, STNode leftTypeDescNode) {
        STNode commaToken, rightTypeDescNode, gtToken;

        switch (nextTokenKind) {
            case COMMA_TOKEN:
                commaToken = parseComma();
                rightTypeDescNode = parseTypeDescriptorInternal(ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC);
                break;
            case GT_TOKEN:
                commaToken = STNodeFactory.createEmptyNode();
                rightTypeDescNode = STNodeFactory.createEmptyNode();
                break;
            default:
                Solution solution =
                        recover(peek(), ParserRuleContext.STREAM_TYPE_FIRST_PARAM_RHS, ltToken, leftTypeDescNode);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }
                return parseStreamTypeParamsNode(solution.tokenKind, ltToken, leftTypeDescNode);
        }

        gtToken = parseGTToken();
        return STNodeFactory.createStreamTypeParamsNode(ltToken, leftTypeDescNode, commaToken, rightTypeDescNode,
                gtToken);
    }

    /**
     * Parse stream-keyword.
     *
     * @return Parsed stream-keyword node
     */
    private STNode parseStreamKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.STREAM_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.STREAM_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse let expression.
     * <p>
     * <code>
     * let-expr := let let-var-decl [, let-var-decl]* in expression
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseLetExpression(boolean isRhsExpr) {
        STNode letKeyword = parseLetKeyword();
        STNode letVarDeclarations = parseLetVarDeclarations(ParserRuleContext.LET_EXPR_LET_VAR_DECL, isRhsExpr);
        STNode inKeyword = parseInKeyword();

        // If the variable declaration list is empty, clone the letKeyword token with the given diagnostic.
        letKeyword = cloneWithDiagnosticIfListEmpty(letVarDeclarations,
                letKeyword, DiagnosticErrorCode.ERROR_MISSING_LET_VARIABLE_DECLARATION);

        // allow-actions flag is always false, since there will not be any actions
        // within the let-expr, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, false);
        return STNodeFactory.createLetExpressionNode(letKeyword, letVarDeclarations, inKeyword, expression);
    }

    /**
     * Parse let-keyword.
     *
     * @return Let-keyword node
     */
    private STNode parseLetKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.LET_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.LET_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse let variable declarations.
     * <p>
     * <code>let-var-decl-list := let-var-decl [, let-var-decl]*</code>
     *
     * @return Parsed node
     */
    private STNode parseLetVarDeclarations(ParserRuleContext context, boolean isRhsExpr) {
        startContext(context);
        List<STNode> varDecls = new ArrayList<>();
        STToken nextToken = peek();

        // Make sure at least one let variable declaration is present
        if (isEndOfLetVarDeclarations(nextToken.kind)) {
            endContext();
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first variable declaration, that has no leading comma
        STNode varDec = parseLetVarDecl(isRhsExpr);
        varDecls.add(varDec);

        // Parse the remaining variable declarations
        nextToken = peek();
        STNode leadingComma;
        while (!isEndOfLetVarDeclarations(nextToken.kind)) {
            leadingComma = parseComma();
            varDecls.add(leadingComma);
            varDec = parseLetVarDecl(isRhsExpr);
            varDecls.add(varDec);
            nextToken = peek();
        }

        endContext();
        return STNodeFactory.createNodeList(varDecls);
    }

    private boolean isEndOfLetVarDeclarations(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case COMMA_TOKEN:
            case AT_TOKEN:
                return false;
            case IN_KEYWORD:
                return true;
            default:
                return !isTypeStartingToken(tokenKind);
        }
    }

    /**
     * Parse let variable declaration.
     * <p>
     * <code>let-var-decl := [annots] typed-binding-pattern = expression</code>
     *
     * @return Parsed node
     */
    private STNode parseLetVarDecl(boolean isRhsExpr) {
        STNode annot = parseAnnotations();
        STNode typedBindingPattern = parseTypedBindingPattern(ParserRuleContext.LET_EXPR_LET_VAR_DECL);
        STNode assign = parseAssignOp();

        // allow-actions flag is always false, since there will not be any actions
        // within the let-var-decl, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, isRhsExpr, false);
        return STNodeFactory.createLetVariableDeclarationNode(annot, typedBindingPattern, assign, expression);
    }

    /**
     * Parse raw backtick string template expression.
     * <p>
     * <code>BacktickString := `expression`</code>
     *
     * @return Template expression node
     */
    private STNode parseTemplateExpression() {
        STNode type = STNodeFactory.createEmptyNode();
        STNode startingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);
        STNode content = parseTemplateContent();
        STNode endingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);
        return STNodeFactory.createTemplateExpressionNode(SyntaxKind.RAW_TEMPLATE_EXPRESSION, type, startingBackTick,
                content, endingBackTick);
    }

    private STNode parseTemplateContent() {
        List<STNode> items = new ArrayList<>();
        STToken nextToken = peek();
        while (!isEndOfBacktickContent(nextToken.kind)) {
            STNode contentItem = parseTemplateItem();
            items.add(contentItem);
            nextToken = peek();
        }
        return STNodeFactory.createNodeList(items);
    }

    private boolean isEndOfBacktickContent(SyntaxKind kind) {
        switch (kind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseTemplateItem() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.INTERPOLATION_START_TOKEN) {
            return parseInterpolation();
        }

        // Template string component
        return consume();
    }

    /**
     * Parse string template expression.
     * <p>
     * <code>string-template-expr := string ` expression `</code>
     *
     * @return String template expression node
     */
    private STNode parseStringTemplateExpression() {
        STNode type = parseStringKeyword();
        STNode startingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);
        STNode content = parseTemplateContent();
        STNode endingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_END);
        return STNodeFactory.createTemplateExpressionNode(SyntaxKind.STRING_TEMPLATE_EXPRESSION, type, startingBackTick,
                content, endingBackTick);
    }

    /**
     * Parse <code>string</code> keyword.
     *
     * @return string keyword node
     */
    private STNode parseStringKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.STRING_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.STRING_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse XML template expression.
     * <p>
     * <code>xml-template-expr := xml BacktickString</code>
     *
     * @return XML template expression
     */
    private STNode parseXMLTemplateExpression() {
        STNode xmlKeyword = parseXMLKeyword();
        STNode startingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);
        STNode content = parseTemplateContentAsXML();
        STNode endingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_END);
        return STNodeFactory.createTemplateExpressionNode(SyntaxKind.XML_TEMPLATE_EXPRESSION, xmlKeyword,
                startingBackTick, content, endingBackTick);
    }

    /**
     * Parse <code>xml</code> keyword.
     *
     * @return xml keyword node
     */
    private STNode parseXMLKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.XML_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.XML_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse the content of the template string as XML. This method first read the
     * input in the same way as the raw-backtick-template (BacktickString). Then
     * it parses the content as XML.
     *
     * @return XML node
     */
    private STNode parseTemplateContentAsXML() {
        // Separate out the interpolated expressions to a queue. Then merge the string content using '${}'.
        // These '&{}' are used to represent the interpolated locations. XML parser will replace '&{}' with
        // the actual interpolated expression, while building the XML tree.
        ArrayDeque<STNode> expressions = new ArrayDeque<>();
        StringBuilder xmlStringBuilder = new StringBuilder();
        STToken nextToken = peek();
        while (!isEndOfBacktickContent(nextToken.kind)) {
            STNode contentItem = parseTemplateItem();
            if (contentItem.kind == SyntaxKind.TEMPLATE_STRING) {
                xmlStringBuilder.append(((STToken) contentItem).text());
            } else {
                xmlStringBuilder.append("${}");
                expressions.add(contentItem);
            }
            nextToken = peek();
        }

        TextDocument textDocument = TextDocuments.from(xmlStringBuilder.toString());
        AbstractTokenReader tokenReader = new TokenReader(new XMLLexer(textDocument.getCharacterReader()));
        XMLParser xmlParser = new XMLParser(tokenReader, expressions);
        return xmlParser.parse();
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
        startContext(ParserRuleContext.INTERPOLATION);
        STNode interpolStart = parseInterpolationStart();
        STNode expr = parseExpression();
        removeAdditionalTokensInInterpolation();
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createInterpolationNode(interpolStart, expr, closeBrace);
    }

    /**
     * Parse interpolation start token.
     * <p>
     * <code>interpolation-start := ${</code>
     *
     * @return Interpolation start token
     */
    private STNode parseInterpolationStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.INTERPOLATION_START_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.INTERPOLATION_START_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Remove if there any tokens left after the expression inside the interpolation.
     */
    private void removeAdditionalTokensInInterpolation() {
        while (true) {
            STToken nextToken = peek();
            switch (nextToken.kind) {
                case EOF_TOKEN:
                    return;
                case CLOSE_BRACE_TOKEN:
                    return;
                default:
                    consume();
                    this.errorHandler.reportInvalidNode(nextToken, "invalid token '" + nextToken.text() + "'");
            }
        }
    }

    /**
     * Parse back-tick token.
     *
     * @return Back-tick token
     */
    private STNode parseBacktickToken(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.BACKTICK_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ctx);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse table type descriptor.
     * <p>
     * table-type-descriptor := table row-type-parameter [key-constraint]
     * row-type-parameter := type-parameter
     * key-constraint := key-specifier | key-type-constraint
     * key-specifier := key ( [ field-name (, field-name)* ] )
     * key-type-constraint := key type-parameter
     * </p>
     *
     * @return Parsed table type desc node.
     */
    private STNode parseTableTypeDescriptor() {
        STNode tableKeywordToken = parseTableKeyword();
        STNode rowTypeParameterNode = parseRowTypeParameter();
        STNode keyConstraintNode;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.KEY_KEYWORD) {
            STNode keyKeywordToken = parseKeyKeyword();
            keyConstraintNode = parseKeyConstraint(keyKeywordToken);
        } else {
            keyConstraintNode = STNodeFactory.createEmptyNode();
        }
        return STNodeFactory.createTableTypeDescriptorNode(tableKeywordToken, rowTypeParameterNode, keyConstraintNode);
    }

    /**
     * Parse row type parameter node.
     * <p>
     * row-type-parameter := type-parameter
     * </p>
     *
     * @return Parsed node.
     */
    private STNode parseRowTypeParameter() {
        startContext(ParserRuleContext.ROW_TYPE_PARAM);
        STNode rowTypeParameterNode = parseTypeParameter();
        endContext();
        return rowTypeParameterNode;
    }

    /**
     * Parse type parameter node.
     * <p>
     * type-parameter := < type-descriptor >
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseTypeParameter() {
        STNode ltToken = parseLTToken();
        STNode typeNode = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_ANGLE_BRACKETS);
        STNode gtToken = parseGTToken();
        return STNodeFactory.createTypeParameterNode(ltToken, typeNode, gtToken);
    }

    /**
     * Parse key constraint.
     * <p>
     * key-constraint := key-specifier | key-type-constraint
     * </p>
     *
     * @return Parsed node.
     */
    private STNode parseKeyConstraint(STNode keyKeywordToken) {
        return parseKeyConstraint(peek().kind, keyKeywordToken);
    }

    private STNode parseKeyConstraint(SyntaxKind nextTokenKind, STNode keyKeywordToken) {
        switch (nextTokenKind) {
            case OPEN_PAREN_TOKEN:
                return parseKeySpecifier(keyKeywordToken);
            case LT_TOKEN:
                return parseKeyTypeConstraint(keyKeywordToken);
            default:
                Solution solution = recover(peek(), ParserRuleContext.KEY_CONSTRAINTS_RHS, keyKeywordToken);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }
                return parseKeyConstraint(solution.tokenKind, keyKeywordToken);
        }
    }

    /**
     * Parse key specifier given parsed key keyword token.
     * <p>
     * <code>key-specifier := key ( [ field-name (, field-name)* ] )</code>
     *
     * @return Parsed node
     */
    private STNode parseKeySpecifier(STNode keyKeywordToken) {
        startContext(ParserRuleContext.KEY_SPECIFIER);
        STNode openParenToken = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        STNode fieldNamesNode = parseFieldNames();
        STNode closeParenToken = parseCloseParenthesis();
        endContext();
        return STNodeFactory.createKeySpecifierNode(keyKeywordToken, openParenToken, fieldNamesNode, closeParenToken);
    }

    /**
     * Parse key type constraint.
     * <p>
     * key-type-constraint := key type-parameter
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseKeyTypeConstraint(STNode keyKeywordToken) {
        STNode typeParameterNode = parseTypeParameter();
        return STNodeFactory.createKeyTypeConstraintNode(keyKeywordToken, typeParameterNode);
    }

    /**
     * Parse function type descriptor.
     * <p>
     * <code>function-type-descriptor := function function-signature</code>
     *
     * @return Function type descriptor node
     */
    private STNode parseFunctionTypeDesc() {
        startContext(ParserRuleContext.FUNC_TYPE_DESC);
        STNode functionKeyword = parseFunctionKeyword();
        STNode signature = parseFuncSignature(true);
        endContext();
        return STNodeFactory.createFunctionTypeDescriptorNode(functionKeyword, signature);
    }

    /**
     * Parse explicit anonymous function expression.
     * <p>
     * <code>explicit-anonymous-function-expr := [annots] function function-signature anon-func-body</code>
     *
     * @param annots Annotations.
     * @param isRhsExpr Is expression in rhs context
     * @return Anonymous function expression node
     */
    private STNode parseExplicitFunctionExpression(STNode annots, boolean isRhsExpr) {
        startContext(ParserRuleContext.ANON_FUNC_EXPRESSION);
        STNode funcKeyword = parseFunctionKeyword();
        STNode funcSignature = parseFuncSignature(false);
        STNode funcBody = parseAnonFuncBody(isRhsExpr);
        return STNodeFactory.createExplicitAnonymousFunctionExpressionNode(annots, funcKeyword, funcSignature,
                funcBody);
    }

    /**
     * Parse anonymous function body.
     * <p>
     * <code>anon-func-body := block-function-body | expr-function-body</code>
     *
     * @param isRhsExpr Is expression in rhs context
     * @return Anon function body node
     */
    private STNode parseAnonFuncBody(boolean isRhsExpr) {
        return parseAnonFuncBody(peek().kind, isRhsExpr);
    }

    private STNode parseAnonFuncBody(SyntaxKind nextTokenKind, boolean isRhsExpr) {
        switch (nextTokenKind) {
            case OPEN_BRACE_TOKEN:
            case EOF_TOKEN:
                STNode body = parseFunctionBodyBlock(true);
                endContext();
                return body;
            case RIGHT_DOUBLE_ARROW_TOKEN:
                // we end the anon-func context here, before going for expressions.
                // That is because we wouldn't know when will it end inside expressions.
                endContext();
                return parseExpressionFuncBody(true, isRhsExpr);
            default:
                Solution solution = recover(peek(), ParserRuleContext.ANON_FUNC_BODY, isRhsExpr);
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }
                return parseAnonFuncBody(solution.tokenKind, isRhsExpr);
        }
    }

    /**
     * Parse expression function body.
     * <p>
     * <code>expr-function-body := => expression</code>
     *
     * @param isAnon Is anonymous function.
     * @param isRhsExpr Is expression in rhs context
     * @return Expression function body node
     */
    private STNode parseExpressionFuncBody(boolean isAnon, boolean isRhsExpr) {
        STNode rightDoubleArrow = parseDoubleRightArrow();
        STNode expression = parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, isRhsExpr, false);

        STNode semiColon;
        if (isAnon) {
            semiColon = STNodeFactory.createEmptyNode();
        } else {
            semiColon = parseSemicolon();
        }
        return STNodeFactory.createExpressionFunctionBodyNode(rightDoubleArrow, expression, semiColon);
    }

    /**
     * Parse '=>' token.
     *
     * @return Double right arrow token
     */
    private STNode parseDoubleRightArrow() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.EXPR_FUNC_BODY_START);
            return sol.recoveredNode;
        }
    }

    private STNode parseImplicitAnonFunc(STNode params, boolean isRhsExpr) {
        switch (params.kind) {
            case SIMPLE_NAME_REFERENCE:
            case INFER_PARAM_LIST:
                break;
            case BRACED_EXPRESSION:
                params = getAnonFuncParam((STBracedExpressionNode) params);
                break;
            default:
                this.errorHandler.reportInvalidNode(null, "lhs must be an identifier or a param list");
        }
        STNode rightDoubleArrow = parseDoubleRightArrow();
        STNode expression = parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, isRhsExpr, false);
        return STNodeFactory.createImplicitAnonymousFunctionExpressionNode(params, rightDoubleArrow, expression);
    }

    /**
     * Create a new anon-func-param node from a braced expression.
     *
     * @param params Braced expression
     * @return Anon-func param node
     */
    private STNode getAnonFuncParam(STBracedExpressionNode params) {
        List<STNode> paramList = new ArrayList<>();
        paramList.add(params.expression);
        return STNodeFactory.createImplicitAnonymousFunctionParameters(params.openParen,
                STNodeFactory.createNodeList(paramList), params.closeParen);
    }

    /**
     * Parse implicit anon function expression.
     *
     * @param openParen Open parenthesis token
     * @param firstParam First parameter
     * @param isRhsExpr Is expression in rhs context
     * @return Implicit anon function expression node
     */
    private STNode parseImplicitAnonFunc(STNode openParen, STNode firstParam, boolean isRhsExpr) {
        List<STNode> paramList = new ArrayList<>();
        paramList.add(firstParam);

        // Parse the remaining params
        STToken nextToken = peek();
        STNode paramEnd;
        STNode param;
        while (!isEndOfAnonFuncParametersList(nextToken.kind)) {
            paramEnd = parseImplicitAnonFuncParamEnd(nextToken.kind);
            if (paramEnd == null) {
                break;
            }

            paramList.add(paramEnd);
            param = parseIdentifier(ParserRuleContext.IMPLICIT_ANON_FUNC_PARAM);
            paramList.add(param);
            nextToken = peek();
        }

        STNode params = STNodeFactory.createNodeList(paramList);
        STNode closeParen = parseCloseParenthesis();
        endContext(); // end arg list context

        STNode inferedParams = STNodeFactory.createImplicitAnonymousFunctionParameters(openParen, params, closeParen);
        return parseImplicitAnonFunc(inferedParams, isRhsExpr);
    }

    private STNode parseImplicitAnonFuncParamEnd() {
        return parseImplicitAnonFuncParamEnd(peek().kind);
    }

    private STNode parseImplicitAnonFuncParamEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_PAREN_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.ANON_FUNC_PARAM_RHS);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseImplicitAnonFuncParamEnd(solution.tokenKind);
        }
    }

    private boolean isEndOfAnonFuncParametersList(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case RETURNS_KEYWORD:
            case TYPE_KEYWORD:
            case LISTENER_KEYWORD:
            case IF_KEYWORD:
            case WHILE_KEYWORD:
            case OPEN_BRACE_TOKEN:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse tuple type descriptor.
     * <p>
     * <code>tuple-type-descriptor := [ tuple-member-type-descriptors ]
     * <br/><br/>
     * tuple-member-type-descriptors := member-type-descriptor (, member-type-descriptor)* [, tuple-rest-descriptor]
     *                                     | [ tuple-rest-descriptor ]
     * <br/><br/>
     * tuple-rest-descriptor := type-descriptor ...
     * </code>
     *
     * @return
     */
    private STNode parseTupleTypeDesc() {
        STNode openBracket = parseOpenBracket();
        startContext(ParserRuleContext.TYPE_DESC_IN_TUPLE);
        STNode memberTypeDesc = parseTupleMemberTypeDescList();
        STNode closeBracket = parseCloseBracket();
        endContext();

        // If the tuple member type-desc list is empty, clone the openBracket token with the given diagnostic,
        openBracket = cloneWithDiagnosticIfListEmpty(memberTypeDesc,
                openBracket, DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC);

        return STNodeFactory.createTupleTypeDescriptorNode(openBracket, memberTypeDesc, closeBracket);
    }

    /**
     * Parse tuple member type descriptors.
     *
     * @return Parsed node
     */
    private STNode parseTupleMemberTypeDescList() {
        List<STNode> typeDescList = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list
        if (isEndOfTypeList(nextToken.kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first typedesc, that has no leading comma
        STNode typeDesc = parseTypeDescriptorInternal(ParserRuleContext.TYPE_DESC_IN_TUPLE);

        return parseTupleTypeMembers(typeDesc, typeDescList);
    }

    private STNode parseTupleTypeMembers(STNode typeDesc, List<STNode> typeDescList) {
        STToken nextToken;
        // Parse the remaining type descs
        nextToken = peek();
        STNode tupleMemberRhs;
        while (!isEndOfTypeList(nextToken.kind)) {
            tupleMemberRhs = parseTupleMemberRhs(nextToken.kind);
            if (tupleMemberRhs == null) {
                break;
            }
            if (tupleMemberRhs.kind == SyntaxKind.ELLIPSIS_TOKEN) {
                typeDesc = STNodeFactory.createRestDescriptorNode(typeDesc, tupleMemberRhs);
                break;
            }
            typeDescList.add(typeDesc);
            typeDescList.add(tupleMemberRhs);
            typeDesc = parseTypeDescriptorInternal(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            nextToken = peek();
        }

        typeDescList.add(typeDesc);

        return STNodeFactory.createNodeList(typeDescList);
    }

    private STNode parseTupleMemberRhs() {
        return parseTupleMemberRhs(peek().kind);
    }

    private STNode parseTupleMemberRhs(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            case ELLIPSIS_TOKEN:
                return parseEllipsis();
            default:
                Solution solution = recover(peek(), ParserRuleContext.TYPE_DESC_IN_TUPLE_RHS);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTupleMemberRhs(solution.tokenKind);
        }
    }

    private boolean isEndOfTypeList(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case CLOSE_BRACKET_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case EOF_TOKEN:
            case EQUAL_TOKEN:
            case OPEN_BRACE_TOKEN:
            case SEMICOLON_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse table constructor or query expression.
     * <p>
     * <code>
     * table-constructor-or-query-expr := table-constructor-expr | query-expr
     * <br/>
     * table-constructor-expr := table [key-specifier] [ [row-list] ]
     * <br/>
     * query-expr := [query-construct-type] query-pipeline select-clause
     * <br/>
     * query-construct-type := table key-specifier | stream
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseTableConstructorOrQuery(boolean isRhsExpr) {
        startContext(ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION);
        STNode tableOrQueryExpr = parseTableConstructorOrQuery(peek().kind, isRhsExpr);
        endContext();
        return tableOrQueryExpr;
    }

    private STNode parseTableConstructorOrQuery(SyntaxKind nextTokenKind, boolean isRhsExpr) {
        STNode queryConstructType;
        switch (nextTokenKind) {
            case FROM_KEYWORD:
                queryConstructType = STNodeFactory.createEmptyNode();
                return parseQueryExprRhs(queryConstructType, isRhsExpr);
            case STREAM_KEYWORD:
                queryConstructType = parseStreamKeyword();
                return parseQueryExprRhs(queryConstructType, isRhsExpr);
            case TABLE_KEYWORD:
                STNode tableKeyword = parseTableKeyword();
                return parseTableConstructorOrQuery(tableKeyword, isRhsExpr);
            default:
                Solution solution = recover(peek(), ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_START, isRhsExpr);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTableConstructorOrQuery(solution.tokenKind, isRhsExpr);
        }

    }

    private STNode parseTableConstructorOrQuery(STNode tableKeyword, boolean isRhsExpr) {
        return parseTableConstructorOrQuery(peek().kind, tableKeyword, isRhsExpr);
    }

    private STNode parseTableConstructorOrQuery(SyntaxKind nextTokenKind, STNode tableKeyword, boolean isRhsExpr) {
        STNode keySpecifier;
        switch (nextTokenKind) {
            case OPEN_BRACKET_TOKEN:
                keySpecifier = STNodeFactory.createEmptyNode();
                return parseTableConstructorExprRhs(tableKeyword, keySpecifier);
            case KEY_KEYWORD:
                keySpecifier = parseKeySpecifier();
                return parseTableConstructorOrQueryRhs(tableKeyword, keySpecifier, isRhsExpr);
            default:
                Solution solution = recover(peek(), ParserRuleContext.TABLE_KEYWORD_RHS, tableKeyword, isRhsExpr);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTableConstructorOrQuery(solution.tokenKind, tableKeyword, isRhsExpr);
        }
    }

    private STNode parseTableConstructorOrQueryRhs(STNode tableKeyword, STNode keySpecifier, boolean isRhsExpr) {
        return parseTableConstructorOrQueryRhs(peek().kind, tableKeyword, keySpecifier, isRhsExpr);
    }

    private STNode parseTableConstructorOrQueryRhs(SyntaxKind nextTokenKind, STNode tableKeyword, STNode keySpecifier,
                                                   boolean isRhsExpr) {
        switch (nextTokenKind) {
            case FROM_KEYWORD:
                return parseQueryExprRhs(parseQueryConstructType(tableKeyword, keySpecifier), isRhsExpr);
            case OPEN_BRACKET_TOKEN:
                return parseTableConstructorExprRhs(tableKeyword, keySpecifier);
            default:
                Solution solution = recover(peek(), ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_RHS, tableKeyword,
                        keySpecifier, isRhsExpr);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTableConstructorOrQueryRhs(solution.tokenKind, tableKeyword, keySpecifier, isRhsExpr);
        }
    }

    /**
     * Parse query construct type.
     * <p>
     * <code>query-construct-type := table key-specifier</code>
     *
     * @return Parsed node
     */
    private STNode parseQueryConstructType(STNode tableKeyword, STNode keySpecifier) {
        return STNodeFactory.createQueryConstructTypeNode(tableKeyword, keySpecifier);
    }

    /**
     * Parse query expression.
     * <p>
     * <code>
     * query-expr-rhs := query-pipeline select-clause
     * <br/>
     * query-pipeline := from-clause intermediate-clause*
     * </code>
     *
     * @param queryConstructType queryConstructType that precedes this rhs
     * @return Parsed node
     */
    private STNode parseQueryExprRhs(STNode queryConstructType, boolean isRhsExpr) {
        switchContext(ParserRuleContext.QUERY_EXPRESSION);
        STNode fromClause = parseFromClause(isRhsExpr);

        List<STNode> clauses = new ArrayList<>();
        STNode intermediateClause;
        STNode selectClause = null;
        while (!isEndOfIntermediateClause(peek().kind)) {
            intermediateClause = parseIntermediateClause(isRhsExpr);
            if (intermediateClause == null) {
                break;
            }

            // If there are more clauses after select clause they are ignored
            if (selectClause != null) {
                // TODO: In future we should store ignored nodes
                this.errorHandler.reportInvalidNode(null, "extra clauses after select clause");
                break;
            }

            if (intermediateClause.kind == SyntaxKind.SELECT_CLAUSE) {
                selectClause = intermediateClause;
            } else {
                clauses.add(intermediateClause);
            }
        }

        if (peek().kind == SyntaxKind.DO_KEYWORD) {
            STNode intermediateClauses = STNodeFactory.createNodeList(clauses);
            STNode queryPipeline = STNodeFactory.createQueryPipelineNode(fromClause, intermediateClauses);
            return parseQueryAction(queryPipeline, selectClause);
        }

        if (selectClause == null) {
            STNode selectKeyword = errorHandler.createMissingToken(SyntaxKind.SELECT_KEYWORD);
            STNode expr = errorHandler.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            selectClause = STNodeFactory.createSelectClauseNode(selectKeyword, expr);

            // Now we need to attach the diagnostic to the last intermediate clause.
            // If there are no intermediate clauses, then attach to the from clause.
            if (clauses.isEmpty()) {
                fromClause = errorHandler.addDiagnostics(fromClause, DiagnosticErrorCode.ERROR_MISSING_SELECT_CLAUSE);
            } else {
                int lastIndex = clauses.size() - 1;
                STNode intClauseWithDiagnostic = errorHandler.addDiagnostics(clauses.get(lastIndex),
                        DiagnosticErrorCode.ERROR_MISSING_SELECT_CLAUSE);
                clauses.set(lastIndex, intClauseWithDiagnostic);
            }
        }

        STNode intermediateClauses = STNodeFactory.createNodeList(clauses);
        STNode queryPipeline = STNodeFactory.createQueryPipelineNode(fromClause, intermediateClauses);
        return STNodeFactory.createQueryExpressionNode(queryConstructType, queryPipeline, selectClause);
    }

    /**
     * Parse an intermediate clause.
     * <p>
     * <code>
     * intermediate-clause := from-clause | where-clause | let-clause
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseIntermediateClause(boolean isRhsExpr) {
        return parseIntermediateClause(peek().kind, isRhsExpr);
    }

    private STNode parseIntermediateClause(SyntaxKind nextTokenKind, boolean isRhsExpr) {
        switch (nextTokenKind) {
            case FROM_KEYWORD:
                return parseFromClause(isRhsExpr);
            case WHERE_KEYWORD:
                return parseWhereClause(isRhsExpr);
            case LET_KEYWORD:
                return parseLetClause(isRhsExpr);
            case SELECT_KEYWORD:
                return parseSelectClause(isRhsExpr);
            case DO_KEYWORD:
            case SEMICOLON_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.QUERY_PIPELINE_RHS, isRhsExpr);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseIntermediateClause(solution.tokenKind, isRhsExpr);
        }
    }

    private boolean isEndOfIntermediateClause(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case SEMICOLON_TOKEN:
            case PUBLIC_KEYWORD:
            case FUNCTION_KEYWORD:
            case EOF_TOKEN:
            case RESOURCE_KEYWORD:
            case LISTENER_KEYWORD:
            case DOCUMENTATION_LINE:
            case PRIVATE_KEYWORD:
            case RETURNS_KEYWORD:
            case SERVICE_KEYWORD:
            case TYPE_KEYWORD:
            case CONST_KEYWORD:
            case FINAL_KEYWORD:
            case DO_KEYWORD:
                return true;
            default:
                return isValidExprRhsStart(tokenKind);
        }
    }

    /**
     * Parse from clause.
     * <p>
     * <code>from-clause := from typed-binding-pattern in expression</code>
     *
     * @return Parsed node
     */
    private STNode parseFromClause(boolean isRhsExpr) {
        STNode fromKeyword = parseFromKeyword();
        STNode typedBindingPattern = parseTypedBindingPattern(ParserRuleContext.FROM_CLAUSE);
        STNode inKeyword = parseInKeyword();

        // allow-actions flag is always false, since there will not be any actions
        // within the from-clause, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        return STNodeFactory.createFromClauseNode(fromKeyword, typedBindingPattern, inKeyword, expression);
    }

    /**
     * Parse from-keyword.
     *
     * @return From-keyword node
     */
    private STNode parseFromKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FROM_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FROM_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse where clause.
     * <p>
     * <code>where-clause := where expression</code>
     *
     * @return Parsed node
     */
    private STNode parseWhereClause(boolean isRhsExpr) {
        STNode whereKeyword = parseWhereKeyword();

        // allow-actions flag is always false, since there will not be any actions
        // within the where-clause, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        return STNodeFactory.createWhereClauseNode(whereKeyword, expression);
    }

    /**
     * Parse where-keyword.
     *
     * @return Where-keyword node
     */
    private STNode parseWhereKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.WHERE_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.WHERE_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse let clause.
     * <p>
     * <code>let-clause := let let-var-decl [, let-var-decl]* </code>
     *
     * @return Parsed node
     */
    private STNode parseLetClause(boolean isRhsExpr) {
        STNode letKeyword = parseLetKeyword();
        STNode letVarDeclarations = parseLetVarDeclarations(ParserRuleContext.LET_CLAUSE_LET_VAR_DECL, isRhsExpr);

        // If the variable declaration list is empty, clone the letKeyword token with the given diagnostic.
        letKeyword = cloneWithDiagnosticIfListEmpty(letVarDeclarations,
                letKeyword, DiagnosticErrorCode.ERROR_MISSING_LET_VARIABLE_DECLARATION);

        return STNodeFactory.createLetClauseNode(letKeyword, letVarDeclarations);
    }

    /**
     * Parse select clause.
     * <p>
     * <code>select-clause := select expression</code>
     *
     * @return Parsed node
     */
    private STNode parseSelectClause(boolean isRhsExpr) {
        STNode selectKeyword = parseSelectKeyword();

        // allow-actions flag is always false, since there will not be any actions
        // within the select-clause, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        return STNodeFactory.createSelectClauseNode(selectKeyword, expression);
    }

    /**
     * Parse select-keyword.
     *
     * @return Select-keyword node
     */
    private STNode parseSelectKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SELECT_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.SELECT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse start action.
     * <p>
     * <code>start-action := [annots] start (function-call-expr|method-call-expr|remote-method-call-action)</code>
     *
     * @return Start action node
     */
    private STNode parseStartAction(STNode annots) {
        STNode startKeyword = parseStartKeyword();
        STNode expr = parseActionOrExpression();
        validateExprInStartAction(expr);
        return STNodeFactory.createStartActionNode(getAnnotations(annots), startKeyword, expr);
    }

    /**
     * Parse start keyword.
     *
     * @return Start keyword node
     */
    private STNode parseStartKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.START_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.START_KEYWORD);
            return sol.recoveredNode;
        }
    }

    private void validateExprInStartAction(STNode expression) {
        switch (expression.kind) {
            case FUNCTION_CALL:
            case METHOD_CALL:
            case REMOTE_METHOD_CALL_ACTION:
                break;
            default:
                if (isMissingNode(expression)) {
                    break;
                }

                this.errorHandler.reportInvalidNode(null, "expression followed by the start keyword must be a " +
                        "func-call, a method-call or a remote-method-call");
                break;
        }
    }

    /**
     * Parse flush action.
     * <p>
     * <code>flush-action := flush [peer-worker]</code>
     *
     * @return flush action node
     */
    private STNode parseFlushAction() {
        STNode flushKeyword = parseFlushKeyword();
        STNode peerWorker = parseOptionalPeerWorkerName();
        return STNodeFactory.createFlushActionNode(flushKeyword, peerWorker);
    }

    /**
     * Parse flush keyword.
     *
     * @return flush keyword node
     */
    private STNode parseFlushKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FLUSH_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.FLUSH_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse peer worker.
     * <p>
     * <code>peer-worker := worker-name | default</code>
     *
     * @return peer worker name node
     */
    private STNode parseOptionalPeerWorkerName() {
        STToken token = peek();
        switch (token.kind) {
            case IDENTIFIER_TOKEN:
            case DEFAULT_KEYWORD:
                return STNodeFactory.createSimpleNameReferenceNode(consume());
            default:
                return STNodeFactory.createEmptyNode();
        }
    }

    /**
     * Parse intersection type descriptor.
     * <p>
     * intersection-type-descriptor := type-descriptor & type-descriptor
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseIntersectionTypeDescriptor(STNode leftTypeDesc, ParserRuleContext context) {
        // we come here only after seeing & token hence consume.
        STNode bitwiseAndToken = consume();
        STNode rightTypeDesc = parseTypeDescriptor(context);
        return STNodeFactory.createIntersectionTypeDescriptorNode(leftTypeDesc, bitwiseAndToken, rightTypeDesc);
    }

    /**
     * Parse singleton type descriptor.
     * <p>
     * singleton-type-descriptor := simple-const-expr
     * simple-const-expr :=
     * nil-literal
     * | boolean-literal
     * | [Sign] int-literal
     * | [Sign] floating-point-literal
     * | string-literal
     * | constant-reference-expr
     * </p>
     */
    private STNode parseSingletonTypeDesc() {
        STNode simpleContExpr = parseConstExpr();
        return STNodeFactory.createSingletonTypeDescriptorNode(simpleContExpr);
    }

    private STNode parseSignedIntOrFloat() {
        STNode operator = parseUnaryOperator();
        STNode literal;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                literal = consume();
                break;
            default: // decimal integer literal
                literal = parseDecimalIntLiteral(ParserRuleContext.DECIMAL_INTEGER_LITERAL);
        }
        return STNodeFactory.createUnaryExpressionNode(operator, literal);
    }

    private boolean isSingletonTypeDescStart(SyntaxKind tokenKind, boolean inTypeDescCtx) {
        STToken nextNextToken = getNextNextToken(tokenKind);
        switch (tokenKind) {
            case STRING_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
                if (inTypeDescCtx || isValidTypeDescRHSOutSideTypeDescCtx(nextNextToken)) {
                    return true;
                }
                return false;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return isIntOrFloat(nextNextToken);
            default:
                return false;
        }
    }

    static boolean isIntOrFloat(STToken token) {
        switch (token.kind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidTypeDescRHSOutSideTypeDescCtx(STToken token) {
        switch (token.kind) {
            case IDENTIFIER_TOKEN:
            case QUESTION_MARK_TOKEN:
            case OPEN_PAREN_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
            case OPEN_BRACE_TOKEN:
            case ERROR_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the parser reached to a valid expression start.
     *
     * @param nextTokenKind Kind of the next immediate token.
     * @param nextTokenIndex Index to the next token.
     * @return <code>true</code> if this is a start of a valid expression. <code>false</code> otherwise
     */
    private boolean isValidExpressionStart(SyntaxKind nextTokenKind, int nextTokenIndex) {
        switch (nextTokenKind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            case IDENTIFIER_TOKEN:
                return isValidExprRhsStart(peek(nextTokenIndex + 1).kind);
            case OPEN_PAREN_TOKEN:
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
            case OPEN_BRACE_TOKEN:
            case TYPEOF_KEYWORD:
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
            case TRAP_KEYWORD:
            case OPEN_BRACKET_TOKEN:
            case LT_TOKEN:
            case FROM_KEYWORD:
            case LET_KEYWORD:
            case BACKTICK_TOKEN:
            case NEW_KEYWORD:
            case LEFT_ARROW_TOKEN:
                return true;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                nextTokenIndex++;
                return isValidExpressionStart(peek(nextTokenIndex).kind, nextTokenIndex);
            case FUNCTION_KEYWORD:

            case TABLE_KEYWORD:
                return peek(nextTokenIndex).kind == SyntaxKind.FROM_KEYWORD;
            case STREAM_KEYWORD:
                STToken nextNextToken = peek(nextTokenIndex);
                return nextNextToken.kind == SyntaxKind.KEY_KEYWORD ||
                        nextNextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN ||
                        nextNextToken.kind == SyntaxKind.FROM_KEYWORD;
            case ERROR_KEYWORD:
                return peek(nextTokenIndex).kind == SyntaxKind.OPEN_PAREN_TOKEN;
            case SERVICE_KEYWORD:
                return peek(nextTokenIndex).kind == SyntaxKind.OPEN_BRACE_TOKEN;
            case XML_KEYWORD:
            case STRING_KEYWORD:
                return peek(nextTokenIndex).kind == SyntaxKind.BACKTICK_TOKEN;

            // 'start' and 'flush' are start of actions, but not expressions.
            case START_KEYWORD:
            case FLUSH_KEYWORD:
            case WAIT_KEYWORD:
            default:
                return false;
        }
    }

    /**
     * Parse sync send action.
     * <p>
     * <code>sync-send-action := expression ->> peer-worker</code>
     *
     * @param expression LHS expression of the sync send action
     * @return Sync send action node
     */
    private STNode parseSyncSendAction(STNode expression) {
        STNode syncSendToken = parseSyncSendToken();
        STNode peerWorker = parsePeerWorkerName();
        return STNodeFactory.createSyncSendActionNode(expression, syncSendToken, peerWorker);
    }

    /**
     * Parse peer worker.
     * <p>
     * <code>peer-worker := worker-name | default</code>
     *
     * @return peer worker name node
     */
    private STNode parsePeerWorkerName() {
        STToken token = peek();
        switch (token.kind) {
            case IDENTIFIER_TOKEN:
            case DEFAULT_KEYWORD:
                return STNodeFactory.createSimpleNameReferenceNode(consume());
            default:
                Solution sol = recover(token, ParserRuleContext.PEER_WORKER_NAME);
                return sol.recoveredNode;
        }
    }

    /**
     * Parse sync send token.
     * <p>
     * <code>sync-send-token :=  ->> </code>
     *
     * @return sync send token
     */
    private STNode parseSyncSendToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SYNC_SEND_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.SYNC_SEND_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse receive action.
     * <p>
     * <code>receive-action := single-receive-action | multiple-receive-action</code>
     *
     * @return Receive action
     */
    private STNode parseReceiveAction() {
        STNode leftArrow = parseLeftArrowToken();
        STNode receiveWorkers = parseReceiveWorkers();
        return STNodeFactory.createReceiveActionNode(leftArrow, receiveWorkers);
    }

    private STNode parseReceiveWorkers() {
        return parseReceiveWorkers(peek().kind);
    }

    private STNode parseReceiveWorkers(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case DEFAULT_KEYWORD:
            case IDENTIFIER_TOKEN:
                return parsePeerWorkerName();
            case OPEN_BRACE_TOKEN:
                return parseMultipleReceiveWorkers();
            default:
                Solution solution = recover(peek(), ParserRuleContext.RECEIVE_WORKERS);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseReceiveWorkers(solution.tokenKind);
        }
    }

    /**
     * Parse multiple worker receivers.
     * <p>
     * <code>{ receive-field (, receive-field)* }</code>
     *
     * @return Multiple worker receiver node
     */
    private STNode parseMultipleReceiveWorkers() {
        startContext(ParserRuleContext.MULTI_RECEIVE_WORKERS);
        STNode openBrace = parseOpenBrace();
        STNode receiveFields = parseReceiveFields();
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createReceiveFieldsNode(openBrace, receiveFields, closeBrace);
    }

    private STNode parseReceiveFields() {
        List<STNode> receiveFields = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list
        if (isEndOfReceiveFields(nextToken.kind)) {
            this.errorHandler.reportMissingTokenError("missing receive field");
            return STNodeFactory.createNodeList(new ArrayList<>());
        }

        // Parse first receive field, that has no leading comma
        STNode receiveField = parseReceiveField();
        receiveFields.add(receiveField);

        // Parse the remaining receive fields
        nextToken = peek();
        STNode recieveFieldEnd;
        while (!isEndOfReceiveFields(nextToken.kind)) {
            recieveFieldEnd = parseReceiveFieldEnd(nextToken.kind);
            if (recieveFieldEnd == null) {
                break;
            }

            receiveFields.add(recieveFieldEnd);
            receiveField = parseReceiveField();
            receiveFields.add(receiveField);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(receiveFields);
    }

    private boolean isEndOfReceiveFields(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseReceiveFieldEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.RECEIVE_FIELD_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseReceiveFieldEnd(solution.tokenKind);
        }
    }

    private STNode parseReceiveField() {
        return parseReceiveField(peek().kind);
    }

    /**
     * Parse receive field.
     * <p>
     * <code>receive-field := peer-worker | field-name : peer-worker</code>
     *
     * @param nextTokenKind Kind of the next token
     * @return Receiver field node
     */
    private STNode parseReceiveField(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case DEFAULT_KEYWORD:
                return parseDefaultKeyword();
            case IDENTIFIER_TOKEN:
                STNode identifier = parseIdentifier(ParserRuleContext.RECEIVE_FIELD_NAME);
                return createQualifiedReceiveField(identifier);
            default:
                Solution solution = recover(peek(), ParserRuleContext.RECEIVE_FIELD);

                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                if (solution.tokenKind == SyntaxKind.IDENTIFIER_TOKEN) {
                    return createQualifiedReceiveField(solution.recoveredNode);
                }

                return solution.recoveredNode;
        }
    }

    private STNode createQualifiedReceiveField(STNode identifier) {
        if (peek().kind != SyntaxKind.COLON_TOKEN) {
            return identifier;
        }

        STNode colon = parseColon();
        STNode peerWorker = parsePeerWorkerName();
        return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, peerWorker);
    }

    /**
     *
     * Parse left arrow (<-) token.
     *
     * @return left arrow token
     */
    private STNode parseLeftArrowToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.LEFT_ARROW_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.LEFT_ARROW_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse signed right shift token (>>).
     *
     * @return Parsed node
     */
    private STNode parseSignedRightShiftToken() {
        STNode openGTToken = parseGTToken();
        validateRightShiftOperatorWS(openGTToken);

        STNode endLGToken = parseGTToken();
        return STNodeFactory.createDoubleGTTokenNode(openGTToken, endLGToken);
    }

    /**
     * Parse unsigned right shift token (>>>).
     *
     * @return Parsed node
     */
    private STNode parseUnsignedRightShiftToken() {
        STNode openGTToken = parseGTToken();
        validateRightShiftOperatorWS(openGTToken);

        STNode middleGTToken = parseGTToken();
        validateRightShiftOperatorWS(middleGTToken);

        STNode endLGToken = parseGTToken();
        return STNodeFactory.createTrippleGTTokenNode(openGTToken, middleGTToken, endLGToken);
    }

    /**
     * Validate the whitespace between '>' tokens of right shift operators.
     *
     * @param node Preceding node
     */
    private void validateRightShiftOperatorWS(STNode node) {
        int diff = node.widthWithTrailingMinutiae() - node.width();
        if (diff > 0) {
            this.errorHandler.reportMissingTokenError("no whitespaces allowed between >>");
        }
    }

    /**
     * Parse wait action.
     * <p>
     * <code>wait-action := single-wait-action | multiple-wait-action | alternate-wait-action </code>
     *
     * @return Wait action node
     */
    private STNode parseWaitAction() {
        STNode waitKeyword = parseWaitKeyword();
        if (peek().kind == SyntaxKind.OPEN_BRACE_TOKEN) {
            return parseMultiWaitAction(waitKeyword);
        }

        return parseSingleOrAlternateWaitAction(waitKeyword);
    }

    /**
     * Parse wait keyword.
     *
     * @return wait keyword
     */
    private STNode parseWaitKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.WAIT_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.WAIT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse single or alternate wait actions.
     * <p>
     * <code>
     * alternate-or-single-wait-action := wait wait-future-expr (| wait-future-expr)+
     * <br/>
     * wait-future-expr := expression but not mapping-constructor-expr
     * </code>
     *
     * @param waitKeyword wait keyword
     * @return Single or alternate wait action node
     */
    private STNode parseSingleOrAlternateWaitAction(STNode waitKeyword) {
        startContext(ParserRuleContext.ALTERNATE_WAIT_EXPRS);
        STToken nextToken = peek();

        // Return an empty list
        if (isEndOfWaitFutureExprList(nextToken.kind)) {
            this.errorHandler.reportMissingTokenError("missing wait field");
            endContext();
            STNode waitFutureExprs = STNodeFactory.createEmptyNodeList();
            return STNodeFactory.createWaitActionNode(waitKeyword, waitFutureExprs);
        }

        // Parse first wait, that has no leading comma
        List<STNode> waitFutureExprList = new ArrayList<>();
        STNode waitField = parseWaitFutureExpr();
        waitFutureExprList.add(waitField);

        // Parse remaining wait future expression
        nextToken = peek();
        STNode waitFutureExprEnd;
        while (!isEndOfWaitFutureExprList(nextToken.kind)) {
            waitFutureExprEnd = parseWaitFutureExprEnd(nextToken.kind, 1);
            if (waitFutureExprEnd == null) {
                break;
            }

            waitFutureExprList.add(waitFutureExprEnd);
            waitField = parseWaitFutureExpr();
            waitFutureExprList.add(waitField);
            nextToken = peek();
        }

        // https://github.com/ballerina-platform/ballerina-spec/issues/525
        // STNode waitFutureExprs = STNodeFactory.createNodeList(waitFutureExprList);
        endContext();
        return STNodeFactory.createWaitActionNode(waitKeyword, waitFutureExprList.get(0));
    }

    private boolean isEndOfWaitFutureExprList(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case SEMICOLON_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseWaitFutureExpr() {
        STNode waitFutureExpr = parseExpression();
        if (waitFutureExpr.kind == SyntaxKind.MAPPING_CONSTRUCTOR) {
            this.errorHandler.reportInvalidNode(null, "mapping constructor expression cannot use as å wait expression");
        }
        return waitFutureExpr;
    }

    private STNode parseWaitFutureExprEnd(int nextTokenIndex) {
        return parseWaitFutureExprEnd(peek().kind, 1);
    }

    private STNode parseWaitFutureExprEnd(SyntaxKind nextTokenKind, int nextTokenIndex) {
        switch (nextTokenKind) {
            case PIPE_TOKEN:
                return parsePipeToken();
            default:
                if (isEndOfWaitFutureExprList(nextTokenKind) ||
                        !isValidExpressionStart(nextTokenKind, nextTokenIndex)) {
                    return null;
                }

                Solution solution = recover(peek(), ParserRuleContext.WAIT_FUTURE_EXPR_END, nextTokenIndex);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                // current token becomes next token
                return parseWaitFutureExprEnd(solution.tokenKind, 0);
        }
    }

    /**
     * Parse multiple wait action.
     * <p>
     * <code>multiple-wait-action := wait { wait-field (, wait-field)* }</code>
     *
     * @param waitKeyword Wait keyword
     * @return Multiple wait action node
     */
    private STNode parseMultiWaitAction(STNode waitKeyword) {
        startContext(ParserRuleContext.MULTI_WAIT_FIELDS);
        STNode openBrace = parseOpenBrace();
        STNode waitFields = parseWaitFields();
        STNode closeBrace = parseCloseBrace();
        endContext();

        STNode waitFieldsNode = STNodeFactory.createWaitFieldsListNode(openBrace, waitFields, closeBrace);
        return STNodeFactory.createWaitActionNode(waitKeyword, waitFieldsNode);
    }

    private STNode parseWaitFields() {
        List<STNode> waitFields = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list
        if (isEndOfReceiveFields(nextToken.kind)) {
            this.errorHandler.reportMissingTokenError("missing wait field");
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first receive field, that has no leading comma
        STNode waitField = parseWaitField();
        waitFields.add(waitField);

        // Parse the remaining receive fields
        nextToken = peek();
        STNode waitFieldEnd;
        while (!isEndOfReceiveFields(nextToken.kind)) {
            waitFieldEnd = parseWaitFieldEnd(nextToken.kind);
            if (waitFieldEnd == null) {
                break;
            }

            waitFields.add(waitFieldEnd);
            waitField = parseWaitField();
            waitFields.add(waitField);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(waitFields);
    }

    private STNode parseWaitFieldEnd() {
        return parseWaitFieldEnd(peek().kind);
    }

    private STNode parseWaitFieldEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.WAIT_FIELD_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseWaitFieldEnd(solution.tokenKind);
        }
    }

    private STNode parseWaitField() {
        return parseWaitField(peek().kind);
    }

    /**
     * Parse wait field.
     * <p>
     * <code>wait-field := variable-name | field-name : wait-future-expr</code>
     *
     * @param nextTokenKind Kind of the next token
     * @return Receiver field node
     */
    private STNode parseWaitField(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                STNode identifier = parseIdentifier(ParserRuleContext.WAIT_FIELD_NAME);
                return createQualifiedWaitField(identifier);
            default:
                Solution solution = recover(peek(), ParserRuleContext.WAIT_FIELD_NAME);

                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseWaitField(solution.tokenKind);
        }
    }

    private STNode createQualifiedWaitField(STNode identifier) {
        if (peek().kind != SyntaxKind.COLON_TOKEN) {
            return identifier;
        }

        STNode colon = parseColon();
        STNode waitFutureExpr = parseWaitFutureExpr();
        return STNodeFactory.createWaitFieldNode(identifier, colon, waitFutureExpr);
    }

    /**
     * Parse annot access expression.
     * <p>
     * <code>
     * annot-access-expr := expression .@ annot-tag-reference
     * <br/>
     * annot-tag-reference := qualified-identifier | identifier
     * </code>
     *
     * @param lhsExpr Preceding expression of the annot access access
     * @return Parsed node
     */
    private STNode parseAnnotAccessExpression(STNode lhsExpr) {
        STNode annotAccessToken = parseAnnotChainingToken();
        STNode annotTagReference = parseFieldAccessIdentifier();
        return STNodeFactory.createAnnotAccessExpressionNode(lhsExpr, annotAccessToken, annotTagReference);
    }

    /**
     * Parse annot-chaining-token.
     *
     * @return Parsed node
     */
    private STNode parseAnnotChainingToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ANNOT_CHAINING_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ANNOT_CHAINING_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse field access identifier.
     * <p>
     * <code>field-access-identifier := qualified-identifier | identifier</code>
     *
     * @return Parsed node
     */
    private STNode parseFieldAccessIdentifier() {
        return parseQualifiedIdentifier(ParserRuleContext.FIELD_ACCESS_IDENTIFIER);
    }

    /**
     * Parse query action.
     * <p>
     * <code>query-action := query-pipeline do-clause
     * <br/>
     * do-clause := do block-stmt
     * </code>
     *
     * @param queryPipeline Query pipeline
     * @param selectClause Select clause if any This is only for validation.
     * @return Query action node
     */
    private STNode parseQueryAction(STNode queryPipeline, STNode selectClause) {
        if (selectClause != null) {
            this.errorHandler.reportInvalidNode(null, "cannot have a select clause in query action");
        }

        startContext(ParserRuleContext.DO_CLAUSE);
        STNode doKeyword = parseDoKeyword();
        STNode blockStmt = parseBlockNode();
        endContext();
        return STNodeFactory.createQueryActionNode(queryPipeline, doKeyword, blockStmt);
    }

    /**
     * Parse 'do' keyword.
     *
     * @return do keyword node
     */
    private STNode parseDoKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DO_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.DO_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse optional field access or xml optional attribute access expression.
     * <p>
     * <code>
     * optional-field-access-expr := expression ?. field-name
     * <br/>
     * xml-optional-attribute-access-expr := expression ?. xml-attribute-name
     * <br/>
     * xml-attribute-name := xml-qualified-name | qualified-identifier | identifier
     * <br/>
     * xml-qualified-name := xml-namespace-prefix : identifier
     * <br/>
     * xml-namespace-prefix := identifier
     * </code>
     *
     * @param lhsExpr Preceding expression of the optional access
     * @return Parsed node
     */
    private STNode parseOptionalFieldAccessExpression(STNode lhsExpr) {
        STNode optionalFieldAccessToken = parseOptionalChainingToken();
        STNode fieldName = parseFieldAccessIdentifier();
        return STNodeFactory.createOptionalFieldAccessExpressionNode(lhsExpr, optionalFieldAccessToken, fieldName);
    }

    /**
     * Parse optional chaining token.
     *
     * @return parsed node
     */
    private STNode parseOptionalChainingToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPTIONAL_CHAINING_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.OPTIONAL_CHAINING_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse conditional expression.
     * <p>
     * <code>conditional-expr := expression ? expression : expression</code>
     *
     * @param lhsExpr Preceding expression of the question mark
     * @return Parsed node
     */
    private STNode parseConditionalExpression(STNode lhsExpr) {
        startContext(ParserRuleContext.CONDITIONAL_EXPRESSION);
        STNode questionMark = parseQuestionMark();
        STNode middleExpr = parseExpression(OperatorPrecedence.ELVIS_CONDITIONAL, true, false);
        STNode colon = parseColon();
        endContext();
        STNode endExpr = parseExpression(OperatorPrecedence.ELVIS_CONDITIONAL, true, false);
        return STNodeFactory.createConditionalExpressionNode(lhsExpr, questionMark, middleExpr, colon, endExpr);
    }

    /**
     * Parse enum declaration.
     * <p>
     * module-enum-decl :=
     * metadata
     * [public] enum identifier { enum-member (, enum-member)* }
     * enum-member := metadata identifier [= const-expr]
     * </p>
     *
     * @param metadata
     * @param qualifier
     *
     * @return Parsed enum node.
     */
    private STNode parseEnumDeclaration(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.MODULE_ENUM_DECLARATION);
        STNode enumKeywordToken = parseEnumKeyword();
        STNode identifier = parseIdentifier(ParserRuleContext.MODULE_ENUM_NAME);
        STNode openBraceToken = parseOpenBrace();
        STNode enumMemberList = parseEnumMemberList();
        STNode closeBraceToken = parseCloseBrace();

        endContext();
        return STNodeFactory.createEnumDeclarationNode(metadata, qualifier, enumKeywordToken, identifier,
                openBraceToken, enumMemberList, closeBraceToken);
    }

    /**
     * Parse 'enum' keyword.
     *
     * @return enum keyword node
     */
    private STNode parseEnumKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ENUM_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ENUM_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse enum member list.
     * <p>
     * enum-member := metadata identifier [= const-expr]
     * </p>
     *
     * @return enum member list node.
     */
    private STNode parseEnumMemberList() {
        startContext(ParserRuleContext.ENUM_MEMBER_LIST);
        STToken nextToken = peek();

        // Report an empty enum member list
        if (nextToken.kind == SyntaxKind.CLOSE_BRACE_TOKEN) {
            this.errorHandler.reportMissingTokenError("enum member list cannot be empty");
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first enum member, that has no leading comma
        List<STNode> enumMemberList = new ArrayList<>();
        STNode enumMember = parseEnumMember();

        // Parse the remaining enum members
        nextToken = peek();
        STNode enumMemberRhs;
        while (nextToken.kind != SyntaxKind.CLOSE_BRACE_TOKEN) {
            enumMemberRhs = parseEnumMemberEnd(nextToken.kind);
            if (enumMemberRhs == null) {
                break;
            }
            enumMemberList.add(enumMember);
            enumMemberList.add(enumMemberRhs);
            enumMember = parseEnumMember();
            nextToken = peek();
        }

        enumMemberList.add(enumMember);

        endContext();
        return STNodeFactory.createNodeList(enumMemberList);
    }

    /**
     * Parse enum member.
     * <p>
     * enum-member := metadata identifier [= const-expr]
     * </p>
     *
     * @return Parsed enum member node.
     */
    private STNode parseEnumMember() {
        STToken nextToken = peek();
        STNode metadata;
        switch (nextToken.kind) {
            case DOCUMENTATION_LINE:
            case AT_TOKEN:
                metadata = parseMetaData(nextToken.kind);
                break;
            default:
                metadata = STNodeFactory.createEmptyNode();
        }

        STNode identifierNode = parseIdentifier(ParserRuleContext.ENUM_MEMBER_NAME);
        return parseEnumMemberRhs(metadata, identifierNode);
    }

    private STNode parseEnumMemberRhs(STNode metadata, STNode identifierNode) {
        return parseEnumMemberRhs(peek().kind, metadata, identifierNode);
    }

    private STNode parseEnumMemberRhs(SyntaxKind nextToken, STNode metadata, STNode identifierNode) {
        STNode equalToken, constExprNode;
        switch (nextToken) {
            case EQUAL_TOKEN:
                equalToken = parseAssignOp();
                constExprNode = parseExpression();
                break;
            case COMMA_TOKEN:
            case CLOSE_BRACE_TOKEN:
                equalToken = STNodeFactory.createEmptyNode();
                constExprNode = STNodeFactory.createEmptyNode();
                break;
            default:
                Solution solution = recover(peek(), ParserRuleContext.ENUM_MEMBER_RHS, metadata, identifierNode);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseEnumMemberRhs(solution.tokenKind, metadata, identifierNode);
        }

        return STNodeFactory.createEnumMemberNode(metadata, identifierNode, equalToken, constExprNode);
    }

    private STNode parseEnumMemberEnd() {
        return parseEnumMemberEnd(peek().kind);
    }

    private STNode parseEnumMemberEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.ENUM_MEMBER_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseEnumMemberEnd(solution.tokenKind);
        }
    }

    /**
     * Parse transaction statement.
     * <p>
     * <code>transaction-stmt := "transaction" block-stmt ;</code>
     *
     * @return Transaction statement node
     */
    private STNode parseTransactionStatement() {
        startContext(ParserRuleContext.TRANSACTION_STMT);
        STNode transactionKeyword = parseTransactionKeyword();
        STNode blockStmt = parseBlockNode();
        endContext();
        return STNodeFactory.createTransactionStatementNode(transactionKeyword, blockStmt);
    }

    /**
     * Parse transaction keyword.
     *
     * @return parsed node
     */
    private STNode parseTransactionKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TRANSACTION_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TRANSACTION_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse commit action.
     * <p>
     * <code>commit-action := "commit"</code>
     *
     * @return Commit action node
     */
    private STNode parseCommitAction() {
        STNode commitKeyword = parseCommitKeyword();
        return STNodeFactory.createCommitActionNode(commitKeyword);
    }

    /**
     * Parse commit keyword.
     *
     * @return parsed node
     */
    private STNode parseCommitKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.COMMIT_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.COMMIT_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse retry statement.
     * <p>
     * <code>
     * retry-stmt := "retry" retry-spec block-stmt
     * <br/>
     * retry-spec :=  [type-parameter] [ "(" arg-list ")" ]
     * </code>
     *
     * @return Retry statement node
     */
    private STNode parseRetryStatement() {
        startContext(ParserRuleContext.RETRY_STMT);
        STNode retryKeyword = parseRetryKeyword();
        STNode retryStmt = parseRetryKeywordRhs(retryKeyword);
        endContext();
        return retryStmt;
    }

    private STNode parseRetryKeywordRhs(STNode retryKeyword) {
        return parseRetryKeywordRhs(peek().kind, retryKeyword);
    }

    private STNode parseRetryKeywordRhs(SyntaxKind nextTokenKind, STNode retryKeyword) {
        switch (nextTokenKind) {
            case LT_TOKEN:
                STNode typeParam = parseTypeParameter();
                return parseRetryTypeParamRhs(retryKeyword, typeParam);
            case OPEN_PAREN_TOKEN:
            case OPEN_BRACE_TOKEN:
            case TRANSACTION_KEYWORD:
                typeParam = STNodeFactory.createEmptyNode();
                return parseRetryTypeParamRhs(nextTokenKind, retryKeyword, typeParam);
            default:
                Solution solution = recover(peek(), ParserRuleContext.RETRY_KEYWORD_RHS, retryKeyword);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseRetryKeywordRhs(solution.tokenKind, retryKeyword);
        }
    }

    private STNode parseRetryTypeParamRhs(STNode retryKeyword, STNode typeParam) {
        return parseRetryTypeParamRhs(peek().kind, retryKeyword, typeParam);
    }

    private STNode parseRetryTypeParamRhs(SyntaxKind nextTokenKind, STNode retryKeyword, STNode typeParam) {
        STNode args;
        switch (nextTokenKind) {
            case OPEN_PAREN_TOKEN:
                args = parseParenthesizedArgList();
                break;
            case OPEN_BRACE_TOKEN:
            case TRANSACTION_KEYWORD:
                args = STNodeFactory.createEmptyNode();
                break;
            default:
                Solution solution = recover(peek(), ParserRuleContext.RETRY_TYPE_PARAM_RHS, retryKeyword, typeParam);
                return parseRetryTypeParamRhs(solution.tokenKind, retryKeyword, typeParam);
        }

        STNode blockStmt = parseRetryBody();
        return STNodeFactory.createRetryStatementNode(retryKeyword, typeParam, args, blockStmt);
    }

    private STNode parseRetryBody() {
        return parseRetryBody(peek().kind);
    }

    private STNode parseRetryBody(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case OPEN_BRACE_TOKEN:
                return parseBlockNode();
            case TRANSACTION_KEYWORD:
                return parseTransactionStatement();
            default:
                Solution solution = recover(peek(), ParserRuleContext.RETRY_BODY);
                return parseRetryBody(solution.tokenKind);
        }
    }

    /**
     * Parse retry keyword.
     *
     * @return parsed node
     */
    private STNode parseRetryKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RETRY_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.RETRY_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse transaction statement.
     * <p>
     * <code>rollback-stmt := "rollback" [expression] ";"</code>
     *
     * @return Rollback statement node
     */
    private STNode parseRollbackStatement() {
        startContext(ParserRuleContext.ROLLBACK_STMT);
        STNode rollbackKeyword = parseRollbackKeyword();
        STNode expression;
        if (peek().kind == SyntaxKind.SEMICOLON_TOKEN) {
            expression = STNodeFactory.createEmptyNode();
        } else {
            expression = parseExpression();
        }

        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createRollbackStatementNode(rollbackKeyword, expression, semicolon);
    }

    /**
     * Parse rollback keyword.
     *
     * @return Rollback keyword node
     */
    private STNode parseRollbackKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ROLLBACK_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ROLLBACK_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse transactional expression.
     * <p>
     * <code>transactional-expr := "transactional"</code>
     *
     * @return Transactional expression node
     */
    private STNode parseTransactionalExpression() {
        STNode transactionalKeyword = parseTransactionalKeyword();
        return STNodeFactory.createTransactionalExpressionNode(transactionalKeyword);
    }

    /**
     * Parse transactional keyword.
     *
     * @return Transactional keyword node
     */
    private STNode parseTransactionalKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TRANSACTIONAL_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.ROLLBACK_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse service-constructor-expr.
     * <p>
     * <code>
     * service-constructor-expr := [annots] service service-body-block
     * <br/>
     * service-body-block := { service-method-defn* }
     * <br/>
     * service-method-defn := metadata [resource] function identifier function-signature method-defn-body
     * </code>
     *
     * @param annots Annotations
     * @return Service constructor expression node
     */
    private STNode parseServiceConstructorExpression(STNode annots) {
        startContext(ParserRuleContext.SERVICE_CONSTRUCTOR_EXPRESSION);
        STNode serviceKeyword = parseServiceKeyword();
        STNode serviceBody = parseServiceBody();
        endContext();
        return STNodeFactory.createServiceConstructorExpressionNode(annots, serviceKeyword, serviceBody);
    }

    /**
     * Parse base16 literal.
     * <p>
     * <code>
     * byte-array-literal := Base16Literal | Base64Literal
     * <br/>
     * Base16Literal := base16 WS ` HexGroup* WS `
     * <br/>
     * Base64Literal := base64 WS ` Base64Group* [PaddedBase64Group] WS `
     * </code>
     *
     * @param kind byte array literal kind
     * @return parsed node
     */
    private STNode parseByteArrayLiteral(SyntaxKind kind) {
        STNode type;

        if (kind == SyntaxKind.BASE16_KEYWORD) {
            type = parseBase16Keyword();
        } else {
            type = parseBase64Keyword();
        }

        STNode startingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);
        STNode content = parseByteArrayContent(kind);
        STNode endingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_END);
        return STNodeFactory.createByteArrayLiteralNode(type, startingBackTick, content, endingBackTick);
    }

    /**
     * Parse <code>base16</code> keyword.
     *
     * @return base16 keyword node
     */
    private STNode parseBase16Keyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BASE16_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.BASE16_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse <code>base64</code> keyword.
     *
     * @return base64 keyword node
     */
    private STNode parseBase64Keyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BASE64_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.BASE64_KEYWORD);
            return sol.recoveredNode;
        }
    }

    /**
     * Validate and parse byte array literal content.
     * An error is reported, if the content is invalid.
     *
     * @param kind byte array literal kind
     * @return parsed node
     */
    private STNode parseByteArrayContent(SyntaxKind kind) {
        STNode content = STNodeFactory.createEmptyNode();
        STToken nextToken = peek();

        List<STNode> items = new ArrayList<>();
        while (!isEndOfBacktickContent(nextToken.kind)) {
            content = parseTemplateItem();
            items.add(content);
            nextToken = peek();
        }

        if (items.size() > 1) {
            this.errorHandler.reportInvalidNode(null, "invalid content within backticks");
        } else if (items.size() == 1 && content.kind != SyntaxKind.TEMPLATE_STRING) {
            this.errorHandler.reportInvalidNode(null, "invalid content within backticks");
        } else if (items.size() == 1) {
            if (kind == SyntaxKind.BASE16_KEYWORD && !BallerinaLexer.isValidBase16LiteralContent(content.toString())) {
                this.errorHandler.reportInvalidNode(null, "invalid content within backticks");
            } else if (kind == SyntaxKind.BASE64_KEYWORD &&
                    !BallerinaLexer.isValidBase64LiteralContent(content.toString())) {
                this.errorHandler.reportInvalidNode(null, "invalid content within backticks");
            }
        }

        return content;
    }

    /**
     * Parse xml filter expression.
     * <p>
     * <code>xml-filter-expr := expression .< xml-name-pattern ></code>
     *
     * @param lhsExpr Preceding expression of .< token
     * @return Parsed node
     */
    private STNode parseXMLFilterExpression(STNode lhsExpr) {
        STNode xmlNamePatternChain = parseXMLFilterExpressionRhs();
        return STNodeFactory.createXMLFilterExpressionNode(lhsExpr, xmlNamePatternChain);
    }

    /**
     * Parse xml filter expression rhs.
     * <p>
     * <code>filer-expression-rhs := .< xml-name-pattern ></code>
     *
     * @return Parsed node
     */
    private STNode parseXMLFilterExpressionRhs() {
        STNode dotLTToken = parseDotLTToken();
        return parseXMLNamePatternChain(dotLTToken);
    }

    /**
     * Parse xml name pattern chain.
     * <p>
     * <code>
     * xml-name-pattern-chain := filer-expression-rhs | xml-element-children-step | xml-element-descendants-step
     * <br/>
     * filer-expression-rhs := .< xml-name-pattern >
     * <br/>
     * xml-element-children-step := /< xml-name-pattern >
     * <br/>
     * xml-element-descendants-step := /**\/<xml-name-pattern >
     * </code>
     *
     * @param startToken Preceding token of xml name pattern
     * @return Parsed node
     */
    private STNode parseXMLNamePatternChain(STNode startToken) {
        startContext(ParserRuleContext.XML_NAME_PATTERN);
        STNode xmlNamePattern = parseXMLNamePattern();
        STNode gtToken = parseGTToken();
        endContext();
        return STNodeFactory.createXMLNamePatternChainingNode(startToken, xmlNamePattern, gtToken);
    }

    /**
     * Parse <code> .< </code> token.
     *
     * @return Parsed node
     */
    private STNode parseDotLTToken() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.DOT_LT_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(nextToken, ParserRuleContext.DOT_LT_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse xml name pattern.
     * <p>
     * <code>xml-name-pattern := xml-atomic-name-pattern [| xml-atomic-name-pattern]*</code>
     *
     * @return Parsed node
     */
    private STNode parseXMLNamePattern() {
        List<STNode> xmlAtomicNamePatternList = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list
        if (isEndOfXMLNamePattern(nextToken.kind)) {
            this.errorHandler.reportMissingTokenError("missing xml atomic name pattern");
            return STNodeFactory.createNodeList(xmlAtomicNamePatternList);
        }

        // Parse first xml atomic name pattern, that has no leading pipe token
        STNode xmlAtomicNamePattern = parseXMLAtomicNamePattern();
        xmlAtomicNamePatternList.add(xmlAtomicNamePattern);

        // Parse the remaining xml atomic name patterns
        STNode leadingPipe;
        while (!isEndOfXMLNamePattern(peek().kind)) {
            leadingPipe = parsePipeToken();
            xmlAtomicNamePatternList.add(leadingPipe);

            xmlAtomicNamePattern = parseXMLAtomicNamePattern();
            xmlAtomicNamePatternList.add(xmlAtomicNamePattern);
        }

        return STNodeFactory.createNodeList(xmlAtomicNamePatternList);
    }

    private boolean isEndOfXMLNamePattern(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
            case ASTERISK_TOKEN:
            case COLON_TOKEN:
                return false;
            case GT_TOKEN:
            case EOF_TOKEN:
            case AT_TOKEN:
            case DOCUMENTATION_LINE:
            case CLOSE_BRACE_TOKEN:
            case SEMICOLON_TOKEN:
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
            case FUNCTION_KEYWORD:
            case RETURNS_KEYWORD:
            case SERVICE_KEYWORD:
            case TYPE_KEYWORD:
            case LISTENER_KEYWORD:
            case CONST_KEYWORD:
            case FINAL_KEYWORD:
            case RESOURCE_KEYWORD:
                return true;
            default:
                return isSimpleType(tokenKind);
        }
    }

    /**
     * Parse xml atomic name pattern.
     * <p>
     * <code>
     * xml-atomic-name-pattern :=
     *   *
     *   | identifier
     *   | xml-namespace-prefix : identifier
     *   | xml-namespace-prefix : *
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseXMLAtomicNamePattern() {
        startContext(ParserRuleContext.XML_ATOMIC_NAME_PATTERN);
        STNode atomicNamePattern = parseXMLAtomicNamePatternBody();
        endContext();
        return atomicNamePattern;
    }

    private STNode parseXMLAtomicNamePatternBody() {
        STToken token = peek();
        STNode identifier;
        switch (token.kind) {
            case ASTERISK_TOKEN:
                return consume();
            case IDENTIFIER_TOKEN:
                identifier = consume();
                break;
            default:
                Solution sol = recover(token, ParserRuleContext.XML_ATOMIC_NAME_PATTERN_START);
                if (sol.action == Action.REMOVE) {
                    return sol.recoveredNode;
                }

                if (sol.recoveredNode.kind == SyntaxKind.ASTERISK_TOKEN) {
                    return sol.recoveredNode;
                }

                identifier = sol.recoveredNode;
                break;
        }

        return parseXMLAtomicNameIdentifier(identifier);
    }

    private STNode parseXMLAtomicNameIdentifier(STNode identifier) {
        STToken token = peek();
        if (token.kind == SyntaxKind.COLON_TOKEN) {
            STNode colon = consume();
            STToken nextToken = peek();
            if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN || nextToken.kind == SyntaxKind.ASTERISK_TOKEN) {
                STToken endToken = consume();
                return STNodeFactory.createXMLAtomicNamePatternNode(identifier, colon, endToken);
            }
        }
        return STNodeFactory.createSimpleNameReferenceNode(identifier);
    }

    /**
     * Parse xml step expression.
     * <p>
     * <code>xml-step-expr := expression xml-step-start</code>
     *
     * @param lhsExpr Preceding expression of /*, /<, or /**\/< token
     * @return Parsed node
     */
    private STNode parseXMLStepExpression(STNode lhsExpr) {
        STNode xmlStepStart = parseXMLStepStart();
        return STNodeFactory.createXMLStepExpressionNode(lhsExpr, xmlStepStart);
    }

    /**
     * Parse xml filter expression rhs.
     * <p>
     * <code>
     *  xml-step-start :=
     *      xml-all-children-step
     *      | xml-element-children-step
     *      | xml-element-descendants-step
     * <br/>
     * xml-all-children-step := /*
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseXMLStepStart() {
        STToken token = peek();
        STNode startToken;

        switch (token.kind) {
            case SLASH_ASTERISK_TOKEN:
                return consume();
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
                startToken = parseDoubleSlashDoubleAsteriskLTToken();
                break;
            case SLASH_LT_TOKEN:
            default:
                startToken = parseSlashLTToken();
                break;
        }
        return parseXMLNamePatternChain(startToken);
    }

    /**
     * Parse <code> /< </code> token.
     *
     * @return Parsed node
     */
    private STNode parseSlashLTToken() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.SLASH_LT_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(nextToken, ParserRuleContext.SLASH_LT_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse <code> /< </code> token.
     *
     * @return Parsed node
     */
    private STNode parseDoubleSlashDoubleAsteriskLTToken() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(nextToken, ParserRuleContext.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN);
            return sol.recoveredNode;
        }
    }

    // ------------------------ Ambiguity resolution at statement start ---------------------------

    /**
     * Parse any statement that starts with a token that has ambiguity between being
     * a type-desc or an expression.
     *
     * @param annots Annotations
     * @return Statement node
     */
    private STNode parseStmtStartsWithTypeOrExpr(SyntaxKind nextTokenKind, STNode annots) {
        startContext(ParserRuleContext.AMBIGUOUS_STMT);
        STNode typeOrExpr = parseTypedBindingPatternOrExpr(nextTokenKind, true);
        return parseStmtStartsWithTypedBPOrExprRhs(annots, typeOrExpr);
    }

    private STNode parseStmtStartsWithTypedBPOrExprRhs(STNode annots, STNode typedBindingPatternOrExpr) {
        if (typedBindingPatternOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
            STNode finalKeyword = STNodeFactory.createEmptyNode();
            switchContext(ParserRuleContext.VAR_DECL_STMT);
            return parseVarDeclRhs(annots, finalKeyword, typedBindingPatternOrExpr, false);
        }

        STNode expr = getExpression(typedBindingPatternOrExpr);
        expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, expr, false, true);
        return parseStatementStartWithExprRhs(expr);
    }

    private STNode parseTypedBindingPatternOrExpr(boolean allowAssignment) {
        STToken nextToken = peek();
        return parseTypedBindingPatternOrExpr(nextToken.kind, allowAssignment);
    }

    private STNode parseTypedBindingPatternOrExpr(SyntaxKind nextTokenKind, boolean allowAssignment) {
        STNode typeOrExpr;
        switch (nextTokenKind) {
            case OPEN_PAREN_TOKEN:
                return parseTypedBPOrExprStartsWithOpenParenthesis();
            case FUNCTION_KEYWORD:
                return parseAnonFuncExprOrTypedBPWithFuncType();
            case IDENTIFIER_TOKEN:
                typeOrExpr = parseQualifiedIdentifier(ParserRuleContext.TYPE_NAME_OR_VAR_NAME);
                return parseTypedBindingPatternOrExprRhs(typeOrExpr, allowAssignment);

            // Can be a singleton type or expression.
            case NIL_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                STNode basicLiteral = parseBasicLiteral();
                return parseTypedBindingPatternOrExprRhs(basicLiteral, allowAssignment);

            default:
                if (isValidExpressionStart(nextTokenKind, 1)) {
                    return parseActionOrExpressionInLhs(nextTokenKind, null);
                }

                return parseTypedBindingPattern(ParserRuleContext.VAR_DECL_STMT);
        }
    }

    private STNode parseTypedBindingPatternOrExprRhs(STNode typeOrExpr, boolean allowAssignment) {
        STToken nextToken = peek();
        return parseTypedBindingPatternOrExprRhs(nextToken.kind, typeOrExpr, allowAssignment);
    }

    private STNode parseTypedBindingPatternOrExprRhs(SyntaxKind nextTokenKind, STNode typeOrExpr,
                                                     boolean allowAssignment) {
        switch (nextTokenKind) {
            case PIPE_TOKEN:
                STToken nextNextToken = peek(2);
                if (nextNextToken.kind == SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode pipe = parsePipeToken();
                STNode rhsTypedBPOrExpr = parseTypedBindingPatternOrExpr(allowAssignment);
                if (rhsTypedBPOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
                    STTypedBindingPatternNode typedBP = (STTypedBindingPatternNode) rhsTypedBPOrExpr;
                    typeOrExpr = getTypeDescFromExpr(typeOrExpr);
                    STNode newTypeDesc =
                            STNodeFactory.createUnionTypeDescriptorNode(typeOrExpr, pipe, typedBP.typeDescriptor);
                    return STNodeFactory.createTypedBindingPatternNode(newTypeDesc, typedBP.bindingPattern);
                }

                return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr, pipe,
                        rhsTypedBPOrExpr);
            case BITWISE_AND_TOKEN:
                nextNextToken = peek(2);
                if (nextNextToken.kind == SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode ampersand = parseBinaryOperator();
                rhsTypedBPOrExpr = parseTypedBindingPatternOrExpr(allowAssignment);
                if (rhsTypedBPOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
                    STTypedBindingPatternNode typedBP = (STTypedBindingPatternNode) rhsTypedBPOrExpr;
                    typeOrExpr = getTypeDescFromExpr(typeOrExpr);
                    STNode newTypeDesc = STNodeFactory.createIntersectionTypeDescriptorNode(typeOrExpr, ampersand,
                            typedBP.typeDescriptor);
                    return STNodeFactory.createTypedBindingPatternNode(newTypeDesc, typedBP.bindingPattern);
                }

                return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr, ampersand,
                        rhsTypedBPOrExpr);
            case SEMICOLON_TOKEN:
            case IDENTIFIER_TOKEN:
            case QUESTION_MARK_TOKEN:
                if (isAmbiguous(typeOrExpr)) {
                    // treat as type
                    STNode typeDesc = getTypeDescFromExpr(typeOrExpr);
                    return parseTypeBindingPatternStartsWithAmbiguousNode(typeDesc);
                }

                return typeOrExpr;
            case EQUAL_TOKEN:
                return typeOrExpr;
            case OPEN_BRACKET_TOKEN:
                return parseTypedBindingPatternOrMemberAccess(typeOrExpr, false, allowAssignment,
                        ParserRuleContext.AMBIGUOUS_STMT);
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundBinaryOperator(nextTokenKind)) {
                    return typeOrExpr;
                }

                // If the next token is part of a valid expression, then still parse it
                // as a statement that starts with an expression.
                if (isValidExprRhsStart(nextTokenKind)) {
                    return typeOrExpr;
                }

                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.BINDING_PATTERN_OR_EXPR_RHS, typeOrExpr, allowAssignment);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTypedBindingPatternOrExprRhs(solution.tokenKind, typeOrExpr, allowAssignment);
        }
    }

    private STNode parseTypeBindingPatternStartsWithAmbiguousNode(STNode typeDesc) {
        switchContext(ParserRuleContext.VAR_DECL_STMT);

        // We haven't parsed the type-desc as a type-desc (parsed as an identifier/expr).
        // Therefore handle the context manually here.
        startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        typeDesc = parseComplexTypeDescriptor(typeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, false);
        endContext();
        return parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
    }

    private STNode parseTypedBPOrExprStartsWithOpenParenthesis() {
        STNode exprOrTypeDesc = parseTypedDescOrExprStartsWithOpenParenthesis();
        return parseTypedBindingPatternOrExprRhs(exprOrTypeDesc, false);
    }

    /**
     * Parse type or expression that starts with open parenthesis. Possible options are:
     * 1) () - nil type-desc or nil-literal
     * 2) (T) - Parenthesized type-desc
     * 3) (expr) - Parenthesized expression
     * 4) (param, param, ..) - Anon function params
     * 
     * @return Type-desc or expression node
     */
    private STNode parseTypedDescOrExprStartsWithOpenParenthesis() {
        STNode openParen = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        STToken nextToken = peek();

        if (nextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            STNode closeParen = parseCloseParenthesis();
            return parseTypeOrExprStartWithEmptyParenthesis(openParen, closeParen);
        }

        STNode typeOrExpr = parseTypeDescOrExpr();
        if (isAction(typeOrExpr)) {
            STNode closeParen = parseCloseParenthesis();
            return STNodeFactory.createBracedExpressionNode(SyntaxKind.BRACED_ACTION, openParen, typeOrExpr,
                    closeParen);
        }

        if (isExpression(typeOrExpr.kind)) {
            startContext(ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAMS);
            return parseBracedExprOrAnonFuncParamRhs(peek().kind, openParen, typeOrExpr, false);
        }

        STNode closeParen = parseCloseParenthesis();
        return STNodeFactory.createParenthesisedTypeDescriptorNode(openParen, typeOrExpr, closeParen);

    }

    /**
     * Parse type-desc or expression. This method does not handle binding patterns.
     * 
     * @return Type-desc node or expression node
     */
    private STNode parseTypeDescOrExpr() {
        STToken nextToken = peek();
        STNode typeOrExpr;
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                return parseTypedDescOrExprStartsWithOpenParenthesis();
            case FUNCTION_KEYWORD:
                return parseAnonFuncExprOrFuncTypeDesc();
            case IDENTIFIER_TOKEN:
                typeOrExpr = parseQualifiedIdentifier(ParserRuleContext.TYPE_NAME_OR_VAR_NAME);
                return parseTypeDescOrExprRhs(typeOrExpr);

            // Can be a singleton type or expression.
            case NIL_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                STNode basicLiteral = parseBasicLiteral();
                return parseTypeDescOrExprRhs(basicLiteral);
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    return parseActionOrExpressionInLhs(nextToken.kind, null);
                }

                return parseTypeDescriptor(ParserRuleContext.VAR_DECL_STMT);
        }
    }

    private boolean isExpression(SyntaxKind kind) {
        return kind.compareTo(SyntaxKind.BINARY_EXPRESSION) >= 0 &&
                kind.compareTo(SyntaxKind.SERVICE_CONSTRUCTOR_EXPRESSION) <= 0;
    }

    /**
     * Parse statement that starts with an empty parenthesis. Empty parenthesis can be
     * 1) Nil literal
     * 2) Nil type-desc
     * 3) Anon-function params
     * 
     * @param openParen Open parenthesis
     * @param closeParen Close parenthesis
     * @return Parsed node
     */
    private STNode parseTypeOrExprStartWithEmptyParenthesis(STNode openParen, STNode closeParen) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case RIGHT_DOUBLE_ARROW_TOKEN:
                STNode params = STNodeFactory.createNodeList();
                STNode anonFuncParam =
                        STNodeFactory.createImplicitAnonymousFunctionParameters(openParen, params, closeParen);
                endContext();
                return anonFuncParam;
            default:
                return STNodeFactory.createNilLiteralNode(openParen, closeParen);
        }
    }

    private STNode parseAnonFuncExprOrTypedBPWithFuncType() {
        STNode exprOrTypeDesc = parseAnonFuncExprOrFuncTypeDesc();
        if (isAction(exprOrTypeDesc) || isExpression(exprOrTypeDesc.kind)) {
            return exprOrTypeDesc;
        }

        return parseTypedBindingPatternTypeRhs(exprOrTypeDesc, ParserRuleContext.VAR_DECL_STMT);
    }

    /**
     * Parse anon-func-expr or function-type-desc, by resolving the ambiguity.
     * 
     * @return Anon-func-expr or function-type-desc
     */
    private STNode parseAnonFuncExprOrFuncTypeDesc() {
        startContext(ParserRuleContext.FUNC_TYPE_DESC_OR_ANON_FUNC);
        STNode functionKeyword = parseFunctionKeyword();
        STNode funcSignature = parseFuncSignature(true);
        endContext();

        switch (peek().kind) {
            case OPEN_BRACE_TOKEN:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                startContext(ParserRuleContext.ANON_FUNC_EXPRESSION);
                // Anon function cannot have missing param-names. So validate it.
                funcSignature = validateAndGetFuncParams((STFunctionSignatureNode) funcSignature);

                STNode funcBody = parseAnonFuncBody(false);
                STNode annots = STNodeFactory.createEmptyNodeList();
                STNode anonFunc = STNodeFactory.createExplicitAnonymousFunctionExpressionNode(annots, functionKeyword,
                        funcSignature, funcBody);
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, anonFunc, false, true);
            case IDENTIFIER_TOKEN:
            default:
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                STNode funcTypeDesc = STNodeFactory.createFunctionTypeDescriptorNode(functionKeyword, funcSignature);
                return parseComplexTypeDescriptor(funcTypeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN,
                        true);
        }
    }

    private STNode parseTypeDescOrExprRhs(STNode typeOrExpr) {
        SyntaxKind nextTokenKind = peek().kind;
        return parseTypeDescOrExprRhs(nextTokenKind, typeOrExpr);
    }

    private STNode parseTypeDescOrExprRhs(SyntaxKind nextTokenKind, STNode typeOrExpr) {
        STNode typeDesc;
        switch (nextTokenKind) {
            case PIPE_TOKEN:
                STToken nextNextToken = peek(2);
                if (nextNextToken.kind == SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode pipe = parsePipeToken();
                STNode rhsTypeDescOrExpr = parseTypeDescOrExpr();
                if (isExpression(rhsTypeDescOrExpr.kind)) {
                    return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr, pipe,
                            rhsTypeDescOrExpr);
                }

                typeDesc = getTypeDescFromExpr(typeOrExpr);
                return STNodeFactory.createUnionTypeDescriptorNode(typeDesc, pipe, rhsTypeDescOrExpr);
            case BITWISE_AND_TOKEN:
                nextNextToken = peek(2);
                if (nextNextToken.kind != SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode ampersand = parseBinaryOperator();
                rhsTypeDescOrExpr = parseTypeDescOrExpr();
                if (isExpression(rhsTypeDescOrExpr.kind)) {
                    return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr, ampersand,
                            rhsTypeDescOrExpr);
                }

                typeDesc = getTypeDescFromExpr(typeOrExpr);
                return STNodeFactory.createIntersectionTypeDescriptorNode(typeDesc, ampersand, rhsTypeDescOrExpr);
            case IDENTIFIER_TOKEN:
            case QUESTION_MARK_TOKEN:
                // treat as type
                // We haven't parsed the type-desc as a type-desc (parsed as an identifier/expr).
                // Therefore handle the context manually here.
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                typeDesc = parseComplexTypeDescriptor(typeOrExpr, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN,
                        false);
                endContext();
                return typeDesc;
            case SEMICOLON_TOKEN:
                return getTypeDescFromExpr(typeOrExpr);
            case EQUAL_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case EOF_TOKEN:
            case COMMA_TOKEN:
                return typeOrExpr;
            case OPEN_BRACKET_TOKEN:
                return parseTypedBindingPatternOrMemberAccess(typeOrExpr, false, true,
                        ParserRuleContext.AMBIGUOUS_STMT);
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundBinaryOperator(nextTokenKind)) {
                    return typeOrExpr;
                }

                // If the next token is part of a valid expression, then still parse it
                // as a statement that starts with an expression.
                if (isValidExprRhsStart(nextTokenKind)) {
                    return parseExpressionRhs(nextTokenKind, DEFAULT_OP_PRECEDENCE, typeOrExpr, false, false);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.BINDING_PATTERN_OR_EXPR_RHS, typeOrExpr);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTypeDescOrExprRhs(solution.tokenKind, typeOrExpr);
        }
    }

    private boolean isAmbiguous(STNode expression) {
        switch (expression.kind) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case NIL_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return true;
            case BINARY_EXPRESSION:
                STBinaryExpressionNode binaryExpr = (STBinaryExpressionNode) expression;
                if (binaryExpr.operator.kind != SyntaxKind.PIPE_TOKEN ||
                        binaryExpr.operator.kind == SyntaxKind.BITWISE_AND_TOKEN) {
                    return false;
                }
                return isAmbiguous(binaryExpr.lhsExpr) && isAmbiguous(binaryExpr.rhsExpr);
            case BRACED_EXPRESSION:
                return isAmbiguous(((STBracedExpressionNode) expression).expression);
            default:
                return false;
        }
    }

    // ------------------------ Typed binding patterns ---------------------------

    /**
     * Parse binding-patterns.
     * <p>
     * <code>
     * binding-pattern := capture-binding-pattern
     *                      | wildcard-binding-pattern
     *                      | list-binding-pattern
     *                      | mapping-binding-pattern
     *                      | functional-binding-pattern
     * <br/><br/>
     *
     * capture-binding-pattern := variable-name
     * variable-name := identifier
     * <br/><br/>
     *
     * wildcard-binding-pattern := _
     * list-binding-pattern := [ list-member-binding-patterns ]
     * <br/>
     * list-member-binding-patterns := binding-pattern (, binding-pattern)* [, rest-binding-pattern]
     *                                 | [ rest-binding-pattern ]
     * <br/><br/>
     *
     * mapping-binding-pattern := { field-binding-patterns }
     * field-binding-patterns := field-binding-pattern (, field-binding-pattern)* [, rest-binding-pattern]
     *                          | [ rest-binding-pattern ]
     * <br/>
     * field-binding-pattern := field-name : binding-pattern | variable-name
     * <br/>
     * rest-binding-pattern := ... variable-name
     *
     * <br/><br/>
     * functional-binding-pattern := functionally-constructible-type-reference ( arg-list-binding-pattern )
     * <br/>
     * arg-list-binding-pattern := positional-arg-binding-patterns [, other-arg-binding-patterns]
     *                             | other-arg-binding-patterns
     * <br/>
     * positional-arg-binding-patterns := positional-arg-binding-pattern (, positional-arg-binding-pattern)*
     * <br/>
     * positional-arg-binding-pattern := binding-pattern
     * <br/>
     * other-arg-binding-patterns := named-arg-binding-patterns [, rest-binding-pattern]
     *                              | [rest-binding-pattern]
     * <br/>
     * named-arg-binding-patterns := named-arg-binding-pattern (, named-arg-binding-pattern)*
     * <br/>
     * named-arg-binding-pattern := arg-name = binding-pattern
     *</code>
     *
     * @return binding-pattern node
     */
    private STNode parseBindingPattern() {
        STToken token = peek();
        return parseBindingPattern(token.kind);
    }

    private STNode parseBindingPattern(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case OPEN_BRACKET_TOKEN:
                return parseListBindingPattern();
            case IDENTIFIER_TOKEN:
                return parseCaptureOrWildcardBindingPattern();
            case OPEN_BRACE_TOKEN:
                return parseMappingBindingPattern();
            default:
                Solution sol = recover(peek(), ParserRuleContext.BINDING_PATTERN);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (sol.action == Action.REMOVE) {
                    return sol.recoveredNode;
                }

                return parseBindingPattern(sol.tokenKind);
        }
    }

    /**
     * Parse capture-binding-pattern.
     * <p>
     * <code>
     * capture-binding-pattern := variable-name
     * <br/>
     * variable-name := identifier
     * </code>
     *
     * @return capture-binding-pattern node
     */
    private STNode parseCaptureOrWildcardBindingPattern() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STNode varName = parseVariableName();
            return createCaptureOrWildcardBP(varName);
        } else {
            Solution sol = recover(token, ParserRuleContext.CAPTURE_BINDING_PATTERN);
            if (sol.action == Action.REMOVE) {
                return sol.recoveredNode;
            }

            return STNodeFactory.createCaptureBindingPatternNode(sol.recoveredNode);
        }
    }

    private STNode createCaptureOrWildcardBP(STNode varName) {
        STNode bindingPattern;
        if (isWildcardBP(varName)) {
            bindingPattern = getWildcardBindingPattern(varName);
        } else {
            bindingPattern = STNodeFactory.createCaptureBindingPatternNode(varName);
        }
        return bindingPattern;
    }

    /**
     * Parse list-binding-patterns.
     * <p>
     * <code>
     * list-binding-pattern := [ list-member-binding-patterns ]
     * <br/>
     * list-member-binding-patterns := binding-pattern (, binding-pattern)* [, rest-binding-pattern]
     *                                | [ rest-binding-pattern ]
     * </code>
     *
     * @return list-binding-pattern node
     */
    private STNode parseListBindingPattern() {
        startContext(ParserRuleContext.LIST_BINDING_PATTERN);
        STNode openBracket = parseOpenBracket();
        List<STNode> bindingPatternsList = new ArrayList<>();
        STNode listBindingPattern = parseListBindingPattern(openBracket, bindingPatternsList);
        endContext();
        return listBindingPattern;
    }

    private STNode parseListBindingPattern(STNode openBracket, List<STNode> bindingPatternsList) {
        STNode listBindingPatternMember = parseListBindingPatternMember();
        bindingPatternsList.add(listBindingPatternMember);
        STNode listBindingPattern = parseListBindingPattern(openBracket, listBindingPatternMember, bindingPatternsList);
        return listBindingPattern;
    }

    private STNode parseListBindingPattern(STNode openBracket, STNode firstMember, List<STNode> bindingPatterns) {
        STNode member = firstMember;
        // parsing the main chunk of list-binding-pattern
        STToken token = peek(); // get next valid token
        STNode listBindingPatternRhs = null;
        while (!isEndOfListBindingPattern(token.kind) && member.kind != SyntaxKind.REST_BINDING_PATTERN) {
            listBindingPatternRhs = parseListBindingpatternRhs(token.kind);
            if (listBindingPatternRhs == null) {
                break;
            }

            bindingPatterns.add(listBindingPatternRhs);
            member = parseListBindingPatternMember();
            bindingPatterns.add(member);
            token = peek();
        }

        // separating out the rest-binding-pattern
        STNode restBindingPattern;
        if (member.kind == SyntaxKind.REST_BINDING_PATTERN) {
            restBindingPattern = bindingPatterns.remove(bindingPatterns.size() - 1);
        } else {
            restBindingPattern = STNodeFactory.createEmptyNode();
        }

        STNode closeBracket = parseCloseBracket();
        STNode bindingPatternsNode = STNodeFactory.createNodeList(bindingPatterns);
        return STNodeFactory.createListBindingPatternNode(openBracket, bindingPatternsNode, restBindingPattern,
                closeBracket);
    }

    private STNode parseListBindingpatternRhs() {
        return parseListBindingpatternRhs(peek().kind);
    }

    private STNode parseListBindingpatternRhs(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.LIST_BINDING_PATTERN_END_OR_CONTINUE);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseListBindingpatternRhs(solution.tokenKind);
        }
    }

    private boolean isEndOfListBindingPattern(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case CLOSE_BRACKET_TOKEN:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse list-binding-pattern entry.
     * <p>
     * <code>
     * list-binding-pattern := [ list-member-binding-patterns ]
     * <br/>
     * list-member-binding-patterns := binding-pattern (, binding-pattern)* [, rest-binding-pattern]
     *                                  | [ rest-binding-pattern ]
     * </code>
     *
     * @return rest-binding-pattern node
     */
    private STNode parseListBindingPatternMember() {
        STToken token = peek();
        switch (token.kind) {
            case ELLIPSIS_TOKEN:
                return parseRestBindingPattern();
            default:
                return parseBindingPattern();
        }
    }

    private STNode parseRestBindingPattern() {
        startContext(ParserRuleContext.REST_BINDING_PATTERN);
        STNode ellipsis = parseEllipsis();
        STNode varName = parseVariableName();
        endContext();
        return STNodeFactory.createRestBindingPatternNode(ellipsis, varName);
    }

    /**
     * Parse Typed-binding-pattern.
     * <p>
     * <code>
     * typed-binding-pattern := inferable-type-descriptor binding-pattern
     * <br/><br/>
     * inferable-type-descriptor := type-descriptor | var
     * </code>
     *
     * @return Typed binding pattern node
     */
    private STNode parseTypedBindingPattern(ParserRuleContext context) {
        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
        STNode typeBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, context);
        return typeBindingPattern;
    }

    /**
     * Parse mapping-binding-patterns.
     * <p>
     * <code>
     * mapping-binding-pattern := { field-binding-patterns }
     * <br/><br/>
     * field-binding-patterns := field-binding-pattern (, field-binding-pattern)* [, rest-binding-pattern]
     *                          | [ rest-binding-pattern ]
     * <br/><br/>
     * field-binding-pattern := field-name : binding-pattern | variable-name
     * </code>
     *
     * @return mapping-binding-pattern node
     */
    private STNode parseMappingBindingPattern() {
        startContext(ParserRuleContext.MAPPING_BINDING_PATTERN);
        STNode openBrace = parseOpenBrace();

        STToken token = peek();
        if (isEndOfMappingBindingPattern(token.kind)) {
            STNode closeBrace = parseCloseBrace();
            STNode bindingPatternsNode = STNodeFactory.createEmptyNodeList();
            STNode restBindingPattern = STNodeFactory.createEmptyNode();
            endContext();
            return STNodeFactory.createMappingBindingPatternNode(openBrace, bindingPatternsNode, restBindingPattern,
                    closeBrace);
        }

        List<STNode> bindingPatterns = new ArrayList<>();
        STNode prevMember = parseMappingBindingPatternMember();
        bindingPatterns.add(prevMember);
        return parseMappingBindingPattern(openBrace, bindingPatterns, prevMember);
    }

    private STNode parseMappingBindingPattern(STNode openBrace, List<STNode> bindingPatterns, STNode member) {
        STToken token = peek(); // get next valid token
        STNode mappingBindingPatternRhs = null;
        while (!isEndOfMappingBindingPattern(token.kind) && member.kind != SyntaxKind.REST_BINDING_PATTERN) {
            mappingBindingPatternRhs = parseMappingBindingpatternEnd(token.kind);
            if (mappingBindingPatternRhs == null) {
                break;
            }

            bindingPatterns.add(mappingBindingPatternRhs);
            member = parseMappingBindingPatternMember();
            if (member.kind == SyntaxKind.REST_BINDING_PATTERN) {
                break;
            }
            bindingPatterns.add(member);
            token = peek();
        }

        // Separating out the rest-binding-pattern
        STNode restBindingPattern;
        if (member.kind == SyntaxKind.REST_BINDING_PATTERN) {
            restBindingPattern = member;
        } else {
            restBindingPattern = STNodeFactory.createEmptyNode();
        }

        STNode closeBrace = parseCloseBrace();
        STNode bindingPatternsNode = STNodeFactory.createNodeList(bindingPatterns);
        endContext();
        return STNodeFactory.createMappingBindingPatternNode(openBrace, bindingPatternsNode, restBindingPattern,
                closeBrace);
    }

    /**
     * Parse mapping-binding-pattern entry.
     * <p>
     * <code>
     * mapping-binding-pattern := { field-binding-patterns }
     * <br/><br/>
     * field-binding-patterns := field-binding-pattern (, field-binding-pattern)* [, rest-binding-pattern]
     *                          | [ rest-binding-pattern ]
     * <br/><br/>
     * field-binding-pattern := field-name : binding-pattern
     *                          | variable-name
     * </code>
     *
     * @return mapping-binding-pattern node
     */
    private STNode parseMappingBindingPatternMember() {
        STToken token = peek();
        switch (token.kind) {
            case ELLIPSIS_TOKEN:
                return parseRestBindingPattern();
            default:
                return parseFieldBindingPattern();
        }
    }

    private STNode parseMappingBindingpatternEnd() {
        return parseMappingBindingpatternEnd(peek().kind);
    }

    private STNode parseMappingBindingpatternEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.MAPPING_BINDING_PATTERN_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseMappingBindingpatternEnd(solution.tokenKind);
        }
    }

    private STNode parseFieldBindingPattern() {
        return parseFieldBindingPattern(peek().kind);
    }

    /**
     * Parse field-binding-pattern.
     * <code>field-binding-pattern := field-name : binding-pattern | varname</code>
     *
     * @return field-binding-pattern node
     */
    private STNode parseFieldBindingPattern(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                STNode identifier = parseIdentifier(ParserRuleContext.FIELD_BINDING_PATTERN_NAME);
                STNode fieldBindingPattern = parseFieldBindingPattern(identifier);
                return fieldBindingPattern;
            default:
                Solution solution = recover(peek(), ParserRuleContext.FIELD_BINDING_PATTERN_NAME);

                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseFieldBindingPattern(solution.tokenKind);
        }
    }

    private STNode parseFieldBindingPattern(STNode identifier) {
        if (peek().kind != SyntaxKind.COLON_TOKEN) {
            return STNodeFactory.createFieldBindingPatternVarnameNode(identifier);
        }

        STNode colon = parseColon();
        STNode bindingPattern = parseBindingPattern();

        return STNodeFactory.createFieldBindingPatternFullNode(identifier, colon, bindingPattern);
    }

    private boolean isEndOfMappingBindingPattern(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case CLOSE_BRACE_TOKEN:
                return true;
            default:
                return false;
        }
    }

    // ------------------------ Typed binding patterns ---------------------------

    /*
     * This parses Typed binding patterns and deals with ambiguity between types,
     * and binding patterns. An example is 'T[a]'.
     * The ambiguity lies in between:
     * 1) Array Type
     * 2) List binding pattern
     * 3) Member access expression.
     */

    /**
     * Parse the component after the type-desc, of a typed-binding-pattern.
     *
     * @param typeDesc Starting type-desc of the typed-binding-pattern
     * @return Typed-binding pattern
     */
    private STNode parseTypedBindingPatternTypeRhs(STNode typeDesc, ParserRuleContext context) {
        return parseTypedBindingPatternTypeRhs(peek().kind, typeDesc, context);
    }

    private STNode parseTypedBindingPatternTypeRhs(SyntaxKind nextTokenKind, STNode typeDesc,
                                                   ParserRuleContext context) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN: // Capture/Functional binding pattern: T x, T(..)
            case OPEN_BRACE_TOKEN: // Map binding pattern: T { }
            case ERROR_KEYWORD: // Functional binding pattern: T error(..)
                STNode bindingPattern = parseBindingPattern(nextTokenKind);
                return STNodeFactory.createTypedBindingPatternNode(typeDesc, bindingPattern);
            case OPEN_BRACKET_TOKEN:
                // T[..] ..
                STNode typedBindingPattern = parseTypedBindingPatternOrMemberAccess(typeDesc, true, true, context);
                assert typedBindingPattern.kind == SyntaxKind.TYPED_BINDING_PATTERN;
                return typedBindingPattern;
            default:
                Solution solution =
                        recover(peek(), ParserRuleContext.TYPED_BINDING_PATTERN_TYPE_RHS, typeDesc, context);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTypedBindingPatternTypeRhs(solution.tokenKind, typeDesc, context);
        }
    }

    /**
     * Parse typed-binding pattern with list, array-type-desc, or member-access-expr.
     *
     * @param typeDescOrExpr Type desc or the expression at the start
     * @param isTypedBindingPattern Is this is a typed-binding-pattern.
     * @return Parsed node
     */
    private STNode parseTypedBindingPatternOrMemberAccess(STNode typeDescOrExpr, boolean isTypedBindingPattern,
                                                          boolean allowAssignment, ParserRuleContext context) {
        startContext(ParserRuleContext.BRACKETED_LIST);
        STNode openBracket = parseOpenBracket();

        // If the bracketed list is empty, i.e: T[] then T is an array-type-desc, and [] could be anything.
        if (isBracketedListEnd(peek().kind)) {
            return parseAsArrayTypeDesc(typeDescOrExpr, openBracket, STNodeFactory.createEmptyNode(), context);
        }

        // Parse first member
        STNode member = parseBracketedListMember(isTypedBindingPattern);
        SyntaxKind currentNodeType = getBracketedListNodeType(member);
        switch (currentNodeType) {
            case ARRAY_TYPE_DESC:
                STNode typedBindingPattern = parseAsArrayTypeDesc(typeDescOrExpr, openBracket, member, context);
                return typedBindingPattern;
            case LIST_BINDING_PATTERN:
                // If the member type was figured out as a binding pattern, then parse the
                // remaining members as binding patterns and be done with it.
                STNode bindingPattern = parseAsListBindingPattern(openBracket, new ArrayList<>(), member, false);
                STNode typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                return STNodeFactory.createTypedBindingPatternNode(typeDesc, bindingPattern);
            case INDEXED_EXPRESSION:
                return parseAsMemberAccessExpr(typeDescOrExpr, openBracket, member);
            case NONE:
            default:
                // Ideally we would reach here, only if the parsed member was a name-reference.
                // i.e: T[a
                break;
        }

        // Parse separator
        STNode memberEnd = parseBracketedListMemberEnd();
        if (memberEnd != null) {
            // If there are more than one member, then its definitely a binding pattern.
            List<STNode> memberList = new ArrayList<>();
            memberList.add(member);
            memberList.add(memberEnd);
            STNode bindingPattern = parseAsListBindingPattern(openBracket, memberList);
            STNode typeDesc = getTypeDescFromExpr(typeDescOrExpr);
            return STNodeFactory.createTypedBindingPatternNode(typeDesc, bindingPattern);
        }

        // We reach here if it is still ambiguous, even after parsing the full list.
        // That is: T[a]. This could be:
        // 1) Array Type Desc
        // 2) Member access on LHS
        // 3) Typed-binding-pattern
        STNode closeBracket = parseCloseBracket();
        endContext();
        return parseTypedBindingPatternOrMemberAccessRhs(typeDescOrExpr, openBracket, member, closeBracket,
                isTypedBindingPattern, allowAssignment, context);
    }

    private STNode parseAsMemberAccessExpr(STNode typeNameOrExpr, STNode openBracket, STNode member) {
        STNode closeBracket = parseCloseBracket();
        endContext();
        STNode keyExpr = STNodeFactory.createNodeList(member);
        STNode memberAccessExpr =
                STNodeFactory.createIndexedExpressionNode(typeNameOrExpr, openBracket, keyExpr, closeBracket);
        return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, memberAccessExpr, true, false);
    }

    private boolean isBracketedListEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseBracketedListMember(boolean isTypedBindingPattern) {
        return parseBracketedListMember(peek().kind, isTypedBindingPattern);
    }

    /**
     * Parse a member of an ambiguous bracketed list. This member could be:
     * 1) Array length
     * 2) Key expression of a member-access-expr
     * 3) A member-binding pattern of a list-binding-pattern.
     *
     * @param nextTokenKind Kind of the next token
     * @param isTypedBindingPattern Is this in a definite typed-binding pattern
     * @return Parsed member node
     */
    private STNode parseBracketedListMember(SyntaxKind nextTokenKind, boolean isTypedBindingPattern) {
        switch (nextTokenKind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case ASTERISK_TOKEN:
            case STRING_LITERAL:
                return parseBasicLiteral();
            case CLOSE_BRACKET_TOKEN:
                return STNodeFactory.createEmptyNode();
            case OPEN_BRACE_TOKEN:// mapping-binding-pattern
            case ERROR_KEYWORD: // functional-binding-pattern
            case ELLIPSIS_TOKEN: // rest binding pattern
                return parseListBindingPatternMember();
            case IDENTIFIER_TOKEN:
                if (isTypedBindingPattern) {
                    STNode identifier = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                    nextTokenKind = peek().kind;
                    if (nextTokenKind == SyntaxKind.OPEN_PAREN_TOKEN) {
                        // error|T (args) --> functional-binding-pattern
                        return parseListBindingPatternMember();
                    }

                    return identifier;
                }
                break;
            default:
                if (!isTypedBindingPattern && isValidExpressionStart(nextTokenKind, 1)) {
                    break;
                }

                ParserRuleContext recoverContext =
                        isTypedBindingPattern ? ParserRuleContext.LIST_BINDING_MEMBER_OR_ARRAY_LENGTH
                                : ParserRuleContext.BRACKETED_LIST_MEMBER;
                Solution solution = recover(peek(), recoverContext, isTypedBindingPattern);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseBracketedListMember(solution.tokenKind, isTypedBindingPattern);
        }

        STNode expr = parseExpression();
        if (isWildcardBP(expr)) {
            return getWildcardBindingPattern(expr);
        }
        if (expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE || expr.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            nextTokenKind = peek().kind;
            if (nextTokenKind == SyntaxKind.OPEN_PAREN_TOKEN) {
                // error|T (args) --> functional-binding-pattern
                return parseListBindingPatternMember();
            }
        }

        // we don't know which one
        return expr;
    }

    /**
     * Treat the current node as an array, and parse the remainder of the binding pattern.
     *
     * @param typeDesc Type-desc
     * @param openBracket Open bracket
     * @param member Member
     * @return Parsed node
     */
    private STNode parseAsArrayTypeDesc(STNode typeDesc, STNode openBracket, STNode member, ParserRuleContext context) {
        // In ambiguous scenarios typDesc: T[a] may have parsed as an indexed expression.
        // Therefore make an array-type-desc out of it.
        typeDesc = getTypeDescFromExpr(typeDesc);
        STNode closeBracket = parseCloseBracket();
        endContext();
        return parseTypedBindingPatternOrMemberAccessRhs(typeDesc, openBracket, member, closeBracket, true, true,
                context);
    }

    private STNode parseBracketedListMemberEnd() {
        return parseBracketedListMemberEnd(peek().kind);
    }

    private STNode parseBracketedListMemberEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                Solution solution = recover(peek(), ParserRuleContext.BRACKETED_LIST_MEMBER_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseBracketedListMemberEnd(solution.tokenKind);
        }
    }

    /**
     * We reach here to break ambiguity of T[a]. This could be:
     * 1) Array Type Desc
     * 2) Member access on LHS
     * 3) Typed-binding-pattern
     *
     * @param typeDescOrExpr Type name or the expr that precede the open-bracket.
     * @param openBracket Open bracket
     * @param member Member
     * @param closeBracket Open bracket
     * @param isTypedBindingPattern Is this is a typed-binding-pattern.
     * @return Specific node that matches to T[a], after solving ambiguity.
     */
    private STNode parseTypedBindingPatternOrMemberAccessRhs(STNode typeDescOrExpr, STNode openBracket, STNode member,
                                                             STNode closeBracket, boolean isTypedBindingPattern,
                                                             boolean allowAssignment, ParserRuleContext context) {
        STToken nextToken = peek();
        return parseTypedBindingPatternOrMemberAccessRhs(nextToken.kind, typeDescOrExpr, openBracket, member,
                closeBracket, isTypedBindingPattern, allowAssignment, context);
    }

    private STNode parseTypedBindingPatternOrMemberAccessRhs(SyntaxKind nextTokenKind, STNode typeDescOrExpr,
                                                             STNode openBracket, STNode member, STNode closeBracket,
                                                             boolean isTypedBindingPattern, boolean allowAssignment,
                                                             ParserRuleContext context) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN: // Capture binding pattern: T[a] b
            case OPEN_BRACE_TOKEN: // Map binding pattern: T[a] { }
            case ERROR_KEYWORD: // Functional binding pattern: T[a] error(..)
                // T[a] is definitely a type-desc.
                STNode typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                STNode arrayTypeDesc =
                        STNodeFactory.createArrayTypeDescriptorNode(typeDesc, openBracket, member, closeBracket);
                return parseTypedBindingPatternTypeRhs(arrayTypeDesc, context);
            case OPEN_BRACKET_TOKEN: // T[a][b]..
                if (isTypedBindingPattern) {
                    typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                    arrayTypeDesc =
                            STNodeFactory.createArrayTypeDescriptorNode(typeDesc, openBracket, member, closeBracket);
                    return parseTypedBindingPatternTypeRhs(arrayTypeDesc, context);
                }

                // T[a] could be member-access or array-type-desc.
                STNode keyExpr = STNodeFactory.createNodeList(member);
                STNode expr =
                        STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr, closeBracket);
                return parseTypedBindingPatternOrMemberAccess(expr, false, allowAssignment, context);
            case QUESTION_MARK_TOKEN:
                // T[a]? --> Treat T[a] as array type
                typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                arrayTypeDesc =
                        STNodeFactory.createArrayTypeDescriptorNode(typeDesc, openBracket, member, closeBracket);
                typeDesc = parseComplexTypeDescriptor(arrayTypeDesc,
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
                return parseTypedBindingPatternTypeRhs(typeDesc, context);
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
                // "T[a] | R.." or "T[a] & R.."
                return parseComplexTypeDescInTypedBindingPattern(typeDescOrExpr, openBracket, member, closeBracket,
                        context, isTypedBindingPattern);
            case IN_KEYWORD:
                // "in" keyword is only valid for for-each stmt.
                if (context != ParserRuleContext.FOREACH_STMT && context != ParserRuleContext.FROM_CLAUSE) {
                    break;
                }
                return createTypedBindingPattern(typeDescOrExpr, openBracket, member, closeBracket);
            case EQUAL_TOKEN: // T[a] =
                if (context == ParserRuleContext.FOREACH_STMT || context == ParserRuleContext.FROM_CLAUSE) {
                    // equal and semi-colon are not valid terminators for typed-binding-pattern
                    // in foreach-stmt and from-clause. Therefore recover.
                    break;
                }

                // could be member-access or typed-binding-pattern.
                if (isTypedBindingPattern || !allowAssignment || !isValidLVExpr(typeDescOrExpr)) {
                    return createTypedBindingPattern(typeDescOrExpr, openBracket, member, closeBracket);
                }

                keyExpr = STNodeFactory.createNodeList(member);
                typeDescOrExpr = getExpression(typeDescOrExpr);
                return STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr, closeBracket);
            case SEMICOLON_TOKEN: // T[a];
                if (context == ParserRuleContext.FOREACH_STMT || context == ParserRuleContext.FROM_CLAUSE) {
                    // equal and semi-colon are not valid terminators for typed-binding-pattern
                    // in foreach-stmt and from-clause. Therefore recover.
                    break;
                }

                return createTypedBindingPattern(typeDescOrExpr, openBracket, member, closeBracket);
            case CLOSE_BRACE_TOKEN: // T[a]}
            case COMMA_TOKEN:// T[a],
                if (context == ParserRuleContext.AMBIGUOUS_STMT) {
                    keyExpr = STNodeFactory.createNodeList(member);
                    return STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr,
                            closeBracket);
                }
                // fall through
            default:
                if (isValidExprRhsStart(nextTokenKind)) {
                    // We come here if T[a] is in some expression context.
                    keyExpr = STNodeFactory.createNodeList(member);
                    typeDescOrExpr = getExpression(typeDescOrExpr);
                    return STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr,
                            closeBracket);
                }

                break;
        }

        Solution solution = recover(peek(), ParserRuleContext.BRACKETED_LIST_RHS, typeDescOrExpr, openBracket, member,
                closeBracket, isTypedBindingPattern, allowAssignment, context);

        // If the parser recovered by inserting a token, then try to re-parse the same
        // rule with the inserted token. This is done to pick the correct branch
        // to continue the parsing.
        if (solution.action == Action.REMOVE) {
            return solution.recoveredNode;
        }

        return parseTypedBindingPatternOrMemberAccessRhs(solution.tokenKind, typeDescOrExpr, openBracket, member,
                closeBracket, isTypedBindingPattern, allowAssignment, context);
    }

    private STNode createTypedBindingPattern(STNode typeDescOrExpr, STNode openBracket, STNode member,
                                             STNode closeBracket) {
        STNode bindingPatterns;
        if (isEmpty(member)) {
            bindingPatterns = STNodeFactory.createEmptyNodeList();
        } else {
            STNode varName = ((STSimpleNameReferenceNode) member).name;
            STNode bindingPattern = createCaptureOrWildcardBP(varName);
            bindingPatterns = STNodeFactory.createNodeList(bindingPattern);
        }

        STNode restBindingPattern = STNodeFactory.createEmptyNode();
        STNode bindingPattern = STNodeFactory.createListBindingPatternNode(openBracket, bindingPatterns,
                restBindingPattern, closeBracket);
        STNode typeDesc = getTypeDescFromExpr(typeDescOrExpr);
        return STNodeFactory.createTypedBindingPatternNode(typeDesc, bindingPattern);
    }

    /**
     * Parse a union or intersection type-desc/binary-expression that involves ambiguous
     * bracketed list in lhs.
     * <p>
     * e.g: <code>(T[a] & R..)</code> or <code>(T[a] | R.. )</code>
     * <p>
     * Complexity occurs in scenarios such as <code>T[a] |/& R[b]</code>. If the token after this
     * is another binding-pattern, then <code>(T[a] |/& R[b])</code> becomes the type-desc. However,
     * if the token follows this is an equal or semicolon, then <code>(T[a] |/& R)</code> becomes
     * the type-desc, and <code>[b]</code> becomes the binding pattern.
     *
     * @param typeDescOrExpr Type desc or the expression
     * @param openBracket Open bracket
     * @param member Member
     * @param closeBracket Close bracket
     * @param context COntext in which the typed binding pattern occurs
     * @return Parsed node
     */
    private STNode parseComplexTypeDescInTypedBindingPattern(STNode typeDescOrExpr, STNode openBracket, STNode member,
                                                             STNode closeBracket, ParserRuleContext context,
                                                             boolean isTypedBindingPattern) {
        STNode pipeOrAndToken = parseUnionOrIntersectionToken();
        STNode typedBindingPatternOrExpr = parseTypedBindingPatternOrExpr(false);

        if (isTypedBindingPattern || typedBindingPatternOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
            // Treat T[a] as an array-type-desc. But we dont know what R is.
            // R could be R[b] or R[b, c]
            STNode lhsTypeDesc = getTypeDescFromExpr(typeDescOrExpr);
            lhsTypeDesc = STNodeFactory.createArrayTypeDescriptorNode(lhsTypeDesc, openBracket, member, closeBracket);

            STTypedBindingPatternNode rhsTypedBindingPattern = (STTypedBindingPatternNode) typedBindingPatternOrExpr;
            STNode newTypeDesc;
            if (pipeOrAndToken.kind == SyntaxKind.PIPE_TOKEN) {
                newTypeDesc = STNodeFactory.createUnionTypeDescriptorNode(lhsTypeDesc, pipeOrAndToken,
                        rhsTypedBindingPattern.typeDescriptor);
            } else {
                newTypeDesc = STNodeFactory.createIntersectionTypeDescriptorNode(lhsTypeDesc, pipeOrAndToken,
                        rhsTypedBindingPattern.typeDescriptor);
            }

            return STNodeFactory.createTypedBindingPatternNode(newTypeDesc, rhsTypedBindingPattern.bindingPattern);
        } else {
            STNode keyExpr = getExpression(member);
            STNode containerExpr = getExpression(typeDescOrExpr);
            STNode lhsExpr =
                    STNodeFactory.createIndexedExpressionNode(containerExpr, openBracket, keyExpr, closeBracket);
            return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, lhsExpr, pipeOrAndToken,
                    typedBindingPatternOrExpr);
        }
    }

    /**
     * Parse union (|) or intersection (&) type operator.
     *
     * @return pipe or bitwise and token
     */
    private STNode parseUnionOrIntersectionToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.PIPE_TOKEN || token.kind == SyntaxKind.BITWISE_AND_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.UNION_OR_INTERSECTION_TOKEN);
            return sol.recoveredNode;
        }
    }

    /**
     * Infer the type of the ambiguous bracketed list, based on the type of the member.
     *
     * @param memberNode Member node
     * @return Inferred type of the bracketed list
     */
    private SyntaxKind getBracketedListNodeType(STNode memberNode) {
        if (isEmpty(memberNode)) {
            // empty brackets. could be array-type or list-binding-pattern
            return SyntaxKind.NONE;
        }
        if (memberNode.kind.compareTo(SyntaxKind.TYPE_DESC) >= 0 &&
                memberNode.kind.compareTo(SyntaxKind.SINGLETON_TYPE_DESC) <= 0) {
            return SyntaxKind.TUPLE_TYPE_DESC;
        }

        switch (memberNode.kind) {
            case ASTERISK_TOKEN:
                return SyntaxKind.ARRAY_TYPE_DESC;
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return SyntaxKind.LIST_BINDING_PATTERN;
            case QUALIFIED_NAME_REFERENCE: // a qualified-name-ref can only be a type-ref
            case REST_TYPE:
                return SyntaxKind.TUPLE_TYPE_DESC;
            case DECIMAL_INTEGER_LITERAL: // member is a const expression. could be array-type or member-access
            case HEX_INTEGER_LITERAL:
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case BRACKETED_LIST: // member is again ambiguous
                return SyntaxKind.NONE;
            default:
                return SyntaxKind.INDEXED_EXPRESSION;
        }
    }

    // --------------------------------- Statements starts with open-bracket ---------------------------------

    /*
     * This section tries to break the ambiguity in parsing a statement that starts with a open-bracket.
     * The ambiguity lies in between:
     * 1) Assignment that starts with list binding pattern
     * 2) Var-decl statement that starts with tuple type
     * 3) Statement that starts with list constructor, such as sync-send, etc.
     */

    /**
     * Parse any statement that starts with an open-bracket.
     *
     * @param annots Annotations attached to the statement.
     * @return Parsed node
     */
    private STNode parseStatementStartsWithOpenBracket(STNode annots, boolean possibleMappingField) {
        startContext(ParserRuleContext.ASSIGNMENT_OR_VAR_DECL_STMT);
        return parseStatementStartsWithOpenBracket(annots, true, possibleMappingField);
    }

    private STNode parseMemberBracketedList(boolean possibleMappingField) {
        STNode annots = STNodeFactory.createEmptyNodeList();
        return parseStatementStartsWithOpenBracket(annots, false, possibleMappingField);
    }

    /**
     * The bracketed list at the start of a statement can be one of the following.
     * 1) List binding pattern
     * 2) Tuple type
     * 3) List constructor
     *
     * @param isRoot Is this the root of the list
     * @return Parsed node
     */
    private STNode parseStatementStartsWithOpenBracket(STNode annots, boolean isRoot, boolean possibleMappingField) {
        startContext(ParserRuleContext.STMT_START_BRACKETED_LIST);
        STNode openBracket = parseOpenBracket();
        List<STNode> memberList = new ArrayList<>();
        while (!isBracketedListEnd(peek().kind)) {
            // Parse member
            STNode member = parseStatementStartBracketedListMember();
            SyntaxKind currentNodeType = getStmtStartBracketedListType(member);

            switch (currentNodeType) {
                case TUPLE_TYPE_DESC:
                    // If the member type was figured out as a tuple-type-desc member, then parse the
                    // remaining members as tuple type members and be done with it.
                    return parseAsTupleTypeDesc(annots, openBracket, memberList, member, isRoot);
                case LIST_BINDING_PATTERN:
                    // If the member type was figured out as a binding pattern, then parse the
                    // remaining members as binding patterns and be done with it.
                    return parseAsListBindingPattern(openBracket, memberList, member, isRoot);
                case LIST_CONSTRUCTOR:
                    // If the member type was figured out as a list constructor, then parse the
                    // remaining members as list constructor members and be done with it.
                    return parseAsListConstructor(openBracket, memberList, member, isRoot);
                case LIST_BP_OR_LIST_CONSTRUCTOR:
                    return parseAsListBindingPatternOrListConstructor(openBracket, memberList, member, isRoot);
                case NONE:
                default:
                    memberList.add(member);
                    break;
            }

            // Parse separator
            STNode memberEnd = parseBracketedListMemberEnd();
            if (memberEnd == null) {
                break;
            }
            memberList.add(memberEnd);
        }

        // We reach here if it is still ambiguous, even after parsing the full list.
        STNode closeBracket = parseCloseBracket();
        STNode bracketedList = parseStatementStartBracketedList(annots, openBracket, memberList, closeBracket, isRoot,
                possibleMappingField);
        return bracketedList;
    }

    private STNode parseStatementStartBracketedListMember() {
        return parseStatementStartBracketedListMember(peek().kind);
    }

    /**
     * Parse a member of a list-binding-pattern, tuple-type-desc, or
     * list-constructor-expr, when the parent is ambiguous.
     *
     * @param nextTokenKind Kind of the next token.
     * @return Parsed node
     */
    private STNode parseStatementStartBracketedListMember(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case OPEN_BRACKET_TOKEN:
                return parseMemberBracketedList(false);
            case IDENTIFIER_TOKEN:
                STNode identifier = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                STNode varName = ((STSimpleNameReferenceNode) identifier).name;
                if (isWildcardBP(varName)) {
                    return getWildcardBindingPattern(varName);
                }

                nextTokenKind = peek().kind;
                if (nextTokenKind == SyntaxKind.ELLIPSIS_TOKEN) {
                    STNode ellipsis = parseEllipsis();
                    return STNodeFactory.createRestDescriptorNode(identifier, ellipsis);
                }

                // we don't know which one
                // TODO: handle function-binding-pattern
                // TODO handle & and |
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, identifier, false, true);
            case OPEN_BRACE_TOKEN:
                // mapping-binding-pattern
                return parseMappingBindingPatterOrMappingConstructor();
            case ERROR_KEYWORD:
                if (getNextNextToken(nextTokenKind).kind == SyntaxKind.OPEN_PAREN_TOKEN) {
                    return parseErrorConstructorExpr();
                }

                // error-type-desc
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case ELLIPSIS_TOKEN:
                return parseListBindingPatternMember();
            case XML_KEYWORD:
            case STRING_KEYWORD:
                if (getNextNextToken(nextTokenKind).kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseExpression(false);
                }
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
                if (getNextNextToken(nextTokenKind).kind == SyntaxKind.LT_TOKEN) {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }
                return parseExpression(false);
            default:
                if (isValidExpressionStart(nextTokenKind, 1)) {
                    return parseExpression(false);
                }

                if (isTypeStartingToken(nextTokenKind)) {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }

                Solution solution = recover(peek(), ParserRuleContext.STMT_START_BRACKETED_LIST_MEMBER);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseStatementStartBracketedListMember(solution.tokenKind);
        }
    }

    private STNode parseAsTupleTypeDesc(STNode annots, STNode openBracket, List<STNode> memberList, STNode member,
                                        boolean isRoot) {
        memberList = getTypeDescList(memberList);
        endContext();
        switchContext(ParserRuleContext.VAR_DECL_STMT);
        startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        startContext(ParserRuleContext.TYPE_DESC_IN_TUPLE);
        STNode tupleTypeMembers = parseTupleTypeMembers(member, memberList);
        STNode closeBracket = parseCloseBracket();
        endContext();

        STNode tupleType = STNodeFactory.createTupleTypeDescriptorNode(openBracket, tupleTypeMembers, closeBracket);
        STNode typeDesc =
                parseComplexTypeDescriptor(tupleType, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
        endContext();
        STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
        if (!isRoot) {
            return typedBindingPattern;
        }

        return parseVarDeclRhs(annots, STNodeFactory.createEmptyNode(), typedBindingPattern, false);
    }

    private STNode parseAsListBindingPattern(STNode openBracket, List<STNode> memberList, STNode member,
                                             boolean isRoot) {
        memberList = getBindingPatternsList(memberList);
        memberList.add(member);
        switchContext(ParserRuleContext.LIST_BINDING_PATTERN);
        STNode listBindingPattern = parseListBindingPattern(openBracket, member, memberList);
        endContext();
        if (!isRoot) {
            return listBindingPattern;
        }

        return parseAssignmentStmtRhs(listBindingPattern);
    }

    private STNode parseAsListBindingPattern(STNode openBracket, List<STNode> memberList) {
        memberList = getBindingPatternsList(memberList);
        switchContext(ParserRuleContext.LIST_BINDING_PATTERN);
        STNode listBindingPattern = parseListBindingPattern(openBracket, memberList);
        endContext();
        return listBindingPattern;
    }

    private STNode parseAsListBindingPatternOrListConstructor(STNode openBracket, List<STNode> memberList,
                                                              STNode member, boolean isRoot) {
        memberList.add(member);
        STNode memberEnd = parseBracketedListMemberEnd();

        STNode listBindingPatternOrListCons;
        if (memberEnd == null) {
            // We reach here if it is still ambiguous, even after parsing the full list.
            STNode closeBracket = parseCloseBracket();
            listBindingPatternOrListCons =
                    parseListBindingPatternOrListConstructor(openBracket, memberList, closeBracket, isRoot);
        } else {
            memberList.add(memberEnd);
            listBindingPatternOrListCons = parseListBindingPatternOrListConstructor(openBracket, memberList, isRoot);
        }

        return listBindingPatternOrListCons;
    }

    private SyntaxKind getStmtStartBracketedListType(STNode memberNode) {
        if (memberNode.kind.compareTo(SyntaxKind.TYPE_DESC) >= 0 &&
                memberNode.kind.compareTo(SyntaxKind.SINGLETON_TYPE_DESC) <= 0) {
            return SyntaxKind.TUPLE_TYPE_DESC;
        }

        switch (memberNode.kind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case ASTERISK_TOKEN:
                return SyntaxKind.ARRAY_TYPE_DESC;
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return SyntaxKind.LIST_BINDING_PATTERN;
            case QUALIFIED_NAME_REFERENCE: // a qualified-name-ref can only be a type-ref
            case REST_TYPE:
                return SyntaxKind.TUPLE_TYPE_DESC;
            case LIST_CONSTRUCTOR:
            case MAPPING_CONSTRUCTOR:
                return SyntaxKind.LIST_CONSTRUCTOR;
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                // can be either list-bp or list-constructor. Cannot be a tuple-type-desc
                return SyntaxKind.LIST_BP_OR_LIST_CONSTRUCTOR;
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case BRACKETED_LIST: // member is again ambiguous
            default:
                // can be anyof the three.
                return SyntaxKind.NONE;
        }
    }

    private STNode parseStatementStartBracketedList(STNode annots, STNode openBracket, List<STNode> members,
                                                    STNode closeBracket, boolean isRoot, boolean possibleMappingField) {
        switch (peek().kind) {
            case EQUAL_TOKEN:
                if (!isRoot) {
                    endContext();
                    return new STAmbiguousCollectionNode(SyntaxKind.BRACKETED_LIST, openBracket, members, closeBracket);
                }

                STNode memberBindingPatterns = STNodeFactory.createNodeList(getBindingPatternsList(members));
                STNode restBindingPattern = STNodeFactory.createEmptyNode();
                STNode listBindingPattern = STNodeFactory.createListBindingPatternNode(openBracket,
                        memberBindingPatterns, restBindingPattern, closeBracket);
                endContext(); // end tuple typ-desc

                switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                return parseAssignmentStmtRhs(listBindingPattern);
            case IDENTIFIER_TOKEN:
            case OPEN_BRACE_TOKEN:
                if (!isRoot) {
                    endContext();
                    return new STAmbiguousCollectionNode(SyntaxKind.BRACKETED_LIST, openBracket, members, closeBracket);
                }

                if (members.isEmpty()) {
                    this.errorHandler.reportMissingTokenError("missing member");
                }

                switchContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                startContext(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                STNode memberTypeDescs = STNodeFactory.createNodeList(getTypeDescList(members));
                STNode tupleTypeDesc =
                        STNodeFactory.createTupleTypeDescriptorNode(openBracket, memberTypeDescs, closeBracket);
                endContext(); // end tuple typ-desc
                STNode typeDesc = parseComplexTypeDescriptor(tupleTypeDesc,
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
                STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
                endContext(); // end binding pattern
                return parseStmtStartsWithTypedBPOrExprRhs(annots, typedBindingPattern);
            case OPEN_BRACKET_TOKEN:
                // [a, ..][..
                // definitely not binding pattern. Can be type-desc or list-constructor
                if (!isRoot) {
                    // if this is a member, treat as type-desc.
                    // TODO: handle expression case.
                    memberTypeDescs = STNodeFactory.createNodeList(getTypeDescList(members));
                    tupleTypeDesc =
                            STNodeFactory.createTupleTypeDescriptorNode(openBracket, memberTypeDescs, closeBracket);
                    endContext();
                    typeDesc = parseComplexTypeDescriptor(tupleTypeDesc, ParserRuleContext.TYPE_DESC_IN_TUPLE, false);
                    return typeDesc;
                }

                STAmbiguousCollectionNode list =
                        new STAmbiguousCollectionNode(SyntaxKind.BRACKETED_LIST, openBracket, members, closeBracket);
                endContext();
                STNode tpbOrExpr = parseTypedBindingPatternOrExprRhs(list, true);
                return parseStmtStartsWithTypedBPOrExprRhs(annots, tpbOrExpr);
            case COLON_TOKEN: // "{[a]:" could be a computed-name-field in mapping-constructor
                if (possibleMappingField && members.size() == 1) {
                    startContext(ParserRuleContext.MAPPING_CONSTRUCTOR);
                    STNode colon = parseColon();
                    STNode fieldNameExpr = getExpression(members.get(0));
                    STNode valueExpr = parseExpression();
                    return STNodeFactory.createComputedNameFieldNode(openBracket, fieldNameExpr, closeBracket, colon,
                            valueExpr);
                }

                // fall through
            default:
                if (!isRoot) {
                    endContext();
                    return new STAmbiguousCollectionNode(SyntaxKind.BRACKETED_LIST, openBracket, members, closeBracket);
                }

                STNode expressions = STNodeFactory.createNodeList(getExpressionList(members));
                STNode listConstructor =
                        STNodeFactory.createListConstructorExpressionNode(openBracket, expressions, closeBracket);
                endContext();
                return parseStmtStartsWithTypedBPOrExprRhs(annots, listConstructor);
        }
    }

    private boolean isWildcardBP(STNode node) {
        switch (node.kind) {
            case SIMPLE_NAME_REFERENCE:
                STToken nameToken = (STToken) ((STSimpleNameReferenceNode) node).name;
                return isUnderscoreToken(nameToken);
            case IDENTIFIER_TOKEN:
                return isUnderscoreToken((STToken) node);
            default:
                return false;

        }
    }

    private boolean isUnderscoreToken(STToken token) {
        return "_".equals(token.text());
    }

    private STNode getWildcardBindingPattern(STNode identifier) {
        switch (identifier.kind) {
            case SIMPLE_NAME_REFERENCE:
                STNode varName = ((STSimpleNameReferenceNode) identifier).name;
                return STNodeFactory.createWildcardBindingPatternNode(varName);
            case IDENTIFIER_TOKEN:
                return STNodeFactory.createWildcardBindingPatternNode(identifier);
            default:
                throw new IllegalStateException();
        }
    }

    // --------------------------------- Statements starts with open-brace ---------------------------------

    /*
     * This section tries to break the ambiguity in parsing a statement that starts with a open-brace.
     */

    /**
     * Parse statements that starts with open-brace. It could be a:
     * 1) Block statement
     * 2) Var-decl with mapping binding pattern.
     * 3) Statement that starts with mapping constructor expression.
     *
     * @return Parsed node
     */
    private STNode parseStatementStartsWithOpenBrace() {
        startContext(ParserRuleContext.AMBIGUOUS_STMT);
        STNode openBrace = parseOpenBrace();
        if (peek().kind == SyntaxKind.CLOSE_BRACE_TOKEN) {
            STNode closeBrace = parseCloseBrace();
            switch (peek().kind) {
                case EQUAL_TOKEN:
                    switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                    STNode fields = STNodeFactory.createEmptyNodeList();
                    STNode restBindingPattern = STNodeFactory.createEmptyNode();
                    STNode bindingPattern = STNodeFactory.createMappingBindingPatternNode(openBrace, fields,
                            restBindingPattern, closeBrace);
                    return parseAssignmentStmtRhs(bindingPattern);
                case RIGHT_ARROW_TOKEN:
                case SYNC_SEND_TOKEN:
                    switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                    fields = STNodeFactory.createEmptyNodeList();
                    STNode expr = STNodeFactory.createMappingConstructorExpressionNode(openBrace, fields, closeBrace);
                    expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, expr, false, true);
                    return parseStatementStartWithExprRhs(expr);
                default:
                    // else treat as block statement.
                    STNode statements = STNodeFactory.createEmptyNodeList();
                    endContext();
                    return STNodeFactory.createBlockStatementNode(openBrace, statements, closeBrace);
            }
        }

        STNode member = parseStatementStartingBracedListFirstMember();
        SyntaxKind nodeType = getBracedListType(member);
        STNode stmt;
        switch (nodeType) {
            case MAPPING_BINDING_PATTERN:
                return parseStmtAsMappingBindingPatternStart(openBrace, member);
            case MAPPING_CONSTRUCTOR:
                return parseStmtAsMappingConstructorStart(openBrace, member);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                return parseStmtAsMappingBPOrMappingConsStart(openBrace, member);
            case BLOCK_STATEMENT:
                STNode closeBrace = parseCloseBrace();
                stmt = STNodeFactory.createBlockStatementNode(openBrace, member, closeBrace);
                endContext();
                return stmt;
            default:
                // any statement comes here
                ArrayList<STNode> stmts = new ArrayList<>();
                stmts.add(member);
                STNode statements = parseStatements(stmts);
                closeBrace = parseCloseBrace();
                endContext();
                return STNodeFactory.createBlockStatementNode(openBrace, statements, closeBrace);
        }
    }

    /**
     * Parse the rest of the statement, treating the start as a mapping binding pattern.
     *
     * @param openBrace Open brace
     * @param firstMappingField First member
     * @return Parsed node
     */
    private STNode parseStmtAsMappingBindingPatternStart(STNode openBrace, STNode firstMappingField) {
        switchContext(ParserRuleContext.ASSIGNMENT_STMT);
        startContext(ParserRuleContext.MAPPING_BINDING_PATTERN);
        List<STNode> bindingPatterns = new ArrayList<>();
        if (firstMappingField.kind != SyntaxKind.REST_BINDING_PATTERN) {
            bindingPatterns.add(getBindingPattern(firstMappingField));
        }

        STNode mappingBP = parseMappingBindingPattern(openBrace, bindingPatterns, firstMappingField);
        return parseAssignmentStmtRhs(mappingBP);
    }

    /**
     * Parse the rest of the statement, treating the start as a mapping constructor expression.
     *
     * @param openBrace Open brace
     * @param firstMember First member
     * @return Parsed node
     */
    private STNode parseStmtAsMappingConstructorStart(STNode openBrace, STNode firstMember) {
        switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
        List<STNode> members = new ArrayList<>();
        STNode mappingCons = parseAsMappingConstructor(openBrace, members, firstMember);

        // Create the statement
        STNode expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, mappingCons, false, true);
        return parseStatementStartWithExprRhs(expr);
    }

    /**
     * Parse the braced-list as a mapping constructor expression.
     *
     * @param openBrace Open brace
     * @param members members list
     * @param member Most recently parsed member
     * @return Parsed node
     */
    private STNode parseAsMappingConstructor(STNode openBrace, List<STNode> members, STNode member) {
        members.add(member);
        members = getExpressionList(members);

        // create mapping constructor
        switchContext(ParserRuleContext.MAPPING_CONSTRUCTOR);
        STNode fields = parseMappingConstructorFields(members);
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createMappingConstructorExpressionNode(openBrace, fields, closeBrace);
    }

    /**
     * Parse the rest of the statement, treating the start as a mapping binding pattern
     * or a mapping constructor expression.
     *
     * @param openBrace Open brace
     * @param member First member
     * @return Parsed node
     */
    private STNode parseStmtAsMappingBPOrMappingConsStart(STNode openBrace, STNode member) {
        startContext(ParserRuleContext.MAPPING_BP_OR_MAPPING_CONSTRUCTOR);
        List<STNode> members = new ArrayList<>();
        members.add(member);

        STNode bpOrConstructor;
        STNode memberEnd = parseMappingFieldEnd();
        if (memberEnd == null) {
            STNode closeBrace = parseCloseBrace();
            // We reach here if it is still ambiguous, even after parsing the full list.
            bpOrConstructor = parseMappingBindingPatternOrMappingConstructor(openBrace, members, closeBrace);
        } else {
            members.add(memberEnd);
            bpOrConstructor = parseMappingBindingPatternOrMappingConstructor(openBrace, members);;
        }

        switch (bpOrConstructor.kind) {
            case MAPPING_CONSTRUCTOR:
                // Create the statement
                switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                STNode expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, bpOrConstructor, false, true);
                return parseStatementStartWithExprRhs(expr);
            case MAPPING_BINDING_PATTERN:
                switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                STNode bindingPattern = getBindingPattern(bpOrConstructor);
                return parseAssignmentStmtRhs(bindingPattern);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            default:
                // If this is followed by an assignment, then treat thsi node as mapping-binding pattern.
                if (peek().kind == SyntaxKind.EQUAL_TOKEN) {
                    switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                    bindingPattern = getBindingPattern(bpOrConstructor);
                    return parseAssignmentStmtRhs(bindingPattern);
                }

                // else treat as expression.
                switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                expr = getExpression(bpOrConstructor);
                expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, expr, false, true);
                return parseStatementStartWithExprRhs(expr);
        }
    }

    /**
     * Parse a member of a braced-list that occurs at the start of a statement.
     *
     * @return Parsed node
     */
    private STNode parseStatementStartingBracedListFirstMember() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case READONLY_KEYWORD:
                STNode readonlyKeyword = parseReadonlyKeyword();
                return bracedListMemberStartsWithReadonly(readonlyKeyword);
            case IDENTIFIER_TOKEN:
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseIdentifierRhsInStmtStartingBrace(readonlyKeyword);
            case STRING_LITERAL:
                STNode key = parseStringLiteral();
                if (peek().kind == SyntaxKind.COLON_TOKEN) {
                    readonlyKeyword = STNodeFactory.createEmptyNode();
                    STNode colon = parseColon();
                    STNode valueExpr = parseExpression();
                    return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, valueExpr);
                }

                STNode expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, key, false, true);
                return parseStatementStartWithExprRhs(expr);
            case OPEN_BRACKET_TOKEN:
                // [a] can be tuple type, list-bp, list-constructor or computed-field
                STNode annots = STNodeFactory.createEmptyNodeList();
                return parseStatementStartsWithOpenBracket(annots, true);
            case OPEN_BRACE_TOKEN:
                // Then treat parent as a block statement
                switchContext(ParserRuleContext.BLOCK_STMT);
                return parseStatementStartsWithOpenBrace();
            case ELLIPSIS_TOKEN:
                return parseRestBindingPattern();
            default:
                // Then treat parent as a block statement
                switchContext(ParserRuleContext.BLOCK_STMT);
                return parseStatements();
        }
    }

    private STNode bracedListMemberStartsWithReadonly(STNode readonlyKeyword) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                return parseIdentifierRhsInStmtStartingBrace(readonlyKeyword);
            case STRING_LITERAL:
                if (peek(2).kind == SyntaxKind.COLON_TOKEN) {
                    STNode key = parseStringLiteral();
                    STNode colon = parseColon();
                    STNode valueExpr = parseExpression();
                    return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, valueExpr);
                }
                // fall through
            default:
                // Then treat parent as a var-decl statement
                switchContext(ParserRuleContext.BLOCK_STMT);
                startContext(ParserRuleContext.VAR_DECL_STMT);
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                STNode typeDesc = parseComplexTypeDescriptor(readonlyKeyword,
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
                endContext();
                STNode metadata = STNodeFactory.createEmptyNode();
                STNode finalKeyword = STNodeFactory.createEmptyNode();
                STNode typedBP = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
                return parseVarDeclRhs(metadata, finalKeyword, typedBP, false);
        }
    }

    /**
     * Parse the rhs components of an identifier that follows an open brace,
     * at the start of a statement. i.e: "{foo".
     *
     * @param readonlyKeyword Readonly keyword
     * @return Parsed node
     */
    private STNode parseIdentifierRhsInStmtStartingBrace(STNode readonlyKeyword) {
        STNode identifier = parseIdentifier(ParserRuleContext.VARIABLE_REF);
        switch (peek().kind) {
            case COMMA_TOKEN: // { foo,
                // could be map literal or mapping-binding-pattern
                STNode colon = STNodeFactory.createEmptyNode();
                STNode value = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, value);
            case COLON_TOKEN:
                colon = parseColon();
                if (!isEmpty(readonlyKeyword)) {
                    value = parseExpression();
                    return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, value);
                }

                SyntaxKind nextTokenKind = peek().kind;
                switch (nextTokenKind) {
                    case OPEN_BRACKET_TOKEN: // { foo:[
                        STNode bindingPatternOrExpr = parseListBindingPatternOrListConstructor();
                        return getMappingField(identifier, colon, bindingPatternOrExpr);
                    case OPEN_BRACE_TOKEN: // { foo:{
                        bindingPatternOrExpr = parseMappingBindingPatterOrMappingConstructor();
                        return getMappingField(identifier, colon, bindingPatternOrExpr);
                    case IDENTIFIER_TOKEN: // { foo:bar
                        return parseQualifiedIdentifierRhsInStmtStartBrace(identifier, colon);
                    default:
                        STNode expr = parseExpression();
                        return getMappingField(identifier, colon, expr);
                }
            default:
                switchContext(ParserRuleContext.BLOCK_STMT);
                if (!isEmpty(readonlyKeyword)) {
                    startContext(ParserRuleContext.VAR_DECL_STMT);
                    STNode bindingPattern = STNodeFactory.createCaptureBindingPatternNode(identifier);
                    STNode typedBindingPattern =
                            STNodeFactory.createTypedBindingPatternNode(readonlyKeyword, bindingPattern);
                    STNode metadata = STNodeFactory.createEmptyNode();
                    STNode finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVarDeclRhs(metadata, finalKeyword, typedBindingPattern, false);
                }

                startContext(ParserRuleContext.AMBIGUOUS_STMT);
                STNode qualifiedIdentifier = parseQualifiedIdentifier(identifier);
                STNode expr = parseTypedBindingPatternOrExprRhs(qualifiedIdentifier, true);
                STNode annots = STNodeFactory.createEmptyNodeList();
                return parseStmtStartsWithTypedBPOrExprRhs(annots, expr);
        }
    }

    /**
     * Parse the rhs components of "<code>{ identifier : identifier</code>",
     * at the start of a statement. i.e: "{foo:bar".
     *
     * @return Parsed node
     */
    private STNode parseQualifiedIdentifierRhsInStmtStartBrace(STNode identifier, STNode colon) {
        STNode secondIdentifier = parseIdentifier(ParserRuleContext.VARIABLE_REF);
        STNode secondNameRef = STNodeFactory.createSimpleNameReferenceNode(secondIdentifier);
        if (isWildcardBP(secondIdentifier)) {
            // { foo:_
            return getWildcardBindingPattern(secondIdentifier);
        }

        // Reach here for something like: "{foo:bar". This could be anything.
        SyntaxKind nextTokenKind = peek().kind;
        STNode qualifiedNameRef = STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, secondNameRef);
        switch (nextTokenKind) {
            case COMMA_TOKEN:
                // {foo:bar, --> map-literal or binding pattern
                // Return a qualified-name-reference node since this is ambiguous. Downstream code
                // will convert this to the respective node, once the ambiguity is resolved.
                return qualifiedNameRef;
            case OPEN_BRACE_TOKEN: // { foo:bar{ --> var-decl with TBP
            case IDENTIFIER_TOKEN: // var-decl
                STNode finalKeyword = STNodeFactory.createEmptyNode();
                STNode typeBindingPattern =
                        parseTypedBindingPatternTypeRhs(qualifiedNameRef, ParserRuleContext.VAR_DECL_STMT);
                STNode annots = STNodeFactory.createEmptyNodeList();
                return parseVarDeclRhs(annots, finalKeyword, typeBindingPattern, false);
            case OPEN_BRACKET_TOKEN:
                // "{ foo:bar[" Can be (TBP) or (map-literal with member-access) or (statement starts with
                // member-access)
                return parseMemberRhsInStmtStartWithBrace(identifier, colon, secondNameRef);
            case QUESTION_MARK_TOKEN:
                // var-decl
                STNode typeDesc = parseComplexTypeDescriptor(qualifiedNameRef,
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
                finalKeyword = STNodeFactory.createEmptyNode();
                typeBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
                annots = STNodeFactory.createEmptyNodeList();
                return parseVarDeclRhs(annots, finalKeyword, typeBindingPattern, false);
            case EQUAL_TOKEN:
            case SEMICOLON_TOKEN:
                // stmt start with expr
                return parseStatementStartWithExprRhs(qualifiedNameRef);
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
            default:
                return parseMemberWithExprInRhs(identifier, colon, secondNameRef, secondNameRef);
        }
    }

    private SyntaxKind getBracedListType(STNode member) {
        switch (member.kind) {
            case FIELD_BINDING_PATTERN:
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return SyntaxKind.MAPPING_BINDING_PATTERN;
            case SPECIFIC_FIELD:
                STNode expr = ((STSpecificFieldNode) member).valueExpr;
                // "{foo," and "{foo:bar," is ambiguous
                if (expr == null || expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                        expr.kind == SyntaxKind.LIST_BP_OR_LIST_CONSTRUCTOR ||
                        expr.kind == SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR) {
                    return SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR;
                }
                return SyntaxKind.MAPPING_CONSTRUCTOR;
            case SPREAD_FIELD:
            case COMPUTED_NAME_FIELD:
                return SyntaxKind.MAPPING_CONSTRUCTOR;
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case LIST_BP_OR_LIST_CONSTRUCTOR:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            case REST_BINDING_PATTERN:// ambiguous with spread-field in mapping-constructor
                return SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR;
            case LIST:
                return SyntaxKind.BLOCK_STATEMENT;
            default:
                return SyntaxKind.NONE;
        }
    }

    /**
     * Parse mapping binding pattern or mapping constructor.
     *
     * @return Parsed node
     */
    private STNode parseMappingBindingPatterOrMappingConstructor() {
        startContext(ParserRuleContext.MAPPING_BP_OR_MAPPING_CONSTRUCTOR);
        STNode openBrace = parseOpenBrace();
        List<STNode> memberList = new ArrayList<>();
        return parseMappingBindingPatternOrMappingConstructor(openBrace, memberList);
    }

    private boolean isBracedListEnd(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseMappingBindingPatternOrMappingConstructor(STNode openBrace, List<STNode> memberList) {
        STToken nextToken = peek();
        while (!isBracedListEnd(nextToken.kind)) {
            // Parse member
            STNode member = parseMappingBindingPatterOrMappingConstructorMember(nextToken.kind);
            SyntaxKind currentNodeType = getTypeOfMappingBPOrMappingCons(member);

            switch (currentNodeType) {
                case MAPPING_CONSTRUCTOR:
                    // If the member type was figured out as a list constructor, then parse the
                    // remaining members as list constructor members and be done with it.
                    return parseAsMappingConstructor(openBrace, memberList, member);
                case MAPPING_BINDING_PATTERN:
                    // If the member type was figured out as a binding pattern, then parse the
                    // remaining members as binding patterns and be done with it.
                    return parseAsMappingBindingPattern(openBrace, memberList, member);
                case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                default:
                    memberList.add(member);
                    break;
            }

            // Parse separator
            STNode memberEnd = parseMappingFieldEnd();
            if (memberEnd == null) {
                break;
            }
            memberList.add(memberEnd);
            nextToken = peek();
        }

        // We reach here if it is still ambiguous, even after parsing the full list.
        STNode closeBrace = parseCloseBrace();
        return parseMappingBindingPatternOrMappingConstructor(openBrace, memberList, closeBrace);
    }

    private STNode parseMappingBindingPatterOrMappingConstructorMember(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                STNode key = parseIdentifier(ParserRuleContext.MAPPING_FIELD_NAME);
                return parseMappingFieldRhs(key);
            case STRING_LITERAL:
                STNode readonlyKeyword = STNodeFactory.createEmptyNode();
                key = parseStringLiteral();
                STNode colon = parseColon();
                STNode valueExpr = parseExpression();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, valueExpr);
            case OPEN_BRACKET_TOKEN:
                return parseComputedField();
            case ELLIPSIS_TOKEN:
                STNode ellipsis = parseEllipsis();
                STNode expr = parseExpression();
                if (expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    return STNodeFactory.createRestBindingPatternNode(ellipsis, expr);
                }
                return STNodeFactory.createSpreadFieldNode(ellipsis, expr);
            default:
                Solution solution = recover(peek(), ParserRuleContext.MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseListBindingPatternOrListConstructorMember(solution.tokenKind);
        }
    }

    private STNode parseMappingFieldRhs(STNode key) {
        STToken nextToken = peek();
        return parseMappingFieldRhs(nextToken.kind, key);
    }

    private STNode parseMappingFieldRhs(SyntaxKind tokenKind, STNode key) {
        STNode colon;
        STNode valueExpr;
        switch (tokenKind) {
            case COLON_TOKEN:
                colon = parseColon();
                return parseMappingFieldValue(key, colon);
            case COMMA_TOKEN:
            case CLOSE_BRACE_TOKEN:
                STNode readonlyKeyword = STNodeFactory.createEmptyNode();
                colon = STNodeFactory.createEmptyNode();
                valueExpr = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, valueExpr);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.SPECIFIC_FIELD_RHS, key);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseSpecificFieldRhs(solution.tokenKind, readonlyKeyword, key);
        }
    }

    private STNode parseMappingFieldValue(STNode key, STNode colon) {
        // {foo: ...
        STNode expr;
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                expr = parseExpression();
                break;
            case OPEN_BRACKET_TOKEN: // { foo:[
                expr = parseListBindingPatternOrListConstructor();
                break;
            case OPEN_BRACE_TOKEN: // { foo:{
                expr = parseMappingBindingPatterOrMappingConstructor();
                break;
            default:
                expr = parseExpression();
                break;
        }

        STNode readonlyKeyword = STNodeFactory.createEmptyNode();
        return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, expr);
    }

    private SyntaxKind getTypeOfMappingBPOrMappingCons(STNode memberNode) {
        switch (memberNode.kind) {
            case FIELD_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return SyntaxKind.MAPPING_BINDING_PATTERN;
            case SPECIFIC_FIELD:
                STNode expr = ((STSpecificFieldNode) memberNode).valueExpr;
                // "{foo," and "{foo:bar," is ambiguous
                if (expr == null || expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE ||
                        expr.kind == SyntaxKind.LIST_BP_OR_LIST_CONSTRUCTOR ||
                        expr.kind == SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR) {
                    return SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR;
                }
                return SyntaxKind.MAPPING_CONSTRUCTOR;
            case SPREAD_FIELD:
            case COMPUTED_NAME_FIELD:
                return SyntaxKind.MAPPING_CONSTRUCTOR;
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case LIST_BP_OR_LIST_CONSTRUCTOR:
            case REST_BINDING_PATTERN: // ambiguous with spread-field in mapping-constructor
            default:
                return SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR;
        }
    }

    private STNode parseMappingBindingPatternOrMappingConstructor(STNode openBrace, List<STNode> members,
                                                                  STNode closeBrace) {
        endContext();
        return new STAmbiguousCollectionNode(SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR, openBrace, members,
                closeBrace);
    }

    private STNode parseAsMappingBindingPattern(STNode openBrace, List<STNode> members, STNode member) {
        members.add(member);
        members = getBindingPatternsList(members);
        // create mapping binding pattern
        switchContext(ParserRuleContext.MAPPING_BINDING_PATTERN);
        return parseMappingBindingPattern(openBrace, members, member);
    }

    /**
     * Parse list binding pattern or list constructor.
     *
     * @return Parsed node
     */
    private STNode parseListBindingPatternOrListConstructor() {
        startContext(ParserRuleContext.BRACKETED_LIST);
        STNode openBracket = parseOpenBracket();
        List<STNode> memberList = new ArrayList<>();
        return parseListBindingPatternOrListConstructor(openBracket, memberList, false);
    }

    private STNode parseListBindingPatternOrListConstructor(STNode openBracket, List<STNode> memberList,
                                                            boolean isRoot) {
        // Parse the members
        STToken nextToken = peek();
        while (!isBracketedListEnd(nextToken.kind)) {
            // Parse member
            STNode member = parseListBindingPatternOrListConstructorMember(nextToken.kind);
            SyntaxKind currentNodeType = getParsingNodeTypeOfListBPOrListCons(member);

            switch (currentNodeType) {
                case LIST_CONSTRUCTOR:
                    // If the member type was figured out as a list constructor, then parse the
                    // remaining members as list constructor members and be done with it.
                    return parseAsListConstructor(openBracket, memberList, member, isRoot);
                case LIST_BINDING_PATTERN:
                    // If the member type was figured out as a binding pattern, then parse the
                    // remaining members as binding patterns and be done with it.
                    return parseAsListBindingPattern(openBracket, memberList, member, isRoot);
                case LIST_BP_OR_LIST_CONSTRUCTOR:
                default:
                    memberList.add(member);
                    break;
            }

            // Parse separator
            STNode memberEnd = parseBracketedListMemberEnd();
            if (memberEnd == null) {
                break;
            }
            memberList.add(memberEnd);
            nextToken = peek();
        }

        // We reach here if it is still ambiguous, even after parsing the full list.
        STNode closeBracket = parseCloseBracket();
        return parseListBindingPatternOrListConstructor(openBracket, memberList, closeBracket, isRoot);
    }

    private STNode parseListBindingPatternOrListConstructorMember(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case OPEN_BRACKET_TOKEN:
                // we don't know which one
                return parseListBindingPatternOrListConstructor();
            case IDENTIFIER_TOKEN:
                STNode identifier = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                if (isWildcardBP(identifier)) {
                    return getWildcardBindingPattern(identifier);
                }

                // TODO: handle function-binding-pattern
                // we don't know which one
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, identifier, false, false);
            case OPEN_BRACE_TOKEN:
                return parseMappingBindingPatterOrMappingConstructor();
            case ELLIPSIS_TOKEN:
                return parseListBindingPatternMember();
            default:
                if (isValidExpressionStart(nextTokenKind, 1)) {
                    return parseExpression();
                }

                Solution solution = recover(peek(), ParserRuleContext.LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseListBindingPatternOrListConstructorMember(solution.tokenKind);
        }
    }

    private SyntaxKind getParsingNodeTypeOfListBPOrListCons(STNode memberNode) {
        switch (memberNode.kind) {
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return SyntaxKind.LIST_BINDING_PATTERN;
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case LIST_BP_OR_LIST_CONSTRUCTOR: // member is again ambiguous
                return SyntaxKind.LIST_BP_OR_LIST_CONSTRUCTOR;
            default:
                return SyntaxKind.LIST_CONSTRUCTOR;
        }
    }

    private STNode parseAsListConstructor(STNode openBracket, List<STNode> memberList, STNode member, boolean isRoot) {
        memberList.add(member);
        memberList = getExpressionList(memberList);

        switchContext(ParserRuleContext.LIST_CONSTRUCTOR);
        STNode expressions = parseOptionalExpressionsList(memberList);
        STNode closeBracket = parseCloseBracket();
        STNode listConstructor =
                STNodeFactory.createListConstructorExpressionNode(openBracket, expressions, closeBracket);
        endContext();

        STNode expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, listConstructor, false, false);
        if (!isRoot) {
            return expr;
        }

        return parseStatementStartWithExprRhs(expr);

    }

    private STNode parseListBindingPatternOrListConstructor(STNode openBracket, List<STNode> members,
                                                            STNode closeBracket, boolean isRoot) {
        STNode lbpOrListCons;
        switch (peek().kind) {
            case COMMA_TOKEN: // [a, b, c],
            case CLOSE_BRACE_TOKEN: // [a, b, c]}
            case CLOSE_BRACKET_TOKEN:// [a, b, c]]
                if (!isRoot) {
                    endContext();
                    return new STAmbiguousCollectionNode(SyntaxKind.LIST_BP_OR_LIST_CONSTRUCTOR, openBracket, members,
                            closeBracket);
                }
                // fall through
            default:
                if (isValidExprRhsStart(peek().kind)) {
                    members = getExpressionList(members);
                    STNode memberExpressions = STNodeFactory.createNodeList(members);
                    lbpOrListCons = STNodeFactory.createListConstructorExpressionNode(openBracket, memberExpressions,
                            closeBracket);
                    break;
                }

                // Treat everything else as list-binding-pattern
                members = getBindingPatternsList(members);
                STNode bindingPatternsNode = STNodeFactory.createNodeList(members);
                STNode restBindingPattern = STNodeFactory.createEmptyNode();
                lbpOrListCons = STNodeFactory.createListBindingPatternNode(openBracket, bindingPatternsNode,
                        restBindingPattern, closeBracket);
                break;
        }

        endContext();

        if (!isRoot) {
            return lbpOrListCons;
        }

        return parseStmtStartsWithTypedBPOrExprRhs(null, lbpOrListCons);
    }

    private STNode parseMemberRhsInStmtStartWithBrace(STNode identifier, STNode colon, STNode secondIdentifier) {
        STNode typedBPOrExpr =
                parseTypedBindingPatternOrMemberAccess(secondIdentifier, false, true, ParserRuleContext.AMBIGUOUS_STMT);
        if (isExpression(typedBPOrExpr.kind)) {
            return parseMemberWithExprInRhs(identifier, colon, secondIdentifier, typedBPOrExpr);
        }

        switchContext(ParserRuleContext.BLOCK_STMT);
        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode finalKeyword = STNodeFactory.createEmptyNode();
        STNode annots = STNodeFactory.createEmptyNode();

        // We reach here for something like: "{ foo:bar[". But we started parsing the rhs component
        // starting with "bar". Hence if its a typed-binding-pattern, then merge the "foo:" with
        // the rest of the type-desc.
        STNode qualifiedNameRef = STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, secondIdentifier);
        STNode typeDesc = mergeQualifiedNameWithTypeDesc(qualifiedNameRef,
                ((STTypedBindingPatternNode) typedBPOrExpr).typeDescriptor);
        return parseVarDeclRhs(annots, finalKeyword, typeDesc, false);
    }

    /**
     * Parse a member that starts with "foo:bar[", in a statement starting with a brace.
     *
     * @param identifier First identifier of the statement
     * @param colon Colon that follows the first identifier
     * @param secondIdentifier Identifier that follows the colon
     * @param memberAccessExpr Member access expression
     * @return Parsed node
     */
    private STNode parseMemberWithExprInRhs(STNode identifier, STNode colon, STNode secondIdentifier,
                                            STNode memberAccessExpr) {
        STNode expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, memberAccessExpr, false, true);
        switch (peek().kind) {
            case COMMA_TOKEN:
            case CLOSE_BRACE_TOKEN:
                switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                startContext(ParserRuleContext.MAPPING_CONSTRUCTOR);
                STNode readonlyKeyword = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, expr);
            case EQUAL_TOKEN:
            case SEMICOLON_TOKEN:
            default:
                switchContext(ParserRuleContext.BLOCK_STMT);
                startContext(ParserRuleContext.EXPRESSION_STATEMENT);
                // stmt start with expr
                STNode qualifiedName =
                        STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, secondIdentifier);
                STNode updatedExpr = mergeQualifiedNameWithExpr(qualifiedName, expr);
                return parseStatementStartWithExprRhs(updatedExpr);
        }
    }

    /**
     * Replace the first identifier of an expression, with a given qualified-identifier.
     * Only expressions that can start with "bar[..]" can reach here.
     *
     * @param qualifiedName Qualified identifier to replace simple identifier
     * @param exprOrAction Expression or action
     * @return Updated expression
     */
    private STNode mergeQualifiedNameWithExpr(STNode qualifiedName, STNode exprOrAction) {
        switch (exprOrAction.kind) {
            case SIMPLE_NAME_REFERENCE:
                return qualifiedName;
            case BINARY_EXPRESSION:
                STBinaryExpressionNode binaryExpr = (STBinaryExpressionNode) exprOrAction;
                STNode newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, binaryExpr.lhsExpr);
                return STNodeFactory.createBinaryExpressionNode(binaryExpr.kind, newLhsExpr, binaryExpr.operator,
                        binaryExpr.rhsExpr);
            case FIELD_ACCESS:
                STFieldAccessExpressionNode fieldAccess = (STFieldAccessExpressionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, fieldAccess.expression);
                return STNodeFactory.createFieldAccessExpressionNode(newLhsExpr, fieldAccess.dotToken,
                        fieldAccess.fieldName);
            case INDEXED_EXPRESSION:
                STIndexedExpressionNode memberAccess = (STIndexedExpressionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, memberAccess.containerExpression);
                return STNodeFactory.createIndexedExpressionNode(newLhsExpr, memberAccess.openBracket,
                        memberAccess.keyExpression, memberAccess.closeBracket);
            case TYPE_TEST_EXPRESSION:
                STTypeTestExpressionNode typeTest = (STTypeTestExpressionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, typeTest.expression);
                return STNodeFactory.createTypeTestExpressionNode(newLhsExpr, typeTest.isKeyword,
                        typeTest.typeDescriptor);
            case ANNOT_ACCESS:
                STAnnotAccessExpressionNode annotAccess = (STAnnotAccessExpressionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, annotAccess.expression);
                return STNodeFactory.createFieldAccessExpressionNode(newLhsExpr, annotAccess.annotChainingToken,
                        annotAccess.annotTagReference);
            case OPTIONAL_FIELD_ACCESS:
                STOptionalFieldAccessExpressionNode optionalFieldAccess =
                        (STOptionalFieldAccessExpressionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, optionalFieldAccess.expression);
                return STNodeFactory.createFieldAccessExpressionNode(newLhsExpr,
                        optionalFieldAccess.optionalChainingToken, optionalFieldAccess.fieldName);
            case CONDITIONAL_EXPRESSION:
                STConditionalExpressionNode conditionalExpr = (STConditionalExpressionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, conditionalExpr.lhsExpression);
                return STNodeFactory.createConditionalExpressionNode(newLhsExpr, conditionalExpr.questionMarkToken,
                        conditionalExpr.middleExpression, conditionalExpr.colonToken, conditionalExpr.endExpression);
            case REMOTE_METHOD_CALL_ACTION:
                STRemoteMethodCallActionNode remoteCall = (STRemoteMethodCallActionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, remoteCall.expression);
                return STNodeFactory.createRemoteMethodCallActionNode(newLhsExpr, remoteCall.rightArrowToken,
                        remoteCall.methodName, remoteCall.openParenToken, remoteCall.arguments,
                        remoteCall.closeParenToken);
            case ASYNC_SEND_ACTION:
                STAsyncSendActionNode asyncSend = (STAsyncSendActionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, asyncSend.expression);
                return STNodeFactory.createAsyncSendActionNode(newLhsExpr, asyncSend.rightArrowToken,
                        asyncSend.peerWorker);
            case SYNC_SEND_ACTION:
                STSyncSendActionNode syncSend = (STSyncSendActionNode) exprOrAction;
                newLhsExpr = mergeQualifiedNameWithExpr(qualifiedName, syncSend.expression);
                return STNodeFactory.createAsyncSendActionNode(newLhsExpr, syncSend.syncSendToken, syncSend.peerWorker);
            default:
                return exprOrAction;
        }
    }

    private STNode mergeQualifiedNameWithTypeDesc(STNode qualifiedName, STNode typeDesc) {
        switch (typeDesc.kind) {
            case SIMPLE_NAME_REFERENCE:
                return qualifiedName;
            case ARRAY_TYPE_DESC:
                STArrayTypeDescriptorNode arrayTypeDesc = (STArrayTypeDescriptorNode) typeDesc;
                STNode newMemberType = mergeQualifiedNameWithTypeDesc(qualifiedName, arrayTypeDesc.memberTypeDesc);
                return STNodeFactory.createArrayTypeDescriptorNode(newMemberType, arrayTypeDesc.openBracket,
                        arrayTypeDesc.arrayLength, arrayTypeDesc.closeBracket);
            case UNION_TYPE_DESC:
                STUnionTypeDescriptorNode unionTypeDesc = (STUnionTypeDescriptorNode) typeDesc;
                STNode newlhsType = mergeQualifiedNameWithTypeDesc(qualifiedName, unionTypeDesc.leftTypeDesc);
                return STNodeFactory.createUnionTypeDescriptorNode(newlhsType, unionTypeDesc.pipeToken,
                        unionTypeDesc.rightTypeDesc);
            case INTERSECTION_TYPE_DESC:
                STIntersectionTypeDescriptorNode intersectionTypeDesc = (STIntersectionTypeDescriptorNode) typeDesc;
                newlhsType = mergeQualifiedNameWithTypeDesc(qualifiedName, intersectionTypeDesc.leftTypeDesc);
                return STNodeFactory.createUnionTypeDescriptorNode(newlhsType, intersectionTypeDesc.bitwiseAndToken,
                        intersectionTypeDesc.rightTypeDesc);
            case OPTIONAL_TYPE_DESC:
                STOptionalTypeDescriptorNode optionalType = (STOptionalTypeDescriptorNode) typeDesc;
                newMemberType = mergeQualifiedNameWithTypeDesc(qualifiedName, optionalType.typeDescriptor);
                return STNodeFactory.createOptionalTypeDescriptorNode(newMemberType, optionalType.questionMarkToken);
            default:
                return typeDesc;
        }
    }

    // ---------------------- Convert ambiguous nodes to a specific node --------------------------

    private List<STNode> getTypeDescList(List<STNode> ambiguousList) {
        List<STNode> typeDescList = new ArrayList();
        for (STNode item : ambiguousList) {
            typeDescList.add(getTypeDescFromExpr(item));
        }

        return typeDescList;
    }

    /**
     * Create a type-desc out of an expression.
     *
     * @param expression Expression
     * @return Type descriptor
     */
    private STNode getTypeDescFromExpr(STNode expression) {
        switch (expression.kind) {
            case INDEXED_EXPRESSION:
                STIndexedExpressionNode indexedExpr = (STIndexedExpressionNode) expression;
                STNode memberTypeDesc = getTypeDescFromExpr(indexedExpr.containerExpression);
                STNode arrayLength = getArrayLength((STNodeList) indexedExpr.keyExpression);
                return STNodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, indexedExpr.openBracket, arrayLength,
                        indexedExpr.closeBracket);
            case BASIC_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return STNodeFactory.createSingletonTypeDescriptorNode(expression);
            case TYPE_REFERENCE_TYPE_DESC:
                // TODO: this is a temporary workaround
                return ((STTypeReferenceTypeDescNode) expression).typeRef;
            case BRACED_EXPRESSION:
                STBracedExpressionNode bracedExpr = (STBracedExpressionNode) expression;
                STNode typeDesc = getTypeDescFromExpr(bracedExpr.expression);
                return STNodeFactory.createParenthesisedTypeDescriptorNode(bracedExpr.openParen, typeDesc,
                        bracedExpr.closeParen);
            case NIL_LITERAL:
                STNilLiteralNode nilLiteral = (STNilLiteralNode) expression;
                return STNodeFactory.createNilTypeDescriptorNode(nilLiteral.openParenToken, nilLiteral.closeParenToken);
            case BRACKETED_LIST:
            case LIST_BP_OR_LIST_CONSTRUCTOR:
                STAmbiguousCollectionNode innerList = (STAmbiguousCollectionNode) expression;
                STNode memberTypeDescs = STNodeFactory.createNodeList(getTypeDescList(innerList.members));
                return STNodeFactory.createTupleTypeDescriptorNode(innerList.collectionStartToken, memberTypeDescs,
                        innerList.collectionEndToken);
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            default:
                return expression;
        }
    }

    private List<STNode> getBindingPatternsList(List<STNode> ambibuousList) {
        List<STNode> bindingPatterns = new ArrayList<STNode>();
        for (STNode item : ambibuousList) {
            bindingPatterns.add(getBindingPattern(item));
        }
        return bindingPatterns;
    }

    private STNode getBindingPattern(STNode ambiguousNode) {
        if (isEmpty(ambiguousNode)) {
            return ambiguousNode;
        }

        switch (ambiguousNode.kind) {
            case SIMPLE_NAME_REFERENCE:
                STNode varName = ((STSimpleNameReferenceNode) ambiguousNode).name;
                return createCaptureOrWildcardBP(varName);
            case QUALIFIED_NAME_REFERENCE:
                STQualifiedNameReferenceNode qualifiedName = (STQualifiedNameReferenceNode) ambiguousNode;
                return STNodeFactory.createFieldBindingPatternFullNode(qualifiedName.modulePrefix, qualifiedName.colon,
                        getBindingPattern(qualifiedName.identifier));
            case BRACKETED_LIST:
            case LIST_BP_OR_LIST_CONSTRUCTOR:
                STAmbiguousCollectionNode innerList = (STAmbiguousCollectionNode) ambiguousNode;
                STNode memberBindingPatterns = STNodeFactory.createNodeList(getBindingPatternsList(innerList.members));
                STNode restBindingPattern = STNodeFactory.createEmptyNode();
                return STNodeFactory.createListBindingPatternNode(innerList.collectionStartToken, memberBindingPatterns,
                        restBindingPattern, innerList.collectionEndToken);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                innerList = (STAmbiguousCollectionNode) ambiguousNode;
                memberBindingPatterns = STNodeFactory.createNodeList(getBindingPatternsList(innerList.members));
                restBindingPattern = STNodeFactory.createEmptyNode();
                return STNodeFactory.createMappingBindingPatternNode(innerList.collectionStartToken,
                        memberBindingPatterns, restBindingPattern, innerList.collectionEndToken);
            case SPECIFIC_FIELD:
                STSpecificFieldNode field = (STSpecificFieldNode) ambiguousNode;
                return STNodeFactory.createFieldBindingPatternFullNode(field.fieldName, field.colon,
                        getBindingPattern(field.valueExpr));
            default:
                return ambiguousNode;
        }
    }

    private List<STNode> getExpressionList(List<STNode> ambibuousList) {
        List<STNode> exprList = new ArrayList<STNode>();
        for (STNode item : ambibuousList) {
            exprList.add(getExpression(item));
        }
        return exprList;
    }

    private STNode getExpression(STNode ambiguousNode) {
        if (isEmpty(ambiguousNode)) {
            return ambiguousNode;
        }

        switch (ambiguousNode.kind) {
            case BRACKETED_LIST:
            case LIST_BP_OR_LIST_CONSTRUCTOR:
                STAmbiguousCollectionNode innerList = (STAmbiguousCollectionNode) ambiguousNode;
                STNode memberExprs = STNodeFactory.createNodeList(getExpressionList(innerList.members));
                return STNodeFactory.createListConstructorExpressionNode(innerList.collectionStartToken, memberExprs,
                        innerList.collectionEndToken);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                innerList = (STAmbiguousCollectionNode) ambiguousNode;
                memberExprs = STNodeFactory.createNodeList(getExpressionList(innerList.members));
                return STNodeFactory.createMappingConstructorExpressionNode(innerList.collectionStartToken, memberExprs,
                        innerList.collectionEndToken);
            case REST_BINDING_PATTERN:
                STRestBindingPatternNode restBindingPattern = (STRestBindingPatternNode) ambiguousNode;
                return STNodeFactory.createSpreadFieldNode(restBindingPattern.ellipsisToken,
                        restBindingPattern.variableName);
            case SPECIFIC_FIELD:
                // Specific field is used to represent ambiguous scenarios. Hence it needs to be re-written.
                STSpecificFieldNode field = (STSpecificFieldNode) ambiguousNode;
                return STNodeFactory.createSpecificFieldNode(field.readonlyKeyword, field.fieldName, field.colon,
                        getExpression(field.valueExpr));
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            default:
                return ambiguousNode;
        }
    }

    private STNode getMappingField(STNode identifier, STNode colon, STNode bindingPatternOrExpr) {
        STNode simpleNameRef = STNodeFactory.createSimpleNameReferenceNode(identifier);
        switch (bindingPatternOrExpr.kind) {
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
                return STNodeFactory.createFieldBindingPatternFullNode(simpleNameRef, colon, bindingPatternOrExpr);
            case LIST_CONSTRUCTOR:
            case MAPPING_CONSTRUCTOR:
                STNode readonlyKeyword = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, simpleNameRef, colon, identifier);
            case LIST_BP_OR_LIST_CONSTRUCTOR:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            default:
                // If ambiguous, return an specific node, since it is used to represent any
                // ambiguous mapping field
                switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                startContext(ParserRuleContext.MAPPING_CONSTRUCTOR);
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, bindingPatternOrExpr);
        }
    }

    // ----------------------------------------- ~ End of Parser ~ ----------------------------------------

    // NOTE: Please add any new methods to the relevant section of the class. Binding patterns related code is the
    // last section of the class.
}
