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
public class AnnotAccessExpressionNode extends ExpressionNode {

    public AnnotAccessExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token annotChainingToken() {
        return childInBucket(1);
    }

    public NameReferenceNode annotTagReference() {
        return childInBucket(2);
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
                "annotChainingToken",
                "annotTagReference"};
    }

    public AnnotAccessExpressionNode modify(
            ExpressionNode expression,
            Token annotChainingToken,
            NameReferenceNode annotTagReference) {
        if (checkForReferenceEquality(
                expression,
                annotChainingToken,
                annotTagReference)) {
            return this;
        }

        return NodeFactory.createAnnotAccessExpressionNode(
                expression,
                annotChainingToken,
                annotTagReference);
    }

    public AnnotAccessExpressionNodeModifier modify() {
        return new AnnotAccessExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class AnnotAccessExpressionNodeModifier {
        private final AnnotAccessExpressionNode oldNode;
        private ExpressionNode expression;
        private Token annotChainingToken;
        private NameReferenceNode annotTagReference;

        public AnnotAccessExpressionNodeModifier(AnnotAccessExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.annotChainingToken = oldNode.annotChainingToken();
            this.annotTagReference = oldNode.annotTagReference();
        }

        public AnnotAccessExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public AnnotAccessExpressionNodeModifier withAnnotChainingToken(
                Token annotChainingToken) {
            Objects.requireNonNull(annotChainingToken, "annotChainingToken must not be null");
            this.annotChainingToken = annotChainingToken;
            return this;
        }

        public AnnotAccessExpressionNodeModifier withAnnotTagReference(
                NameReferenceNode annotTagReference) {
            Objects.requireNonNull(annotTagReference, "annotTagReference must not be null");
            this.annotTagReference = annotTagReference;
            return this;
        }

        public AnnotAccessExpressionNode apply() {
            return oldNode.modify(
                    expression,
                    annotChainingToken,
                    annotTagReference);
        }
    }
}
