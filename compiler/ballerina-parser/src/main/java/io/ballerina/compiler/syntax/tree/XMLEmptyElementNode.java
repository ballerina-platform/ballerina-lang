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
public class XMLEmptyElementNode extends XMLItemNode {

    public XMLEmptyElementNode(STNode internalNode, int position, NonTerminalNode parent) {
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

    public Token slashToken() {
        return childInBucket(3);
    }

    public Token getToken() {
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
                "ltToken",
                "name",
                "attributes",
                "slashToken",
                "getToken"};
    }

    public XMLEmptyElementNode modify(
            Token ltToken,
            XMLNameNode name,
            NodeList<XMLAttributeNode> attributes,
            Token slashToken,
            Token getToken) {
        if (checkForReferenceEquality(
                ltToken,
                name,
                attributes.underlyingListNode(),
                slashToken,
                getToken)) {
            return this;
        }

        return NodeFactory.createXMLEmptyElementNode(
                ltToken,
                name,
                attributes,
                slashToken,
                getToken);
    }

    public XMLEmptyElementNodeModifier modify() {
        return new XMLEmptyElementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLEmptyElementNodeModifier {
        private final XMLEmptyElementNode oldNode;
        private Token ltToken;
        private XMLNameNode name;
        private NodeList<XMLAttributeNode> attributes;
        private Token slashToken;
        private Token getToken;

        public XMLEmptyElementNodeModifier(XMLEmptyElementNode oldNode) {
            this.oldNode = oldNode;
            this.ltToken = oldNode.ltToken();
            this.name = oldNode.name();
            this.attributes = oldNode.attributes();
            this.slashToken = oldNode.slashToken();
            this.getToken = oldNode.getToken();
        }

        public XMLEmptyElementNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public XMLEmptyElementNodeModifier withName(
                XMLNameNode name) {
            Objects.requireNonNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public XMLEmptyElementNodeModifier withAttributes(
                NodeList<XMLAttributeNode> attributes) {
            Objects.requireNonNull(attributes, "attributes must not be null");
            this.attributes = attributes;
            return this;
        }

        public XMLEmptyElementNodeModifier withSlashToken(
                Token slashToken) {
            Objects.requireNonNull(slashToken, "slashToken must not be null");
            this.slashToken = slashToken;
            return this;
        }

        public XMLEmptyElementNodeModifier withGetToken(
                Token getToken) {
            Objects.requireNonNull(getToken, "getToken must not be null");
            this.getToken = getToken;
            return this;
        }

        public XMLEmptyElementNode apply() {
            return oldNode.modify(
                    ltToken,
                    name,
                    attributes,
                    slashToken,
                    getToken);
        }
    }
}
