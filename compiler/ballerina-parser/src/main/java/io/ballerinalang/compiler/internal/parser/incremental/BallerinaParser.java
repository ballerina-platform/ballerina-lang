/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.internal.parser.incremental;

import io.ballerinalang.compiler.internal.parser.tree.STModulePart;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.internal.parser.tree.STBlockStatement;
import io.ballerinalang.compiler.internal.parser.tree.STExpression;
import io.ballerinalang.compiler.internal.parser.tree.STFunctionDefinition;
import io.ballerinalang.compiler.internal.parser.tree.STVariableDeclaration;
import io.ballerinalang.compiler.internal.parser.tree.STStatement;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.text.TextDocument;

import java.util.ArrayList;
import java.util.List;

public class BallerinaParser {

    private final NodeAndTokenProvider tokenIssuer;

    public BallerinaParser(TextDocument textDocument) {
        this.tokenIssuer = new NonIncrementalTokenProvider(textDocument);
    }

    // TODO Should we pass the SyntaxTree for ModulePart here
    public BallerinaParser(SyntaxTree oldTree, TextDocument newTextDocument, TextDocumentChange textDocumentChange) {
        this.tokenIssuer = new IncrementalTokenProvider(oldTree, newTextDocument, textDocumentChange);
    }

    public STModulePart parserModulePart() {
        STNode imports = parseImportDecls();
        STNode members = parseModuleMemberDecls();
        STToken eofToken = tokenIssuer.consumeToken(SyntaxKind.EOF_TOKEN);
        return new STModulePart(imports, members, eofToken);
    }

    private STNode parseImportDecls() {
        return STNodeList.emptyNodeList;
    }

    private STNode parseModuleMemberDecls() {
        List<STNode> memberList = new ArrayList<>();
        // TODO Parse metadata
        while (true) {

            STToken visibilityQual = parseVisibilityQualifier();
            switch (tokenIssuer.getCurrentToken().kind) {
                case LISTENER_KEYWORD:
                    break;
                case SERVICE_KEYWORD:
                    break;
                case FUNCTION_KEYWORD:
                    memberList.add(parseFunctionDefinition(visibilityQual));
                    break;
                case TYPE_KEYWORD:
                    break;
                case CONST_KEYWORD:
                    break;
                case XMLNS_KEYWORD:
                    break;
                case ANNOTATION_KEYWORD:
                    break;
                case EOF_TOKEN:
                    if (memberList.isEmpty()) {
                        return STNodeList.emptyNodeList;
                    }
                    return new STNodeList(memberList);
                default:
                    // TODO parse module level variable declaration
            }
        }
    }

    private STToken parseVisibilityQualifier() {
        switch (tokenIssuer.getCurrentToken().kind) {
            case PUBLIC_KEYWORD:
                return tokenIssuer.consumeToken(SyntaxKind.PUBLIC_KEYWORD);
            case PRIVATE_KEYWORD:
                return tokenIssuer.consumeToken(SyntaxKind.PRIVATE_KEYWORD);
        }
        return null;
    }

    private STNode parseFunctionDefinition(STToken visibilityQual) {
        STToken funcKeyword = tokenIssuer.consumeToken(SyntaxKind.FUNCTION_KEYWORD);
        STToken funcNameToken = tokenIssuer.consumeToken(SyntaxKind.IDENTIFIER_TOKEN);
        STToken openBraceToken = tokenIssuer.consumeToken(SyntaxKind.OPEN_PAREN_TOKEN);
        STToken closeBraceToken = tokenIssuer.consumeToken(SyntaxKind.CLOSE_PAREN_TOKEN);
        STBlockStatement body = this.parseFunctionBody();
        return new STFunctionDefinition(visibilityQual, funcKeyword, funcNameToken, openBraceToken, null,
                closeBraceToken, null, body);
    }

    private STBlockStatement parseFunctionBody() {
        STToken openBraceToken = tokenIssuer.consumeToken(SyntaxKind.OPEN_BRACE_TOKEN);
        List<STNode> statements = new ArrayList<>();
        while (tokenIssuer.getCurrentToken().kind != SyntaxKind.CLOSE_BRACE_TOKEN) {
            statements.add(parseStatement());
        }

        STToken closeBraceToken = tokenIssuer.consumeToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        return new STBlockStatement(SyntaxKind.BLOCK_STATEMENT, openBraceToken,
                new STNodeList(statements), closeBraceToken);
    }

    // this parses only local variable declarations.  int x; or int x = e;
    private STStatement parseStatement() {

        STToken typeName = tokenIssuer.consumeToken(SyntaxKind.IDENTIFIER_TOKEN);
        STToken varName = tokenIssuer.consumeToken(SyntaxKind.IDENTIFIER_TOKEN);
        STToken equalsToken = null;
        STExpression initializer = null;
        if (tokenIssuer.getCurrentToken().kind != SyntaxKind.SEMICOLON_TOKEN) {
            equalsToken = tokenIssuer.consumeToken(SyntaxKind.EQUAL_TOKEN);
            initializer = this.parseExpression();
        }

        STToken semicolonToken = tokenIssuer.consumeToken(SyntaxKind.SEMICOLON_TOKEN);
        return new STVariableDeclaration(SyntaxKind.LOCAL_VARIABLE_DECL, typeName,
                varName, equalsToken, initializer, semicolonToken);
    }

    private STExpression parseExpression() {

        while (tokenIssuer.getCurrentToken().kind != SyntaxKind.SEMICOLON_TOKEN) {
            switch (tokenIssuer.getCurrentToken().kind) {
            }
        }

        return null;
    }
}
