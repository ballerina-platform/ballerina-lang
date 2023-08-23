/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
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
public class QueryConstructTypeNode extends NonTerminalNode {

    public QueryConstructTypeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token keyword() {
        return childInBucket(0);
    }

    public Optional<KeySpecifierNode> keySpecifier() {
        return optionalChildInBucket(1);
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
                "keyword",
                "keySpecifier"};
    }

    public QueryConstructTypeNode modify(
            Token keyword,
            KeySpecifierNode keySpecifier) {
        if (checkForReferenceEquality(
                keyword,
                keySpecifier)) {
            return this;
        }

        return NodeFactory.createQueryConstructTypeNode(
                keyword,
                keySpecifier);
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
        private Token keyword;
        private KeySpecifierNode keySpecifier;

        public QueryConstructTypeNodeModifier(QueryConstructTypeNode oldNode) {
            this.oldNode = oldNode;
            this.keyword = oldNode.keyword();
            this.keySpecifier = oldNode.keySpecifier().orElse(null);
        }

        public QueryConstructTypeNodeModifier withKeyword(
                Token keyword) {
            Objects.requireNonNull(keyword, "keyword must not be null");
            this.keyword = keyword;
            return this;
        }

        public QueryConstructTypeNodeModifier withKeySpecifier(
                KeySpecifierNode keySpecifier) {
            this.keySpecifier = keySpecifier;
            return this;
        }

        public QueryConstructTypeNode apply() {
            return oldNode.modify(
                    keyword,
                    keySpecifier);
        }
    }
}
