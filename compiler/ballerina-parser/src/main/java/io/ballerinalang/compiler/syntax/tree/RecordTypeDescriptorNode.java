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
public class RecordTypeDescriptorNode extends TypeDescriptorNode {

    public RecordTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token objectKeyword() {
        return childInBucket(0);
    }

    public Token bodyStartDelimiter() {
        return childInBucket(1);
    }

    public NodeList<Node> fields() {
        return new NodeList<>(childInBucket(2));
    }

    public Token bodyEndDelimiter() {
        return childInBucket(3);
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
                "objectKeyword",
                "bodyStartDelimiter",
                "fields",
                "bodyEndDelimiter"};
    }

    public RecordTypeDescriptorNode modify(
            Token objectKeyword,
            Token bodyStartDelimiter,
            NodeList<Node> fields,
            Token bodyEndDelimiter) {
        if (checkForReferenceEquality(
                objectKeyword,
                bodyStartDelimiter,
                fields.underlyingListNode(),
                bodyEndDelimiter)) {
            return this;
        }

        return NodeFactory.createRecordTypeDescriptorNode(
                objectKeyword,
                bodyStartDelimiter,
                fields,
                bodyEndDelimiter);
    }

    public RecordTypeDescriptorNodeModifier modify() {
        return new RecordTypeDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RecordTypeDescriptorNodeModifier {
        private final RecordTypeDescriptorNode oldNode;
        private Token objectKeyword;
        private Token bodyStartDelimiter;
        private NodeList<Node> fields;
        private Token bodyEndDelimiter;

        public RecordTypeDescriptorNodeModifier(RecordTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.objectKeyword = oldNode.objectKeyword();
            this.bodyStartDelimiter = oldNode.bodyStartDelimiter();
            this.fields = oldNode.fields();
            this.bodyEndDelimiter = oldNode.bodyEndDelimiter();
        }

        public RecordTypeDescriptorNodeModifier withObjectKeyword(
                Token objectKeyword) {
            Objects.requireNonNull(objectKeyword, "objectKeyword must not be null");
            this.objectKeyword = objectKeyword;
            return this;
        }

        public RecordTypeDescriptorNodeModifier withBodyStartDelimiter(
                Token bodyStartDelimiter) {
            Objects.requireNonNull(bodyStartDelimiter, "bodyStartDelimiter must not be null");
            this.bodyStartDelimiter = bodyStartDelimiter;
            return this;
        }

        public RecordTypeDescriptorNodeModifier withFields(
                NodeList<Node> fields) {
            Objects.requireNonNull(fields, "fields must not be null");
            this.fields = fields;
            return this;
        }

        public RecordTypeDescriptorNodeModifier withBodyEndDelimiter(
                Token bodyEndDelimiter) {
            Objects.requireNonNull(bodyEndDelimiter, "bodyEndDelimiter must not be null");
            this.bodyEndDelimiter = bodyEndDelimiter;
            return this;
        }

        public RecordTypeDescriptorNode apply() {
            return oldNode.modify(
                    objectKeyword,
                    bodyStartDelimiter,
                    fields,
                    bodyEndDelimiter);
        }
    }
}
