/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;

import java.util.Optional;

/**
 * This class holds position details for the node-based code actions.
 *
 * @since 2.0.0
 */
public class NodeBasedPositionDetailsImpl implements NodeBasedPositionDetails {
    private final NonTerminalNode matchedTopLevelNode;
    private final NonTerminalNode matchedStmtNode;
    private final NonTerminalNode matchedCodeActionNode;
    private final TypeSymbol matchedTopLevelTypeSymbol;
    private final NonTerminalNode matchedDocumentableNode;
    private final NonTerminalNode enclosingDocumentableNode;

    private NodeBasedPositionDetailsImpl(NonTerminalNode matchedTopLevelNode,
                                         NonTerminalNode matchedStmtNode,
                                         NonTerminalNode matchedCodeActionNode,
                                         TypeSymbol matchedTopLevelTypeSymbol,
                                         NonTerminalNode matchedDocumentableNode,
                                         NonTerminalNode enclosingDocumentableNode) {
        this.matchedTopLevelNode = matchedTopLevelNode;
        this.matchedStmtNode = matchedStmtNode;
        this.matchedCodeActionNode = matchedCodeActionNode;
        this.matchedTopLevelTypeSymbol = matchedTopLevelTypeSymbol;
        this.matchedDocumentableNode = matchedDocumentableNode;
        this.enclosingDocumentableNode = enclosingDocumentableNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NonTerminalNode matchedTopLevelNode() {
        return matchedTopLevelNode;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public NonTerminalNode matchedStatementNode() {
        return matchedStmtNode;
    }
    
    @Override
    public NonTerminalNode matchedCodeActionNode() {
        return matchedCodeActionNode;
    }

    /**
     * {@inheritDoc}
     */
    public TypeSymbol matchedTopLevelTypeSymbol() {
        return matchedTopLevelTypeSymbol;
    }

    @Override
    public Optional<NonTerminalNode> matchedDocumentableNode() {
        return Optional.ofNullable(matchedDocumentableNode);
    }

    @Override
    public Optional<NonTerminalNode> enclosingDocumentableNode() {
        return Optional.ofNullable(enclosingDocumentableNode);
    }

    /**
     * Position details builder to populate the required information.
     */
    public static class PositionDetailsBuilder {
        private NonTerminalNode documentableNode;
        private NonTerminalNode enclosingDocumentableNode;
        private NonTerminalNode topLevelNode;
        private NonTerminalNode statementNode;
        private NonTerminalNode codeActionNode;
        private TypeSymbol topLevelNodeType;

        private PositionDetailsBuilder() {
        }

        public PositionDetailsBuilder setDocumentableNode(NonTerminalNode documentableNode) {
            this.documentableNode = documentableNode;
            return this;
        }

        public PositionDetailsBuilder setEnclosingDocumentableNode(NonTerminalNode enclosingDocumentableNode) {
            this.enclosingDocumentableNode = enclosingDocumentableNode;
            return this;
        }

        public PositionDetailsBuilder setStatementNode(NonTerminalNode statementNode) {
            this.statementNode = statementNode;
            return this;
        }

        public PositionDetailsBuilder setTopLevelNode(NonTerminalNode topLevelNode) {
            this.topLevelNode = topLevelNode;
            return this;
        }

        public PositionDetailsBuilder setCodeActionNode(NonTerminalNode codeActionNode) {
            this.codeActionNode = codeActionNode;
            return this;
        }

        public PositionDetailsBuilder setTopLevelNodeType(TypeSymbol topLevelNodeType) {
            this.topLevelNodeType = topLevelNodeType;
            return this;
        }

        public NodeBasedPositionDetails build() {
            return new NodeBasedPositionDetailsImpl(
                    this.topLevelNode,
                    this.statementNode,
                    this.codeActionNode,
                    this.topLevelNodeType,
                    this.documentableNode,
                    this.enclosingDocumentableNode);
        }

        public static PositionDetailsBuilder newBuilder() {
            return new PositionDetailsBuilder();
        }
    }
}
