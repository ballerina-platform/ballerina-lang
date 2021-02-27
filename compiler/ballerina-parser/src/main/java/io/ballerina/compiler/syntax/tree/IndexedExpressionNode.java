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
public class IndexedExpressionNode extends TypeDescriptorNode {

    public IndexedExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode containerExpression() {
        return childInBucket(0);
    }

    public Token openBracket() {
        return childInBucket(1);
    }

    public SeparatedNodeList<ExpressionNode> keyExpression() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Token closeBracket() {
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
                "containerExpression",
                "openBracket",
                "keyExpression",
                "closeBracket"};
    }

    public IndexedExpressionNode modify(
            ExpressionNode containerExpression,
            Token openBracket,
            SeparatedNodeList<ExpressionNode> keyExpression,
            Token closeBracket) {
        if (checkForReferenceEquality(
                containerExpression,
                openBracket,
                keyExpression.underlyingListNode(),
                closeBracket)) {
            return this;
        }

        return NodeFactory.createIndexedExpressionNode(
                containerExpression,
                openBracket,
                keyExpression,
                closeBracket);
    }

    public IndexedExpressionNodeModifier modify() {
        return new IndexedExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class IndexedExpressionNodeModifier {
        private final IndexedExpressionNode oldNode;
        private ExpressionNode containerExpression;
        private Token openBracket;
        private SeparatedNodeList<ExpressionNode> keyExpression;
        private Token closeBracket;

        public IndexedExpressionNodeModifier(IndexedExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.containerExpression = oldNode.containerExpression();
            this.openBracket = oldNode.openBracket();
            this.keyExpression = oldNode.keyExpression();
            this.closeBracket = oldNode.closeBracket();
        }

        public IndexedExpressionNodeModifier withContainerExpression(
                ExpressionNode containerExpression) {
            Objects.requireNonNull(containerExpression, "containerExpression must not be null");
            this.containerExpression = containerExpression;
            return this;
        }

        public IndexedExpressionNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public IndexedExpressionNodeModifier withKeyExpression(
                SeparatedNodeList<ExpressionNode> keyExpression) {
            Objects.requireNonNull(keyExpression, "keyExpression must not be null");
            this.keyExpression = keyExpression;
            return this;
        }

        public IndexedExpressionNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public IndexedExpressionNode apply() {
            return oldNode.modify(
                    containerExpression,
                    openBracket,
                    keyExpression,
                    closeBracket);
        }
    }
}
