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

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;

/**
 * Represents the hover operation context.
 *
 * @since 2.0.0
 */
public interface HoverContext extends PositionedOperationContext {

    /**
     * Get the client's signature capabilities.
     *
     * @param token token to be set
     */
    void setTokenAtCursor(Token token);

    /**
     * Get the token at cursor. In order to successfully query the token, it has to be explicitly set by invoking
     * {@link #setTokenAtCursor(Token)}. Otherwise a runtime exception will be thrown.
     *
     * @return {@link Token} at the cursor
     */
    Token getTokenAtCursor();

    /**
     * Set the node at cursor.
     *
     * @param node {@link NonTerminalNode} at the cursor position
     */
    void setNodeAtCursor(NonTerminalNode node);

    /**
     * Get the node at the hover request triggered cursor position.
     *
     * @return {@link NonTerminalNode} at the cursor position
     */
    NonTerminalNode getNodeAtCursor();
}
