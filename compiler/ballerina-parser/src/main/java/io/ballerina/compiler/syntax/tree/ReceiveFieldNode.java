/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2201.9.0
 */
public class ReceiveFieldNode extends NonTerminalNode {

    public ReceiveFieldNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public SimpleNameReferenceNode fieldName() {
        return childInBucket(0);
    }

    public Token colon() {
        return childInBucket(1);
    }

    public SimpleNameReferenceNode peerWorker() {
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
                "fieldName",
                "colon",
                "peerWorker"};
    }

    public ReceiveFieldNode modify(
            SimpleNameReferenceNode fieldName,
            Token colon,
            SimpleNameReferenceNode peerWorker) {
        if (checkForReferenceEquality(
                fieldName,
                colon,
                peerWorker)) {
            return this;
        }

        return NodeFactory.createReceiveFieldNode(
                fieldName,
                colon,
                peerWorker);
    }

    public ReceiveFieldNodeModifier modify() {
        return new ReceiveFieldNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2201.9.0
     */
    public static class ReceiveFieldNodeModifier {
        private final ReceiveFieldNode oldNode;
        private SimpleNameReferenceNode fieldName;
        private Token colon;
        private SimpleNameReferenceNode peerWorker;

        public ReceiveFieldNodeModifier(ReceiveFieldNode oldNode) {
            this.oldNode = oldNode;
            this.fieldName = oldNode.fieldName();
            this.colon = oldNode.colon();
            this.peerWorker = oldNode.peerWorker();
        }

        public ReceiveFieldNodeModifier withFieldName(
                SimpleNameReferenceNode fieldName) {
            Objects.requireNonNull(fieldName, "fieldName must not be null");
            this.fieldName = fieldName;
            return this;
        }

        public ReceiveFieldNodeModifier withColon(
                Token colon) {
            Objects.requireNonNull(colon, "colon must not be null");
            this.colon = colon;
            return this;
        }

        public ReceiveFieldNodeModifier withPeerWorker(
                SimpleNameReferenceNode peerWorker) {
            Objects.requireNonNull(peerWorker, "peerWorker must not be null");
            this.peerWorker = peerWorker;
            return this;
        }

        public ReceiveFieldNode apply() {
            return oldNode.modify(
                    fieldName,
                    colon,
                    peerWorker);
        }
    }
}
