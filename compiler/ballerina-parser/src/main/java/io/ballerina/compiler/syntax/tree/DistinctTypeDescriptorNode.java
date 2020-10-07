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
public class DistinctTypeDescriptorNode extends TypeDescriptorNode {

    public DistinctTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token distinctKeyword() {
        return childInBucket(0);
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
                "distinctKeyword",
                "typeDescriptor"};
    }

    public DistinctTypeDescriptorNode modify(
            Token distinctKeyword,
            TypeDescriptorNode typeDescriptor) {
        if (checkForReferenceEquality(
                distinctKeyword,
                typeDescriptor)) {
            return this;
        }

        return NodeFactory.createDistinctTypeDescriptorNode(
                distinctKeyword,
                typeDescriptor);
    }

    public DistinctTypeDescriptorNodeModifier modify() {
        return new DistinctTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class DistinctTypeDescriptorNodeModifier {
        private final DistinctTypeDescriptorNode oldNode;
        private Token distinctKeyword;
        private TypeDescriptorNode typeDescriptor;

        public DistinctTypeDescriptorNodeModifier(DistinctTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.distinctKeyword = oldNode.distinctKeyword();
            this.typeDescriptor = oldNode.typeDescriptor();
        }

        public DistinctTypeDescriptorNodeModifier withDistinctKeyword(
                Token distinctKeyword) {
            Objects.requireNonNull(distinctKeyword, "distinctKeyword must not be null");
            this.distinctKeyword = distinctKeyword;
            return this;
        }

        public DistinctTypeDescriptorNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public DistinctTypeDescriptorNode apply() {
            return oldNode.modify(
                    distinctKeyword,
                    typeDescriptor);
        }
    }
}
