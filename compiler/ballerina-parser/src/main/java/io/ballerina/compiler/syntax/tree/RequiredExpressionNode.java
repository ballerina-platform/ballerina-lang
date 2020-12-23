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
public class RequiredExpressionNode extends ExpressionNode {

    public RequiredExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token questionMarkToken() {
        return childInBucket(0);
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
                "questionMarkToken"};
    }

    public RequiredExpressionNode modify(
            Token questionMarkToken) {
        if (checkForReferenceEquality(
                questionMarkToken)) {
            return this;
        }

        return NodeFactory.createRequiredExpressionNode(
                questionMarkToken);
    }

    public RequiredExpressionNodeModifier modify() {
        return new RequiredExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RequiredExpressionNodeModifier {
        private final RequiredExpressionNode oldNode;
        private Token questionMarkToken;

        public RequiredExpressionNodeModifier(RequiredExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.questionMarkToken = oldNode.questionMarkToken();
        }

        public RequiredExpressionNodeModifier withQuestionMarkToken(
                Token questionMarkToken) {
            Objects.requireNonNull(questionMarkToken, "questionMarkToken must not be null");
            this.questionMarkToken = questionMarkToken;
            return this;
        }

        public RequiredExpressionNode apply() {
            return oldNode.modify(
                    questionMarkToken);
        }
    }
}
