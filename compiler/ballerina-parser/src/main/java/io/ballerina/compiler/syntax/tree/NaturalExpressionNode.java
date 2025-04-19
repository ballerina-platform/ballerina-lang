/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.13.0
 */
public class NaturalExpressionNode extends ExpressionNode {

    public NaturalExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> constKeyword() {
        return optionalChildInBucket(0);
    }

    public Token naturalKeyword() {
        return childInBucket(1);
    }

    public Optional<ParenthesizedArgList> parenthesizedArgList() {
        return optionalChildInBucket(2);
    }

    public Token openBraceToken() {
        return childInBucket(3);
    }

    public NodeList<Node> prompt() {
        return new NodeList<>(childInBucket(4));
    }

    public Token closeBraceToken() {
        return childInBucket(5);
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
                "constKeyword",
                "naturalKeyword",
                "parenthesizedArgList",
                "openBraceToken",
                "prompt",
                "closeBraceToken"};
    }

    public NaturalExpressionNode modify(
            Token constKeyword,
            Token naturalKeyword,
            ParenthesizedArgList parenthesizedArgList,
            Token openBraceToken,
            NodeList<Node> prompt,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                constKeyword,
                naturalKeyword,
                parenthesizedArgList,
                openBraceToken,
                prompt.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createNaturalExpressionNode(
                constKeyword,
                naturalKeyword,
                parenthesizedArgList,
                openBraceToken,
                prompt,
                closeBraceToken);
    }

    public NaturalExpressionNodeModifier modify() {
        return new NaturalExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.13.0
     */
    public static class NaturalExpressionNodeModifier {
        private final NaturalExpressionNode oldNode;
        private Token constKeyword;
        private Token naturalKeyword;
        private ParenthesizedArgList parenthesizedArgList;
        private Token openBraceToken;
        private NodeList<Node> prompt;
        private Token closeBraceToken;

        public NaturalExpressionNodeModifier(NaturalExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.constKeyword = oldNode.constKeyword().orElse(null);
            this.naturalKeyword = oldNode.naturalKeyword();
            this.parenthesizedArgList = oldNode.parenthesizedArgList().orElse(null);
            this.openBraceToken = oldNode.openBraceToken();
            this.prompt = oldNode.prompt();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public NaturalExpressionNodeModifier withConstKeyword(
                Token constKeyword) {
            this.constKeyword = constKeyword;
            return this;
        }

        public NaturalExpressionNodeModifier withNaturalKeyword(
                Token naturalKeyword) {
            Objects.requireNonNull(naturalKeyword, "naturalKeyword must not be null");
            this.naturalKeyword = naturalKeyword;
            return this;
        }

        public NaturalExpressionNodeModifier withParenthesizedArgList(
                ParenthesizedArgList parenthesizedArgList) {
            this.parenthesizedArgList = parenthesizedArgList;
            return this;
        }

        public NaturalExpressionNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public NaturalExpressionNodeModifier withPrompt(
                NodeList<Node> prompt) {
            Objects.requireNonNull(prompt, "prompt must not be null");
            this.prompt = prompt;
            return this;
        }

        public NaturalExpressionNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public NaturalExpressionNode apply() {
            return oldNode.modify(
                    constKeyword,
                    naturalKeyword,
                    parenthesizedArgList,
                    openBraceToken,
                    prompt,
                    closeBraceToken);
        }
    }
}
