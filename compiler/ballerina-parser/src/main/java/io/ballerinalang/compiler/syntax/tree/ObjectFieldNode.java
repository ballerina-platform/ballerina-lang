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
public class ObjectFieldNode extends NonTerminalNode {

    public ObjectFieldNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public MetadataNode metadata() {
        return childInBucket(0);
    }

    public Optional<Token> visibilityQualifier() {
        return optionalChildInBucket(1);
    }

    public Optional<Token> readonlyKeyword() {
        return optionalChildInBucket(2);
    }

    public Node typeName() {
        return childInBucket(3);
    }

    public Token fieldName() {
        return childInBucket(4);
    }

    public Token equalsToken() {
        return childInBucket(5);
    }

    public ExpressionNode expression() {
        return childInBucket(6);
    }

    public Token semicolonToken() {
        return childInBucket(7);
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
                "visibilityQualifier",
                "readonlyKeyword",
                "typeName",
                "fieldName",
                "equalsToken",
                "expression",
                "semicolonToken"};
    }

    public ObjectFieldNode modify(
            MetadataNode metadata,
            Token visibilityQualifier,
            Token readonlyKeyword,
            Node typeName,
            Token fieldName,
            Token equalsToken,
            ExpressionNode expression,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                metadata,
                visibilityQualifier,
                readonlyKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createObjectFieldNode(
                metadata,
                visibilityQualifier,
                readonlyKeyword,
                typeName,
                fieldName,
                equalsToken,
                expression,
                semicolonToken);
    }

    public ObjectFieldNodeModifier modify() {
        return new ObjectFieldNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ObjectFieldNodeModifier {
        private final ObjectFieldNode oldNode;
        private MetadataNode metadata;
        private Token visibilityQualifier;
        private Token readonlyKeyword;
        private Node typeName;
        private Token fieldName;
        private Token equalsToken;
        private ExpressionNode expression;
        private Token semicolonToken;

        public ObjectFieldNodeModifier(ObjectFieldNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata();
            this.visibilityQualifier = oldNode.visibilityQualifier().orElse(null);
            this.readonlyKeyword = oldNode.readonlyKeyword().orElse(null);
            this.typeName = oldNode.typeName();
            this.fieldName = oldNode.fieldName();
            this.equalsToken = oldNode.equalsToken();
            this.expression = oldNode.expression();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public ObjectFieldNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public ObjectFieldNodeModifier withVisibilityQualifier(
                Token visibilityQualifier) {
            Objects.requireNonNull(visibilityQualifier, "visibilityQualifier must not be null");
            this.visibilityQualifier = visibilityQualifier;
            return this;
        }

        public ObjectFieldNodeModifier withReadonlyKeyword(
                Token readonlyKeyword) {
            Objects.requireNonNull(readonlyKeyword, "readonlyKeyword must not be null");
            this.readonlyKeyword = readonlyKeyword;
            return this;
        }

        public ObjectFieldNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public ObjectFieldNodeModifier withFieldName(
                Token fieldName) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            this.fieldName = fieldName;
            return this;
        }

        public ObjectFieldNodeModifier withEqualsToken(
                Token equalsToken) {
            Objects.requireNonNull(equalsToken, "equalsToken must not be null");
            this.equalsToken = equalsToken;
            return this;
        }

        public ObjectFieldNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public ObjectFieldNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public ObjectFieldNode apply() {
            return oldNode.modify(
                    metadata,
                    visibilityQualifier,
                    readonlyKeyword,
                    typeName,
                    fieldName,
                    equalsToken,
                    expression,
                    semicolonToken);
        }
    }
}
