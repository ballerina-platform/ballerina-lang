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
public class SpreadFieldNode extends MappingFieldNode {

    public SpreadFieldNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token leadingComma() {
        return childInBucket(0);
    }

    public Token ellipsis() {
        return childInBucket(1);
    }

    public ExpressionNode valueExpr() {
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
                "leadingComma",
                "ellipsis",
                "valueExpr"};
    }

    public SpreadFieldNode modify(
            Token leadingComma,
            Token ellipsis,
            ExpressionNode valueExpr) {
        if (checkForReferenceEquality(
                leadingComma,
                ellipsis,
                valueExpr)) {
            return this;
        }

        return NodeFactory.createSpreadFieldNode(
                leadingComma,
                ellipsis,
                valueExpr);
    }

    public SpreadFieldNodeModifier modify() {
        return new SpreadFieldNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class SpreadFieldNodeModifier {
        private final SpreadFieldNode oldNode;
        private Token leadingComma;
        private Token ellipsis;
        private ExpressionNode valueExpr;

        public SpreadFieldNodeModifier(SpreadFieldNode oldNode) {
            this.oldNode = oldNode;
            this.leadingComma = oldNode.leadingComma();
            this.ellipsis = oldNode.ellipsis();
            this.valueExpr = oldNode.valueExpr();
        }

        public SpreadFieldNodeModifier withLeadingComma(
                Token leadingComma) {
            Objects.requireNonNull(leadingComma, "leadingComma must not be null");
            this.leadingComma = leadingComma;
            return this;
        }

        public SpreadFieldNodeModifier withEllipsis(
                Token ellipsis) {
            Objects.requireNonNull(ellipsis, "ellipsis must not be null");
            this.ellipsis = ellipsis;
            return this;
        }

        public SpreadFieldNodeModifier withValueExpr(
                ExpressionNode valueExpr) {
            Objects.requireNonNull(valueExpr, "valueExpr must not be null");
            this.valueExpr = valueExpr;
            return this;
        }

        public SpreadFieldNode apply() {
            return oldNode.modify(
                    leadingComma,
                    ellipsis,
                    valueExpr);
        }
    }
}
