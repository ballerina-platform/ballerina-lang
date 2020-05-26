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
public class ParameterizedTypeDescriptorNode extends TypeDescriptorNode {

    public ParameterizedTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token parameterizedType() {
        return childInBucket(0);
    }

    public Token ltToken() {
        return childInBucket(1);
    }

    public Node typeNode() {
        return childInBucket(2);
    }

    public Token gtToken() {
        return childInBucket(3);
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
                "parameterizedType",
                "ltToken",
                "typeNode",
                "gtToken"};
    }

    public ParameterizedTypeDescriptorNode modify(
            Token parameterizedType,
            Token ltToken,
            Node typeNode,
            Token gtToken) {
        if (checkForReferenceEquality(
                parameterizedType,
                ltToken,
                typeNode,
                gtToken)) {
            return this;
        }

        return NodeFactory.createParameterizedTypeDescriptorNode(
                parameterizedType,
                ltToken,
                typeNode,
                gtToken);
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
        private Token parameterizedType;
        private Token ltToken;
        private Node typeNode;
        private Token gtToken;

        public ParameterizedTypeDescriptorNodeModifier(ParameterizedTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.parameterizedType = oldNode.parameterizedType();
            this.ltToken = oldNode.ltToken();
            this.typeNode = oldNode.typeNode();
            this.gtToken = oldNode.gtToken();
        }

        public ParameterizedTypeDescriptorNodeModifier withParameterizedType(
                Token parameterizedType) {
            Objects.requireNonNull(parameterizedType, "parameterizedType must not be null");
            this.parameterizedType = parameterizedType;
            return this;
        }

        public ParameterizedTypeDescriptorNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public ParameterizedTypeDescriptorNodeModifier withTypeNode(
                Node typeNode) {
            Objects.requireNonNull(typeNode, "typeNode must not be null");
            this.typeNode = typeNode;
            return this;
        }

        public ParameterizedTypeDescriptorNodeModifier withGtToken(
                Token gtToken) {
            Objects.requireNonNull(gtToken, "gtToken must not be null");
            this.gtToken = gtToken;
            return this;
        }

        public ParameterizedTypeDescriptorNode apply() {
            return oldNode.modify(
                    parameterizedType,
                    ltToken,
                    typeNode,
                    gtToken);
        }
    }
}
