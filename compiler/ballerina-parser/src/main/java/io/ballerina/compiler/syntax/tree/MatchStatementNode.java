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
public class MatchStatementNode extends StatementNode {

    public MatchStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token matchKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode condition() {
        return childInBucket(1);
    }

    public Token openBrace() {
        return childInBucket(2);
    }

    public NodeList<MatchClauseNode> matchClauses() {
        return new NodeList<>(childInBucket(3));
    }

    public Token closeBrace() {
        return childInBucket(4);
    }

    public Optional<OnFailClauseNode> onFailClause() {
        return optionalChildInBucket(5);
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
                "matchKeyword",
                "condition",
                "openBrace",
                "matchClauses",
                "closeBrace",
                "onFailClause"};
    }

    public MatchStatementNode modify(
            Token matchKeyword,
            ExpressionNode condition,
            Token openBrace,
            NodeList<MatchClauseNode> matchClauses,
            Token closeBrace,
            OnFailClauseNode onFailClause) {
        if (checkForReferenceEquality(
                matchKeyword,
                condition,
                openBrace,
                matchClauses.underlyingListNode(),
                closeBrace,
                onFailClause)) {
            return this;
        }

        return NodeFactory.createMatchStatementNode(
                matchKeyword,
                condition,
                openBrace,
                matchClauses,
                closeBrace,
                onFailClause);
    }

    public MatchStatementNodeModifier modify() {
        return new MatchStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MatchStatementNodeModifier {
        private final MatchStatementNode oldNode;
        private Token matchKeyword;
        private ExpressionNode condition;
        private Token openBrace;
        private NodeList<MatchClauseNode> matchClauses;
        private Token closeBrace;
        private OnFailClauseNode onFailClause;

        public MatchStatementNodeModifier(MatchStatementNode oldNode) {
            this.oldNode = oldNode;
            this.matchKeyword = oldNode.matchKeyword();
            this.condition = oldNode.condition();
            this.openBrace = oldNode.openBrace();
            this.matchClauses = oldNode.matchClauses();
            this.closeBrace = oldNode.closeBrace();
            this.onFailClause = oldNode.onFailClause().orElse(null);
        }

        public MatchStatementNodeModifier withMatchKeyword(
                Token matchKeyword) {
            Objects.requireNonNull(matchKeyword, "matchKeyword must not be null");
            this.matchKeyword = matchKeyword;
            return this;
        }

        public MatchStatementNodeModifier withCondition(
                ExpressionNode condition) {
            Objects.requireNonNull(condition, "condition must not be null");
            this.condition = condition;
            return this;
        }

        public MatchStatementNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public MatchStatementNodeModifier withMatchClauses(
                NodeList<MatchClauseNode> matchClauses) {
            Objects.requireNonNull(matchClauses, "matchClauses must not be null");
            this.matchClauses = matchClauses;
            return this;
        }

        public MatchStatementNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public MatchStatementNodeModifier withOnFailClause(
                OnFailClauseNode onFailClause) {
            Objects.requireNonNull(onFailClause, "onFailClause must not be null");
            this.onFailClause = onFailClause;
            return this;
        }

        public MatchStatementNode apply() {
            return oldNode.modify(
                    matchKeyword,
                    condition,
                    openBrace,
                    matchClauses,
                    closeBrace,
                    onFailClause);
        }
    }
}
