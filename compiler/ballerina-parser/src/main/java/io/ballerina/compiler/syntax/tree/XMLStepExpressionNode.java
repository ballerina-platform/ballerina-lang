/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
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
public class XMLStepExpressionNode extends XMLNavigateExpressionNode {

    public XMLStepExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Node xmlStepStart() {
        return childInBucket(1);
    }

    public NodeList<Node> xmlStepExtend() {
        return new NodeList<>(childInBucket(2));
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
                "xmlStepStart",
                "xmlStepExtend"};
    }

    public XMLStepExpressionNode modify(
            ExpressionNode expression,
            Node xmlStepStart,
            NodeList<Node> xmlStepExtend) {
        if (checkForReferenceEquality(
                expression,
                xmlStepStart,
                xmlStepExtend.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createXMLStepExpressionNode(
                expression,
                xmlStepStart,
                xmlStepExtend);
    }

    public XMLStepExpressionNodeModifier modify() {
        return new XMLStepExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLStepExpressionNodeModifier {
        private final XMLStepExpressionNode oldNode;
        private ExpressionNode expression;
        private Node xmlStepStart;
        private NodeList<Node> xmlStepExtend;

        public XMLStepExpressionNodeModifier(XMLStepExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.xmlStepStart = oldNode.xmlStepStart();
            this.xmlStepExtend = oldNode.xmlStepExtend();
        }

        public XMLStepExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public XMLStepExpressionNodeModifier withXmlStepStart(
                Node xmlStepStart) {
            Objects.requireNonNull(xmlStepStart, "xmlStepStart must not be null");
            this.xmlStepStart = xmlStepStart;
            return this;
        }

        public XMLStepExpressionNodeModifier withXmlStepExtend(
                NodeList<Node> xmlStepExtend) {
            Objects.requireNonNull(xmlStepExtend, "xmlStepExtend must not be null");
            this.xmlStepExtend = xmlStepExtend;
            return this;
        }

        public XMLStepExpressionNode apply() {
            return oldNode.modify(
                    expression,
                    xmlStepStart,
                    xmlStepExtend);
        }
    }
}
