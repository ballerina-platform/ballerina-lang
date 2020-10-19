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

import io.ballerina.compiler.internal.diagnostics.DiagnosticCode;
import io.ballerina.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.compiler.internal.parser.AbstractParserErrorHandler.Action;
import io.ballerina.compiler.internal.parser.AbstractParserErrorHandler.Solution;
import io.ballerina.compiler.internal.parser.tree.STAmbiguousCollectionNode;
import io.ballerina.compiler.internal.parser.tree.STAnnotAccessExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STArrayTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.tree.STAsyncSendActionNode;
import io.ballerina.compiler.internal.parser.tree.STBinaryExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STBracedExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STConditionalExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STDefaultableParameterNode;
import io.ballerina.compiler.internal.parser.tree.STFieldAccessExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionArgumentNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionCallExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STFunctionSignatureNode;
import io.ballerina.compiler.internal.parser.tree.STIndexedExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STIntersectionTypeDescriptorNode;
import io.ballerina.compiler.internal.parser.tree.STListConstructorExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STMappingConstructorExpressionNode;
import io.ballerina.compiler.internal.parser.tree.STMissingToken;
import io.ballerina.compiler.internal.parser.tree.STNamedArgumentNode;
import io.ballerina.compiler.internal.parser.tree.STNilLiteralNode;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeFactory;
import io.ballerina.compiler.internal.parser.tree.STNodeList;
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
import io.ballerina.compiler.internal.syntax.SyntaxUtils;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.CharReader;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
            case SERVICE_KEYWORD:
            case ENUM_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
                // Following are module-class-defn starting tokens
            case CLASS_KEYWORD:
            case DISTINCT_KEYWORD:
            case CLIENT_KEYWORD:
            case READONLY_KEYWORD:
                metadata = STNodeFactory.createEmptyNode();
                break;
            case IDENTIFIER_TOKEN:
                // Here we assume that after recovering, we'll never reach here.
                // Otherwise the tokenOffset will not be 1.
                if (isModuleVarDeclStart(1)) {
                    // This is an early exit, so that we don't have to do the same check again.
                    return parseModuleVarDecl(STNodeFactory.createEmptyNode(), null);
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
                    metadata = STNodeFactory.createEmptyNodeList();
                    break;
                }

                return parseTopLevelNode();
        }

        return parseTopLevelNode(metadata);
    }

    /**
     * Parse top level node having an optional modifier preceding it, given the next token kind.
     *
     * @param metadata Next token kind
     * @return Parsed node
     */
    private STNode parseTopLevelNode(STNode metadata) {
        STToken nextToken = peek();
        STNode qualifier = null;
        switch (nextToken.kind) {
            case EOF_TOKEN:
                if (metadata != null) {
                    addInvalidNodeToNextToken(metadata, DiagnosticErrorCode.ERROR_INVALID_METADATA);
                }
                return null;
            case PUBLIC_KEYWORD:
                qualifier = parseQualifier();
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
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
                // Following are module-class-defn starting tokens
            case CLASS_KEYWORD:
            case DISTINCT_KEYWORD:
            case CLIENT_KEYWORD:
            case READONLY_KEYWORD:
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
                if (isTypeStartingToken(nextToken.kind) && nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_METADATA, metadata);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    qualifier = STNodeFactory.createEmptyNode();
                    break;
                }

                return parseTopLevelNode(metadata);
        }

        return parseTopLevelNode(metadata, qualifier);
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
            case OPEN_BRACE_TOKEN: // Scenario foo[] (Array type descriptor with custom type)
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
     * <code>import-decl :=  import [org-name /] module-name [version sem-ver] [as import-prefix] ;</code>
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
        STNode version;
        STNode alias;

        switch (nextToken.kind) {
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
                moduleName = parseModuleName(identifier);
                version = parseVersion();
                alias = parseImportPrefixDecl();
                break;
            case AS_KEYWORD:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(identifier);
                version = STNodeFactory.createEmptyNode();
                alias = parseImportPrefixDecl();
                break;
            case SEMICOLON_TOKEN:
                orgName = STNodeFactory.createEmptyNode();
                moduleName = parseModuleName(identifier);
                version = STNodeFactory.createEmptyNode();
                alias = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.IMPORT_DECL_RHS, importKeyword, identifier);
                return parseImportDecl(importKeyword, identifier);
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
        while (!isEndOfImportModuleName(nextToken)) {
            moduleNameParts.add(parseDotToken());
            moduleNameParts.add(parseIdentifier(ParserRuleContext.IMPORT_MODULE_NAME));
            nextToken = peek();
        }

        return STNodeFactory.createNodeList(moduleNameParts);
    }

    private boolean isEndOfImportModuleName(STToken nextToken) {
        return nextToken.kind != SyntaxKind.DOT_TOKEN && nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN;
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
     *
     * @return Parsed node
     */
    private STNode parseVersion() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case VERSION_KEYWORD:
                STNode versionKeyword = parseVersionKeyword();
                STNode versionNumber = parseVersionNumber();
                return STNodeFactory.createImportVersionNode(versionKeyword, versionNumber);
            case AS_KEYWORD:
            case SEMICOLON_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                if (isEndOfImportDecl(nextToken)) {
                    return STNodeFactory.createEmptyNode();
                }

                recover(peek(), ParserRuleContext.IMPORT_VERSION_DECL);
                return parseVersion();
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
     * <code>import-prefix-decl := as import-prefix
     * <br/>
     * import-prefix := a identifier | _
     * </code>
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
     *
     * @return Parsed node
     */
    private STNode parseImportPrefix() {
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            recover(peek(), ParserRuleContext.IMPORT_PREFIX);
            return parseImportPrefix();
        }
    }

    /**
     * Parse top level node, given the modifier that precedes it.
     *
     * @param qualifier Qualifier that precedes the top level node
     * @return Parsed node
     */
    private STNode parseTopLevelNode(STNode metadata, STNode qualifier) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EOF_TOKEN:
                reportInvalidQualifier(qualifier);
                reportInvalidMetaData(metadata);
                return null;
            case ISOLATED_KEYWORD:
            case CLIENT_KEYWORD:
                // Note that following top level nodes can have the isolated qualifier.
                // funcDef, funcType, classDef, objectType
                if (!isFuncDefOrFuncTypeStart()) {
                    if (isObjectTypeStart()) {
                        return parseModuleVarDecl(metadata, qualifier);
                    } else {
                        return parseClassDefinition(metadata, qualifier);
                    }
                }
                // Else fall through
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
                // Anything starts with a function keyword could be a function definition
                // or a module-var-decl with function type desc.
                List<STNode> qualifiers = new ArrayList<>();
                if (qualifier != null) {
                    qualifiers.add(qualifier);
                }
                return parseFuncDefOrFuncTypeDesc(ParserRuleContext.TOP_LEVEL_FUNC_DEF_OR_FUNC_TYPE_DESC, metadata,
                        qualifiers, false, false);
            case TYPE_KEYWORD:
                return parseModuleTypeDefinition(metadata, getQualifier(qualifier));
            case CLASS_KEYWORD:
            case DISTINCT_KEYWORD:
            case READONLY_KEYWORD:
                return parseClassDefinition(metadata, getQualifier(qualifier));
            case LISTENER_KEYWORD:
                return parseListenerDeclaration(metadata, getQualifier(qualifier));
            case CONST_KEYWORD:
                return parseConstantDeclaration(metadata, getQualifier(qualifier));
            case ANNOTATION_KEYWORD:
                STNode constKeyword = STNodeFactory.createEmptyNode();
                return parseAnnotationDeclaration(metadata, getQualifier(qualifier), constKeyword);
            case IMPORT_KEYWORD:
                reportInvalidQualifier(qualifier);
                reportInvalidMetaData(metadata);
                return parseImportDecl();
            case XMLNS_KEYWORD:
                reportInvalidQualifier(qualifier);
                reportInvalidMetaData(metadata);
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
                if (isTypeStartingToken(nextToken.kind) && nextToken.kind != SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseModuleVarDecl(metadata, qualifier);
                }

                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITHOUT_MODIFIER, metadata, qualifier);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not. There only one such
                    // cases here, which is the `case IDENTIFIER_TOKEN`. So accept it, and continue.
                    return parseModuleVarDecl(metadata, qualifier);
                }

                return parseTopLevelNode(metadata, qualifier);
        }

    }

    private boolean isObjectTypeStart() {
        return peek(2).kind == SyntaxKind.OBJECT_KEYWORD || peek(3).kind == SyntaxKind.OBJECT_KEYWORD;
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
            addInvalidNodeToNextToken(qualifier, DiagnosticErrorCode.ERROR_INVALID_QUALIFIER,
                    ((STToken) qualifier).text());
        }
    }

    private void reportInvalidMetaData(STNode metadata) {
        if (metadata != null && metadata.kind != SyntaxKind.NONE) {
            addInvalidNodeToNextToken(metadata, DiagnosticErrorCode.ERROR_INVALID_METADATA);
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
            recover(token, ParserRuleContext.PUBLIC_KEYWORD);
            return parseQualifier();
        }
    }

    private STNode parseFuncDefinition(STNode metadata, boolean isObjectMember, STNode qualifiers) {
        startContext(ParserRuleContext.FUNC_DEF);
        STNode functionKeyword = parseFunctionKeyword();
        STNode funcDef = parseFunctionKeywordRhs(metadata, functionKeyword, qualifiers, true, isObjectMember, false);
        return funcDef;
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
     * @param context Parsing context
     * @param metadata Preceding metadata
     * @param qualifiers Preceding visibility qualifier
     * @param isObjectMember Whether object member or not
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed node
     */
    private STNode parseFuncDefOrFuncTypeDesc(ParserRuleContext context, STNode metadata, List<STNode> qualifiers,
                                              boolean isObjectMember, boolean isObjectTypeDesc) {
        STNode qualifierList = parseFunctionQualifiers(context, qualifiers);
        return parseFuncDefOrFuncTypeDesc(metadata, qualifierList, isObjectMember, isObjectTypeDesc);
    }

    private STNode parseFuncDefOrFuncTypeDesc(STNode metadata, STNode qualifiers, boolean isObjectMember,
                                              boolean isObjectTypeDesc) {
        startContext(ParserRuleContext.FUNC_DEF_OR_FUNC_TYPE);
        STNode functionKeyword = parseFunctionKeyword();
        STNode funcDefOrType = parseFunctionKeywordRhs(metadata, functionKeyword, qualifiers, false,
                isObjectMember, isObjectTypeDesc);
        return funcDefOrType;
    }

    private STNode parseFunctionKeywordRhs(STNode metadata, STNode functionKeyword, STNode qualifiers,
                                           boolean isFuncDef, boolean isObjectMember, boolean isObjectTypeDesc) {
        // If the function name is present, treat this as a function def
        if (isFuncDef) {
            STNode name = parseFunctionName();
            switchContext(ParserRuleContext.FUNC_DEF);
            STNode funcSignature = parseFuncSignature(false);
            STNode funcDef = createFuncDefOrMethodDecl(metadata, functionKeyword, name, funcSignature, qualifiers,
                    isObjectMember, isObjectTypeDesc);
            endContext();
            return funcDef;
        }

        return parseFunctionKeywordRhs(metadata, functionKeyword, qualifiers, isObjectMember, isObjectTypeDesc);
    }

    private STNode parseFunctionKeywordRhs(STNode metadata, STNode functionKeyword, STNode qualifiers,
                                           boolean isObjectMember, boolean isObjectTypeDesc) {
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                STNode name = parseFunctionName();
                switchContext(ParserRuleContext.FUNC_DEF);
                STNode funcSignature = parseFuncSignature(false);
                STNode funcDef = createFuncDefOrMethodDecl(metadata, functionKeyword, name, funcSignature, qualifiers,
                        isObjectMember, isObjectTypeDesc);
                endContext();
                return funcDef;
            case OPEN_PAREN_TOKEN:
                funcSignature = parseFuncSignature(true);
                return parseReturnTypeDescRhs(metadata, functionKeyword, funcSignature, qualifiers, isObjectMember,
                        isObjectTypeDesc);
            default:
                STToken token = peek();
                recover(token, ParserRuleContext.FUNCTION_KEYWORD_RHS, metadata, functionKeyword, isObjectMember,
                        qualifiers);
                return parseFunctionKeywordRhs(metadata, functionKeyword, qualifiers, isObjectMember,
                        isObjectTypeDesc);
        }
    }

    private STNode createFuncDefOrMethodDecl(STNode metadata, STNode functionKeyword, STNode name, STNode funcSignature,
                                             STNode qualifierList, boolean isObjectMethod, boolean isObjectTypeDesc) {
        if (isObjectTypeDesc) {
            STNode semicolon = parseSemicolon();
            return STNodeFactory.createMethodDeclarationNode(metadata, qualifierList, functionKeyword, name,
                    funcSignature, semicolon);
        }

        STNode body = parseFunctionBody();
        if (isObjectMethod) {
            return STNodeFactory.createFunctionDefinitionNode(SyntaxKind.OBJECT_METHOD_DEFINITION, metadata,
                    qualifierList, functionKeyword, name, funcSignature, body);
        } else {
            return STNodeFactory.createFunctionDefinitionNode(SyntaxKind.FUNCTION_DEFINITION, metadata, qualifierList,
                    functionKeyword, name, funcSignature, body);
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
        STNode openParenthesis = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
        STNode parameters = parseParamList(isParamNameOptional);
        STNode closeParenthesis = parseCloseParenthesis();
        endContext(); // end param-list
        STNode returnTypeDesc = parseFuncReturnTypeDescriptor();
        return STNodeFactory.createFunctionSignatureNode(openParenthesis, parameters, closeParenthesis, returnTypeDesc);
    }

    private STNode parseReturnTypeDescRhs(STNode metadata, STNode functionKeyword, STNode funcSignature,
                                          STNode qualifiers, boolean isObjectMember, boolean isObjectTypeDesc) {
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
                endContext(); // end the func-type
                return parseVarDeclWithFunctionType(functionKeyword, funcSignature, qualifiers, metadata,
                        isObjectMember, isObjectTypeDesc);
        }

        // Treat as function definition.

        // We reach this method only if the func-name is not present.
        STNode name = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                DiagnosticErrorCode.ERROR_MISSING_FUNCTION_NAME);

        // Function definition cannot have missing param-names. So validate it.
        funcSignature = validateAndGetFuncParams((STFunctionSignatureNode) funcSignature);

        STNode funcDef = createFuncDefOrMethodDecl(metadata, functionKeyword, name, funcSignature, qualifiers,
                isObjectMember, isObjectTypeDesc);
        endContext();
        return funcDef;
    }

    private STNode parseVarDeclWithFunctionType(STNode functionKeyword, STNode funcSignature, STNode qualifiers,
                                                STNode metadata, boolean isObjectMember, boolean isObjectTypeDesc) {
        STNodeList qualifierList = (STNodeList) qualifiers;
        STNode visibilityQualifier = STNodeFactory.createEmptyNode();
        List<STNode> validatedQualifierList = new ArrayList<>();

        // qualifiers are only allowed in the following cases for func type desc.
        // isolated and transactional qualifiers are allowed.
        // public, private and remote qualifiers are allowed in object field.
        for (int position = 0; position < qualifierList.size(); position++) {
            STNode qualifier = qualifierList.get(position);

            if (qualifier.kind == SyntaxKind.ISOLATED_KEYWORD || qualifier.kind == SyntaxKind.TRANSACTIONAL_KEYWORD) {
                validatedQualifierList.add(qualifier);
                continue;
            }

            if (isObjectMember) {
                if (isVisibilityQualifier(qualifier)) {
                    // public or private qualifier allowed in object field.
                    visibilityQualifier = qualifier;
                    continue;
                } else if (qualifier.kind == SyntaxKind.REMOTE_KEYWORD) {
                    validatedQualifierList.add(qualifier);
                    continue;
                }
            }

            functionKeyword = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(functionKeyword, qualifier,
                    DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
        }

        qualifiers = STNodeFactory.createNodeList(validatedQualifierList);
        STNode typeDesc = STNodeFactory.createFunctionTypeDescriptorNode(qualifiers, functionKeyword,
                funcSignature);

        // Check if it is a complex type desc starting with function type.
        typeDesc = parseComplexTypeDescriptor(typeDesc,
                ParserRuleContext.TOP_LEVEL_FUNC_DEF_OR_FUNC_TYPE_DESC, false);

        if (isObjectMember) {
            STNode readonlyQualifier = STNodeFactory.createEmptyNode();
            STNode fieldName = parseVariableName();
            return parseObjectFieldRhs(metadata, visibilityQualifier, readonlyQualifier, typeDesc, fieldName,
                    isObjectTypeDesc);
        }

        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
        return parseVarDeclRhs(metadata, STNodeFactory.createEmptyNode(), typedBindingPattern, true);
    }

    private boolean isVisibilityQualifier(STNode qualifier) {
        switch (qualifier.kind) {
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
                return true;
            default:
                return false;
        }
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
                        param = STNodeFactory
                                .createRequiredParameterNode(requiredParam.annotations, requiredParam.typeName,
                                        paramName);
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
            recover(token, ctx);
            return parseOpenParenthesis(ctx);
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
            recover(token, ParserRuleContext.CLOSE_PARENTHESIS);
            return parseCloseParenthesis();
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
                    updateLastNodeInListWithInvalidNode(paramsList, paramEnd, paramOrderError);
                    updateLastNodeInListWithInvalidNode(paramsList, param, null);
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

    private boolean isNodeWithSyntaxKindInList(List<STNode> nodeList, SyntaxKind kind) {
        for (STNode node : nodeList) {
            if (node.kind == kind) {
                return true;
            }
        }
        return false;
    }

    private STNode parseParameterRhs() {
        return parseParameterRhs(peek().kind);
    }

    private STNode parseParameterRhs(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case COMMA_TOKEN:
                return parseComma();
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
            case IDENTIFIER_TOKEN:
                annots = STNodeFactory.createEmptyNodeList();
                break;
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    annots = STNodeFactory.createEmptyNodeList();
                    break;
                }

                STToken token = peek();
                Solution solution =
                        recover(token, ParserRuleContext.PARAMETER_START, prevParamKind, isParamNameOptional);

                if (solution.action == Action.KEEP) {
                    // If the solution is {@link Action#KEEP}, that means next immediate token is
                    // at the correct place, but some token after that is not.
                    annots = STNodeFactory.createEmptyNodeList();
                    break;
                }

                return parseParameter(prevParamKind, isParamNameOptional);
        }

        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode param = parseAfterParamType(prevParamKind, annots, type, isParamNameOptional);
        return param;
    }

    private STNode parseAfterParamType(SyntaxKind prevParamKind, STNode annots, STNode type,
                                       boolean isParamNameOptional) {
        STNode paramName;
        STToken token = peek();
        switch (token.kind) {
            case ELLIPSIS_TOKEN:
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
                return parseParameterRhs(prevParamKind, annots, type, paramName);
            case EQUAL_TOKEN:
                if (!isParamNameOptional) {
                    break;
                }
                // If this is a function-type-desc, then param name is optional, and may not exist
                paramName = STNodeFactory.createEmptyNode();
                return parseParameterRhs(prevParamKind, annots, type, paramName);
            default:
                if (!isParamNameOptional) {
                    break;
                }
                // If this is a function-type-desc, then param name is optional, and may not exist
                paramName = STNodeFactory.createEmptyNode();
                return parseParameterRhs(prevParamKind, annots, type, paramName);
        }

        recover(token, ParserRuleContext.AFTER_PARAMETER_TYPE, prevParamKind, annots, type,
                isParamNameOptional);
        return parseAfterParamType(prevParamKind, annots, type, isParamNameOptional);
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
    private STNode parseParameterRhs(SyntaxKind prevParamKind, STNode annots, STNode type,
                                     STNode paramName) {
        STToken nextToken = peek();
        // Required parameters
        if (isEndOfParameter(nextToken.kind)) {
            return STNodeFactory.createRequiredParameterNode(annots, type, paramName);
        } else if (nextToken.kind == SyntaxKind.EQUAL_TOKEN) {
            // If we were processing required params so far and found a defualtable
            // parameter, then switch the context to defaultable params.
            if (prevParamKind == SyntaxKind.REQUIRED_PARAM) {
                switchContext(ParserRuleContext.DEFAULTABLE_PARAM);
            }

            // Defaultable parameters
            STNode equal = parseAssignOp();
            STNode expr = parseExpression();
            return STNodeFactory.createDefaultableParameterNode(annots, type, paramName, equal, expr);
        } else {
            recover(nextToken, ParserRuleContext.PARAMETER_NAME_RHS, prevParamKind, annots, type, paramName);
            return parseParameterRhs(prevParamKind, annots, type, paramName);
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
     * @return Parsed node
     */
    private STNode parseFuncReturnTypeDescriptor() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACE_TOKEN: // func-body block
            case EQUAL_TOKEN: // external func
                return STNodeFactory.createEmptyNode();
            case RETURNS_KEYWORD:
                break;
            default:
                STToken nextNextToken = getNextNextToken(nextToken.kind);
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
        return parseTypeDescriptor(context, false, false);
    }

    private STNode parseTypeDescriptorInExpression(ParserRuleContext context, boolean isInConditionalExpr) {
        return parseTypeDescriptor(context, false, isInConditionalExpr);
    }

    private STNode parseTypeDescriptor(ParserRuleContext context, boolean isTypedBindingPattern,
                                       boolean isInConditionalExpr) {
        startContext(context);
        STNode typeDesc = parseTypeDescriptorInternal(context, isTypedBindingPattern, isInConditionalExpr);
        endContext();
        return typeDesc;
    }

    private STNode parseTypeDescriptorWithoutContext(ParserRuleContext context, boolean isInConditionalExpr) {
        return parseTypeDescriptorInternal(context, false, isInConditionalExpr);
    }

    private STNode parseTypeDescriptorInternal(ParserRuleContext context, boolean isTypedBindingPattern,
                                               boolean isInConditionalExpr) {
        STNode typeDesc = parseTypeDescriptorInternal(context, isInConditionalExpr);

        // var is parsed as a built-in simple type. However, since var is not allowed everywhere,
        // validate it here. This is done to give better error messages.
        if (typeDesc.kind == SyntaxKind.VAR_TYPE_DESC &&
                context != ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN) {
            STToken missingToken = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
            missingToken = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(missingToken, typeDesc,
                    DiagnosticErrorCode.ERROR_INVALID_USAGE_OF_VAR);
            typeDesc = STNodeFactory.createSimpleNameReferenceNode(missingToken);
        }

        return parseComplexTypeDescriptor(typeDesc, context, isTypedBindingPattern);
    }

    /**
     * This will handle the parsing of optional,array,union type desc to infinite length.
     *
     * @param typeDesc
     * @return Parsed type descriptor node
     */
    private STNode parseComplexTypeDescriptor(STNode typeDesc, ParserRuleContext context,
                                              boolean isTypedBindingPattern) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case QUESTION_MARK_TOKEN:
                // If next token after a type descriptor is '?' then it is an optional type descriptor
                if (context == ParserRuleContext.TYPE_DESC_IN_EXPRESSION &&
                        !isValidTypeContinuationToken(getNextNextToken(nextToken.kind)) &&
                        isValidExprStart(getNextNextToken(nextToken.kind).kind)) {
                    return typeDesc;
                }
                return parseComplexTypeDescriptor(parseOptionalTypeDescriptor(typeDesc), context,
                        isTypedBindingPattern);
            case OPEN_BRACKET_TOKEN:
                // If next token after a type descriptor is '[' then it is an array type descriptor
                if (isTypedBindingPattern) { // checking for typedesc parsing originating at typed-binding-pattern
                    return typeDesc;
                }
                return parseComplexTypeDescriptor(parseArrayTypeDescriptor(typeDesc), context, isTypedBindingPattern);
            case PIPE_TOKEN:
                // If next token after a type descriptor is '|' then it is an union type descriptor
                return parseUnionTypeDescriptor(typeDesc, context, isTypedBindingPattern);
            case BITWISE_AND_TOKEN:
                // If next token after a type descriptor is '&' then it is intersection type descriptor
                return parseIntersectionTypeDescriptor(typeDesc, context, isTypedBindingPattern);
            default:
                return typeDesc;
        }
    }

    private boolean isValidTypeContinuationToken(STToken nextToken) {
        switch (nextToken.kind) {
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
     * </p>
     * If the preceding token is <code>?</code> then it is an optional type descriptor
     *
     * @param context             Current context
     * @param isInConditionalExpr
     * @return Parsed node
     */
    private STNode parseTypeDescriptorInternal(ParserRuleContext context, boolean isInConditionalExpr) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                return parseTypeReference(isInConditionalExpr);
            case RECORD_KEYWORD:
                // Record type descriptor
                return parseRecordTypeDescriptor();
            case READONLY_KEYWORD:
                STToken nextNextToken = getNextNextToken(nextToken.kind);
                SyntaxKind nextNextTokenKind = nextNextToken.kind;
                if (nextNextTokenKind != SyntaxKind.OBJECT_KEYWORD &&
                        nextNextTokenKind != SyntaxKind.ABSTRACT_KEYWORD &&
                        nextNextTokenKind != SyntaxKind.CLIENT_KEYWORD) {
                    return parseSimpleTypeDescriptor();
                }
                // Allow `readonly` to be parsed as a object type qualifier and then log an error
                // Fall through
            case OBJECT_KEYWORD:
            case ABSTRACT_KEYWORD: // Allow `abstract` to be parsed as an object type qualifier and then log an error
            case CLIENT_KEYWORD:
                return parseObjectTypeDescriptor();
            case OPEN_PAREN_TOKEN:
                return parseNilOrParenthesisedTypeDesc();
            case MAP_KEYWORD: // map type desc
            case FUTURE_KEYWORD: // future type desc
                return parseParameterizedTypeDescriptor();
            case TYPEDESC_KEYWORD: // typedesc type desc
                return parseTypedescTypeDescriptor();
            case ERROR_KEYWORD: // error type descriptor
                return parseErrorTypeDescriptor();
            case XML_KEYWORD: // typedesc type desc
                return parseXmlTypeDescriptor();
            case STREAM_KEYWORD: // stream type desc
                return parseStreamTypeDescriptor();
            case TABLE_KEYWORD: // table type desc
                return parseTableTypeDescriptor();
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
                return parseFunctionTypeDesc();
            case OPEN_BRACKET_TOKEN:
                return parseTupleTypeDesc();
            case DISTINCT_KEYWORD:
                return parseDistinctTypeDesc(context);
            case ISOLATED_KEYWORD:
                if (isFuncDefOrFuncTypeStart()) {
                    return parseFunctionTypeDesc();
                } else {
                    return parseObjectTypeDescriptor();
                }
            default:
                if (isSingletonTypeDescStart(nextToken.kind, true)) {
                    return parseSingletonTypeDesc();
                }
                if (isSimpleType(nextToken.kind)) {
                    return parseSimpleTypeDescriptor();
                }

                Solution solution = recover(nextToken, ParserRuleContext.TYPE_DESCRIPTOR, context, isInConditionalExpr);

                if (solution.action == Action.KEEP) {
                    return parseSingletonTypeDesc();
                }

                return parseTypeDescriptorInternal(context, isInConditionalExpr);
        }
    }

    private boolean isFuncDefOrFuncTypeStart() {
        STToken nextNextToken = peek(2);
        switch (nextNextToken.kind) {
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case REMOTE_KEYWORD:
            case RESOURCE_KEYWORD:
                return true;
            case OBJECT_KEYWORD:
            case CLIENT_KEYWORD:
            default:
                return false;
        }
    }

    /**
     * Parse distinct type descriptor.
     * <p>
     * <code>
     * distinct-type-descriptor := distinct type-descriptor
     * </code>
     *
     * @param context Context in which the type desc is used.
     * @return Distinct type descriptor
     */
    private STNode parseDistinctTypeDesc(ParserRuleContext context) {
        STNode distinctKeyword = parseDistinctKeyword();
        STNode typeDesc = parseTypeDescriptor(context);
        return STNodeFactory.createDistinctTypeDescriptorNode(distinctKeyword, typeDesc);
    }

    private STNode parseDistinctKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.DISTINCT_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.DISTINCT_KEYWORD);
            return parseDistinctKeyword();
        }
    }

    private STNode parseNilOrParenthesisedTypeDesc() {
        STNode openParen = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
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

                recover(peek(), ParserRuleContext.NIL_OR_PARENTHESISED_TYPE_DESC_RHS, openParen);
                return parseNilOrParenthesisedTypeDescRhs(openParen);
        }
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

    private STNode createBuiltinSimpleNameReference(STNode token) {
        SyntaxKind typeKind = getTypeSyntaxKind(token.kind);
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

            //Local type def is not allowed in new spec, hence add it as invalid node minutia.
            if (stmt.kind == SyntaxKind.LOCAL_TYPE_DEFINITION_STATEMENT) {
                addInvalidNodeToNextToken(stmt, DiagnosticErrorCode.ERROR_LOCAL_TYPE_DEFINITION_NOT_ALLOWED);
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
            case DO_KEYWORD:
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
            case DO_KEYWORD:
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
                recover(nextToken, ParserRuleContext.EXTERNAL_FUNC_BODY_OPTIONAL_ANNOTS, assign);
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
     * <p>
     * Parse a class definition.
     * </p>
     * <code>
     * module-class-defn := metadata [public] class-type-quals class identifier { class-member* }
     * <br/>
     * class-type-quals := (distinct | client | readonly | isolated)*
     * </code>
     *
     * @param metadata Metadata
     * @param qualifier Visibility qualifier
     * @return Parsed node
     */
    private STNode parseClassDefinition(STNode metadata, STNode qualifier) {
        startContext(ParserRuleContext.MODULE_CLASS_DEFINITION);
        STNode classTypeQualifiers = parseClassTypeQualifiers();
        STNode classKeyword = parseClassKeyword();
        STNode className = parseClassName();
        STNode openBrace = parseOpenBrace();
        STNode classMembers = parseObjectMembers(ParserRuleContext.CLASS_MEMBER);
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createClassDefinitionNode(metadata, qualifier, classTypeQualifiers, classKeyword,
                className, openBrace, classMembers, closeBrace);
    }

    /**
     * Parse class type qualifiers.
     *
     * @return Parsed node
     */
    private STNode parseClassTypeQualifiers() {
        List<STNode> qualifiers = new ArrayList<>();
        STNode qualifier;
        for (int i = 0; i < 4; i++) {
            STNode nextToken = peek();
            if (isNodeWithSyntaxKindInList(qualifiers, nextToken.kind)) {
                qualifier = consume();
                updateLastNodeInListOrAddInvalidNodeToNextToken(qualifiers, nextToken,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            qualifier = parseSingleClassTypeQualifier();
            if (qualifier == null) {
                return STNodeFactory.createNodeList(qualifiers);
            }

            qualifiers.add(qualifier);
        }

        return STNodeFactory.createNodeList(qualifiers);
    }

    private STNode parseSingleClassTypeQualifier() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case CLIENT_KEYWORD:
            case READONLY_KEYWORD:
            case DISTINCT_KEYWORD:
            case ISOLATED_KEYWORD:
                return consume();
            case CLASS_KEYWORD:
            case EOF_TOKEN:
                // null indicates the end of qualifiers
                return null;
            default:
                recover(nextToken, ParserRuleContext.MODULE_CLASS_DEFINITION_START);
                return parseSingleClassTypeQualifier();
        }
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

        boolean isInclusive = bodyStartDelimiter.kind == SyntaxKind.OPEN_BRACE_TOKEN;

        ArrayList<STNode> recordFields = new ArrayList<>();
        STToken token = peek();
        STNode recordRestDescriptor = null;
        while (!isEndOfRecordTypeNode(token.kind)) {
            STNode field = parseFieldOrRestDescriptor(isInclusive);
            if (field == null) {
                break;
            }
            token = peek();
            if (field.kind == SyntaxKind.RECORD_REST_TYPE) {
                recordRestDescriptor = field;
                break;
            }
            recordFields.add(field);
        }

        // Following loop will only run if there are more fields after the rest type descriptor.
        // Try to parse them and mark as invalid.
        while (recordRestDescriptor != null && !isEndOfRecordTypeNode(token.kind)) {
            STNode invalidField = parseFieldOrRestDescriptor(isInclusive);
            recordRestDescriptor = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(recordRestDescriptor, invalidField,
                    DiagnosticErrorCode.ERROR_MORE_RECORD_FIELDS_AFTER_REST_FIELD);
            token = peek();
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

    private STNode parseFieldOrRestDescriptor(boolean isInclusive) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
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
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
                startContext(ParserRuleContext.RECORD_FIELD);
                STNode metadata = parseMetaData();
                nextToken = peek();
                return parseRecordField(nextToken, isInclusive, metadata);
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    // individual-field-descriptor
                    startContext(ParserRuleContext.RECORD_FIELD);
                    metadata = STNodeFactory.createEmptyNode();
                    return parseRecordField(nextToken, isInclusive, metadata);
                }

                recover(peek(), ParserRuleContext.RECORD_FIELD_OR_RECORD_END, isInclusive);
                return parseFieldOrRestDescriptor(isInclusive);
        }
    }

    private STNode parseRecordField(STToken nextToken, boolean isInclusive, STNode metadata) {
        if (nextToken.kind != SyntaxKind.READONLY_KEYWORD) {
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
            fieldOrRestDesc = parseFieldDescriptor(isInclusive, metadata, type);
            endContext();
            return fieldOrRestDesc;
        } else if (isTypeStartingToken(nextToken.kind)) {
            type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_RECORD_FIELD);
        } else {
            readOnlyQualifier = createBuiltinSimpleNameReference(readOnlyQualifier);
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
        STNode typeReference = parseTypeDescriptor(ParserRuleContext.TYPE_REFERENCE);
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
                DiagnosticErrorCode.ONLY_TYPE_REFERENCE_ALLOWED_HERE_AS_TYPE_INCLUSIONS);
        return emptyNameReference;
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
        } else {
            recover(token, currentCtx, isInConditionalExpr);
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

        STToken nextNextToken = peek(2);
        switch (nextNextToken.kind) {
            case IDENTIFIER_TOKEN:
                STToken colon = consume();
                STNode varOrFuncName = consume();
                return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
            case MAP_KEYWORD:
                colon = consume();
                STToken mapKeyword = consume();
                STNode refName = STNodeFactory.createIdentifierToken(mapKeyword.text(), mapKeyword.leadingMinutiae(),
                        mapKeyword.trailingMinutiae(), mapKeyword.diagnostics());
                return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, refName);
            case COLON_TOKEN:
                // specially handle cases where there are more than one colon.
                addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
                return parseQualifiedIdentifier(identifier, isInConditionalExpr);
            default:
                if (isInConditionalExpr) {
                    return STNodeFactory.createSimpleNameReferenceNode(identifier);
                }

                colon = consume();
                varOrFuncName = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_IDENTIFIER);
                return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, varOrFuncName);
        }
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
                reportInvalidMetaData(metadata);
                STNode ellipsis = parseEllipsis();
                STNode semicolonToken = parseSemicolon();
                return STNodeFactory.createRecordRestDescriptorNode(type, ellipsis, semicolonToken);
            case IDENTIFIER_TOKEN:
                STNode readonlyQualifier = STNodeFactory.createEmptyNode();
                return parseIndividualRecordField(metadata, readonlyQualifier, type);
            default:
                recover(nextToken, ParserRuleContext.FIELD_OR_REST_DESCIPTOR_RHS, metadata, type);
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
                recover(nextToken, ParserRuleContext.FIELD_DESCRIPTOR_RHS, metadata, readonlyQualifier, type,
                        fieldName);
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

            //Local type def is not allowed in new spec, hence add it as invalid node minutia.
            if (stmt.kind == SyntaxKind.LOCAL_TYPE_DEFINITION_STATEMENT) {
                addInvalidNodeToNextToken(stmt, DiagnosticErrorCode.ERROR_LOCAL_TYPE_DEFINITION_NOT_ALLOWED);
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

    private STNode getAnnotations(STNode nullbaleAnnot) {
        if (nullbaleAnnot != null) {
            return nullbaleAnnot;
        }

        return STNodeFactory.createEmptyNodeList();
    }

    /**
     * Parse a single statement, given the next token kind.
     *
     * @param annots Annotations
     * @return Parsed node
     */
    private STNode parseStatement(STNode annots) {
        STToken nextToken = peek();
        // Validate annotations if present.
        if (!isNodeListEmpty(annots)) {
            validateStatementAnnotations(nextToken, annots);
        }
        switch (nextToken.kind) {
            case CLOSE_BRACE_TOKEN:
                // Returning null marks the end of statements
                return null;
            case SEMICOLON_TOKEN:
                addInvalidTokenToNextToken(errorHandler.consumeInvalidToken());
                return parseStatement(annots);
            case FINAL_KEYWORD:
                STNode finalKeyword = parseFinalKeyword();
                return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
            case IF_KEYWORD:
                return parseIfElseBlock();
            case WHILE_KEYWORD:
                return parseWhileStatement();
            case DO_KEYWORD:
                return parseDoStatement();
            case PANIC_KEYWORD:
                return parsePanicStatement();
            case CONTINUE_KEYWORD:
                return parseContinueStatement();
            case BREAK_KEYWORD:
                return parseBreakStatement();
            case RETURN_KEYWORD:
                return parseReturnStatement();
            case FAIL_KEYWORD:
                return parseFailStatement();
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
                return parseExpressionStatement(getAnnotations(annots));
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
            case TRANSACTIONAL_KEYWORD:
                if (peek(2).kind == SyntaxKind.WORKER_KEYWORD) {
                    return parseNamedWorkerDeclaration(getAnnotations(annots));
                }
                return parseStmtStartsWithTypeOrExpr(getAnnotations(annots));
            case FUNCTION_KEYWORD:
            case OPEN_PAREN_TOKEN:
            case IDENTIFIER_TOKEN:
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
                return parseStmtStartsWithTypeOrExpr(getAnnotations(annots));
            case MATCH_KEYWORD:
                return parseMatchStatement();
            case ERROR_KEYWORD:
                // Error type desc or error binding pattern
                return parseErrorTypeDescOrErrorBP(getAnnotations(annots));
            case ISOLATED_KEYWORD:
                if (isFuncDefOrFuncTypeStart()) {
                    return parseStmtStartsWithTypeOrExpr(getAnnotations(annots));
                } else {
                    finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
                }
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    // These are expressions that are definitely not types.
                    return parseStatementStartWithExpr(getAnnotations(annots));
                }

                if (isTypeStartingToken(nextToken.kind)) {
                    // If the statement starts with a type, then its a var declaration.
                    // This is an optimization since if we know the next token is a type, then
                    // we can parse the var-def faster.
                    finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT_WITHOUT_ANNOTS, annots);

                if (solution.action == Action.KEEP) {
                    // singleton type starting tokens can be correct one's hence keep them.
                    finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
                }

                return parseStatement(annots);
        }
    }

    private void validateStatementAnnotations(STToken nextToken, STNode annots) {
        switch (nextToken.kind) {
            // Statements in which annotations are not allowed.
            case CLOSE_BRACE_TOKEN:
            case IF_KEYWORD:
            case WHILE_KEYWORD:
            case DO_KEYWORD:
            case PANIC_KEYWORD:
            case CONTINUE_KEYWORD:
            case BREAK_KEYWORD:
            case RETURN_KEYWORD:
            case FAIL_KEYWORD:
            case LOCK_KEYWORD:
            case OPEN_BRACE_TOKEN:
            case FORK_KEYWORD:
            case FOREACH_KEYWORD:
            case XMLNS_KEYWORD:
            case TRANSACTION_KEYWORD:
            case RETRY_KEYWORD:
            case ROLLBACK_KEYWORD:
            case MATCH_KEYWORD:
                addInvalidNodeToNextToken(annots, DiagnosticErrorCode.ERROR_INVALID_ANNOTATIONS);
                break;
            default:
                break;
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
     * @param annots       Annotations or metadata
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
            recover(token, ParserRuleContext.FINAL_KEYWORD);
            return parseFinalKeyword();
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
     * @param metadata            metadata
     * @param finalKeyword        Final keyword
     * @param typedBindingPattern Typed binding pattern
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(STNode metadata, STNode finalKeyword, STNode typedBindingPattern,
                                   boolean isModuleVar) {
        STNode assign;
        STNode expr;
        STNode semicolon;
        STToken nextToken = peek();
        switch (nextToken.kind) {
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
                recover(nextToken, ParserRuleContext.VAR_DECL_STMT_RHS, metadata, finalKeyword, typedBindingPattern,
                        isModuleVar);
                return parseVarDeclRhs(metadata, finalKeyword, typedBindingPattern, isModuleVar);
        }

        endContext();
        if (isModuleVar) {
            return STNodeFactory.createModuleVariableDeclarationNode(metadata, finalKeyword, typedBindingPattern,
                    assign, expr, semicolon);
        }
        assert metadata.kind == SyntaxKind.LIST; // Annotations only
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
        STNode assign = parseAssignOp();
        STNode expr = parseActionOrExpression();
        STNode semicolon = parseSemicolon();
        endContext();

        if (lvExpr.kind == SyntaxKind.FUNCTION_CALL &&
                isPossibleErrorBindingPattern((STFunctionCallExpressionNode) lvExpr)) {
            lvExpr = getBindingPattern(lvExpr);
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

    private STNode attachErrorExpectedActionFoundDiagnostic(STNode node) {
        return SyntaxErrors.addDiagnostic(node, DiagnosticErrorCode.ERROR_EXPRESSION_EXPECTED_ACTION_FOUND);
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

    /**
     * Parse terminal expressions. A terminal expression has the highest precedence level
     * out of all expressions, and will be at the leaves of an expression tree.
     *
     * @param annots       Annotations
     * @param isRhsExpr    Is a rhs expression
     * @param allowActions Allow actions
     * @return Parsed node
     */
    private STNode parseTerminalExpression(STNode annots, boolean isRhsExpr, boolean allowActions,
                                           boolean isInConditionalExpr) {
        // Whenever a new expression start is added, make sure to
        // add it to all the other places as well.
        STToken nextToken = peek();
        // Validate annotations if present.
        if (!isNodeListEmpty(annots)) {
            validateExpressionAnnotations(nextToken, annots);
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
                return parseTableConstructorOrQuery(isRhsExpr);
            case ERROR_KEYWORD:
                if (peek(2).kind == SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseErrorBindingPattern();
                }
                return parseErrorConstructorExpr();
            case LET_KEYWORD:
                return parseLetExpression(isRhsExpr);
            case BACKTICK_TOKEN:
                return parseTemplateExpression();
            case CLIENT_KEYWORD:
            case OBJECT_KEYWORD:
                return parseObjectConstructorExpression(annots);
            case XML_KEYWORD:
                STToken nextNextToken = getNextNextToken(nextToken.kind);
                if (nextNextToken.kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseXMLTemplateExpression();
                }
                return parseSimpleTypeDescriptor();
            case STRING_KEYWORD:
                nextNextToken = getNextNextToken(nextToken.kind);
                if (nextNextToken.kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseStringTemplateExpression();
                }
                return parseSimpleTypeDescriptor();
            case FUNCTION_KEYWORD:
            case ISOLATED_KEYWORD:
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
                nextNextToken = getNextNextToken(nextToken.kind);
                if (nextNextToken.kind == SyntaxKind.ISOLATED_KEYWORD ||
                        nextNextToken.kind == SyntaxKind.FUNCTION_KEYWORD) {
                    return parseExplicitFunctionExpression(annots, isRhsExpr);
                }
                return parseTransactionalExpression();
            case SERVICE_KEYWORD:
                return parseServiceConstructorExpression(annots);
            case BASE16_KEYWORD:
            case BASE64_KEYWORD:
                return parseByteArrayLiteral();
            default:
                if (isSimpleType(nextToken.kind)) {
                    return parseSimpleTypeDescriptor();
                }

                break;
        }

        Solution solution = recover(nextToken, ParserRuleContext.TERMINAL_EXPRESSION, annots, isRhsExpr, allowActions,
                isInConditionalExpr);

        if (solution.action == Action.KEEP) {
            if (nextToken.kind == SyntaxKind.XML_KEYWORD) {
                return parseXMLTemplateExpression();
            }

            return parseStringTemplateExpression();
        }

        return parseTerminalExpression(annots, isRhsExpr, allowActions, isInConditionalExpr);
    }

    private void validateExpressionAnnotations(STToken nextToken, STNode annots) {
        switch (nextToken.kind) {
            case CLIENT_KEYWORD:
            case OBJECT_KEYWORD:
            case FUNCTION_KEYWORD:
            case ISOLATED_KEYWORD:
            case START_KEYWORD:
            case SERVICE_KEYWORD:
                break;
            case TRANSACTIONAL_KEYWORD:
                STToken nextNextToken = getNextNextToken(nextToken.kind);
                if (nextNextToken.kind == SyntaxKind.ISOLATED_KEYWORD ||
                        nextNextToken.kind == SyntaxKind.FUNCTION_KEYWORD) {
                    break;
                }
                // fall through
            default:
                addInvalidNodeToNextToken(annots, DiagnosticErrorCode.ERROR_ANNOTATIONS_ATTACHED_TO_EXPRESSION);
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
            case STRING_KEYWORD:
            case FUNCTION_KEYWORD:
            case AT_TOKEN:
            case NEW_KEYWORD:
            case START_KEYWORD:
            case FLUSH_KEYWORD:
            case LEFT_ARROW_TOKEN:
            case WAIT_KEYWORD:
            case SERVICE_KEYWORD:
            case ISOLATED_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
                return true;
            default:
                return isSimpleType(tokenKind);
        }
    }

    /**
     * <p>
     * Parse a new expression.
     * </p>
     * <code>
     * new-expr := explicit-new-expr | implicit-new-expr
     * <br/>
     * explicit-new-expr := new type-descriptor ( arg-list )
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

    private STNode parseNewKeywordRhs(STNode newKeyword) {
        STNode token = peek();
        return parseNewKeywordRhs(token.kind, newKeyword);
    }

    /**
     * <p>
     * Parse an implicit or explicit new expression.
     * </p>
     *
     * @param kind       next token kind.
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
            case STREAM_KEYWORD:
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
     * explicit-new-expr := new type-descriptor ( arg-list )
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
            actionOrExpression = attachErrorExpectedActionFoundDiagnostic(actionOrExpression);
        }
        return actionOrExpression;
    }

    private STNode parseExpressionRhsInternal(OperatorPrecedence currentPrecedenceLevel, STNode lhsExpr,
                                              boolean isRhsExpr, boolean allowActions, boolean isInMatchGuard,
                                              boolean isInConditionalExpr) {
        SyntaxKind nextTokenKind = peek().kind;
        if (isEndOfExpression(nextTokenKind, isRhsExpr, isInMatchGuard, lhsExpr.kind)) {
            return lhsExpr;
        }

        if (lhsExpr.kind == SyntaxKind.ASYNC_SEND_ACTION) {
            // Async-send action can only exists in an action-statement. It also has to be the
            // right-most action. i.e: Should be followed by a semicolon
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
                newLhsExpr = parseTypeTestExpression(lhsExpr, isInConditionalExpr);
                break;
            case RIGHT_ARROW_TOKEN:
                newLhsExpr = parseRemoteMethodCallOrAsyncSendAction(lhsExpr, isRhsExpr);
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
                // Handle 'a/<b|c>...' scenarios. These have ambiguity between being a xml-step expr
                // or a binary expr (division), with a type-cast as the denominator.
                if (nextTokenKind == SyntaxKind.SLASH_TOKEN && peek(2).kind == SyntaxKind.LT_TOKEN) {
                    SyntaxKind expectedNodeType = getExpectedNodeKind(3, isRhsExpr, isInMatchGuard, lhsExpr.kind);
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

                // Actions within binary-expressions are not allowed.
                if (isAction(lhsExpr) && lhsExpr.kind != SyntaxKind.BRACED_ACTION) {
                    lhsExpr = attachErrorExpectedActionFoundDiagnostic(lhsExpr);
                }

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
        Solution solution = recover(token, ParserRuleContext.EXPRESSION_RHS, currentPrecedenceLevel, lhsExpr, isRhsExpr,
                allowActions, isInMatchGuard, isInConditionalExpr);

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
            return parseExpressionRhsInternal(currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions, isInMatchGuard,
                    isInConditionalExpr);
        } else {
            return parseExpressionRhsInternal(currentPrecedenceLevel, lhsExpr, isRhsExpr, allowActions, isInMatchGuard,
                    isInConditionalExpr);
        }
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

    private SyntaxKind getExpectedNodeKind(int lookahead, boolean isRhsExpr, boolean isInMatchGuard,
                                           SyntaxKind precedingNodeKind) {
        STToken nextToken = peek(lookahead);
        switch (nextToken.kind) {
            case ASTERISK_TOKEN:
                return SyntaxKind.XML_STEP_EXPRESSION;
            case GT_TOKEN:
                break;
            case PIPE_TOKEN:
                return getExpectedNodeKind(++lookahead, isRhsExpr, isInMatchGuard, precedingNodeKind);
            case IDENTIFIER_TOKEN:
                nextToken = peek(++lookahead);
                switch (nextToken.kind) {
                    case GT_TOKEN: // a>
                        break;
                    case PIPE_TOKEN: // a|
                        return getExpectedNodeKind(++lookahead, isRhsExpr, isInMatchGuard, precedingNodeKind);
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
                                    return getExpectedNodeKind(++lookahead, isRhsExpr, isInMatchGuard,
                                            precedingNodeKind);
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
        if (token.kind == SyntaxKind.MAP_KEYWORD || token.kind == SyntaxKind.START_KEYWORD) {
            STNode methodName = getKeywordAsSimpleNameRef();
            STNode openParen = parseOpenParenthesis(ParserRuleContext.ARG_LIST_START);
            STNode args = parseArgsList();
            STNode closeParen = parseCloseParenthesis();
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
            STNode openParen = parseOpenParenthesis(ParserRuleContext.ARG_LIST_START);
            STNode args = parseArgsList();
            STNode closeParen = parseCloseParenthesis();
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

        return parseBracedExprOrAnonFuncParamRhs(openParen, expr, isRhsExpr);
    }

    private STNode parseNilLiteralOrEmptyAnonFuncParamRhs(STNode openParen) {
        STNode closeParen = parseCloseParenthesis();
        STToken nextToken = peek();
        if (nextToken.kind != SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN) {
            return STNodeFactory.createNilLiteralNode(openParen, closeParen);
        } else {
            STNode params = STNodeFactory.createEmptyNodeList();
            STNode anonFuncParam =
                    STNodeFactory.createImplicitAnonymousFunctionParameters(openParen, params, closeParen);
            return anonFuncParam;
        }
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
                    recover(nextToken, ParserRuleContext.BRACED_EXPR_OR_ANON_FUNC_PARAM_RHS, openParen, expr,
                            isRhsExpr);
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
    private boolean isEndOfExpression(SyntaxKind tokenKind, boolean isRhsExpr, boolean isInMatchGuard,
                                      SyntaxKind precedingNodeKind) {
        if (!isRhsExpr) {
            if (isCompoundBinaryOperator(tokenKind)) {
                return true;
            }

            if (isInMatchGuard && tokenKind == SyntaxKind.RIGHT_DOUBLE_ARROW_TOKEN) {
                return true;
            }

            return !isValidExprRhsStart(tokenKind, precedingNodeKind);
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
        STNode errorKeyword = parseErrorKeyword();
        errorKeyword = createBuiltinSimpleNameReference(errorKeyword);
        return parseFuncCall(errorKeyword);
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
                    isMissingPositionalArg(curArg)) {
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
                errorCode = DiagnosticErrorCode.ERROR_ARG_FOLLOWED_BY_REST_ARG;
                break;
            default:
                // This line should never get reached
                throw new IllegalStateException("Invalid SyntaxKind in an argument");
        }
        return errorCode;
    }

    private boolean isMissingPositionalArg(STNode arg) {
        STNode expr = ((STPositionalArgumentNode) arg).expression;
        return expr.kind == SyntaxKind.SIMPLE_NAME_REFERENCE && ((STSimpleNameReferenceNode) expr).name.isMissing();
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
                // TODO: Handle package-qualified var-refs (i.e: qualified-identifier).
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
     * <code>(client | isolated)* object { object-member-descriptor* }</code>
     *
     * @return Parsed node
     */
    private STNode parseObjectTypeDescriptor() {
        startContext(ParserRuleContext.OBJECT_TYPE_DESCRIPTOR);
        STNode objectTypeQualifiers = parseObjectTypeQualifiers();
        STNode objectKeyword = parseObjectKeyword();
        STNode openBrace = parseOpenBrace();
        STNode objectMemberDescriptors = parseObjectMembers(ParserRuleContext.OBJECT_MEMBER_DESCRIPTOR);
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createObjectTypeDescriptorNode(objectTypeQualifiers, objectKeyword, openBrace,
                objectMemberDescriptors, closeBrace);
    }

    /**
     * <p>
     * Parse object constructor expression.
     * </p>
     * <code>
     * object-constructor-expr := [annots] [client] object [type-reference] { object-member* }
     * <br/>
     * object-member := object-field | method-defn
     * </code>
     *
     * @param annots annotations attached to object constructor
     * @return Parsed object constructor expression node
     */
    private STNode parseObjectConstructorExpression(STNode annots) {
        startContext(ParserRuleContext.OBJECT_CONSTRUCTOR);
        STNode objectTypeQualifier = parseObjectConstructorQualifiers();
        STNode objectKeyword = parseObjectKeyword();
        STNode typeReference = parseObjectConstructorTypeReference();
        STNode openBrace = parseOpenBrace();
        STNode objectMembers = parseObjectMembers(ParserRuleContext.OBJECT_MEMBER);
        STNode closeBrace = parseCloseBrace();
        endContext();
        return STNodeFactory.createObjectConstructorExpressionNode(annots,
                objectTypeQualifier, objectKeyword, typeReference, openBrace, objectMembers, closeBrace);
    }

    /**
     * <p>
     * Parse object constructor qualifiers.
     * </p>
     * <code>object-constructor-qualifier := [client]</code>
     *
     * @return Parsed node
     */
    private STNode parseObjectConstructorQualifiers() {
        List<STNode> qualifiers = new ArrayList<>();
        STNode qualifier;
        for (int i = 0; i < 2; i++) {
            STToken nextToken = peek();
            if (isNodeWithSyntaxKindInList(qualifiers, nextToken.kind)) {
                qualifier = consume();
                updateLastNodeInListOrAddInvalidNodeToNextToken(qualifiers, nextToken,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            qualifier = parseSingleObjectConstructorQualifier();
            if (qualifier == null) {
                return STNodeFactory.createNodeList(qualifiers);
            }

            if (qualifier.kind == SyntaxKind.ISOLATED_KEYWORD) {
                updateLastNodeInListOrAddInvalidNodeToNextToken(qualifiers, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
                continue;
            }

            qualifiers.add(qualifier);
        }

        return STNodeFactory.createNodeList(qualifiers);
    }

    private STNode parseSingleObjectConstructorQualifier() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case CLIENT_KEYWORD:
            case ISOLATED_KEYWORD: // Here we allow parsing isolated qualifier and then log an error
                return consume();
            case OBJECT_KEYWORD:
            case EOF_TOKEN:
                // null indicates the end of qualifiers
                return null;
            default:
                recover(nextToken, ParserRuleContext.OBJECT_TYPE_QUALIFIER);
                return parseSingleObjectConstructorQualifier();
        }
    }

    /**
     * <p>
     * Parse object type descriptor qualifiers.
     * </p>
     * <code>object-type-descriptor-qualifiers := (client | isolated)*</code>
     *
     * @return Parsed node
     */
    private STNode parseObjectTypeQualifiers() {
        List<STNode> qualifiers = new ArrayList<>();
        STNode qualifier;
        for (int i = 0; i < 4; i++) {
            STToken nextToken = peek();
            if (isNodeWithSyntaxKindInList(qualifiers, nextToken.kind)) {
                qualifier = consume();
                updateLastNodeInListOrAddInvalidNodeToNextToken(qualifiers, nextToken,
                        DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER, ((STToken) qualifier).text());
                continue;
            }

            qualifier = parseSingleObjectTypeQualifier();
            if (qualifier == null) {
                return STNodeFactory.createNodeList(qualifiers);
            }

            if (qualifier.kind == SyntaxKind.ABSTRACT_KEYWORD || qualifier.kind == SyntaxKind.READONLY_KEYWORD) {
                updateLastNodeInListOrAddInvalidNodeToNextToken(qualifiers, qualifier,
                        DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED, ((STToken) qualifier).text());
                continue;
            }

            qualifiers.add(qualifier);
        }

        return STNodeFactory.createNodeList(qualifiers);
    }

    private STNode parseSingleObjectTypeQualifier() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case CLIENT_KEYWORD:
            case ISOLATED_KEYWORD:
                // Here we allow parsing of old object type qualifiers (`abstract` and `readonly`)
                // and then log an error
            case READONLY_KEYWORD:
            case ABSTRACT_KEYWORD:
                return consume();
            case OBJECT_KEYWORD:
            case EOF_TOKEN:
                // null indicates the end of qualifiers
                return null;
            default:
                recover(nextToken, ParserRuleContext.OBJECT_TYPE_QUALIFIER);
                return parseSingleObjectTypeQualifier();
        }
    }

    /**
     * Parse object constructor expression type reference.
     *
     * @return Parsed type reference or empty node
     */
    private STNode parseObjectConstructorTypeReference() {
        STNode typeReference;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                typeReference = parseTypeReference();
                break;
            case OPEN_BRACE_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                recover(nextToken, ParserRuleContext.OBJECT_CONSTRUCTOR_TYPE_REF);
                return parseObjectConstructorTypeReference();
        }
        return typeReference;
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

            if (context == ParserRuleContext.OBJECT_MEMBER && member.kind == SyntaxKind.TYPE_REFERENCE) {
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
            case RESOURCE_KEYWORD:// resource qualifier is not allowed but let it pass here and validate in
                // parseFunctionQualifiers method
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
                if (context == ParserRuleContext.OBJECT_MEMBER) {
                    recoveryCtx = ParserRuleContext.OBJECT_MEMBER_START;
                } else {
                    recoveryCtx = ParserRuleContext.CLASS_MEMBER_START;
                }

                recover(peek(), recoveryCtx);
                return parseObjectMember(context);
        }

        return parseObjectMemberWithoutMeta(metadata, context);
    }

    private STNode parseObjectMemberWithoutMeta(STNode metadata, ParserRuleContext context) {
        boolean isObjectTypeDesc = context == ParserRuleContext.OBJECT_MEMBER_DESCRIPTOR;
        STNode member;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EOF_TOKEN:
            case CLOSE_BRACE_TOKEN:
                reportInvalidMetaData(metadata);
                return null;
            case PUBLIC_KEYWORD:
            case PRIVATE_KEYWORD:
                STNode visibilityQualifier = consume();
                if (isObjectTypeDesc && visibilityQualifier.kind == SyntaxKind.PRIVATE_KEYWORD) {
                    addInvalidNodeToNextToken(visibilityQualifier, DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED,
                            visibilityQualifier.toString().trim());
                    visibilityQualifier = STNodeFactory.createEmptyNode();
                }
                member = parseObjectMethodOrField(metadata, visibilityQualifier, isObjectTypeDesc);
                break;
            case ISOLATED_KEYWORD:
                if (isFuncDefOrFuncTypeStart()) {
                    member = parseObjectMethodOrFuncTypeDesc(metadata, new ArrayList<>(), isObjectTypeDesc);
                } else {
                    member =  parseObjectField(metadata, STNodeFactory.createEmptyNode(), isObjectTypeDesc);
                }
                break;
            case REMOTE_KEYWORD:
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case RESOURCE_KEYWORD: // resource qualifier is not allowed but let it pass here and validate in
                // parseFunctionQualifiers method
                member = parseObjectMethodOrFuncTypeDesc(metadata, new ArrayList<>(), isObjectTypeDesc);
                break;
            case ASTERISK_TOKEN:
                reportInvalidMetaData(metadata);
                STNode asterisk = consume();
                STNode type = parseTypeReference();
                STNode semicolonToken = parseSemicolon();
                member = STNodeFactory.createTypeReferenceNode(asterisk, type, semicolonToken);
                break;
            default:
                if (nextToken.kind == SyntaxKind.FINAL_KEYWORD || isTypeStartingToken(nextToken.kind)) {
                    member = parseObjectField(metadata, STNodeFactory.createEmptyNode(), isObjectTypeDesc);
                    break;
                }

                ParserRuleContext recoveryCtx;
                if (context == ParserRuleContext.OBJECT_MEMBER) {
                    recoveryCtx = ParserRuleContext.OBJECT_MEMBER_WITHOUT_METADATA;
                } else {
                    recoveryCtx = ParserRuleContext.CLASS_MEMBER_WITHOUT_METADATA;
                }

                recover(peek(), recoveryCtx, metadata);
                return parseObjectMemberWithoutMeta(metadata, context);
        }

        return member;
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
        STToken nextToken = peek(1);
        STToken nextNextToken = peek(2);
        List<STNode> qualifiers = new ArrayList<>();
        switch (nextToken.kind) {
            case ISOLATED_KEYWORD:
                if (!isFuncDefOrFuncTypeStart()) {
                    return parseObjectField(metadata, visibilityQualifier, isObjectTypeDesc);
                }
                // Else fall through
            case REMOTE_KEYWORD:
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case RESOURCE_KEYWORD: // resource qualifier is not allowed but let it pass here and validate in
                // parseFunctionQualifiers method
                if (visibilityQualifier != null) {
                    qualifiers.add(visibilityQualifier);
                }
                return parseObjectMethodOrFuncTypeDesc(metadata, qualifiers, isObjectTypeDesc);

            // All 'type starting tokens' here. should be same as 'parseTypeDescriptor(...)'
            case IDENTIFIER_TOKEN:
                if (nextNextToken.kind != SyntaxKind.OPEN_PAREN_TOKEN) {
                    // Here we try to catch the common user error of missing the function keyword.
                    // In such cases, lookahead for the open-parenthesis and figure out whether
                    // this is an object-method with missing name. If yes, then try to recover.
                    return parseObjectField(metadata, visibilityQualifier, isObjectTypeDesc);
                }
                break;
            default:
                if (nextToken.kind == SyntaxKind.FINAL_KEYWORD || isTypeStartingToken(nextToken.kind)) {
                    return parseObjectField(metadata, visibilityQualifier, isObjectTypeDesc);
                }
                break;
        }

        recover(peek(), ParserRuleContext.OBJECT_FUNC_OR_FIELD_WITHOUT_VISIBILITY, metadata, visibilityQualifier);
        return parseObjectMethodOrField(metadata, visibilityQualifier, isObjectTypeDesc);
    }

    /**
     * Parse function qualifiers.
     *
     * @return Parsed node
     */
    private STNode parseFunctionQualifiers(ParserRuleContext context, List<STNode> qualifierList) {
        STToken nextToken = peek();
        while (!isEndOfFunctionQualifiers(nextToken.kind)) {
            STToken qualifier;
            switch (nextToken.kind) {
                case REMOTE_KEYWORD:
                case TRANSACTIONAL_KEYWORD:
                case RESOURCE_KEYWORD:
                case ISOLATED_KEYWORD:
                    qualifier = consume();
                    break;
                default:
                    recover(peek(), context, context, qualifierList);
                    return parseFunctionQualifiers(context, qualifierList);
            }

            DiagnosticCode diagnosticCode = validateFunctionQualifier(qualifier, context, qualifierList);
            if (diagnosticCode != null) {
                updateLastNodeInListOrAddInvalidNodeToNextToken(qualifierList, qualifier, diagnosticCode,
                        qualifier.text());
            } else {
                qualifierList.add(qualifier);
            }

            nextToken = peek();
        }

        return STNodeFactory.createNodeList(qualifierList);
    }

    private boolean isEndOfFunctionQualifiers(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case FUNCTION_KEYWORD:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    private DiagnosticCode validateFunctionQualifier(STNode currentQualifier, ParserRuleContext context,
                                                     List<STNode> qualifierList) {
        switch (currentQualifier.kind) {
            case REMOTE_KEYWORD:
                if (context != ParserRuleContext.OBJECT_METHOD_START) {
                    return DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED;
                }
                break;
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
                break;
            default:// RESOURCE_KEYWORD
                if (context != ParserRuleContext.RESOURCE_DEF_QUALIFIERS) {
                    return DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED;
                }
        }

        if (isNodeWithSyntaxKindInList(qualifierList, currentQualifier.kind)) {
            return DiagnosticErrorCode.ERROR_DUPLICATE_QUALIFIER;
        }

        return null;
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
     * @param metadata Preceding metadata
     * @param visibilityQualifier Preceding visibility qualifier
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed node
     */
    private STNode parseObjectField(STNode metadata, STNode visibilityQualifier, boolean isObjectTypeDesc) {
        STToken nextToken = peek();
        STNode finalQualifier = STNodeFactory.createEmptyNode();
        if (nextToken.kind == SyntaxKind.FINAL_KEYWORD) {
            finalQualifier = consume();
        }

        if (finalQualifier != null && isObjectTypeDesc) {
            addInvalidNodeToNextToken(finalQualifier, DiagnosticErrorCode.ERROR_QUALIFIER_NOT_ALLOWED,
                    ((STToken) finalQualifier).text());
            finalQualifier = STNodeFactory.createEmptyNode();
        }

        STNode type = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode fieldName = parseVariableName();
        return parseObjectFieldRhs(metadata, visibilityQualifier, finalQualifier, type, fieldName,
                isObjectTypeDesc);
    }

    /**
     * Parse object field rhs, and complete the object field parsing. Returns the parsed object field.
     *
     * @param metadata            Metadata
     * @param visibilityQualifier Visibility qualifier
     * @param finalQualifier      Final qualifier
     * @param type                Type descriptor
     * @param fieldName           Field name
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed object field
     */
    private STNode parseObjectFieldRhs(STNode metadata, STNode visibilityQualifier, STNode finalQualifier,
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
                if (!isObjectTypeDesc) {
                    equalsToken = parseAssignOp();
                    expression = parseExpression();
                    semicolonToken = parseSemicolon();
                    break;
                }
                // Else fall through
            default:
                recover(peek(), ParserRuleContext.OBJECT_FIELD_RHS, metadata, visibilityQualifier, finalQualifier,
                        type, fieldName);
                return parseObjectFieldRhs(metadata, visibilityQualifier, finalQualifier, type, fieldName,
                        isObjectTypeDesc);
        }

        return STNodeFactory.createObjectFieldNode(metadata, visibilityQualifier, finalQualifier, type, fieldName,
                equalsToken, expression, semicolonToken);
    }

    /**
     * Parse method definition or declaration.
     *
     * @param metadata Preceding metadata
     * @param qualifiers Preceding visibility qualifier
     * @param isObjectTypeDesc Whether object type or not
     * @return Parsed node
     */
    private STNode parseObjectMethodOrFuncTypeDesc(STNode metadata, List<STNode> qualifiers, boolean isObjectTypeDesc) {
        return parseFuncDefOrFuncTypeDesc(ParserRuleContext.OBJECT_METHOD_START, metadata, qualifiers, true,
                isObjectTypeDesc);
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
                recover(nextToken, fieldContext, fieldContext);
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
                recover(peek(), ParserRuleContext.SPECIFIC_FIELD, readonlyKeyword);
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

                recover(nextToken, ParserRuleContext.SPECIFIC_FIELD_RHS, readonlyKeyword, key);
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
        if (isCompoundBinaryOperator(token.kind)) {
            return consume();
        } else {
            recover(token, ParserRuleContext.COMPOUND_BINARY_OPERATOR);
            return parseCompoundBinaryOperator();
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
     * @param metadata       Metadata
     * @param serviceKeyword Service keyword
     * @return Parsed node
     */
    private STNode parseServiceRhs(STNode metadata, STNode serviceKeyword) {
        STNode serviceName = parseServiceName();
        STNode onKeyword = parseOnKeyword();
        STNode expressionList = parseListeners();
        STNode serviceBody = parseServiceBody();

        onKeyword =
                cloneWithDiagnosticIfListEmpty(expressionList, onKeyword, DiagnosticErrorCode.ERROR_MISSING_EXPRESSION);
        return STNodeFactory.createServiceDeclarationNode(metadata, serviceKeyword, serviceName, onKeyword,
                expressionList, serviceBody);
    }

    private STNode parseServiceName() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case IDENTIFIER_TOKEN:
                return parseIdentifier(ParserRuleContext.SERVICE_NAME);
            case ON_KEYWORD:
                return STNodeFactory.createEmptyNode();
            default:
                recover(nextToken, ParserRuleContext.OPTIONAL_SERVICE_NAME);
                return parseServiceName();
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
    private boolean isCompoundBinaryOperator(SyntaxKind tokenKind) {
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
        STNode metadata;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case RESOURCE_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
            case ISOLATED_KEYWORD:
            case FUNCTION_KEYWORD:
            case REMOTE_KEYWORD: // remote qualifier is not allowed but let it pass here and validate in
                // parseFunctionQualifiers method
                metadata = STNodeFactory.createEmptyNode();
                break;
            case DOCUMENTATION_STRING:
            case AT_TOKEN:
                metadata = parseMetaData();
                break;
            default:
                if (isEndOfServiceDecl(nextToken.kind)) {
                    return null;
                }

                recover(peek(), ParserRuleContext.RESOURCE_DEF);
                return parseResource();
        }

        return parseResource(metadata);
    }

    private STNode parseResource(STNode metadata) {
        STNode qualifierList = parseFunctionQualifiers(ParserRuleContext.RESOURCE_DEF_QUALIFIERS, new ArrayList<>());
        return parseFuncDefinition(metadata, false, qualifierList);
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
        STNode constDecl = parseConstDecl(metadata, qualifier, constKeyword);
        endContext();
        return constDecl;
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
                return parseAnnotationDeclaration(metadata, qualifier, constKeyword);
            case IDENTIFIER_TOKEN:
                return parseConstantOrListenerDeclWithOptionalType(metadata, qualifier, constKeyword, false);
            default:
                if (isTypeStartingToken(nextToken.kind)) {
                    break;
                }

                recover(peek(), ParserRuleContext.CONST_DECL_TYPE, metadata, qualifier, constKeyword);
                return parseConstDecl(metadata, qualifier, constKeyword);
        }

        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_BEFORE_IDENTIFIER);
        STNode variableName = parseVariableName();
        STNode equalsToken = parseAssignOp();
        STNode initializer = parseExpression();
        STNode semicolonToken = parseSemicolon();
        return STNodeFactory.createConstantDeclarationNode(metadata, qualifier, constKeyword, typeDesc, variableName,
                equalsToken, initializer, semicolonToken);
    }

    private STNode parseConstantOrListenerDeclWithOptionalType(STNode metadata, STNode qualifier, STNode constKeyword,
                                                               boolean isListener) {
        STNode varNameOrTypeName = parseStatementStartIdentifier();
        STNode constDecl =
                parseConstantOrListenerDeclRhs(metadata, qualifier, constKeyword, varNameOrTypeName, isListener);
        return constDecl;
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
                recover(peek(), ParserRuleContext.CONST_DECL_RHS, metadata, qualifier, keyword, typeOrVarName,
                        isListener);
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
        typeDescriptorNode = validateForUsageOfVar(typeDescriptorNode);
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
        return STNodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openBracketToken, arrayLengthNode,
                closeBracketToken);
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
     * <i>Note: In the ballerina spec ({@link https://ballerina.io/spec/lang/2020R1/#annots})
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
     * Parse is expression.
     * <code>
     * is-expr := expression is type-descriptor
     * </code>
     *
     * @param lhsExpr Preceding expression of the is expression
     * @return Is expression node
     */
    private STNode parseTypeTestExpression(STNode lhsExpr, boolean isInConditionalExpr) {
        STNode isKeyword = parseIsKeyword();
        STNode typeDescriptor =
                parseTypeDescriptorInExpression(ParserRuleContext.TYPE_DESC_IN_EXPRESSION, isInConditionalExpr);
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
            recover(token, ParserRuleContext.IS_KEYWORD);
            return parseIsKeyword();
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
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case EQUAL_TOKEN:
                switchContext(ParserRuleContext.ASSIGNMENT_STMT);
                return parseAssignmentStmtRhs(expression);
            case SEMICOLON_TOKEN:
                return getExpressionAsStatement(expression);
            case IDENTIFIER_TOKEN:
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundBinaryOperator(nextToken.kind)) {
                    return parseCompoundAssignmentStmtRhs(expression);
                }

                ParserRuleContext context;
                if (isPossibleExpressionStatement(expression)) {
                    context = ParserRuleContext.EXPR_STMT_RHS;
                } else {
                    context = ParserRuleContext.STMT_START_WITH_EXPR_RHS;
                }

                recover(peek(), context, expression);
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
                STNode semicolon = parseSemicolon();
                endContext();
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
            case ASTERISK_LITERAL:
            case SIMPLE_NAME_REFERENCE:
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
        // TODO Validate the expression.
        // This is not a must because this expression is validated in the semantic analyzer.
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
     * Parse remote method call action, given the starting expression.
     * <p>
     * <code>
     * remote-method-call-action := expression -> method-name ( arg-list )
     * <br/>
     * async-send-action := expression -> peer-worker ;
     * </code>
     *
     * @param isRhsExpr  Is this an RHS action
     * @param expression LHS expression
     * @return
     */
    private STNode parseRemoteMethodCallOrAsyncSendAction(STNode expression, boolean isRhsExpr) {
        STNode rightArrow = parseRightArrow();
        return parseRemoteCallOrAsyncSendActionRhs(expression, isRhsExpr, rightArrow);
    }

    private STNode parseRemoteCallOrAsyncSendActionRhs(STNode expression, boolean isRhsExpr, STNode rightArrow) {
        STNode name;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case DEFAULT_KEYWORD:
                STNode defaultKeyword = parseDefaultKeyword();
                name = STNodeFactory.createSimpleNameReferenceNode(defaultKeyword);
                return parseAsyncSendAction(expression, rightArrow, name);
            case IDENTIFIER_TOKEN:
                name = STNodeFactory.createSimpleNameReferenceNode(parseFunctionName());
                break;
            case CONTINUE_KEYWORD:
            case COMMIT_KEYWORD:
                name = getKeywordAsSimpleNameRef();
                break;
            default:
                STToken token = peek();
                recover(token, ParserRuleContext.REMOTE_CALL_OR_ASYNC_SEND_RHS, expression, isRhsExpr, rightArrow);
                return parseRemoteCallOrAsyncSendActionRhs(expression, isRhsExpr, rightArrow);
        }

        return parseRemoteCallOrAsyncSendEnd(expression, rightArrow, name);
    }

    private STNode parseRemoteCallOrAsyncSendEnd(STNode expression, STNode rightArrow, STNode name) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                return parseRemoteMethodCallAction(expression, rightArrow, name);
            case SEMICOLON_TOKEN:
                return parseAsyncSendAction(expression, rightArrow, name);
            default:
                recover(peek(), ParserRuleContext.REMOTE_CALL_OR_ASYNC_SEND_END, expression, rightArrow, name);
                return parseRemoteCallOrAsyncSendEnd(expression, rightArrow, name);
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
            return consume();
        } else {
            recover(token, ParserRuleContext.DEFAULT_KEYWORD);
            return parseDefaultKeyword();
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
            recover(nextToken, ParserRuleContext.RIGHT_ARROW);
            return parseRightArrow();
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
        STNode typeParameter = parseTypeParameter();
        return STNodeFactory.createParameterizedTypeDescriptorNode(parameterizedTypeKeyword, typeParameter);
    }

    /**
     * Parse <code>map</code> or <code>future</code> keyword token.
     *
     * @return Parsed node
     */
    private STNode parseParameterizedTypeKeyword() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case MAP_KEYWORD: // map type desc
            case FUTURE_KEYWORD: // future type desc
                return consume();
            default:
                recover(nextToken, ParserRuleContext.PARAMETERIZED_TYPE);
                return parseParameterizedTypeKeyword();
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
        STNode openParenthesisToken = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
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

                recover(peek(), ParserRuleContext.ANNOT_DECL_OPTIONAL_TYPE, metadata, qualifier, constKeyword,
                        annotationKeyword);
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
                recover(peek(), ParserRuleContext.ANNOT_DECL_RHS, metadata, qualifier, constKeyword, annotationKeyword,
                        typeDescOrAnnotTag);
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
                recover(peek(), ParserRuleContext.ANNOT_OPTIONAL_ATTACH_POINTS, metadata, qualifier, constKeyword,
                        annotationKeyword, typeDesc, annotTag);
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
     *     type
     *     | class
     * | [object|resource] function
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
                attachPoint = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
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
            case RESOURCE_KEYWORD:
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
     * dual-attach-point-ident := [object] type | [object|resource] function | parameter
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
            case CLASS_KEYWORD:
                firstIdent = consume();
                return parseDualAttachPointIdent(sourceKeyword, firstIdent);
            default:
                recover(peek(), ParserRuleContext.ATTACH_POINT_IDENT, sourceKeyword);
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
            case TYPE_KEYWORD:
            case FUNCTION_KEYWORD:
            case PARAMETER_KEYWORD:
            case RETURN_KEYWORD:
            case SERVICE_KEYWORD:
            case FIELD_KEYWORD:
            case CLASS_KEYWORD:
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
        switch (peek().kind) {
            case STRING_LITERAL_TOKEN:
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case NULL_KEYWORD:
                return parseBasicLiteral();
            case IDENTIFIER_TOKEN:
                return parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return parseSignedIntOrFloat();
            case OPEN_PAREN_TOKEN:
                return parseNilLiteral();
            default:
                STToken token = peek();
                recover(token, ParserRuleContext.CONSTANT_EXPRESSION_START);
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
                recover(peek(), ParserRuleContext.XML_NAMESPACE_PREFIX_DECL, xmlnsKeyword, namespaceUri, isModuleVar);
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
     * @return Parsed node
     */
    private STNode parseNamedWorkerDeclaration(STNode annots) {
        startContext(ParserRuleContext.NAMED_WORKER_DECL);

        STNode transactionalKeyword = parseOptionalTransactionalKeyword();
        STNode workerKeyword = parseWorkerKeyword();
        STNode workerName = parseWorkerName();
        STNode returnTypeDesc = parseReturnTypeDescriptor();
        STNode workerBody = parseBlockNode();
        endContext();
        return STNodeFactory.createNamedWorkerDeclarationNode(annots, transactionalKeyword, workerKeyword, workerName,
                returnTypeDesc, workerBody);
    }

    private STNode parseOptionalTransactionalKeyword() {
        STNode transactionalKeyword = STNodeFactory.createEmptyNode();
        STToken token = peek();
        switch (token.kind) {
            case TRANSACTIONAL_KEYWORD:
                transactionalKeyword = parseTransactionalKeyword();
                break;
            case WORKER_KEYWORD:
                break;
            default:
                recover(token, ParserRuleContext.NAMED_WORKER_DECL_START);
                parseOptionalTransactionalKeyword();
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
        STNode pipeToken = parsePipeToken();
        STNode rightTypeDesc = parseTypeDescriptor(context, isTypedBindingPattern, false);
        return createUnionTypeDesc(leftTypeDesc, pipeToken, rightTypeDesc);
    }

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
            case DISTINCT_KEYWORD:
            case ISOLATED_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
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
            case TYPEDESC_KEYWORD: // This is for recovery logic. <code>typedesc a;</code> scenario recovered here.
            case READONLY_KEYWORD:
            case DISTINCT_KEYWORD:
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
            case READONLY_KEYWORD:
                return SyntaxKind.READONLY_TYPE_DESC;
            case NEVER_KEYWORD:
                return SyntaxKind.NEVER_TYPE_DESC;
            case SERVICE_KEYWORD:
                return SyntaxKind.SERVICE_TYPE_DESC;
            case VAR_KEYWORD:
                return SyntaxKind.VAR_TYPE_DESC;
            case ERROR_KEYWORD:
                return SyntaxKind.ERROR_TYPE_DESC;
            default:
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

            //Local type def is not allowed in new spec, hence add it as invalid node minutia.
            if (stmt.kind == SyntaxKind.LOCAL_TYPE_DEFINITION_STATEMENT) {
                addInvalidNodeToNextToken(stmt, DiagnosticErrorCode.ERROR_LOCAL_TYPE_DEFINITION_NOT_ALLOWED);
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
        STNode expr =
                parseExpression(OperatorPrecedence.EXPRESSION_ACTION, isRhsExpr, allowActions, isInConditionalExpr);
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
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.LIST_CONSTRUCTOR_MEMBER_END);
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
        STNode errorKeywordToken = parseErrorKeyword();
        return parseErrorTypeDescriptor(errorKeywordToken);
    }

    private STNode parseErrorTypeDescriptor(STNode errorKeywordToken) {
        STNode errorTypeParamsNode;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.LT_TOKEN) {
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
     * Parse typedesc type descriptor.
     * typedesc-type-descriptor := typedesc type-parameter
     *
     * @return Parsed typedesc type node
     */
    private STNode parseTypedescTypeDescriptor() {
        STNode typedescKeywordToken = parseTypedescKeyword();
        STNode typedescTypeParamsNode;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.LT_TOKEN) {
            typedescTypeParamsNode = parseTypeParameter();
        } else {
            typedescTypeParamsNode = STNodeFactory.createEmptyNode();
        }
        return STNodeFactory.createTypedescTypeDescriptorNode(typedescKeywordToken, typedescTypeParamsNode);
    }

    /**
     * Parse typedesc-keyword.
     *
     * @return Parsed typedesc-keyword node
     */
    private STNode parseTypedescKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TYPEDESC_KEYWORD) {
            return consume();
        } else {
            recover(token, ParserRuleContext.TYPEDESC_KEYWORD);
            return parseTypedescKeyword();
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
     * Parse xml type descriptor.
     * xml-type-descriptor := xml type-parameter
     *
     * @return Parsed typedesc type node
     */
    private STNode parseXmlTypeDescriptor() {
        STNode xmlKeywordToken = parseXMLKeyword();
        STNode typedescTypeParamsNode;
        STToken nextToken = peek();
        if (nextToken.kind == SyntaxKind.LT_TOKEN) {
            typedescTypeParamsNode = parseTypeParameter();
        } else {
            typedescTypeParamsNode = STNodeFactory.createEmptyNode();
        }
        return STNodeFactory.createXmlTypeDescriptorNode(xmlKeywordToken, typedescTypeParamsNode);
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
        STNode leftTypeDescNode =
                parseTypeDescriptorWithoutContext(ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC, false);
        STNode streamTypedesc = parseStreamTypeParamsNode(ltToken, leftTypeDescNode);
        endContext();
        return streamTypedesc;
    }

    private STNode parseStreamTypeParamsNode(STNode ltToken, STNode leftTypeDescNode) {
        STNode commaToken, rightTypeDescNode, gtToken;
        switch (peek().kind) {
            case COMMA_TOKEN:
                commaToken = parseComma();
                rightTypeDescNode =
                        parseTypeDescriptorWithoutContext(ParserRuleContext.TYPE_DESC_IN_STREAM_TYPE_DESC, false);
                break;
            case GT_TOKEN:
                commaToken = STNodeFactory.createEmptyNode();
                rightTypeDescNode = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.STREAM_TYPE_FIRST_PARAM_RHS, ltToken, leftTypeDescNode);
                return parseStreamTypeParamsNode(ltToken, leftTypeDescNode);
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
            recover(token, ParserRuleContext.STREAM_KEYWORD);
            return parseStreamKeyword();
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
        letKeyword = cloneWithDiagnosticIfListEmpty(letVarDeclarations, letKeyword,
                DiagnosticErrorCode.ERROR_MISSING_LET_VARIABLE_DECLARATION);

        // allow-actions flag is always false, since there will not be any actions
        // within the let-expr, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
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
    private STNode parseLetVarDeclarations(ParserRuleContext context, boolean isRhsExpr) {
        startContext(context);
        List<STNode> varDecls = new ArrayList<>();
        STToken nextToken = peek();

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
        STNode annot = parseOptionalAnnotations();
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
        while (true) {
            STToken nextToken = peek();
            if (nextToken.kind == SyntaxKind.EOF_TOKEN || nextToken.kind == SyntaxKind.CLOSE_BRACE_TOKEN) {
                break;
            } else {
                nextToken = consume();
                expr = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(expr, nextToken,
                        DiagnosticErrorCode.ERROR_INVALID_TOKEN, nextToken.text());
            }
        }

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
    private STNode parseTableTypeDescriptor() {
        STNode tableKeywordToken = parseTableKeyword();
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
                recover(peek(), ParserRuleContext.KEY_CONSTRAINTS_RHS, keyKeywordToken);
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
     * <code>function-type-descriptor := [isolated] function function-signature</code>
     *
     * @return Function type descriptor node
     */
    private STNode parseFunctionTypeDesc() {
        startContext(ParserRuleContext.FUNC_TYPE_DESC);
        List<STNode> qualifiers = new ArrayList<>();
        STNode qualifierList = parseFunctionQualifiers(ParserRuleContext.FUNC_TYPE_DESC_START, qualifiers);
        STNode functionKeyword = parseFunctionKeyword();
        STNode signature = parseFuncSignature(true);
        endContext();
        return STNodeFactory.createFunctionTypeDescriptorNode(qualifierList, functionKeyword, signature);
    }

    /**
     * Parse explicit anonymous function expression.
     * <p>
     * <code>explicit-anonymous-function-expr := [annots] [isolated] function function-signature anon-func-body</code>
     *
     * @param annots    Annotations.
     * @param isRhsExpr Is expression in rhs context
     * @return Anonymous function expression node
     */
    private STNode parseExplicitFunctionExpression(STNode annots, boolean isRhsExpr) {
        startContext(ParserRuleContext.ANON_FUNC_EXPRESSION);
        List<STNode> qualifiers = new ArrayList<>();
        STNode qualifierList = parseFunctionQualifiers(ParserRuleContext.ANON_FUNC_EXPRESSION_START, qualifiers);
        STNode funcKeyword = parseFunctionKeyword();
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
                recover(peek(), ParserRuleContext.ANON_FUNC_BODY, isRhsExpr);
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
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);

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
            default:
                STToken syntheticParam = STNodeFactory.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
                syntheticParam = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(syntheticParam, params,
                        DiagnosticErrorCode.ERROR_INVALID_PARAM_LIST_IN_INFER_ANONYMOUS_FUNCTION_EXPR);
                params = STNodeFactory.createSimpleNameReferenceNode(syntheticParam);
        }

        STNode rightDoubleArrow = parseDoubleRightArrow();
        // start parsing the expr by giving higher-precedence to parse the right side arguments for right associative
        // operators. That is done by lowering the current precedence.
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
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
        STNode typeDesc = parseTypeDescriptorWithoutContext(ParserRuleContext.TYPE_DESC_IN_TUPLE, false);

        return parseTupleTypeMembers(typeDesc, typeDescList);
    }

    private STNode parseTupleTypeMembers(STNode typeDesc, List<STNode> typeDescList) {
        STToken nextToken;
        // Parse the remaining type descs
        nextToken = peek();
        STNode tupleMemberRhs;
        while (!isEndOfTypeList(nextToken.kind)) {
            tupleMemberRhs = parseTupleMemberRhs();
            if (tupleMemberRhs == null) {
                break;
            }
            if (tupleMemberRhs.kind == SyntaxKind.ELLIPSIS_TOKEN) {
                typeDesc = STNodeFactory.createRestDescriptorNode(typeDesc, tupleMemberRhs);
                break;
            }
            typeDescList.add(typeDesc);
            typeDescList.add(tupleMemberRhs);
            typeDesc = parseTypeDescriptorWithoutContext(ParserRuleContext.TYPE_DESC_IN_TUPLE, false);
            nextToken = peek();
        }

        typeDescList.add(typeDesc);
        return STNodeFactory.createNodeList(typeDescList);
    }

    private STNode parseTupleMemberRhs() {
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACKET_TOKEN:
                return null;
            case ELLIPSIS_TOKEN:
                return parseEllipsis();
            default:
                recover(peek(), ParserRuleContext.TYPE_DESC_IN_TUPLE_RHS);
                return parseTupleMemberRhs();
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
     * query-construct-type := table key-specifier | stream
     * </code>
     *
     * @return Parsed node
     */
    private STNode parseTableConstructorOrQuery(boolean isRhsExpr) {
        startContext(ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_EXPRESSION);
        STNode tableOrQueryExpr = parseTableConstructorOrQueryInternal(isRhsExpr);
        endContext();
        return tableOrQueryExpr;
    }

    private STNode parseTableConstructorOrQueryInternal(boolean isRhsExpr) {
        STNode queryConstructType;
        switch (peek().kind) {
            case FROM_KEYWORD:
                queryConstructType = STNodeFactory.createEmptyNode();
                return parseQueryExprRhs(queryConstructType, isRhsExpr);
            case STREAM_KEYWORD:
                queryConstructType = parseQueryConstructType(parseStreamKeyword(), null);
                return parseQueryExprRhs(queryConstructType, isRhsExpr);
            case TABLE_KEYWORD:
                STNode tableKeyword = parseTableKeyword();
                return parseTableConstructorOrQuery(tableKeyword, isRhsExpr);
            default:
                recover(peek(), ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_START, isRhsExpr);
                return parseTableConstructorOrQueryInternal(isRhsExpr);
        }

    }

    private STNode parseTableConstructorOrQuery(STNode tableKeyword, boolean isRhsExpr) {
        STNode keySpecifier;
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACKET_TOKEN:
                keySpecifier = STNodeFactory.createEmptyNode();
                return parseTableConstructorExprRhs(tableKeyword, keySpecifier);
            case KEY_KEYWORD:
                keySpecifier = parseKeySpecifier();
                return parseTableConstructorOrQueryRhs(tableKeyword, keySpecifier, isRhsExpr);
            case IDENTIFIER_TOKEN:
                if (isKeyKeyword(nextToken)) {
                    keySpecifier = parseKeySpecifier();
                    return parseTableConstructorOrQueryRhs(tableKeyword, keySpecifier, isRhsExpr);
                }
                break;
            default:
                break;
        }

        recover(peek(), ParserRuleContext.TABLE_KEYWORD_RHS, tableKeyword, isRhsExpr);
        return parseTableConstructorOrQuery(tableKeyword, isRhsExpr);
    }

    private STNode parseTableConstructorOrQueryRhs(STNode tableKeyword, STNode keySpecifier, boolean isRhsExpr) {
        switch (peek().kind) {
            case FROM_KEYWORD:
                return parseQueryExprRhs(parseQueryConstructType(tableKeyword, keySpecifier), isRhsExpr);
            case OPEN_BRACKET_TOKEN:
                return parseTableConstructorExprRhs(tableKeyword, keySpecifier);
            default:
                recover(peek(), ParserRuleContext.TABLE_CONSTRUCTOR_OR_QUERY_RHS, tableKeyword, keySpecifier,
                        isRhsExpr);
                return parseTableConstructorOrQueryRhs(tableKeyword, keySpecifier, isRhsExpr);
        }
    }

    /**
     * Parse query construct type.
     * <p>
     * <code>query-construct-type := table key-specifier | stream</code>
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

            // If there are more clauses after select clause they are add as invalid nodes to the select clause
            if (selectClause != null) {
                selectClause = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(selectClause, intermediateClause,
                        DiagnosticErrorCode.ERROR_MORE_CLAUSES_AFTER_SELECT_CLAUSE);
                continue;
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
            return parseQueryAction(queryConstructType, queryPipeline, selectClause, isRhsExpr);
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
        return STNodeFactory.createQueryExpressionNode(queryConstructType, queryPipeline, selectClause,
                onConflictClause);
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
    private STNode parseIntermediateClause(boolean isRhsExpr) {
        switch (peek().kind) {
            case FROM_KEYWORD:
                return parseFromClause(isRhsExpr);
            case WHERE_KEYWORD:
                return parseWhereClause(isRhsExpr);
            case LET_KEYWORD:
                return parseLetClause(isRhsExpr);
            case SELECT_KEYWORD:
                return parseSelectClause(isRhsExpr);
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
                recover(peek(), ParserRuleContext.QUERY_PIPELINE_RHS, isRhsExpr);
                return parseIntermediateClause(isRhsExpr);
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
    private STNode parseLetClause(boolean isRhsExpr) {
        STNode letKeyword = parseLetKeyword();
        STNode letVarDeclarations = parseLetVarDeclarations(ParserRuleContext.LET_CLAUSE_LET_VAR_DECL, isRhsExpr);

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
    private STNode parseSelectClause(boolean isRhsExpr) {
        startContext(ParserRuleContext.SELECT_CLAUSE);
        STNode selectKeyword = parseSelectKeyword();

        // allow-actions flag is always false, since there will not be any actions
        // within the select-clause, due to the precedence.
        STNode expression = parseExpression(OperatorPrecedence.QUERY, isRhsExpr, false);
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
                STNode openParenToken = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.OPEN_PAREN_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_OPEN_PAREN_TOKEN);
                STNode arguments = STNodeFactory.createEmptyNodeList();
                STNode closeParenToken = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.CLOSE_PAREN_TOKEN,
                        DiagnosticErrorCode.ERROR_MISSING_CLOSE_PAREN_TOKEN);
                expr = STNodeFactory.createFunctionCallExpressionNode(expr, openParenToken, arguments, closeParenToken);
                break;
            default:
                startKeyword = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(startKeyword, expr,
                        DiagnosticErrorCode.ERROR_INVALID_EXPRESSION_IN_START_ACTION);
                STNode funcName = SyntaxErrors.createMissingToken(SyntaxKind.IDENTIFIER_TOKEN);
                funcName = STNodeFactory.createSimpleNameReferenceNode(funcName);
                openParenToken = SyntaxErrors.createMissingToken(SyntaxKind.OPEN_PAREN_TOKEN);
                arguments = STNodeFactory.createEmptyNodeList();
                closeParenToken = SyntaxErrors.createMissingToken(SyntaxKind.CLOSE_PAREN_TOKEN);
                expr = STNodeFactory.createFunctionCallExpressionNode(funcName, openParenToken, arguments,
                        closeParenToken);
                break;
        }

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
    private STNode parseIntersectionTypeDescriptor(STNode leftTypeDesc, ParserRuleContext context,
                                                   boolean isTypedBindingPattern) {
        // we come here only after seeing & token hence consume.
        STNode bitwiseAndToken = consume();
        STNode rightTypeDesc = parseTypeDescriptor(context, isTypedBindingPattern, false);
        return createIntersectionTypeDesc(leftTypeDesc, bitwiseAndToken, rightTypeDesc);
    }

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

    private boolean isSingletonTypeDescStart(SyntaxKind tokenKind, boolean inTypeDescCtx) {
        STToken nextNextToken = getNextNextToken(tokenKind);
        switch (tokenKind) {
            case STRING_LITERAL_TOKEN:
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
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
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
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
                return nextNextTokenKind == SyntaxKind.SEMICOLON_TOKEN || nextNextTokenKind == SyntaxKind.COMMA_TOKEN ||
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
                return true;
            case PLUS_TOKEN:
            case MINUS_TOKEN:
                return isValidExpressionStart(peek(nextTokenIndex).kind, nextTokenIndex);
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
            case DEFAULT_KEYWORD:
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
            case DEFAULT_KEYWORD:
                STNode defaultKeyword = parseDefaultKeyword();
                return STNodeFactory.createSimpleNameReferenceNode(defaultKeyword);
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
        return STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, peerWorker);
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
     *
     * @return Parsed node
     */
    private STNode parseSignedRightShiftToken() {
        STNode openGTToken = consume();
        STToken endLGToken = consume();
        STNode doubleGTToken = STNodeFactory.createToken(SyntaxKind.DOUBLE_GT_TOKEN, openGTToken.leadingMinutiae(),
                endLGToken.trailingMinutiae());

        if (hasTrailingMinutiae(openGTToken)) {
            doubleGTToken = SyntaxErrors.addDiagnostic(doubleGTToken,
                    DiagnosticErrorCode.ERROR_NO_WHITESPACES_ALLOWED_IN_RIGHT_SHIFT_OP);
        }
        return doubleGTToken;
    }

    /**
     * Parse unsigned right shift token (>>>).
     *
     * @return Parsed node
     */
    private STNode parseUnsignedRightShiftToken() {
        STNode openGTToken = consume();
        STNode middleGTToken = consume();
        STNode endLGToken = consume();
        STNode unsignedRightShiftToken = STNodeFactory.createToken(SyntaxKind.TRIPPLE_GT_TOKEN,
                openGTToken.leadingMinutiae(), endLGToken.trailingMinutiae());

        boolean validOpenGTToken = !hasTrailingMinutiae(openGTToken);
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
    private STNode parseQueryAction(STNode queryConstructType, STNode queryPipeline, STNode selectClause,
                                    boolean isRhsExpr) {
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
     * @param lhsExpr Preceding expression of the question mark
     * @return Parsed node
     */
    private STNode parseConditionalExpression(STNode lhsExpr) {
        startContext(ParserRuleContext.CONDITIONAL_EXPRESSION);
        STNode questionMark = parseQuestionMark();
        // start parsing middle-expr, by giving higher-precedence to the middle-expr, over currently
        // parsing conditional expr. That is done by lowering the current precedence.
        STNode middleExpr = parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, true, false, true);

        // Special case "a ? b : c", since "b:c" matches to var-ref due to expr-precedence.
        STNode nextToken = peek();
        STNode endExpr;
        STNode colon;
        if (nextToken.kind != SyntaxKind.COLON_TOKEN && middleExpr.kind == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            STQualifiedNameReferenceNode qualifiedNameRef = (STQualifiedNameReferenceNode) middleExpr;
            middleExpr = STNodeFactory.createSimpleNameReferenceNode(qualifiedNameRef.modulePrefix);
            colon = qualifiedNameRef.colon;
            endContext();
            endExpr = STNodeFactory.createSimpleNameReferenceNode(qualifiedNameRef.identifier);
        } else {
            colon = parseColon();
            endContext();
            // start parsing end-expr, by giving higher-precedence to the end-expr, over currently
            // parsing conditional expr. That is done by lowering the current precedence.
            endExpr = parseExpression(OperatorPrecedence.ANON_FUNC_OR_LET, true, false);
        }

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
        openBraceToken = cloneWithDiagnosticIfListEmpty(enumMemberList, openBraceToken,
                DiagnosticErrorCode.ERROR_MISSING_ENUM_MEMBER);
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
                recover(peek(), ParserRuleContext.ENUM_MEMBER_RHS, metadata, identifierNode);
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

    /**
     * Parse transaction statement.
     * <p>
     * <code>transaction-stmt := `transaction` block-stmt [on-fail-clause]</code>
     *
     * @return Transaction statement node
     */
    private STNode parseTransactionStatement() {
        startContext(ParserRuleContext.TRANSACTION_STMT);
        STNode transactionKeyword = parseTransactionKeyword();
        STNode blockStmt = parseBlockNode();
        endContext();
        STNode onFailClause = parseOptionalOnFailClause();
        return STNodeFactory.createTransactionStatementNode(transactionKeyword, blockStmt, onFailClause);
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
            recover(token, ParserRuleContext.TRANSACTION_KEYWORD);
            return parseTransactionKeyword();
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
                recover(peek(), ParserRuleContext.RETRY_KEYWORD_RHS, retryKeyword);
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
                recover(peek(), ParserRuleContext.RETRY_TYPE_PARAM_RHS, retryKeyword, typeParam);
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
                return parseTransactionStatement();
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

                // Statements starts other than var-decl
            case IF_KEYWORD:
            case WHILE_KEYWORD:
            case DO_KEYWORD:
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
     * on-fail-clause := on fail typed-binding-pattern statement-block
     * </code>
     *
     * @return On fail clause node
     */
    private STNode parseOnFailClause() {
        startContext(ParserRuleContext.ON_FAIL_CLAUSE);
        STNode onKeyword = parseOnKeyword();
        STNode failKeyword = parseFailKeyword();
        STNode typeDescriptor = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true, false);
        STNode identifier = parseIdentifier(ParserRuleContext.VARIABLE_REF);
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
                return true;
            default:
                return false;
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
        switch (peek().kind) {
            case IF_KEYWORD:
                STNode ifKeyword = parseIfKeyword();
                STNode expr = parseExpression(DEFAULT_OP_PRECEDENCE, true, false, true, false);
                return STNodeFactory.createMatchGuardNode(ifKeyword, expr);
            case RIGHT_DOUBLE_ARROW_TOKEN:
                return STNodeFactory.createEmptyNode();
            default:
                recover(peek(), ParserRuleContext.OPTIONAL_MATCH_GUARD);
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

            STNode seperator = parseMatchPatternEnd();
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
            case RIGHT_ARROW_TOKEN:
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
        switch (peek().kind) {
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
            case IDENTIFIER_TOKEN:
                // If it is an identifier it can be error match pattern with missing error keyword or const pattern
                STNode typeRefOrConstExpr = parseQualifiedIdentifier(ParserRuleContext.MATCH_PATTERN);
                return parseErrorMatchPatternOrConsPattern(typeRefOrConstExpr);
            case VAR_KEYWORD:
                return parseVarTypedBindingPattern();
            case OPEN_BRACKET_TOKEN:
                return parseListMatchPattern();
            case OPEN_BRACE_TOKEN:
                return parseMappingMatchPattern();
            case ERROR_KEYWORD:
                return parseErrorMatchPattern();
            default:
                recover(peek(), ParserRuleContext.MATCH_PATTERN_START);
                return parseMatchPattern();
        }
    }

    private STNode parseMatchPatternEnd() {
        switch (peek().kind) {
            case PIPE_TOKEN:
                return parsePipeToken();
            case IF_KEYWORD:
            case RIGHT_DOUBLE_ARROW_TOKEN:
                // Returning null indicates the end of the match-patterns list
                return null;
            default:
                recover(peek(), ParserRuleContext.MATCH_PATTERN_RHS);
                return parseMatchPatternEnd();
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
        STNode restMatchPattern = null;
        STNode listMatchPatternMemberRhs = null;
        boolean isEndOfFields = false;

        while (!isEndOfListMatchPattern()) {
            STNode listMatchPatternMember = parseListMatchPatternMember();
            if (listMatchPatternMember.kind == SyntaxKind.REST_MATCH_PATTERN) {
                restMatchPattern = listMatchPatternMember;
                listMatchPatternMemberRhs = parseListMatchPatternMemberRhs();
                isEndOfFields = true;
                break;
            }
            matchPatternList.add(listMatchPatternMember);
            listMatchPatternMemberRhs = parseListMatchPatternMemberRhs();

            if (listMatchPatternMemberRhs != null) {
                matchPatternList.add(listMatchPatternMemberRhs);
            } else {
                break;
            }
        }

        // Following loop will only run if there are more fields after the rest match pattern.
        // Try to parse them and mark as invalid.
        while (isEndOfFields && listMatchPatternMemberRhs != null) {
            STNode invalidField = parseListMatchPatternMember();
            restMatchPattern =
                    SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(restMatchPattern, listMatchPatternMemberRhs);
            restMatchPattern = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(restMatchPattern, invalidField);
            restMatchPattern = SyntaxErrors.addDiagnostic(restMatchPattern,
                    DiagnosticErrorCode.ERROR_MORE_MATCH_PATTERNS_AFTER_REST_MATCH_PATTERN);
            listMatchPatternMemberRhs = parseListMatchPatternMemberRhs();
        }

        if (restMatchPattern == null) {
            restMatchPattern = STNodeFactory.createEmptyNode();
        }

        STNode matchPatternListNode = STNodeFactory.createNodeList(matchPatternList);
        STNode closeBracketToken = parseCloseBracket();
        endContext();

        return STNodeFactory.createListMatchPatternNode(openBracketToken, matchPatternListNode, restMatchPattern,
                closeBracketToken);
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
        List<STNode> fieldMatchPatternList = new ArrayList<>();
        STNode restMatchPattern = null;
        boolean isEndOfFields = false;

        while (!isEndOfMappingMatchPattern()) {
            STNode fieldMatchPatternMember = parseFieldMatchPatternMember();
            if (fieldMatchPatternMember.kind == SyntaxKind.REST_MATCH_PATTERN) {
                restMatchPattern = fieldMatchPatternMember;
                isEndOfFields = true;
                break;
            }
            fieldMatchPatternList.add(fieldMatchPatternMember);
            STNode fieldMatchPatternRhs = parseFieldMatchPatternRhs();

            if (fieldMatchPatternRhs != null) {
                fieldMatchPatternList.add(fieldMatchPatternRhs);
            } else {
                break;
            }
        }

        // Following loop will only run if there are more fields after the rest match pattern.
        // Try to parse them and mark as invalid.
        STNode fieldMatchPatternRhs = parseFieldMatchPatternRhs();
        while (isEndOfFields && fieldMatchPatternRhs != null) {
            STNode invalidField = parseFieldMatchPatternMember();
            restMatchPattern =
                    SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(restMatchPattern, fieldMatchPatternRhs);
            restMatchPattern = SyntaxErrors.cloneWithTrailingInvalidNodeMinutiae(restMatchPattern, invalidField);
            restMatchPattern = SyntaxErrors.addDiagnostic(restMatchPattern,
                    DiagnosticErrorCode.ERROR_MORE_FIELD_MATCH_PATTERNS_AFTER_REST_FIELD);
            fieldMatchPatternRhs = parseFieldMatchPatternRhs();
        }

        if (restMatchPattern == null) {
            restMatchPattern = STNodeFactory.createEmptyNode();
        }

        STNode fieldMatchPatterns = STNodeFactory.createNodeList(fieldMatchPatternList);
        STNode closeBraceToken = parseCloseBrace();
        endContext();

        return STNodeFactory.createMappingMatchPatternNode(openBraceToken, fieldMatchPatterns, restMatchPattern,
                closeBraceToken);
    }

    private STNode parseFieldMatchPatternMember() {
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                return parseFieldMatchPattern();
            case ELLIPSIS_TOKEN:
                return parseRestMatchPattern();
            default:
                recover(peek(), ParserRuleContext.FIELD_MATCH_PATTERN_MEMBER);
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
                recover(peek(), ParserRuleContext.ERROR_MATCH_PATTERN_OR_CONST_PATTERN, typeRefOrConstExpr);
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
            case IDENTIFIER_TOKEN:
                typeRef = parseTypeReference();
                break;
            case OPEN_PAREN_TOKEN:
                typeRef = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.ERROR_MATCH_PATTERN_ERROR_KEYWORD_RHS);
                return parseErrorMatchPattern(errorKeyword);
        }
        return parseErrorMatchPattern(errorKeyword, typeRef);
    }

    private STNode parseErrorMatchPattern(STNode errorKeyword, STNode typeRef) {
        STNode openParenthesisToken = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
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
        switch (nextToken.kind) {
            case ELLIPSIS_TOKEN:
                return parseRestMatchPattern();
            case IDENTIFIER_TOKEN:
                // Identifier can means two things: either its a named-arg, or its simple match pattern.
                return parseNamedOrSimpleMatchPattern();
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
                STNode varKeyword = consume();
                STNode variableName = parseVariableName();
                return STNodeFactory.createTypedBindingPatternNode(varKeyword, variableName);
            default:
                recover(nextToken, context);
                return parseErrorArgListMatchPattern(context);
        }
    }

    private STNode parseNamedOrSimpleMatchPattern() {
        STNode identifier = consume(); // We only approach here by seeing identifier.
        STToken secondToken = peek();
        switch (secondToken.kind) {
            case EQUAL_TOKEN:
                return parseNamedArgMatchPattern(identifier);
            case COMMA_TOKEN:
            case CLOSE_PAREN_TOKEN:
            default:
                return identifier;
        }
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
                    return DiagnosticErrorCode.ERROR_ARG_FOLLOWED_BY_REST_ARG;
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
        CharReader charReader = CharReader.from(documentationStringToken.text());
        DocumentationLexer documentationLexer = new DocumentationLexer(charReader, leadingTriviaList);
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
     * @return Statement node
     */
    private STNode parseStmtStartsWithTypeOrExpr(STNode annots) {
        startContext(ParserRuleContext.AMBIGUOUS_STMT);
        STNode typeOrExpr = parseTypedBindingPatternOrExpr(true);
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
        STNode typeOrExpr;
        switch (nextToken.kind) {
            case OPEN_PAREN_TOKEN:
                return parseTypedBPOrExprStartsWithOpenParenthesis();
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
                return parseAnonFuncExprOrTypedBPWithFuncType();
            case IDENTIFIER_TOKEN:
                typeOrExpr = parseQualifiedIdentifier(ParserRuleContext.TYPE_NAME_OR_VAR_NAME);
                return parseTypedBindingPatternOrExprRhs(typeOrExpr, allowAssignment);
            case OPEN_BRACKET_TOKEN:
                typeOrExpr = parseTupleTypeDescOrExprStartsWithOpenBracket();
                return parseTypedBindingPatternOrExprRhs(typeOrExpr, allowAssignment);
            case ISOLATED_KEYWORD:
                if (isFuncDefOrFuncTypeStart()) {
                    return parseAnonFuncExprOrTypedBPWithFuncType();
                } else {
                    return parseTypedBindingPattern(ParserRuleContext.VAR_DECL_STMT);
                }
            // Can be a singleton type or expression.
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                STNode basicLiteral = parseBasicLiteral();
                return parseTypedBindingPatternOrExprRhs(basicLiteral, allowAssignment);
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    return parseActionOrExpressionInLhs(STNodeFactory.createEmptyNodeList());
                }

                return parseTypedBindingPattern(ParserRuleContext.VAR_DECL_STMT);
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
                STToken nextNextToken = peek(2);
                if (nextNextToken.kind == SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode pipe = parsePipeToken();
                STNode rhsTypedBPOrExpr = parseTypedBindingPatternOrExpr(allowAssignment);
                if (rhsTypedBPOrExpr.kind == SyntaxKind.TYPED_BINDING_PATTERN) {
                    STTypedBindingPatternNode typedBP = (STTypedBindingPatternNode) rhsTypedBPOrExpr;
                    typeOrExpr = getTypeDescFromExpr(typeOrExpr);
                    STNode newTypeDesc = createUnionTypeDesc(typeOrExpr, pipe, typedBP.typeDescriptor);
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
                    STNode newTypeDesc = createIntersectionTypeDesc(typeOrExpr, ampersand, typedBP.typeDescriptor);
                    return STNodeFactory.createTypedBindingPatternNode(newTypeDesc, typedBP.bindingPattern);
                }

                return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr, ampersand,
                        rhsTypedBPOrExpr);
            case SEMICOLON_TOKEN:
                if (isDefiniteExpr(typeOrExpr.kind)) {
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
                if (isCompoundBinaryOperator(nextToken.kind)) {
                    return typeOrExpr;
                }

                // If the next token is part of a valid expression, then still parse it
                // as a statement that starts with an expression.
                if (isValidExprRhsStart(nextToken.kind, typeOrExpr.kind)) {
                    return typeOrExpr;
                }

                STToken token = peek();
                recover(token, ParserRuleContext.BINDING_PATTERN_OR_EXPR_RHS, typeOrExpr, allowAssignment);
                return parseTypedBindingPatternOrExprRhs(typeOrExpr, allowAssignment);
        }
    }

    private STNode parseTypeBindingPatternStartsWithAmbiguousNode(STNode typeDesc) {
        // switchContext(ParserRuleContext.VAR_DECL_STMT);

        // We haven't parsed the type-desc as a type-desc (parsed as an identifier/expr).
        // Therefore handle the context manually here.
        startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
        typeDesc = parseComplexTypeDescriptor(typeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, false);
        endContext();
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
        return kind.compareTo(SyntaxKind.RECORD_TYPE_DESC) >= 0 && kind.compareTo(SyntaxKind.SINGLETON_TYPE_DESC) <= 0;
    }

    private boolean isDefiniteExpr(SyntaxKind kind) {
        if (kind == SyntaxKind.QUALIFIED_NAME_REFERENCE || kind == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return false;
        }

        return kind.compareTo(SyntaxKind.BINARY_EXPRESSION) >= 0 &&
                kind.compareTo(SyntaxKind.XML_ATOMIC_NAME_PATTERN) <= 0;
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
            return parseBracedExprOrAnonFuncParamRhs(openParen, typeOrExpr, false);
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
                typeOrExpr = parseTypedDescOrExprStartsWithOpenParenthesis();
                break;
            case FUNCTION_KEYWORD:
                typeOrExpr = parseAnonFuncExprOrFuncTypeDesc();
                break;
            case IDENTIFIER_TOKEN:
                typeOrExpr = parseQualifiedIdentifier(ParserRuleContext.TYPE_NAME_OR_VAR_NAME);
                return parseTypeDescOrExprRhs(typeOrExpr);
            case OPEN_BRACKET_TOKEN:
                typeOrExpr = parseTupleTypeDescOrExprStartsWithOpenBracket();
                break;
            case ISOLATED_KEYWORD:
                if (isFuncDefOrFuncTypeStart()) {
                    typeOrExpr = parseAnonFuncExprOrFuncTypeDesc();
                    break;
                } else {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                }
            // Can be a singleton type or expression.
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case STRING_LITERAL_TOKEN:
            case NULL_KEYWORD:
            case TRUE_KEYWORD:
            case FALSE_KEYWORD:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
                STNode basicLiteral = parseBasicLiteral();
                return parseTypeDescOrExprRhs(basicLiteral);
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    return parseActionOrExpressionInLhs(STNodeFactory.createEmptyNodeList());
                }
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
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
                        kind.compareTo(SyntaxKind.XML_ATOMIC_NAME_PATTERN) <= 0;
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
        List<STNode> qualifiers = new ArrayList<>();
        STNode qualifierList = parseFunctionQualifiers(ParserRuleContext.FUNC_TYPE_DESC_START, qualifiers);
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
                STNode anonFunc = STNodeFactory.createExplicitAnonymousFunctionExpressionNode(annots, qualifierList,
                        functionKeyword, funcSignature, funcBody);
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, anonFunc, false, true);
            case IDENTIFIER_TOKEN:
            default:
                switchContext(ParserRuleContext.VAR_DECL_STMT);
                STNode funcTypeDesc = STNodeFactory.createFunctionTypeDescriptorNode(qualifierList, functionKeyword,
                        funcSignature);
                return parseComplexTypeDescriptor(funcTypeDesc, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN,
                        true);
        }
    }

    private STNode parseTypeDescOrExprRhs(STNode typeOrExpr) {
        STToken nextToken = peek();
        STNode typeDesc;
        switch (nextToken.kind) {
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
                rhsTypeDescOrExpr = getTypeDescFromExpr(rhsTypeDescOrExpr);
                return createUnionTypeDesc(typeDesc, pipe, rhsTypeDescOrExpr);
            case BITWISE_AND_TOKEN:
                nextNextToken = peek(2);
                if (nextNextToken.kind == SyntaxKind.EQUAL_TOKEN) {
                    return typeOrExpr;
                }

                STNode ampersand = parseBinaryOperator();
                rhsTypeDescOrExpr = parseTypeDescOrExpr();
                if (isExpression(rhsTypeDescOrExpr.kind)) {
                    return STNodeFactory.createBinaryExpressionNode(SyntaxKind.BINARY_EXPRESSION, typeOrExpr, ampersand,
                            rhsTypeDescOrExpr);
                }

                typeDesc = getTypeDescFromExpr(typeOrExpr);
                rhsTypeDescOrExpr = getTypeDescFromExpr(rhsTypeDescOrExpr);
                return createIntersectionTypeDesc(typeDesc, ampersand, rhsTypeDescOrExpr);
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
            case ELLIPSIS_TOKEN:
                STNode ellipsis = parseEllipsis();
                typeOrExpr = getTypeDescFromExpr(typeOrExpr);
                return STNodeFactory.createRestDescriptorNode(typeOrExpr, ellipsis);
            default:
                // If its a binary operator then this can be a compound assignment statement
                if (isCompoundBinaryOperator(nextToken.kind)) {
                    return typeOrExpr;
                }

                // If the next token is part of a valid expression, then still parse it
                // as a statement that starts with an expression.
                if (isValidExprRhsStart(nextToken.kind, typeOrExpr.kind)) {
                    return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, typeOrExpr, false, false, false, false);
                }

                recover(peek(), ParserRuleContext.TYPE_DESC_OR_EXPR_RHS, typeOrExpr);
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

    private STNode parseTupleTypeDescOrExprStartsWithOpenBracket() {
        startContext(ParserRuleContext.BRACKETED_LIST);
        STNode openBracket = parseOpenBracket();
        List<STNode> members = new ArrayList<>();
        STNode memberEnd;
        while (!isEndOfListConstructor(peek().kind)) {
            STNode expr = parseTypeDescOrExpr();
            // Tuple type desc can contain rest descriptor which is not a regular type desc,
            // hence handle it here.
            if (peek().kind == SyntaxKind.ELLIPSIS_TOKEN && isDefiniteTypeDesc(expr.kind)) {
                STNode ellipsis = consume();
                expr = STNodeFactory.createRestDescriptorNode(expr, ellipsis);
            }
            members.add(expr);

            memberEnd = parseBracketedListMemberEnd();
            if (memberEnd == null) {
                break;
            }
            members.add(memberEnd);
        }

        STNode memberNodes = STNodeFactory.createNodeList(members);
        STNode closeBracket = parseCloseBracket();
        endContext();
        return STNodeFactory.createTupleTypeDescriptorNode(openBracket, memberNodes, closeBracket);
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
            STNode identifier = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.IDENTIFIER_TOKEN,
                    ParserRuleContext.BINDING_PATTERN_STARTING_IDENTIFIER);
            identifier = SyntaxErrors.cloneWithLeadingInvalidNodeMinutiae(identifier, argNameOrBindingPattern);
            return createCaptureOrWildcardBP(identifier);
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
            STNode restBindingPattern = STNodeFactory.createEmptyNode();
            STNode bindingPatternsNode = STNodeFactory.createNodeList(bindingPatternsList);
            return STNodeFactory.createListBindingPatternNode(openBracket, bindingPatternsNode, restBindingPattern,
                    closeBracket);
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
        STNode typeDesc = parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true, false);
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
            STNode restBindingPattern = STNodeFactory.createEmptyNode();
            endContext();
            return STNodeFactory.createMappingBindingPatternNode(openBrace, bindingPatternsNode, restBindingPattern,
                    closeBrace);
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

        // Separating out the rest-binding-pattern
        STNode restBindingPattern;
        if (prevMember.kind == SyntaxKind.REST_BINDING_PATTERN) {
            restBindingPattern = prevMember;
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
        switch (peek().kind) {
            case COMMA_TOKEN:
                return parseComma();
            case CLOSE_BRACE_TOKEN:
                return null;
            default:
                recover(peek(), ParserRuleContext.MAPPING_BINDING_PATTERN_END);
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
        switch (peek().kind) {
            case IDENTIFIER_TOKEN:
                STNode identifier = parseIdentifier(ParserRuleContext.FIELD_BINDING_PATTERN_NAME);
                STNode fieldBindingPattern = parseFieldBindingPattern(identifier);
                return fieldBindingPattern;
            default:
                recover(peek(), ParserRuleContext.FIELD_BINDING_PATTERN_NAME);
                return parseFieldBindingPattern();
        }
    }

    private STNode parseFieldBindingPattern(STNode identifier) {
        STNode simpleNameReference = STNodeFactory.createSimpleNameReferenceNode(identifier);

        if (peek().kind != SyntaxKind.COLON_TOKEN) {
            return STNodeFactory.createFieldBindingPatternVarnameNode(simpleNameReference);
        }

        STNode colon = parseColon();
        STNode bindingPattern = parseBindingPattern();
        return STNodeFactory.createFieldBindingPatternFullNode(simpleNameReference, colon, bindingPattern);
    }

    private boolean isEndOfMappingBindingPattern(SyntaxKind nextTokenKind) {
        return nextTokenKind == SyntaxKind.CLOSE_BRACE_TOKEN;
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
        return parseVariableDecl(getAnnotations(annots), finalKeyword, false);
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
            case IDENTIFIER_TOKEN:
                typeRef = parseTypeReference();
                break;
            case OPEN_PAREN_TOKEN:
                typeRef = STNodeFactory.createEmptyNode();
                break;
            default:
                recover(peek(), ParserRuleContext.ERROR_BINDING_PATTERN_ERROR_KEYWORD_RHS);
                return parseErrorBindingPattern(errorKeyword);
        }
        return parseErrorBindingPattern(errorKeyword, typeRef);
    }

    private STNode parseErrorBindingPattern(STNode errorKeyword, STNode typeRef) {
        STNode openParenthesis = parseOpenParenthesis(ParserRuleContext.OPEN_PARENTHESIS);
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
        STNode firstArg = parseErrorArgListBindingPattern(ParserRuleContext.ERROR_ARG_LIST_BINDING_PATTERN_START);
        if (firstArg.kind == SyntaxKind.CAPTURE_BINDING_PATTERN ||
                firstArg.kind == SyntaxKind.WILDCARD_BINDING_PATTERN) {

            argListBindingPatterns.add(firstArg);
            STNode argEnd = parseErrorArgsBindingPatternEnd(ParserRuleContext.ERROR_MESSAGE_BINDING_PATTERN_END);
            if (argEnd != null) {
                // null marks the end of args
                STNode secondArg = parseErrorArgListBindingPattern(ParserRuleContext.ERROR_MESSAGE_BINDING_PATTERN_RHS);
                if (isValidSecondArgBindingPattern(secondArg.kind)) {
                    argListBindingPatterns.add(argEnd);
                    argListBindingPatterns.add(secondArg);
                } else {
                    updateLastNodeInListWithInvalidNode(argListBindingPatterns, argEnd, null);
                    updateLastNodeInListWithInvalidNode(argListBindingPatterns, secondArg,
                            DiagnosticErrorCode.ERROR_BINDING_PATTERN_NOT_ALLOWED);
                }
            }
        } else {
            if (firstArg.kind != SyntaxKind.NAMED_ARG_BINDING_PATTERN &&
                    firstArg.kind != SyntaxKind.REST_BINDING_PATTERN) {
                addInvalidNodeToNextToken(firstArg, DiagnosticErrorCode.ERROR_BINDING_PATTERN_NOT_ALLOWED);
            } else {
                argListBindingPatterns.add(firstArg);
            }
        }
        parseErrorFieldBindingPatterns(argListBindingPatterns);
        return STNodeFactory.createNodeList(argListBindingPatterns);
    }

    private boolean isValidSecondArgBindingPattern(SyntaxKind syntaxKind) {
        switch (syntaxKind) {
            case CAPTURE_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
            case ERROR_BINDING_PATTERN:
            case NAMED_ARG_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
                return true;
            default:
                return false;
        }
    }

    private void parseErrorFieldBindingPatterns(List<STNode> argListBindingPatterns) {
        SyntaxKind lastValidArgKind = SyntaxKind.NAMED_ARG_BINDING_PATTERN;
        while (!isEndOfErrorFieldBindingPatterns()) {
            STNode argEnd = parseErrorArgsBindingPatternEnd(ParserRuleContext.ERROR_FIELD_BINDING_PATTERN_END);
            if (argEnd == null) {
                // null marks the end of args
                break;
            }
            STNode currentArg = parseErrorArgListBindingPattern(ParserRuleContext.ERROR_FIELD_BINDING_PATTERN);
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

    private STNode parseErrorArgListBindingPattern(ParserRuleContext context) {
        switch (peek().kind) {
            case ELLIPSIS_TOKEN:
                return parseRestBindingPattern();
            case IDENTIFIER_TOKEN:
                // Identifier can means two things: either its a named-arg, or its simple binding pattern.
                return parseNamedOrSimpleArgBindingPattern();
            case OPEN_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case ERROR_KEYWORD:
                return parseBindingPattern();
            default:
                recover(peek(), context);
                return parseErrorArgListBindingPattern(context);
        }
    }

    private STNode parseNamedOrSimpleArgBindingPattern() {
        STNode argNameOrSimpleBindingPattern = consume(); // We only approach here by seeing identifier.
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
                    return DiagnosticErrorCode.ERROR_ARG_FOLLOWED_BY_REST_ARG;
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
        switch (peek().kind) {
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
                recover(peek(), ParserRuleContext.TYPED_BINDING_PATTERN_TYPE_RHS, typeDesc, context, isRoot);
                return parseTypedBindingPatternTypeRhs(typeDesc, context, isRoot);
        }
    }

    /**
     * Parse typed-binding pattern with list, array-type-desc, or member-access-expr.
     *
     * @param typeDescOrExpr        Type desc or the expression at the start
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
                if (!isTypedBindingPattern && isValidExpressionStart(nextToken.kind, 1)) {
                    break;
                }

                ParserRuleContext recoverContext =
                        isTypedBindingPattern ? ParserRuleContext.LIST_BINDING_MEMBER_OR_ARRAY_LENGTH
                                : ParserRuleContext.BRACKETED_LIST_MEMBER;
                recover(peek(), recoverContext, isTypedBindingPattern);
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
        typeDesc = validateForUsageOfVar(typeDesc);
        STNode closeBracket = parseCloseBracket();
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
                    arrayTypeDesc = createArrayTypeDesc(typeDesc, openBracket, member, closeBracket);
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
                if (isValidExprRhsStart(nextToken.kind, closeBracket.kind)) {
                    // We come here if T[a] is in some expression context.
                    keyExpr = getKeyExpr(member);
                    typeDescOrExpr = getExpression(typeDescOrExpr);
                    return STNodeFactory.createIndexedExpressionNode(typeDescOrExpr, openBracket, keyExpr,
                            closeBracket);
                }

                break;
        }

        recover(peek(), ParserRuleContext.BRACKETED_LIST_RHS, typeDescOrExpr, openBracket, member, closeBracket,
                isTypedBindingPattern, allowAssignment, context);
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
        STNode bindingPatterns;
        if (isEmpty(member)) {
            bindingPatterns = STNodeFactory.createEmptyNodeList();
        } else {
            STNode bindingPattern = getBindingPattern(member);
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
     * @param openBracket    Open bracket
     * @param member         Member
     * @param closeBracket   Close bracket
     * @param context        COntext in which the typed binding pattern occurs
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
            lhsTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, lhsTypeDesc);
            STTypedBindingPatternNode rhsTypedBindingPattern = (STTypedBindingPatternNode) typedBindingPatternOrExpr;
            STNode newTypeDesc;
            if (pipeOrAndToken.kind == SyntaxKind.PIPE_TOKEN) {
                newTypeDesc = createUnionTypeDesc(lhsTypeDesc, pipeOrAndToken, rhsTypedBindingPattern.typeDescriptor);
            } else {
                newTypeDesc =
                        createIntersectionTypeDesc(lhsTypeDesc, pipeOrAndToken, rhsTypedBindingPattern.typeDescriptor);
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

    private STNode getArrayTypeDesc(STNode openBracket, STNode member, STNode closeBracket, STNode lhsTypeDesc) {
        if (lhsTypeDesc.kind == SyntaxKind.UNION_TYPE_DESC) {
            STUnionTypeDescriptorNode unionTypeDesc = (STUnionTypeDescriptorNode) lhsTypeDesc;
            STNode middleTypeDesc = getArrayTypeDesc(openBracket, member, closeBracket, unionTypeDesc.rightTypeDesc);
            lhsTypeDesc = createUnionTypeDesc(unionTypeDesc.leftTypeDesc, unionTypeDesc.pipeToken, middleTypeDesc);
        } else if (lhsTypeDesc.kind == SyntaxKind.INTERSECTION_TYPE_DESC) {
            STIntersectionTypeDescriptorNode intersectionTypeDesc = (STIntersectionTypeDescriptorNode) lhsTypeDesc;
            STNode middleTypeDesc =
                    getArrayTypeDesc(openBracket, member, closeBracket, intersectionTypeDesc.rightTypeDesc);
            lhsTypeDesc = createIntersectionTypeDesc(intersectionTypeDesc.leftTypeDesc,
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
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case BRACKETED_LIST: // member is again ambiguous
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
                return SyntaxKind.NONE;
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
        STNode bracketedList = parseStatementStartBracketedList(annots, openBracket, memberList, closeBracket, isRoot,
                possibleMappingField);
        return bracketedList;
    }

    /**
     * Parse a member of a list-binding-pattern, tuple-type-desc, or
     * list-constructor-expr, when the parent is ambiguous.
     *
     * @return Parsed node
     */
    private STNode parseStatementStartBracketedListMember() {
        STToken nextToken = peek();
        switch (nextToken.kind) {
            case OPEN_BRACKET_TOKEN:
                return parseMemberBracketedList(false);
            case IDENTIFIER_TOKEN:
                STNode identifier = parseQualifiedIdentifier(ParserRuleContext.VARIABLE_REF);
                if (isWildcardBP(identifier)) {
                    STNode varName = ((STSimpleNameReferenceNode) identifier).name;
                    return getWildcardBindingPattern(varName);
                }

                if (peek().kind == SyntaxKind.ELLIPSIS_TOKEN) {
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
                if (getNextNextToken(nextToken.kind).kind == SyntaxKind.OPEN_PAREN_TOKEN) {
                    return parseErrorConstructorExpr();
                }
                if (peek(2).kind == SyntaxKind.IDENTIFIER_TOKEN) {
                    return parseErrorBindingPattern();
                }
                // error-type-desc
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case ELLIPSIS_TOKEN:
                return parseListBindingPatternMember();
            case XML_KEYWORD:
            case STRING_KEYWORD:
                if (getNextNextToken(nextToken.kind).kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseExpression(false);
                }
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
                if (getNextNextToken(nextToken.kind).kind == SyntaxKind.LT_TOKEN) {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }
                return parseExpression(false);
            case OPEN_PAREN_TOKEN:
                return parseTypeDescOrExpr();
            case FUNCTION_KEYWORD:
            case TRANSACTIONAL_KEYWORD:
                return parseAnonFuncExprOrFuncTypeDesc();
            case ISOLATED_KEYWORD:
                if (isFuncDefOrFuncTypeStart()) {
                    return parseAnonFuncExprOrFuncTypeDesc();
                } else {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }
            default:
                if (isValidExpressionStart(nextToken.kind, 1)) {
                    return parseExpression(false);
                }

                if (isTypeStartingToken(nextToken.kind)) {
                    return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
                }

                recover(nextToken, ParserRuleContext.STMT_START_BRACKETED_LIST_MEMBER);
                return parseStatementStartBracketedListMember();
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
                if (getNextNextToken(nextToken.kind).kind == SyntaxKind.OPEN_PAREN_TOKEN) {
                    return parseErrorConstructorExpr();
                }

                // error-type-desc
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case XML_KEYWORD:
            case STRING_KEYWORD:
                if (getNextNextToken(nextToken.kind).kind == SyntaxKind.BACKTICK_TOKEN) {
                    return parseExpression(false);
                }
                return parseTypeDescriptor(ParserRuleContext.TYPE_DESC_IN_TUPLE);
            case TABLE_KEYWORD:
            case STREAM_KEYWORD:
                if (getNextNextToken(nextToken.kind).kind == SyntaxKind.LT_TOKEN) {
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

                recover(peek(), ParserRuleContext.TUPLE_TYPE_DESC_OR_LIST_CONST_MEMBER, annots);
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
            case CLOSE_BRACKET_TOKEN:// [a, b, c]]
                if (!isRoot) {
                    endContext();
                    return new STAmbiguousCollectionNode(SyntaxKind.TUPLE_TYPE_DESC_OR_LIST_CONST, openBracket, members,
                            closeBracket);
                }
                // fall through
            default:
                if (isValidExprRhsStart(peek().kind, closeBracket.kind) ||
                        (isRoot && peek().kind == SyntaxKind.EQUAL_TOKEN)) {
                    members = getExpressionList(members);
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
            STNode finalKeyword = STNodeFactory.createEmptyNode();
            STNode typedBindingPattern =
                    parseTypedBindingPatternTypeRhs(tupleTypeOrListConst, ParserRuleContext.VAR_DECL_STMT, isRoot);
            if (!isRoot) {
                return typedBindingPattern;
            }
            switchContext(ParserRuleContext.VAR_DECL_STMT);
            return parseVarDeclRhs(annots, finalKeyword, typedBindingPattern, false);
        }

        STNode expr = getExpression(tupleTypeOrListConst);
        expr = parseExpressionRhs(DEFAULT_OP_PRECEDENCE, expr, false, true);
        return parseStatementStartWithExprRhs(expr);
    }

    private STNode parseAsTupleTypeDesc(STNode annots, STNode openBracket, List<STNode> memberList, STNode member,
                                        boolean isRoot) {
        memberList = getTypeDescList(memberList);
        startContext(ParserRuleContext.TYPE_DESC_IN_TUPLE);
        STNode tupleTypeMembers = parseTupleTypeMembers(member, memberList);
        STNode closeBracket = parseCloseBracket();
        endContext();

        STNode tupleType = STNodeFactory.createTupleTypeDescriptorNode(openBracket, tupleTypeMembers, closeBracket);
        STNode typeDesc =
                parseComplexTypeDescriptor(tupleType, ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
        endContext();
        STNode typedBindingPattern = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT, isRoot);
        if (!isRoot) {
            return typedBindingPattern;
        }

        switchContext(ParserRuleContext.VAR_DECL_STMT);
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
        if (memberNode.kind.compareTo(SyntaxKind.RECORD_TYPE_DESC) >= 0 &&
                memberNode.kind.compareTo(SyntaxKind.TYPEDESC_TYPE_DESC) <= 0) {
            return SyntaxKind.TUPLE_TYPE_DESC;
        }

        switch (memberNode.kind) {
            case NUMERIC_LITERAL:
            case ASTERISK_LITERAL:
                return SyntaxKind.ARRAY_TYPE_DESC;
            case CAPTURE_BINDING_PATTERN:
            case LIST_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
            case ERROR_BINDING_PATTERN:
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
                return SyntaxKind.NONE;
            case FUNCTION_CALL:
                if (isPossibleErrorBindingPattern((STFunctionCallExpressionNode) memberNode)) {
                    return SyntaxKind.NONE;
                }
                return SyntaxKind.LIST_CONSTRUCTOR;
            case INDEXED_EXPRESSION:
                return SyntaxKind.TUPLE_TYPE_DESC_OR_LIST_CONST;
            default:
                if (isExpression(memberNode.kind) && !isAllBasicLiterals(memberNode) && !isAmbiguous(memberNode)) {
                    return SyntaxKind.LIST_CONSTRUCTOR;
                }

                // can be anyof the three.
                return SyntaxKind.NONE;
        }
    }

    private boolean isPossibleErrorBindingPattern(STFunctionCallExpressionNode funcCall) {
        STNode args = funcCall.arguments;
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
            case FUNCTION_CALL:
                return isPossibleErrorBindingPattern((STFunctionCallExpressionNode) node);
            default:
                return false;
        }
    }

    private STNode parseStatementStartBracketedList(STNode annots, STNode openBracket, List<STNode> members,
                                                    STNode closeBracket, boolean isRoot, boolean possibleMappingField) {
        STToken nextToken = peek();
        switch (nextToken.kind) {
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
     * @param openBrace         Open brace
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
                STNode bindingPattern = getBindingPattern(bpOrConstructor);
                return parseAssignmentStmtRhs(bindingPattern);
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
            default:
                // If this is followed by an assignment, then treat this node as mapping-binding pattern.
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
                startContext(ParserRuleContext.VAR_DECL_STMT);
                startContext(ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN);
                STNode typeDesc = parseComplexTypeDescriptor(readonlyKeyword,
                        ParserRuleContext.TYPE_DESC_IN_TYPE_BINDING_PATTERN, true);
                endContext();
                STNode annots = STNodeFactory.createEmptyNodeList();
                STNode finalKeyword = STNodeFactory.createEmptyNode();
                STNode typedBP = parseTypedBindingPatternTypeRhs(typeDesc, ParserRuleContext.VAR_DECL_STMT);
                return parseVarDeclRhs(annots, finalKeyword, typedBP, false);
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

                switch (peek().kind) {
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
                    STNode annots = STNodeFactory.createEmptyNodeList();
                    STNode finalKeyword = STNodeFactory.createEmptyNode();
                    return parseVarDeclRhs(annots, finalKeyword, typedBindingPattern, false);
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
            return getWildcardBindingPattern(secondIdentifier);
        }

        // Reach here for something like: "{foo:bar". This could be anything.
        STNode qualifiedNameRef = STNodeFactory.createQualifiedNameReferenceNode(identifier, colon, secondNameRef);
        switch (peek().kind) {
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
                    case FUNCTION_CALL:
                        if (isPossibleErrorBindingPattern((STFunctionCallExpressionNode) expr)) {
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
                recover(token, ParserRuleContext.FIELD_BINDING_PATTERN_END, key);
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

                // TODO: handle function-binding-pattern
                // we don't know which one
                return parseExpressionRhs(DEFAULT_OP_PRECEDENCE, identifier, false, false);
            case OPEN_BRACE_TOKEN:
                return parseMappingBindingPatterOrMappingConstructor();
            case ELLIPSIS_TOKEN:
                return parseListBindingPatternMember();
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
            case REST_BINDING_PATTERN:
            case MAPPING_BINDING_PATTERN:
            case WILDCARD_BINDING_PATTERN:
                return SyntaxKind.LIST_BINDING_PATTERN;
            case SIMPLE_NAME_REFERENCE: // member is a simple type-ref/var-ref
            case LIST_BP_OR_LIST_CONSTRUCTOR: // member is again ambiguous
            case MAPPING_BP_OR_MAPPING_CONSTRUCTOR:
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
                if (isValidExprRhsStart(peek().kind, closeBracket.kind)) {
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
        STNode annots = STNodeFactory.createEmptyNodeList();

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
                return createArrayTypeDesc(newMemberType, arrayTypeDesc.openBracket, arrayTypeDesc.arrayLength,
                        arrayTypeDesc.closeBracket);
            case UNION_TYPE_DESC:
                STUnionTypeDescriptorNode unionTypeDesc = (STUnionTypeDescriptorNode) typeDesc;
                STNode newlhsType = mergeQualifiedNameWithTypeDesc(qualifiedName, unionTypeDesc.leftTypeDesc);
                return createUnionTypeDesc(newlhsType, unionTypeDesc.pipeToken, unionTypeDesc.rightTypeDesc);
            case INTERSECTION_TYPE_DESC:
                STIntersectionTypeDescriptorNode intersectionTypeDesc = (STIntersectionTypeDescriptorNode) typeDesc;
                newlhsType = mergeQualifiedNameWithTypeDesc(qualifiedName, intersectionTypeDesc.leftTypeDesc);
                return createUnionTypeDesc(newlhsType, intersectionTypeDesc.bitwiseAndToken,
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
        switch (expression.kind) {
            case INDEXED_EXPRESSION:
                return parseArrayTypeDescriptorNode((STIndexedExpressionNode) expression);
            case NUMERIC_LITERAL:
            case BOOLEAN_LITERAL:
            case STRING_LITERAL:
            case NULL_LITERAL:
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
            case BINARY_EXPRESSION:
                STBinaryExpressionNode binaryExpr = (STBinaryExpressionNode) expression;
                switch (binaryExpr.operator.kind) {
                    case PIPE_TOKEN:
                        STNode lhsTypeDesc = getTypeDescFromExpr(binaryExpr.lhsExpr);
                        STNode rhsTypeDesc = getTypeDescFromExpr(binaryExpr.rhsExpr);
                        return createUnionTypeDesc(lhsTypeDesc, binaryExpr.operator, rhsTypeDesc);
                    case BITWISE_AND_TOKEN:
                        lhsTypeDesc = getTypeDescFromExpr(binaryExpr.lhsExpr);
                        rhsTypeDesc = getTypeDescFromExpr(binaryExpr.rhsExpr);
                        return createIntersectionTypeDesc(lhsTypeDesc, binaryExpr.operator, rhsTypeDesc);
                    default:
                        break;
                }
                return expression;
            case UNARY_EXPRESSION:
                return STNodeFactory.createSingletonTypeDescriptorNode(expression);
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
                STNode fieldName = STNodeFactory.createSimpleNameReferenceNode(qualifiedName.modulePrefix);
                return STNodeFactory.createFieldBindingPatternFullNode(fieldName, qualifiedName.colon,
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
                List<STNode> bindingPatterns = new ArrayList<>();
                restBindingPattern = STNodeFactory.createEmptyNode();
                for (int i = 0; i < innerList.members.size(); i++) {
                    STNode bp = getBindingPattern(innerList.members.get(i));
                    if (bp.kind == SyntaxKind.REST_BINDING_PATTERN) {
                        restBindingPattern = bp;
                        break;
                    }
                    bindingPatterns.add(bp);
                }
                memberBindingPatterns = STNodeFactory.createNodeList(bindingPatterns);
                return STNodeFactory.createMappingBindingPatternNode(innerList.collectionStartToken,
                        memberBindingPatterns, restBindingPattern, innerList.collectionEndToken);
            case SPECIFIC_FIELD:
                STSpecificFieldNode field = (STSpecificFieldNode) ambiguousNode;
                fieldName = STNodeFactory.createSimpleNameReferenceNode(field.fieldName);
                if (field.valueExpr == null) {
                    return STNodeFactory.createFieldBindingPatternVarnameNode(fieldName);
                }
                return STNodeFactory.createFieldBindingPatternFullNode(fieldName, field.colon,
                        getBindingPattern(field.valueExpr));
            case FUNCTION_CALL:
                STFunctionCallExpressionNode funcCall = (STFunctionCallExpressionNode) ambiguousNode;
                STNode args = funcCall.arguments;
                int size = args.bucketCount();
                bindingPatterns = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    STNode arg = args.childInBucket(i);
                    bindingPatterns.add(getBindingPattern(arg));
                }

                STNode argListBindingPatterns = STNodeFactory.createNodeList(bindingPatterns);
                STNode errorKeyword;
                STNode typeRef;
                // function name can either be error keyword or type ref.
                if (funcCall.functionName.kind == SyntaxKind.ERROR_TYPE_DESC) {
                    errorKeyword = funcCall.functionName;
                    typeRef = STNodeFactory.createEmptyNode();
                } else {
                    // Create missing error keyword
                    errorKeyword = SyntaxErrors.createMissingTokenWithDiagnostics(SyntaxKind.ERROR_KEYWORD,
                            ParserRuleContext.ERROR_KEYWORD);
                    typeRef = funcCall.functionName;
                }
                return STNodeFactory.createErrorBindingPatternNode(errorKeyword, typeRef, funcCall.openParenToken,
                        argListBindingPatterns, funcCall.closeParenToken);
            case POSITIONAL_ARG:
                STPositionalArgumentNode positionalArg = (STPositionalArgumentNode) ambiguousNode;
                return getBindingPattern(positionalArg.expression);
            case NAMED_ARG:
                STNamedArgumentNode namedArg = (STNamedArgumentNode) ambiguousNode;
                return STNodeFactory.createNamedArgBindingPatternNode(namedArg.argumentName, namedArg.equalsToken,
                        getBindingPattern(namedArg.expression));
            case REST_ARG:
                STRestArgumentNode restArg = (STRestArgumentNode) ambiguousNode;
                return STNodeFactory.createRestBindingPatternNode(restArg.ellipsis, restArg.expression);
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
            case TUPLE_TYPE_DESC_OR_LIST_CONST:
                STAmbiguousCollectionNode innerList = (STAmbiguousCollectionNode) ambiguousNode;
                STNode memberExprs = STNodeFactory.createNodeList(getExpressionList(innerList.members));
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
                        fieldNode = getExpression(field);
                    }

                    fieldList.add(fieldNode);
                }
                STNode fields = STNodeFactory.createNodeList(fieldList);
                return STNodeFactory.createMappingConstructorExpressionNode(innerList.collectionStartToken, fields,
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
                readonlyKeyword = STNodeFactory.createEmptyNode();
                return STNodeFactory.createSpecificFieldNode(readonlyKeyword, identifier, colon, bindingPatternOrExpr);
        }
    }

    // ----------------------------------------- ~ End of Parser ~ ----------------------------------------

    // NOTE: Please add any new methods to the relevant section of the class. Binding patterns related code is the
    // last section of the class.
}
