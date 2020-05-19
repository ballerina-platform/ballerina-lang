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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class FromClauseNode extends ClauseNode {

    public FromClauseNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token fromKeyword() {
        return childInBucket(0);
    }

    public Node typeName() {
        return childInBucket(1);
    }

    public Token variableName() {
        return childInBucket(2);
    }

    public Token inKeyword() {
        return childInBucket(3);
    }

    public ExpressionNode expression() {
        return childInBucket(4);
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
                "fromKeyword",
                "typeName",
                "variableName",
                "inKeyword",
                "expression"};
    }

    public FromClauseNode modify(
            Token fromKeyword,
            Node typeName,
            Token variableName,
            Token inKeyword,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                fromKeyword,
                typeName,
                variableName,
                inKeyword,
                expression)) {
            return this;
        }

        return NodeFactory.createFromClauseNode(
                fromKeyword,
                typeName,
                variableName,
                inKeyword,
                expression);
    }

    public FromClauseNodeModifier modify() {
        return new FromClauseNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FromClauseNodeModifier {
        private final FromClauseNode oldNode;
        private Token fromKeyword;
        private Node typeName;
        private Token variableName;
        private Token inKeyword;
        private ExpressionNode expression;

        public FromClauseNodeModifier(FromClauseNode oldNode) {
            this.oldNode = oldNode;
            this.fromKeyword = oldNode.fromKeyword();
            this.typeName = oldNode.typeName();
            this.variableName = oldNode.variableName();
            this.inKeyword = oldNode.inKeyword();
            this.expression = oldNode.expression();
        }

        public FromClauseNodeModifier withFromKeyword(Token fromKeyword) {
            Objects.requireNonNull(fromKeyword, "fromKeyword must not be null");
            this.fromKeyword = fromKeyword;
            return this;
        }

        public FromClauseNodeModifier withTypeName(Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public FromClauseNodeModifier withVariableName(Token variableName) {
            Objects.requireNonNull(variableName, "variableName must not be null");
            this.variableName = variableName;
            return this;
        }

        public FromClauseNodeModifier withInKeyword(Token inKeyword) {
            Objects.requireNonNull(inKeyword, "inKeyword must not be null");
            this.inKeyword = inKeyword;
            return this;
        }

        public FromClauseNodeModifier withExpression(ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public FromClauseNode apply() {
            return oldNode.modify(
                    fromKeyword,
                    typeName,
                    variableName,
                    inKeyword,
                    expression);
        }
    }
}
