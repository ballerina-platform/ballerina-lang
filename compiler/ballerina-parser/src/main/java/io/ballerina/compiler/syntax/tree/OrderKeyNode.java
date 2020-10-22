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
public class OrderKeyNode extends NonTerminalNode {

    public OrderKeyNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Optional<Token> orderDirection() {
        return optionalChildInBucket(1);
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
                "orderDirection"};
    }

    public OrderKeyNode modify(
            ExpressionNode expression,
            Token orderDirection) {
        if (checkForReferenceEquality(
                expression,
                orderDirection)) {
            return this;
        }

        return NodeFactory.createOrderKeyNode(
                expression,
                orderDirection);
    }

    public OrderKeyNodeModifier modify() {
        return new OrderKeyNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OrderKeyNodeModifier {
        private final OrderKeyNode oldNode;
        private ExpressionNode expression;
        private Token orderDirection;

        public OrderKeyNodeModifier(OrderKeyNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.orderDirection = oldNode.orderDirection().orElse(null);
        }

        public OrderKeyNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public OrderKeyNodeModifier withOrderDirection(
                Token orderDirection) {
            Objects.requireNonNull(orderDirection, "orderDirection must not be null");
            this.orderDirection = orderDirection;
            return this;
        }

        public OrderKeyNode apply() {
            return oldNode.modify(
                    expression,
                    orderDirection);
        }
    }
}
