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
public class AssignmentStatementNode extends StatementNode {

    public AssignmentStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node varRef() {
        return childInBucket(0);
    }

    public Token equalsToken() {
        return childInBucket(1);
    }

    public ExpressionNode expression() {
        return childInBucket(2);
    }

    public Token semicolonToken() {
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
                "varRef",
                "equalsToken",
                "expression",
                "semicolonToken"};
    }

    public AssignmentStatementNode modify(
            Node varRef,
            Token equalsToken,
            ExpressionNode expression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                varRef,
                equalsToken,
                expression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createAssignmentStatementNode(
                varRef,
                equalsToken,
                expression,
                semicolonToken);
    }

    public AssignmentStatementNodeModifier modify() {
        return new AssignmentStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class AssignmentStatementNodeModifier {
        private final AssignmentStatementNode oldNode;
        private Node varRef;
        private Token equalsToken;
        private ExpressionNode expression;
        private Token semicolonToken;

        public AssignmentStatementNodeModifier(AssignmentStatementNode oldNode) {
            this.oldNode = oldNode;
            this.varRef = oldNode.varRef();
            this.equalsToken = oldNode.equalsToken();
            this.expression = oldNode.expression();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public AssignmentStatementNodeModifier withVarRef(
                Node varRef) {
            Objects.requireNonNull(varRef, "varRef must not be null");
            this.varRef = varRef;
            return this;
        }

        public AssignmentStatementNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public AssignmentStatementNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public AssignmentStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public AssignmentStatementNode apply() {
            return oldNode.modify(
                    varRef,
                    equalsToken,
                    expression,
                    semicolonToken);
        }
    }
}
