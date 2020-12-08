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
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class MethodCallExpressionNode extends ExpressionNode {

    public MethodCallExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token dotToken() {
        return childInBucket(1);
    }

    public NameReferenceNode methodName() {
        return childInBucket(2);
    }

    public Token openParenToken() {
        return childInBucket(3);
    }

    public SeparatedNodeList<FunctionArgumentNode> arguments() {
        return new SeparatedNodeList<>(childInBucket(4));
    }

    public Token closeParenToken() {
        return childInBucket(5);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "expression",
                "dotToken",
                "methodName",
                "openParenToken",
                "arguments",
                "closeParenToken"};
    }

    public MethodCallExpressionNode modify(
            ExpressionNode expression,
            Token dotToken,
            NameReferenceNode methodName,
            Token openParenToken,
            SeparatedNodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments.underlyingListNode(),
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createMethodCallExpressionNode(
                expression,
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public MethodCallExpressionNodeModifier modify() {
        return new MethodCallExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MethodCallExpressionNodeModifier {
        private final MethodCallExpressionNode oldNode;
        private ExpressionNode expression;
        private Token dotToken;
        private NameReferenceNode methodName;
        private Token openParenToken;
        private SeparatedNodeList<FunctionArgumentNode> arguments;
        private Token closeParenToken;

        public MethodCallExpressionNodeModifier(MethodCallExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.dotToken = oldNode.dotToken();
            this.methodName = oldNode.methodName();
            this.openParenToken = oldNode.openParenToken();
            this.arguments = oldNode.arguments();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public MethodCallExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public MethodCallExpressionNodeModifier withDotToken(
                Token dotToken) {
            Objects.requireNonNull(dotToken, "dotToken must not be null");
            this.dotToken = dotToken;
            return this;
        }

        public MethodCallExpressionNodeModifier withMethodName(
                NameReferenceNode methodName) {
            Objects.requireNonNull(methodName, "methodName must not be null");
            this.methodName = methodName;
            return this;
        }

        public MethodCallExpressionNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public MethodCallExpressionNodeModifier withArguments(
                SeparatedNodeList<FunctionArgumentNode> arguments) {
            Objects.requireNonNull(arguments, "arguments must not be null");
            this.arguments = arguments;
            return this;
        }

        public MethodCallExpressionNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public MethodCallExpressionNode apply() {
            return oldNode.modify(
                    expression,
                    dotToken,
                    methodName,
                    openParenToken,
                    arguments,
                    closeParenToken);
        }
    }
}
