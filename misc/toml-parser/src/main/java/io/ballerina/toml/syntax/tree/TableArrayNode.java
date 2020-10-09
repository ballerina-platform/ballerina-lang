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
public class TableArrayNode extends DocumentMemberDeclarationNode {

    public TableArrayNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token firstOpenBracket() {
        return childInBucket(0);
    }

    public Token secondOpenBracket() {
        return childInBucket(1);
    }

    public SeparatedNodeList<ValueNode> identifier() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Token firstCloseBracket() {
        return childInBucket(3);
    }

    public Token secondCloseBracket() {
        return childInBucket(4);
    }

    public NodeList<KeyValueNode> fields() {
        return new NodeList<>(childInBucket(5));
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
                "firstOpenBracket",
                "secondOpenBracket",
                "identifier",
                "firstCloseBracket",
                "secondCloseBracket",
                "fields"};
    }

    public TableArrayNode modify(
            Token firstOpenBracket,
            Token secondOpenBracket,
            SeparatedNodeList<ValueNode> identifier,
            Token firstCloseBracket,
            Token secondCloseBracket,
            NodeList<KeyValueNode> fields) {
        if (checkForReferenceEquality(
                firstOpenBracket,
                secondOpenBracket,
                identifier.underlyingListNode(),
                firstCloseBracket,
                secondCloseBracket,
                fields.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createTableArrayNode(
                firstOpenBracket,
                secondOpenBracket,
                identifier,
                firstCloseBracket,
                secondCloseBracket,
                fields);
    }

    public TableArrayNodeModifier modify() {
        return new TableArrayNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TableArrayNodeModifier {
        private final TableArrayNode oldNode;
        private Token firstOpenBracket;
        private Token secondOpenBracket;
        private SeparatedNodeList<ValueNode> identifier;
        private Token firstCloseBracket;
        private Token secondCloseBracket;
        private NodeList<KeyValueNode> fields;

        public TableArrayNodeModifier(TableArrayNode oldNode) {
            this.oldNode = oldNode;
            this.firstOpenBracket = oldNode.firstOpenBracket();
            this.secondOpenBracket = oldNode.secondOpenBracket();
            this.identifier = oldNode.identifier();
            this.firstCloseBracket = oldNode.firstCloseBracket();
            this.secondCloseBracket = oldNode.secondCloseBracket();
            this.fields = oldNode.fields();
        }

        public TableArrayNodeModifier withFirstOpenBracket(
                Token firstOpenBracket) {
            Objects.requireNonNull(firstOpenBracket, "firstOpenBracket must not be null");
            this.firstOpenBracket = firstOpenBracket;
            return this;
        }

        public TableArrayNodeModifier withSecondOpenBracket(
                Token secondOpenBracket) {
            Objects.requireNonNull(secondOpenBracket, "secondOpenBracket must not be null");
            this.secondOpenBracket = secondOpenBracket;
            return this;
        }

        public TableArrayNodeModifier withIdentifier(
                SeparatedNodeList<ValueNode> identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public TableArrayNodeModifier withFirstCloseBracket(
                Token firstCloseBracket) {
            Objects.requireNonNull(firstCloseBracket, "firstCloseBracket must not be null");
            this.firstCloseBracket = firstCloseBracket;
            return this;
        }

        public TableArrayNodeModifier withSecondCloseBracket(
                Token secondCloseBracket) {
            Objects.requireNonNull(secondCloseBracket, "secondCloseBracket must not be null");
            this.secondCloseBracket = secondCloseBracket;
            return this;
        }

        public TableArrayNodeModifier withFields(
                NodeList<KeyValueNode> fields) {
            Objects.requireNonNull(fields, "fields must not be null");
            this.fields = fields;
            return this;
        }

        public TableArrayNode apply() {
            return oldNode.modify(
                    firstOpenBracket,
                    secondOpenBracket,
                    identifier,
                    firstCloseBracket,
                    secondCloseBracket,
                    fields);
        }
    }
}
