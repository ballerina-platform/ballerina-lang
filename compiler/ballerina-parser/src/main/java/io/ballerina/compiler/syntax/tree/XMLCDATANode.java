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

    public Token cDATAStart() {
        return childInBucket(0);
    }

    public NodeList<Node> content() {
        return new NodeList<>(childInBucket(1));
    }

    public Token cDATAEnd() {
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
                "cDATAStart",
                "content",
                "cDATAEnd"};
    }

    public XMLCDATANode modify(
            Token cDATAStart,
            NodeList<Node> content,
            Token cDATAEnd) {
        if (checkForReferenceEquality(
                cDATAStart,
                content.underlyingListNode(),
                cDATAEnd)) {
            return this;
        }

        return NodeFactory.createXMLCDATANode(
                cDATAStart,
                content,
                cDATAEnd);
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
        private Token cDATAStart;
        private NodeList<Node> content;
        private Token cDATAEnd;

        public XMLCDATANodeModifier(XMLCDATANode oldNode) {
            this.oldNode = oldNode;
            this.cDATAStart = oldNode.cDATAStart();
            this.content = oldNode.content();
            this.cDATAEnd = oldNode.cDATAEnd();
        }

        public XMLCDATANodeModifier withCDATAStart(
                Token cDATAStart) {
            Objects.requireNonNull(cDATAStart, "cDATAStart must not be null");
            this.cDATAStart = cDATAStart;
            return this;
        }

        public XMLCDATANodeModifier withContent(
                NodeList<Node> content) {
            Objects.requireNonNull(content, "content must not be null");
            this.content = content;
            return this;
        }

        public XMLCDATANodeModifier withCDATAEnd(
                Token cDATAEnd) {
            Objects.requireNonNull(cDATAEnd, "cDATAEnd must not be null");
            this.cDATAEnd = cDATAEnd;
            return this;
        }

        public XMLCDATANode apply() {
            return oldNode.modify(
                    cDATAStart,
                    content,
                    cDATAEnd);
        }
    }
}
