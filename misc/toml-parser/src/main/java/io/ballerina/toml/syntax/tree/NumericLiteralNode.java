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
public class NumericLiteralNode extends ValueNode {

    public NumericLiteralNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> sign() {
        return optionalChildInBucket(0);
    }

    public Token value() {
        return childInBucket(1);
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
                "sign",
                "value"};
    }

    public NumericLiteralNode modify(
            SyntaxKind kind,
            Token sign,
            Token value) {
        if (checkForReferenceEquality(
                sign,
                value)) {
            return this;
        }

        return NodeFactory.createNumericLiteralNode(
                kind,
                sign,
                value);
    }

    public NumericLiteralNodeModifier modify() {
        return new NumericLiteralNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class NumericLiteralNodeModifier {
        private final NumericLiteralNode oldNode;
        @Nullable
        private Token sign;
        private Token value;

        public NumericLiteralNodeModifier(NumericLiteralNode oldNode) {
            this.oldNode = oldNode;
            this.sign = oldNode.sign().orElse(null);
            this.value = oldNode.value();
        }

        public NumericLiteralNodeModifier withSign(
                Token sign) {
            this.sign = sign;
            return this;
        }

        public NumericLiteralNodeModifier withValue(
                Token value) {
            Objects.requireNonNull(value, "value must not be null");
            this.value = value;
            return this;
        }

        public NumericLiteralNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    sign,
                    value);
        }
    }
}
