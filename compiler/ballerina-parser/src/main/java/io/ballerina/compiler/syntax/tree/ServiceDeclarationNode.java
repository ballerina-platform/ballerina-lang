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

    public NodeList<Token> qualifiers() {
        return new NodeList<>(childInBucket(1));
    }

    public Token serviceKeyword() {
        return childInBucket(2);
    }

    public Optional<IdentifierToken> serviceName() {
        return optionalChildInBucket(3);
    }

    public Token onKeyword() {
        return childInBucket(4);
    }

    public SeparatedNodeList<ExpressionNode> expressions() {
        return new SeparatedNodeList<>(childInBucket(5));
    }

    public Node serviceBody() {
        return childInBucket(6);
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
                "qualifiers",
                "serviceKeyword",
                "serviceName",
                "onKeyword",
                "expressions",
                "serviceBody"};
    }

    public ServiceDeclarationNode modify(
            MetadataNode metadata,
            NodeList<Token> qualifiers,
            Token serviceKeyword,
            IdentifierToken serviceName,
            Token onKeyword,
            SeparatedNodeList<ExpressionNode> expressions,
            Node serviceBody) {
        if (checkForReferenceEquality(
                metadata,
                qualifiers.underlyingListNode(),
                serviceKeyword,
                serviceName,
                onKeyword,
                expressions.underlyingListNode(),
                serviceBody)) {
            return this;
        }

        return NodeFactory.createServiceDeclarationNode(
                metadata,
                qualifiers,
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
        private NodeList<Token> qualifiers;
        private Token serviceKeyword;
        private IdentifierToken serviceName;
        private Token onKeyword;
        private SeparatedNodeList<ExpressionNode> expressions;
        private Node serviceBody;

        public ServiceDeclarationNodeModifier(ServiceDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.qualifiers = oldNode.qualifiers();
            this.serviceKeyword = oldNode.serviceKeyword();
            this.serviceName = oldNode.serviceName().orElse(null);
            this.onKeyword = oldNode.onKeyword();
            this.expressions = oldNode.expressions();
            this.serviceBody = oldNode.serviceBody();
        }

        public ServiceDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            this.metadata = metadata;
            return this;
        }

        public ServiceDeclarationNodeModifier withQualifiers(
                NodeList<Token> qualifiers) {
            Objects.requireNonNull(qualifiers, "qualifiers must not be null");
            this.qualifiers = qualifiers;
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
                    qualifiers,
                    serviceKeyword,
                    serviceName,
                    onKeyword,
                    expressions,
                    serviceBody);
        }
    }
}
