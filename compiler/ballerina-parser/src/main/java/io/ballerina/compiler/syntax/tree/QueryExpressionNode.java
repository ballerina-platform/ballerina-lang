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
public class QueryExpressionNode extends ExpressionNode {

    public QueryExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<QueryConstructTypeNode> queryConstructType() {
        return optionalChildInBucket(0);
    }

    public QueryPipelineNode queryPipeline() {
        return childInBucket(1);
    }

    /**
     * @deprecated Use {@link #resultClause()} instead.
     */
    @Deprecated
    public SelectClauseNode selectClause() {
        ClauseNode resultClause = resultClause();
        if (resultClause.kind() != SyntaxKind.SELECT_CLAUSE) {
            throw new IllegalStateException("select-clause not found");
        }
        return (SelectClauseNode) resultClause;
    }

    public ClauseNode resultClause() {
        return childInBucket(2);
    }

    public Optional<OnConflictClauseNode> onConflictClause() {
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
                "queryConstructType",
                "queryPipeline",
                "resultClause",
                "onConflictClause"};
    }

    public QueryExpressionNode modify(
            QueryConstructTypeNode queryConstructType,
            QueryPipelineNode queryPipeline,
            ClauseNode resultClause,
            OnConflictClauseNode onConflictClause) {
        if (checkForReferenceEquality(
                queryConstructType,
                queryPipeline,
                resultClause,
                onConflictClause)) {
            return this;
        }

        return NodeFactory.createQueryExpressionNode(
                queryConstructType,
                queryPipeline,
                resultClause,
                onConflictClause);
    }

    public QueryExpressionNodeModifier modify() {
        return new QueryExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class QueryExpressionNodeModifier {
        private final QueryExpressionNode oldNode;
        private QueryConstructTypeNode queryConstructType;
        private QueryPipelineNode queryPipeline;
        private ClauseNode resultClause;
        private OnConflictClauseNode onConflictClause;

        public QueryExpressionNodeModifier(QueryExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.queryConstructType = oldNode.queryConstructType().orElse(null);
            this.queryPipeline = oldNode.queryPipeline();
            this.resultClause = oldNode.resultClause();
            this.onConflictClause = oldNode.onConflictClause().orElse(null);
        }

        public QueryExpressionNodeModifier withQueryConstructType(
                QueryConstructTypeNode queryConstructType) {
            this.queryConstructType = queryConstructType;
            return this;
        }

        public QueryExpressionNodeModifier withQueryPipeline(
                QueryPipelineNode queryPipeline) {
            Objects.requireNonNull(queryPipeline, "queryPipeline must not be null");
            this.queryPipeline = queryPipeline;
            return this;
        }

        public QueryExpressionNodeModifier withResultClause(
                ClauseNode resultClause) {
            Objects.requireNonNull(resultClause, "resultClause must not be null");
            this.resultClause = resultClause;
            return this;
        }

        public QueryExpressionNodeModifier withOnConflictClause(
                OnConflictClauseNode onConflictClause) {
            this.onConflictClause = onConflictClause;
            return this;
        }

        public QueryExpressionNode apply() {
            return oldNode.modify(
                    queryConstructType,
                    queryPipeline,
                    resultClause,
                    onConflictClause);
        }
    }
}
