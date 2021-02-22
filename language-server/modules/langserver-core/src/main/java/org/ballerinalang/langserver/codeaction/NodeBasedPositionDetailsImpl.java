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

/**
 * This class holds position details for the node-based code actions.
 *
 * @since 2.0.0
 */
public class NodeBasedPositionDetailsImpl implements NodeBasedPositionDetails {
    private final NonTerminalNode matchedTopLevelNode;
    private final NonTerminalNode matchedStmtNode;
    private final TypeSymbol matchedTopLevelTypeSymbol;

    private NodeBasedPositionDetailsImpl(NonTerminalNode matchedTopLevelNode,
                                         NonTerminalNode matchedStmtNode,
                                         TypeSymbol matchedTopLevelTypeSymbol) {
        this.matchedTopLevelNode = matchedTopLevelNode;
        this.matchedStmtNode = matchedStmtNode;
        this.matchedTopLevelTypeSymbol = matchedTopLevelTypeSymbol;
    }

    public static NodeBasedPositionDetailsImpl from(NonTerminalNode matchedTopLevelNode,
                                                    NonTerminalNode matchedStmtNode,
                                                    TypeSymbol matchedTopLevelTypeSymbol) {
        return new NodeBasedPositionDetailsImpl(matchedTopLevelNode, matchedStmtNode, matchedTopLevelTypeSymbol);
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

    /**
     * {@inheritDoc}
     */
    public TypeSymbol matchedTopLevelTypeSymbol() {
        return matchedTopLevelTypeSymbol;
    }
}
