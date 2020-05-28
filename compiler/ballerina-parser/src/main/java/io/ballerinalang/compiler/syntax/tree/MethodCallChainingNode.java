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
public class MethodCallChainingNode extends NonTerminalNode {

    public MethodCallChainingNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token dotToken() {
        return childInBucket(0);
    }

    public NameReferenceNode methodName() {
        return childInBucket(1);
    }

    public Token openParenToken() {
        return childInBucket(2);
    }

    public NodeList<FunctionArgumentNode> arguments() {
        return new NodeList<>(childInBucket(3));
    }

    public Token closeParenToken() {
        return childInBucket(4);
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
                "dotToken",
                "methodName",
                "openParenToken",
                "arguments",
                "closeParenToken"};
    }

    public MethodCallChainingNode modify(
            Token dotToken,
            NameReferenceNode methodName,
            Token openParenToken,
            NodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                dotToken,
                methodName,
                openParenToken,
                arguments.underlyingListNode(),
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createMethodCallChainingNode(
                dotToken,
                methodName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public MethodCallChainingNodeModifier modify() {
        return new MethodCallChainingNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MethodCallChainingNodeModifier {
        private final MethodCallChainingNode oldNode;
        private Token dotToken;
        private NameReferenceNode methodName;
        private Token openParenToken;
        private NodeList<FunctionArgumentNode> arguments;
        private Token closeParenToken;

        public MethodCallChainingNodeModifier(MethodCallChainingNode oldNode) {
            this.oldNode = oldNode;
            this.dotToken = oldNode.dotToken();
            this.methodName = oldNode.methodName();
            this.openParenToken = oldNode.openParenToken();
            this.arguments = oldNode.arguments();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public MethodCallChainingNodeModifier withDotToken(
                Token dotToken) {
            Objects.requireNonNull(dotToken, "dotToken must not be null");
            this.dotToken = dotToken;
            return this;
        }

        public MethodCallChainingNodeModifier withMethodName(
                NameReferenceNode methodName) {
            Objects.requireNonNull(methodName, "methodName must not be null");
            this.methodName = methodName;
            return this;
        }

        public MethodCallChainingNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public MethodCallChainingNodeModifier withArguments(
                NodeList<FunctionArgumentNode> arguments) {
            Objects.requireNonNull(arguments, "arguments must not be null");
            this.arguments = arguments;
            return this;
        }

        public MethodCallChainingNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public MethodCallChainingNode apply() {
            return oldNode.modify(
                    dotToken,
                    methodName,
                    openParenToken,
                    arguments,
                    closeParenToken);
        }
    }
}
