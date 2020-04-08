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
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A factory for creating nodes in the syntax tree.
 *
 * @since 1.3.0
 */
public class NodeFactory {

    private NodeFactory() {
    }

    public static ModulePart createModulePart(NodeList<ImportDeclaration> imports,
                                              NodeList<ModuleMemberDeclaration> members,
                                              Token eofToken) {
        Objects.requireNonNull(imports, "imports must not be null");
        Objects.requireNonNull(members, "members must not be null");
        Objects.requireNonNull(eofToken, "eofToken must not be null");
        STNode stModulePart = STNodeFactory.createModulePart(imports.underlyingListNode().internalNode(),
                members.underlyingListNode().internalNode(), eofToken.internalNode());
        return stModulePart.createUnlinkedFacade();
    }

    public static FunctionDefinitionNode createFunctionDefinitionNode(Token visibilityQualifier,
                                                                      Token functionKeyword,
                                                                      Identifier functionName,
                                                                      Token openParenToken,
                                                                      NodeList<Parameter> parameters,
                                                                      Token closeParenToken,
                                                                      Node returnTypeDesc,
                                                                      BlockStatement functionBody) {
        Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
        Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
        Objects.requireNonNull(functionName, "functionName must not be null");
        Objects.requireNonNull(openParenToken, "openParenToken must not be null");
        Objects.requireNonNull(parameters, "parameters must not be null");
        Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
        Objects.requireNonNull(returnTypeDesc, "returnTypeDesc must not be null");
        Objects.requireNonNull(functionBody, "functionBody must not be null");
        STNode stFuncDefNode = STNodeFactory.createFunctionDefinition(visibilityQualifier.internalNode(),
                functionKeyword.internalNode(), functionName.internalNode(), openParenToken.internalNode(),
                parameters.underlyingListNode().internalNode(), closeParenToken.internalNode(),
                returnTypeDesc.internalNode(), functionBody.internalNode());
        return stFuncDefNode.createUnlinkedFacade();
    }

    public static BlockStatement createBlockStatement(Token openBraceToken,
                                                      NodeList<Statement> statements,
                                                      Token closeBraceToken) {
        Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
        Objects.requireNonNull(statements, "statements must not be null");
        Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
        STNode stBlockNode = STNodeFactory.createBlockStatement(openBraceToken.internalNode(),
                statements.underlyingListNode().internalNode(), closeBraceToken.internalNode());
        return stBlockNode.createUnlinkedFacade();
    }

    public static AssignmentStatement createAssignmentStatement(Token variableName, Token equalsToken,
                                                                Node expression, Token semicolonToken) {
        Objects.requireNonNull(variableName, "variableName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
        STNode stAssignNode = STNodeFactory.createAssignmentStatement(SyntaxKind.ASSIGNMENT_STATEMENT,
                variableName.internalNode(), equalsToken.internalNode(), expression.internalNode(),
                semicolonToken.internalNode());
        return stAssignNode.createUnlinkedFacade();
    }

    public static VariableDeclaration createVariableDeclaration(Token finalKeyword,
                                                                Node typeName,
                                                                Identifier variableName,
                                                                Token equalsToken,
                                                                Node initializer,
                                                                Token semicolonToken) {
        Objects.requireNonNull(finalKeyword, "finalKeyword must not be null");
        Objects.requireNonNull(typeName, "typeName must not be null");
        Objects.requireNonNull(equalsToken, "equalsToken must not be null");
        Objects.requireNonNull(initializer, "initializer must not be null");
        Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
        STNode stVarDecl = STNodeFactory.createVariableDeclaration(SyntaxKind.VARIABLE_DECL,
                finalKeyword.internalNode(), typeName.internalNode(), variableName.internalNode,
                equalsToken.internalNode(), initializer.internalNode(), semicolonToken.internalNode());
        return stVarDecl.createUnlinkedFacade();
    }

    public static Identifier createIdentifier(String text) {
        STToken token = STNodeFactory.createIdentifier(text, STNodeFactory.createNodeList(new ArrayList<>()),
                STNodeFactory.createNodeList(new ArrayList<>()));
        return token.createUnlinkedFacade();
    }
}


// TODO Need a way to create a new tree by updating a single token or a node.
