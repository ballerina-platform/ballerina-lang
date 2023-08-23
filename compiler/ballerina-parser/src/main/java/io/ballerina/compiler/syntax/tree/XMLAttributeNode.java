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
public class XMLAttributeNode extends NonTerminalNode {

    public XMLAttributeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public XMLNameNode attributeName() {
        return childInBucket(0);
    }

    public Token equalToken() {
        return childInBucket(1);
    }

    public XMLAttributeValue value() {
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
                "attributeName",
                "equalToken",
                "value"};
    }

    public XMLAttributeNode modify(
            XMLNameNode attributeName,
            Token equalToken,
            XMLAttributeValue value) {
        if (checkForReferenceEquality(
                attributeName,
                equalToken,
                value)) {
            return this;
        }

        return NodeFactory.createXMLAttributeNode(
                attributeName,
                equalToken,
                value);
    }

    public XMLAttributeNodeModifier modify() {
        return new XMLAttributeNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLAttributeNodeModifier {
        private final XMLAttributeNode oldNode;
        private XMLNameNode attributeName;
        private Token equalToken;
        private XMLAttributeValue value;

        public XMLAttributeNodeModifier(XMLAttributeNode oldNode) {
            this.oldNode = oldNode;
            this.attributeName = oldNode.attributeName();
            this.equalToken = oldNode.equalToken();
            this.value = oldNode.value();
        }

        public XMLAttributeNodeModifier withAttributeName(
                XMLNameNode attributeName) {
            Objects.requireNonNull(attributeName, "attributeName must not be null");
            this.attributeName = attributeName;
            return this;
        }

        public XMLAttributeNodeModifier withEqualToken(
                Token equalToken) {
            Objects.requireNonNull(equalToken, "equalToken must not be null");
            this.equalToken = equalToken;
            return this;
        }

        public XMLAttributeNodeModifier withValue(
                XMLAttributeValue value) {
            Objects.requireNonNull(value, "value must not be null");
            this.value = value;
            return this;
        }

        public XMLAttributeNode apply() {
            return oldNode.modify(
                    attributeName,
                    equalToken,
                    value);
        }
    }
}
