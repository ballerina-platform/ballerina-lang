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
public class ServiceConstructorExpressionNode extends ExpressionNode {

    public ServiceConstructorExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public Token serviceKeyword() {
        return childInBucket(1);
    }

    public Node serviceBody() {
        return childInBucket(2);
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
                "serviceKeyword",
                "serviceBody"};
    }

    public ServiceConstructorExpressionNode modify(
            NodeList<AnnotationNode> annotations,
            Token serviceKeyword,
            Node serviceBody) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                serviceKeyword,
                serviceBody)) {
            return this;
        }

        return NodeFactory.createServiceConstructorExpressionNode(
                annotations,
                serviceKeyword,
                serviceBody);
    }

    public ServiceConstructorExpressionNodeModifier modify() {
        return new ServiceConstructorExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ServiceConstructorExpressionNodeModifier {
        private final ServiceConstructorExpressionNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private Token serviceKeyword;
        private Node serviceBody;

        public ServiceConstructorExpressionNodeModifier(ServiceConstructorExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.serviceKeyword = oldNode.serviceKeyword();
            this.serviceBody = oldNode.serviceBody();
        }

        public ServiceConstructorExpressionNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public ServiceConstructorExpressionNodeModifier withServiceKeyword(
                Token serviceKeyword) {
            Objects.requireNonNull(serviceKeyword, "serviceKeyword must not be null");
            this.serviceKeyword = serviceKeyword;
            return this;
        }

        public ServiceConstructorExpressionNodeModifier withServiceBody(
                Node serviceBody) {
            Objects.requireNonNull(serviceBody, "serviceBody must not be null");
            this.serviceBody = serviceBody;
            return this;
        }

        public ServiceConstructorExpressionNode apply() {
            return oldNode.modify(
                    annotations,
                    serviceKeyword,
                    serviceBody);
        }
    }
}
