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
public class ServiceRemoteAttachPointIdentifierNode extends NonTerminalNode {

    public ServiceRemoteAttachPointIdentifierNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token serviceKeyword() {
        return childInBucket(0);
    }

    public Token remoteKeyword() {
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
                "serviceKeyword",
                "remoteKeyword"};
    }

    public ServiceRemoteAttachPointIdentifierNode modify(
            Token serviceKeyword,
            Token remoteKeyword) {
        if (checkForReferenceEquality(
                serviceKeyword,
                remoteKeyword)) {
            return this;
        }

        return NodeFactory.createServiceRemoteAttachPointIdentifierNode(
                serviceKeyword,
                remoteKeyword);
    }

    public ServiceRemoteAttachPointIdentifierNodeModifier modify() {
        return new ServiceRemoteAttachPointIdentifierNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ServiceRemoteAttachPointIdentifierNodeModifier {
        private final ServiceRemoteAttachPointIdentifierNode oldNode;
        private Token serviceKeyword;
        private Token remoteKeyword;

        public ServiceRemoteAttachPointIdentifierNodeModifier(ServiceRemoteAttachPointIdentifierNode oldNode) {
            this.oldNode = oldNode;
            this.serviceKeyword = oldNode.serviceKeyword();
            this.remoteKeyword = oldNode.remoteKeyword();
        }

        public ServiceRemoteAttachPointIdentifierNodeModifier withServiceKeyword(
                Token serviceKeyword) {
            Objects.requireNonNull(serviceKeyword, "serviceKeyword must not be null");
            this.serviceKeyword = serviceKeyword;
            return this;
        }

        public ServiceRemoteAttachPointIdentifierNodeModifier withRemoteKeyword(
                Token remoteKeyword) {
            Objects.requireNonNull(remoteKeyword, "remoteKeyword must not be null");
            this.remoteKeyword = remoteKeyword;
            return this;
        }

        public ServiceRemoteAttachPointIdentifierNode apply() {
            return oldNode.modify(
                    serviceKeyword,
                    remoteKeyword);
        }
    }
}
