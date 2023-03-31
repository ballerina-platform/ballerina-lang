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
package org.ballerinalang.langserver.commons.codeaction.spi;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;

import java.util.Optional;

/**
 * This class holds position details for the range based code actions.
 *
 * @since 2.0.0
 */
public interface RangeBasedPositionDetails {
    /**
     * Returns matched top-level node for the current position.
     *
     * @return {@link NonTerminalNode}
     */
    @Deprecated(forRemoval = true)
    NonTerminalNode matchedTopLevelNode();

    /**
     * Returns matched scoped statement node for the current position.
     *
     * @return {@link Symbol}
     */
    NonTerminalNode matchedStatementNode();

    /**
     * Get the syntax tree node for which we should suggest code actions for under the current context.
     *
     * @return Node for which we are going to provide code actions
     */
    NonTerminalNode matchedCodeActionNode();

    /**
     * Returns matched type of scoped node for the current position.
     *
     * @return {@link NonTerminalNode}
     */
    TypeSymbol matchedTopLevelTypeSymbol();

    /**
     * Get the matched documentable node at the cursor.
     *
     * @return {@link Optional}
     */
    Optional<NonTerminalNode> matchedDocumentableNode();

    /**
     * Get the documentable node which is enclosing the cursor.
     * This method is not the same as {@link #matchedDocumentableNode()} because in some cases, the cursor can be
     * within a function body/ record type definition body rather than within the signature of the node.
     *
     * @return {@link Optional}
     */
    Optional<NonTerminalNode> enclosingDocumentableNode();
}
