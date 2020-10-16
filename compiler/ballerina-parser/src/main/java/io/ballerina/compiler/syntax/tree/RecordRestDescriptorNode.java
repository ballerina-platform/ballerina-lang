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
public class RecordRestDescriptorNode extends NonTerminalNode {

    public RecordRestDescriptorNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Node typeName() {
        return childInBucket(0);
    }

    public Token ellipsisToken() {
        return childInBucket(1);
    }

    public Token semicolonToken() {
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
                "typeName",
                "ellipsisToken",
                "semicolonToken"};
    }

    public RecordRestDescriptorNode modify(
            Node typeName,
            Token ellipsisToken,
            Token semicolonToken) {
        if (checkForReferenceEquality(
                typeName,
                ellipsisToken,
                semicolonToken)) {
            return this;
        }

        return NodeFactory.createRecordRestDescriptorNode(
                typeName,
                ellipsisToken,
                semicolonToken);
    }

    public RecordRestDescriptorNodeModifier modify() {
        return new RecordRestDescriptorNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class RecordRestDescriptorNodeModifier {
        private final RecordRestDescriptorNode oldNode;
        private Node typeName;
        private Token ellipsisToken;
        private Token semicolonToken;

        public RecordRestDescriptorNodeModifier(RecordRestDescriptorNode oldNode) {
            this.oldNode = oldNode;
            this.typeName = oldNode.typeName();
            this.ellipsisToken = oldNode.ellipsisToken();
            this.semicolonToken = oldNode.semicolonToken();
        }

        public RecordRestDescriptorNodeModifier withTypeName(
                Node typeName) {
            Objects.requireNonNull(typeName, "typeName must not be null");
            this.typeName = typeName;
            return this;
        }

        public RecordRestDescriptorNodeModifier withEllipsisToken(
                Token ellipsisToken) {
            Objects.requireNonNull(ellipsisToken, "ellipsisToken must not be null");
            this.ellipsisToken = ellipsisToken;
            return this;
        }

        public RecordRestDescriptorNodeModifier withSemicolonToken(
                Token semicolonToken) {
            Objects.requireNonNull(semicolonToken, "semicolonToken must not be null");
            this.semicolonToken = semicolonToken;
            return this;
        }

        public RecordRestDescriptorNode apply() {
            return oldNode.modify(
                    typeName,
                    ellipsisToken,
                    semicolonToken);
        }
    }
}
