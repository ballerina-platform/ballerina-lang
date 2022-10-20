/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.3.0
 */
public class ReSequenceNode extends NonTerminalNode {

    public ReSequenceNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<ReTermNode> reTerm() {
        return new NodeList<>(childInBucket(0));
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
                "reTerm"};
    }

    public ReSequenceNode modify(
            NodeList<ReTermNode> reTerm) {
        if (checkForReferenceEquality(
                reTerm.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createReSequenceNode(
                reTerm);
    }

    public ReSequenceNodeModifier modify() {
        return new ReSequenceNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReSequenceNodeModifier {
        private final ReSequenceNode oldNode;
        private NodeList<ReTermNode> reTerm;

        public ReSequenceNodeModifier(ReSequenceNode oldNode) {
            this.oldNode = oldNode;
            this.reTerm = oldNode.reTerm();
        }

        public ReSequenceNodeModifier withReTerm(
                NodeList<ReTermNode> reTerm) {
            Objects.requireNonNull(reTerm, "reTerm must not be null");
            this.reTerm = reTerm;
            return this;
        }

        public ReSequenceNode apply() {
            return oldNode.modify(
                    reTerm);
        }
    }
}
