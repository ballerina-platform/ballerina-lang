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
public class TypeCastExpressionNode extends ExpressionNode {

    public TypeCastExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ltToken() {
        return childInBucket(0);
    }

    public TypeCastParamNode typeCastParam() {
        return childInBucket(1);
    }

    public Token gtToken() {
        return childInBucket(2);
    }

    public ExpressionNode expression() {
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
                "ltToken",
                "typeCastParam",
                "gtToken",
                "expression"};
    }

    public TypeCastExpressionNode modify(
            Token ltToken,
            TypeCastParamNode typeCastParam,
            Token gtToken,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                ltToken,
                typeCastParam,
                gtToken,
                expression)) {
            return this;
        }

        return NodeFactory.createTypeCastExpressionNode(
                ltToken,
                typeCastParam,
                gtToken,
                expression);
    }

    public TypeCastExpressionNodeModifier modify() {
        return new TypeCastExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TypeCastExpressionNodeModifier {
        private final TypeCastExpressionNode oldNode;
        private Token ltToken;
        private TypeCastParamNode typeCastParam;
        private Token gtToken;
        private ExpressionNode expression;

        public TypeCastExpressionNodeModifier(TypeCastExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.ltToken = oldNode.ltToken();
            this.typeCastParam = oldNode.typeCastParam();
            this.gtToken = oldNode.gtToken();
            this.expression = oldNode.expression();
        }

        public TypeCastExpressionNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public TypeCastExpressionNodeModifier withTypeCastParam(
                TypeCastParamNode typeCastParam) {
            Objects.requireNonNull(typeCastParam, "typeCastParam must not be null");
            this.typeCastParam = typeCastParam;
            return this;
        }

        public TypeCastExpressionNodeModifier withGtToken(
                Token gtToken) {
            Objects.requireNonNull(gtToken, "gtToken must not be null");
            this.gtToken = gtToken;
            return this;
        }

        public TypeCastExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public TypeCastExpressionNode apply() {
            return oldNode.modify(
                    ltToken,
                    typeCastParam,
                    gtToken,
                    expression);
        }
    }
}
