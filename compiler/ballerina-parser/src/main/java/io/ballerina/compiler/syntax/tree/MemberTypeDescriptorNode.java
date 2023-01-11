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
 * @since 2201.4.0
 */
public class MemberTypeDescriptorNode extends NonTerminalNode {

    public MemberTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public TypeDescriptorNode typeDescriptor() {
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
                "typeDescriptor"};
    }

    public MemberTypeDescriptorNode modify(
            NodeList<AnnotationNode> annotations,
            TypeDescriptorNode typeDescriptor) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                typeDescriptor)) {
            return this;
        }

        return NodeFactory.createMemberTypeDescriptorNode(
                annotations,
                typeDescriptor);
    }

    public MemberTypeDescriptorNodeModifier modify() {
        return new MemberTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.4.0
     */
    public static class MemberTypeDescriptorNodeModifier {
        private final MemberTypeDescriptorNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private TypeDescriptorNode typeDescriptor;

        public MemberTypeDescriptorNodeModifier(MemberTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.typeDescriptor = oldNode.typeDescriptor();
        }

        public MemberTypeDescriptorNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public MemberTypeDescriptorNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public MemberTypeDescriptorNode apply() {
            return oldNode.modify(
                    annotations,
                    typeDescriptor);
        }
    }
}
