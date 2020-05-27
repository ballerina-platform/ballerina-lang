/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler.format;

import io.ballerinalang.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerinalang.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionBodyNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.syntax.tree.IdentifierToken;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NodeFactory;
import io.ballerinalang.compiler.syntax.tree.NodeList;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.StatementNode;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.syntax.tree.TreeModifier;

/**
 * Modifies the given tree to format the nodes.
 *
 * @since 2.0.0
 */
public class FormattingTreeModifier extends TreeModifier {
    @Override
    public FunctionDefinitionNode transform(FunctionDefinitionNode functionDefinitionNode) {
        Token functionKeyword = getToken(functionDefinitionNode.functionKeyword());
        Token functionName = getToken(functionDefinitionNode.functionName());

        FunctionSignatureNode functionSignatureNode = functionDefinitionNode.functionSignature();
        Token functionSignatureOpenPara = getToken(functionSignatureNode.openParenToken());
        Token functionSignatureClosePara = getToken(functionSignatureNode.closeParenToken());

        FunctionBodyNode functionBodyNode = this.modifyNode(functionDefinitionNode.functionBody());

        return functionDefinitionNode.modify()
                .withFunctionKeyword(formatToken(functionKeyword, 0, 0, 0))
                .withFunctionName((IdentifierToken) formatToken(functionName, 0, 0, 0))
                .withFunctionSignature(functionSignatureNode
                        .modify(functionSignatureOpenPara, functionSignatureNode.parameters(),
                                functionSignatureClosePara, null))
                .withFunctionBody(functionBodyNode)
                .apply();
    }

    @Override
    public FunctionBodyBlockNode transform(FunctionBodyBlockNode functionBodyBlockNode) {

        Token functionBodyOpenBrace = getToken(functionBodyBlockNode.openBraceToken());
        Token functionBodyCloseBrace = getToken(functionBodyBlockNode.closeBraceToken());

        NodeList<StatementNode> statements = this.modifyNodeList(functionBodyBlockNode.statements());

        return functionBodyBlockNode.modify()
                .withOpenBraceToken(formatToken(functionBodyOpenBrace, 1, 0, 0))
                .withCloseBraceToken(formatToken(functionBodyCloseBrace, 0, 0, 0))
                .withStatements(statements)
                .apply();
    }

    @Override
    public ExpressionStatementNode transform(ExpressionStatementNode expressionStatementNode) {
        ExpressionNode expression = this.modifyNode(expressionStatementNode.expression());
        Token semicolonToken = expressionStatementNode.semicolonToken();

        return expressionStatementNode.modify()
                .withExpression(expression)
                .withSemicolonToken(formatToken(semicolonToken, 0, 0, 0))
                .apply();
    }

    @Override
    public FunctionCallExpressionNode transform(FunctionCallExpressionNode functionCallExpressionNode) {
        Node functionName = this.modifyNode(functionCallExpressionNode.functionName());
        Token functionCallOpenPara = getToken(functionCallExpressionNode.openParenToken());
        Token functionCallClosePara = getToken(functionCallExpressionNode.closeParenToken());
        NodeList<FunctionArgumentNode> arguments = this.modifyNodeList(functionCallExpressionNode.arguments());
        ;

        return functionCallExpressionNode.modify()
                .withFunctionName(functionName)
                .withOpenParenToken(formatToken(functionCallOpenPara, 0, 0, 0))
                .withCloseParenToken(formatToken(functionCallClosePara, 0, 0, 0))
                .withArguments(arguments)
                .apply();
    }

    @Override
    public QualifiedNameReferenceNode transform(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        Token modulePrefix = getToken(qualifiedNameReferenceNode.modulePrefix());
        Token identifier = getToken(qualifiedNameReferenceNode.identifier());

        return qualifiedNameReferenceNode.modify()
                .withModulePrefix(formatToken(modulePrefix, 4, 0, 1))
                .withIdentifier((IdentifierToken) formatToken(identifier, 0, 0, 0))
                .apply();
    }

    private Token formatToken(Token token, int leadingSpaces, int trailingSpaces, int newLines) {
        MinutiaeList leadingMinutiaeList = token.leadingMinutiae();
        MinutiaeList trailingMinutiaeList = token.trailingMinutiae();

        MinutiaeList newLeadingMinutiaeList = modifyMinutiaeList(leadingMinutiaeList, leadingSpaces, newLines);

        return token.modify(newLeadingMinutiaeList, trailingMinutiaeList);
    }

    private MinutiaeList modifyMinutiaeList(MinutiaeList minutiaeList, int spaces, int newLines) {
        Minutiae minutiae = NodeFactory.createWhitespaceMinutiae(getWhiteSpaces(spaces, newLines));
        return minutiaeList.add(minutiae);
    }

    private String getWhiteSpaces(int column, int newLines) {
        StringBuilder whiteSpaces = new StringBuilder();
        for (int i = 0; i <= (newLines - 1); i++) {
            whiteSpaces.append("\n");
        }
        for (int i = 0; i <= (column - 1); i++) {
            whiteSpaces.append(" ");
        }

        return whiteSpaces.toString();
    }

    private <T extends Token> Token getToken(T node) {
        return node.modify(AbstractNodeFactory.createEmptyMinutiaeList(),
                AbstractNodeFactory.createEmptyMinutiaeList());
    }
}
