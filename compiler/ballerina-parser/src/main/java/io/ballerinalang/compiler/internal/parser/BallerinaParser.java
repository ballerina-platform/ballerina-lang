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

import io.ballerinalang.compiler.internal.parser.BallerinaParserErrorHandler.Action;
import io.ballerinalang.compiler.internal.parser.BallerinaParserErrorHandler.Solution;
import io.ballerinalang.compiler.internal.parser.tree.STAssignmentStatement;
import io.ballerinalang.compiler.internal.parser.tree.STBinaryExpression;
import io.ballerinalang.compiler.internal.parser.tree.STBlockStatement;
import io.ballerinalang.compiler.internal.parser.tree.STBracedExpression;
import io.ballerinalang.compiler.internal.parser.tree.STDefaultableParameter;
import io.ballerinalang.compiler.internal.parser.tree.STEmptyNode;
import io.ballerinalang.compiler.internal.parser.tree.STExternalFuncBody;
import io.ballerinalang.compiler.internal.parser.tree.STFunctionDefinition;
import io.ballerinalang.compiler.internal.parser.tree.STMissingToken;
import io.ballerinalang.compiler.internal.parser.tree.STModulePart;
import io.ballerinalang.compiler.internal.parser.tree.STModuleTypeDefinition;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.STRecordField;
import io.ballerinalang.compiler.internal.parser.tree.STRecordFieldWithDefaultValue;
import io.ballerinalang.compiler.internal.parser.tree.STRecordRestDescriptor;
import io.ballerinalang.compiler.internal.parser.tree.STRecordTypeDescriptor;
import io.ballerinalang.compiler.internal.parser.tree.STRecordTypeReference;
import io.ballerinalang.compiler.internal.parser.tree.STRequiredParameter;
import io.ballerinalang.compiler.internal.parser.tree.STRestParameter;
import io.ballerinalang.compiler.internal.parser.tree.STReturnTypeDescriptor;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.STVariableDeclaration;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;

import java.util.ArrayList;
import java.util.List;

/**
 * A LL(k) recursive-descent parser for ballerina.
 * 
 * @since 1.2.0
 */
public class BallerinaParser {

    private final BallerinaParserErrorHandler errorHandler;
    private final AbstractNodeSupplier tokenReader;

    private ParserRuleContext currentParamKind = ParserRuleContext.REQUIRED_PARAM;

    public BallerinaParser(BallerinaLexer lexer) {
        this.tokenReader = new TokenReader(lexer);
        this.errorHandler = new BallerinaParserErrorHandler(tokenReader, this);
    }

    public BallerinaParser(String source) {
        this(new BallerinaLexer(source));
    }

    public BallerinaParser(TextDocument textDocument) {
        this.tokenReader = new TokenReader(new BallerinaLexer(textDocument.getCharacterReader()));
        this.errorHandler = new BallerinaParserErrorHandler(tokenReader, this);
    }

    public BallerinaParser(SyntaxTree oldTree, TextDocument newTextDocument, TextDocumentChange textDocumentChange) {
        this.tokenReader = new IntermixingNodeSupplier(oldTree, newTextDocument, textDocumentChange);
        this.errorHandler = new BallerinaParserErrorHandler(tokenReader, this);
    }

    /**
     * Start parsing the given input.
     */
    public STNode parse() {
        STNode node = parseCompUnit();
        System.out.println(node);
        return node;
    }

