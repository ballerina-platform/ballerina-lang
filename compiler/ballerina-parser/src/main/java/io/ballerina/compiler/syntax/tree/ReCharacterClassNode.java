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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.3.0
 */
public class ReCharacterClassNode extends ReAtomNode {

    public ReCharacterClassNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracketAndNegation() {
        return childInBucket(0);
    }

    public Optional<ReCharSetNode> reCharSet() {
        return optionalChildInBucket(1);
    }

    public Token closeBracket() {
        return childInBucket(2);
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
                "openBracketAndNegation",
                "reCharSet",
                "closeBracket"};
    }

    public ReCharacterClassNode modify(
            Token openBracketAndNegation,
            ReCharSetNode reCharSet,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracketAndNegation,
                reCharSet,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createReCharacterClassNode(
                openBracketAndNegation,
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
        private Token openBracketAndNegation;
        private ReCharSetNode reCharSet;
        private Token closeBracket;

        public ReCharacterClassNodeModifier(ReCharacterClassNode oldNode) {
            this.oldNode = oldNode;
            this.openBracketAndNegation = oldNode.openBracketAndNegation();
            this.reCharSet = oldNode.reCharSet().orElse(null);
            this.closeBracket = oldNode.closeBracket();
        }

        public ReCharacterClassNodeModifier withOpenBracketAndNegation(
                Token openBracketAndNegation) {
            Objects.requireNonNull(openBracketAndNegation, "openBracketAndNegation must not be null");
            this.openBracketAndNegation = openBracketAndNegation;
            return this;
        }

        public ReCharacterClassNodeModifier withReCharSet(
                ReCharSetNode reCharSet) {
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
                    openBracketAndNegation,
                    reCharSet,
                    closeBracket);
        }
    }
}
