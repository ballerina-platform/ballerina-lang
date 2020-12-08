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
public class MarkdownParameterDocumentationLineNode extends DocumentationNode {

    public MarkdownParameterDocumentationLineNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token hashToken() {
        return childInBucket(0);
    }

    public Token plusToken() {
        return childInBucket(1);
    }

    public Token parameterName() {
        return childInBucket(2);
    }

    public Token minusToken() {
        return childInBucket(3);
    }

    public NodeList<Node> documentElements() {
        return new NodeList<>(childInBucket(4));
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
                "plusToken",
                "parameterName",
                "minusToken",
                "documentElements"};
    }

    public MarkdownParameterDocumentationLineNode modify(
            SyntaxKind kind,
            Token hashToken,
            Token plusToken,
            Token parameterName,
            Token minusToken,
            NodeList<Node> documentElements) {
        if (checkForReferenceEquality(
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createMarkdownParameterDocumentationLineNode(
                kind,
                hashToken,
                plusToken,
                parameterName,
                minusToken,
                documentElements);
    }

    public MarkdownParameterDocumentationLineNodeModifier modify() {
        return new MarkdownParameterDocumentationLineNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MarkdownParameterDocumentationLineNodeModifier {
        private final MarkdownParameterDocumentationLineNode oldNode;
        private Token hashToken;
        private Token plusToken;
        private Token parameterName;
        private Token minusToken;
        private NodeList<Node> documentElements;

        public MarkdownParameterDocumentationLineNodeModifier(MarkdownParameterDocumentationLineNode oldNode) {
            this.oldNode = oldNode;
            this.hashToken = oldNode.hashToken();
            this.plusToken = oldNode.plusToken();
            this.parameterName = oldNode.parameterName();
            this.minusToken = oldNode.minusToken();
            this.documentElements = oldNode.documentElements();
        }

        public MarkdownParameterDocumentationLineNodeModifier withHashToken(
                Token hashToken) {
            Objects.requireNonNull(hashToken, "hashToken must not be null");
            this.hashToken = hashToken;
            return this;
        }

        public MarkdownParameterDocumentationLineNodeModifier withPlusToken(
                Token plusToken) {
            Objects.requireNonNull(plusToken, "plusToken must not be null");
            this.plusToken = plusToken;
            return this;
        }

        public MarkdownParameterDocumentationLineNodeModifier withParameterName(
                Token parameterName) {
            Objects.requireNonNull(parameterName, "parameterName must not be null");
            this.parameterName = parameterName;
            return this;
        }

        public MarkdownParameterDocumentationLineNodeModifier withMinusToken(
                Token minusToken) {
            Objects.requireNonNull(minusToken, "minusToken must not be null");
            this.minusToken = minusToken;
            return this;
        }

        public MarkdownParameterDocumentationLineNodeModifier withDocumentElements(
                NodeList<Node> documentElements) {
            Objects.requireNonNull(documentElements, "documentElements must not be null");
            this.documentElements = documentElements;
            return this;
        }

        public MarkdownParameterDocumentationLineNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    hashToken,
                    plusToken,
                    parameterName,
                    minusToken,
                    documentElements);
        }
    }
}
