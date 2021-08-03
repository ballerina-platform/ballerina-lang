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
public class InlineTableNode extends ValueNode {

    public InlineTableNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBrace() {
        return childInBucket(0);
    }

    public SeparatedNodeList<KeyValueNode> values() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token closeBrace() {
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
                "openBrace",
                "values",
                "closeBrace"};
    }

    public InlineTableNode modify(
            Token openBrace,
            SeparatedNodeList<KeyValueNode> values,
            Token closeBrace) {
        if (checkForReferenceEquality(
                openBrace,
                values.underlyingListNode(),
                closeBrace)) {
            return this;
        }

        return NodeFactory.createInlineTableNode(
                openBrace,
                values,
                closeBrace);
    }

    public InlineTableNodeModifier modify() {
        return new InlineTableNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class InlineTableNodeModifier {
        private final InlineTableNode oldNode;
        private Token openBrace;
        private SeparatedNodeList<KeyValueNode> values;
        private Token closeBrace;

        public InlineTableNodeModifier(InlineTableNode oldNode) {
            this.oldNode = oldNode;
            this.openBrace = oldNode.openBrace();
            this.values = oldNode.values();
            this.closeBrace = oldNode.closeBrace();
        }

        public InlineTableNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public InlineTableNodeModifier withValues(
                SeparatedNodeList<KeyValueNode> values) {
            Objects.requireNonNull(values, "values must not be null");
            this.values = values;
            return this;
        }

        public InlineTableNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public InlineTableNode apply() {
            return oldNode.modify(
                    openBrace,
                    values,
                    closeBrace);
        }
    }
}
