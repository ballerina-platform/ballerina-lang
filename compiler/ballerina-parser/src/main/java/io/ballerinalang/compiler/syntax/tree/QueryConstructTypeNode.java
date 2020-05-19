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
public class QueryConstructTypeNode extends NonTerminalNode {

    public QueryConstructTypeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token tableKeyword() {
        return childInBucket(0);
    }

    public KeySpecifierNode KeySpecifier() {
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
                "tableKeyword",
                "KeySpecifier"};
    }

    public QueryConstructTypeNode modify(
            Token tableKeyword,
            KeySpecifierNode KeySpecifier) {
        if (checkForReferenceEquality(
                tableKeyword,
                KeySpecifier)) {
            return this;
        }

        return NodeFactory.createQueryConstructTypeNode(
                tableKeyword,
                KeySpecifier);
    }

    public QueryConstructTypeNodeModifier modify() {
        return new QueryConstructTypeNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class QueryConstructTypeNodeModifier {
        private final QueryConstructTypeNode oldNode;
        private Token tableKeyword;
        private KeySpecifierNode KeySpecifier;

        public QueryConstructTypeNodeModifier(QueryConstructTypeNode oldNode) {
            this.oldNode = oldNode;
            this.tableKeyword = oldNode.tableKeyword();
            this.KeySpecifier = oldNode.KeySpecifier();
        }

        public QueryConstructTypeNodeModifier withTableKeyword(Token tableKeyword) {
            Objects.requireNonNull(tableKeyword, "tableKeyword must not be null");
            this.tableKeyword = tableKeyword;
            return this;
        }

        public QueryConstructTypeNodeModifier withKeySpecifier(KeySpecifierNode KeySpecifier) {
            Objects.requireNonNull(KeySpecifier, "KeySpecifier must not be null");
            this.KeySpecifier = KeySpecifier;
            return this;
        }

        public QueryConstructTypeNode apply() {
            return oldNode.modify(
                    tableKeyword,
                    KeySpecifier);
        }
    }
}
