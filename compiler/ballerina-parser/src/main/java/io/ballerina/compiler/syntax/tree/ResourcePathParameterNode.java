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

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(1));
    }

    public TypeDescriptorNode typeDescriptor() {
        return childInBucket(2);
    }

    public Optional<Token> ellipsisToken() {
        return optionalChildInBucket(3);
    }

    public Optional<Token> paramName() {
        return optionalChildInBucket(4);
    }

    public Token closeBracketToken() {
        return childInBucket(5);
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
                "annotations",
                "typeDescriptor",
                "ellipsisToken",
                "paramName",
                "closeBracketToken"};
    }

    public ResourcePathParameterNode modify(
            SyntaxKind kind,
            Token openBracketToken,
            NodeList<AnnotationNode> annotations,
            TypeDescriptorNode typeDescriptor,
            Token ellipsisToken,
            Token paramName,
            Token closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                annotations.underlyingListNode(),
                typeDescriptor,
                ellipsisToken,
                paramName,
                closeBracketToken)) {
            return this;
        }

        return NodeFactory.createResourcePathParameterNode(
                kind,
                openBracketToken,
                annotations,
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
        private NodeList<AnnotationNode> annotations;
        private TypeDescriptorNode typeDescriptor;
        private Token ellipsisToken;
        private Token paramName;
        private Token closeBracketToken;

        public ResourcePathParameterNodeModifier(ResourcePathParameterNode oldNode) {
            this.oldNode = oldNode;
            this.openBracketToken = oldNode.openBracketToken();
            this.annotations = oldNode.annotations();
            this.typeDescriptor = oldNode.typeDescriptor();
            this.ellipsisToken = oldNode.ellipsisToken().orElse(null);
            this.paramName = oldNode.paramName().orElse(null);
            this.closeBracketToken = oldNode.closeBracketToken();
        }

        public ResourcePathParameterNodeModifier withOpenBracketToken(
                Token openBracketToken) {
            Objects.requireNonNull(openBracketToken, "openBracketToken must not be null");
            this.openBracketToken = openBracketToken;
            return this;
        }

        public ResourcePathParameterNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
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
                    oldNode.kind(),
                    openBracketToken,
                    annotations,
                    typeDescriptor,
                    ellipsisToken,
                    paramName,
                    closeBracketToken);
        }
    }
}
