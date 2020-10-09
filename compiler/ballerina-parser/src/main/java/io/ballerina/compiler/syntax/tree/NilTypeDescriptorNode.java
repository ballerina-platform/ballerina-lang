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
public class NilTypeDescriptorNode extends TypeDescriptorNode {

    public NilTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openParenToken() {
        return childInBucket(0);
    }

    public Token closeParenToken() {
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
                "openParenToken",
                "closeParenToken"};
    }

    public NilTypeDescriptorNode modify(
            Token openParenToken,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                openParenToken,
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createNilTypeDescriptorNode(
                openParenToken,
                closeParenToken);
    }

    public NilTypeDescriptorNodeModifier modify() {
        return new NilTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class NilTypeDescriptorNodeModifier {
        private final NilTypeDescriptorNode oldNode;
        private Token openParenToken;
        private Token closeParenToken;

        public NilTypeDescriptorNodeModifier(NilTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.openParenToken = oldNode.openParenToken();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public NilTypeDescriptorNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public NilTypeDescriptorNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public NilTypeDescriptorNode apply() {
            return oldNode.modify(
                    openParenToken,
                    closeParenToken);
        }
    }
}
