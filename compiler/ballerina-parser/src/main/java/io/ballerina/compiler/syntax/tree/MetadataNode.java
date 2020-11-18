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
public class MetadataNode extends NonTerminalNode {

    public MetadataNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Node> documentationString() {
        return optionalChildInBucket(0);
    }

    public NodeList<AnnotationNode> annotations() {
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
                "documentationString",
                "annotations"};
    }

    public MetadataNode modify(
            Node documentationString,
            NodeList<AnnotationNode> annotations) {
        if (checkForReferenceEquality(
                documentationString,
                annotations.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createMetadataNode(
                documentationString,
                annotations);
    }

    public MetadataNodeModifier modify() {
        return new MetadataNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MetadataNodeModifier {
        private final MetadataNode oldNode;
        private Node documentationString;
        private NodeList<AnnotationNode> annotations;

        public MetadataNodeModifier(MetadataNode oldNode) {
            this.oldNode = oldNode;
            this.documentationString = oldNode.documentationString().orElse(null);
            this.annotations = oldNode.annotations();
        }

        public MetadataNodeModifier withDocumentationString(
                Node documentationString) {
            Objects.requireNonNull(documentationString, "documentationString must not be null");
            this.documentationString = documentationString;
            return this;
        }

        public MetadataNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public MetadataNode apply() {
            return oldNode.modify(
                    documentationString,
                    annotations);
        }
    }
}
