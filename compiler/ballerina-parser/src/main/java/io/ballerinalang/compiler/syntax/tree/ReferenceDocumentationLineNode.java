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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ReferenceDocumentationLineNode extends DocumentationNode {

    public ReferenceDocumentationLineNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token hashToken() {
        return childInBucket(0);
    }

    public NodeList<Node> referenceOrDescription() {
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
                "referenceOrDescription"};
    }

    public ReferenceDocumentationLineNode modify(
            Token hashToken,
            NodeList<Node> referenceOrDescription) {
        if (checkForReferenceEquality(
                hashToken,
                referenceOrDescription.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createReferenceDocumentationLineNode(
                hashToken,
                referenceOrDescription);
    }

    public ReferenceDocumentationLineNodeModifier modify() {
        return new ReferenceDocumentationLineNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReferenceDocumentationLineNodeModifier {
        private final ReferenceDocumentationLineNode oldNode;
        private Token hashToken;
        private NodeList<Node> referenceOrDescription;

        public ReferenceDocumentationLineNodeModifier(ReferenceDocumentationLineNode oldNode) {
            this.oldNode = oldNode;
            this.hashToken = oldNode.hashToken();
            this.referenceOrDescription = oldNode.referenceOrDescription();
        }

        public ReferenceDocumentationLineNodeModifier withHashToken(
                Token hashToken) {
            Objects.requireNonNull(hashToken, "hashToken must not be null");
            this.hashToken = hashToken;
            return this;
        }

        public ReferenceDocumentationLineNodeModifier withReferenceOrDescription(
                NodeList<Node> referenceOrDescription) {
            Objects.requireNonNull(referenceOrDescription, "referenceOrDescription must not be null");
            this.referenceOrDescription = referenceOrDescription;
            return this;
        }

        public ReferenceDocumentationLineNode apply() {
            return oldNode.modify(
                    hashToken,
                    referenceOrDescription);
        }
    }
}
