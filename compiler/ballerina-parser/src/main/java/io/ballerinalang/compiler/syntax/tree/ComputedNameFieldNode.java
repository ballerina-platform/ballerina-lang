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
public class ComputedNameFieldNode extends MappingFieldNode {

    public ComputedNameFieldNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public ExpressionNode fieldNameExpr() {
        return childInBucket(1);
    }

    public Token closeBracket() {
        return childInBucket(2);
    }

    public Token colonToken() {
        return childInBucket(3);
    }

    public ExpressionNode valueExpr() {
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
                "openBracket",
                "fieldNameExpr",
                "closeBracket",
                "colonToken",
                "valueExpr"};
    }

    public ComputedNameFieldNode modify(
            Token openBracket,
            ExpressionNode fieldNameExpr,
            Token closeBracket,
            Token colonToken,
            ExpressionNode valueExpr) {
        if (checkForReferenceEquality(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr)) {
            return this;
        }

        return NodeFactory.createComputedNameFieldNode(
                openBracket,
                fieldNameExpr,
                closeBracket,
                colonToken,
                valueExpr);
    }

    public ComputedNameFieldNodeModifier modify() {
        return new ComputedNameFieldNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ComputedNameFieldNodeModifier {
        private final ComputedNameFieldNode oldNode;
        private Token openBracket;
        private ExpressionNode fieldNameExpr;
        private Token closeBracket;
        private Token colonToken;
        private ExpressionNode valueExpr;

        public ComputedNameFieldNodeModifier(ComputedNameFieldNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.fieldNameExpr = oldNode.fieldNameExpr();
            this.closeBracket = oldNode.closeBracket();
            this.colonToken = oldNode.colonToken();
            this.valueExpr = oldNode.valueExpr();
        }

        public ComputedNameFieldNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ComputedNameFieldNodeModifier withFieldNameExpr(
                ExpressionNode fieldNameExpr) {
            Objects.requireNonNull(fieldNameExpr, "fieldNameExpr must not be null");
            this.fieldNameExpr = fieldNameExpr;
            return this;
        }

        public ComputedNameFieldNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public ComputedNameFieldNodeModifier withColonToken(
                Token colonToken) {
            Objects.requireNonNull(colonToken, "colonToken must not be null");
            this.colonToken = colonToken;
            return this;
        }

        public ComputedNameFieldNodeModifier withValueExpr(
                ExpressionNode valueExpr) {
            Objects.requireNonNull(valueExpr, "valueExpr must not be null");
            this.valueExpr = valueExpr;
            return this;
        }

        public ComputedNameFieldNode apply() {
            return oldNode.modify(
                    openBracket,
                    fieldNameExpr,
                    closeBracket,
                    colonToken,
                    valueExpr);
        }
    }
}
