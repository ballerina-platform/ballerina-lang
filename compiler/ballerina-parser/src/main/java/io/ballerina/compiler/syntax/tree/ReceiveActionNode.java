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
public class ReceiveActionNode extends ActionNode {

    public ReceiveActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token leftArrow() {
        return childInBucket(0);
    }

    public SimpleNameReferenceNode receiveWorkers() {
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
                "leftArrow",
                "receiveWorkers"};
    }

    public ReceiveActionNode modify(
            Token leftArrow,
            SimpleNameReferenceNode receiveWorkers) {
        if (checkForReferenceEquality(
                leftArrow,
                receiveWorkers)) {
            return this;
        }

        return NodeFactory.createReceiveActionNode(
                leftArrow,
                receiveWorkers);
    }

    public ReceiveActionNodeModifier modify() {
        return new ReceiveActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReceiveActionNodeModifier {
        private final ReceiveActionNode oldNode;
        private Token leftArrow;
        private SimpleNameReferenceNode receiveWorkers;

        public ReceiveActionNodeModifier(ReceiveActionNode oldNode) {
            this.oldNode = oldNode;
            this.leftArrow = oldNode.leftArrow();
            this.receiveWorkers = oldNode.receiveWorkers();
        }

        public ReceiveActionNodeModifier withLeftArrow(
                Token leftArrow) {
            Objects.requireNonNull(leftArrow, "leftArrow must not be null");
            this.leftArrow = leftArrow;
            return this;
        }

        public ReceiveActionNodeModifier withReceiveWorkers(
                SimpleNameReferenceNode receiveWorkers) {
            Objects.requireNonNull(receiveWorkers, "receiveWorkers must not be null");
            this.receiveWorkers = receiveWorkers;
            return this;
        }

        public ReceiveActionNode apply() {
            return oldNode.modify(
                    leftArrow,
                    receiveWorkers);
        }
    }
}
