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
public class XMLElementNode extends XMLItemNode {

    public XMLElementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public XMLStartTagNode startTag() {
        return childInBucket(0);
    }

    public NodeList<XMLItemNode> content() {
        return new NodeList<>(childInBucket(1));
    }

    public XMLEndTagNode endTag() {
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
                "startTag",
                "content",
                "endTag"};
    }

    public XMLElementNode modify(
            XMLStartTagNode startTag,
            NodeList<XMLItemNode> content,
            XMLEndTagNode endTag) {
        if (checkForReferenceEquality(
                startTag,
                content.underlyingListNode(),
                endTag)) {
            return this;
        }

        return NodeFactory.createXMLElementNode(
                startTag,
                content,
                endTag);
    }

    public XMLElementNodeModifier modify() {
        return new XMLElementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLElementNodeModifier {
        private final XMLElementNode oldNode;
        private XMLStartTagNode startTag;
        private NodeList<XMLItemNode> content;
        private XMLEndTagNode endTag;

        public XMLElementNodeModifier(XMLElementNode oldNode) {
            this.oldNode = oldNode;
            this.startTag = oldNode.startTag();
            this.content = oldNode.content();
            this.endTag = oldNode.endTag();
        }

        public XMLElementNodeModifier withStartTag(
                XMLStartTagNode startTag) {
            Objects.requireNonNull(startTag, "startTag must not be null");
            this.startTag = startTag;
            return this;
        }

        public XMLElementNodeModifier withContent(
                NodeList<XMLItemNode> content) {
            Objects.requireNonNull(content, "content must not be null");
            this.content = content;
            return this;
        }

        public XMLElementNodeModifier withEndTag(
                XMLEndTagNode endTag) {
            Objects.requireNonNull(endTag, "endTag must not be null");
            this.endTag = endTag;
            return this;
        }

        public XMLElementNode apply() {
            return oldNode.modify(
                    startTag,
                    content,
                    endTag);
        }
    }
}
