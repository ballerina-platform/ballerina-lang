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
public class Base64GroupNode extends NonTerminalNode {

    public Base64GroupNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startChar() {
        return childInBucket(0);
    }

    public Token secondChar() {
        return childInBucket(1);
    }

    public Token thirdChar() {
        return childInBucket(2);
    }

    public Token endChar() {
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
                "startChar",
                "secondChar",
                "thirdChar",
                "endChar"};
    }

    public Base64GroupNode modify(
            Token startChar,
            Token secondChar,
            Token thirdChar,
            Token endChar) {
        if (checkForReferenceEquality(
                startChar,
                secondChar,
                thirdChar,
                endChar)) {
            return this;
        }

        return NodeFactory.createBase64GroupNode(
                startChar,
                secondChar,
                thirdChar,
                endChar);
    }

    public Base64GroupNodeModifier modify() {
        return new Base64GroupNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class Base64GroupNodeModifier {
        private final Base64GroupNode oldNode;
        private Token startChar;
        private Token secondChar;
        private Token thirdChar;
        private Token endChar;

        public Base64GroupNodeModifier(Base64GroupNode oldNode) {
            this.oldNode = oldNode;
            this.startChar = oldNode.startChar();
            this.secondChar = oldNode.secondChar();
            this.thirdChar = oldNode.thirdChar();
            this.endChar = oldNode.endChar();
        }

        public Base64GroupNodeModifier withStartChar(
                Token startChar) {
            Objects.requireNonNull(startChar, "startChar must not be null");
            this.startChar = startChar;
            return this;
        }

        public Base64GroupNodeModifier withSecondChar(
                Token secondChar) {
            Objects.requireNonNull(secondChar, "secondChar must not be null");
            this.secondChar = secondChar;
            return this;
        }

        public Base64GroupNodeModifier withThirdChar(
                Token thirdChar) {
            Objects.requireNonNull(thirdChar, "thirdChar must not be null");
            this.thirdChar = thirdChar;
            return this;
        }

        public Base64GroupNodeModifier withEndChar(
                Token endChar) {
            Objects.requireNonNull(endChar, "endChar must not be null");
            this.endChar = endChar;
            return this;
        }

        public Base64GroupNode apply() {
            return oldNode.modify(
                    startChar,
                    secondChar,
                    thirdChar,
                    endChar);
        }
    }
}
