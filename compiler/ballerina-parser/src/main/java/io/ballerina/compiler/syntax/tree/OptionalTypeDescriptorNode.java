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
public class OptionalTypeDescriptorNode extends TypeDescriptorNode {

    public OptionalTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node typeDescriptor() {
        return childInBucket(0);
    }

    public Token questionMarkToken() {
        return childInBucket(1);
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
                "typeDescriptor",
                "questionMarkToken"};
    }

    public OptionalTypeDescriptorNode modify(
            Node typeDescriptor,
            Token questionMarkToken) {
        if (checkForReferenceEquality(
                typeDescriptor,
                questionMarkToken)) {
            return this;
        }

        return NodeFactory.createOptionalTypeDescriptorNode(
                typeDescriptor,
                questionMarkToken);
    }

    public OptionalTypeDescriptorNodeModifier modify() {
        return new OptionalTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OptionalTypeDescriptorNodeModifier {
        private final OptionalTypeDescriptorNode oldNode;
        private Node typeDescriptor;
        private Token questionMarkToken;

        public OptionalTypeDescriptorNodeModifier(OptionalTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.typeDescriptor = oldNode.typeDescriptor();
            this.questionMarkToken = oldNode.questionMarkToken();
        }

        public OptionalTypeDescriptorNodeModifier withTypeDescriptor(
                Node typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public OptionalTypeDescriptorNodeModifier withQuestionMarkToken(
                Token questionMarkToken) {
            Objects.requireNonNull(questionMarkToken, "questionMarkToken must not be null");
            this.questionMarkToken = questionMarkToken;
            return this;
        }

        public OptionalTypeDescriptorNode apply() {
            return oldNode.modify(
                    typeDescriptor,
                    questionMarkToken);
        }
    }
}
