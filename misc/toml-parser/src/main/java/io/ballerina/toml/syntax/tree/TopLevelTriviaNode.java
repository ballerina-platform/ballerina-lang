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
package io.ballerina.toml.syntax.tree;

import io.ballerina.toml.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class TopLevelTriviaNode extends DocumentMemberDeclarationNode {

    public TopLevelTriviaNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<Token> newLines() {
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
                "newLines"};
    }

    public TopLevelTriviaNode modify(
            NodeList<Token> newLines) {
        if (checkForReferenceEquality(
                newLines.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createTopLevelTriviaNode(
                newLines);
    }

    public TopLevelTriviaNodeModifier modify() {
        return new TopLevelTriviaNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TopLevelTriviaNodeModifier {
        private final TopLevelTriviaNode oldNode;
        private NodeList<Token> newLines;

        public TopLevelTriviaNodeModifier(TopLevelTriviaNode oldNode) {
            this.oldNode = oldNode;
            this.newLines = oldNode.newLines();
        }

        public TopLevelTriviaNodeModifier withNewLines(
                NodeList<Token> newLines) {
            Objects.requireNonNull(newLines, "newLines must not be null");
            this.newLines = newLines;
            return this;
        }

        public TopLevelTriviaNode apply() {
            return oldNode.modify(
                    newLines);
        }
    }
}
