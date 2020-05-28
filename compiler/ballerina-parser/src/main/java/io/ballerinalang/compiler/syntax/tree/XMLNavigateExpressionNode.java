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
public class XMLNavigateExpressionNode extends ExpressionNode {

    public XMLNavigateExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token dotLtToken() {
        return childInBucket(1);
    }

    public SeparatedNodeList<Node> xmlNamePattern() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Token gtToken() {
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
                "expression",
                "dotLtToken",
                "xmlNamePattern",
                "gtToken"};
    }

    public XMLNavigateExpressionNode modify(
            ExpressionNode expression,
            Token dotLtToken,
            SeparatedNodeList<Node> xmlNamePattern,
            Token gtToken) {
        if (checkForReferenceEquality(
                expression,
                dotLtToken,
                xmlNamePattern.underlyingListNode(),
                gtToken)) {
            return this;
        }

        return NodeFactory.createXMLNavigateExpressionNode(
                expression,
                dotLtToken,
                xmlNamePattern,
                gtToken);
    }

    public XMLNavigateExpressionNodeModifier modify() {
        return new XMLNavigateExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLNavigateExpressionNodeModifier {
        private final XMLNavigateExpressionNode oldNode;
        private ExpressionNode expression;
        private Token dotLtToken;
        private SeparatedNodeList<Node> xmlNamePattern;
        private Token gtToken;

        public XMLNavigateExpressionNodeModifier(XMLNavigateExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.dotLtToken = oldNode.dotLtToken();
            this.xmlNamePattern = oldNode.xmlNamePattern();
            this.gtToken = oldNode.gtToken();
        }

        public XMLNavigateExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public XMLNavigateExpressionNodeModifier withDotLtToken(
                Token dotLtToken) {
            Objects.requireNonNull(dotLtToken, "dotLtToken must not be null");
            this.dotLtToken = dotLtToken;
            return this;
        }

        public XMLNavigateExpressionNodeModifier withXmlNamePattern(
                SeparatedNodeList<Node> xmlNamePattern) {
            Objects.requireNonNull(xmlNamePattern, "xmlNamePattern must not be null");
            this.xmlNamePattern = xmlNamePattern;
            return this;
        }

        public XMLNavigateExpressionNodeModifier withGtToken(
                Token gtToken) {
            Objects.requireNonNull(gtToken, "gtToken must not be null");
            this.gtToken = gtToken;
            return this;
        }

        public XMLNavigateExpressionNode apply() {
            return oldNode.modify(
                    expression,
                    dotLtToken,
                    xmlNamePattern,
                    gtToken);
        }
    }
}
