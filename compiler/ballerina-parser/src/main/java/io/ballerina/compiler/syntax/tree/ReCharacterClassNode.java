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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ReCharacterClassNode extends NonTerminalNode {

    public ReCharacterClassNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public Optional<Token> negation() {
        return optionalChildInBucket(1);
    }

    public Optional<Node> reCharSet() {
        return optionalChildInBucket(2);
    }

    public Token closeBracket() {
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
                "openBracket",
                "negation",
                "reCharSet",
                "closeBracket"};
    }

    public ReCharacterClassNode modify(
            Token openBracket,
            Token negation,
            Node reCharSet,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                negation,
                reCharSet,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createReCharacterClassNode(
                openBracket,
                negation,
                reCharSet,
                closeBracket);
    }

    public ReCharacterClassNodeModifier modify() {
        return new ReCharacterClassNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReCharacterClassNodeModifier {
        private final ReCharacterClassNode oldNode;
        private Token openBracket;
        private Token negation;
        private Node reCharSet;
        private Token closeBracket;

        public ReCharacterClassNodeModifier(ReCharacterClassNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.negation = oldNode.negation().orElse(null);
            this.reCharSet = oldNode.reCharSet().orElse(null);
            this.closeBracket = oldNode.closeBracket();
        }

        public ReCharacterClassNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ReCharacterClassNodeModifier withNegation(
                Token negation) {
            this.negation = negation;
            return this;
        }

        public ReCharacterClassNodeModifier withReCharSet(
                Node reCharSet) {
            this.reCharSet = reCharSet;
            return this;
        }

        public ReCharacterClassNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public ReCharacterClassNode apply() {
            return oldNode.modify(
                    openBracket,
                    negation,
                    reCharSet,
                    closeBracket);
        }
    }
}
