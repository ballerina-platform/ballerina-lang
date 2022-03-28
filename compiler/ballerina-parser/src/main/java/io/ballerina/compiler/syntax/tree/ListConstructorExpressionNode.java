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
public class ListConstructorExpressionNode extends ExpressionNode {

    public ListConstructorExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public SeparatedNodeList<Node> expressions() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token closeBracket() {
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
                "openBracket",
                "expressions",
                "closeBracket"};
    }

    public ListConstructorExpressionNode modify(
            Token openBracket,
            SeparatedNodeList<Node> expressions,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                expressions.underlyingListNode(),
                closeBracket)) {
            return this;
        }

        return NodeFactory.createListConstructorExpressionNode(
                openBracket,
                expressions,
                closeBracket);
    }

    public ListConstructorExpressionNodeModifier modify() {
        return new ListConstructorExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ListConstructorExpressionNodeModifier {
        private final ListConstructorExpressionNode oldNode;
        private Token openBracket;
        private SeparatedNodeList<Node> expressions;
        private Token closeBracket;

        public ListConstructorExpressionNodeModifier(ListConstructorExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.expressions = oldNode.expressions();
            this.closeBracket = oldNode.closeBracket();
        }

        public ListConstructorExpressionNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ListConstructorExpressionNodeModifier withExpressions(
                SeparatedNodeList<Node> expressions) {
            Objects.requireNonNull(expressions, "expressions must not be null");
            this.expressions = expressions;
            return this;
        }

        public ListConstructorExpressionNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public ListConstructorExpressionNode apply() {
            return oldNode.modify(
                    openBracket,
                    expressions,
                    closeBracket);
        }
    }
}
