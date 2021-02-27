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
public class ParenthesisedTypeDescriptorNode extends TypeDescriptorNode {

    public ParenthesisedTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openParenToken() {
        return childInBucket(0);
    }

    public TypeDescriptorNode typedesc() {
        return childInBucket(1);
    }

    public Token closeParenToken() {
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
                "openParenToken",
                "typedesc",
                "closeParenToken"};
    }

    public ParenthesisedTypeDescriptorNode modify(
            Token openParenToken,
            TypeDescriptorNode typedesc,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                openParenToken,
                typedesc,
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createParenthesisedTypeDescriptorNode(
                openParenToken,
                typedesc,
                closeParenToken);
    }

    public ParenthesisedTypeDescriptorNodeModifier modify() {
        return new ParenthesisedTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ParenthesisedTypeDescriptorNodeModifier {
        private final ParenthesisedTypeDescriptorNode oldNode;
        private Token openParenToken;
        private TypeDescriptorNode typedesc;
        private Token closeParenToken;

        public ParenthesisedTypeDescriptorNodeModifier(ParenthesisedTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.openParenToken = oldNode.openParenToken();
            this.typedesc = oldNode.typedesc();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public ParenthesisedTypeDescriptorNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public ParenthesisedTypeDescriptorNodeModifier withTypedesc(
                TypeDescriptorNode typedesc) {
            Objects.requireNonNull(typedesc, "typedesc must not be null");
            this.typedesc = typedesc;
            return this;
        }

        public ParenthesisedTypeDescriptorNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public ParenthesisedTypeDescriptorNode apply() {
            return oldNode.modify(
                    openParenToken,
                    typedesc,
                    closeParenToken);
        }
    }
}
