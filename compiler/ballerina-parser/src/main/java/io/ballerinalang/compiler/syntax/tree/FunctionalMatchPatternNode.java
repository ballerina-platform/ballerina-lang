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
public class FunctionalMatchPatternNode extends NonTerminalNode {

    public FunctionalMatchPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node typeRef() {
        return childInBucket(0);
    }

    public Token openParenthesisToken() {
        return childInBucket(1);
    }

    public SeparatedNodeList<Node> argListMatchPatternNode() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Token closeParenthesisToken() {
        return childInBucket(3);
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
                "typeRef",
                "openParenthesisToken",
                "argListMatchPatternNode",
                "closeParenthesisToken"};
    }

    public FunctionalMatchPatternNode modify(
            Node typeRef,
            Token openParenthesisToken,
            SeparatedNodeList<Node> argListMatchPatternNode,
            Token closeParenthesisToken) {
        if (checkForReferenceEquality(
                typeRef,
                openParenthesisToken,
                argListMatchPatternNode.underlyingListNode(),
                closeParenthesisToken)) {
            return this;
        }

        return NodeFactory.createFunctionalMatchPatternNode(
                typeRef,
                openParenthesisToken,
                argListMatchPatternNode,
                closeParenthesisToken);
    }

    public FunctionalMatchPatternNodeModifier modify() {
        return new FunctionalMatchPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FunctionalMatchPatternNodeModifier {
        private final FunctionalMatchPatternNode oldNode;
        private Node typeRef;
        private Token openParenthesisToken;
        private SeparatedNodeList<Node> argListMatchPatternNode;
        private Token closeParenthesisToken;

        public FunctionalMatchPatternNodeModifier(FunctionalMatchPatternNode oldNode) {
            this.oldNode = oldNode;
            this.typeRef = oldNode.typeRef();
            this.openParenthesisToken = oldNode.openParenthesisToken();
            this.argListMatchPatternNode = oldNode.argListMatchPatternNode();
            this.closeParenthesisToken = oldNode.closeParenthesisToken();
        }

        public FunctionalMatchPatternNodeModifier withTypeRef(
                Node typeRef) {
            Objects.requireNonNull(typeRef, "typeRef must not be null");
            this.typeRef = typeRef;
            return this;
        }

        public FunctionalMatchPatternNodeModifier withOpenParenthesisToken(
                Token openParenthesisToken) {
            Objects.requireNonNull(openParenthesisToken, "openParenthesisToken must not be null");
            this.openParenthesisToken = openParenthesisToken;
            return this;
        }

        public FunctionalMatchPatternNodeModifier withArgListMatchPatternNode(
                SeparatedNodeList<Node> argListMatchPatternNode) {
            Objects.requireNonNull(argListMatchPatternNode, "argListMatchPatternNode must not be null");
            this.argListMatchPatternNode = argListMatchPatternNode;
            return this;
        }

        public FunctionalMatchPatternNodeModifier withCloseParenthesisToken(
                Token closeParenthesisToken) {
            Objects.requireNonNull(closeParenthesisToken, "closeParenthesisToken must not be null");
            this.closeParenthesisToken = closeParenthesisToken;
            return this;
        }

        public FunctionalMatchPatternNode apply() {
            return oldNode.modify(
                    typeRef,
                    openParenthesisToken,
                    argListMatchPatternNode,
                    closeParenthesisToken);
        }
    }
}
