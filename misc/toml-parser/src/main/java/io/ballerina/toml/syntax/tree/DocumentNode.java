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
package io.ballerina.toml.syntax.tree;

import io.ballerina.toml.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class DocumentNode extends NonTerminalNode {

    public DocumentNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<DocumentMemberDeclarationNode> members() {
        return new NodeList<>(childInBucket(0));
    }

    public Token eofToken() {
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
                "members",
                "eofToken"};
    }

    public DocumentNode modify(
            NodeList<DocumentMemberDeclarationNode> members,
            Token eofToken) {
        if (checkForReferenceEquality(
                members.underlyingListNode(),
                eofToken)) {
            return this;
        }

        return NodeFactory.createDocumentNode(
                members,
                eofToken);
    }

    public DocumentNodeModifier modify() {
        return new DocumentNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class DocumentNodeModifier {
        private final DocumentNode oldNode;
        private NodeList<DocumentMemberDeclarationNode> members;
        private Token eofToken;

        public DocumentNodeModifier(DocumentNode oldNode) {
            this.oldNode = oldNode;
            this.members = oldNode.members();
            this.eofToken = oldNode.eofToken();
        }

        public DocumentNodeModifier withMembers(
                NodeList<DocumentMemberDeclarationNode> members) {
            Objects.requireNonNull(members, "members must not be null");
            this.members = members;
            return this;
        }

        public DocumentNodeModifier withEofToken(
                Token eofToken) {
            Objects.requireNonNull(eofToken, "eofToken must not be null");
            this.eofToken = eofToken;
            return this;
        }

        public DocumentNode apply() {
            return oldNode.modify(
                    members,
                    eofToken);
        }
    }
}
