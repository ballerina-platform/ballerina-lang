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
public class NamedWorkerDeclarationNode extends NonTerminalNode {

    public NamedWorkerDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<AnnotationNode> annotations() {
        return new NodeList<>(childInBucket(0));
    }

    public Optional<Token> transactionalKeyword() {
        return optionalChildInBucket(1);
    }

    public Token workerKeyword() {
        return childInBucket(2);
    }

    public IdentifierToken workerName() {
        return childInBucket(3);
    }

    public Optional<Node> returnTypeDesc() {
        return optionalChildInBucket(4);
    }

    public BlockStatementNode workerBody() {
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
                "annotations",
                "transactionalKeyword",
                "workerKeyword",
                "workerName",
                "returnTypeDesc",
                "workerBody"};
    }

    public NamedWorkerDeclarationNode modify(
            NodeList<AnnotationNode> annotations,
            Token transactionalKeyword,
            Token workerKeyword,
            IdentifierToken workerName,
            Node returnTypeDesc,
            BlockStatementNode workerBody) {
        if (checkForReferenceEquality(
                annotations.underlyingListNode(),
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody)) {
            return this;
        }

        return NodeFactory.createNamedWorkerDeclarationNode(
                annotations,
                transactionalKeyword,
                workerKeyword,
                workerName,
                returnTypeDesc,
                workerBody);
    }

    public NamedWorkerDeclarationNodeModifier modify() {
        return new NamedWorkerDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class NamedWorkerDeclarationNodeModifier {
        private final NamedWorkerDeclarationNode oldNode;
        private NodeList<AnnotationNode> annotations;
        private Token transactionalKeyword;
        private Token workerKeyword;
        private IdentifierToken workerName;
        private Node returnTypeDesc;
        private BlockStatementNode workerBody;

        public NamedWorkerDeclarationNodeModifier(NamedWorkerDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.annotations = oldNode.annotations();
            this.transactionalKeyword = oldNode.transactionalKeyword().orElse(null);
            this.workerKeyword = oldNode.workerKeyword();
            this.workerName = oldNode.workerName();
            this.returnTypeDesc = oldNode.returnTypeDesc().orElse(null);
            this.workerBody = oldNode.workerBody();
        }

        public NamedWorkerDeclarationNodeModifier withAnnotations(
                NodeList<AnnotationNode> annotations) {
            Objects.requireNonNull(annotations, "annotations must not be null");
            this.annotations = annotations;
            return this;
        }

        public NamedWorkerDeclarationNodeModifier withTransactionalKeyword(
                Token transactionalKeyword) {
            Objects.requireNonNull(transactionalKeyword, "transactionalKeyword must not be null");
            this.transactionalKeyword = transactionalKeyword;
            return this;
        }

        public NamedWorkerDeclarationNodeModifier withWorkerKeyword(
                Token workerKeyword) {
            Objects.requireNonNull(workerKeyword, "workerKeyword must not be null");
            this.workerKeyword = workerKeyword;
            return this;
        }

        public NamedWorkerDeclarationNodeModifier withWorkerName(
                IdentifierToken workerName) {
            Objects.requireNonNull(workerName, "workerName must not be null");
            this.workerName = workerName;
            return this;
        }

        public NamedWorkerDeclarationNodeModifier withReturnTypeDesc(
                Node returnTypeDesc) {
            Objects.requireNonNull(returnTypeDesc, "returnTypeDesc must not be null");
            this.returnTypeDesc = returnTypeDesc;
            return this;
        }

        public NamedWorkerDeclarationNodeModifier withWorkerBody(
                BlockStatementNode workerBody) {
            Objects.requireNonNull(workerBody, "workerBody must not be null");
            this.workerBody = workerBody;
            return this;
        }

        public NamedWorkerDeclarationNode apply() {
            return oldNode.modify(
                    annotations,
                    transactionalKeyword,
                    workerKeyword,
                    workerName,
                    returnTypeDesc,
                    workerBody);
        }
    }
}
