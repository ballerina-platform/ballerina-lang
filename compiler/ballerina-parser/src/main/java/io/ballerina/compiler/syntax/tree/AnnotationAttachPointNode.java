/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class AnnotationAttachPointNode extends NonTerminalNode {

    public AnnotationAttachPointNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> sourceKeyword() {
        return optionalChildInBucket(0);
    }

    public NodeList<Token> identifiers() {
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
                "sourceKeyword",
                "identifiers"};
    }

    public AnnotationAttachPointNode modify(
            Token sourceKeyword,
            NodeList<Token> identifiers) {
        if (checkForReferenceEquality(
                sourceKeyword,
                identifiers.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createAnnotationAttachPointNode(
                sourceKeyword,
                identifiers);
    }

    public AnnotationAttachPointNodeModifier modify() {
        return new AnnotationAttachPointNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class AnnotationAttachPointNodeModifier {
        private final AnnotationAttachPointNode oldNode;
        @Nullable
        private Token sourceKeyword;
        private NodeList<Token> identifiers;

        public AnnotationAttachPointNodeModifier(AnnotationAttachPointNode oldNode) {
            this.oldNode = oldNode;
            this.sourceKeyword = oldNode.sourceKeyword().orElse(null);
            this.identifiers = oldNode.identifiers();
        }

        public AnnotationAttachPointNodeModifier withSourceKeyword(
                Token sourceKeyword) {
            this.sourceKeyword = sourceKeyword;
            return this;
        }

        public AnnotationAttachPointNodeModifier withIdentifiers(
                NodeList<Token> identifiers) {
            Objects.requireNonNull(identifiers, "identifiers must not be null");
            this.identifiers = identifiers;
            return this;
        }

        public AnnotationAttachPointNode apply() {
            return oldNode.modify(
                    sourceKeyword,
                    identifiers);
        }
    }
}
