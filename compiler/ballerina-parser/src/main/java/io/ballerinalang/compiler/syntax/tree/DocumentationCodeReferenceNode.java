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
public class DocumentationCodeReferenceNode extends DocumentationNode {

    public DocumentationCodeReferenceNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startHigherOrderBacktick() {
        return childInBucket(0);
    }

    public Token backtickContent() {
        return childInBucket(1);
    }

    public Token endHigherOrderBacktick() {
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
                "startHigherOrderBacktick",
                "backtickContent",
                "endHigherOrderBacktick"};
    }

    public DocumentationCodeReferenceNode modify(
            Token startHigherOrderBacktick,
            Token backtickContent,
            Token endHigherOrderBacktick) {
        if (checkForReferenceEquality(
                startHigherOrderBacktick,
                backtickContent,
                endHigherOrderBacktick)) {
            return this;
        }

        return NodeFactory.createDocumentationCodeReferenceNode(
                startHigherOrderBacktick,
                backtickContent,
                endHigherOrderBacktick);
    }

    public DocumentationCodeReferenceNodeModifier modify() {
        return new DocumentationCodeReferenceNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class DocumentationCodeReferenceNodeModifier {
        private final DocumentationCodeReferenceNode oldNode;
        private Token startHigherOrderBacktick;
        private Token backtickContent;
        private Token endHigherOrderBacktick;

        public DocumentationCodeReferenceNodeModifier(DocumentationCodeReferenceNode oldNode) {
            this.oldNode = oldNode;
            this.startHigherOrderBacktick = oldNode.startHigherOrderBacktick();
            this.backtickContent = oldNode.backtickContent();
            this.endHigherOrderBacktick = oldNode.endHigherOrderBacktick();
        }

        public DocumentationCodeReferenceNodeModifier withStartHigherOrderBacktick(
                Token startHigherOrderBacktick) {
            Objects.requireNonNull(startHigherOrderBacktick, "startHigherOrderBacktick must not be null");
            this.startHigherOrderBacktick = startHigherOrderBacktick;
            return this;
        }

        public DocumentationCodeReferenceNodeModifier withBacktickContent(
                Token backtickContent) {
            Objects.requireNonNull(backtickContent, "backtickContent must not be null");
            this.backtickContent = backtickContent;
            return this;
        }

        public DocumentationCodeReferenceNodeModifier withEndHigherOrderBacktick(
                Token endHigherOrderBacktick) {
            Objects.requireNonNull(endHigherOrderBacktick, "endHigherOrderBacktick must not be null");
            this.endHigherOrderBacktick = endHigherOrderBacktick;
            return this;
        }

        public DocumentationCodeReferenceNode apply() {
            return oldNode.modify(
                    startHigherOrderBacktick,
                    backtickContent,
                    endHigherOrderBacktick);
        }
    }
}
