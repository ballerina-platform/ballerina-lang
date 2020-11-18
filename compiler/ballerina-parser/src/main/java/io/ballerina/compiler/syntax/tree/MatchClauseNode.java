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
public class MatchClauseNode extends NonTerminalNode {

    public MatchClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SeparatedNodeList<Node> matchPatterns() {
        return new SeparatedNodeList<>(childInBucket(0));
    }

    public Optional<MatchGuardNode> matchGuard() {
        return optionalChildInBucket(1);
    }

    public Token rightDoubleArrow() {
        return childInBucket(2);
    }

    public BlockStatementNode blockStatement() {
        return childInBucket(3);
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
                "matchPatterns",
                "matchGuard",
                "rightDoubleArrow",
                "blockStatement"};
    }

    public MatchClauseNode modify(
            SeparatedNodeList<Node> matchPatterns,
            MatchGuardNode matchGuard,
            Token rightDoubleArrow,
            BlockStatementNode blockStatement) {
        if (checkForReferenceEquality(
                matchPatterns.underlyingListNode(),
                matchGuard,
                rightDoubleArrow,
                blockStatement)) {
            return this;
        }

        return NodeFactory.createMatchClauseNode(
                matchPatterns,
                matchGuard,
                rightDoubleArrow,
                blockStatement);
    }

    public MatchClauseNodeModifier modify() {
        return new MatchClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MatchClauseNodeModifier {
        private final MatchClauseNode oldNode;
        private SeparatedNodeList<Node> matchPatterns;
        private MatchGuardNode matchGuard;
        private Token rightDoubleArrow;
        private BlockStatementNode blockStatement;

        public MatchClauseNodeModifier(MatchClauseNode oldNode) {
            this.oldNode = oldNode;
            this.matchPatterns = oldNode.matchPatterns();
            this.matchGuard = oldNode.matchGuard().orElse(null);
            this.rightDoubleArrow = oldNode.rightDoubleArrow();
            this.blockStatement = oldNode.blockStatement();
        }

        public MatchClauseNodeModifier withMatchPatterns(
                SeparatedNodeList<Node> matchPatterns) {
            Objects.requireNonNull(matchPatterns, "matchPatterns must not be null");
            this.matchPatterns = matchPatterns;
            return this;
        }

        public MatchClauseNodeModifier withMatchGuard(
                MatchGuardNode matchGuard) {
            Objects.requireNonNull(matchGuard, "matchGuard must not be null");
            this.matchGuard = matchGuard;
            return this;
        }

        public MatchClauseNodeModifier withRightDoubleArrow(
                Token rightDoubleArrow) {
            Objects.requireNonNull(rightDoubleArrow, "rightDoubleArrow must not be null");
            this.rightDoubleArrow = rightDoubleArrow;
            return this;
        }

        public MatchClauseNodeModifier withBlockStatement(
                BlockStatementNode blockStatement) {
            Objects.requireNonNull(blockStatement, "blockStatement must not be null");
            this.blockStatement = blockStatement;
            return this;
        }

        public MatchClauseNode apply() {
            return oldNode.modify(
                    matchPatterns,
                    matchGuard,
                    rightDoubleArrow,
                    blockStatement);
        }
    }
}
