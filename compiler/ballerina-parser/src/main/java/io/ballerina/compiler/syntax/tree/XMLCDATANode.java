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
public class XMLCDATANode extends XMLItemNode {

    public XMLCDATANode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token cdataStart() {
        return childInBucket(0);
    }

    public NodeList<Node> content() {
        return new NodeList<>(childInBucket(1));
    }

    public Token cdataEnd() {
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
                "cdataStart",
                "content",
                "cdataEnd"};
    }

    public XMLCDATANode modify(
            Token cdataStart,
            NodeList<Node> content,
            Token cdataEnd) {
        if (checkForReferenceEquality(
                cdataStart,
                content.underlyingListNode(),
                cdataEnd)) {
            return this;
        }

        return NodeFactory.createXMLCDATANode(
                cdataStart,
                content,
                cdataEnd);
    }

    public XMLCDATANodeModifier modify() {
        return new XMLCDATANodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLCDATANodeModifier {
        private final XMLCDATANode oldNode;
        private Token cdataStart;
        private NodeList<Node> content;
        private Token cdataEnd;

        public XMLCDATANodeModifier(XMLCDATANode oldNode) {
            this.oldNode = oldNode;
            this.cdataStart = oldNode.cdataStart();
            this.content = oldNode.content();
            this.cdataEnd = oldNode.cdataEnd();
        }

        public XMLCDATANodeModifier withCdataStart(
                Token cdataStart) {
            Objects.requireNonNull(cdataStart, "cdataStart must not be null");
            this.cdataStart = cdataStart;
            return this;
        }

        public XMLCDATANodeModifier withContent(
                NodeList<Node> content) {
            Objects.requireNonNull(content, "content must not be null");
            this.content = content;
            return this;
        }

        public XMLCDATANodeModifier withCdataEnd(
                Token cdataEnd) {
            Objects.requireNonNull(cdataEnd, "cdataEnd must not be null");
            this.cdataEnd = cdataEnd;
            return this;
        }

        public XMLCDATANode apply() {
            return oldNode.modify(
                    cdataStart,
                    content,
                    cdataEnd);
        }
    }
}
