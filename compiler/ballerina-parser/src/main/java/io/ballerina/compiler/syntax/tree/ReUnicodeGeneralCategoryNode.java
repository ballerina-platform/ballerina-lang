/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * @since 2201.3.0
 */
public class ReUnicodeGeneralCategoryNode extends ReUnicodePropertyNode {

    public ReUnicodeGeneralCategoryNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<Node> categoryStart() {
        return optionalChildInBucket(0);
    }

    public Node reUnicodeGeneralCategoryName() {
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
                "categoryStart",
                "reUnicodeGeneralCategoryName"};
    }

    public ReUnicodeGeneralCategoryNode modify(
            Node categoryStart,
            Node reUnicodeGeneralCategoryName) {
        if (checkForReferenceEquality(
                categoryStart,
                reUnicodeGeneralCategoryName)) {
            return this;
        }

        return NodeFactory.createReUnicodeGeneralCategoryNode(
                categoryStart,
                reUnicodeGeneralCategoryName);
    }

    public ReUnicodeGeneralCategoryNodeModifier modify() {
        return new ReUnicodeGeneralCategoryNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReUnicodeGeneralCategoryNodeModifier {
        private final ReUnicodeGeneralCategoryNode oldNode;
        private Node categoryStart;
        private Node reUnicodeGeneralCategoryName;

        public ReUnicodeGeneralCategoryNodeModifier(ReUnicodeGeneralCategoryNode oldNode) {
            this.oldNode = oldNode;
            this.categoryStart = oldNode.categoryStart().orElse(null);
            this.reUnicodeGeneralCategoryName = oldNode.reUnicodeGeneralCategoryName();
        }

        public ReUnicodeGeneralCategoryNodeModifier withCategoryStart(
                Node categoryStart) {
            this.categoryStart = categoryStart;
            return this;
        }

        public ReUnicodeGeneralCategoryNodeModifier withReUnicodeGeneralCategoryName(
                Node reUnicodeGeneralCategoryName) {
            Objects.requireNonNull(reUnicodeGeneralCategoryName, "reUnicodeGeneralCategoryName must not be null");
            this.reUnicodeGeneralCategoryName = reUnicodeGeneralCategoryName;
            return this;
        }

        public ReUnicodeGeneralCategoryNode apply() {
            return oldNode.modify(
                    categoryStart,
                    reUnicodeGeneralCategoryName);
        }
    }
}
