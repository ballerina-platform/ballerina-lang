/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.utils;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitAnonymousFunctionExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;

/**
 * Validates whether the user inputs are completed.
 *
 * @since 2.0.0
 */
public class IncompleteInputFinder extends NodeTransformer<Boolean> {

    @Override
    public Boolean transform(SimpleNameReferenceNode node) {
        return node.name().isMissing();
    }

    @Override
    public Boolean transform(BinaryExpressionNode node) {
        return node.rhsExpr().apply(this);
    }

    @Override
    public Boolean transform(FunctionBodyBlockNode node) {
        return node.closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(ExplicitAnonymousFunctionExpressionNode node) {
        return node.functionBody().apply(this);
    }

    @Override
    public Boolean transform(BasicLiteralNode basicLiteralNode) {
        return false;
    }

    @Override
    public Boolean transform(ExpressionFunctionBodyNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(ListConstructorExpressionNode node) {
        return node.closeBracket().isMissing();
    }

    @Override
    public Boolean transform(IfElseStatementNode node) {
        return node.ifBody().isMissing() || node.ifBody().closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(WhileStatementNode node) {
        return node.whileBody().isMissing() || node.whileBody().closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(ForEachStatementNode node) {
        return node.blockStatement().isMissing() || node.blockStatement().closeBraceToken().isMissing();
    }

    @Override
    public Boolean transform(VariableDeclarationNode node) {
        if (node.equalsToken().isEmpty()) {
            return false;
        }
        if (node.initializer().isPresent()) {
            return node.initializer().get().apply(this);
        }
        return node.initializer().isEmpty();
    }

    @Override
    public Boolean transform(TypeDefinitionNode node) {
        return node.typeDescriptor().apply(this);
    }

    @Override
    public Boolean transform(RecordTypeDescriptorNode node) {
        return node.bodyStartDelimiter().isMissing() || node.bodyEndDelimiter().isMissing();
    }

    @Override
    public Boolean transform(MappingConstructorExpressionNode node) {
        return node.closeBrace().isMissing();
    }

    @Override
    public Boolean transform(QueryExpressionNode node) {
        return node.selectClause().isMissing() || node.selectClause().apply(this);
    }

    @Override
    public Boolean transform(SelectClauseNode node) {
        return node.expression().apply(this);
    }

    @Override
    public Boolean transform(TableConstructorExpressionNode node) {
        return node.closeBracket().isMissing();
    }

    @Override
    protected Boolean transformSyntaxNode(Node node) {
        return false;
    }
}
