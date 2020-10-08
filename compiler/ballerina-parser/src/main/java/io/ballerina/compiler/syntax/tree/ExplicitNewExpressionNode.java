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
public class ExplicitNewExpressionNode extends NewExpressionNode {

    public ExplicitNewExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token newKeyword() {
        return childInBucket(0);
    }

    public TypeDescriptorNode typeDescriptor() {
        return childInBucket(1);
    }

    public ParenthesizedArgList parenthesizedArgList() {
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
                "newKeyword",
                "typeDescriptor",
                "parenthesizedArgList"};
    }

    public ExplicitNewExpressionNode modify(
            Token newKeyword,
            TypeDescriptorNode typeDescriptor,
            ParenthesizedArgList parenthesizedArgList) {
        if (checkForReferenceEquality(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList)) {
            return this;
        }

        return NodeFactory.createExplicitNewExpressionNode(
                newKeyword,
                typeDescriptor,
                parenthesizedArgList);
    }

    public ExplicitNewExpressionNodeModifier modify() {
        return new ExplicitNewExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ExplicitNewExpressionNodeModifier {
        private final ExplicitNewExpressionNode oldNode;
        private Token newKeyword;
        private TypeDescriptorNode typeDescriptor;
        private ParenthesizedArgList parenthesizedArgList;

        public ExplicitNewExpressionNodeModifier(ExplicitNewExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.newKeyword = oldNode.newKeyword();
            this.typeDescriptor = oldNode.typeDescriptor();
            this.parenthesizedArgList = oldNode.parenthesizedArgList();
        }

        public ExplicitNewExpressionNodeModifier withNewKeyword(
                Token newKeyword) {
            Objects.requireNonNull(newKeyword, "newKeyword must not be null");
            this.newKeyword = newKeyword;
            return this;
        }

        public ExplicitNewExpressionNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            Objects.requireNonNull(typeDescriptor, "typeDescriptor must not be null");
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public ExplicitNewExpressionNodeModifier withParenthesizedArgList(
                ParenthesizedArgList parenthesizedArgList) {
            Objects.requireNonNull(parenthesizedArgList, "parenthesizedArgList must not be null");
            this.parenthesizedArgList = parenthesizedArgList;
            return this;
        }

        public ExplicitNewExpressionNode apply() {
            return oldNode.modify(
                    newKeyword,
                    typeDescriptor,
                    parenthesizedArgList);
        }
    }
}
