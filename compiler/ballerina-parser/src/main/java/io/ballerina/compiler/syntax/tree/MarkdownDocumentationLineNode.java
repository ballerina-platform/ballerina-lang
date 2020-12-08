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
public class MarkdownDocumentationLineNode extends DocumentationNode {

    public MarkdownDocumentationLineNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token hashToken() {
        return childInBucket(0);
    }

    public NodeList<Node> documentElements() {
        return new NodeList<>(childInBucket(1));
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
                "hashToken",
                "documentElements"};
    }

    public MarkdownDocumentationLineNode modify(
            SyntaxKind kind,
            Token hashToken,
            NodeList<Node> documentElements) {
        if (checkForReferenceEquality(
                hashToken,
                documentElements.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createMarkdownDocumentationLineNode(
                kind,
                hashToken,
                documentElements);
    }

    public MarkdownDocumentationLineNodeModifier modify() {
        return new MarkdownDocumentationLineNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MarkdownDocumentationLineNodeModifier {
        private final MarkdownDocumentationLineNode oldNode;
        private Token hashToken;
        private NodeList<Node> documentElements;

        public MarkdownDocumentationLineNodeModifier(MarkdownDocumentationLineNode oldNode) {
            this.oldNode = oldNode;
            this.hashToken = oldNode.hashToken();
            this.documentElements = oldNode.documentElements();
        }

        public MarkdownDocumentationLineNodeModifier withHashToken(
                Token hashToken) {
            Objects.requireNonNull(hashToken, "hashToken must not be null");
            this.hashToken = hashToken;
            return this;
        }

        public MarkdownDocumentationLineNodeModifier withDocumentElements(
                NodeList<Node> documentElements) {
            Objects.requireNonNull(documentElements, "documentElements must not be null");
            this.documentElements = documentElements;
            return this;
        }

        public MarkdownDocumentationLineNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    hashToken,
                    documentElements);
        }
    }
}
