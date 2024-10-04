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
package io.ballerina.toml.syntax.tree;

import io.ballerina.toml.internal.parser.tree.STNode;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class LiteralStringLiteralNode extends ValueNode {

    public LiteralStringLiteralNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startSingleQuote() {
        return childInBucket(0);
    }

    public Optional<Token> content() {
        return optionalChildInBucket(1);
    }

    public Token endSingleQuote() {
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
                "startSingleQuote",
                "content",
                "endSingleQuote"};
    }

    public LiteralStringLiteralNode modify(
            Token startSingleQuote,
            Token content,
            Token endSingleQuote) {
        if (checkForReferenceEquality(
                startSingleQuote,
                content,
                endSingleQuote)) {
            return this;
        }

        return NodeFactory.createLiteralStringLiteralNode(
                startSingleQuote,
                content,
                endSingleQuote);
    }

    public LiteralStringLiteralNodeModifier modify() {
        return new LiteralStringLiteralNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class LiteralStringLiteralNodeModifier {
        private final LiteralStringLiteralNode oldNode;
        private Token startSingleQuote;
        @Nullable
        private Token content;
        private Token endSingleQuote;

        public LiteralStringLiteralNodeModifier(LiteralStringLiteralNode oldNode) {
            this.oldNode = oldNode;
            this.startSingleQuote = oldNode.startSingleQuote();
            this.content = oldNode.content().orElse(null);
            this.endSingleQuote = oldNode.endSingleQuote();
        }

        public LiteralStringLiteralNodeModifier withStartSingleQuote(
                Token startSingleQuote) {
            Objects.requireNonNull(startSingleQuote, "startSingleQuote must not be null");
            this.startSingleQuote = startSingleQuote;
            return this;
        }

        public LiteralStringLiteralNodeModifier withContent(
                Token content) {
            this.content = content;
            return this;
        }

        public LiteralStringLiteralNodeModifier withEndSingleQuote(
                Token endSingleQuote) {
            Objects.requireNonNull(endSingleQuote, "endSingleQuote must not be null");
            this.endSingleQuote = endSingleQuote;
            return this;
        }

        public LiteralStringLiteralNode apply() {
            return oldNode.modify(
                    startSingleQuote,
                    content,
                    endSingleQuote);
        }
    }
}
