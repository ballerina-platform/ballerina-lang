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
public class ParameterizedTypeDescriptorNode extends TypeDescriptorNode {

    public ParameterizedTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token keywordToken() {
        return childInBucket(0);
    }

    public Optional<TypeParameterNode> typeParamNode() {
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
                "keywordToken",
                "typeParamNode"};
    }

    public ParameterizedTypeDescriptorNode modify(
            SyntaxKind kind,
            Token keywordToken,
            TypeParameterNode typeParamNode) {
        if (checkForReferenceEquality(
                keywordToken,
                typeParamNode)) {
            return this;
        }

        return NodeFactory.createParameterizedTypeDescriptorNode(
                kind,
                keywordToken,
                typeParamNode);
    }

    public ParameterizedTypeDescriptorNodeModifier modify() {
        return new ParameterizedTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ParameterizedTypeDescriptorNodeModifier {
        private final ParameterizedTypeDescriptorNode oldNode;
        private Token keywordToken;
        @Nullable
        private TypeParameterNode typeParamNode;

        public ParameterizedTypeDescriptorNodeModifier(ParameterizedTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.keywordToken = oldNode.keywordToken();
            this.typeParamNode = oldNode.typeParamNode().orElse(null);
        }

        public ParameterizedTypeDescriptorNodeModifier withKeywordToken(
                Token keywordToken) {
            Objects.requireNonNull(keywordToken, "keywordToken must not be null");
            this.keywordToken = keywordToken;
            return this;
        }

        public ParameterizedTypeDescriptorNodeModifier withTypeParamNode(
                TypeParameterNode typeParamNode) {
            this.typeParamNode = typeParamNode;
            return this;
        }

        public ParameterizedTypeDescriptorNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    keywordToken,
                    typeParamNode);
        }
    }
}
