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
public class QueryPipelineNode extends NonTerminalNode {

    public QueryPipelineNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public FromClauseNode fromClause() {
        return childInBucket(0);
    }

    public NodeList<IntermediateClauseNode> intermediateClauses() {
        return new NodeList<>(childInBucket(1));
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
                "fromClause",
                "intermediateClauses"};
    }

    public QueryPipelineNode modify(
            FromClauseNode fromClause,
            NodeList<IntermediateClauseNode> intermediateClauses) {
        if (checkForReferenceEquality(
                fromClause,
                intermediateClauses.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createQueryPipelineNode(
                fromClause,
                intermediateClauses);
    }

    public QueryPipelineNodeModifier modify() {
        return new QueryPipelineNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class QueryPipelineNodeModifier {
        private final QueryPipelineNode oldNode;
        private FromClauseNode fromClause;
        private NodeList<IntermediateClauseNode> intermediateClauses;

        public QueryPipelineNodeModifier(QueryPipelineNode oldNode) {
            this.oldNode = oldNode;
            this.fromClause = oldNode.fromClause();
            this.intermediateClauses = oldNode.intermediateClauses();
        }

        public QueryPipelineNodeModifier withFromClause(
                FromClauseNode fromClause) {
            Objects.requireNonNull(fromClause, "fromClause must not be null");
            this.fromClause = fromClause;
            return this;
        }

        public QueryPipelineNodeModifier withIntermediateClauses(
                NodeList<IntermediateClauseNode> intermediateClauses) {
            Objects.requireNonNull(intermediateClauses, "intermediateClauses must not be null");
            this.intermediateClauses = intermediateClauses;
            return this;
        }

        public QueryPipelineNode apply() {
            return oldNode.modify(
                    fromClause,
                    intermediateClauses);
        }
    }
}
