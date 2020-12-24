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
package org.ballerinalang.langserver.contexts;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.CompletionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Language server context implementation.
 *
 * @since 2.0.0
 */
public class BallerinaCompletionContextImpl extends CompletionContextImpl implements BallerinaCompletionContext {

    private final List<Node> resolverChain = new ArrayList<>();
    private Token tokenAtCursor;
    private NonTerminalNode nodeAtCursor;

    public BallerinaCompletionContextImpl(CompletionContext context) {
        super(context.operation(),
                context.fileUri(),
                context.workspace(),
                context.getCapabilities(),
                context.getCursorPosition());
    }

    @Override
    public void setTokenAtCursor(Token token) {
        if (this.tokenAtCursor != null) {
            throw new RuntimeException("Setting the token more than once is not allowed");
        }
        this.tokenAtCursor = token;
    }

    @Override
    public Token getTokenAtCursor() {
        return this.tokenAtCursor;
    }

    @Override
    public void setNodeAtCursor(NonTerminalNode node) {
        if (this.nodeAtCursor != null) {
            throw new RuntimeException("Setting the node more than once is not allowed");
        }
        this.nodeAtCursor = node;
    }

    @Override
    public NonTerminalNode getNodeAtCursor() {
        return this.nodeAtCursor;
    }

    @Override
    public void addResolver(Node node) {
        this.resolverChain.add(node);
    }

    @Override
    public List<Node> getResolverChain() {
        return this.resolverChain;
    }
}
