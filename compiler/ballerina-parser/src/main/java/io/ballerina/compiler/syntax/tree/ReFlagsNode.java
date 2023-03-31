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
public class ReFlagsNode extends NonTerminalNode {

    public ReFlagsNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<Node> reFlag() {
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
                "reFlag"};
    }

    public ReFlagsNode modify(
            NodeList<Node> reFlag) {
        if (checkForReferenceEquality(
                reFlag.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createReFlagsNode(
                reFlag);
    }

    public ReFlagsNodeModifier modify() {
        return new ReFlagsNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReFlagsNodeModifier {
        private final ReFlagsNode oldNode;
        private NodeList<Node> reFlag;

        public ReFlagsNodeModifier(ReFlagsNode oldNode) {
            this.oldNode = oldNode;
            this.reFlag = oldNode.reFlag();
        }

        public ReFlagsNodeModifier withReFlag(
                NodeList<Node> reFlag) {
            Objects.requireNonNull(reFlag, "reFlag must not be null");
            this.reFlag = reFlag;
            return this;
        }

        public ReFlagsNode apply() {
            return oldNode.modify(
                    reFlag);
        }
    }
}
