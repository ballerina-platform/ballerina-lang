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
public class FunctionTypeDescriptorNode extends TypeDescriptorNode {

    public FunctionTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<Token> qualifierList() {
        return new NodeList<>(childInBucket(0));
    }

    public Token functionKeyword() {
        return childInBucket(1);
    }

    public FunctionSignatureNode functionSignature() {
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
                "qualifierList",
                "functionKeyword",
                "functionSignature"};
    }

    public FunctionTypeDescriptorNode modify(
            NodeList<Token> qualifierList,
            Token functionKeyword,
            FunctionSignatureNode functionSignature) {
        if (checkForReferenceEquality(
                qualifierList.underlyingListNode(),
                functionKeyword,
                functionSignature)) {
            return this;
        }

        return NodeFactory.createFunctionTypeDescriptorNode(
                qualifierList,
                functionKeyword,
                functionSignature);
    }

    public FunctionTypeDescriptorNodeModifier modify() {
        return new FunctionTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FunctionTypeDescriptorNodeModifier {
        private final FunctionTypeDescriptorNode oldNode;
        private NodeList<Token> qualifierList;
        private Token functionKeyword;
        private FunctionSignatureNode functionSignature;

        public FunctionTypeDescriptorNodeModifier(FunctionTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.qualifierList = oldNode.qualifierList();
            this.functionKeyword = oldNode.functionKeyword();
            this.functionSignature = oldNode.functionSignature();
        }

        public FunctionTypeDescriptorNodeModifier withQualifierList(
                NodeList<Token> qualifierList) {
            Objects.requireNonNull(qualifierList, "qualifierList must not be null");
            this.qualifierList = qualifierList;
            return this;
        }

        public FunctionTypeDescriptorNodeModifier withFunctionKeyword(
                Token functionKeyword) {
            Objects.requireNonNull(functionKeyword, "functionKeyword must not be null");
            this.functionKeyword = functionKeyword;
            return this;
        }

        public FunctionTypeDescriptorNodeModifier withFunctionSignature(
                FunctionSignatureNode functionSignature) {
            Objects.requireNonNull(functionSignature, "functionSignature must not be null");
            this.functionSignature = functionSignature;
            return this;
        }

        public FunctionTypeDescriptorNode apply() {
            return oldNode.modify(
                    qualifierList,
                    functionKeyword,
                    functionSignature);
        }
    }
}
