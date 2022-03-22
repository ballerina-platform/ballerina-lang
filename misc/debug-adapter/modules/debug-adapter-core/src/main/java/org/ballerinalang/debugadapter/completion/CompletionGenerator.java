/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugadapter.completion;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import org.ballerinalang.debugadapter.completion.context.AsyncSendActionNodeContext;
import org.ballerinalang.debugadapter.completion.context.CompletionContext;
import org.ballerinalang.debugadapter.completion.context.RemoteMethodCallActionNodeContext;
import org.ballerinalang.debugadapter.completion.resolver.FieldAccessCompletionResolver;
import org.eclipse.lsp4j.debug.CompletionItem;

import java.util.List;

import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.getCompletions;

/**
 * Completion generator to get the completion items based on the resolver node.
 *
 * @since 2201.1.0
 */
public class CompletionGenerator {

    private CompletionGenerator() {
    }
    
    public static CompletionItem[] getFieldAccessCompletions(CompletionContext ctx, Node resolverNode) {
        FieldAccessCompletionResolver fieldAccessCompletionResolver =
                new FieldAccessCompletionResolver(ctx);
        List<Symbol> visibleEntries = fieldAccessCompletionResolver
                .getVisibleEntries(((FieldAccessExpressionNode) resolverNode).expression());
        return getCompletions(visibleEntries);
    }

    public static CompletionItem[] getRemoteMethodCallActionCompletions(CompletionContext ctx, Node resolverNode) {
        RemoteMethodCallActionNodeContext remoteMethodCallActionNodeContext =
                new RemoteMethodCallActionNodeContext();
        List<CompletionItem> completions = remoteMethodCallActionNodeContext
                .getCompletions(ctx, ((RemoteMethodCallActionNode) resolverNode));
        return completions.toArray(new CompletionItem[0]);
    }

    public static CompletionItem[] getAsyncSendActionCompletions(CompletionContext ctx, Node resolverNode) {
        AsyncSendActionNodeContext asyncSendActionNodeContext = new AsyncSendActionNodeContext();
        List<CompletionItem> completions = asyncSendActionNodeContext
                .getCompletions(ctx, ((AsyncSendActionNode) resolverNode));
        return completions.toArray(new CompletionItem[0]);
    }
}
