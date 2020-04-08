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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeFactory;

import java.util.Arrays;

/**
 * Produces a new tree by doing a depth-first traversal of the tree.
 *
 * @since 1.3.0
 */
public abstract class SyntaxTreeModifier extends SyntaxNodeTransformer<Node> {

    @Override
    public Node transform(ModulePart modulePart) {
        NodeList<ImportDeclaration> imports = modifyNodeList(modulePart.imports());
        NodeList<ModuleMemberDeclaration> members = modifyNodeList(modulePart.members());
        Token eofToken = modifyToken(modulePart.eofToken());
        return modulePart.modify(imports, members, eofToken);
    }

    @Override
    public Node transform(FunctionDefinitionNode functionDefNode) {
        Token visibilityQualifier = modifyToken(functionDefNode.visibilityQualifier());
        Token funcKeyword = modifyToken(functionDefNode.functionKeyword());
        Identifier funcName = modifyToken(functionDefNode.functionName());
        Token openParenToken = modifyToken(functionDefNode.openParenToken());
        NodeList<Parameter> parameters = modifyNodeList(functionDefNode.parameters());
        Token closeParenToken = modifyToken(functionDefNode.closeParenToken());
        Node returnTypeDesc = modifyNode(functionDefNode.returnTypeDesc());
        BlockStatement body = modifyNode(functionDefNode.functionBody());
        return functionDefNode.modify(visibilityQualifier, funcKeyword, funcName,
                openParenToken, parameters, closeParenToken, returnTypeDesc, body);
    }

    // Statements

    @Override
    public Node transform(AssignmentStatement assignmentStmt) {
        Token varName = modifyToken(assignmentStmt.variableName());
        Token equalsToken = modifyToken(assignmentStmt.equalsToken());
        Node expr = modifyNode(assignmentStmt.expression());
        Token semicolonToken = modifyToken(assignmentStmt.semicolonToken());
        return assignmentStmt.modify(varName, equalsToken, expr, semicolonToken);
    }

    @Override
    public Node transform(VariableDeclaration localVarDecl) {
        Token finalKeyword = modifyToken(localVarDecl.finalKeyword());
        Node typeName = modifyNode(localVarDecl.typeName());
        Identifier variableName = modifyToken(localVarDecl.variableName());
        Token equalsToken = modifyToken(localVarDecl.equalsToken());
        Node initializer = modifyNode(localVarDecl.initializer());
        Token semicolonToken = modifyToken(localVarDecl.semicolonToken());
        return localVarDecl.modify(finalKeyword, typeName, variableName,
                equalsToken, initializer, semicolonToken);
    }

    @Override
    public Node transform(BlockStatement blockStmt) {
        Token openBraceToken = modifyToken(blockStmt.openBraceToken());
        NodeList<Statement> stmts = modifyNodeList(blockStmt.statements());
        Token closeBraceToken = modifyToken(blockStmt.closeBraceToken());
        return blockStmt.modify(openBraceToken, stmts, closeBraceToken);
    }

    // Tokens

    @Override
    public Node transform(Token token) {
        return token;
    }

    @Override
    public Node transform(Identifier identifier) {
        return identifier;
    }

    @Override
    public Node transform(EmptyToken emptyToken) {
        return emptyToken;
    }

    @Override
    protected Node transformSyntaxNode(Node node) {
        return node;
    }

    protected <T extends Node> NodeList<T> modifyNodeList(NodeList<T> nodeList) {
        if (nodeList.isEmpty()) {
            return nodeList;
        }

        boolean nodeModified = false;
        STNode[] newSTNodes = new STNode[nodeList.size()];
        for (int index = 0; index < nodeList.size(); index++) {
            T oldNode = nodeList.get(index);
            T newNode = modifyNode(oldNode);
            if (oldNode != newNode) {
                nodeModified = true;
            }
            newSTNodes[index] = newNode.internalNode();
        }

        if (!nodeModified) {
            return nodeList;
        }

        STNode stNodeList = STNodeFactory.createNodeList(Arrays.asList(newSTNodes));
        return new NodeList<>(stNodeList.createUnlinkedFacade());
    }

    protected <T extends Token> T modifyToken(T token) {
        // TODO
        return (T) token.apply(this);
    }

    protected <T extends Node> T modifyNode(T node) {
        // TODO
        return (T) node.apply(this);
    }
}
