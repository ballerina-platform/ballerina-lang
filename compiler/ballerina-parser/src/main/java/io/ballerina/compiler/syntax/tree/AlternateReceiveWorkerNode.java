/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
public class AlternateReceiveWorkerNode extends NonTerminalNode {

    public AlternateReceiveWorkerNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SeparatedNodeList<SimpleNameReferenceNode> workers() {
        return new SeparatedNodeList<>(childInBucket(0));
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
                "workers"};
    }

    public AlternateReceiveWorkerNode modify(
            SeparatedNodeList<SimpleNameReferenceNode> workers) {
        if (checkForReferenceEquality(
                workers.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createAlternateReceiveWorkerNode(
                workers);
    }

    public AlternateReceiveWorkerNodeModifier modify() {
        return new AlternateReceiveWorkerNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.9.0
     */
    public static class AlternateReceiveWorkerNodeModifier {
        private final AlternateReceiveWorkerNode oldNode;
        private SeparatedNodeList<SimpleNameReferenceNode> workers;

        public AlternateReceiveWorkerNodeModifier(AlternateReceiveWorkerNode oldNode) {
            this.oldNode = oldNode;
            this.workers = oldNode.workers();
        }

        public AlternateReceiveWorkerNodeModifier withWorkers(
                SeparatedNodeList<SimpleNameReferenceNode> workers) {
            Objects.requireNonNull(workers, "workers must not be null");
            this.workers = workers;
            return this;
        }

        public AlternateReceiveWorkerNode apply() {
            return oldNode.modify(
                    workers);
        }
    }
}
