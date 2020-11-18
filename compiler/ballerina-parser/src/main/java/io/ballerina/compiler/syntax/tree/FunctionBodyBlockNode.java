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
public class FunctionBodyBlockNode extends FunctionBodyNode {

    public FunctionBodyBlockNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token openBraceToken() {
        return childInBucket(0);
    }

    public Optional<NamedWorkerDeclarator> namedWorkerDeclarator() {
        return optionalChildInBucket(1);
    }

    public NodeList<StatementNode> statements() {
        return new NodeList<>(childInBucket(2));
    }

    public Token closeBraceToken() {
        return childInBucket(3);
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
                "openBraceToken",
                "namedWorkerDeclarator",
                "statements",
                "closeBraceToken"};
    }

    public FunctionBodyBlockNode modify(
            Token openBraceToken,
            NamedWorkerDeclarator namedWorkerDeclarator,
            NodeList<StatementNode> statements,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                openBraceToken,
                namedWorkerDeclarator,
                statements.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createFunctionBodyBlockNode(
                openBraceToken,
                namedWorkerDeclarator,
                statements,
                closeBraceToken);
    }

    public FunctionBodyBlockNodeModifier modify() {
        return new FunctionBodyBlockNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FunctionBodyBlockNodeModifier {
        private final FunctionBodyBlockNode oldNode;
        private Token openBraceToken;
        private NamedWorkerDeclarator namedWorkerDeclarator;
        private NodeList<StatementNode> statements;
        private Token closeBraceToken;

        public FunctionBodyBlockNodeModifier(FunctionBodyBlockNode oldNode) {
            this.oldNode = oldNode;
            this.openBraceToken = oldNode.openBraceToken();
            this.namedWorkerDeclarator = oldNode.namedWorkerDeclarator().orElse(null);
            this.statements = oldNode.statements();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public FunctionBodyBlockNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public FunctionBodyBlockNodeModifier withNamedWorkerDeclarator(
                NamedWorkerDeclarator namedWorkerDeclarator) {
            Objects.requireNonNull(namedWorkerDeclarator, "namedWorkerDeclarator must not be null");
            this.namedWorkerDeclarator = namedWorkerDeclarator;
            return this;
        }

        public FunctionBodyBlockNodeModifier withStatements(
                NodeList<StatementNode> statements) {
            Objects.requireNonNull(statements, "statements must not be null");
            this.statements = statements;
            return this;
        }

        public FunctionBodyBlockNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public FunctionBodyBlockNode apply() {
            return oldNode.modify(
                    openBraceToken,
                    namedWorkerDeclarator,
                    statements,
                    closeBraceToken);
        }
    }
}
