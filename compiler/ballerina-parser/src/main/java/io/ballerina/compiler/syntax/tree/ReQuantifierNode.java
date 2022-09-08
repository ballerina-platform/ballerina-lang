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
public class ReQuantifierNode extends NonTerminalNode {

    public ReQuantifierNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node reBaseQuantifier() {
        return childInBucket(0);
    }

    public Token nonGreedyChar() {
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
                "reBaseQuantifier",
                "nonGreedyChar"};
    }

    public ReQuantifierNode modify(
            Node reBaseQuantifier,
            Token nonGreedyChar) {
        if (checkForReferenceEquality(
                reBaseQuantifier,
                nonGreedyChar)) {
            return this;
        }

        return NodeFactory.createReQuantifierNode(
                reBaseQuantifier,
                nonGreedyChar);
    }

    public ReQuantifierNodeModifier modify() {
        return new ReQuantifierNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReQuantifierNodeModifier {
        private final ReQuantifierNode oldNode;
        private Node reBaseQuantifier;
        private Token nonGreedyChar;

        public ReQuantifierNodeModifier(ReQuantifierNode oldNode) {
            this.oldNode = oldNode;
            this.reBaseQuantifier = oldNode.reBaseQuantifier();
            this.nonGreedyChar = oldNode.nonGreedyChar();
        }

        public ReQuantifierNodeModifier withReBaseQuantifier(
                Node reBaseQuantifier) {
            Objects.requireNonNull(reBaseQuantifier, "reBaseQuantifier must not be null");
            this.reBaseQuantifier = reBaseQuantifier;
            return this;
        }

        public ReQuantifierNodeModifier withNonGreedyChar(
                Token nonGreedyChar) {
            Objects.requireNonNull(nonGreedyChar, "nonGreedyChar must not be null");
            this.nonGreedyChar = nonGreedyChar;
            return this;
        }

        public ReQuantifierNode apply() {
            return oldNode.modify(
                    reBaseQuantifier,
                    nonGreedyChar);
        }
    }
}
