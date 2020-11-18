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
public class TemplateExpressionNode extends ExpressionNode {

    public TemplateExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> type() {
        return optionalChildInBucket(0);
    }

    public Token startBacktick() {
        return childInBucket(1);
    }

    public NodeList<TemplateMemberNode> content() {
        return new NodeList<>(childInBucket(2));
    }

    public Token endBacktick() {
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
                "type",
                "startBacktick",
                "content",
                "endBacktick"};
    }

    public TemplateExpressionNode modify(
            SyntaxKind kind,
            Token type,
            Token startBacktick,
            NodeList<TemplateMemberNode> content,
            Token endBacktick) {
        if (checkForReferenceEquality(
                type,
                startBacktick,
                content.underlyingListNode(),
                endBacktick)) {
            return this;
        }

        return NodeFactory.createTemplateExpressionNode(
                kind,
                type,
                startBacktick,
                content,
                endBacktick);
    }

    public TemplateExpressionNodeModifier modify() {
        return new TemplateExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TemplateExpressionNodeModifier {
        private final TemplateExpressionNode oldNode;
        private Token type;
        private Token startBacktick;
        private NodeList<TemplateMemberNode> content;
        private Token endBacktick;

        public TemplateExpressionNodeModifier(TemplateExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.type = oldNode.type().orElse(null);
            this.startBacktick = oldNode.startBacktick();
            this.content = oldNode.content();
            this.endBacktick = oldNode.endBacktick();
        }

        public TemplateExpressionNodeModifier withType(
                Token type) {
            Objects.requireNonNull(type, "type must not be null");
            this.type = type;
            return this;
        }

        public TemplateExpressionNodeModifier withStartBacktick(
                Token startBacktick) {
            Objects.requireNonNull(startBacktick, "startBacktick must not be null");
            this.startBacktick = startBacktick;
            return this;
        }

        public TemplateExpressionNodeModifier withContent(
                NodeList<TemplateMemberNode> content) {
            Objects.requireNonNull(content, "content must not be null");
            this.content = content;
            return this;
        }

        public TemplateExpressionNodeModifier withEndBacktick(
                Token endBacktick) {
            Objects.requireNonNull(endBacktick, "endBacktick must not be null");
            this.endBacktick = endBacktick;
            return this;
        }

        public TemplateExpressionNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    type,
                    startBacktick,
                    content,
                    endBacktick);
        }
    }
}
