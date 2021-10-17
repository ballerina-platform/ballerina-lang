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
package org.ballerinalang.langserver.commons.toml;

import io.ballerina.toml.syntax.tree.NonTerminalNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;

import java.util.Optional;

/**
 * Represents the completion operation context for toml.
 *
 * @since 2.0.0
 */
public interface TomlCompletionContext extends CompletionContext {

    /**
     * Set the Node at cursor.
     *
     * @param node Node at cursor
     */
    void setNodeAtCursor(NonTerminalNode node);

    /**
     * Get the node at cursor.
     *
     * @return {@link NonTerminalNode} nodeAtCursor
     */
    Optional<NonTerminalNode> getNodeAtCursor();

    /**
     * Get the current toml syntax tree.
     *
     * @return {@link Optional<SyntaxTree>} toml syntax tree
     */
    Optional<SyntaxTree> getTomlSyntaxTree();

    /**
     * Get the language server context.
     *
     * @return {@link LanguageServerContext}
     */
    LanguageServerContext getLanguageServerContext();

}
