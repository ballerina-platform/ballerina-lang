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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ReturnTypeDescriptorNode extends NonTerminalNode {

    public ReturnTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token returnsKeyword() {
        return childInBucket(0);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(1));
    }

    public Node type() {
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
                "returnsKeyword",
                "annotations",
                "type"};
    }

    public ReturnTypeDescriptorNode modify(
            Token returnsKeyword,
            NodeList<AnnotationNode> annotations,
            Node type) {
        if (checkForReferenceEquality(
                returnsKeyword,
                annotations.underlyingListNode(),
                type)) {
            return this;
        }

        return NodeFactory.createReturnTypeDescriptorNode(
                returnsKeyword,
                annotations,
                type);
    }

    public ReturnTypeDescriptorNodeModifier modify() {
        return new ReturnTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReturnTypeDescriptorNodeModifier {
        private final ReturnTypeDescriptorNode oldNode;
        private Token returnsKeyword;
        private NodeList<AnnotationNode> annotations;
        private Node type;

        public ReturnTypeDescriptorNodeModifier(ReturnTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.returnsKeyword = oldNode.returnsKeyword();
            this.annotations = oldNode.annotations();
            this.type = oldNode.type();
        }

        public ReturnTypeDescriptorNodeModifier withReturnsKeyword(
                Token returnsKeyword) {
            Objects.requireNonNull(returnsKeyword, "returnsKeyword must not be null");
            this.returnsKeyword = returnsKeyword;
            return this;
        }

        public ReturnTypeDescriptorNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public ReturnTypeDescriptorNodeModifier withType(
                Node type) {
            Objects.requireNonNull(type, "type must not be null");
            this.type = type;
            return this;
        }

        public ReturnTypeDescriptorNode apply() {
            return oldNode.modify(
                    returnsKeyword,
                    annotations,
                    type);
        }
    }
}
