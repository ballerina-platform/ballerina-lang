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
public class ResourceAccessRestSegmentNode extends NonTerminalNode {

    public ResourceAccessRestSegmentNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracketToken() {
        return childInBucket(0);
    }

    public Token ellipsisToken() {
        return childInBucket(1);
    }

    public ExpressionNode expression() {
        return childInBucket(2);
    }

    public Token closeBracketToken() {
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
                "openBracketToken",
                "ellipsisToken",
                "expression",
                "closeBracketToken"};
    }

    public ResourceAccessRestSegmentNode modify(
            Token openBracketToken,
            Token ellipsisToken,
            ExpressionNode expression,
            Token closeBracketToken) {
        if (checkForReferenceEquality(
                openBracketToken,
                ellipsisToken,
                expression,
                closeBracketToken)) {
            return this;
        }

        return NodeFactory.createResourceAccessRestSegmentNode(
                openBracketToken,
                ellipsisToken,
                expression,
                closeBracketToken);
    }

    public ResourceAccessRestSegmentNodeModifier modify() {
        return new ResourceAccessRestSegmentNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.2.0
     */
    public static class ResourceAccessRestSegmentNodeModifier {
        private final ResourceAccessRestSegmentNode oldNode;
        private Token openBracketToken;
        private Token ellipsisToken;
        private ExpressionNode expression;
        private Token closeBracketToken;

        public ResourceAccessRestSegmentNodeModifier(ResourceAccessRestSegmentNode oldNode) {
            this.oldNode = oldNode;
            this.openBracketToken = oldNode.openBracketToken();
            this.ellipsisToken = oldNode.ellipsisToken();
            this.expression = oldNode.expression();
            this.closeBracketToken = oldNode.closeBracketToken();
        }

        public ResourceAccessRestSegmentNodeModifier withOpenBracketToken(
                Token openBracketToken) {
            Objects.requireNonNull(openBracketToken, "openBracketToken must not be null");
            this.openBracketToken = openBracketToken;
            return this;
        }

        public ResourceAccessRestSegmentNodeModifier withEllipsisToken(
                Token ellipsisToken) {
            Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");
            this.ellipsisToken = ellipsisToken;
            return this;
        }

        public ResourceAccessRestSegmentNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public ResourceAccessRestSegmentNodeModifier withCloseBracketToken(
                Token closeBracketToken) {
            Objects.requireNonNull(closeBracketToken, "closeBracketToken must not be null");
            this.closeBracketToken = closeBracketToken;
            return this;
        }

        public ResourceAccessRestSegmentNode apply() {
            return oldNode.modify(
                    openBracketToken,
                    ellipsisToken,
                    expression,
                    closeBracketToken);
        }
    }
}
