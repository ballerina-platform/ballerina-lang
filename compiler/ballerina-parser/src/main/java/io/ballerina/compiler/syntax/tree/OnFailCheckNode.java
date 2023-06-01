/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
public class OnFailCheckNode extends NonTerminalNode {

    public OnFailCheckNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token onKeyword() {
        return childInBucket(0);
    }

    public Token failKeyword() {
        return childInBucket(1);
    }

    public IdentifierToken identifier() {
        return childInBucket(2);
    }

    public Token rightArrowToken() {
        return childInBucket(3);
    }

    public ErrorConstructorExpressionNode errorConstructor() {
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
                "onKeyword",
                "failKeyword",
                "identifier",
                "rightArrowToken",
                "errorConstructor"};
    }

    public OnFailCheckNode modify(
            Token onKeyword,
            Token failKeyword,
            IdentifierToken identifier,
            Token rightArrowToken,
            ErrorConstructorExpressionNode errorConstructor) {
        if (checkForReferenceEquality(
                onKeyword,
                failKeyword,
                identifier,
                rightArrowToken,
                errorConstructor)) {
            return this;
        }

        return NodeFactory.createOnFailCheckNode(
                onKeyword,
                failKeyword,
                identifier,
                rightArrowToken,
                errorConstructor);
    }

    public OnFailCheckNodeModifier modify() {
        return new OnFailCheckNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class OnFailCheckNodeModifier {
        private final OnFailCheckNode oldNode;
        private Token onKeyword;
        private Token failKeyword;
        private IdentifierToken identifier;
        private Token rightArrowToken;
        private ErrorConstructorExpressionNode errorConstructor;

        public OnFailCheckNodeModifier(OnFailCheckNode oldNode) {
            this.oldNode = oldNode;
            this.onKeyword = oldNode.onKeyword();
            this.failKeyword = oldNode.failKeyword();
            this.identifier = oldNode.identifier();
            this.rightArrowToken = oldNode.rightArrowToken();
            this.errorConstructor = oldNode.errorConstructor();
        }

        public OnFailCheckNodeModifier withOnKeyword(
                Token onKeyword) {
            Objects.requireNonNull(onKeyword, "onKeyword must not be null");
            this.onKeyword = onKeyword;
            return this;
        }

        public OnFailCheckNodeModifier withFailKeyword(
                Token failKeyword) {
            Objects.requireNonNull(failKeyword, "failKeyword must not be null");
            this.failKeyword = failKeyword;
            return this;
        }

        public OnFailCheckNodeModifier withIdentifier(
                IdentifierToken identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public OnFailCheckNodeModifier withRightArrowToken(
                Token rightArrowToken) {
            Objects.requireNonNull(rightArrowToken, "rightArrowToken must not be null");
            this.rightArrowToken = rightArrowToken;
            return this;
        }

        public OnFailCheckNodeModifier withErrorConstructor(
                ErrorConstructorExpressionNode errorConstructor) {
            Objects.requireNonNull(errorConstructor, "errorConstructor must not be null");
            this.errorConstructor = errorConstructor;
            return this;
        }

        public OnFailCheckNode apply() {
            return oldNode.modify(
                    onKeyword,
                    failKeyword,
                    identifier,
                    rightArrowToken,
                    errorConstructor);
        }
    }
}
