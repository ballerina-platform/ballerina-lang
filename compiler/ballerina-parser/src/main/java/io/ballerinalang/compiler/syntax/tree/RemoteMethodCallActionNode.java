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

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class RemoteMethodCallActionNode extends ActionNode {

    public RemoteMethodCallActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token rightArrowToken() {
        return childInBucket(1);
    }

    public SimpleNameReferenceNode methodName() {
        return childInBucket(2);
    }

    public Token openParenToken() {
        return childInBucket(3);
    }

    public NodeList<FunctionArgumentNode> arguments() {
        return new NodeList<>(childInBucket(4));
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
                "rightArrowToken",
                "methodName",
                "openParenToken",
                "arguments",
                "closeParenToken"};
    }

    public RemoteMethodCallActionNode modify(
            ExpressionNode expression,
            Token rightArrowToken,
            SimpleNameReferenceNode methodName,
            Token openParenToken,
            NodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                expression,
                rightArrowToken,
                methodName,
                openParenToken,
                arguments.underlyingListNode(),
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createRemoteMethodCallActionNode(
                expression,
                rightArrowToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public RemoteMethodCallActionNodeModifier modify() {
        return new RemoteMethodCallActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RemoteMethodCallActionNodeModifier {
        private final RemoteMethodCallActionNode oldNode;
        private ExpressionNode expression;
        private Token rightArrowToken;
        private SimpleNameReferenceNode methodName;
        private Token openParenToken;
        private NodeList<FunctionArgumentNode> arguments;
        private Token closeParenToken;

        public RemoteMethodCallActionNodeModifier(RemoteMethodCallActionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.rightArrowToken = oldNode.rightArrowToken();
            this.methodName = oldNode.methodName();
            this.openParenToken = oldNode.openParenToken();
            this.arguments = oldNode.arguments();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public RemoteMethodCallActionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public RemoteMethodCallActionNodeModifier withRightArrowToken(
                Token rightArrowToken) {
            Objects.requireNonNull(rightArrowToken, "rightArrowToken must not be null");
            this.rightArrowToken = rightArrowToken;
            return this;
        }

        public RemoteMethodCallActionNodeModifier withMethodName(
                SimpleNameReferenceNode methodName) {
            Objects.requireNonNull(methodName, "methodName must not be null");
            this.methodName = methodName;
            return this;
        }

        public RemoteMethodCallActionNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public RemoteMethodCallActionNodeModifier withArguments(
                NodeList<FunctionArgumentNode> arguments) {
            Objects.requireNonNull(arguments, "arguments must not be null");
            this.arguments = arguments;
            return this;
        }

        public RemoteMethodCallActionNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public RemoteMethodCallActionNode apply() {
            return oldNode.modify(
                    expression,
                    rightArrowToken,
                    methodName,
                    openParenToken,
                    arguments,
                    closeParenToken);
        }
    }
}
