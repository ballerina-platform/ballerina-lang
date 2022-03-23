/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.debugadapter.completion.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AsyncSendActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import org.ballerinalang.debugadapter.completion.resolver.ContextTypeResolver;
import org.ballerinalang.debugadapter.completion.util.SymbolUtil;
import org.eclipse.lsp4j.debug.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.completion.util.CommonUtil.getClientActions;
import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.getCompletionItemList;

/**
 * Handles the completions for the {@link AsyncSendActionNode}.
 *
 * @since 2201.1.0
 */
public class AsyncSendActionNodeContext {

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context Debug completion context
     * @param node    Node instance for the parser context
     * @return {@link List}     List of calculated Completion Items
     */
    public List<CompletionItem> getCompletions(CompletionContext context, AsyncSendActionNode node) {
        return getFilteredItems(context, node.expression());
    }

    private List<CompletionItem> getFilteredItems(CompletionContext context, ExpressionNode expressionNode) {
        List<CompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getSuspendedContext().getLineNumber() - 1, 0);
        ContextTypeResolver resolver = new ContextTypeResolver(context);
        Optional<TypeSymbol> expressionType = expressionNode.apply(resolver);

        if (expressionType.isPresent() && SymbolUtil.isClient(expressionType.get())) {
            /*
            Covers the following case where a is a client object and we suggest the remote actions
            (1) a -> g<cursor>
             */
            List<Symbol> clientActions = getClientActions(expressionType.get());
            completionItems.addAll(getCompletionItemList(clientActions, context));
        } else {
            /*
            Covers the following case where a is any other variable and we suggest the workers
            (1) a -> <cursor>
            (2) a -> w<cursor>
             */
            List<Symbol> filteredWorkers = visibleSymbols.stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.WORKER)
                    .collect(Collectors.toList());
            completionItems.addAll(getCompletionItemList(filteredWorkers, context));
        }

        return completionItems;
    }
}
