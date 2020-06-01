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
public class ByteArrayLiteralNode extends ExpressionNode {

    public ByteArrayLiteralNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token type() {
        return childInBucket(0);
    }

    public Token startBacktick() {
        return childInBucket(1);
    }

    public Token content() {
        return childInBucket(2);
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

    public ByteArrayLiteralNode modify(
            Token type,
            Token startBacktick,
            Token content,
            Token endBacktick) {
        if (checkForReferenceEquality(
                type,
                startBacktick,
                content,
                endBacktick)) {
            return this;
        }

        return NodeFactory.createByteArrayLiteralNode(
                type,
                startBacktick,
                content,
                endBacktick);
    }

    public ByteArrayLiteralNodeModifier modify() {
        return new ByteArrayLiteralNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ByteArrayLiteralNodeModifier {
        private final ByteArrayLiteralNode oldNode;
        private Token type;
        private Token startBacktick;
        private Token content;
        private Token endBacktick;

        public ByteArrayLiteralNodeModifier(ByteArrayLiteralNode oldNode) {
            this.oldNode = oldNode;
            this.type = oldNode.type();
            this.startBacktick = oldNode.startBacktick();
            this.content = oldNode.content();
            this.endBacktick = oldNode.endBacktick();
        }

        public ByteArrayLiteralNodeModifier withType(
                Token type) {
            Objects.requireNonNull(type, "type must not be null");
            this.type = type;
            return this;
        }

        public ByteArrayLiteralNodeModifier withStartBacktick(
                Token startBacktick) {
            Objects.requireNonNull(startBacktick, "startBacktick must not be null");
            this.startBacktick = startBacktick;
            return this;
        }

        public ByteArrayLiteralNodeModifier withContent(
                Token content) {
            Objects.requireNonNull(content, "content must not be null");
            this.content = content;
            return this;
        }

        public ByteArrayLiteralNodeModifier withEndBacktick(
                Token endBacktick) {
            Objects.requireNonNull(endBacktick, "endBacktick must not be null");
            this.endBacktick = endBacktick;
            return this;
        }

        public ByteArrayLiteralNode apply() {
            return oldNode.modify(
                    type,
                    startBacktick,
                    content,
                    endBacktick);
        }
    }
}
