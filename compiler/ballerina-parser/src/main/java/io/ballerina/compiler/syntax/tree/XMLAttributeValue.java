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
public class XMLAttributeValue extends NonTerminalNode {

    public XMLAttributeValue(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startQuote() {
        return childInBucket(0);
    }

    public NodeList<Node> value() {
        return new NodeList<>(childInBucket(1));
    }

    public Token endQuote() {
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
                "startQuote",
                "value",
                "endQuote"};
    }

    public XMLAttributeValue modify(
            Token startQuote,
            NodeList<Node> value,
            Token endQuote) {
        if (checkForReferenceEquality(
                startQuote,
                value.underlyingListNode(),
                endQuote)) {
            return this;
        }

        return NodeFactory.createXMLAttributeValue(
                startQuote,
                value,
                endQuote);
    }

    public XMLAttributeValueModifier modify() {
        return new XMLAttributeValueModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLAttributeValueModifier {
        private final XMLAttributeValue oldNode;
        private Token startQuote;
        private NodeList<Node> value;
        private Token endQuote;

        public XMLAttributeValueModifier(XMLAttributeValue oldNode) {
            this.oldNode = oldNode;
            this.startQuote = oldNode.startQuote();
            this.value = oldNode.value();
            this.endQuote = oldNode.endQuote();
        }

        public XMLAttributeValueModifier withStartQuote(
                Token startQuote) {
            Objects.requireNonNull(startQuote, "startQuote must not be null");
            this.startQuote = startQuote;
            return this;
        }

        public XMLAttributeValueModifier withValue(
                NodeList<Node> value) {
            Objects.requireNonNull(value, "value must not be null");
            this.value = value;
            return this;
        }

        public XMLAttributeValueModifier withEndQuote(
                Token endQuote) {
            Objects.requireNonNull(endQuote, "endQuote must not be null");
            this.endQuote = endQuote;
            return this;
        }

        public XMLAttributeValue apply() {
            return oldNode.modify(
                    startQuote,
                    value,
                    endQuote);
        }
    }
}
