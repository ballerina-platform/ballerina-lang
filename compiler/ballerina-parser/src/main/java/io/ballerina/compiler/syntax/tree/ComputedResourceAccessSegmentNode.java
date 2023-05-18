/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.2.0
 */
public class ComputedResourceAccessSegmentNode extends NonTerminalNode {

    public ComputedResourceAccessSegmentNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracketToken() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
        return childInBucket(1);
    }

    public Token closeBracketToken() {
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
                "openBracketToken",
                "expression",
                "closeBracketToken"};
    }

    public ComputedResourceAccessSegmentNode modify(
            Token openBracketToken,
            ExpressionNode expression,
            Token closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                expression,
                closeBracketToken)) {
            return this;
        }

        return NodeFactory.createComputedResourceAccessSegmentNode(
                openBracketToken,
                expression,
                closeBracketToken);
    }

    public ComputedResourceAccessSegmentNodeModifier modify() {
        return new ComputedResourceAccessSegmentNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.2.0
     */
    public static class ComputedResourceAccessSegmentNodeModifier {
        private final ComputedResourceAccessSegmentNode oldNode;
        private Token openBracketToken;
        private ExpressionNode expression;
        private Token closeBracketToken;

        public ComputedResourceAccessSegmentNodeModifier(ComputedResourceAccessSegmentNode oldNode) {
            this.oldNode = oldNode;
            this.openBracketToken = oldNode.openBracketToken();
            this.expression = oldNode.expression();
            this.closeBracketToken = oldNode.closeBracketToken();
        }

        public ComputedResourceAccessSegmentNodeModifier withOpenBracketToken(
                Token openBracketToken) {
            Objects.requireNonNull(openBracketToken, "openBracketToken must not be null");
            this.openBracketToken = openBracketToken;
            return this;
        }

        public ComputedResourceAccessSegmentNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public ComputedResourceAccessSegmentNodeModifier withCloseBracketToken(
                Token closeBracketToken) {
            Objects.requireNonNull(closeBracketToken, "closeBracketToken must not be null");
            this.closeBracketToken = closeBracketToken;
            return this;
        }

        public ComputedResourceAccessSegmentNode apply() {
            return oldNode.modify(
                    openBracketToken,
                    expression,
                    closeBracketToken);
        }
    }
}
