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
public class NamedArgumentNode extends FunctionArgumentNode {

    public NamedArgumentNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SimpleNameReferenceNode argumentName() {
        return childInBucket(0);
    }

    public Token equalsToken() {
        return childInBucket(1);
    }

    public ExpressionNode expression() {
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
                "argumentName",
                "equalsToken",
                "expression"};
    }

    public NamedArgumentNode modify(
            SimpleNameReferenceNode argumentName,
            Token equalsToken,
            ExpressionNode expression) {
        if (checkForReferenceEquality(
                argumentName,
                equalsToken,
                expression)) {
            return this;
        }

        return NodeFactory.createNamedArgumentNode(
                argumentName,
                equalsToken,
                expression);
    }

    public NamedArgumentNodeModifier modify() {
        return new NamedArgumentNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class NamedArgumentNodeModifier {
        private final NamedArgumentNode oldNode;
        private SimpleNameReferenceNode argumentName;
        private Token equalsToken;
        private ExpressionNode expression;

        public NamedArgumentNodeModifier(NamedArgumentNode oldNode) {
            this.oldNode = oldNode;
            this.argumentName = oldNode.argumentName();
            this.equalsToken = oldNode.equalsToken();
            this.expression = oldNode.expression();
        }

        public NamedArgumentNodeModifier withArgumentName(
                SimpleNameReferenceNode argumentName) {
            Objects.requireNonNull(argumentName, "argumentName must not be null");
            this.argumentName = argumentName;
            return this;
        }

        public NamedArgumentNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public NamedArgumentNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public NamedArgumentNode apply() {
            return oldNode.modify(
                    argumentName,
                    equalsToken,
                    expression);
        }
    }
}
