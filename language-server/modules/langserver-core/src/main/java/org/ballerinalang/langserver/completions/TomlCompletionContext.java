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
package org.ballerinalang.langserver.completions;

import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import org.ballerinalang.langserver.commons.CompletionContext;

import java.util.List;

/**
 * Represents the Completion operation context for toml.
 *
 * @since 2.0.0
 */
@Deprecated
public interface TomlCompletionContext extends CompletionContext {

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
}
