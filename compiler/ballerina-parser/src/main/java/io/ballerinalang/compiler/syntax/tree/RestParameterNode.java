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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class RestParameterNode extends ParameterNode {

    public RestParameterNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> leadingComma() {
        return optionalChildInBucket(0);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(1));
    }

    public Node typeName() {
        return childInBucket(2);
    }

    public Token ellipsisToken() {
        return childInBucket(3);
    }

    public Optional<Token> paramName() {
        return optionalChildInBucket(4);
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
                "leadingComma",
                "annotations",
                "typeName",
                "ellipsisToken",
                "paramName"};
    }

    public RestParameterNode modify(
            Token leadingComma,
            NodeList<AnnotationNode> annotations,
            Node typeName,
            Token ellipsisToken,
            Token paramName) {
        if (checkForReferenceEquality(
                leadingComma,
                annotations.underlyingListNode(),
                typeName,
                ellipsisToken,
                paramName)) {
            return this;
        }

        return NodeFactory.createRestParameterNode(
                leadingComma,
                annotations,
                typeName,
                ellipsisToken,
                paramName);
    }

    public RestParameterNodeModifier modify() {
        return new RestParameterNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RestParameterNodeModifier {
        private final RestParameterNode oldNode;
        private Token leadingComma;
        private NodeList<AnnotationNode> annotations;
        private Node typeName;
        private Token ellipsisToken;
        private Token paramName;

        public RestParameterNodeModifier(RestParameterNode oldNode) {
            this.oldNode = oldNode;
            this.leadingComma = oldNode.leadingComma().orElse(null);
            this.annotations = oldNode.annotations();
            this.typeName = oldNode.typeName();
            this.ellipsisToken = oldNode.ellipsisToken();
            this.paramName = oldNode.paramName().orElse(null);
        }

        public RestParameterNodeModifier withLeadingComma(
                Token leadingComma) {
            Objects.requireNonNull(leadingComma, "leadingComma must not be null");
            this.leadingComma = leadingComma;
            return this;
        }

        public RestParameterNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public RestParameterNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public RestParameterNodeModifier withEllipsisToken(
                Token ellipsisToken) {
            Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");
            this.ellipsisToken = ellipsisToken;
            return this;
        }

        public RestParameterNodeModifier withParamName(
                Token paramName) {
            Objects.requireNonNull(paramName, "paramName must not be null");
            this.paramName = paramName;
            return this;
        }

        public RestParameterNode apply() {
            return oldNode.modify(
                    leadingComma,
                    annotations,
                    typeName,
                    ellipsisToken,
                    paramName);
        }
    }
}
