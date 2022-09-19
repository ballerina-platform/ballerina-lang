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

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.3.0
 */
public class ReUnicodePropertyEscapeNode extends NonTerminalNode {

    public ReUnicodePropertyEscapeNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token slashToken() {
        return childInBucket(0);
    }

    public Node property() {
        return childInBucket(1);
    }

    public Token openBraceToken() {
        return childInBucket(2);
    }

    public ReUnicodePropertyNode reUnicodeProperty() {
        return childInBucket(3);
    }

    public Token closeBraceToken() {
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
                "slashToken",
                "property",
                "openBraceToken",
                "reUnicodeProperty",
                "closeBraceToken"};
    }

    public ReUnicodePropertyEscapeNode modify(
            Token slashToken,
            Node property,
            Token openBraceToken,
            ReUnicodePropertyNode reUnicodeProperty,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createReUnicodePropertyEscapeNode(
                slashToken,
                property,
                openBraceToken,
                reUnicodeProperty,
                closeBraceToken);
    }

    public ReUnicodePropertyEscapeNodeModifier modify() {
        return new ReUnicodePropertyEscapeNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReUnicodePropertyEscapeNodeModifier {
        private final ReUnicodePropertyEscapeNode oldNode;
        private Token slashToken;
        private Node property;
        private Token openBraceToken;
        private ReUnicodePropertyNode reUnicodeProperty;
        private Token closeBraceToken;

        public ReUnicodePropertyEscapeNodeModifier(ReUnicodePropertyEscapeNode oldNode) {
            this.oldNode = oldNode;
            this.slashToken = oldNode.slashToken();
            this.property = oldNode.property();
            this.openBraceToken = oldNode.openBraceToken();
            this.reUnicodeProperty = oldNode.reUnicodeProperty();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public ReUnicodePropertyEscapeNodeModifier withSlashToken(
                Token slashToken) {
            Objects.requireNonNull(slashToken, "slashToken must not be null");
            this.slashToken = slashToken;
            return this;
        }

        public ReUnicodePropertyEscapeNodeModifier withProperty(
                Node property) {
            Objects.requireNonNull(property, "property must not be null");
            this.property = property;
            return this;
        }

        public ReUnicodePropertyEscapeNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public ReUnicodePropertyEscapeNodeModifier withReUnicodeProperty(
                ReUnicodePropertyNode reUnicodeProperty) {
            Objects.requireNonNull(reUnicodeProperty, "reUnicodeProperty must not be null");
            this.reUnicodeProperty = reUnicodeProperty;
            return this;
        }

        public ReUnicodePropertyEscapeNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public ReUnicodePropertyEscapeNode apply() {
            return oldNode.modify(
                    slashToken,
                    property,
                    openBraceToken,
                    reUnicodeProperty,
                    closeBraceToken);
        }
    }
}
