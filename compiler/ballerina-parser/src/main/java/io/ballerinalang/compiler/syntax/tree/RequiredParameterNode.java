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
public class RequiredParameterNode extends ParameterNode {

    public RequiredParameterNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> leadingComma() {
        return optionalChildInBucket(0);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(1));
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(2);
    }

    public Node typeName() {
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
                "visibilityQualifier",
                "typeName",
                "paramName"};
    }

    public RequiredParameterNode modify(
            Token leadingComma,
            NodeList<AnnotationNode> annotations,
            Token visibilityQualifier,
            Node typeName,
            Token paramName) {
        if (checkForReferenceEquality(
                leadingComma,
                annotations.underlyingListNode(),
                visibilityQualifier,
                typeName,
                paramName)) {
            return this;
        }

        return NodeFactory.createRequiredParameterNode(
                leadingComma,
                annotations,
                visibilityQualifier,
                typeName,
                paramName);
    }

    public RequiredParameterNodeModifier modify() {
        return new RequiredParameterNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RequiredParameterNodeModifier {
        private final RequiredParameterNode oldNode;
        private Token leadingComma;
        private NodeList<AnnotationNode> annotations;
        private Token visibilityQualifier;
        private Node typeName;
        private Token paramName;

        public RequiredParameterNodeModifier(RequiredParameterNode oldNode) {
            this.oldNode = oldNode;
            this.leadingComma = oldNode.leadingComma().orElse(null);
            this.annotations = oldNode.annotations();
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.typeName = oldNode.typeName();
            this.paramName = oldNode.paramName().orElse(null);
        }

        public RequiredParameterNodeModifier withLeadingComma(
                Token leadingComma) {
            Objects.requireNonNull(leadingComma, "leadingComma must not be null");
            this.leadingComma = leadingComma;
            return this;
        }

        public RequiredParameterNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public RequiredParameterNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public RequiredParameterNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public RequiredParameterNodeModifier withParamName(
                Token paramName) {
            Objects.requireNonNull(paramName, "paramName must not be null");
            this.paramName = paramName;
            return this;
        }

        public RequiredParameterNode apply() {
            return oldNode.modify(
                    leadingComma,
                    annotations,
                    visibilityQualifier,
                    typeName,
                    paramName);
        }
    }
}
