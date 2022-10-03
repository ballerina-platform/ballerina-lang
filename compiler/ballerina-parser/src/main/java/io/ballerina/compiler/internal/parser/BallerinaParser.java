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
package io.ballerina.compiler.internal.parser;

import io.ballerina.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.compiler.internal.parser.AbstractParserErrorHandler.Action;
import io.ballerina.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerina.compiler.internal.parser.tree.STAbstractNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STAmbiguousCollectionNode;
import io.ballerina.compiler.internal.parser.tree.STAnnotAccessExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STArrayTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.tree.STAsyncSendActionNode;
import io.ballerina.compiler.internal.parser.tree.STBinaryExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STBracedExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STConditionalExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STDefaultableParameterNode;
import io.ballerina.compiler.internal.parser.tree.STErrorConstructorExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STFieldAccessExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionArgumentNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionCallExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionSignatureNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.tree.STIndexedExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STIntersectionTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.tree.STListConstructorExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STMappingConstructorExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STMetadataNode;
import io.ballerina.compiler.internal.parser.tree.STMissingToken;
import io.ballerina.compiler.internal.parser.tree.STNamedArgumentNode;
import io.ballerina.compiler.internal.parser.tree.STNilLiteralNode;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STNodeList;
import io.ballerina.compiler.internal.parser.tree.STObjectTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.tree.STOptionalFieldAccessExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STOptionalTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.tree.STPositionalArgumentNode;
import io.ballerina.compiler.internal.parser.tree.STQualifiedNameReferenceNode;
import io.ballerina.compiler.internal.parser.tree.STRemoteMethodCallActionNode;
import io.ballerina.compiler.internal.parser.tree.STRequiredParameterNode;
import io.ballerina.compiler.internal.parser.tree.STRestArgumentNode;
import io.ballerina.compiler.internal.parser.tree.STRestBindingPatternNode;
import io.ballerina.compiler.internal.parser.tree.STRestParameterNode;
import io.ballerina.compiler.internal.parser.tree.STSimpleNameReferenceNode;
import io.ballerina.compiler.internal.parser.tree.STSpecificFieldNode;
import io.ballerina.compiler.internal.parser.tree.STSyncSendActionNode;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.internal.parser.tree.STTypeReferenceTypeDescNode;
import io.ballerina.compiler.internal.parser.tree.STTypeTestExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STTypedBindingPatternNode;
import io.ballerina.compiler.internal.parser.tree.STUnaryExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STUnionTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.utils.ConditionalExprResolver;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.text.CharReader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A LL(k) recursive-descent parser for ballerina.
 *
 * @since 1.2.0
 */
public class BallerinaParser extends AbstractParser {

    private static final OperatorPrecedence DEFAULT_OP_PRECEDENCE = OperatorPrecedence.DEFAULT;

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
     * Completely parses a given input a statement.
     *
     * @return Parsed node
     */
    public STNode parseAsStatement() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.FUNC_DEF);
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        STNode stmt = parseStatement();

        if (stmt == null || validateStatement(stmt)) {
            stmt = createMissingSimpleVarDecl(false);
            stmt = invalidateRestAndAddToTrailingMinutiae(stmt);
            return stmt;
        }

        if (stmt.kind == SyntaxKind.NAMED_WORKER_DECLARATION) {
            addInvalidNodeToNextToken(stmt, DiagnosticErrorCode.ERROR_NAMED_WORKER_NOT_ALLOWED_HERE);
            stmt = createMissingSimpleVarDecl(false);
            stmt = invalidateRestAndAddToTrailingMinutiae(stmt);
            return stmt;
        }

        stmt = invalidateRestAndAddToTrailingMinutiae(stmt);
        return stmt;
    }

    /**
     * Completely parses a given input as a block statement.
     *
     * @return Parsed node
     */
    public STNode parseAsBlockStatement() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.FUNC_DEF);
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        startContext(ParserRuleContext.WHILE_BLOCK);
        STNode blockStmtNode = parseBlockNode();

        blockStmtNode = invalidateRestAndAddToTrailingMinutiae(blockStmtNode);
        return blockStmtNode;
    }

    /**
     * Completely parses a given input as statements.
     *
     * @return Parsed node
     */
    public STNode parseAsStatements() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        STNode stmtsNode = parseStatements();

        STNodeList stmtNodeList = (STNodeList) stmtsNode;
        ArrayList<STNode> stmts = new ArrayList<>(stmtNodeList.size() + 1);

        // add all stmts except last stmt
        for (int i = 0; i < stmtNodeList.size() - 1; i++) {
            stmts.add(stmtNodeList.get(i));
        }

        STNode lastStmt;
        if (stmtNodeList.isEmpty()) {
            lastStmt = createMissingSimpleVarDecl(false);
        } else {
            lastStmt = stmtNodeList.get(stmtNodeList.size() - 1);
        }

        lastStmt = invalidateRestAndAddToTrailingMinutiae(lastStmt);
        stmts.add(lastStmt);

        return STNodeFactory.createNodeList(stmts);
    }

    /**
     * Completely parses a given input as an expression.
     *
     * @return Parsed node
     */
    public STNode parseAsExpression() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode expr = parseExpression();

        expr = invalidateRestAndAddToTrailingMinutiae(expr);
        return expr;
    }

    /**
     * Completely parses a given input as an action or expression.
     *
     * @return Parsed node
     */
    public STNode parseAsActionOrExpression() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.FUNC_DEF);
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode actionOrExpr = parseActionOrExpression();

        actionOrExpr = invalidateRestAndAddToTrailingMinutiae(actionOrExpr);
        return actionOrExpr;
    }

    /**
     * Completely parses a given input as a module member declaration.
     *
     * @return Parsed node
     */
    public STNode parseAsModuleMemberDeclaration() {
        startContext(ParserRuleContext.COMP_UNIT);
        STNode topLevelNode = parseTopLevelNode();

        if (topLevelNode == null) {
            topLevelNode = createMissingSimpleVarDecl(true);
        }

        if (topLevelNode.kind == SyntaxKind.IMPORT_DECLARATION) {
            STNode temp = topLevelNode;
            topLevelNode = createMissingSimpleVarDecl(true);
            topLevelNode = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(topLevelNode, temp);
        }

        topLevelNode = invalidateRestAndAddToTrailingMinutiae(topLevelNode);
        return topLevelNode;
    }

    /**
     * Completely parses a given input as an import declaration.
     *
     * @return Parsed node
     */
    public STNode parseAsImportDeclaration() {
        startContext(ParserRuleContext.COMP_UNIT);
        STNode importDecl = parseImportDecl();

        importDecl = invalidateRestAndAddToTrailingMinutiae(importDecl);
        return importDecl;
    }

    /**
     * Completely parses a given input as a type descriptor.
     *
     * @return Parsed node
     */
    public STNode parseAsTypeDescriptor() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.MODULE_TYPE_DEFINITION);
        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_DEF);

        typeDesc = invalidateRestAndAddToTrailingMinutiae(typeDesc);
        return typeDesc;
    }

    /**
     * Completely parses a given input as a binding pattern.
     *
     * @return Parsed node
     */
    public STNode parseAsBindingPattern() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode bindingPattern = parseBindingPattern();

        bindingPattern = invalidateRestAndAddToTrailingMinutiae(bindingPattern);
        return bindingPattern;
    }

    /**
     * Completely parses a given input as a function body block.
     *
     * @return Parsed node
     */
    public STNode parseAsFunctionBodyBlock() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.FUNC_DEF);
        STNode funcBodyBlock = parseFunctionBodyBlock(false);

        funcBodyBlock = invalidateRestAndAddToTrailingMinutiae(funcBodyBlock);
        return funcBodyBlock;
    }

    /**
     * Completely parses a given input as an object member.
     *
     * @return Parsed node
     */
    public STNode parseAsObjectMember() {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.SERVICE_DECL);
        startContext(ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER);
        STNode objectMember = parseObjectMember(ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER);

        if (objectMember == null) {
            objectMember = createMissingSimpleObjectField();
        }
        objectMember = invalidateRestAndAddToTrailingMinutiae(objectMember);
        return objectMember;
    }

    /**
     * Completely parses a given input as an intermediate clause.
     *
     * @param allowActions Allow actions
     * @return Parsed node
     */
    public STNode parseAsIntermediateClause(boolean allowActions) {
        startContext(ParserRuleContext.COMP_UNIT);
        startContext(ParserRuleContext.FUNC_DEF);
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        startContext(ParserRuleContext.VAR_DECL_STMT);
        startContext(ParserRuleContext.QUERY_EXPRESSION);

        STNode intermediateClause = null;
        if (!isEndOfIntermediateClause(peek().kind)) {
            intermediateClause = parseIntermediateClause(true, allowActions);
        }

        if (intermediateClause == null) {
            intermediateClause = createMissingWhereClause();
        }

        if (intermediateClause.kind == SyntaxKind.SELECT_CLAUSE) {
            STNode temp = intermediateClause;
            intermediateClause = createMissingWhereClause();
            intermediateClause = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(intermediateClause, temp);
        }

        intermediateClause = invalidateRestAndAddToTrailingMinutiae(intermediateClause);
        return intermediateClause;
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
                startContext(ParserRuleContext.FUNC_BODY_BLOCK);
                return parseStatement();
            case EXPRESSION:
                startContext(ParserRuleContext.COMP_UNIT);
                startContext(ParserRuleContext.VAR_DECL_STMT);
                return parseExpression();
            default:
                throw new UnsupportedOperationException("Cannot start parsing from: " + context);
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
        List<STNode> otherDecls = new ArrayList<>();
        List<STNode> importDecls = new ArrayList<>();

        boolean processImports = true;
        STToken token = peek();
        while (token.kind != SyntaxKind.EOF_TOKEN) {
            STNode decl = parseTopLevelNode();
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
                    updateLastNodeInListWithInvalidNode(otherDecls, decl,
                            DiagnosticErrorCode.ERROR_IMPORT_DECLARATION_AFTER_OTHER_DECLARATIONS);
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
    protected STNode parseTopLevelNode() {
        STToken nextToken = peek();
        STNode metadata;
        switch (nextToken.kind) {
            case EOF_TOKEN:
                return null;
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
                metadata = parseMetaData();
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
            case ENUM_KEYWORD:
            case CLASS_KEYWORD:
                // Top level qualifiers
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
            case DISTINCT_KEYWORD:
            case CLIENT_KEYWORD:
            case READONLY_KEYWORD:
            case CONFIGURABLE_KEYWORD:
            case SERVICE_KEYWORD:
                metadata = STNodeFactory.createEmptyNode();
                break;
            case IDENTIFIER_TOKEN:
                // Here we assume that after recovering, we'll never reach here.
                // Otherwise the tokenOffset will not be 1.
                if (isModuleVarDeclStart(1) || nextToken.isMissing()) {
                    // This is an early exit, so that we don't have to do the same check again.
                    return parseModuleVarDecl(STNodeFactory.createEmptyNode());
                }
                // Else fall through
            default:
                if (isTypeStartingToken(nextToken.kind) && nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    metadata = STNodeFactory.createEmptyNode();
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    metadata = STNodeFactory.createEmptyNode();
                    break;
                }

                return parseTopLevelNode();
        }

        return parseTopLevelNode(metadata);
    }

    /**
     * Parse top level node having an optional modifier preceding it, given the next token kind.
     *
     * @param metadata Metadata that precedes the top level node
     * @return Parsed node
     */
    private STNode parseTopLevelNode(STNode metadata) {
        STToken nextToken = peek();
        STNode publicQualifier = null;
        switch (nextToken.kind) {
            case EOF_TOKEN:
                if (metadata != null) {
                    metadata = addMetadataNotAttachedDiagnostic((STMetadataNode) metadata);
                    return createMissingSimpleVarDecl(metadata, true);
                }
                return null;
            case PUBLIC_KEYWORD:
                publicQualifier = consume();
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
            case CLASS_KEYWORD:
                // Top level qualifiers
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
            case DISTINCT_KEYWORD:
            case CLIENT_KEYWORD:
            case READONLY_KEYWORD:
            case SERVICE_KEYWORD:
            case CONFIGURABLE_KEYWORD:
                break;
            case IDENTIFIER_TOKEN:
                // Here we assume that after recovering, we'll never reach here.
                // Otherwise the tokenOffset will not be 1.
                if (isModuleVarDeclStart(1)) {
                    // This is an early exit, so that we don't have to do the same check again.
                    return parseModuleVarDecl(metadata);
                }
                // Else fall through
            default:
                if (isTypeStartingToken(nextToken.kind) && nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_METADATA);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    publicQualifier = STNodeFactory.createEmptyNode();
                    break;
                }

                return parseTopLevelNode(metadata);
        }

        return parseTopLevelNode(metadata, publicQualifier);
    }

    private STNode addMetadataNotAttachedDiagnostic(STMetadataNode metadata) {
        STNode docString = metadata.documentationString;
        if (docString != null) {
            docString = SyntaxErrors.addDiagnostic(docString,
                    DiagnosticErrorCode.ERROR_DOCUMENTATION_NOT_ATTACHED_TO_A_CONSTRUCT);
        }

        STNodeList annotList = (STNodeList) metadata.annotations;
        STNode annotations = addAnnotNotAttachedDiagnostic(annotList);

        return STNodeFactory.createMetadataNode(docString, annotations);
    }

    private STNode addAnnotNotAttachedDiagnostic(STNodeList annotList) {
        STNode annotations = SyntaxErrors.updateAllNodesInNodeListWithDiagnostic(annotList,
                DiagnosticErrorCode.ERROR_ANNOTATION_NOT_ATTACHED_TO_A_CONSTRUCT);
        return annotations;
    }

    /**
     * Check whether the cursor is at the start of a module level var-decl.
     *
     * @param lookahead Offset of the token to to check
     * @return <code>true</code> if the cursor is at the start of a module level var-decl.
     * <code>false</code> otherwise.
     */
    private boolean isModuleVarDeclStart(int lookahead) {
        // Assumes that we reach here after a peek()
        STToken nextToken = peek(lookahead + 1);
        switch (nextToken.kind) {
            case EQUAL_TOKEN: // Scenario: foo = . Even though this is not valid, consider this as a var-decl and
                // continue;
            case OPEN_BRACKET_TOKEN: // Scenario foo[] (Array type descriptor with custom type)
            case QUESTION_MARK_TOKEN: // Scenario foo? (Optional type descriptor with custom type)
            case PIPE_TOKEN: // Scenario foo | (Union type descriptor with custom type)
            case BITWISE_AND_TOKEN: // Scenario foo & (Intersection type descriptor with custom type)
            case OPEN_BRACE_TOKEN: // Scenario foo{} (mapping-binding-pattern)
            case ERROR_KEYWORD: // Scenario foo error (error-binding-pattern)
            case EOF_TOKEN:
                return true;
            case IDENTIFIER_TOKEN:
                switch (peek(lookahead + 2).kind) {
                    case EQUAL_TOKEN: // Scenario: foo bar =
                    case SEMICOLON_TOKEN: // Scenario: foo bar;
                    case EOF_TOKEN:
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

                switch (peek(lookahead + 2).kind) {
                    case IDENTIFIER_TOKEN: // Scenario: foo:bar baz ...
                        return isModuleVarDeclStart(lookahead + 2);
                    case EOF_TOKEN: // Scenario: foo: recovery
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    /**
     * Parse import declaration.
     * <p>
     * <code>import-decl :=  import [org-name /] module-name [as import-prefix] ;</code>
     *
     * @return Parsed node
     */
    private STNode parseImportDecl() {
        startContext(ParserRuleContext.IMPORT_DECL);
        this.tokenReader.startMode(ParserMode.IMPORT);
        STNode importKeyword = parseImportKeyword();
        STNode identifier = parseIdentifier(ParserRuleContext.IMPORT_ORG_OR_MODULE_NAME);
        STNode importDecl = parseImportDecl(importKeyword, identifier);
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
            recover(token, ParserRuleContext.IMPORT_KEYWORD);
            return parseImportKeyword();
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
        } else if (token.kind == SyntaxKind.MAP_KEYWORD) {
            STToken mapKeyword = consume();
            return STNodeFactory.createIdentifierToken(mapKeyword.text(), mapKeyword.leadingMinutiae(),
                    mapKeyword.trailingMinutiae(), mapKeyword.diagnostics());
        } else {
            recover(token, currentCtx);
            return parseIdentifier(currentCtx);
        }
    }

    /**
     * Parse RHS of the import declaration. This includes the components after the
     * starting identifier (org-name/module-name) of the import decl.
     *
     * @param importKeyword Import keyword
     * @param identifier    Org-name or the module name
     * @return Parsed node
     */
    private STNode parseImportDecl(STNode importKeyword, STNode identifier) {
        STToken nextToken = peek();
        STNode orgName;
        STNode moduleName;
        STNode alias;

        switch (nextToken.kind) {
            case SLASH_TOKEN:
                STNode slash = parseSlashToken();
                orgName = STNodeFactory.createImportOrgNameNode(identifier, slash);
                moduleName = parseModuleName();
                parseVersion(); // Parse version and log an error
                alias = parseImportPrefixDecl();
                break;
            case DOT_TOKEN:
            case VERSION_KEYWORD:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(identifier);
                parseVersion(); // Parse version and log an error
                alias = parseImportPrefixDecl();
                break;
            case AS_KEYWORD:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(identifier);
                alias = parseImportPrefixDecl();
                break;
            case SEMICOLON_TOKEN:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(identifier);
                alias = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.IMPORT_DECL_ORG_OR_MODULE_NAME_RHS);
                return parseImportDecl(importKeyword, identifier);
        }

        STNode semicolon = parseSemicolon();
        return STNodeFactory.createImportDeclarationNode(importKeyword, orgName, moduleName, alias, semicolon);
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
            recover(token, ParserRuleContext.SLASH);
            return parseSlashToken();
        }
    }

    /**
     * Parse dot token.
     *
     * @return Parsed node
     */
    private STNode parseDotToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DOT_TOKEN) {
            return consume();
        } else {
            recover(peek(), ParserRuleContext.DOT);
            return parseDotToken();
        }
    }

    /**
     * Parse module name of a import declaration.
     *
     * @return Parsed node
     */
    private STNode parseModuleName() {
        STNode moduleNameStart = parseIdentifier(ParserRuleContext.IMPORT_MODULE_NAME);
        return parseModuleName(moduleNameStart);
    }

    /**
     * Parse import module name of a import declaration, given the module name start identifier.
     *
     * @return Parsed node
     */
    private STNode parseModuleName(STNode moduleNameStart) {
        List<STNode> moduleNameParts = new ArrayList<>();
        moduleNameParts.add(moduleNameStart);

        STToken nextToken = peek();
        while (!isEndOfImportDecl(nextToken)) {
            STNode moduleNameSeparator = parseModuleNameRhs();
            if (moduleNameSeparator == null) {
                break;
            }

            moduleNameParts.add(moduleNameSeparator);
            moduleNameParts.add(parseIdentifier(ParserRuleContext.IMPORT_MODULE_NAME));
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(moduleNameParts);
    }

    private STNode parseModuleNameRhs() {
        switch(peek().kind) {
            case DOT_TOKEN:
                return consume();
            case AS_KEYWORD:
            case VERSION_KEYWORD:
            case SEMICOLON_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.AFTER_IMPORT_MODULE_NAME);
                return parseModuleNameRhs();
        }
    }

    private boolean isEndOfImportDecl(STToken nextToken) {
        switch (nextToken.kind) {
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
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse version component of a import declaration.
     * <p>
     * <code>version-decl := version sem-ver</code>
     * @deprecated
     * Version is no longer supported. Hence, parse it and log an error.
     **/
    @Deprecated
    private void parseVersion() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case VERSION_KEYWORD:
                STNode versionKeyword = parseVersionKeyword();
                STNode versionNumber = parseVersionNumber();
                addInvalidNodeToNextToken(versionKeyword,
                        DiagnosticErrorCode.ERROR_VERSION_IN_IMPORT_DECLARATION_NO_LONGER_SUPPORTED);
                addInvalidNodeToNextToken(versionNumber, null);
                return;
            case AS_KEYWORD:
            case SEMICOLON_TOKEN:
                return;
            default:
                if (isEndOfImportDecl(nextToken)) {
                    return;
                }

                recover(peek(), ParserRuleContext.IMPORT_VERSION_DECL);
                parseVersion();
        }
    }

    /**
     * Parse version keyword.
     *
     * @return Parsed node
     */
    private STNode parseVersionKeyword() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.VERSION_KEYWORD) {
            return consume();
        } else {
            recover(peek(), ParserRuleContext.VERSION_KEYWORD);
            return parseVersionKeyword();
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
        STNode majorVersion;
        switch (nextToken.kind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
                majorVersion = parseMajorVersion();
                break;
            default:
                recover(peek(), ParserRuleContext.VERSION_NUMBER);
                return parseVersionNumber();
        }

        List<STNode> versionParts = new ArrayList<>();
        versionParts.add(majorVersion);

        STNode minorVersionEnd = parseSubVersionEnd();
        if (minorVersionEnd != null) {
            versionParts.add(minorVersionEnd);
            STNode minorVersion = parseMinorVersion();
            versionParts.add(minorVersion);

            STNode patchVersionEnd = parseSubVersionEnd();
            if (patchVersionEnd != null) {
                versionParts.add(patchVersionEnd);
                STNode patchVersion = parsePatchVersion();
                versionParts.add(patchVersion);
            }
        }

        return STNodeFactory.createNodeList(versionParts);

    }

    private STNode parseMajorVersion() {
        return parseDecimalIntLiteral(ParserRuleContext.MAJOR_VERSION);
    }

    private STNode parseMinorVersion() {
        return parseDecimalIntLiteral(ParserRuleContext.MINOR_VERSION);
    }

    private STNode parsePatchVersion() {
        return parseDecimalIntLiteral(ParserRuleContext.PATCH_VERSION);
    }

    /**
     * Parse decimal literal.
     *
     * @param context Context in which the decimal literal is used.
     * @return Parsed node
     */
    private STNode parseDecimalIntLiteral(ParserRuleContext context) {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN) {
            return consume();
        } else {
            recover(peek(), context);
            return parseDecimalIntLiteral(context);
        }
    }

    private STNode parseSubVersionEnd() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case AS_KEYWORD:
            case SEMICOLON_TOKEN:
            case EOF_TOKEN:
                return null;
            case DOT_TOKEN:
                return parseDotToken();
            default:
                recover(nextToken, ParserRuleContext.IMPORT_SUB_VERSION);
                return parseSubVersionEnd();
        }
    }

    /**
     * Parse import prefix declaration.
     * <p>
     * <code>import-prefix-decl := as import-prefix</code>
     *
     * @return Parsed node
     */
    private STNode parseImportPrefixDecl() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case AS_KEYWORD:
                STNode asKeyword = parseAsKeyword();
                STNode prefix = parseImportPrefix();
                return STNodeFactory.createImportPrefixNode(asKeyword, prefix);
            case SEMICOLON_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                if (isEndOfImportDecl(nextToken)) {
                    return STNodeFactory.createEmptyNode();
                }

                recover(peek(), ParserRuleContext.IMPORT_PREFIX_DECL);
                return parseImportPrefixDecl();
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
            recover(peek(), ParserRuleContext.AS_KEYWORD);
            return parseAsKeyword();
        }
    }

    /**
     * Parse import prefix.
     * <p>
     * <code>
     * import-prefix := module-prefix | _
     * <br/>
     * module-prefix := identifier | predeclared-prefix
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseImportPrefix() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STToken identifier = consume();
            if (isUnderscoreToken(identifier)) {
                return getUnderscoreKeyword(identifier);
            }
            return identifier;
        } else if (isPredeclaredPrefix(nextToken.kind)) {
            STToken preDeclaredPrefix = consume();
            return STNodeFactory.createIdentifierToken(preDeclaredPrefix.text(), preDeclaredPrefix.leadingMinutiae(),
                    preDeclaredPrefix.trailingMinutiae());
        } else {
            recover(peek(), ParserRuleContext.IMPORT_PREFIX);
            return parseImportPrefix();
        }
    }

    /**
     * Parse top level node, given the modifier that precedes it.
     *
     * @param metadata        Metadata that precedes the top level node
     * @param publicQualifier Public qualifier that precedes the top level node
     * @return Parsed node
     */
    private STNode parseTopLevelNode(STNode metadata, STNode publicQualifier) {
        List<STNode> topLevelQualifiers = new ArrayList<>();
        return parseTopLevelNode(metadata, publicQualifier, topLevelQualifiers);
    }

    private STNode parseTopLevelNode(STNode metadata, STNode publicQualifier, List<STNode> qualifiers) {
        parseTopLevelQualifiers(qualifiers);
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EOF_TOKEN:
                return createMissingSimpleVarDecl(metadata, publicQualifier, qualifiers, true);
            case FUNCTION_KEYWORD:
                // Anything starts with a function keyword could be a function definition
                // or a module-var-decl with function type desc.
                return parseFuncDefOrFuncTypeDesc(metadata, publicQualifier, qualifiers, false, false);
            case TYPE_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseModuleTypeDefinition(metadata, publicQualifier);
            case CLASS_KEYWORD:
                return parseClassDefinition(metadata, publicQualifier, qualifiers);
            case LISTENER_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseListenerDeclaration(metadata, publicQualifier);
            case CONST_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseConstantDeclaration(metadata, publicQualifier);
            case ANNOTATION_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                STNode constKeyword = STNodeFactory.createEmptyNode();
                return parseAnnotationDeclaration(metadata, publicQualifier, constKeyword);
            case IMPORT_KEYWORD:
                reportInvalidMetaData(metadata, "import declaration");
                reportInvalidQualifier(publicQualifier);
                reportInvalidQualifierList(qualifiers);
                return parseImportDecl();
            case XMLNS_KEYWORD:
                reportInvalidMetaData(metadata, "XML namespace declaration");
                reportInvalidQualifier(publicQualifier);
                reportInvalidQualifierList(qualifiers);
                return parseXMLNamespaceDeclaration(true);
            case ENUM_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseEnumDeclaration(metadata, publicQualifier);
            case IDENTIFIER_TOKEN:
                // Here we assume that after recovering, we'll never reach here.
                // Otherwise the tokenOffset will not be 1.
                if (isModuleVarDeclStart(1)) {
                    return parseModuleVarDecl(metadata, publicQualifier, qualifiers);
                }
                // fall through
            default:
                if (isPossibleServiceDecl(qualifiers)) {
                    return parseServiceDeclOrVarDecl(metadata, publicQualifier, qualifiers);
                }

                if (isTypeStartingToken(nextToken.kind) && nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseModuleVarDecl(metadata, publicQualifier, qualifiers);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_MODIFIER);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    return parseModuleVarDecl(metadata, publicQualifier, qualifiers);
                }

                return parseTopLevelNode(metadata, publicQualifier, qualifiers);
        }
    }

    private STNode parseModuleVarDecl(STNode metadata) {
        List<STNode> emptyList = new ArrayList<>();
        STNode publicQualifier = STNodeFactory.createEmptyNode();
        return parseVariableDecl(metadata, publicQualifier, emptyList, emptyList, true);
    }

    /**
     * <p>
     * Parse module variable declaration.
     * </p>
     *
     * <code>
     * module-var-decl := module-init-var-decl | module-no-init-var-decl
     * <br/><br/>
     * module-init-var-decl := metadata [public] module-init-var-quals typed-binding-pattern = module-var-init ;
     * <br/><br/>
     * module-no-init-var-decl := metadata [public] [final] type-descriptor variable-name ;
     * <br/><br/>
     * module-init-var-quals := (final | isolated-qual )* | configurable
     * <br/><br/>
     * module-var-init := expression | ?
     * </code>
     *
     * @param metadata           Preceding metadata
     * @param topLevelQualifiers Preceding top level qualifiers
     * @return Parsed node
     */
    private STNode parseModuleVarDecl(STNode metadata, STNode publicQualifier, List<STNode> topLevelQualifiers) {
        List<STNode> varDeclQuals = extractVarDeclQualifiers(topLevelQualifiers, true);
        return parseVariableDecl(metadata, publicQualifier, varDeclQuals, topLevelQualifiers, true);
    }

    private List<STNode> extractVarDeclQualifiers(List<STNode> qualifiers, boolean isModuleVar) {
        // Check if the first two qualifiers are belong to the variable declaration.
        // If they do, extract them to a separate list and return.
        List<STNode> varDeclQualList = new ArrayList<>();
        final int initialListSize = qualifiers.size();
        int configurableQualIndex = -1;
        for (int i = 0; i < 2 && i < initialListSize; i++) {
            SyntaxKind qualifierKind = qualifiers.get(0).kind;
            if (!isSyntaxKindInList(varDeclQualList, qualifierKind) && isModuleVarDeclQualifier(qualifierKind)) {
                varDeclQualList.add(qualifiers.remove(0));
                if (qualifierKind == SyntaxKind.CONFIGURABLE_KEYWORD) {
                    configurableQualIndex = i;
                }
                continue;
            }
            break;
        }

        // Invalidate other qualifiers with configurable
        if (isModuleVar && configurableQualIndex > -1) {
            STNode configurableQual = varDeclQualList.get(configurableQualIndex);
            for (int i = 0; i < varDeclQualList.size(); i++) {
                if (i < configurableQualIndex) {
                    STNode invalidQual = varDeclQualList.get(i);
                    configurableQual = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(configurableQual, invalidQual,
                            getInvalidQualifierError(invalidQual.kind), ((STToken) invalidQual).text());
                } else if (i > configurableQualIndex) {
                    STNode invalidQual = varDeclQualList.get(i);
                    configurableQual = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(configurableQual, invalidQual,
                            getInvalidQualifierError(invalidQual.kind), ((STToken) invalidQual).text());
                }
            }
            varDeclQualList = new ArrayList<>(Collections.singletonList(configurableQual));
        }

        return varDeclQualList;
    }

    private DiagnosticErrorCode getInvalidQualifierError(SyntaxKind qualifierKind) {
        return qualifierKind == SyntaxKind.FINAL_KEYWORD ? DiagnosticErrorCode.ERROR_CONFIGURABLE_VAR_IMPLICITLY_FINAL :
                DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED;
    }

    boolean isModuleVarDeclQualifier(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case FINAL_KEYWORD:
            case ISOLATED_KEYWORD:
            case CONFIGURABLE_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    private void reportInvalidQualifier(STNode qualifier) {
        if (qualifier != null && qualifier.kind != SyntaxKind.NONE) {
            addInvalidNodeToNextToken(qualifier, DiagnosticErrorCode.ERROR_INVALID_QUALIFIER,
                    ((STToken) qualifier).text());
        }
    }

    private void reportInvalidMetaData(STNode metadata, String constructName) {
        if (metadata != null && metadata.kind != SyntaxKind.NONE) {
            addInvalidNodeToNextToken(metadata, DiagnosticErrorCode.ERROR_INVALID_METADATA, constructName);
        }
    }

    private void reportInvalidQualifierList(List<STNode> qualifiers) {
        for (STNode qual : qualifiers) {
            addInvalidNodeToNextToken(qual, DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qual).text());
        }
    }

    private void reportInvalidStatementAnnots(STNode annots, List<STNode> qualifiers) {
        DiagnosticErrorCode diagnosticErrorCode = DiagnosticErrorCode.ERROR_ANNOTATIONS_ATTACHED_TO_STATEMENT;
        reportInvalidAnnotations(annots, qualifiers, diagnosticErrorCode);
    }

    private void reportInvalidExpressionAnnots(STNode annots, List<STNode> qualifiers) {
        DiagnosticErrorCode diagnosticErrorCode = DiagnosticErrorCode.ERROR_ANNOTATIONS_ATTACHED_TO_EXPRESSION;
        reportInvalidAnnotations(annots, qualifiers, diagnosticErrorCode);
    }

    private void reportInvalidAnnotations(STNode annots, List<STNode> qualifiers, DiagnosticErrorCode errorCode) {
        if (isNodeListEmpty(annots)) {
            return;
        }

        if (qualifiers.isEmpty()) {
            addInvalidNodeToNextToken(annots, errorCode);
        } else {
            updateFirstNodeInListWithLeadingInvalidNode(qualifiers, annots, errorCode);
        }
    }

    private boolean isTopLevelQualifier(SyntaxKind tokenKind) {
        STToken nextNextToken;
        switch (tokenKind) {
            case FINAL_KEYWORD: // final-qualifier
            case CONFIGURABLE_KEYWORD: // configurable-qualifier
                return true;
            case READONLY_KEYWORD: // readonly-type-desc, class-def
                nextNextToken = getNextNextToken();
                // Treat readonly as a top level qualifier only with class definition.
                switch (nextNextToken.kind) {
                    case CLIENT_KEYWORD:
                    case SERVICE_KEYWORD:
                    case DISTINCT_KEYWORD:
                    case ISOLATED_KEYWORD:
                    case CLASS_KEYWORD:
                        return true;
                    default:
                        return false;
                }
            case DISTINCT_KEYWORD: // class-def, distinct-type-desc
                nextNextToken = getNextNextToken();
                // distinct-type-desc can occur recursively.
                // e.g. `distinct distinct student` is a valid type descriptor
                // Treat distinct as a top level qualifier only with class definition.
                switch (nextNextToken.kind) {
                    case CLIENT_KEYWORD:
                    case SERVICE_KEYWORD:
                    case READONLY_KEYWORD:
                    case ISOLATED_KEYWORD:
                    case CLASS_KEYWORD:
                        return true;
                    default:
                        return false;
                }
            default:
                return isTypeDescQualifier(tokenKind);
        }
    }

    private boolean isTypeDescQualifier(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case TRANSACTIONAL_KEYWORD: // func-type-dec, func-def
            case ISOLATED_KEYWORD: // func-type-dec, object-type-desc, func-def, class-def, isolated-final-qual
            case CLIENT_KEYWORD: // object-type-desc, class-def
            case ABSTRACT_KEYWORD: // object-type-desc(outdated)
            case SERVICE_KEYWORD: // object-type-desc, object-constructor, class-def, service-decl
                return true;
            default:
                return false;
        }
    }

    private boolean isObjectMemberQualifier(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case REMOTE_KEYWORD: // method-def, method-decl
            case RESOURCE_KEYWORD: // resource-method-def
            case FINAL_KEYWORD: // final-qualifier
                return true;
            default:
                return isTypeDescQualifier(tokenKind);
        }
    }

    private boolean isExprQualifier(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case TRANSACTIONAL_KEYWORD: // transactional-expr, object-type, func-type
                STToken nextNextToken = getNextNextToken();
                // Treat transactional as a expr level qualifier only with object-type and func-type.
                switch (nextNextToken.kind) {
                    case CLIENT_KEYWORD:
                    case ABSTRACT_KEYWORD:
                    case ISOLATED_KEYWORD:
                    case OBJECT_KEYWORD:
                    case FUNCTION_KEYWORD:
                        return true;
                    default:
                        return false;
                }
            default:
                return isTypeDescQualifier(tokenKind);
        }
    }

    private void parseTopLevelQualifiers(List<STNode> qualifiers) {
        while (isTopLevelQualifier(peek().kind)) {
            STToken qualifier = consume();
            qualifiers.add(qualifier);
        }
    }

    private void parseTypeDescQualifiers(List<STNode> qualifiers) {
        while (isTypeDescQualifier(peek().kind)) {
            STToken qualifier = consume();
            qualifiers.add(qualifier);
        }
    }

    private void parseObjectMemberQualifiers(List<STNode> qualifiers) {
        while (isObjectMemberQualifier(peek().kind)) {
            STToken qualifier = consume();
            qualifiers.add(qualifier);
        }
    }

    private void parseExprQualifiers(List<STNode> qualifiers) {
        while (isExprQualifier(peek().kind)) {
            STToken qualifier = consume();
            qualifiers.add(qualifier);
        }
    }

    /**
     * Parse optional relative resource path.
     *
     * @param isObjectMember Whether object member or not
     * @return Parsed node
     */
    private STNode parseOptionalRelativePath(boolean isObjectMember) {
        STNode resourcePath;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case DOT_TOKEN:
            case IDENTIFIER_TOKEN:
            case OPEN_BRACKET_TOKEN:
                resourcePath = parseRelativeResourcePath();
                break;
            case OPEN_PAREN_TOKEN:
                return STNodeFactory.createEmptyNodeList();
            default:
                recover(nextToken, ParserRuleContext.OPTIONAL_RELATIVE_PATH);
                return parseOptionalRelativePath(isObjectMember);
        }

        if (!isObjectMember) {
            addInvalidNodeToNextToken(resourcePath, DiagnosticErrorCode.ERROR_RESOURCE_PATH_IN_FUNCTION_DEFINITION);
            return STNodeFactory.createEmptyNodeList();
        }

        return resourcePath;
    }

    /**
     * Parse function definition for the function type descriptor.
     * <p>
     * <code>
     * function-defn := `function` identifier function-signature function-body
     * <br/>
     * function-type-descriptor := `function` function-signature
     * </code>
     *
     * @param metadata Preceding metadata
     * @param visibilityQualifier Preceding visibility qualifier
     * @param qualifiers Preceding visibility qualifier
     * @param isObjectMember Whether object member or not
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed node
     */
    private STNode parseFuncDefOrFuncTypeDesc(STNode metadata, STNode visibilityQualifier, List<STNode> qualifiers,
                                              boolean isObjectMember, boolean isObjectTypeDesc) {
        startContext(ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE);
        STNode functionKeyword = parseFunctionKeyword();
        STNode funcDefOrType = parseFunctionKeywordRhs(metadata, visibilityQualifier, qualifiers, functionKeyword,
                isObjectMember, isObjectTypeDesc);
        return funcDefOrType;
    }

    private STNode parseFunctionDefinition(STNode metadata, STNode visibilityQualifier, STNode resourcePath,
                                           List<STNode> qualifiers, STNode functionKeyword, STNode name,
                                           boolean isObjectMember, boolean isObjectTypeDesc) {
        switchContext(ParserRuleContext.FUNC_DEF);
        STNode funcSignature = parseFuncSignature(false);
        STNode funcDef = parseFuncDefOrMethodDeclEnd(metadata, visibilityQualifier, qualifiers, functionKeyword, name,
                                                     resourcePath, funcSignature, isObjectMember, isObjectTypeDesc);
        endContext();
        return funcDef;
    }

    /**
     * Parse function definition or function type descriptor identifier rhs.
     *
     * @param metadata Preceding metadata
     * @param visibilityQualifier Preceding visibility qualifier
     * @param qualifiers Preceding visibility qualifier
     * @param functionKeyword `function` keyword
     * @param isObjectMember Whether object member or not
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed node
     */
    private STNode parseFuncDefOrFuncTypeDescRhs(STNode metadata, STNode visibilityQualifier, List<STNode> qualifiers,
                                                 STNode functionKeyword, STNode name, boolean isObjectMember,
                                                 boolean isObjectTypeDesc) {
        switch (peek().kind) {
            case OPEN_PAREN_TOKEN:
            case DOT_TOKEN:
            case IDENTIFIER_TOKEN:
            case OPEN_BRACKET_TOKEN:
                STNode resourcePath = parseOptionalRelativePath(isObjectMember);
                return parseFunctionDefinition(metadata, visibilityQualifier, resourcePath, qualifiers, functionKeyword,
                                               name, isObjectMember, isObjectTypeDesc);
            case EQUAL_TOKEN:
            case SEMICOLON_TOKEN:
                endContext();
                List<STNode> extractQualifiersList = extractVarDeclOrObjectFieldQualifiers(qualifiers, isObjectMember,
                                                                                           isObjectTypeDesc);
                STNode typeDesc = createFunctionTypeDescriptor(qualifiers, functionKeyword,
                                                               STNodeFactory.createEmptyNode(), false);

                if (isObjectMember) {
                    STNode objectFieldQualNodeList = STNodeFactory.createNodeList(extractQualifiersList);
                    return parseObjectFieldRhs(metadata, visibilityQualifier, objectFieldQualNodeList, typeDesc, name,
                                               isObjectTypeDesc);
                }
                startContext(ParserRuleContext.VAR_DECL_STMT);
                STNode funcTypeName = STNodeFactory.createSimpleNameReferenceNode(name);
                STNode bindingPattern = createCaptureOrWildcardBP(((STSimpleNameReferenceNode) funcTypeName).name);
                STNode typedBindingPattern = STNodeFactory.createTypedBindingPatternNode(typeDesc, bindingPattern);
                return parseVarDeclRhs(metadata, visibilityQualifier, extractQualifiersList, typedBindingPattern, true);
            default:
            STToken token = peek();
            recover(token, ParserRuleContext.FUNC_DEF_OR_TYPE_DESC_RHS);
            return parseFuncDefOrFuncTypeDescRhs(metadata, visibilityQualifier, qualifiers, functionKeyword, name,
                                                 isObjectMember, isObjectTypeDesc);
        }
    }

    private STNode parseFunctionKeywordRhs(STNode metadata, STNode visibilityQualifier, List<STNode> qualifiers,
                                           STNode functionKeyword, boolean isObjectMember, boolean isObjectTypeDesc) {
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                STNode name = consume();
                return parseFuncDefOrFuncTypeDescRhs(metadata, visibilityQualifier, qualifiers, functionKeyword, name,
                                                     isObjectMember, isObjectTypeDesc);
            case OPEN_PAREN_TOKEN:
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                startContext(ParserRuleContext.FUNC_TYPE_DESC);
                STNode funcSignature = parseFuncSignature(true);
                endContext();
                endContext();
                return parseFunctionTypeDescRhs(metadata, visibilityQualifier, qualifiers, functionKeyword,
                        funcSignature, isObjectMember, isObjectTypeDesc);
            default:
                STToken token = peek();
                if (isValidTypeContinuationToken(token) || isBindingPatternsStartToken(token.kind)) {
                    return parseVarDeclWithFunctionType(metadata, visibilityQualifier, qualifiers, functionKeyword,
                                                        STNodeFactory.createEmptyNode(), isObjectMember,
                                                        isObjectTypeDesc, false);
                }

                recover(token, ParserRuleContext.FUNCTION_KEYWORD_RHS);
                return parseFunctionKeywordRhs(metadata, visibilityQualifier, qualifiers, functionKeyword,
                        isObjectMember, isObjectTypeDesc);
        }
    }

    private boolean isBindingPatternsStartToken(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case ERROR_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * <p>
     * Parse function definition, object method definition, object method declaration,
     * resource accessor definition or resource accessor declaration end.
     * </p>
     *
     * @return Parsed node
     */
    private STNode parseFuncDefOrMethodDeclEnd(STNode metadata, STNode visibilityQualifier, List<STNode> qualifierList,
                                               STNode functionKeyword, STNode name, STNode resourcePath,
                                               STNode funcSignature, boolean isObjectMember, boolean isObjectTypeDesc) {
        if (!isObjectMember) {
            return createFunctionDefinition(metadata, visibilityQualifier, qualifierList, functionKeyword, name,
                    funcSignature);

        }

        boolean hasResourcePath = !isNodeListEmpty(resourcePath);
        boolean hasResourceQual = isSyntaxKindInList(qualifierList, SyntaxKind.RESOURCE_KEYWORD);
        if (hasResourceQual && !hasResourcePath) {
            // create missing relative path and direct towards resource accessor definition / declaration
            List<STNode> relativePath = new ArrayList<>();
            relativePath.add(STNodeFactory.createMissingToken(SyntaxKind.DOT_TOKEN));
            resourcePath = STNodeFactory.createNodeList(relativePath);
            DiagnosticErrorCode errorCode;
            if (isObjectTypeDesc) {
                errorCode = DiagnosticErrorCode.ERROR_MISSING_RESOURCE_PATH_IN_RESOURCE_ACCESSOR_DECLARATION;
            } else {
                errorCode = DiagnosticErrorCode.ERROR_MISSING_RESOURCE_PATH_IN_RESOURCE_ACCESSOR_DEFINITION;
            }
            name = SyntaxErrors.addDiagnostic(name, errorCode);
            hasResourcePath = true;
        }


        if (hasResourcePath) {
            return createResourceAccessorDefnOrDecl(metadata, visibilityQualifier, qualifierList, functionKeyword, name,
                    resourcePath, funcSignature, isObjectTypeDesc);
        }

        if (isObjectTypeDesc) {
            return createMethodDeclaration(metadata, visibilityQualifier, qualifierList, functionKeyword, name,
                    funcSignature);
        } else {
            return createMethodDefinition(metadata, visibilityQualifier, qualifierList, functionKeyword, name,
                   funcSignature);
        }
    }

    /**
     * Parse function definition.
     * <p>
     * <code>
     * function-defn :=
     *    metadata [public] (isolated-qual | transactional-qual)*
     *    `function` identifier function-signature function-defn-body
     * </code>
     *
     * @return Parsed node
     */
    private STNode createFunctionDefinition(STNode metadata, STNode visibilityQualifier, List<STNode> qualifierList,
                                            STNode functionKeyword, STNode name, STNode funcSignature) {
        /*
         * Validate qualifier list.
         * Rules:
         * - Isolated and transactional are allowed
         * - Remote and resource are not allowed (already validated)
         * - Visibility qualifier is allowed and it is the first qualifier in the list
         */
        List<STNode> validatedList = new ArrayList<>();
        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            if (isRegularFuncQual(qualifier.kind)) {
                validatedList.add(qualifier);
                continue;
            }

            // We only reach here for invalid qualifiers
            if (qualifierList.size() == nextIndex) {
                functionKeyword = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(functionKeyword, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        if (visibilityQualifier != null) {
            validatedList.add(0, visibilityQualifier);
        }

        STNode qualifiers = STNodeFactory.createNodeList(validatedList);
        STNode resourcePath = STNodeFactory.createEmptyNodeList();
        STNode body = parseFunctionBody();
        return STNodeFactory.createFunctionDefinitionNode(SyntaxKind.FUNCTION_DEFINITION, metadata, qualifiers,
                functionKeyword, name, resourcePath, funcSignature, body);
    }

    /**
     * Parse method definition.
     * <p>
     * <code>
     * method-defn :=
     *    metadata method-defn-quals
     *    function method-name function-signature method-defn-body
     * <br/>
     * method-defn-quals := object-visibility-qual function-qual* | (remote-qual | function-qual)*
     * <br/>
     * function-qual := isolated-qual | transactional-qual
     * <br/>
     * object-visibility-qual := public|private
     * </code>
     *
     * @return Parsed node
     */
    private STNode createMethodDefinition(STNode metadata, STNode visibilityQualifier, List<STNode> qualifierList,
                                          STNode functionKeyword, STNode name, STNode funcSignature) {
        /*
         * Validate qualifier list.
         * Rules:
         * - Isolated, transactional and remote are allowed
         * - Resource is not allowed (already validated)
         * - Visibility qualifier is not allowed with remote
         * - If there's a visibility qualifier it is the first qualifier in the list
         */
        List<STNode> validatedList = new ArrayList<>();
        boolean hasRemoteQual = false;

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            if (qualifier.kind == SyntaxKind.REMOTE_KEYWORD) {
                hasRemoteQual = true;
                validatedList.add(qualifier);
                continue;
            }

            if (isRegularFuncQual(qualifier.kind)) {
                validatedList.add(qualifier);
                continue;
            }

            // We only reach here for invalid qualifiers
            if (qualifierList.size() == nextIndex) {
                functionKeyword = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(functionKeyword, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        if (visibilityQualifier != null) {
            if (hasRemoteQual) {
                updateFirstNodeInListWithLeadingInvalidNode(validatedList, visibilityQualifier,
                        DiagnosticErrorCode.ERROR_REMOTE_METHOD_HAS_A_VISIBILITY_QUALIFIER);
            } else {
                validatedList.add(0, visibilityQualifier);
            }
        }

        STNode qualifiers = STNodeFactory.createNodeList(validatedList);
        STNode resourcePath = STNodeFactory.createEmptyNodeList();
        STNode body = parseFunctionBody();
        return STNodeFactory.createFunctionDefinitionNode(SyntaxKind.OBJECT_METHOD_DEFINITION, metadata, qualifiers,
                functionKeyword, name, resourcePath, funcSignature, body);
    }

    /**
     * Parse method declaration.
     * <p>
     * <code>
     * method-decl :=
     *    metadata method-decl-quals
     *    function method-name function-signature ;
     * <br/>
     * method-decl-quals := public function-qual* | (remote-qual | function-qual)*
     * <br/>
     * function-qual := isolated-qual | transactional-qual
     * </code>
     *
     * @return Parsed node
     */
    private STNode createMethodDeclaration(STNode metadata, STNode visibilityQualifier, List<STNode> qualifierList,
                                           STNode functionKeyword, STNode name, STNode funcSignature) {
        /*
         * Validate qualifier list.
         * Rules:
         * - Isolated, transactional and remote are allowed
         * - Resource is not allowed
         * - Visibility qualifier is not allowed with remote
         * - If there's a visibility qualifier it is the first qualifier in the list
         */
        List<STNode> validatedList = new ArrayList<>();
        boolean hasRemoteQual = false;

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            if (qualifier.kind == SyntaxKind.REMOTE_KEYWORD) {
                hasRemoteQual = true;
                validatedList.add(qualifier);
                continue;
            }

            if (isRegularFuncQual(qualifier.kind)) {
                validatedList.add(qualifier);
                continue;
            }

            // We only reach here for invalid qualifiers
            if (qualifierList.size() == nextIndex) {
                functionKeyword = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(functionKeyword, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        if (visibilityQualifier != null) {
            if (hasRemoteQual) {
                updateFirstNodeInListWithLeadingInvalidNode(validatedList, visibilityQualifier,
                        DiagnosticErrorCode.ERROR_REMOTE_METHOD_HAS_A_VISIBILITY_QUALIFIER);
            } else {
                validatedList.add(0, visibilityQualifier);
            }
        }

        STNode qualifiers = STNodeFactory.createNodeList(validatedList);
        STNode resourcePath = STNodeFactory.createEmptyNodeList();
        STNode semicolon = parseSemicolon();
        return STNodeFactory.createMethodDeclarationNode(SyntaxKind.METHOD_DECLARATION, metadata, qualifiers,
                functionKeyword, name, resourcePath, funcSignature, semicolon);
    }

    /**
     * Parse resource accessor definition or declaration.
     * <p>
     * <code>
     * resource-accessor-defn :=
     *    metadata `resource` `function` accessor-name relative-resource-path
     *    function-signature method-defn-body
     * <br/>
     * resource-accessor-decl :=
     *    metadata `resource` `function` accessor-name relative-resource-path
     *    function-signature ;
     * <br/>
     * accessor-name := identifier
     * <br/>
     * relative-resource-path := "." | (identifier ("/" identifier)*)
     * </code>
     *
     * @return Parsed node
     */
    private STNode createResourceAccessorDefnOrDecl(STNode metadata, STNode visibilityQualifier,
                                                    List<STNode> qualifierList, STNode functionKeyword, STNode name,
                                                    STNode resourcePath, STNode funcSignature,
                                                    boolean isObjectTypeDesc) {
        /*
         * Validate qualifier list.
         * Rules:
         * - Isolated, transactional and resource are allowed.
         * - Remote is not allowed.
         * - Visibility qualifier is not allowed
         */
        List<STNode> validatedList = new ArrayList<>();
        boolean hasResourceQual = false;

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            if (qualifier.kind == SyntaxKind.RESOURCE_KEYWORD) {
                hasResourceQual = true;
                validatedList.add(qualifier);
                continue;
            }

            if (isRegularFuncQual(qualifier.kind)) {
                validatedList.add(qualifier);
                continue;
            }

            // We only reach here for invalid qualifiers
            if (qualifierList.size() == nextIndex) {
                functionKeyword = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(functionKeyword, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        if (!hasResourceQual) {
            validatedList.add(STNodeFactory.createMissingToken(SyntaxKind.RESOURCE_KEYWORD));
            functionKeyword =
                    SyntaxErrors.addDiagnostic(functionKeyword, DiagnosticErrorCode.ERROR_MISSING_RESOURCE_KEYWORD);
        }

        if (visibilityQualifier != null) {
            updateFirstNodeInListWithLeadingInvalidNode(validatedList, visibilityQualifier,
                    DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) visibilityQualifier).text());
        }

        STNode qualifiers = STNodeFactory.createNodeList(validatedList);

        if (isObjectTypeDesc) {
            STNode semicolon = parseSemicolon();
            return STNodeFactory.createMethodDeclarationNode(SyntaxKind.RESOURCE_ACCESSOR_DECLARATION, metadata,
                    qualifiers, functionKeyword, name, resourcePath, funcSignature, semicolon);
        } else {
            STNode body = parseFunctionBody();
            return STNodeFactory.createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, metadata,
                    qualifiers, functionKeyword, name, resourcePath, funcSignature, body);
        }
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
     * @return Function signature node
     */
    private STNode parseFuncSignature(boolean isParamNameOptional) {
        STNode openParenthesis = parseOpenParenthesis();
        STNode parameters = parseParamList(isParamNameOptional);
        STNode closeParenthesis = parseCloseParenthesis();
        endContext(); // end param-list
        STNode returnTypeDesc = parseFuncReturnTypeDescriptor(isParamNameOptional);
        return STNodeFactory.createFunctionSignatureNode(openParenthesis, parameters, closeParenthesis, returnTypeDesc);
    }

    private STNode parseFunctionTypeDescRhs(STNode metadata, STNode visibilityQualifier, List<STNode> qualifiers,
                                            STNode functionKeyword, STNode funcSignature, boolean isObjectMember,
                                            boolean isObjectTypeDesc) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACE_TOKEN: // function body block
            case EQUAL_TOKEN: // external function
                break;
            // var-decl with function type
            case SEMICOLON_TOKEN:
            case IDENTIFIER_TOKEN:
            case OPEN_BRACKET_TOKEN:
                // Parse the remaining as var-decl, because its the only module-level construct
                // that can start with a func-type-desc. Constants cannot have func-type-desc.
            default:
                return parseVarDeclWithFunctionType(metadata, visibilityQualifier, qualifiers, functionKeyword,
                                                    funcSignature, isObjectMember, isObjectTypeDesc, true);
        }

        // Treat as function definition.
        switchContext(ParserRuleContext.FUNC_DEF);
        // We reach this method only if the func-name is not present.
        STNode name = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_FUNCTION_NAME);

        // Function definition cannot have missing param-names. So validate it.
        funcSignature = validateAndGetFuncParams((STFunctionSignatureNode) funcSignature);

        STNode resourcePath = STNodeFactory.createEmptyNodeList();
        STNode funcDef = parseFuncDefOrMethodDeclEnd(metadata, visibilityQualifier, qualifiers, functionKeyword,
                name, resourcePath, funcSignature, isObjectMember, isObjectTypeDesc);
        endContext();
        return funcDef;
    }

    private List<STNode> extractVarDeclOrObjectFieldQualifiers(List<STNode> qualifierList, boolean isObjectMember,
                                                               boolean isObjectTypeDesc) {
        if (isObjectMember) {
            return extractObjectFieldQualifiers(qualifierList, isObjectTypeDesc);
        }
        return extractVarDeclQualifiers(qualifierList, false);
    }

    private STNode createFunctionTypeDescriptor(List<STNode> qualifierList, STNode functionKeyword,
                                                STNode funcSignature, boolean hasFuncSignature) {
        // Validate function type qualifiers
        STNode[] nodes = createFuncTypeQualNodeList(qualifierList, functionKeyword, hasFuncSignature);
        STNode qualifierNodeList = nodes[0];
        functionKeyword = nodes[1];

        return STNodeFactory.createFunctionTypeDescriptorNode(qualifierNodeList, functionKeyword, funcSignature);
    }

    private STNode parseVarDeclWithFunctionType(STNode metadata, STNode visibilityQualifier, List<STNode> qualifierList,
                                                STNode functionKeyword, STNode funcSignature, boolean isObjectMember,
                                                boolean isObjectTypeDesc, boolean hasFuncSignature) {
        switchContext(ParserRuleContext.VAR_DECL_STMT);
        List<STNode> extractQualifiersList = extractVarDeclOrObjectFieldQualifiers(qualifierList, isObjectMember,
                                                                                   isObjectTypeDesc);
        STNode typeDesc = createFunctionTypeDescriptor(qualifierList, functionKeyword, funcSignature, hasFuncSignature);

        // Check if it is a complex type desc starting with function type.
        typeDesc = parseComplexTypeDescriptor(typeDesc,
                ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);

        if (isObjectMember) {
            endContext();
            STNode objectFieldQualNodeList = STNodeFactory.createNodeList(extractQualifiersList);
            STNode fieldName = parseVariableName();
            return parseObjectFieldRhs(metadata, visibilityQualifier, objectFieldQualNodeList, typeDesc, fieldName,
                    isObjectTypeDesc);
        }

        STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
        return parseVarDeclRhs(metadata, visibilityQualifier, extractQualifiersList, typedBindingPattern, true);
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
                        param = STNodeFactory.createRequiredParameterNode(requiredParam.annotations,
                                requiredParam.typeName, paramName);
                    }
                    break;
                case DEFAULTABLE_PARAM:
                    STDefaultableParameterNode defaultableParam = (STDefaultableParameterNode) param;
                    if (isEmpty(defaultableParam.paramName)) {
                        param = STNodeFactory
                                .createDefaultableParameterNode(defaultableParam.annotations, defaultableParam.typeName,
                                        paramName, defaultableParam.equalsToken, defaultableParam.expression);
                    }
                    break;
                case REST_PARAM:
                    STRestParameterNode restParam = (STRestParameterNode) param;
                    if (isEmpty(restParam.paramName)) {
                        param = STNodeFactory.createRestParameterNode(restParam.annotations, restParam.typeName,
                                restParam.ellipsisToken, paramName);
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
        return !SyntaxUtils.isSTNodePresent(node);
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
            recover(token, ParserRuleContext.FUNCTION_KEYWORD);
            return parseFunctionKeyword();
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
            recover(token, ParserRuleContext.FUNC_NAME);
            return parseFunctionName();
        }
    }

    /**
     * Parse arg list starting open parenthesis.
     *
     * @return Parsed node
     */
    private STNode parseArgListOpenParenthesis() {
        return parseOpenParenthesis(ParserRuleContext.ARG_LIST_OPEN_PAREN);
    }

    /**
     * Parse open parenthesis.
     *
     * @return Parsed node
     */
    private STNode parseOpenParenthesis() {
        return parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
    }

    /**
     * Parse open parenthesis in a given context.
     *
     * @param ctx Context of the parenthesis
     * @return Parsed node
     */
    private STNode parseOpenParenthesis(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseOpenParenthesis(ctx);
        }
    }

    /**
     * Parse arg list ending close parenthesis.
     *
     * @return Parsed node
     */
    private STNode parseArgListCloseParenthesis() {
        return parseCloseParenthesis(ParserRuleContext.ARG_LIST_CLOSE_PAREN);
    }

    /**
     * Parse close parenthesis.
     *
     * @return Parsed node
     */
    private STNode parseCloseParenthesis() {
        return parseCloseParenthesis(ParserRuleContext.CLOSE_PARENTHESIS);
    }

    /**
     * Parse close parenthesis in a given context.
     *
     * @param ctx Context of the parenthesis
     * @return Parsed node
     */
    private STNode parseCloseParenthesis(ParserRuleContext ctx) {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            return consume();
        } else {
            recover(token, ctx);
            return parseCloseParenthesis(ctx);
        }
    }

    /**
     * <p>
     * Parse parameter list.
     * </p>
     * <code>
     * param-list := required-params [, defaultable-params] [, rest-param]
     * <br/>&nbsp;| defaultable-params [, rest-param]
     * <br/>&nbsp;| [rest-param]
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

        // Parse the first parameter, that has no leading comma
        ArrayList<STNode> paramsList = new ArrayList<>();
        startContext(ParserRuleContext.REQUIRED_PARAM);
        STNode firstParam = parseParameter(SyntaxKind.REQUIRED_PARAM, isParamNameOptional);
        SyntaxKind prevParamKind = firstParam.kind;
        paramsList.add(firstParam);

        // Parse follow-up parameters.
        boolean paramOrderErrorPresent = false;
        token = peek();
        while (!isEndOfParametersList(token.kind)) {
            STNode paramEnd = parseParameterRhs();
            if (paramEnd == null) {
                break;
            }

            endContext();
            if (prevParamKind == SyntaxKind.DEFAULTABLE_PARAM) {
                startContext(ParserRuleContext.DEFAULTABLE_PARAM);
            } else {
                startContext(ParserRuleContext.REQUIRED_PARAM);
            }
            STNode param = parseParameter(prevParamKind, isParamNameOptional);

            if (paramOrderErrorPresent) {
                updateLastNodeInListWithInvalidNode(paramsList, paramEnd, null);
                updateLastNodeInListWithInvalidNode(paramsList, param, null);
            } else {
                DiagnosticCode paramOrderError = validateParamOrder(param, prevParamKind);
                if (paramOrderError == null) {
                    paramsList.add(paramEnd);
                    paramsList.add(param);
                } else {
                    paramOrderErrorPresent = true;
                    updateLastNodeInListWithInvalidNode(paramsList, paramEnd, null);
                    updateLastNodeInListWithInvalidNode(paramsList, param, paramOrderError);
                }
            }

            prevParamKind = param.kind;
            token = peek();
        }

        endContext(); // end the context for the last parameter
        return STNodeFactory.createNodeList(paramsList);
    }

    /**
     * Return the appropriate {@code DiagnosticCode} if there are parameter order issues.
     *
     * @param param         the new parameter
     * @param prevParamKind the SyntaxKind of the previously added parameter
     */
    private DiagnosticCode validateParamOrder(STNode param, SyntaxKind prevParamKind) {
        if (prevParamKind == SyntaxKind.REST_PARAM) {
            return DiagnosticErrorCode.ERROR_PARAMETER_AFTER_THE_REST_PARAMETER;
        } else if (prevParamKind == SyntaxKind.DEFAULTABLE_PARAM && param.kind == SyntaxKind.REQUIRED_PARAM) {
            return DiagnosticErrorCode.ERROR_REQUIRED_PARAMETER_AFTER_THE_DEFAULTABLE_PARAMETER;
        } else {
            return null;
        }
    }

    private boolean isSyntaxKindInList(List<STNode> nodeList, SyntaxKind kind) {
        for (STNode node : nodeList) {
            if (node.kind == kind) {
                return true;
            }
        }
        return false;
    }

    private boolean isPossibleServiceDecl(List<STNode> nodeList) {
        if (nodeList.isEmpty()) {
            return false;
        }

        // Check for [isolated] service match
        STNode firstElement = nodeList.get(0);
        switch (firstElement.kind) {
            case SERVICE_KEYWORD:
                return true;
            case ISOLATED_KEYWORD:
                return nodeList.size() > 1 && nodeList.get(1).kind == SyntaxKind.SERVICE_KEYWORD;
            default:
                return false;
        }
    }

    private STNode parseParameterRhs() {
        return parseParameterRhs(peek().kind);
    }

    private STNode parseParameterRhs(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case COMMA_TOKEN:
                return consume();
            case CLOSE_PAREN_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.PARAM_END);
                return parseParameterRhs();
        }

    }

    /**
     * Parse a single parameter. Parameter can be a required parameter, a defaultable
     * parameter, or a rest parameter.
     *
     * @param annots              Annotation that precedes the parameter
     * @param prevParamKind       Kind of the parameter that precedes current parameter
     * @param isParamNameOptional Whether the param names in the signature is optional or not
     * @return Parsed node
     */
    private STNode parseParameter(STNode annots, SyntaxKind prevParamKind, boolean isParamNameOptional) {
        STNode inclusionSymbol;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ASTERISK_TOKEN:
                inclusionSymbol = consume();
                break;
            case IDENTIFIER_TOKEN:
                inclusionSymbol = STNodeFactory.createEmptyNode();
                break;
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    inclusionSymbol = STNodeFactory.createEmptyNode();
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.PARAMETER_START_WITHOUT_ANNOTATION);
                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not.
                    inclusionSymbol = STNodeFactory.createEmptyNodeList();
                    break;
                }

                return parseParameter(annots, prevParamKind, isParamNameOptional);
        }

        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        return parseAfterParamType(prevParamKind, annots, inclusionSymbol, type, isParamNameOptional);
    }

    /**
     * Parse a single parameter. Parameter can be a required parameter, a defaultable
     * parameter, or a rest parameter.
     *
     * @param prevParamKind       Kind of the parameter that precedes current parameter
     * @param isParamNameOptional Whether the param names in the signature is optional or not.
     * @return Parsed node
     */
    private STNode parseParameter(SyntaxKind prevParamKind, boolean isParamNameOptional) {
        STNode annots;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case AT_TOKEN:
                annots = parseOptionalAnnotations();
                break;
            case ASTERISK_TOKEN:
            case IDENTIFIER_TOKEN:
                annots = STNodeFactory.createEmptyNodeList();
                break;
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    annots = STNodeFactory.createEmptyNodeList();
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.PARAMETER_START);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not.
                    annots = STNodeFactory.createEmptyNodeList();
                    break;
                }

                return parseParameter(prevParamKind, isParamNameOptional);
        }
        return parseParameter(annots, prevParamKind, isParamNameOptional);
    }

    private STNode parseAfterParamType(SyntaxKind prevParamKind, STNode annots, STNode inclusionSymbol, STNode type,
                                       boolean isParamNameOptional) {
        STNode paramName;
        STToken token = peek();
        switch (token.kind) {
            case ELLIPSIS_TOKEN:
                if (inclusionSymbol != null) {
                    type = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(type, inclusionSymbol,
                            DiagnosticErrorCode.REST_PARAMETER_CANNOT_BE_INCLUDED_RECORD_PARAMETER);
                }
                switchContext(ParserRuleContext.REST_PARAM);
                STNode ellipsis = parseEllipsis();
                if (isParamNameOptional && peek().kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    paramName = STNodeFactory.createEmptyNode();
                } else {
                    paramName = parseVariableName();
                }
                return STNodeFactory.createRestParameterNode(annots, type, ellipsis, paramName);
            case IDENTIFIER_TOKEN:
                paramName = parseVariableName();
                return parseParameterRhs(prevParamKind, annots, inclusionSymbol, type, paramName);
            case EQUAL_TOKEN:
                if (!isParamNameOptional) {
                    break;
                }
                // If this is a function-type-desc, then param name is optional, and may not exist
                paramName = STNodeFactory.createEmptyNode();
                return parseParameterRhs(prevParamKind, annots, inclusionSymbol, type, paramName);
            default:
                if (!isParamNameOptional) {
                    break;
                }
                // If this is a function-type-desc, then param name is optional, and may not exist
                paramName = STNodeFactory.createEmptyNode();
                return parseParameterRhs(prevParamKind, annots, inclusionSymbol, type, paramName);
        }

        recover(token, ParserRuleContext.AFTER_PARAMETER_TYPE);
        return parseAfterParamType(prevParamKind, annots, inclusionSymbol, type, isParamNameOptional);
    }

    /**
     * Parse ellipsis.
     *
     * @return Parsed node
     */
    private STNode parseEllipsis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ELLIPSIS_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.ELLIPSIS);
            return parseEllipsis();
        }
    }

    /**
     * <p>
     * Parse the right hand side of a required/defaultable parameter.
     * </p>
     * <code>parameter-rhs := [= expression]</code>
     *
     * @param prevParamKind Kind of the parameter that precedes current parameter
     * @param annots        Annotations attached to the parameter
     * @param type          Type descriptor
     * @param paramName     Name of the parameter
     * @return Parsed parameter node
     */
    private STNode parseParameterRhs(SyntaxKind prevParamKind, STNode annots, STNode inclusionSymbol, STNode type,
                                     STNode paramName) {
        STToken nextToken = peek();
        // Required parameters
        if (isEndOfParameter(nextToken.kind)) {
            if (inclusionSymbol != null) {
                return STNodeFactory.createIncludedRecordParameterNode(annots, inclusionSymbol, type, paramName);
            } else {
                return STNodeFactory.createRequiredParameterNode(annots, type, paramName);
            }
        } else if (nextToken.kind == SyntaxKind.EQUAL_TOKEN) {
            // If we were processing required params so far and found a defualtable
            // parameter, then switch the context to defaultable params.
            if (prevParamKind == SyntaxKind.REQUIRED_PARAM) {
                switchContext(ParserRuleContext.DEFAULTABLE_PARAM);
            }

            // Defaultable parameters
            STNode equal = parseAssignOp();
            STNode expr = parseInferredTypeDescDefaultOrExpression();
            if (inclusionSymbol != null) {
                type = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(type, inclusionSymbol,
                                DiagnosticErrorCode.ERROR_DEFAULTABLE_PARAMETER_CANNOT_BE_INCLUDED_RECORD_PARAMETER);
            }
            return STNodeFactory.createDefaultableParameterNode(annots, type, paramName, equal, expr);
        } else {
            recover(nextToken, ParserRuleContext.PARAMETER_NAME_RHS);
            return parseParameterRhs(prevParamKind, annots, inclusionSymbol, type, paramName);
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
            recover(token, ParserRuleContext.COMMA);
            return parseComma();
        }
    }

    /**
     * Parse return type descriptor of a function. A return type descriptor has the following structure.
     *
     * <code>return-type-descriptor := [ returns annots type-descriptor ]</code>
     *
     * @param isFuncTypeDesc Whether function type desc. paramNameOptional means its function type desc or ambiguous
     * @return Parsed node
     */
    private STNode parseFuncReturnTypeDescriptor(boolean isFuncTypeDesc) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACE_TOKEN: // func-body block
            case EQUAL_TOKEN: // external func
                return STNodeFactory.createEmptyNode();
            case RETURNS_KEYWORD:
                break;
            case IDENTIFIER_TOKEN: // function foo() r<cursor> and function foo() returns function() r<cursor>
                if (!isFuncTypeDesc || isSafeMissingReturnsParse()) {
                    break;    
                }
                // fall through
            default:
                STToken nextNextToken = getNextNextToken();
                if (nextNextToken.kind == SyntaxKind.RETURNS_KEYWORD) {
                    break;
                }

                return STNodeFactory.createEmptyNode();
        }

        STNode returnsKeyword = parseReturnsKeyword();
        STNode annot = parseOptionalAnnotations();
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RETURN_TYPE_DESC);
        return STNodeFactory.createReturnTypeDescriptorNode(returnsKeyword, annot, type);
    }

    private boolean isSafeMissingReturnsParse() {
        for (ParserRuleContext context : this.errorHandler.getContextStack()) {
            if (!isSafeMissingReturnsParseCtx(context)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSafeMissingReturnsParseCtx(ParserRuleContext ctx) {
        switch (ctx) {
            case TYPE_DESC_IN_ANNOTATION_DECL:
            case TYPE_DESC_BEFORE_IDENTIFIER:
            case TYPE_DESC_IN_RECORD_FIELD:
            case TYPE_DESC_IN_PARAM:
            case TYPE_DESC_IN_TYPE_BINDING_PATTERN:
            case VAR_DECL_STARTED_WITH_DENTIFIER:
            case TYPE_DESC_IN_PATH_PARAM:
            case AMBIGUOUS_STMT:
                // Contexts that expect an identifier after function type are not safe to parse as a missing return type
                return false;
            default:
                return true;
        }
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
            recover(token, ParserRuleContext.RETURNS_KEYWORD);
            return parseReturnsKeyword();
        }
    }

    /**
     * <p>
     * Parse a type descriptor. A type descriptor has the following structure.
     * </p>
     * <code>type-descriptor :=
     * &nbsp;simple-type-descriptor<br/>
     * &nbsp;| structured-type-descriptor<br/>
     * &nbsp;| behavioral-type-descriptor<br/>
     * &nbsp;| singleton-type-descriptor<br/>
     * &nbsp;| union-type-descriptor<br/>
     * &nbsp;| optional-type-descriptor<br/>
     * &nbsp;| any-type-descriptor<br/>
     * &nbsp;| anydata-type-descriptor<br/>
     * &nbsp;| byte-type-descriptor<br/>
     * &nbsp;| json-type-descriptor<br/>
     * &nbsp;| type-descriptor-reference<br/>
     * &nbsp;| ( type-descriptor )
     * <br/>
     * type-descriptor-reference := qualified-identifier</code>
     *
     * @return Parsed node
     */
    private STNode parseTypeDescriptor(ParserRuleContext context) {
        return parseTypeDescriptor(context, false, false, TypePrecedence.DEFAULT);
    }

    private STNode parseTypeDescriptor(ParserRuleContext context, TypePrecedence precedence) {
        return parseTypeDescriptor(context, false, false, precedence);
    }

    private STNode parseTypeDescriptor(List<STNode> qualifiers, ParserRuleContext context) {
        return parseTypeDescriptor(qualifiers, context, false, false, TypePrecedence.DEFAULT);
    }

    private STNode parseTypeDescriptorInExpression(boolean isInConditionalExpr) {
        return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_EXPRESSION, false, isInConditionalExpr,
                TypePrecedence.DEFAULT);
    }

    private STNode parseTypeDescriptor(ParserRuleContext context, boolean isTypedBindingPattern,
                                       boolean isInConditionalExpr, TypePrecedence precedence) {
        List<STNode> typeDescQualifiers = new ArrayList<>();
        return parseTypeDescriptor(typeDescQualifiers, context, isTypedBindingPattern, isInConditionalExpr, precedence);
    }


    
    private STNode parseTypeDescriptor(List<STNode> qualifiers, ParserRuleContext context,
                                       boolean isTypedBindingPattern, boolean isInConditionalExpr,
                                       TypePrecedence precedence) {
        startContext(context);
        STNode typeDesc = parseTypeDescriptorInternal(qualifiers, context, isTypedBindingPattern, isInConditionalExpr,
                precedence);
        endContext();
        return typeDesc;
    }

    private STNode parseTypeDescriptorInternal(List<STNode> qualifiers, ParserRuleContext context,
                                               boolean isTypedBindingPattern, boolean isInConditionalExpr,
                                               TypePrecedence precedence) {
        STNode typeDesc = parseTypeDescriptorInternal(qualifiers, context, isInConditionalExpr);

        // var is parsed as a built-in simple type. However, since var is not allowed everywhere,
        // validate it here. This is done to give better error messages.
        if (typeDesc.kind == SyntaxKind.VAR_TYPE_DESC &&
                context != ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN) {
            STToken missingToken = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            missingToken = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(missingToken, typeDesc,
                    DiagnosticErrorCode.ERROR_INVALID_USAGE_OF_VAR);
            typeDesc = STNodeFactory.createSimpleNameReferenceNode(missingToken);
        }

        return parseComplexTypeDescriptorInternal(typeDesc, context, isTypedBindingPattern, precedence);
    }

    private STNode parseComplexTypeDescriptor(STNode typeDesc, ParserRuleContext context,
                                              boolean isTypedBindingPattern) {
        startContext(context);
        STNode complexTypeDesc = parseComplexTypeDescriptorInternal(typeDesc, context, isTypedBindingPattern,
                TypePrecedence.DEFAULT);
        endContext();
        return complexTypeDesc;
    }

    /**
     * This will handle the parsing of optional,array,union type desc to infinite length.
     *
     * @param typeDesc              LHS type of the complex type
     * @param context               Parsing context
     * @param isTypedBindingPattern Whether in the typed-bp parsing or not
     * @param precedence            Precedence of the parsing type
     * @return Parsed type descriptor node
     */
    private STNode parseComplexTypeDescriptorInternal(STNode typeDesc, ParserRuleContext context,
                                                      boolean isTypedBindingPattern, TypePrecedence precedence) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case QUESTION_MARK_TOKEN:
                if (precedence.isHigherThanOrEqual(TypePrecedence.ARRAY_OR_OPTIONAL)) {
                    return typeDesc;
                }

                // If next token after a type descriptor is '?' then it is a possible optional type descriptor
                boolean isPossibleOptionalType = true;
                STToken nextNextToken = getNextNextToken();
                if (context == ParserRuleContext.TYPE_DESC_IN_EXPRESSION &&
                        !isValidTypeContinuationToken(nextNextToken) && isValidExprStart(nextNextToken.kind)) {
                    if (nextNextToken.kind == SyntaxKind.OPEN_BRACE_TOKEN) {
                        // TODO: support conditional expressions in which the middle expression starts with `{` #31033
                        ParserRuleContext grandParentCtx = this.errorHandler.getGrandParentContext();
                        isPossibleOptionalType = grandParentCtx == ParserRuleContext.IF_BLOCK ||
                                grandParentCtx == ParserRuleContext.WHILE_BLOCK;
                    } else {
                        isPossibleOptionalType = false;
                    }
                }

                if (!isPossibleOptionalType) {
                    return typeDesc;
                }

                STNode optionalTypeDes = parseOptionalTypeDescriptor(typeDesc);
                return parseComplexTypeDescriptorInternal(optionalTypeDes, context, isTypedBindingPattern, precedence);
            case OPEN_BRACKET_TOKEN:
                // If next token after a type descriptor is '[' then it is an array type descriptor
                if (isTypedBindingPattern) {
                    return typeDesc;
                }

                if (precedence.isHigherThanOrEqual(TypePrecedence.ARRAY_OR_OPTIONAL)) {
                    return typeDesc;
                }
                
                STNode arrayTypeDesc = parseArrayTypeDescriptor(typeDesc);
                return parseComplexTypeDescriptorInternal(arrayTypeDesc, context, isTypedBindingPattern, precedence);
            case PIPE_TOKEN:
                if (precedence.isHigherThanOrEqual(TypePrecedence.UNION)) {
                    return  typeDesc;
                }
                
                STNode newTypeDesc = parseUnionTypeDescriptor(typeDesc, context, isTypedBindingPattern);
                return parseComplexTypeDescriptorInternal(newTypeDesc, context, isTypedBindingPattern, precedence);
            case BITWISE_AND_TOKEN:
                if (precedence.isHigherThanOrEqual(TypePrecedence.INTERSECTION)) {
                    return  typeDesc;
                }
                
                newTypeDesc = parseIntersectionTypeDescriptor(typeDesc, context, isTypedBindingPattern);
                return parseComplexTypeDescriptorInternal(newTypeDesc, context, isTypedBindingPattern, precedence);
            default:
                return typeDesc;
        }
    }

    private boolean isValidTypeContinuationToken(STToken token) {
        switch (token.kind) {
            case QUESTION_MARK_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode validateForUsageOfVar(STNode typeDesc) {
        if (typeDesc.kind != SyntaxKind.VAR_TYPE_DESC) {
            return typeDesc;
        }

        STToken missingToken = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
        missingToken = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(missingToken, typeDesc,
                DiagnosticErrorCode.ERROR_INVALID_USAGE_OF_VAR);
        return STNodeFactory.createSimpleNameReferenceNode(missingToken);
    }

    /**
     * <p>
     * Parse a type descriptor, given the next token kind.
     * <br/>
     * Note that this method itself is capable of parsing type descriptor qualifiers
     * if they are not already parsed before coming here.
     * </p>
     *
     * @param qualifiers          Preceding type descriptor qualifiers
     * @param context             Current context
     * @param isInConditionalExpr Whether in the conditional expression or not
     * @return Parsed node
     */
    private STNode parseTypeDescriptorInternal(List<STNode> qualifiers, ParserRuleContext context,
                                               boolean isInConditionalExpr) {
        parseTypeDescQualifiers(qualifiers);
        STToken nextToken = peek();
        if (isQualifiedIdentifierPredeclaredPrefix(nextToken.kind)) {
            return parseQualifiedTypeRefOrTypeDesc(qualifiers, isInConditionalExpr);
        }
        
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                reportInvalidQualifierList(qualifiers);
                return parseTypeReference(isInConditionalExpr);
            case RECORD_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseRecordTypeDescriptor();
            case OBJECT_KEYWORD:
                STNode objectTypeQualifiers = createObjectTypeQualNodeList(qualifiers);
                return parseObjectTypeDescriptor(consume(), objectTypeQualifiers);
            case OPEN_PAREN_TOKEN:
                reportInvalidQualifierList(qualifiers);
                return parseNilOrParenthesisedTypeDesc();
            case MAP_KEYWORD: // map type desc
                reportInvalidQualifierList(qualifiers);
                return parseMapTypeDescriptor(consume());
            case STREAM_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseStreamTypeDescriptor(consume());
            case TABLE_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseTableTypeDescriptor(consume());
            case FUNCTION_KEYWORD:
                return parseFunctionTypeDesc(qualifiers);
            case OPEN_BRACKET_TOKEN:
                reportInvalidQualifierList(qualifiers);
                return parseTupleTypeDesc();
            case DISTINCT_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                STNode distinctKeyword = consume();
                return parseDistinctTypeDesc(distinctKeyword, context);
            case TRANSACTION_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseQualifiedIdentWithTransactionPrefix(context);
            default:
                if (isParameterizedTypeToken(nextToken.kind)) {
                    reportInvalidQualifierList(qualifiers);
                    return parseParameterizedTypeDescriptor(consume());
                }
                
                if (isSingletonTypeDescStart(nextToken.kind, getNextNextToken())) {
                    reportInvalidQualifierList(qualifiers);
                    return parseSingletonTypeDesc();
                }

                if (isSimpleType(nextToken.kind)) {
                    reportInvalidQualifierList(qualifiers);
                    return parseSimpleTypeDescriptor();
                }
        }

        ParserRuleContext recoveryCtx = getTypeDescRecoveryCtx(qualifiers);
        Solution solution = recover(peek(), recoveryCtx);

        if (solution.action == Action.KEEP) {
            reportInvalidQualifierList(qualifiers);
            return parseSingletonTypeDesc();
        }

        return parseTypeDescriptorInternal(qualifiers, context, isInConditionalExpr);
    }

    /**
     * Returns recovery context for the type descriptor.
     * <br><br/>
     * Note that, when recovering for the type descriptor in presence of pre-parsed qualifiers,
     * <br/>
     * {@link ParserRuleContext#TYPE_DESCRIPTOR} should be narrowed down to applicable types.
     *
     * @param qualifiers Pre-parsed qualifiers
     * @return Context to recover
     */
    private ParserRuleContext getTypeDescRecoveryCtx(List<STNode> qualifiers) {
        if (qualifiers.isEmpty()) {
            return ParserRuleContext.TYPE_DESCRIPTOR;
        }

        STNode lastQualifier = getLastNodeInList(qualifiers);
        switch (lastQualifier.kind) {
            case ISOLATED_KEYWORD:
                return ParserRuleContext.TYPE_DESC_WITHOUT_ISOLATED;
            case TRANSACTIONAL_KEYWORD:
                return ParserRuleContext.FUNC_TYPE_DESC;
            case SERVICE_KEYWORD:
            case CLIENT_KEYWORD:
            default:
                // We reach here for service and client only.
                return ParserRuleContext.OBJECT_TYPE_DESCRIPTOR;
        }
    }

    /**
     * Check whether the given token is a parameterized type keyword.
     *
     * @param tokenKind Token to check
     * @return <code>true</code> if the given token is a parameterized type keyword. <code>false</code> otherwise
     */
    static boolean isParameterizedTypeToken(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case TYPEDESC_KEYWORD:
            case FUTURE_KEYWORD:
            case XML_KEYWORD:
            case ERROR_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    private STNode parseQualifiedIdentWithTransactionPrefix(ParserRuleContext context) {
        STToken transactionKeyword = consume();
        STNode identifier = STNodeFactory.createIdentifierToken(transactionKeyword.text(),
                transactionKeyword.leadingMinutiae(), transactionKeyword.trailingMinutiae());
        // If we reach hear `colon` is missing for qualified identifier. Hence, create missing token
        STNode colon = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.COLON_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_COLON_TOKEN);
        STNode varOrFuncName = parseIdentifier(context);
        return createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
    }

    private STNode parseQualifiedTypeRefOrTypeDesc(List<STNode> qualifiers, boolean isInConditionalExpr) {
        STToken preDeclaredPrefix = consume();
        STToken nextNextToken = getNextNextToken();
        if (preDeclaredPrefix.kind == SyntaxKind.TRANSACTION_KEYWORD ||
                nextNextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            reportInvalidQualifierList(qualifiers);
            return parseQualifiedIdentifierWithPredeclPrefix(preDeclaredPrefix, isInConditionalExpr);
        }
        ParserRuleContext context;
        switch (preDeclaredPrefix.kind) {
            case MAP_KEYWORD:
                context = ParserRuleContext.MAP_TYPE_OR_TYPE_REF;
                break;
            case OBJECT_KEYWORD:
                context = ParserRuleContext.OBJECT_TYPE_OR_TYPE_REF;
                break;
            case STREAM_KEYWORD:
                context = ParserRuleContext.STREAM_TYPE_OR_TYPE_REF;
                break;
            case TABLE_KEYWORD:
                context = ParserRuleContext.TABLE_TYPE_OR_TYPE_REF;
                break;
            default:
                if (isParameterizedTypeToken(preDeclaredPrefix.kind)) {
                    context = ParserRuleContext.PARAMETERIZED_TYPE_OR_TYPE_REF;
                } else {
                    context = ParserRuleContext.TYPE_DESC_RHS_OR_TYPE_REF;
                }
        }

        Solution solution = recover(peek(), context);

        if (solution.action == Action.KEEP) {
            reportInvalidQualifierList(qualifiers);
            return parseQualifiedIdentifierWithPredeclPrefix(preDeclaredPrefix, isInConditionalExpr);
        }
        return parseTypeDescStartWithPredeclPrefix(preDeclaredPrefix, qualifiers);
    }

    private STNode parseTypeDescStartWithPredeclPrefix(STToken preDeclaredPrefix, List<STNode> qualifiers) {
        switch (preDeclaredPrefix.kind) {
            case MAP_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseMapTypeDescriptor(preDeclaredPrefix);
            case OBJECT_KEYWORD:
                STNode objectTypeQualifiers = createObjectTypeQualNodeList(qualifiers);
                return parseObjectTypeDescriptor(preDeclaredPrefix, objectTypeQualifiers);
            case STREAM_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseStreamTypeDescriptor(preDeclaredPrefix);
            case TABLE_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseTableTypeDescriptor(preDeclaredPrefix);
            default:
                if (isParameterizedTypeToken(preDeclaredPrefix.kind)) {
                    reportInvalidQualifierList(qualifiers);
                    return parseParameterizedTypeDescriptor(preDeclaredPrefix);
                }
                
                return createBuiltinSimpleNameReference(preDeclaredPrefix);
        }
    }

    private STNode parseQualifiedIdentifierWithPredeclPrefix(STToken preDeclaredPrefix, boolean isInConditionalExpr) {
        STNode identifier = STNodeFactory.createIdentifierToken(preDeclaredPrefix.text(),
                preDeclaredPrefix.leadingMinutiae(), preDeclaredPrefix.trailingMinutiae());
        return parseQualifiedIdentifier(identifier, isInConditionalExpr);
    }

    /**
     * Parse distinct type descriptor.
     * <p>
     * <code>
     * distinct-type-descriptor := distinct type-descriptor
     * </code>
     *
     * @param distinctKeyword Distinct keyword that precedes the type descriptor
     * @param context         Context in which the type desc is used.
     * @return Distinct type descriptor
     */
    private STNode parseDistinctTypeDesc(STNode distinctKeyword, ParserRuleContext context) {
        STNode typeDesc = parseTypeDescriptor(context, TypePrecedence.DISTINCT);
        return STNodeFactory.createDistinctTypeDescriptorNode(distinctKeyword, typeDesc);
    }

    private STNode parseNilOrParenthesisedTypeDesc() {
        STNode openParen = parseOpenParenthesis();
        return parseNilOrParenthesisedTypeDescRhs(openParen);
    }

    private STNode parseNilOrParenthesisedTypeDescRhs(STNode openParen) {
        STNode closeParen;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case CLOSE_PAREN_TOKEN:
                closeParen = parseCloseParenthesis();
                return STNodeFactory.createNilTypeDescriptorNode(openParen, closeParen);
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    STNode typedesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_PARENTHESIS);
                    closeParen = parseCloseParenthesis();
                    return STNodeFactory.createParenthesisedTypeDescriptorNode(openParen, typedesc, closeParen);
                }

                recover(peek(), ParserRuleContext.NIL_OR_PARENTHESISED_TYPE_DESC_RHS);
                return parseNilOrParenthesisedTypeDescRhs(openParen);
        }
    }

    /**
     * Parse simple type descriptor in terminal expression.
     *
     * @return Parsed node
     */
    private STNode parseSimpleTypeInTerminalExpr() {
        startContext(ParserRuleContext.TYPE_DESC_IN_EXPRESSION);
        STNode simpleTypeDescriptor = parseSimpleTypeDescriptor();
        endContext();
        return simpleTypeDescriptor;
    }

    /**
     * Parse simple type descriptor.
     *
     * @return Parsed node
     */
    private STNode parseSimpleTypeDescriptor() {
        STToken nextToken = peek();
        if (isSimpleType(nextToken.kind)) {
            STToken token = consume();
            return createBuiltinSimpleNameReference(token);
        } else {
            recover(nextToken, ParserRuleContext.SIMPLE_TYPE_DESCRIPTOR);
            return parseSimpleTypeDescriptor();
        }
    }

    public static STNode createBuiltinSimpleNameReference(STNode token) {
        SyntaxKind typeKind = getBuiltinTypeSyntaxKind(token.kind);
        return STNodeFactory.createBuiltinSimpleNameReferenceNode(typeKind, token);
    }

    /**
     * <p>
     * Parse function body. A function body has the following structure.
     * </p>
     * <code>
     * function-body := function-body-block | external-function-body
     * external-function-body := = annots external ;
     * function-body-block := { [default-worker-init named-worker-decl+] default-worker }
     * </code>
     *
     * @return Parsed node
     */
    protected STNode parseFunctionBody() {
        STToken token = peek();
        switch (token.kind) {
            case EQUAL_TOKEN:
                return parseExternalFunctionBody();
            case OPEN_BRACE_TOKEN:
                return parseFunctionBodyBlock(false);
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return parseExpressionFuncBody(false, false);
            default:
                recover(token, ParserRuleContext.FUNC_BODY);
                return parseFunctionBody();
        }
    }

    /**
     * <p>
     * Parse function body block. A function body block has the following structure.
     * </p>
     *
     * <code>
     * function-body-block := { [default-worker-init, named-worker-decl+] default-worker } [;]<br/>
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

            if (validateStatement(stmt)) {
                continue;
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
                        updateLastNodeInListWithInvalidNode(secondStmtList, stmt,
                                DiagnosticErrorCode.ERROR_NAMED_WORKER_NOT_ALLOWED_HERE);
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
        STNode semicolon = isAnonFunc ? STNodeFactory.createEmptyNode() : parseOptionalSemicolon();
        endContext();
        return STNodeFactory.createFunctionBodyBlockNode(openBrace, namedWorkersList, statements, closeBrace,
                semicolon);
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
                return isEndOfModuleLevelNode(1);
        }
    }

    private boolean isEndOfObjectTypeNode() {
        return isEndOfModuleLevelNode(1, true);
    }

    private boolean isEndOfStatements() {
        switch (peek().kind) {
            case RESOURCE_KEYWORD:
                return true;
            default:
                return isEndOfModuleLevelNode(1);
        }
    }

    private boolean isEndOfModuleLevelNode(int peekIndex) {
        return isEndOfModuleLevelNode(peekIndex, false);
    }

    private boolean isEndOfModuleLevelNode(int peekIndex, boolean isObject) {
        switch (peek(peekIndex).kind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
            case IMPORT_KEYWORD:
            case ANNOTATION_KEYWORD:
            case LISTENER_KEYWORD:
            case CLASS_KEYWORD:
                return true;
            case SERVICE_KEYWORD:
                return isServiceDeclStart(ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER, 1);
            case PUBLIC_KEYWORD:
                return !isObject && isEndOfModuleLevelNode(peekIndex + 1, false);
            case FUNCTION_KEYWORD:
                if (isObject) {
                    return false;
                }

                // if function keyword follows by a identifier treat is as
                // the function name. Only function def can have func-name
                return peek(peekIndex + 1).kind == SyntaxKind.IDENTIFIER_TOKEN &&
                        peek(peekIndex + 2).kind == SyntaxKind.OPEN_PAREN_TOKEN;
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
            case DO_KEYWORD:
            case AT_TOKEN:
                return true;
            default:
                return isEndOfModuleLevelNode(1);
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
            case DO_KEYWORD:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return true;
            default:
                return isEndOfModuleLevelNode(1);
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
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            recover(peek(), ParserRuleContext.VARIABLE_NAME);
            return parseVariableName();
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
            recover(token, ParserRuleContext.OPEN_BRACE);
            return parseOpenBrace();
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
            recover(token, ParserRuleContext.CLOSE_BRACE);
            return parseCloseBrace();
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
        return parseExternalFuncBodyRhs(assign);
    }

    private STNode parseExternalFuncBodyRhs(STNode assign) {
        STNode annotation;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case AT_TOKEN:
                annotation = parseAnnotations();
                break;
            case EXTERNAL_KEYWORD:
                annotation = STNodeFactory.createEmptyNodeList();
                break;
            default:
                recover(nextToken, ParserRuleContext.EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS);
                return parseExternalFuncBodyRhs(assign);
        }

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
            recover(token, ParserRuleContext.SEMICOLON);
            return parseSemicolon();
        }
    }

    /**
     * Parse optional semicolon.
     *
     * @return Parsed node
     */
    private STNode parseOptionalSemicolon() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SEMICOLON_TOKEN) {
            return consume();
        }
        return STNodeFactory.createEmptyNode();
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
            recover(token, ParserRuleContext.EXTERNAL_KEYWORD);
            return parseExternalKeyword();
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
            recover(token, ParserRuleContext.ASSIGN_OP);
            return parseAssignOp();
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
            recover(token, ParserRuleContext.BINARY_OPERATOR);
            return parseBinaryOperator();
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
            case NOT_IS_KEYWORD:
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
                return OperatorPrecedence.ANON_FUNC_OR_LET;
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
            case MULTIPLICATIVE:
                return SyntaxKind.ASTERISK_TOKEN;
            case DEFAULT:
            case UNARY:
            case ACTION:
            case EXPRESSION_ACTION:
            case REMOTE_CALL_ACTION:
            case ANON_FUNC_OR_LET:
            case QUERY:
            case TRAP:
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
     * Get the operator context to insert during recovery, given the precedence level.
     * </p>
     *
     * @param opPrecedenceLevel Precedence of the given operator
     * @return Context of the missing operator
     */
    private ParserRuleContext getMissingBinaryOperatorContext(OperatorPrecedence opPrecedenceLevel) {
        switch (opPrecedenceLevel) {
            case MULTIPLICATIVE:
                return ParserRuleContext.ASTERISK;
            case DEFAULT:
            case UNARY:
            case ACTION:
            case EXPRESSION_ACTION:
            case REMOTE_CALL_ACTION:
            case ANON_FUNC_OR_LET:
            case QUERY:
            case TRAP:
            case ADDITIVE:
                return ParserRuleContext.PLUS_TOKEN;
            case SHIFT:
                return ParserRuleContext.DOUBLE_LT;
            case RANGE:
                return ParserRuleContext.ELLIPSIS;
            case BINARY_COMPARE:
                return ParserRuleContext.LT_TOKEN;
            case EQUALITY:
                return ParserRuleContext.DOUBLE_EQUAL;
            case BITWISE_AND:
                return ParserRuleContext.BITWISE_AND_OPERATOR;
            case BITWISE_XOR:
                return ParserRuleContext.BITWISE_XOR;
            case BITWISE_OR:
                return ParserRuleContext.PIPE;
            case LOGICAL_AND:
                return ParserRuleContext.LOGICAL_AND;
            case LOGICAL_OR:
                return ParserRuleContext.LOGICAL_OR;
            case ELVIS_CONDITIONAL:
                return ParserRuleContext.ELVIS;
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
     * @param metadata  Metadata
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
     * Parse a class definition.
     * <p>
     * <code>
     * module-class-defn :=
     * <br/>
     * metadata [public] class-type-quals class identifier { class-member* } [;]
     * </code>
     *
     * @param metadata   Metadata
     * @param qualifier  Visibility qualifier
     * @param qualifiers Class type qualifiers
     * @return Parsed node
     */
    private STNode parseClassDefinition(STNode metadata, STNode qualifier, List<STNode> qualifiers) {
        startContext(ParserRuleContext.MODULE_CLASS_DEFINITION);
        STNode classTypeQualifiers = createClassTypeQualNodeList(qualifiers);
        STNode classKeyword = parseClassKeyword();
        STNode className = parseClassName();
        STNode openBrace = parseOpenBrace();
        STNode classMembers = parseObjectMembers(ParserRuleContext.CLASS_MEMBER);
        STNode closeBrace = parseCloseBrace();
        STNode semicolon = parseOptionalSemicolon();
        endContext();
        return STNodeFactory.createClassDefinitionNode(metadata, qualifier, classTypeQualifiers, classKeyword,
                className, openBrace, classMembers, closeBrace, semicolon);
    }

    private boolean isClassTypeQual(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case READONLY_KEYWORD:
            case DISTINCT_KEYWORD:
            case ISOLATED_KEYWORD:
                return true;
            default:
                return isObjectNetworkQual(tokenKind);
        }
    }

    private boolean isObjectTypeQual(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case ISOLATED_KEYWORD:
                return true;
            default:
                return isObjectNetworkQual(tokenKind);
        }
    }

    private boolean isObjectNetworkQual(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case SERVICE_KEYWORD:
            case CLIENT_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Validate and create class type qualifier node list.
     * <p>
     * <code> class-type-quals := (distinct | readonly | isolated | object-network-qual)*</code>
     *
     * @param qualifierList Qualifier list to be validated
     * @return Parsed node
     */
    private STNode createClassTypeQualNodeList(List<STNode> qualifierList) {
        // Validate qualifiers and create a STNodeList
        List<STNode> validatedList = new ArrayList<>();
        boolean hasNetworkQual = false;

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            if (isObjectNetworkQual(qualifier.kind)) {
                if (hasNetworkQual) {
                    updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                            DiagnosticErrorCode.ERROR_MORE_THAN_ONE_OBJECT_NETWORK_QUALIFIERS);
                } else {
                    validatedList.add(qualifier);
                    hasNetworkQual = true;
                }
                continue;
            }

            if (isClassTypeQual(qualifier.kind)) {
                validatedList.add(qualifier);
                continue;
            }

            // We only reach here for invalid qualifiers
            if (qualifierList.size() == nextIndex) {
                addInvalidNodeToNextToken(qualifier, DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED,
                        ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        return STNodeFactory.createNodeList(validatedList);
    }

    /**
     * Validate and create object type qualifier node list.
     * <p>
     * <code> object-type-quals := (isolated | object-network-qual)*
     * <br/>
     * object-network-qual := client | service
     * </code>
     *
     * @param qualifierList Qualifier list to be validated
     * @return Parsed node
     */
    private STNode createObjectTypeQualNodeList(List<STNode> qualifierList) {
        // Validate qualifiers and create a STNodeList
        List<STNode> validatedList = new ArrayList<>();
        boolean hasNetworkQual = false;

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            if (isObjectNetworkQual(qualifier.kind)) {
                if (hasNetworkQual) {
                    updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                            DiagnosticErrorCode.ERROR_MORE_THAN_ONE_OBJECT_NETWORK_QUALIFIERS);
                } else {
                    validatedList.add(qualifier);
                    hasNetworkQual = true;
                }
                continue;
            }

            if (isObjectTypeQual(qualifier.kind)) {
                validatedList.add(qualifier);
                continue;
            }

            // We only reach here for invalid qualifiers
            if (qualifierList.size() == nextIndex) {
                addInvalidNodeToNextToken(qualifier, DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED,
                        ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        return STNodeFactory.createNodeList(validatedList);
    }

    /**
     * Parse class keyword.
     *
     * @return Parsed node
     */
    private STNode parseClassKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLASS_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.CLASS_KEYWORD);
            return parseClassKeyword();
        }
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
            recover(token, ParserRuleContext.TYPE_KEYWORD);
            return parseTypeKeyword();
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
            recover(token, ParserRuleContext.TYPE_NAME);
            return parseTypeName();
        }
    }

    /**
     * Parse class name.
     *
     * @return Parsed node
     */
    private STNode parseClassName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.CLASS_NAME);
            return parseClassName();
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

        ArrayList<STNode> recordFields = new ArrayList<>();
        STToken token = peek();
        STNode recordRestDescriptor = STNodeFactory.createEmptyNode();
        while (!isEndOfRecordTypeNode(token.kind)) {
            STNode field = parseFieldOrRestDescriptor();
            if (field == null) {
                break;
            }

            token = peek();
            if (field.kind == SyntaxKind.RECORD_REST_TYPE && bodyStartDelimiter.kind == SyntaxKind.OPEN_BRACE_TOKEN) {
                if (recordFields.size() == 0) {
                    bodyStartDelimiter = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(bodyStartDelimiter, field,
                            DiagnosticErrorCode.ERROR_INCLUSIVE_RECORD_TYPE_CANNOT_CONTAIN_REST_FIELD);
                } else {
                    updateLastNodeInListWithInvalidNode(recordFields, field,
                            DiagnosticErrorCode.ERROR_INCLUSIVE_RECORD_TYPE_CANNOT_CONTAIN_REST_FIELD);
                }
                continue;
            } else if (field.kind == SyntaxKind.RECORD_REST_TYPE) {
                recordRestDescriptor = field;
                // If there are more fields after rest type descriptor, parse them and mark as invalid.
                while (!isEndOfRecordTypeNode(token.kind)) {
                    STNode invalidField = parseFieldOrRestDescriptor();
                    if (invalidField == null) {
                        break;
                    }
                    recordRestDescriptor = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(recordRestDescriptor,
                            invalidField, DiagnosticErrorCode.ERROR_MORE_RECORD_FIELDS_AFTER_REST_FIELD);
                    token = peek();
                }
                break;
            }

            recordFields.add(field);
        }

        STNode fields = STNodeFactory.createNodeList(recordFields);
        STNode bodyEndDelimiter = parseRecordBodyCloseDelimiter(bodyStartDelimiter.kind);
        endContext();
        return STNodeFactory.createRecordTypeDescriptorNode(recordKeyword, bodyStartDelimiter, fields,
                recordRestDescriptor, bodyEndDelimiter);
    }

    /**
     * Parse record body start delimiter.
     *
     * @return Parsed node
     */
    private STNode parseRecordBodyStartDelimiter() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACE_PIPE_TOKEN:
                return parseClosedRecordBodyStart();
            case OPEN_BRACE_TOKEN:
                return parseOpenBrace();
            default:
                recover(nextToken, ParserRuleContext.RECORD_BODY_START);
                return parseRecordBodyStartDelimiter();
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
            recover(token, ParserRuleContext.CLOSED_RECORD_BODY_START);
            return parseClosedRecordBodyStart();
        }
    }

    /**
     * Parse record body close delimiter.
     *
     * @return Parsed node
     */
    private STNode parseRecordBodyCloseDelimiter(SyntaxKind startingDelimeter) {
        if (startingDelimeter == SyntaxKind.OPEN_BRACE_PIPE_TOKEN) {
            return parseClosedRecordBodyEnd();
        }
        return parseCloseBrace();
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
            recover(token, ParserRuleContext.CLOSED_RECORD_BODY_END);
            return parseClosedRecordBodyEnd();
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
            recover(token, ParserRuleContext.RECORD_KEYWORD);
            return parseRecordKeyword();
        }
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

    private STNode parseFieldOrRestDescriptor() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
                return null;
            case ASTERISK_TOKEN:
                // record-type-reference
                startContext(ParserRuleContext.RECORD_FIELD);
                STNode asterisk = consume();
                STNode type = parseTypeReferenceInTypeInclusion();
                STNode semicolonToken = parseSemicolon();
                endContext();
                return STNodeFactory.createTypeReferenceNode(asterisk, type, semicolonToken);
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
                return parseRecordField();
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    // individual-field-descriptor
                    return parseRecordField();
                }

                recover(peek(), ParserRuleContext.RECORD_FIELD_OR_RECORD_END);
                return parseFieldOrRestDescriptor();
        }
    }

    private STNode parseRecordField() {
        startContext(ParserRuleContext.RECORD_FIELD);
        STNode metadata = parseMetaData();
        STNode fieldOrRestDesc = parseRecordField(peek(), metadata);
        endContext();
        return fieldOrRestDesc;
    }

    private STNode parseRecordField(STToken nextToken, STNode metadata) {
        if (nextToken.kind != SyntaxKind.READONLY_KEYWORD) {
            STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
            return parseFieldOrRestDescriptorRhs(metadata, type);
        }

        // If the readonly-keyword is present, check whether its qualifier
        // or the readonly-type-desc.
        STNode type;
        STNode readOnlyQualifier;
        readOnlyQualifier = parseReadonlyKeyword();

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
                        type = createBuiltinSimpleNameReference(readOnlyQualifier);
                        readOnlyQualifier = STNodeFactory.createEmptyNode();
                        STNode fieldName = ((STSimpleNameReferenceNode) fieldNameOrTypeDesc).name;
                        return parseFieldDescriptorRhs(metadata, readOnlyQualifier, type, fieldName);
                    default:
                        // else, treat a as the type-name
                        type = parseComplexTypeDescriptor(fieldNameOrTypeDesc,
                                ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD, false);
                        break;
                }
            }
        } else if (nextToken.kind == SyntaxKind.ELLIPSIS_TOKEN) {
            // readonly ...
            type = createBuiltinSimpleNameReference(readOnlyQualifier);
            return parseFieldOrRestDescriptorRhs(metadata, type);
        } else if (isTypeStartingToken(nextToken.kind)) {
            type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
        } else {
            readOnlyQualifier = createBuiltinSimpleNameReference(readOnlyQualifier);
            type = parseComplexTypeDescriptor(readOnlyQualifier, ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD, false);
            readOnlyQualifier = STNodeFactory.createEmptyNode();
        }

        return parseIndividualRecordField(metadata, readOnlyQualifier, type);
    }

    private STNode parseIndividualRecordField(STNode metadata, STNode readOnlyQualifier, STNode type) {
        STNode fieldName = parseVariableName();
        return parseFieldDescriptorRhs(metadata, readOnlyQualifier, type, fieldName);
    }

    /**
     * Parse type reference in type inclusion.
     * <p>
     * <code>type-reference := identifier | qualified-identifier</code>
     *
     * @return Type reference node
     */
    private STNode parseTypeReferenceInTypeInclusion() {
        STNode typeReference = parseTypeDescriptor(ParserRuleContext.TYPE_REFERENCE_IN_TYPE_INCLUSION);
        if (typeReference.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            if (typeReference.hasDiagnostics()) {
                // When a missing type desc is recovered, diagnostic code will be missing type desc.
                // Since this is type ref context we correct the error message by creating a new missing token
                STNode emptyNameReference = STNodeFactory.createSimpleNameReferenceNode
                        (SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                                DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER));
                return emptyNameReference;
            }
            return typeReference;
        }
        if (typeReference.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return typeReference;
        }

        STNode emptyNameReference = STNodeFactory
                .createSimpleNameReferenceNode(SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN));
        emptyNameReference = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(emptyNameReference, typeReference,
                DiagnosticErrorCode.ERROR_ONLY_TYPE_REFERENCE_ALLOWED_AS_TYPE_INCLUSIONS);
        return emptyNameReference;
    }

    /**
     * Parse type reference.
     * <p>
     * <code>type-reference := identifier | qualified-identifier</code>
     *
     * @return Type reference node
     */
    private STNode parseTypeReference() {
        return parseTypeReference(false);
    }

    private STNode parseTypeReference(boolean isInConditionalExpr) {
        return parseQualifiedIdentifier(ParserRuleContext.TYPE_REFERENCE, isInConditionalExpr);
    }

    /**
     * Parse identifier or qualified identifier.
     *
     * @return Identifier node
     */
    private STNode parseQualifiedIdentifier(ParserRuleContext currentCtx) {
        return parseQualifiedIdentifier(currentCtx, false);
    }

    private STNode parseQualifiedIdentifier(ParserRuleContext currentCtx, boolean isInConditionalExpr) {
        STToken token = peek();
        STNode typeRefOrPkgRef;
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            typeRefOrPkgRef = consume();
        } else if (isQualifiedIdentifierPredeclaredPrefix(token.kind)) {
            STToken preDeclaredPrefix = consume();
            typeRefOrPkgRef = STNodeFactory.createIdentifierToken(preDeclaredPrefix.text(),
                    preDeclaredPrefix.leadingMinutiae(), preDeclaredPrefix.trailingMinutiae());
        } else {
            recover(token, currentCtx);
            if (peek().kind != SyntaxKind.IDENTIFIER_TOKEN) {
                addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
                return parseQualifiedIdentifier(currentCtx, isInConditionalExpr);
            }

            typeRefOrPkgRef = consume();
        }

        return parseQualifiedIdentifier(typeRefOrPkgRef, isInConditionalExpr);
    }

    /**
     * Parse identifier or qualified identifier, given the starting identifier.
     *
     * @param identifier Starting identifier
     * @return Parse node
     */
    private STNode parseQualifiedIdentifier(STNode identifier, boolean isInConditionalExpr) {
        STToken nextToken = peek(1);
        if (nextToken.kind != SyntaxKind.COLON_TOKEN) {
            return STNodeFactory.createSimpleNameReferenceNode(identifier);
        }
        
        if (isInConditionalExpr && (hasTrailingMinutiae(identifier) || hasTrailingMinutiae(nextToken))) {
            return ConditionalExprResolver.getSimpleNameRefNode(identifier);
        }

        STToken nextNextToken = peek(2);
        switch (nextNextToken.kind) {
            case IDENTIFIER_TOKEN:
                STToken colon = consume();
                STNode varOrFuncName = consume();
                return createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
            case MAP_KEYWORD:
                colon = consume();
                STToken mapKeyword = consume();
                STNode refName = STNodeFactory.createIdentifierToken(mapKeyword.text(), mapKeyword.leadingMinutiae(),
                        mapKeyword.trailingMinutiae(), mapKeyword.diagnostics());
                return createQualifiedNameReferenceNode(identifier, colon, refName);
            case COLON_TOKEN:
                // specially handle cases where there are more than one colon.
                addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
                return parseQualifiedIdentifier(identifier, isInConditionalExpr);
            default:
                if (isInConditionalExpr) {
                    return ConditionalExprResolver.getSimpleNameRefNode(identifier);
                }

                colon = consume();
                varOrFuncName = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER);
                return createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
        }
    }

    private STNode createQualifiedNameReferenceNode(STNode identifier, STNode colon, STNode varOrFuncName) {
        if (hasTrailingMinutiae(identifier) || hasTrailingMinutiae(colon)) {
            colon = SyntaxErrors.addDiagnostic(colon,
                    DiagnosticErrorCode.ERROR_INTERVENING_WHITESPACES_ARE_NOT_ALLOWED);
        }

        return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
    }

    /**
     * Parse RHS of a field or rest type descriptor.
     *
     * @param metadata Metadata
     * @param type     Type descriptor
     * @return Parsed node
     */
    private STNode parseFieldOrRestDescriptorRhs(STNode metadata, STNode type) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ELLIPSIS_TOKEN:
                reportInvalidMetaData(metadata, "record rest descriptor");
                STNode ellipsis = parseEllipsis();
                STNode semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordRestDescriptorNode(type, ellipsis, semicolonToken);
            case IDENTIFIER_TOKEN:
                STNode readonlyQualifier = STNodeFactory.createEmptyNode();
                return parseIndividualRecordField(metadata, readonlyQualifier, type);
            default:
                recover(nextToken, ParserRuleContext.FIELD_OR_REST_DESCIPTOR_RHS);
                return parseFieldOrRestDescriptorRhs(metadata, type);
        }
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
     * @param metadata  Metadata
     * @param type      Type descriptor
     * @param fieldName Field name
     * @return Parsed node
     */
    private STNode parseFieldDescriptorRhs(STNode metadata, STNode readonlyQualifier, STNode type, STNode fieldName) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
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
                recover(nextToken, ParserRuleContext.FIELD_DESCRIPTOR_RHS);
                return parseFieldDescriptorRhs(metadata, readonlyQualifier, type, fieldName);
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
            return consume();
        } else {
            recover(token, ParserRuleContext.QUESTION_MARK);
            return parseQuestionMark();
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
                addInvalidNodeToNextToken(stmt, DiagnosticErrorCode.ERROR_NAMED_WORKER_NOT_ALLOWED_HERE);
                continue;
            }

            if (validateStatement(stmt)) {
                continue;
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
        STToken nextToken = peek();
        STNode annots = STNodeFactory.createEmptyNodeList();
        switch (nextToken.kind) {
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
                // Returning null marks the end of statements
                return null;
            case SEMICOLON_TOKEN:
                addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
                return parseStatement();
            case AT_TOKEN:
                annots = parseOptionalAnnotations();
                break;
            default:
                if (isStatementStartingToken(nextToken.kind)) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT);
                if (solution.action == Action.KEEP) {
                    // singleton type starting tokens can be correct one's hence keep them.
                    break;
                }

                return parseStatement();
        }

        return parseStatement(annots);
    }

    /**
     * Invalidate top-level nodes which are allowed to be parsed as statements to improve the error messages.
     *
     * @param statement Statement to validate
     * @return <code>true</code> if the statement is not valid <code>false</code> otherwise
     */
    boolean validateStatement(STNode statement) {
        switch (statement.kind) {
            case LOCAL_TYPE_DEFINITION_STATEMENT:
                addInvalidNodeToNextToken(statement, DiagnosticErrorCode.ERROR_LOCAL_TYPE_DEFINITION_NOT_ALLOWED);
                return true;
            case CONST_DECLARATION:
                addInvalidNodeToNextToken(statement, DiagnosticErrorCode.ERROR_LOCAL_CONST_DECL_NOT_ALLOWED);
                return true;
            default:
                return false;
        }
    }

    private STNode getAnnotations(STNode nullbaleAnnot) {
        if (nullbaleAnnot != null) {
            return nullbaleAnnot;
        }

        return STNodeFactory.createEmptyNodeList();
    }

    /**
     * Parse a single statement, given the next token kind.
     *
     * @param annots Annotations that precedes the statement
     * @return Parsed node
     */
    private STNode parseStatement(STNode annots) {
        List<STNode> typeDescQualifiers = new ArrayList<>();
        return parseStatement(annots, typeDescQualifiers);
    }

    private STNode parseStatement(STNode annots, List<STNode> qualifiers) {
        parseTypeDescQualifiers(qualifiers);
        STToken nextToken = peek();
        if (isPredeclaredIdentifier(nextToken.kind)) {
            return parseStmtStartsWithTypeOrExpr(getAnnotations(annots), qualifiers);
        }
        
        switch (nextToken.kind) {
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
                STNode publicQualifier = STNodeFactory.createEmptyNode();
                return createMissingSimpleVarDecl(getAnnotations(annots), publicQualifier, qualifiers, false);
            case SEMICOLON_TOKEN:
                addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
                return parseStatement(annots, qualifiers);
            case FINAL_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                STNode finalKeyword = consume();
                return parseVariableDecl(getAnnotations(annots), finalKeyword);
            case IF_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseIfElseBlock();
            case WHILE_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseWhileStatement();
            case DO_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseDoStatement();
            case PANIC_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parsePanicStatement();
            case CONTINUE_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseContinueStatement();
            case BREAK_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseBreakStatement();
            case RETURN_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseReturnStatement();
            case FAIL_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseFailStatement();
            case TYPE_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                return parseLocalTypeDefinitionStatement(getAnnotations(annots));
            case CONST_KEYWORD:
                // This is done to give proper error msg by invalidating local const-decl after parsing.
                reportInvalidQualifierList(qualifiers);
                return parseConstantDeclaration(annots, STNodeFactory.createEmptyNode());
            case LOCK_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseLockStatement();
            case OPEN_BRACE_TOKEN:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseStatementStartsWithOpenBrace();
            case WORKER_KEYWORD:
                // Even-though worker is not a statement, we parse it as statements.
                // then validates it based on the context. This is done to provide
                // better error messages
                return parseNamedWorkerDeclaration(getAnnotations(annots), qualifiers);
            case FORK_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseForkStatement();
            case FOREACH_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
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
                reportInvalidQualifierList(qualifiers);
                return parseExpressionStatement(getAnnotations(annots));
            case XMLNS_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseXMLNamespaceDeclaration(false);
            case TRANSACTION_KEYWORD:
                return parseTransactionStmtOrVarDecl(annots, qualifiers, consume());
            case RETRY_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseRetryStatement();
            case ROLLBACK_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseRollbackStatement();
            case OPEN_BRACKET_TOKEN:
                reportInvalidQualifierList(qualifiers);
                return parseStatementStartsWithOpenBracket(getAnnotations(annots), false);
            case FUNCTION_KEYWORD:
            case OPEN_PAREN_TOKEN:
                // Can be a singleton type or expression.
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case STRING_KEYWORD:
            case XML_KEYWORD:
                // These are statement starting tokens that has ambiguity between
                // being a type-desc or an expressions.
                return parseStmtStartsWithTypeOrExpr(getAnnotations(annots), qualifiers);
            case MATCH_KEYWORD:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseMatchStatement();
            case ERROR_KEYWORD:
                // Error type desc or error binding pattern
                reportInvalidQualifierList(qualifiers);
                return parseErrorTypeDescOrErrorBP(getAnnotations(annots));
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    // These are expressions that are definitely not types.
                    reportInvalidQualifierList(qualifiers);
                    return parseStatementStartWithExpr(getAnnotations(annots));
                }

                if (isTypeStartingToken(nextToken.kind)) {
                    // If the statement starts with a type, then its a var declaration.
                    // This is an optimization since if we know the next token is a type, then
                    // we can parse the var-def faster.
                    publicQualifier = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), publicQualifier, new ArrayList<>(), qualifiers,
                            false);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT_WITHOUT_ANNOTS);

                if (solution.action == Action.KEEP) {
                    // singleton type starting tokens can be correct one's hence keep them.
                    reportInvalidQualifierList(qualifiers);
                    finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), finalKeyword);
                }

                return parseStatement(annots, qualifiers);
        }
    }

    /**
     * <p>
     * Parse local variable declaration.
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
     * @param annots       Annotations or metadata
     * @param finalKeyword Preceding final qualifier
     * @return Parsed node
     */
    private STNode parseVariableDecl(STNode annots, STNode finalKeyword) {
        List<STNode> typeDescQualifiers = new ArrayList<>();
        List<STNode> varDecQualifiers = new ArrayList<>();
        if (finalKeyword != null) {
            varDecQualifiers.add(finalKeyword);
        }
        STNode publicQualifier = STNodeFactory.createEmptyNode();
        return parseVariableDecl(annots, publicQualifier, varDecQualifiers, typeDescQualifiers, false);
    }

    private STNode parseVariableDecl(STNode annots, STNode publicQualifier, List<STNode> varDeclQuals,
                                     List<STNode> typeDescQualifiers, boolean isModuleVar) {
        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode typeBindingPattern = parseTypedBindingPattern(typeDescQualifiers,
                ParserRuleContext.VAR_DECL_STMT);
        return parseVarDeclRhs(annots, publicQualifier, varDeclQuals, typeBindingPattern, isModuleVar);
    }

    private STNode parseVarDeclTypeDescRhs(STNode typeDesc, STNode metadata, List<STNode> qualifiers,
                                           boolean isTypedBindingPattern, boolean isModuleVar) {
        STNode publicQualifier = STNodeFactory.createEmptyNode();
        return parseVarDeclTypeDescRhs(typeDesc, metadata, publicQualifier, qualifiers, isTypedBindingPattern,
                isModuleVar);
    }

    private STNode parseVarDeclTypeDescRhs(STNode typeDesc, STNode metadata, STNode publicQual, List<STNode> qualifiers,
                                           boolean isTypedBindingPattern, boolean isModuleVar) {
        startContext(ParserRuleContext.VAR_DECL_STMT);
        // Check for complex type desc
        typeDesc = parseComplexTypeDescriptor(typeDesc,
                ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, isTypedBindingPattern);

        STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc,
                ParserRuleContext.VAR_DECL_STMT);
        return parseVarDeclRhs(metadata, publicQual, qualifiers, typedBindingPattern, isModuleVar);
    }

    /**
     * <p>
     * Parse the right hand side of a variable declaration statement.
     * </p>
     * <code>
     * var-decl-rhs := ; | = action-or-expr ;
     * </code>
     *
     * @param metadata                metadata
     * @param varDeclQuals Isolated final qualifiers
     * @param typedBindingPattern     Typed binding pattern
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(STNode metadata, List<STNode> varDeclQuals,
                                   STNode typedBindingPattern, boolean isModuleVar) {
        STNode publicQualifier = STNodeFactory.createEmptyNode();
        return parseVarDeclRhs(metadata, publicQualifier, varDeclQuals, typedBindingPattern, isModuleVar);
    }

    private STNode parseVarDeclRhs(STNode metadata, STNode publicQualifier, List<STNode> varDeclQuals,
                                   STNode typedBindingPattern, boolean isModuleVar) {
        STNode assign;
        STNode expr;
        STNode semicolon;
        boolean hasVarInit = false;
        boolean isConfigurable = false;

        if (isModuleVar && isSyntaxKindInList(varDeclQuals, SyntaxKind.CONFIGURABLE_KEYWORD)) {
            isConfigurable = true;
        }

        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EQUAL_TOKEN:
                assign = parseAssignOp();
                if (isModuleVar) {
                    if (isConfigurable) {
                        expr = parseConfigurableVarDeclRhs();
                    } else {
                        expr = parseExpression();
                    }
                } else {
                    expr = parseActionOrExpression();
                }
                semicolon = parseSemicolon();
                hasVarInit = true;
                break;
            case SEMICOLON_TOKEN:
                assign = STNodeFactory.createEmptyNode();
                expr = STNodeFactory.createEmptyNode();
                semicolon = parseSemicolon();
                break;
            default:
                recover(nextToken, ParserRuleContext.VAR_DECL_STMT_RHS);
                return parseVarDeclRhs(metadata, publicQualifier, varDeclQuals, typedBindingPattern, isModuleVar);
        }

        endContext();

        if (!hasVarInit) {
            SyntaxKind bindingPatternKind = ((STTypedBindingPatternNode) typedBindingPattern).bindingPattern.kind;
            if (bindingPatternKind != SyntaxKind.CAPTURE_BINDING_PATTERN) {
                assign = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.EQUAL_TOKEN,
                        DiagnosticErrorCode.ERROR_VARIABLE_DECL_HAVING_BP_MUST_BE_INITIALIZED);
                STNode identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
                expr = STNodeFactory.createSimpleNameReferenceNode(identifier);
            }
        }

        if (isModuleVar) {
            return createModuleVarDeclaration(metadata, publicQualifier, varDeclQuals, typedBindingPattern, assign,
                    expr, semicolon, isConfigurable, hasVarInit);
        }

        STNode finalKeyword;
        if (varDeclQuals.isEmpty()) {
            finalKeyword = STNodeFactory.createEmptyNode();
        } else {
            finalKeyword = varDeclQuals.get(0);
        }
        assert metadata.kind == SyntaxKind.LIST; // Annotations only
        return STNodeFactory.createVariableDeclarationNode(metadata, finalKeyword, typedBindingPattern, assign,
                expr, semicolon);
    }

    private STNode parseConfigurableVarDeclRhs() {
        STNode expr;
        STToken nextToken = peek();
        switch(nextToken.kind) {
            case QUESTION_MARK_TOKEN:
                expr = STNodeFactory.createRequiredExpressionNode(consume());
                break;
            default:
                if (isValidExprStart(nextToken.kind)) {
                    expr = parseExpression();
                    break;
                }
                recover(nextToken, ParserRuleContext.CONFIG_VAR_DECL_RHS);
                return parseConfigurableVarDeclRhs();
        }
        return expr;
    }

    private STNode createModuleVarDeclaration(STNode metadata, STNode publicQualifier, List<STNode> varDeclQuals,
                                              STNode typedBindingPattern, STNode assign, STNode expr, STNode semicolon,
                                              boolean isConfigurable, boolean hasVarInit) {
        if (hasVarInit || varDeclQuals.isEmpty()) {
            return createModuleVarDeclaration(metadata, publicQualifier, varDeclQuals, typedBindingPattern, assign,
                    expr, semicolon);
        }

        if (isConfigurable) {
            return createConfigurableModuleVarDeclWithMissingInitializer(metadata, publicQualifier, varDeclQuals,
                    typedBindingPattern, semicolon);
        }

        // If following 3 conditions are satisfied, we should let isolated qualifier to be a part of the type.
        // 1. module variable declaration is not initialized.
        // 2. type descriptor in the typed binding pattern is either object or function type.
        // 3. qualifier list has isolated qualifier as the last token.
        STNode lastQualifier = getLastNodeInList(varDeclQuals);
        if (lastQualifier.kind == SyntaxKind.ISOLATED_KEYWORD) {
            lastQualifier = varDeclQuals.remove(varDeclQuals.size() - 1);
            typedBindingPattern =
                    modifyTypedBindingPatternWithIsolatedQualifier(typedBindingPattern, lastQualifier);
        }

        return createModuleVarDeclaration(metadata, publicQualifier, varDeclQuals, typedBindingPattern, assign, expr,
                semicolon);
    }

    private STNode createConfigurableModuleVarDeclWithMissingInitializer(STNode metadata, STNode publicQualifier,
                                                                         List<STNode> varDeclQuals,
                                                                         STNode typedBindingPattern, STNode semicolon) {
        // Configurable variable must have an initialization.
        STNode assign = SyntaxErrors.createMissingToken(SyntaxKind.EQUAL_TOKEN);
        assign = SyntaxErrors.addDiagnostic(assign,
                DiagnosticErrorCode.ERROR_CONFIGURABLE_VARIABLE_MUST_BE_INITIALIZED_OR_REQUIRED);
        STToken questionMarkToken = SyntaxErrors.createMissingToken(SyntaxKind.QUESTION_MARK_TOKEN);
        STNode expr = STNodeFactory.createRequiredExpressionNode(questionMarkToken);
        return createModuleVarDeclaration(metadata, publicQualifier, varDeclQuals, typedBindingPattern, assign, expr,
                semicolon);
    }

    // Do not use this method directly.
    private STNode createModuleVarDeclaration(STNode metadata, STNode publicQualifier, List<STNode> varDeclQuals,
                                              STNode typedBindingPattern, STNode assign,
                                              STNode expr, STNode semicolon) {
        // Invalidate public qualifier with isolated qualifier and declared with var
        if (publicQualifier != null) {
            if (((STTypedBindingPatternNode) typedBindingPattern).typeDescriptor.kind == SyntaxKind.VAR_TYPE_DESC) {
                if (varDeclQuals.size() > 0) {
                    updateFirstNodeInListWithLeadingInvalidNode(varDeclQuals, publicQualifier,
                            DiagnosticErrorCode.ERROR_VARIABLE_DECLARED_WITH_VAR_CANNOT_BE_PUBLIC);
                } else {
                    typedBindingPattern = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(typedBindingPattern,
                            publicQualifier, DiagnosticErrorCode.ERROR_VARIABLE_DECLARED_WITH_VAR_CANNOT_BE_PUBLIC);
                }
                publicQualifier = STNodeFactory.createEmptyNode();

            } else if (isSyntaxKindInList(varDeclQuals, SyntaxKind.ISOLATED_KEYWORD)) {
                updateFirstNodeInListWithLeadingInvalidNode(varDeclQuals, publicQualifier,
                        DiagnosticErrorCode.ERROR_ISOLATED_VAR_CANNOT_BE_DECLARED_AS_PUBLIC);
                publicQualifier = STNodeFactory.createEmptyNode();
            }
        }

        STNode varDeclQualifiersNode = STNodeFactory.createNodeList(varDeclQuals);
        return STNodeFactory.createModuleVariableDeclarationNode(metadata, publicQualifier, varDeclQualifiersNode,
                typedBindingPattern, assign, expr, semicolon);
    }

    private STNode createMissingSimpleVarDecl(boolean isModuleVar) {
        STNode metadata = isModuleVar ? STNodeFactory.createEmptyNode() : STNodeFactory.createEmptyNodeList();
        return createMissingSimpleVarDecl(metadata, isModuleVar);
    }

    private STNode createMissingSimpleVarDecl(STNode metadata, boolean isModuleVar) {
        STNode publicQualifier = STNodeFactory.createEmptyNode();
        return createMissingSimpleVarDecl(metadata, publicQualifier, new ArrayList<>(), isModuleVar);
    }

    private STNode createMissingSimpleVarDecl(STNode metadata, STNode publicQualifier, List<STNode> qualifiers,
                                              boolean isModuleVar) {
        STNode emptyNode = STNodeFactory.createEmptyNode();
        STNode simpleTypeDescIdentifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC);
        STNode identifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_VARIABLE_NAME);
        STNode simpleNameRef = STNodeFactory.createSimpleNameReferenceNode(simpleTypeDescIdentifier);
        STNode semicolon = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.SEMICOLON_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_TOKEN);
        
        STNode captureBP = STNodeFactory.createCaptureBindingPatternNode(identifier);
        STNode typedBindingPattern = STNodeFactory.createTypedBindingPatternNode(simpleNameRef, captureBP);
        
        if (isModuleVar) {
            List<STNode> varDeclQuals = extractVarDeclQualifiers(qualifiers, isModuleVar);
            typedBindingPattern = modifyNodeWithInvalidTokenList(qualifiers, typedBindingPattern);

            if (isSyntaxKindInList(varDeclQuals, SyntaxKind.CONFIGURABLE_KEYWORD)) {
                return createConfigurableModuleVarDeclWithMissingInitializer(metadata, publicQualifier, varDeclQuals,
                        typedBindingPattern, semicolon);
            }

            STNode varDeclQualNodeList = STNodeFactory.createNodeList(varDeclQuals);
            return STNodeFactory.createModuleVariableDeclarationNode(metadata, publicQualifier, varDeclQualNodeList,
                    typedBindingPattern, emptyNode, emptyNode, semicolon);
        }

        typedBindingPattern = modifyNodeWithInvalidTokenList(qualifiers, typedBindingPattern);

        return STNodeFactory.createVariableDeclarationNode(metadata, emptyNode, typedBindingPattern, emptyNode,
                emptyNode, semicolon);
    }

    private STNode createMissingWhereClause() {
        STNode whereKeyword = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.WHERE_KEYWORD,
                DiagnosticErrorCode.ERROR_MISSING_WHERE_KEYWORD);
        STNode missingIdentifier = SyntaxErrors.createMissingTokenWithDiagnostics(
                SyntaxKind.IDENTIFIER_TOKEN, DiagnosticErrorCode.ERROR_MISSING_EXPRESSION);
        STNode missingExpr = STNodeFactory.createSimpleNameReferenceNode(missingIdentifier);
        return STNodeFactory.createWhereClauseNode(whereKeyword, missingExpr);
    }

    private STNode createMissingSimpleObjectField(STNode metadata, List<STNode> qualifiers, boolean isObjectTypeDesc) {
        STNode emptyNode = STNodeFactory.createEmptyNode();
        STNode simpleTypeDescIdentifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC);
        STNode simpleNameRef = STNodeFactory.createSimpleNameReferenceNode(simpleTypeDescIdentifier);
        STNode identifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_FIELD_NAME);
        STNode semicolon = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.SEMICOLON_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_SEMICOLON_TOKEN);

        List<STNode> objectFieldQualifiers = extractObjectFieldQualifiers(qualifiers, isObjectTypeDesc);
        STNode objectFieldQualNodeList = STNodeFactory.createNodeList(objectFieldQualifiers);

        simpleNameRef = modifyNodeWithInvalidTokenList(qualifiers, simpleNameRef);

        if (metadata != null) {
            metadata = addMetadataNotAttachedDiagnostic((STMetadataNode) metadata);
        }

        return STNodeFactory.createObjectFieldNode(metadata, emptyNode, objectFieldQualNodeList,
                simpleNameRef, identifier, emptyNode, emptyNode, semicolon);
    }

    private STNode createMissingSimpleObjectField() {
        STNode metadata = STNodeFactory.createEmptyNode();
        List<STNode> qualifiers = new ArrayList<>();
        return createMissingSimpleObjectField(metadata,  qualifiers, false);
    }

    private STNode modifyNodeWithInvalidTokenList(List<STNode> qualifiers, STNode node) {
        for (int i = qualifiers.size() - 1; i >= 0; i--) {
            STNode qualifier = qualifiers.get(i);
            node = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(node, qualifier);
        }
        return node;
    }

    private STNode modifyTypedBindingPatternWithIsolatedQualifier(STNode typedBindingPattern,
                                                                  STNode isolatedQualifier) {
        STTypedBindingPatternNode typedBindingPatternNode = (STTypedBindingPatternNode) typedBindingPattern;
        STNode typeDescriptor = typedBindingPatternNode.typeDescriptor;
        STNode bindingPattern = typedBindingPatternNode.bindingPattern;
        switch (typeDescriptor.kind) {
            case OBJECT_TYPE_DESC:
                typeDescriptor = modifyObjectTypeDescWithALeadingQualifier(typeDescriptor, isolatedQualifier);
                break;
            case FUNCTION_TYPE_DESC:
                typeDescriptor = modifyFuncTypeDescWithALeadingQualifier(typeDescriptor, isolatedQualifier);
                break;
            default:
                typeDescriptor = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(typeDescriptor, isolatedQualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) isolatedQualifier).text());
        }

        return STNodeFactory.createTypedBindingPatternNode(typeDescriptor, bindingPattern);
    }

    /**
     * Add a leading qualifier to object type descriptor and modify the node.
     *
     * @param objectTypeDesc Object type descriptor node to be modified
     * @param newQualifier   New qualifier to be made an object type qualifier
     * @return Modified node
     */
    private STNode modifyObjectTypeDescWithALeadingQualifier(STNode objectTypeDesc, STNode newQualifier) {
        STObjectTypeDescriptorNode objectTypeDescriptorNode = (STObjectTypeDescriptorNode) objectTypeDesc;
        STNodeList qualifierList = (STNodeList) objectTypeDescriptorNode.objectTypeQualifiers;
        STNode newObjectTypeQualifiers = modifyNodeListWithALeadingQualifier(qualifierList, newQualifier);
        return objectTypeDescriptorNode.modify(newObjectTypeQualifiers, objectTypeDescriptorNode.objectKeyword,
                objectTypeDescriptorNode.openBrace, objectTypeDescriptorNode.members,
                objectTypeDescriptorNode.closeBrace);
    }

    /**
     * Add a leading qualifier to function type descriptor and modify the node.
     *
     * @param funcTypeDesc Function type descriptor node to be modified
     * @param newQualifier New qualifier to be made an object type qualifier
     * @return Modified node
     */
    private STNode modifyFuncTypeDescWithALeadingQualifier(STNode funcTypeDesc, STNode newQualifier) {
        STFunctionTypeDescriptorNode funcTypeDescriptorNode = (STFunctionTypeDescriptorNode) funcTypeDesc;
        STNode qualifierList = funcTypeDescriptorNode.qualifierList;
        STNode newfuncTypeQualifiers = modifyNodeListWithALeadingQualifier(qualifierList, newQualifier);
        return funcTypeDescriptorNode.modify(newfuncTypeQualifiers, funcTypeDescriptorNode.functionKeyword,
                funcTypeDescriptorNode.functionSignature);
    }

    /**
     * Add a leading qualifier to qualifier list and modify the node.
     * In case same kind of qualifier is already in the list, an error will be logged.
     *
     * @param qualifiers   STNodeList to be modified
     * @param newQualifier New qualifier to be added to the qualifier list
     * @return Modified node
     */
    private STNode modifyNodeListWithALeadingQualifier(STNode qualifiers, STNode newQualifier) {
        List<STNode> newQualifierList = new ArrayList<>();
        newQualifierList.add(newQualifier);

        STNodeList qualifierNodeList = (STNodeList) qualifiers;
        for (int i = 0; i < qualifierNodeList.size(); i++) {
            STNode qualifier = qualifierNodeList.get(i);
            if (qualifier.kind == newQualifier.kind) {
                updateLastNodeInListWithInvalidNode(newQualifierList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
            } else {
                newQualifierList.add(qualifier);
            }
        }

        return STNodeFactory.createNodeList(newQualifierList);
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
        STNode assign = parseAssignOp();
        STNode expr = parseActionOrExpression();
        STNode semicolon = parseSemicolon();
        endContext();

        if (lvExpr.kind == SyntaxKind.ERROR_CONSTRUCTOR &&
                isPossibleErrorBindingPattern((STErrorConstructorExpressionNode) lvExpr)) {
            lvExpr = getBindingPattern(lvExpr, false);
        }

        if (isWildcardBP(lvExpr)) {
            lvExpr = getWildcardBindingPattern(lvExpr);
        }
        
        boolean lvExprValid = isValidLVExpr(lvExpr);
        if (!lvExprValid) {
            // Create a missing simple variable reference and attach the invalid lvExpr as minutiae
            STNode identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            STNode simpleNameRef = STNodeFactory.createSimpleNameReferenceNode(identifier);
            lvExpr = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(simpleNameRef, lvExpr,
                    DiagnosticErrorCode.ERROR_INVALID_EXPR_IN_ASSIGNMENT_LHS);
        }
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

    private STNode parseActionOrExpressionInLhs(STNode annots) {
        return parseExpression(DEFAULT_OP_PRECEDENCE, annots, false, true, false);
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

    private boolean isValidLVExpr(STNode expression) {
        switch (expression.kind) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case ERROR_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
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
     * @param isRhsExpr       Flag indicating whether this is a rhs expression
     * @param allowActions    Flag indicating whether the current context support actions
     * @return Parsed node
     */
    private STNode parseExpression(OperatorPrecedence precedenceLevel, boolean isRhsExpr, boolean allowActions) {
        return parseExpression(precedenceLevel, isRhsExpr, allowActions, false);
    }

    private STNode parseExpression(OperatorPrecedence precedenceLevel, boolean isRhsExpr, boolean allowActions,
                                   boolean isInConditionalExpr) {
        return parseExpression(precedenceLevel, isRhsExpr, allowActions, false, isInConditionalExpr);
    }

    private STNode parseExpression(OperatorPrecedence precedenceLevel, boolean isRhsExpr, boolean allowActions,
                                   boolean isInMatchGuard, boolean isInConditionalExpr) {
        STNode expr = parseTerminalExpression(isRhsExpr, allowActions, isInConditionalExpr);
        return parseExpressionRhs(precedenceLevel, expr, isRhsExpr, allowActions, isInMatchGuard, isInConditionalExpr);
    }

    private STNode invalidateActionAndGetMissingExpr(STNode node) {
        STToken identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
        identifier = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(identifier, node,
                DiagnosticErrorCode.ERROR_EXPRESSION_EXPECTED_ACTION_FOUND);
        return STNodeFactory.createSimpleNameReferenceNode(identifier);
    }

    private STNode parseExpression(OperatorPrecedence precedenceLevel, STNode annots, boolean isRhsExpr,
                                   boolean allowActions, boolean isInConditionalExpr) {
        STNode expr = parseTerminalExpression(annots, isRhsExpr, allowActions, isInConditionalExpr);
        return parseExpressionRhs(precedenceLevel, expr, isRhsExpr, allowActions, false, isInConditionalExpr);
    }

    private STNode parseTerminalExpression(boolean isRhsExpr, boolean allowActions, boolean isInConditionalExpr) {
        STNode annots = STNodeFactory.createEmptyNodeList();
        if (peek().kind == SyntaxKind.AT_TOKEN) {
            annots = parseOptionalAnnotations();
        }
        return parseTerminalExpression(annots, isRhsExpr, allowActions, isInConditionalExpr);
    }

    private STNode parseTerminalExpression(STNode annots, boolean isRhsExpr, boolean allowActions,
                                           boolean isInConditionalExpr) {
        List<STNode> qualifiers = new ArrayList<>();
        return parseTerminalExpression(annots, qualifiers, isRhsExpr, allowActions, isInConditionalExpr);
    }

    /**
     * Parse terminal expressions. A terminal expression has the highest precedence level
     * out of all expressions, and will be at the leaves of an expression tree.
     *
     * @param annots       Annotations
     * @param qualifiers   Initial qualifiers
     * @param isRhsExpr    Is a rhs expression
     * @param allowActions Allow actions
     * @return Parsed node
     */
    private STNode parseTerminalExpression(STNode annots, List<STNode> qualifiers, boolean isRhsExpr,
                                           boolean allowActions, boolean isInConditionalExpr) {
        parseExprQualifiers(qualifiers);
        STToken nextToken = peek();

        STNodeList annotNodeList = (STNodeList) annots;
        if (!annotNodeList.isEmpty() && !isAnnotAllowedExprStart(nextToken)) {
            annots = addAnnotNotAttachedDiagnostic(annotNodeList);
            STNode qualifierNodeList = createObjectTypeQualNodeList(qualifiers);
            return createMissingObjectConstructor(annots, qualifierNodeList);
        }

        // Whenever a new expression start is added, make sure to
        // add relevant entries in isValidExprStart and validateExprAnnotsAndQualifiers methods.
        validateExprAnnotsAndQualifiers(nextToken, annots, qualifiers);
        if (isQualifiedIdentifierPredeclaredPrefix(nextToken.kind)) {
            return parseQualifiedIdentifierOrExpression(isInConditionalExpr, isRhsExpr, allowActions);
        }

        switch (nextToken.kind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                return parseBasicLiteral();
            case IDENTIFIER_TOKEN:
                return parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF, isInConditionalExpr);
            case OPEN_PAREN_TOKEN:
                return parseBracedExpression(isRhsExpr, allowActions);
            case CHECK_KEYWORD:
            case CHECKPANIC_KEYWORD:
                // In the checking action, nested actions are allowed. And that's the only
                // place where actions are allowed within an action or an expression.
                return parseCheckExpression(isRhsExpr, allowActions, isInConditionalExpr);
            case OPEN_BRACE_TOKEN:
                return parseMappingConstructorExpr();
            case TYPEOF_KEYWORD:
                return parseTypeofExpression(isRhsExpr, isInConditionalExpr);
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case NEGATION_TOKEN:
            case EXCLAMATION_MARK_TOKEN:
                return parseUnaryExpression(isRhsExpr, isInConditionalExpr);
            case TRAP_KEYWORD:
                return parseTrapExpression(isRhsExpr, allowActions, isInConditionalExpr);
            case OPEN_BRACKET_TOKEN:
                return parseListConstructorExpr();
            case LT_TOKEN:
                return parseTypeCastExpr(isRhsExpr, allowActions, isInConditionalExpr);
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
            case FROM_KEYWORD:
            case MAP_KEYWORD:
                return parseTableConstructorOrQuery(isRhsExpr, allowActions);
            case ERROR_KEYWORD:
                return parseErrorConstructorExpr(consume());
            case LET_KEYWORD:
                return parseLetExpression(isRhsExpr, isInConditionalExpr);
            case BACKTICK_TOKEN:
                return parseTemplateExpression();
            case OBJECT_KEYWORD:
                return parseObjectConstructorExpression(annots, qualifiers);
            case XML_KEYWORD:
                return parseXMLTemplateExpression();
            case RE_KEYWORD:
                return parseRegExpTemplateExpression();
            case STRING_KEYWORD:
                STToken nextNextToken = getNextNextToken();
                if (nextNextToken.kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseStringTemplateExpression();
                }
                return parseSimpleTypeInTerminalExpr();
            case FUNCTION_KEYWORD:
                return parseExplicitFunctionExpression(annots, qualifiers, isRhsExpr);
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
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
                return parseByteArrayLiteral();
            case TRANSACTION_KEYWORD:
                return parseQualifiedIdentWithTransactionPrefix(ParserRuleContext.VARIABLE_REF);
            default:
                if (isSimpleTypeInExpression(nextToken.kind)) {
                    return parseSimpleTypeInTerminalExpr();
                }

                recover(nextToken, ParserRuleContext.TERMINAL_EXPRESSION);
                return parseTerminalExpression(annots, qualifiers, isRhsExpr, allowActions, isInConditionalExpr);
        }
    }

    private STNode createMissingObjectConstructor(STNode annots, STNode qualifierNodeList) {
        STNode objectKeyword = SyntaxErrors.createMissingToken(SyntaxKind.OBJECT_KEYWORD);
        STNode openBrace = SyntaxErrors.createMissingToken(SyntaxKind.OPEN_BRACE_TOKEN);
        STNode closeBrace = SyntaxErrors.createMissingToken(SyntaxKind.CLOSE_BRACE_TOKEN);

        STNode objConstructor = STNodeFactory.createObjectConstructorExpressionNode(annots, qualifierNodeList,
                objectKeyword, STNodeFactory.createEmptyNode(), openBrace, STNodeFactory.createEmptyNodeList(),
                closeBrace);
        objConstructor = SyntaxErrors.addDiagnostic(objConstructor,
                DiagnosticErrorCode.ERROR_MISSING_OBJECT_CONSTRUCTOR_EXPRESSION);
        return objConstructor;
    }

    private STNode parseQualifiedIdentifierOrExpression(boolean isInConditionalExpr, boolean isRhsExpr,
                                                        boolean allowActions) {
        STToken preDeclaredPrefix = consume();
        STToken nextNextToken = getNextNextToken();
        if (nextNextToken.kind == SyntaxKind.IDENTIFIER_TOKEN && !isKeyKeyword(nextNextToken)) {
            return parseQualifiedIdentifierWithPredeclPrefix(preDeclaredPrefix, isInConditionalExpr);
        }
        ParserRuleContext context;
        switch (preDeclaredPrefix.kind) {
            case TABLE_KEYWORD:
                context = ParserRuleContext.TABLE_CONS_OR_QUERY_EXPR_OR_VAR_REF;
                break;
            case STREAM_KEYWORD:
                context = ParserRuleContext.QUERY_EXPR_OR_VAR_REF;
                break;
            case ERROR_KEYWORD:
                context = ParserRuleContext.ERROR_CONS_EXPR_OR_VAR_REF;
                break;
            default:
                return parseQualifiedIdentifierWithPredeclPrefix(preDeclaredPrefix, isInConditionalExpr);
        }

        Solution solution = recover(peek(), context);
        if (solution.action == Action.KEEP) {
            return parseQualifiedIdentifierWithPredeclPrefix(preDeclaredPrefix, isInConditionalExpr);
        }
        if (preDeclaredPrefix.kind == SyntaxKind.ERROR_KEYWORD) {
            return parseErrorConstructorExpr(preDeclaredPrefix);
        }
        startContext(ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION);
        STNode tableOrQuery;
        if (preDeclaredPrefix.kind == SyntaxKind.STREAM_KEYWORD) {
            STNode queryConstructType = parseQueryConstructType(preDeclaredPrefix, null);
            tableOrQuery = parseQueryExprRhs(queryConstructType, isRhsExpr, allowActions);
        } else {
            tableOrQuery = parseTableConstructorOrQuery(preDeclaredPrefix, isRhsExpr, allowActions);
        }
        endContext();
        return tableOrQuery;
    }

    private void validateExprAnnotsAndQualifiers(STToken nextToken, STNode annots, List<STNode> qualifiers) {
        switch (nextToken.kind) {
            case START_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                break;
            case FUNCTION_KEYWORD:
            case OBJECT_KEYWORD:
            case AT_TOKEN: // goes to recovery and re-parse
                break;
            default:
                if (isValidExprStart(nextToken.kind)) {
                    reportInvalidExpressionAnnots(annots, qualifiers);
                    reportInvalidQualifierList(qualifiers);
                }
        }
    }

    private boolean isAnnotAllowedExprStart(STToken nextToken) {
        switch (nextToken.kind) {
            case START_KEYWORD:
            case FUNCTION_KEYWORD:
            case OBJECT_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidExprStart(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
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
            case RE_KEYWORD:
            case STRING_KEYWORD:
            case FUNCTION_KEYWORD:
            case AT_TOKEN:
            case NEW_KEYWORD:
            case START_KEYWORD:
            case FLUSH_KEYWORD:
            case LEFT_ARROW_TOKEN:
            case WAIT_KEYWORD:
            case COMMIT_KEYWORD:
            case SERVICE_KEYWORD:
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
            case ISOLATED_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case CLIENT_KEYWORD:
            case OBJECT_KEYWORD:
                return true;
            default:
                if (isPredeclaredPrefix(tokenKind)) {
                    return true;
                }
                return isSimpleTypeInExpression(tokenKind);
        }
    }

    /**
     * <p>
     * Parse a new expression.
     * </p>
     * <code>
     * new-expr := explicit-new-expr | implicit-new-expr
     * <br/>
     * explicit-new-expr := new class-descriptor ( arg-list )
     * <br/>
     * class-descriptor := identifier | qualified-identifier |
     * <br/>&nbsp;stream-type-descriptor
     * <br/>
     * implicit-new-expr := new [( arg-list )]
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
            recover(token, ParserRuleContext.NEW_KEYWORD);
            return parseNewKeyword();
        }
    }

    /**
     * <p>
     * Parse an implicit or explicit new expression.
     * </p>
     *
     * @param newKeyword parsed node for `new` keyword.
     * @return Parsed new-expression node.
     */
    private STNode parseNewKeywordRhs(STNode newKeyword) {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            return parseImplicitNewExpr(newKeyword);
        }
        
        if (isClassDescriptorStartToken(nextToken.kind)) {
            return parseExplicitNewExpr(newKeyword);
        }

        return createImplicitNewExpr(newKeyword, STNodeFactory.createEmptyNode());
    }

    private boolean isClassDescriptorStartToken(SyntaxKind tokenKind) {
        return tokenKind == SyntaxKind.STREAM_KEYWORD || isPredeclaredIdentifier(tokenKind);
    }

    /**
     * <p>
     * Parse explicit new expression.
     * </p>
     * <code>
     * explicit-new-expr := new type-descriptor ( arg-list )
     * </code>
     *
     * @param newKeyword Parsed `new` keyword.
     * @return Parsed explicit-new-expr.
     */
    private STNode parseExplicitNewExpr(STNode newKeyword) {
        STNode typeDescriptor = parseClassDescriptor();
        STNode parenthesizedArgsList = parseParenthesizedArgList();
        return STNodeFactory.createExplicitNewExpressionNode(newKeyword, typeDescriptor, parenthesizedArgsList);
    }

    /**
     * Parse class descriptor.
     * <p>
     * <code>class-descriptor := identifier | qualified-identifier |
     * <br/>&nbsp;stream-type-descriptor</code>
     *
     * @return Parsed node
     */
    private STNode parseClassDescriptor() {
        startContext(ParserRuleContext.CLASS_DESCRIPTOR_IN_NEW_EXPR);
        STNode classDescriptor;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case STREAM_KEYWORD:
                classDescriptor = parseStreamTypeDescriptor(consume());
                break;
            default:
                if (isPredeclaredIdentifier(nextToken.kind)) {
                    classDescriptor = parseTypeReference();
                    break;
                }
                
                recover(nextToken, ParserRuleContext.CLASS_DESCRIPTOR);
                return parseClassDescriptor();
        }
        
        endContext();
        return classDescriptor;
    }
    
    /**
     * <p>
     * Parse implicit new expression with arguments.
     * </p>
     *
     * @param newKeyword Parsed `new` keyword.
     * @return Parsed implicit-new-expr.
     */
    private STNode parseImplicitNewExpr(STNode newKeyword) {
        STNode parenthesizedArgList = parseParenthesizedArgList();
        return createImplicitNewExpr(newKeyword, parenthesizedArgList);
    }

    private STNode createImplicitNewExpr(STNode newKeyword, STNode parenthesizedArgList) {
        return STNodeFactory.createImplicitNewExpressionNode(newKeyword, parenthesizedArgList);
    }

    /**
     * <p>
     * Parse the parenthesized argument list.
     * <br/>
     * <code>
     *     parenthesized-arg-list:= ( arg-list )
     * </code>
     * </p>
     *
     * @return Parsed parenthesized argument list
     */
    private STNode parseParenthesizedArgList() {
        STNode openParan = parseArgListOpenParenthesis();
        STNode arguments = parseArgsList();
        STNode closeParan = parseArgListCloseParenthesis();
        return STNodeFactory.createParenthesizedArgList(openParan, arguments, closeParan);
    }

    /**
     * <p>
     * Parse the right-hand-side of an expression.
     * </p>
     * <code>expr-rhs := (binary-op expression
     * | dot identifier
     * | open-bracket expression close-bracket
     * )*</code>
     *
     * @param precedenceLevel Precedence level of the expression that is being parsed currently
     * @param lhsExpr         LHS expression of the expression
     * @param isRhsExpr       Flag indicating whether this is on a rhsExpr of a statement
     * @param allowActions    Flag indicating whether the current context support actions
     * @return Parsed node
     */
    private STNode parseExpressionRhs(OperatorPrecedence precedenceLevel, STNode lhsExpr, boolean isRhsExpr,
                                      boolean allowActions) {
        return parseExpressionRhs(precedenceLevel, lhsExpr, isRhsExpr, allowActions, false, false);
    }

    /**
     * Parse the right hand side of an expression given the next token kind.
     *
     * @param currentPrecedenceLevel Precedence level of the expression that is being parsed currently
     * @param lhsExpr                LHS expression
     * @param isRhsExpr              Flag indicating whether this is a rhs expr or not
     * @param allowActions           Flag indicating whether to allow actions or not
     * @param isInMatchGuard         Flag indicating whether this expression is in a match-guard
     * @return Parsed node
     */
    private STNode parseExpressionRhs(OperatorPrecedence currentPrecedenceLevel, STNode lhsExpr, boolean isRhsExpr,
                                      boolean allowActions, boolean isInMatchGuard, boolean isInConditionalExpr) {
        STNode actionOrExpression = parseExpressionRhsInternal(currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions,
                isInMatchGuard, isInConditionalExpr);
        // braced actions are just parenthesis enclosing actions, no need to add a diagnostic there when we have added
        // diagnostics to its children
        if (!allowActions && isAction(actionOrExpression) && actionOrExpression.kind != SyntaxKind.BRACED_ACTION) {
            actionOrExpression = invalidateActionAndGetMissingExpr(actionOrExpression);
        }
        return actionOrExpression;
    }

    private STNode parseExpressionRhsInternal(OperatorPrecedence currentPrecedenceLevel, STNode lhsExpr,
                                              boolean isRhsExpr, boolean allowActions, boolean isInMatchGuard,
                                              boolean isInConditionalExpr) {
        SyntaxKind nextTokenKind = peek().kind;
        if (isAction(lhsExpr) || isEndOfActionOrExpression(nextTokenKind, isRhsExpr, isInMatchGuard)) {
            // Action has to be the left most action or expression
            return lhsExpr;
        }

        if (!isValidExprRhsStart(nextTokenKind, lhsExpr.kind)) {
            return recoverExpressionRhs(currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions, isInMatchGuard,
                    isInConditionalExpr);
        }

        // Look for >> and >>> tokens as they are not sent from lexer due to ambiguity. e.g. <map<int>> a
        if (nextTokenKind == SyntaxKind.GT_TOKEN && peek(2).kind == SyntaxKind.GT_TOKEN) {
            if (peek(3).kind == SyntaxKind.GT_TOKEN) {
                nextTokenKind = SyntaxKind.TRIPPLE_GT_TOKEN;
            } else {
                nextTokenKind = SyntaxKind.DOUBLE_GT_TOKEN;
            }
        }

        // If the precedence level of the operator that was being parsed is higher than the newly found (next)
        // operator, then return and finish the previous expr, because it has a higher precedence.
        OperatorPrecedence nextOperatorPrecedence = getOpPrecedence(nextTokenKind);
        if (currentPrecedenceLevel.isHigherThanOrEqual(nextOperatorPrecedence, allowActions)) {
            return lhsExpr;
        }

        STNode newLhsExpr;
        STNode operator;
        switch (nextTokenKind) {
            case OPEN_PAREN_TOKEN:
                newLhsExpr = parseFuncCall(lhsExpr);
                break;
            case OPEN_BRACKET_TOKEN:
                newLhsExpr = parseMemberAccessExpr(lhsExpr, isRhsExpr);
                break;
            case DOT_TOKEN:
                newLhsExpr = parseFieldAccessOrMethodCall(lhsExpr, isInConditionalExpr);
                break;
            case IS_KEYWORD:
            case NOT_IS_KEYWORD:
                newLhsExpr = parseTypeTestExpression(lhsExpr, isInConditionalExpr);
                break;
            case RIGHT_ARROW_TOKEN:
                newLhsExpr = parseRemoteMethodCallOrClientResourceAccessOrAsyncSendAction(lhsExpr, isRhsExpr, 
                        isInMatchGuard);
                break;
            case SYNC_SEND_TOKEN:
                newLhsExpr = parseSyncSendAction(lhsExpr);
                break;
            case RIGHT_DOUBLE_ARROW_TOKEN:
                newLhsExpr = parseImplicitAnonFunc(lhsExpr, isRhsExpr);
                break;
            case ANNOT_CHAINING_TOKEN:
                newLhsExpr = parseAnnotAccessExpression(lhsExpr, isInConditionalExpr);
                break;
            case OPTIONAL_CHAINING_TOKEN:
                newLhsExpr = parseOptionalFieldAccessExpression(lhsExpr, isInConditionalExpr);
                break;
            case QUESTION_MARK_TOKEN:
                newLhsExpr = parseConditionalExpression(lhsExpr, isInConditionalExpr);
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
                // Handle 'a/<b|c>...' scenarios. These have ambiguity between being a xml-step expr
                // or a binary expr (division), with a type-cast as the denominator.
                if (nextTokenKind == SyntaxKind.SLASH_TOKEN && peek(2).kind == SyntaxKind.LT_TOKEN) {
                    SyntaxKind expectedNodeType = getExpectedNodeKind(3);
                    if (expectedNodeType == SyntaxKind.XML_STEP_EXPRESSION) {
                        newLhsExpr = createXMLStepExpression(lhsExpr);
                        break;
                    }

                    // if (expectedNodeType == SyntaxKind.TYPE_CAST_EXPRESSION) or anything else.
                    // Fall through, and continue to parse as a binary expr
                }

                if (nextTokenKind == SyntaxKind.DOUBLE_GT_TOKEN) {
                    operator = parseSignedRightShiftToken();
                } else if (nextTokenKind == SyntaxKind.TRIPPLE_GT_TOKEN) {
                    operator = parseUnsignedRightShiftToken();
                } else {
                    operator = parseBinaryOperator();
                }

                // Treat everything else as binary expression.

                // Parse the expression that follows the binary operator, until a operator with different precedence
                // is encountered. If an operator with a lower precedence is reached, then come back here and finish
                // the current binary expr. If a an operator with higher precedence level is reached, then complete
                // that binary-expr, come back here and finish the current expr.

                STNode rhsExpr = parseExpression(nextOperatorPrecedence, isRhsExpr, false, isInConditionalExpr);
                newLhsExpr = STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, lhsExpr, operator,
                        rhsExpr);
                break;
        }

        // Then continue the operators with the same precedence level.
        return parseExpressionRhsInternal(currentPrecedenceLevel, newLhsExpr, isRhsExpr, allowActions, isInMatchGuard,
                isInConditionalExpr);
    }

    private STNode recoverExpressionRhs(OperatorPrecedence currentPrecedenceLevel, STNode lhsExpr, boolean isRhsExpr,
                                        boolean allowActions, boolean isInMatchGuard, boolean isInConditionalExpr) {
        STToken token = peek();
        SyntaxKind lhsExprKind = lhsExpr.kind;
        Solution solution;
        if (lhsExprKind == SyntaxKind.QUALIFIED_NAME_REFERENCE || lhsExprKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            solution = recover(token, ParserRuleContext.VARIABLE_REF_RHS);
        } else {
            solution = recover(token, ParserRuleContext.EXPRESSION_RHS);
        }

        // If the current rule was recovered by removing a token, then this entire rule is already
        // parsed while recovering. so we done need to parse the remaining of this rule again.
        // Proceed only if the recovery action was an insertion.
        if (solution.action == Action.REMOVE) {
            return parseExpressionRhs(currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions, isInMatchGuard,
                    isInConditionalExpr);
        }

        // If the parser recovered by inserting a token, then try to re-parse the same rule with the
        // inserted token. This is done to pick the correct branch to continue the parsing.
        if (solution.ctx == ParserRuleContext.BINARY_OPERATOR) {
            // We come here if the operator is missing. Treat this as injecting an operator
            // that matches to the current operator precedence level, and continue.
            SyntaxKind binaryOpKind = getBinaryOperatorKindToInsert(currentPrecedenceLevel);
            ParserRuleContext binaryOpContext = getMissingBinaryOperatorContext(currentPrecedenceLevel);
            insertToken(binaryOpKind, binaryOpContext);
        }

        return parseExpressionRhsInternal(currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions, isInMatchGuard,
                isInConditionalExpr);
    }

    private STNode createXMLStepExpression(STNode lhsExpr) {
        STNode newLhsExpr;
        STNode slashToken = parseSlashToken();
        STNode ltToken = parseLTToken();

        STNode slashLT;
        if (hasTrailingMinutiae(slashToken) || hasLeadingMinutiae(ltToken)) {
            List<STNodeDiagnostic> diagnostics = new ArrayList<>();
            diagnostics
                    .add(SyntaxErrors.createDiagnostic(DiagnosticErrorCode.ERROR_INVALID_WHITESPACE_IN_SLASH_LT_TOKEN));
            slashLT = STNodeFactory.createMissingToken(SyntaxKind.SLASH_LT_TOKEN, diagnostics);
            slashLT = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(slashLT, slashToken);
            slashLT = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(slashLT, ltToken);
        } else {
            slashLT = STNodeFactory.createToken(SyntaxKind.SLASH_LT_TOKEN, slashToken.leadingMinutiae(),
                    ltToken.trailingMinutiae());
        }

        STNode namePattern = parseXMLNamePatternChain(slashLT);
        newLhsExpr = STNodeFactory.createXMLStepExpressionNode(lhsExpr, namePattern);
        return newLhsExpr;
    }

    private SyntaxKind getExpectedNodeKind(int lookahead) {
        STToken nextToken = peek(lookahead);
        switch (nextToken.kind) {
            case ASTERISK_TOKEN:
                return SyntaxKind.XML_STEP_EXPRESSION;
            case GT_TOKEN:
                break;
            case PIPE_TOKEN:
                return getExpectedNodeKind(++lookahead);
            case IDENTIFIER_TOKEN:
                nextToken = peek(++lookahead);
                switch (nextToken.kind) {
                    case GT_TOKEN: // a>
                        break;
                    case PIPE_TOKEN: // a|
                        return getExpectedNodeKind(++lookahead);
                    case COLON_TOKEN:
                        nextToken = peek(++lookahead);
                        switch (nextToken.kind) {
                            case ASTERISK_TOKEN: // a:*
                            case GT_TOKEN: // a:>
                                return SyntaxKind.XML_STEP_EXPRESSION;
                            case IDENTIFIER_TOKEN: // a:b
                                nextToken = peek(++lookahead);

                                // a:b |
                                if (nextToken.kind == SyntaxKind.PIPE_TOKEN) {
                                    return getExpectedNodeKind(++lookahead);
                                }

                                // a:b> or everything else
                                break;
                            default:
                                return SyntaxKind.TYPE_CAST_EXPRESSION;
                        }
                        break;
                    default:
                        return SyntaxKind.TYPE_CAST_EXPRESSION;
                }
                break;
            default:
                return SyntaxKind.TYPE_CAST_EXPRESSION;
        }

        nextToken = peek(++lookahead);
        switch (nextToken.kind) {
            case OPEN_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case FROM_KEYWORD:
            case LET_KEYWORD:
                return SyntaxKind.XML_STEP_EXPRESSION;
            default:
                if (isValidExpressionStart(nextToken.kind, lookahead)) {
                    break;
                }
                return SyntaxKind.XML_STEP_EXPRESSION;
        }

        return SyntaxKind.TYPE_CAST_EXPRESSION;
    }

    private boolean hasTrailingMinutiae(STNode node) {
        return node.widthWithTrailingMinutiae() > node.width();
    }

    private boolean hasLeadingMinutiae(STNode node) {
        return node.widthWithLeadingMinutiae() > node.width();
    }

    private boolean isValidExprRhsStart(SyntaxKind tokenKind, SyntaxKind precedingNodeKind) {
        switch (tokenKind) {
            case OPEN_PAREN_TOKEN:
                // Only an identifier or a qualified identifier is followed by a function call.
                return precedingNodeKind == SyntaxKind.QUALIFIED_NAME_REFERENCE ||
                        precedingNodeKind == SyntaxKind.SIMPLE_NAME_REFERENCE;
            case DOT_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case IS_KEYWORD:
            case RIGHT_ARROW_TOKEN:
            case RIGHT_DOUBLE_ARROW_TOKEN:
            case SYNC_SEND_TOKEN:
            case ANNOT_CHAINING_TOKEN:
            case OPTIONAL_CHAINING_TOKEN:
            case COLON_TOKEN:
            case DOT_LT_TOKEN:
            case SLASH_LT_TOKEN:
            case DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN:
            case SLASH_ASTERISK_TOKEN:
            case NOT_IS_KEYWORD:
                return true;
            case QUESTION_MARK_TOKEN:
                // TODO : Should fix properly #33259
                return getNextNextToken().kind != SyntaxKind.EQUAL_TOKEN && peek(3).kind != SyntaxKind.EQUAL_TOKEN;
            default:
                return isBinaryOperator(tokenKind);
        }
    }

    /**
     * Parse member access expression.
     *
     * @param lhsExpr   Container expression
     * @param isRhsExpr Is this is a rhs expression
     * @return Member access expression
     */
    private STNode parseMemberAccessExpr(STNode lhsExpr, boolean isRhsExpr) {
        startContext(ParserRuleContext.MEMBER_ACCESS_KEY_EXPR);
        STNode openBracket = parseOpenBracket();
        STNode keyExpr = parseMemberAccessKeyExprs(isRhsExpr);
        STNode closeBracket = parseCloseBracket();
        endContext();

        // If this is in RHS, then its definitely a member-access.
        if (isRhsExpr && ((STNodeList) keyExpr).isEmpty()) {
            STNode missingVarRef = STNodeFactory
                    .createSimpleNameReferenceNode(SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN));
            keyExpr = STNodeFactory.createNodeList(missingVarRef);
            closeBracket = SyntaxErrors.addDiagnostic(closeBracket,
                    DiagnosticErrorCode.ERROR_MISSING_KEY_EXPR_IN_MEMBER_ACCESS_EXPR);
        }

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

        return STNodeFactory.createNodeList(exprList);
    }

    private STNode parseKeyExpr(boolean isRhsExpr) {
        if (!isRhsExpr && peek().kind == SyntaxKind.ASTERISK_TOKEN) {
            return STNodeFactory.createBasicLiteralNode(SyntaxKind.ASTERISK_LITERAL, consume());
        }

        return parseExpression(isRhsExpr);
    }

    private STNode parseMemberAccessKeyExprEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.MEMBER_ACCESS_KEY_EXPR_END);
                return parseMemberAccessKeyExprEnd();
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
            recover(token, ParserRuleContext.CLOSE_BRACKET);
            return parseCloseBracket();
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
    private STNode parseFieldAccessOrMethodCall(STNode lhsExpr, boolean isInConditionalExpr) {
        STNode dotToken = parseDotToken();
        STToken token = peek();
        if (token.kind == SyntaxKind.MAP_KEYWORD || token.kind == SyntaxKind.START_KEYWORD || 
                token.kind == SyntaxKind.JOIN_KEYWORD) {
            STNode methodName = getKeywordAsSimpleNameRef();
            STNode openParen = parseArgListOpenParenthesis();
            STNode args = parseArgsList();
            STNode closeParen = parseArgListCloseParenthesis();
            return STNodeFactory.createMethodCallExpressionNode(lhsExpr, dotToken, methodName, openParen, args,
                    closeParen);
        }

        STNode fieldOrMethodName = parseFieldAccessIdentifier(isInConditionalExpr);
        if (fieldOrMethodName.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return STNodeFactory.createFieldAccessExpressionNode(lhsExpr, dotToken, fieldOrMethodName);
        }

        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            // function invocation
            STNode openParen = parseArgListOpenParenthesis();
            STNode args = parseArgsList();
            STNode closeParen = parseArgListCloseParenthesis();
            return STNodeFactory.createMethodCallExpressionNode(lhsExpr, dotToken, fieldOrMethodName, openParen, args,
                    closeParen);
        }

        // Everything else is field-access
        return STNodeFactory.createFieldAccessExpressionNode(lhsExpr, dotToken, fieldOrMethodName);
    }

    private STNode getKeywordAsSimpleNameRef() {
        STToken mapKeyword = consume();
        STNode methodName = STNodeFactory.createIdentifierToken(mapKeyword.text(), mapKeyword.leadingMinutiae(),
                mapKeyword.trailingMinutiae(), mapKeyword.diagnostics());
        methodName = STNodeFactory.createSimpleNameReferenceNode(methodName);
        return methodName;
    }

    /**
     * <p>
     * Parse braced expression.
     * </p>
     * <code>braced-expr := ( expression )</code>
     *
     * @param isRhsExpr    Flag indicating whether this is on a rhsExpr of a statement
     * @param allowActions Allow actions
     * @return Parsed node
     */
    private STNode parseBracedExpression(boolean isRhsExpr, boolean allowActions) {
        STNode openParen = parseOpenParenthesis();

        if (peek().kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            // Could be nil literal or empty param-list of an implicit-anon-func-expr'
            return STNodeFactory.createNilLiteralNode(openParen, consume());
        }

        startContext(ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAMS);
        STNode expr;
        if (allowActions) {
            expr = parseExpression(DEFAULT_OP_PRECEDENCE, isRhsExpr, true);
        } else {
            expr = parseExpression(isRhsExpr);
        }

        return parseBracedExprOrAnonFuncParamRhs(openParen, expr, isRhsExpr);
    }

    private STNode parseBracedExprOrAnonFuncParamRhs(STNode openParen, STNode expr, boolean isRhsExpr) {
        STToken nextToken = peek();
        if (expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            switch (nextToken.kind) {
                case CLOSE_PAREN_TOKEN:
                    break;
                case COMMA_TOKEN:
                    // Here the context is ended inside the method.
                    return parseImplicitAnonFunc(openParen, expr, isRhsExpr);
                default:
                    recover(nextToken, ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS);
                    return parseBracedExprOrAnonFuncParamRhs(openParen, expr, isRhsExpr);
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
            case CLIENT_RESOURCE_ACCESS_ACTION:    
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the given token is an end of a action or expression.
     *
     * @param tokenKind Token to check
     * @param isRhsExpr Flag indicating whether this is on a rhsExpr of a statement
     * @return <code>true</code> if the token represents an end of a block. <code>false</code> otherwise
     */
    private boolean isEndOfActionOrExpression(SyntaxKind tokenKind, boolean isRhsExpr, boolean isInMatchGuard) {
        if (!isRhsExpr) {
            if (isCompoundAssignment(tokenKind)) {
                return true;
            }

            if (isInMatchGuard && tokenKind == SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN) {
                return true;
            }
        }

        switch (tokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case OPEN_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case COMMA_TOKEN:
            case PUBLIC_KEYWORD:
            case CONST_KEYWORD:
            case LISTENER_KEYWORD:
            case RESOURCE_KEYWORD:
            case EQUAL_TOKEN:
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
            case AS_KEYWORD:
            case IN_KEYWORD:
            case FROM_KEYWORD:
            case WHERE_KEYWORD:
            case LET_KEYWORD:
            case SELECT_KEYWORD:
            case DO_KEYWORD:
            case COLON_TOKEN:
            case ON_KEYWORD:
            case CONFLICT_KEYWORD:
            case LIMIT_KEYWORD:
            case JOIN_KEYWORD:
            case OUTER_KEYWORD:
            case ORDER_KEYWORD:
            case BY_KEYWORD:
            case ASCENDING_KEYWORD:
            case DESCENDING_KEYWORD:
            case EQUALS_KEYWORD:
            case TYPE_KEYWORD:
                return true;
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return isInMatchGuard;
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
        STNode literalToken = consume();
        return parseBasicLiteral(literalToken);
    }

    private STNode parseBasicLiteral(STNode literalToken) {
        SyntaxKind nodeKind;
        switch (literalToken.kind) {
            case NULL_KEYWORD:
                nodeKind = SyntaxKind.NULL_LITERAL;
                break;
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
                nodeKind = SyntaxKind.BOOLEAN_LITERAL;
                break;
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                nodeKind = SyntaxKind.NUMERIC_LITERAL;
                break;
            case STRING_LITERAL_TOKEN:
                nodeKind = SyntaxKind.STRING_LITERAL;
                break;
            case ASTERISK_TOKEN:
                nodeKind = SyntaxKind.ASTERISK_LITERAL;
                break;
            default:
                nodeKind = literalToken.kind;
        }
        return STNodeFactory.createBasicLiteralNode(nodeKind, literalToken);
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
        STNode openParen = parseArgListOpenParenthesis();
        STNode args = parseArgsList();
        STNode closeParen = parseArgListCloseParenthesis();
        return STNodeFactory.createFunctionCallExpressionNode(identifier, openParen, args, closeParen);
    }

    private STNode parseErrorBindingPatternOrErrorConstructor() {
        return parseErrorConstructorExpr(true);
    }

    private STNode parseErrorConstructorExpr(STNode errorKeyword) {
        startContext(ParserRuleContext.ERROR_CONSTRUCTOR);
        return parseErrorConstructorExpr(errorKeyword, false);
    }

    /**
     * <p>
     * Parse error constructor expression.
     * </p>
     * <code>
     * error-constructor-expr := error [error-type-reference] ( error-arg-list )
     * error-arg-list := positional-arg [, positional-arg] (, named-arg)*
     * </code>
     *
     * @param isAmbiguous Whether ambiguous error-constructor/error-BP parsing
     * @return Error constructor expression
     */
    private STNode parseErrorConstructorExpr(boolean isAmbiguous) {
        startContext(ParserRuleContext.ERROR_CONSTRUCTOR);
        STNode errorKeyword = parseErrorKeyword();
        return parseErrorConstructorExpr(errorKeyword, isAmbiguous);
    }

    private STNode parseErrorConstructorExpr(STNode errorKeyword, boolean isAmbiguous) {
        STNode typeReference = parseErrorTypeReference();
        STNode openParen = parseArgListOpenParenthesis();
        STNode functionArgs = parseArgsList();

        // Skip validation on top of function-args when ambiguous
        STNode errorArgs = isAmbiguous ? functionArgs : getErrorArgList(functionArgs);
        STNode closeParen = parseArgListCloseParenthesis();
        endContext();

        openParen = cloneWithDiagnosticIfListEmpty(errorArgs, openParen,
                DiagnosticErrorCode.ERROR_MISSING_ARG_WITHIN_PARENTHESIS);
        return STNodeFactory.createErrorConstructorExpressionNode(errorKeyword, typeReference, openParen, errorArgs,
                closeParen);
    }

    private STNode parseErrorTypeReference() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                if (isPredeclaredIdentifier(nextToken.kind)) {
                    return parseTypeReference();
                }
                recover(nextToken, ParserRuleContext.ERROR_CONSTRUCTOR_RHS);
                return parseErrorTypeReference();
        }
    }

    /**
     * Validate function args and return error arg node list.
     *
     * @param functionArgs function args to be validated
     * @return Error arg list
     */
    private STNode getErrorArgList(STNode functionArgs) {
        STNodeList argList = (STNodeList) functionArgs;
        if (argList.isEmpty()) {
            return argList;
        }

        List<STNode> errorArgList = new ArrayList<>();
        // Validate first arg
        STNode arg = argList.get(0);
        switch (arg.kind) {
            case POSITIONAL_ARG:
                errorArgList.add(arg);
                break;
            case NAMED_ARG:
                arg = SyntaxErrors.addDiagnostic(arg,
                        DiagnosticErrorCode.ERROR_MISSING_ERROR_MESSAGE_IN_ERROR_CONSTRUCTOR);
                errorArgList.add(arg);
                break;
            default: // rest arg
                arg = SyntaxErrors.addDiagnostic(arg,
                        DiagnosticErrorCode.ERROR_MISSING_ERROR_MESSAGE_IN_ERROR_CONSTRUCTOR);
                arg = SyntaxErrors.addDiagnostic(arg, DiagnosticErrorCode.ERROR_REST_ARG_IN_ERROR_CONSTRUCTOR);
                errorArgList.add(arg);
                break;
        }

        // Validate remaining args
        DiagnosticErrorCode diagnosticErrorCode = DiagnosticErrorCode.ERROR_REST_ARG_IN_ERROR_CONSTRUCTOR;
        boolean hasPositionalArg = false;
        STNode leadingComma;
        for (int i = 1; i < argList.size(); i = i + 2) {
            leadingComma = argList.get(i);
            arg = argList.get(i + 1);

            if (arg.kind == SyntaxKind.NAMED_ARG) {
                errorArgList.add(leadingComma);
                errorArgList.add(arg);
                continue;
            }

            if (arg.kind == SyntaxKind.POSITIONAL_ARG) {
                if (!hasPositionalArg) {
                    errorArgList.add(leadingComma);
                    errorArgList.add(arg);
                    hasPositionalArg = true;
                    continue;
                }
                diagnosticErrorCode = DiagnosticErrorCode.ERROR_ADDITIONAL_POSITIONAL_ARG_IN_ERROR_CONSTRUCTOR;
            }

            // We only reach here for invalid args
            updateLastNodeInListWithInvalidNode(errorArgList, leadingComma, null);
            updateLastNodeInListWithInvalidNode(errorArgList, arg, diagnosticErrorCode);
        }

        return STNodeFactory.createNodeList(errorArgList);
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

        STNode firstArg = parseArgument();
        STNode argsList = parseArgList(firstArg);
        endContext();
        return argsList;
    }

    /**
     * Parse follow up arguments.
     *
     * @param firstArg first argument in the list
     * @return the argument list
     */
    private STNode parseArgList(STNode firstArg) {
        ArrayList<STNode> argsList = new ArrayList<>();
        argsList.add(firstArg);
        SyntaxKind lastValidArgKind = firstArg.kind;

        STToken nextToken = peek();
        while (!isEndOfParametersList(nextToken.kind)) {
            STNode argEnd = parseArgEnd();
            if (argEnd == null) {
                // null marks the end of args
                break;
            }

            STNode curArg = parseArgument();
            DiagnosticErrorCode errorCode = validateArgumentOrder(lastValidArgKind, curArg.kind);
            if (errorCode == null) {
                argsList.add(argEnd);
                argsList.add(curArg);
                lastValidArgKind = curArg.kind;
            } else if (errorCode == DiagnosticErrorCode.ERROR_NAMED_ARG_FOLLOWED_BY_POSITIONAL_ARG &&
                    ((STPositionalArgumentNode) curArg).expression.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                STNode missingEqual = SyntaxErrors.createMissingToken(SyntaxKind.EQUAL_TOKEN);
                STToken missingIdentifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
                STNode nameRef = STNodeFactory.createSimpleNameReferenceNode(missingIdentifier);

                STNode expr = ((STPositionalArgumentNode) curArg).expression;
                if (((STSimpleNameReferenceNode) expr).name.isMissing()) {
                    errorCode = DiagnosticErrorCode.ERROR_MISSING_NAMED_ARG;
                    expr = nameRef; // this is to clean up the missing identifier diagnostic in the expr.
                }

                curArg = STNodeFactory.createNamedArgumentNode(expr, missingEqual, nameRef);
                curArg = SyntaxErrors.addDiagnostic(curArg, errorCode);

                argsList.add(argEnd);
                argsList.add(curArg);
            } else {
                updateLastNodeInListWithInvalidNode(argsList, argEnd, null);
                updateLastNodeInListWithInvalidNode(argsList, curArg, errorCode);
            }

            nextToken = peek();
        }
        return STNodeFactory.createNodeList(argsList);
    }

    private DiagnosticErrorCode validateArgumentOrder(SyntaxKind prevArgKind, SyntaxKind curArgKind) {
        DiagnosticErrorCode errorCode = null;
        switch (prevArgKind) {
            case POSITIONAL_ARG:
                break;
            case NAMED_ARG:
                if (curArgKind == SyntaxKind.POSITIONAL_ARG) {
                    errorCode = DiagnosticErrorCode.ERROR_NAMED_ARG_FOLLOWED_BY_POSITIONAL_ARG;
                }
                break;
            case REST_ARG:
                // Nothing is allowed after a rest arg
                errorCode = DiagnosticErrorCode.ERROR_REST_ARG_FOLLOWED_BY_ANOTHER_ARG;
                break;
            default:
                // This line should never get reached
                throw new IllegalStateException("Invalid SyntaxKind in an argument");
        }
        return errorCode;
    }

    private STNode parseArgEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_PAREN_TOKEN:
                // null marks the end of args
                return null;
            default:
                recover(peek(), ParserRuleContext.ARG_END);
                return parseArgEnd();
        }
    }

    /**
     * Parse function call argument.
     *
     * @return Parsed argument node
     */
    private STNode parseArgument() {
        STNode arg;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ELLIPSIS_TOKEN:
                STToken ellipsis = consume();
                STNode expr = parseExpression();
                arg = STNodeFactory.createRestArgumentNode(ellipsis, expr);
                break;
            case IDENTIFIER_TOKEN:
                // Identifier can means two things: either its a named-arg, or just an expression.
                arg = parseNamedOrPositionalArg();
                break;
            default:
                if (isValidExprStart(nextToken.kind)) {
                    expr = parseExpression();
                    arg = STNodeFactory.createPositionalArgumentNode(expr);
                    break;
                }

                recover(peek(), ParserRuleContext.ARG_START);
                return parseArgument();
        }

        return arg;
    }

    /**
     * Parse positional or named arg. This method assumed peek()/peek(1)
     * is always an identifier.
     *
     * @return Parsed argument node
     */
    private STNode parseNamedOrPositionalArg() {
        STNode argNameOrExpr = parseTerminalExpression(true, false, false);
        STToken secondToken = peek();
        switch (secondToken.kind) {
            case EQUAL_TOKEN:
                STNode equal = parseAssignOp();
                STNode valExpr = parseExpression();
                return STNodeFactory.createNamedArgumentNode(argNameOrExpr, equal, valExpr);
            case COMMA_TOKEN:
            case CLOSE_PAREN_TOKEN:
                return STNodeFactory.createPositionalArgumentNode(argNameOrExpr);
            default:
                // Treat everything else as a single expression. If something is missing,
                // expression-parsing will recover it.
                argNameOrExpr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, argNameOrExpr, true, false);
                return STNodeFactory.createPositionalArgumentNode(argNameOrExpr);
        }
    }

    /**
     * <p>
     * Parse object type descriptor.
     * </p>
     * <code>object-type-descriptor := object-type-quals object { object-member-descriptor* }</code>
     *
     * @param objectTypeQualifiers Object type qualifiers
     * @return Parsed node
     */
    private STNode parseObjectTypeDescriptor(STNode objectKeyword, STNode objectTypeQualifiers) {
        startContext(ParserRuleContext.OBJECT_TYPE_DESCRIPTOR);
        STNode openBrace = parseOpenBrace();
        STNode objectMemberDescriptors = parseObjectMembers(ParserRuleContext.OBJECT_TYPE_MEMBER);
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createObjectTypeDescriptorNode(objectTypeQualifiers, objectKeyword, openBrace,
                objectMemberDescriptors, closeBrace);
    }

    /**
     * Parse object constructor expression.
     * <p>
     * <code>
     * object-constructor-expr := [annots] object-type-quals object [type-reference] { object-member* }
     * <br/>
     * object-member := object-field | method-defn
     * </code>
     *
     * @param annots     Annotations attached to object constructor
     * @param qualifiers Preceding object type qualifiers
     * @return Parsed object constructor expression node
     */
    private STNode parseObjectConstructorExpression(STNode annots, List<STNode> qualifiers) {
        startContext(ParserRuleContext.OBJECT_CONSTRUCTOR);
        STNode objectTypeQualifier = createObjectTypeQualNodeList(qualifiers);
        STNode objectKeyword = parseObjectKeyword();
        STNode typeReference = parseObjectConstructorTypeReference();
        STNode openBrace = parseOpenBrace();
        STNode objectMembers = parseObjectMembers(ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER);
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createObjectConstructorExpressionNode(annots,
                objectTypeQualifier, objectKeyword, typeReference, openBrace, objectMembers, closeBrace);
    }


    /**
     * Parse object constructor expression type reference.
     *
     * @return Parsed type reference or empty node
     */
    private STNode parseObjectConstructorTypeReference() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACE_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                if (isPredeclaredIdentifier(nextToken.kind)) {
                    return parseTypeReference();
                }
                recover(nextToken, ParserRuleContext.OBJECT_CONSTRUCTOR_TYPE_REF);
                return parseObjectConstructorTypeReference();
        }
    }

    private boolean isPredeclaredIdentifier(SyntaxKind tokenKind) {
        return tokenKind == SyntaxKind.IDENTIFIER_TOKEN || isQualifiedIdentifierPredeclaredPrefix(tokenKind);
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
            recover(token, ParserRuleContext.OBJECT_KEYWORD);
            return parseObjectKeyword();
        }
    }

    /**
     * <p>
     * Parse object members.
     * </p>
     * <code>
     * object-members :=
     *      class-member*
     *      | object-member*
     *      | object-member-descriptor*
     * </code>
     *
     * @param context Parsing context of the object members
     * @return Parsed node
     */
    private STNode parseObjectMembers(ParserRuleContext context) {
        ArrayList<STNode> objectMembers = new ArrayList<>();
        while (!isEndOfObjectTypeNode()) {
            startContext(context);
            STNode member = parseObjectMember(context);
            endContext();

            // Null member indicates the end of object members
            if (member == null) {
                break;
            }

            if (context == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER && member.kind == SyntaxKind.TYPE_REFERENCE) {
                addInvalidNodeToNextToken(member, DiagnosticErrorCode.ERROR_TYPE_INCLUSION_IN_OBJECT_CONSTRUCTOR);
            } else {
                objectMembers.add(member);
            }
        }

        return STNodeFactory.createNodeList(objectMembers);
    }

    /**
     * <p>
     * Parse a single class member, object member or object member descriptor.
     * </p>
     * <code>
     * class-member := object-field | method-defn | object-type-inclusion
     * <br/>
     * object-member := object-field | method-defn
     * <br/>
     * object-member-descriptor := object-field-descriptor | method-decl | object-type-inclusion
     * </code>
     *
     * @param context Parsing context of the object member
     * @return Parsed node
     */
    private STNode parseObjectMember(ParserRuleContext context) {
        STNode metadata;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                // Null return indicates the end of object members
                return null;
            case ASTERISK_TOKEN:
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
            case FINAL_KEYWORD:
            case REMOTE_KEYWORD:
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
            case RESOURCE_KEYWORD:
                metadata = STNodeFactory.createEmptyNode();
                break;
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
                metadata = parseMetaData();
                break;
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    metadata = STNodeFactory.createEmptyNode();
                    break;
                }

                ParserRuleContext recoveryCtx;
                if (context == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER) {
                    recoveryCtx = ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER_START;
                } else {
                    recoveryCtx = ParserRuleContext.CLASS_MEMBER_OR_OBJECT_MEMBER_START;
                }

                Solution solution = recover(peek(), recoveryCtx);

                if (solution.action == Action.KEEP) {
                    metadata = STNodeFactory.createEmptyNode();
                    break;
                }
                
                return parseObjectMember(context);
        }

        return parseObjectMemberWithoutMeta(metadata, context);
    }

    private STNode parseObjectMemberWithoutMeta(STNode metadata, ParserRuleContext context) {
        boolean isObjectTypeDesc = context == ParserRuleContext.OBJECT_TYPE_MEMBER;

        ParserRuleContext recoveryCtx;
        if (context == ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER) {
            recoveryCtx = ParserRuleContext.OBJECT_CONS_MEMBER_WITHOUT_META;
        } else {
            recoveryCtx = ParserRuleContext.CLASS_MEMBER_OR_OBJECT_MEMBER_WITHOUT_META;
        }

        List<STNode> typeDescQualifiers = new ArrayList<>();
        return parseObjectMemberWithoutMeta(metadata, typeDescQualifiers, recoveryCtx, isObjectTypeDesc);
    }

    private STNode parseObjectMemberWithoutMeta(STNode metadata, List<STNode> qualifiers, ParserRuleContext recoveryCtx,
                                                boolean isObjectTypeDesc) {
        parseObjectMemberQualifiers(qualifiers);
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                if (metadata != null || qualifiers.size() > 0) {
                    return createMissingSimpleObjectField(metadata, qualifiers, isObjectTypeDesc);
                }
                return null;
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                STNode visibilityQualifier = consume();
                if (isObjectTypeDesc && visibilityQualifier.kind == SyntaxKind.PRIVATE_KEYWORD) {
                    addInvalidNodeToNextToken(visibilityQualifier,
                            DiagnosticErrorCode.ERROR_PRIVATE_QUALIFIER_IN_OBJECT_MEMBER_DESCRIPTOR);
                    visibilityQualifier = STNodeFactory.createEmptyNode();
                }
                return parseObjectMethodOrField(metadata, visibilityQualifier, isObjectTypeDesc);
            case FUNCTION_KEYWORD:
                visibilityQualifier = STNodeFactory.createEmptyNode();
                return parseObjectMethodOrFuncTypeDesc(metadata, visibilityQualifier, qualifiers, isObjectTypeDesc);
            case ASTERISK_TOKEN:
                reportInvalidMetaData(metadata, "object type inclusion");
                reportInvalidQualifierList(qualifiers);
                STNode asterisk = consume();
                STNode type = parseTypeReferenceInTypeInclusion();
                STNode semicolonToken = parseSemicolon();
                return STNodeFactory.createTypeReferenceNode(asterisk, type, semicolonToken);
            case IDENTIFIER_TOKEN:
                if (isObjectFieldStart() || nextToken.isMissing()) {
                    return parseObjectField(metadata, STNodeFactory.createEmptyNode(), qualifiers, isObjectTypeDesc);
                }

                if (isObjectMethodStart(getNextNextToken())) {
                    // Invalidate identifier before object method or object field start. This is a special case when
                    // typing a qualifier before an object method or object field start start. ErrorHandler is not able
                    // to correctly remove the identifier with the `LOOKAHEAD_LIMIT = 4`. Hence, special case this.
                    addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
                    return parseObjectMemberWithoutMeta(metadata, qualifiers, recoveryCtx, isObjectTypeDesc);
                }
                // Else fall through
            default:
                if (isTypeStartingToken(nextToken.kind) && nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseObjectField(metadata, STNodeFactory.createEmptyNode(), qualifiers, isObjectTypeDesc);
                }

                Solution solution = recover(peek(), recoveryCtx);

                if (solution.action == Action.KEEP) {
                    return parseObjectField(metadata, STNodeFactory.createEmptyNode(), qualifiers, isObjectTypeDesc);
                }
                
                return parseObjectMemberWithoutMeta(metadata, qualifiers, recoveryCtx, isObjectTypeDesc);
        }
    }

    /**
     * Check whether the cursor is at the start of a object field.
     *
     * @return <code>true</code> if the cursor is at the start of a object field.
     * <code>false</code> otherwise.
     */
    private boolean isObjectFieldStart() {
        STToken nextNextToken = getNextNextToken();
        switch (nextNextToken.kind) {
            case ERROR_KEYWORD: // error-binding-pattern not allowed in fields
            case OPEN_BRACE_TOKEN: // mapping-binding-pattern not allowed in fields
                return false;
            case CLOSE_BRACE_TOKEN:
                return true;
            default:
                return isModuleVarDeclStart(1);
        }
    }

    private boolean isObjectMethodStart(STToken token) {
        switch (token.kind) {
            case FUNCTION_KEYWORD:
            case REMOTE_KEYWORD:
            case RESOURCE_KEYWORD:
            case ISOLATED_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Parse an object member, given the visibility modifier. Object member can have
     * only one visibility qualifier. This mean the methodQualifiers list can have
     * one qualifier at-most.
     *
     * @param metadata             Metadata
     * @param visibilityQualifier Visibility qualifier
     * @param isObjectTypeDesc Whether object type or not
     * @return Parse object member node
     */
    private STNode parseObjectMethodOrField(STNode metadata, STNode visibilityQualifier, boolean isObjectTypeDesc) {
        List<STNode> objectMemberQualifiers = new ArrayList<>();
        return parseObjectMethodOrField(metadata, visibilityQualifier, objectMemberQualifiers, isObjectTypeDesc);
    }

    private STNode parseObjectMethodOrField(STNode metadata, STNode visibilityQualifier, List<STNode> qualifiers,
                                            boolean isObjectTypeDesc) {
        parseObjectMemberQualifiers(qualifiers);
        STToken nextToken = peek(1);
        STToken nextNextToken = peek(2);
        switch (nextToken.kind) {
            case FUNCTION_KEYWORD:
                return parseObjectMethodOrFuncTypeDesc(metadata, visibilityQualifier, qualifiers, isObjectTypeDesc);

            // All 'type starting tokens' here. should be same as 'parseTypeDescriptor(...)'
            case IDENTIFIER_TOKEN:
                if (nextNextToken.kind != SyntaxKind.OPEN_PAREN_TOKEN) {
                    // Here we try to catch the common user error of missing the function keyword.
                    // In such cases, lookahead for the open-parenthesis and figure out whether
                    // this is an object-method with missing name. If yes, then try to recover.
                    return parseObjectField(metadata, visibilityQualifier, qualifiers, isObjectTypeDesc);
                }
                break;
            default:
               if (isTypeStartingToken(nextToken.kind)) {
                    return parseObjectField(metadata, visibilityQualifier, qualifiers, isObjectTypeDesc);
                }
                break;
        }

        recover(peek(), ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY);
        return parseObjectMethodOrField(metadata, visibilityQualifier, qualifiers, isObjectTypeDesc);
    }

    /**
     * <p>
     * Parse object field or object field descriptor.
     * </p>
     * <code>
     * object-field-descriptor := metadata [public] type-descriptor field-name ;
     * <br/>
     * object-field := metadata [object-visibility-qual] [final] type-descriptor field-name [= field-initializer] ;
     * <br/>
     * object-visibility-qual := public|private
     * <br/>
     * field-initializer := expression
     * </code>
     *
     * @param metadata            Preceding metadata
     * @param visibilityQualifier Preceding visibility qualifier
     * @param qualifiers          Preceding qualifiers
     * @param isObjectTypeDesc    Whether object type or not
     * @return Parsed node
     */
    private STNode parseObjectField(STNode metadata, STNode visibilityQualifier, List<STNode> qualifiers,
                                    boolean isObjectTypeDesc) {
        List<STNode> objectFieldQualifiers = extractObjectFieldQualifiers(qualifiers, isObjectTypeDesc);
        STNode objectFieldQualNodeList = STNodeFactory.createNodeList(objectFieldQualifiers);
        STNode type = parseTypeDescriptor(qualifiers, ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode fieldName = parseVariableName();
        return parseObjectFieldRhs(metadata, visibilityQualifier, objectFieldQualNodeList, type, fieldName,
                isObjectTypeDesc);
    }

    private List<STNode> extractObjectFieldQualifiers(List<STNode> qualifiers, boolean isObjectTypeDesc) {
        // Check if the first qualifier is final and extract it to a separate list and return.
        List<STNode> objectFieldQualifiers = new ArrayList<>();
        if (!qualifiers.isEmpty() && !isObjectTypeDesc) {
            STNode firstQualifier = qualifiers.get(0);
            if (firstQualifier.kind == SyntaxKind.FINAL_KEYWORD) {
                objectFieldQualifiers.add(qualifiers.remove(0));
            }
        }

        return objectFieldQualifiers;
    }

    /**
     * Parse object field rhs, and complete the object field parsing. Returns the parsed object field.
     *
     * @param metadata            Metadata
     * @param visibilityQualifier Visibility qualifier
     * @param qualifiers          Object field qualifiers
     * @param type                Type descriptor
     * @param fieldName           Field name
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed object field
     */
    private STNode parseObjectFieldRhs(STNode metadata, STNode visibilityQualifier, STNode qualifiers,
                                       STNode type, STNode fieldName, boolean isObjectTypeDesc) {
        STToken nextToken = peek();
        STNode equalsToken;
        STNode expression;
        STNode semicolonToken;
        switch (nextToken.kind) {
            case SEMICOLON_TOKEN:
                equalsToken = STNodeFactory.createEmptyNode();
                expression = STNodeFactory.createEmptyNode();
                semicolonToken = parseSemicolon();
                break;
            case EQUAL_TOKEN:
                equalsToken = parseAssignOp();
                expression = parseExpression();
                semicolonToken = parseSemicolon();
                if (isObjectTypeDesc) {
                    fieldName = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(fieldName, equalsToken,
                            DiagnosticErrorCode.ERROR_FIELD_INITIALIZATION_NOT_ALLOWED_IN_OBJECT_TYPE);
                    fieldName = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(fieldName, expression);
                    equalsToken = STNodeFactory.createEmptyNode();
                    expression = STNodeFactory.createEmptyNode();
                }
                break;
            default:
                recover(peek(), ParserRuleContext.OBJECT_FIELD_RHS);
                return parseObjectFieldRhs(metadata, visibilityQualifier, qualifiers, type, fieldName,
                        isObjectTypeDesc);
        }

        return STNodeFactory.createObjectFieldNode(metadata, visibilityQualifier, qualifiers, type, fieldName,
                equalsToken, expression, semicolonToken);
    }

    /**
     * Parse method definition or declaration.
     *
     * @param metadata Preceding metadata
     * @param visibilityQualifier Preceding visibility qualifier
     * @param qualifiers Preceding visibility qualifier
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed node
     */
    private STNode parseObjectMethodOrFuncTypeDesc(STNode metadata, STNode visibilityQualifier, List<STNode> qualifiers,
                                                   boolean isObjectTypeDesc) {
        return parseFuncDefOrFuncTypeDesc(metadata, visibilityQualifier, qualifiers, true, isObjectTypeDesc);
    }

    /**
     * Parse relative resource path.
     * <p>
     * <code>relative-resource-path := "." | (resource-path-segment ("/" resource-path-segment)*)</code>
     *
     * @return Parsed node
     */
    private STNode parseRelativeResourcePath() {
        startContext(ParserRuleContext.RELATIVE_RESOURCE_PATH);
        List<STNode> pathElementList = new ArrayList<>();

        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.DOT_TOKEN) {
            pathElementList.add(consume());
            endContext();
            return STNodeFactory.createNodeList(pathElementList);
        }

        // Parse first resource path segment, that has no leading slash
        STNode pathSegment = parseResourcePathSegment(true);
        pathElementList.add(pathSegment);

        STNode leadingSlash;
        while (!isEndRelativeResourcePath(nextToken.kind)) {
            leadingSlash = parseRelativeResourcePathEnd();
            if (leadingSlash == null) {
                break;
            }

            pathElementList.add(leadingSlash);
            pathSegment = parseResourcePathSegment(false);
            pathElementList.add(pathSegment);
            nextToken = peek();
        }

        endContext();
        return createResourcePathNodeList(pathElementList);
    }

    private boolean isEndRelativeResourcePath(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case OPEN_PAREN_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode createResourcePathNodeList(List<STNode> pathElementList) {
        if (pathElementList.isEmpty()) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Validate resource path elements and create a STNodeList
        List<STNode> validatedList = new ArrayList<>();
        STNode firstElement = pathElementList.get(0);
        validatedList.add(firstElement);
        boolean hasRestPram = firstElement.kind == SyntaxKind.RESOURCE_PATH_REST_PARAM;

        for (int i = 1; i < pathElementList.size(); i = i + 2) {
            STNode leadingSlash = pathElementList.get(i);
            STNode pathSegment = pathElementList.get(i + 1);

            if (hasRestPram) {
                updateLastNodeInListWithInvalidNode(validatedList, leadingSlash, null);
                updateLastNodeInListWithInvalidNode(validatedList, pathSegment, 
                        DiagnosticErrorCode.ERROR_RESOURCE_PATH_SEGMENT_NOT_ALLOWED_AFTER_REST_PARAM);
                continue;
            }

            hasRestPram = pathSegment.kind == SyntaxKind.RESOURCE_PATH_REST_PARAM;
            validatedList.add(leadingSlash);
            validatedList.add(pathSegment);
        }

        return STNodeFactory.createNodeList(validatedList);
    }

    /**
     * Parse resource path segment.
     * <p>
     * <code>resource-path-segment := identifier | resource-path-parameter</code>
     *
     * @param isFirstSegment Whether we are parsing the first segment
     * @return Parsed node
     */
    private STNode parseResourcePathSegment(boolean isFirstSegment) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                if (isFirstSegment && nextToken.isMissing() && isInvalidNodeStackEmpty() &&
                        getNextNextToken().kind == SyntaxKind.SLASH_TOKEN) {
                    // special case `[MISSING]/` to improve the error message for `/hello`
                    removeInsertedToken(); // to ignore current missing identifier diagnostic
                    return SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN, 
                            DiagnosticErrorCode.ERROR_RESOURCE_PATH_CANNOT_BEGIN_WITH_SLASH);
                }
                return consume();
            case OPEN_BRACKET_TOKEN:
                return parseResourcePathParameter();
            default:
                recover(nextToken, ParserRuleContext.RESOURCE_PATH_SEGMENT);
                return parseResourcePathSegment(isFirstSegment);
        }
    }

    /**
     * Parse resource path parameter.
     * <p>
     * <code>resource-path-parameter := "[" [annots] type-descriptor [...] [param-name] "]"</code>
     *
     * @return Parsed node
     */
    private STNode parseResourcePathParameter() {
        STNode openBracket = parseOpenBracket();
        STNode annots = parseOptionalAnnotations();
        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_PATH_PARAM);
        STNode ellipsis = parseOptionalEllipsis();
        STNode paramName = parseOptionalPathParamName();
        STNode closeBracket = parseCloseBracket();

        SyntaxKind pathPramKind =
                ellipsis == null ? SyntaxKind.RESOURCE_PATH_SEGMENT_PARAM : SyntaxKind.RESOURCE_PATH_REST_PARAM;
        return STNodeFactory.createResourcePathParameterNode(pathPramKind, openBracket, annots, type, ellipsis,
                paramName, closeBracket);
    }

    private STNode parseOptionalPathParamName() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                return consume();
            case CLOSE_BRACKET_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                recover(nextToken, ParserRuleContext.OPTIONAL_PATH_PARAM_NAME);
                return parseOptionalPathParamName();
        }
    }
    
    private STNode parseOptionalEllipsis() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ELLIPSIS_TOKEN:
                return consume();
            case IDENTIFIER_TOKEN:
            case CLOSE_BRACKET_TOKEN:    
                return STNodeFactory.createEmptyNode();
            default:
                recover(nextToken, ParserRuleContext.PATH_PARAM_ELLIPSIS);
                return parseOptionalEllipsis();
        }
    }

    /**
     * Parse relative resource path end.
     *
     * @return Parsed node
     */
    private STNode parseRelativeResourcePathEnd() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
            case EOF_TOKEN:
                // null represents the end of resource path.
                return null;
            case SLASH_TOKEN:
                return consume();
            default:
                recover(nextToken, ParserRuleContext.RELATIVE_RESOURCE_PATH_END);
                return parseRelativeResourcePathEnd();
        }
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
            recover(token, ParserRuleContext.IF_KEYWORD);
            return parseIfKeyword();
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
            recover(token, ParserRuleContext.ELSE_KEYWORD);
            return parseElseKeyword();
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
        switch (nextToken.kind) {
            case IF_KEYWORD:
                return parseIfElseBlock();
            case OPEN_BRACE_TOKEN:
                return parseBlockNode();
            default:
                recover(peek(), ParserRuleContext.ELSE_BODY);
                return parseElseBody();
        }
    }

    /**
     * Parse do statement.
     * <code>do-stmt := do block-stmt [on-fail-clause]</code>
     *
     * @return Do statement
     */
    private STNode parseDoStatement() {
        startContext(ParserRuleContext.DO_BLOCK);
        STNode doKeyword = parseDoKeyword();
        STNode doBody = parseBlockNode();
        endContext();
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createDoStatementNode(doKeyword, doBody, onFailClause);
    }

    /**
     * Parse while statement.
     * <code>while-stmt := while expression block-stmt [on-fail-clause]</code>
     *
     * @return While statement
     */
    private STNode parseWhileStatement() {
        startContext(ParserRuleContext.WHILE_BLOCK);
        STNode whileKeyword = parseWhileKeyword();
        STNode condition = parseExpression();
        STNode whileBody = parseBlockNode();
        endContext();
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createWhileStatementNode(whileKeyword, condition, whileBody, onFailClause);
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
            recover(token, ParserRuleContext.WHILE_KEYWORD);
            return parseWhileKeyword();
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
            recover(token, ParserRuleContext.PANIC_KEYWORD);
            return parsePanicKeyword();
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
     * @param isRhsExpr    Is rhs expression
     * @return Check expression node
     */
    private STNode parseCheckExpression(boolean isRhsExpr, boolean allowActions, boolean isInConditionalExpr) {
        STNode checkingKeyword = parseCheckingKeyword();
        STNode expr =
                parseExpression(OperatorPrecedence.EXPRESSION_ACTION, isRhsExpr, allowActions, isInConditionalExpr);
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
            recover(token, ParserRuleContext.CHECKING_KEYWORD);
            return parseCheckingKeyword();
        }
    }

    /**
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
            recover(token, ParserRuleContext.CONTINUE_KEYWORD);
            return parseContinueKeyword();
        }
    }

    /**
     * Parse fail statement.
     * <code>fail-stmt := fail expression ;</code>
     *
     * @return Fail statement
     */
    private STNode parseFailStatement() {
        startContext(ParserRuleContext.FAIL_STATEMENT);
        STNode failKeyword = parseFailKeyword();
        STNode expr = parseExpression();
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createFailStatementNode(failKeyword, expr, semicolon);
    }

    /**
     * Parse fail keyword.
     * <p>
     * <code>
     * fail-keyword := fail
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseFailKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FAIL_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.FAIL_KEYWORD);
            return parseFailKeyword();
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
            recover(token, ParserRuleContext.RETURN_KEYWORD);
            return parseReturnKeyword();
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
            recover(token, ParserRuleContext.BREAK_KEYWORD);
            return parseBreakKeyword();
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
        if (field != null) {
            fields.add(field);
        }

        return parseMappingConstructorFields(fields);
    }

    private STNode parseMappingConstructorFields(List<STNode> fields) {
        STToken nextToken;
        // Parse the remaining field mappings
        STNode mappingFieldEnd;
        nextToken = peek();
        while (!isEndOfMappingConstructor(nextToken.kind)) {
            mappingFieldEnd = parseMappingFieldEnd();
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
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.MAPPING_FIELD_END);
                return parseMappingFieldEnd();
        }
    }

    private boolean isEndOfMappingConstructor(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
            case READONLY_KEYWORD:
                return false;
            case EOF_TOKEN:
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
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
     * @return Parsed node
     */
    private STNode parseMappingField(ParserRuleContext fieldContext) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                STNode readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseSpecificFieldWithOptionalValue(readonlyKeyword);
            case STRING_LITERAL_TOKEN:
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseQualifiedSpecificField(readonlyKeyword);
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
                recover(nextToken, fieldContext);
                return parseMappingField(fieldContext);
        }
    }

    private STNode parseSpecificField(STNode readonlyKeyword) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case STRING_LITERAL_TOKEN:
                return parseQualifiedSpecificField(readonlyKeyword);
            case IDENTIFIER_TOKEN:
                return parseSpecificFieldWithOptionalValue(readonlyKeyword);
            default:
                recover(peek(), ParserRuleContext.SPECIFIC_FIELD);
                return parseSpecificField(readonlyKeyword);
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
        STNode colon;
        STNode valueExpr;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COLON_TOKEN:
                colon = parseColon();
                valueExpr = parseExpression();
                break;
            case COMMA_TOKEN:
                colon = STNodeFactory.createEmptyNode();
                valueExpr = STNodeFactory.createEmptyNode();
                break;
            default:
                if (isEndOfMappingConstructor(nextToken.kind)) {
                    colon = STNodeFactory.createEmptyNode();
                    valueExpr = STNodeFactory.createEmptyNode();
                    break;
                }

                recover(nextToken, ParserRuleContext.SPECIFIC_FIELD_RHS);
                return parseSpecificFieldRhs(readonlyKeyword, key);

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
        STNode stringLiteral;
        if (token.kind == SyntaxKind.STRING_LITERAL_TOKEN) {
            stringLiteral = consume();
        } else {
            recover(token, ParserRuleContext.STRING_LITERAL_TOKEN);
            return parseStringLiteral();
        }
        return parseBasicLiteral(stringLiteral);
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
            recover(token, ParserRuleContext.COLON);
            return parseColon();
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
            recover(token, ParserRuleContext.READONLY_KEYWORD);
            return parseReadonlyKeyword();
        }
    }

    /**
     * Parse computed-name-field of a mapping constructor expression.
     * <p>
     * <code>computed-name-field := [ field-name-expr ] : value-expr</code>
     *
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
            recover(token, ParserRuleContext.OPEN_BRACKET);
            return parseOpenBracket();
        }
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
        STNode binaryOperator = parseCompoundBinaryOperator();
        STNode equalsToken = parseAssignOp();
        STNode expr = parseActionOrExpression();
        STNode semicolon = parseSemicolon();
        endContext();

        boolean lvExprValid = isValidLVExpr(lvExpr);
        if (!lvExprValid) {
            // Create a missing simple variable reference and attach the invalid lvExpr as minutiae
            STNode identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            STNode simpleNameRef = STNodeFactory.createSimpleNameReferenceNode(identifier);
            lvExpr = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(simpleNameRef, lvExpr,
                    DiagnosticErrorCode.ERROR_INVALID_EXPR_IN_COMPOUND_ASSIGNMENT_LHS);
        }

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
        if (isCompoundAssignment(token.kind)) {
            return consume();
        } else {
            recover(token, ParserRuleContext.COMPOUND_BINARY_OPERATOR);
            return parseCompoundBinaryOperator();
        }
    }

    /**
     * Parse service declaration or variable declaration with service object type descriptor.
     *
     * @param metadata       Metadata
     * @param qualifiers     Preceding top level qualifiers
     * @return Parsed node
     */
    private STNode parseServiceDeclOrVarDecl(STNode metadata, STNode publicQualifier, List<STNode> qualifiers) {
        startContext(ParserRuleContext.SERVICE_DECL);
        List<STNode> serviceDeclQualList = extractServiceDeclQualifiers(qualifiers);
        STNode serviceKeyword = extractServiceKeyword(qualifiers);
        STNode typeDesc = parseServiceDeclTypeDescriptor(qualifiers);

        if (typeDesc != null && typeDesc.kind == SyntaxKind.OBJECT_TYPE_DESC) {
            return parseServiceDeclOrVarDecl(metadata, publicQualifier, serviceDeclQualList, serviceKeyword,
                    typeDesc);
        } else {
            return parseServiceDecl(metadata, publicQualifier, serviceDeclQualList, serviceKeyword, typeDesc);
        }
    }

    private STNode parseServiceDeclOrVarDecl(STNode metadata, STNode publicQualifier, List<STNode> serviceDeclQualList,
                                             STNode serviceKeyword, STNode typeDesc) {
        // Reaching here means service keyword is followed by an object type desc.
        // It could either be service declaration or variable declaration with service object type.
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case SLASH_TOKEN:
            case ON_KEYWORD:
                return parseServiceDecl(metadata, publicQualifier, serviceDeclQualList, serviceKeyword, typeDesc);
            case OPEN_BRACKET_TOKEN: // List binding pattern
            case IDENTIFIER_TOKEN: // Binding pattern starts with identifier
            case OPEN_BRACE_TOKEN: // Mapping binding pattern
            case ERROR_KEYWORD: // Error binding pattern
                endContext(); // End `SERVICE_DECL` context
                typeDesc = modifyObjectTypeDescWithALeadingQualifier(typeDesc, serviceKeyword);
                if (!serviceDeclQualList.isEmpty()) {
                    // Currently, service-decl-quals := isolated-qual only.
                    STNode isolatedQualifier = serviceDeclQualList.get(0);
                    typeDesc = modifyObjectTypeDescWithALeadingQualifier(typeDesc, isolatedQualifier);
                }
                return parseVarDeclTypeDescRhs(typeDesc, metadata, publicQualifier, new ArrayList<>(), true, true);
            default:
                recover(nextToken, ParserRuleContext.SERVICE_DECL_OR_VAR_DECL);
                return parseServiceDeclOrVarDecl(metadata, publicQualifier, serviceDeclQualList, serviceKeyword,
                        typeDesc);
        }
    }

    private List<STNode> extractServiceDeclQualifiers(List<STNode> qualifierList) {
        // We only reach here for a qualifierList containing service keyword/s.
        // Validate qualifierList until first service keyword is reached.
        List<STNode> validatedList = new ArrayList<>();

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (qualifier.kind == SyntaxKind.SERVICE_KEYWORD) {
                qualifierList.subList(0 , i).clear();  // clear inserted list till service keyword
                break;
            }

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            if (qualifier.kind == SyntaxKind.ISOLATED_KEYWORD) {
                validatedList.add(qualifier);
                continue;
            }

            // We only reach here for invalid qualifiers
            if (qualifierList.size() == nextIndex) {
                addInvalidNodeToNextToken(qualifier, DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED,
                        ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        return validatedList;
    }

    private STNode extractServiceKeyword(List<STNode> qualifierList) {
        assert !qualifierList.isEmpty();
        STNode serviceKeyword = qualifierList.remove(0);
        assert serviceKeyword.kind == SyntaxKind.SERVICE_KEYWORD;
        return serviceKeyword;
    }

    /**
     * Parse service declaration.
     * <p>
     * <code>
     * service-decl := metadata `service` [type-descriptor] [absolute-resource-path | string-literal]
     * `on` expression-list object-constructor-block [;]
     * <br/>
     * absolute-resource-path := "/" | ("/" identifier)+
     * <br/>
     * expression-list := expression (, expression)*
     * <br/>
     * object-constructor-block := { object-member* }
     * </code>
     *
     * @param metadata        Metadata
     * @param publicQualifier Public qualifier to invalidate if present
     * @param qualList        Qualifiers that precede service keyword
     * @param serviceKeyword  Service keyword
     * @param serviceType     Type descriptor
     * @return Parsed node
     */
    private STNode parseServiceDecl(STNode metadata, STNode publicQualifier, List<STNode> qualList,
                                    STNode serviceKeyword, STNode serviceType) {
        // Invalidate public qualifier if present
        if (publicQualifier != null) {
            if (qualList.size() > 0) {
                updateFirstNodeInListWithLeadingInvalidNode(qualList, publicQualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED);
            } else {
                serviceKeyword = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(serviceKeyword, publicQualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED);
            }
        }

        STNode qualNodeList = STNodeFactory.createNodeList(qualList);

        STNode resourcePath = parseOptionalAbsolutePathOrStringLiteral();
        STNode onKeyword = parseOnKeyword();
        STNode expressionList = parseListeners();
        STNode openBrace = parseOpenBrace();
        STNode objectMembers = parseObjectMembers(ParserRuleContext.OBJECT_CONSTRUCTOR_MEMBER);
        STNode closeBrace = parseCloseBrace();
        STNode semicolon = parseOptionalSemicolon();

        onKeyword =
                cloneWithDiagnosticIfListEmpty(expressionList, onKeyword, DiagnosticErrorCode.ERROR_MISSING_EXPRESSION);
        endContext();
        return STNodeFactory.createServiceDeclarationNode(metadata, qualNodeList, serviceKeyword, serviceType,
                resourcePath, onKeyword, expressionList, openBrace, objectMembers, closeBrace, semicolon);
    }

    /**
     * Parse service declaration type descriptor.
     * <p>
     * <code>service-decl-type-descriptor := [type-descriptor]</code>
     *
     * @param qualifiers Preceding type descriptor qualifiers
     * @return Parsed node
     */
    private STNode parseServiceDeclTypeDescriptor(List<STNode> qualifiers) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case SLASH_TOKEN:
            case ON_KEYWORD:
            case STRING_LITERAL_TOKEN: // should belong to the resource path
                reportInvalidQualifierList(qualifiers);
                return STNodeFactory.createEmptyNode();
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    return parseTypeDescriptor(qualifiers, ParserRuleContext.TYPE_DESC_IN_SERVICE);
                }
                recover(nextToken, ParserRuleContext.OPTIONAL_SERVICE_DECL_TYPE);
                return parseServiceDeclTypeDescriptor(qualifiers);
        }
    }

    /**
     * Parse optional absolute resource path or string literal.
     *
     * @return Parsed node
     */
    private STNode parseOptionalAbsolutePathOrStringLiteral() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case SLASH_TOKEN:
                return parseAbsoluteResourcePath();
            case STRING_LITERAL_TOKEN:
                STToken stringLiteralToken = consume();
                STNode stringLiteralNode = parseBasicLiteral(stringLiteralToken);
                return STNodeFactory.createNodeList(Collections.singletonList(stringLiteralNode));
            case ON_KEYWORD:
                return STNodeFactory.createEmptyNodeList();
            default:
                recover(nextToken, ParserRuleContext.OPTIONAL_ABSOLUTE_PATH);
                return parseOptionalAbsolutePathOrStringLiteral();
        }
    }

    /**
     * <p>
     * Parse absolute resource path.
     * </p>
     * <code>
     * absolute-resource-path := "/" | ("/" identifier)+
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseAbsoluteResourcePath() {
        startContext(ParserRuleContext.ABSOLUTE_RESOURCE_PATH);
        List<STNode> identifierList = new ArrayList<>();

        STToken nextToken = peek();
        STNode leadingSlash;
        boolean isInitialSlash = true;
        while (!isEndAbsoluteResourcePath(nextToken.kind)) {
            leadingSlash = parseAbsoluteResourcePathEnd(isInitialSlash);
            if (leadingSlash == null) {
                break;
            }
            identifierList.add(leadingSlash);

            nextToken = peek();
            if (isInitialSlash && nextToken.kind == SyntaxKind.ON_KEYWORD) {
                // Initial slash could not be followed by an identifier
                break;
            }
            isInitialSlash = false;

            leadingSlash = parseIdentifier(ParserRuleContext.IDENTIFIER);
            identifierList.add(leadingSlash);
            nextToken = peek();
        }

        endContext();
        return STNodeFactory.createNodeList(identifierList);
    }

    private boolean isEndAbsoluteResourcePath(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EOF_TOKEN:
            case ON_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse absolute resource path end.
     *
     * @param isInitialSlash Whether this is the initial slash
     * @return Parsed node
     */
    private STNode parseAbsoluteResourcePathEnd(boolean isInitialSlash) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ON_KEYWORD:
            case EOF_TOKEN:
                // null represents the end of resource path.
                return null;
            case SLASH_TOKEN:
                return consume();
            default:
                ParserRuleContext context;
                if (isInitialSlash) {
                    context = ParserRuleContext.OPTIONAL_ABSOLUTE_PATH;
                } else {
                    context = ParserRuleContext.ABSOLUTE_RESOURCE_PATH_END;
                }
                recover(nextToken, context);
                return parseAbsoluteResourcePathEnd(isInitialSlash);
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
            recover(token, ParserRuleContext.SERVICE_KEYWORD);
            return parseServiceKeyword();
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
    static boolean isCompoundBinaryOperator(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case SLASH_TOKEN:
            case ASTERISK_TOKEN:
            case BITWISE_AND_TOKEN:
            case BITWISE_XOR_TOKEN:
            case PIPE_TOKEN:
            case DOUBLE_LT_TOKEN:
            case DOUBLE_GT_TOKEN:
            case TRIPPLE_GT_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isCompoundAssignment(SyntaxKind tokenKind) {
        return isCompoundBinaryOperator(tokenKind) && getNextNextToken().kind == SyntaxKind.EQUAL_TOKEN;
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
            recover(token, ParserRuleContext.ON_KEYWORD);
            return parseOnKeyword();
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
        startContext(ParserRuleContext.LISTENERS_LIST);
        List<STNode> listeners = new ArrayList<>();

        STToken nextToken = peek();
        if (isEndOfListeners(nextToken.kind)) {
            endContext();
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first expression, that has no leading comma
        STNode expr = parseExpression();
        listeners.add(expr);

        // Parse the remaining expressions
        STNode listenersMemberEnd;
        while (!isEndOfListeners(peek().kind)) {
            listenersMemberEnd = parseListenersMemberEnd();
            if (listenersMemberEnd == null) {
                break;
            }
            listeners.add(listenersMemberEnd);
            expr = parseExpression();
            listeners.add(expr);
        }

        endContext();
        return STNodeFactory.createNodeList(listeners);
    }

    private boolean isEndOfListeners(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case OPEN_BRACE_TOKEN:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseListenersMemberEnd() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COMMA_TOKEN:
                return parseComma();
            case OPEN_BRACE_TOKEN:
                return null;
            default:
                recover(nextToken, ParserRuleContext.LISTENERS_LIST_END);
                return parseListenersMemberEnd();
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
     * <code>false</code> otherwise
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
     * <p>
     * <code>
     * listener-decl := metadata [public] listener [type-descriptor] variable-name = expression ;
     * </code>
     *
     * @param metadata  Metadata
     * @param qualifier Qualifier that precedes the listener declaration
     * @return Parsed node
     */
    private STNode parseListenerDeclaration(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.LISTENER_DECL);
        STNode listenerKeyword = parseListenerKeyword();

        if (peek().kind == SyntaxKind.IDENTIFIER_TOKEN) {
            STNode listenerDecl =
                    parseConstantOrListenerDeclWithOptionalType(metadata, qualifier, listenerKeyword, true);
            endContext();
            return listenerDecl;
        }

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
            recover(token, ParserRuleContext.LISTENER_KEYWORD);
            return parseListenerKeyword();
        }
    }

    /**
     * Parse constant declaration, given the qualifier.
     * <p>
     * <code>module-const-decl := metadata [public] const [type-descriptor] identifier = const-expr ;</code>
     *
     * @param metadata  Metadata
     * @param qualifier Qualifier that precedes the listener declaration
     * @return Parsed node
     */
    private STNode parseConstantDeclaration(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.CONSTANT_DECL);
        STNode constKeyword = parseConstantKeyword();
        // context ends inside the method
        return parseConstDecl(metadata, qualifier, constKeyword);
    }

    /**
     * Parse the components that follows after the const keyword of a constant declaration.
     *
     * @param metadata     Metadata
     * @param qualifier    Qualifier that precedes the constant decl
     * @param constKeyword Const keyword
     * @return Parsed node
     */
    private STNode parseConstDecl(STNode metadata, STNode qualifier, STNode constKeyword) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ANNOTATION_KEYWORD:
                endContext();
                return parseAnnotationDeclaration(metadata, qualifier, constKeyword);
            case IDENTIFIER_TOKEN:
                STNode constantDecl =
                        parseConstantOrListenerDeclWithOptionalType(metadata, qualifier, constKeyword, false);
                endContext();
                return constantDecl;
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    break;
                }

                recover(peek(), ParserRuleContext.CONST_DECL_TYPE);
                return parseConstDecl(metadata, qualifier, constKeyword);
        }

        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode variableName = parseVariableName();
        STNode equalsToken = parseAssignOp();
        STNode initializer = parseExpression();
        STNode semicolonToken = parseSemicolon();
        endContext();
        return STNodeFactory.createConstantDeclarationNode(metadata, qualifier, constKeyword, typeDesc, variableName,
                equalsToken, initializer, semicolonToken);
    }

    private STNode parseConstantOrListenerDeclWithOptionalType(STNode metadata, STNode qualifier, STNode constKeyword,
                                                               boolean isListener) {
        STNode varNameOrTypeName = parseStatementStartIdentifier();
        return parseConstantOrListenerDeclRhs(metadata, qualifier, constKeyword, varNameOrTypeName, isListener);
    }

    /**
     * Parse the component that follows the first identifier in a const decl. The identifier
     * can be either the type-name (a user defined type) or the var-name there the type-name
     * is not present.
     *
     * @param qualifier     Qualifier that precedes the constant decl
     * @param keyword       Keyword
     * @param typeOrVarName Identifier that follows the const-keywoord
     * @return Parsed node
     */
    private STNode parseConstantOrListenerDeclRhs(STNode metadata, STNode qualifier, STNode keyword,
                                                  STNode typeOrVarName, boolean isListener) {
        if (typeOrVarName.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            STNode type = typeOrVarName;
            STNode variableName = parseVariableName();
            return parseListenerOrConstRhs(metadata, qualifier, keyword, isListener, type, variableName);
        }

        STNode type;
        STNode variableName;
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                type = typeOrVarName;
                variableName = parseVariableName();
                break;
            case EQUAL_TOKEN:
                variableName = ((STSimpleNameReferenceNode) typeOrVarName).name; // variableName is a token
                type = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.CONST_DECL_RHS);
                return parseConstantOrListenerDeclRhs(metadata, qualifier, keyword, typeOrVarName, isListener);
        }

        return parseListenerOrConstRhs(metadata, qualifier, keyword, isListener, type, variableName);
    }

    private STNode parseListenerOrConstRhs(STNode metadata, STNode qualifier, STNode keyword, boolean isListener,
                                           STNode type, STNode variableName) {
        STNode equalsToken = parseAssignOp();
        STNode initializer = parseExpression();
        STNode semicolonToken = parseSemicolon();

        if (isListener) {
            return STNodeFactory.createListenerDeclarationNode(metadata, qualifier, keyword, type, variableName,
                    equalsToken, initializer, semicolonToken);
        }

        return STNodeFactory.createConstantDeclarationNode(metadata, qualifier, keyword, type, variableName,
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
            recover(token, ParserRuleContext.CONST_KEYWORD);
            return parseConstantKeyword();
        }
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
    private STNode parseTypeofExpression(boolean isRhsExpr, boolean isInConditionalExpr) {
        STNode typeofKeyword = parseTypeofKeyword();

        // allow-actions flag is always false, since there will not be any actions
        // within the typeof-expression, due to the precedence.
        STNode expr = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, false, isInConditionalExpr);
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
            recover(token, ParserRuleContext.TYPEOF_KEYWORD);
            return parseTypeofKeyword();
        }
    }

    /**
     * Parse optional type descriptor given the type.
     * <p>
     * <code>optional-type-descriptor := type-descriptor `?`</code>
     * </p>
     *
     * @param typeDescriptorNode Preceding type descriptor
     * @return Parsed node
     */
    private STNode parseOptionalTypeDescriptor(STNode typeDescriptorNode) {
        startContext(ParserRuleContext.OPTIONAL_TYPE_DESCRIPTOR);
        STNode questionMarkToken = parseQuestionMark();
        endContext();
        return createOptionalTypeDesc(typeDescriptorNode, questionMarkToken);
    }

    private STNode createOptionalTypeDesc(STNode typeDescNode, STNode questionMarkToken) {
        if (typeDescNode.kind == SyntaxKind.UNION_TYPE_DESC) {
            STUnionTypeDescriptorNode unionTypeDesc = (STUnionTypeDescriptorNode) typeDescNode;
            STNode middleTypeDesc = createOptionalTypeDesc(unionTypeDesc.rightTypeDesc, questionMarkToken);
            typeDescNode = mergeTypesWithUnion(unionTypeDesc.leftTypeDesc, unionTypeDesc.pipeToken, middleTypeDesc);
        } else if (typeDescNode.kind == SyntaxKind.INTERSECTION_TYPE_DESC) {
            STIntersectionTypeDescriptorNode intersectionTypeDesc = (STIntersectionTypeDescriptorNode) typeDescNode;
            STNode middleTypeDesc = createOptionalTypeDesc(intersectionTypeDesc.rightTypeDesc, questionMarkToken);
            typeDescNode = mergeTypesWithIntersection(intersectionTypeDesc.leftTypeDesc,
                    intersectionTypeDesc.bitwiseAndToken, middleTypeDesc);
        } else {
            typeDescNode = validateForUsageOfVar(typeDescNode);
            typeDescNode = STNodeFactory.createOptionalTypeDescriptorNode(typeDescNode, questionMarkToken);
        }

        return typeDescNode;
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
    private STNode parseUnaryExpression(boolean isRhsExpr, boolean isInConditionalExpr) {
        STNode unaryOperator = parseUnaryOperator();

        // allow-actions flag is always false, since there will not be any actions
        // within the unary expression, due to the precedence.
        STNode expr = parseExpression(OperatorPrecedence.UNARY, isRhsExpr, false, isInConditionalExpr);
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
            recover(token, ParserRuleContext.UNARY_OPERATOR);
            return parseUnaryOperator();
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
     * int-literal
     * | constant-reference-expr
     * | inferred-array-length
     * inferred-array-length := *
     * </code>
     * </p>
     *
     * @param memberTypeDesc
     * @return Parsed Node
     */
    private STNode parseArrayTypeDescriptor(STNode memberTypeDesc) {
        startContext(ParserRuleContext.ARRAY_TYPE_DESCRIPTOR);
        STNode openBracketToken = parseOpenBracket();
        STNode arrayLengthNode = parseArrayLength();
        STNode closeBracketToken = parseCloseBracket();
        endContext();
        return createArrayTypeDesc(memberTypeDesc, openBracketToken, arrayLengthNode, closeBracketToken);
    }

    private STNode createArrayTypeDesc(STNode memberTypeDesc, STNode openBracketToken, STNode arrayLengthNode,
                                       STNode closeBracketToken) {
        memberTypeDesc = validateForUsageOfVar(memberTypeDesc);
        if (arrayLengthNode != null) {
            switch (arrayLengthNode.kind) {
                case ASTERISK_LITERAL:
                case SIMPLE_NAME_REFERENCE:
                case QUALIFIED_NAME_REFERENCE:
                    break;
                case NUMERIC_LITERAL:
                    SyntaxKind numericLiteralKind = arrayLengthNode.childInBucket(0).kind;
                    if (numericLiteralKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN ||
                            numericLiteralKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
                        break;
                    }
                    // fall through
                default:
                    openBracketToken = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(openBracketToken,
                            arrayLengthNode, DiagnosticErrorCode.ERROR_INVALID_ARRAY_LENGTH);
                    arrayLengthNode = STNodeFactory.createEmptyNode();
            }
        }
        
        // If the member type desc is an array type desc flatten the dimensions
        List<STNode> arrayDimensions = new ArrayList();
        if (memberTypeDesc.kind == SyntaxKind.ARRAY_TYPE_DESC) {
            STArrayTypeDescriptorNode innerArrayType = (STArrayTypeDescriptorNode) memberTypeDesc;
            STNode innerArrayDimensions = innerArrayType.dimensions;
            int dimensionCount = innerArrayDimensions.bucketCount();
            
            for (int i = 0; i < dimensionCount; i++) {
                arrayDimensions.add(innerArrayDimensions.childInBucket(i));
            }
            memberTypeDesc = innerArrayType.memberTypeDesc;
        }
        
        STNode arrayDimension = STNodeFactory.createArrayDimensionNode(openBracketToken, arrayLengthNode, 
                closeBracketToken);
        arrayDimensions.add(arrayDimension);
        STNode arrayDimensionNodeList = STNodeFactory.createNodeList(arrayDimensions);
        
        return STNodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, arrayDimensionNodeList);
    }

    /**
     * Parse array length.
     * <p>
     * <code>
     * array-length :=
     * int-literal
     * | constant-reference-expr
     * | inferred-array-length
     * constant-reference-expr := variable-reference-expr
     * </code>
     * </p>
     *
     * @return Parsed array length
     */
    private STNode parseArrayLength() {
        STToken token = peek();
        switch (token.kind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case ASTERISK_TOKEN:
                return parseBasicLiteral();
            case CLOSE_BRACKET_TOKEN:
                return STNodeFactory.createEmptyNode();
            // Parsing variable-reference-expr is same as parsing qualified identifier
            case IDENTIFIER_TOKEN:
                return parseQualifiedIdentifier(ParserRuleContext.ARRAY_LENGTH);
            default:
                recover(token, ParserRuleContext.ARRAY_LENGTH);
                return parseArrayLength();
        }
    }

    /**
     * Parse annotations.
     * <p>
     * <i>Note: In the <a href="https://ballerina.io/spec/lang/2020R1/#annots">ballerina spec</a>
     * annotations-list is specified as one-or-more annotations. And the usage is marked as
     * optional annotations-list. However, for the consistency of the tree, here we make the
     * annotation-list as zero-or-more annotations, and the usage is not-optional.</i>
     * <p>
     * <code>annots := annotation*</code>
     *
     * @return Parsed node
     */
    private STNode parseOptionalAnnotations() {
        startContext(ParserRuleContext.ANNOTATIONS);
        List<STNode> annotList = new ArrayList<>();
        STToken nextToken = peek();
        while (nextToken.kind == SyntaxKind.AT_TOKEN) {
            annotList.add(parseAnnotation());
            nextToken = peek();
        }

        endContext();
        return STNodeFactory.createNodeList(annotList);
    }

    /**
     * Parse annotation list with at least one annotation.
     *
     * @return Annotation list
     */
    private STNode parseAnnotations() {
        startContext(ParserRuleContext.ANNOTATIONS);
        List<STNode> annotList = new ArrayList<>();
        annotList.add(parseAnnotation());
        while (peek().kind == SyntaxKind.AT_TOKEN) {
            annotList.add(parseAnnotation());
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
        if (isPredeclaredIdentifier(peek().kind)) {
            annotReference = parseQualifiedIdentifier(ParserRuleContext.ANNOT_REFERENCE);
        } else {
            annotReference = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            annotReference = STNodeFactory.createSimpleNameReferenceNode(annotReference);
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
            recover(nextToken, ParserRuleContext.AT);
            return parseAtToken();
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
    private STNode parseMetaData() {
        STNode docString;
        STNode annotations;
        switch (peek().kind) {
            case DOCUMENTATION_STRING:
                docString = parseMarkdownDocumentation();
                annotations = parseOptionalAnnotations();
                break;
            case AT_TOKEN:
                docString = STNodeFactory.createEmptyNode();
                annotations = parseOptionalAnnotations();
                break;
            default:
                return STNodeFactory.createEmptyNode();
        }

        return createMetadata(docString, annotations);
    }

    /**
     * Create metadata node.
     *
     * @return A metadata node
     */
    private STNode createMetadata(STNode docString, STNode annotations) {
        if (annotations == null && docString == null) {
            return STNodeFactory.createEmptyNode();
        } else {
            return STNodeFactory.createMetadataNode(docString, annotations);
        }
    }

    /**
     * Parse type test expression.
     * <code>
     * type-test-expr := expression (is | !is) type-descriptor
     * </code>
     *
     * @param lhsExpr Preceding expression of the is expression
     * @return Is expression node
     */
    private STNode parseTypeTestExpression(STNode lhsExpr, boolean isInConditionalExpr) {
        STNode isOrNotIsKeyword = parseIsOrNotIsKeyword();
        STNode typeDescriptor = parseTypeDescriptorInExpression(isInConditionalExpr);
        return STNodeFactory.createTypeTestExpressionNode(lhsExpr, isOrNotIsKeyword, typeDescriptor);
    }

    /**
     * Parse `is` keyword or `!is` keyword.
     *
     * @return is-keyword or not-is-keyword node
     */
    private STNode parseIsOrNotIsKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IS_KEYWORD ||
                token.kind == SyntaxKind.NOT_IS_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.IS_KEYWORD);
            return parseIsOrNotIsKeyword();
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
     * @param annots        Annotations
     * @return Statement node
     */
    private STNode parseExpressionStatement(STNode annots) {
        startContext(ParserRuleContext.EXPRESSION_STATEMENT);
        STNode expression = parseActionOrExpressionInLhs(annots);
        return getExpressionAsStatement(expression);
    }

    /**
     * Parse statements that starts with an expression.
     *
     * @return Statement node
     */
    private STNode parseStatementStartWithExpr(STNode annots) {
        startContext(ParserRuleContext.AMBIGUOUS_STMT);
        STNode expr = parseActionOrExpressionInLhs(annots);
        return parseStatementStartWithExprRhs(expr);
    }

    /**
     * Parse the component followed by the expression, at the beginning of a statement.
     *
     * @param expression Action or expression in LHS
     * @return Statement node
     */
    private STNode parseStatementStartWithExprRhs(STNode expression) {
        SyntaxKind nextTokenKind = peek().kind;
        if (isAction(expression) || nextTokenKind == SyntaxKind.SEMICOLON_TOKEN) {
            return getExpressionAsStatement(expression);
        }

        switch (nextTokenKind) {
            case EQUAL_TOKEN:
                switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                return parseAssignmentStmtRhs(expression);
            case IDENTIFIER_TOKEN:
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundAssignment(nextTokenKind)) {
                    return parseCompoundAssignmentStmtRhs(expression);
                }

                ParserRuleContext context;
                if (isPossibleExpressionStatement(expression)) {
                    context = ParserRuleContext.EXPR_STMT_RHS;
                } else {
                    context = ParserRuleContext.STMT_START_WITH_EXPR_RHS;
                }

                recover(peek(), context);
                return parseStatementStartWithExprRhs(expression);
        }
    }

    private boolean isPossibleExpressionStatement(STNode expression) {
        switch (expression.kind) {
            case METHOD_CALL:
            case FUNCTION_CALL:
            case CHECK_EXPRESSION:
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
                return true;
            default:
                return false;
        }
    }

    private STNode getExpressionAsStatement(STNode expression) {
        switch (expression.kind) {
            case METHOD_CALL:
            case FUNCTION_CALL:
                return parseCallStatement(expression);
            case CHECK_EXPRESSION:
                return parseCheckStatement(expression);
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
            case CLIENT_RESOURCE_ACCESS_ACTION:
                return parseActionStatement(expression);
            default:
                // Everything else can not be written as a statement.
                STNode semicolon = parseSemicolon();
                endContext();
                expression = getExpression(expression);
                STNode exprStmt = STNodeFactory.createExpressionStatementNode(SyntaxKind.INVALID_EXPRESSION_STATEMENT,
                        expression, semicolon);
                exprStmt = SyntaxErrors.addDiagnostic(exprStmt, DiagnosticErrorCode.ERROR_INVALID_EXPRESSION_STATEMENT);
                return exprStmt;
        }
    }

    private STNode parseArrayTypeDescriptorNode(STIndexedExpressionNode indexedExpr) {
        STNode memberTypeDesc = getTypeDescFromExpr(indexedExpr.containerExpression);
        STNodeList lengthExprs = (STNodeList) indexedExpr.keyExpression;
        if (lengthExprs.isEmpty()) {
            return createArrayTypeDesc(memberTypeDesc, indexedExpr.openBracket, STNodeFactory.createEmptyNode(),
                    indexedExpr.closeBracket);
        }

        // Validate the array length expression
        STNode lengthExpr = lengthExprs.get(0);
        switch (lengthExpr.kind) {
            case SIMPLE_NAME_REFERENCE:
                STSimpleNameReferenceNode nameRef = (STSimpleNameReferenceNode) lengthExpr;
                if (nameRef.name.isMissing()) {
                    return createArrayTypeDesc(memberTypeDesc, indexedExpr.openBracket, STNodeFactory.createEmptyNode(),
                            indexedExpr.closeBracket);
                }
                break;
            case ASTERISK_LITERAL:
            case QUALIFIED_NAME_REFERENCE:
                break;
            case NUMERIC_LITERAL:
                SyntaxKind innerChildKind = lengthExpr.childInBucket(0).kind;
                if (innerChildKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN ||
                        innerChildKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
                    break;
                }
                //fall through
            default:
                STNode newOpenBracketWithDiagnostics = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(
                        indexedExpr.openBracket, lengthExpr, DiagnosticErrorCode.ERROR_INVALID_ARRAY_LENGTH);
                indexedExpr = indexedExpr.replace(indexedExpr.openBracket, newOpenBracketWithDiagnostics);
                lengthExpr = STNodeFactory.createEmptyNode();
        }

        return createArrayTypeDesc(memberTypeDesc, indexedExpr.openBracket, lengthExpr, indexedExpr.closeBracket);
    }

    /**
     * <p>
     * Parse call statement, given the call expression.
     * </p>
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
        return parseCallStatementOrCheckStatement(expression);
    }

    /**
     * <p>
     * Parse checking statement.
     * </p>
     * <code>
     * checking-stmt := checking-expr ;
     * <br/>
     * checking-expr := checking-keyword expr ;
     * </code>
     *
     * @param expression Checking expression associated with the checking statement
     * @return Checking statement node
     */
    private STNode parseCheckStatement(STNode expression) {
        return parseCallStatementOrCheckStatement(expression);
    }

    private STNode parseCallStatementOrCheckStatement(STNode expression) {
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createExpressionStatementNode(SyntaxKind.CALL_STATEMENT, expression, semicolon);
    }

    private STNode parseActionStatement(STNode action) {
        STNode semicolon = parseSemicolon();
        endContext();
        return STNodeFactory.createExpressionStatementNode(SyntaxKind.ACTION_STATEMENT, action, semicolon);
    }

    /**
     * Parse client resource access action, given the starting expression.
     * <br/><br/>
     * <code>
     * client-resource-access-action := expression "->" "/" [resource-access-path] ["." method-name] ["(" arg-list ")"]
     * </code>
     * 
     * @param expression Expression
     * @param rightArrow Right arrow token
     * @param slashToken Slash token
     * @return Parsed node
     */
    private STNode parseClientResourceAccessAction(STNode expression, STNode rightArrow, STNode slashToken,
                                                   boolean isRhsExpr, boolean isInMatchGuard) {
        startContext(ParserRuleContext.CLIENT_RESOURCE_ACCESS_ACTION);
        
        STNode resourceAccessPath = parseOptionalResourceAccessPath(isRhsExpr, isInMatchGuard);
        STNode resourceAccessMethodDot = parseOptionalResourceAccessMethodDot(isRhsExpr, isInMatchGuard);
        STNode resourceAccessMethodName = STNodeFactory.createEmptyNode();
        if (resourceAccessMethodDot != null) {
            resourceAccessMethodName = STNodeFactory.createSimpleNameReferenceNode(parseFunctionName());
        }
        
        STNode resourceMethodCallArgList = parseOptionalResourceAccessActionArgList(isRhsExpr, isInMatchGuard);
        endContext();
        
        return STNodeFactory.createClientResourceAccessActionNode(expression, rightArrow, slashToken, 
                resourceAccessPath, resourceAccessMethodDot, resourceAccessMethodName, resourceMethodCallArgList);
    }
    
    private STNode parseOptionalResourceAccessPath(boolean isRhsExpr, boolean isInMatchGuard) {
        STNode resourceAccessPath = STNodeFactory.createEmptyNodeList();
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
            case OPEN_BRACKET_TOKEN:
                resourceAccessPath = parseResourceAccessPath(isRhsExpr, isInMatchGuard);
                break;
            case DOT_TOKEN:
            case OPEN_PAREN_TOKEN:
                break;
            default:
                if (isEndOfActionOrExpression(nextToken.kind, isRhsExpr, isInMatchGuard)) {
                    break;
                }

                recover(nextToken, ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_PATH);
                return parseOptionalResourceAccessPath(isRhsExpr, isInMatchGuard);
        }
        return  resourceAccessPath;
    }
    
    private STNode parseOptionalResourceAccessMethodDot(boolean isRhsExpr, boolean isInMatchGuard) {
        STNode dotToken = STNodeFactory.createEmptyNode();
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case DOT_TOKEN:
                dotToken = consume();
                break;
            case OPEN_PAREN_TOKEN:
                break;
            default:
                if (isEndOfActionOrExpression(nextToken.kind, isRhsExpr, isInMatchGuard)) {
                    break;
                }

                recover(nextToken, ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_METHOD);
                return parseOptionalResourceAccessMethodDot(isRhsExpr, isInMatchGuard);
        }
        
        return dotToken;
    }
    
    private STNode parseOptionalResourceAccessActionArgList(boolean isRhsExpr, boolean isInMatchGuard) {
        STNode argList = STNodeFactory.createEmptyNode();
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                argList = parseParenthesizedArgList();
                break;
            default:
                if (isEndOfActionOrExpression(nextToken.kind, isRhsExpr, isInMatchGuard)) {
                    break;
                }

                recover(nextToken, ParserRuleContext.OPTIONAL_RESOURCE_ACCESS_ACTION_ARG_LIST);
                return parseOptionalResourceAccessActionArgList(isRhsExpr, isInMatchGuard);
        }
        
        return argList;
    }

    /**
     * Parse resource access path.
     * <br/><br/>
     * <code>
     * resource-access-path :=
     *    resource-access-segments ["/" resource-access-rest-segment]
     *    | resource-access-rest-segment
     * <br/><br/>
     * resource-access-segments := resource-access-segment ("/" resource-access-segment ")*
     * <br/><br/>
     * resource-access-segment := resource-path-segment-name | computed-resource-access-segment
     * <br/><br/>
     * resource-path-segment-name := identifier
     * </code>
     * @return
     */
    private STNode parseResourceAccessPath(boolean isRhsExpr, boolean isInMatchGuard) {
        List<STNode> pathSegmentList = new ArrayList<>();
        // Parse first resource access path segment, that has no leading slash
        STNode pathSegment = parseResourceAccessSegment();
        pathSegmentList.add(pathSegment);

        STNode leadingSlash;
        STNode previousPathSegmentNode = pathSegment;
        while (!isEndOfResourceAccessPathSegments(peek().kind, isRhsExpr, isInMatchGuard)) {
            leadingSlash = parseResourceAccessSegmentRhs(isRhsExpr, isInMatchGuard);
            if (leadingSlash == null) {
                break;
            }
            
            pathSegment = parseResourceAccessSegment();
            
            if (previousPathSegmentNode.kind == SyntaxKind.RESOURCE_ACCESS_REST_SEGMENT) {
                updateLastNodeInListWithInvalidNode(pathSegmentList, leadingSlash, null);
                updateLastNodeInListWithInvalidNode(pathSegmentList, pathSegment, 
                        DiagnosticErrorCode.RESOURCE_ACCESS_SEGMENT_IS_NOT_ALLOWED_AFTER_REST_SEGMENT);
            } else {
                pathSegmentList.add(leadingSlash);
                pathSegmentList.add(pathSegment);
                previousPathSegmentNode = pathSegment;
            }
        }

        return STNodeFactory.createNodeList(pathSegmentList);
    }
    
    private STNode parseResourceAccessSegment() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                return consume();
            case OPEN_BRACKET_TOKEN:
                return parseComputedOrResourceAccessRestSegment(consume());
            default:
                recover(nextToken, ParserRuleContext.RESOURCE_ACCESS_PATH_SEGMENT);
                return parseResourceAccessSegment();
        }
    }

    /**
     * Parse computed resource segment or resource access rest segment.
     * <code>
     * <br/>
     * computed-resource-access-segment := "[" expression "]"
     * <br/>
     * resource-access-rest-segment := "[" "..." expression "]"
     * </code>
     * @param openBracket Open bracket token
     * @return Parsed node
     */
    private STNode parseComputedOrResourceAccessRestSegment(STNode openBracket) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ELLIPSIS_TOKEN:
                STNode ellipsisToken = consume();
                STNode expression = parseExpression();
                STNode closeBracketToken = parseCloseBracket();
                return STNodeFactory.createResourceAccessRestSegmentNode(openBracket, ellipsisToken,
                        expression, closeBracketToken);
            default:
                if (isValidExprStart(nextToken.kind)) {
                    expression = parseExpression();
                    closeBracketToken = parseCloseBracket();
                    return STNodeFactory.createComputedResourceAccessSegmentNode(openBracket, expression, 
                            closeBracketToken);
                }
                
                recover(nextToken, ParserRuleContext.COMPUTED_SEGMENT_OR_REST_SEGMENT);
                return parseComputedOrResourceAccessRestSegment(openBracket);
        }
    }

    /**
     * Parse resource access segment end.
     *
     * @return Parsed node
     */
    private STNode parseResourceAccessSegmentRhs(boolean isRhsExpr, boolean isInMatchGuard) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case SLASH_TOKEN:
                return consume();
            default:
                if (isEndOfResourceAccessPathSegments(nextToken.kind, isRhsExpr, isInMatchGuard)) {
                    return null;
                }
                
                recover(nextToken, ParserRuleContext.RESOURCE_ACCESS_SEGMENT_RHS);
                return parseResourceAccessSegmentRhs(isRhsExpr, isInMatchGuard);
        }
    }
    
    private boolean isEndOfResourceAccessPathSegments(SyntaxKind nextTokenKind,
                                                      boolean isRhsExpr, boolean isInMatchGuard) {
        switch (nextTokenKind) {
            case DOT_TOKEN:
            case OPEN_PAREN_TOKEN:
                return true;
            default:
                return isEndOfActionOrExpression(nextTokenKind, isRhsExpr, isInMatchGuard);
        }
    }
    
    private STNode parseRemoteMethodCallOrClientResourceAccessOrAsyncSendAction(STNode expression, boolean isRhsExpr,
                                                                                boolean isInMatchGuard) {
        STNode rightArrow = parseRightArrow();
        return parseClientResourceAccessOrAsyncSendActionRhs(expression, rightArrow, isRhsExpr, isInMatchGuard);
    }

    private STNode parseClientResourceAccessOrAsyncSendActionRhs(STNode expression, STNode rightArrow,
                                                                 boolean isRhsExpr, boolean isInMatchGuard) {
        STNode name;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case FUNCTION_KEYWORD:
                STNode functionKeyword = consume();
                name = STNodeFactory.createSimpleNameReferenceNode(functionKeyword);
                return parseAsyncSendAction(expression, rightArrow, name);
            case CONTINUE_KEYWORD:
            case COMMIT_KEYWORD:
                name = getKeywordAsSimpleNameRef();
                break;
            case SLASH_TOKEN:
                STNode slashToken = consume();
                return parseClientResourceAccessAction(expression, rightArrow, slashToken, isRhsExpr, isInMatchGuard);
            default:
                if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
                    // This can be `expr->identifier` or `expr->identifier()` or `expr->[MISSING /]identifier`
                    // This logic is added to improve recovery for resource method call action slash token
                    // Next token is a Missing token means, it is a correct token recovered previously
                    SyntaxKind nextNextNextTokenKind = getNextNextToken().kind;
                    if (nextNextNextTokenKind == SyntaxKind.OPEN_PAREN_TOKEN || 
                            isEndOfActionOrExpression(nextNextNextTokenKind, isRhsExpr, isInMatchGuard) || 
                            nextToken.isMissing()) {
                        name = STNodeFactory.createSimpleNameReferenceNode(parseFunctionName());
                        break;
                    }
                }
                
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.REMOTE_OR_RESOURCE_CALL_OR_ASYNC_SEND_RHS);
                if (solution.action == Action.KEEP) {
                    // identifier token can be valid for remote method call or async send
                    name = STNodeFactory.createSimpleNameReferenceNode(parseFunctionName());
                    break;
                }
                return parseClientResourceAccessOrAsyncSendActionRhs(expression, rightArrow, isRhsExpr, isInMatchGuard);
        }

        return parseRemoteCallOrAsyncSendEnd(expression, rightArrow, name);
    }

    private STNode parseRemoteCallOrAsyncSendEnd(STNode expression, STNode rightArrow, STNode name) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                return parseRemoteMethodCallAction(expression, rightArrow, name);
            case SEMICOLON_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case OPEN_BRACE_TOKEN:
            case COMMA_TOKEN:
            case FROM_KEYWORD:
            case JOIN_KEYWORD:
            case ON_KEYWORD:
            case LET_KEYWORD:
            case WHERE_KEYWORD:
            case ORDER_KEYWORD:
            case LIMIT_KEYWORD:
            case SELECT_KEYWORD:
                return parseAsyncSendAction(expression, rightArrow, name);
            default:
                recover(peek(), ParserRuleContext.REMOTE_CALL_OR_ASYNC_SEND_END);
                return parseRemoteCallOrAsyncSendEnd(expression, rightArrow, name);
        }
    }

    private STNode parseAsyncSendAction(STNode expression, STNode rightArrow, STNode peerWorker) {
        return STNodeFactory.createAsyncSendActionNode(expression, rightArrow, peerWorker);
    }


    /**
     * Parse remote method call action.
     * <p>
     * <code>
     * remote-method-call-action := expression -> method-name ( arg-list )
     * <br/>
     * async-send-action := expression -> peer-worker ;
     * </code>
     *
     * @param expression LHS expression
     * @param rightArrow  right arrow token
     * @param name remote method name
     * @return
     */
    private STNode parseRemoteMethodCallAction(STNode expression, STNode rightArrow, STNode name) {
        STNode openParenToken = parseArgListOpenParenthesis();
        STNode arguments = parseArgsList();
        STNode closeParenToken = parseArgListCloseParenthesis();
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
            recover(nextToken, ParserRuleContext.RIGHT_ARROW);
            return parseRightArrow();
        }
    }

    /**
     * Parse map type descriptor.
     * map-type-descriptor := `map` type-parameter
     *
     * @return Parsed node
     */
    private STNode parseMapTypeDescriptor(STNode mapKeyword) {
        STNode typeParameter = parseTypeParameter();
        return STNodeFactory.createMapTypeDescriptorNode(mapKeyword, typeParameter);
    }

    /**
     * Parse parameterized type descriptor.
     * parameterized-type-descriptor := `typedesc` [type-parameter]
     * <br/>&nbsp;| `future` [type-parameter]
     * <br/>&nbsp;| `xml` [type-parameter]
     * <br/>&nbsp;| `error` [type-parameter]
     *
     * @return Parsed node
     */
    private STNode parseParameterizedTypeDescriptor(STNode keywordToken) {
        STNode typeParamNode;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.LT_TOKEN) {
            typeParamNode = parseTypeParameter();
        } else {
            typeParamNode = STNodeFactory.createEmptyNode();
        }

        SyntaxKind parameterizedTypeDescKind = getParameterizedTypeDescKind(keywordToken);
        return STNodeFactory.createParameterizedTypeDescriptorNode(parameterizedTypeDescKind, keywordToken,
                typeParamNode);
    }

    private SyntaxKind getParameterizedTypeDescKind(STNode keywordToken) {
        switch (keywordToken.kind) {
            case TYPEDESC_KEYWORD:
                return SyntaxKind.TYPEDESC_TYPE_DESC;
            case FUTURE_KEYWORD:
                return SyntaxKind.FUTURE_TYPE_DESC;
            case XML_KEYWORD:
                return SyntaxKind.XML_TYPE_DESC;
            case ERROR_KEYWORD:
            default:
                return SyntaxKind.ERROR_TYPE_DESC;
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
            recover(nextToken, ParserRuleContext.GT);
            return parseGTToken();
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
            recover(nextToken, ParserRuleContext.LT);
            return parseLTToken();
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
     * @param metadata     Metadata
     * @param qualifier    Qualifier that precedes the listener declaration
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
            recover(token, ParserRuleContext.ANNOTATION_KEYWORD);
            return parseAnnotationKeyword();
        }
    }

    /**
     * Parse the components that follows after the annotation keyword of a annotation declaration.
     *
     * @param metadata          Metadata
     * @param qualifier         Qualifier that precedes the constant decl
     * @param constKeyword      Const keyword
     * @param annotationKeyword
     * @return Parsed node
     */
    private STNode parseAnnotationDeclFromType(STNode metadata, STNode qualifier, STNode constKeyword,
                                               STNode annotationKeyword) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                return parseAnnotationDeclWithOptionalType(metadata, qualifier, constKeyword, annotationKeyword);
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    break;
                }

                recover(peek(), ParserRuleContext.ANNOT_DECL_OPTIONAL_TYPE);
                return parseAnnotationDeclFromType(metadata, qualifier, constKeyword, annotationKeyword);
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
            recover(peek(), ParserRuleContext.ANNOTATION_TAG);
            return parseAnnotationTag();
        }
    }

    private STNode parseAnnotationDeclWithOptionalType(STNode metadata, STNode qualifier, STNode constKeyword,
                                                       STNode annotationKeyword) {
        // We come here if the type name also and identifier.
        // However, if it is a qualified identifier, then it has to be the type-desc.
        STNode typeDescOrAnnotTag = parseQualifiedIdentifier(ParserRuleContext.ANNOT_DECL_OPTIONAL_TYPE);
        if (typeDescOrAnnotTag.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            STNode annotTag = parseAnnotationTag();
            return parseAnnotationDeclAttachPoints(metadata, qualifier, constKeyword, annotationKeyword,
                    typeDescOrAnnotTag, annotTag);
        }

        // an simple identifier
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN || isValidTypeContinuationToken(nextToken)) {
            STNode typeDesc = parseComplexTypeDescriptor(typeDescOrAnnotTag,
                    ParserRuleContext.TYPE_DESC_IN_ANNOTATION_DECL, false);
            STNode annotTag = parseAnnotationTag();
            return parseAnnotationDeclAttachPoints(metadata, qualifier, constKeyword, annotationKeyword, typeDesc,
                    annotTag);
        }

        STNode annotTag = ((STSimpleNameReferenceNode) typeDescOrAnnotTag).name;
        return parseAnnotationDeclRhs(metadata, qualifier, constKeyword, annotationKeyword, annotTag);
    }

    /**
     * Parse the component that follows the first identifier in an annotation decl. The identifier
     * can be either the type-name (a user defined type) or the annot-tag, where the type-name
     * is not present.
     *
     * @param metadata           Metadata
     * @param qualifier          Qualifier that precedes the annotation decl
     * @param constKeyword       Const keyword
     * @param annotationKeyword  Annotation keyword
     * @param typeDescOrAnnotTag Identifier that follows the annotation-keyword
     * @return Parsed node
     */
    private STNode parseAnnotationDeclRhs(STNode metadata, STNode qualifier, STNode constKeyword,
                                          STNode annotationKeyword, STNode typeDescOrAnnotTag) {
        STToken nextToken = peek();
        STNode typeDesc;
        STNode annotTag;
        switch (nextToken.kind) {
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
                recover(peek(), ParserRuleContext.ANNOT_DECL_RHS);
                return parseAnnotationDeclRhs(metadata, qualifier, constKeyword, annotationKeyword, typeDescOrAnnotTag);
        }

        return parseAnnotationDeclAttachPoints(metadata, qualifier, constKeyword, annotationKeyword, typeDesc,
                annotTag);
    }

    private STNode parseAnnotationDeclAttachPoints(STNode metadata, STNode qualifier, STNode constKeyword,
                                                   STNode annotationKeyword, STNode typeDesc, STNode annotTag) {
        STNode onKeyword;
        STNode attachPoints;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case SEMICOLON_TOKEN:
                onKeyword = STNodeFactory.createEmptyNode();
                attachPoints = STNodeFactory.createEmptyNodeList();
                break;
            case ON_KEYWORD:
                onKeyword = parseOnKeyword();
                attachPoints = parseAnnotationAttachPoints();
                onKeyword = cloneWithDiagnosticIfListEmpty(attachPoints, onKeyword,
                        DiagnosticErrorCode.ERROR_MISSING_ANNOTATION_ATTACH_POINT);
                break;
            default:
                recover(peek(), ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS);
                return parseAnnotationDeclAttachPoints(metadata, qualifier, constKeyword, annotationKeyword, typeDesc,
                        annotTag);
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
     * type
     * | class
     * | [object|service remote] function
     * | parameter
     * | return
     * | service
     * | [object|record] field
     * <br/><br/>
     * source-only-attach-point := source source-only-attach-point-ident
     * <br/><br/>
     * source-only-attach-point-ident :=
     * annotation
     * | external
     * | var
     * | const
     * | listener
     * | worker
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
                STToken missingAttachPointIdent = SyntaxErrors.createMissingToken(SyntaxKind.TYPE_KEYWORD);
                STNode identList = STNodeFactory.createNodeList(missingAttachPointIdent);
                attachPoint = STNodeFactory.createAnnotationAttachPointNode(STNodeFactory.createEmptyNode(), identList);
                attachPoint = SyntaxErrors.addDiagnostic(attachPoint,
                        DiagnosticErrorCode.ERROR_MISSING_ANNOTATION_ATTACH_POINT);
                attachPoints.add(attachPoint);
                break;
            }

            attachPoints.add(attachPoint);
            nextToken = peek();
        }
        
        if (attachPoint.lastToken().isMissing() && this.tokenReader.peek().kind == SyntaxKind.IDENTIFIER_TOKEN &&
                !this.tokenReader.head().hasTrailingNewline()) {
            // Special case, when annotation-decl is in typing state.
            // e.g. annotation name on source object f<cursor>
            STToken nextNonVirtualToken = this.tokenReader.read();
            updateLastNodeInListWithInvalidNode(attachPoints, nextNonVirtualToken,
                    DiagnosticErrorCode.ERROR_INVALID_TOKEN, nextNonVirtualToken.text());
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
        switch (peek().kind) {
            case SEMICOLON_TOKEN:
                // null represents the end of attach points.
                return null;
            case COMMA_TOKEN:
                return consume();
            default:
                recover(peek(), ParserRuleContext.ATTACH_POINT_END);
                return parseAttachPointEnd();
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
        switch (peek().kind) {
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
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
            case RECORD_KEYWORD:
            case CLASS_KEYWORD:
                sourceKeyword = STNodeFactory.createEmptyNode();
                STNode firstIdent = consume();
                return parseDualAttachPointIdent(sourceKeyword, firstIdent);
            default:
                recover(peek(), ParserRuleContext.ATTACH_POINT);
                return parseAnnotationAttachPoint();
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
            recover(token, ParserRuleContext.SOURCE_KEYWORD);
            return parseSourceKeyword();
        }
    }

    /**
     * Parse attach point ident gievn.
     * <p>
     * <code>
     * source-only-attach-point-ident := annotation | external | var | const | listener | worker
     * <br/><br/>
     * dual-attach-point-ident := type | class | [object|service remote] function | parameter
     * | return | service | [object|record] field
     * </code>
     *
     * @param sourceKeyword Source keyword
     * @return Parsed node
     */
    private STNode parseAttachPointIdent(STNode sourceKeyword) {
        switch (peek().kind) {
            case ANNOTATION_KEYWORD:
            case EXTERNAL_KEYWORD:
            case VAR_KEYWORD:
            case CONST_KEYWORD:
            case LISTENER_KEYWORD:
            case WORKER_KEYWORD:
                STNode firstIdent = consume();
                STNode identList = STNodeFactory.createNodeList(firstIdent);
                return STNodeFactory.createAnnotationAttachPointNode(sourceKeyword, identList);
            case OBJECT_KEYWORD:
            case RESOURCE_KEYWORD:
            case RECORD_KEYWORD:
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
            case CLASS_KEYWORD:
                firstIdent = consume();
                return parseDualAttachPointIdent(sourceKeyword, firstIdent);
            default:
                recover(peek(), ParserRuleContext.ATTACH_POINT_IDENT);
                return parseAttachPointIdent(sourceKeyword);
        }
    }

    /**
     * Parse dual-attach-point ident.
     *
     * @param sourceKeyword Source keyword
     * @param firstIdent    first part of the dual attach-point
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
            case SERVICE_KEYWORD:
                return parseServiceAttachPoint(sourceKeyword, firstIdent);
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case FIELD_KEYWORD:
            case CLASS_KEYWORD:
            default: // default case should never be reached.
                STNode identList = STNodeFactory.createNodeList(firstIdent);
                return STNodeFactory.createAnnotationAttachPointNode(sourceKeyword, identList);
        }

        STNode identList = STNodeFactory.createNodeList(firstIdent, secondIdent);
        return STNodeFactory.createAnnotationAttachPointNode(sourceKeyword, identList);
    }

    /**
     * Parse remote ident.
     *
     * @return Parsed node
     */
    private STNode parseRemoteIdent() {
        STToken token = peek();
        if (token.kind == SyntaxKind.REMOTE_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.REMOTE_IDENT);
            return parseRemoteIdent();
        }
    }

    /**
     * Parse service attach point.
     * <code>service-attach-point := service | service remote function</code>
     *
     * @return Parsed node
     */
    private STNode parseServiceAttachPoint(STNode sourceKeyword, STNode firstIdent) {
        STNode identList;
        STToken token = peek();
        switch (token.kind) {
            case REMOTE_KEYWORD:
                STNode secondIdent = parseRemoteIdent();
                STNode thirdIdent = parseFunctionIdent();
                identList = STNodeFactory.createNodeList(firstIdent, secondIdent, thirdIdent);
                return STNodeFactory.createAnnotationAttachPointNode(sourceKeyword, identList);
            case COMMA_TOKEN:
            case SEMICOLON_TOKEN:
                identList = STNodeFactory.createNodeList(firstIdent);
                return STNodeFactory.createAnnotationAttachPointNode(sourceKeyword, identList);
            default:
                recover(token, ParserRuleContext.SERVICE_IDENT_RHS);
                return parseServiceAttachPoint(sourceKeyword, firstIdent);
        }
    }

    /**
     * Parse the idents that are supported after object-ident.
     *
     * @return Parsed node
     */
    private STNode parseIdentAfterObjectIdent() {
        STToken token = peek();
        switch (token.kind) {
            case FUNCTION_KEYWORD:
            case FIELD_KEYWORD:
                return consume();
            default:
                recover(token, ParserRuleContext.IDENT_AFTER_OBJECT_IDENT);
                return parseIdentAfterObjectIdent();
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
            recover(token, ParserRuleContext.FUNCTION_IDENT);
            return parseFunctionIdent();
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
            recover(token, ParserRuleContext.FIELD_IDENT);
            return parseFieldIdent();
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

        STNode namespaceUri = parseSimpleConstExpr();
        while (!isValidXMLNameSpaceURI(namespaceUri)) {
            xmlnsKeyword = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(xmlnsKeyword, namespaceUri,
                    DiagnosticErrorCode.ERROR_INVALID_XML_NAMESPACE_URI);
            namespaceUri = parseSimpleConstExpr();
        }

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
            recover(token, ParserRuleContext.XMLNS_KEYWORD);
            return parseXMLNSKeyword();
        }
    }

    private boolean isValidXMLNameSpaceURI(STNode expr) {
        switch (expr.kind) {
            case STRING_LITERAL:
            case QUALIFIED_NAME_REFERENCE:
            case SIMPLE_NAME_REFERENCE:
                return true;
            case IDENTIFIER_TOKEN:
            default:
                return false;
        }
    }

    private STNode parseSimpleConstExpr() {
        startContext(ParserRuleContext.CONSTANT_EXPRESSION);
        STNode expr = parseSimpleConstExprInternal();
        endContext();
        return expr;
    }

    /**
     * Parse simple constants expr.
     *
     * @return Parsed node
     */
    private STNode parseSimpleConstExprInternal() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case STRING_LITERAL_TOKEN:
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
                return parseBasicLiteral();
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return parseSignedIntOrFloat();
            case OPEN_PAREN_TOKEN:
                return parseNilLiteral();
            default:
                if (isPredeclaredIdentifier(nextToken.kind)) {
                    return parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                }
                
                recover(nextToken, ParserRuleContext.CONSTANT_EXPRESSION_START);
                return parseSimpleConstExprInternal();
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
        STNode asKeyword = STNodeFactory.createEmptyNode();
        STNode namespacePrefix = STNodeFactory.createEmptyNode();

        switch (peek().kind) {
            case AS_KEYWORD:
                asKeyword = parseAsKeyword();
                namespacePrefix = parseNamespacePrefix();
                break;
            case SEMICOLON_TOKEN:
                break;
            default:
                recover(peek(), ParserRuleContext.XML_NAMESPACE_PREFIX_DECL);
                return parseXMLDeclRhs(xmlnsKeyword, namespaceUri, isModuleVar);
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
            recover(peek(), ParserRuleContext.NAMESPACE_PREFIX);
            return parseNamespacePrefix();
        }
    }

    /**
     * Parse named worker declaration.
     * <p>
     * <code>named-worker-decl := [annots] [transactional] worker worker-name return-type-descriptor { sequence-stmt }
     * </code>
     *
     * @param annots Annotations attached to the worker decl
     * @param qualifiers Preceding transactional keyword in a list
     * @return Parsed node
     */
    private STNode parseNamedWorkerDeclaration(STNode annots, List<STNode> qualifiers) {
        startContext(ParserRuleContext.NAMED_WORKER_DECL);
        STNode transactionalKeyword = getTransactionalKeyword(qualifiers);
        STNode workerKeyword = parseWorkerKeyword();
        STNode workerName = parseWorkerName();
        STNode returnTypeDesc = parseReturnTypeDescriptor();
        STNode workerBody = parseBlockNode();
        endContext();
        return STNodeFactory.createNamedWorkerDeclarationNode(annots, transactionalKeyword, workerKeyword, workerName,
                returnTypeDesc, workerBody);
    }

    private STNode getTransactionalKeyword(List<STNode> qualifierList) {
        // Validate qualifiers extract transactional keyword
        List<STNode> validatedList = new ArrayList<>();

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
            } else if (qualifier.kind == SyntaxKind.TRANSACTIONAL_KEYWORD) {
                validatedList.add(qualifier);
            } else if (qualifierList.size() == nextIndex) {
                addInvalidNodeToNextToken(qualifier, DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED,
                        ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        STNode transactionalKeyword;
        if (validatedList.isEmpty()) {
            transactionalKeyword = STNodeFactory.createEmptyNode();
        } else {
            transactionalKeyword = validatedList.get(0);
        }
        return transactionalKeyword;
    }

    private STNode parseReturnTypeDescriptor() {
        // If the return type is not present, simply return
        STToken token = peek();
        if (token.kind != SyntaxKind.RETURNS_KEYWORD) {
            return STNodeFactory.createEmptyNode();
        }

        STNode returnsKeyword = consume();
        STNode annot = parseOptionalAnnotations();
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
            recover(peek(), ParserRuleContext.WORKER_KEYWORD);
            return parseWorkerKeyword();
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
            recover(peek(), ParserRuleContext.WORKER_NAME);
            return parseWorkerName();
        }
    }

    /**
     * Parse lock statement.
     * <code>lock-stmt := lock block-stmt [on-fail-clause]</code>
     *
     * @return Lock statement
     */
    private STNode parseLockStatement() {
        startContext(ParserRuleContext.LOCK_STMT);
        STNode lockKeyword = parseLockKeyword();
        STNode blockStatement = parseBlockNode();
        endContext();
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createLockStatementNode(lockKeyword, blockStatement, onFailClause);
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
            recover(token, ParserRuleContext.LOCK_KEYWORD);
            return parseLockKeyword();
        }
    }

    /**
     * Parse union type descriptor.
     * union-type-descriptor := type-descriptor | type-descriptor
     *
     * @param leftTypeDesc Type desc in the LHS os the union type desc.
     * @param context      Current context.
     * @return parsed union type desc node
     */
    private STNode parseUnionTypeDescriptor(STNode leftTypeDesc, ParserRuleContext context,
                                            boolean isTypedBindingPattern) {
        // we come here only after seeing | token hence consume.
        STNode pipeToken = consume();
        STNode rightTypeDesc = parseTypeDescriptorInternal(new ArrayList<>(), context, isTypedBindingPattern, false,
                TypePrecedence.UNION);
        return mergeTypesWithUnion(leftTypeDesc, pipeToken, rightTypeDesc);
    }

    /**
     * Creates a union type descriptor after validating lhs and rhs types.
     * <p>
     * <i>Note: Since type precedence and associativity are not taken into account here,
     * this method should not be called directly when types are unknown.
     * <br/>
     * Call {@link #mergeTypesWithUnion(STNode, STNode, STNode)} instead</i>
     *
     * @param leftTypeDesc  lhs type
     * @param pipeToken     pipe token
     * @param rightTypeDesc rhs type
     * @return a UnionTypeDescriptorNode
     */
    private STNode createUnionTypeDesc(STNode leftTypeDesc, STNode pipeToken, STNode rightTypeDesc) {
        leftTypeDesc = validateForUsageOfVar(leftTypeDesc);
        rightTypeDesc = validateForUsageOfVar(rightTypeDesc);
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
            recover(token, ParserRuleContext.PIPE);
            return parsePipeToken();
        }
    }

    private boolean isTypeStartingToken(SyntaxKind nodeKind) {
        return isTypeStartingToken(nodeKind, getNextNextToken());
    }
    
    private static boolean isTypeStartingToken(SyntaxKind nextTokenKind, STToken nextNextToken) {
        switch (nextTokenKind) {
            case IDENTIFIER_TOKEN:
            case SERVICE_KEYWORD:
            case RECORD_KEYWORD:
            case OBJECT_KEYWORD:
            case ABSTRACT_KEYWORD:
            case CLIENT_KEYWORD:
            case OPEN_PAREN_TOKEN: // nil type descriptor '()'
            case MAP_KEYWORD: // map type desc
            case STREAM_KEYWORD: // stream type desc
            case TABLE_KEYWORD: // table type
            case FUNCTION_KEYWORD:
            case OPEN_BRACKET_TOKEN:
            case DISTINCT_KEYWORD:
            case ISOLATED_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case TRANSACTION_KEYWORD:
                return true;
            default:
                if (isParameterizedTypeToken(nextTokenKind)) {
                    return true;
                }
                
                if (isSingletonTypeDescStart(nextTokenKind, nextNextToken)) {
                    return true;
                }
                return isSimpleType(nextTokenKind);
        }
    }

    /**
     * Check if the token kind is a type descriptor in terminal expression.
     * <p>
     * simple-type-in-expr :=
     * boolean | int | byte | float | decimal | string | handle | json | anydata | any | never
     *
     * @param nodeKind token kind to check
     * @return <code>true</code> for simple type token in expression. <code>false</code> otherwise.
     */
    private boolean isSimpleTypeInExpression(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case VAR_KEYWORD:
            case READONLY_KEYWORD:
                return false;
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
            case JSON_KEYWORD:
            case HANDLE_KEYWORD:
            case ANY_KEYWORD:
            case ANYDATA_KEYWORD:
            case NEVER_KEYWORD:
            case VAR_KEYWORD:
            case READONLY_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    static boolean isPredeclaredPrefix(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case BOOLEAN_KEYWORD:
            case DECIMAL_KEYWORD:
            case ERROR_KEYWORD:
            case FLOAT_KEYWORD:
            case FUNCTION_KEYWORD:
            case FUTURE_KEYWORD:
            case INT_KEYWORD:
            case MAP_KEYWORD:
            case OBJECT_KEYWORD:
            case STREAM_KEYWORD:
            case STRING_KEYWORD:
            case TABLE_KEYWORD:
            case TRANSACTION_KEYWORD:
            case TYPEDESC_KEYWORD:
            case XML_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    private boolean isQualifiedIdentifierPredeclaredPrefix(SyntaxKind nodeKind) {
       return isPredeclaredPrefix(nodeKind) && getNextNextToken().kind == SyntaxKind.COLON_TOKEN;
    }

    private static SyntaxKind getBuiltinTypeSyntaxKind(SyntaxKind typeKeyword) {
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
            case VAR_KEYWORD:
                return SyntaxKind.VAR_TYPE_DESC;
            case READONLY_KEYWORD:
                return SyntaxKind.READONLY_TYPE_DESC;
            default:
                assert false : typeKeyword + " is not a built-in type";
                return SyntaxKind.TYPE_REFERENCE;
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
            recover(token, ParserRuleContext.FORK_KEYWORD);
            return parseForkKeyword();
        }
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

        // Parse named-worker declarations
        ArrayList<STNode> workers = new ArrayList<>();
        while (!isEndOfStatements()) {
            STNode stmt = parseStatement();
            if (stmt == null) {
                break;
            }

            if (validateStatement(stmt)) {
                continue;
            }

            switch (stmt.kind) {
                case NAMED_WORKER_DECLARATION:
                    workers.add(stmt);
                    break;
                default:
                    // TODO need to check whether we've already added the same diagnostics before
                    // TODO We need to avoid repetitive diagnostics of same kind
                    // I think we need a method to check whether a node has a particular diagnostic
                    if (workers.isEmpty()) {
                        openBrace = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(openBrace, stmt,
                                DiagnosticErrorCode.ERROR_ONLY_NAMED_WORKERS_ALLOWED_HERE);
                    } else {
                        updateLastNodeInListWithInvalidNode(workers, stmt,
                                DiagnosticErrorCode.ERROR_ONLY_NAMED_WORKERS_ALLOWED_HERE);
                    }
            }
        }

        STNode namedWorkerDeclarations = STNodeFactory.createNodeList(workers);
        STNode closeBrace = parseCloseBrace();
        endContext();

        STNode forkStmt =
                STNodeFactory.createForkStatementNode(forkKeyword, openBrace, namedWorkerDeclarations, closeBrace);
        if (isNodeListEmpty(namedWorkerDeclarations)) {
            return SyntaxErrors.addDiagnostic(forkStmt,
                    DiagnosticErrorCode.ERROR_MISSING_NAMED_WORKER_DECLARATION_IN_FORK_STMT);
        }

        return forkStmt;
    }

    /**
     * Parse trap expression.
     * <p>
     * <code>
     * trap-expr := trap expression
     * </code>
     *
     * @param allowActions Allow actions
     * @param isRhsExpr    Whether this is a RHS expression or not
     * @return Trap expression node
     */
    private STNode parseTrapExpression(boolean isRhsExpr, boolean allowActions, boolean isInConditionalExpr) {
        STNode trapKeyword = parseTrapKeyword();
        STNode expr = parseExpression(OperatorPrecedence.TRAP, isRhsExpr, allowActions, isInConditionalExpr);
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
            recover(token, ParserRuleContext.TRAP_KEYWORD);
            return parseTrapKeyword();
        }
    }

    /**
     * Parse list constructor expression.
     * <p>
     * <code>
     * list-constructor-expr := [ [ list-members  ] ]
     * <br/>
     * list-members  := list-member  (, list-member)*
     * <br/>
     * list-member := expression | spread-member
     * <br/>
     * spread-member := ... expression
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseListConstructorExpr() {
        startContext(ParserRuleContext.LIST_CONSTRUCTOR);
        STNode openBracket = parseOpenBracket();
        STNode listMembers = parseListMembers();
        STNode closeBracket = parseCloseBracket();
        endContext();
        return STNodeFactory.createListConstructorExpressionNode(openBracket, listMembers, closeBracket);
    }

    /**
     * Parse optional list member list.
     *
     * @return Parsed node
     */
    private STNode parseListMembers() {
        List<STNode> listMembers = new ArrayList<>();
        if (isEndOfListConstructor(peek().kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        STNode listMember = parseListMember();
        listMembers.add(listMember);
        return parseListMembers(listMembers);
    }

    private STNode parseListMembers(List<STNode> listMembers) {
        // Parse the remaining list members
        STNode listConstructorMemberEnd;
        while (!isEndOfListConstructor(peek().kind)) {
            listConstructorMemberEnd = parseListConstructorMemberEnd();
            if (listConstructorMemberEnd == null) {
                break;
            }
            listMembers.add(listConstructorMemberEnd);

            STNode listMember = parseListMember();
            listMembers.add(listMember);
        }

        return STNodeFactory.createNodeList(listMembers);
    }

    /**
     * Parse list member.
     * <p>
     * <code>
     * list-member := expression | spread-member
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseListMember() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.ELLIPSIS_TOKEN) {
            return parseSpreadMember();
        } else {
            return parseExpression();
        }
    }

    /**
     * Parse spread member.
     * <p>
     * <code>
     * spread-member := ... expression
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseSpreadMember() {
        STNode ellipsis = parseEllipsis();
        STNode expr = parseExpression();
        return STNodeFactory.createSpreadMemberNode(ellipsis, expr);
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
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COMMA_TOKEN:
                return consume();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                recover(nextToken, ParserRuleContext.LIST_CONSTRUCTOR_MEMBER_END);
                return parseListConstructorMemberEnd();
        }
    }

    /**
     * Parse foreach statement.
     * <code>foreach-stmt := foreach typed-binding-pattern in action-or-expr block-stmt [on-fail-clause]</code>
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
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createForEachStatementNode(forEachKeyword, typedBindingPattern, inKeyword, actionOrExpr,
                blockStatement, onFailClause);
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
            recover(token, ParserRuleContext.FOREACH_KEYWORD);
            return parseForEachKeyword();
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
            recover(token, ParserRuleContext.IN_KEYWORD);
            return parseInKeyword();
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
    private STNode parseTypeCastExpr(boolean isRhsExpr, boolean allowActions, boolean isInConditionalExpr) {
        startContext(ParserRuleContext.TYPE_CAST);
        STNode ltToken = parseLTToken();
        return parseTypeCastExpr(ltToken, isRhsExpr, allowActions, isInConditionalExpr);
    }

    private STNode parseTypeCastExpr(STNode ltToken, boolean isRhsExpr, boolean allowActions,
                                     boolean isInConditionalExpr) {
        STNode typeCastParam = parseTypeCastParam();
        STNode gtToken = parseGTToken();
        endContext();

        // allow-actions flag is always false, since there will not be any actions
        // within the type-cast-expr, due to the precedence.
        STNode expression =
                parseExpression(OperatorPrecedence.EXPRESSION_ACTION, isRhsExpr, allowActions, isInConditionalExpr);
        return STNodeFactory.createTypeCastExpressionNode(ltToken, typeCastParam, gtToken, expression);
    }

    private STNode parseTypeCastParam() {
        STNode annot;
        STNode type;
        STToken token = peek();

        switch (token.kind) {
            case AT_TOKEN:
                annot = parseOptionalAnnotations();
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
            recover(token, ParserRuleContext.TABLE_KEYWORD);
            return parseTableKeyword();
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
        STNode rowEnd;
        while (!isEndOfTableRowList(nextToken.kind)) {
            rowEnd = parseTableRowEnd();
            if (rowEnd == null) {
                break;
            }

            mappings.add(rowEnd);
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

    private STNode parseTableRowEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
            case EOF_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.TABLE_ROW_END);
                return parseTableRowEnd();
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
        }

        if (isKeyKeyword(token)) {
            // this is to treat "key" as a keyword, even if its parsed as an identifier from lexer.
            return getKeyKeyword(consume());
        }

        recover(token, ParserRuleContext.KEY_KEYWORD);
        return parseKeyKeyword();
    }

    static boolean isKeyKeyword(STToken token) {
        return token.kind == SyntaxKind.IDENTIFIER_TOKEN && LexerTerminals.KEY.equals(token.text());
    }

    private STNode getKeyKeyword(STToken token) {
        return STNodeFactory.createToken(SyntaxKind.KEY_KEYWORD, token.leadingMinutiae(), token.trailingMinutiae(),
                token.diagnostics());
    }

    private STToken getUnderscoreKeyword(STToken token) {
        return STNodeFactory.createToken(SyntaxKind.UNDERSCORE_KEYWORD, token.leadingMinutiae(),
                token.trailingMinutiae(), token.diagnostics());
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
     * Parse error-keyword.
     *
     * @return Parsed error-keyword node
     */
    private STNode parseErrorKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ERROR_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.ERROR_KEYWORD);
            return parseErrorKeyword();
        }
    }

    /**
     * Parse stream type descriptor.
     * <p>
     * stream-type-descriptor := stream [stream-type-parameters]
     * <br/>
     * stream-type-parameters := < type-descriptor [, type-descriptor]>
     * </p>
     *
     * @return Parsed stream type descriptor node
     */
    private STNode parseStreamTypeDescriptor(STNode streamKeywordToken) {
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
        STNode leftTypeDescNode = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC);
        STNode streamTypedesc = parseStreamTypeParamsNode(ltToken, leftTypeDescNode);
        endContext();
        return streamTypedesc;
    }

    private STNode parseStreamTypeParamsNode(STNode ltToken, STNode leftTypeDescNode) {
        STNode commaToken, rightTypeDescNode, gtToken;
        switch (peek().kind) {
            case COMMA_TOKEN:
                commaToken = parseComma();
                rightTypeDescNode = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC);
                break;
            case GT_TOKEN:
                commaToken = STNodeFactory.createEmptyNode();
                rightTypeDescNode = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.STREAM_TYPE_FIRST_PARAM_RHS);
                return parseStreamTypeParamsNode(ltToken, leftTypeDescNode);
        }

        gtToken = parseGTToken();
        return STNodeFactory.createStreamTypeParamsNode(ltToken, leftTypeDescNode, commaToken, rightTypeDescNode,
                gtToken);
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
    private STNode parseLetExpression(boolean isRhsExpr, boolean isInConditionalExpr) {
        STNode letKeyword = parseLetKeyword();
        STNode letVarDeclarations = parseLetVarDeclarations(ParserRuleContext.LET_EXPR_LET_VAR_DECL, isRhsExpr, false);
        STNode inKeyword = parseInKeyword();

        // If the variable declaration list is empty, clone the letKeyword token with the given diagnostic.
        letKeyword = cloneWithDiagnosticIfListEmpty(letVarDeclarations, letKeyword,
                DiagnosticErrorCode.ERROR_MISSING_LET_VARIABLE_DECLARATION);

        // allow-actions flag is always false, since there will not be any actions
        // within the let-expr, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.REMOTE_CALL_ACTION, isRhsExpr, false,
                isInConditionalExpr);
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
            recover(token, ParserRuleContext.LET_KEYWORD);
            return parseLetKeyword();
        }
    }

    /**
     * Parse let variable declarations.
     * <p>
     * <code>let-var-decl-list := let-var-decl [, let-var-decl]*</code>
     *
     * @return Parsed node
     */
    private STNode parseLetVarDeclarations(ParserRuleContext context, boolean isRhsExpr, boolean allowActions) {
        startContext(context);
        List<STNode> varDecls = new ArrayList<>();
        STToken nextToken = peek();

        if (isEndOfLetVarDeclarations(nextToken.kind, getNextNextToken())) {
            endContext();
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first variable declaration, that has no leading comma
        STNode varDec = parseLetVarDecl(context, isRhsExpr, allowActions);
        varDecls.add(varDec);

        // Parse the remaining variable declarations
        nextToken = peek();
        STNode leadingComma;
        while (!isEndOfLetVarDeclarations(nextToken.kind, getNextNextToken())) {
            leadingComma = parseComma();
            varDecls.add(leadingComma);
            varDec = parseLetVarDecl(context, isRhsExpr, allowActions);
            varDecls.add(varDec);
            nextToken = peek();
        }

        endContext();
        return STNodeFactory.createNodeList(varDecls);
    }

    static boolean isEndOfLetVarDeclarations(SyntaxKind tokenKind, STToken nextNextToken) {
        switch (tokenKind) {
            case COMMA_TOKEN:
            case AT_TOKEN:
                return false;
            case IN_KEYWORD:
                return true;
            default:
                return !isTypeStartingToken(tokenKind, nextNextToken);
        }
    }

    /**
     * Parse let variable declaration.
     * <p>
     * <code>let-var-decl := [annots] typed-binding-pattern = expression</code>
     *
     * @return Parsed node
     */
    private STNode parseLetVarDecl(ParserRuleContext context, boolean isRhsExpr, boolean allowActions) {
        STNode annot = parseOptionalAnnotations();
        STNode typedBindingPattern = parseTypedBindingPattern(ParserRuleContext.LET_EXPR_LET_VAR_DECL);
        STNode assign = parseAssignOp();
        STNode expression = context ==  ParserRuleContext.LET_CLAUSE_LET_VAR_DECL ?
                parseExpression(OperatorPrecedence.QUERY, isRhsExpr, allowActions) :
                parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, isRhsExpr, false);

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
            recover(token, ParserRuleContext.STRING_KEYWORD);
            return parseStringKeyword();
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

        if (startingBackTick.isMissing()) {
            return createMissingTemplateExpressionNode(xmlKeyword, SyntaxKind.XML_TEMPLATE_EXPRESSION);
        }

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
            recover(token, ParserRuleContext.XML_KEYWORD);
            return parseXMLKeyword();
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

        CharReader charReader = CharReader.from(xmlStringBuilder.toString());
        AbstractTokenReader tokenReader = new TokenReader(new XMLLexer(charReader));
        XMLParser xmlParser = new XMLParser(tokenReader, expressions);
        return xmlParser.parse();
    }

    /**
     * Parse regular expression constructor.
     * <p>
     * <code>regexp-constructor-expr := re BacktickString</code>
     *
     * @return Regular expression template expression
     */
    private STNode parseRegExpTemplateExpression() {
        STNode reKeyword = consume();
        STNode startingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);

        if (startingBackTick.isMissing()) {
            return createMissingTemplateExpressionNode(reKeyword, SyntaxKind.REGEX_TEMPLATE_EXPRESSION);
        }

        STNode content = parseTemplateContentAsRegExp();
        STNode endingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_END);
        return STNodeFactory.createTemplateExpressionNode(SyntaxKind.REGEX_TEMPLATE_EXPRESSION, reKeyword,
                startingBackTick, content, endingBackTick);
    }

    private STNode createMissingTemplateExpressionNode(STNode reKeyword, SyntaxKind kind) {
        // Create new missing startingBackTick token which has no diagnostic.
        STNode startingBackTick = SyntaxErrors.createMissingToken(SyntaxKind.BACKTICK_TOKEN);
        STNode endingBackTick = SyntaxErrors.createMissingToken(SyntaxKind.BACKTICK_TOKEN);
        STNode content = STAbstractNodeFactory.createEmptyNodeList();
        STNode templateExpr =
                STNodeFactory.createTemplateExpressionNode(kind, reKeyword, startingBackTick, content, endingBackTick);
        templateExpr = SyntaxErrors.addDiagnostic(templateExpr, DiagnosticErrorCode.ERROR_MISSING_BACKTICK_STRING);
        return templateExpr;
    }

    /**
     * Parse the content of the template string as regular expression. This method first read the
     * input in the same way as the raw-backtick-template (BacktickString). Then
     * it parses the content as regular expression.
     *
     * @return Template expression node
     */
    private STNode parseTemplateContentAsRegExp() {
        // Separate out the interpolated expressions to a queue. Then merge the string content using '${}'.
        // These '${}' are used to represent the interpolated locations. Regular expression parser will replace '${}'
        // with the actual interpolated expression, while building the regular expression tree.
        ArrayDeque<STNode> expressions = new ArrayDeque<>();
        StringBuilder regExpStringBuilder = new StringBuilder();
        STToken nextToken = peek();
        while (!isEndOfBacktickContent(nextToken.kind)) {
            STNode contentItem = parseTemplateItem();
            if (contentItem.kind == SyntaxKind.TEMPLATE_STRING) {
                regExpStringBuilder.append(((STToken) contentItem).text());
            } else {
                regExpStringBuilder.append("${}");
                expressions.add(contentItem);
            }
            nextToken = peek();
        }

        CharReader charReader = CharReader.from(regExpStringBuilder.toString());
        AbstractTokenReader tokenReader = new TokenReader(new RegExpLexer(charReader));
        RegExpParser regExpParser = new RegExpParser(tokenReader, expressions);
        return regExpParser.parse();
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

        // Remove additional token in interpolation
        while (!isEndOfInterpolation()) {
            STToken nextToken = consume();
            expr = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(expr, nextToken,
                    DiagnosticErrorCode.ERROR_INVALID_TOKEN, nextToken.text());
        }

        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createInterpolationNode(interpolStart, expr, closeBrace);
    }

    private boolean isEndOfInterpolation() {
        SyntaxKind nextTokenKind = peek().kind;
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case BACKTICK_TOKEN:
                return true;
            default:
                // Validate if the close brace is the end close brace of interpolation
                ParserMode currentLexerMode = this.tokenReader.getCurrentMode();
                return nextTokenKind == SyntaxKind.CLOSE_BRACE_TOKEN && currentLexerMode != ParserMode.INTERPOLATION &&
                        currentLexerMode != ParserMode.INTERPOLATION_BRACED_CONTENT;
        }
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
            recover(token, ParserRuleContext.INTERPOLATION_START_TOKEN);
            return parseInterpolationStart();
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
            recover(token, ctx);
            return parseBacktickToken(ctx);
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
    private STNode parseTableTypeDescriptor(STNode tableKeywordToken) {
        STNode rowTypeParameterNode = parseRowTypeParameter();
        STNode keyConstraintNode;
        STToken nextToken = peek();
        if (isKeyKeyword(nextToken)) {
            STNode keyKeywordToken = getKeyKeyword(consume());
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
        switch (peek().kind) {
            case OPEN_PAREN_TOKEN:
                return parseKeySpecifier(keyKeywordToken);
            case LT_TOKEN:
                return parseKeyTypeConstraint(keyKeywordToken);
            default:
                recover(peek(), ParserRuleContext.KEY_CONSTRAINTS_RHS);
                return parseKeyConstraint(keyKeywordToken);
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
        STNode openParenToken = parseOpenParenthesis();
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
     * <code>
     * function-type-descriptor := function-quals function function-signature 
     * <br/>&nbsp;| [isolated] function
     * <br/>
     * function-quals := (transactional | isolated)*
     * </code>
     *
     * @param qualifiers Preceding type descriptor qualifiers
     * @return Function type descriptor node
     */
    private STNode parseFunctionTypeDesc(List<STNode> qualifiers) {
        startContext(ParserRuleContext.FUNC_TYPE_DESC);
        STNode functionKeyword = parseFunctionKeyword();

        boolean hasFuncSignature = false;
        STNode signature = STNodeFactory.createEmptyNode();
        if (peek().kind == SyntaxKind.OPEN_PAREN_TOKEN ||
                isSyntaxKindInList(qualifiers, SyntaxKind.TRANSACTIONAL_KEYWORD)) {
            signature = parseFuncSignature(true);
            hasFuncSignature = true;
        }

        // Validate function type qualifiers
        STNode[] nodes = createFuncTypeQualNodeList(qualifiers, functionKeyword, hasFuncSignature);
        STNode qualifierList = nodes[0];
        functionKeyword = nodes[1];

        endContext();
        return STNodeFactory.createFunctionTypeDescriptorNode(qualifierList, functionKeyword, signature);
    }
    
    private STNode getLastNodeInList(List<STNode> nodeList) {
        return nodeList.get(nodeList.size() - 1);
    }

    private STNode[] createFuncTypeQualNodeList(List<STNode> qualifierList, STNode functionKeyword,
                                                boolean hasFuncSignature) {
        // Validate qualifiers and create a STNodeList
        List<STNode> validatedList = new ArrayList<>();

        for (int i = 0; i < qualifierList.size(); i++) {
            STNode qualifier = qualifierList.get(i);
            int nextIndex = i + 1;

            if (isSyntaxKindInList(validatedList, qualifier.kind)) {
                updateLastNodeInListWithInvalidNode(validatedList, qualifier,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
            } else if (hasFuncSignature && isRegularFuncQual(qualifier.kind)) {
                validatedList.add(qualifier);
            } else if (qualifier.kind == SyntaxKind.ISOLATED_KEYWORD) {
                validatedList.add(qualifier);
            } else if (qualifierList.size() == nextIndex) {
                functionKeyword = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(functionKeyword, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            } else {
                updateANodeInListWithLeadingInvalidNode(qualifierList, nextIndex, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
            }
        }

        STNode nodeList = STNodeFactory.createNodeList(validatedList);
        return new STNode[]{ nodeList, functionKeyword };
    }

    private boolean isRegularFuncQual(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case ISOLATED_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse explicit anonymous function expression.
     * <p>
     * <code>explicit-anonymous-function-expr :=
     * [annots] (isolated| transactional) function function-signature anon-func-body</code>
     *
     * @param annots     Annotations.
     * @param qualifiers Function qualifiers
     * @param isRhsExpr  Is expression in rhs context
     * @return Anonymous function expression node
     */
    private STNode parseExplicitFunctionExpression(STNode annots, List<STNode> qualifiers, boolean isRhsExpr) {
        startContext(ParserRuleContext.ANON_FUNC_EXPRESSION);
        STNode funcKeyword = parseFunctionKeyword();

        // Validate function type qualifiers
        STNode[] nodes = createFuncTypeQualNodeList(qualifiers, funcKeyword, true);
        STNode qualifierList = nodes[0];
        funcKeyword = nodes[1];

        STNode funcSignature = parseFuncSignature(false);
        // Context ended inside parseAnonFuncBody method
        STNode funcBody = parseAnonFuncBody(isRhsExpr);
        return STNodeFactory.createExplicitAnonymousFunctionExpressionNode(annots, qualifierList, funcKeyword,
                funcSignature, funcBody);
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
        switch (peek().kind) {
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
                recover(peek(), ParserRuleContext.ANON_FUNC_BODY);
                return parseAnonFuncBody(isRhsExpr);
        }
    }

    /**
     * Parse expression function body.
     * <p>
     * <code>expr-function-body := => expression</code>
     *
     * @param isAnon    Is anonymous function.
     * @param isRhsExpr Is expression in rhs context
     * @return Expression function body node
     */
    private STNode parseExpressionFuncBody(boolean isAnon, boolean isRhsExpr) {
        STNode rightDoubleArrow = parseDoubleRightArrow();

        // Give high priority to the body-expr. This is done by lowering the current
        // precedence bewfore visiting the body.
        STNode expression = parseExpression(OperatorPrecedence.REMOTE_CALL_ACTION, isRhsExpr, false);

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
            recover(token, ParserRuleContext.EXPR_FUNC_BODY_START);
            return parseDoubleRightArrow();
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
            case NIL_LITERAL:
                STNilLiteralNode nilLiteralNode = (STNilLiteralNode) params;
                params = STNodeFactory.createImplicitAnonymousFunctionParameters(nilLiteralNode.openParenToken,
                        STNodeFactory.createNodeList(new ArrayList<>()), nilLiteralNode.closeParenToken);
                break;
            default:
                STToken syntheticParam = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
                syntheticParam = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(syntheticParam, params,
                        DiagnosticErrorCode.ERROR_INVALID_PARAM_LIST_IN_INFER_ANONYMOUS_FUNCTION_EXPR);
                params = STNodeFactory.createSimpleNameReferenceNode(syntheticParam);
        }

        STNode rightDoubleArrow = parseDoubleRightArrow();
        // start parsing the expr by giving higher-precedence to parse the right side arguments for right associative
        // operators. That is done by lowering the current precedence.
        STNode expression = parseExpression(OperatorPrecedence.REMOTE_CALL_ACTION, isRhsExpr, false);
        return STNodeFactory.createImplicitAnonymousFunctionExpressionNode(params, rightDoubleArrow, expression);
    }

    /**
     * Create a new anon-func-param node from a braced expression.
     *
     * @param bracedExpression Braced expression
     * @return Anon-func param node
     */
    private STNode getAnonFuncParam(STBracedExpressionNode bracedExpression) {
        List<STNode> paramList = new ArrayList<>();
        STNode innerExpression = bracedExpression.expression;
        STNode openParen = bracedExpression.openParen;
        if (innerExpression.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            paramList.add(innerExpression);
        } else {
            openParen = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(openParen, innerExpression,
                    DiagnosticErrorCode.ERROR_INVALID_PARAM_LIST_IN_INFER_ANONYMOUS_FUNCTION_EXPR);
        }
        return STNodeFactory.createImplicitAnonymousFunctionParameters(openParen,
                STNodeFactory.createNodeList(paramList), bracedExpression.closeParen);
    }

    /**
     * Parse implicit anon function expression.
     *
     * @param openParen  Open parenthesis token
     * @param firstParam First parameter
     * @param isRhsExpr  Is expression in rhs context
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
            paramEnd = parseImplicitAnonFuncParamEnd();
            if (paramEnd == null) {
                break;
            }

            paramList.add(paramEnd);
            param = parseIdentifier(ParserRuleContext.IMPLICIT_ANON_FUNC_PARAM);
            param = STNodeFactory.createSimpleNameReferenceNode(param);
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
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_PAREN_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.ANON_FUNC_PARAM_RHS);
                return parseImplicitAnonFuncParamEnd();
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
            case DO_KEYWORD:
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
     * | [ tuple-rest-descriptor ]
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
        openBracket = cloneWithDiagnosticIfListEmpty(memberTypeDesc, openBracket,
                DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC);

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
        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);

        return parseTupleTypeMembers(typeDesc, typeDescList);
    }

    private STNode parseTupleTypeMembers(STNode typeDesc, List<STNode> typeDescList) {
        typeDesc = parseComplexTypeDescriptor(typeDesc, ParserRuleContext.TYPE_DESC_IN_TUPLE, false);

        STNode tupleMemberRhs = parseTypeDescInTupleRhs();
        if (tupleMemberRhs != null) {
            typeDesc = STNodeFactory.createRestDescriptorNode(typeDesc, tupleMemberRhs);
        }

        // Parse the remaining type descs
        while (!isEndOfTypeList(peek().kind)) {
            if (typeDesc.kind == SyntaxKind.REST_TYPE) {
                typeDesc = invalidateTypeDescAfterRestDesc(typeDesc);
                break;
            }

            tupleMemberRhs = parseTupleMemberRhs();
            if (tupleMemberRhs == null) {
                break;
            }

            typeDescList.add(typeDesc);
            typeDescList.add(tupleMemberRhs);
            typeDesc = parseMemberDescriptor();
        }

        typeDescList.add(typeDesc);
        return STNodeFactory.createNodeList(typeDescList);
    }

    private STNode parseMemberDescriptor() {
        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
        STNode tupleMemberRhs = parseTypeDescInTupleRhs();

        if (tupleMemberRhs != null) {
            return STNodeFactory.createRestDescriptorNode(typeDesc, tupleMemberRhs);
        }

        return typeDesc;
    }

    private STNode invalidateTypeDescAfterRestDesc(STNode restDescriptor) {
        while (!isEndOfTypeList(peek().kind)) {
            STNode tupleMemberRhs = parseTupleMemberRhs();
            if (tupleMemberRhs == null) {
                break;
            }

            restDescriptor = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(restDescriptor, tupleMemberRhs, null);
            restDescriptor = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(restDescriptor, parseMemberDescriptor(),
                    DiagnosticErrorCode.ERROR_TYPE_DESC_AFTER_REST_DESCRIPTOR);
        }

        return restDescriptor;
    }

    private STNode parseTupleMemberRhs() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                recover(nextToken, ParserRuleContext.TUPLE_TYPE_MEMBER_RHS);
                return parseTupleMemberRhs();
        }
    }

    private STNode parseTypeDescInTupleRhs() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COMMA_TOKEN:
            case CLOSE_BRACKET_TOKEN:
                return null;
            case ELLIPSIS_TOKEN:
                return parseEllipsis();
            default:
                recover(nextToken, ParserRuleContext.TYPE_DESC_IN_TUPLE_RHS);
                return parseTypeDescInTupleRhs();
        }
    }

    private boolean isEndOfTypeList(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case CLOSE_BRACKET_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case EOF_TOKEN:
            case EQUAL_TOKEN:
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
     * [query-construct-type] query-pipeline select-clause on-conflict-clause?
     * <br/>
     * query-construct-type := table key-specifier | stream | map
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseTableConstructorOrQuery(boolean isRhsExpr, boolean allowActions) {
        startContext(ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION);
        STNode tableOrQueryExpr = parseTableConstructorOrQueryInternal(isRhsExpr, allowActions);
        endContext();
        return tableOrQueryExpr;
    }

    private STNode parseTableConstructorOrQueryInternal(boolean isRhsExpr, boolean allowActions) {
        STNode queryConstructType;
        switch (peek().kind) {
            case FROM_KEYWORD:
                queryConstructType = STNodeFactory.createEmptyNode();
                return parseQueryExprRhs(queryConstructType, isRhsExpr, allowActions);
            case TABLE_KEYWORD:
                STNode tableKeyword = parseTableKeyword();
                return parseTableConstructorOrQuery(tableKeyword, isRhsExpr, allowActions);
            case STREAM_KEYWORD:
            case MAP_KEYWORD:
                STNode streamOrMapKeyword = consume();
                STNode keySpecifier = STNodeFactory.createEmptyNode();
                queryConstructType = parseQueryConstructType(streamOrMapKeyword, keySpecifier);
                return parseQueryExprRhs(queryConstructType, isRhsExpr, allowActions);
            default:
                recover(peek(), ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_START);
                return parseTableConstructorOrQueryInternal(isRhsExpr, allowActions);
        }

    }

    private STNode parseTableConstructorOrQuery(STNode tableKeyword, boolean isRhsExpr, boolean allowActions) {
        STNode keySpecifier;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACKET_TOKEN:
                keySpecifier = STNodeFactory.createEmptyNode();
                return parseTableConstructorExprRhs(tableKeyword, keySpecifier);
            case KEY_KEYWORD:
                keySpecifier = parseKeySpecifier();
                return parseTableConstructorOrQueryRhs(tableKeyword, keySpecifier, isRhsExpr, allowActions);
            case IDENTIFIER_TOKEN:
                if (isKeyKeyword(nextToken)) {
                    keySpecifier = parseKeySpecifier();
                    return parseTableConstructorOrQueryRhs(tableKeyword, keySpecifier, isRhsExpr, allowActions);
                }
                break;
            default:
                break;
        }

        recover(peek(), ParserRuleContext.TABLE_KEYWORD_RHS);
        return parseTableConstructorOrQuery(tableKeyword, isRhsExpr, allowActions);
    }

    private STNode parseTableConstructorOrQueryRhs(STNode tableKeyword, STNode keySpecifier, boolean isRhsExpr,
                                                   boolean allowActions) {
        switch (peek().kind) {
            case FROM_KEYWORD:
                return parseQueryExprRhs(parseQueryConstructType(tableKeyword, keySpecifier), isRhsExpr, allowActions);
            case OPEN_BRACKET_TOKEN:
                return parseTableConstructorExprRhs(tableKeyword, keySpecifier);
            default:
                recover(peek(), ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_RHS);
                return parseTableConstructorOrQueryRhs(tableKeyword, keySpecifier, isRhsExpr, allowActions);
        }
    }

    /**
     * Parse query construct type.
     * <p>
     * <code>query-construct-type := table key-specifier | stream | map</code>
     *
     * @return Parsed node
     */
    private STNode parseQueryConstructType(STNode keyword, STNode keySpecifier) {
        return STNodeFactory.createQueryConstructTypeNode(keyword, keySpecifier);
    }

    /**
     * Parse query action or expression.
     * <p>
     * <code>
     * query-expr-rhs := query-pipeline select-clause
     * query-pipeline select-clause on-conflict-clause?
     * <br/>
     * query-pipeline := from-clause intermediate-clause*
     * </code>
     *
     * @param queryConstructType queryConstructType that precedes this rhs
     * @return Parsed node
     */
    private STNode parseQueryExprRhs(STNode queryConstructType, boolean isRhsExpr, boolean allowActions) {
//        this.tokenReader.startKeywordMode(KeywordMode.QUERY);
        switchContext(ParserRuleContext.QUERY_EXPRESSION);
        STNode fromClause = parseFromClause(isRhsExpr, allowActions);

        List<STNode> clauses = new ArrayList<>();
        STNode intermediateClause;
        STNode selectClause = null;
        while (!isEndOfIntermediateClause(peek().kind)) {
            intermediateClause = parseIntermediateClause(isRhsExpr, allowActions);
            if (intermediateClause == null) {
                break;
            }

            // If there are more clauses after select clause they are add as invalid nodes to the select clause
            if (selectClause != null) {
                selectClause = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(selectClause, intermediateClause,
                        DiagnosticErrorCode.ERROR_MORE_CLAUSES_AFTER_SELECT_CLAUSE);
                continue;
            }

            if (intermediateClause.kind != SyntaxKind.SELECT_CLAUSE) {
                clauses.add(intermediateClause);
                continue;
            }

            selectClause = intermediateClause;

            if (isNestedQueryExpr() || !isValidIntermediateQueryStart(peek().kind)) {
                // Break the loop for,
                // 1. nested query expressions as remaining clauses belong to the parent.
                // 2. next token not being an intermediate-clause start as that token could belong to the parent node.
                break;
            }
        }

        if (peek().kind == SyntaxKind.DO_KEYWORD) {
            STNode intermediateClauses = STNodeFactory.createNodeList(clauses);
            STNode queryPipeline = STNodeFactory.createQueryPipelineNode(fromClause, intermediateClauses);
            return parseQueryAction(queryConstructType, queryPipeline, selectClause);
        }

        if (selectClause == null) {
            STNode selectKeyword = SyntaxErrors.createMissingToken(SyntaxKind.SELECT_KEYWORD);
            STNode expr = STNodeFactory
                    .createSimpleNameReferenceNode(SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN));
            selectClause = STNodeFactory.createSelectClauseNode(selectKeyword, expr);

            // Now we need to attach the diagnostic to the last intermediate clause.
            // If there are no intermediate clauses, then attach to the from clause.
            if (clauses.isEmpty()) {
                fromClause = SyntaxErrors.addDiagnostic(fromClause, DiagnosticErrorCode.ERROR_MISSING_SELECT_CLAUSE);
            } else {
                int lastIndex = clauses.size() - 1;
                STNode intClauseWithDiagnostic = SyntaxErrors.addDiagnostic(clauses.get(lastIndex),
                        DiagnosticErrorCode.ERROR_MISSING_SELECT_CLAUSE);
                clauses.set(lastIndex, intClauseWithDiagnostic);
            }
        }

        STNode intermediateClauses = STNodeFactory.createNodeList(clauses);
        STNode queryPipeline = STNodeFactory.createQueryPipelineNode(fromClause, intermediateClauses);
        STNode onConflictClause = parseOnConflictClause(isRhsExpr);

//        this.tokenReader.endKeywordMode();
        return STNodeFactory.createQueryExpressionNode(queryConstructType, queryPipeline, selectClause,
                onConflictClause);
    }

    /**
     * Check whether currently parsing query expr is a nested query expression.
     *
     * @return <code>true</code> if currently parsing query-expr is a nested query-expr. <code>false</code> otherwise.
     */
    private boolean isNestedQueryExpr() {
        return Collections.frequency(this.errorHandler.getContextStack(), ParserRuleContext.QUERY_EXPRESSION) > 1;
    }

    private boolean isValidIntermediateQueryStart(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case FROM_KEYWORD:
            case WHERE_KEYWORD:
            case LET_KEYWORD:
            case SELECT_KEYWORD:
            case JOIN_KEYWORD:
            case OUTER_KEYWORD:
            case ORDER_KEYWORD:
            case BY_KEYWORD:
            case ASCENDING_KEYWORD:
            case DESCENDING_KEYWORD:
            case LIMIT_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse an intermediate clause.
     * <p>
     * <code>
     * intermediate-clause := from-clause | where-clause | let-clause | join-clause | limit-clause | order-by-clause
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseIntermediateClause(boolean isRhsExpr, boolean allowActions) {
        switch (peek().kind) {
            case FROM_KEYWORD:
                return parseFromClause(isRhsExpr, allowActions);
            case WHERE_KEYWORD:
                return parseWhereClause(isRhsExpr);
            case LET_KEYWORD:
                return parseLetClause(isRhsExpr, allowActions);
            case SELECT_KEYWORD:
                return parseSelectClause(isRhsExpr, allowActions);
            case JOIN_KEYWORD:
            case OUTER_KEYWORD:
                return parseJoinClause(isRhsExpr);
            case ORDER_KEYWORD:
            case BY_KEYWORD:
            case ASCENDING_KEYWORD:
            case DESCENDING_KEYWORD:
                return parseOrderByClause(isRhsExpr);
            case LIMIT_KEYWORD:
                return parseLimitClause(isRhsExpr);
            case DO_KEYWORD:
            case SEMICOLON_TOKEN:
            case ON_KEYWORD:
            case CONFLICT_KEYWORD:
                return null;
            default:
                recover(peek(), ParserRuleContext.QUERY_PIPELINE_RHS);
                return parseIntermediateClause(isRhsExpr, allowActions);
        }
    }

    /**
     * Parse join-keyword.
     *
     * @return Join-keyword node
     */
    private STNode parseJoinKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.JOIN_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.JOIN_KEYWORD);
            return parseJoinKeyword();
        }
    }

    /**
     * Parse equals keyword.
     *
     * @return Parsed node
     */
    private STNode parseEqualsKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.EQUALS_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.EQUALS_KEYWORD);
            return parseEqualsKeyword();
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
            case DOCUMENTATION_STRING:
            case PRIVATE_KEYWORD:
            case RETURNS_KEYWORD:
            case SERVICE_KEYWORD:
            case TYPE_KEYWORD:
            case CONST_KEYWORD:
            case FINAL_KEYWORD:
            case DO_KEYWORD:
            case ON_KEYWORD:
            case CONFLICT_KEYWORD:
                return true;
            default:
                return isValidExprRhsStart(tokenKind, SyntaxKind.NONE);
        }
    }

    /**
     * Parse from clause.
     * <p>
     * <code>from-clause := from typed-binding-pattern in expression</code>
     *
     * @return Parsed node
     */
    private STNode parseFromClause(boolean isRhsExpr, boolean allowActions) {
        STNode fromKeyword = parseFromKeyword();
        STNode typedBindingPattern = parseTypedBindingPattern(ParserRuleContext.FROM_CLAUSE);
        STNode inKeyword = parseInKeyword();
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, allowActions);
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
            recover(token, ParserRuleContext.FROM_KEYWORD);
            return parseFromKeyword();
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
            recover(token, ParserRuleContext.WHERE_KEYWORD);
            return parseWhereKeyword();
        }
    }

    /**
     * Parse limit-keyword.
     *
     * @return limit-keyword node
     */
    private STNode parseLimitKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.LIMIT_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.LIMIT_KEYWORD);
            return parseLimitKeyword();
        }
    }

    /**
     * Parse let clause.
     * <p>
     * <code>let-clause := let let-var-decl [, let-var-decl]* </code>
     *
     * @return Parsed node
     */
    private STNode parseLetClause(boolean isRhsExpr, boolean allowActions) {
        STNode letKeyword = parseLetKeyword();
        STNode letVarDeclarations = parseLetVarDeclarations(ParserRuleContext.LET_CLAUSE_LET_VAR_DECL, isRhsExpr,
                allowActions);

        // If the variable declaration list is empty, clone the letKeyword token with the given diagnostic.
        letKeyword = cloneWithDiagnosticIfListEmpty(letVarDeclarations, letKeyword,
                DiagnosticErrorCode.ERROR_MISSING_LET_VARIABLE_DECLARATION);

        return STNodeFactory.createLetClauseNode(letKeyword, letVarDeclarations);
    }

    /**
     * Parse order-keyword.
     *
     * @return Order-keyword node
     */
    private STNode parseOrderKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ORDER_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.ORDER_KEYWORD);
            return parseOrderKeyword();
        }
    }

    /**
     * Parse by-keyword.
     *
     * @return By-keyword node
     */
    private STNode parseByKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.BY_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.BY_KEYWORD);
            return parseByKeyword();
        }
    }

    /**
     * Parse order by clause.
     * <p>
     * <code>order-by-clause := order by order-key-list
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseOrderByClause(boolean isRhsExpr) {
        STNode orderKeyword = parseOrderKeyword();
        STNode byKeyword = parseByKeyword();
        STNode orderKeys = parseOrderKeyList(isRhsExpr);
        byKeyword = cloneWithDiagnosticIfListEmpty(orderKeys, byKeyword, DiagnosticErrorCode.ERROR_MISSING_ORDER_KEY);
        return STNodeFactory.createOrderByClauseNode(orderKeyword, byKeyword, orderKeys);
    }

    /**
     * Parse order key.
     * <p>
     * <code>order-key-list := order-key [, order-key]*</code>
     *
     * @return Parsed node
     */
    private STNode parseOrderKeyList(boolean isRhsExpr) {
        startContext(ParserRuleContext.ORDER_KEY_LIST);
        List<STNode> orderKeys = new ArrayList<>();
        STToken nextToken = peek();

        if (isEndOfOrderKeys(nextToken.kind)) {
            endContext();
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first order key, that has no leading comma
        STNode orderKey = parseOrderKey(isRhsExpr);
        orderKeys.add(orderKey);

        // Parse the remaining order keys
        nextToken = peek();
        STNode orderKeyListMemberEnd;
        while (!isEndOfOrderKeys(nextToken.kind)) {
            orderKeyListMemberEnd = parseOrderKeyListMemberEnd();
            if (orderKeyListMemberEnd == null) {
                break;
            }
            orderKeys.add(orderKeyListMemberEnd);
            orderKey = parseOrderKey(isRhsExpr);
            orderKeys.add(orderKey);
            nextToken = peek();
        }

        endContext();
        return STNodeFactory.createNodeList(orderKeys);
    }

    private boolean isEndOfOrderKeys(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case COMMA_TOKEN:
            case ASCENDING_KEYWORD:
            case DESCENDING_KEYWORD:
                return false;
            case SEMICOLON_TOKEN:
            case EOF_TOKEN:
                return true;
            default:
                return isQueryClauseStartToken(tokenKind);
        }
    }

    private boolean isQueryClauseStartToken(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case SELECT_KEYWORD:
            case LET_KEYWORD:
            case WHERE_KEYWORD:
            case OUTER_KEYWORD:
            case JOIN_KEYWORD:
            case ORDER_KEYWORD:
            case DO_KEYWORD:
            case FROM_KEYWORD:
            case LIMIT_KEYWORD:
                return true;
            default:
                return false;
        }
    }

    private STNode parseOrderKeyListMemberEnd() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COMMA_TOKEN:
                return parseComma();
            case EOF_TOKEN:
                return null;
            default:
                if (isQueryClauseStartToken(nextToken.kind)) {
                    // null marks the end of order keys
                    return null;
                }

                recover(peek(), ParserRuleContext.ORDER_KEY_LIST_END);
                return parseOrderKeyListMemberEnd();
        }
    }

    /**
     * Parse order key.
     * <p>
     * <code>order-key := expression (ascending | descending)?</code>
     *
     * @return Parsed node
     */
    private STNode parseOrderKey(boolean isRhsExpr) {
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);

        STNode orderDirection;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case ASCENDING_KEYWORD:
            case DESCENDING_KEYWORD:
                orderDirection = consume();
                break;
            default:
                orderDirection = STNodeFactory.createEmptyNode();
        }

        return STNodeFactory.createOrderKeyNode(expression, orderDirection);
    }

    /**
     * Parse select clause.
     * <p>
     * <code>select-clause := select expression</code>
     *
     * @return Parsed node
     */
    private STNode parseSelectClause(boolean isRhsExpr, boolean allowActions) {
        startContext(ParserRuleContext.SELECT_CLAUSE);
        STNode selectKeyword = parseSelectKeyword();
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, allowActions);
        endContext();
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
            recover(token, ParserRuleContext.SELECT_KEYWORD);
            return parseSelectKeyword();
        }
    }

    /**
     * Parse on-conflict clause.
     * <p>
     * <code>
     * onConflictClause := on conflict expression
     * </code>
     *
     * @return On conflict clause node
     */
    private STNode parseOnConflictClause(boolean isRhsExpr) {
        STToken nextToken = peek();
        if (nextToken.kind != SyntaxKind.ON_KEYWORD && nextToken.kind != SyntaxKind.CONFLICT_KEYWORD) {
            return STNodeFactory.createEmptyNode();
        }

        startContext(ParserRuleContext.ON_CONFLICT_CLAUSE);
        STNode onKeyword = parseOnKeyword();
        STNode conflictKeyword = parseConflictKeyword();
        endContext();
        STNode expr = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        return STNodeFactory.createOnConflictClauseNode(onKeyword, conflictKeyword, expr);
    }

    /**
     * Parse conflict keyword.
     *
     * @return Conflict keyword node
     */
    private STNode parseConflictKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CONFLICT_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.CONFLICT_KEYWORD);
            return parseConflictKeyword();
        }
    }

    /**
     * Parse limit clause.
     * <p>
     * <code>limitClause := limit expression</code>
     *
     * @return Limit expression node
     */
    private STNode parseLimitClause(boolean isRhsExpr) {
        STNode limitKeyword = parseLimitKeyword();
        // allow-actions flag is always false, since there will not be any actions
        // within the where-clause, due to the precedence.
        STNode expr = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        return STNodeFactory.createLimitClauseNode(limitKeyword, expr);
    }

    /**
     * Parse join clause.
     * <p>
     * <code>
     * join-clause := (join-var-decl | outer-join-var-decl) in expression on-clause
     * <br/>
     * join-var-decl := join (typeName | var) bindingPattern
     * <br/>
     * outer-join-var-decl := outer join var binding-pattern
     * </code>
     *
     * @return Join clause
     */
    private STNode parseJoinClause(boolean isRhsExpr) {
        startContext(ParserRuleContext.JOIN_CLAUSE);
        STNode outerKeyword;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.OUTER_KEYWORD) {
            outerKeyword = consume();
        } else {
            outerKeyword = STNodeFactory.createEmptyNode();
        }

        STNode joinKeyword = parseJoinKeyword();
        STNode typedBindingPattern = parseTypedBindingPattern(ParserRuleContext.JOIN_CLAUSE);
        STNode inKeyword = parseInKeyword();
        // allow-actions flag is always false, since there will not be any actions
        // within the from-clause, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        endContext();
        STNode onCondition = parseOnClause(isRhsExpr);
        return STNodeFactory.createJoinClauseNode(outerKeyword, joinKeyword, typedBindingPattern, inKeyword, expression,
                onCondition);
    }

    /**
     * Parse on clause.
     * <p>
     * <code>on clause := `on` expression `equals` expression</code>
     *
     * @return On clause node
     */
    private STNode parseOnClause(boolean isRhsExpr) {
        STToken nextToken = peek();
        if (isQueryClauseStartToken(nextToken.kind)) {
            return createMissingOnClauseNode();
        }

        startContext(ParserRuleContext.ON_CLAUSE);
        STNode onKeyword = parseOnKeyword();
        STNode lhsExpression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        STNode equalsKeyword = parseEqualsKeyword();
        endContext();
        STNode rhsExpression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
        return STNodeFactory.createOnClauseNode(onKeyword, lhsExpression, equalsKeyword, rhsExpression);
    }

    private STNode createMissingOnClauseNode() {
        STNode onKeyword = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.ON_KEYWORD,
                DiagnosticErrorCode.ERROR_MISSING_ON_KEYWORD);
        STNode identifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER);
        STNode equalsKeyword = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.EQUALS_KEYWORD,
                DiagnosticErrorCode.ERROR_MISSING_EQUALS_KEYWORD);

        STNode lhsExpression = STNodeFactory.createSimpleNameReferenceNode(identifier);
        STNode rhsExpression = STNodeFactory.createSimpleNameReferenceNode(identifier);
        return STNodeFactory.createOnClauseNode(onKeyword, lhsExpression, equalsKeyword, rhsExpression);
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

        // Validate expression or action in start action
        switch (expr.kind) {
            case FUNCTION_CALL:
            case METHOD_CALL:
            case REMOTE_METHOD_CALL_ACTION:
                break;
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case FIELD_ACCESS:
            case ASYNC_SEND_ACTION:
                expr = generateValidExprForStartAction(expr);
                break;
            default:
                startKeyword = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(startKeyword, expr,
                        DiagnosticErrorCode.ERROR_INVALID_EXPRESSION_IN_START_ACTION);
                STNode funcName = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
                funcName = STNodeFactory.createSimpleNameReferenceNode(funcName);
                STNode openParenToken = SyntaxErrors.createMissingToken(SyntaxKind.OPEN_PAREN_TOKEN);
                STNode closeParenToken = SyntaxErrors.createMissingToken(SyntaxKind.CLOSE_PAREN_TOKEN);
                expr = STNodeFactory.createFunctionCallExpressionNode(funcName, openParenToken,
                        STNodeFactory.createEmptyNodeList(), closeParenToken);
                break;
        }

        return STNodeFactory.createStartActionNode(getAnnotations(annots), startKeyword, expr);
    }

    private STNode generateValidExprForStartAction(STNode expr) {
        STNode openParenToken = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.OPEN_PAREN_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_OPEN_PAREN_TOKEN);
        STNode arguments = STNodeFactory.createEmptyNodeList();
        STNode closeParenToken = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.CLOSE_PAREN_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_CLOSE_PAREN_TOKEN);

        switch (expr.kind) {
            case FIELD_ACCESS:
                STFieldAccessExpressionNode fieldAccessExpr = (STFieldAccessExpressionNode) expr;
                return STNodeFactory.createMethodCallExpressionNode(fieldAccessExpr.expression,
                        fieldAccessExpr.dotToken, fieldAccessExpr.fieldName, openParenToken, arguments,
                        closeParenToken);
            case ASYNC_SEND_ACTION:
                STAsyncSendActionNode asyncSendAction = (STAsyncSendActionNode) expr;
                return STNodeFactory.createRemoteMethodCallActionNode(asyncSendAction.expression,
                        asyncSendAction.rightArrowToken, asyncSendAction.peerWorker, openParenToken, arguments,
                        closeParenToken);
            default: // QualifiedNameRef or SimpleNameRef
                return STNodeFactory.createFunctionCallExpressionNode(expr, openParenToken, arguments, closeParenToken);
        }
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
            recover(token, ParserRuleContext.START_KEYWORD);
            return parseStartKeyword();
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
            recover(token, ParserRuleContext.FLUSH_KEYWORD);
            return parseFlushKeyword();
        }
    }

    /**
     * Parse peer worker.
     * <p>
     * <code>peer-worker := worker-name | function</code>
     *
     * @return peer worker name node
     */
    private STNode parseOptionalPeerWorkerName() {
        STToken token = peek();
        switch (token.kind) {
            case IDENTIFIER_TOKEN:
            case FUNCTION_KEYWORD:
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
    private STNode parseIntersectionTypeDescriptor(STNode leftTypeDesc, ParserRuleContext context,
                                                   boolean isTypedBindingPattern) {
        // we come here only after seeing & token hence consume.
        STNode bitwiseAndToken = consume();
        STNode rightTypeDesc = parseTypeDescriptorInternal(new ArrayList<>(), context, isTypedBindingPattern, false,
                TypePrecedence.INTERSECTION);
        return mergeTypesWithIntersection(leftTypeDesc, bitwiseAndToken, rightTypeDesc);
    }

    /**
     * Creates an intersection type descriptor after validating lhs and rhs types.
     * <p>
     * <i>Note: Since type precedence and associativity are not taken into account here,
     * this method should not be called directly when types are unknown.
     * <br/>
     * Call {@link #mergeTypesWithIntersection(STNode, STNode, STNode)} instead</i>
     *
     * @param leftTypeDesc    lhs type
     * @param bitwiseAndToken bitwise-and token
     * @param rightTypeDesc   rhs type
     * @return an IntersectionTypeDescriptorNode
     */
    private STNode createIntersectionTypeDesc(STNode leftTypeDesc, STNode bitwiseAndToken, STNode rightTypeDesc) {
        leftTypeDesc = validateForUsageOfVar(leftTypeDesc);
        rightTypeDesc = validateForUsageOfVar(rightTypeDesc);
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
        STNode simpleContExpr = parseSimpleConstExpr();
        return STNodeFactory.createSingletonTypeDescriptorNode(simpleContExpr);
    }

    // TODO: Fix this properly
    private STNode parseSignedIntOrFloat() {
        STNode operator = parseUnaryOperator();
        STNode literal;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                literal = parseBasicLiteral();
                break;
            default: // decimal integer literal
                literal = parseDecimalIntLiteral(ParserRuleContext.DECIMAL_INTEGER_LITERAL_TOKEN);
                literal = STNodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, literal);
        }
        return STNodeFactory.createUnaryExpressionNode(operator, literal);
    }

    private static boolean isSingletonTypeDescStart(SyntaxKind tokenKind, STToken nextNextToken) {
        switch (tokenKind) {
            case STRING_LITERAL_TOKEN:
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
                return true;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return isIntOrFloat(nextNextToken);
            default:
                return false;
        }
    }

    static boolean isIntOrFloat(STToken token) {
        switch (token.kind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether the parser reached to a valid expression start.
     *
     * @param nextTokenKind  Kind of the next immediate token.
     * @param nextTokenIndex Index to the next token.
     * @return <code>true</code> if this is a start of a valid expression. <code>false</code> otherwise
     */
    private boolean isValidExpressionStart(SyntaxKind nextTokenKind, int nextTokenIndex) {
        nextTokenIndex++;
        switch (nextTokenKind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                SyntaxKind nextNextTokenKind = peek(nextTokenIndex).kind;
                if (nextNextTokenKind == SyntaxKind.PIPE_TOKEN || nextNextTokenKind == SyntaxKind.BITWISE_AND_TOKEN) {
                    // e.g. -2|0 t1;
                    nextTokenIndex++;
                    return isValidExpressionStart(peek(nextTokenIndex).kind, nextTokenIndex);
                }

                return nextNextTokenKind == SyntaxKind.SEMICOLON_TOKEN ||
                        nextNextTokenKind == SyntaxKind.COMMA_TOKEN ||
                        nextNextTokenKind == SyntaxKind.CLOSE_BRACKET_TOKEN ||
                        isValidExprRhsStart(nextNextTokenKind, SyntaxKind.SIMPLE_NAME_REFERENCE);
            case IDENTIFIER_TOKEN:
                return isValidExprRhsStart(peek(nextTokenIndex).kind, SyntaxKind.SIMPLE_NAME_REFERENCE);
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
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
                return true;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return isValidExpressionStart(peek(nextTokenIndex).kind, nextTokenIndex);
            case TABLE_KEYWORD:
            case MAP_KEYWORD:
                return peek(nextTokenIndex).kind == SyntaxKind.FROM_KEYWORD;
            case STREAM_KEYWORD:
                STToken nextNextToken = peek(nextTokenIndex);
                return nextNextToken.kind == SyntaxKind.KEY_KEYWORD ||
                        nextNextToken.kind == SyntaxKind.OPEN_BRACKET_TOKEN ||
                        nextNextToken.kind == SyntaxKind.FROM_KEYWORD;
            case ERROR_KEYWORD:
                return peek(nextTokenIndex).kind == SyntaxKind.OPEN_PAREN_TOKEN;
            case XML_KEYWORD:
            case STRING_KEYWORD:
            case RE_KEYWORD:
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
     * <code>peer-worker := worker-name | function</code>
     *
     * @return peer worker name node
     */
    private STNode parsePeerWorkerName() {
        STToken token = peek();
        switch (token.kind) {
            case IDENTIFIER_TOKEN:
            case FUNCTION_KEYWORD:
                return STNodeFactory.createSimpleNameReferenceNode(consume());
            default:
                recover(token, ParserRuleContext.PEER_WORKER_NAME);
                return parsePeerWorkerName();
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
            recover(token, ParserRuleContext.SYNC_SEND_TOKEN);
            return parseSyncSendToken();
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
        switch (peek().kind) {
            case FUNCTION_KEYWORD:
            case IDENTIFIER_TOKEN:
                return parsePeerWorkerName();
            case OPEN_BRACE_TOKEN:
                return parseMultipleReceiveWorkers();
            default:
                recover(peek(), ParserRuleContext.RECEIVE_WORKERS);
                return parseReceiveWorkers();
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

        openBrace = cloneWithDiagnosticIfListEmpty(receiveFields, openBrace,
                DiagnosticErrorCode.ERROR_MISSING_RECEIVE_FIELD_IN_RECEIVE_ACTION);
        return STNodeFactory.createReceiveFieldsNode(openBrace, receiveFields, closeBrace);
    }

    private STNode parseReceiveFields() {
        List<STNode> receiveFields = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list
        if (isEndOfReceiveFields(nextToken.kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first receive field, that has no leading comma
        STNode receiveField = parseReceiveField();
        receiveFields.add(receiveField);

        // Parse the remaining receive fields
        nextToken = peek();
        STNode recieveFieldEnd;
        while (!isEndOfReceiveFields(nextToken.kind)) {
            recieveFieldEnd = parseReceiveFieldEnd();
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
            case CLOSE_BRACE_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseReceiveFieldEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.RECEIVE_FIELD_END);
                return parseReceiveFieldEnd();
        }
    }

    /**
     * Parse receive field.
     * <p>
     * <code>receive-field := peer-worker | field-name : peer-worker</code>
     *
     * @return Receiver field node
     */
    private STNode parseReceiveField() {
        switch (peek().kind) {
            case FUNCTION_KEYWORD:
                STNode functionKeyword = consume();
                return STNodeFactory.createSimpleNameReferenceNode(functionKeyword);
            case IDENTIFIER_TOKEN:
                STNode identifier = parseIdentifier(ParserRuleContext.RECEIVE_FIELD_NAME);
                return createQualifiedReceiveField(identifier);
            default:
                recover(peek(), ParserRuleContext.RECEIVE_FIELD);
                return parseReceiveField();
        }
    }

    private STNode createQualifiedReceiveField(STNode identifier) {
        if (peek().kind != SyntaxKind.COLON_TOKEN) {
            return identifier;
        }

        STNode colon = parseColon();
        STNode peerWorker = parsePeerWorkerName();
        return createQualifiedNameReferenceNode(identifier, colon, peerWorker);
    }

    /**
     * Parse left arrow (<-) token.
     *
     * @return left arrow token
     */
    private STNode parseLeftArrowToken() {
        STToken token = peek();
        if (token.kind == SyntaxKind.LEFT_ARROW_TOKEN) {
            return consume();
        } else {
            recover(token, ParserRuleContext.LEFT_ARROW_TOKEN);
            return parseLeftArrowToken();
        }
    }

    /**
     * Parse signed right shift token (>>).
     * This method should only be called by seeing a `DOUBLE_GT_TOKEN` or
     * by seeing a `GT_TOKEN` followed by a `GT_TOKEN`
     *
     * @return Parsed node
     */
    private STNode parseSignedRightShiftToken() {
        STNode firstToken = consume();
        if (firstToken.kind == SyntaxKind.DOUBLE_GT_TOKEN) {
            return firstToken;
        }

        STToken endLGToken = consume();
        STNode doubleGTToken = STNodeFactory.createToken(SyntaxKind.DOUBLE_GT_TOKEN, firstToken.leadingMinutiae(),
                endLGToken.trailingMinutiae());

        if (hasTrailingMinutiae(firstToken)) {
            doubleGTToken = SyntaxErrors.addDiagnostic(doubleGTToken,
                    DiagnosticErrorCode.ERROR_NO_WHITESPACES_ALLOWED_IN_RIGHT_SHIFT_OP);
        }
        return doubleGTToken;
    }

    /**
     * Parse unsigned right shift token (>>>).
     * This method should only be called by seeing a `TRIPPLE_GT_TOKEN` or
     * by seeing a `GT_TOKEN` followed by two `GT_TOKEN`s
     *
     * @return Parsed node
     */
    private STNode parseUnsignedRightShiftToken() {
        STNode firstToken = consume();
        if (firstToken.kind == SyntaxKind.TRIPPLE_GT_TOKEN) {
            return firstToken;
        }

        STNode middleGTToken = consume();
        STNode endLGToken = consume();
        STNode unsignedRightShiftToken = STNodeFactory.createToken(SyntaxKind.TRIPPLE_GT_TOKEN,
                firstToken.leadingMinutiae(), endLGToken.trailingMinutiae());

        boolean validOpenGTToken = !hasTrailingMinutiae(firstToken);
        boolean validMiddleGTToken = !hasTrailingMinutiae(middleGTToken);
        if (validOpenGTToken && validMiddleGTToken) {
            return unsignedRightShiftToken;
        }

        unsignedRightShiftToken = SyntaxErrors.addDiagnostic(unsignedRightShiftToken,
                DiagnosticErrorCode.ERROR_NO_WHITESPACES_ALLOWED_IN_UNSIGNED_RIGHT_SHIFT_OP);
        return unsignedRightShiftToken;
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
            recover(token, ParserRuleContext.WAIT_KEYWORD);
            return parseWaitKeyword();
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
            endContext();
            STNode waitFutureExprs = STNodeFactory
                    .createSimpleNameReferenceNode(STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN));
            waitFutureExprs = SyntaxErrors.addDiagnostic(waitFutureExprs,
                    DiagnosticErrorCode.ERROR_MISSING_WAIT_FUTURE_EXPRESSION);
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
            waitFutureExprEnd = parseWaitFutureExprEnd();
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
            case OPEN_BRACE_TOKEN:
                return true;
            case PIPE_TOKEN:
            default:
                return false;
        }
    }

    private STNode parseWaitFutureExpr() {
        STNode waitFutureExpr = parseActionOrExpression();
        if (waitFutureExpr.kind == SyntaxKind.MAPPING_CONSTRUCTOR) {
            waitFutureExpr = SyntaxErrors.addDiagnostic(waitFutureExpr,
                    DiagnosticErrorCode.ERROR_MAPPING_CONSTRUCTOR_EXPR_AS_A_WAIT_EXPR);
        } else if (isAction(waitFutureExpr)) {
            waitFutureExpr =
                    SyntaxErrors.addDiagnostic(waitFutureExpr, DiagnosticErrorCode.ERROR_ACTION_AS_A_WAIT_EXPR);
        }
        return waitFutureExpr;
    }

    private STNode parseWaitFutureExprEnd() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case PIPE_TOKEN:
                return parsePipeToken();
            default:
                if (isEndOfWaitFutureExprList(nextToken.kind) || !isValidExpressionStart(nextToken.kind, 1)) {
                    return null;
                }

                recover(peek(), ParserRuleContext.WAIT_FUTURE_EXPR_END);
                return parseWaitFutureExprEnd();
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

        openBrace = cloneWithDiagnosticIfListEmpty(waitFields, openBrace,
                DiagnosticErrorCode.ERROR_MISSING_WAIT_FIELD_IN_WAIT_ACTION);
        STNode waitFieldsNode = STNodeFactory.createWaitFieldsListNode(openBrace, waitFields, closeBrace);
        return STNodeFactory.createWaitActionNode(waitKeyword, waitFieldsNode);
    }

    private STNode parseWaitFields() {
        List<STNode> waitFields = new ArrayList<>();
        STToken nextToken = peek();

        // Return an empty list
        if (isEndOfWaitFields(nextToken.kind)) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first receive field, that has no leading comma
        STNode waitField = parseWaitField();
        waitFields.add(waitField);

        // Parse the remaining receive fields
        nextToken = peek();
        STNode waitFieldEnd;
        while (!isEndOfWaitFields(nextToken.kind)) {
            waitFieldEnd = parseWaitFieldEnd();
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

    private boolean isEndOfWaitFields(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseWaitFieldEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.WAIT_FIELD_END);
                return parseWaitFieldEnd();
        }
    }

    /**
     * Parse wait field.
     * <p>
     * <code>wait-field := variable-name | field-name : wait-future-expr</code>
     *
     * @return Receiver field node
     */
    private STNode parseWaitField() {
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                STNode identifier = parseIdentifier(ParserRuleContext.WAIT_FIELD_NAME);
                identifier = STNodeFactory.createSimpleNameReferenceNode(identifier);
                return createQualifiedWaitField(identifier);
            default:
                recover(peek(), ParserRuleContext.WAIT_FIELD_NAME);
                return parseWaitField();
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
    private STNode parseAnnotAccessExpression(STNode lhsExpr, boolean isInConditionalExpr) {
        STNode annotAccessToken = parseAnnotChainingToken();
        STNode annotTagReference = parseFieldAccessIdentifier(isInConditionalExpr);
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
            recover(token, ParserRuleContext.ANNOT_CHAINING_TOKEN);
            return parseAnnotChainingToken();
        }
    }

    /**
     * Parse field access identifier.
     * <p>
     * <code>field-access-identifier := qualified-identifier | identifier</code>
     *
     * @return Parsed node
     */
    private STNode parseFieldAccessIdentifier(boolean isInConditionalExpr) {
        STToken nextToken = peek();
        if (!isPredeclaredIdentifier(nextToken.kind)) {
            // foo.<cursor>
            STNode identifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                    DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER);
            return parseQualifiedIdentifier(identifier, isInConditionalExpr);
        }

        return parseQualifiedIdentifier(ParserRuleContext.FIELD_ACCESS_IDENTIFIER, isInConditionalExpr);
    }

    /**
     * Parse query action.
     * <p>
     * <code>query-action := query-pipeline do-clause
     * <br/>
     * do-clause := do block-stmt
     * </code>
     *
     * @param queryConstructType Query construct type. This is only for validation
     * @param queryPipeline      Query pipeline
     * @param selectClause       Select clause if any This is only for validation.
     * @return Query action node
     */
    private STNode parseQueryAction(STNode queryConstructType, STNode queryPipeline, STNode selectClause) {
        if (queryConstructType != null) {
            queryPipeline = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(queryPipeline, queryConstructType,
                    DiagnosticErrorCode.ERROR_QUERY_CONSTRUCT_TYPE_IN_QUERY_ACTION);
        }
        if (selectClause != null) {
            queryPipeline = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(queryPipeline, selectClause,
                    DiagnosticErrorCode.ERROR_SELECT_CLAUSE_IN_QUERY_ACTION);
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
            recover(token, ParserRuleContext.DO_KEYWORD);
            return parseDoKeyword();
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
    private STNode parseOptionalFieldAccessExpression(STNode lhsExpr, boolean isInConditionalExpr) {
        STNode optionalFieldAccessToken = parseOptionalChainingToken();
        STNode fieldName = parseFieldAccessIdentifier(isInConditionalExpr);
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
            recover(token, ParserRuleContext.OPTIONAL_CHAINING_TOKEN);
            return parseOptionalChainingToken();
        }
    }

    /**
     * Parse conditional expression.
     * <p>
     * <code>conditional-expr := expression ? expression : expression</code>
     *
     * @param lhsExpr             Preceding expression of the question mark
     * @param isInConditionalExpr whether calling from a conditional-expr
     * @return Parsed node
     */
    private STNode parseConditionalExpression(STNode lhsExpr, boolean isInConditionalExpr) {
        startContext(ParserRuleContext.CONDITIONAL_EXPRESSION);
        STNode questionMark = parseQuestionMark();
        // start parsing middle-expr, by giving higher-precedence to the middle-expr, over currently
        // parsing conditional expr. That is done by lowering the current precedence.
        STNode middleExpr = parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, true, false, true);

        // There are special cases when the nextToken is not `:`. Any expression that contains qualified name ref at
        // the beginning or at the end, is a possible match for conditionalExpr.
        if (peek().kind != SyntaxKind.COLON_TOKEN) {
            if (middleExpr.kind == SyntaxKind.CONDITIONAL_EXPRESSION) {
                STConditionalExpressionNode innerConditionalExpr = (STConditionalExpressionNode) middleExpr;
                STNode innerMiddleExpr = innerConditionalExpr.middleExpression;

                // eg1 `a? b? c:d:e` reaches here as `a? (b? (c:d) : e)`
                // eg2 `a? b? c:d.e:f` reaches here as `a? (b? (c:d.e) : f)`
                // eg3 `a? b? c.d:e:f` reaches here as `a? (b? (c.d:e) : f)`
                STNode rightMostQNameRef = ConditionalExprResolver.getQualifiedNameRefNode(innerMiddleExpr, false);
                if (rightMostQNameRef != null) {
                    middleExpr = generateConditionalExprForRightMost(innerConditionalExpr.lhsExpression,
                            innerConditionalExpr.questionMarkToken, innerMiddleExpr, rightMostQNameRef);
                    endContext();
                    return STNodeFactory.createConditionalExpressionNode(lhsExpr, questionMark, middleExpr,
                            innerConditionalExpr.colonToken, innerConditionalExpr.endExpression);
                }

                STNode leftMostQNameRef = ConditionalExprResolver.getQualifiedNameRefNode(innerMiddleExpr, true);
                if (leftMostQNameRef != null) {
                    middleExpr = generateConditionalExprForLeftMost(innerConditionalExpr.lhsExpression,
                            innerConditionalExpr.questionMarkToken, innerMiddleExpr, leftMostQNameRef);
                    endContext();
                    return STNodeFactory.createConditionalExpressionNode(lhsExpr, questionMark, middleExpr,
                            innerConditionalExpr.colonToken, innerConditionalExpr.endExpression);
                }
            }

            // eg1. `a ? b : c`, since `b:c` matches to var-ref due to expr-precedence.
            // eg2. `a ? b : c()`, since `b:c()` matches to func-call due to expr-precedence.
            // eg3. `a ? <TargetType> b : c` since `b:c` matches to var-ref due to expr-precedence.
            STNode rightMostQNameRef = ConditionalExprResolver.getQualifiedNameRefNode(middleExpr, false);
            if (rightMostQNameRef != null) {
                endContext();
                return generateConditionalExprForRightMost(lhsExpr, questionMark, middleExpr, rightMostQNameRef);
            }

            STNode leftMostQNameRef = ConditionalExprResolver.getQualifiedNameRefNode(middleExpr, true);
            if (leftMostQNameRef != null) {
                endContext();
                return generateConditionalExprForLeftMost(lhsExpr, questionMark, middleExpr, leftMostQNameRef);
            }
        }

        return parseConditionalExprRhs(lhsExpr, questionMark, middleExpr, isInConditionalExpr);
    }

    private STNode generateConditionalExprForRightMost(STNode lhsExpr, STNode questionMark, STNode middleExpr,
                                                       STNode rightMostQualifiedNameRef) {
        STQualifiedNameReferenceNode qualifiedNameRef =
                (STQualifiedNameReferenceNode) rightMostQualifiedNameRef;
        STNode endExpr = STNodeFactory.createSimpleNameReferenceNode(qualifiedNameRef.identifier);

        STNode simpleNameRef =
                ConditionalExprResolver.getSimpleNameRefNode(qualifiedNameRef.modulePrefix);
        middleExpr = middleExpr.replace(rightMostQualifiedNameRef, simpleNameRef);
        return STNodeFactory.createConditionalExpressionNode(lhsExpr, questionMark, middleExpr, qualifiedNameRef.colon,
                endExpr);
    }

    private STNode generateConditionalExprForLeftMost(STNode lhsExpr, STNode questionMark, STNode middleExpr,
                                                      STNode leftMostQualifiedNameRef) {
        STQualifiedNameReferenceNode qualifiedNameRef = (STQualifiedNameReferenceNode) leftMostQualifiedNameRef;
        STNode simpleNameRef = STNodeFactory.createSimpleNameReferenceNode(qualifiedNameRef.identifier);
        STNode endExpr = middleExpr.replace(leftMostQualifiedNameRef, simpleNameRef);
        middleExpr = ConditionalExprResolver.getSimpleNameRefNode(qualifiedNameRef.modulePrefix);
        return STNodeFactory.createConditionalExpressionNode(lhsExpr, questionMark, middleExpr, qualifiedNameRef.colon,
                endExpr);
    }

    private STNode parseConditionalExprRhs(STNode lhsExpr, STNode questionMark, STNode middleExpr,
                                           boolean isInConditionalExpr) {
        STNode colon = parseColon();
        endContext();
        // start parsing end-expr, by giving higher-precedence to the end-expr, over currently
        // parsing conditional expr. That is done by lowering the current precedence.
        STNode endExpr = parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, true, false,
                isInConditionalExpr);
        return STNodeFactory.createConditionalExpressionNode(lhsExpr, questionMark, middleExpr, colon, endExpr);
    }

    /**
     * Parse enum declaration.
     * <p>
     * module-enum-decl :=
     * metadata
     * [public] enum identifier { enum-member (, enum-member)* } [;]
     * enum-member := metadata identifier [= const-expr]
     * </p>
     *
     * @param metadata
     * @param qualifier
     * @return Parsed enum node.
     */
    private STNode parseEnumDeclaration(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.MODULE_ENUM_DECLARATION);
        STNode enumKeywordToken = parseEnumKeyword();
        STNode identifier = parseIdentifier(ParserRuleContext.MODULE_ENUM_NAME);
        STNode openBraceToken = parseOpenBrace();
        STNode enumMemberList = parseEnumMemberList();
        STNode closeBraceToken = parseCloseBrace();
        STNode semicolon = parseOptionalSemicolon();

        endContext();
        openBraceToken = cloneWithDiagnosticIfListEmpty(enumMemberList, openBraceToken,
                DiagnosticErrorCode.ERROR_MISSING_ENUM_MEMBER);
        return STNodeFactory.createEnumDeclarationNode(metadata, qualifier, enumKeywordToken, identifier,
                openBraceToken, enumMemberList, closeBraceToken, semicolon);
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
            recover(token, ParserRuleContext.ENUM_KEYWORD);
            return parseEnumKeyword();
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

        // Report an empty enum member list
        if (peek().kind == SyntaxKind.CLOSE_BRACE_TOKEN) {
            return STNodeFactory.createEmptyNodeList();
        }

        // Parse first enum member, that has no leading comma
        List<STNode> enumMemberList = new ArrayList<>();
        STNode enumMember = parseEnumMember();

        // Parse the remaining enum members
        STNode enumMemberRhs;
        while (peek().kind != SyntaxKind.CLOSE_BRACE_TOKEN) {
            enumMemberRhs = parseEnumMemberEnd();
            if (enumMemberRhs == null) {
                break;
            }
            enumMemberList.add(enumMember);
            enumMemberList.add(enumMemberRhs);
            enumMember = parseEnumMember();
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
        STNode metadata;
        switch (peek().kind) {
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
                metadata = parseMetaData();
                break;
            default:
                metadata = STNodeFactory.createEmptyNode();
        }

        STNode identifierNode = parseIdentifier(ParserRuleContext.ENUM_MEMBER_NAME);
        return parseEnumMemberRhs(metadata, identifierNode);
    }

    private STNode parseEnumMemberRhs(STNode metadata, STNode identifierNode) {
        STNode equalToken, constExprNode;
        switch (peek().kind) {
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
                recover(peek(), ParserRuleContext.ENUM_MEMBER_RHS);
                return parseEnumMemberRhs(metadata, identifierNode);
        }

        return STNodeFactory.createEnumMemberNode(metadata, identifierNode, equalToken, constExprNode);
    }

    private STNode parseEnumMemberEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.ENUM_MEMBER_END);
                return parseEnumMemberEnd();
        }
    }

    private STNode parseTransactionStmtOrVarDecl(STNode annots, List<STNode> qualifiers, STToken transactionKeyword) {
        switch (peek().kind) {
            case OPEN_BRACE_TOKEN:
                reportInvalidStatementAnnots(annots, qualifiers);
                reportInvalidQualifierList(qualifiers);
                return parseTransactionStatement(transactionKeyword);
            case COLON_TOKEN:
                if (getNextNextToken().kind == SyntaxKind.IDENTIFIER_TOKEN) {
                    STNode typeDesc = parseQualifiedIdentifierWithPredeclPrefix(transactionKeyword, false);
                    return parseVarDeclTypeDescRhs(typeDesc, annots, qualifiers, true, false);
                }
                // fall through
            default:
                Solution solution = recover(peek(), ParserRuleContext.TRANSACTION_STMT_RHS_OR_TYPE_REF);

                if (solution.action == Action.KEEP ||
                        (solution.action == Action.INSERT && solution.tokenKind == SyntaxKind.COLON_TOKEN)) {
                    // If the solution is {@link Action#KEEP}, that means the colon is correct. hence parse var decl
                    STNode typeDesc = parseQualifiedIdentifierWithPredeclPrefix(transactionKeyword, false);
                    return parseVarDeclTypeDescRhs(typeDesc, annots, qualifiers, true, false);
                }

                return parseTransactionStmtOrVarDecl(annots, qualifiers, transactionKeyword);
        }
    }

    /**
     * Parse transaction statement.
     * <p>
     * <code>transaction-stmt := `transaction` block-stmt [on-fail-clause]</code>
     *
     * @return Transaction statement node
     */
    private STNode parseTransactionStatement(STNode transactionKeyword) {
        startContext(ParserRuleContext.TRANSACTION_STMT);
        STNode blockStmt = parseBlockNode();
        endContext();
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createTransactionStatementNode(transactionKeyword, blockStmt, onFailClause);
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
            recover(token, ParserRuleContext.COMMIT_KEYWORD);
            return parseCommitKeyword();
        }
    }

    /**
     * Parse retry statement.
     * <p>
     * <code>
     * retry-stmt := `retry` retry-spec block-stmt [on-fail-clause]
     * <br/>
     * retry-spec :=  [type-parameter] [ `(` arg-list `)` ]
     * </code>
     *
     * @return Retry statement node
     */
    private STNode parseRetryStatement() {
        startContext(ParserRuleContext.RETRY_STMT);
        STNode retryKeyword = parseRetryKeyword();
        // Context is closed inside the the method.
        STNode retryStmt = parseRetryKeywordRhs(retryKeyword);
        return retryStmt;
    }

    private STNode parseRetryKeywordRhs(STNode retryKeyword) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case LT_TOKEN:
                STNode typeParam = parseTypeParameter();
                return parseRetryTypeParamRhs(retryKeyword, typeParam);
            case OPEN_PAREN_TOKEN:
            case OPEN_BRACE_TOKEN:
            case TRANSACTION_KEYWORD:
                typeParam = STNodeFactory.createEmptyNode();
                return parseRetryTypeParamRhs(retryKeyword, typeParam);
            default:
                recover(peek(), ParserRuleContext.RETRY_KEYWORD_RHS);
                return parseRetryKeywordRhs(retryKeyword);
        }
    }

    private STNode parseRetryTypeParamRhs(STNode retryKeyword, STNode typeParam) {
        STNode args;
        switch (peek().kind) {
            case OPEN_PAREN_TOKEN:
                args = parseParenthesizedArgList();
                break;
            case OPEN_BRACE_TOKEN:
            case TRANSACTION_KEYWORD:
                args = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.RETRY_TYPE_PARAM_RHS);
                return parseRetryTypeParamRhs(retryKeyword, typeParam);
        }

        STNode blockStmt = parseRetryBody();
        endContext(); // end retry-stmt
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createRetryStatementNode(retryKeyword, typeParam, args, blockStmt, onFailClause);
    }

    private STNode parseRetryBody() {
        switch (peek().kind) {
            case OPEN_BRACE_TOKEN:
                return parseBlockNode();
            case TRANSACTION_KEYWORD:
                return parseTransactionStatement(consume());
            default:
                recover(peek(), ParserRuleContext.RETRY_BODY);
                return parseRetryBody();
        }
    }

    /**
     * Parse optional on fail clause.
     *
     * @return Parsed node
     */
    private STNode parseOptionalOnFailClause() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.ON_KEYWORD) {
            return parseOnFailClause();
        }

        if (isEndOfRegularCompoundStmt(nextToken.kind)) {
            return STNodeFactory.createEmptyNode();
        }

        recover(nextToken, ParserRuleContext.REGULAR_COMPOUND_STMT_RHS);
        return parseOptionalOnFailClause();
    }

    private boolean isEndOfRegularCompoundStmt(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case CLOSE_BRACE_TOKEN:
            case SEMICOLON_TOKEN:
            case AT_TOKEN:
            case EOF_TOKEN:
                return true;
            default:
                return isStatementStartingToken(nodeKind);
        }
    }

    private boolean isStatementStartingToken(SyntaxKind nodeKind) {
        switch (nodeKind) {
            case FINAL_KEYWORD:

                // Statement starts other than var-decl
            case IF_KEYWORD:
            case WHILE_KEYWORD:
            case DO_KEYWORD:
            case PANIC_KEYWORD:
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
            case RETURN_KEYWORD:
            case LOCK_KEYWORD:
            case OPEN_BRACE_TOKEN:
            case FORK_KEYWORD:
            case FOREACH_KEYWORD:
            case XMLNS_KEYWORD:
            case TRANSACTION_KEYWORD:
            case RETRY_KEYWORD:
            case ROLLBACK_KEYWORD:
            case MATCH_KEYWORD:
            case FAIL_KEYWORD:

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
            case TYPE_KEYWORD: // This is done to give proper error msg by invalidating local type-def after parsing.
            case CONST_KEYWORD: // This is done to give proper error msg by invalidating local const-decl after parsing.
                return true;
            default:
                // Var-decl-stmt start
                if (isTypeStartingToken(nodeKind)) {
                    return true;
                }

                // Expression-stmt start
                if (isValidExpressionStart(nodeKind, 1)) {
                    return true;
                }

                return false;
        }
    }

    /**
     * Parse on fail clause.
     * <p>
     * <code>
     * on-fail-clause := on fail [typed-binding-pattern] statement-block
     * </code>
     *
     * @return On fail clause node
     */
    private STNode parseOnFailClause() {
        startContext(ParserRuleContext.ON_FAIL_CLAUSE);
        STNode onKeyword = parseOnKeyword();
        STNode failKeyword = parseFailKeyword();
        STToken token = peek();
        STNode typeDescriptor = STNodeFactory.createEmptyNode();
        STNode identifier = STNodeFactory.createEmptyNode();
        if (token.kind != SyntaxKind.OPEN_BRACE_TOKEN) {
            typeDescriptor = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true, false,
                    TypePrecedence.DEFAULT);
            identifier = parseIdentifier(ParserRuleContext.VARIABLE_NAME);
        }
        STNode blockStatement = parseBlockNode();
        endContext();
        return STNodeFactory.createOnFailClauseNode(onKeyword, failKeyword, typeDescriptor, identifier,
                blockStatement);
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
            recover(token, ParserRuleContext.RETRY_KEYWORD);
            return parseRetryKeyword();
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
            recover(token, ParserRuleContext.ROLLBACK_KEYWORD);
            return parseRollbackKeyword();
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
            recover(token, ParserRuleContext.TRANSACTIONAL_KEYWORD);
            return parseTransactionalKeyword();
        }
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
     * @return parsed node
     */
    private STNode parseByteArrayLiteral() {
        STNode type;
        if (peek().kind == SyntaxKind.BASE16_KEYWORD) {
            type = parseBase16Keyword();
        } else {
            type = parseBase64Keyword();
        }

        STNode startingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_START);
        if (startingBackTick.isMissing()) {
            // Create new missing startingBackTick token which as no diagnostic.
            startingBackTick = SyntaxErrors.createMissingToken(SyntaxKind.BACKTICK_TOKEN);
            STNode endingBackTick = SyntaxErrors.createMissingToken(SyntaxKind.BACKTICK_TOKEN);
            STNode content = STNodeFactory.createEmptyNode();
            STNode byteArrayLiteral =
                    STNodeFactory.createByteArrayLiteralNode(type, startingBackTick, content, endingBackTick);
            byteArrayLiteral =
                    SyntaxErrors.addDiagnostic(byteArrayLiteral, DiagnosticErrorCode.ERROR_MISSING_BYTE_ARRAY_CONTENT);
            return byteArrayLiteral;
        }

        STNode content = parseByteArrayContent();
        return parseByteArrayLiteral(type, startingBackTick, content);
    }

    /**
     * Parse byte array literal.
     *
     * @param typeKeyword      keyword token, possible values are `base16` and `base64`
     * @param startingBackTick starting backtick token
     * @param byteArrayContent byte array literal content to be validated
     * @return parsed byte array literal node
     */
    private STNode parseByteArrayLiteral(STNode typeKeyword, STNode startingBackTick, STNode byteArrayContent) {
        STNode content = STNodeFactory.createEmptyNode();
        STNode newStartingBackTick = startingBackTick;
        STNodeList items = (STNodeList) byteArrayContent;
        if (items.size() == 1) {
            STNode item = items.get(0);
            if (typeKeyword.kind == SyntaxKind.BASE16_KEYWORD && !isValidBase16LiteralContent(item.toString())) {
                newStartingBackTick = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(startingBackTick, item,
                        DiagnosticErrorCode.ERROR_INVALID_BASE16_CONTENT_IN_BYTE_ARRAY_LITERAL);
            } else if (typeKeyword.kind == SyntaxKind.BASE64_KEYWORD && !isValidBase64LiteralContent(item.toString())) {
                newStartingBackTick = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(startingBackTick, item,
                        DiagnosticErrorCode.ERROR_INVALID_BASE64_CONTENT_IN_BYTE_ARRAY_LITERAL);
            } else if (item.kind != SyntaxKind.TEMPLATE_STRING) {
                newStartingBackTick = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(startingBackTick, item,
                        DiagnosticErrorCode.ERROR_INVALID_CONTENT_IN_BYTE_ARRAY_LITERAL);
            } else {
                content = item;
            }
        } else if (items.size() > 1) {
            // In this iteration, I am marking all the items as invalid
            STNode clonedStartingBackTick = startingBackTick;
            for (int index = 0; index < items.size(); index++) {
                STNode item = items.get(index);
                clonedStartingBackTick =
                        SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(clonedStartingBackTick, item);
            }
            newStartingBackTick = SyntaxErrors.addDiagnostic(clonedStartingBackTick,
                    DiagnosticErrorCode.ERROR_INVALID_CONTENT_IN_BYTE_ARRAY_LITERAL);
        }

        STNode endingBackTick = parseBacktickToken(ParserRuleContext.TEMPLATE_END);
        return STNodeFactory.createByteArrayLiteralNode(typeKeyword, newStartingBackTick, content, endingBackTick);
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
            recover(token, ParserRuleContext.BASE16_KEYWORD);
            return parseBase16Keyword();
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
            recover(token, ParserRuleContext.BASE64_KEYWORD);
            return parseBase64Keyword();
        }
    }

    /**
     * Validate and parse byte array literal content.
     * An error is reported, if the content is invalid.
     *
     * @return parsed node
     */
    private STNode parseByteArrayContent() {
        STToken nextToken = peek();

        List<STNode> items = new ArrayList<>();
        while (!isEndOfBacktickContent(nextToken.kind)) {
            STNode content = parseTemplateItem();
            items.add(content);
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(items);
    }

    /**
     * Validate base16 literal content.
     * <p>
     * <code>
     * Base16Literal := base16 WS ` HexGroup* WS `
     * <br/>
     * HexGroup := WS HexDigit WS HexDigit
     * <br/>
     * WS := WhiteSpaceChar*
     * <br/>
     * WhiteSpaceChar := 0x9 | 0xA | 0xD | 0x20
     * </code>
     *
     * @param content the string surrounded by the backticks
     * @return <code>true</code>, if the string content is valid. <code>false</code> otherwise.
     */
    static boolean isValidBase16LiteralContent(String content) {
        char[] charArray = content.toCharArray();
        int hexDigitCount = 0;

        for (char c : charArray) {
            switch (c) {
                case LexerTerminals.TAB:
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.SPACE:
                    break;
                default:
                    if (isHexDigit(c)) {
                        hexDigitCount++;
                    } else {
                        return false;
                    }
                    break;
            }
        }

        return hexDigitCount % 2 == 0;
    }

    /**
     * Validate base64 literal content.
     * <p>
     * <code>
     * Base64Literal := base64 WS ` Base64Group* [PaddedBase64Group] WS `
     * <br/>
     * Base64Group := WS Base64Char WS Base64Char WS Base64Char WS Base64Char
     * <br/>
     * PaddedBase64Group :=
     * WS Base64Char WS Base64Char WS Base64Char WS PaddingChar
     * | WS Base64Char WS Base64Char WS PaddingChar WS PaddingChar
     * <br/>
     * Base64Char := A .. Z | a .. z | 0 .. 9 | + | /
     * <br/>
     * PaddingChar := =
     * <br/>
     * WS := WhiteSpaceChar*
     * <br/>
     * WhiteSpaceChar := 0x9 | 0xA | 0xD | 0x20
     * </code>
     *
     * @param content the string surrounded by the backticks
     * @return <code>true</code>, if the string content is valid. <code>false</code> otherwise.
     */
    static boolean isValidBase64LiteralContent(String content) {
        char[] charArray = content.toCharArray();
        int base64CharCount = 0;
        int paddingCharCount = 0;

        for (char c : charArray) {
            switch (c) {
                case LexerTerminals.TAB:
                case LexerTerminals.NEWLINE:
                case LexerTerminals.CARRIAGE_RETURN:
                case LexerTerminals.SPACE:
                    break;
                case LexerTerminals.EQUAL:
                    paddingCharCount++;
                    break;
                default:
                    if (isBase64Char(c)) {
                        if (paddingCharCount == 0) {
                            base64CharCount++;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    break;
            }
        }

        if (paddingCharCount > 2) {
            return false;
        } else if (paddingCharCount == 0) {
            return base64CharCount % 4 == 0;
        } else {
            return base64CharCount % 4 == 4 - paddingCharCount;
        }
    }

    /**
     * <p>
     * Check whether a given char is a base64 char.
     * </p>
     * <code>Base64Char := A .. Z | a .. z | 0 .. 9 | + | /</code>
     *
     * @param c character to check
     * @return <code>true</code>, if the character represents a base64 char. <code>false</code> otherwise.
     */
    static boolean isBase64Char(int c) {
        if ('a' <= c && c <= 'z') {
            return true;
        }
        if ('A' <= c && c <= 'Z') {
            return true;
        }
        if (c == '+' || c == '/') {
            return true;
        }
        return isDigit(c);
    }

    static boolean isHexDigit(int c) {
        if ('a' <= c && c <= 'f') {
            return true;
        }
        if ('A' <= c && c <= 'F') {
            return true;
        }
        return isDigit(c);
    }

    static boolean isDigit(int c) {
        return ('0' <= c && c <= '9');
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

        startToken = cloneWithDiagnosticIfListEmpty(xmlNamePattern, startToken,
                DiagnosticErrorCode.ERROR_MISSING_XML_ATOMIC_NAME_PATTERN);
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
            recover(nextToken, ParserRuleContext.DOT_LT_TOKEN);
            return parseDotLTToken();
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
            return STNodeFactory.createNodeList(xmlAtomicNamePatternList);
        }

        // Parse first xml atomic name pattern, that has no leading pipe token
        STNode xmlAtomicNamePattern = parseXMLAtomicNamePattern();
        xmlAtomicNamePatternList.add(xmlAtomicNamePattern);

        // Parse the remaining xml atomic name patterns
        STNode separator;
        while (!isEndOfXMLNamePattern(peek().kind)) {
            separator = parseXMLNamePatternSeparator();
            if (separator == null) {
                break;
            }
            xmlAtomicNamePatternList.add(separator);

            xmlAtomicNamePattern = parseXMLAtomicNamePattern();
            xmlAtomicNamePatternList.add(xmlAtomicNamePattern);
        }

        return STNodeFactory.createNodeList(xmlAtomicNamePatternList);
    }

    private boolean isEndOfXMLNamePattern(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case GT_TOKEN:
            case EOF_TOKEN:
                return true;
            case IDENTIFIER_TOKEN:
            case ASTERISK_TOKEN:
            case COLON_TOKEN:
            default:
                return false;
        }
    }

    private STNode parseXMLNamePatternSeparator() {
        STToken token = peek();
        switch (token.kind) {
            case PIPE_TOKEN:
                return consume();
            case GT_TOKEN:
            case EOF_TOKEN:
                return null;
            default:
                recover(token, ParserRuleContext.XML_NAME_PATTERN_RHS);
                return parseXMLNamePatternSeparator();
        }
    }

    /**
     * Parse xml atomic name pattern.
     * <p>
     * <code>
     * xml-atomic-name-pattern :=
     * *
     * | identifier
     * | xml-namespace-prefix : identifier
     * | xml-namespace-prefix : *
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
                recover(token, ParserRuleContext.XML_ATOMIC_NAME_PATTERN_START);
                return parseXMLAtomicNamePatternBody();
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
     * xml-step-start :=
     * xml-all-children-step
     * | xml-element-children-step
     * | xml-element-descendants-step
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
            recover(nextToken, ParserRuleContext.SLASH_LT_TOKEN);
            return parseSlashLTToken();
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
            recover(nextToken, ParserRuleContext.DOUBLE_SLASH_DOUBLE_ASTERISK_LT_TOKEN);
            return parseDoubleSlashDoubleAsteriskLTToken();
        }
    }

    /**
     * Parse match statement.
     * <p>
     * <code>match-stmt := match action-or-expr { match-clause+ } [on-fail-clause]</code>
     *
     * @return Match statement
     */
    private STNode parseMatchStatement() {
        startContext(ParserRuleContext.MATCH_STMT);
        STNode matchKeyword = parseMatchKeyword();
        STNode actionOrExpr = parseActionOrExpression();
        startContext(ParserRuleContext.MATCH_BODY);
        STNode openBrace = parseOpenBrace();
        // Parse match clauses list
        List<STNode> matchClausesList = new ArrayList<>();
        while (!isEndOfMatchClauses(peek().kind)) {
            STNode clause = parseMatchClause();
            matchClausesList.add(clause);
        }
        STNode matchClauses =  STNodeFactory.createNodeList(matchClausesList);
        // At least one match clause required
        if (isNodeListEmpty(matchClauses)) {
            openBrace = SyntaxErrors.addDiagnostic(openBrace,
                    DiagnosticErrorCode.ERROR_MATCH_STATEMENT_SHOULD_HAVE_ONE_OR_MORE_MATCH_CLAUSES);
        }

        STNode closeBrace = parseCloseBrace();
        endContext();
        endContext();
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createMatchStatementNode(matchKeyword, actionOrExpr, openBrace, matchClauses, closeBrace,
                onFailClause);
    }

    /**
     * Parse match keyword.
     *
     * @return Match keyword node
     */
    private STNode parseMatchKeyword() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.MATCH_KEYWORD) {
            return consume();
        } else {
            recover(nextToken, ParserRuleContext.MATCH_KEYWORD);
            return parseMatchKeyword();
        }
    }

    private boolean isEndOfMatchClauses(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case TYPE_KEYWORD:
                return true;
            default:
                return isEndOfStatements();
        }
    }

    /**
     * Parse a single match match clause.
     * <p>
     * <code>
     * match-clause := match-pattern-list [match-guard] => block-stmt
     * <br/>
     * match-guard := if expression
     * </code>
     *
     * @return A match clause
     */
    private STNode parseMatchClause() {
        STNode matchPatterns = parseMatchPatternList();
        STNode matchGuard = parseMatchGuard();
        STNode rightDoubleArrow = parseDoubleRightArrow();
        STNode blockStmt = parseBlockNode();
        
        if (isNodeListEmpty(matchPatterns)) {
            STToken identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            STNode constantPattern = STNodeFactory.createSimpleNameReferenceNode(identifier);
            matchPatterns = STNodeFactory.createNodeList(constantPattern);
            
            DiagnosticErrorCode errorCode = DiagnosticErrorCode.ERROR_MISSING_MATCH_PATTERN;
            if (matchGuard != null) {
                matchGuard = SyntaxErrors.addDiagnostic(matchGuard, errorCode);
            } else {
                rightDoubleArrow = SyntaxErrors.addDiagnostic(rightDoubleArrow, errorCode);
            }
        }
        
        return STNodeFactory.createMatchClauseNode(matchPatterns, matchGuard, rightDoubleArrow, blockStmt);
    }

    /**
     * Parse match guard.
     * <p>
     * <code>match-guard := if expression</code>
     *
     * @return Match guard
     */
    private STNode parseMatchGuard() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IF_KEYWORD:
                STNode ifKeyword = parseIfKeyword();
                STNode expr = parseExpression(DEFAULT_OP_PRECEDENCE, true, false, true, false);
                return STNodeFactory.createMatchGuardNode(ifKeyword, expr);
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                recover(nextToken, ParserRuleContext.OPTIONAL_MATCH_GUARD);
                return parseMatchGuard();
        }
    }

    /**
     * Parse match patterns list.
     * <p>
     * <code>match-pattern-list := match-pattern (| match-pattern)*</code>
     *
     * @return Match patterns list
     */
    private STNode parseMatchPatternList() {
        startContext(ParserRuleContext.MATCH_PATTERN);
        List<STNode> matchClauses = new ArrayList<>();
        while (!isEndOfMatchPattern(peek().kind)) {
            STNode clause = parseMatchPattern();
            if (clause == null) {
                break;
            }
            matchClauses.add(clause);

            STNode seperator = parseMatchPatternListMemberRhs();
            if (seperator == null) {
                break;
            }
            matchClauses.add(seperator);
        }

        endContext();
        return STNodeFactory.createNodeList(matchClauses);
    }

    private boolean isEndOfMatchPattern(SyntaxKind nextTokenKind) {
        switch (nextTokenKind) {
            case PIPE_TOKEN:
            case IF_KEYWORD:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse match pattern.
     * <p>
     * <code>
     * match-pattern := var binding-pattern
     * | wildcard-match-pattern
     * | const-pattern
     * | list-match-pattern
     * | mapping-match-pattern
     * | error-match-pattern
     * </code>
     *
     * @return Match pattern
     */
    private STNode parseMatchPattern() {
        STToken nextToken = peek();
        if (isPredeclaredIdentifier(nextToken.kind)) {
            // This can be const-pattern or error-match-pattern with missing error keyword.
            STNode typeRefOrConstExpr = parseQualifiedIdentifier(ParserRuleContext.MATCH_PATTERN);
            return parseErrorMatchPatternOrConsPattern(typeRefOrConstExpr);
        }

        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
                return parseSimpleConstExpr();
            case VAR_KEYWORD:
                return parseVarTypedBindingPattern();
            case OPEN_BRACKET_TOKEN:
                return parseListMatchPattern();
            case OPEN_BRACE_TOKEN:
                return parseMappingMatchPattern();
            case ERROR_KEYWORD:
                return parseErrorMatchPattern();
            default:
                recover(nextToken, ParserRuleContext.MATCH_PATTERN_START);
                return parseMatchPattern();
        }
    }

    private STNode parseMatchPatternListMemberRhs() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case PIPE_TOKEN:
                return parsePipeToken();
            case IF_KEYWORD:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                // Returning null indicates the end of the match-patterns list
                return null;
            default:
                recover(nextToken, ParserRuleContext.MATCH_PATTERN_LIST_MEMBER_RHS);
                return parseMatchPatternListMemberRhs();
        }
    }

    /**
     * Parse var typed binding pattern.
     * <p>
     * <code>var binding-pattern</code>
     * </p>
     *
     * @return Parsed typed binding pattern node
     */
    private STNode parseVarTypedBindingPattern() {
        STNode varKeyword = parseVarKeyword();
        STNode varTypeDesc = createBuiltinSimpleNameReference(varKeyword);
        STNode bindingPattern = parseBindingPattern();
        return STNodeFactory.createTypedBindingPatternNode(varTypeDesc, bindingPattern);
    }

    /**
     * Parse var keyword.
     *
     * @return Var keyword node
     */
    private STNode parseVarKeyword() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.VAR_KEYWORD) {
            return consume();
        } else {
            recover(nextToken, ParserRuleContext.VAR_KEYWORD);
            return parseVarKeyword();
        }
    }

    /**
     * Parse list match pattern.
     * <p>
     * <code>
     * list-match-pattern := [ list-member-match-patterns ]
     * list-member-match-patterns :=
     * match-pattern (, match-pattern)* [, rest-match-pattern]
     * | [ rest-match-pattern ]
     * </code>
     * </p>
     *
     * @return Parsed list match pattern node
     */
    private STNode parseListMatchPattern() {
        startContext(ParserRuleContext.LIST_MATCH_PATTERN);
        STNode openBracketToken = parseOpenBracket();
        List<STNode> matchPatternList = new ArrayList<>();
        STNode listMatchPatternMemberRhs = null;
        boolean isEndOfFields = false;

        while (!isEndOfListMatchPattern()) {
            STNode listMatchPatternMember = parseListMatchPatternMember();
            matchPatternList.add(listMatchPatternMember);
            listMatchPatternMemberRhs = parseListMatchPatternMemberRhs();

            if (listMatchPatternMember.kind == SyntaxKind.REST_MATCH_PATTERN) {
                isEndOfFields = true;
                break;
            }

            if (listMatchPatternMemberRhs != null) {
                matchPatternList.add(listMatchPatternMemberRhs);
            } else {
                break;
            }
        }

        // Following loop will only run if there are more fields after the rest match pattern.
        // Try to parse them and mark as invalid.
        while (isEndOfFields && listMatchPatternMemberRhs != null) {
            updateLastNodeInListWithInvalidNode(matchPatternList, listMatchPatternMemberRhs, null);

            if (peek().kind == SyntaxKind.CLOSE_BRACKET_TOKEN) {
                break;
            }

            STNode invalidField = parseListMatchPatternMember();
            updateLastNodeInListWithInvalidNode(matchPatternList, invalidField,
                    DiagnosticErrorCode.ERROR_MATCH_PATTERN_AFTER_REST_MATCH_PATTERN);
            listMatchPatternMemberRhs = parseListMatchPatternMemberRhs();
        }

        STNode matchPatternListNode = STNodeFactory.createNodeList(matchPatternList);
        STNode closeBracketToken = parseCloseBracket();
        endContext();

        return STNodeFactory.createListMatchPatternNode(openBracketToken, matchPatternListNode, closeBracketToken);
    }

    public boolean isEndOfListMatchPattern() {
        switch (peek().kind) {
            case CLOSE_BRACKET_TOKEN:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseListMatchPatternMember() {
        STNode nextToken = peek();
        switch (nextToken.kind) {
            case ELLIPSIS_TOKEN:
                return parseRestMatchPattern();
            default:
                // No need of recovery here
                return parseMatchPattern();
        }
    }

    /**
     * Parse rest match pattern.
     * <p>
     * <code>
     * rest-match-pattern := ... var variable-name
     * </code>
     * </p>
     *
     * @return Parsed rest match pattern node
     */
    private STNode parseRestMatchPattern() {
        startContext(ParserRuleContext.REST_MATCH_PATTERN);
        STNode ellipsisToken = parseEllipsis();
        STNode varKeywordToken = parseVarKeyword();
        STNode variableName = parseVariableName();
        endContext();

        STSimpleNameReferenceNode simpleNameReferenceNode =
                (STSimpleNameReferenceNode) STNodeFactory.createSimpleNameReferenceNode(variableName);
        return STNodeFactory.createRestMatchPatternNode(ellipsisToken, varKeywordToken, simpleNameReferenceNode);
    }

    private STNode parseListMatchPatternMemberRhs() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
            case EOF_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.LIST_MATCH_PATTERN_MEMBER_RHS);
                return parseListMatchPatternMemberRhs();
        }
    }

    /**
     * Parse mapping match pattern.
     * <p>
     * mapping-match-pattern := { field-match-patterns }
     * <br/>
     * field-match-patterns := field-match-pattern (, field-match-pattern)* [, rest-match-pattern]
     * | [ rest-match-pattern ]
     * <br/>
     * field-match-pattern := field-name : match-pattern
     * <br/>
     * rest-match-pattern := ... var variable-name
     * </p>
     *
     * @return Parsed Node.
     */
    private STNode parseMappingMatchPattern() {
        startContext(ParserRuleContext.MAPPING_MATCH_PATTERN);
        STNode openBraceToken = parseOpenBrace();
        STNode fieldMatchPatterns = parseFieldMatchPatternList();
        STNode closeBraceToken = parseCloseBrace();
        endContext();
        return STNodeFactory.createMappingMatchPatternNode(openBraceToken, fieldMatchPatterns, closeBraceToken);
    }

    private STNode parseFieldMatchPatternList() {
        List<STNode> fieldMatchPatterns = new ArrayList<>();

        STNode fieldMatchPatternMember = parseFieldMatchPatternMember();
        if (fieldMatchPatternMember == null) {
            return STNodeFactory.createEmptyNodeList();
        }

        fieldMatchPatterns.add(fieldMatchPatternMember);
        if (fieldMatchPatternMember.kind == SyntaxKind.REST_MATCH_PATTERN) {
            invalidateExtraFieldMatchPatterns(fieldMatchPatterns);
            return STNodeFactory.createNodeList(fieldMatchPatterns);
        }

        return parseFieldMatchPatternList(fieldMatchPatterns);
    }

    private STNode parseFieldMatchPatternList(List<STNode> fieldMatchPatterns) {
        while (!isEndOfMappingMatchPattern()) {
            STNode fieldMatchPatternRhs = parseFieldMatchPatternRhs();
            if (fieldMatchPatternRhs == null) {
                break;
            }

            fieldMatchPatterns.add(fieldMatchPatternRhs);
            STNode fieldMatchPatternMember = parseFieldMatchPatternMember();
            if (fieldMatchPatternMember == null) {
                fieldMatchPatternMember = createMissingFieldMatchPattern();
            }

            fieldMatchPatterns.add(fieldMatchPatternMember);
            if (fieldMatchPatternMember.kind == SyntaxKind.REST_MATCH_PATTERN) {
                invalidateExtraFieldMatchPatterns(fieldMatchPatterns);
                break;
            }
        }

        return STNodeFactory.createNodeList(fieldMatchPatterns);
    }

    private STNode createMissingFieldMatchPattern() {
        STNode fieldName = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
        STNode colon = SyntaxErrors.createMissingToken(SyntaxKind.COLON_TOKEN);
        STNode identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
        STNode matchPattern = STNodeFactory.createSimpleNameReferenceNode(identifier);
        STNode fieldMatchPatternMember = STNodeFactory.createFieldMatchPatternNode(fieldName, colon, matchPattern);
        fieldMatchPatternMember = SyntaxErrors.addDiagnostic(fieldMatchPatternMember,
                DiagnosticErrorCode.ERROR_MISSING_FIELD_MATCH_PATTERN_MEMBER);
        return fieldMatchPatternMember;
    }

    /**
     * Parse and invalidate all field match pattern members after a rest-match-pattern.
     *
     * @param fieldMatchPatterns field-match-patterns list
     */
    private void invalidateExtraFieldMatchPatterns(List<STNode> fieldMatchPatterns) {
        while (!isEndOfMappingMatchPattern()) {
            STNode fieldMatchPatternRhs = parseFieldMatchPatternRhs();
            if (fieldMatchPatternRhs == null) {
                break;
            }

            STNode fieldMatchPatternMember = parseFieldMatchPatternMember();
            if (fieldMatchPatternMember == null) {
                updateLastNodeInListWithInvalidNode(fieldMatchPatterns, fieldMatchPatternRhs,
                        DiagnosticErrorCode.ERROR_INVALID_TOKEN, ((STToken) fieldMatchPatternRhs).text());
            } else {
                updateLastNodeInListWithInvalidNode(fieldMatchPatterns, fieldMatchPatternRhs, null);
                updateLastNodeInListWithInvalidNode(fieldMatchPatterns, fieldMatchPatternMember,
                        DiagnosticErrorCode.ERROR_MATCH_PATTERN_AFTER_REST_MATCH_PATTERN);
            }
        }
    }

    private STNode parseFieldMatchPatternMember() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                return parseFieldMatchPattern();
            case ELLIPSIS_TOKEN:
                return parseRestMatchPattern();
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
                // null marks the end of field-match-patterns
                return null;
            default:
                recover(nextToken, ParserRuleContext.FIELD_MATCH_PATTERNS_START);
                return parseFieldMatchPatternMember();
        }
    }

    /**
     * Parse filed match pattern.
     * <p>
     * field-match-pattern := field-name : match-pattern
     * </p>
     *
     * @return Parsed field match pattern node
     */
    public STNode parseFieldMatchPattern() {
        STNode fieldNameNode = parseVariableName();
        STNode colonToken = parseColon();
        STNode matchPattern = parseMatchPattern();
        return STNodeFactory.createFieldMatchPatternNode(fieldNameNode, colonToken, matchPattern);
    }

    public boolean isEndOfMappingMatchPattern() {
        switch (peek().kind) {
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseFieldMatchPatternRhs() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
            case EOF_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.FIELD_MATCH_PATTERN_MEMBER_RHS);
                return parseFieldMatchPatternRhs();
        }
    }

    private STNode parseErrorMatchPatternOrConsPattern(STNode typeRefOrConstExpr) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                STNode errorKeyword = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.ERROR_KEYWORD,
                        ParserRuleContext.ERROR_KEYWORD);
                startContext(ParserRuleContext.ERROR_MATCH_PATTERN); // Context ended inside the method
                return parseErrorMatchPattern(errorKeyword, typeRefOrConstExpr);
            default:
                if (isMatchPatternEnd(peek().kind)) {
                    return typeRefOrConstExpr;
                }
                recover(peek(), ParserRuleContext.ERROR_MATCH_PATTERN_OR_CONST_PATTERN);
                return parseErrorMatchPatternOrConsPattern(typeRefOrConstExpr);
        }
    }

    private boolean isMatchPatternEnd(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case RIGHT_DOUBLE_ARROW_TOKEN:
            case COMMA_TOKEN:
            case CLOSE_BRACE_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case PIPE_TOKEN:
            case IF_KEYWORD:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse functional match pattern.
     * <p>
     * error-match-pattern := error [error-type-reference] ( error-arg-list-match-pattern )
     * error-arg-list-match-pattern :=
     * error-message-match-pattern [, error-cause-match-pattern] [, error-field-match-patterns]
     * | [error-field-match-patterns]
     * error-message-match-pattern := simple-match-pattern
     * error-cause-match-pattern := simple-match-pattern | error-match-pattern
     * simple-match-pattern :=
     * wildcard-match-pattern
     * | const-pattern
     * | var variable-name
     * error-field-match-patterns :=
     * named-arg-match-pattern (, named-arg-match-pattern)* [, rest-match-pattern]
     * | rest-match-pattern
     * named-arg-match-pattern := arg-name = match-pattern
     * </p>
     *
     * @return Parsed functional match pattern node.
     */
    private STNode parseErrorMatchPattern() {
        startContext(ParserRuleContext.ERROR_MATCH_PATTERN);
        STNode errorKeyword = consume();
        return parseErrorMatchPattern(errorKeyword);
    }

    private STNode parseErrorMatchPattern(STNode errorKeyword) {
        STToken nextToken = peek();
        STNode typeRef;
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                typeRef = STNodeFactory.createEmptyNode();
                break;
            default:
                if (isPredeclaredIdentifier(nextToken.kind)) {
                    typeRef = parseTypeReference();
                    break;
                }
                recover(peek(), ParserRuleContext.ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS);
                return parseErrorMatchPattern(errorKeyword);
        }
        return parseErrorMatchPattern(errorKeyword, typeRef);
    }

    private STNode parseErrorMatchPattern(STNode errorKeyword, STNode typeRef) {
        STNode openParenthesisToken = parseOpenParenthesis();
        STNode argListMatchPatternNode = parseErrorArgListMatchPatterns();
        STNode closeParenthesisToken = parseCloseParenthesis();
        endContext();
        return STNodeFactory.createErrorMatchPatternNode(errorKeyword, typeRef, openParenthesisToken,
                argListMatchPatternNode, closeParenthesisToken);
    }

    private STNode parseErrorArgListMatchPatterns() {
        List<STNode> argListMatchPatterns = new ArrayList<>();

        if (isEndOfErrorFieldMatchPatterns()) {
            return STNodeFactory.createNodeList(argListMatchPatterns);
        }
        startContext(ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_FIRST_ARG);
        STNode firstArg = parseErrorArgListMatchPattern(ParserRuleContext.ERROR_ARG_LIST_MATCH_PATTERN_START);
        endContext();

        if (isSimpleMatchPattern(firstArg.kind)) {

            argListMatchPatterns.add(firstArg);
            STNode argEnd = parseErrorArgListMatchPatternEnd(ParserRuleContext.ERROR_MESSAGE_MATCH_PATTERN_END);
            if (argEnd != null) {
                // null marks the end of args
                STNode secondArg = parseErrorArgListMatchPattern(ParserRuleContext.ERROR_MESSAGE_MATCH_PATTERN_RHS);
                if (isValidSecondArgMatchPattern(secondArg.kind)) {
                    argListMatchPatterns.add(argEnd);
                    argListMatchPatterns.add(secondArg);
                } else {
                    updateLastNodeInListWithInvalidNode(argListMatchPatterns, argEnd, null);
                    updateLastNodeInListWithInvalidNode(argListMatchPatterns, secondArg,
                            DiagnosticErrorCode.ERROR_MATCH_PATTERN_NOT_ALLOWED);
                }
            }
        } else {
            if (firstArg.kind != SyntaxKind.NAMED_ARG_MATCH_PATTERN &&
                    firstArg.kind != SyntaxKind.REST_MATCH_PATTERN) {
                addInvalidNodeToNextToken(firstArg, DiagnosticErrorCode.ERROR_MATCH_PATTERN_NOT_ALLOWED);
            } else {
                argListMatchPatterns.add(firstArg);
            }
        }

        parseErrorFieldMatchPatterns(argListMatchPatterns);
        return STNodeFactory.createNodeList(argListMatchPatterns);
    }

    private boolean isSimpleMatchPattern(SyntaxKind matchPatternKind) {
        switch (matchPatternKind) {
            case IDENTIFIER_TOKEN:
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case NUMERIC_LITERAL:
            case STRING_LITERAL:
            case NULL_LITERAL:
            case NIL_LITERAL:
            case BOOLEAN_LITERAL:
            case TYPED_BINDING_PATTERN:
            case UNARY_EXPRESSION:
                return true;
            default:
                return false;
        }
    }

    private boolean isValidSecondArgMatchPattern(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case ERROR_MATCH_PATTERN:
            case NAMED_ARG_MATCH_PATTERN:
            case REST_MATCH_PATTERN:
                return true;
            default:
                if (isSimpleMatchPattern(syntaxKind)) {
                    return true;
                }
                return false;
        }
    }

    /**
     * Parse error field match patterns.
     * error-field-match-patterns :=
     * named-arg-match-pattern (, named-arg-match-pattern)* [, rest-match-pattern]
     * | rest-match-pattern
     * named-arg-match-pattern := arg-name = match-pattern
     * @param argListMatchPatterns
     */
    private void parseErrorFieldMatchPatterns(List<STNode> argListMatchPatterns) {
        SyntaxKind lastValidArgKind = SyntaxKind.NAMED_ARG_MATCH_PATTERN;
        while (!isEndOfErrorFieldMatchPatterns()) {
            STNode argEnd = parseErrorArgListMatchPatternEnd(ParserRuleContext.ERROR_FIELD_MATCH_PATTERN_RHS);
            if (argEnd == null) {
                // null marks the end of args
                break;
            }
            STNode currentArg = parseErrorArgListMatchPattern(ParserRuleContext.ERROR_FIELD_MATCH_PATTERN);
            DiagnosticErrorCode errorCode = validateErrorFieldMatchPatternOrder(lastValidArgKind, currentArg.kind);
            if (errorCode == null) {
                argListMatchPatterns.add(argEnd);
                argListMatchPatterns.add(currentArg);
                lastValidArgKind = currentArg.kind;
            } else if (argListMatchPatterns.size() == 0) {
                addInvalidNodeToNextToken(argEnd, null);
                addInvalidNodeToNextToken(currentArg, errorCode);
            } else {
                updateLastNodeInListWithInvalidNode(argListMatchPatterns, argEnd, null);
                updateLastNodeInListWithInvalidNode(argListMatchPatterns, currentArg, errorCode);
            }
        }
    }

    private boolean isEndOfErrorFieldMatchPatterns() {
        // We can use the same method here.
        return isEndOfErrorFieldBindingPatterns();
    }

    private STNode parseErrorArgListMatchPatternEnd(ParserRuleContext currentCtx) {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return consume();
            case CLOSE_PAREN_TOKEN:
                return null;
            default:
                recover(peek(), currentCtx);
                return parseErrorArgListMatchPatternEnd(currentCtx);
        }
    }

    private STNode parseErrorArgListMatchPattern(ParserRuleContext context) {
        STToken nextToken = peek();
        if (isPredeclaredIdentifier(nextToken.kind)) {
            return parseNamedArgOrSimpleMatchPattern();
        }

        switch (nextToken.kind) {
            case ELLIPSIS_TOKEN:
                return parseRestMatchPattern();
            case OPEN_PAREN_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case OPEN_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case ERROR_KEYWORD:
                return parseMatchPattern();
            case VAR_KEYWORD:
                STNode varType = createBuiltinSimpleNameReference(consume());
                STNode variableName = createCaptureOrWildcardBP(parseVariableName());
                return STNodeFactory.createTypedBindingPatternNode(varType, variableName);
            case CLOSE_PAREN_TOKEN:
                return SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_MATCH_PATTERN);
            default:
                recover(nextToken, context);
                return parseErrorArgListMatchPattern(context);
        }
    }

    private STNode parseNamedArgOrSimpleMatchPattern() {
        STNode constRefExpr = parseQualifiedIdentifier(ParserRuleContext.MATCH_PATTERN);
        if (constRefExpr.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE || peek().kind != SyntaxKind.EQUAL_TOKEN) {
            return constRefExpr;
        }

        // We reach here for identifier followed by '=' token.
        return parseNamedArgMatchPattern(((STSimpleNameReferenceNode) constRefExpr).name);
    }

    /**
     * Parses the next named arg match pattern.
     * <br/>
     * <code>named-arg-match-pattern := arg-name = match-pattern</code>
     * <br/>
     * <br/>
     *
     * @return arg match pattern list node added the new arg match pattern
     */
    private STNode parseNamedArgMatchPattern(STNode identifier) {
        startContext(ParserRuleContext.NAMED_ARG_MATCH_PATTERN);
        STNode equalToken = parseAssignOp();
        STNode matchPattern = parseMatchPattern();
        endContext();
        return STNodeFactory.createNamedArgMatchPatternNode(identifier, equalToken, matchPattern);
    }

    private DiagnosticErrorCode validateErrorFieldMatchPatternOrder(SyntaxKind prevArgKind, SyntaxKind currentArgKind) {
        switch (currentArgKind) {
            case NAMED_ARG_MATCH_PATTERN:
            case REST_MATCH_PATTERN:
                // Nothing is allowed after a rest arg
                if (prevArgKind == SyntaxKind.REST_MATCH_PATTERN) {
                    return DiagnosticErrorCode.ERROR_REST_ARG_FOLLOWED_BY_ANOTHER_ARG;
                }
                return null;
            default:
                return DiagnosticErrorCode.ERROR_MATCH_PATTERN_NOT_ALLOWED;
        }
    }

    /**
     * Parse markdown documentation.
     *
     * @return markdown documentation node
     */
    private STNode parseMarkdownDocumentation() {
        List<STNode> markdownDocLineList = new ArrayList<>();

        // With multi-line documentation, there could be more than one documentation string.
        // e.g.
        // # line1 (this is captured as one documentation string)
        //
        // # line2 (this is captured as another documentation string)
        STToken nextToken = peek();
        while (nextToken.kind == SyntaxKind.DOCUMENTATION_STRING) {
            STToken documentationString = consume();
            STNode parsedDocLines = parseDocumentationString(documentationString);
            appendParsedDocumentationLines(markdownDocLineList, parsedDocLines);
            nextToken = peek();
        }

        STNode markdownDocLines = STNodeFactory.createNodeList(markdownDocLineList);
        return STNodeFactory.createMarkdownDocumentationNode(markdownDocLines);
    }

    /**
     * Parse documentation string.
     *
     * @return markdown documentation line list node
     */
    private STNode parseDocumentationString(STToken documentationStringToken) {
        List<STNode> leadingTriviaList = getLeadingTriviaList(documentationStringToken.leadingMinutiae());
        Collection<STNodeDiagnostic> diagnostics = new ArrayList<>((documentationStringToken.diagnostics()));

        CharReader charReader = CharReader.from(documentationStringToken.text());
        DocumentationLexer documentationLexer = new DocumentationLexer(charReader, leadingTriviaList, diagnostics);
        AbstractTokenReader tokenReader = new TokenReader(documentationLexer);
        DocumentationParser documentationParser = new DocumentationParser(tokenReader);
        return documentationParser.parse();
    }

    private List<STNode> getLeadingTriviaList(STNode leadingMinutiaeNode) {
        List<STNode> leadingTriviaList = new ArrayList<>();
        int bucketCount = leadingMinutiaeNode.bucketCount();
        for (int i = 0; i < bucketCount; i++) {
            leadingTriviaList.add(leadingMinutiaeNode.childInBucket(i));
        }

        return leadingTriviaList;
    }

    private void appendParsedDocumentationLines(List<STNode> markdownDocLineList, STNode parsedDocLines) {
        int bucketCount = parsedDocLines.bucketCount();
        for (int i = 0; i < bucketCount; i++) {
            STNode markdownDocLine = parsedDocLines.childInBucket(i);
            markdownDocLineList.add(markdownDocLine);
        }
    }

    // ------------------------ Ambiguity resolution at statement start ---------------------------

    /**
     * Parse any statement that starts with a token that has ambiguity between being
     * a type-desc or an expression.
     *
     * @param annots Annotations
     * @param qualifiers
     * @return Statement node
     */
    private STNode parseStmtStartsWithTypeOrExpr(STNode annots, List<STNode> qualifiers) {
        startContext(ParserRuleContext.AMBIGUOUS_STMT);
        STNode typeOrExpr = parseTypedBindingPatternOrExpr(qualifiers, true);
        return parseStmtStartsWithTypedBPOrExprRhs(annots, typeOrExpr);
    }

    private STNode parseStmtStartsWithTypedBPOrExprRhs(STNode annots, STNode typedBindingPatternOrExpr) {
        if (typedBindingPatternOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
            List<STNode> varDeclQualifiers = new ArrayList<>();
            switchContext(ParserRuleContext.VAR_DECL_STMT);
            return parseVarDeclRhs(annots, varDeclQualifiers, typedBindingPatternOrExpr, false);
        }

        STNode expr = getExpression(typedBindingPatternOrExpr);
        expr = getExpression(parseExpressionRhs(DEFAULT_OP_PRECEDENCE, expr, false, true));
        return parseStatementStartWithExprRhs(expr);
    }

    private STNode parseTypedBindingPatternOrExpr(boolean allowAssignment) {
        List<STNode> typeDescQualifiers = new ArrayList<>();
        return parseTypedBindingPatternOrExpr(typeDescQualifiers, allowAssignment);
    }

    private STNode parseTypedBindingPatternOrExpr(List<STNode> qualifiers, boolean allowAssignment) {
        parseTypeDescQualifiers(qualifiers);
        STToken nextToken = peek();
        STNode typeOrExpr;
        if (isPredeclaredIdentifier(nextToken.kind)) {
            reportInvalidQualifierList(qualifiers);
            typeOrExpr = parseQualifiedIdentifier(ParserRuleContext.TYPE_NAME_OR_VAR_NAME);
            return parseTypedBindingPatternOrExprRhs(typeOrExpr, allowAssignment);
        }
        
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                reportInvalidQualifierList(qualifiers);
                return parseTypedBPOrExprStartsWithOpenParenthesis();
            case FUNCTION_KEYWORD:
                return parseAnonFuncExprOrTypedBPWithFuncType(qualifiers);
            case OPEN_BRACKET_TOKEN:
                reportInvalidQualifierList(qualifiers);
                typeOrExpr = parseTupleTypeDescOrListConstructor(STNodeFactory.createEmptyNodeList());
                return parseTypedBindingPatternOrExprRhs(typeOrExpr, allowAssignment);
            // Can be a singleton type or expression.
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                reportInvalidQualifierList(qualifiers);
                STNode basicLiteral = parseBasicLiteral();
                return parseTypedBindingPatternOrExprRhs(basicLiteral, allowAssignment);
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    reportInvalidQualifierList(qualifiers);
                    return parseActionOrExpressionInLhs(STNodeFactory.createEmptyNodeList());
                }

                return parseTypedBindingPattern(qualifiers, ParserRuleContext.VAR_DECL_STMT);
        }
    }

    /**
     * Parse the component after the ambiguous starting node. Ambiguous node could be either an expr
     * or a type-desc. The component followed by this ambiguous node could be the binding-pattern or
     * the expression-rhs.
     *
     * @param typeOrExpr      Type desc or the expression
     * @param allowAssignment Flag indicating whether to allow assignment. i.e.: whether this is a
     *                        valid lvalue expression
     * @return Typed-binding-pattern node or an expression node
     */
    private STNode parseTypedBindingPatternOrExprRhs(STNode typeOrExpr, boolean allowAssignment) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
                STToken nextNextToken = peek(2);
                if (nextNextToken.kind == SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode pipeOrAndToken = parseBinaryOperator();
                STNode rhsTypedBPOrExpr = parseTypedBindingPatternOrExpr(allowAssignment);
                if (rhsTypedBPOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
                    STTypedBindingPatternNode typedBP = (STTypedBindingPatternNode) rhsTypedBPOrExpr;
                    typeOrExpr = getTypeDescFromExpr(typeOrExpr);

                    STNode newTypeDesc = mergeTypes(typeOrExpr, pipeOrAndToken, typedBP.typeDescriptor);
                    return STNodeFactory.createTypedBindingPatternNode(newTypeDesc, typedBP.bindingPattern);
                }

                // If the next token is an equal, then it is typedBP with union/intersection type and missing var name
                if (peek().kind == SyntaxKind.EQUAL_TOKEN) {
                    return createCaptureBPWithMissingVarName(typeOrExpr, pipeOrAndToken, rhsTypedBPOrExpr);
                }

                return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr,
                        pipeOrAndToken, rhsTypedBPOrExpr);
            case SEMICOLON_TOKEN:
                if (isExpression(typeOrExpr.kind)) {
                    return typeOrExpr;
                }

                if (isDefiniteTypeDesc(typeOrExpr.kind) || !isAllBasicLiterals(typeOrExpr)) {
                    // treat as type
                    STNode typeDesc = getTypeDescFromExpr(typeOrExpr);
                    return parseTypeBindingPatternStartsWithAmbiguousNode(typeDesc);
                }

                return typeOrExpr;
            case IDENTIFIER_TOKEN:
            case QUESTION_MARK_TOKEN:
                if (isAmbiguous(typeOrExpr) || isDefiniteTypeDesc(typeOrExpr.kind)) {
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
            case OPEN_BRACE_TOKEN: // mapping binding pattern
            case ERROR_KEYWORD: // error binding pattern
                STNode typeDesc = getTypeDescFromExpr(typeOrExpr);
                return parseTypeBindingPatternStartsWithAmbiguousNode(typeDesc);
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundAssignment(nextToken.kind)) {
                    return typeOrExpr;
                }

                // If the next token is part of a valid expression, then still parse it
                // as a statement that starts with an expression.
                if (isValidExprRhsStart(nextToken.kind, typeOrExpr.kind)) {
                    return typeOrExpr;
                }

                STToken token = peek();
                SyntaxKind typeOrExprKind = typeOrExpr.kind;
                if (typeOrExprKind == SyntaxKind.QUALIFIED_NAME_REFERENCE ||
                        typeOrExprKind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    recover(token, ParserRuleContext.BINDING_PATTERN_OR_VAR_REF_RHS);
                } else {
                    recover(token, ParserRuleContext.BINDING_PATTERN_OR_EXPR_RHS);
                }

                return parseTypedBindingPatternOrExprRhs(typeOrExpr, allowAssignment);
        }
    }

    private STNode createCaptureBPWithMissingVarName(STNode lhsType, STNode separatorToken, STNode rhsType) {
        lhsType = getTypeDescFromExpr(lhsType);
        rhsType = getTypeDescFromExpr(rhsType);

        STNode newTypeDesc = mergeTypes(lhsType, separatorToken, rhsType);

        STNode identifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                ParserRuleContext.VARIABLE_NAME);
        STNode captureBP = STNodeFactory.createCaptureBindingPatternNode(identifier);

        return STNodeFactory.createTypedBindingPatternNode(newTypeDesc, captureBP);
    }

    private STNode parseTypeBindingPatternStartsWithAmbiguousNode(STNode typeDesc) {
        // switchContext(ParserRuleContext.VAR_DECL_STMT);

        // We haven't parsed the type-desc as a type-desc (parsed as an identifier/expr).
        // Therefore handle the context manually here.
        typeDesc = parseComplexTypeDescriptor(typeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
        return parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
    }

    private STNode parseTypedBPOrExprStartsWithOpenParenthesis() {
        STNode exprOrTypeDesc = parseTypedDescOrExprStartsWithOpenParenthesis();
        if (isDefiniteTypeDesc(exprOrTypeDesc.kind)) {
            return parseTypeBindingPatternStartsWithAmbiguousNode(exprOrTypeDesc);
        }

        return parseTypedBindingPatternOrExprRhs(exprOrTypeDesc, false);
    }

    private boolean isDefiniteTypeDesc(SyntaxKind kind) {
        return kind.compareTo(SyntaxKind.RECORD_TYPE_DESC) >= 0 && kind.compareTo(SyntaxKind.FUTURE_TYPE_DESC) <= 0;
    }

    private boolean isDefiniteExpr(SyntaxKind kind) {
        if (kind == SyntaxKind.QUALIFIED_NAME_REFERENCE || kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return false;
        }

        return kind.compareTo(SyntaxKind.BINARY_EXPRESSION) >= 0 && kind.compareTo(SyntaxKind.ERROR_CONSTRUCTOR) <= 0;
    }

    private boolean isDefiniteAction(SyntaxKind kind) {
        return kind.compareTo(SyntaxKind.REMOTE_METHOD_CALL_ACTION) >= 0 && 
                kind.compareTo(SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION) <= 0;
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
        STNode openParen = parseOpenParenthesis();
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
            return parseBracedExprOrAnonFuncParamRhs(openParen, typeOrExpr, false);
        }

        STNode typeDescNode = getTypeDescFromExpr(typeOrExpr);
        typeDescNode = parseComplexTypeDescriptor(typeDescNode, ParserRuleContext.TYPE_DESC_IN_PARENTHESIS, false);

        STNode closeParen = parseCloseParenthesis();
        return STNodeFactory.createParenthesisedTypeDescriptorNode(openParen, typeDescNode, closeParen);
    }

    /**
     * Parse type-desc or expression. This method does not handle binding patterns.
     *
     * @return Type-desc node or expression node
     */
    private STNode parseTypeDescOrExpr() {
        List<STNode> typeDescQualifiers = new ArrayList<>();
        return parseTypeDescOrExpr(typeDescQualifiers);
    }

    private STNode parseTypeDescOrExpr(List<STNode> qualifiers) {
        parseTypeDescQualifiers(qualifiers);
        STToken nextToken = peek();
        STNode typeOrExpr;
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                reportInvalidQualifierList(qualifiers);
                typeOrExpr = parseTypedDescOrExprStartsWithOpenParenthesis();
                break;
            case FUNCTION_KEYWORD:
                typeOrExpr = parseAnonFuncExprOrFuncTypeDesc(qualifiers);
                break;
            case IDENTIFIER_TOKEN:
                reportInvalidQualifierList(qualifiers);
                typeOrExpr = parseQualifiedIdentifier(ParserRuleContext.TYPE_NAME_OR_VAR_NAME);
                return parseTypeDescOrExprRhs(typeOrExpr);
            case OPEN_BRACKET_TOKEN:
                reportInvalidQualifierList(qualifiers);
                typeOrExpr = parseTupleTypeDescOrListConstructor(STNodeFactory.createEmptyNodeList());
                break;
            // Can be a singleton type or expression.
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                reportInvalidQualifierList(qualifiers);
                STNode basicLiteral = parseBasicLiteral();
                return parseTypeDescOrExprRhs(basicLiteral);
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    reportInvalidQualifierList(qualifiers);
                    return parseActionOrExpressionInLhs(STNodeFactory.createEmptyNodeList());
                }
                return parseTypeDescriptor(qualifiers, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        }

        if (isDefiniteTypeDesc(typeOrExpr.kind)) {
            return parseComplexTypeDescriptor(typeOrExpr, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
        }

        return parseTypeDescOrExprRhs(typeOrExpr);
    }

    private boolean isExpression(SyntaxKind kind) {
        switch (kind) {
            case NUMERIC_LITERAL:
            case STRING_LITERAL_TOKEN:
            case NIL_LITERAL:
            case NULL_LITERAL:
            case BOOLEAN_LITERAL:
                return true;
            default:
                return kind.compareTo(SyntaxKind.BINARY_EXPRESSION) >= 0 &&
                        kind.compareTo(SyntaxKind.ERROR_CONSTRUCTOR) <= 0;
        }
    }

    /**
     * Parse statement that starts with an empty parenthesis. Empty parenthesis can be
     * 1) Nil literal
     * 2) Nil type-desc
     * 3) Anon-function params
     *
     * @param openParen  Open parenthesis
     * @param closeParen Close parenthesis
     * @return Parsed node
     */
    private STNode parseTypeOrExprStartWithEmptyParenthesis(STNode openParen, STNode closeParen) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case RIGHT_DOUBLE_ARROW_TOKEN:
                STNode params = STNodeFactory.createEmptyNodeList();
                STNode anonFuncParam =
                        STNodeFactory.createImplicitAnonymousFunctionParameters(openParen, params, closeParen);
                return parseImplicitAnonFunc(anonFuncParam, false);
            default:
                return STNodeFactory.createNilLiteralNode(openParen, closeParen);
        }
    }

    private STNode parseAnonFuncExprOrTypedBPWithFuncType(List<STNode> qualifiers) {
        STNode exprOrTypeDesc = parseAnonFuncExprOrFuncTypeDesc(qualifiers);
        if (isAction(exprOrTypeDesc) || isExpression(exprOrTypeDesc.kind)) {
            return exprOrTypeDesc;
        }

        return parseTypedBindingPatternTypeRhs(exprOrTypeDesc, ParserRuleContext.VAR_DECL_STMT);
    }

    /**
     * Parse anon-func-expr or function-type-desc, by resolving the ambiguity.
     *
     * @param qualifiers Preceding qualifiers
     * @return Anon-func-expr or function-type-desc
     */
    private STNode parseAnonFuncExprOrFuncTypeDesc(List<STNode> qualifiers) {
        startContext(ParserRuleContext.FUNC_TYPE_DESC_OR_ANON_FUNC);
        STNode qualifierList;
        STNode functionKeyword = parseFunctionKeyword();
        STNode funcSignature;

        if (peek().kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            funcSignature = parseFuncSignature(true);

            // Validate function type qualifiers
            STNode[] nodes = createFuncTypeQualNodeList(qualifiers, functionKeyword, true);
            qualifierList = nodes[0];
            functionKeyword = nodes[1];

            endContext();
            return parseAnonFuncExprOrFuncTypeDesc(qualifierList, functionKeyword, funcSignature);
        }

        funcSignature = STNodeFactory.createEmptyNode();

        // Validate function type qualifiers
        STNode[] nodes = createFuncTypeQualNodeList(qualifiers, functionKeyword, false);
        qualifierList = nodes[0];
        functionKeyword = nodes[1];

        STNode funcTypeDesc = STNodeFactory.createFunctionTypeDescriptorNode(qualifierList, functionKeyword,
                                                                             funcSignature);
        if (getCurrentContext() != ParserRuleContext.STMT_START_BRACKETED_LIST) {
            switchContext(ParserRuleContext.VAR_DECL_STMT);
            return parseComplexTypeDescriptor(funcTypeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
        }
        return parseComplexTypeDescriptor(funcTypeDesc, ParserRuleContext.TYPE_DESC_IN_TUPLE, false);
    }

    private STNode parseAnonFuncExprOrFuncTypeDesc(STNode qualifierList, STNode functionKeyword, STNode funcSignature) {
        ParserRuleContext currentCtx = getCurrentContext();
        switch (peek().kind) {
            case OPEN_BRACE_TOKEN:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                if (currentCtx != ParserRuleContext.STMT_START_BRACKETED_LIST) {
                    switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                }
                startContext(ParserRuleContext.ANON_FUNC_EXPRESSION);
                // Anon function cannot have missing param-names. So validate it.
                funcSignature = validateAndGetFuncParams((STFunctionSignatureNode) funcSignature);

                STNode funcBody = parseAnonFuncBody(false);
                STNode annots = STNodeFactory.createEmptyNodeList();
                STNode anonFunc = STNodeFactory.createExplicitAnonymousFunctionExpressionNode(annots, qualifierList,
                        functionKeyword, funcSignature, funcBody);
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, anonFunc, false, true);
            case IDENTIFIER_TOKEN:
            default:
                STNode funcTypeDesc = STNodeFactory.createFunctionTypeDescriptorNode(qualifierList, functionKeyword,
                        funcSignature);
                if (currentCtx != ParserRuleContext.STMT_START_BRACKETED_LIST) {
                    switchContext(ParserRuleContext.VAR_DECL_STMT);
                    return parseComplexTypeDescriptor(funcTypeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN,
                            true);
                }
                return parseComplexTypeDescriptor(funcTypeDesc, ParserRuleContext.TYPE_DESC_IN_TUPLE, false);
        }
    }

    private STNode parseTypeDescOrExprRhs(STNode typeOrExpr) {
        STToken nextToken = peek();
        STNode typeDesc;
        switch (nextToken.kind) {
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
                STToken nextNextToken = peek(2);
                if (nextNextToken.kind == SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode pipeOrAndToken = parseBinaryOperator();
                STNode rhsTypeDescOrExpr = parseTypeDescOrExpr();
                if (isExpression(rhsTypeDescOrExpr.kind)) {
                    return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr,
                            pipeOrAndToken, rhsTypeDescOrExpr);
                }

                typeDesc = getTypeDescFromExpr(typeOrExpr);
                rhsTypeDescOrExpr = getTypeDescFromExpr(rhsTypeDescOrExpr);
                return mergeTypes(typeDesc, pipeOrAndToken, rhsTypeDescOrExpr);
            case IDENTIFIER_TOKEN:
            case QUESTION_MARK_TOKEN:
                // treat as type
                // We haven't parsed the type-desc as a type-desc (parsed as an identifier/expr).
                // Therefore handle the context manually here.
                typeDesc = parseComplexTypeDescriptor(getTypeDescFromExpr(typeOrExpr), 
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, false);
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
            case ELLIPSIS_TOKEN:
                STNode ellipsis = parseEllipsis();
                typeOrExpr = getTypeDescFromExpr(typeOrExpr);
                return STNodeFactory.createRestDescriptorNode(typeOrExpr, ellipsis);
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundAssignment(nextToken.kind)) {
                    return typeOrExpr;
                }

                // If the next token is part of a valid expression, then still parse it
                // as a statement that starts with an expression.
                if (isValidExprRhsStart(nextToken.kind, typeOrExpr.kind)) {
                    return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, typeOrExpr, false, false, false, false);
                }

                recover(peek(), ParserRuleContext.TYPE_DESC_OR_EXPR_RHS);
                return parseTypeDescOrExprRhs(typeOrExpr);
        }
    }

    private boolean isAmbiguous(STNode node) {
        switch (node.kind) {
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case NIL_LITERAL:
            case NULL_LITERAL:
            case NUMERIC_LITERAL:
            case STRING_LITERAL:
            case BOOLEAN_LITERAL:
            case BRACKETED_LIST:
                return true;
            case BINARY_EXPRESSION:
                STBinaryExpressionNode binaryExpr = (STBinaryExpressionNode) node;
                if (binaryExpr.operator.kind != SyntaxKind.PIPE_TOKEN ||
                        binaryExpr.operator.kind == SyntaxKind.BITWISE_AND_TOKEN) {
                    return false;
                }
                return isAmbiguous(binaryExpr.lhsExpr) && isAmbiguous(binaryExpr.rhsExpr);
            case BRACED_EXPRESSION:
                return isAmbiguous(((STBracedExpressionNode) node).expression);
            case INDEXED_EXPRESSION:
                STIndexedExpressionNode indexExpr = (STIndexedExpressionNode) node;
                if (!isAmbiguous(indexExpr.containerExpression)) {
                    return false;
                }

                STNode keys = indexExpr.keyExpression;
                for (int i = 0; i < keys.bucketCount(); i++) {
                    STNode item = keys.childInBucket(i);
                    if (item.kind == SyntaxKind.COMMA_TOKEN) {
                        continue;
                    }

                    if (!isAmbiguous(item)) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private boolean isAllBasicLiterals(STNode node) {
        switch (node.kind) {
            case NIL_LITERAL:
            case NULL_LITERAL:
            case NUMERIC_LITERAL:
            case STRING_LITERAL:
            case BOOLEAN_LITERAL:
                return true;
            case BINARY_EXPRESSION:
                STBinaryExpressionNode binaryExpr = (STBinaryExpressionNode) node;
                if (binaryExpr.operator.kind != SyntaxKind.PIPE_TOKEN ||
                        binaryExpr.operator.kind == SyntaxKind.BITWISE_AND_TOKEN) {
                    return false;
                }
                return isAmbiguous(binaryExpr.lhsExpr) && isAmbiguous(binaryExpr.rhsExpr);
            case BRACED_EXPRESSION:
                return isAmbiguous(((STBracedExpressionNode) node).expression);
            case BRACKETED_LIST:
                STAmbiguousCollectionNode list = (STAmbiguousCollectionNode) node;
                for (STNode member : list.members) {
                    if (member.kind == SyntaxKind.COMMA_TOKEN) {
                        continue;
                    }

                    if (!isAllBasicLiterals(member)) {
                        return false;
                    }
                }

                return true;
            case UNARY_EXPRESSION:
                STUnaryExpressionNode unaryExpr = (STUnaryExpressionNode) node;
                if (unaryExpr.unaryOperator.kind != SyntaxKind.PLUS_TOKEN &&
                        unaryExpr.unaryOperator.kind != SyntaxKind.MINUS_TOKEN) {
                    return false;
                }

                return isNumericLiteral(unaryExpr.expression);
            default:
                return false;
        }
    }

    private boolean isNumericLiteral(STNode node) {
        switch (node.kind) {
            case NUMERIC_LITERAL:
                return true;
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
     * | wildcard-binding-pattern
     * | list-binding-pattern
     * | mapping-binding-pattern
     * | functional-binding-pattern
     * <br/><br/>
     * <p>
     * capture-binding-pattern := variable-name
     * variable-name := identifier
     * <br/><br/>
     * <p>
     * wildcard-binding-pattern := _
     * list-binding-pattern := [ list-member-binding-patterns ]
     * <br/>
     * list-member-binding-patterns := binding-pattern (, binding-pattern)* [, rest-binding-pattern]
     * | [ rest-binding-pattern ]
     * <br/><br/>
     * <p>
     * mapping-binding-pattern := { field-binding-patterns }
     * field-binding-patterns := field-binding-pattern (, field-binding-pattern)* [, rest-binding-pattern]
     * | [ rest-binding-pattern ]
     * <br/>
     * field-binding-pattern := field-name : binding-pattern | variable-name
     * <br/>
     * rest-binding-pattern := ... variable-name
     * <p>
     * <br/><br/>
     * functional-binding-pattern := functionally-constructible-type-reference ( arg-list-binding-pattern )
     * <br/>
     * arg-list-binding-pattern := positional-arg-binding-patterns [, other-arg-binding-patterns]
     * | other-arg-binding-patterns
     * <br/>
     * positional-arg-binding-patterns := positional-arg-binding-pattern (, positional-arg-binding-pattern)*
     * <br/>
     * positional-arg-binding-pattern := binding-pattern
     * <br/>
     * other-arg-binding-patterns := named-arg-binding-patterns [, rest-binding-pattern]
     * | [rest-binding-pattern]
     * <br/>
     * named-arg-binding-patterns := named-arg-binding-pattern (, named-arg-binding-pattern)*
     * <br/>
     * named-arg-binding-pattern := arg-name = binding-pattern
     * </code>
     *
     * @return binding-pattern node
     */
    private STNode parseBindingPattern() {
        switch (peek().kind) {
            case OPEN_BRACKET_TOKEN:
                return parseListBindingPattern();
            case IDENTIFIER_TOKEN:
                return parseBindingPatternStartsWithIdentifier();
            case OPEN_BRACE_TOKEN:
                return parseMappingBindingPattern();
            case ERROR_KEYWORD:
                return parseErrorBindingPattern();
            default:
                recover(peek(), ParserRuleContext.BINDING_PATTERN);
                return parseBindingPattern();
        }
    }

    private STNode parseBindingPatternStartsWithIdentifier() {
        STNode argNameOrBindingPattern =
                parseQualifiedIdentifier(ParserRuleContext.BINDING_PATTERN_STARTING_IDENTIFIER);
        STToken secondToken = peek();
        if (secondToken.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            startContext(ParserRuleContext.ERROR_BINDING_PATTERN);
            STNode errorKeyword = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.ERROR_KEYWORD,
                    ParserRuleContext.ERROR_KEYWORD);
            return parseErrorBindingPattern(errorKeyword, argNameOrBindingPattern);
        }

        if (argNameOrBindingPattern.kind != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            STNode identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            identifier = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(identifier, argNameOrBindingPattern,
                    DiagnosticErrorCode.ERROR_FIELD_BP_INSIDE_LIST_BP);
            return STNodeFactory.createCaptureBindingPatternNode(identifier);
        }

        return createCaptureOrWildcardBP(((STSimpleNameReferenceNode) argNameOrBindingPattern).name);
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
     * | [ rest-binding-pattern ]
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
        if (isEndOfListBindingPattern(peek().kind) && bindingPatternsList.size() == 0) {
            // Handle empty list binding pattern
            STNode closeBracket = parseCloseBracket();
            STNode bindingPatternsNode = STNodeFactory.createNodeList(bindingPatternsList);
            return STNodeFactory.createListBindingPatternNode(openBracket, bindingPatternsNode, closeBracket);
        }
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
            listBindingPatternRhs = parseListBindingPatternMemberRhs();
            if (listBindingPatternRhs == null) {
                break;
            }

            bindingPatterns.add(listBindingPatternRhs);
            member = parseListBindingPatternMember();
            bindingPatterns.add(member);
            token = peek();
        }

        STNode closeBracket = parseCloseBracket();
        STNode bindingPatternsNode = STNodeFactory.createNodeList(bindingPatterns);
        return STNodeFactory.createListBindingPatternNode(openBracket, bindingPatternsNode, closeBracket);
    }

    private STNode parseListBindingPatternMemberRhs() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.LIST_BINDING_PATTERN_MEMBER_END);
                return parseListBindingPatternMemberRhs();
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
     * Parse list-binding-pattern member.
     * <p>
     * <code>
     * list-binding-pattern := [ list-member-binding-patterns ]
     * <br/>
     * list-member-binding-patterns := binding-pattern (, binding-pattern)* [, rest-binding-pattern]
     * | [ rest-binding-pattern ]
     * </code>
     *
     * @return List binding pattern member
     */
    private STNode parseListBindingPatternMember() {
        switch (peek().kind) {
            case ELLIPSIS_TOKEN:
                return parseRestBindingPattern();
            case OPEN_BRACKET_TOKEN:
            case IDENTIFIER_TOKEN:
            case OPEN_BRACE_TOKEN:
            case ERROR_KEYWORD:
                return parseBindingPattern();
            default:
                recover(peek(), ParserRuleContext.LIST_BINDING_PATTERN_MEMBER);
                return parseListBindingPatternMember();
        }
    }

    /**
     * Parse rest binding pattern.
     * <p>
     * <code>
     * rest-binding-pattern := ... variable-name
     * </code>
     *
     * @return Rest binding pattern node
     */
    private STNode parseRestBindingPattern() {
        startContext(ParserRuleContext.REST_BINDING_PATTERN);
        STNode ellipsis = parseEllipsis();
        STNode varName = parseVariableName();
        endContext();

        STSimpleNameReferenceNode simpleNameReferenceNode =
                (STSimpleNameReferenceNode) STNodeFactory.createSimpleNameReferenceNode(varName);
        return STNodeFactory.createRestBindingPatternNode(ellipsis, simpleNameReferenceNode);
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
        List<STNode> typeDescQualifiers = new ArrayList<>();
        return parseTypedBindingPattern(typeDescQualifiers, context);
    }

    private STNode parseTypedBindingPattern(List<STNode> qualifiers, ParserRuleContext context) {
        STNode typeDesc = parseTypeDescriptor(qualifiers,
                ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true, false, TypePrecedence.DEFAULT);
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
     * | [ rest-binding-pattern ]
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
            endContext();
            return STNodeFactory.createMappingBindingPatternNode(openBrace, bindingPatternsNode, closeBrace);
        }

        List<STNode> bindingPatterns = new ArrayList<>();
        STNode prevMember = parseMappingBindingPatternMember();
        if (prevMember.kind != SyntaxKind.REST_BINDING_PATTERN) {
            bindingPatterns.add(prevMember);
        }
        return parseMappingBindingPattern(openBrace, bindingPatterns, prevMember);
    }

    private STNode parseMappingBindingPattern(STNode openBrace, List<STNode> bindingPatterns, STNode prevMember) {
        STToken token = peek(); // get next valid token
        STNode mappingBindingPatternRhs = null;
        while (!isEndOfMappingBindingPattern(token.kind) && prevMember.kind != SyntaxKind.REST_BINDING_PATTERN) {
            mappingBindingPatternRhs = parseMappingBindingPatternEnd();
            if (mappingBindingPatternRhs == null) {
                break;
            }

            bindingPatterns.add(mappingBindingPatternRhs);
            prevMember = parseMappingBindingPatternMember();
            if (prevMember.kind == SyntaxKind.REST_BINDING_PATTERN) {
                break;
            }
            bindingPatterns.add(prevMember);
            token = peek();
        }

        if (prevMember.kind == SyntaxKind.REST_BINDING_PATTERN) {
            bindingPatterns.add(prevMember);
        }

        STNode closeBrace = parseCloseBrace();
        STNode bindingPatternsNode = STNodeFactory.createNodeList(bindingPatterns);
        endContext();
        return STNodeFactory.createMappingBindingPatternNode(openBrace, bindingPatternsNode, closeBrace);
    }

    /**
     * Parse mapping-binding-pattern entry.
     * <p>
     * <code>
     * mapping-binding-pattern := { field-binding-patterns }
     * <br/><br/>
     * field-binding-patterns := field-binding-pattern (, field-binding-pattern)* [, rest-binding-pattern]
     * | [ rest-binding-pattern ]
     * <br/><br/>
     * field-binding-pattern := field-name : binding-pattern
     * | variable-name
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

    private STNode parseMappingBindingPatternEnd() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                recover(nextToken, ParserRuleContext.MAPPING_BINDING_PATTERN_END);
                return parseMappingBindingPatternEnd();
        }
    }

    /**
     * Parse field-binding-pattern.
     * <code>field-binding-pattern := field-name : binding-pattern | varname</code>
     *
     * @return field-binding-pattern node
     */
    private STNode parseFieldBindingPattern() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                STNode identifier = parseIdentifier(ParserRuleContext.FIELD_BINDING_PATTERN_NAME);
                STNode simpleNameReference = STNodeFactory.createSimpleNameReferenceNode(identifier);
                return parseFieldBindingPattern(simpleNameReference);
            default:
                recover(nextToken, ParserRuleContext.FIELD_BINDING_PATTERN_NAME);
                return parseFieldBindingPattern();
        }
    }

    private STNode parseFieldBindingPattern(STNode simpleNameReference) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case COMMA_TOKEN:
            case CLOSE_BRACE_TOKEN:
                return STNodeFactory.createFieldBindingPatternVarnameNode(simpleNameReference);
            case COLON_TOKEN:
                STNode colon = parseColon();
                STNode bindingPattern = parseBindingPattern();
                return STNodeFactory.createFieldBindingPatternFullNode(simpleNameReference, colon, bindingPattern);
            default:
                recover(nextToken, ParserRuleContext.FIELD_BINDING_PATTERN_END);
                return parseFieldBindingPattern(simpleNameReference);
        }
    }

    private boolean isEndOfMappingBindingPattern(SyntaxKind nextTokenKind) {
        return nextTokenKind == SyntaxKind.CLOSE_BRACE_TOKEN || isEndOfModuleLevelNode(1);
    }

    private STNode parseErrorTypeDescOrErrorBP(STNode annots) {
        STToken nextNextToken = peek(2);
        switch (nextNextToken.kind) {
            case OPEN_PAREN_TOKEN:// Error binding pattern
                return parseAsErrorBindingPattern();
            case LT_TOKEN:
                return parseAsErrorTypeDesc(annots);
            case IDENTIFIER_TOKEN:
                // If the next token is identifier it can be either error a; or error a (errorMessage);
                SyntaxKind nextNextNextTokenKind = peek(3).kind;
                if (nextNextNextTokenKind == SyntaxKind.COLON_TOKEN ||
                        nextNextNextTokenKind == SyntaxKind.OPEN_PAREN_TOKEN) {
                    return parseAsErrorBindingPattern();
                }
                // Fall through.
            default:
                return parseAsErrorTypeDesc(annots);
        }
    }

    private STNode parseAsErrorBindingPattern() {
        startContext(ParserRuleContext.ASSIGNMENT_STMT);
        return parseAssignmentStmtRhs(parseErrorBindingPattern());
    }

    private STNode parseAsErrorTypeDesc(STNode annots) {
        STNode finalKeyword = STNodeFactory.createEmptyNode();
        return parseVariableDecl(getAnnotations(annots), finalKeyword);
    }

    /**
     * Parse error binding pattern node.
     * <p>
     * <code>error-binding-pattern := error [error-type-reference] ( error-arg-list-binding-pattern )</code>
     * <br/><br/>
     * error-arg-list-binding-pattern :=
     * error-message-binding-pattern [, error-cause-binding-pattern] [, error-field-binding-patterns]
     * | [error-field-binding-patterns]
     * <br/><br/>
     * error-message-binding-pattern := simple-binding-pattern
     * <br/><br/>
     * error-cause-binding-pattern := simple-binding-pattern | error-binding-pattern
     * <br/><br/>
     * simple-binding-pattern := capture-binding-pattern | wildcard-binding-pattern
     * <br/><br/>
     * error-field-binding-patterns :=
     * named-arg-binding-pattern (, named-arg-binding-pattern)* [, rest-binding-pattern]
     * | rest-binding-pattern
     * <br/><br/>
     * named-arg-binding-pattern := arg-name = binding-pattern
     *
     * @return Error binding pattern node.
     */
    private STNode parseErrorBindingPattern() {
        startContext(ParserRuleContext.ERROR_BINDING_PATTERN);
        STNode errorKeyword = parseErrorKeyword();
        return parseErrorBindingPattern(errorKeyword);
    }

    private STNode parseErrorBindingPattern(STNode errorKeyword) {
        STToken nextToken = peek();
        STNode typeRef;
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                typeRef = STNodeFactory.createEmptyNode();
                break;
            default:
                if (isPredeclaredIdentifier(nextToken.kind)) {
                    typeRef = parseTypeReference();
                    break;
                }
                recover(peek(), ParserRuleContext.ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS);
                return parseErrorBindingPattern(errorKeyword);
        }
        return parseErrorBindingPattern(errorKeyword, typeRef);
    }

    private STNode parseErrorBindingPattern(STNode errorKeyword, STNode typeRef) {
        STNode openParenthesis = parseOpenParenthesis();
        STNode argListBindingPatterns = parseErrorArgListBindingPatterns();
        STNode closeParenthesis = parseCloseParenthesis();
        endContext();
        return STNodeFactory.createErrorBindingPatternNode(errorKeyword, typeRef, openParenthesis,
                argListBindingPatterns, closeParenthesis);
    }

    /**
     * Parse error arg list binding pattern.
     * <p>
     * <code>
     * error-arg-list-binding-pattern :=
     * error-message-binding-pattern [, error-cause-binding-pattern] [, error-field-binding-patterns]
     * | [error-field-binding-patterns]
     * <br/><br/>
     * <p>
     * error-message-binding-pattern := simple-binding-pattern
     * <br/><br/>
     * <p>
     * error-cause-binding-pattern := simple-binding-pattern | error-binding-pattern
     * <br/><br/>
     * <p>
     * simple-binding-pattern := capture-binding-pattern | wildcard-binding-pattern
     * <br/><br/>
     * <p>
     * error-field-binding-patterns :=
     * named-arg-binding-pattern (, named-arg-binding-pattern)* [, rest-binding-pattern]
     * | rest-binding-pattern
     * <br/><br/>
     * <p>
     * named-arg-binding-pattern := arg-name = binding-pattern
     * </code>
     *
     * @return Error arg list binding patterns.
     */
    private STNode parseErrorArgListBindingPatterns() {
        List<STNode> argListBindingPatterns = new ArrayList<>();
        if (isEndOfErrorFieldBindingPatterns()) {
            return STNodeFactory.createNodeList(argListBindingPatterns);
        }
        return parseErrorArgListBindingPatterns(argListBindingPatterns);
    }

    private STNode parseErrorArgListBindingPatterns(List<STNode> argListBindingPatterns) {
        STNode firstArg = parseErrorArgListBindingPattern(ParserRuleContext.ERROR_ARG_LIST_BINDING_PATTERN_START, true);
        if (firstArg == null) {
            // null marks the end of args
            return STNodeFactory.createNodeList(argListBindingPatterns);
        }

        switch (firstArg.kind) {
            case CAPTURE_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                argListBindingPatterns.add(firstArg);
                return parseErrorArgListBPWithoutErrorMsg(argListBindingPatterns);
            case ERROR_BINDING_PATTERN:
                STNode missingIdentifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
                STNode missingErrorMsgBP = STNodeFactory.createCaptureBindingPatternNode(missingIdentifier);
                missingErrorMsgBP = SyntaxErrors.addDiagnostic(missingErrorMsgBP,
                        DiagnosticErrorCode.ERROR_MISSING_ERROR_MESSAGE_BINDING_PATTERN);
                STNode missingComma = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.COMMA_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_COMMA_TOKEN);
                argListBindingPatterns.add(missingErrorMsgBP);
                argListBindingPatterns.add(missingComma);
                argListBindingPatterns.add(firstArg);
                return parseErrorArgListBPWithoutErrorMsgAndCause(argListBindingPatterns, firstArg.kind);
            case REST_BINDING_PATTERN:
            case NAMED_ARG_BINDING_PATTERN:
                argListBindingPatterns.add(firstArg);
                return parseErrorArgListBPWithoutErrorMsgAndCause(argListBindingPatterns, firstArg.kind);
            default:
                // we reach here for list and mapping binding patterns
                // mark them as invalid and re-parse the first arg.
                addInvalidNodeToNextToken(firstArg, DiagnosticErrorCode.ERROR_BINDING_PATTERN_NOT_ALLOWED);
                return parseErrorArgListBindingPatterns(argListBindingPatterns);
        }
    }

    private STNode parseErrorArgListBPWithoutErrorMsg(List<STNode> argListBindingPatterns) {
        STNode argEnd = parseErrorArgsBindingPatternEnd(ParserRuleContext.ERROR_MESSAGE_BINDING_PATTERN_END);
        if (argEnd == null) {
            // null marks the end of args
            return STNodeFactory.createNodeList(argListBindingPatterns);
        }

        STNode secondArg = parseErrorArgListBindingPattern(ParserRuleContext.ERROR_MESSAGE_BINDING_PATTERN_RHS, false);
        assert secondArg != null; // depending on the recovery context we will not get null here
        switch (secondArg.kind) {
            case CAPTURE_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
            case ERROR_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case NAMED_ARG_BINDING_PATTERN:
                argListBindingPatterns.add(argEnd);
                argListBindingPatterns.add(secondArg);
                return parseErrorArgListBPWithoutErrorMsgAndCause(argListBindingPatterns, secondArg.kind);
            default:
                // we reach here for list and mapping binding patterns
                // mark them as invalid and re-parse the second arg.
                updateLastNodeInListWithInvalidNode(argListBindingPatterns, argEnd, null);
                updateLastNodeInListWithInvalidNode(argListBindingPatterns, secondArg,
                        DiagnosticErrorCode.ERROR_BINDING_PATTERN_NOT_ALLOWED);
                return parseErrorArgListBPWithoutErrorMsg(argListBindingPatterns);
        }
    }

    private STNode parseErrorArgListBPWithoutErrorMsgAndCause(List<STNode> argListBindingPatterns,
                                                              SyntaxKind lastValidArgKind) {
        while (!isEndOfErrorFieldBindingPatterns()) {
            STNode argEnd = parseErrorArgsBindingPatternEnd(ParserRuleContext.ERROR_FIELD_BINDING_PATTERN_END);
            if (argEnd == null) {
                // null marks the end of args
                break;
            }
            STNode currentArg = parseErrorArgListBindingPattern(ParserRuleContext.ERROR_FIELD_BINDING_PATTERN, false);
            assert currentArg != null; // depending on the recovery context we will not get null here
            DiagnosticErrorCode errorCode = validateErrorFieldBindingPatternOrder(lastValidArgKind, currentArg.kind);
            if (errorCode == null) {
                argListBindingPatterns.add(argEnd);
                argListBindingPatterns.add(currentArg);
                lastValidArgKind = currentArg.kind;
            } else if (argListBindingPatterns.size() == 0) {
                addInvalidNodeToNextToken(argEnd, null);
                addInvalidNodeToNextToken(currentArg, errorCode);
            } else {
                updateLastNodeInListWithInvalidNode(argListBindingPatterns, argEnd, null);
                updateLastNodeInListWithInvalidNode(argListBindingPatterns, currentArg, errorCode);
            }
        }

        return STNodeFactory.createNodeList(argListBindingPatterns);
    }

    private boolean isEndOfErrorFieldBindingPatterns() {
        SyntaxKind nextTokenKind = peek().kind;
        switch (nextTokenKind) {
            case CLOSE_PAREN_TOKEN:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private STNode parseErrorArgsBindingPatternEnd(ParserRuleContext currentCtx) {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return consume();
            case CLOSE_PAREN_TOKEN:
                return null;
            default:
                recover(peek(), currentCtx);
                return parseErrorArgsBindingPatternEnd(currentCtx);
        }
    }

    private STNode parseErrorArgListBindingPattern(ParserRuleContext context, boolean isFirstArg) {
        switch (peek().kind) {
            case ELLIPSIS_TOKEN:
                return parseRestBindingPattern();
            case IDENTIFIER_TOKEN:
                // Identifier can means two things: either its a named-arg, or its simple binding pattern.
                STNode argNameOrSimpleBindingPattern = consume();
                return parseNamedOrSimpleArgBindingPattern(argNameOrSimpleBindingPattern);
            case OPEN_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case ERROR_KEYWORD:
                return parseBindingPattern();
            case CLOSE_PAREN_TOKEN:
                if (isFirstArg) {
                    // null marks the end of error-arg-list-BP
                    return null;
                }
                // else fall through
            default:
                recover(peek(), context);
                return parseErrorArgListBindingPattern(context, isFirstArg);
        }
    }

    private STNode parseNamedOrSimpleArgBindingPattern(STNode argNameOrSimpleBindingPattern) {
        STToken secondToken = peek();
        switch (secondToken.kind) {
            case EQUAL_TOKEN:
                STNode equal = consume();
                STNode bindingPattern = parseBindingPattern();
                return STNodeFactory.createNamedArgBindingPatternNode(argNameOrSimpleBindingPattern,
                        equal, bindingPattern);
            case COMMA_TOKEN:
            case CLOSE_PAREN_TOKEN:
            default:
                return createCaptureOrWildcardBP(argNameOrSimpleBindingPattern);
        }
    }

    private DiagnosticErrorCode validateErrorFieldBindingPatternOrder(SyntaxKind prevArgKind,
                                                                      SyntaxKind currentArgKind) {
        switch (currentArgKind) {
            case NAMED_ARG_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
                // Nothing is allowed after a rest arg
                if (prevArgKind == SyntaxKind.REST_BINDING_PATTERN) {
                    return DiagnosticErrorCode.ERROR_REST_ARG_FOLLOWED_BY_ANOTHER_ARG;
                }
                return null;
            case CAPTURE_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
            case ERROR_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            default:
                return DiagnosticErrorCode.ERROR_BINDING_PATTERN_NOT_ALLOWED;
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
        return parseTypedBindingPatternTypeRhs(typeDesc, context, true);
    }

    private STNode parseTypedBindingPatternTypeRhs(STNode typeDesc, ParserRuleContext context, boolean isRoot) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN: // Capture/error binding pattern: T x, error T(..)
            case OPEN_BRACE_TOKEN: // Map binding pattern: T { }
            case ERROR_KEYWORD: // Error binding pattern: error T(..)
                STNode bindingPattern = parseBindingPattern();
                return STNodeFactory.createTypedBindingPatternNode(typeDesc, bindingPattern);
            case OPEN_BRACKET_TOKEN:
                // T[..] ..
                STNode typedBindingPattern = parseTypedBindingPatternOrMemberAccess(typeDesc, true, true, context);
                assert typedBindingPattern.kind == SyntaxKind.TYPED_BINDING_PATTERN;
                return typedBindingPattern;
            case CLOSE_PAREN_TOKEN:
            case COMMA_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case CLOSE_BRACE_TOKEN:
                if (!isRoot) {
                    return typeDesc;
                }
                // fall through
            default:
                recover(nextToken, ParserRuleContext.TYPED_BINDING_PATTERN_TYPE_RHS);
                return parseTypedBindingPatternTypeRhs(typeDesc, context, isRoot);
        }
    }

    /**
     * Parse typed-binding pattern with list, array-type-desc, or member-access-expr.
     *
     * @param typeDescOrExpr        Type desc or the expression at the start
     * @param isTypedBindingPattern Is this is a typed-binding-pattern. If this is `false`, then it's still ambiguous
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
        SyntaxKind currentNodeType = getBracketedListNodeType(member, isTypedBindingPattern);
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
            case ARRAY_TYPE_DESC_OR_MEMBER_ACCESS:
                break;
            case NONE:
            default:
                // Ideally we would reach here, only if the parsed member was a name-reference or a possible error-BP.
                // i.e: T[a or T[error(a),

                // Parse separator
                STNode memberEnd = parseBracketedListMemberEnd();
                if (memberEnd != null) {
                    // If there are more than one member, then its definitely a binding pattern.
                    List<STNode> memberList = new ArrayList<>();
                    memberList.add(getBindingPattern(member, true));
                    memberList.add(memberEnd);
                    bindingPattern = parseAsListBindingPattern(openBracket, memberList);
                    typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                    return STNodeFactory.createTypedBindingPatternNode(typeDesc, bindingPattern);
                }
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
        member = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, member, false, true);
        STNode closeBracket = parseCloseBracket();
        endContext();
        STNode keyExpr = STNodeFactory.createNodeList(member);
        STNode memberAccessExpr =
                STNodeFactory.createIndexedExpressionNode(typeNameOrExpr, openBracket, keyExpr, closeBracket);
        return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, memberAccessExpr, false, false);
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

    /**
     * Parse a member of an ambiguous bracketed list. This member could be:
     * 1) Array length
     * 2) Key expression of a member-access-expr
     * 3) A member-binding pattern of a list-binding-pattern.
     *
     * @param isTypedBindingPattern Is this in a definite typed-binding pattern
     * @return Parsed member node
     */
    private STNode parseBracketedListMember(boolean isTypedBindingPattern) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case ASTERISK_TOKEN:
            case STRING_LITERAL_TOKEN:
                return parseBasicLiteral();
            case CLOSE_BRACKET_TOKEN:
                return STNodeFactory.createEmptyNode();
            case OPEN_BRACE_TOKEN:// mapping-binding-pattern
            case ERROR_KEYWORD: // error-binding-pattern
            case ELLIPSIS_TOKEN: // rest binding pattern
            case OPEN_BRACKET_TOKEN: // list-binding-pattern
                return parseStatementStartBracketedListMember();
            case IDENTIFIER_TOKEN:
                if (isTypedBindingPattern) {
                    return parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                }
                break;
            default:
                if ((!isTypedBindingPattern && isValidExpressionStart(nextToken.kind, 1)) ||
                        isQualifiedIdentifierPredeclaredPrefix(nextToken.kind)) {
                    break;
                }

                ParserRuleContext recoverContext =
                        isTypedBindingPattern ? ParserRuleContext.LIST_BINDING_MEMBER_OR_ARRAY_LENGTH
                                : ParserRuleContext.BRACKETED_LIST_MEMBER;
                recover(peek(), recoverContext);
                return parseBracketedListMember(isTypedBindingPattern);
        }

        STNode expr = parseExpression();
        if (isWildcardBP(expr)) {
            return getWildcardBindingPattern(expr);
        }

        // we don't know which one
        return expr;
    }

    /**
     * Treat the current node as an array, and parse the remainder of the binding pattern.
     *
     * @param typeDesc    Type-desc
     * @param openBracket Open bracket
     * @param member      Member
     * @return Parsed node
     */
    private STNode parseAsArrayTypeDesc(STNode typeDesc, STNode openBracket, STNode member, ParserRuleContext context) {
        // In ambiguous scenarios typDesc: T[a] may have parsed as an indexed expression.
        // Therefore make an array-type-desc out of it.
        typeDesc = getTypeDescFromExpr(typeDesc);
        switchContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        startContext(ParserRuleContext.ARRAY_TYPE_DESCRIPTOR);
        STNode closeBracket = parseCloseBracket();
        endContext();
        endContext();
        return parseTypedBindingPatternOrMemberAccessRhs(typeDesc, openBracket, member, closeBracket, true, true,
                context);
    }

    private STNode parseBracketedListMemberEnd() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.BRACKETED_LIST_MEMBER_END);
                return parseBracketedListMemberEnd();
        }
    }

    /**
     * We reach here to break ambiguity of T[a]. This could be:
     * 1) Array Type Desc
     * 2) Member access on LHS
     * 3) Typed-binding-pattern
     *
     * @param typeDescOrExpr        Type name or the expr that precede the open-bracket.
     * @param openBracket           Open bracket
     * @param member                Member
     * @param closeBracket          Open bracket
     * @param isTypedBindingPattern Is this is a typed-binding-pattern.
     * @return Specific node that matches to T[a], after solving ambiguity.
     */
    private STNode parseTypedBindingPatternOrMemberAccessRhs(STNode typeDescOrExpr, STNode openBracket, STNode member,
                                                             STNode closeBracket, boolean isTypedBindingPattern,
                                                             boolean allowAssignment, ParserRuleContext context) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN: // Capture binding pattern: T[a] b
            case OPEN_BRACE_TOKEN: // Map binding pattern: T[a] { }
            case ERROR_KEYWORD: // Error binding pattern: T[a] error(..)
                // T[a] is definitely a type-desc.
                STNode typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                STNode arrayTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, typeDesc);
                return parseTypedBindingPatternTypeRhs(arrayTypeDesc, context);
            case OPEN_BRACKET_TOKEN: // T[a][b]..
                if (isTypedBindingPattern) {
                    typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                    arrayTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, typeDesc);
                    return parseTypedBindingPatternTypeRhs(arrayTypeDesc, context);
                }

                // T[a] could be member-access or array-type-desc.
                STNode keyExpr = getKeyExpr(member);
                STNode expr =
                        STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr, closeBracket);
                return parseTypedBindingPatternOrMemberAccess(expr, false, allowAssignment, context);
            case QUESTION_MARK_TOKEN:
                // T[a]? --> Treat T[a] as array type
                typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                arrayTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, typeDesc);
                typeDesc = parseComplexTypeDescriptor(arrayTypeDesc,
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
                return parseTypedBindingPatternTypeRhs(typeDesc, context);
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
                // "T[a] | R.." or "T[a] & R.."
                return parseComplexTypeDescInTypedBPOrExprRhs(typeDescOrExpr, openBracket, member, closeBracket,
                        isTypedBindingPattern);
            case IN_KEYWORD:
                // "in" keyword is only valid for for-each-stmt, from-clause and join-clause.
                if (context != ParserRuleContext.FOREACH_STMT &&
                        context != ParserRuleContext.FROM_CLAUSE &&
                        context != ParserRuleContext.JOIN_CLAUSE) {
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

                keyExpr = getKeyExpr(member);
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
                    keyExpr = getKeyExpr(member);
                    return STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr,
                            closeBracket);
                }
                // fall through
            default:
                if (!isTypedBindingPattern && isValidExprRhsStart(nextToken.kind, closeBracket.kind)) {
                    // We come here if T[a] is in some expression context.
                    keyExpr = getKeyExpr(member);
                    typeDescOrExpr = getExpression(typeDescOrExpr);
                    return STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr,
                            closeBracket);
                }

                break;
        }

        ParserRuleContext recoveryCtx = ParserRuleContext.BRACKETED_LIST_RHS;
        if (isTypedBindingPattern) {
            recoveryCtx = ParserRuleContext.TYPE_DESC_RHS_OR_BP_RHS;
        }

        recover(peek(), recoveryCtx);
        return parseTypedBindingPatternOrMemberAccessRhs(typeDescOrExpr, openBracket, member, closeBracket,
                isTypedBindingPattern, allowAssignment, context);
    }

    private STNode getKeyExpr(STNode member) {
        if (member == null) {
            STToken keyIdentifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                    DiagnosticErrorCode.ERROR_MISSING_KEY_EXPR_IN_MEMBER_ACCESS_EXPR);
            STNode missingVarRef = STNodeFactory.createSimpleNameReferenceNode(keyIdentifier);

            return STNodeFactory.createNodeList(missingVarRef);
        }
        return STNodeFactory.createNodeList(member);
    }

    private STNode createTypedBindingPattern(STNode typeDescOrExpr, STNode openBracket, STNode member,
                                             STNode closeBracket) {
        STNode bindingPatterns = STNodeFactory.createEmptyNodeList();
        if (!isEmpty(member)) {
            SyntaxKind memberKind = member.kind;
            if (memberKind == SyntaxKind.NUMERIC_LITERAL || memberKind == SyntaxKind.ASTERISK_LITERAL) {
                STNode typeDesc = getTypeDescFromExpr(typeDescOrExpr);
                STNode arrayTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, typeDesc);
                STToken identifierToken = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_VARIABLE_NAME);
                STNode variableName = STNodeFactory.createCaptureBindingPatternNode(identifierToken);
                return STNodeFactory.createTypedBindingPatternNode(arrayTypeDesc, variableName);
            }

            STNode bindingPattern = getBindingPattern(member, true);
            bindingPatterns = STNodeFactory.createNodeList(bindingPattern);
        }

        STNode bindingPattern = STNodeFactory.createListBindingPatternNode(openBracket, bindingPatterns, closeBracket);
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
     * @param openBracket    Open bracket
     * @param member         Member
     * @param closeBracket   Close bracket
     * @return Parsed node
     */
    private STNode parseComplexTypeDescInTypedBPOrExprRhs(STNode typeDescOrExpr, STNode openBracket, STNode member,
                                                             STNode closeBracket, boolean isTypedBindingPattern) {
        STNode pipeOrAndToken = parseUnionOrIntersectionToken();
        STNode typedBindingPatternOrExpr = parseTypedBindingPatternOrExpr(false);

        if (typedBindingPatternOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
            // Treat T[a] as an array-type-desc. But we dont know what R is.
            // R could be R[b] or R[b, c]
            STNode lhsTypeDesc = getTypeDescFromExpr(typeDescOrExpr);
            lhsTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, lhsTypeDesc);

            STTypedBindingPatternNode rhsTypedBindingPattern = (STTypedBindingPatternNode) typedBindingPatternOrExpr;
            STNode rhsTypeDesc = rhsTypedBindingPattern.typeDescriptor;

            STNode newTypeDesc = mergeTypes(lhsTypeDesc, pipeOrAndToken, rhsTypeDesc);
            return STNodeFactory.createTypedBindingPatternNode(newTypeDesc, rhsTypedBindingPattern.bindingPattern);
        }

        if (isTypedBindingPattern) {
            // If we reach here typedBindingPatternOrExpr is parsed as expression
            STNode lhsTypeDesc = getTypeDescFromExpr(typeDescOrExpr);
            lhsTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, lhsTypeDesc);
            return createCaptureBPWithMissingVarName(lhsTypeDesc, pipeOrAndToken, typedBindingPatternOrExpr);
        }

        STNode keyExpr = getExpression(member);
        STNode containerExpr = getExpression(typeDescOrExpr);
        STNode lhsExpr =
                STNodeFactory.createIndexedExpressionNode(containerExpr, openBracket, keyExpr, closeBracket);
        return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, lhsExpr, pipeOrAndToken,
                typedBindingPatternOrExpr);

    }

    /**
     * Merges two types separated by <code>|</code> or <code>&</code> into one type, while taking precedence
     * and associativity into account.
     *
     * @param lhsTypeDesc    lhs type
     * @param pipeOrAndToken pipe or bitwise-and token
     * @param rhsTypeDesc    rhs type
     * @return a TypeDescriptorNode
     */
    private STNode mergeTypes(STNode lhsTypeDesc, STNode pipeOrAndToken, STNode rhsTypeDesc) {
        if (pipeOrAndToken.kind == SyntaxKind.PIPE_TOKEN) {
            return mergeTypesWithUnion(lhsTypeDesc, pipeOrAndToken, rhsTypeDesc);
        } else {
            return mergeTypesWithIntersection(lhsTypeDesc, pipeOrAndToken, rhsTypeDesc);
        }
    }

    /**
     * Merges two types separated by <code>|</code> into one type, while taking precedence
     * and associativity into account.
     *
     * @param lhsTypeDesc lhs type
     * @param pipeToken   pipe token
     * @param rhsTypeDesc rhs type
     * @return a TypeDescriptorNode
     */
    private STNode mergeTypesWithUnion(STNode lhsTypeDesc, STNode pipeToken, STNode rhsTypeDesc) {
        if (rhsTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
            // We reach here for A[]|B|C. B and C itself can be union.
            // If we directly create a union, that will be A[] | (B|C).
            // For left-associativity, re-arrange it to (A[]|B) | C.

            STUnionTypeDescriptorNode rhsUnionTypeDesc = (STUnionTypeDescriptorNode) rhsTypeDesc;
            return replaceLeftMostUnionWithAUnion(lhsTypeDesc, pipeToken, rhsUnionTypeDesc);
        } else {
            return createUnionTypeDesc(lhsTypeDesc, pipeToken, rhsTypeDesc);
        }
    }

    /**
     * Merges two types separated by <code>&</code> into one type, while taking precedence
     * and associativity into account.
     *
     * @param lhsTypeDesc     lhs type
     * @param bitwiseAndToken bitwise-and token
     * @param rhsTypeDesc     rhs type
     * @return a TypeDescriptorNode
     */
    private STNode mergeTypesWithIntersection(STNode lhsTypeDesc, STNode bitwiseAndToken, STNode rhsTypeDesc) {
        if (lhsTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
            // We reach here for A[] & B|C and A[] & B&C, When A is a Union.
            STUnionTypeDescriptorNode lhsUnionTypeDesc = (STUnionTypeDescriptorNode) lhsTypeDesc;
            if (rhsTypeDesc.kind == SyntaxKind.INTERSECTION_TYPE_DESC) {
                rhsTypeDesc = replaceLeftMostIntersectionWithAIntersection(lhsUnionTypeDesc.rightTypeDesc,
                        bitwiseAndToken, (STIntersectionTypeDescriptorNode) rhsTypeDesc);
                return createUnionTypeDesc(lhsUnionTypeDesc.leftTypeDesc, lhsUnionTypeDesc.pipeToken, rhsTypeDesc);
            } else if (rhsTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
                rhsTypeDesc = replaceLeftMostUnionWithAIntersection(lhsUnionTypeDesc.rightTypeDesc,
                        bitwiseAndToken, (STUnionTypeDescriptorNode) rhsTypeDesc);
                return replaceLeftMostUnionWithAUnion(lhsUnionTypeDesc.leftTypeDesc,
                        lhsUnionTypeDesc.pipeToken, (STUnionTypeDescriptorNode) rhsTypeDesc);
            } else {
                rhsTypeDesc = createIntersectionTypeDesc(lhsUnionTypeDesc.rightTypeDesc, bitwiseAndToken, rhsTypeDesc);
                return createUnionTypeDesc(lhsUnionTypeDesc.leftTypeDesc, lhsUnionTypeDesc.pipeToken, rhsTypeDesc);
            }
        }

        if (rhsTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
            // We reach here for A[] & B|C. B and C itself can be union/intersection.
            // If we directly create a intersection, that will be A[] & (B|C).
            // Therefore, re-arrange it to (A[] & B) | C.

            STUnionTypeDescriptorNode rhsUnionTypeDesc = (STUnionTypeDescriptorNode) rhsTypeDesc;
            return replaceLeftMostUnionWithAIntersection(lhsTypeDesc, bitwiseAndToken, rhsUnionTypeDesc);
        } else if (rhsTypeDesc.kind == SyntaxKind.INTERSECTION_TYPE_DESC) {
            // We reach here for A[] & B&C. B and C itself can be intersection.
            // If we directly create a intersection, that will be A[] & (B&C).
            // For left-associativity, re-arrange it to (A[] & B) & C.

            STIntersectionTypeDescriptorNode rhsIntSecTypeDesc = (STIntersectionTypeDescriptorNode) rhsTypeDesc;
            return replaceLeftMostIntersectionWithAIntersection(lhsTypeDesc, bitwiseAndToken, rhsIntSecTypeDesc);
        } else {
            return createIntersectionTypeDesc(lhsTypeDesc, bitwiseAndToken, rhsTypeDesc);
        }
    }

    private STNode replaceLeftMostUnionWithAUnion(STNode typeDesc, STNode pipeToken,
                                                  STUnionTypeDescriptorNode unionTypeDesc) {
        STNode leftTypeDesc = unionTypeDesc.leftTypeDesc;

        // e.g. E[] | ((A|B)|C)
        // To: ((E[]|A)|B) | C
        if (leftTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
            return unionTypeDesc.replace(unionTypeDesc.leftTypeDesc,
                    replaceLeftMostUnionWithAUnion(typeDesc, pipeToken, (STUnionTypeDescriptorNode) leftTypeDesc));
        }

        // e.g. E[] | (A|B)
        // To: (E[]|A) | B
        leftTypeDesc = createUnionTypeDesc(typeDesc, pipeToken, leftTypeDesc);
        return unionTypeDesc.replace(unionTypeDesc.leftTypeDesc, leftTypeDesc);
    }
    
    private STNode replaceLeftMostUnionWithAIntersection(STNode typeDesc, STNode bitwiseAndToken,
                                                         STUnionTypeDescriptorNode unionTypeDesc) {
        STNode leftTypeDesc = unionTypeDesc.leftTypeDesc;
        
        // e.g. E[] & ((A|B)|C)
        // To: ((E[]&A)|B) | C
        if (leftTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
            return unionTypeDesc.replace(unionTypeDesc.leftTypeDesc,
                    replaceLeftMostUnionWithAIntersection(typeDesc, bitwiseAndToken,
                            (STUnionTypeDescriptorNode) leftTypeDesc));
        }

        // e.g. E[] & ((A&B)|C)
        // To: ((E[]&A)&B) | C
        if (leftTypeDesc.kind == SyntaxKind.INTERSECTION_TYPE_DESC) {
            return unionTypeDesc.replace(unionTypeDesc.leftTypeDesc, 
                    replaceLeftMostIntersectionWithAIntersection(typeDesc, bitwiseAndToken,
                            (STIntersectionTypeDescriptorNode) leftTypeDesc));
        }

        // e.g. E[] & (A|B)
        // To: (E[]&A) | B
        leftTypeDesc = createIntersectionTypeDesc(typeDesc, bitwiseAndToken, leftTypeDesc);
        return unionTypeDesc.replace(unionTypeDesc.leftTypeDesc, leftTypeDesc);
    }

    private STNode replaceLeftMostIntersectionWithAIntersection(STNode typeDesc,
                                                                STNode bitwiseAndToken,
                                                                STIntersectionTypeDescriptorNode intersectionTypeDesc) {
        STNode leftTypeDesc = intersectionTypeDesc.leftTypeDesc;

        // e.g. E[] & ((A&B)&C)
        // To: (E[]&A)&B) & C
        if (leftTypeDesc.kind == SyntaxKind.INTERSECTION_TYPE_DESC) {
            return intersectionTypeDesc.replace(intersectionTypeDesc.leftTypeDesc,
                    replaceLeftMostIntersectionWithAIntersection(typeDesc, bitwiseAndToken,
                            (STIntersectionTypeDescriptorNode) leftTypeDesc));
        }

        // e.g. E[] & (A&B)
        // To: (E[]&A) & B
        leftTypeDesc = createIntersectionTypeDesc(typeDesc, bitwiseAndToken, leftTypeDesc);
        return intersectionTypeDesc.replace(intersectionTypeDesc.leftTypeDesc, leftTypeDesc);
    }
    
    private STNode getArrayTypeDesc(STNode openBracket, STNode member, STNode closeBracket, STNode lhsTypeDesc) {
        if (lhsTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
            STUnionTypeDescriptorNode unionTypeDesc = (STUnionTypeDescriptorNode) lhsTypeDesc;
            STNode middleTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, unionTypeDesc.rightTypeDesc);
            lhsTypeDesc = mergeTypesWithUnion(unionTypeDesc.leftTypeDesc, unionTypeDesc.pipeToken, middleTypeDesc);
        } else if (lhsTypeDesc.kind == SyntaxKind.INTERSECTION_TYPE_DESC) {
            STIntersectionTypeDescriptorNode intersectionTypeDesc = (STIntersectionTypeDescriptorNode) lhsTypeDesc;
            STNode middleTypeDesc =
                    getArrayTypeDesc(openBracket, member, closeBracket, intersectionTypeDesc.rightTypeDesc);
            lhsTypeDesc = mergeTypesWithIntersection(intersectionTypeDesc.leftTypeDesc,
                    intersectionTypeDesc.bitwiseAndToken, middleTypeDesc);
        } else {
            lhsTypeDesc = createArrayTypeDesc(lhsTypeDesc, openBracket, member, closeBracket);
        }

        return lhsTypeDesc;
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
            recover(token, ParserRuleContext.UNION_OR_INTERSECTION_TOKEN);
            return parseUnionOrIntersectionToken();
        }
    }

    /**
     * Infer the type of the ambiguous bracketed list, based on the type of the member.
     *
     * @param memberNode Member node
     * @return Inferred type of the bracketed list
     */
    private SyntaxKind getBracketedListNodeType(STNode memberNode, boolean isTypedBindingPattern) {
        if (isEmpty(memberNode)) {
            // empty brackets. could be array-type or list-binding-pattern
            return SyntaxKind.NONE;
        }

        if (isDefiniteTypeDesc(memberNode.kind)) {
            return SyntaxKind.TUPLE_TYPE_DESC;
        }

        switch (memberNode.kind) {
            case ASTERISK_LITERAL:
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
            case NUMERIC_LITERAL: // member is a const expression. could be array-type or member-access
                if (isTypedBindingPattern) {
                    return SyntaxKind.ARRAY_TYPE_DESC;
                }
                return SyntaxKind.ARRAY_TYPE_DESC_OR_MEMBER_ACCESS;
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case BRACKETED_LIST: // member is again ambiguous
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                return SyntaxKind.NONE;
            case ERROR_CONSTRUCTOR:
                if (isPossibleErrorBindingPattern((STErrorConstructorExpressionNode) memberNode)) {
                    return SyntaxKind.NONE;
                }
                return SyntaxKind.INDEXED_EXPRESSION;
            default:
                if (isTypedBindingPattern) {
                    return SyntaxKind.NONE;
                }
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

    private STNode parseMemberBracketedList() {
        STNode annots = STNodeFactory.createEmptyNodeList();
        return parseStatementStartsWithOpenBracket(annots, false, false);
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
                case TUPLE_TYPE_DESC_OR_LIST_CONST:
                    return parseAsTupleTypeDescOrListConstructor(annots, openBracket, memberList, member, isRoot);
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
        STNode bracketedList = parseStatementStartBracketedListRhs(annots, openBracket, memberList, closeBracket,
                isRoot, possibleMappingField);
        return bracketedList;
    }

    /**
     * Parse a member of a list-binding-pattern, tuple-type-desc, or
     * list-constructor-expr, when the parent is ambiguous.
     *
     * @return Parsed node
     */
    private STNode parseStatementStartBracketedListMember() {
        List<STNode> typeDescQualifiers = new ArrayList<>();
        return parseStatementStartBracketedListMember(typeDescQualifiers);
    }

    private STNode parseStatementStartBracketedListMember(List<STNode> qualifiers) {
        parseTypeDescQualifiers(qualifiers);
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACKET_TOKEN:
                reportInvalidQualifierList(qualifiers);
                return parseMemberBracketedList();
            case IDENTIFIER_TOKEN:
                reportInvalidQualifierList(qualifiers);
                STNode identifier = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                if (isWildcardBP(identifier)) {
                    STNode varName = ((STSimpleNameReferenceNode) identifier).name;
                    return getWildcardBindingPattern(varName);
                }

                nextToken = peek();
                if (nextToken.kind == SyntaxKind.ELLIPSIS_TOKEN) {
                    STNode ellipsis = parseEllipsis();
                    return STNodeFactory.createRestDescriptorNode(identifier, ellipsis);
                }

                if (nextToken.kind != SyntaxKind.OPEN_BRACKET_TOKEN && isValidTypeContinuationToken(nextToken)) {
                    // This will parse union and intersection type over bitwise expression since it is the valid way
                    return parseComplexTypeDescriptor(identifier, ParserRuleContext.TYPE_DESC_IN_TUPLE, false);
                }

                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, identifier, false, true);
            case OPEN_BRACE_TOKEN:
                // mapping-binding-pattern
                reportInvalidQualifierList(qualifiers);
                return parseMappingBindingPatterOrMappingConstructor();
            case ERROR_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                STToken nextNextToken = getNextNextToken();
                if (nextNextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN ||
                        nextNextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseErrorBindingPatternOrErrorConstructor();
                }
                // error-type-desc
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case ELLIPSIS_TOKEN:
                // could be rest-binding or list-rest-member
                reportInvalidQualifierList(qualifiers);
                return parseRestBindingOrSpreadMember();
            case XML_KEYWORD:
            case STRING_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                if (getNextNextToken().kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseExpression(false);
                }
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
                reportInvalidQualifierList(qualifiers);
                if (getNextNextToken().kind == SyntaxKind.LT_TOKEN) {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }
                return parseExpression(false);
            case OPEN_PAREN_TOKEN:
                return parseTypeDescOrExpr(qualifiers);
            case FUNCTION_KEYWORD:
                return parseAnonFuncExprOrFuncTypeDesc(qualifiers);
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    reportInvalidQualifierList(qualifiers);
                    return parseExpression(false);
                }

                if (isTypeStartingToken(nextToken.kind)) {
                    return parseTypeDescriptor(qualifiers, ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }

                recover(peek(), ParserRuleContext.STMT_START_BRACKETED_LIST_MEMBER);
                return parseStatementStartBracketedListMember(qualifiers);
        }
    }

    private STNode parseRestBindingOrSpreadMember() {
        STNode ellipsis = parseEllipsis();
        STNode expr = parseExpression();
        if (expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return STNodeFactory.createRestBindingPatternNode(ellipsis, expr);
        } else {
            return STNodeFactory.createSpreadMemberNode(ellipsis, expr);
        }
    }

    private STNode parseAsTupleTypeDescOrListConstructor(STNode annots, STNode openBracket, List<STNode> memberList,
                                                         STNode member, boolean isRoot) {
        memberList.add(member);
        STNode memberEnd = parseBracketedListMemberEnd();

        STNode tupleTypeDescOrListCons;
        if (memberEnd == null) {
            // We reach here if it is still ambiguous, even after parsing the full list.
            STNode closeBracket = parseCloseBracket();
            tupleTypeDescOrListCons =
                    parseTupleTypeDescOrListConstructorRhs(openBracket, memberList, closeBracket, isRoot);
        } else {
            memberList.add(memberEnd);
            tupleTypeDescOrListCons = parseTupleTypeDescOrListConstructor(annots, openBracket, memberList, isRoot);
        }

        return tupleTypeDescOrListCons;
    }

    /**
     * Parse tuple type desc or list constructor.
     *
     * @return Parsed node
     */
    private STNode parseTupleTypeDescOrListConstructor(STNode annots) {
        startContext(ParserRuleContext.BRACKETED_LIST);
        STNode openBracket = parseOpenBracket();
        List<STNode> memberList = new ArrayList<>();
        return parseTupleTypeDescOrListConstructor(annots, openBracket, memberList, false);
    }

    private STNode parseTupleTypeDescOrListConstructor(STNode annots, STNode openBracket, List<STNode> memberList,
                                                       boolean isRoot) {
        // Parse the members
        STToken nextToken = peek();
        while (!isBracketedListEnd(nextToken.kind)) {
            // Parse member
            STNode member = parseTupleTypeDescOrListConstructorMember(annots);
            SyntaxKind currentNodeType = getParsingNodeTypeOfTupleTypeOrListCons(member);

            switch (currentNodeType) {
                case LIST_CONSTRUCTOR:
                    // If the member type was figured out as a list constructor, then parse the
                    // remaining members as list constructor members and be done with it.
                    return parseAsListConstructor(openBracket, memberList, member, isRoot);
                case TUPLE_TYPE_DESC:
                    // If the member type was figured out as a tuple-type-desc member, then parse the
                    // remaining members as tuple type members and be done with it.
                    return parseAsTupleTypeDesc(annots, openBracket, memberList, member, isRoot);
                case TUPLE_TYPE_DESC_OR_LIST_CONST:
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
        return parseTupleTypeDescOrListConstructorRhs(openBracket, memberList, closeBracket, isRoot);
    }

    private STNode parseTupleTypeDescOrListConstructorMember(STNode annots) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACKET_TOKEN:
                // we don't know which one
                return parseTupleTypeDescOrListConstructor(annots);
            case IDENTIFIER_TOKEN:
                STNode identifier = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                // we don't know which one can be array type desc or expr
                if (peek().kind == SyntaxKind.ELLIPSIS_TOKEN) {
                    STNode ellipsis = parseEllipsis();
                    return STNodeFactory.createRestDescriptorNode(identifier, ellipsis);
                }
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, identifier, false, false);
            case OPEN_BRACE_TOKEN:
                // mapping-const
                return parseMappingConstructorExpr();
            case ERROR_KEYWORD:
                STToken nextNextToken = getNextNextToken();
                if (nextNextToken.kind == SyntaxKind.OPEN_PAREN_TOKEN ||
                        nextNextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseErrorConstructorExpr(false);
                }
                // error-type-desc
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case XML_KEYWORD:
            case STRING_KEYWORD:
                if (getNextNextToken().kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseExpression(false);
                }
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
                if (getNextNextToken().kind == SyntaxKind.LT_TOKEN) {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }
                return parseExpression(false);
            case OPEN_PAREN_TOKEN:
                return parseTypeDescOrExpr();
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    return parseExpression(false);
                }

                if (isTypeStartingToken(nextToken.kind)) {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }

                recover(peek(), ParserRuleContext.TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER);
                return parseTupleTypeDescOrListConstructorMember(annots);
        }
    }

    private SyntaxKind getParsingNodeTypeOfTupleTypeOrListCons(STNode memberNode) {
        // We can use the same method
        return getStmtStartBracketedListType(memberNode);
    }

    private STNode parseTupleTypeDescOrListConstructorRhs(STNode openBracket, List<STNode> members, STNode closeBracket,
                                                          boolean isRoot) {
        STNode tupleTypeOrListConst;
        switch (peek().kind) {
            case COMMA_TOKEN: // [a, b, c],
            case CLOSE_BRACE_TOKEN: // [a, b, c]}
            case CLOSE_BRACKET_TOKEN: // [a, b, c]]
            case PIPE_TOKEN: // [a, b, c] |
            case BITWISE_AND_TOKEN: // [a, b, c] &
                if (!isRoot) {
                    endContext();
                    return new STAmbiguousCollectionNode(SyntaxKind.TUPLE_TYPE_DESC_OR_LIST_CONST, openBracket, members,
                            closeBracket);
                }
                // fall through
            default:
                if (isValidExprRhsStart(peek().kind, closeBracket.kind) ||
                        (isRoot && peek().kind == SyntaxKind.EQUAL_TOKEN)) {
                    members = getExpressionList(members, false);
                    STNode memberExpressions = STNodeFactory.createNodeList(members);
                    tupleTypeOrListConst = STNodeFactory.createListConstructorExpressionNode(openBracket,
                            memberExpressions, closeBracket);
                    break;
                }

                // Treat everything else as tuple type desc
                STNode memberTypeDescs = STNodeFactory.createNodeList(getTypeDescList(members));
                STNode tupleTypeDesc =
                        STNodeFactory.createTupleTypeDescriptorNode(openBracket, memberTypeDescs, closeBracket);
                tupleTypeOrListConst =
                        parseComplexTypeDescriptor(tupleTypeDesc, ParserRuleContext.TYPE_DESC_IN_TUPLE, false);
        }

        endContext();

        if (!isRoot) {
            return tupleTypeOrListConst;
        }

        STNode annots = STNodeFactory.createEmptyNodeList();
        return parseStmtStartsWithTupleTypeOrExprRhs(annots, tupleTypeOrListConst, isRoot);

    }

    private STNode parseStmtStartsWithTupleTypeOrExprRhs(STNode annots, STNode tupleTypeOrListConst, boolean isRoot) {
        if (tupleTypeOrListConst.kind.compareTo(SyntaxKind.RECORD_TYPE_DESC) >= 0 &&
                tupleTypeOrListConst.kind.compareTo(SyntaxKind.TYPEDESC_TYPE_DESC) <= 0) {
            List<STNode> varDeclQualifiers = new ArrayList<>();
            STNode typedBindingPattern =
                    parseTypedBindingPatternTypeRhs(tupleTypeOrListConst, ParserRuleContext.VAR_DECL_STMT, isRoot);
            if (!isRoot) {
                return typedBindingPattern;
            }
            switchContext(ParserRuleContext.VAR_DECL_STMT);
            return parseVarDeclRhs(annots, varDeclQualifiers, typedBindingPattern, false);
        }

        STNode expr = getExpression(tupleTypeOrListConst);
        expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, expr, false, true);
        return parseStatementStartWithExprRhs(expr);
    }

    private STNode parseAsTupleTypeDesc(STNode annots, STNode openBracket, List<STNode> memberList, STNode member,
                                        boolean isRoot) {
        memberList = getTypeDescList(memberList);
        STNode tupleTypeMembers = parseTupleTypeMembers(member, memberList);
        STNode closeBracket = parseCloseBracket();

        STNode tupleType = STNodeFactory.createTupleTypeDescriptorNode(openBracket, tupleTypeMembers, closeBracket);
        STNode typeDesc =
                parseComplexTypeDescriptor(tupleType, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
        endContext();

        if (!isRoot) {
            return typeDesc;
        }

        STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT, isRoot);

        switchContext(ParserRuleContext.VAR_DECL_STMT);
        return parseVarDeclRhs(annots, new ArrayList<>(), typedBindingPattern, false);
    }

    private STNode parseAsListBindingPattern(STNode openBracket, List<STNode> memberList, STNode member,
                                             boolean isRoot) {
        memberList = getBindingPatternsList(memberList, true);
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
        memberList = getBindingPatternsList(memberList, true);
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
        if (memberNode.kind.compareTo(SyntaxKind.RECORD_TYPE_DESC) >= 0 &&
                memberNode.kind.compareTo(SyntaxKind.FUTURE_TYPE_DESC) <= 0) {
            return SyntaxKind.TUPLE_TYPE_DESC;
        }

        switch (memberNode.kind) {
            case WILDCARD_BINDING_PATTERN:
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case ERROR_BINDING_PATTERN:
                return SyntaxKind.LIST_BINDING_PATTERN;
            case QUALIFIED_NAME_REFERENCE: // a qualified-name-ref can only be a type-ref
            case REST_TYPE:
                return SyntaxKind.TUPLE_TYPE_DESC;
            case LIST_CONSTRUCTOR:
            case MAPPING_CONSTRUCTOR:
            case SPREAD_MEMBER:
                return SyntaxKind.LIST_CONSTRUCTOR;
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            case REST_BINDING_PATTERN:
                // can be either list-bp or list-constructor. Cannot be a tuple-type-desc
                return SyntaxKind.LIST_BP_OR_LIST_CONSTRUCTOR;
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case BRACKETED_LIST: // member is again ambiguous
                return SyntaxKind.NONE;
            case ERROR_CONSTRUCTOR:
                if (isPossibleErrorBindingPattern((STErrorConstructorExpressionNode) memberNode)) {
                    return SyntaxKind.NONE;
                }
                return SyntaxKind.LIST_CONSTRUCTOR;
            case INDEXED_EXPRESSION:
                return SyntaxKind.TUPLE_TYPE_DESC_OR_LIST_CONST;
            default:
                if (isExpression(memberNode.kind) && !isAllBasicLiterals(memberNode) && !isAmbiguous(memberNode)) {
                    return SyntaxKind.LIST_CONSTRUCTOR;
                }

                // can be any of the three.
                return SyntaxKind.NONE;
        }
    }

    private boolean isPossibleErrorBindingPattern(STErrorConstructorExpressionNode errorConstructor) {
        STNode args = errorConstructor.arguments;
        int size = args.bucketCount();

        for (int i = 0; i < size; i++) {
            STNode arg = args.childInBucket(i);
            if (arg.kind != SyntaxKind.NAMED_ARG && arg.kind != SyntaxKind.POSITIONAL_ARG &&
                    arg.kind != SyntaxKind.REST_ARG) {
                continue;
            }

            if (!isPosibleArgBindingPattern((STFunctionArgumentNode) arg)) {
                return false;
            }
        }

        return true;
    }

    private boolean isPosibleArgBindingPattern(STFunctionArgumentNode arg) {
        switch (arg.kind) {
            case POSITIONAL_ARG:
                STNode expr = ((STPositionalArgumentNode) arg).expression;
                return isPosibleBindingPattern(expr);
            case NAMED_ARG:
                expr = ((STNamedArgumentNode) arg).expression;
                return isPosibleBindingPattern(expr);
            case REST_ARG:
                expr = ((STRestArgumentNode) arg).expression;
                return expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE;
            default:
                return false;
        }
    }

    private boolean isPosibleBindingPattern(STNode node) {
        switch (node.kind) {
            case SIMPLE_NAME_REFERENCE:
                return true;
            case LIST_CONSTRUCTOR:
                STListConstructorExpressionNode listConstructor = (STListConstructorExpressionNode) node;
                for (int i = 0; i < listConstructor.bucketCount(); i++) {
                    STNode expr = listConstructor.childInBucket(i);
                    if (!isPosibleBindingPattern(expr)) {
                        return false;
                    }
                }
                return true;
            case MAPPING_CONSTRUCTOR:
                STMappingConstructorExpressionNode mappingConstructor = (STMappingConstructorExpressionNode) node;
                for (int i = 0; i < mappingConstructor.bucketCount(); i++) {
                    STNode expr = mappingConstructor.childInBucket(i);
                    if (!isPosibleBindingPattern(expr)) {
                        return false;
                    }
                }
                return true;
            case SPECIFIC_FIELD:
                STSpecificFieldNode specificField = (STSpecificFieldNode) node;
                if (specificField.readonlyKeyword != null) {
                    return false;
                }

                if (specificField.valueExpr == null) {
                    return true;
                }

                return isPosibleBindingPattern(specificField.valueExpr);
            case ERROR_CONSTRUCTOR:
                return isPossibleErrorBindingPattern((STErrorConstructorExpressionNode) node);
            default:
                return false;
        }
    }

    private STNode parseStatementStartBracketedListRhs(STNode annots, STNode openBracket, List<STNode> members,
                                                    STNode closeBracket, boolean isRoot, boolean possibleMappingField) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EQUAL_TOKEN:
                if (!isRoot) {
                    endContext();
                    return new STAmbiguousCollectionNode(SyntaxKind.BRACKETED_LIST, openBracket, members, closeBracket);
                }

                STNode memberBindingPatterns = STNodeFactory.createNodeList(getBindingPatternsList(members, true));
                STNode listBindingPattern = STNodeFactory.createListBindingPatternNode(openBracket,
                        memberBindingPatterns, closeBracket);
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
                    openBracket =
                            SyntaxErrors.addDiagnostic(openBracket, DiagnosticErrorCode.ERROR_MISSING_TUPLE_MEMBER);
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
                endContext();
                if (!isRoot) {
                    return new STAmbiguousCollectionNode(SyntaxKind.BRACKETED_LIST, openBracket, members, closeBracket);
                }

                list = new STAmbiguousCollectionNode(SyntaxKind.BRACKETED_LIST, openBracket, members, closeBracket);
                STNode exprOrTPB = parseTypedBindingPatternOrExprRhs(list, false);
                return parseStmtStartsWithTypedBPOrExprRhs(annots, exprOrTPB);
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
        STNode underscore;
        switch (identifier.kind) {
            case SIMPLE_NAME_REFERENCE:
                STNode varName = ((STSimpleNameReferenceNode) identifier).name;
                underscore = getUnderscoreKeyword((STToken) varName);
                return STNodeFactory.createWildcardBindingPatternNode(underscore);
            case IDENTIFIER_TOKEN:
                underscore = getUnderscoreKeyword((STToken) identifier);
                return STNodeFactory.createWildcardBindingPatternNode(underscore);
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
                    STNode bindingPattern = STNodeFactory.createMappingBindingPatternNode(openBrace, fields,
                            closeBrace);
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

        STNode member = parseStatementStartingBracedListFirstMember(openBrace.isMissing());
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
     * @param openBrace         Open brace
     * @param firstMappingField First member
     * @return Parsed node
     */
    private STNode parseStmtAsMappingBindingPatternStart(STNode openBrace, STNode firstMappingField) {
        switchContext(ParserRuleContext.ASSIGNMENT_STMT);
        startContext(ParserRuleContext.MAPPING_BINDING_PATTERN);
        List<STNode> bindingPatterns = new ArrayList<>();
        if (firstMappingField.kind != SyntaxKind.REST_BINDING_PATTERN) {
            bindingPatterns.add(getBindingPattern(firstMappingField, false));
        }

        STNode mappingBP = parseMappingBindingPattern(openBrace, bindingPatterns, firstMappingField);
        return parseAssignmentStmtRhs(mappingBP);
    }

    /**
     * Parse the rest of the statement, treating the start as a mapping constructor expression.
     *
     * @param openBrace   Open brace
     * @param firstMember First member
     * @return Parsed node
     */
    private STNode parseStmtAsMappingConstructorStart(STNode openBrace, STNode firstMember) {
        switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
        startContext(ParserRuleContext.MAPPING_CONSTRUCTOR);
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
     * @param members   members list
     * @param member    Most recently parsed member
     * @return Parsed node
     */
    private STNode parseAsMappingConstructor(STNode openBrace, List<STNode> members, STNode member) {
        members.add(member);
        members = getExpressionList(members, true);

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
     * @param member    First member
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
            bpOrConstructor = parseMappingBindingPatternOrMappingConstructor(openBrace, members);
        }

        switch (bpOrConstructor.kind) {
            case MAPPING_CONSTRUCTOR:
                // Create the statement
                switchContext(ParserRuleContext.EXPRESSION_STATEMENT);
                STNode expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, bpOrConstructor, false, true);
                return parseStatementStartWithExprRhs(expr);
            case MAPPING_BINDING_PATTERN:
                switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                STNode bindingPattern = getBindingPattern(bpOrConstructor, false);
                return parseAssignmentStmtRhs(bindingPattern);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            default:
                // If this is followed by an assignment, then treat this node as mapping-binding pattern.
                if (peek().kind == SyntaxKind.EQUAL_TOKEN) {
                    switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                    bindingPattern = getBindingPattern(bpOrConstructor, false);
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
    private STNode parseStatementStartingBracedListFirstMember(boolean isOpenBraceMissing) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case READONLY_KEYWORD:
                STNode readonlyKeyword = parseReadonlyKeyword();
                return bracedListMemberStartsWithReadonly(readonlyKeyword);
            case IDENTIFIER_TOKEN:
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseIdentifierRhsInStmtStartingBrace(readonlyKeyword);
            case STRING_LITERAL_TOKEN:
                STNode key = parseStringLiteral();
                if (peek().kind == SyntaxKind.COLON_TOKEN) {
                    readonlyKeyword = STNodeFactory.createEmptyNode();
                    STNode colon = parseColon();
                    STNode valueExpr = parseExpression();
                    return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, valueExpr);
                }
                switchContext(ParserRuleContext.BLOCK_STMT);
                startContext(ParserRuleContext.AMBIGUOUS_STMT);
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
                // If the openBrace is missing we should not rout this as another `BLOCK_STMT`. This is done to prevent
                // infinite loop
                if (isOpenBraceMissing) {
                    readonlyKeyword = STNodeFactory.createEmptyNode();
                    return parseIdentifierRhsInStmtStartingBrace(readonlyKeyword);
                }
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
            case STRING_LITERAL_TOKEN:
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
                STNode typeDesc = createBuiltinSimpleNameReference(readonlyKeyword);
                return parseVarDeclTypeDescRhs(typeDesc, STNodeFactory.createEmptyNodeList(), new ArrayList<>(),
                        true, false);
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
                // could be either [readonly] field-name : value-expr or field-name : binding-pattern
                colon = parseColon();
                if (!isEmpty(readonlyKeyword)) {
                    value = parseExpression();
                    return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, value);
                }

                switch (peek().kind) {
                    case OPEN_BRACKET_TOKEN: // { foo:[
                        STNode bindingPatternOrExpr = parseListBindingPatternOrListConstructor();
                        return getMappingField(identifier, colon, bindingPatternOrExpr);
                    case OPEN_BRACE_TOKEN: // { foo:{
                        bindingPatternOrExpr = parseMappingBindingPatterOrMappingConstructor();
                        return getMappingField(identifier, colon, bindingPatternOrExpr);
                    case ERROR_KEYWORD: // { foo: error
                        bindingPatternOrExpr = parseErrorBindingPatternOrErrorConstructor();
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
                    STNode annots = STNodeFactory.createEmptyNodeList();
                    List<STNode> varDeclQualifiers = new ArrayList<>();
                    return parseVarDeclRhs(annots, varDeclQualifiers, typedBindingPattern, false);
                }

                startContext(ParserRuleContext.AMBIGUOUS_STMT);
                STNode qualifiedIdentifier = parseQualifiedIdentifier(identifier, false);
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
            STNode wildcardBP = getWildcardBindingPattern(secondIdentifier);
            STNode nameRef = STNodeFactory.createSimpleNameReferenceNode(identifier);
            return STNodeFactory.createFieldBindingPatternFullNode(nameRef, colon, wildcardBP);
        }

        // Reach here for something like: "{foo:bar". This could be anything.
        STNode qualifiedNameRef = createQualifiedNameReferenceNode(identifier, colon, secondIdentifier);
        switch (peek().kind) {
            case COMMA_TOKEN:
                // {foo:bar, --> map-literal or binding pattern
                // Return a specific field node since this is ambiguous. Downstream code
                // will convert this to the respective node, once the ambiguity is resolved.
                return STNodeFactory.createSpecificFieldNode(STNodeFactory.createEmptyNode(), identifier, colon,
                        secondNameRef);
            case OPEN_BRACE_TOKEN: // { foo:bar{ --> var-decl with TBP
            case IDENTIFIER_TOKEN: // var-decl
                switchContext(ParserRuleContext.BLOCK_STMT);
                startContext(ParserRuleContext.VAR_DECL_STMT);
                List<STNode> varDeclQualifiers = new ArrayList<>();
                STNode typeBindingPattern =
                        parseTypedBindingPatternTypeRhs(qualifiedNameRef, ParserRuleContext.VAR_DECL_STMT);
                STNode annots = STNodeFactory.createEmptyNodeList();
                return parseVarDeclRhs(annots, varDeclQualifiers, typeBindingPattern, false);
            case OPEN_BRACKET_TOKEN:
                // "{ foo:bar[" Can be (TBP) or (map-literal with member-access) or (statement starts with
                // member-access)
                return parseMemberRhsInStmtStartWithBrace(identifier, colon, secondIdentifier, secondNameRef);
            case QUESTION_MARK_TOKEN:
                // var-decl
                STNode typeDesc = parseComplexTypeDescriptor(qualifiedNameRef,
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
                varDeclQualifiers = new ArrayList<>();
                typeBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
                annots = STNodeFactory.createEmptyNodeList();
                return parseVarDeclRhs(annots, varDeclQualifiers, typeBindingPattern, false);
            case EQUAL_TOKEN:
            case SEMICOLON_TOKEN:
                // stmt start with expr
                return parseStatementStartWithExprRhs(qualifiedNameRef);
            case PIPE_TOKEN:
            case BITWISE_AND_TOKEN:
            default:
                return parseMemberWithExprInRhs(identifier, colon, secondIdentifier, secondNameRef);
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
                if (expr == null) {
                    return SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR;
                }

                // "{foo," and "{foo:bar," is ambiguous
                switch (expr.kind) {
                    case SIMPLE_NAME_REFERENCE:
                    case LIST_BP_OR_LIST_CONSTRUCTOR:
                    case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                        return SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR;
                    case ERROR_BINDING_PATTERN:
                        return SyntaxKind.MAPPING_BINDING_PATTERN;
                    case ERROR_CONSTRUCTOR:
                        if (isPossibleErrorBindingPattern((STErrorConstructorExpressionNode) expr)) {
                            return SyntaxKind.MAPPING_BP_OR_MAPPING_CONSTRUCTOR;
                        }
                        return SyntaxKind.MAPPING_CONSTRUCTOR;
                    default:
                        return SyntaxKind.MAPPING_CONSTRUCTOR;
                }
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
            STNode member = parseMappingBindingPatterOrMappingConstructorMember();
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

    private STNode parseMappingBindingPatterOrMappingConstructorMember() {
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                STNode key = parseIdentifier(ParserRuleContext.MAPPING_FIELD_NAME);
                return parseMappingFieldRhs(key);
            case STRING_LITERAL_TOKEN:
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
                recover(peek(), ParserRuleContext.MAPPING_BP_OR_MAPPING_CONSTRUCTOR_MEMBER);
                return parseMappingBindingPatterOrMappingConstructorMember();
        }
    }

    private STNode parseMappingFieldRhs(STNode key) {
        STNode colon;
        STNode valueExpr;
        switch (peek().kind) {
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
                recover(token, ParserRuleContext.FIELD_BINDING_PATTERN_END);
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return parseSpecificFieldRhs(readonlyKeyword, key);
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

        if (isBindingPattern(expr.kind)) {
            key = STNodeFactory.createSimpleNameReferenceNode(key);
            return STNodeFactory.createFieldBindingPatternFullNode(key, colon, expr);
        }

        STNode readonlyKeyword = STNodeFactory.createEmptyNode();
        return STNodeFactory.createSpecificFieldNode(readonlyKeyword, key, colon, expr);
    }

    private boolean isBindingPattern(SyntaxKind kind) {
        switch (kind) {
            case FIELD_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return true;
            default:
                return false;
        }
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
        members = getBindingPatternsList(members, false);
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
            STNode member = parseListBindingPatternOrListConstructorMember();
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

    private STNode parseListBindingPatternOrListConstructorMember() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACKET_TOKEN:
                // we don't know which one
                return parseListBindingPatternOrListConstructor();
            case IDENTIFIER_TOKEN:
                STNode identifier = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                if (isWildcardBP(identifier)) {
                    return getWildcardBindingPattern(identifier);
                }
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, identifier, false, false);
            case OPEN_BRACE_TOKEN:
                return parseMappingBindingPatterOrMappingConstructor();
            case ELLIPSIS_TOKEN:
                return parseRestBindingOrSpreadMember();
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    return parseExpression();
                }

                recover(peek(), ParserRuleContext.LIST_BP_OR_LIST_CONSTRUCTOR_MEMBER);
                return parseListBindingPatternOrListConstructorMember();
        }
    }

    private SyntaxKind getParsingNodeTypeOfListBPOrListCons(STNode memberNode) {
        switch (memberNode.kind) {
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return SyntaxKind.LIST_BINDING_PATTERN;
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case LIST_BP_OR_LIST_CONSTRUCTOR: // member is again ambiguous
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            case REST_BINDING_PATTERN:
                return SyntaxKind.LIST_BP_OR_LIST_CONSTRUCTOR;
            case SPREAD_MEMBER:
            default:
                return SyntaxKind.LIST_CONSTRUCTOR;
        }
    }

    private STNode parseAsListConstructor(STNode openBracket, List<STNode> memberList, STNode member, boolean isRoot) {
        memberList.add(member);
        memberList = getExpressionList(memberList, false);

        switchContext(ParserRuleContext.LIST_CONSTRUCTOR);
        STNode listMembers = parseListMembers(memberList);
        STNode closeBracket = parseCloseBracket();
        STNode listConstructor =
                STNodeFactory.createListConstructorExpressionNode(openBracket, listMembers, closeBracket);
        endContext();

        STNode expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, listConstructor, false, true);
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
                SyntaxKind nextTokenKind = peek().kind;
                if (isValidExprRhsStart(nextTokenKind, closeBracket.kind) || 
                        nextTokenKind == SyntaxKind.SEMICOLON_TOKEN && isRoot) { // [a, b, c];
                    members = getExpressionList(members, false);
                    STNode memberExpressions = STNodeFactory.createNodeList(members);
                    lbpOrListCons = STNodeFactory.createListConstructorExpressionNode(openBracket, memberExpressions,
                            closeBracket);
                    lbpOrListCons = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, lbpOrListCons, false, true);
                    break;
                }

                // Treat everything else as list-binding-pattern
                members = getBindingPatternsList(members, true);
                STNode bindingPatternsNode = STNodeFactory.createNodeList(members);
                lbpOrListCons = STNodeFactory.createListBindingPatternNode(openBracket, bindingPatternsNode,
                        closeBracket);
                break;
        }

        endContext();

        if (!isRoot) {
            return lbpOrListCons;
        }
        
        if (lbpOrListCons.kind == SyntaxKind.LIST_BINDING_PATTERN) {
            return parseAssignmentStmtRhs(lbpOrListCons);
        } else {
            return parseStatementStartWithExprRhs(lbpOrListCons);
        }
    }

    private STNode parseMemberRhsInStmtStartWithBrace(STNode identifier, STNode colon, STNode secondIdentifier,
                                                      STNode secondNameRef) {
        STNode typedBPOrExpr =
                parseTypedBindingPatternOrMemberAccess(secondNameRef, false, true, ParserRuleContext.AMBIGUOUS_STMT);
        if (isExpression(typedBPOrExpr.kind)) {
            return parseMemberWithExprInRhs(identifier, colon, secondIdentifier, typedBPOrExpr);
        }

        switchContext(ParserRuleContext.BLOCK_STMT);
        startContext(ParserRuleContext.VAR_DECL_STMT);
        List<STNode> varDeclQualifiers = new ArrayList<>();
        STNode annots = STNodeFactory.createEmptyNodeList();

        // We reach here for something like: "{ foo:bar[". But we started parsing the rhs component
        // starting with "bar". Hence if its a typed-binding-pattern, then merge the "foo:" with
        // the rest of the type-desc.
        STTypedBindingPatternNode typedBP = (STTypedBindingPatternNode) typedBPOrExpr;
        STNode qualifiedNameRef = createQualifiedNameReferenceNode(identifier, colon, secondIdentifier);
        STNode newTypeDesc = mergeQualifiedNameWithTypeDesc(qualifiedNameRef, typedBP.typeDescriptor);

        STNode newTypeBP = STNodeFactory.createTypedBindingPatternNode(newTypeDesc, typedBP.bindingPattern);
        STNode publicQualifier = STNodeFactory.createEmptyNode();
        return parseVarDeclRhs(annots, publicQualifier, varDeclQualifiers, newTypeBP, false);
    }

    /**
     * Parse a member that starts with "foo:bar[", in a statement starting with a brace.
     *
     * @param identifier       First identifier of the statement
     * @param colon            Colon that follows the first identifier
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
                STNode readonlyKeyword = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, expr);
            case EQUAL_TOKEN:
            case SEMICOLON_TOKEN:
            default:
                switchContext(ParserRuleContext.BLOCK_STMT);
                startContext(ParserRuleContext.EXPRESSION_STATEMENT);
                // stmt start with expr
                STNode qualifiedName = createQualifiedNameReferenceNode(identifier, colon, secondIdentifier);
                STNode updatedExpr = mergeQualifiedNameWithExpr(qualifiedName, expr);
                return parseStatementStartWithExprRhs(updatedExpr);
        }
    }

    private STNode parseInferredTypeDescDefaultOrExpression() {
        STToken nextToken = peek();
        SyntaxKind nextTokenKind = nextToken.kind;

        if (nextTokenKind == SyntaxKind.LT_TOKEN) {
            return parseInferredTypeDescDefaultOrExpression(consume());
        }

        if (isValidExprStart(nextTokenKind)) {
            return parseExpression();
        }

         recover(nextToken, ParserRuleContext.EXPR_START_OR_INFERRED_TYPEDESC_DEFAULT_START);
         return parseInferredTypeDescDefaultOrExpression();
    }

    private STNode parseInferredTypeDescDefaultOrExpression(STToken ltToken) {
        STToken nextToken = peek();

        if (nextToken.kind == SyntaxKind.GT_TOKEN) {
            return STNodeFactory.createInferredTypedescDefaultNode(ltToken, consume());
        }

        if (isTypeStartingToken(nextToken.kind) || nextToken.kind == SyntaxKind.AT_TOKEN) {
            startContext(ParserRuleContext.TYPE_CAST);
            STNode expr = parseTypeCastExpr(ltToken, true, false, false);
            return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, expr, true, false);
        }

        recover(nextToken, ParserRuleContext.TYPE_CAST_PARAM_START_OR_INFERRED_TYPEDESC_DEFAULT_END);
        return parseInferredTypeDescDefaultOrExpression(ltToken);
    }

    /**
     * Replace the first identifier of an expression, with a given qualified-identifier.
     * Only expressions that can start with "bar[..]" can reach here.
     *
     * @param qualifiedName Qualified identifier to replace simple identifier
     * @param exprOrAction  Expression or action     * @return Updated expression
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
            case FUNCTION_CALL:
                STFunctionCallExpressionNode funcCall = (STFunctionCallExpressionNode) exprOrAction;
                return STNodeFactory.createFunctionCallExpressionNode(qualifiedName, funcCall.openParenToken,
                        funcCall.arguments, funcCall.closeParenToken);
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
                return STNodeFactory.createArrayTypeDescriptorNode(newMemberType, arrayTypeDesc.dimensions);
            case UNION_TYPE_DESC:
                STUnionTypeDescriptorNode unionTypeDesc = (STUnionTypeDescriptorNode) typeDesc;
                STNode newlhsType = mergeQualifiedNameWithTypeDesc(qualifiedName, unionTypeDesc.leftTypeDesc);
                return mergeTypesWithUnion(newlhsType, unionTypeDesc.pipeToken, unionTypeDesc.rightTypeDesc);
            case INTERSECTION_TYPE_DESC:
                STIntersectionTypeDescriptorNode intersectionTypeDesc = (STIntersectionTypeDescriptorNode) typeDesc;
                newlhsType = mergeQualifiedNameWithTypeDesc(qualifiedName, intersectionTypeDesc.leftTypeDesc);
                return mergeTypesWithIntersection(newlhsType, intersectionTypeDesc.bitwiseAndToken,
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
        List<STNode> typeDescList = new ArrayList<>();
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
        if (isDefiniteTypeDesc(expression.kind) || expression.kind == SyntaxKind.COMMA_TOKEN) {
            return expression;
        }
        
        switch (expression.kind) {
            case INDEXED_EXPRESSION:
                return parseArrayTypeDescriptorNode((STIndexedExpressionNode) expression);
            case NUMERIC_LITERAL:
            case BOOLEAN_LITERAL:
            case STRING_LITERAL:
            case NULL_LITERAL:
            case UNARY_EXPRESSION:
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
            case TUPLE_TYPE_DESC_OR_LIST_CONST:    
                STAmbiguousCollectionNode innerList = (STAmbiguousCollectionNode) expression;
                STNode memberTypeDescs = STNodeFactory.createNodeList(getTypeDescList(innerList.members));
                return STNodeFactory.createTupleTypeDescriptorNode(innerList.collectionStartToken, memberTypeDescs,
                        innerList.collectionEndToken);
            case BINARY_EXPRESSION:
                STBinaryExpressionNode binaryExpr = (STBinaryExpressionNode) expression;
                switch (binaryExpr.operator.kind) {
                    case PIPE_TOKEN:
                    case BITWISE_AND_TOKEN:
                        STNode lhsTypeDesc = getTypeDescFromExpr(binaryExpr.lhsExpr);
                        STNode rhsTypeDesc = getTypeDescFromExpr(binaryExpr.rhsExpr);
                        return mergeTypes(lhsTypeDesc, binaryExpr.operator, rhsTypeDesc);
                    default:
                        break;
                }
                return expression;
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
                return expression;
            default:
                STNode simpleTypeDescIdentifier = SyntaxErrors.createMissingTokenWithDiagnostics(
                        SyntaxKind.IDENTIFIER_TOKEN, DiagnosticErrorCode.ERROR_MISSING_TYPE_DESC);
                simpleTypeDescIdentifier = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(simpleTypeDescIdentifier, 
                        expression);
                return STNodeFactory.createSimpleNameReferenceNode(simpleTypeDescIdentifier);
        }
    }

    private List<STNode> getBindingPatternsList(List<STNode> ambibuousList, boolean isListBP) {
        List<STNode> bindingPatterns = new ArrayList<>();
        for (STNode item : ambibuousList) {
            bindingPatterns.add(getBindingPattern(item, isListBP));
        }
        return bindingPatterns;
    }

    private STNode getBindingPattern(STNode ambiguousNode, boolean isListBP) {
        DiagnosticCode errorCode = DiagnosticErrorCode.ERROR_INVALID_BINDING_PATTERN;

        if (isEmpty(ambiguousNode)) {
            return ambiguousNode;
        }

        switch (ambiguousNode.kind) {
            case WILDCARD_BINDING_PATTERN:
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case ERROR_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case FIELD_BINDING_PATTERN:
            case NAMED_ARG_BINDING_PATTERN:
            case COMMA_TOKEN: // when we are iterating through a comma separated list we can reach here with a comma   
                return ambiguousNode;
            case SIMPLE_NAME_REFERENCE:
                STNode varName = ((STSimpleNameReferenceNode) ambiguousNode).name;
                return createCaptureOrWildcardBP(varName);
            case QUALIFIED_NAME_REFERENCE:
                if (isListBP) {
                    errorCode = DiagnosticErrorCode.ERROR_FIELD_BP_INSIDE_LIST_BP;
                    break;
                }
                STQualifiedNameReferenceNode qualifiedName = (STQualifiedNameReferenceNode) ambiguousNode;
                STNode fieldName = STNodeFactory.createSimpleNameReferenceNode(qualifiedName.modulePrefix);
                return STNodeFactory.createFieldBindingPatternFullNode(fieldName, qualifiedName.colon,
                        createCaptureOrWildcardBP(qualifiedName.identifier));
            case BRACKETED_LIST:
            case LIST_BP_OR_LIST_CONSTRUCTOR:
                STAmbiguousCollectionNode innerList = (STAmbiguousCollectionNode) ambiguousNode;
                STNode memberBindingPatterns =
                        STNodeFactory.createNodeList(getBindingPatternsList(innerList.members, true));
                return STNodeFactory.createListBindingPatternNode(innerList.collectionStartToken, memberBindingPatterns,
                        innerList.collectionEndToken);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                innerList = (STAmbiguousCollectionNode) ambiguousNode;
                List<STNode> bindingPatterns = new ArrayList<>();
                for (int i = 0; i < innerList.members.size(); i++) {
                    STNode bp = getBindingPattern(innerList.members.get(i), false);
                    bindingPatterns.add(bp);
                    if (bp.kind == SyntaxKind.REST_BINDING_PATTERN) {
                        break;
                    }
                }
                memberBindingPatterns = STNodeFactory.createNodeList(bindingPatterns);
                return STNodeFactory.createMappingBindingPatternNode(innerList.collectionStartToken,
                        memberBindingPatterns, innerList.collectionEndToken);
            case SPECIFIC_FIELD:
                STSpecificFieldNode field = (STSpecificFieldNode) ambiguousNode;
                fieldName = STNodeFactory.createSimpleNameReferenceNode(field.fieldName);
                if (field.valueExpr == null) {
                    return STNodeFactory.createFieldBindingPatternVarnameNode(fieldName);
                }
                return STNodeFactory.createFieldBindingPatternFullNode(fieldName, field.colon,
                        getBindingPattern(field.valueExpr, false));
            case ERROR_CONSTRUCTOR:
                STErrorConstructorExpressionNode errorCons = (STErrorConstructorExpressionNode) ambiguousNode;
                STNode args = errorCons.arguments;
                int size = args.bucketCount();
                bindingPatterns = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    STNode arg = args.childInBucket(i);
                    bindingPatterns.add(getBindingPattern(arg, false));
                }

                STNode argListBindingPatterns = STNodeFactory.createNodeList(bindingPatterns);
                return STNodeFactory.createErrorBindingPatternNode(errorCons.errorKeyword, errorCons.typeReference,
                        errorCons.openParenToken, argListBindingPatterns, errorCons.closeParenToken);
            case POSITIONAL_ARG:
                STPositionalArgumentNode positionalArg = (STPositionalArgumentNode) ambiguousNode;
                return getBindingPattern(positionalArg.expression, false);
            case NAMED_ARG:
                STNamedArgumentNode namedArg = (STNamedArgumentNode) ambiguousNode;
                STNode bindingPatternArgName = ((STSimpleNameReferenceNode) namedArg.argumentName).name;
                return STNodeFactory.createNamedArgBindingPatternNode(bindingPatternArgName, namedArg.equalsToken,
                        getBindingPattern(namedArg.expression, false));
            case REST_ARG:
                STRestArgumentNode restArg = (STRestArgumentNode) ambiguousNode;
                return STNodeFactory.createRestBindingPatternNode(restArg.ellipsis, restArg.expression);
        }

        STNode identifier = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
        identifier = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(identifier, ambiguousNode, errorCode);
        return STNodeFactory.createCaptureBindingPatternNode(identifier);
    }

    private List<STNode> getExpressionList(List<STNode> ambibuousList, boolean isMappingConstructor) {
        List<STNode> exprList = new ArrayList<STNode>();
        for (STNode item : ambibuousList) {
            exprList.add(getExpression(item, isMappingConstructor));
        }
        return exprList;
    }

    private STNode getExpression(STNode ambiguousNode) {
        return getExpression(ambiguousNode, false);
    }

    private STNode getExpression(STNode ambiguousNode, boolean isInMappingConstructor) {
        if (isEmpty(ambiguousNode) || 
                (isDefiniteExpr(ambiguousNode.kind) && ambiguousNode.kind != SyntaxKind.INDEXED_EXPRESSION) || 
                isDefiniteAction(ambiguousNode.kind) || ambiguousNode.kind == SyntaxKind.COMMA_TOKEN) {
            return ambiguousNode;
        }

        switch (ambiguousNode.kind) {
            case BRACKETED_LIST:
            case LIST_BP_OR_LIST_CONSTRUCTOR:
            case TUPLE_TYPE_DESC_OR_LIST_CONST:
                STAmbiguousCollectionNode innerList = (STAmbiguousCollectionNode) ambiguousNode;
                STNode memberExprs = STNodeFactory.createNodeList(getExpressionList(innerList.members, false));
                return STNodeFactory.createListConstructorExpressionNode(innerList.collectionStartToken, memberExprs,
                        innerList.collectionEndToken);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                innerList = (STAmbiguousCollectionNode) ambiguousNode;
                List<STNode> fieldList = new ArrayList<>();
                for (int i = 0; i < innerList.members.size(); i++) {
                    STNode field = innerList.members.get(i);
                    STNode fieldNode;
                    if (field.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                        STQualifiedNameReferenceNode qualifiedNameRefNode = (STQualifiedNameReferenceNode) field;
                        STNode readOnlyKeyword = STNodeFactory.createEmptyNode();
                        STNode fieldName = qualifiedNameRefNode.modulePrefix;
                        STNode colon = qualifiedNameRefNode.colon;
                        STNode valueExpr = getExpression(qualifiedNameRefNode.identifier);
                        fieldNode = STNodeFactory.createSpecificFieldNode(readOnlyKeyword, fieldName, colon, valueExpr);
                    } else {
                        fieldNode = getExpression(field, true);
                    }

                    fieldList.add(fieldNode);
                }
                STNode fields = STNodeFactory.createNodeList(fieldList);
                return STNodeFactory.createMappingConstructorExpressionNode(innerList.collectionStartToken, fields,
                        innerList.collectionEndToken);
            case REST_BINDING_PATTERN:
                STRestBindingPatternNode restBindingPattern = (STRestBindingPatternNode) ambiguousNode;
                if (isInMappingConstructor) {
                    return STNodeFactory.createSpreadFieldNode(restBindingPattern.ellipsisToken,
                            restBindingPattern.variableName);
                }
                return STNodeFactory.createSpreadMemberNode(restBindingPattern.ellipsisToken,
                        restBindingPattern.variableName);
            case SPECIFIC_FIELD:
                // Specific field is used to represent ambiguous scenarios. Hence it needs to be re-written.
                STSpecificFieldNode field = (STSpecificFieldNode) ambiguousNode;
                return STNodeFactory.createSpecificFieldNode(field.readonlyKeyword, field.fieldName, field.colon,
                        getExpression(field.valueExpr));
            case ERROR_CONSTRUCTOR:
                STErrorConstructorExpressionNode errorCons = (STErrorConstructorExpressionNode) ambiguousNode;
                // We have skipped the validation top of function args when ambiguous. Ref: parseErrorConstructorExpr
                // Hence, do the validation here.
                STNode errorArgs = getErrorArgList(errorCons.arguments);
                return STNodeFactory.createErrorConstructorExpressionNode(errorCons.errorKeyword,
                        errorCons.typeReference, errorCons.openParenToken, errorArgs, errorCons.closeParenToken);
            case IDENTIFIER_TOKEN:
                return STNodeFactory.createSimpleNameReferenceNode(ambiguousNode);
            case INDEXED_EXPRESSION:
                STIndexedExpressionNode indexedExpressionNode = (STIndexedExpressionNode) ambiguousNode;
                STNodeList keys = (STNodeList) indexedExpressionNode.keyExpression;
                
                if (keys.size() != 0) {
                    return ambiguousNode;
                }
                
                STNode lhsExpr = indexedExpressionNode.containerExpression;
                STNode openBracket = indexedExpressionNode.openBracket;
                STNode closeBracket = indexedExpressionNode.closeBracket;
                STNode missingVarRef = STNodeFactory
                        .createSimpleNameReferenceNode(SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN));
                STNode keyExpr = STNodeFactory.createNodeList(missingVarRef);
                closeBracket = SyntaxErrors.addDiagnostic(closeBracket,
                        DiagnosticErrorCode.ERROR_MISSING_KEY_EXPR_IN_MEMBER_ACCESS_EXPR);
                return STNodeFactory.createIndexedExpressionNode(lhsExpr, openBracket, keyExpr, closeBracket);
            case SIMPLE_NAME_REFERENCE:
            case QUALIFIED_NAME_REFERENCE:
            case COMPUTED_NAME_FIELD:
            case SPREAD_FIELD:    
            case SPREAD_MEMBER:
                return ambiguousNode;
            default:
                STNode simpleVarRef = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_EXPRESSION);
                simpleVarRef = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(simpleVarRef, ambiguousNode);
                return STNodeFactory.createSimpleNameReferenceNode(simpleVarRef);
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
                return STNodeFactory
                        .createSpecificFieldNode(readonlyKeyword, identifier, colon, bindingPatternOrExpr);
            case LIST_BP_OR_LIST_CONSTRUCTOR:
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            default:
                // If ambiguous, return an specific node, since it is used to represent any
                // ambiguous mapping field
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, bindingPatternOrExpr);
        }
    }

    // ----------------------------------------- Error Recovery ----------------------------------------

    private Solution recover(STToken nextToken, ParserRuleContext currentCtx) {
        if (isInsideABlock(nextToken)) {
            return this.recover(nextToken, currentCtx, true);
        } else {
            return this.recover(nextToken, currentCtx, false);
        }
    }

    private boolean isInsideABlock(STToken nextToken) {
        // We need to detect the end close brace when typing inside a block.
        // Otherwise recovery may remove it, causing other constructs beneath affected.
        if (nextToken.kind != SyntaxKind.CLOSE_BRACE_TOKEN) {
            return false;
        }
        
        for (ParserRuleContext ctx : this.errorHandler.getContextStack()) {
            if (isBlockContext(ctx)) {
                // This is done to exit at the earliest point when climbing up in the context stack.
                return true;
            }
        }
        return false;
    }

    private boolean isBlockContext(ParserRuleContext ctx) {
        switch (ctx) {
            case FUNC_BODY_BLOCK:
            case CLASS_MEMBER:
            case OBJECT_CONSTRUCTOR_MEMBER:
            case OBJECT_TYPE_MEMBER:
            case BLOCK_STMT:
            case MATCH_BODY:
            case MAPPING_MATCH_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case MAPPING_CONSTRUCTOR:
            case FORK_STMT:
            case MULTI_RECEIVE_WORKERS:
            case MULTI_WAIT_FIELDS:
            case MODULE_ENUM_DECLARATION:
                return true;
            default:
                return false;
        }
    }

    // ----------------------------------------- ~ End of Parser ~ ----------------------------------------

    // NOTE: Please add any new methods to the relevant section of the class. Binding patterns related code is the
    // last section of the class.
}
