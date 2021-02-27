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
public class KeyValueNode extends DocumentMemberDeclarationNode {

    public KeyValueNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SeparatedNodeList<ValueNode> identifier() {
        return new SeparatedNodeList<>(childInBucket(0));
    }

    public Token assign() {
        return childInBucket(1);
    }

    public ValueNode value() {
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
                "assign",
                "value"};
    }

    public KeyValueNode modify(
            SeparatedNodeList<ValueNode> identifier,
            Token assign,
            ValueNode value) {
        if (checkForReferenceEquality(
                identifier.underlyingListNode(),
                assign,
                value)) {
            return this;
        }

        return NodeFactory.createKeyValueNode(
                identifier,
                assign,
                value);
    }

    public KeyValueNodeModifier modify() {
        return new KeyValueNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class KeyValueNodeModifier {
        private final KeyValueNode oldNode;
        private SeparatedNodeList<ValueNode> identifier;
        private Token assign;
        private ValueNode value;

        public KeyValueNodeModifier(KeyValueNode oldNode) {
            this.oldNode = oldNode;
            this.identifier = oldNode.identifier();
            this.assign = oldNode.assign();
            this.value = oldNode.value();
        }

        public KeyValueNodeModifier withIdentifier(
                SeparatedNodeList<ValueNode> identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public KeyValueNodeModifier withAssign(
                Token assign) {
            Objects.requireNonNull(assign, "assign must not be null");
            this.assign = assign;
            return this;
        }

        public KeyValueNodeModifier withValue(
                ValueNode value) {
            Objects.requireNonNull(value, "value must not be null");
            this.value = value;
            return this;
        }

        public KeyValueNode apply() {
            return oldNode.modify(
                    identifier,
                    assign,
                    value);
        }
    }
}
