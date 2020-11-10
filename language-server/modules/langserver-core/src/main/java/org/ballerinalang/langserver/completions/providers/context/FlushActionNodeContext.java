/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FlushActionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Provider for {@link FlushActionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class FlushActionNodeContext extends AbstractCompletionProvider<FlushActionNode> {
    public FlushActionNodeContext() {
        super(FlushActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, FlushActionNode node)
            throws LSCompletionException {
        // TODO: Following logic can be generalized
        ArrayList<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<Symbol> filteredWorkers = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.WORKER)
                .collect(Collectors.toList());

        List<LSCompletionItem> completionItems = new ArrayList<>(this.getCompletionItemList(filteredWorkers, context));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_DEFAULT.get()));

        return completionItems;
    }
}
