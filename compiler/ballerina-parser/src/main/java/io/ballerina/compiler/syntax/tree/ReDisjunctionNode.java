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
public class ReDisjunctionNode extends NonTerminalNode {

    public ReDisjunctionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SeparatedNodeList<ReSequenceNode> reSequence() {
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
                "reSequence"};
    }

    public ReDisjunctionNode modify(
            SeparatedNodeList<ReSequenceNode> reSequence) {
        if (checkForReferenceEquality(
                reSequence.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createReDisjunctionNode(
                reSequence);
    }

    public ReDisjunctionNodeModifier modify() {
        return new ReDisjunctionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReDisjunctionNodeModifier {
        private final ReDisjunctionNode oldNode;
        private SeparatedNodeList<ReSequenceNode> reSequence;

        public ReDisjunctionNodeModifier(ReDisjunctionNode oldNode) {
            this.oldNode = oldNode;
            this.reSequence = oldNode.reSequence();
        }

        public ReDisjunctionNodeModifier withReSequence(
                SeparatedNodeList<ReSequenceNode> reSequence) {
            Objects.requireNonNull(reSequence, "reSequence must not be null");
            this.reSequence = reSequence;
            return this;
        }

        public ReDisjunctionNode apply() {
            return oldNode.modify(
                    reSequence);
        }
    }
}
