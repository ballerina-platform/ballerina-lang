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
public class AsyncActionNode extends ActionNode {

    public AsyncActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public ExpressionNode expression() {
        return childInBucket(0);
    }

    public Token rightArrowToken() {
        return childInBucket(1);
    }

    public Token peerWorker() {
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
                "expression",
                "rightArrowToken",
                "peerWorker"};
    }

    public AsyncActionNode modify(
            ExpressionNode expression,
            Token rightArrowToken,
            Token peerWorker) {
        if (checkForReferenceEquality(
                expression,
                rightArrowToken,
                peerWorker)) {
            return this;
        }

        return NodeFactory.createAsyncActionNode(
                expression,
                rightArrowToken,
                peerWorker);
    }

    public AsyncActionNodeModifier modify() {
        return new AsyncActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class AsyncActionNodeModifier {
        private final AsyncActionNode oldNode;
        private ExpressionNode expression;
        private Token rightArrowToken;
        private Token peerWorker;

        public AsyncActionNodeModifier(AsyncActionNode oldNode) {
            this.oldNode = oldNode;
            this.expression = oldNode.expression();
            this.rightArrowToken = oldNode.rightArrowToken();
            this.peerWorker = oldNode.peerWorker();
        }

        public AsyncActionNodeModifier withExpression(ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public AsyncActionNodeModifier withRightArrowToken(Token rightArrowToken) {
            Objects.requireNonNull(rightArrowToken, "rightArrowToken must not be null");
            this.rightArrowToken = rightArrowToken;
            return this;
        }

        public AsyncActionNodeModifier withPeerWorker(Token peerWorker) {
            Objects.requireNonNull(peerWorker, "peerWorker must not be null");
            this.peerWorker = peerWorker;
            return this;
        }

        public AsyncActionNode apply() {
            return oldNode.modify(
                    expression,
                    rightArrowToken,
                    peerWorker);
        }
    }
}
