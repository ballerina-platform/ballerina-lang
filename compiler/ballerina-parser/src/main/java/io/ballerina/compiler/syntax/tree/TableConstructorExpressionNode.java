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
public class TableConstructorExpressionNode extends ExpressionNode {

    public TableConstructorExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token tableKeyword() {
        return childInBucket(0);
    }

    public Optional<KeySpecifierNode> keySpecifier() {
        return optionalChildInBucket(1);
    }

    public Token openBracket() {
        return childInBucket(2);
    }

    public SeparatedNodeList<Node> rows() {
        return new SeparatedNodeList<>(childInBucket(3));
    }

    public Token closeBracket() {
        return childInBucket(4);
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
                "tableKeyword",
                "keySpecifier",
                "openBracket",
                "rows",
                "closeBracket"};
    }

    public TableConstructorExpressionNode modify(
            Token tableKeyword,
            KeySpecifierNode keySpecifier,
            Token openBracket,
            SeparatedNodeList<Node> rows,
            Token closeBracket) {
        if (checkForReferenceEquality(
                tableKeyword,
                keySpecifier,
                openBracket,
                rows.underlyingListNode(),
                closeBracket)) {
            return this;
        }

        return NodeFactory.createTableConstructorExpressionNode(
                tableKeyword,
                keySpecifier,
                openBracket,
                rows,
                closeBracket);
    }

    public TableConstructorExpressionNodeModifier modify() {
        return new TableConstructorExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class TableConstructorExpressionNodeModifier {
        private final TableConstructorExpressionNode oldNode;
        private Token tableKeyword;
        private KeySpecifierNode keySpecifier;
        private Token openBracket;
        private SeparatedNodeList<Node> rows;
        private Token closeBracket;

        public TableConstructorExpressionNodeModifier(TableConstructorExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.tableKeyword = oldNode.tableKeyword();
            this.keySpecifier = oldNode.keySpecifier().orElse(null);
            this.openBracket = oldNode.openBracket();
            this.rows = oldNode.rows();
            this.closeBracket = oldNode.closeBracket();
        }

        public TableConstructorExpressionNodeModifier withTableKeyword(
                Token tableKeyword) {
            Objects.requireNonNull(tableKeyword, "tableKeyword must not be null");
            this.tableKeyword = tableKeyword;
            return this;
        }

        public TableConstructorExpressionNodeModifier withKeySpecifier(
                KeySpecifierNode keySpecifier) {
            Objects.requireNonNull(keySpecifier, "keySpecifier must not be null");
            this.keySpecifier = keySpecifier;
            return this;
        }

        public TableConstructorExpressionNodeModifier withOpenBracket(
                Token openBracket) {
            Objects.requireNonNull(openBracket, "openBracket must not be null");
            this.openBracket = openBracket;
            return this;
        }

        public TableConstructorExpressionNodeModifier withRows(
                SeparatedNodeList<Node> rows) {
            Objects.requireNonNull(rows, "rows must not be null");
            this.rows = rows;
            return this;
        }

        public TableConstructorExpressionNodeModifier withCloseBracket(
                Token closeBracket) {
            Objects.requireNonNull(closeBracket, "closeBracket must not be null");
            this.closeBracket = closeBracket;
            return this;
        }

        public TableConstructorExpressionNode apply() {
            return oldNode.modify(
                    tableKeyword,
                    keySpecifier,
                    openBracket,
                    rows,
                    closeBracket);
        }
    }
}
