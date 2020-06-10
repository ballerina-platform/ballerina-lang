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
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class KeySpecifierNode extends NonTerminalNode {

    public KeySpecifierNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token keyKeyword() {
        return childInBucket(0);
    }

    public Token openParenToken() {
        return childInBucket(1);
    }

    public SeparatedNodeList<IdentifierToken> fieldNames() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Token closeParenToken() {
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
                "keyKeyword",
                "openParenToken",
                "fieldNames",
                "closeParenToken"};
    }

    public KeySpecifierNode modify(
            Token keyKeyword,
            Token openParenToken,
            SeparatedNodeList<IdentifierToken> fieldNames,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                keyKeyword,
                openParenToken,
                fieldNames.underlyingListNode(),
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createKeySpecifierNode(
                keyKeyword,
                openParenToken,
                fieldNames,
                closeParenToken);
    }

    public KeySpecifierNodeModifier modify() {
        return new KeySpecifierNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class KeySpecifierNodeModifier {
        private final KeySpecifierNode oldNode;
        private Token keyKeyword;
        private Token openParenToken;
        private SeparatedNodeList<IdentifierToken> fieldNames;
        private Token closeParenToken;

        public KeySpecifierNodeModifier(KeySpecifierNode oldNode) {
            this.oldNode = oldNode;
            this.keyKeyword = oldNode.keyKeyword();
            this.openParenToken = oldNode.openParenToken();
            this.fieldNames = oldNode.fieldNames();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public KeySpecifierNodeModifier withKeyKeyword(
                Token keyKeyword) {
            Objects.requireNonNull(keyKeyword, "keyKeyword must not be null");
            this.keyKeyword = keyKeyword;
            return this;
        }

        public KeySpecifierNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public KeySpecifierNodeModifier withFieldNames(
                SeparatedNodeList<IdentifierToken> fieldNames) {
            Objects.requireNonNull(fieldNames, "fieldNames must not be null");
            this.fieldNames = fieldNames;
            return this;
        }

        public KeySpecifierNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public KeySpecifierNode apply() {
            return oldNode.modify(
                    keyKeyword,
                    openParenToken,
                    fieldNames,
                    closeParenToken);
        }
    }
}
