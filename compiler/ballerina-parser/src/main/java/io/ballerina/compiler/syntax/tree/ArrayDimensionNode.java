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
public class ArrayDimensionNode extends NonTerminalNode {

    public ArrayDimensionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBracket() {
        return childInBucket(0);
    }

    public Optional<Node> arrayLength() {
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
                "openBracket",
                "arrayLength",
                "closeBracket"};
    }

    public ArrayDimensionNode modify(
            Token openBracket,
            Node arrayLength,
            Token closeBracket) {
        if (checkForReferenceEquality(
                openBracket,
                arrayLength,
                closeBracket)) {
            return this;
        }

        return NodeFactory.createArrayDimensionNode(
                openBracket,
                arrayLength,
                closeBracket);
    }

    public ArrayDimensionNodeModifier modify() {
        return new ArrayDimensionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ArrayDimensionNodeModifier {
        private final ArrayDimensionNode oldNode;
        private Token openBracket;
        private Node arrayLength;
        private Token closeBracket;

        public ArrayDimensionNodeModifier(ArrayDimensionNode oldNode) {
            this.oldNode = oldNode;
            this.openBracket = oldNode.openBracket();
            this.arrayLength = oldNode.arrayLength().orElse(null);
            this.closeBracket = oldNode.closeBracket();
        }

        public ArrayDimensionNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public ArrayDimensionNodeModifier withArrayLength(
                Node arrayLength) {
            this.arrayLength = arrayLength;
            return this;
        }

        public ArrayDimensionNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public ArrayDimensionNode apply() {
            return oldNode.modify(
                    openBracket,
                    arrayLength,
                    closeBracket);
        }
    }
}
