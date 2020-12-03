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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ErrorConstructorExpressionNode extends ExpressionNode {

    public ErrorConstructorExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token errorKeyword() {
        return childInBucket(0);
    }

    public Optional<TypeDescriptorNode> typeReference() {
        return optionalChildInBucket(1);
    }

    public Token openParenToken() {
        return childInBucket(2);
    }

    public SeparatedNodeList<FunctionArgumentNode> arguments() {
        return new SeparatedNodeList<>(childInBucket(3));
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
                "errorKeyword",
                "typeReference",
                "openParenToken",
                "arguments",
                "closeParenToken"};
    }

    public ErrorConstructorExpressionNode modify(
            Token errorKeyword,
            TypeDescriptorNode typeReference,
            Token openParenToken,
            SeparatedNodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments.underlyingListNode(),
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createErrorConstructorExpressionNode(
                errorKeyword,
                typeReference,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public ErrorConstructorExpressionNodeModifier modify() {
        return new ErrorConstructorExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ErrorConstructorExpressionNodeModifier {
        private final ErrorConstructorExpressionNode oldNode;
        private Token errorKeyword;
        private TypeDescriptorNode typeReference;
        private Token openParenToken;
        private SeparatedNodeList<FunctionArgumentNode> arguments;
        private Token closeParenToken;

        public ErrorConstructorExpressionNodeModifier(ErrorConstructorExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.errorKeyword = oldNode.errorKeyword();
            this.typeReference = oldNode.typeReference().orElse(null);
            this.openParenToken = oldNode.openParenToken();
            this.arguments = oldNode.arguments();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public ErrorConstructorExpressionNodeModifier withErrorKeyword(
                Token errorKeyword) {
            Objects.requireNonNull(errorKeyword, "errorKeyword must not be null");
            this.errorKeyword = errorKeyword;
            return this;
        }

        public ErrorConstructorExpressionNodeModifier withTypeReference(
                TypeDescriptorNode typeReference) {
            this.typeReference = typeReference;
            return this;
        }

        public ErrorConstructorExpressionNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public ErrorConstructorExpressionNodeModifier withArguments(
                SeparatedNodeList<FunctionArgumentNode> arguments) {
            Objects.requireNonNull(arguments, "arguments must not be null");
            this.arguments = arguments;
            return this;
        }

        public ErrorConstructorExpressionNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public ErrorConstructorExpressionNode apply() {
            return oldNode.modify(
                    errorKeyword,
                    typeReference,
                    openParenToken,
                    arguments,
                    closeParenToken);
        }
    }
}
