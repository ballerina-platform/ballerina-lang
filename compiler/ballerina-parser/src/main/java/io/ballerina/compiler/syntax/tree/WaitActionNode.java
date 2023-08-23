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
public class WaitActionNode extends ActionNode {

    public WaitActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token waitKeyword() {
        return childInBucket(0);
    }

    public Node waitFutureExpr() {
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
                "waitKeyword",
                "waitFutureExpr"};
    }

    public WaitActionNode modify(
            Token waitKeyword,
            Node waitFutureExpr) {
        if (checkForReferenceEquality(
                waitKeyword,
                waitFutureExpr)) {
            return this;
        }

        return NodeFactory.createWaitActionNode(
                waitKeyword,
                waitFutureExpr);
    }

    public WaitActionNodeModifier modify() {
        return new WaitActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class WaitActionNodeModifier {
        private final WaitActionNode oldNode;
        private Token waitKeyword;
        private Node waitFutureExpr;

        public WaitActionNodeModifier(WaitActionNode oldNode) {
            this.oldNode = oldNode;
            this.waitKeyword = oldNode.waitKeyword();
            this.waitFutureExpr = oldNode.waitFutureExpr();
        }

        public WaitActionNodeModifier withWaitKeyword(
                Token waitKeyword) {
            Objects.requireNonNull(waitKeyword, "waitKeyword must not be null");
            this.waitKeyword = waitKeyword;
            return this;
        }

        public WaitActionNodeModifier withWaitFutureExpr(
                Node waitFutureExpr) {
            Objects.requireNonNull(waitFutureExpr, "waitFutureExpr must not be null");
            this.waitFutureExpr = waitFutureExpr;
            return this;
        }

        public WaitActionNode apply() {
            return oldNode.modify(
                    waitKeyword,
                    waitFutureExpr);
        }
    }
}
