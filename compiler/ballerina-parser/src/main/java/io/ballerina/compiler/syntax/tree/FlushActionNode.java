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
public class FlushActionNode extends ExpressionNode {

    public FlushActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token flushKeyword() {
        return childInBucket(0);
    }

    public Optional<NameReferenceNode> peerWorker() {
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
                "flushKeyword",
                "peerWorker"};
    }

    public FlushActionNode modify(
            Token flushKeyword,
            NameReferenceNode peerWorker) {
        if (checkForReferenceEquality(
                flushKeyword,
                peerWorker)) {
            return this;
        }

        return NodeFactory.createFlushActionNode(
                flushKeyword,
                peerWorker);
    }

    public FlushActionNodeModifier modify() {
        return new FlushActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FlushActionNodeModifier {
        private final FlushActionNode oldNode;
        private Token flushKeyword;
        private NameReferenceNode peerWorker;

        public FlushActionNodeModifier(FlushActionNode oldNode) {
            this.oldNode = oldNode;
            this.flushKeyword = oldNode.flushKeyword();
            this.peerWorker = oldNode.peerWorker().orElse(null);
        }

        public FlushActionNodeModifier withFlushKeyword(
                Token flushKeyword) {
            Objects.requireNonNull(flushKeyword, "flushKeyword must not be null");
            this.flushKeyword = flushKeyword;
            return this;
        }

        public FlushActionNodeModifier withPeerWorker(
                NameReferenceNode peerWorker) {
            Objects.requireNonNull(peerWorker, "peerWorker must not be null");
            this.peerWorker = peerWorker;
            return this;
        }

        public FlushActionNode apply() {
            return oldNode.modify(
                    flushKeyword,
                    peerWorker);
        }
    }
}
