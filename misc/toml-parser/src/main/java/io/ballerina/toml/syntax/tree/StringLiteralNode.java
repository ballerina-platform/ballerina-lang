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

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class StringLiteralNode extends ValueNode {

    public StringLiteralNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startDoubleQuote() {
        return childInBucket(0);
    }

    public Token content() {
        return childInBucket(1);
    }

    public Token endDoubleQuote() {
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
                "startDoubleQuote",
                "content",
                "endDoubleQuote"};
    }

    public StringLiteralNode modify(
            Token startDoubleQuote,
            Token content,
            Token endDoubleQuote) {
        if (checkForReferenceEquality(
                startDoubleQuote,
                content,
                endDoubleQuote)) {
            return this;
        }

        return NodeFactory.createStringLiteralNode(
                startDoubleQuote,
                content,
                endDoubleQuote);
    }

    public StringLiteralNodeModifier modify() {
        return new StringLiteralNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class StringLiteralNodeModifier {
        private final StringLiteralNode oldNode;
        private Token startDoubleQuote;
        private Token content;
        private Token endDoubleQuote;

        public StringLiteralNodeModifier(StringLiteralNode oldNode) {
            this.oldNode = oldNode;
            this.startDoubleQuote = oldNode.startDoubleQuote();
            this.content = oldNode.content();
            this.endDoubleQuote = oldNode.endDoubleQuote();
        }

        public StringLiteralNodeModifier withStartDoubleQuote(
                Token startDoubleQuote) {
            Objects.requireNonNull(startDoubleQuote, "startDoubleQuote must not be null");
            this.startDoubleQuote = startDoubleQuote;
            return this;
        }

        public StringLiteralNodeModifier withContent(
                Token content) {
            Objects.requireNonNull(content, "content must not be null");
            this.content = content;
            return this;
        }

        public StringLiteralNodeModifier withEndDoubleQuote(
                Token endDoubleQuote) {
            Objects.requireNonNull(endDoubleQuote, "endDoubleQuote must not be null");
            this.endDoubleQuote = endDoubleQuote;
            return this;
        }

        public StringLiteralNode apply() {
            return oldNode.modify(
                    startDoubleQuote,
                    content,
                    endDoubleQuote);
        }
    }
}
