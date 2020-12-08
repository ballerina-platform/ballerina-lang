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
public class NamedArgMatchPatternNode extends NonTerminalNode {

    public NamedArgMatchPatternNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public IdentifierToken identifier() {
        return childInBucket(0);
    }

    public Token equalToken() {
        return childInBucket(1);
    }

    public Node matchPattern() {
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
                "identifier",
                "equalToken",
                "matchPattern"};
    }

    public NamedArgMatchPatternNode modify(
            IdentifierToken identifier,
            Token equalToken,
            Node matchPattern) {
        if (checkForReferenceEquality(
                identifier,
                equalToken,
                matchPattern)) {
            return this;
        }

        return NodeFactory.createNamedArgMatchPatternNode(
                identifier,
                equalToken,
                matchPattern);
    }

    public NamedArgMatchPatternNodeModifier modify() {
        return new NamedArgMatchPatternNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class NamedArgMatchPatternNodeModifier {
        private final NamedArgMatchPatternNode oldNode;
        private IdentifierToken identifier;
        private Token equalToken;
        private Node matchPattern;

        public NamedArgMatchPatternNodeModifier(NamedArgMatchPatternNode oldNode) {
            this.oldNode = oldNode;
            this.identifier = oldNode.identifier();
            this.equalToken = oldNode.equalToken();
            this.matchPattern = oldNode.matchPattern();
        }

        public NamedArgMatchPatternNodeModifier withIdentifier(
                IdentifierToken identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public NamedArgMatchPatternNodeModifier withEqualToken(
                Token equalToken) {
            Objects.requireNonNull(equalToken, "equalToken must not be null");
            this.equalToken = equalToken;
            return this;
        }

        public NamedArgMatchPatternNodeModifier withMatchPattern(
                Node matchPattern) {
            Objects.requireNonNull(matchPattern, "matchPattern must not be null");
            this.matchPattern = matchPattern;
            return this;
        }

        public NamedArgMatchPatternNode apply() {
            return oldNode.modify(
                    identifier,
                    equalToken,
                    matchPattern);
        }
    }
}
