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

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ImportPrefixNode extends NonTerminalNode {

    public ImportPrefixNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token asKeyword() {
        return childInBucket(0);
    }

    public Token prefix() {
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
                "asKeyword",
                "prefix"};
    }

    public ImportPrefixNode modify(
            Token asKeyword,
            Token prefix) {
        if (checkForReferenceEquality(
                asKeyword,
                prefix)) {
            return this;
        }

        return NodeFactory.createImportPrefixNode(
                asKeyword,
                prefix);
    }

    public ImportPrefixNodeModifier modify() {
        return new ImportPrefixNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImportPrefixNodeModifier {
        private final ImportPrefixNode oldNode;
        private Token asKeyword;
        private Token prefix;

        public ImportPrefixNodeModifier(ImportPrefixNode oldNode) {
            this.oldNode = oldNode;
            this.asKeyword = oldNode.asKeyword();
            this.prefix = oldNode.prefix();
        }

        public ImportPrefixNodeModifier withAsKeyword(
                Token asKeyword) {
            Objects.requireNonNull(asKeyword, "asKeyword must not be null");
            this.asKeyword = asKeyword;
            return this;
        }

        public ImportPrefixNodeModifier withPrefix(
                Token prefix) {
            Objects.requireNonNull(prefix, "prefix must not be null");
            this.prefix = prefix;
            return this;
        }

        public ImportPrefixNode apply() {
            return oldNode.modify(
                    asKeyword,
                    prefix);
        }
    }
}
