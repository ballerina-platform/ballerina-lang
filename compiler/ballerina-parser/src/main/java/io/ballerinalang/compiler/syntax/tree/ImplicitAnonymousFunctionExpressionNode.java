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
public class ImplicitAnonymousFunctionExpressionNode extends AnonymousFunctionExpressionNode {

    public ImplicitAnonymousFunctionExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<Token> qualifierList() {
        return new NodeList<>(childInBucket(0));
    }

    public Node params() {
        return childInBucket(1);
    }

    public Token rightDoubleArrow() {
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
                "qualifierList",
                "params",
                "rightDoubleArrow",
                "expression"};
    }

    public ImplicitAnonymousFunctionExpressionNode modify(
            NodeList<Token> qualifierList,
            Node params,
            Token rightDoubleArrow,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                qualifierList.underlyingListNode(),
                params,
                rightDoubleArrow,
                expression)) {
            return this;
        }

        return NodeFactory.createImplicitAnonymousFunctionExpressionNode(
                qualifierList,
                params,
                rightDoubleArrow,
                expression);
    }

    public ImplicitAnonymousFunctionExpressionNodeModifier modify() {
        return new ImplicitAnonymousFunctionExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImplicitAnonymousFunctionExpressionNodeModifier {
        private final ImplicitAnonymousFunctionExpressionNode oldNode;
        private NodeList<Token> qualifierList;
        private Node params;
        private Token rightDoubleArrow;
        private ExpressionNode expression;

        public ImplicitAnonymousFunctionExpressionNodeModifier(ImplicitAnonymousFunctionExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.qualifierList = oldNode.qualifierList();
            this.params = oldNode.params();
            this.rightDoubleArrow = oldNode.rightDoubleArrow();
            this.expression = oldNode.expression();
        }

        public ImplicitAnonymousFunctionExpressionNodeModifier withQualifierList(
                NodeList<Token> qualifierList) {
            Objects.requireNonNull(qualifierList, "qualifierList must not be null");
            this.qualifierList = qualifierList;
            return this;
        }

        public ImplicitAnonymousFunctionExpressionNodeModifier withParams(
                Node params) {
            Objects.requireNonNull(params, "params must not be null");
            this.params = params;
            return this;
        }

        public ImplicitAnonymousFunctionExpressionNodeModifier withRightDoubleArrow(
                Token rightDoubleArrow) {
            Objects.requireNonNull(rightDoubleArrow, "rightDoubleArrow must not be null");
            this.rightDoubleArrow = rightDoubleArrow;
            return this;
        }

        public ImplicitAnonymousFunctionExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public ImplicitAnonymousFunctionExpressionNode apply() {
            return oldNode.modify(
                    qualifierList,
                    params,
                    rightDoubleArrow,
                    expression);
        }
    }
}
