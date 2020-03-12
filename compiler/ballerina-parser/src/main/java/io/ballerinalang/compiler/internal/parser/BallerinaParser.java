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
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;

import java.util.ArrayDeque;

/**
 * A LL(k) recursive-descent parser for ballerina.
 * 
 * @since 1.2.0
 */
public class BallerinaParser {

    private final BallerinaParserListener listner = new BallerinaParserListener();
    private final BallerinaParserErrorHandler errorHandler;
    private final AbstractNodeSupplier tokenReader;

    private ParserRuleContext currentParamKind = ParserRuleContext.REQUIRED_PARAM;
    private ArrayDeque<ParserRuleContext> currentFieldKindStack = new ArrayDeque<>();

    public BallerinaParser(BallerinaLexer lexer) {
        this.tokenReader = new TokenReader(lexer);
        this.errorHandler = new BallerinaParserErrorHandler(tokenReader, listner, this);
    }

    public BallerinaParser(String source) {
        this(new BallerinaLexer(source));
    }

    public BallerinaParser(TextDocument textDocument) {
        this.tokenReader = new TokenReader(new BallerinaLexer(textDocument.getCharacterReader()));
        this.errorHandler = new BallerinaParserErrorHandler(tokenReader, listner, this);
    }

    public BallerinaParser(SyntaxTree oldTree, TextDocument newTextDocument, TextDocumentChange textDocumentChange) {
        this.tokenReader = new IntermixingNodeSupplier(oldTree, newTextDocument, textDocumentChange);
        this.errorHandler = new BallerinaParserErrorHandler(tokenReader, listner, this);
    }

    /**
     * Start parsing the given input.
     */
    public void parse() {
        parseCompUnit();
    }

