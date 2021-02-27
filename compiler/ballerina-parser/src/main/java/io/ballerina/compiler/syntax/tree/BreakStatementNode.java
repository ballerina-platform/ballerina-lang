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
public class BreakStatementNode extends StatementNode {

    public BreakStatementNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token breakToken() {
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
                "breakToken",
                "semicolonToken"};
    }

    public BreakStatementNode modify(
            Token breakToken,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                breakToken,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createBreakStatementNode(
                breakToken,
                semicolonToken);
    }

    public BreakStatementNodeModifier modify() {
        return new BreakStatementNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class BreakStatementNodeModifier {
        private final BreakStatementNode oldNode;
        private Token breakToken;
        private Token semicolonToken;

        public BreakStatementNodeModifier(BreakStatementNode oldNode) {
            this.oldNode = oldNode;
            this.breakToken = oldNode.breakToken();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public BreakStatementNodeModifier withBreakToken(
                Token breakToken) {
            Objects.requireNonNull(breakToken, "breakToken must not be null");
            this.breakToken = breakToken;
            return this;
        }

        public BreakStatementNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public BreakStatementNode apply() {
            return oldNode.modify(
                    breakToken,
                    semicolonToken);
        }
    }
}
