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

import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NonTerminalNode;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.completions.TomlCompletionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Language server Toml context implementation.
 *
 * @since 2.0.0
 */
public class K8sCompletionContextImpl extends CompletionContextImpl implements TomlCompletionContext {

    private final List<Node> resolverChain = new ArrayList<>();
    private NonTerminalNode nodeAtCursor;

    public K8sCompletionContextImpl(CompletionContext context, LanguageServerContext serverContext) {
        super(context.operation(),
                context.fileUri(),
                context.workspace(),
                context.getCapabilities(),
                context.getCursorPosition(),
                serverContext);
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