    /**
     * Resume the parsing from the given context.
     * 
     * @param context Context to resume parsing
     */
    public void resumeParsing(ParserRuleContext context) {
        switch (context) {
            case COMP_UNIT:
                parseCompUnit();
                break;
            case EXTERNAL_FUNC_BODY:
                parseExternalFunctionBody();
                break;
            case FUNC_BODY:
                parseFunctionBody();
                break;
            case OPEN_BRACE:
                parseOpenBrace();
                break;
            case CLOSE_BRACE:
                parseCloseBrace();
                break;
            case FUNC_DEFINITION:
                parseFunctionDefinition();
                break;
            case FUNC_NAME:
                parseFunctionName();
                break;
            case FUNC_SIGNATURE:
                parseFunctionSignature();
                break;
            case OPEN_PARENTHESIS:
                parseOpenParenthesis();
                break;
            case REQUIRED_PARAM:
                parseParameter();
                break;
            case PARAM_LIST:
                parseParamList();
                break;
            case RETURN_TYPE_DESCRIPTOR:
                parseReturnTypeDescriptor();
                break;
            case TYPE_DESCRIPTOR:
                parseTypeDescriptor();
                break;
            case ASSIGN_OP:
                parseAssignOp();
                break;
            case ANNOTATION_ATTACHMENT:
            case EXTERNAL_KEYWORD:
                parseExternalKeyword();
                break;
            case FUNC_BODY_BLOCK:
                parseFunctionBodyBlock();
                break;
            case SEMICOLON:
                parseSemicolon();
                break;
            case CLOSE_PARENTHESIS:
                parseCloseParenthesis();
                break;
            case VARIABLE_NAME:
                parseVariableName();
                break;
            case EXPRESSION:
                parseExpression();
                break;
            case STATEMENT:
                parseStatement();
                break;
            case VAR_DECL_STMT:
                parseVariableDeclStmt();
                break;
            case ASSIGNMENT_STMT:
                parseAssignmentStmt();
                break;
            case BINARY_EXPR_RHS:
                parseBinaryExprRhs();
                break;
            case FOLLOW_UP_PARAM:
                parseFollowUpParameter();
                break;
            case VAR_DECL_STMT_RHS:
                parseVarDeclRhs();
                break;
            case AFTER_PARAMETER_TYPE:
                parseAfterParamType();
                break;
            case PARAMETER_RHS:
                parseParameterRhs();
                break;
            case TOP_LEVEL_NODE:
                parseTopLevelNode();
                break;
            case TOP_LEVEL_NODE_WITH_MODIFIER:
                parseTopLevelNodeWithModifier();
                break;
            case TYPE_OR_VAR_NAME:
                parseUserDefinedTypeOrVarName();
                break;
            case ASSIGNMENT_OR_VAR_DECL_STMT_RHS:
                parseAssignmentOrVarDeclRhs();
                break;
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
     */
    public void parse(ParserRuleContext context) {
        switch (context) {
            case COMP_UNIT:
                parseCompUnit();
                break;
            case TOP_LEVEL_NODE_WITH_MODIFIER:
            case TOP_LEVEL_NODE:
                startContext(ParserRuleContext.COMP_UNIT);
                parseTopLevelNodeWithModifier();
                break;
            case FUNC_DEFINITION:
                startContext(ParserRuleContext.COMP_UNIT);
                parseFunctionDefinition();
                break;
            case STATEMENT:
                startContext(ParserRuleContext.COMP_UNIT);
                startContext(ParserRuleContext.FUNC_DEFINITION);
                startContext(ParserRuleContext.FUNC_BODY_BLOCK);
                parseStatement();
                break;
            case EXPRESSION:
                startContext(ParserRuleContext.COMP_UNIT);
                startContext(ParserRuleContext.FUNC_DEFINITION);
                startContext(ParserRuleContext.FUNC_BODY_BLOCK);
                startContext(ParserRuleContext.STATEMENT);
                parseExpression();
                break;
            default:
                throw new UnsupportedOperationException("Cannot start parsing from: " + context);
        }
    }

    /**
     * Get the built tree.
     * 
     * @return STNode that represents the tree
     */
    public STNode getTree() {
        // TODO: have a proper mechanism to retrieve the parsed tree.
        return this.listner.getLastNode();
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

    private Solution recover(STToken token, ParserRuleContext currentCtx) {
        return this.errorHandler.recover(currentCtx, token);
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
     */
    private void parseCompUnit() {
        startContext(ParserRuleContext.COMP_UNIT);
        STToken token = peek();
        while (token.kind != SyntaxKind.EOF_TOKEN) {
            parseTopLevelNodeWithModifier(token.kind);
            token = peek();
        }

        this.listner.exitCompUnit(consume());
        endContext();
    }

    /**
     * Parse top level node having an optional modifier preceding it.
     */
    private void parseTopLevelNodeWithModifier() {
        STToken token = peek();
        parseTopLevelNodeWithModifier(token.kind);
    }

    /**
     * Parse top level node having an optional modifier preceding it, given the next token kind.
     * 
     * @param tokenKind Next token kind
     */
    private void parseTopLevelNodeWithModifier(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case PUBLIC_KEYWORD:
                parseModifier();
                parseTopLevelNode();
                return;
            case FUNCTION_KEYWORD:
            case TYPE_KEYWORD:
                this.listner.addEmptyModifier();
                parseTopLevelNode(tokenKind);
                return;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE_WITH_MODIFIER);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action == Action.INSERT) {
                    parseTopLevelNodeWithModifier(solution.tokenKind);
                }

                return;
        }
    }

    /**
     * Parse top level node.
     */
    private void parseTopLevelNode() {
        STToken token = peek();
        parseTopLevelNode(token.kind);
    }

    /**
     * Parse top level node given the next token kind.
     * 
     * @param tokenKind Next token kind
     */
    private void parseTopLevelNode(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case FUNCTION_KEYWORD:
                parseFunctionDefinition();
                break;
            case TYPE_KEYWORD:
                parseModuleTypeDefinition();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.TOP_LEVEL_NODE);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseTopLevelNode(solution.tokenKind);
        }
    }

    /**
     * Parse access modifiers.
     */
    private void parseModifier() {
        STToken token = peek();
        if (token.kind == SyntaxKind.PUBLIC_KEYWORD) {
            this.listner.exitModifier(consume()); // public keyword
        } else {
            recover(token, ParserRuleContext.PUBLIC);
        }
    }

    /**
     * <p>
     * Parse function definition. A function definition has the following structure.
     * </p>
     * <code>
     * function-defn := FUNCTION identifier function-signature function-body
     * </code>
     */
    private void parseFunctionDefinition() {
        startContext(ParserRuleContext.FUNC_DEFINITION);
        parseFunctionKeyword();
        parseFunctionName();
        parseFunctionSignature();
        parseFunctionBody();
        this.listner.exitFunctionDefinition();
        endContext();
    }

    /**
     * Parse function keyword. Need to validate the token before consuming,
     * since we can reach here while recovering.
     */
    private void parseFunctionKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.FUNCTION_KEYWORD) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.FUNCTION_KEYWORD);
        }
    }

    /**
     * Parse a syntax node. This method assumes the validity of the token
     * is checked prior to calling this method. Therfore this will not
     * do any validations.
     */
    private void parseSyntaxNode() {
        this.listner.exitSyntaxNode(consume());
    }

    /**
     * Parse function name.
     */
    private void parseFunctionName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            this.listner.exitIdentifier(consume()); // function name
        } else {
            recover(token, ParserRuleContext.FUNC_NAME);
        }
    }

    /**
     * <p>
     * Parse function signature. A function signature has the following structure.
     * </p>
     * <code>
     * function-signature := ( param-list ) return-type-descriptor
     * </code>
     */
    private void parseFunctionSignature() {
        startContext(ParserRuleContext.FUNC_SIGNATURE);
        parseOpenParenthesis();
        parseParamList();
        parseCloseParenthesis();
        parseReturnTypeDescriptor();
        this.listner.exitFunctionSignature();
        endContext();
    }

    /**
     * Parse open parenthesis.
     */
    private void parseOpenParenthesis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_PAREN_TOKEN) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.OPEN_PARENTHESIS);
        }
    }

    /**
     * Parse close parenthesis.
     */
    private void parseCloseParenthesis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_PAREN_TOKEN) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.CLOSE_PARENTHESIS);
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
     */
    private void parseParamList() {
        startContext(ParserRuleContext.PARAM_LIST);
        this.listner.startParamList();

        STToken token = peek();
        if (!isEndOfParametersList(token)) {
            // comma precedes the first parameter, which doesn't exist
            this.listner.addEmptyNode();
            this.currentParamKind = ParserRuleContext.REQUIRED_PARAM;
            parseParameters();
        }

        this.listner.exitParamList();
        endContext();
    }

    /**
     * Parse parameters, separated by commas.
     */
    private void parseParameters() {
        parseParameter();

        STToken token = peek();
        if (isEndOfParametersList(token)) {
            return;
        }

        parseFollowUpParameter();
    }

    /**
     * Parse a single parameter. Parameter can be a required parameter, a defaultable
     * parameter, or a rest parameter.
     */
    private void parseParameter() {
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

        parseTypeDescriptor(token.kind);
        parseAfterParamType();
        endContext();
    }

    private void parseAfterParamType() {
        STToken token = peek();
        parseAfterParamType(token.kind);
    }

    private void parseAfterParamType(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case ELLIPSIS_TOKEN:
                this.currentParamKind = ParserRuleContext.REST_PARAM;
                switchContext(ParserRuleContext.REST_PARAM);
                parseEllipsis();
                parseVariableName();
                this.listner.exitRestParameter();
                break;
            case IDENTIFIER_TOKEN:
                parseVariableName();
                parseParameterRhs();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.AFTER_PARAMETER_TYPE);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    return;
                }

                parseAfterParamType(solution.tokenKind);
                break;
        }
    }

    /**
     * Parse ellipsis.
     */
    private void parseEllipsis() {
        STToken token = peek();
        if (token.kind == SyntaxKind.ELLIPSIS_TOKEN) {
            parseSyntaxNode(); // parse '...'
        } else {
            recover(token, ParserRuleContext.ELLIPSIS);
        }
    }

    /**
     * <p>
     * Parse the right hand side of a required/defaultable parameter.
     * </p>
     * <code>parameter-rhs := [= expression]</code>
     */
    private void parseParameterRhs() {
        STToken token = peek();
        parseParameterRhs(token.kind);
    }

    /**
     * Parse the right hand side of a required/defaultable parameter, given the next token kind.
     * 
     * @param tokenKind Next token kind
     */
    private void parseParameterRhs(SyntaxKind tokenKind) {
        // Required parameters
        if (isEndOfParameter(tokenKind)) {
            this.listner.exitRequiredParameter();

            if (this.currentParamKind == ParserRuleContext.DEFAULTABLE_PARAM) {
                // This is an erroneous scenario, where a required parameters comes after
                // a defaulatble parameter. Log an error, and continue.

                // TODO: mark the node as erroneous
                // TODO: Fix the line number in the error
                this.errorHandler.reportInvalidNode(peek(),
                        "cannot have a required parameter after a defaultable parameter");
            }
            return;
        } else if (tokenKind == SyntaxKind.EQUAL_TOKEN) {

            // If we were processing required params so far and found a defualtable
            // parameter, then switch the context to defaultable params.
            if (this.currentParamKind == ParserRuleContext.REQUIRED_PARAM) {
                this.currentParamKind = ParserRuleContext.DEFAULTABLE_PARAM;
                switchContext(ParserRuleContext.DEFAULTABLE_PARAM);
            }

            // Defaultable parameters
            parseAssignOp();
            parseExpression();
            this.listner.exitDefaultableParameter();
        } else {
            STToken token = peek();
            Solution solution = recover(token, ParserRuleContext.PARAMETER_RHS);

            // If the parser recovered by inserting a token, then try to re-parse the same
            // rule with the inserted token. This is done to pick the correct branch
            // to continue the parsing.
            if (solution.action != Action.INSERT) {
                return;
            }

            parseParameterRhs(solution.tokenKind);
        }
    }

    /**
     * Parse a parameter that follows another parameter.
     */
    private void parseFollowUpParameter() {
        STToken token = peek();
        if (token.kind == SyntaxKind.COMMA_TOKEN) {
            parsePreValidatedComma();
        } else {
            Solution solution = recover(token, ParserRuleContext.FOLLOW_UP_PARAM);

            // If the current rule was recovered by removing a token,
            // then this entire rule is already parsed while recovering.
            // so we done need to parse the remaining of this rule again.
            // Proceed only if the recovery action was an insertion.
            if (solution.action == Action.REMOVE) {
                return;
            }
        }

        parseParameters();
    }

    /**
     * Parse comma.
     */
    private void parsePreValidatedComma() {
        parseSyntaxNode();
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
     * @param token Next token kind
     * @return <code>true</code> if the token represents an end of a parameter-list. <code>false</code> otherwise
     */
    private boolean isEndOfParametersList(STToken token) {
        switch (token.kind) {
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
     */
    private void parseReturnTypeDescriptor() {
        startContext(ParserRuleContext.RETURN_TYPE_DESCRIPTOR);

        // If the return type is not present, simply return
        STToken token = peek();
        if (token.kind == SyntaxKind.RETURNS_KEYWORD) {
            parseSyntaxNode();
        } else {
            this.listner.addEmptyNode();
            return;
        }

        parseAnnotations();

        parseTypeDescriptor();

        this.listner.exitReturnTypeDescriptor();
        endContext();
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
     */
    private void parseTypeDescriptor() {
        STToken token = peek();
        parseTypeDescriptor(token.kind);
    }

    /**
     * <p>
     * Parse a type descriptor, given the next token kind.
     * </p>
     * 
     * @param tokenKind Next token kind
     */
    private void parseTypeDescriptor(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case TYPE_TOKEN:
            case IDENTIFIER_TOKEN:
                // simple type descriptor
                this.listner.exitTypeDescriptor(consume()); // type descriptor
                break;
            case RECORD_KEYWORD:
                // Record type descriptor
                parseRecordTypeDescriptor();
                break;
            default:
                recover(peek(), ParserRuleContext.TYPE_DESCRIPTOR);
        }

        // TODO: only supports built-in/user-defined types. Add others.
    }

    /**
     * Parse annotation attachments.
     */
    private void parseAnnotations() {
        // TODO
        this.listner.exitAnnotations();
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
     */
    private void parseFunctionBody() {
        STToken token = peek();
        parseFunctionBody(token.kind);
    }

    /**
     * Parse function body, given the next token kind.
     * 
     * @param tokenKind Next token kind
     */
    private void parseFunctionBody(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EQUAL_TOKEN:
                parseExternalFunctionBody();
                break;
            case OPEN_BRACE_TOKEN:
                parseFunctionBodyBlock();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FUNC_BODY);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.

                if (solution.action != Action.INSERT) {
                    break;
                }

                // If the recovered token is not something that can be re-parsed,
                // then don't try to re-parse the same rule.
                if (solution.tokenKind == SyntaxKind.NONE) {
                    break;
                }

                parseFunctionBody(solution.tokenKind);
        }

        this.listner.exitFunctionBody();
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
     */
    private void parseFunctionBodyBlock() {
        startContext(ParserRuleContext.FUNC_BODY_BLOCK);
        parseOpenBrace();
        parseStatements(); // TODO: allow workers
        parseCloseBrace();
        this.listner.exitFunctionBodyBlock();
        endContext();
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
     */
    private void parseUserDefinedTypeOrVarName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            this.listner.exitIdentifier(consume()); // type or variable name
            return;
        } else {
            recover(token, ParserRuleContext.TYPE_OR_VAR_NAME);
        }
    }

    /**
     * Parse variable name.
     */
    private void parseVariableName() {
        STToken token = peek();
        parseVariableName(token.kind);
    }

    /**
     * Parse variable name.
     */
    private void parseVariableName(SyntaxKind tokenKind) {
        if (tokenKind == SyntaxKind.IDENTIFIER_TOKEN) {
            this.listner.exitIdentifier(consume()); // variable name
            return;
        } else {
            recover(peek(), ParserRuleContext.VARIABLE_NAME);
        }
    }

    /**
     * Parse open brace.
     */
    private void parseOpenBrace() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACE_TOKEN) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.OPEN_BRACE);
        }
    }

    /**
     * Parse close brace.
     */
    private void parseCloseBrace() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACE_TOKEN) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.CLOSE_BRACE);
        }
    }

    /**
     * <p>
     * Parse external function body. An external function body has the following structure.
     * </p>
     * <code>
     * external-function-body := = annots external ;
     * </code>
     */
    private void parseExternalFunctionBody() {
        startContext(ParserRuleContext.EXTERNAL_FUNC_BODY);
        parseAssignOp();
        parseAnnotations();
        parseExternalKeyword();
        parseSemicolon();
        this.listner.exitExternalFunctionBody();
        endContext();
    }

    /**
     * Parse semicolon.
     */
    private void parseSemicolon() {
        STToken token = peek();
        if (token.kind == SyntaxKind.SEMICOLON_TOKEN) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.SEMICOLON);
        }
    }

    /**
     * Parse <code>external</code> keyword.
     */
    private void parseExternalKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.EXTERNAL_KEYWORD) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.EXTERNAL_KEYWORD);
        }
    }

    /*
     * Operators
     */

    /**
     * Parse assign operator.
     */
    private void parseAssignOp() {
        STToken token = peek();
        if (token.kind == SyntaxKind.EQUAL_TOKEN) {
            this.listner.exitOperator(consume()); // =
        } else {
            recover(token, ParserRuleContext.ASSIGN_OP);
        }
    }

    /**
     * Parse binary operator.
     */
    private void parseBinaryOperator() {
        STToken token = peek();
        if (isBinaryOperator(token.kind)) {
            this.listner.exitOperator(consume()); // binary operator
        } else {
            recover(token, ParserRuleContext.BINARY_OPERATOR);
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
     * @return
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
     */
    private void parseModuleTypeDefinition() {
        startContext(ParserRuleContext.TYPE_DEFINITION);
        parseTypeKeyword();
        parseTypeName();
        parseTypeDescriptor();
        parseSemicolon();
        this.listner.exitModuleTypeDefinition();
        endContext();
    }

    /**
     * Parse type keyword.
     */
    private void parseTypeKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.TYPE_KEYWORD) {
            this.listner.exitSyntaxNode(consume()); // 'type' keyword
        } else {
            recover(token, ParserRuleContext.FUNC_NAME);
        }
    }

    /**
     * Parse type name.
     */
    private void parseTypeName() {
        STToken token = peek();
        if (token.kind == SyntaxKind.IDENTIFIER_TOKEN) {
            this.listner.exitIdentifier(consume()); // type name
        } else {
            recover(token, ParserRuleContext.FUNC_NAME);
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
     */
    private void parseRecordTypeDescriptor() {
        parseRecordKeyword();
        parseRecordBodyStartDelimiter();
        parseFieldDescriptors();
        parseRecordBodyCloseDelimiter();
        this.listner.exitRecordTypeDescriptor();
    }

    /**
     * 
     */
    private void parseRecordBodyStartDelimiter() {
        STToken token = peek();
        parseRecordBodyStartDelimiter(token.kind);
    }

    private void parseRecordBodyStartDelimiter(SyntaxKind kind) {
        switch (kind) {
            case OPEN_BRACE_PIPE_TOKEN:
                parseClosedRecordBodyStart();
                break;
            case OPEN_BRACE_TOKEN:
                parseOpenBrace();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RECORD_BODY_START);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseRecordBodyStartDelimiter(solution.tokenKind);
                return;
        }
    }

    /**
     * 
     */
    private void parseClosedRecordBodyStart() {
        STToken token = peek();
        if (token.kind == SyntaxKind.OPEN_BRACE_PIPE_TOKEN) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.CLOSED_RECORD_BODY_START);
        }
    }

    /**
     * 
     */
    private void parseRecordBodyCloseDelimiter() {
        STToken token = peek();
        parseRecordBodyCloseDelimiter(token.kind);
    }

    /**
     * 
     */
    private void parseRecordBodyCloseDelimiter(SyntaxKind kind) {
        switch (kind) {
            case CLOSE_BRACE_PIPE_TOKEN:
                parseClosedRecordBodyEnd();
                break;
            case CLOSE_BRACE_TOKEN:
                parseCloseBrace();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.RECORD_BODY_END);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseRecordBodyStartDelimiter(solution.tokenKind);
                return;
        }
    }

    /**
     * 
     */
    private void parseClosedRecordBodyEnd() {
        STToken token = peek();
        if (token.kind == SyntaxKind.CLOSE_BRACE_PIPE_TOKEN) {
            parseSyntaxNode();
        } else {
            recover(token, ParserRuleContext.CLOSED_RECORD_BODY_END);
        }
    }

    /**
     * Parse record keyword.
     */
    private void parseRecordKeyword() {
        STToken token = peek();
        if (token.kind == SyntaxKind.RECORD_KEYWORD) {
            this.listner.exitSyntaxNode(consume()); // 'record' keyword
        } else {
            recover(token, ParserRuleContext.RECORD_KEYWORD);
        }
    }

    /**
     * <p>
     * Parse field descriptors.
     * </p>
     */
    private void parseFieldDescriptors() {
        this.listner.startFieldDescriptors();

        STToken token = peek();
        ParserRuleContext currentFieldKind = ParserRuleContext.RECORD_FIELD;
        this.currentFieldKindStack.push(currentFieldKind);

        while (!isEndOfBlock(token.kind) && currentFieldKind == ParserRuleContext.RECORD_FIELD) {
            parseFieldOrRestDescriptor();
            token = peek();
            currentFieldKind = this.currentFieldKindStack.peek();
        }

        if (currentFieldKind == ParserRuleContext.RECORD_FIELD) {
            // Last processed field is a record-field. That means no rest-field exists.
            // Therefore add an empty node.
            this.listner.addEmptyNode();
        } else {
            // Else, record rest-field is processed. Then there cannot be anymore record-fields.
            // If there's any, then process them normally, but log an error.
            while (!isEndOfBlock(token.kind)) {
                parseFieldOrRestDescriptor();
                this.errorHandler.reportInvalidNode(token, "cannot have more fields after the rest type descriptor");
                // TODO: discard the last processed field
                token = peek();
            }
        }

        this.currentFieldKindStack.pop();
        this.listner.exitFieldDescriptors();
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
     */
    private void parseFieldOrRestDescriptor() {
        STToken token = peek();
        // record-type-reference
        if (token.kind == SyntaxKind.ASTERISK_TOKEN) {
            this.listner.exitSyntaxNode(consume()); // '*' token
            parseTypeDescriptor();
            parseSemicolon();
            this.listner.exitRecordTypeReference();
            return;
        }

        // individual-field-descriptor
        parseTypeDescriptor();
        parseFieldOrRestDescriptorRhs();
    }

    /**
     * 
     */
    private void parseFieldOrRestDescriptorRhs() {
        STToken token = peek();
        parseFieldOrRestDescriptorRhs(token.kind);
    }

    /**
     * 
     */
    private void parseFieldOrRestDescriptorRhs(SyntaxKind kind) {
        switch (kind) {
            case ELLIPSIS_TOKEN:
                parseEllipsis();
                parseSemicolon();
                this.listner.exitRecordRestDescriptor();
                this.currentFieldKindStack.pop();
                this.currentFieldKindStack.push(ParserRuleContext.RECORD_REST_FIELD);
                break;
            case IDENTIFIER_TOKEN:
                parseVariableName();
                parseFieldDescriptorRhs();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FIELD_OR_REST_DESCIPTOR_RHS);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseFieldOrRestDescriptorRhs(solution.tokenKind);
                return;
        }
    }

    /**
     * <p>
     * Parse field descriptor rhs.
     * </p>
     */
    private void parseFieldDescriptorRhs() {
        STToken token = peek();
        parseFieldDescriptorRhs(token.kind);
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
     */
    private void parseFieldDescriptorRhs(SyntaxKind kind) {
        switch (kind) {
            case SEMICOLON_TOKEN:
                this.listner.addEmptyNode();
                parseSemicolon();
                this.listner.exitRecordField();
                break;
            case QUESTION_MARK_TOKEN:
                parseQuestionMark();
                parseSemicolon();
                this.listner.exitRecordField();
                break;
            case EQUAL_TOKEN:
                parseRecordDefaultValue();
                this.listner.exitRecordFieldWithDefaultValue();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.FIELD_DESCRIPTOR_RHS);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseFieldDescriptorRhs(solution.tokenKind);
                break;
        }
    }

    /**
     * Parse question mark.
     */
    private void parseQuestionMark() {
        STToken token = peek();
        if (token.kind == SyntaxKind.QUESTION_MARK_TOKEN) {
            this.listner.exitSyntaxNode(consume()); // '?' token
        } else {
            recover(token, ParserRuleContext.QUESTION_MARK);
        }
    }

    /**
     * <p>
     * Parse record field default value.
     * </p>
     * 
     * <code>
     * <br/>default-value := = expression
     * </code>
     */
    private void parseRecordDefaultValue() {
        parseAssignOp();
        parseExpression();
    }

    /*
     * Statements
     */

    /**
     * Parse statements, until an end of a block is reached.
     */
    private void parseStatements() {
        // TODO: parse statements/worker declrs
        STToken token = peek();
        while (!isEndOfBlock(token.kind)) {
            parseStatement();
            token = peek();
        }
    }

    /**
     * Parse a single statement.
     */
    private void parseStatement() {
        STToken token = peek();
        parseStatement(token.kind);
    }

    /**
     * Parse a single statement, given the next token kind.
     * 
     * @param tokenKind Next tokenKind
     */
    private void parseStatement(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case TYPE_TOKEN:
                // If the statement starts with a built-in type, then its a var declaration.
                // This is an optimization since if we know the next token is a type, then
                // we can parse the var-def faster.
                parseVariableDeclStmt();
                break;
            case IDENTIFIER_TOKEN:
                // If the statement starts with an identifier, it could be either an assignment
                // statement or a var-decl-stmt with a user defined type.
                parseAssignmentOrVarDecl();
                break;
            default:
                // If the next token in the token stream does not match to any of the statements and
                // if it is not the end of statement, then try to fix it and continue.
                if (isEndOfBlock(tokenKind)) {
                    break;
                }

                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.STATEMENT);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseStatement(solution.tokenKind);
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
     */
    private void parseVariableDeclStmt() {
        startContext(ParserRuleContext.VAR_DECL_STMT);
        parseTypeDescriptor();
        parseVariableName();
        parseVarDeclRhs();
        endContext();
    }

    /**
     * <p>
     * Parse the right hand side of a variable declaration statement.
     * </p>
     * <code>
     * var-decl-rhs := ; | = action-or-expr ;
     * </code>
     */
    private void parseVarDeclRhs() {
        STToken token = peek();
        parseVarDeclRhs(token.kind);
    }

    /**
     * Parse the right hand side of a variable declaration statement, given the
     * next token kind.
     * 
     * @param tokenKind Next token kind
     */
    private void parseVarDeclRhs(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case EQUAL_TOKEN:
                parseAssignOp();
                parseExpression();
                parseSemicolon();
                this.listner.exitVarDefStmt(true);
                break;
            case SEMICOLON_TOKEN:
                parseSemicolon();
                this.listner.exitVarDefStmt(false);
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.VAR_DECL_STMT_RHS);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseVarDeclRhs(solution.tokenKind);
        }
    }

    /**
     * If the statement starts with an identifier, it could be either an assignment statement or
     * a var-decl-stmt with a user defined type. This method will parse such ambiguous scenarios.
     */
    private void parseAssignmentOrVarDecl() {
        startContext(ParserRuleContext.ASSIGNMENT_OR_VAR_DECL_STMT_RHS);
        parseUserDefinedTypeOrVarName();
        parseAssignmentOrVarDeclRhs();
        endContext();
    }

    /**
     * Parse the second portion of an assignment statement or a var-decl statement when ambiguous.
     */
    private void parseAssignmentOrVarDeclRhs() {
        STToken token = peek();
        parseAssignmentOrVarDeclRhs(token.kind);
    }

    /**
     * Parse the second portion of an assignment statement or a var-decl statement when ambiguous,
     * given the next token kind.
     * 
     * @param tokenKind Next token kind
     */
    private void parseAssignmentOrVarDeclRhs(SyntaxKind tokenKind) {
        switch (tokenKind) {
            case IDENTIFIER_TOKEN:
                parseVariableName();
                parseVarDeclRhs();
                break;
            case EQUAL_TOKEN:
                parseAssignmentStmtRhs();
                break;
            default:
                STToken token = peek();
                Solution solution = recover(token, ParserRuleContext.ASSIGNMENT_OR_VAR_DECL_STMT_RHS);

                // If the parser recovered by inserting a token, then try to re-parse the same
                // rule with the inserted token. This is done to pick the correct branch
                // to continue the parsing.
                if (solution.action != Action.INSERT) {
                    break;
                }

                parseAssignmentOrVarDeclRhs(solution.tokenKind);
        }
    }

    /**
     * <p>
     * Parse assignment statement, which takes the following format.
     * </p>
     * <code>assignment-stmt := lvexpr = action-or-expr ;</code>
     */
    private void parseAssignmentStmt() {
        startContext(ParserRuleContext.ASSIGNMENT_STMT);
        parseVariableName();
        parseAssignmentStmtRhs();
        this.listner.exitAssignmentStmt();
        endContext();
    }

    /**
     * <p>
     * Parse the RHS portion of the assignment.
     * </p>
     * <code>assignment-stmt-rhs := = action-or-expr ;</code>
     */
    private void parseAssignmentStmtRhs() {
        parseAssignOp();
        parseExpression();
        parseSemicolon();
        this.listner.exitAssignmentStmt();
    }

    /*
     * Expressions
     */

    /**
     * Parse expression. This will start parsing expressions from the lowest level of precedence.
     */
    private void parseExpression() {
        parseExpression(OperatorPrecedence.BINARY_COMPARE);
    }

    /**
     * Parse an expression that has an equal or higher precedence than a given level.
     * 
     * @param precedenceLevel Precedence level of expression to be parsed
     */
    private void parseExpression(OperatorPrecedence precedenceLevel) {
        parseTerminalExpression();
        parseBinaryExprRhs(precedenceLevel);
    }

    /**
     * Parse terminal expressions. A terminal expression has the highest precedence level
     * out of all expressions, and will be at the leaves of an expression tree.
     */
    private void parseTerminalExpression() {
        STToken token = peek();
        switch (token.kind) {
            case NUMERIC_LITERAL_TOKEN:
                parseLiteral();
                break;
            case IDENTIFIER_TOKEN:
                parseVariableName();
                break;
            case OPEN_PAREN_TOKEN:
                parseBracedExpression();
                break;
            default:
                recover(token, ParserRuleContext.EXPRESSION);
                break;
        }
    }

    /**
     * Parse the right-hand-side of a binary expression.
     */
    private void parseBinaryExprRhs() {
        parseBinaryExprRhs(OperatorPrecedence.BINARY_COMPARE);
    }

    /**
     * <p>
     * Parse the right-hand-side of a binary expression.
     * </p>
     * <code>binary-expr-rhs := (binary-op expression)*</code>
     * 
     * @param precedenceLevel Precedence level of the expression that is being parsed currently
     */
    private void parseBinaryExprRhs(OperatorPrecedence precedenceLevel) {
        STToken token = peek();
        parseBinaryExprRhs(precedenceLevel, token.kind);
    }

    /**
     * Parse the right hand side of a binary expression given the next token kind.
     * 
     * @param precedenceLevel Precedence level of the expression that is being parsed currently
     * @param tokenKind Next token kind
     */
    private void parseBinaryExprRhs(OperatorPrecedence precedenceLevel, SyntaxKind tokenKind) {
        if (isEndOfExpression(tokenKind)) {
            return;
        }

        if (!isBinaryOperator(tokenKind)) {
            STToken token = peek();
            Solution solution = recover(token, ParserRuleContext.BINARY_EXPR_RHS);

            // If the current rule was recovered by removing a token,
            // then this entire rule is already parsed while recovering.
            // so we done need to parse the remaining of this rule again.
            // Proceed only if the recovery action was an insertion.
            if (solution.action == Action.REMOVE) {
                return;
            }

            // If the parser recovered by inserting a token, then try to re-parse the same
            // rule with the inserted token. This is done to pick the correct branch to
            // continue the parsing.
            if (solution.ctx == ParserRuleContext.BINARY_OPERATOR) {
                // We come here if the operator is missing. Treat this as injecting an operator
                // that matches to the current operator precedence level, and continue.
                SyntaxKind binaryOpKind = getOperatorKindToInsert(precedenceLevel);
                parseBinaryExprRhs(precedenceLevel, binaryOpKind);
            } else {
                parseBinaryExprRhs(precedenceLevel, solution.tokenKind);
            }

            return;
        }

        // If the precedence level of the operator that was being parsed is higher than
        // the newly found (next) operator, then return and finish the previous expr,
        // because it has a higher precedence.
        OperatorPrecedence operatorPrecedence = getOpPrecedence(tokenKind);
        if (precedenceLevel.isHigherThan(operatorPrecedence)) {
            return;
        }

        parseBinaryOperator();

        // Parse the expression that follows the binary operator, until a operator
        // with different precedence is encountered. If an operator with a lower
        // precedence is reached, then come back here and finish the current
        // binary expr. If a an operator with higher precedence level is reached,
        // then complete that binary-expr, come back here and finish the current
        // expr.
        parseExpression(operatorPrecedence);
        this.listner.endBinaryExpression();

        // Then continue the operators with the same precedence level.
        parseBinaryExprRhs(precedenceLevel);
    }

    /**
     * <p>
     * Parse braced expression.
     * </p>
     * <code>braced-expr := ( expression )</code>
     */
    private void parseBracedExpression() {
        parseOpenParenthesis();
        parseExpression();
        parseCloseParenthesis();
        this.listner.endBracedExpression();
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
     */
    private void parseLiteral() {
        this.listner.exitLiteral(consume()); // literal
    }
}
