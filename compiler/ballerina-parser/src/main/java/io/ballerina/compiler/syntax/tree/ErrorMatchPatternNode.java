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
public class ErrorMatchPatternNode extends NonTerminalNode {

    public ErrorMatchPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token errorKeyword() {
        return childInBucket(0);
    }

    public Optional<NameReferenceNode> typeReference() {
        return optionalChildInBucket(1);
    }

    public Token openParenthesisToken() {
        return childInBucket(2);
    }

    public SeparatedNodeList<Node> argListMatchPatternNode() {
        return new SeparatedNodeList<>(childInBucket(3));
    }

    public Token closeParenthesisToken() {
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
                "openParenthesisToken",
                "argListMatchPatternNode",
                "closeParenthesisToken"};
    }

    public ErrorMatchPatternNode modify(
            Token errorKeyword,
            NameReferenceNode typeReference,
            Token openParenthesisToken,
            SeparatedNodeList<Node> argListMatchPatternNode,
            Token closeParenthesisToken) {
        if (checkForReferenceEquality(
                errorKeyword,
                typeReference,
                openParenthesisToken,
                argListMatchPatternNode.underlyingListNode(),
                closeParenthesisToken)) {
            return this;
        }

        return NodeFactory.createErrorMatchPatternNode(
                errorKeyword,
                typeReference,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken);
    }

    public ErrorMatchPatternNodeModifier modify() {
        return new ErrorMatchPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ErrorMatchPatternNodeModifier {
        private final ErrorMatchPatternNode oldNode;
        private Token errorKeyword;
        private NameReferenceNode typeReference;
        private Token openParenthesisToken;
        private SeparatedNodeList<Node> argListMatchPatternNode;
        private Token closeParenthesisToken;

        public ErrorMatchPatternNodeModifier(ErrorMatchPatternNode oldNode) {
            this.oldNode = oldNode;
            this.errorKeyword = oldNode.errorKeyword();
            this.typeReference = oldNode.typeReference().orElse(null);
            this.openParenthesisToken = oldNode.openParenthesisToken();
            this.argListMatchPatternNode = oldNode.argListMatchPatternNode();
            this.closeParenthesisToken = oldNode.closeParenthesisToken();
        }

        public ErrorMatchPatternNodeModifier withErrorKeyword(
                Token errorKeyword) {
            Objects.requireNonNull(errorKeyword, "errorKeyword must not be null");
            this.errorKeyword = errorKeyword;
            return this;
        }

        public ErrorMatchPatternNodeModifier withTypeReference(
                NameReferenceNode typeReference) {
            Objects.requireNonNull(typeReference, "typeReference must not be null");
            this.typeReference = typeReference;
            return this;
        }

        public ErrorMatchPatternNodeModifier withOpenParenthesisToken(
                Token openParenthesisToken) {
            Objects.requireNonNull(openParenthesisToken, "openParenthesisToken must not be null");
            this.openParenthesisToken = openParenthesisToken;
            return this;
        }

        public ErrorMatchPatternNodeModifier withArgListMatchPatternNode(
                SeparatedNodeList<Node> argListMatchPatternNode) {
            Objects.requireNonNull(argListMatchPatternNode, "argListMatchPatternNode must not be null");
            this.argListMatchPatternNode = argListMatchPatternNode;
            return this;
        }

        public ErrorMatchPatternNodeModifier withCloseParenthesisToken(
                Token closeParenthesisToken) {
            Objects.requireNonNull(closeParenthesisToken, "closeParenthesisToken must not be null");
            this.closeParenthesisToken = closeParenthesisToken;
            return this;
        }

        public ErrorMatchPatternNode apply() {
            return oldNode.modify(
                    errorKeyword,
                    typeReference,
                    openParenthesisToken,
                    argListMatchPatternNode,
                    closeParenthesisToken);
        }
    }
}
