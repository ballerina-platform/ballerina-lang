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
public class TypedBindingPatternNode extends NonTerminalNode {

    public TypedBindingPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public TypeDescriptorNode typeDescriptor() {
        return childInBucket(0);
    }

    public BindingPatternNode bindingPattern() {
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
                "typeDescriptor",
                "bindingPattern"};
    }

    public TypedBindingPatternNode modify(
            TypeDescriptorNode typeDescriptor,
            BindingPatternNode bindingPattern) {
        if (checkForReferenceEquality(
                typeDescriptor,
                bindingPattern)) {
            return this;
        }

        return NodeFactory.createTypedBindingPatternNode(
                typeDescriptor,
                bindingPattern);
    }

    public TypedBindingPatternNodeModifier modify() {
        return new TypedBindingPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TypedBindingPatternNodeModifier {
        private final TypedBindingPatternNode oldNode;
        private TypeDescriptorNode typeDescriptor;
        private BindingPatternNode bindingPattern;

        public TypedBindingPatternNodeModifier(TypedBindingPatternNode oldNode) {
            this.oldNode = oldNode;
            this.typeDescriptor = oldNode.typeDescriptor();
            this.bindingPattern = oldNode.bindingPattern();
        }

        public TypedBindingPatternNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public TypedBindingPatternNodeModifier withBindingPattern(
                BindingPatternNode bindingPattern) {
            Objects.requireNonNull(bindingPattern, "bindingPattern must not be null");
            this.bindingPattern = bindingPattern;
            return this;
        }

        public TypedBindingPatternNode apply() {
            return oldNode.modify(
                    typeDescriptor,
                    bindingPattern);
        }
    }
}
