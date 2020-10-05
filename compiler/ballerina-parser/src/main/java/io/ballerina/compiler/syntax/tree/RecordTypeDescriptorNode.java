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
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class RecordTypeDescriptorNode extends TypeDescriptorNode {

    public RecordTypeDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token recordKeyword() {
        return childInBucket(0);
    }

    public Token bodyStartDelimiter() {
        return childInBucket(1);
    }

    public NodeList<Node> fields() {
        return new NodeList<>(childInBucket(2));
    }

    public Optional<RecordRestDescriptorNode> recordRestDescriptor() {
        return optionalChildInBucket(3);
    }

    public Token bodyEndDelimiter() {
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
                "recordKeyword",
                "bodyStartDelimiter",
                "fields",
                "recordRestDescriptor",
                "bodyEndDelimiter"};
    }

    public RecordTypeDescriptorNode modify(
            Token recordKeyword,
            Token bodyStartDelimiter,
            NodeList<Node> fields,
            RecordRestDescriptorNode recordRestDescriptor,
            Token bodyEndDelimiter) {
        if (checkForReferenceEquality(
                recordKeyword,
                bodyStartDelimiter,
                fields.underlyingListNode(),
                recordRestDescriptor,
                bodyEndDelimiter)) {
            return this;
        }

        return NodeFactory.createRecordTypeDescriptorNode(
                recordKeyword,
                bodyStartDelimiter,
                fields,
                recordRestDescriptor,
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
        private Token recordKeyword;
        private Token bodyStartDelimiter;
        private NodeList<Node> fields;
        private RecordRestDescriptorNode recordRestDescriptor;
        private Token bodyEndDelimiter;

        public RecordTypeDescriptorNodeModifier(RecordTypeDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.recordKeyword = oldNode.recordKeyword();
            this.bodyStartDelimiter = oldNode.bodyStartDelimiter();
            this.fields = oldNode.fields();
            this.recordRestDescriptor = oldNode.recordRestDescriptor().orElse(null);
            this.bodyEndDelimiter = oldNode.bodyEndDelimiter();
        }

        public RecordTypeDescriptorNodeModifier withRecordKeyword(
                Token recordKeyword) {
            Objects.requireNonNull(recordKeyword, "recordKeyword must not be null");
            this.recordKeyword = recordKeyword;
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

        public RecordTypeDescriptorNodeModifier withRecordRestDescriptor(
                RecordRestDescriptorNode recordRestDescriptor) {
            Objects.requireNonNull(recordRestDescriptor, "recordRestDescriptor must not be null");
            this.recordRestDescriptor = recordRestDescriptor;
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
                    recordKeyword,
                    bodyStartDelimiter,
                    fields,
                    recordRestDescriptor,
                    bodyEndDelimiter);
        }
    }
}
