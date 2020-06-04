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
public class XMLFilterExpressionNode extends XMLNavigateExpressionNode {

    public XMLFilterExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public XMLNamePatternChainingNode xmlPatternChain() {
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
                "expression",
                "xmlPatternChain"};
    }

    public XMLFilterExpressionNode modify(
            ExpressionNode expression,
            XMLNamePatternChainingNode xmlPatternChain) {
        if (checkForReferenceEquality(
                expression,
                xmlPatternChain)) {
            return this;
        }

        return NodeFactory.createXMLFilterExpressionNode(
                expression,
                xmlPatternChain);
    }

    public XMLFilterExpressionNodeModifier modify() {
        return new XMLFilterExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLFilterExpressionNodeModifier {
        private final XMLFilterExpressionNode oldNode;
        private ExpressionNode expression;
        private XMLNamePatternChainingNode xmlPatternChain;

        public XMLFilterExpressionNodeModifier(XMLFilterExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.xmlPatternChain = oldNode.xmlPatternChain();
        }

        public XMLFilterExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public XMLFilterExpressionNodeModifier withXmlPatternChain(
                XMLNamePatternChainingNode xmlPatternChain) {
            Objects.requireNonNull(xmlPatternChain, "xmlPatternChain must not be null");
            this.xmlPatternChain = xmlPatternChain;
            return this;
        }

        public XMLFilterExpressionNode apply() {
            return oldNode.modify(
                    expression,
                    xmlPatternChain);
        }
    }
}
