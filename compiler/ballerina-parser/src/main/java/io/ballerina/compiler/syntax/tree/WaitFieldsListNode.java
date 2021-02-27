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
public class WaitFieldsListNode extends NonTerminalNode {

    public WaitFieldsListNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBrace() {
        return childInBucket(0);
    }

    public SeparatedNodeList<Node> waitFields() {
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
                "waitFields",
                "closeBrace"};
    }

    public WaitFieldsListNode modify(
            Token openBrace,
            SeparatedNodeList<Node> waitFields,
            Token closeBrace) {
        if (checkForReferenceEquality(
                openBrace,
                waitFields.underlyingListNode(),
                closeBrace)) {
            return this;
        }

        return NodeFactory.createWaitFieldsListNode(
                openBrace,
                waitFields,
                closeBrace);
    }

    public WaitFieldsListNodeModifier modify() {
        return new WaitFieldsListNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class WaitFieldsListNodeModifier {
        private final WaitFieldsListNode oldNode;
        private Token openBrace;
        private SeparatedNodeList<Node> waitFields;
        private Token closeBrace;

        public WaitFieldsListNodeModifier(WaitFieldsListNode oldNode) {
            this.oldNode = oldNode;
            this.openBrace = oldNode.openBrace();
            this.waitFields = oldNode.waitFields();
            this.closeBrace = oldNode.closeBrace();
        }

        public WaitFieldsListNodeModifier withOpenBrace(
                Token openBrace) {
            Objects.requireNonNull(openBrace, "openBrace must not be null");
            this.openBrace = openBrace;
            return this;
        }

        public WaitFieldsListNodeModifier withWaitFields(
                SeparatedNodeList<Node> waitFields) {
            Objects.requireNonNull(waitFields, "waitFields must not be null");
            this.waitFields = waitFields;
            return this;
        }

        public WaitFieldsListNodeModifier withCloseBrace(
                Token closeBrace) {
            Objects.requireNonNull(closeBrace, "closeBrace must not be null");
            this.closeBrace = closeBrace;
            return this;
        }

        public WaitFieldsListNode apply() {
            return oldNode.modify(
                    openBrace,
                    waitFields,
                    closeBrace);
        }
    }
}
