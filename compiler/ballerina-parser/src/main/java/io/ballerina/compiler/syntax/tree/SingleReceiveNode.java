/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 * @since 2201.9.0
 */
public class SingleReceiveNode extends NonTerminalNode {

    public SingleReceiveNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SimpleNameReferenceNode worker() {
        return childInBucket(0);
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
                "worker"};
    }

    public SingleReceiveNode modify(
            SimpleNameReferenceNode worker) {
        if (checkForReferenceEquality(
                worker)) {
            return this;
        }

        return NodeFactory.createSingleReceiveNode(
                worker);
    }

    public SingleReceiveNodeModifier modify() {
        return new SingleReceiveNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.9.0
     */
    public static class SingleReceiveNodeModifier {
        private final SingleReceiveNode oldNode;
        private SimpleNameReferenceNode worker;

        public SingleReceiveNodeModifier(SingleReceiveNode oldNode) {
            this.oldNode = oldNode;
            this.worker = oldNode.worker();
        }

        public SingleReceiveNodeModifier withWorker(
                SimpleNameReferenceNode worker) {
            Objects.requireNonNull(worker, "worker must not be null");
            this.worker = worker;
            return this;
        }

        public SingleReceiveNode apply() {
            return oldNode.modify(
                    worker);
        }
    }
}
