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
public class OrderByClauseNode extends ClauseNode {

    public OrderByClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token orderKeyword() {
        return childInBucket(0);
    }

    public Token byKeyword() {
        return childInBucket(1);
    }

    public SeparatedNodeList<OrderKeyNode> orderKey() {
        return new SeparatedNodeList<>(childInBucket(2));
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
                "orderKeyword",
                "byKeyword",
                "orderKey"};
    }

    public OrderByClauseNode modify(
            Token orderKeyword,
            Token byKeyword,
            SeparatedNodeList<OrderKeyNode> orderKey) {
        if (checkForReferenceEquality(
                orderKeyword,
                byKeyword,
                orderKey.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createOrderByClauseNode(
                orderKeyword,
                byKeyword,
                orderKey);
    }

    public OrderByClauseNodeModifier modify() {
        return new OrderByClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OrderByClauseNodeModifier {
        private final OrderByClauseNode oldNode;
        private Token orderKeyword;
        private Token byKeyword;
        private SeparatedNodeList<OrderKeyNode> orderKey;

        public OrderByClauseNodeModifier(OrderByClauseNode oldNode) {
            this.oldNode = oldNode;
            this.orderKeyword = oldNode.orderKeyword();
            this.byKeyword = oldNode.byKeyword();
            this.orderKey = oldNode.orderKey();
        }

        public OrderByClauseNodeModifier withOrderKeyword(
                Token orderKeyword) {
            Objects.requireNonNull(orderKeyword, "orderKeyword must not be null");
            this.orderKeyword = orderKeyword;
            return this;
        }

        public OrderByClauseNodeModifier withByKeyword(
                Token byKeyword) {
            Objects.requireNonNull(byKeyword, "byKeyword must not be null");
            this.byKeyword = byKeyword;
            return this;
        }

        public OrderByClauseNodeModifier withOrderKey(
                SeparatedNodeList<OrderKeyNode> orderKey) {
            Objects.requireNonNull(orderKey, "orderKey must not be null");
            this.orderKey = orderKey;
            return this;
        }

        public OrderByClauseNode apply() {
            return oldNode.modify(
                    orderKeyword,
                    byKeyword,
                    orderKey);
        }
    }
}
