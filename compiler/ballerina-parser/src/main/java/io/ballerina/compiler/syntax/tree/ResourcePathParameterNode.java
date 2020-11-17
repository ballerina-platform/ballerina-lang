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
public class ResourcePathParameterNode extends NonTerminalNode {

    public ResourcePathParameterNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracketToken() {
        return childInBucket(0);
    }

    public TypeDescriptorNode typeDescriptor() {
        return childInBucket(1);
    }

    public Optional<Token> ellipsisToken() {
        return optionalChildInBucket(2);
    }

    public Token paramName() {
        return childInBucket(3);
    }

    public Token closeBracketToken() {
        return childInBucket(4);
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
                "openBracketToken",
                "typeDescriptor",
                "ellipsisToken",
                "paramName",
                "closeBracketToken"};
    }

    public ResourcePathParameterNode modify(
            Token openBracketToken,
            TypeDescriptorNode typeDescriptor,
            Token ellipsisToken,
            Token paramName,
            Token closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken)) {
            return this;
        }

        return NodeFactory.createResourcePathParameterNode(
                openBracketToken,
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken);
    }

    public ResourcePathParameterNodeModifier modify() {
        return new ResourcePathParameterNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ResourcePathParameterNodeModifier {
        private final ResourcePathParameterNode oldNode;
        private Token openBracketToken;
        private TypeDescriptorNode typeDescriptor;
        private Token ellipsisToken;
        private Token paramName;
        private Token closeBracketToken;

        public ResourcePathParameterNodeModifier(ResourcePathParameterNode oldNode) {
            this.oldNode = oldNode;
            this.openBracketToken = oldNode.openBracketToken();
            this.typeDescriptor = oldNode.typeDescriptor();
            this.ellipsisToken = oldNode.ellipsisToken().orElse(null);
            this.paramName = oldNode.paramName();
            this.closeBracketToken = oldNode.closeBracketToken();
        }

        public ResourcePathParameterNodeModifier withOpenBracketToken(
                Token openBracketToken) {
            Objects.requireNonNull(openBracketToken, "openBracketToken must not be null");
            this.openBracketToken = openBracketToken;
            return this;
        }

        public ResourcePathParameterNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public ResourcePathParameterNodeModifier withEllipsisToken(
                Token ellipsisToken) {
            this.ellipsisToken = ellipsisToken;
            return this;
        }

        public ResourcePathParameterNodeModifier withParamName(
                Token paramName) {
            Objects.requireNonNull(paramName, "paramName must not be null");
            this.paramName = paramName;
            return this;
        }

        public ResourcePathParameterNodeModifier withCloseBracketToken(
                Token closeBracketToken) {
            Objects.requireNonNull(closeBracketToken, "closeBracketToken must not be null");
            this.closeBracketToken = closeBracketToken;
            return this;
        }

        public ResourcePathParameterNode apply() {
            return oldNode.modify(
                    openBracketToken,
                    typeDescriptor,
                    ellipsisToken,
                    paramName,
                    closeBracketToken);
        }
    }
}
