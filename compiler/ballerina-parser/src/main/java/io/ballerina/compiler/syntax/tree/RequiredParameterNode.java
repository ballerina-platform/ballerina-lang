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
public class RequiredParameterNode extends ParameterNode {

    public RequiredParameterNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public Node typeName() {
        return childInBucket(1);
    }

    public Optional<Token> paramName() {
        return optionalChildInBucket(2);
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
                "annotations",
                "typeName",
                "paramName"};
    }

    public RequiredParameterNode modify(
            NodeList<AnnotationNode> annotations,
            Node typeName,
            Token paramName) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                typeName,
                paramName)) {
            return this;
        }

        return NodeFactory.createRequiredParameterNode(
                annotations,
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
        private NodeList<AnnotationNode> annotations;
        private Node typeName;
        private Token paramName;

        public RequiredParameterNodeModifier(RequiredParameterNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.typeName = oldNode.typeName();
            this.paramName = oldNode.paramName().orElse(null);
        }

        public RequiredParameterNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
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
                    annotations,
                    typeName,
                    paramName);
        }
    }
}
