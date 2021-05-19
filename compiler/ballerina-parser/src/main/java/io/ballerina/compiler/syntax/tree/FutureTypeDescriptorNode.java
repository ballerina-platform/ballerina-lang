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
public class FutureTypeDescriptorNode extends TypeDescriptorNode {

    public FutureTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token futureKeywordToken() {
        return childInBucket(0);
    }

    public Optional<TypeParameterNode> futureTypeParamsNode() {
        return optionalChildInBucket(1);
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
                "futureKeywordToken",
                "futureTypeParamsNode"};
    }

    public FutureTypeDescriptorNode modify(
            Token futureKeywordToken,
            TypeParameterNode futureTypeParamsNode) {
        if (checkForReferenceEquality(
                futureKeywordToken,
                futureTypeParamsNode)) {
            return this;
        }

        return NodeFactory.createFutureTypeDescriptorNode(
                futureKeywordToken,
                futureTypeParamsNode);
    }

    public FutureTypeDescriptorNodeModifier modify() {
        return new FutureTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FutureTypeDescriptorNodeModifier {
        private final FutureTypeDescriptorNode oldNode;
        private Token futureKeywordToken;
        private TypeParameterNode futureTypeParamsNode;

        public FutureTypeDescriptorNodeModifier(FutureTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.futureKeywordToken = oldNode.futureKeywordToken();
            this.futureTypeParamsNode = oldNode.futureTypeParamsNode().orElse(null);
        }

        public FutureTypeDescriptorNodeModifier withFutureKeywordToken(
                Token futureKeywordToken) {
            Objects.requireNonNull(futureKeywordToken, "futureKeywordToken must not be null");
            this.futureKeywordToken = futureKeywordToken;
            return this;
        }

        public FutureTypeDescriptorNodeModifier withFutureTypeParamsNode(
                TypeParameterNode futureTypeParamsNode) {
            this.futureTypeParamsNode = futureTypeParamsNode;
            return this;
        }

        public FutureTypeDescriptorNode apply() {
            return oldNode.modify(
                    futureKeywordToken,
                    futureTypeParamsNode);
        }
    }
}
