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
public class NamedWorkerDeclarator extends NonTerminalNode {

    public NamedWorkerDeclarator(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NodeList<StatementNode> workerInitStatements() {
        return new NodeList<>(childInBucket(0));
    }

    public NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations() {
        return new NodeList<>(childInBucket(1));
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
                "workerInitStatements",
                "namedWorkerDeclarations"};
    }

    public NamedWorkerDeclarator modify(
            NodeList<StatementNode> workerInitStatements,
            NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations) {
        if (checkForReferenceEquality(
                workerInitStatements.underlyingListNode(),
                namedWorkerDeclarations.underlyingListNode())) {
            return this;
        }

        return NodeFactory.createNamedWorkerDeclarator(
                workerInitStatements,
                namedWorkerDeclarations);
    }

    public NamedWorkerDeclaratorModifier modify() {
        return new NamedWorkerDeclaratorModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class NamedWorkerDeclaratorModifier {
        private final NamedWorkerDeclarator oldNode;
        private NodeList<StatementNode> workerInitStatements;
        private NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations;

        public NamedWorkerDeclaratorModifier(NamedWorkerDeclarator oldNode) {
            this.oldNode = oldNode;
            this.workerInitStatements = oldNode.workerInitStatements();
            this.namedWorkerDeclarations = oldNode.namedWorkerDeclarations();
        }

        public NamedWorkerDeclaratorModifier withWorkerInitStatements(
                NodeList<StatementNode> workerInitStatements) {
            Objects.requireNonNull(workerInitStatements, "workerInitStatements must not be null");
            this.workerInitStatements = workerInitStatements;
            return this;
        }

        public NamedWorkerDeclaratorModifier withNamedWorkerDeclarations(
                NodeList<NamedWorkerDeclarationNode> namedWorkerDeclarations) {
            Objects.requireNonNull(namedWorkerDeclarations, "namedWorkerDeclarations must not be null");
            this.namedWorkerDeclarations = namedWorkerDeclarations;
            return this;
        }

        public NamedWorkerDeclarator apply() {
            return oldNode.modify(
                    workerInitStatements,
                    namedWorkerDeclarations);
        }
    }
}
