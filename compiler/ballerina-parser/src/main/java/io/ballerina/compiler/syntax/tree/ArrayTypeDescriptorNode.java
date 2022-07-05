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
public class ArrayTypeDescriptorNode extends TypeDescriptorNode {

    public ArrayTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public TypeDescriptorNode memberTypeDesc() {
        return childInBucket(0);
    }

    public NodeList<ArrayDimensionNode> dimensions() {
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
                "memberTypeDesc",
                "dimensions"};
    }

    public ArrayTypeDescriptorNode modify(
            TypeDescriptorNode memberTypeDesc,
            NodeList<ArrayDimensionNode> dimensions) {
        if (checkForReferenceEquality(
                memberTypeDesc,
                dimensions.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createArrayTypeDescriptorNode(
                memberTypeDesc,
                dimensions);
    }

    public ArrayTypeDescriptorNodeModifier modify() {
        return new ArrayTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ArrayTypeDescriptorNodeModifier {
        private final ArrayTypeDescriptorNode oldNode;
        private TypeDescriptorNode memberTypeDesc;
        private NodeList<ArrayDimensionNode> dimensions;

        public ArrayTypeDescriptorNodeModifier(ArrayTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.memberTypeDesc = oldNode.memberTypeDesc();
            this.dimensions = oldNode.dimensions();
        }

        public ArrayTypeDescriptorNodeModifier withMemberTypeDesc(
                TypeDescriptorNode memberTypeDesc) {
            Objects.requireNonNull(memberTypeDesc, "memberTypeDesc must not be null");
            this.memberTypeDesc = memberTypeDesc;
            return this;
        }

        public ArrayTypeDescriptorNodeModifier withDimensions(
                NodeList<ArrayDimensionNode> dimensions) {
            Objects.requireNonNull(dimensions, "dimensions must not be null");
            this.dimensions = dimensions;
            return this;
        }

        public ArrayTypeDescriptorNode apply() {
            return oldNode.modify(
                    memberTypeDesc,
                    dimensions);
        }
    }
}
