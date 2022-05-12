/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.1.0
 */
public class SpreadMemberNode extends NonTerminalNode {

    public SpreadMemberNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ellipsis() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
        return childInBucket(1);
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
                "ellipsis",
                "expression"};
    }

    public SpreadMemberNode modify(
            Token ellipsis,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                ellipsis,
                expression)) {
            return this;
        }

        return NodeFactory.createSpreadMemberNode(
                ellipsis,
                expression);
    }

    public SpreadMemberNodeModifier modify() {
        return new SpreadMemberNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.1.0
     */
    public static class SpreadMemberNodeModifier {
        private final SpreadMemberNode oldNode;
        private Token ellipsis;
        private ExpressionNode expression;

        public SpreadMemberNodeModifier(SpreadMemberNode oldNode) {
            this.oldNode = oldNode;
            this.ellipsis = oldNode.ellipsis();
            this.expression = oldNode.expression();
        }

        public SpreadMemberNodeModifier withEllipsis(
                Token ellipsis) {
            Objects.requireNonNull(ellipsis, "ellipsis must not be null");
            this.ellipsis = ellipsis;
            return this;
        }

        public SpreadMemberNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public SpreadMemberNode apply() {
            return oldNode.modify(
                    ellipsis,
                    expression);
        }
    }
}
