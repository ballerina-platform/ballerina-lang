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
public class TypedescTypeDescriptorNode extends TypeDescriptorNode {

    public TypedescTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token typedescKeywordToken() {
        return childInBucket(0);
    }

    public Optional<TypeParameterNode> typedescTypeParamsNode() {
        return optionalChildInBucket(1);
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
                "typedescKeywordToken",
                "typedescTypeParamsNode"};
    }

    public TypedescTypeDescriptorNode modify(
            Token typedescKeywordToken,
            TypeParameterNode typedescTypeParamsNode) {
        if (checkForReferenceEquality(
                typedescKeywordToken,
                typedescTypeParamsNode)) {
            return this;
        }

        return NodeFactory.createTypedescTypeDescriptorNode(
                typedescKeywordToken,
                typedescTypeParamsNode);
    }

    public TypedescTypeDescriptorNodeModifier modify() {
        return new TypedescTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TypedescTypeDescriptorNodeModifier {
        private final TypedescTypeDescriptorNode oldNode;
        private Token typedescKeywordToken;
        private TypeParameterNode typedescTypeParamsNode;

        public TypedescTypeDescriptorNodeModifier(TypedescTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.typedescKeywordToken = oldNode.typedescKeywordToken();
            this.typedescTypeParamsNode = oldNode.typedescTypeParamsNode().orElse(null);
        }

        public TypedescTypeDescriptorNodeModifier withTypedescKeywordToken(
                Token typedescKeywordToken) {
            Objects.requireNonNull(typedescKeywordToken, "typedescKeywordToken must not be null");
            this.typedescKeywordToken = typedescKeywordToken;
            return this;
        }

        public TypedescTypeDescriptorNodeModifier withTypedescTypeParamsNode(
                TypeParameterNode typedescTypeParamsNode) {
            Objects.requireNonNull(typedescTypeParamsNode, "typedescTypeParamsNode must not be null");
            this.typedescTypeParamsNode = typedescTypeParamsNode;
            return this;
        }

        public TypedescTypeDescriptorNode apply() {
            return oldNode.modify(
                    typedescKeywordToken,
                    typedescTypeParamsNode);
        }
    }
}
