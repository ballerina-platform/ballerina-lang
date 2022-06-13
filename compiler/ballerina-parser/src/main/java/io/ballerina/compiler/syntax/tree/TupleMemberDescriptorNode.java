/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.2.0
 */
public class TupleMemberDescriptorNode extends NonTerminalNode {

    public TupleMemberDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public TypeDescriptorNode typeDesc() {
        return childInBucket(1);
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
                "annotations",
                "typeDesc"};
    }

    public TupleMemberDescriptorNode modify(
            NodeList<AnnotationNode> annotations,
            TypeDescriptorNode typeDesc) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                typeDesc)) {
            return this;
        }

        return NodeFactory.createTupleMemberDescriptorNode(
                annotations,
                typeDesc);
    }

    public TupleMemberDescriptorNodeModifier modify() {
        return new TupleMemberDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TupleMemberDescriptorNodeModifier {
        private final TupleMemberDescriptorNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private TypeDescriptorNode typeDesc;

        public TupleMemberDescriptorNodeModifier(TupleMemberDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.typeDesc = oldNode.typeDesc();
        }

        public TupleMemberDescriptorNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public TupleMemberDescriptorNodeModifier withTypeDesc(
                TypeDescriptorNode typeDesc) {
            Objects.requireNonNull(typeDesc, "typeDesc must not be null");
            this.typeDesc = typeDesc;
            return this;
        }

        public TupleMemberDescriptorNode apply() {
            return oldNode.modify(
                    annotations,
                    typeDesc);
        }
    }
}
