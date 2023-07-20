/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
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
public class InferredTypedescDefaultNode extends ExpressionNode {

    public InferredTypedescDefaultNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token ltToken() {
        return childInBucket(0);
    }

    public Token gtToken() {
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
                "ltToken",
                "gtToken"};
    }

    public InferredTypedescDefaultNode modify(
            Token ltToken,
            Token gtToken) {
        if (checkForReferenceEquality(
                ltToken,
                gtToken)) {
            return this;
        }

        return NodeFactory.createInferredTypedescDefaultNode(
                ltToken,
                gtToken);
    }

    public InferredTypedescDefaultNodeModifier modify() {
        return new InferredTypedescDefaultNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class InferredTypedescDefaultNodeModifier {
        private final InferredTypedescDefaultNode oldNode;
        private Token ltToken;
        private Token gtToken;

        public InferredTypedescDefaultNodeModifier(InferredTypedescDefaultNode oldNode) {
            this.oldNode = oldNode;
            this.ltToken = oldNode.ltToken();
            this.gtToken = oldNode.gtToken();
        }

        public InferredTypedescDefaultNodeModifier withLtToken(
                Token ltToken) {
            Objects.requireNonNull(ltToken, "ltToken must not be null");
            this.ltToken = ltToken;
            return this;
        }

        public InferredTypedescDefaultNodeModifier withGtToken(
                Token gtToken) {
            Objects.requireNonNull(gtToken, "gtToken must not be null");
            this.gtToken = gtToken;
            return this;
        }

        public InferredTypedescDefaultNode apply() {
            return oldNode.modify(
                    ltToken,
                    gtToken);
        }
    }
}
