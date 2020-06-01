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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ImportSubVersionNode extends NonTerminalNode {

    public ImportSubVersionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Token> leadingDot() {
        return optionalChildInBucket(0);
    }

    public Token versionNumber() {
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
                "leadingDot",
                "versionNumber"};
    }

    public ImportSubVersionNode modify(
            Token leadingDot,
            Token versionNumber) {
        if (checkForReferenceEquality(
                leadingDot,
                versionNumber)) {
            return this;
        }

        return NodeFactory.createImportSubVersionNode(
                leadingDot,
                versionNumber);
    }

    public ImportSubVersionNodeModifier modify() {
        return new ImportSubVersionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ImportSubVersionNodeModifier {
        private final ImportSubVersionNode oldNode;
        private Token leadingDot;
        private Token versionNumber;

        public ImportSubVersionNodeModifier(ImportSubVersionNode oldNode) {
            this.oldNode = oldNode;
            this.leadingDot = oldNode.leadingDot().orElse(null);
            this.versionNumber = oldNode.versionNumber();
        }

        public ImportSubVersionNodeModifier withLeadingDot(
                Token leadingDot) {
            Objects.requireNonNull(leadingDot, "leadingDot must not be null");
            this.leadingDot = leadingDot;
            return this;
        }

        public ImportSubVersionNodeModifier withVersionNumber(
                Token versionNumber) {
            Objects.requireNonNull(versionNumber, "versionNumber must not be null");
            this.versionNumber = versionNumber;
            return this;
        }

        public ImportSubVersionNode apply() {
            return oldNode.modify(
                    leadingDot,
                    versionNumber);
        }
    }
}
