/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser rule based statement context provider.
 *
 * @since 0.992.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class InvocationArgsContextProvider extends AbstractCompletionProvider {

    public InvocationArgsContextProvider() {
        this.attachmentPoints.add(InvocationArgsContextProvider.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) throws LSCompletionException {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

        if (this.isAnnotationAccessExpression(context)) {
            return this.getProvider(AnnotationAccessExpressionContextProvider.class).getCompletions(context);
        }

        if (invocationOrDelimiterTokenType > -1) {
            Either<List<CompletionItem>, List<SymbolInfo>> filtered = SymbolFilters
                    .get(DelimiterBasedContentFilter.class).filterItems(context);
            return this.getCompletionItemList(filtered, context);
        }
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
        visibleSymbols.removeIf(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                    && CommonUtil.isValidInvokableSymbol(bSymbol))
                    || (bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED));
        });
        completionItems.addAll(getCompletionItemList(visibleSymbols, context));
        completionItems.addAll(this.getPackagesCompletionItems(context));
        // Add the untaint keyword
        CompletionItem untaintKeyword = Snippet.KW_UNTAINT.get().build(context);
        completionItems.add(untaintKeyword);

        return completionItems;
    }
}

