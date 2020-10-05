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
public class ContinueStatementNode extends StatementNode {

    public ContinueStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token continueToken() {
        return childInBucket(0);
    }

    public Token semicolonToken() {
        return childInBucket(1);
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
                "continueToken",
                "semicolonToken"};
    }

    public ContinueStatementNode modify(
            Token continueToken,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                continueToken,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createContinueStatementNode(
                continueToken,
                semicolonToken);
    }

    public ContinueStatementNodeModifier modify() {
        return new ContinueStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ContinueStatementNodeModifier {
        private final ContinueStatementNode oldNode;
        private Token continueToken;
        private Token semicolonToken;

        public ContinueStatementNodeModifier(ContinueStatementNode oldNode) {
            this.oldNode = oldNode;
            this.continueToken = oldNode.continueToken();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ContinueStatementNodeModifier withContinueToken(
                Token continueToken) {
            Objects.requireNonNull(continueToken, "continueToken must not be null");
            this.continueToken = continueToken;
            return this;
        }

        public ContinueStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ContinueStatementNode apply() {
            return oldNode.modify(
                    continueToken,
                    semicolonToken);
        }
    }
}
