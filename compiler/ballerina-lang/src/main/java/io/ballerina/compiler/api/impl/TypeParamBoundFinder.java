/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;

/**
 * A visitor class to determine the bound type of the type param.
 *
 * @since 2201.8.0
 */
public class TypeParamBoundFinder extends NodeVisitor {

    private ExpressionNode expressionNode;
    private NameReferenceNode methodName;

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        this.visitParentNode(simpleNameReferenceNode);
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        this.visitParentNode(functionCallExpressionNode);
    }

    @Override
    public void visit(PositionalArgumentNode positionalArgumentNode) {
        this.visitParentNode(positionalArgumentNode);
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        this.expressionNode = methodCallExpressionNode.expression();
        this.methodName = methodCallExpressionNode.methodName();
    }

    @Override
    protected void visitSyntaxNode(Node node) {
    }

    private void visitParentNode(Node node) {
        node.parent().accept(this);
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public String getMethodName() {
        return methodName.toString();
    }
}
