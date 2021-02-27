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
                "blockStatement"};
    }

    public QueryActionNode modify(
            QueryPipelineNode queryPipeline,
            Token doKeyword,
            BlockStatementNode blockStatement) {
        if (checkForReferenceEquality(
                queryPipeline,
                doKeyword,
                blockStatement)) {
            return this;
        }

        return NodeFactory.createQueryActionNode(
                queryPipeline,
                doKeyword,
                blockStatement);
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

        public QueryActionNodeModifier(QueryActionNode oldNode) {
            this.oldNode = oldNode;
            this.queryPipeline = oldNode.queryPipeline();
            this.doKeyword = oldNode.doKeyword();
            this.blockStatement = oldNode.blockStatement();
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

        public QueryActionNode apply() {
            return oldNode.modify(
                    queryPipeline,
                    doKeyword,
                    blockStatement);
        }
    }
}
