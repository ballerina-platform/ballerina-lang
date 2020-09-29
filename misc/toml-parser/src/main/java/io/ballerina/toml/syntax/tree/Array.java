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
public class Array extends ValueNode {

    public Array(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public SeparatedNodeList<ValueNode> values() {
        return new SeparatedNodeList<>(childInBucket(1));
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
                "openBracket",
                "values",
                "closeBracket"};
    }

    public Array modify(
            Token openBracket,
            SeparatedNodeList<ValueNode> values,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                values.underlyingListNode(),
                closeBracket)) {
            return this;
        }

        return NodeFactory.createArray(
                openBracket,
                values,
                closeBracket);
    }

    public ArrayModifier modify() {
        return new ArrayModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ArrayModifier {
        private final Array oldNode;
        private Token openBracket;
        private SeparatedNodeList<ValueNode> values;
        private Token closeBracket;

        public ArrayModifier(Array oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.values = oldNode.values();
            this.closeBracket = oldNode.closeBracket();
        }

        public ArrayModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ArrayModifier withValues(
                SeparatedNodeList<ValueNode> values) {
            Objects.requireNonNull(values, "values must not be null");
            this.values = values;
            return this;
        }

        public ArrayModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public Array apply() {
            return oldNode.modify(
                    openBracket,
                    values,
                    closeBracket);
        }
    }
}
