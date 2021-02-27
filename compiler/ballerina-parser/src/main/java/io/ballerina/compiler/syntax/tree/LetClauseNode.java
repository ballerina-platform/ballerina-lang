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
public class LetClauseNode extends IntermediateClauseNode {

    public LetClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token letKeyword() {
        return childInBucket(0);
    }

    public SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations() {
        return new SeparatedNodeList<>(childInBucket(1));
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
                "letKeyword",
                "letVarDeclarations"};
    }

    public LetClauseNode modify(
            Token letKeyword,
            SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations) {
        if (checkForReferenceEquality(
                letKeyword,
                letVarDeclarations.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createLetClauseNode(
                letKeyword,
                letVarDeclarations);
    }

    public LetClauseNodeModifier modify() {
        return new LetClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class LetClauseNodeModifier {
        private final LetClauseNode oldNode;
        private Token letKeyword;
        private SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations;

        public LetClauseNodeModifier(LetClauseNode oldNode) {
            this.oldNode = oldNode;
            this.letKeyword = oldNode.letKeyword();
            this.letVarDeclarations = oldNode.letVarDeclarations();
        }

        public LetClauseNodeModifier withLetKeyword(
                Token letKeyword) {
            Objects.requireNonNull(letKeyword, "letKeyword must not be null");
            this.letKeyword = letKeyword;
            return this;
        }

        public LetClauseNodeModifier withLetVarDeclarations(
                SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations) {
            Objects.requireNonNull(letVarDeclarations, "letVarDeclarations must not be null");
            this.letVarDeclarations = letVarDeclarations;
            return this;
        }

        public LetClauseNode apply() {
            return oldNode.modify(
                    letKeyword,
                    letVarDeclarations);
        }
    }
}
