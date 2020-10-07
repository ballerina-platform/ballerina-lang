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
public class BinaryExpressionNode extends ExpressionNode {

    public BinaryExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node lhsExpr() {
        return childInBucket(0);
    }

    public Token operator() {
        return childInBucket(1);
    }

    public Node rhsExpr() {
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
                "lhsExpr",
                "operator",
                "rhsExpr"};
    }

    public BinaryExpressionNode modify(
            SyntaxKind kind,
            Node lhsExpr,
            Token operator,
            Node rhsExpr) {
        if (checkForReferenceEquality(
                lhsExpr,
                operator,
                rhsExpr)) {
            return this;
        }

        return NodeFactory.createBinaryExpressionNode(
                kind,
                lhsExpr,
                operator,
                rhsExpr);
    }

    public BinaryExpressionNodeModifier modify() {
        return new BinaryExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class BinaryExpressionNodeModifier {
        private final BinaryExpressionNode oldNode;
        private Node lhsExpr;
        private Token operator;
        private Node rhsExpr;

        public BinaryExpressionNodeModifier(BinaryExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.lhsExpr = oldNode.lhsExpr();
            this.operator = oldNode.operator();
            this.rhsExpr = oldNode.rhsExpr();
        }

        public BinaryExpressionNodeModifier withLhsExpr(
                Node lhsExpr) {
            Objects.requireNonNull(lhsExpr, "lhsExpr must not be null");
            this.lhsExpr = lhsExpr;
            return this;
        }

        public BinaryExpressionNodeModifier withOperator(
                Token operator) {
            Objects.requireNonNull(operator, "operator must not be null");
            this.operator = operator;
            return this;
        }

        public BinaryExpressionNodeModifier withRhsExpr(
                Node rhsExpr) {
            Objects.requireNonNull(rhsExpr, "rhsExpr must not be null");
            this.rhsExpr = rhsExpr;
            return this;
        }

        public BinaryExpressionNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    lhsExpr,
                    operator,
                    rhsExpr);
        }
    }
}
