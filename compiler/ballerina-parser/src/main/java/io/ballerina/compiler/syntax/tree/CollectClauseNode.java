/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
 * @since 2201.7.0
 */
public class CollectClauseNode extends ClauseNode {

    public CollectClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token collectKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
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
                "collectKeyword",
                "expression"};
    }

    public CollectClauseNode modify(
            Token collectKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                collectKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createCollectClauseNode(
                collectKeyword,
                expression);
    }

    public CollectClauseNodeModifier modify() {
        return new CollectClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.7.0
     */
    public static class CollectClauseNodeModifier {
        private final CollectClauseNode oldNode;
        private Token collectKeyword;
        private ExpressionNode expression;

        public CollectClauseNodeModifier(CollectClauseNode oldNode) {
            this.oldNode = oldNode;
            this.collectKeyword = oldNode.collectKeyword();
            this.expression = oldNode.expression();
        }

        public CollectClauseNodeModifier withCollectKeyword(
                Token collectKeyword) {
            Objects.requireNonNull(collectKeyword, "collectKeyword must not be null");
            this.collectKeyword = collectKeyword;
            return this;
        }

        public CollectClauseNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public CollectClauseNode apply() {
            return oldNode.modify(
                    collectKeyword,
                    expression);
        }
    }
}