    /**
     * Resume the parsing from the given context.
     * 
     * @param context Context to resume parsing
     * @return Parsed node
     */
    public STNode resumeParsing(ParserRuleContext context, STNode... parsedNodes) {
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
            case PARAM_LIST:
                return parseParamList();
            case RETURN_TYPE_DESCRIPTOR:
                return parseReturnTypeDescriptor();
            case SIMPLE_TYPE_DESCRIPTOR:
                return parseTypeDescriptor();
            case ASSIGN_OP:
                return parseAssignOp();
            case ANNOTATION_ATTACHMENT:
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
            case EXPRESSION:
                return parseExpression();
            case STATEMENT:
                return parseStatement();
            case VAR_DECL_STMT:
                return parseVariableDeclStmt();
            case ASSIGNMENT_STMT:
                return parseAssignmentStmt();
            case BINARY_EXPR_RHS:
                return parseBinaryExprRhs(parsedNodes[0]);
            case FOLLOW_UP_PARAM:
                return parseFollowUpParameter();
            case AFTER_PARAMETER_TYPE:
                return parseAfterParamType(parsedNodes[0], parsedNodes[1]);
            case PARAMETER_RHS:
                return parseParameterRhs(parsedNodes[0], parsedNodes[1], parsedNodes[2]);
            case TOP_LEVEL_NODE:
                return parseTopLevelNode(parsedNodes[0]);
            case TOP_LEVEL_NODE_WITH_MODIFIER:
                return parseTopLevelNodeWithModifier();
            case TYPE_OR_VAR_NAME:
                return parseUserDefinedTypeOrVarName();
            case VAR_DECL_STMT_RHS:
                return parseVarDeclRhs(parsedNodes[0], parsedNodes[1]);
            case ASSIGNMENT_OR_VAR_DECL_STMT_RHS:
                return parseAssignmentOrVarDeclRhs(parsedNodes[0]);
            case TYPE_REFERENCE:
                return parseTypeReference();
            case FIELD_DESCRIPTOR_RHS:
                return parseFieldDescriptorRhs(parsedNodes[0], parsedNodes[1]);
            case FUNC_DEFINITION:
            case REQUIRED_PARAM:
            default:
                throw new IllegalStateException("Cannot re-parse rule: " + context);
        }
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
            case TOP_LEVEL_NODE_WITH_MODIFIER:
            case TOP_LEVEL_NODE:
                startContext(ParserRuleContext.COMP_UNIT);
                return parseTopLevelNodeWithModifier();
            case STATEMENT:
                startContext(ParserRuleContext.COMP_UNIT);
                startContext(ParserRuleContext.FUNC_DEFINITION);
                startContext(ParserRuleContext.FUNC_BODY_BLOCK);
                return parseStatement();
            case EXPRESSION:
                startContext(ParserRuleContext.COMP_UNIT);
                startContext(ParserRuleContext.FUNC_DEFINITION);
                startContext(ParserRuleContext.FUNC_BODY_BLOCK);
                startContext(ParserRuleContext.STATEMENT);
                return parseExpression();
            default:
                throw new UnsupportedOperationException("Cannot start parsing from: " + context);
        }
    }

    /*
     * Private methods
     */

    private STToken peek() {
        return this.tokenReader.peek();
    }

    private STToken consume() {
        return this.tokenReader.read();
    }

    private Solution recover(STToken token, ParserRuleContext currentCtx, STNode... parsedNodes) {
        return this.errorHandler.recover(currentCtx, token, parsedNodes);
    }

    private void startContext(ParserRuleContext context) {
        this.errorHandler.startContext(context);
    }

    private void endContext() {
        this.errorHandler.endContext();
    }

    /**
     * Switch the current context to the provided one. This will replace the
     * existing context.
     * 
     * @param context Context to switch to.
     */
    private void switchContext(ParserRuleContext context) {
        this.errorHandler.switchContext(context);
    }

    /**
     * Parse a given input and returns the AST. Starts parsing from the top of a compilation unit.
     * 
     * @return Parsed node
     */
    private STNode parseCompUnit() {
        startContext(ParserRuleContext.COMP_UNIT);
        STToken token = peek();
        List<STNode> otherDecls = new ArrayList<>();
        while (token.kind != SyntaxKind.EOF_TOKEN) {
            otherDecls.add(parseTopLevelNodeWithModifier(token.kind));
            token = peek();
        }

        STToken eof = consume();
        endContext();

        STNode importDecls = new STNodeList(new ArrayList<>());
        STModulePart modulePart = new STModulePart(importDecls, new STNodeList(otherDecls), eof);
        return modulePart;
    }

    /**
     * Parse top level node having an optional modifier preceding it.
     * 
     * @return Parsed node
     */
    private STNode parseTopLevelNodeWithModifier() {
        STToken token = peek();
        return parseTopLevelNodeWithModifier(token.kind);
    }

    /**
     * Parse top level node having an optional modifier preceding it, given the next token kind.
     * 
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseTopLevelNodeWithModifier(SyntaxKind tokenKind) {
        STNode modifier;
        switch (tokenKind) {
            case PUBLIC_KEYWORD:
                modifier = parseModifier();
                break;
            case FUNCTION_KEYWORD:
            case TYPE_KEYWORD:
                modifier = new STEmptyNode();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITH_MODIFIER);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTopLevelNodeWithModifier(solution.tokenKind);
        }

        return parseTopLevelNode(modifier);
    }

    /**
     * Parse top level node.
     * 
     * @return Parsed node
     */
    private STNode parseTopLevelNode(STNode modifier) {
        STToken token = peek();
        return parseTopLevelNode(token.kind, modifier);
    }

    /**
     * Parse top level node given the next token kind.
     * 
     * @param tokenKind Next token kind
     * @return Parsed top-level node
     */
    private STNode parseTopLevelNode(SyntaxKind tokenKind, STNode modifier) {
        switch (tokenKind) {
            case FUNCTION_KEYWORD:
                return parseFunctionDefinition(modifier);
            case TYPE_KEYWORD:
                return parseModuleTypeDefinition(modifier);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE, modifier);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTopLevelNode(solution.tokenKind, modifier);
        }
    }

    /**
     * Parse access modifiers.
     * 
     * @return Parsed node
     */
    private STNode parseModifier() {
        STToken token = peek();
        if (token.kind == SyntaxKind.PUBLIC_KEYWORD) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.PUBLIC);
            return sol.recoveredNode;
        }
    }

    /**
     * <p>
     * Parse function definition. A function definition has the following structure.
     * </p>
     * <code>
     * function-defn := FUNCTION identifier function-signature function-body
     * </code>
     * 
     * @return Parsed node
     */
    private STNode parseFunctionDefinition(STNode modifier) {
        startContext(ParserRuleContext.FUNC_DEFINITION);

        STNode functionKeyword = parseFunctionKeyword();
        STNode name = parseFunctionName();
        STNode openParenthesis = parseOpenParenthesis();
        STNode parameters = parseParamList();
        STNode closeParenthesis = parseCloseParenthesis();
        STNode returnTypeDesc = parseReturnTypeDescriptor();
        STNode body = parseFunctionBody();

        STFunctionDefinition func = new STFunctionDefinition(modifier, functionKeyword, name, openParenthesis,
                parameters, closeParenthesis, returnTypeDesc, body);

        endContext();
        return func;
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
     * @return Parsed node
     */
    private STNode parseParamList() {
        startContext(ParserRuleContext.PARAM_LIST);

        ArrayList<STNode> paramsList = new ArrayList<>();

        STToken token = peek();
        if (!isEndOfParametersList(token.kind)) {
            // comma precedes the first parameter, which doesn't exist
            STEmptyNode startingComma = new STEmptyNode();
            this.currentParamKind = ParserRuleContext.REQUIRED_PARAM;
            paramsList.add(parseParameter(startingComma));
        }

        while (true) {
            STNode param = parseFollowUpParameter();
            if (param == null) {
                break;
            }
            paramsList.add(param);
        }

        STNode params = new STNodeList(paramsList);
        endContext();
        return params;
    }

    /**
     * Parse a single parameter. Parameter can be a required parameter, a defaultable
     * parameter, or a rest parameter.
     * 
     * @param leadingComma Comma the occurs before the param
     * @return Parsed node
     */
    private STNode parseParameter(STNode leadingComma) {
        STToken token = peek();
        if (this.currentParamKind == ParserRuleContext.REST_PARAM) {
            // This is an erroneous scenario, where there are more parameters after
            // the rest parameter. Log an error, and continue the remainder of the
            // parameters by removing the order restriction.

            // TODO: mark the node as erroneous
            this.errorHandler.reportInvalidNode(token, "cannot have more parameters after the rest-parameter");
            startContext(ParserRuleContext.REQUIRED_PARAM);
        } else {
            startContext(this.currentParamKind);
        }

        if (token.kind == SyntaxKind.PUBLIC_KEYWORD) {
            parseModifier();
            token = peek();
        }

        STNode type = parseTypeDescriptor(token.kind);
        STNode param = parseAfterParamType(leadingComma, type);
        endContext();
        return param;
    }

    private STNode parseAfterParamType(STNode leadingComma, STNode type) {
        STToken token = peek();
        return parseAfterParamType(token.kind, leadingComma, type);
    }

    private STNode parseAfterParamType(SyntaxKind tokenKind, STNode leadingComma, STNode type) {
        switch (tokenKind) {
            case ELLIPSIS_TOKEN:
                this.currentParamKind = ParserRuleContext.REST_PARAM;
                switchContext(ParserRuleContext.REST_PARAM);
                STNode ellipsis = parseEllipsis();
                STNode paramName = parseVariableName();
                return new STRestParameter(SyntaxKind.PARAMETER, leadingComma, type, ellipsis, paramName);
            case IDENTIFIER_TOKEN:
                paramName = parseVariableName();
                return parseParameterRhs(leadingComma, type, paramName);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.AFTER_PARAMETER_TYPE, leadingComma, type);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseAfterParamType(solution.tokenKind, leadingComma, type);
        }
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
     * @return Parsed parameter node
     */
    private STNode parseParameterRhs(STNode leadingComma, STNode type, STNode paramName) {
        STToken token = peek();
        return parseParameterRhs(token.kind, leadingComma, type, paramName);
    }

    /**
     * Parse the right hand side of a required/defaultable parameter, given the next token kind.
     * 
     * @param tokenKind Next token kind
     * @return Parsed parameter node
     */
    private STNode parseParameterRhs(SyntaxKind tokenKind, STNode leadingComma, STNode type, STNode paramName) {
        // Required parameters
        if (isEndOfParameter(tokenKind)) {
            if (this.currentParamKind == ParserRuleContext.DEFAULTABLE_PARAM) {
                // This is an erroneous scenario, where a required parameters comes after
                // a defaulatble parameter. Log an error, and continue.

                // TODO: mark the node as erroneous
                // TODO: Fix the line number in the error
                this.errorHandler.reportInvalidNode(peek(),
                        "cannot have a required parameter after a defaultable parameter");
            }

            // TODO: add access modifier
            STNode accessModifier = new STEmptyNode();
            return new STRequiredParameter(SyntaxKind.PARAMETER, leadingComma, accessModifier, type, paramName);
        } else if (tokenKind == SyntaxKind.EQUAL_TOKEN) {

            // If we were processing required params so far and found a defualtable
            // parameter, then switch the context to defaultable params.
            if (this.currentParamKind == ParserRuleContext.REQUIRED_PARAM) {
                this.currentParamKind = ParserRuleContext.DEFAULTABLE_PARAM;
                switchContext(ParserRuleContext.DEFAULTABLE_PARAM);
            }

            // Defaultable parameters
            STNode equal = parseAssignOp();
            STNode expr = parseExpression();
            // this.listner.exitDefaultableParameter();
            STNode accessModifier = new STEmptyNode();
            STDefaultableParameter param = new STDefaultableParameter(SyntaxKind.PARAMETER, leadingComma,
                    accessModifier, type, paramName, equal, expr);
            return param;
        } else {
            STToken token = peek();
            Solution solution = recover(token, ParserRuleContext.PARAMETER_RHS, leadingComma, type, paramName);

            // If the parser recovered by inserting a token, then try to re-parse the same
            // rule with the inserted token. This is done to pick the correct branch
            // to continue the parsing.
            if (solution.action == Action.REMOVE) {
                return solution.recoveredNode;
            }

            return parseParameterRhs(solution.tokenKind, leadingComma, type, paramName);
        }
    }

    private STNode parseFollowUpParameter() {
        STToken token = peek();
        return parseFollowUpParameter(token.kind);
    }

    /**
     * Parse a parameter that follows another parameter.
     * 
     * @return Parsed node
     */
    private STNode parseFollowUpParameter(SyntaxKind kind) {
        if (isEndOfParametersList(kind)) {
            return null;
        }

        STNode leadingComma = parseComma();
        return parseParameter(leadingComma);
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
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case COMMA_TOKEN:
            case PUBLIC_KEYWORD:
            case FUNCTION_KEYWORD:
            case EOF_TOKEN:
            case RETURNS_KEYWORD:
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
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case OPEN_BRACE_TOKEN:
            case SEMICOLON_TOKEN:
            case PUBLIC_KEYWORD:
            case FUNCTION_KEYWORD:
            case EOF_TOKEN:
            case RETURNS_KEYWORD:
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
     * @return Parsed node
     */
    private STNode parseReturnTypeDescriptor() {
        startContext(ParserRuleContext.RETURN_TYPE_DESCRIPTOR);

        // If the return type is not present, simply return
        STToken token = peek();

        STNode returnsKeyword;
        if (token.kind == SyntaxKind.RETURNS_KEYWORD) {
            returnsKeyword = consume();
        } else {
            return new STEmptyNode();
        }

        STNode annot = parseAnnotations();
        STNode type = parseTypeDescriptor();

        endContext();
        return new STReturnTypeDescriptor(returnsKeyword, annot, type);
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
    private STNode parseTypeDescriptor() {
        STToken token = peek();
        return parseTypeDescriptor(token.kind);
    }

    /**
     * <p>
     * Parse a type descriptor, given the next token kind.
     * </p>
     * 
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseTypeDescriptor(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case TYPE_TOKEN:
            case IDENTIFIER_TOKEN:
                // simple type descriptor
                return parseSimpleTypeDescriptor();
            case RECORD_KEYWORD:
                // Record type descriptor
                return parseRecordTypeDescriptor();
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TYPE_DESCRIPTOR);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseTypeDescriptor(solution.tokenKind);
        }

        // TODO: only supports built-in/user-defined types. Add others.
    }

    /**
     * Parse simple type desscriptor.
     * 
     * @return Parsed node
     */
    private STNode parseSimpleTypeDescriptor() {
        STToken node = peek();
        switch (node.kind) {
            case TYPE_TOKEN:
            case IDENTIFIER_TOKEN:
                return consume();
            default:
                Solution sol = recover(peek(), ParserRuleContext.SIMPLE_TYPE_DESCRIPTOR);
                return sol.recoveredNode;
        }
    }

    /**
     * Parse annotation attachments.
     * 
     * @return Parsed node
     */
    private STNode parseAnnotations() {
        // TODO
        return new STEmptyNode();
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
    private STNode parseFunctionBody(SyntaxKind tokenKind) {
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
                    // TODO: check this again
                    return new STMissingToken(solution.tokenKind);
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
        STNode stmts = parseStatements(); // TODO: allow workers
        STNode closeBrace = parseCloseBrace();
        endContext();
        return new STBlockStatement(SyntaxKind.BLOCK_STATEMENT, openBrace, stmts, closeBrace);
    }

    /**
     * Check whether the given token is an end of a block.
     * 
     * @param tokenKind STToken to check
     * @return <code>true</code> if the token represents an end of a block. <code>false</code> otherwise
     */
    private boolean isEndOfBlock(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case CLOSE_BRACE_TOKEN:
            case PUBLIC_KEYWORD:
            case FUNCTION_KEYWORD:
            case EOF_TOKEN:
            case CLOSE_BRACE_PIPE_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse user defined type or variable name.
     * 
     * @return Parsed node
     */
    private STNode parseUserDefinedTypeOrVarName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TYPE_OR_VAR_NAME);
            return sol.recoveredNode;
        }
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
        STExternalFuncBody externFunc = new STExternalFuncBody(SyntaxKind.EXTERNAL_FUNCTION_BODY, assign, annotation,
                externalKeyword, semicolon);
        return externFunc;
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
            case EQUAL_GT_TOKEN:
            case DOUBLE_EQUAL_TOKEN:
            case TRIPPLE_EQUAL_TOKEN:
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
                return OperatorPrecedence.BINARY_COMPARE;
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
    private SyntaxKind getOperatorKindToInsert(OperatorPrecedence opPrecedenceLevel) {
        switch (opPrecedenceLevel) {
            case MULTIPLICATIVE:
                return SyntaxKind.ASTERISK_TOKEN;
            case ADDITIVE:
                return SyntaxKind.PLUS_TOKEN;
            case BINARY_COMPARE:
                return SyntaxKind.LT_TOKEN;
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
     * @return Parsed node
     */
    private STNode parseModuleTypeDefinition(STNode modifier) {
        startContext(ParserRuleContext.MODULE_TYPE_DEFINITION);

        STNode typeKeyword = parseTypeKeyword();
        STNode typeName = parseTypeName();
        STNode typeDescriptor = parseTypeDescriptor();
        STNode comma = parseSemicolon();

        endContext();
        STModuleTypeDefinition typeDef =
                new STModuleTypeDefinition(modifier, typeKeyword, typeName, typeDescriptor, comma);
        return typeDef;
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

        STNode bodyEndDelimiter = parseRecordBodyCloseDelimiter();
        endContext();

        return new STRecordTypeDescriptor(recordKeyword, bodyStartDelimiter, fields, bodyEndDelimiter);
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
    private STNode parseRecordBodyCloseDelimiter() {
        STToken token = peek();
        return parseRecordBodyCloseDelimiter(token.kind);
    }

    private STNode parseRecordBodyCloseDelimiter(SyntaxKind kind) {
        switch (kind) {
            case CLOSE_BRACE_PIPE_TOKEN:
                return parseClosedRecordBodyEnd();
            case CLOSE_BRACE_TOKEN:
                return parseCloseBrace();
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RECORD_BODY_END);

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

        while (!isEndOfBlock(token.kind)) {
            STNode field = parseFieldOrRestDescriptor(isInclusive);
            recordFields.add(field);
            token = peek();

            if (field.kind == SyntaxKind.RECORD_REST_TYPE) {
                break;
            }
        }

        // Following loop will only run if there are more fields after the rest type descriptor.
        // Try to parse them and mark as invalid.
        while (!isEndOfBlock(token.kind)) {
            parseFieldOrRestDescriptor(isInclusive);
            this.errorHandler.reportInvalidNode(token, "cannot have more fields after the rest type descriptor");
            token = peek();
        }

        return new STNodeList(recordFields);
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
        startContext(ParserRuleContext.RECORD_FIELD);

        // record-type-reference
        STToken token = peek();
        if (token.kind == SyntaxKind.ASTERISK_TOKEN) {
            STNode asterisk = consume();
            STNode type = parseTypeReference();
            STNode semicolonToken = parseSemicolon();
            endContext();
            return new STRecordTypeReference(asterisk, type, semicolonToken);
        }

        // individual-field-descriptor
        STNode type = parseTypeDescriptor();
        STNode fieldOrRestDesc;
        if (isInclusive) {
            STNode fieldName = parseVariableName();
            fieldOrRestDesc = parseFieldDescriptorRhs(type, fieldName);
        } else {
            fieldOrRestDesc = parseFieldOrRestDescriptorRhs(type);
        }

        endContext();
        return fieldOrRestDesc;
    }

    private STNode parseTypeReference() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            return consume();
        } else {
            Solution sol = recover(token, ParserRuleContext.TYPE_REFERENCE);
            return sol.recoveredNode;
        }
    }

    /**
     * Parse RHS of a field or rest type descriptor.
     * 
     * @return Parsed node
     */
    private STNode parseFieldOrRestDescriptorRhs(STNode type) {
        STToken token = peek();
        return parseFieldOrRestDescriptorRhs(token.kind, type);
    }

    private STNode parseFieldOrRestDescriptorRhs(SyntaxKind kind, STNode type) {
        switch (kind) {
            case ELLIPSIS_TOKEN:
                STNode ellipsis = parseEllipsis();
                STNode semicolonToken = parseSemicolon();
                return new STRecordRestDescriptor(type, ellipsis, semicolonToken);
            case IDENTIFIER_TOKEN:
                STNode fieldName = parseVariableName();
                return parseFieldDescriptorRhs(type, fieldName);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FIELD_OR_REST_DESCIPTOR_RHS, type);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseFieldOrRestDescriptorRhs(solution.tokenKind, type);
        }
    }

    /**
     * <p>
     * Parse field descriptor rhs.
     * </p>
     * 
     * @return Parsed node
     */
    private STNode parseFieldDescriptorRhs(STNode type, STNode fieldName) {

        STToken token = peek();
        return parseFieldDescriptorRhs(token.kind, type, fieldName);
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
     * @return Parsed node
     */
    private STNode parseFieldDescriptorRhs(SyntaxKind kind, STNode type, STNode fieldName) {
        switch (kind) {
            case SEMICOLON_TOKEN:
                STNode questionMarkToken = new STEmptyNode();
                STNode semicolonToken = parseSemicolon();
                return new STRecordField(type, fieldName, questionMarkToken, semicolonToken);
            case QUESTION_MARK_TOKEN:
                questionMarkToken = parseQuestionMark();
                semicolonToken = parseSemicolon();
                return new STRecordField(type, fieldName, questionMarkToken, semicolonToken);
            case EQUAL_TOKEN:
                // parseRecordDefaultValue();
                STNode equalsToken = parseAssignOp();
                STNode expression = parseExpression();
                semicolonToken = parseSemicolon();
                return new STRecordFieldWithDefaultValue(type, fieldName, equalsToken, expression, semicolonToken);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FIELD_DESCRIPTOR_RHS, type, fieldName);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseFieldDescriptorRhs(solution.tokenKind, type, fieldName);
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
        // TODO: parse statements/worker declrs
        STToken token = peek();

        ArrayList<STNode> stmts = new ArrayList<>();
        while (!isEndOfBlock(token.kind)) {
            stmts.add(parseStatement());
            token = peek();
        }

        return new STNodeList(stmts);
    }

    /**
     * Parse a single statement.
     * 
     * @return Parsed node
     */
    private STNode parseStatement() {
        STToken token = peek();
        return parseStatement(token.kind);
    }

    /**
     * Parse a single statement, given the next token kind.
     * 
     * @param tokenKind Next tokenKind
     * @return Parsed node
     */
    private STNode parseStatement(SyntaxKind tokenKind) {
        switch (tokenKind) {
            // TODO: add all 'type starting tokens' here. should be same as 'parseTypeDescriptor(...)'
            case TYPE_TOKEN:
            case RECORD_KEYWORD:
                // If the statement starts with a type, then its a var declaration.
                // This is an optimization since if we know the next token is a type, then
                // we can parse the var-def faster.
                return parseVariableDeclStmt();
            case IDENTIFIER_TOKEN:
                // If the statement starts with an identifier, it could be either an assignment
                // statement or a var-decl-stmt with a user defined type.
                return parseAssignmentOrVarDecl();
            default:
                // If the next token in the token stream does not match to any of the statements and
                // if it is not the end of statement, then try to fix it and continue.
                if (isEndOfBlock(tokenKind)) {
                    // TODO: revisit this
                    return new STEmptyNode();
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
    }

    /**
     * <p>
     * Parse local variable declaration statement. A local variable declaration can take following format.
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
     * @return Parsed node
     */
    private STNode parseVariableDeclStmt() {
        startContext(ParserRuleContext.VAR_DECL_STMT);
        STNode type = parseTypeDescriptor();
        STNode varName = parseVariableName();
        STNode varDecl = parseVarDeclRhs(type, varName);
        endContext();
        return varDecl;
    }

    /**
     * <p>
     * Parse the right hand side of a variable declaration statement.
     * </p>
     * <code>
     * var-decl-rhs := ; | = action-or-expr ;
     * </code>
     * 
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(STNode type, STNode varName) {
        STToken token = peek();
        return parseVarDeclRhs(token.kind, type, varName);
    }

    /**
     * Parse the right hand side of a variable declaration statement, given the
     * next token kind.
     * 
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseVarDeclRhs(SyntaxKind tokenKind, STNode type, STNode varName) {
        STNode assign;
        STNode expr;
        STNode semicolon;

        switch (tokenKind) {
            case EQUAL_TOKEN:
                assign = parseAssignOp();
                expr = parseExpression();
                semicolon = parseSemicolon();
                break;
            case SEMICOLON_TOKEN:
                assign = new STEmptyNode();
                expr = new STEmptyNode();
                semicolon = parseSemicolon();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.VAR_DECL_STMT_RHS, type, varName);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseVarDeclRhs(solution.tokenKind, type, varName);
        }

        return new STVariableDeclaration(SyntaxKind.LOCAL_VARIABLE_DECL, type, varName, assign, expr, semicolon);
    }

    /**
     * If the statement starts with an identifier, it could be either an assignment statement or
     * a var-decl-stmt with a user defined type. This method will parse such ambiguous scenarios.
     * 
     * @return Parsed node
     */
    private STNode parseAssignmentOrVarDecl() {
        startContext(ParserRuleContext.ASSIGNMENT_OR_VAR_DECL_STMT_RHS);
        STNode typeOrVarName = parseUserDefinedTypeOrVarName();
        STNode assignmentOrVarDecl = parseAssignmentOrVarDeclRhs(typeOrVarName);
        endContext();
        return assignmentOrVarDecl;
    }

    /**
     * Parse the second portion of an assignment statement or a var-decl statement when ambiguous.
     * 
     * @param typeOrVarName Type name or variable name
     * @return Parsed node
     */
    private STNode parseAssignmentOrVarDeclRhs(STNode typeOrVarName) {
        STToken token = peek();
        return parseAssignmentOrVarDeclRhs(token.kind, typeOrVarName);
    }

    /**
     * Parse the second portion of an assignment statement or a var-decl statement when ambiguous,
     * given the next token kind.
     * 
     * @param tokenKind Next token kind
     * @param typeOrVarName Type name or variable name
     * @return Parsed node
     */
    private STNode parseAssignmentOrVarDeclRhs(SyntaxKind tokenKind, STNode typeOrVarName) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
                STNode varName = parseVariableName();
                return parseVarDeclRhs(typeOrVarName, varName);
            case EQUAL_TOKEN:
                return parseAssignmentStmtRhs(typeOrVarName);
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.ASSIGNMENT_OR_VAR_DECL_STMT_RHS, typeOrVarName);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.REMOVE) {
                    return solution.recoveredNode;
                }

                return parseAssignmentOrVarDeclRhs(solution.tokenKind, typeOrVarName);
        }
    }

    /**
     * <p>
     * Parse assignment statement, which takes the following format.
     * </p>
     * <code>assignment-stmt := lvexpr = action-or-expr ;</code>
     * 
     * @return Parsed node
     */
    private STNode parseAssignmentStmt() {
        startContext(ParserRuleContext.ASSIGNMENT_STMT);
        STNode varName = parseVariableName();
        STNode assignmentStmt = parseAssignmentStmtRhs(varName);
        endContext();
        return assignmentStmt;
    }

    /**
     * <p>
     * Parse the RHS portion of the assignment.
     * </p>
     * <code>assignment-stmt-rhs := = action-or-expr ;</code>
     * 
     * @param varName Var ref name
     * @return Parsed node
     */
    private STNode parseAssignmentStmtRhs(STNode varName) {
        STNode assign = parseAssignOp();
        STNode expr = parseExpression();
        STNode semicolon = parseSemicolon();
        return new STAssignmentStatement(SyntaxKind.ASSIGNMENT_STATEMENT, varName, assign, expr, semicolon);
    }

    /*
     * Expressions
     */

    /**
     * Parse expression. This will start parsing expressions from the lowest level of precedence.
     * 
     * @return Parsed node
     */
    private STNode parseExpression() {
        return parseExpression(OperatorPrecedence.BINARY_COMPARE);
    }

    /**
     * Parse an expression that has an equal or higher precedence than a given level.
     * 
     * @param precedenceLevel Precedence level of expression to be parsed
     * @return Parsed node
     */
    private STNode parseExpression(OperatorPrecedence precedenceLevel) {
        STNode expr = parseTerminalExpression();
        return parseBinaryExprRhs(precedenceLevel, expr);
    }

    /**
     * Parse terminal expressions. A terminal expression has the highest precedence level
     * out of all expressions, and will be at the leaves of an expression tree.
     * 
     * @return Parsed node
     */
    private STNode parseTerminalExpression() {
        STToken token = peek();
        switch (token.kind) {
            case NUMERIC_LITERAL_TOKEN:
                return parseLiteral();
            case IDENTIFIER_TOKEN:
                return parseVariableName();
            case OPEN_PAREN_TOKEN:
                return parseBracedExpression();
            default:
                Solution sol = recover(token, ParserRuleContext.EXPRESSION);
                return sol.recoveredNode;
        }
    }

    /**
     * Parse the right-hand-side of a binary expression.
     * 
     * @return Parsed node
     */
    private STNode parseBinaryExprRhs(STNode lhsExpr) {
        return parseBinaryExprRhs(OperatorPrecedence.BINARY_COMPARE, lhsExpr);
    }

    /**
     * <p>
     * Parse the right-hand-side of a binary expression.
     * </p>
     * <code>binary-expr-rhs := (binary-op expression)*</code>
     * 
     * @param precedenceLevel Precedence level of the expression that is being parsed currently
     * @param lhsExpr LHS expression of the binary expression
     * @return Parsed node
     */
    private STNode parseBinaryExprRhs(OperatorPrecedence precedenceLevel, STNode lhsExpr) {
        STToken token = peek();
        return parseBinaryExprRhs(precedenceLevel, token.kind, lhsExpr);
    }

    /**
     * Parse the right hand side of a binary expression given the next token kind.
     * 
     * @param precedenceLevel Precedence level of the expression that is being parsed currently
     * @param tokenKind Next token kind
     * @return Parsed node
     */
    private STNode parseBinaryExprRhs(OperatorPrecedence precedenceLevel, SyntaxKind tokenKind, STNode lhsExpr) {
        if (isEndOfExpression(tokenKind)) {
            return lhsExpr;
        }

        if (!isBinaryOperator(tokenKind)) {
            STToken token = peek();
            Solution solution = recover(token, ParserRuleContext.BINARY_EXPR_RHS, lhsExpr);

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
                SyntaxKind binaryOpKind = getOperatorKindToInsert(precedenceLevel);
                return parseBinaryExprRhs(precedenceLevel, binaryOpKind, lhsExpr);
            } else {
                return parseBinaryExprRhs(precedenceLevel, solution.tokenKind, lhsExpr);
            }
        }

        // If the precedence level of the operator that was being parsed is higher than
        // the newly found (next) operator, then return and finish the previous expr,
        // because it has a higher precedence.
        OperatorPrecedence operatorPrecedence = getOpPrecedence(tokenKind);
        if (precedenceLevel.isHigherThan(operatorPrecedence)) {
            return lhsExpr;
        }

        STNode operator = parseBinaryOperator();

        // Parse the expression that follows the binary operator, until a operator
        // with different precedence is encountered. If an operator with a lower
        // precedence is reached, then come back here and finish the current
        // binary expr. If a an operator with higher precedence level is reached,
        // then complete that binary-expr, come back here and finish the current
        // expr.
        STNode rhsExpr = parseExpression(operatorPrecedence);

        STBinaryExpression binaryExpr =
                new STBinaryExpression(SyntaxKind.BINARY_EXPRESSION, lhsExpr, operator, rhsExpr);

        // Then continue the operators with the same precedence level.
        return parseBinaryExprRhs(precedenceLevel, binaryExpr);
    }

    /**
     * <p>
     * Parse braced expression.
     * </p>
     * <code>braced-expr := ( expression )</code>
     * 
     * @return Parsed node
     */
    private STNode parseBracedExpression() {
        STNode openParen = parseOpenParenthesis();
        STNode expr = parseExpression();
        STNode closeParen = parseCloseParenthesis();
        return new STBracedExpression(SyntaxKind.BRACED_EXPRESSION, openParen, expr, closeParen);
    }

    /**
     * Check whether the given token is an end of a expression.
     * 
     * @param tokenKind Token to check
     * @return <code>true</code> if the token represents an end of a block. <code>false</code> otherwise
     */
    private boolean isEndOfExpression(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case CLOSE_BRACE_TOKEN:
            case CLOSE_PAREN_TOKEN:
            case CLOSE_BRACKET_TOKEN:
            case SEMICOLON_TOKEN:
            case COMMA_TOKEN:
            case PUBLIC_KEYWORD:
            case FUNCTION_KEYWORD:
            case EOF_TOKEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Parse a literal expression.
     * 
     * @return Parsed node
     */
    private STNode parseLiteral() {
        return consume();
    }
}
