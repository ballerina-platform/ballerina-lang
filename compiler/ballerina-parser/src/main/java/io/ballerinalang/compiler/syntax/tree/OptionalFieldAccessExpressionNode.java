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
public class OptionalFieldAccessExpressionNode extends ExpressionNode {

    public OptionalFieldAccessExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token optionalChainingToken() {
        return childInBucket(1);
    }

    public NameReferenceNode fieldName() {
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
                "optionalChainingToken",
                "fieldName"};
    }

    public OptionalFieldAccessExpressionNode modify(
            ExpressionNode expression,
            Token optionalChainingToken,
            NameReferenceNode fieldName) {
        if (checkForReferenceEquality(
                expression,
                optionalChainingToken,
                fieldName)) {
            return this;
        }

        return NodeFactory.createOptionalFieldAccessExpressionNode(
                expression,
                optionalChainingToken,
                fieldName);
    }

    public OptionalFieldAccessExpressionNodeModifier modify() {
        return new OptionalFieldAccessExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OptionalFieldAccessExpressionNodeModifier {
        private final OptionalFieldAccessExpressionNode oldNode;
        private ExpressionNode expression;
        private Token optionalChainingToken;
        private NameReferenceNode fieldName;

        public OptionalFieldAccessExpressionNodeModifier(OptionalFieldAccessExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.optionalChainingToken = oldNode.optionalChainingToken();
            this.fieldName = oldNode.fieldName();
        }

        public OptionalFieldAccessExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public OptionalFieldAccessExpressionNodeModifier withOptionalChainingToken(
                Token optionalChainingToken) {
            Objects.requireNonNull(optionalChainingToken, "optionalChainingToken must not be null");
            this.optionalChainingToken = optionalChainingToken;
            return this;
        }

        public OptionalFieldAccessExpressionNodeModifier withFieldName(
                NameReferenceNode fieldName) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            this.fieldName = fieldName;
            return this;
        }

        public OptionalFieldAccessExpressionNode apply() {
            return oldNode.modify(
                    expression,
                    optionalChainingToken,
                    fieldName);
        }
    }
}
