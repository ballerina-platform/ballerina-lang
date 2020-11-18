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
public class EnumMemberNode extends NonTerminalNode {

    public EnumMemberNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public IdentifierToken identifier() {
        return childInBucket(1);
    }

    public Optional<Token> equalToken() {
        return optionalChildInBucket(2);
    }

    public Optional<ExpressionNode> constExprNode() {
        return optionalChildInBucket(3);
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
                "identifier",
                "equalToken",
                "constExprNode"};
    }

    public EnumMemberNode modify(
            MetadataNode metadata,
            IdentifierToken identifier,
            Token equalToken,
            ExpressionNode constExprNode) {
        if (checkForReferenceEquality(
                metadata,
                identifier,
                equalToken,
                constExprNode)) {
            return this;
        }

        return NodeFactory.createEnumMemberNode(
                metadata,
                identifier,
                equalToken,
                constExprNode);
    }

    public EnumMemberNodeModifier modify() {
        return new EnumMemberNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class EnumMemberNodeModifier {
        private final EnumMemberNode oldNode;
        private MetadataNode metadata;
        private IdentifierToken identifier;
        private Token equalToken;
        private ExpressionNode constExprNode;

        public EnumMemberNodeModifier(EnumMemberNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.identifier = oldNode.identifier();
            this.equalToken = oldNode.equalToken().orElse(null);
            this.constExprNode = oldNode.constExprNode().orElse(null);
        }

        public EnumMemberNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public EnumMemberNodeModifier withIdentifier(
                IdentifierToken identifier) {
            Objects.requireNonNull(identifier, "identifier must not be null");
            this.identifier = identifier;
            return this;
        }

        public EnumMemberNodeModifier withEqualToken(
                Token equalToken) {
            Objects.requireNonNull(equalToken, "equalToken must not be null");
            this.equalToken = equalToken;
            return this;
        }

        public EnumMemberNodeModifier withConstExprNode(
                ExpressionNode constExprNode) {
            Objects.requireNonNull(constExprNode, "constExprNode must not be null");
            this.constExprNode = constExprNode;
            return this;
        }

        public EnumMemberNode apply() {
            return oldNode.modify(
                    metadata,
                    identifier,
                    equalToken,
                    constExprNode);
        }
    }
}
