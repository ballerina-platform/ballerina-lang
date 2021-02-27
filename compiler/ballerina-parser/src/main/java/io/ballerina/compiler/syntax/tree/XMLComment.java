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
public class XMLComment extends XMLItemNode {

    public XMLComment(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token commentStart() {
        return childInBucket(0);
    }

    public NodeList<Node> content() {
        return new NodeList<>(childInBucket(1));
    }

    public Token commentEnd() {
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
                "commentStart",
                "content",
                "commentEnd"};
    }

    public XMLComment modify(
            Token commentStart,
            NodeList<Node> content,
            Token commentEnd) {
        if (checkForReferenceEquality(
                commentStart,
                content.underlyingListNode(),
                commentEnd)) {
            return this;
        }

        return NodeFactory.createXMLComment(
                commentStart,
                content,
                commentEnd);
    }

    public XMLCommentModifier modify() {
        return new XMLCommentModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class XMLCommentModifier {
        private final XMLComment oldNode;
        private Token commentStart;
        private NodeList<Node> content;
        private Token commentEnd;

        public XMLCommentModifier(XMLComment oldNode) {
            this.oldNode = oldNode;
            this.commentStart = oldNode.commentStart();
            this.content = oldNode.content();
            this.commentEnd = oldNode.commentEnd();
        }

        public XMLCommentModifier withCommentStart(
                Token commentStart) {
            Objects.requireNonNull(commentStart, "commentStart must not be null");
            this.commentStart = commentStart;
            return this;
        }

        public XMLCommentModifier withContent(
                NodeList<Node> content) {
            Objects.requireNonNull(content, "content must not be null");
            this.content = content;
            return this;
        }

        public XMLCommentModifier withCommentEnd(
                Token commentEnd) {
            Objects.requireNonNull(commentEnd, "commentEnd must not be null");
            this.commentEnd = commentEnd;
            return this;
        }

        public XMLComment apply() {
            return oldNode.modify(
                    commentStart,
                    content,
                    commentEnd);
        }
    }
}
