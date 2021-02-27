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
public class ReceiveFieldsNode extends NonTerminalNode {

    public ReceiveFieldsNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBrace() {
        return childInBucket(0);
    }

    public SeparatedNodeList<NameReferenceNode> receiveFields() {
        return new SeparatedNodeList<>(childInBucket(1));
    }

    public Token closeBrace() {
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
                "openBrace",
                "receiveFields",
                "closeBrace"};
    }

    public ReceiveFieldsNode modify(
            Token openBrace,
            SeparatedNodeList<NameReferenceNode> receiveFields,
            Token closeBrace) {
        if (checkForReferenceEquality(
                openBrace,
                receiveFields.underlyingListNode(),
                closeBrace)) {
            return this;
        }

        return NodeFactory.createReceiveFieldsNode(
                openBrace,
                receiveFields,
                closeBrace);
    }

    public ReceiveFieldsNodeModifier modify() {
        return new ReceiveFieldsNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ReceiveFieldsNodeModifier {
        private final ReceiveFieldsNode oldNode;
        private Token openBrace;
        private SeparatedNodeList<NameReferenceNode> receiveFields;
        private Token closeBrace;

        public ReceiveFieldsNodeModifier(ReceiveFieldsNode oldNode) {
            this.oldNode = oldNode;
            this.openBrace = oldNode.openBrace();
            this.receiveFields = oldNode.receiveFields();
            this.closeBrace = oldNode.closeBrace();
        }

        public ReceiveFieldsNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public ReceiveFieldsNodeModifier withReceiveFields(
                SeparatedNodeList<NameReferenceNode> receiveFields) {
            Objects.requireNonNull(receiveFields, "receiveFields must not be null");
            this.receiveFields = receiveFields;
            return this;
        }

        public ReceiveFieldsNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public ReceiveFieldsNode apply() {
            return oldNode.modify(
                    openBrace,
                    receiveFields,
                    closeBrace);
        }
    }
}
