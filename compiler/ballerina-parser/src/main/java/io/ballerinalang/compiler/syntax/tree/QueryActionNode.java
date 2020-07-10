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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class QueryActionNode extends ActionNode {

    public QueryActionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public QueryPipelineNode queryPipeline() {
        return childInBucket(0);
    }

    public Token doKeyword() {
        return childInBucket(1);
    }

    public BlockStatementNode blockStatement() {
        return childInBucket(2);
    }

    public Optional<LimitClauseNode> limitClause() {
        return optionalChildInBucket(3);
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
                "queryPipeline",
                "doKeyword",
                "blockStatement",
                "limitClause"};
    }

    public QueryActionNode modify(
            QueryPipelineNode queryPipeline,
            Token doKeyword,
            BlockStatementNode blockStatement,
            LimitClauseNode limitClause) {
        if (checkForReferenceEquality(
                queryPipeline,
                doKeyword,
                blockStatement,
                limitClause)) {
            return this;
        }

        return NodeFactory.createQueryActionNode(
                queryPipeline,
                doKeyword,
                blockStatement,
                limitClause);
    }

    public QueryActionNodeModifier modify() {
        return new QueryActionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class QueryActionNodeModifier {
        private final QueryActionNode oldNode;
        private QueryPipelineNode queryPipeline;
        private Token doKeyword;
        private BlockStatementNode blockStatement;
        private LimitClauseNode limitClause;

        public QueryActionNodeModifier(QueryActionNode oldNode) {
            this.oldNode = oldNode;
            this.queryPipeline = oldNode.queryPipeline();
            this.doKeyword = oldNode.doKeyword();
            this.blockStatement = oldNode.blockStatement();
            this.limitClause = oldNode.limitClause().orElse(null);
        }

        public QueryActionNodeModifier withQueryPipeline(
                QueryPipelineNode queryPipeline) {
            Objects.requireNonNull(queryPipeline, "queryPipeline must not be null");
            this.queryPipeline = queryPipeline;
            return this;
        }

        public QueryActionNodeModifier withDoKeyword(
                Token doKeyword) {
            Objects.requireNonNull(doKeyword, "doKeyword must not be null");
            this.doKeyword = doKeyword;
            return this;
        }

        public QueryActionNodeModifier withBlockStatement(
                BlockStatementNode blockStatement) {
            Objects.requireNonNull(blockStatement, "blockStatement must not be null");
            this.blockStatement = blockStatement;
            return this;
        }

        public QueryActionNodeModifier withLimitClause(
                LimitClauseNode limitClause) {
            Objects.requireNonNull(limitClause, "limitClause must not be null");
            this.limitClause = limitClause;
            return this;
        }

        public QueryActionNode apply() {
            return oldNode.modify(
                    queryPipeline,
                    doKeyword,
                    blockStatement,
                    limitClause);
        }
    }
}
