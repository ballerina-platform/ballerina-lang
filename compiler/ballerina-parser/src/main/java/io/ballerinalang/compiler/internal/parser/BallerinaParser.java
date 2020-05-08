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

import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Action;
import io.ballerinalang.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerinalang.compiler.internal.parser.tree.STBracedExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STCheckExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STDefaultableParameterNode;
import io.ballerinalang.compiler.internal.parser.tree.STFieldAccessExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STFunctionSignatureNode;
import io.ballerinalang.compiler.internal.parser.tree.STIndexedExpressionNode;
import io.ballerinalang.compiler.internal.parser.tree.STMissingToken;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;
import io.ballerinalang.compiler.internal.parser.tree.STRequiredParameterNode;
import io.ballerinalang.compiler.internal.parser.tree.STRestParameterNode;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
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
                return parseFunctionBody();
            case OPEN_BRACE:
                return parseOpenBrace();
            case CLOSE_BRACE:
                return parseCloseBrace();
            case FUNC_NAME:
                return parseFunctionName();
            case OPEN_PARENTHESIS:
                return parseOpenParenthesis();
            // case RETURN_TYPE_DESCRIPTOR:
            // return parseFuncReturnTypeDescriptor();
            case SIMPLE_TYPE_DESCRIPTOR:
                return parseSimpleTypeDescriptor();
            case ASSIGN_OP:
                return parseAssignOp();
            case EXTERNAL_KEYWORD:
                return parseExternalKeyword();
            case FUNC_BODY_BLOCK:
                return parseFunctionBodyBlock();
            case SEMICOLON:
                return parseSemicolon();
            case CLOSE_PARENTHESIS:
                return parseCloseParenthesis();
            case VARIABLE_NAME:
                return parseVariableName();
            case TERMINAL_EXPRESSION:
                return parseTerminalExpression((boolean) args[0], (boolean) args[1]);
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
            case STATEMENT_START_IDENTIFIER:
                return parseStatementStartIdentifier();
            case VAR_DECL_STMT_RHS:
                return parseVarDeclRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (STNode) args[3],
                        (boolean) args[4]);
            case TYPE_REFERENCE:
                return parseTypeReference();
            case FIELD_DESCRIPTOR_RHS:
                return parseFieldDescriptorRhs((STNode) args[0], (STNode) args[1], (STNode) args[2]);
            case NAMED_OR_POSITIONAL_ARG_RHS:
                return parseNamedOrPositionalArg((STNode) args[0]);
            case RECORD_BODY_START:
                return parseRecordBodyStartDelimiter();
            case TYPE_DESCRIPTOR:
                return parseTypeDescriptorInternal((ParserRuleContext) args[0]);
            case OBJECT_MEMBER:
                return parseObjectMember();
            case OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY:
                return parseObjectMethodOrField((STNode) args[0], (STNode) args[1]);
            case OBJECT_FIELD_RHS:
                return parseObjectFieldRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (STNode) args[3]);
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
            case FIELD_OR_FUNC_NAME:
            case SERVICE_NAME:
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
                return parseMappingField((STNode) args[0]);
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
                return parseStamentStartWithExpr((STNode) args[0], (STNode) args[1]);
            case COMMA:
                return parseComma();
            case CONST_DECL_TYPE:
                return parseConstDecl((STNode) args[0], (STNode) args[1], (STNode) args[2]);
            case STMT_START_WITH_IDENTIFIER:
                return parseStatementStartsWithIdentifier((STNode) args[0], (STNode) args[1]);
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
                return parseXMLDeclRhs((STNode) args[0], (STNode) args[1]);
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
                return parseTableConstructorExpr((STNode) args[0], (STNode) args[1]);
            case ERROR_KEYWORD:
                return parseErrorKeyWord();
            case LET_KEYWORD:
                return parseLetKeyword();
            case STREAM_KEYWORD:
                return parseStreamKeyWord();
            case STREAM_TYPE_FIRST_PARAM_RHS:
                return parseStreamTypeParamsNode((STNode) args[0], (STNode) args[1]);
            case TEMPLATE_START:
            case TEMPLATE_END:
                return parseBacktickToken(context);
            case FUNCTION_KEYWORD_RHS:
                return parseFunctionKeywordRhs((STNode) args[0], (STNode) args[1], (STNode) args[2], (boolean) args[3]);
            case FUNC_OPTIONAL_RETURNS:
                return parseFuncReturnTypeDescriptor((boolean) args[0]);
            case RETURNS_KEYWORD:
                return parseReturnsKeyword();
            default:
                throw new IllegalStateException("cannot resume parsing the rule: " + context);
        }
    }

    /*
     * Private methods
     */

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
                // TODO: add type binding pattern
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
                    metadata = STNodeFactory.createNodeList(new ArrayList<>());
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
     * @param tokenKind Next token kind
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
                return consume();
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
                // TODO: add type binding pattern
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
                // Scenario foo| (Union type descriptor with custom type)
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
                return parseFuncDefOrFuncTypeDesc(metadata, getQualifier(qualifier));
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
                return parseXMLNamepsaceDeclaration();

            // TODO: add all 'type starting tokens' here. should be same as 'parseTypeDescriptor(...)'
            // TODO: add type binding pattern
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

    private STNode parseFuncDefinition(STNode metadata, STNode visibilityQualifier) {
        startContext(ParserRuleContext.FUNC_DEF);
        STNode functionKeyword = parseFunctionKeyword();
        return parseFunctionKeywordRhs(metadata, visibilityQualifier, functionKeyword, true);
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
    private STNode parseFuncDefOrFuncTypeDesc(STNode metadata, STNode visibilityQualifier) {
        startContext(ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE);
        STNode functionKeyword = parseFunctionKeyword();
        return parseFunctionKeywordRhs(metadata, visibilityQualifier, functionKeyword, false);
    }

    private STNode parseFunctionKeywordRhs(STNode metadata, STNode visibilityQualifier, STNode functionKeyword,
                                           boolean isFuncDef) {
        return parseFunctionKeywordRhs(peek().kind, metadata, visibilityQualifier, functionKeyword, isFuncDef);
    }

    private STNode parseFunctionKeywordRhs(SyntaxKind nextTokenKind, STNode metadata, STNode visibilityQualifier,
                                           STNode functionKeyword, boolean isFuncDef) {
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
                        visibilityQualifier, functionKeyword, isFuncDef);

                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }
                return parseFunctionKeywordRhs(solution.tokenKind, metadata, visibilityQualifier, functionKeyword,
                        isFuncDef);
        }

        // If the function name is present, treat this as a function def
        if (isFuncDef) {
            switchContext(ParserRuleContext.FUNC_DEF);
            STNode funcSignature = parseFuncSignature(false, false);
            STNode body = parseFunctionBody();
            return STNodeFactory.createFunctionDefinitionNode(metadata, visibilityQualifier, functionKeyword, name,
                    funcSignature, body);
        }

        // Otherwise it could be a func-def or a func-type
        STNode funcSignature = parseFuncSignature(true, false);
        return parseReturnTypeDescRhs(metadata, visibilityQualifier, functionKeyword, funcSignature);
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
    private STNode parseFuncSignature(boolean isParamNameOptional, boolean isInExprContext) {
        STNode openParenthesis = parseOpenParenthesis();
        STNode parameters = parseParamList(isParamNameOptional);
        STNode closeParenthesis = parseCloseParenthesis();
        endContext(); // end param-list
        STNode returnTypeDesc = parseFuncReturnTypeDescriptor(isInExprContext);
        endContext(); // end func-signature
        return STNodeFactory.createFunctionSignatureNode(openParenthesis, parameters, closeParenthesis, returnTypeDesc);
    }

    private STNode parseReturnTypeDescRhs(STNode metadata, STNode visibilityQualifier, STNode functionKeyword,
                                          STNode funcSignature) {
        switch (peek().kind) {
            // TODO: add binding-patterns

            // var-decl with function type
            case SEMICOLON_TOKEN:
            case IDENTIFIER_TOKEN:
                // Parse the remaining as var-decl, because its the only module-level construct
                // that can start with a func-type-desc. Constants cannot have func-type-desc.
                startContext(ParserRuleContext.VAR_DECL_STMT);
                STNode typeDesc = STNodeFactory.createFunctionTypeDescriptorNode(functionKeyword, funcSignature);
                STNode varName = parseVariableName();
                STNode varDecl = parseVarDeclRhs(metadata, visibilityQualifier, typeDesc, varName, true);
                endContext();
                return varDecl;
            case OPEN_PAREN_TOKEN: // function body block
            case EQUAL_TOKEN: // external function
                break;
            default:
                break;
        }

        // Treat as function definition.

        // We reach this method only if the func-name is not present.
        this.errorHandler.reportMissingTokenError("missing " + ParserRuleContext.FUNC_NAME);
        STNode name = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);

        // Function definition cannot have missing param-names. So validate it.
        funcSignature = validateAndGetFuncParams((STFunctionSignatureNode) funcSignature);

        STNode body = parseFunctionBody();
        return STNodeFactory.createFunctionDefinitionNode(metadata, visibilityQualifier, functionKeyword, name,
                funcSignature, body);
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
     * @return Parsed node
     */
    private STNode parseOpenParenthesis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.OPEN_PARENTHESIS);
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
        ArrayList<STNode> paramsList = new ArrayList<>();

        STToken token = peek();
        if (isEndOfParametersList(token.kind)) {
            STNode params = STNodeFactory.createNodeList(paramsList);
            return params;
        }

        // Parse the first parameter. Comma precedes the first parameter doesn't exist.
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

            STNode leadingComma = parseComma();
            STNode param = parseParameter(leadingComma, prevParamKind, isParamNameOptional);
            prevParamKind = param.kind;
            paramsList.add(param);
            token = peek();
        }

        STNode params = STNodeFactory.createNodeList(paramsList);
        return params;
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
                annots = STNodeFactory.createNodeList(new ArrayList<>());
                break;
            default:
                // TODO: can remove the first check?
                if (nextTokenKind != SyntaxKind.IDENTIFIER_TOKEN && isTypeStartingToken(nextTokenKind)) {
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
                    annots = STNodeFactory.createNodeList(new ArrayList<>());
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
     * Check whether the given token is an end of a parameter.
     *
     * @param tokenKind Next token kind
     * @return <code>true</code> if the token represents an end of a parameter. <code>false</code> otherwise
     */
    private boolean isEndOfParameter(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case COMMA_TOKEN:
            case PUBLIC_KEYWORD:
            case RETURNS_KEYWORD:
            case TYPE_KEYWORD:
            case LISTENER_KEYWORD:
            case IF_KEYWORD:
            case WHILE_KEYWORD:
            case AT_TOKEN:
                return true;
            default:
                return false;
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
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse return type descriptor of a function. A return type descriptor has the following structure.
     *
     * <code>return-type-descriptor := [ returns annots type-descriptor ]</code>
     *
     * @param isInExprContext
     * @return Parsed node
     */
    private STNode parseFuncReturnTypeDescriptor(boolean isInExprContext) {
        return parseFuncReturnTypeDescriptor(peek().kind, isInExprContext);
    }

    private STNode parseFuncReturnTypeDescriptor(SyntaxKind nextTokenKind, boolean isInExprContext) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
                STToken nextNExtToken = getNextNextToken(nextTokenKind);
                // If global var-decl
                if (nextNExtToken.kind == SyntaxKind.EQUAL_TOKEN || nextNExtToken.kind == SyntaxKind.SEMICOLON_TOKEN) {
                    return STNodeFactory.createEmptyNode();
                }

                // Otherwise could be missing 'returns' keyword
                break;
            case OPEN_BRACE_TOKEN: // func-body block
            case EQUAL_TOKEN: // external func
                return STNodeFactory.createEmptyNode();
            case RETURNS_KEYWORD:
                break;
            default:
                nextNExtToken = getNextNextToken(nextTokenKind);
                if (nextNExtToken.kind == SyntaxKind.RETURNS_KEYWORD) {
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
        startContext(context);
        STNode typeDesc = parseTypeDescriptorInternal(context);
        endContext();
        return typeDesc;

    }

    private STNode parseTypeDescriptorInternal(ParserRuleContext context) {
        STToken token = peek();
        STNode typeDesc = parseTypeDescriptorInternal(token.kind, context);
        return parseComplexTypeDescriptor(typeDesc, context);

    }

    /**
     * This will handle the parsing of optional,array,union type desc to infinite length.
     *
     * @param typeDesc
     *
     * @return Parsed type descriptor node
     */
    private STNode parseComplexTypeDescriptor(STNode typeDesc, ParserRuleContext context) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            // If next token after a type descriptor is <code>?</code> then it is an optional type descriptor
            case QUESTION_MARK_TOKEN:
                return parseComplexTypeDescriptor(parseOptionalTypeDescriptor(typeDesc), context);
            // If next token after a type descriptor is <code>[</code> then it is an array type descriptor
            case OPEN_BRACKET_TOKEN:
                return parseComplexTypeDescriptor(parseArrayTypeDescriptor(typeDesc), context);
            // If next token after a type descriptor is <code>[</code> then it is an array type descriptor
            case PIPE_TOKEN:
                return parseComplexTypeDescriptor(parseUnionTypeDescriptor(typeDesc, context), context);
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
                // nil type descriptor '()'
                return parseNilTypeDescriptor();
            case MAP_KEYWORD: // map type desc
            case FUTURE_KEYWORD: // future type desc
            case TYPEDESC_KEYWORD: // typedesc type desc
                return parseParameterizedTypeDescriptor();
            case ERROR_KEYWORD: // error type descriptor
                return parseErrorTypeDescriptor();
            case STREAM_KEYWORD: // stream type desc
                return parseStreamTypeDescriptor();
            case FUNCTION_KEYWORD:
                return parseFunctionTypeDesc();
            default:
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
     * @return Parsed node
     */
    private STNode parseFunctionBody() {
        STToken token = peek();
        return parseFunctionBody(token.kind);
    }

    /**
     * Parse function body, given the next token kind.
     *
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    protected STNode parseFunctionBody(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EQUAL_TOKEN:
                return parseExternalFunctionBody();
            case OPEN_BRACE_TOKEN:
                return parseFunctionBodyBlock();
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FUNC_BODY);

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

                return parseFunctionBody(solution.tokenKind);
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
     * @return Parsed node
     */
    private STNode parseFunctionBodyBlock() {
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        STNode openBrace = parseOpenBrace();
        STToken token = peek();

        ArrayList<STNode> firstStmtList = new ArrayList<>();
        ArrayList<STNode> workers = new ArrayList<>();
        ArrayList<STNode> secondStmtList = new ArrayList<>();

        ParserRuleContext currentCtx = ParserRuleContext.DEFAULT_WORKER_INIT;
        boolean hasNamedWorkers = false;
        while (!isEndOfStatements(token.kind)) {
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

    private boolean isEndOfRecordTypeNode(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
            case TYPE_KEYWORD:
            case PUBLIC_KEYWORD:
            case LISTENER_KEYWORD:
            case IMPORT_KEYWORD:
                return true;
            case SERVICE_KEYWORD:
                return isServiceDeclStart(ParserRuleContext.RECORD_FIELD, 1);
            default:
                return false;
        }
    }

    private boolean isEndOfObjectTypeNode(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
            case IMPORT_KEYWORD:
                return true;
            case SERVICE_KEYWORD:
                return isServiceDeclStart(ParserRuleContext.OBJECT_MEMBER, 1);
            default:
                return false;
        }
    }

    /**
     * Parse type reference or variable reference.
     *
     * @return Parsed node
     */
    private STNode parseStatementStartIdentifier() {
        return parseQualifiedIdentifier(ParserRuleContext.STATEMENT_START_IDENTIFIER);
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
                return OperatorPrecedence.ACTION;
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
                // If the current precedence level is unary/action, then we return
                // the binary operator with closest precedence level to it.
                // Therefore fall through
            case MULTIPLICATIVE:
                return SyntaxKind.ASTERISK_TOKEN;
            case ADDITIVE:
                return SyntaxKind.PLUS_TOKEN;
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
                startContext(ParserRuleContext.RECORD_FIELD);
                STNode metadata = parseMetaData(nextTokenKind);
                type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
                STNode fieldOrRestDesc = parseFieldDescriptor(isInclusive, type, metadata);
                endContext();
                return fieldOrRestDesc;
            default:
                if (isTypeStartingToken(nextTokenKind)) {
                    // individual-field-descriptor
                    startContext(ParserRuleContext.RECORD_FIELD);
                    metadata = createEmptyMetadata();
                    type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
                    fieldOrRestDesc = parseFieldDescriptor(isInclusive, type, metadata);
                    endContext();
                    return fieldOrRestDesc;
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

    private STNode parseFieldDescriptor(boolean isInclusive, STNode type, STNode metadata) {
        if (isInclusive) {
            STNode fieldName = parseVariableName();
            return parseFieldDescriptorRhs(metadata, type, fieldName);
        } else {
            return parseFieldOrRestDescriptorRhs(metadata, type);
        }
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
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STNode typeRefOrPkgRef = consume();
            return parseQualifiedIdentifier(typeRefOrPkgRef);
        } else {
            Solution sol = recover(token, currentCtx);
            return sol.recoveredNode;
        }
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
                STNode fieldName = parseVariableName();
                return parseFieldDescriptorRhs(metadata, type, fieldName);
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
     * @param type Type descriptor
     * @param fieldName Field name
     * @return Parsed node
     */
    private STNode parseFieldDescriptorRhs(STNode metadata, STNode type, STNode fieldName) {
        STToken token = peek();
        return parseFieldDescriptorRhs(token.kind, metadata, type, fieldName);
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
    private STNode parseFieldDescriptorRhs(SyntaxKind kind, STNode metadata, STNode type, STNode fieldName) {
        switch (kind) {
            case SEMICOLON_TOKEN:
                STNode questionMarkToken = STNodeFactory.createEmptyNode();
                STNode semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordFieldNode(metadata, type, fieldName, questionMarkToken,
                        semicolonToken);
            case QUESTION_MARK_TOKEN:
                questionMarkToken = parseQuestionMark();
                semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordFieldNode(metadata, type, fieldName, questionMarkToken,
                        semicolonToken);
            case EQUAL_TOKEN:
                // parseRecordDefaultValue();
                STNode equalsToken = parseAssignOp();
                STNode expression = parseExpression();
                semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordFieldWithDefaultValueNode(metadata, type, fieldName, equalsToken,
                        expression, semicolonToken);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FIELD_DESCRIPTOR_RHS, metadata, type, fieldName);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseFieldDescriptorRhs(solution.tokenKind, metadata, type, fieldName);
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
        STToken token = peek();

        ArrayList<STNode> stmts = new ArrayList<>();
        while (!isEndOfStatements(token.kind)) {
            STNode stmt = parseStatement();
            if (stmt == null) {
                break;
            }

            if (stmt.kind == SyntaxKind.NAMED_WORKER_DECLARATION) {
                this.errorHandler.reportInvalidNode(null, "named-workers are not allowed here");
                break;
            }

            stmts.add(stmt);
            token = peek();
        }

        return STNodeFactory.createNodeList(stmts);
    }

    private boolean isEndOfStatements(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                return true;
            case SERVICE_KEYWORD:
                return isServiceDeclStart(ParserRuleContext.STATEMENT, 1);
            default:
                return false;
        }
    }

    /**
     * Parse a single statement.
     *
     * @return Parsed node
     */
    protected STNode parseStatement() {
        STToken token = peek();
        return parseStatement(token.kind);
    }

    private STNode parseStatement(SyntaxKind tokenKind) {
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
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
            case RETURN_KEYWORD:
            case TYPE_KEYWORD:
            case LOCK_KEYWORD:
            case OPEN_BRACE_TOKEN:
            case FORK_KEYWORD:
            case FOREACH_KEYWORD:

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
                if (isValidLHSExpression(tokenKind)) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseStatement(solution.tokenKind);
        }

        return parseStatement(tokenKind, annots);
    }

    private STNode getAnnotations(STNode nullbaleAnnot) {
        if (nullbaleAnnot != null) {
            return nullbaleAnnot;
        }

        return STNodeFactory.createNodeList(new ArrayList<>());
    }

    private STNode parseStatement(STNode annots) {
        return parseStatement(peek().kind, annots);
    }

    /**
     * Parse a single statement, given the next token kind.
     *
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseStatement(SyntaxKind tokenKind, STNode annots) {
        // TODO: validate annotations: not every statement supports annots
        switch (tokenKind) {
            case CLOSE_BRACE_TOKEN:
                this.errorHandler.reportInvalidNode(null, "invalid annotations");
                // Returning null marks the end of statements
                return null;
            case SEMICOLON_TOKEN:
                this.errorHandler.removeInvalidToken();
                return parseStatement(tokenKind, annots);
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
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
                // Need to pass the token kind, since we may be coming here after recovering.
                // If so, `peek().kind` will not be same as `tokenKind`.
                return parseStamentStartsWithExpr(tokenKind, getAnnotations(annots));
            case IDENTIFIER_TOKEN:
                // If the statement starts with an identifier, it could be a var-decl-stmt
                // with a user defined type, or some statement starts with an expression
                return parseStatementStartsWithIdentifier(getAnnotations(annots));
            case LOCK_KEYWORD:
                return parseLockStatement();
            case OPEN_BRACE_TOKEN:
                return parseBlockNode();
            case WORKER_KEYWORD:
                // Even-though worker is not a statement, we parse it as statements.
                // then validates it based on the context. This is done to provide
                // better error messages
                return parseNamedWorkerDeclaration(getAnnotations(annots));
            case FORK_KEYWORD:
                return parseForkStatement();
            case FOREACH_KEYWORD:
                return parseForEachStatement();
            default:
                if (isTypeStartingToken(tokenKind)) {
                    // If the statement starts with a type, then its a var declaration.
                    // This is an optimization since if we know the next token is a type, then
                    // we can parse the var-def faster.
                    finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
                }
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT_WITHOUT_ANNOTS, annots);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseStatement(solution.tokenKind, annots);
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
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        STNode varName = parseVariableName();
        STNode varDecl = parseVarDeclRhs(annots, finalKeyword, type, varName, isModuleVar);
        endContext();
        return varDecl;
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
     * @param type Type descriptor
     * @param varName Variable name
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(STNode metadata, STNode finalKeyword, STNode type, STNode varName,
                                   boolean isModuleVar) {
        STToken token = peek();
        return parseVarDeclRhs(token.kind, metadata, finalKeyword, type, varName, isModuleVar);
    }

    /**
     * Parse the right hand side of a variable declaration statement, given the
     * next token kind.
     *
     * @param tokenKind Next token kind
     * @param metadata Metadata
     * @param finalKeyword Final keyword
     * @param type Type descriptor
     * @param varName Variable name
     * @param isModuleVar flag indicating whether the var is module level
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(SyntaxKind tokenKind, STNode metadata, STNode finalKeyword, STNode type,
                                   STNode varName, boolean isModuleVar) {
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
                if (isModuleVar) {
                    this.errorHandler.reportMissingTokenError("assignment required");
                }
                assign = STNodeFactory.createEmptyNode();
                expr = STNodeFactory.createEmptyNode();
                semicolon = parseSemicolon();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.VAR_DECL_STMT_RHS, metadata, finalKeyword, type,
                        varName, isModuleVar);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseVarDeclRhs(solution.tokenKind, metadata, finalKeyword, type, varName, isModuleVar);
        }

        if (isModuleVar) {
            return STNodeFactory.createModuleVariableDeclarationNode(metadata, finalKeyword, type, varName, assign,
                    expr, semicolon);
        }

        return STNodeFactory.createVariableDeclarationNode(metadata, finalKeyword, type, varName, assign, expr,
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

    private STNode parseActionOrExpression(SyntaxKind tokenKind) {
        return parseExpression(tokenKind, DEFAULT_OP_PRECEDENCE, true, true);
    }

    private STNode parseActionOrExpression(boolean isRhsExpr) {
        return parseExpression(DEFAULT_OP_PRECEDENCE, isRhsExpr, true);
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
                return true;
            case FIELD_ACCESS:
                return isValidLVExpr(((STFieldAccessExpressionNode) expression).expression);
            case INDEXED_EXPRESSION:
                return isValidLVExpr(((STIndexedExpressionNode) expression).containerExpression);
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

    /**
     * Parse terminal expressions. A terminal expression has the highest precedence level
     * out of all expressions, and will be at the leaves of an expression tree.
     *
     * @param isRhsExpr Is a rhs expression
     * @param allowActions Allow actions
     * @return Parsed node
     */
    private STNode parseTerminalExpression(boolean isRhsExpr, boolean allowActions) {
        return parseTerminalExpression(peek().kind, isRhsExpr, allowActions);
    }

    private STNode parseTerminalExpression(SyntaxKind kind, boolean isRhsExpr, boolean allowActions) {
        // TODO: Whenever a new expression start is added, make sure to
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
                STToken nextNextToken = getNextNextToken(kind);
                // parse nil literal '()'
                if (nextNextToken.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
                    return parseNilLiteral();
                }
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
                return parseTrapExpression(isRhsExpr);
            case OPEN_BRACKET_TOKEN:
                return parseListConstructorExpr();
            case LT_TOKEN:
                return parseTypeCastExpr();
            case TABLE_KEYWORD:
                return parseTableConstructorExpr();
            case ERROR_KEYWORD:
                return parseFunctionalConstructorExpr(SyntaxKind.ERROR_CONSTRUCTOR_EXPRESSION);
            case LET_KEYWORD:
                return parseLetExpression();
            case BACKTICK_TOKEN:
                return parseTemplateExpression();
            case XML_KEYWORD:
                nextNextToken = getNextNextToken(kind);
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
                return parseFunctionExpression(null);
            case AT_TOKEN:
                // Annon-func can have annotations. Check for other expressions
                // that van start with annots.
                break;
            default:
                break;
        }

        Solution solution = recover(peek(), ParserRuleContext.TERMINAL_EXPRESSION, isRhsExpr, allowActions);
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
                this.errorHandler.reportMissingTokenError("missing " + solution.recoveredNode);
                return parseQualifiedIdentifier(solution.recoveredNode);
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                this.errorHandler.reportMissingTokenError("missing " + solution.recoveredNode);
                return solution.recoveredNode;
            default:
                return parseTerminalExpression(solution.tokenKind, isRhsExpr, allowActions);
        }
    }

    private STNode parseActionOrExpressionInLhs(SyntaxKind nextTokenKind, STNode lhsExpr) {
        return parseExpressionRhs(nextTokenKind, DEFAULT_OP_PRECEDENCE, lhsExpr, false, true);
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
     * @param isLVExpr Flag indicating whether this is on a lhsExpr of a statement
     * @param allowActions Flag indicating whether the current context support actions
     * @return Parsed node
     */
    private STNode parseExpressionRhs(OperatorPrecedence precedenceLevel, STNode lhsExpr, boolean isLVExpr,
                                      boolean allowActions) {
        STToken token = peek();
        return parseExpressionRhs(token.kind, precedenceLevel, lhsExpr, isLVExpr, allowActions);
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

        // If the precedence level of the operator that was being parsed is higher than
        // the newly found (next) operator, then return and finish the previous expr,
        // because it has a higher precedence.
        OperatorPrecedence nextOperatorPrecedence = getOpPrecedence(tokenKind);
        if (currentPrecedenceLevel.isHigherThan(nextOperatorPrecedence)) {
            return lhsExpr;
        }

        STNode newLhsExpr;
        switch (tokenKind) {
            case OPEN_PAREN_TOKEN:
                newLhsExpr = parseFuncCall(lhsExpr);
                break;
            case OPEN_BRACKET_TOKEN:
                newLhsExpr = parseMemberAccessExpr(lhsExpr);
                break;
            case DOT_TOKEN:
                newLhsExpr = parseFieldAccessOrMethodCall(lhsExpr);
                break;
            case IS_KEYWORD:
                newLhsExpr = parseTypeTestExpression(lhsExpr);
                break;
            case RIGHT_ARROW_TOKEN:
                newLhsExpr = parseAction(tokenKind, lhsExpr);
                if (!allowActions) {
                    this.errorHandler.reportInvalidNode(null, "actions are not allowed here");
                }
                break;
            default:
                STNode operator = parseBinaryOperator();

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
                return true;
            default:
                return isBinaryOperator(tokenKind);
        }
    }

    /**
     * Parse member access expression.
     *
     * @param lhsExpr Container expression
     * @return Member access expression
     */
    private STNode parseMemberAccessExpr(STNode lhsExpr) {
        STNode openBracket = parseOpenBracket();

        STNode keyExpr;
        switch (peek().kind) {
            case CLOSE_BRACKET_TOKEN:
                // array-type-desc can have an empty array-len-expr
                keyExpr = STNodeFactory.createEmptyNode();
                break;
            case ASTERISK_TOKEN:
                keyExpr = consume();
                break;
            default:
                keyExpr = parseExpression();
                break;
        }

        STNode closeBracket = parseCloseBracket();
        return STNodeFactory.createIndexedExpressionNode(lhsExpr, openBracket, keyExpr, closeBracket);
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
     * Parse field access expression and method call expression.
     *
     * @param lhsExpr Preceding expression of the field access or method call
     * @return One of <code>field-access-expression</code> or <code>method-call-expression</code>.
     */
    private STNode parseFieldAccessOrMethodCall(STNode lhsExpr) {
        STNode dotToken = parseDotToken();
        STNode fieldOrMethodName = parseIdentifier(ParserRuleContext.FIELD_OR_FUNC_NAME);

        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            // function invocation
            STNode openParen = parseOpenParenthesis();
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
        STNode openParen = parseOpenParenthesis();
        STNode expr;
        if (allowActions) {
            expr = parseActionOrExpression(isRhsExpr);
        } else {
            expr = parseExpression(isRhsExpr);
        }

        STNode closeParen = parseCloseParenthesis();
        if (isAction(expr)) {
            return STNodeFactory.createBracedExpressionNode(SyntaxKind.BRACED_ACTION, openParen, expr, closeParen);
        } else {
            return STNodeFactory.createBracedExpressionNode(SyntaxKind.BRACED_EXPRESSION, openParen, expr, closeParen);
        }
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
        STNode openParen = parseOpenParenthesis();
        STNode args = parseArgsList();
        STNode closeParen = parseCloseParenthesis();
        return STNodeFactory.createFunctionCallExpressionNode(identifier, openParen, args, closeParen);
    }

    /**
     * Parse error constructor expression.
     * <code>functional-constructor-expr := functionally-constructible-type-reference ( arg-list )
     * functionally-constructible-type-reference := error | type-reference</code>
     *
     * @param kind Syntax kind
     * @return Functional constructor expression
     */
    private STNode parseFunctionalConstructorExpr(SyntaxKind kind) {
        STNode functionallyConstructibleTypeReference;
        if (kind == SyntaxKind.ERROR_CONSTRUCTOR_EXPRESSION) {
            functionallyConstructibleTypeReference = parseErrorKeyWord();
        } else {
            functionallyConstructibleTypeReference = parseTypeReference();
        }
        STNode openParen = parseOpenParenthesis();
        STNode args = parseArgsList();
        STNode closeParen = parseCloseParenthesis();
        return STNodeFactory.createFunctionalConstructorExpressionNode(kind,
                functionallyConstructibleTypeReference, openParen, args, closeParen);
    }

    /**
     * Parse function call argument list.
     *
     * @return Parsed args list
     */
    private STNode parseArgsList() {
        startContext(ParserRuleContext.ARG_LIST);
        ArrayList<STNode> argsList = new ArrayList<>();

        STToken token = peek();
        if (isEndOfParametersList(token.kind)) {
            STNode args = STNodeFactory.createNodeList(argsList);
            endContext();
            return args;
        }

        SyntaxKind lastProcessedArgKind = parseFirstArg(argsList);
        parseFollowUpArg(argsList, lastProcessedArgKind);

        STNode args = STNodeFactory.createNodeList(argsList);
        endContext();
        return args;
    }

    /**
     * Parse the first argument of a function call.
     *
     * @param argsList Arguments list to which the parsed argument must be added
     * @return Kind of the argument first argument.
     */
    private SyntaxKind parseFirstArg(ArrayList<STNode> argsList) {
        // Comma precedes the first argument is an empty node, since it doesn't exist.
        STNode leadingComma = STNodeFactory.createEmptyNode();
        STNode arg = parseArg(leadingComma);
        if (SyntaxKind.POSITIONAL_ARG.ordinal() <= arg.kind.ordinal()) {
            argsList.add(arg);
            return arg.kind;
        } else {
            reportInvalidOrderOfArgs(peek(), SyntaxKind.POSITIONAL_ARG, arg.kind);
            return SyntaxKind.POSITIONAL_ARG;
        }
    }

    /**
     * Parse follow up arguments.
     *
     * @param argsList Arguments list to which the parsed argument must be added
     * @param lastProcessedArgKind Kind of the argument processed prior to this
     */
    private void parseFollowUpArg(ArrayList<STNode> argsList, SyntaxKind lastProcessedArgKind) {
        STToken nextToken = peek();
        while (!isEndOfParametersList(nextToken.kind)) {
            STNode leadingComma = parseComma();

            // If there's an extra comma at the end of arguments list, remove it.
            // Then stop the argument parsing.
            nextToken = peek();
            if (isEndOfParametersList(nextToken.kind)) {
                this.errorHandler.reportInvalidNode((STToken) leadingComma, "invalid token " + leadingComma);
                break;
            }

            STNode arg = parseArg(nextToken.kind, leadingComma);
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
                arg = parseNamedOrPositionalArg(leadingComma);
                break;

            // Any other expression goes here
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case OPEN_PAREN_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
            default:
                expr = parseExpression();
                arg = STNodeFactory.createPositionalArgumentNode(leadingComma, expr);
                break;
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
    private STNode parseNamedOrPositionalArg(STNode leadingComma) {
        STToken secondToken = peek(2);
        switch (secondToken.kind) {
            case EQUAL_TOKEN:
                STNode argNameOrVarRef = STNodeFactory.createSimpleNameReferenceNode(consume());
                STNode equal = parseAssignOp();
                STNode expr = parseExpression();
                return STNodeFactory.createNamedArgumentNode(leadingComma, argNameOrVarRef, equal, expr);
            case COMMA_TOKEN:
            case CLOSE_PAREN_TOKEN:
                argNameOrVarRef = STNodeFactory.createSimpleNameReferenceNode(consume());
                return STNodeFactory.createPositionalArgumentNode(leadingComma, argNameOrVarRef);

            // Treat everything else as a single expression. If something is missing,
            // expression-parsing will recover it.
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case IDENTIFIER_TOKEN:
            case OPEN_PAREN_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            default:
                expr = parseExpression();
                return STNodeFactory.createPositionalArgumentNode(leadingComma, expr);
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
        List<STNode> qualifiers = new ArrayList<>();
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
                return STNodeFactory.createNodeList(qualifiers);
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
        STToken nextToken = peek();
        while (!isEndOfObjectTypeNode(nextToken.kind)) {
            startContext(ParserRuleContext.OBJECT_MEMBER);
            STNode member = parseObjectMember(nextToken.kind);
            endContext();

            // Null member indicates the end of object members
            if (member == null) {
                break;
            }
            objectMembers.add(member);
            nextToken = peek();
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

                Solution solution = recover(peek(), ParserRuleContext.OBJECT_MEMBER);

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
                if (visibilityQualifiers.kind != SyntaxKind.NONE) {
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
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode fieldName = parseVariableName();
        return parseObjectFieldRhs(metadata, methodQualifiers, type, fieldName);
    }

    /**
     * Parse object field rhs, and complete the object field parsing. Returns the parsed object field.
     *
     * @param metadata Metadata
     * @param visibilityQualifier Visibility qualifier
     * @param type Type descriptor
     * @param fieldName Field name
     * @return Parsed object field
     */
    private STNode parseObjectFieldRhs(STNode metadata, STNode visibilityQualifier, STNode type, STNode fieldName) {
        STToken nextToken = peek();
        return parseObjectFieldRhs(nextToken.kind, metadata, visibilityQualifier, type, fieldName);
    }

    /**
     * Parse object field rhs, and complete the object field parsing. Returns the parsed object field.
     *
     * @param nextTokenKind Kind of the next token
     * @param metadata Metadata
     * @param visibilityQualifier Visibility qualifier
     * @param type Type descriptor
     * @param fieldName Field name
     * @return Parsed object field
     */
    private STNode parseObjectFieldRhs(SyntaxKind nextTokenKind, STNode metadata, STNode visibilityQualifier,
                                       STNode type, STNode fieldName) {
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

                return parseObjectFieldRhs(solution.tokenKind, metadata, visibilityQualifier, type, fieldName);
        }

        return STNodeFactory.createObjectFieldNode(metadata, visibilityQualifier, type, fieldName, equalsToken,
                expression, semicolonToken);
    }

    private STNode parseObjectMethod(STNode metadata, STNode methodQualifiers) {
        return parseFuncDefOrFuncTypeDesc(metadata, methodQualifiers);
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
        STNode expr = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, allowActions);
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
        STNode semicolon;
        STToken token = peek();

        switch (token.kind) {
            case SEMICOLON_TOKEN:
                expr = STNodeFactory.createEmptyNode();
                break;
            default:
                expr = parseActionOrExpression();
                break;
        }

        semicolon = parseSemicolon();
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
        List<STNode> fields = new ArrayList<>();
        STToken nextToken = peek();
        if (isEndOfMappingConstructor(nextToken.kind)) {
            return STNodeFactory.createNodeList(fields);
        }

        // Parse first field mapping, that has no leading comma
        STNode leadingComma = STNodeFactory.createEmptyNode();
        STNode field = parseMappingField(leadingComma);
        fields.add(field);

        // Parse the remaining field mappings
        nextToken = peek();
        while (!isEndOfMappingConstructor(nextToken.kind)) {
            leadingComma = parseComma();
            field = parseMappingField(leadingComma);
            fields.add(field);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(fields);
    }

    private boolean isEndOfMappingConstructor(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
                return false;
            case EOF_TOKEN:
            case AT_TOKEN:
            case DOCUMENTATION_LINE:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
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
     * @param leadingComma Leading comma
     * @return Parsed node
     */
    private STNode parseMappingField(STNode leadingComma) {
        STToken nextToken = peek();
        return parseMappingField(nextToken.kind, leadingComma);
    }

    private STNode parseMappingField(SyntaxKind tokenKind, STNode leadingComma) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
                return parseSpecificFieldWithOptionValue(leadingComma);
            case STRING_LITERAL:
                STNode key = parseStringLiteral();
                STNode colon = parseColon();
                STNode valueExpr = parseExpression();
                return STNodeFactory.createSpecificFieldNode(leadingComma, key, colon, valueExpr);
            case OPEN_BRACKET_TOKEN:
                return parseComputedField(leadingComma);
            case ELLIPSIS_TOKEN:
                STNode ellipsis = parseEllipsis();
                STNode expr = parseExpression();
                return STNodeFactory.createSpreadFieldNode(leadingComma, ellipsis, expr);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.MAPPING_FIELD, leadingComma);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseMappingField(solution.tokenKind, leadingComma);
        }
    }

    /**
     * Parse mapping constructor specific-field with an optional value.
     *
     * @param leadingComma
     * @return Parsed node
     */
    private STNode parseSpecificFieldWithOptionValue(STNode leadingComma) {
        STNode key = parseIdentifier(ParserRuleContext.MAPPING_FIELD_NAME);
        return parseSpecificFieldRhs(leadingComma, key);
    }

    private STNode parseSpecificFieldRhs(STNode leadingComma, STNode key) {
        STToken nextToken = peek();
        return parseSpecificFieldRhs(nextToken.kind, leadingComma, key);
    }

    private STNode parseSpecificFieldRhs(SyntaxKind tokenKind, STNode leadingComma, STNode key) {
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
                Solution solution = recover(token, ParserRuleContext.SPECIFIC_FIELD_RHS, leadingComma, key);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseSpecificFieldRhs(solution.tokenKind, leadingComma, key);

        }
        return STNodeFactory.createSpecificFieldNode(leadingComma, key, colon, valueExpr);
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
     * Parse computed-name-field of a mapping constructor expression.
     * <p>
     * <code>computed-name-field := [ field-name-expr ] : value-expr</code>
     *
     * @param leadingComma Leading comma
     * @return Parsed node
     */
    private STNode parseComputedField(STNode leadingComma) {
        // Parse computed field name
        startContext(ParserRuleContext.COMPUTED_FIELD_NAME);
        STNode openBracket = parseOpenBracket();
        STNode fieldNameExpr = parseExpression();
        STNode closeBracket = parseCloseBracket();
        endContext();

        // Parse rhs
        STNode colon = parseColon();
        STNode valueExpr = parseExpression();
        return STNodeFactory.createComputedNameFieldNode(leadingComma, openBracket, fieldNameExpr, closeBracket, colon,
                valueExpr);
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
        STNode service = STNodeFactory.createServiceDeclarationNode(metadata, serviceKeyword, serviceName, onKeyword,
                expressionList, serviceBody);
        return service;
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
            this.errorHandler.reportMissingTokenError("missing expression");
            return STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
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
            case IDENTIFIER_TOKEN:
                return false;
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
            case AT_TOKEN:
            case DOCUMENTATION_LINE:
            case PRIVATE_KEYWORD:
            case RETURNS_KEYWORD:
            case SERVICE_KEYWORD:
            case TYPE_KEYWORD:
            case CONST_KEYWORD:
            case FINAL_KEYWORD:
                return true;
            default:
                return isSimpleType(tokenKind);
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
                return parseFuncDefinition(metadata, resourceKeyword);
            case FUNCTION_KEYWORD:
                return parseFuncDefinition(metadata, STNodeFactory.createEmptyNode());
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
                    case EQUAL_TOKEN: // service foo = ...
                    case SEMICOLON_TOKEN: // service foo;
                        return false;
                    case ON_KEYWORD: // service foo on ...
                        return true;
                    default:
                        // If not any of above, this is not a valid syntax. Hence try to recover
                        // silently and find what's the best token. From that recovered token try
                        // to determine whether the next construct is a service decl or not.
                        ParserRuleContext sol = this.errorHandler.findBestPath(currentContext);
                        return sol == ParserRuleContext.SERVICE_DECL || sol == ParserRuleContext.CLOSE_BRACE;
                }
            case ON_KEYWORD:
                // Next token sequence is similar to: `service foo on ...`.
                // Then this is a service decl.
                return true;
            default:
                this.errorHandler.removeInvalidToken();
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
                variableName = typeOrVarName;
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
        STNode openParenthesisToken = parseOpenParenthesis();
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
     * @param typeDescriptorNode
     *
     * @return Parsed Node
     */
    private STNode parseArrayTypeDescriptor(STNode typeDescriptorNode) {
        startContext(ParserRuleContext.ARRAY_TYPE_DESCRIPTOR);
        STNode openBracketToken = parseOpenBracket();
        STNode arrayLengthNode = parseArrayLength();
        STNode closeBracketToken = parseCloseBracket();

        endContext();
        return STNodeFactory.createIndexedExpressionNode(typeDescriptorNode, openBracketToken, arrayLengthNode,
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
                return consume();
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
        STNode annotValue = parseMappingConstructorExpr();
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
                // TODO:
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
                STNodeFactory.createNodeList(new ArrayList<>()));
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
     * Pass statements that starts with an identifier.
     *
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseStatementStartsWithIdentifier(STNode annots) {
        startContext(ParserRuleContext.STMT_START_WITH_IDENTIFIER);
        STNode identifier = parseStatementStartIdentifier();
        STToken nextToken = peek();
        STNode stmt = parseStatementStartsWithIdentifier(nextToken.kind, annots, identifier);
        endContext();
        return stmt;
    }

    private STNode parseStatementStartsWithIdentifier(STNode annots, STNode identifier) {
        return parseStatementStartsWithIdentifier(peek().kind, annots, identifier);
    }

    private STNode parseStatementStartsWithIdentifier(SyntaxKind nextTokenKind, STNode annots, STNode identifier) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
            case QUESTION_MARK_TOKEN:
                // if the next token is question-mark then it is an optional type descriptor with user defined type.
                return parseTypeDescStartsWithIdentifier(identifier, annots);
            case EQUAL_TOKEN:
            case SEMICOLON_TOKEN:
                // Here we directly start parsing as a statement that starts with an expression.
                return parseStamentStartWithExpr(nextTokenKind, annots, identifier);
            case PIPE_TOKEN:
                STToken nextNextToken = peek(2);
                if (nextNextToken.kind != SyntaxKind.EQUAL_TOKEN) {
                    return parseTypeDescStartsWithIdentifier(identifier, annots);
                }
                // fall through
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundBinaryOperator(nextTokenKind)) {
                    return parseCompoundAssignmentStmtRhs(identifier);
                }
                // If the next token is part of a valid expression, then still parse it
                // as a statement that starts with an expression.
                if (isValidExprRhsStart(nextTokenKind)) {
                    STNode expression = parseActionOrExpressionInLhs(nextTokenKind, identifier);
                    return parseStamentStartWithExpr(annots, expression);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STMT_START_WITH_IDENTIFIER, annots, identifier);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseStatementStartsWithIdentifier(solution.tokenKind, annots, identifier);
        }
    }

    private STNode parseTypeDescStartsWithIdentifier(STNode typeDesc, STNode annots) {
        switchContext(ParserRuleContext.VAR_DECL_STMT);

        // We haven't parsed the type-desc as a type-desc (parsed as an identifier).
        // Therefore handle the context manually here.
        startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        typeDesc = parseComplexTypeDescriptor(typeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        endContext();

        STNode varName = parseVariableName();
        STNode finalKeyword = STNodeFactory.createEmptyNode();
        return parseVarDeclRhs(annots, finalKeyword, typeDesc, varName, false);
    }

    /**
     * Parse statement which is only consists of an action or expression.
     *
     * @param annots Annotations
     * @param nextTokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseStamentStartsWithExpr(SyntaxKind nextTokenKind, STNode annots) {
        startContext(ParserRuleContext.EXPRESSION_STATEMENT);
        STNode expression = parseActionOrExpression(nextTokenKind);
        STNode stmt = parseStamentStartWithExpr(annots, expression);
        endContext();
        return stmt;
    }

    /**
     * Parse statements that starts with an expression.
     *
     * @param annots Annotations
     * @return Parsed node
     */
    private STNode parseStamentStartWithExpr(STNode annots, STNode expression) {
        STToken nextToken = peek();
        return parseStamentStartWithExpr(nextToken.kind, annots, expression);
    }

    /**
     * Parse the component followed by the expression, at the beginning of a statement.
     *
     * @param nextTokenKind Kind of the next token
     * @param annots Annotations
     * @return Parsed node
     */
    private STNode parseStamentStartWithExpr(SyntaxKind nextTokenKind, STNode annots, STNode expression) {
        switch (nextTokenKind) {
            case EQUAL_TOKEN:
                switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                return parseAssignmentStmtRhs(expression);
            case SEMICOLON_TOKEN:
                return getExpressionAsStatement(expression);
            case IDENTIFIER_TOKEN:
                // Could be a var-decl, with array-type
                if (isPossibleArrayType(expression)) {
                    switchContext(ParserRuleContext.VAR_DECL_STMT);
                    STNode varName = parseVariableName();
                    STNode finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVarDeclRhs(annots, finalKeyword, expression, varName, false);
                }
                // fall through
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundBinaryOperator(nextTokenKind)) {
                    return parseCompoundAssignmentStmtRhs(expression);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STMT_START_WITH_EXPR_RHS, annots, expression);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseStamentStartWithExpr(solution.tokenKind, annots, expression);
        }
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
                return parseActionStatement(expression);
            default:
                // Everything else can not be written as a statement.
                // TODO: Add proper error reporting
                this.errorHandler.reportInvalidNode(null,
                        "left hand side of an assignment must be a variable reference");

                STNode semicolon = parseSemicolon();
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

                // TODO:
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
        return node instanceof STMissingToken;
    }

    private STNode parseActionStatement(STNode action) {
        STNode semicolon = parseSemicolon();
        return STNodeFactory.createExpressionStatementNode(SyntaxKind.ACTION_STATEMENT, action, semicolon);
    }

    private STNode parseAction(SyntaxKind tokenKind, STNode lhsExpr) {
        switch (tokenKind) {
            case RIGHT_ARROW_TOKEN:
                return parseRemoteMethodCallAction(lhsExpr);
            default:
                // Should never reach here.
                return null;
        }
    }

    /**
     * Parse remote method call action, given the starting expression.
     * <p>
     * <code>remote-method-call-action := expression -> method-name ( arg-list )</code>
     * 
     * @param expression LHS expression
     * @return
     */
    private STNode parseRemoteMethodCallAction(STNode expression) {
        STNode rightArrow = parseRightArrow();
        STNode methodName = parseFunctionName();
        STNode openParenToken = parseOpenParenthesis();
        STNode arguments = parseArgsList();
        STNode closeParenToken = parseCloseParenthesis();
        return STNodeFactory.createRemoteMethodCallActionNode(expression, rightArrow, methodName, openParenToken,
                arguments, closeParenToken);
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
     * Check whether this is a valid lhs expression.
     * 
     * @param tokenKind Kind of the next token
     * @return <code>true</code>if this is a start of an expression. <code>false</code> otherwise
     */
    private boolean isValidLHSExpression(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case STRING_LITERAL:
            case IDENTIFIER_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
            case TYPEOF_KEYWORD:
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
                return true;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return !isCompoundBinaryOperator(tokenKind);
            case OPEN_PAREN_TOKEN:
            default:
                return false;
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
        STNode openParenthesisToken = parseOpenParenthesis();
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
        STNode equalsToken = parseAssignOp();
        STNode initializer = parseExpression();
        STNode semicolonToken = parseSemicolon();
        return STNodeFactory.createConstantDeclarationNode(metadata, qualifier, constKeyword, typeDesc, annotTag,
                equalsToken, initializer, semicolonToken);
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
                attachPoints = STNodeFactory.createEmptyNode();
                break;
            case ON_KEYWORD:
                onKeyword = parseOnKeyword();
                attachPoints = parseAnnotationAttachPoints();
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
            this.errorHandler.reportMissingTokenError("missing attach point");
            return STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
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
                this.errorHandler.reportMissingTokenError("missing attach point");
                attachPoint = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
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
    private STNode parseXMLNamepsaceDeclaration() {
        startContext(ParserRuleContext.XML_NAMESPACE_DECLARATION);
        STNode xmlnsKeyword = parseXMLNSKeyword();
        STNode namespaceUri = parseXMLNamespaceUri();
        STNode xmlnsDecl = parseXMLDeclRhs(xmlnsKeyword, namespaceUri);
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

    /**
     * Parse constants expr.
     *
     * @return Parsed node
     */
    private STNode parseConstExpr() {
        startContext(ParserRuleContext.CONSTANT_EXPRESSION);
        STToken nextToken = peek();
        STNode expr;
        switch (nextToken.kind) {
            case STRING_LITERAL:
            case DECIMAL_INTEGER_LITERAL:
            case HEX_INTEGER_LITERAL:
            case DECIMAL_FLOATING_POINT_LITERAL:
            case HEX_FLOATING_POINT_LITERAL:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
                expr = consume();
                break;
            case IDENTIFIER_TOKEN:
                expr = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                break;
            case OPEN_BRACE_TOKEN:
                // TODO: nil-literal
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.CONSTANT_EXPRESSION_START);
                expr = solution.recoveredNode;
                break;
        }

        endContext();
        return expr;
    }

    /**
     * Parse the portion after the namsepsace-uri of an XML declaration.
     * 
     * @param xmlnsKeyword XMLNS keyword
     * @param namespaceUri Namespace URI
     * @return Parsed node
     */
    private STNode parseXMLDeclRhs(STNode xmlnsKeyword, STNode namespaceUri) {
        return parseXMLDeclRhs(peek().kind, xmlnsKeyword, namespaceUri);
    }

    private STNode parseXMLDeclRhs(SyntaxKind nextTokenKind, STNode xmlnsKeyword, STNode namespaceUri) {
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
                        recover(token, ParserRuleContext.XML_NAMESPACE_PREFIX_DECL, xmlnsKeyword, namespaceUri);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseXMLDeclRhs(solution.tokenKind, xmlnsKeyword, namespaceUri);
        }
        STNode semicolon = parseSemicolon();
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
     * Checks whether the given expression is a possible array-type-desc.
     * <br/>
     * i.e.: a member-access-expr, where its container is also a member-access.
     * <code>a[b][]</code>
     *
     * @param expression EXpression to check
     * @return <code>true</code> if the expression provided is a possible array-type desc. <code>false</code> otherwise
     */
    private boolean isPossibleArrayType(STNode expression) {
        switch (expression.kind) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
                return true;
            case INDEXED_EXPRESSION:
                return isPossibleArrayType(((STIndexedExpressionNode) expression).containerExpression);
            default:
                return false;
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
            case FUNCTION_KEYWORD:
                return true;
            default:
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
    private STNode parseMultileNamedWorkerDeclarations() {
        STToken token = peek();
        ArrayList<STNode> workers = new ArrayList<>();

        while (!isEndOfStatements(token.kind)) {
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
            token = peek();
        }

        if (workers.isEmpty()) {
            this.errorHandler.reportInvalidNode(null, "Fork Statement must contain atleast one named-worker");
        }
        STNode namedWorkers = STNodeFactory.createNodeList(workers);
        return namedWorkers;
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
        STNode namedWorkerDeclarations = parseMultileNamedWorkerDeclarations();
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
     * @param isRhsExpr
     * @return Trap expression node
     */
    private STNode parseTrapExpression(boolean isRhsExpr) {
        STNode trapKeyword = parseTrapKeyword();

        // allow-actions flag is always false, since there will not be any actions
        // within the trap-expression, due to the precedence.
        STNode expr = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, false);
        return STNodeFactory.createTrapExpressionNode(trapKeyword, expr);
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
        STToken nextToken = peek();

        // Return an empty list if list is empty
        if (isEndOfExpressionsList(nextToken.kind)) {
            return STNodeFactory.createNodeList(new ArrayList<>());
        }

        // Parse first expression, that has no leading comma
        STNode expr = parseExpression();
        expressions.add(expr);

        // Parse the remaining expressions
        nextToken = peek();
        STNode leadingComma;
        while (!isEndOfExpressionsList(nextToken.kind)) {
            leadingComma = parseComma();
            expressions.add(leadingComma);
            expr = parseExpression();
            expressions.add(expr);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(expressions);
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
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        STNode varName = parseVariableName();
        STNode inKeyword = parseInKeyword();
        STNode actionOrExpr = parseActionOrExpression();
        STNode blockStatement = parseBlockNode();
        endContext();
        return STNodeFactory.createForEachStatementNode(forEachKeyword, type, varName, inKeyword, actionOrExpr,
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
    private STNode parseTypeCastExpr() {
        startContext(ParserRuleContext.TYPE_CAST_EXPRESSION);
        STNode ltToken = parseLTToken();
        STNode typeCastParam = parseTypeCastParam();
        STNode gtToken = parseGTToken();
        STNode expression = parseExpression();
        endContext();
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

        return STNodeFactory.createTypeCastParamNode(annot, type);
    }

    /**
     * Parse table constructor expression.
     * <p>
     * <code>
     * table-constructor-expr := table [key-specifier] [ [row-list] ]
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseTableConstructorExpr() {
        startContext(ParserRuleContext.TABLE_CONSTRUCTOR);
        STNode tableKeyword = parseTableKeyword();
        STNode keySpecifier = STNodeFactory.createEmptyNode();
        return parseTableConstructorExpr(tableKeyword, keySpecifier);
    }

    private STNode parseTableConstructorExpr(STNode tableKeyword, STNode keySpecifier) {
        return parseTableConstructorExpr(peek().kind, tableKeyword, keySpecifier);
    }

    private STNode parseTableConstructorExpr(SyntaxKind nextTokenKind, STNode tableKeyword, STNode keySpecifier) {
        STNode openBracket;
        STNode rowList;
        STNode closeBracket;

        switch (nextTokenKind) {
            case KEY_KEYWORD:
                keySpecifier = parseKeySpecifier();
                break;
            case OPEN_BRACKET_TOKEN:
                break;
            default:
                Solution solution = recover(peek(), ParserRuleContext.TABLE_KEYWORD_RHS, tableKeyword, keySpecifier);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTableConstructorExpr(solution.tokenKind, tableKeyword, keySpecifier);
        }

        openBracket = parseOpenBracket();
        rowList = parseRowList();
        closeBracket = parseCloseBracket();
        endContext();
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
        List<STNode> mappings = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list if list is empty
        if (isEndOfMappingConstructorsList(nextToken.kind)) {
            return STNodeFactory.createNodeList(new ArrayList<>());
        }

        // Parse first mapping constructor, that has no leading comma
        STNode mapExpr = parseMappingConstructorExpr();
        mappings.add(mapExpr);

        // Parse the remaining mapping constructors
        nextToken = peek();
        STNode leadingComma;
        while (!isEndOfMappingConstructorsList(nextToken.kind)) {
            leadingComma = parseComma();
            mappings.add(leadingComma);
            mapExpr = parseMappingConstructorExpr();
            mappings.add(mapExpr);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(mappings);
    }

    private boolean isEndOfMappingConstructorsList(SyntaxKind tokenKind) {
        switch (tokenKind) {
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
        STNode openParen = parseOpenParenthesis();
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
        List<STNode> fieldNames = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list if list is empty
        if (isEndOfFieldNamesList(nextToken.kind)) {
            return STNodeFactory.createNodeList(new ArrayList<>());
        }

        // Parse first field name, that has no leading comma
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
        STNode streamKeywordToken = parseStreamKeyWord();
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
    private STNode parseStreamKeyWord() {
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
    private STNode parseLetExpression() {
        STNode letKeyword = parseLetKeyword();
        STNode letVarDeclarations = parseLetVarDeclarations();
        STNode inKeyword = parseInKeyword();
        STNode expression = parseExpression();
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
    private STNode parseLetVarDeclarations() {
        startContext(ParserRuleContext.LET_VAR_DECL);
        List<STNode> varDecls = new ArrayList<>();
        STToken nextToken = peek();

        // Make sure at least one let variable declaration is present
        if (isEndOfLetVarDeclarations(nextToken.kind)) {
            endContext();
            this.errorHandler.reportMissingTokenError("missing let variable declaration");
            return STNodeFactory.createNodeList(varDecls);
        }

        // Parse first variable declaration, that has no leading comma
        STNode varDec = parseLetVarDec();
        varDecls.add(varDec);

        // Parse the remaining variable declarations
        nextToken = peek();
        STNode leadingComma;
        while (!isEndOfLetVarDeclarations(nextToken.kind)) {
            leadingComma = parseComma();
            varDecls.add(leadingComma);
            varDec = parseLetVarDec();
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
    private STNode parseLetVarDec() {
        STNode annot = parseAnnotations();
        // TODO: Replace type and varName with typed-binding-pattern
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        STNode varName = parseVariableName();
        STNode assign = parseAssignOp();
        STNode expression = parseExpression();
        return STNodeFactory.createLetVariableDeclarationNode(annot, type, varName, assign, expression);
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
        STNode endingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);
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
     * Parse function type descriptor.
     * <p>
     * <code>function-type-descriptor := function function-signature</code>
     * 
     * @return Function type descriptor node
     */
    private STNode parseFunctionTypeDesc() {
        startContext(ParserRuleContext.FUNC_TYPE_DESC);
        STNode functionKeyword = parseFunctionKeyword();
        STNode signature = parseFuncSignature(true, false);
        return STNodeFactory.createFunctionTypeDescriptorNode(functionKeyword, signature);
    }

    /**
     * Parse anonymous function or function type desc. In an expression
     * context, both of these are possible. Hence we can distinguish only
     * after reaching the body.
     * 
     * @param annots
     * @return
     */
    private STNode parseFunctionExpression(STNode annots) {
        startContext(ParserRuleContext.ANON_FUNC_EXPRESSION);
        STNode funcKeyword = parseFunctionKeyword();
        STNode funcSignature = parseFuncSignature(true, false);
        STNode funcBody = parseFunctionTypeOrAnonFuncBody();

        if (funcBody == null) {
            return STNodeFactory.createFunctionTypeDescriptorNode(funcKeyword, funcSignature);
        }

        // TODO: Add parameter name validation
        return STNodeFactory.createAnonymousFunctionExpressionNode(annots, funcKeyword, funcSignature, funcBody);
    }

    private STNode parseFunctionTypeOrAnonFuncBody() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACE_TOKEN:
                return parseFunctionBodyBlock();
            case EQUAL_GT_TOKEN:
                // EXpression func body
                return null;
            default:
                return null;
        }
    }
}
