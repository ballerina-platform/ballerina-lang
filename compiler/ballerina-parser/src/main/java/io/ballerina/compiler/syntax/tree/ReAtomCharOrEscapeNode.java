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
public class ReAtomCharOrEscapeNode extends NonTerminalNode {

    public ReAtomCharOrEscapeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node reAtomCharOrEscape() {
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
                "reAtomCharOrEscape"};
    }

    public ReAtomCharOrEscapeNode modify(
            Node reAtomCharOrEscape) {
        if (checkForReferenceEquality(
                reAtomCharOrEscape)) {
            return this;
        }

        return NodeFactory.createReAtomCharOrEscapeNode(
                reAtomCharOrEscape);
    }

    public ReAtomCharOrEscapeNodeModifier modify() {
        return new ReAtomCharOrEscapeNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReAtomCharOrEscapeNodeModifier {
        private final ReAtomCharOrEscapeNode oldNode;
        private Node reAtomCharOrEscape;

        public ReAtomCharOrEscapeNodeModifier(ReAtomCharOrEscapeNode oldNode) {
            this.oldNode = oldNode;
            this.reAtomCharOrEscape = oldNode.reAtomCharOrEscape();
        }

        public ReAtomCharOrEscapeNodeModifier withReAtomCharOrEscape(
                Node reAtomCharOrEscape) {
            Objects.requireNonNull(reAtomCharOrEscape, "reAtomCharOrEscape must not be null");
            this.reAtomCharOrEscape = reAtomCharOrEscape;
            return this;
        }

        public ReAtomCharOrEscapeNode apply() {
            return oldNode.modify(
                    reAtomCharOrEscape);
        }
    }
}
