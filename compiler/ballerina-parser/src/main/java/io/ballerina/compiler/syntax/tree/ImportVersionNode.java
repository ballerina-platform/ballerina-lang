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
public class ImportVersionNode extends NonTerminalNode {

    public ImportVersionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token versionKeyword() {
        return childInBucket(0);
    }

    public SeparatedNodeList<Token> versionNumber() {
        return new SeparatedNodeList<>(childInBucket(1));
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
                "versionKeyword",
                "versionNumber"};
    }

    public ImportVersionNode modify(
            Token versionKeyword,
            SeparatedNodeList<Token> versionNumber) {
        if (checkForReferenceEquality(
                versionKeyword,
                versionNumber.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createImportVersionNode(
                versionKeyword,
                versionNumber);
    }

    public ImportVersionNodeModifier modify() {
        return new ImportVersionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImportVersionNodeModifier {
        private final ImportVersionNode oldNode;
        private Token versionKeyword;
        private SeparatedNodeList<Token> versionNumber;

        public ImportVersionNodeModifier(ImportVersionNode oldNode) {
            this.oldNode = oldNode;
            this.versionKeyword = oldNode.versionKeyword();
            this.versionNumber = oldNode.versionNumber();
        }

        public ImportVersionNodeModifier withVersionKeyword(
                Token versionKeyword) {
            Objects.requireNonNull(versionKeyword, "versionKeyword must not be null");
            this.versionKeyword = versionKeyword;
            return this;
        }

        public ImportVersionNodeModifier withVersionNumber(
                SeparatedNodeList<Token> versionNumber) {
            Objects.requireNonNull(versionNumber, "versionNumber must not be null");
            this.versionNumber = versionNumber;
            return this;
        }

        public ImportVersionNode apply() {
            return oldNode.modify(
                    versionKeyword,
                    versionNumber);
        }
    }
}
