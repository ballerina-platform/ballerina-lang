/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class MarkdownCodeBlockNode extends DocumentationNode {

    public MarkdownCodeBlockNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token startLineHashToken() {
        return childInBucket(0);
    }

    public Token startBacktick() {
        return childInBucket(1);
    }

    public Optional<Token> langAttribute() {
        return optionalChildInBucket(2);
    }

    public NodeList<MarkdownCodeLineNode> codeLines() {
        return new NodeList<>(childInBucket(3));
    }

    public Token endLineHashToken() {
        return childInBucket(4);
    }

    public Token endBacktick() {
        return childInBucket(5);
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
                "startLineHashToken",
                "startBacktick",
                "langAttribute",
                "codeLines",
                "endLineHashToken",
                "endBacktick"};
    }

    public MarkdownCodeBlockNode modify(
            Token startLineHashToken,
            Token startBacktick,
            Token langAttribute,
            NodeList<MarkdownCodeLineNode> codeLines,
            Token endLineHashToken,
            Token endBacktick) {
        if (checkForReferenceEquality(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines.underlyingListNode(),
                endLineHashToken,
                endBacktick)) {
            return this;
        }

        return NodeFactory.createMarkdownCodeBlockNode(
                startLineHashToken,
                startBacktick,
                langAttribute,
                codeLines,
                endLineHashToken,
                endBacktick);
    }

    public MarkdownCodeBlockNodeModifier modify() {
        return new MarkdownCodeBlockNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class MarkdownCodeBlockNodeModifier {
        private final MarkdownCodeBlockNode oldNode;
        private Token startLineHashToken;
        private Token startBacktick;
        @Nullable
        private Token langAttribute;
        private NodeList<MarkdownCodeLineNode> codeLines;
        private Token endLineHashToken;
        private Token endBacktick;

        public MarkdownCodeBlockNodeModifier(MarkdownCodeBlockNode oldNode) {
            this.oldNode = oldNode;
            this.startLineHashToken = oldNode.startLineHashToken();
            this.startBacktick = oldNode.startBacktick();
            this.langAttribute = oldNode.langAttribute().orElse(null);
            this.codeLines = oldNode.codeLines();
            this.endLineHashToken = oldNode.endLineHashToken();
            this.endBacktick = oldNode.endBacktick();
        }

        public MarkdownCodeBlockNodeModifier withStartLineHashToken(
                Token startLineHashToken) {
            Objects.requireNonNull(startLineHashToken, "startLineHashToken must not be null");
            this.startLineHashToken = startLineHashToken;
            return this;
        }

        public MarkdownCodeBlockNodeModifier withStartBacktick(
                Token startBacktick) {
            Objects.requireNonNull(startBacktick, "startBacktick must not be null");
            this.startBacktick = startBacktick;
            return this;
        }

        public MarkdownCodeBlockNodeModifier withLangAttribute(
                Token langAttribute) {
            this.langAttribute = langAttribute;
            return this;
        }

        public MarkdownCodeBlockNodeModifier withCodeLines(
                NodeList<MarkdownCodeLineNode> codeLines) {
            Objects.requireNonNull(codeLines, "codeLines must not be null");
            this.codeLines = codeLines;
            return this;
        }

        public MarkdownCodeBlockNodeModifier withEndLineHashToken(
                Token endLineHashToken) {
            Objects.requireNonNull(endLineHashToken, "endLineHashToken must not be null");
            this.endLineHashToken = endLineHashToken;
            return this;
        }

        public MarkdownCodeBlockNodeModifier withEndBacktick(
                Token endBacktick) {
            Objects.requireNonNull(endBacktick, "endBacktick must not be null");
            this.endBacktick = endBacktick;
            return this;
        }

        public MarkdownCodeBlockNode apply() {
            return oldNode.modify(
                    startLineHashToken,
                    startBacktick,
                    langAttribute,
                    codeLines,
                    endLineHashToken,
                    endBacktick);
        }
    }
}
