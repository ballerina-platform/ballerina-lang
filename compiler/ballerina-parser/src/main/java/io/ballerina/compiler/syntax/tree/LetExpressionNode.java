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
public class LetExpressionNode extends ExpressionNode {

    public LetExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token letKeyword() {
        return childInBucket(0);
    }

    public SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token inKeyword() {
        return childInBucket(2);
    }

    public ExpressionNode expression() {
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
                "letKeyword",
                "letVarDeclarations",
                "inKeyword",
                "expression"};
    }

    public LetExpressionNode modify(
            Token letKeyword,
            SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations,
            Token inKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                letKeyword,
                letVarDeclarations.underlyingListNode(),
                inKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createLetExpressionNode(
                letKeyword,
                letVarDeclarations,
                inKeyword,
                expression);
    }

    public LetExpressionNodeModifier modify() {
        return new LetExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class LetExpressionNodeModifier {
        private final LetExpressionNode oldNode;
        private Token letKeyword;
        private SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations;
        private Token inKeyword;
        private ExpressionNode expression;

        public LetExpressionNodeModifier(LetExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.letKeyword = oldNode.letKeyword();
            this.letVarDeclarations = oldNode.letVarDeclarations();
            this.inKeyword = oldNode.inKeyword();
            this.expression = oldNode.expression();
        }

        public LetExpressionNodeModifier withLetKeyword(
                Token letKeyword) {
            Objects.requireNonNull(letKeyword, "letKeyword must not be null");
            this.letKeyword = letKeyword;
            return this;
        }

        public LetExpressionNodeModifier withLetVarDeclarations(
                SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations) {
            Objects.requireNonNull(letVarDeclarations, "letVarDeclarations must not be null");
            this.letVarDeclarations = letVarDeclarations;
            return this;
        }

        public LetExpressionNodeModifier withInKeyword(
                Token inKeyword) {
            Objects.requireNonNull(inKeyword, "inKeyword must not be null");
            this.inKeyword = inKeyword;
            return this;
        }

        public LetExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public LetExpressionNode apply() {
            return oldNode.modify(
                    letKeyword,
                    letVarDeclarations,
                    inKeyword,
                    expression);
        }
    }
}
