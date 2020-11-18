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
public class DefaultableParameterNode extends ParameterNode {

    public DefaultableParameterNode(STNode internalNode, int position, NonTerminalNode parent) {
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

    public Token equalsToken() {
        return childInBucket(3);
    }

    public Node expression() {
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
                "annotations",
                "typeName",
                "paramName",
                "equalsToken",
                "expression"};
    }

    public DefaultableParameterNode modify(
            NodeList<AnnotationNode> annotations,
            Node typeName,
            Token paramName,
            Token equalsToken,
            Node expression) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                typeName,
                paramName,
                equalsToken,
                expression)) {
            return this;
        }

        return NodeFactory.createDefaultableParameterNode(
                annotations,
                typeName,
                paramName,
                equalsToken,
                expression);
    }

    public DefaultableParameterNodeModifier modify() {
        return new DefaultableParameterNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class DefaultableParameterNodeModifier {
        private final DefaultableParameterNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private Node typeName;
        private Token paramName;
        private Token equalsToken;
        private Node expression;

        public DefaultableParameterNodeModifier(DefaultableParameterNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.typeName = oldNode.typeName();
            this.paramName = oldNode.paramName().orElse(null);
            this.equalsToken = oldNode.equalsToken();
            this.expression = oldNode.expression();
        }

        public DefaultableParameterNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public DefaultableParameterNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public DefaultableParameterNodeModifier withParamName(
                Token paramName) {
            Objects.requireNonNull(paramName, "paramName must not be null");
            this.paramName = paramName;
            return this;
        }

        public DefaultableParameterNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public DefaultableParameterNodeModifier withExpression(
                Node expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public DefaultableParameterNode apply() {
            return oldNode.modify(
                    annotations,
                    typeName,
                    paramName,
                    equalsToken,
                    expression);
        }
    }
}
