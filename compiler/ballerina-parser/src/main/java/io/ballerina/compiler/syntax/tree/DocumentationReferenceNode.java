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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class DocumentationReferenceNode extends DocumentationNode {

    public DocumentationReferenceNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> referenceType() {
        return optionalChildInBucket(0);
    }

    public Token startBacktick() {
        return childInBucket(1);
    }

    public Node backtickContent() {
        return childInBucket(2);
    }

    public Token endBacktick() {
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
                "referenceType",
                "startBacktick",
                "backtickContent",
                "endBacktick"};
    }

    public DocumentationReferenceNode modify(
            Token referenceType,
            Token startBacktick,
            Node backtickContent,
            Token endBacktick) {
        if (checkForReferenceEquality(
                referenceType,
                startBacktick,
                backtickContent,
                endBacktick)) {
            return this;
        }

        return NodeFactory.createDocumentationReferenceNode(
                referenceType,
                startBacktick,
                backtickContent,
                endBacktick);
    }

    public DocumentationReferenceNodeModifier modify() {
        return new DocumentationReferenceNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class DocumentationReferenceNodeModifier {
        private final DocumentationReferenceNode oldNode;
        private Token referenceType;
        private Token startBacktick;
        private Node backtickContent;
        private Token endBacktick;

        public DocumentationReferenceNodeModifier(DocumentationReferenceNode oldNode) {
            this.oldNode = oldNode;
            this.referenceType = oldNode.referenceType().orElse(null);
            this.startBacktick = oldNode.startBacktick();
            this.backtickContent = oldNode.backtickContent();
            this.endBacktick = oldNode.endBacktick();
        }

        public DocumentationReferenceNodeModifier withReferenceType(
                Token referenceType) {
            Objects.requireNonNull(referenceType, "referenceType must not be null");
            this.referenceType = referenceType;
            return this;
        }

        public DocumentationReferenceNodeModifier withStartBacktick(
                Token startBacktick) {
            Objects.requireNonNull(startBacktick, "startBacktick must not be null");
            this.startBacktick = startBacktick;
            return this;
        }

        public DocumentationReferenceNodeModifier withBacktickContent(
                Node backtickContent) {
            Objects.requireNonNull(backtickContent, "backtickContent must not be null");
            this.backtickContent = backtickContent;
            return this;
        }

        public DocumentationReferenceNodeModifier withEndBacktick(
                Token endBacktick) {
            Objects.requireNonNull(endBacktick, "endBacktick must not be null");
            this.endBacktick = endBacktick;
            return this;
        }

        public DocumentationReferenceNode apply() {
            return oldNode.modify(
                    referenceType,
                    startBacktick,
                    backtickContent,
                    endBacktick);
        }
    }
}
