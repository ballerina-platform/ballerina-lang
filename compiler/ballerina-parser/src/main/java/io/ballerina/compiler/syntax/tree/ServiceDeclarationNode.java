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
public class ServiceDeclarationNode extends ModuleMemberDeclarationNode {

    public ServiceDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Token serviceKeyword() {
        return childInBucket(1);
    }

    public Optional<IdentifierToken> serviceName() {
        return optionalChildInBucket(2);
    }

    public Token onKeyword() {
        return childInBucket(3);
    }

    public SeparatedNodeList<ExpressionNode> expressions() {
        return new SeparatedNodeList<>(childInBucket(4));
    }

    public Node serviceBody() {
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
                "serviceKeyword",
                "serviceName",
                "onKeyword",
                "expressions",
                "serviceBody"};
    }

    public ServiceDeclarationNode modify(
            MetadataNode metadata,
            Token serviceKeyword,
            IdentifierToken serviceName,
            Token onKeyword,
            SeparatedNodeList<ExpressionNode> expressions,
            Node serviceBody) {
        if (checkForReferenceEquality(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions.underlyingListNode(),
                serviceBody)) {
            return this;
        }

        return NodeFactory.createServiceDeclarationNode(
                metadata,
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions,
                serviceBody);
    }

    public ServiceDeclarationNodeModifier modify() {
        return new ServiceDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ServiceDeclarationNodeModifier {
        private final ServiceDeclarationNode oldNode;
        private MetadataNode metadata;
        private Token serviceKeyword;
        private IdentifierToken serviceName;
        private Token onKeyword;
        private SeparatedNodeList<ExpressionNode> expressions;
        private Node serviceBody;

        public ServiceDeclarationNodeModifier(ServiceDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.serviceKeyword = oldNode.serviceKeyword();
            this.serviceName = oldNode.serviceName().orElse(null);
            this.onKeyword = oldNode.onKeyword();
            this.expressions = oldNode.expressions();
            this.serviceBody = oldNode.serviceBody();
        }

        public ServiceDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            Objects.requireNonNull(metadata, "metadata must not be null");
            this.metadata = metadata;
            return this;
        }

        public ServiceDeclarationNodeModifier withServiceKeyword(
                Token serviceKeyword) {
            Objects.requireNonNull(serviceKeyword, "serviceKeyword must not be null");
            this.serviceKeyword = serviceKeyword;
            return this;
        }

        public ServiceDeclarationNodeModifier withServiceName(
                IdentifierToken serviceName) {
            Objects.requireNonNull(serviceName, "serviceName must not be null");
            this.serviceName = serviceName;
            return this;
        }

        public ServiceDeclarationNodeModifier withOnKeyword(
                Token onKeyword) {
            Objects.requireNonNull(onKeyword, "onKeyword must not be null");
            this.onKeyword = onKeyword;
            return this;
        }

        public ServiceDeclarationNodeModifier withExpressions(
                SeparatedNodeList<ExpressionNode> expressions) {
            Objects.requireNonNull(expressions, "expressions must not be null");
            this.expressions = expressions;
            return this;
        }

        public ServiceDeclarationNodeModifier withServiceBody(
                Node serviceBody) {
            Objects.requireNonNull(serviceBody, "serviceBody must not be null");
            this.serviceBody = serviceBody;
            return this;
        }

        public ServiceDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    serviceKeyword,
                    serviceName,
                    onKeyword,
                    expressions,
                    serviceBody);
        }
    }
}
