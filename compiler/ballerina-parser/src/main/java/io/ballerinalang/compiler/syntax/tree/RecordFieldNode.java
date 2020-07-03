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
public class RecordFieldNode extends NonTerminalNode {

    public RecordFieldNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public MetadataNode metadata() {
        return childInBucket(0);
    }

    public Optional<Token> readonlyKeyword() {
        return optionalChildInBucket(1);
    }

    public Node typeName() {
        return childInBucket(2);
    }

    public Token fieldName() {
        return childInBucket(3);
    }

    public Optional<Token> questionMarkToken() {
        return optionalChildInBucket(4);
    }

    public Token semicolonToken() {
        return childInBucket(5);
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
                "metadata",
                "readonlyKeyword",
                "typeName",
                "fieldName",
                "questionMarkToken",
                "semicolonToken"};
    }

    public RecordFieldNode modify(
            MetadataNode metadata,
            Token readonlyKeyword,
            Node typeName,
            Token fieldName,
            Token questionMarkToken,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createRecordFieldNode(
                metadata,
                readonlyKeyword,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    public RecordFieldNodeModifier modify() {
        return new RecordFieldNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RecordFieldNodeModifier {
        private final RecordFieldNode oldNode;
        private MetadataNode metadata;
        private Token readonlyKeyword;
        private Node typeName;
        private Token fieldName;
        private Token questionMarkToken;
        private Token semicolonToken;

        public RecordFieldNodeModifier(RecordFieldNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata();
            this.readonlyKeyword = oldNode.readonlyKeyword().orElse(null);
            this.typeName = oldNode.typeName();
            this.fieldName = oldNode.fieldName();
            this.questionMarkToken = oldNode.questionMarkToken().orElse(null);
            this.semicolonToken = oldNode.semicolonToken();
        }

        public RecordFieldNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public RecordFieldNodeModifier withReadonlyKeyword(
                Token readonlyKeyword) {
            Objects.requireNonNull(readonlyKeyword, "readonlyKeyword must not be null");
            this.readonlyKeyword = readonlyKeyword;
            return this;
        }

        public RecordFieldNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public RecordFieldNodeModifier withFieldName(
                Token fieldName) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            this.fieldName = fieldName;
            return this;
        }

        public RecordFieldNodeModifier withQuestionMarkToken(
                Token questionMarkToken) {
            Objects.requireNonNull(questionMarkToken, "questionMarkToken must not be null");
            this.questionMarkToken = questionMarkToken;
            return this;
        }

        public RecordFieldNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public RecordFieldNode apply() {
            return oldNode.modify(
                    metadata,
                    readonlyKeyword,
                    typeName,
                    fieldName,
                    questionMarkToken,
                    semicolonToken);
        }
    }
}
