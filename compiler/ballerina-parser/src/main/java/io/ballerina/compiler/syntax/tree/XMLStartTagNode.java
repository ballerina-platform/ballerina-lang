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
public class XMLStartTagNode extends XMLElementTagNode {

    public XMLStartTagNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ltToken() {
        return childInBucket(0);
    }

    public XMLNameNode name() {
        return childInBucket(1);
    }

    public NodeList<XMLAttributeNode> attributes() {
        return new NodeList<>(childInBucket(2));
    }

    public Token getToken() {
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
                "name",
                "attributes",
                "getToken"};
    }

    public XMLStartTagNode modify(
            Token ltToken,
            XMLNameNode name,
            NodeList<XMLAttributeNode> attributes,
            Token getToken) {
        if (checkForReferenceEquality(
                ltToken,
                name,
                attributes.underlyingListNode(),
                getToken)) {
            return this;
        }

        return NodeFactory.createXMLStartTagNode(
                ltToken,
                name,
                attributes,
                getToken);
    }

    public XMLStartTagNodeModifier modify() {
        return new XMLStartTagNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLStartTagNodeModifier {
        private final XMLStartTagNode oldNode;
        private Token ltToken;
        private XMLNameNode name;
        private NodeList<XMLAttributeNode> attributes;
        private Token getToken;

        public XMLStartTagNodeModifier(XMLStartTagNode oldNode) {
            this.oldNode = oldNode;
            this.ltToken = oldNode.ltToken();
            this.name = oldNode.name();
            this.attributes = oldNode.attributes();
            this.getToken = oldNode.getToken();
        }

        public XMLStartTagNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public XMLStartTagNodeModifier withName(
                XMLNameNode name) {
            Objects.requireNonNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public XMLStartTagNodeModifier withAttributes(
                NodeList<XMLAttributeNode> attributes) {
            Objects.requireNonNull(attributes, "attributes must not be null");
            this.attributes = attributes;
            return this;
        }

        public XMLStartTagNodeModifier withGetToken(
                Token getToken) {
            Objects.requireNonNull(getToken, "getToken must not be null");
            this.getToken = getToken;
            return this;
        }

        public XMLStartTagNode apply() {
            return oldNode.modify(
                    ltToken,
                    name,
                    attributes,
                    getToken);
        }
    }
}
