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
package org.ballerinalang.langserver.commons;

import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.eclipse.lsp4j.CompletionParams;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Completion operation context.
 *
 * @since 2.0.0
 */
public interface BallerinaCompletionContext extends CompletionContext, BallerinaEnclosedPositionContext {

    /**
     * Set the token at the completion's cursor position.
     *
     * @param token {@link Token} at the cursor
     */
    void setTokenAtCursor(Token token);

    /**
     * Get the token at the cursor.
     *
     * @return {@link Token}
     */
    Token getTokenAtCursor();

    /**
     * Set the node at cursor.
     *
     * @param node {@link NonTerminalNode} at the cursor position
     */
    void setNodeAtCursor(NonTerminalNode node);

    /**
     * Get the node at the completion request triggered cursor position.
     *
     * @return {@link NonTerminalNode} at the cursor position
     */
    NonTerminalNode getNodeAtCursor();

    /**
     * Add a resolver to the resolver chain.
     *
     * @param node {@link Node} to be added to the chain
     */
    void addResolver(Node node);

    /**
     * Get the resolver chain which is the list of node evaluated against the completion item resolving.
     *
     * @return {@link List} of nodes
     */
    List<Node> getResolverChain();

    /**
     * Get the ContextType for the node at cursor.
     *
     * @return {@link Optional<TypeSymbol>} Context TypeSymbol for node at cursor.
     */
    Optional<TypeSymbol> getContextType();

    /**
     * Get the Completion Parameters.
     * 
     * @return {@link CompletionParams}
     */
    CompletionParams getCompletionParams();
}
