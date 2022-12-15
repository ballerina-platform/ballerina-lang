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
 * @since 2201.3.0
 */
public class ReQuoteEscapeNode extends NonTerminalNode {

    public ReQuoteEscapeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token slashToken() {
        return childInBucket(0);
    }

    public Node reSyntaxChar() {
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
                "slashToken",
                "reSyntaxChar"};
    }

    public ReQuoteEscapeNode modify(
            Token slashToken,
            Node reSyntaxChar) {
        if (checkForReferenceEquality(
                slashToken,
                reSyntaxChar)) {
            return this;
        }

        return NodeFactory.createReQuoteEscapeNode(
                slashToken,
                reSyntaxChar);
    }

    public ReQuoteEscapeNodeModifier modify() {
        return new ReQuoteEscapeNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReQuoteEscapeNodeModifier {
        private final ReQuoteEscapeNode oldNode;
        private Token slashToken;
        private Node reSyntaxChar;

        public ReQuoteEscapeNodeModifier(ReQuoteEscapeNode oldNode) {
            this.oldNode = oldNode;
            this.slashToken = oldNode.slashToken();
            this.reSyntaxChar = oldNode.reSyntaxChar();
        }

        public ReQuoteEscapeNodeModifier withSlashToken(
                Token slashToken) {
            Objects.requireNonNull(slashToken, "slashToken must not be null");
            this.slashToken = slashToken;
            return this;
        }

        public ReQuoteEscapeNodeModifier withReSyntaxChar(
                Node reSyntaxChar) {
            Objects.requireNonNull(reSyntaxChar, "reSyntaxChar must not be null");
            this.reSyntaxChar = reSyntaxChar;
            return this;
        }

        public ReQuoteEscapeNode apply() {
            return oldNode.modify(
                    slashToken,
                    reSyntaxChar);
        }
    }
}
